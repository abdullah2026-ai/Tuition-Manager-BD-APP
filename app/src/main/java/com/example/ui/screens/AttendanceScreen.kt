package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Student
import com.example.ui.components.EmptyStateCard
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BorderVerySubtle
import com.example.ui.theme.PastelBlueBg
import com.example.ui.theme.PastelBlueIcon
import com.example.ui.theme.PastelGreenBg
import com.example.ui.theme.PastelGreenIcon
import com.example.ui.theme.PastelRoseBg
import com.example.ui.theme.PastelRoseIcon
import com.example.ui.theme.PastelYellowBg
import com.example.ui.theme.PastelYellowIcon
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.TuitionViewModel
import com.example.util.BengaliDateUtils
import com.example.util.Localization

@Composable
fun AttendanceScreen(
    viewModel: TuitionViewModel,
    initialSessionId: Long? = null,
    initialSessionType: String? = null,
    modifier: Modifier = Modifier
) {
    val isBn by viewModel.isBengali.collectAsState()
    val allSessions by viewModel.allUnifiedSessions.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()
    val crossRefs by viewModel.batchCrossRefs.collectAsState()
    val allAttendance by viewModel.allAttendance.collectAsState()

    var selectedDate by remember { mutableStateOf(BengaliDateUtils.getTodayDateString()) }

    var selectedSessionId by remember(allSessions) {
        mutableStateOf(initialSessionId ?: allSessions.firstOrNull()?.id ?: 0L)
    }
    var selectedSessionType by remember(allSessions) {
        mutableStateOf(initialSessionType ?: allSessions.firstOrNull()?.type ?: "COACHING")
    }

    val currentSession = allSessions.find { it.id == selectedSessionId && it.type == selectedSessionType }
        ?: allSessions.firstOrNull()

    // Determine enrolled students for this session
    val enrolledStudents: List<Student> = remember(currentSession, allStudents, crossRefs) {
        if (currentSession == null) emptyList()
        else if (currentSession.type == "COACHING") {
            val studentIds = crossRefs.filter { it.batchId == currentSession.id }.map { it.studentId }
            allStudents.filter { it.id in studentIds && !it.isArchived }
        } else {
            // Home tuition
            val sId = currentSession.rawTuition?.studentId ?: 0L
            allStudents.filter { it.id == sId }
        }
    }

    // Attendance records for current session and date
    val sessionAttendanceForDate = allAttendance.filter {
        it.sessionId == (currentSession?.id ?: 0L) &&
                it.sessionType == (currentSession?.type ?: "") &&
                it.date == selectedDate
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("attendance_screen")
    ) {
        // Top Header: Date Selector & Quick "সবাই উপস্থিত"
        Surface(
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = Localization.get("attendance_title", isBn),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = BengaliDateUtils.formatFullDate(selectedDate, isBn),
                                style = MaterialTheme.typography.bodySmall,
                                color = PrimaryBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Mark All Present button
                    if (enrolledStudents.isNotEmpty() && currentSession != null) {
                        Button(
                            onClick = {
                                viewModel.markAllPresent(
                                    sessionId = currentSession.id,
                                    sessionType = currentSession.type,
                                    date = selectedDate,
                                    studentIds = enrolledStudents.map { it.id }
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentGreen,
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                            modifier = Modifier.testTag("btn_mark_all_present")
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = Localization.get("mark_all_present", isBn),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Sessions Selector Carousel
                Text(
                    text = if (isBn) "ব্যাচ বা সেশন নির্বাচন করুন:" else "Select Batch or Session:",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (allSessions.isEmpty()) {
                    Text(
                        text = if (isBn) "কোন ব্যাচ বা টিউশন তৈরি করা নেই" else "No classes created yet",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(allSessions, key = { "${it.type}_${it.id}" }) { session ->
                            val isSelected = session.id == currentSession?.id && session.type == currentSession.type
                            Surface(
                                onClick = {
                                    selectedSessionId = session.id
                                    selectedSessionType = session.type
                                },
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) PrimaryBlue else Color(0xFFF1F5F9),
                                modifier = Modifier.height(38.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (session.type == "COACHING") Icons.Default.School else Icons.Default.Home,
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else PrimaryBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = session.title,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Attendance stats chips for today for current session
        if (currentSession != null && enrolledStudents.isNotEmpty()) {
            val presents = sessionAttendanceForDate.count { it.status == "PRESENT" }
            val absents = sessionAttendanceForDate.count { it.status == "ABSENT" }
            val leaves = sessionAttendanceForDate.count { it.status == "LEAVE" }
            val notTaken = enrolledStudents.size - sessionAttendanceForDate.size

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AttendanceCountCard(label = Localization.get("present", isBn), count = presents, bgColor = PastelGreenBg, textColor = PastelGreenIcon, isBn = isBn, modifier = Modifier.weight(1f))
                AttendanceCountCard(label = Localization.get("absent", isBn), count = absents, bgColor = PastelRoseBg, textColor = PastelRoseIcon, isBn = isBn, modifier = Modifier.weight(1f))
                AttendanceCountCard(label = Localization.get("leave", isBn), count = leaves, bgColor = PastelYellowBg, textColor = PastelYellowIcon, isBn = isBn, modifier = Modifier.weight(1f))
                AttendanceCountCard(label = Localization.get("not_taken", isBn), count = notTaken.coerceAtLeast(0), bgColor = Color(0xFFF1F5F9), textColor = TextSecondary, isBn = isBn, modifier = Modifier.weight(1f))
            }
        }

        // Student Attendance List
        if (allSessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateCard(
                    message = if (isBn) "উপস্থিতি নিতে প্রথমে রুটিন ট্যাব থেকে কোচিং ব্যাচ বা হোম টিউশন যোগ করুন।" else "Add a batch or home tuition first.",
                    icon = Icons.Default.Schedule
                )
            }
        } else if (enrolledStudents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateCard(
                    message = if (isBn) "এই সেশনে এখনও কোন শিক্ষার্থী যুক্ত করা হয়নি। রুটিনে গিয়ে শিক্ষার্থী যোগ করুন।" else "No students assigned to this session.",
                    icon = Icons.Default.School
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(enrolledStudents, key = { it.id }) { student ->
                    val record = sessionAttendanceForDate.find { it.studentId == student.id }
                    val currentStatus = record?.status // "PRESENT", "ABSENT", "LEAVE", or null

                    StudentAttendanceRow(
                        student = student,
                        isBn = isBn,
                        currentStatus = currentStatus,
                        onStatusSelected = { status ->
                            if (currentSession != null) {
                                viewModel.recordAttendance(
                                    studentId = student.id,
                                    sessionId = currentSession.id,
                                    sessionType = currentSession.type,
                                    date = selectedDate,
                                    status = status
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AttendanceCountCard(
    label: String,
    count: Int,
    bgColor: Color,
    textColor: Color,
    isBn: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = bgColor
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isBn) BengaliDateUtils.toBengaliDigits(count) else count.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = textColor.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
fun StudentAttendanceRow(
    student: Student,
    isBn: Boolean,
    currentStatus: String?,
    onStatusSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("attendance_row_${student.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${student.className} ${if (student.studentId.isNotBlank()) "• রোল: ${student.studentId}" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            // Attendance Status Segmented Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Present
                AttendanceSegmentButton(
                    label = Localization.get("present", isBn),
                    isSelected = currentStatus == "PRESENT",
                    selectedBg = AccentGreen,
                    unselectedBg = PastelGreenBg,
                    selectedText = Color.White,
                    unselectedText = com.example.ui.theme.PastelGreenText,
                    onClick = { onStatusSelected("PRESENT") }
                )
                // Absent
                AttendanceSegmentButton(
                    label = Localization.get("absent", isBn),
                    isSelected = currentStatus == "ABSENT",
                    selectedBg = Color(0xFFDC2626),
                    unselectedBg = PastelRoseBg,
                    selectedText = Color.White,
                    unselectedText = com.example.ui.theme.PastelRoseText,
                    onClick = { onStatusSelected("ABSENT") }
                )
                // Leave
                AttendanceSegmentButton(
                    label = Localization.get("leave", isBn),
                    isSelected = currentStatus == "LEAVE",
                    selectedBg = Color(0xFFD97706),
                    unselectedBg = PastelYellowBg,
                    selectedText = Color.White,
                    unselectedText = com.example.ui.theme.PastelYellowText,
                    onClick = { onStatusSelected("LEAVE") }
                )
            }
        }
    }
}

@Composable
fun AttendanceSegmentButton(
    label: String,
    isSelected: Boolean,
    selectedBg: Color,
    unselectedBg: Color,
    selectedText: Color,
    unselectedText: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) selectedBg else unselectedBg,
        modifier = Modifier.height(34.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 9.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) selectedText else unselectedText
            )
        }
    }
}
