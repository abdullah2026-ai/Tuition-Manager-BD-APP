package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AttendanceRecord
import com.example.data.model.BatchStudentCrossRef
import com.example.data.model.CoachingBatch
import com.example.data.model.FeePayment
import com.example.data.model.HomeTuition
import com.example.data.model.MonthlyFeeDue
import com.example.data.model.Student
import com.example.data.model.TeacherProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface TeacherProfileDao {
    @Query("SELECT * FROM teacher_profile WHERE id = 1 LIMIT 1")
    fun getProfile(): Flow<TeacherProfile?>

    @Query("SELECT * FROM teacher_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfileSync(): TeacherProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(profile: TeacherProfile)
}

@Dao
interface StudentDao {
    @Query("SELECT * FROM students WHERE isArchived = 0 ORDER BY name ASC")
    fun getAllActiveStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE isArchived = 1 ORDER BY name ASC")
    fun getArchivedStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students ORDER BY name ASC")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
    fun getStudentById(id: Long): Flow<Student?>

    @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
    suspend fun getStudentByIdSync(id: Long): Student?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Update
    suspend fun updateStudent(student: Student)

    @Query("UPDATE students SET isArchived = :archived WHERE id = :id")
    suspend fun setArchived(id: Long, archived: Boolean)

    @Delete
    suspend fun deleteStudent(student: Student)
}

@Dao
interface CoachingBatchDao {
    @Query("SELECT * FROM coaching_batches ORDER BY startTime ASC")
    fun getAllBatches(): Flow<List<CoachingBatch>>

    @Query("SELECT * FROM coaching_batches WHERE id = :id LIMIT 1")
    fun getBatchById(id: Long): Flow<CoachingBatch?>

    @Query("SELECT * FROM coaching_batches WHERE id = :id LIMIT 1")
    suspend fun getBatchByIdSync(id: Long): CoachingBatch?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: CoachingBatch): Long

    @Update
    suspend fun updateBatch(batch: CoachingBatch)

    @Delete
    suspend fun deleteBatch(batch: CoachingBatch)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addStudentToBatch(crossRef: BatchStudentCrossRef)

    @Query("DELETE FROM batch_students WHERE batchId = :batchId AND studentId = :studentId")
    suspend fun removeStudentFromBatch(batchId: Long, studentId: Long)

    @Query("DELETE FROM batch_students WHERE batchId = :batchId")
    suspend fun removeAllStudentsFromBatch(batchId: Long)

    @Query("""
        SELECT s.* FROM students s
        INNER JOIN batch_students bs ON s.id = bs.studentId
        WHERE bs.batchId = :batchId AND s.isArchived = 0
        ORDER BY s.name ASC
    """)
    fun getStudentsForBatch(batchId: Long): Flow<List<Student>>

    @Query("""
        SELECT s.* FROM students s
        INNER JOIN batch_students bs ON s.id = bs.studentId
        WHERE bs.batchId = :batchId AND s.isArchived = 0
        ORDER BY s.name ASC
    """)
    suspend fun getStudentsForBatchSync(batchId: Long): List<Student>

    @Query("""
        SELECT b.* FROM coaching_batches b
        INNER JOIN batch_students bs ON b.id = bs.batchId
        WHERE bs.studentId = :studentId
        ORDER BY b.batchName ASC
    """)
    fun getBatchesForStudent(studentId: Long): Flow<List<CoachingBatch>>

    @Query("SELECT * FROM batch_students")
    fun getAllCrossRefs(): Flow<List<BatchStudentCrossRef>>
}

@Dao
interface HomeTuitionDao {
    @Query("SELECT * FROM home_tuitions ORDER BY startTime ASC")
    fun getAllHomeTuitions(): Flow<List<HomeTuition>>

    @Query("SELECT * FROM home_tuitions WHERE id = :id LIMIT 1")
    fun getHomeTuitionById(id: Long): Flow<HomeTuition?>

    @Query("SELECT * FROM home_tuitions WHERE id = :id LIMIT 1")
    suspend fun getHomeTuitionByIdSync(id: Long): HomeTuition?

    @Query("SELECT * FROM home_tuitions WHERE studentId = :studentId")
    fun getHomeTuitionsForStudent(studentId: Long): Flow<List<HomeTuition>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomeTuition(tuition: HomeTuition): Long

    @Update
    suspend fun updateHomeTuition(tuition: HomeTuition)

    @Delete
    suspend fun deleteHomeTuition(tuition: HomeTuition)
}

@Dao
interface AttendanceDao {
    @Query("""
        SELECT * FROM attendance_records 
        WHERE sessionId = :sessionId AND sessionType = :sessionType AND date = :date
    """)
    fun getAttendanceForSessionAndDate(sessionId: Long, sessionType: String, date: String): Flow<List<AttendanceRecord>>

    @Query("""
        SELECT * FROM attendance_records 
        WHERE sessionId = :sessionId AND sessionType = :sessionType AND date = :date
    """)
    suspend fun getAttendanceForSessionAndDateSync(sessionId: Long, sessionType: String, date: String): List<AttendanceRecord>

    @Query("SELECT * FROM attendance_records WHERE studentId = :studentId ORDER BY date DESC")
    fun getAttendanceForStudent(studentId: Long): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records ORDER BY date DESC")
    fun getAllAttendance(): Flow<List<AttendanceRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAttendance(record: AttendanceRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAll(records: List<AttendanceRecord>)

    @Query("DELETE FROM attendance_records WHERE id = :id")
    suspend fun deleteAttendance(id: Long)
}

@Dao
interface FeeDao {
    @Query("SELECT * FROM fee_payments ORDER BY paymentDate DESC, id DESC")
    fun getAllPayments(): Flow<List<FeePayment>>

    @Query("SELECT * FROM fee_payments WHERE studentId = :studentId ORDER BY paymentDate DESC, id DESC")
    fun getPaymentsForStudent(studentId: Long): Flow<List<FeePayment>>

    @Query("SELECT * FROM fee_payments WHERE monthYear = :monthYear ORDER BY paymentDate DESC")
    fun getPaymentsForMonth(monthYear: String): Flow<List<FeePayment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: FeePayment): Long

    @Query("DELETE FROM fee_payments WHERE id = :id")
    suspend fun deletePayment(id: Long)

    @Query("SELECT * FROM monthly_fee_dues")
    fun getAllDues(): Flow<List<MonthlyFeeDue>>

    @Query("SELECT * FROM monthly_fee_dues WHERE studentId = :studentId")
    fun getDuesForStudent(studentId: Long): Flow<List<MonthlyFeeDue>>

    @Query("SELECT * FROM monthly_fee_dues WHERE monthYear = :monthYear")
    fun getDuesForMonth(monthYear: String): Flow<List<MonthlyFeeDue>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDue(due: MonthlyFeeDue)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDues(dues: List<MonthlyFeeDue>)

    @Query("DELETE FROM monthly_fee_dues WHERE id = :id")
    suspend fun deleteDue(id: Long)
}
