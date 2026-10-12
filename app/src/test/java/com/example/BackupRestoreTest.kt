package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.model.AttendanceRecord
import com.example.data.model.BatchStudentCrossRef
import com.example.data.model.CoachingBatch
import com.example.data.model.FeePayment
import com.example.data.model.HomeTuition
import com.example.data.model.MonthlyFeeDue
import com.example.data.model.Student
import com.example.data.model.TeacherProfile
import com.example.util.BackupManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BackupRestoreTest {

    private lateinit var database: AppDatabase
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testExportContainsAllEightEntities() = runBlocking {
        // 1. Profile
        database.teacherProfileDao().insertOrUpdate(
            TeacherProfile(id = 1, name = "Test Teacher", phone = "01700000000")
        )

        // 2. Student
        val sId = database.studentDao().insertStudent(
            Student(id = 1, name = "Student One", studentId = "101", className = "Class 10", monthlyFee = 1500.0)
        )

        // 3. Batch
        val bId = database.coachingBatchDao().insertBatch(
            CoachingBatch(id = 1, batchName = "Math Batch", className = "Class 10")
        )

        // 4. Enrollment CrossRef
        database.coachingBatchDao().addStudentToBatch(
            BatchStudentCrossRef(batchId = bId, studentId = sId)
        )

        // 5. Home Tuition
        database.homeTuitionDao().insertHomeTuition(
            HomeTuition(id = 1, studentId = sId, subject = "English")
        )

        // 6. Attendance Record
        database.attendanceDao().insertOrUpdateAttendance(
            AttendanceRecord(id = 1, studentId = sId, sessionType = "COACHING", sessionId = bId, date = "2026-10-10", status = "PRESENT")
        )

        // 7. Payment
        database.feeDao().insertPayment(
            FeePayment(id = 1, studentId = sId, monthYear = "2026-10", amount = 1500.0, paymentDate = "2026-10-05")
        )

        // 8. Due override
        database.feeDao().insertOrUpdateDue(
            MonthlyFeeDue(id = 1, studentId = sId, monthYear = "2026-10", dueAmount = 1500.0)
        )

        val json = BackupManager.generateBackupJson(database)
        val root = JSONObject(json)

        assertEquals(1, root.getInt("backupVersion"))
        assertEquals("TuitionManagerBD", root.getString("appIdentifier"))
        assertTrue(root.has("exportTimestamp"))

        val data = root.getJSONObject("data")
        assertEquals("Test Teacher", data.getJSONObject("profile").getString("name"))
        assertEquals(1, data.getJSONArray("students").length())
        assertEquals(1, data.getJSONArray("batches").length())
        assertEquals(1, data.getJSONArray("batchCrossRefs").length())
        assertEquals(1, data.getJSONArray("homeTuitions").length())
        assertEquals(1, data.getJSONArray("attendance").length()) // Attendance included!
        assertEquals(1, data.getJSONArray("payments").length())
        assertEquals(1, data.getJSONArray("dues").length())
    }

    @Test
    fun testValidationOfValidBackup() {
        val json = """
            {
                "backupVersion": 1,
                "appIdentifier": "TuitionManagerBD",
                "exportTimestamp": 1728637200000,
                "data": {
                    "students": [{"id": 1, "name": "Tanvir"}],
                    "batches": [{"id": 1, "batchName": "Morning Batch"}],
                    "payments": [{"id": 1, "studentId": 1, "amount": 1000.0}]
                }
            }
        """.trimIndent()

        val result = BackupManager.validateBackupJson(json, isBengali = true)
        assertTrue(result is BackupManager.ValidationResult.Success)
        val success = result as BackupManager.ValidationResult.Success
        assertEquals(1, success.version)
        assertEquals(1, success.studentCount)
        assertEquals(1, success.batchCount)
        assertEquals(1, success.paymentCount)
    }

    @Test
    fun testValidationOfInvalidJson() {
        val result = BackupManager.validateBackupJson("not a json string", isBengali = true)
        assertTrue(result is BackupManager.ValidationResult.Error)
    }

    @Test
    fun testValidationOfUnsupportedVersion() {
        val json = """
            {
                "backupVersion": 99,
                "appIdentifier": "TuitionManagerBD",
                "data": {}
            }
        """.trimIndent()

        val result = BackupManager.validateBackupJson(json, isBengali = true)
        assertTrue(result is BackupManager.ValidationResult.Error)
    }

    @Test
    fun testReplaceAllRestoresCleanly() = runBlocking {
        // Initial state: 1 student
        database.studentDao().insertStudent(Student(id = 1, name = "Original Student"))
        val backupJson = BackupManager.generateBackupJson(database)

        // Modify local state by adding student 2
        database.studentDao().insertStudent(Student(id = 2, name = "Temporary Student"))
        assertEquals(2, database.studentDao().getAllStudents().first().size)

        // Restore Replace All
        val res = BackupManager.restoreReplaceAll(database, backupJson)
        assertTrue(res.isSuccess)

        val restoredStudents = database.studentDao().getAllStudents().first()
        assertEquals(1, restoredStudents.size)
        assertEquals("Original Student", restoredStudents[0].name)
    }

    @Test
    fun testMergePreservesUnrelatedLocalRecordsAndAvoidsDuplicatePayments() = runBlocking {
        // Local state: Local Student A with 1 payment
        val sAId = database.studentDao().insertStudent(Student(id = 1, name = "Local Student A", studentId = "101", className = "10"))
        database.feeDao().insertPayment(
            FeePayment(id = 1, studentId = sAId, monthYear = "2026-10", amount = 1000.0, paymentDate = "2026-10-01", paymentMethod = "CASH")
        )

        // Incoming backup has: Local Student A with same payment P1, PLUS New Student B with payment P2
        val backupJson = """
            {
                "backupVersion": 1,
                "appIdentifier": "TuitionManagerBD",
                "data": {
                    "students": [
                        {"id": 1, "name": "Local Student A", "studentId": "101", "className": "10", "monthlyFee": 1000.0},
                        {"id": 5, "name": "Backup Student B", "studentId": "105", "className": "10", "monthlyFee": 1200.0}
                    ],
                    "payments": [
                        {"id": 1, "studentId": 1, "monthYear": "2026-10", "amount": 1000.0, "paymentDate": "2026-10-01", "paymentMethod": "CASH"},
                        {"id": 2, "studentId": 5, "monthYear": "2026-10", "amount": 1200.0, "paymentDate": "2026-10-02", "paymentMethod": "BKASH"}
                    ]
                }
            }
        """.trimIndent()

        val mergeRes = BackupManager.restoreMerge(database, backupJson)
        assertTrue(mergeRes.isSuccess)

        // Both students must exist!
        val allStudents = database.studentDao().getAllStudents().first()
        assertEquals(2, allStudents.size)

        // Payments: Payment P1 must not be duplicated! Only P1 and P2 should exist (total 2 payments)
        val allPayments = database.feeDao().getAllPayments().first()
        assertEquals(2, allPayments.size)
    }
}
