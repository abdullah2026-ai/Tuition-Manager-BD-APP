package com.example.util

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
import kotlinx.coroutines.withContext

object DemoDataGenerator {

    suspend fun populateDemoData(database: AppDatabase) = withContext(Dispatchers.IO) {
        val studentDao = database.studentDao()
        val batchDao = database.coachingBatchDao()
        val tuitionDao = database.homeTuitionDao()
        val attendanceDao = database.attendanceDao()
        val feeDao = database.feeDao()
        val profileDao = database.teacherProfileDao()

        // 1. Profile
        profileDao.insertOrUpdate(
            TeacherProfile(
                id = 1,
                name = "মোঃ আব্দুল করিম",
                coachingName = "করিম'স লার্নিং কেয়ার ও হোম টিউশন",
                phone = "01712-345678",
                address = "মিরপুর-১০, ঢাকা",
                language = "bn"
            )
        )

        // 2. Students
        val s1Id = studentDao.insertStudent(
            Student(
                name = "তানভীর আহমেদ",
                studentId = "১০১",
                className = "১০ম শ্রেণী",
                guardianName = "মোঃ কামাল হোসেন",
                guardianPhone = "01711122334",
                subjects = "উচ্চতর গণিত ও পদার্থবিজ্ঞান",
                monthlyFee = 1500.0,
                tuitionType = "COACHING",
                notes = "মেধাবী ছাত্র, নিয়মিত উপস্থিতি বজায় রাখে।"
            )
        )

        val s2Id = studentDao.insertStudent(
            Student(
                name = "সুমাইয়া আক্তার",
                studentId = "১০২",
                className = "১০ম শ্রেণী",
                guardianName = "সালমা বেগম",
                guardianPhone = "01822233445",
                subjects = "উচ্চতর গণিত",
                monthlyFee = 1500.0,
                tuitionType = "COACHING",
                notes = "সপ্তাহে ৩ দিন কোচিং ব্যাচ।"
            )
        )

        val s3Id = studentDao.insertStudent(
            Student(
                name = "আব্দুল্লাহ আল মামুন",
                studentId = "১০৩",
                className = "এইচএসসি (বিজ্ঞান)",
                guardianName = "ফরিদুল ইসলাম",
                guardianPhone = "01933344556",
                subjects = "পদার্থবিজ্ঞান ১ম ও ২য় পত্র",
                monthlyFee = 2000.0,
                tuitionType = "COACHING",
                notes = "কলেজ টেস্ট পরীক্ষার প্রস্তুতি চলছে।"
            )
        )

        val s4Id = studentDao.insertStudent(
            Student(
                name = "নুসরাত জাহান রিমি",
                studentId = "১০৪",
                className = "৮ম শ্রেণী",
                guardianName = "নজরুল ইসলাম চৌধুরী",
                guardianPhone = "01644455667",
                subjects = "সকল বিষয় (হোম টিউশন)",
                monthlyFee = 4000.0,
                tuitionType = "HOME",
                notes = "হোম টিউশন, বাসা: ধানমন্ডি ৪/এ।"
            )
        )

        // 3. Batches
        val b1Id = batchDao.insertBatch(
            CoachingBatch(
                batchName = "সকাল - এসএসসি গণিত ব্যাচ",
                className = "১০ম শ্রেণী",
                subject = "উচ্চতর গণিত",
                daysOfWeek = "SAT,MON,WED",
                startTime = "07:30",
                endTime = "09:00",
                monthlyFee = 1500.0
            )
        )

        val b2Id = batchDao.insertBatch(
            CoachingBatch(
                batchName = "বিকাল - এইচএসসি পদার্থ বিজ্ঞান ব্যাচ",
                className = "এইচএসসি",
                subject = "পদার্থবিজ্ঞান",
                daysOfWeek = "SUN,TUE,THU",
                startTime = "16:00",
                endTime = "17:30",
                monthlyFee = 2000.0
            )
        )

        // 4. Enrollments
        batchDao.addStudentToBatch(BatchStudentCrossRef(batchId = b1Id, studentId = s1Id))
        batchDao.addStudentToBatch(BatchStudentCrossRef(batchId = b1Id, studentId = s2Id))
        batchDao.addStudentToBatch(BatchStudentCrossRef(batchId = b2Id, studentId = s3Id))

        // 5. Home Tuition
        tuitionDao.insertHomeTuition(
            HomeTuition(
                studentId = s4Id,
                subject = "সাধারণ গণিত ও ইংরেজি",
                address = "বাড়ি নং ১২, রোড ৪/এ, ধানমন্ডি, ঢাকা",
                guardianPhone = "01644455667",
                daysOfWeek = "SAT,MON,WED",
                startTime = "18:00",
                endTime = "19:30",
                monthlyFee = 4000.0
            )
        )

        // 6. Current Month Fee Dues & Payments
        val currentMonth = BengaliDateUtils.getCurrentMonthYear()
        val todayDate = BengaliDateUtils.getTodayDateString()

        feeDao.insertOrUpdateDue(MonthlyFeeDue(studentId = s1Id, monthYear = currentMonth, dueAmount = 1500.0))
        feeDao.insertOrUpdateDue(MonthlyFeeDue(studentId = s2Id, monthYear = currentMonth, dueAmount = 1500.0))
        feeDao.insertOrUpdateDue(MonthlyFeeDue(studentId = s3Id, monthYear = currentMonth, dueAmount = 2000.0))
        feeDao.insertOrUpdateDue(MonthlyFeeDue(studentId = s4Id, monthYear = currentMonth, dueAmount = 4000.0))

        // Partial and full payments
        feeDao.insertPayment(
            FeePayment(
                studentId = s1Id,
                monthYear = currentMonth,
                amount = 1500.0,
                paymentDate = todayDate,
                paymentMethod = "BKASH",
                notes = "বিকাশ ট্রানজাকশন আইডি: 9J4K2L8"
            )
        )

        feeDao.insertPayment(
            FeePayment(
                studentId = s4Id,
                monthYear = currentMonth,
                amount = 2000.0,
                paymentDate = todayDate,
                paymentMethod = "CASH",
                notes = "নগদে আংশিক প্রদান, বাকি ২০০০ টাকা পরের সপ্তাহে দিবে।"
            )
        )

        // 7. Today's attendance
        attendanceDao.insertOrUpdateAttendance(
            AttendanceRecord(
                studentId = s1Id,
                sessionType = "COACHING",
                sessionId = b1Id,
                date = todayDate,
                status = "PRESENT"
            )
        )
        attendanceDao.insertOrUpdateAttendance(
            AttendanceRecord(
                studentId = s2Id,
                sessionType = "COACHING",
                sessionId = b1Id,
                date = todayDate,
                status = "PRESENT"
            )
        )
    }

    suspend fun clearAllData(database: AppDatabase) = withContext(Dispatchers.IO) {
        database.clearAllTables()
    }
}
