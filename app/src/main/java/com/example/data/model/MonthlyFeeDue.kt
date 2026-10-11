package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "monthly_fee_dues",
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["studentId", "monthYear"], unique = true)
    ]
)
data class MonthlyFeeDue(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val monthYear: String, // "YYYY-MM"
    val dueAmount: Double,
    val createdAt: Long = System.currentTimeMillis()
)
