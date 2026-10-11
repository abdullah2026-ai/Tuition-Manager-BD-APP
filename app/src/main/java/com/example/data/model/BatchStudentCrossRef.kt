package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "batch_students",
    primaryKeys = ["batchId", "studentId"],
    foreignKeys = [
        ForeignKey(
            entity = CoachingBatch::class,
            parentColumns = ["id"],
            childColumns = ["batchId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["studentId"])]
)
data class BatchStudentCrossRef(
    val batchId: Long,
    val studentId: Long,
    val joinedAt: Long = System.currentTimeMillis()
)
