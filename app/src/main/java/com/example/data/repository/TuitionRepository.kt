package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.model.AttendanceRecord
import com.example.data.model.BatchStudentCrossRef
import com.example.data.model.CoachingBatch
import com.example.data.model.FeePayment
import com.example.data.model.HomeTuition
import com.example.data.model.MonthlyFeeDue
import com.example.data.model.Student
import com.example.data.model.TeacherProfile
import kotlinx.coroutines.flow.Flow

class TuitionRepository(private val database: AppDatabase) {

    private val profileDao = database.teacherProfileDao()
    private val studentDao = database.studentDao()
    private val batchDao = database.coachingBatchDao()
    private val tuitionDao = database.homeTuitionDao()
    private val attendanceDao = database.attendanceDao()
    private val feeDao = database.feeDao()

    // Profile
    val profile: Flow<TeacherProfile?> = profileDao.getProfile()
    suspend fun saveProfile(profile: TeacherProfile) = profileDao.insertOrUpdate(profile)

    // Students
    val activeStudents: Flow<List<Student>> = studentDao.getAllActiveStudents()
    val archivedStudents: Flow<List<Student>> = studentDao.getArchivedStudents()
    val allStudents: Flow<List<Student>> = studentDao.getAllStudents()

    fun getStudentById(id: Long): Flow<Student?> = studentDao.getStudentById(id)
    suspend fun getStudentByIdSync(id: Long): Student? = studentDao.getStudentByIdSync(id)

    suspend fun saveStudent(student: Student): Long {
        return if (student.id == 0L) {
            val newId = studentDao.insertStudent(student)
            // Initialize current month's due for the new student
            val currentMonth = com.example.util.BengaliDateUtils.getCurrentMonthYear()
            if (student.monthlyFee > 0) {
                feeDao.insertOrUpdateDue(
                    MonthlyFeeDue(
                        studentId = newId,
                        monthYear = currentMonth,
                        dueAmount = student.monthlyFee
                    )
                )
            }
            newId
        } else {
            studentDao.updateStudent(student)
            student.id
        }
    }

    suspend fun setArchived(id: Long, archived: Boolean) = studentDao.setArchived(id, archived)
    suspend fun deleteStudent(student: Student) = studentDao.deleteStudent(student)

    // Coaching Batches
    val allBatches: Flow<List<CoachingBatch>> = batchDao.getAllBatches()
    val allCrossRefs: Flow<List<BatchStudentCrossRef>> = batchDao.getAllCrossRefs()

    suspend fun saveBatch(batch: CoachingBatch): Long {
        return if (batch.id == 0L) {
            batchDao.insertBatch(batch)
        } else {
            batchDao.updateBatch(batch)
            batch.id
        }
    }

    suspend fun deleteBatch(batch: CoachingBatch) = batchDao.deleteBatch(batch)

    suspend fun addStudentToBatch(batchId: Long, studentId: Long) {
        batchDao.addStudentToBatch(BatchStudentCrossRef(batchId = batchId, studentId = studentId))
    }

    suspend fun removeStudentFromBatch(batchId: Long, studentId: Long) {
        batchDao.removeStudentFromBatch(batchId, studentId)
    }

    fun getStudentsForBatch(batchId: Long): Flow<List<Student>> = batchDao.getStudentsForBatch(batchId)
    suspend fun getStudentsForBatchSync(batchId: Long): List<Student> = batchDao.getStudentsForBatchSync(batchId)
    fun getBatchesForStudent(studentId: Long): Flow<List<CoachingBatch>> = batchDao.getBatchesForStudent(studentId)

    // Home Tuition
    val allHomeTuitions: Flow<List<HomeTuition>> = tuitionDao.getAllHomeTuitions()

    suspend fun saveHomeTuition(tuition: HomeTuition): Long {
        return if (tuition.id == 0L) {
            tuitionDao.insertHomeTuition(tuition)
        } else {
            tuitionDao.updateHomeTuition(tuition)
            tuition.id
        }
    }

    suspend fun deleteHomeTuition(tuition: HomeTuition) = tuitionDao.deleteHomeTuition(tuition)
    fun getHomeTuitionsForStudent(studentId: Long): Flow<List<HomeTuition>> = tuitionDao.getHomeTuitionsForStudent(studentId)

    // Attendance
    val allAttendance: Flow<List<AttendanceRecord>> = attendanceDao.getAllAttendance()

    fun getAttendanceForSessionAndDate(sessionId: Long, sessionType: String, date: String): Flow<List<AttendanceRecord>> {
        return attendanceDao.getAttendanceForSessionAndDate(sessionId, sessionType, date)
    }

    fun getAttendanceForStudent(studentId: Long): Flow<List<AttendanceRecord>> {
        return attendanceDao.getAttendanceForStudent(studentId)
    }

    suspend fun recordAttendance(record: AttendanceRecord) {
        attendanceDao.insertOrUpdateAttendance(record)
    }

    suspend fun recordAttendanceList(records: List<AttendanceRecord>) {
        attendanceDao.insertOrUpdateAll(records)
    }

    // Fees
    val allPayments: Flow<List<FeePayment>> = feeDao.getAllPayments()
    val allDues: Flow<List<MonthlyFeeDue>> = feeDao.getAllDues()

    fun getPaymentsForStudent(studentId: Long): Flow<List<FeePayment>> = feeDao.getPaymentsForStudent(studentId)
    fun getDuesForStudent(studentId: Long): Flow<List<MonthlyFeeDue>> = feeDao.getDuesForStudent(studentId)

    suspend fun recordPayment(payment: FeePayment): Long {
        return feeDao.insertPayment(payment)
    }

    suspend fun deletePayment(id: Long) = feeDao.deletePayment(id)

    suspend fun setMonthlyDue(due: MonthlyFeeDue) {
        feeDao.insertOrUpdateDue(due)
    }

    // Demo Data
    suspend fun loadDemoData() = com.example.util.DemoDataGenerator.populateDemoData(database)
    suspend fun clearDatabase() = com.example.util.DemoDataGenerator.clearAllData(database)
}
