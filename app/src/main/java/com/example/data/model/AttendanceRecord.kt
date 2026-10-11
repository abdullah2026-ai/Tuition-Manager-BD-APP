package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attendance_records",
    indices = [
        Index(
            value = ["studentId", "sessionType", "sessionId", "date"],
            unique = true
        ),
        Index(value = ["sessionId", "date"]),
        Index(value = ["studentId", "date"])
    ]
)
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val sessionType: String, // "COACHING" or "HOME"
    val sessionId: Long,     // batchId or homeTuitionId
    val date: String,        // "YYYY-MM-DD"
    val status: String,      // "PRESENT", "ABSENT", "LEAVE"
    val remarks: String = "",
    val recordedAt: Long = System.currentTimeMillis()
)
