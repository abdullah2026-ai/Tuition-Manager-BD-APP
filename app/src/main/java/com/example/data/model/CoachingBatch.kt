package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "coaching_batches")
data class CoachingBatch(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val batchName: String,
    val className: String = "",
    val subject: String = "",
    val daysOfWeek: String = "", // e.g. "SAT,MON,WED"
    val startTime: String = "08:00", // "HH:mm"
    val endTime: String = "09:30",
    val monthlyFee: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)
