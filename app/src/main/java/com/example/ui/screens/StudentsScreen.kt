package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Student
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.ModalHeader
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BorderVerySubtle
import com.example.ui.theme.PastelBlueBg
import com.example.ui.theme.PastelBlueIcon
import com.example.ui.theme.PastelGreenBg
import com.example.ui.theme.PastelGreenIcon
import com.example.ui.theme.PastelPurpleBg
import com.example.ui.theme.PastelPurpleIcon
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.TuitionViewModel
import com.example.util.BengaliDateUtils
import com.example.util.Localization

@Composable
fun StudentsScreen(
    viewModel: TuitionViewModel,
    onOpenAddStudent: () -> Unit,
    onEditStudent: (Student) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isBn by viewModel.isBengali.collectAsState()
    val activeStudents by viewModel.activeStudents.collectAsState()
    val archivedStudents by viewModel.archivedStudents.collectAsState()
    val allAttendance by viewModel.allAttendance.collectAsState()
    val allPayments by viewModel.allPayments.collectAsState()
    val batches by viewModel.batches.collectAsState()
    val crossRefs by viewModel.batchCrossRefs.collectAsState()
    val homeTuitions by viewModel.homeTuitions.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Active, 1: Archived

    var viewingStudent by remember { mutableStateOf<Student?>(null) }
    var studentToDelete by remember { mutableStateOf<Student?>(null) }
    var studentToArchiveToggle by remember { mutableStateOf<Student?>(null) }

    val rawList = if (selectedTab == 0) activeStudents else archivedStudents
    val filteredStudents = rawList.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.studentId.contains(searchQuery, ignoreCase = true) ||
                it.guardianPhone.contains(searchQuery, ignoreCase = true) ||
                it.className.contains(searchQuery, ignoreCase = true)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("students_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Modern Search Bar & Segmented Tabs
            Surface(
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    // Pill-style Search Input
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("student_search_input"),
                        placeholder = {
                            Text(
                                Localization.get("search_student_hint", isBn),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary.copy(alpha = 0.8f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search",
                                tint = PrimaryBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = BorderSubtle,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color(0xFFFBFDFF)
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.White,
                        contentColor = PrimaryBlue
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = {
                                Text(
                                    "${Localization.get("active_tab", isBn)} (${if (isBn) BengaliDateUtils.toBengaliDigits(activeStudents.size) else activeStudents.size})",
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = {
                                Text(
                                    "${Localization.get("archived_tab", isBn)} (${if (isBn) BengaliDateUtils.toBengaliDigits(archivedStudents.size) else archivedStudents.size})",
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }

            // Student List
            if (filteredStudents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateCard(
                        message = if (searchQuery.isNotEmpty()) {
                            if (isBn) "কোন শিক্ষার্থী খুঁজে পাওয়া যায়নি" else "No matching students found"
                        } else {
                            if (selectedTab == 0) {
                                if (isBn) "কোন সক্রিয় শিক্ষার্থী নেই। নিচে বোতাম চেপে যুক্ত করুন।" else "No active students. Click below to add."
                            } else {
                                if (isBn) "আর্কাইভে কোন শিক্ষার্থী নেই" else "No archived students"
                            }
                        },
                        icon = Icons.Default.Person,
                        buttonText = if (selectedTab == 0 && searchQuery.isEmpty()) Localization.get("add_student", isBn) else null,
                        onButtonClick = if (selectedTab == 0 && searchQuery.isEmpty()) onOpenAddStudent else null
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                listOf(
                                    Color(0xFFFFFFFF),
                                    Color(0xFFF8FAFC),
                                    Color(0xFFF1F5F9)
                                )
                            )
                        ),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredStudents, key = { it.id }) { student ->
                        StudentItemCard(
                            student = student,
                            isBn = isBn,
                            onClick = { viewingStudent = student },
                            onCall = {
                                if (student.guardianPhone.isNotBlank()) {
                                    val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:${student.guardianPhone}")
                                    }
                                    context.startActivity(dialIntent)
                                }
                            }
                        )
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = onOpenAddStudent,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 18.dp, bottom = 82.dp)
                .testTag("fab_add_student"),
            containerColor = PrimaryBlue,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = Localization.get("add_student", isBn))
        }
    }

    // Student Detail Dialog
    viewingStudent?.let { student ->
        val studentAttendance = allAttendance.filter { it.studentId == student.id }
        val studentPayments = allPayments.filter { it.studentId == student.id }
        val assignedBatches = batches.filter { b -> crossRefs.any { it.batchId == b.id && it.studentId == student.id } }
        val assignedHomeTuitions = homeTuitions.filter { it.studentId == student.id }

        StudentDetailDialog(
            student = student,
            isBn = isBn,
            attendance = studentAttendance,
            payments = studentPayments,
            batches = assignedBatches,
            homeTuitions = assignedHomeTuitions,
            onDismiss = { viewingStudent = null },
            onEdit = {
                viewingStudent = null
                onEditStudent(student)
            },
            onCallGuardian = {
                if (student.guardianPhone.isNotBlank()) {
                    val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:${student.guardianPhone}")
                    }
                    context.startActivity(dialIntent)
                }
            },
            onArchiveToggle = {
                studentToArchiveToggle = student
            },
            onDelete = {
                studentToDelete = student
            }
        )
    }

    // Confirmation for Archive
    studentToArchiveToggle?.let { student ->
        AlertDialog(
            onDismissRequest = { studentToArchiveToggle = null },
            title = {
                ModalHeader(
                    title = if (student.isArchived) Localization.get("unarchive_student", isBn)
                    else Localization.get("archive_student", isBn),
                    onClose = { studentToArchiveToggle = null }
                )
            },
            text = {
                Text(
                    if (student.isArchived) {
                        if (isBn) "আপনি কি এই শিক্ষার্থীকে পুনরায় সক্রিয় তালিকায় নিতে চান?" else "Restore this student to active list?"
                    } else {
                        Localization.get("confirm_archive", isBn)
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setStudentArchived(student.id, !student.isArchived)
                        studentToArchiveToggle = null
                        viewingStudent = null
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = if (isBn) "হ্যাঁ, নিশ্চিত" else "Confirm",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { studentToArchiveToggle = null }) {
                    Text(
                        text = Localization.get("cancel", isBn),
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }

    // Confirmation for Permanent Delete
    studentToDelete?.let { student ->
        AlertDialog(
            onDismissRequest = { studentToDelete = null },
            title = {
                ModalHeader(
                    title = Localization.get("delete_student", isBn),
                    onClose = { studentToDelete = null }
                )
            },
            text = {
                Text(Localization.get("confirm_delete", isBn))
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteStudent(student)
                        studentToDelete = null
                        viewingStudent = null
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
                TextButton(onClick = { studentToDelete = null }) {
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
fun StudentItemCard(
    student: Student,
    isBn: Boolean,
    onClick: () -> Unit,
    onCall: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("student_item_${student.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.5.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Student avatar with pastel badge
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(PastelPurpleBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = student.name.take(1),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PastelPurpleIcon
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (student.studentId.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF1F5F9)
                        ) {
                            Text(
                                text = "রোল: ${if (isBn) BengaliDateUtils.toBengaliDigits(student.studentId) else student.studentId}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = student.className.ifBlank { "সাধারণ" },
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "•", color = TextSecondary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = BengaliDateUtils.formatCurrency(student.monthlyFee, isBn),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = PastelGreenIcon
                    )
                }

                if (student.guardianName.isNotBlank() || student.guardianPhone.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "অভিভাবক: ${student.guardianName.ifBlank { student.guardianPhone }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (student.guardianPhone.isNotBlank()) {
                IconButton(
                    onClick = onCall,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(PastelGreenBg)
                ) {
                    Icon(
                        Icons.Default.Call,
                        contentDescription = "Call",
                        tint = PastelGreenIcon,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StudentDetailDialog(
    student: Student,
    isBn: Boolean,
    attendance: List<com.example.data.model.AttendanceRecord>,
    payments: List<com.example.data.model.FeePayment>,
    batches: List<com.example.data.model.CoachingBatch>,
    homeTuitions: List<com.example.data.model.HomeTuition>,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onCallGuardian: () -> Unit,
    onArchiveToggle: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {},
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Profile Info
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(PastelPurpleBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = student.name.take(1),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = PastelPurpleIcon
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = student.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${student.className} • রোল: ${if (isBn) BengaliDateUtils.toBengaliDigits(student.studentId) else student.studentId}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                        }
                    }
                }

                // Guardian & Phone
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFF8FAFC)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "অভিভাবক: ${student.guardianName.ifBlank { "উল্লেখ নেই" }}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            if (student.guardianPhone.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "মোবাইল: ${student.guardianPhone}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                    Surface(
                                        onClick = onCallGuardian,
                                        shape = RoundedCornerShape(10.dp),
                                        color = PastelGreenBg
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Call, contentDescription = null, tint = PastelGreenIcon, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("কল করুন", style = MaterialTheme.typography.labelSmall, color = PastelGreenIcon, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Subjects, Fee, Notes
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "বিষয়সমূহ: ${student.subjects.ifBlank { "সকল বিষয়" }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary
                        )
                        Text(
                            text = "মাসিক বেতন: ${BengaliDateUtils.formatCurrency(student.monthlyFee, isBn)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                        if (student.notes.isNotBlank()) {
                            Text(
                                text = "নোট: ${student.notes}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }

                // Assigned Batches / Tuitions
                item {
                    Divider(color = BorderVerySubtle)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "নির্ধারিত ব্যাচ ও টিউশন",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (batches.isEmpty() && homeTuitions.isEmpty()) {
                        Text(
                            text = "এখনও কোন নির্দিষ্ট ব্যাচ বা হোম টিউশনে যুক্ত করা হয়নি।",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    } else {
                        batches.forEach { b ->
                            Text(
                                text = "• [কোচিং] ${b.batchName} (${BengaliDateUtils.formatTimeSlot(b.startTime, b.endTime, isBn)})",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                        }
                        homeTuitions.forEach { ht ->
                            Text(
                                text = "• [হোম টিউশন] ${ht.subject} (${BengaliDateUtils.formatTimeSlot(ht.startTime, ht.endTime, isBn)})",
                                style = MaterialTheme.typography.bodySmall,
                                color = AccentGreen
                            )
                        }
                    }
                }

                // Attendance Summary
                item {
                    Divider(color = BorderVerySubtle)
                    Spacer(modifier = Modifier.height(2.dp))
                    val presents = attendance.count { it.status == "PRESENT" }
                    val absents = attendance.count { it.status == "ABSENT" }
                    val leaves = attendance.count { it.status == "LEAVE" }
                    val total = attendance.size
                    val percent = if (total > 0) (presents * 100) / total else 0

                    Text(
                        text = "উপস্থিতির সারসংক্ষেপ",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF8FAFC)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("মোট", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                Text(if (isBn) BengaliDateUtils.toBengaliDigits(total) else total.toString(), fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("উপস্থিত", style = MaterialTheme.typography.labelSmall, color = PastelGreenIcon)
                                Text(if (isBn) BengaliDateUtils.toBengaliDigits(presents) else presents.toString(), fontWeight = FontWeight.Bold, color = PastelGreenIcon)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("অনুপস্থিত", style = MaterialTheme.typography.labelSmall, color = Color(0xFFDC2626))
                                Text(if (isBn) BengaliDateUtils.toBengaliDigits(absents) else absents.toString(), fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("হার", style = MaterialTheme.typography.labelSmall, color = PrimaryBlue)
                                Text("${if (isBn) BengaliDateUtils.toBengaliDigits(percent) else percent}%", fontWeight = FontWeight.Bold, color = PrimaryBlue)
                            }
                        }
                    }
                }

                // Payment History
                item {
                    Divider(color = BorderVerySubtle)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "বেতন পরিশোধের ইতিহাস",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (payments.isEmpty()) {
                        Text(
                            text = "কোন পেমেন্ট রেকর্ড পাওয়া যায়নি",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    } else {
                        payments.take(5).forEach { p ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${BengaliDateUtils.formatMonthYearDisplay(p.monthYear, isBn)} (${p.paymentMethod})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextPrimary
                                )
                                Text(
                                    text = BengaliDateUtils.formatCurrency(p.amount, isBn),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = PastelGreenIcon
                                )
                            }
                        }
                    }
                }

                // Action Buttons: Edit, Archive, Delete
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onEdit,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryBlue,
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = Localization.get("edit_student", isBn),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        OutlinedButton(
                            onClick = onArchiveToggle,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                if (student.isArchived) Icons.Default.Unarchive else Icons.Default.Archive,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        OutlinedButton(
                            onClick = onDelete,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626))
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    )
}
