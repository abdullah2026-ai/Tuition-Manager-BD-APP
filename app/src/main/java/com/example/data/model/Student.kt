package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val studentId: String = "",
    val className: String = "",
    val guardianName: String = "",
    val guardianPhone: String = "",
    val subjects: String = "",
    val monthlyFee: Double = 0.0,
    val tuitionType: String = "COACHING", // "COACHING", "HOME", "BOTH"
    val notes: String = "",
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
