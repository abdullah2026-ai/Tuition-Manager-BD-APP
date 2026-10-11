package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.AttendanceRecord
import com.example.data.model.BatchStudentCrossRef
import com.example.data.model.CoachingBatch
import com.example.data.model.FeePayment
import com.example.data.model.HomeTuition
import com.example.data.model.MonthlyFeeDue
import com.example.data.model.Student
import com.example.data.model.TeacherProfile

@Database(
    entities = [
        TeacherProfile::class,
        Student::class,
        CoachingBatch::class,
        BatchStudentCrossRef::class,
        HomeTuition::class,
        AttendanceRecord::class,
        FeePayment::class,
        MonthlyFeeDue::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun teacherProfileDao(): TeacherProfileDao
    abstract fun studentDao(): StudentDao
    abstract fun coachingBatchDao(): CoachingBatchDao
    abstract fun homeTuitionDao(): HomeTuitionDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun feeDao(): FeeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tuition_manager_bd.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
