package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "fee_payments",
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["studentId"]),
        Index(value = ["monthYear"])
    ]
)
data class FeePayment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val monthYear: String, // "YYYY-MM", e.g. "2026-10"
    val amount: Double,
    val paymentDate: String, // "YYYY-MM-DD"
    val paymentMethod: String = "CASH", // "CASH", "BKASH", "NAGAD", "BANK", "OTHER"
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
