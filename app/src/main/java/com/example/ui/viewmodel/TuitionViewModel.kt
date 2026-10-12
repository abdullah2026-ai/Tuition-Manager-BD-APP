package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.AttendanceRecord
import com.example.data.model.BatchStudentCrossRef
import com.example.data.model.CoachingBatch
import com.example.data.model.FeePayment
import com.example.data.model.HomeTuition
import com.example.data.model.MonthlyFeeDue
import com.example.data.model.Student
import com.example.data.model.TeacherProfile
import com.example.data.repository.TuitionRepository
import com.example.util.BengaliDateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

data class UnifiedSession(
    val id: Long,
    val type: String, // "COACHING" or "HOME"
    val title: String,
    val subtitle: String,
    val timeSlot: String,
    val startTime: String,
    val endTime: String,
    val daysOfWeek: String,
    val fee: Double,
    val address: String = "",
    val phone: String = "",
    val studentCount: Int = 0,
    val rawBatch: CoachingBatch? = null,
    val rawTuition: HomeTuition? = null
)

data class OverlapItem(
    val session1: UnifiedSession,
    val session2: UnifiedSession
)

class TuitionViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    val repository = TuitionRepository(database)

    val profile: StateFlow<TeacherProfile?> = repository.profile.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        TeacherProfile()
    )

    val activeStudents: StateFlow<List<Student>> = repository.activeStudents.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val archivedStudents: StateFlow<List<Student>> = repository.archivedStudents.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val allStudents: StateFlow<List<Student>> = repository.allStudents.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val batches: StateFlow<List<CoachingBatch>> = repository.allBatches.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val batchCrossRefs: StateFlow<List<BatchStudentCrossRef>> = repository.allCrossRefs.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val homeTuitions: StateFlow<List<HomeTuition>> = repository.allHomeTuitions.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val allAttendance: StateFlow<List<AttendanceRecord>> = repository.allAttendance.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val allPayments: StateFlow<List<FeePayment>> = repository.allPayments.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val allDues: StateFlow<List<MonthlyFeeDue>> = repository.allDues.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Current language (true = Bengali, false = English)
    val isBengali: StateFlow<Boolean> = combine(profile) { prof ->
        prof[0]?.language != "en"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    // Unified sessions list
    val allUnifiedSessions: StateFlow<List<UnifiedSession>> = combine(
        batches,
        homeTuitions,
        batchCrossRefs,
        allStudents
    ) { bList, htList, crossRefs, sList ->
        val result = mutableListOf<UnifiedSession>()

        // Coaching batches
        bList.forEach { batch ->
            val count = crossRefs.count { it.batchId == batch.id }
            result.add(
                UnifiedSession(
                    id = batch.id,
                    type = "COACHING",
                    title = batch.batchName,
                    subtitle = "${batch.className} • ${batch.subject}".trim().removePrefix("•").removeSuffix("•").trim(),
                    timeSlot = BengaliDateUtils.formatTimeSlot(batch.startTime, batch.endTime, true),
                    startTime = batch.startTime,
                    endTime = batch.endTime,
                    daysOfWeek = batch.daysOfWeek,
                    fee = batch.monthlyFee,
                    studentCount = count,
                    rawBatch = batch
                )
            )
        }

        // Home tuitions
        htList.forEach { tuition ->
            val student = sList.find { it.id == tuition.studentId }
            val studentName = student?.name ?: "শিক্ষার্থী"
            result.add(
                UnifiedSession(
                    id = tuition.id,
                    type = "HOME",
                    title = studentName,
                    subtitle = "${tuition.subject} • ${tuition.address}".trim().removePrefix("•").removeSuffix("•").trim(),
                    timeSlot = BengaliDateUtils.formatTimeSlot(tuition.startTime, tuition.endTime, true),
                    startTime = tuition.startTime,
                    endTime = tuition.endTime,
                    daysOfWeek = tuition.daysOfWeek,
                    fee = tuition.monthlyFee,
                    address = tuition.address,
                    phone = tuition.guardianPhone.ifBlank { student?.guardianPhone ?: "" },
                    studentCount = 1,
                    rawTuition = tuition
                )
            )
        }

        result.sortedBy { it.startTime }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Feedback message state (Snackbar/Toast)
    val userMessage = MutableStateFlow<String?>(null)

    fun clearUserMessage() {
        userMessage.value = null
    }

    // Initialize with demo data if completely new
    init {
        viewModelScope.launch {
            // Ensure profile exists
            val current = repository.profile
            // We do not force demo data, but if user wants they can tap the demo button
        }
    }

    // Student operations
    fun saveStudent(student: Student, onDone: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.saveStudent(student)
            userMessage.value = if (isBengali.value) "শিক্ষার্থীর তথ্য সফলভাবে সংরক্ষিত হয়েছে" else "Student saved successfully"
            onDone?.invoke()
        }
    }

    fun setStudentArchived(id: Long, archived: Boolean) {
        viewModelScope.launch {
            repository.setArchived(id, archived)
            userMessage.value = if (archived) {
                if (isBengali.value) "শিক্ষার্থীকে সংরক্ষণাগারে নেওয়া হয়েছে" else "Student archived"
            } else {
                if (isBengali.value) "শিক্ষার্থীকে সক্রিয় করা হয়েছে" else "Student restored"
            }
        }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            repository.deleteStudent(student)
            userMessage.value = if (isBengali.value) "শিক্ষার্থী স্থায়ীভাবে মুছে ফেলা হয়েছে" else "Student deleted"
        }
    }

    // Batch operations
    fun saveBatch(batch: CoachingBatch, selectedStudentIds: List<Long>, onDone: (() -> Unit)? = null) {
        viewModelScope.launch {
            val batchId = repository.saveBatch(batch)
            // Update assigned students
            val currentStudents = repository.getStudentsForBatchSync(batchId)
            val currentIds = currentStudents.map { it.id }.toSet()
            val newIds = selectedStudentIds.toSet()

            // Remove unselected
            currentIds.filter { it !in newIds }.forEach { removeId ->
                repository.removeStudentFromBatch(batchId, removeId)
            }
            // Add new
            newIds.filter { it !in currentIds }.forEach { addId ->
                repository.addStudentToBatch(batchId, addId)
            }

            userMessage.value = if (isBengali.value) "কোচিং ব্যাচ সংরক্ষিত হয়েছে" else "Coaching batch saved"
            onDone?.invoke()
        }
    }

    fun deleteBatch(batch: CoachingBatch) {
        viewModelScope.launch {
            repository.deleteBatch(batch)
            userMessage.value = if (isBengali.value) "ব্যাচ মুছে ফেলা হয়েছে" else "Batch deleted"
        }
    }

    // Home tuition operations
    fun saveHomeTuition(tuition: HomeTuition, onDone: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.saveHomeTuition(tuition)
            userMessage.value = if (isBengali.value) "হোম টিউশন সংরক্ষিত হয়েছে" else "Home tuition saved"
            onDone?.invoke()
        }
    }

    fun deleteHomeTuition(tuition: HomeTuition) {
        viewModelScope.launch {
            repository.deleteHomeTuition(tuition)
            userMessage.value = if (isBengali.value) "হোম টিউশন মুছে ফেলা হয়েছে" else "Home tuition deleted"
        }
    }

    // Attendance operations
    fun recordAttendance(
        studentId: Long,
        sessionId: Long,
        sessionType: String,
        date: String,
        status: String
    ) {
        viewModelScope.launch {
            val record = AttendanceRecord(
                studentId = studentId,
                sessionId = sessionId,
                sessionType = sessionType,
                date = date,
                status = status
            )
            repository.recordAttendance(record)
        }
    }

    fun markAllPresent(
        sessionId: Long,
        sessionType: String,
        date: String,
        studentIds: List<Long>
    ) {
        viewModelScope.launch {
            val records = studentIds.map { sId ->
                AttendanceRecord(
                    studentId = sId,
                    sessionId = sessionId,
                    sessionType = sessionType,
                    date = date,
                    status = "PRESENT"
                )
            }
            repository.recordAttendanceList(records)
            userMessage.value = if (isBengali.value) "সকলকে উপস্থিত করা হয়েছে" else "Marked all present"
        }
    }

    // Fee operations
    private var isSubmittingPayment = false

    fun recordPayment(payment: FeePayment, onDone: (() -> Unit)? = null) {
        if (isSubmittingPayment) return
        isSubmittingPayment = true
        viewModelScope.launch {
            try {
                repository.recordPayment(payment)
                // Ensure a monthly due record exists for this month so accounting is accurate
                val currentDues = repository.allDues
                // Also check if student's default fee is set
                val student = repository.getStudentByIdSync(payment.studentId)
                if (student != null) {
                    val feeDue = MonthlyFeeDue(
                        studentId = student.id,
                        monthYear = payment.monthYear,
                        dueAmount = student.monthlyFee
                    )
                    repository.setMonthlyDue(feeDue)
                }

                userMessage.value = if (isBengali.value) "বেতন সফলভাবে গ্রহণ করা হয়েছে" else "Payment recorded successfully"
                onDone?.invoke()
            } finally {
                isSubmittingPayment = false
            }
        }
    }

    fun deletePayment(id: Long) {
        viewModelScope.launch {
            repository.deletePayment(id)
            userMessage.value = if (isBengali.value) "পেমেন্ট রেকর্ড মুছে ফেলা হয়েছে" else "Payment record deleted"
        }
    }

    // Profile & Settings
    fun updateProfile(profile: TeacherProfile) {
        viewModelScope.launch {
            repository.saveProfile(profile)
            userMessage.value = if (isBengali.value) "প্রোফাইল সংরক্ষিত হয়েছে" else "Profile saved"
        }
    }

    fun toggleLanguage() {
        viewModelScope.launch {
            val current = profile.value ?: TeacherProfile()
            val newLang = if (current.language == "bn") "en" else "bn"
            repository.saveProfile(current.copy(language = newLang))
        }
    }

    // Demo Data Management
    fun loadDemoData() {
        viewModelScope.launch {
            repository.loadDemoData()
            userMessage.value = if (isBengali.value) "নমুনা ডেমো ডেটা সফলভাবে যোগ করা হয়েছে" else "Demo data loaded successfully"
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearDatabase()
            userMessage.value = if (isBengali.value) "সকল ডেটা মুছে ফেলা হয়েছে" else "All data cleared"
        }
    }

    // Overlap calculation for a specific day
    fun getOverlapsForDay(dayCode: String, sessions: List<UnifiedSession>): List<OverlapItem> {
        val filtered = sessions.filter { it.daysOfWeek.contains(dayCode, ignoreCase = true) }
        val overlaps = mutableListOf<OverlapItem>()
        for (i in filtered.indices) {
            for (j in i + 1 until filtered.size) {
                val s1 = filtered[i]
                val s2 = filtered[j]
                if (BengaliDateUtils.isTimesOverlapping(s1.startTime, s1.endTime, s2.startTime, s2.endTime)) {
                    overlaps.add(OverlapItem(s1, s2))
                }
            }
        }
        return overlaps
    }

    // Backup & Restore helpers via BackupManager
    suspend fun exportDataJson(): String {
        return com.example.util.BackupManager.generateBackupJson(database)
    }

    fun validateBackup(jsonString: String): com.example.util.BackupManager.ValidationResult {
        return com.example.util.BackupManager.validateBackupJson(jsonString, isBengali.value)
    }

    suspend fun restoreReplaceAll(jsonString: String): Result<Unit> {
        val result = com.example.util.BackupManager.restoreReplaceAll(database, jsonString)
        if (result.isSuccess) {
            userMessage.value = if (isBengali.value) "সফলভাবে ডেটা রিস্টোর সম্পন্ন হয়েছে!" else "Data restored successfully!"
        }
        return result
    }

    suspend fun restoreMerge(jsonString: String): Result<Unit> {
        val result = com.example.util.BackupManager.restoreMerge(database, jsonString)
        if (result.isSuccess) {
            userMessage.value = if (isBengali.value) "সফলভাবে ডেটা মার্জ সম্পন্ন হয়েছে!" else "Data merged successfully!"
        }
        return result
    }

    suspend fun restoreDataJson(jsonString: String): Boolean {
        return restoreReplaceAll(jsonString).isSuccess
    }
}
