package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teacher_profile")
data class TeacherProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "মোঃ রফিকুল ইসলাম",
    val coachingName: String = "টিউটর পয়েন্ট ও কোচিং",
    val phone: String = "০১৭১১-২২৩৩৪৪",
    val address: String = "ঢাকা, বাংলাদেশ",
    val language: String = "bn"
)
