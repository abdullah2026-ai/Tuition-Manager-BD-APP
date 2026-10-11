package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "home_tuitions",
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["studentId"])]
)
data class HomeTuition(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val subject: String = "",
    val address: String = "",
    val guardianPhone: String = "",
    val daysOfWeek: String = "", // e.g. "SAT,MON,WED"
    val startTime: String = "16:00",
    val endTime: String = "17:30",
    val monthlyFee: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)
