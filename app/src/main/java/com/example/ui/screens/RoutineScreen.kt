package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CoachingBatch
import com.example.data.model.HomeTuition
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.ModalHeader
import com.example.ui.components.OverlapWarningCard
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BorderVerySubtle
import com.example.ui.theme.PastelBlueBg
import com.example.ui.theme.PastelBlueIcon
import com.example.ui.theme.PastelGreenBg
import com.example.ui.theme.PastelGreenIcon
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.TuitionViewModel
import com.example.ui.viewmodel.UnifiedSession
import com.example.util.BengaliDateUtils
import com.example.util.Localization

@Composable
fun RoutineScreen(
    viewModel: TuitionViewModel,
    onOpenAddBatch: () -> Unit,
    onOpenAddHomeTuition: () -> Unit,
    onEditBatch: (CoachingBatch) -> Unit,
    onEditTuition: (HomeTuition) -> Unit,
    onNavigateToAttendanceForSession: (sessionId: Long, sessionType: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isBn by viewModel.isBengali.collectAsState()
    val allSessions by viewModel.allUnifiedSessions.collectAsState()

    val todayCode = BengaliDateUtils.getTodayDayCode()
    var selectedDayFilter by remember { mutableStateOf("TODAY") } // "ALL", "TODAY", or "SAT", "SUN"...
    var fabMenuExpanded by remember { mutableStateOf(false) }

    var sessionToDelete by remember { mutableStateOf<UnifiedSession?>(null) }

    val activeDayCode = if (selectedDayFilter == "TODAY") todayCode else selectedDayFilter

    val filteredSessions = when (selectedDayFilter) {
        "ALL" -> allSessions
        "TODAY" -> allSessions.filter { it.daysOfWeek.contains(todayCode, ignoreCase = true) }
        else -> allSessions.filter { it.daysOfWeek.contains(selectedDayFilter, ignoreCase = true) }
    }

    // Overlap detection for active day
    val overlaps = if (selectedDayFilter != "ALL") {
        viewModel.getOverlapsForDay(activeDayCode, filteredSessions)
    } else {
        emptyList()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("routine_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Horizontal Day Filter Pills
            Surface(
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        DayFilterChip(
                            label = Localization.get("tab_today", isBn),
                            isSelected = selectedDayFilter == "TODAY",
                            onClick = { selectedDayFilter = "TODAY" }
                        )
                    }
                    item {
                        DayFilterChip(
                            label = Localization.get("tab_all", isBn),
                            isSelected = selectedDayFilter == "ALL",
                            onClick = { selectedDayFilter = "ALL" }
                        )
                    }
                    items(BengaliDateUtils.dayCodes) { code ->
                        val dayLabel = if (isBn) BengaliDateUtils.dayCodeToBengaliShort[code] ?: code
                        else BengaliDateUtils.dayCodeToEnglishShort[code] ?: code
                        DayFilterChip(
                            label = dayLabel,
                            isSelected = selectedDayFilter == code,
                            onClick = { selectedDayFilter = code }
                        )
                    }
                }
            }

            // Overlap warning banner
            if (overlaps.isNotEmpty()) {
                val s1 = overlaps.first().session1.title
                val s2 = overlaps.first().session2.title
                OverlapWarningCard(
                    message = if (isBn) {
                        "⚠️ সতর্কতা: '$s1' এবং '$s2' এর সময়ের সংঘাত রয়েছে!"
                    } else {
                        "⚠️ Warning: Clash between '$s1' and '$s2'!"
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Routine items list
            if (filteredSessions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateCard(
                        message = if (isBn) "এই বারে কোন ক্লাস বা হোম টিউশন নির্ধারিত নেই" else "No sessions scheduled for this day",
                        icon = Icons.Default.Schedule,
                        buttonText = Localization.get("action_add_class", isBn),
                        onButtonClick = { fabMenuExpanded = true }
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredSessions, key = { "${it.type}_${it.id}" }) { session ->
                        RoutineSessionItemCard(
                            session = session,
                            isBn = isBn,
                            onTakeAttendance = {
                                onNavigateToAttendanceForSession(session.id, session.type)
                            },
                            onEdit = {
                                if (session.type == "COACHING" && session.rawBatch != null) {
                                    onEditBatch(session.rawBatch)
                                } else if (session.type == "HOME" && session.rawTuition != null) {
                                    onEditTuition(session.rawTuition)
                                }
                            },
                            onDelete = {
                                sessionToDelete = session
                            }
                        )
                    }
                }
            }
        }

        // Floating Action Button with options
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 18.dp, bottom = 82.dp)
        ) {
            FloatingActionButton(
                onClick = { fabMenuExpanded = true },
                containerColor = PrimaryBlue,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("fab_add_routine")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }

            DropdownMenu(
                expanded = fabMenuExpanded,
                onDismissRequest = { fabMenuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.School, contentDescription = null, tint = PastelBlueIcon, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(Localization.get("add_batch", isBn), fontWeight = FontWeight.Medium)
                        }
                    },
                    onClick = {
                        fabMenuExpanded = false
                        onOpenAddBatch()
                    }
                )
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Home, contentDescription = null, tint = PastelGreenIcon, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(Localization.get("add_home_tuition", isBn), fontWeight = FontWeight.Medium)
                        }
                    },
                    onClick = {
                        fabMenuExpanded = false
                        onOpenAddHomeTuition()
                    }
                )
            }
        }
    }

    // Delete Confirmation
    sessionToDelete?.let { session ->
        AlertDialog(
            onDismissRequest = { sessionToDelete = null },
            title = {
                ModalHeader(
                    title = if (isBn) "সেশন বাতিল বা মুছে ফেলা" else "Delete Session",
                    onClose = { sessionToDelete = null }
                )
            },
            text = {
                Text(
                    if (isBn) "আপনি কি নিশ্চিত '${session.title}' রুটিন থেকে স্থায়ীভাবে মুছে ফেলতে চান?"
                    else "Are you sure you want to delete '${session.title}'?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (session.type == "COACHING" && session.rawBatch != null) {
                            viewModel.deleteBatch(session.rawBatch)
                        } else if (session.type == "HOME" && session.rawTuition != null) {
                            viewModel.deleteHomeTuition(session.rawTuition)
                        }
                        sessionToDelete = null
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDC2626),
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = if (isBn) "মুছে ফেলুন" else "Delete",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { sessionToDelete = null }) {
                    Text(
                        text = Localization.get("cancel", isBn),
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
}

@Composable
fun DayFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) PrimaryBlue else Color(0xFFF1F5F9),
        shadowElevation = if (isSelected) 2.dp else 0.dp,
        modifier = Modifier.height(38.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else TextPrimary
            )
        }
    }
}

@Composable
fun RoutineSessionItemCard(
    session: UnifiedSession,
    isBn: Boolean,
    onTakeAttendance: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("routine_card_${session.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.5.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Type badge & time slot
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (session.type == "COACHING") PastelBlueBg else PastelGreenBg
                    ) {
                        Text(
                            text = if (session.type == "COACHING") Localization.get("badge_coaching", isBn)
                            else Localization.get("badge_home", isBn),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (session.type == "COACHING") PastelBlueIcon else PastelGreenIcon,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = session.timeSlot,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }

                // Edit & Delete icons
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = session.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            if (session.subtitle.isNotBlank()) {
                Text(
                    text = session.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Secondary details: Student count / Days
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (session.type == "COACHING") {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.People, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${if (isBn) BengaliDateUtils.toBengaliDigits(session.studentCount) else session.studentCount} জন শিক্ষার্থী",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    } else if (session.address.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = session.address,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Quick Take Attendance Button
                Surface(
                    onClick = onTakeAttendance,
                    shape = RoundedCornerShape(10.dp),
                    color = PrimaryBlue
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = Localization.get("action_attendance", isBn),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
