package com.example.util

import androidx.room.withTransaction
import com.example.data.db.AppDatabase
import com.example.data.model.AttendanceRecord
import com.example.data.model.BatchStudentCrossRef
import com.example.data.model.CoachingBatch
import com.example.data.model.FeePayment
import com.example.data.model.HomeTuition
import com.example.data.model.MonthlyFeeDue
import com.example.data.model.Student
import com.example.data.model.TeacherProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BackupManager {

    const val BACKUP_VERSION = 1
    const val APP_IDENTIFIER = "TuitionManagerBD"

    sealed class ValidationResult {
        data class Success(
            val version: Int,
            val exportTimestamp: Long,
            val exportDateFormatted: String,
            val studentCount: Int,
            val batchCount: Int,
            val enrollmentCount: Int,
            val homeTuitionCount: Int,
            val attendanceCount: Int,
            val paymentCount: Int,
            val dueCount: Int,
            val isLegacy: Boolean,
            val rawJson: String
        ) : ValidationResult()

        data class Error(val message: String) : ValidationResult()
    }

    suspend fun generateBackupJson(database: AppDatabase): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        val timestamp = System.currentTimeMillis()
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date(timestamp))

        root.put("backupVersion", BACKUP_VERSION)
        root.put("appIdentifier", APP_IDENTIFIER)
        root.put("exportTimestamp", timestamp)
        root.put("exportDateIso", isoFormat)
        root.put("appVersion", "1.0")

        val dataObj = JSONObject()

        // 1. Profile
        val prof = database.teacherProfileDao().getProfileSync() ?: TeacherProfile()
        dataObj.put("profile", JSONObject().apply {
            put("id", prof.id)
            put("name", prof.name)
            put("coachingName", prof.coachingName)
            put("phone", prof.phone)
            put("address", prof.address)
            put("language", prof.language)
        })

        // 2. Students
        val students = database.studentDao().getAllStudents().first()
        val sArray = JSONArray()
        students.forEach { s ->
            sArray.put(JSONObject().apply {
                put("id", s.id)
                put("name", s.name)
                put("studentId", s.studentId)
                put("className", s.className)
                put("guardianName", s.guardianName)
                put("guardianPhone", s.guardianPhone)
                put("subjects", s.subjects)
                put("monthlyFee", s.monthlyFee)
                put("tuitionType", s.tuitionType)
                put("notes", s.notes)
                put("isArchived", s.isArchived)
                put("createdAt", s.createdAt)
            })
        }
        dataObj.put("students", sArray)

        // 3. Batches
        val batches = database.coachingBatchDao().getAllBatches().first()
        val bArray = JSONArray()
        batches.forEach { b ->
            bArray.put(JSONObject().apply {
                put("id", b.id)
                put("batchName", b.batchName)
                put("className", b.className)
                put("subject", b.subject)
                put("daysOfWeek", b.daysOfWeek)
                put("startTime", b.startTime)
                put("endTime", b.endTime)
                put("monthlyFee", b.monthlyFee)
                put("createdAt", b.createdAt)
            })
        }
        dataObj.put("batches", bArray)

        // 4. Batch Student CrossRefs (Enrollments)
        val crossRefs = database.coachingBatchDao().getAllCrossRefs().first()
        val refArray = JSONArray()
        crossRefs.forEach { r ->
            refArray.put(JSONObject().apply {
                put("batchId", r.batchId)
                put("studentId", r.studentId)
                put("joinedAt", r.joinedAt)
            })
        }
        dataObj.put("batchCrossRefs", refArray)

        // 5. Home Tuitions
        val homeTuitions = database.homeTuitionDao().getAllHomeTuitions().first()
        val htArray = JSONArray()
        homeTuitions.forEach { ht ->
            htArray.put(JSONObject().apply {
                put("id", ht.id)
                put("studentId", ht.studentId)
                put("subject", ht.subject)
                put("address", ht.address)
                put("guardianPhone", ht.guardianPhone)
                put("daysOfWeek", ht.daysOfWeek)
                put("startTime", ht.startTime)
                put("endTime", ht.endTime)
                put("monthlyFee", ht.monthlyFee)
                put("createdAt", ht.createdAt)
            })
        }
        dataObj.put("homeTuitions", htArray)

        // 6. Attendance Records
        val attendance = database.attendanceDao().getAllAttendance().first()
        val attArray = JSONArray()
        attendance.forEach { a ->
            attArray.put(JSONObject().apply {
                put("id", a.id)
                put("studentId", a.studentId)
                put("sessionType", a.sessionType)
                put("sessionId", a.sessionId)
                put("date", a.date)
                put("status", a.status)
                put("remarks", a.remarks)
                put("recordedAt", a.recordedAt)
            })
        }
        dataObj.put("attendance", attArray)

        // 7. Fee Payments
        val payments = database.feeDao().getAllPayments().first()
        val payArray = JSONArray()
        payments.forEach { p ->
            payArray.put(JSONObject().apply {
                put("id", p.id)
                put("studentId", p.studentId)
                put("monthYear", p.monthYear)
                put("amount", p.amount)
                put("paymentDate", p.paymentDate)
                put("paymentMethod", p.paymentMethod)
                put("notes", p.notes)
                put("createdAt", p.createdAt)
            })
        }
        dataObj.put("payments", payArray)

        // 8. Monthly Fee Dues
        val dues = database.feeDao().getAllDues().first()
        val dueArray = JSONArray()
        dues.forEach { d ->
            dueArray.put(JSONObject().apply {
                put("id", d.id)
                put("studentId", d.studentId)
                put("monthYear", d.monthYear)
                put("dueAmount", d.dueAmount)
                put("createdAt", d.createdAt)
            })
        }
        dataObj.put("dues", dueArray)

        root.put("data", dataObj)
        root.toString(2)
    }

    fun validateBackupJson(jsonString: String, isBengali: Boolean = true): ValidationResult {
        return try {
            val trimmed = jsonString.trim()
            if (trimmed.isEmpty() || !trimmed.startsWith("{")) {
                return ValidationResult.Error(Localization.get("invalid_backup_format", isBengali))
            }

            val root = JSONObject(trimmed)

            val isLegacy = !root.has("backupVersion") && root.has("students")
            val version = if (isLegacy) 1 else root.optInt("backupVersion", 1)

            if (version > BACKUP_VERSION) {
                return ValidationResult.Error(Localization.get("unsupported_backup_version", isBengali))
            }

            val appIdentifier = root.optString("appIdentifier", "")
            if (!isLegacy && appIdentifier.isNotBlank() && appIdentifier != APP_IDENTIFIER) {
                return ValidationResult.Error(Localization.get("invalid_backup_format", isBengali))
            }

            val dataObj = if (root.has("data")) root.getJSONObject("data") else root

            val students = dataObj.optJSONArray("students")
            val batches = dataObj.optJSONArray("batches")
            val refs = dataObj.optJSONArray("batchCrossRefs")
            val homeTuitions = dataObj.optJSONArray("homeTuitions")
            val attendance = dataObj.optJSONArray("attendance")
            val payments = dataObj.optJSONArray("payments")
            val dues = dataObj.optJSONArray("dues")

            val studentCount = students?.length() ?: 0
            val batchCount = batches?.length() ?: 0
            val refCount = refs?.length() ?: 0
            val homeTuitionCount = homeTuitions?.length() ?: 0
            val attendanceCount = attendance?.length() ?: 0
            val paymentCount = payments?.length() ?: 0
            val dueCount = dues?.length() ?: 0

            val timestamp = root.optLong("exportTimestamp", System.currentTimeMillis())
            val dateFormatted = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(Date(timestamp))

            ValidationResult.Success(
                version = version,
                exportTimestamp = timestamp,
                exportDateFormatted = dateFormatted,
                studentCount = studentCount,
                batchCount = batchCount,
                enrollmentCount = refCount,
                homeTuitionCount = homeTuitionCount,
                attendanceCount = attendanceCount,
                paymentCount = paymentCount,
                dueCount = dueCount,
                isLegacy = isLegacy,
                rawJson = trimmed
            )
        } catch (e: Exception) {
            ValidationResult.Error("${Localization.get("invalid_backup_format", isBengali)} (${e.message ?: "Parsing error"})")
        }
    }

    suspend fun restoreReplaceAll(database: AppDatabase, jsonString: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            val dataObj = if (root.has("data")) root.getJSONObject("data") else root

            database.withTransaction {
                // Clear all existing data atomically
                database.clearAllTables()

                // 1. Profile
                if (dataObj.has("profile")) {
                    val pObj = dataObj.getJSONObject("profile")
                    database.teacherProfileDao().insertOrUpdate(
                        TeacherProfile(
                            id = pObj.optInt("id", 1),
                            name = pObj.optString("name", "মোঃ রফিকুল ইসলাম"),
                            coachingName = pObj.optString("coachingName", "টিউটর পয়েন্ট"),
                            phone = pObj.optString("phone", ""),
                            address = pObj.optString("address", ""),
                            language = pObj.optString("language", "bn")
                        )
                    )
                }

                // 2. Students (preserves IDs for relational fidelity)
                val sArray = dataObj.optJSONArray("students")
                if (sArray != null) {
                    for (i in 0 until sArray.length()) {
                        val o = sArray.getJSONObject(i)
                        database.studentDao().insertStudent(
                            Student(
                                id = o.optLong("id", 0L),
                                name = o.optString("name", ""),
                                studentId = o.optString("studentId", ""),
                                className = o.optString("className", ""),
                                guardianName = o.optString("guardianName", ""),
                                guardianPhone = o.optString("guardianPhone", ""),
                                subjects = o.optString("subjects", ""),
                                monthlyFee = o.optDouble("monthlyFee", 0.0),
                                tuitionType = o.optString("tuitionType", "COACHING"),
                                notes = o.optString("notes", ""),
                                isArchived = o.optBoolean("isArchived", false),
                                createdAt = o.optLong("createdAt", System.currentTimeMillis())
                            )
                        )
                    }
                }

                // 3. Batches
                val bArray = dataObj.optJSONArray("batches")
                if (bArray != null) {
                    for (i in 0 until bArray.length()) {
                        val o = bArray.getJSONObject(i)
                        database.coachingBatchDao().insertBatch(
                            CoachingBatch(
                                id = o.optLong("id", 0L),
                                batchName = o.optString("batchName", ""),
                                className = o.optString("className", ""),
                                subject = o.optString("subject", ""),
                                daysOfWeek = o.optString("daysOfWeek", ""),
                                startTime = o.optString("startTime", "08:00"),
                                endTime = o.optString("endTime", "09:30"),
                                monthlyFee = o.optDouble("monthlyFee", 0.0),
                                createdAt = o.optLong("createdAt", System.currentTimeMillis())
                            )
                        )
                    }
                }

                // 4. Enrollments
                val refArray = dataObj.optJSONArray("batchCrossRefs")
                if (refArray != null) {
                    for (i in 0 until refArray.length()) {
                        val o = refArray.getJSONObject(i)
                        database.coachingBatchDao().addStudentToBatch(
                            BatchStudentCrossRef(
                                batchId = o.optLong("batchId"),
                                studentId = o.optLong("studentId"),
                                joinedAt = o.optLong("joinedAt", System.currentTimeMillis())
                            )
                        )
                    }
                }

                // 5. Home Tuitions
                val htArray = dataObj.optJSONArray("homeTuitions")
                if (htArray != null) {
                    for (i in 0 until htArray.length()) {
                        val o = htArray.getJSONObject(i)
                        database.homeTuitionDao().insertHomeTuition(
                            HomeTuition(
                                id = o.optLong("id", 0L),
                                studentId = o.optLong("studentId"),
                                subject = o.optString("subject", ""),
                                address = o.optString("address", ""),
                                guardianPhone = o.optString("guardianPhone", ""),
                                daysOfWeek = o.optString("daysOfWeek", ""),
                                startTime = o.optString("startTime", "16:00"),
                                endTime = o.optString("endTime", "17:30"),
                                monthlyFee = o.optDouble("monthlyFee", 0.0),
                                createdAt = o.optLong("createdAt", System.currentTimeMillis())
                            )
                        )
                    }
                }

                // 6. Attendance Records
                val attArray = dataObj.optJSONArray("attendance")
                if (attArray != null) {
                    for (i in 0 until attArray.length()) {
                        val o = attArray.getJSONObject(i)
                        database.attendanceDao().insertOrUpdateAttendance(
                            AttendanceRecord(
                                id = o.optLong("id", 0L),
                                studentId = o.optLong("studentId"),
                                sessionType = o.optString("sessionType", "COACHING"),
                                sessionId = o.optLong("sessionId"),
                                date = o.optString("date", ""),
                                status = o.optString("status", "PRESENT"),
                                remarks = o.optString("remarks", ""),
                                recordedAt = o.optLong("recordedAt", System.currentTimeMillis())
                            )
                        )
                    }
                }

                // 7. Payments
                val payArray = dataObj.optJSONArray("payments")
                if (payArray != null) {
                    for (i in 0 until payArray.length()) {
                        val o = payArray.getJSONObject(i)
                        database.feeDao().insertPayment(
                            FeePayment(
                                id = o.optLong("id", 0L),
                                studentId = o.optLong("studentId"),
                                monthYear = o.optString("monthYear", ""),
                                amount = o.optDouble("amount", 0.0),
                                paymentDate = o.optString("paymentDate", ""),
                                paymentMethod = o.optString("paymentMethod", "CASH"),
                                notes = o.optString("notes", ""),
                                createdAt = o.optLong("createdAt", System.currentTimeMillis())
                            )
                        )
                    }
                }

                // 8. Dues
                val dueArray = dataObj.optJSONArray("dues")
                if (dueArray != null) {
                    for (i in 0 until dueArray.length()) {
                        val o = dueArray.getJSONObject(i)
                        database.feeDao().insertOrUpdateDue(
                            MonthlyFeeDue(
                                id = o.optLong("id", 0L),
                                studentId = o.optLong("studentId"),
                                monthYear = o.optString("monthYear", ""),
                                dueAmount = o.optDouble("dueAmount", 0.0),
                                createdAt = o.optLong("createdAt", System.currentTimeMillis())
                            )
                        )
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreMerge(database: AppDatabase, jsonString: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            val dataObj = if (root.has("data")) root.getJSONObject("data") else root

            database.withTransaction {
                // Fetch local records for matching and conflict resolution
                val localStudents = database.studentDao().getAllStudents().first()
                val localBatches = database.coachingBatchDao().getAllBatches().first()
                val localCrossRefs = database.coachingBatchDao().getAllCrossRefs().first()
                val localHomeTuitions = database.homeTuitionDao().getAllHomeTuitions().first()
                val localAttendance = database.attendanceDao().getAllAttendance().first()
                val localPayments = database.feeDao().getAllPayments().first()
                val localDues = database.feeDao().getAllDues().first()

                val studentIdMap = mutableMapOf<Long, Long>()
                val batchIdMap = mutableMapOf<Long, Long>()

                val usedLocalStudentIds = localStudents.map { it.id }.toMutableSet()
                val usedLocalBatchIds = localBatches.map { it.id }.toMutableSet()

                // 1. Merge Students
                val sArray = dataObj.optJSONArray("students")
                if (sArray != null) {
                    for (i in 0 until sArray.length()) {
                        val o = sArray.getJSONObject(i)
                        val incomingId = o.optLong("id", 0L)
                        val incomingName = o.optString("name", "").trim()
                        val incomingRoll = o.optString("studentId", "").trim()
                        val incomingClass = o.optString("className", "").trim()
                        val incomingPhone = o.optString("guardianPhone", "").trim()

                        // Match criteria:
                        // A: Same id and same name
                        // B: Same non-empty Roll and Class
                        // C: Same name (case-insensitive) and same phone
                        val matchedStudent = localStudents.find { local ->
                            (local.id == incomingId && local.name.equals(incomingName, ignoreCase = true)) ||
                            (incomingRoll.isNotBlank() && incomingClass.isNotBlank() &&
                             local.studentId.equals(incomingRoll, ignoreCase = true) && local.className.equals(incomingClass, ignoreCase = true)) ||
                            (incomingPhone.isNotBlank() && local.guardianPhone == incomingPhone && local.name.equals(incomingName, ignoreCase = true))
                        }

                        if (matchedStudent != null) {
                            studentIdMap[incomingId] = matchedStudent.id
                            // Only update empty fields in local student; do not overwrite local modifications
                            var updated = matchedStudent
                            if (updated.notes.isBlank() && o.optString("notes", "").isNotBlank()) {
                                updated = updated.copy(notes = o.optString("notes"))
                            }
                            if (updated.subjects.isBlank() && o.optString("subjects", "").isNotBlank()) {
                                updated = updated.copy(subjects = o.optString("subjects"))
                            }
                            if (updated.guardianName.isBlank() && o.optString("guardianName", "").isNotBlank()) {
                                updated = updated.copy(guardianName = o.optString("guardianName"))
                            }
                            if (updated != matchedStudent) {
                                database.studentDao().updateStudent(updated)
                            }
                        } else {
                            // Insert new student
                            val candidateId = if (incomingId > 0 && !usedLocalStudentIds.contains(incomingId)) incomingId else 0L
                            val newStudent = Student(
                                id = candidateId,
                                name = incomingName,
                                studentId = incomingRoll,
                                className = incomingClass,
                                guardianName = o.optString("guardianName", ""),
                                guardianPhone = incomingPhone,
                                subjects = o.optString("subjects", ""),
                                monthlyFee = o.optDouble("monthlyFee", 0.0),
                                tuitionType = o.optString("tuitionType", "COACHING"),
                                notes = o.optString("notes", ""),
                                isArchived = o.optBoolean("isArchived", false),
                                createdAt = o.optLong("createdAt", System.currentTimeMillis())
                            )
                            val insertedId = database.studentDao().insertStudent(newStudent)
                            studentIdMap[incomingId] = insertedId
                            usedLocalStudentIds.add(insertedId)
                        }
                    }
                }

                // 2. Merge Batches
                val bArray = dataObj.optJSONArray("batches")
                if (bArray != null) {
                    for (i in 0 until bArray.length()) {
                        val o = bArray.getJSONObject(i)
                        val incomingId = o.optLong("id", 0L)
                        val incomingBatchName = o.optString("batchName", "").trim()
                        val incomingClassName = o.optString("className", "").trim()

                        val matchedBatch = localBatches.find { local ->
                            (local.id == incomingId && local.batchName.equals(incomingBatchName, ignoreCase = true)) ||
                            (local.batchName.equals(incomingBatchName, ignoreCase = true) && local.className.equals(incomingClassName, ignoreCase = true))
                        }

                        if (matchedBatch != null) {
                            batchIdMap[incomingId] = matchedBatch.id
                        } else {
                            val candidateId = if (incomingId > 0 && !usedLocalBatchIds.contains(incomingId)) incomingId else 0L
                            val newBatch = CoachingBatch(
                                id = candidateId,
                                batchName = incomingBatchName,
                                className = incomingClassName,
                                subject = o.optString("subject", ""),
                                daysOfWeek = o.optString("daysOfWeek", ""),
                                startTime = o.optString("startTime", "08:00"),
                                endTime = o.optString("endTime", "09:30"),
                                monthlyFee = o.optDouble("monthlyFee", 0.0),
                                createdAt = o.optLong("createdAt", System.currentTimeMillis())
                            )
                            val insertedId = database.coachingBatchDao().insertBatch(newBatch)
                            batchIdMap[incomingId] = insertedId
                            usedLocalBatchIds.add(insertedId)
                        }
                    }
                }

                // 3. Merge Batch Enrollments (BatchStudentCrossRef)
                val refArray = dataObj.optJSONArray("batchCrossRefs")
                if (refArray != null) {
                    for (i in 0 until refArray.length()) {
                        val o = refArray.getJSONObject(i)
                        val rawBId = o.optLong("batchId")
                        val rawSId = o.optLong("studentId")
                        val targetBId = batchIdMap[rawBId] ?: rawBId
                        val targetSId = studentIdMap[rawSId] ?: rawSId

                        // Insert only if foreign keys exist and pairing does not already exist
                        if (usedLocalBatchIds.contains(targetBId) && usedLocalStudentIds.contains(targetSId)) {
                            val alreadyEnrolled = localCrossRefs.any { it.batchId == targetBId && it.studentId == targetSId }
                            if (!alreadyEnrolled) {
                                database.coachingBatchDao().addStudentToBatch(
                                    BatchStudentCrossRef(
                                        batchId = targetBId,
                                        studentId = targetSId,
                                        joinedAt = o.optLong("joinedAt", System.currentTimeMillis())
                                    )
                                )
                            }
                        }
                    }
                }

                // 4. Merge Home Tuitions
                val htArray = dataObj.optJSONArray("homeTuitions")
                if (htArray != null) {
                    for (i in 0 until htArray.length()) {
                        val o = htArray.getJSONObject(i)
                        val rawSId = o.optLong("studentId")
                        val targetSId = studentIdMap[rawSId] ?: rawSId

                        if (usedLocalStudentIds.contains(targetSId)) {
                            val incomingSubject = o.optString("subject", "").trim()
                            val exists = localHomeTuitions.any { it.studentId == targetSId && it.subject.equals(incomingSubject, ignoreCase = true) }
                            if (!exists) {
                                database.homeTuitionDao().insertHomeTuition(
                                    HomeTuition(
                                        id = 0L,
                                        studentId = targetSId,
                                        subject = incomingSubject,
                                        address = o.optString("address", ""),
                                        guardianPhone = o.optString("guardianPhone", ""),
                                        daysOfWeek = o.optString("daysOfWeek", ""),
                                        startTime = o.optString("startTime", "16:00"),
                                        endTime = o.optString("endTime", "17:30"),
                                        monthlyFee = o.optDouble("monthlyFee", 0.0),
                                        createdAt = o.optLong("createdAt", System.currentTimeMillis())
                                    )
                                )
                            }
                        }
                    }
                }

                // 5. Merge Attendance Records
                val attArray = dataObj.optJSONArray("attendance")
                if (attArray != null) {
                    for (i in 0 until attArray.length()) {
                        val o = attArray.getJSONObject(i)
                        val rawSId = o.optLong("studentId")
                        val targetSId = studentIdMap[rawSId] ?: rawSId
                        val sessionType = o.optString("sessionType", "COACHING")
                        val rawSessionId = o.optLong("sessionId")
                        val targetSessionId = if (sessionType == "COACHING") (batchIdMap[rawSessionId] ?: rawSessionId) else rawSessionId
                        val date = o.optString("date", "")

                        // Attendance uniqueness is: (studentId, sessionType, sessionId, date)
                        val localExists = localAttendance.any {
                            it.studentId == targetSId && it.sessionType == sessionType && it.sessionId == targetSessionId && it.date == date
                        }
                        if (!localExists && usedLocalStudentIds.contains(targetSId)) {
                            database.attendanceDao().insertOrUpdateAttendance(
                                AttendanceRecord(
                                    id = 0L,
                                    studentId = targetSId,
                                    sessionType = sessionType,
                                    sessionId = targetSessionId,
                                    date = date,
                                    status = o.optString("status", "PRESENT"),
                                    remarks = o.optString("remarks", ""),
                                    recordedAt = o.optLong("recordedAt", System.currentTimeMillis())
                                )
                            )
                        }
                    }
                }

                // 6. Merge Fee Payments (with deduplication)
                val payArray = dataObj.optJSONArray("payments")
                if (payArray != null) {
                    for (i in 0 until payArray.length()) {
                        val o = payArray.getJSONObject(i)
                        val rawSId = o.optLong("studentId")
                        val targetSId = studentIdMap[rawSId] ?: rawSId
                        val monthYear = o.optString("monthYear", "")
                        val amount = o.optDouble("amount", 0.0)
                        val paymentDate = o.optString("paymentDate", "")
                        val paymentMethod = o.optString("paymentMethod", "CASH")

                        // Deduplication: prevent double-inserting the exact payment record
                        val duplicate = localPayments.any {
                            it.studentId == targetSId && it.monthYear == monthYear &&
                            it.amount == amount && it.paymentDate == paymentDate && it.paymentMethod == paymentMethod
                        }
                        if (!duplicate && usedLocalStudentIds.contains(targetSId)) {
                            database.feeDao().insertPayment(
                                FeePayment(
                                    id = 0L,
                                    studentId = targetSId,
                                    monthYear = monthYear,
                                    amount = amount,
                                    paymentDate = paymentDate,
                                    paymentMethod = paymentMethod,
                                    notes = o.optString("notes", ""),
                                    createdAt = o.optLong("createdAt", System.currentTimeMillis())
                                )
                            )
                        }
                    }
                }

                // 7. Merge Monthly Fee Dues
                val dueArray = dataObj.optJSONArray("dues")
                if (dueArray != null) {
                    for (i in 0 until dueArray.length()) {
                        val o = dueArray.getJSONObject(i)
                        val rawSId = o.optLong("studentId")
                        val targetSId = studentIdMap[rawSId] ?: rawSId
                        val monthYear = o.optString("monthYear", "")
                        val dueAmount = o.optDouble("dueAmount", 0.0)

                        // If local override already exists, preserve local override
                        val localOverrideExists = localDues.any { it.studentId == targetSId && it.monthYear == monthYear }
                        if (!localOverrideExists && usedLocalStudentIds.contains(targetSId)) {
                            database.feeDao().insertOrUpdateDue(
                                MonthlyFeeDue(
                                    id = 0L,
                                    studentId = targetSId,
                                    monthYear = monthYear,
                                    dueAmount = dueAmount,
                                    createdAt = o.optLong("createdAt", System.currentTimeMillis())
                                )
                            )
                        }
                    }
                }

                // 8. Teacher Profile
                val localProfile = database.teacherProfileDao().getProfileSync()
                if ((localProfile == null || localProfile.name.isBlank()) && dataObj.has("profile")) {
                    val pObj = dataObj.getJSONObject("profile")
                    database.teacherProfileDao().insertOrUpdate(
                        TeacherProfile(
                            id = 1,
                            name = pObj.optString("name", "মোঃ রফিকুল ইসলাম"),
                            coachingName = pObj.optString("coachingName", "টিউটর পয়েন্ট"),
                            phone = pObj.optString("phone", ""),
                            address = pObj.optString("address", ""),
                            language = pObj.optString("language", "bn")
                        )
                    )
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
