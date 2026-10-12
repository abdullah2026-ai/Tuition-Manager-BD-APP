package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import com.example.ui.dialogs.SelectClassTypeDialog
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.OverlapWarningCard
import com.example.ui.components.PinterestQuickAccessCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatCard
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BorderVerySubtle
import com.example.ui.theme.HeaderGradientEnd
import com.example.ui.theme.HeaderGradientMid
import com.example.ui.theme.HeaderGradientStart
import com.example.ui.theme.PastelBlueBg
import com.example.ui.theme.PastelBlueIcon
import com.example.ui.theme.PastelGreenBg
import com.example.ui.theme.PastelGreenIcon
import com.example.ui.theme.PastelOrangeBg
import com.example.ui.theme.PastelOrangeIcon
import com.example.ui.theme.PastelPurpleBg
import com.example.ui.theme.PastelPurpleIcon
import com.example.ui.theme.PastelRoseBg
import com.example.ui.theme.PastelRoseIcon
import com.example.ui.theme.PastelYellowBg
import com.example.ui.theme.PastelYellowIcon
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.TuitionViewModel
import com.example.ui.viewmodel.UnifiedSession
import com.example.util.BengaliDateUtils
import com.example.util.Localization

@Composable
fun HomeScreen(
    viewModel: TuitionViewModel,
    onNavigateToStudents: () -> Unit,
    onNavigateToRoutine: () -> Unit,
    onNavigateToFees: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onOpenAddStudentDialog: () -> Unit,
    onOpenAddClassDialog: () -> Unit = {},
    onOpenAddBatch: () -> Unit = onOpenAddClassDialog,
    onOpenAddHomeTuition: () -> Unit = {},
    onOpenRecordFeeDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isBn by viewModel.isBengali.collectAsState()
    val profile by viewModel.profile.collectAsState()
    val activeStudents by viewModel.activeStudents.collectAsState()
    val allSessions by viewModel.allUnifiedSessions.collectAsState()
    val payments by viewModel.allPayments.collectAsState()
    val dues by viewModel.allDues.collectAsState()

    var showClassTypeDialog by remember { mutableStateOf(false) }

    val todayCode = BengaliDateUtils.getTodayDayCode()
    val todayDate = BengaliDateUtils.getTodayDateString()
    val currentMonth = BengaliDateUtils.getCurrentMonthYear()

    // Real calculations
    val todaySessions = allSessions.filter { it.daysOfWeek.contains(todayCode, ignoreCase = true) }
    val overlapsToday = viewModel.getOverlapsForDay(todayCode, todaySessions)

    val currentMonthPayments = payments.filter { it.monthYear == currentMonth }
    val totalCollected = currentMonthPayments.sumOf { it.amount }

    val currentMonthDuesMap = dues.filter { it.monthYear == currentMonth }.associateBy { it.studentId }
    val totalExpected = activeStudents.sumOf { student ->
        currentMonthDuesMap[student.id]?.dueAmount ?: student.monthlyFee
    }
    val totalOutstanding = (totalExpected - totalCollected).coerceAtLeast(0.0)

    var greeting by remember { mutableStateOf(BengaliDateUtils.getGreeting(isBn)) }
    LaunchedEffect(isBn) {
        greeting = BengaliDateUtils.getGreeting(isBn)
        while (true) {
            delay(30_000L)
            greeting = BengaliDateUtils.getGreeting(isBn)
        }
    }

    val greetingIcon = when (greeting) {
        "শুভ সকাল", "Good Morning" -> "☀️"
        "শুভ দুপুর", "Good Afternoon" -> "🌤️"
        "শুভ সন্ধ্যা", "Good Evening" -> "🌅"
        else -> "🌙"
    }

    val todayDateFormatted = BengaliDateUtils.formatFullDate(todayDate, isBn)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    listOf(
                        Color(0xFFFFFFFF),
                        Color(0xFFF8FAFC),
                        Color(0xFFF1F5F9)
                    )
                )
            )
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // 1. Pinterest Reference Style Top Curved Gradient Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                HeaderGradientStart,
                                HeaderGradientMid,
                                HeaderGradientEnd
                            )
                        )
                    )
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Top Bar with Title, Notification & Avatar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = Localization.get("app_title", isBn),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$greeting, ${profile?.name?.split(" ")?.firstOrNull() ?: if (isBn) "শিক্ষক" else "Teacher"} $greetingIcon",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isBn) "আজকের দিনটি সুন্দর হোক!" else "Have a great teaching day ahead!",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.82f)
                            )
                        }

                        // Teacher Avatar with circular ring
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = profile?.name?.take(1) ?: "T",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Floating Search & Filter Pill in Header (Pinterest Reference Style)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToStudents() },
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White,
                        shadowElevation = 3.5.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = PrimaryBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (isBn) "শিক্ষার্থী, ব্যাচ বা ফোন নম্বর খুঁজুন..." else "Search students, batches, fees...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary.copy(alpha = 0.85f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Date Pill Indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = todayDateFormatted,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        if (!profile?.coachingName.isNullOrBlank()) {
                            Text(
                                text = profile?.coachingName.orEmpty(),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // 2. Pinterest Reference Style "Quick Access" Section
        item {
            Column(modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 22.dp)) {
                SectionHeader(
                    title = if (isBn) "দ্রুত অ্যাকশন" else "Quick Access",
                    actionText = if (isBn) "সকল শিক্ষার্থী" else "View Students",
                    onActionClick = onNavigateToStudents
                )

                // 2x2 Grid of pastel rounded cards with circular icons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    PinterestQuickAccessCard(
                        title = Localization.get("action_add_student", isBn),
                        subtitle = if (isBn) "নতুন ভর্তি ও প্রোফাইল" else "New Admission",
                        icon = Icons.Default.PersonAdd,
                        cardBgColor = PastelPurpleBg,
                        iconBgColor = PastelPurpleIcon,
                        onClick = onOpenAddStudentDialog,
                        modifier = Modifier.weight(1f),
                        testTag = "quick_add_student"
                    )

                    PinterestQuickAccessCard(
                        title = if (isBn) "ক্লাস / ব্যাচ যোগ" else "Add Class / Batch",
                        subtitle = if (isBn) "হোম ব্যাচ বা টিউশন" else "Home Batch or Tuition",
                        icon = Icons.Default.Add,
                        cardBgColor = PastelOrangeBg,
                        iconBgColor = PastelOrangeIcon,
                        onClick = { showClassTypeDialog = true },
                        modifier = Modifier.weight(1f),
                        testTag = "quick_add_class"
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    PinterestQuickAccessCard(
                        title = Localization.get("action_attendance", isBn),
                        subtitle = if (isBn) "দৈনিক হাজিরা হিসাব" else "Take Attendance",
                        icon = Icons.Default.CheckCircle,
                        cardBgColor = PastelGreenBg,
                        iconBgColor = PastelGreenIcon,
                        onClick = onNavigateToAttendance,
                        modifier = Modifier.weight(1f),
                        testTag = "quick_attendance"
                    )

                    PinterestQuickAccessCard(
                        title = Localization.get("action_collect_fee", isBn),
                        subtitle = if (isBn) "ফি আদায় ও রশিদ" else "Record Payment",
                        icon = Icons.Default.Payments,
                        cardBgColor = PastelBlueBg,
                        iconBgColor = PastelBlueIcon,
                        onClick = onOpenRecordFeeDialog,
                        modifier = Modifier.weight(1f),
                        testTag = "quick_collect_fee"
                    )
                }
            }
        }

        // 3. Summary Statistics Cards
        item {
            Column(modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 24.dp)) {
                SectionHeader(
                    title = if (isBn) "হিসাব ও পরিসংখ্যান" else "Monthly Overview",
                    actionText = if (isBn) "বেতন খাতা" else "Fee Ledger",
                    onActionClick = onNavigateToFees
                )

                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val availableWidth = maxWidth
                    val spacing = if (availableWidth < 340.dp) 8.dp else 12.dp

                    Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(spacing)
                        ) {
                            StatCard(
                                title = Localization.get("today_sessions", isBn),
                                value = if (isBn) BengaliDateUtils.toBengaliDigits(todaySessions.size) else todaySessions.size.toString(),
                                icon = Icons.Default.Schedule,
                                iconTint = PastelBlueIcon,
                                containerColor = PastelBlueBg,
                                modifier = Modifier.weight(1f),
                                testTag = "stat_today_sessions"
                            )
                            StatCard(
                                title = Localization.get("active_students", isBn),
                                value = if (isBn) BengaliDateUtils.toBengaliDigits(activeStudents.size) else activeStudents.size.toString(),
                                icon = Icons.Default.People,
                                iconTint = PastelGreenIcon,
                                containerColor = PastelGreenBg,
                                modifier = Modifier.weight(1f),
                                testTag = "stat_active_students"
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(spacing)
                        ) {
                            StatCard(
                                title = Localization.get("monthly_collected", isBn),
                                value = BengaliDateUtils.formatCurrency(totalCollected, isBn),
                                icon = Icons.Default.Payments,
                                iconTint = PastelYellowIcon,
                                containerColor = PastelYellowBg,
                                modifier = Modifier.weight(1f),
                                testTag = "stat_collected"
                            )
                            StatCard(
                                title = Localization.get("monthly_dues", isBn),
                                value = BengaliDateUtils.formatCurrency(totalOutstanding, isBn),
                                icon = Icons.Default.Payments,
                                iconTint = if (totalOutstanding > 0) PastelRoseIcon else PastelGreenIcon,
                                containerColor = if (totalOutstanding > 0) PastelRoseBg else PastelGreenBg,
                                modifier = Modifier.weight(1f),
                                testTag = "stat_dues"
                            )
                        }
                    }
                }
            }
        }

        // Schedule Overlap Warning
        if (overlapsToday.isNotEmpty()) {
            item {
                val s1 = overlapsToday.first().session1.title
                val s2 = overlapsToday.first().session2.title
                OverlapWarningCard(
                    message = if (isBn) {
                        "আজকের ক্লাসে সময়ের ওভারল্যাপ: '$s1' এবং '$s2' এর সময় একে অপরের সাথে মিলে যাচ্ছে।"
                    } else {
                        "Time overlap today: '$s1' and '$s2' clash."
                    },
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
                )
            }
        }

        // 4. Dedicated Dashboard Routine / Time Table Section (Structured Tabular & List Format)
        item {
            Column(modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 22.dp)) {
                DashboardRoutineTableWidget(
                    todaySessions = todaySessions,
                    overlapsToday = overlapsToday,
                    isBn = isBn,
                    onNavigateToRoutine = onNavigateToRoutine,
                    onTakeAttendance = onNavigateToAttendance,
                    onAddClass = { showClassTypeDialog = true }
                )
            }
        }

        // Demo Data Suggestion Card if Empty
        if (activeStudents.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = PastelBlueBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(PastelBlueIcon.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Book,
                                    contentDescription = null,
                                    tint = PastelBlueIcon,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (isBn) "সহজে ডেমো দিয়ে অ্যাপটি দেখুন" else "Explore with Demo Data",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isBn) {
                                "এক ক্লিকে কিছু নমুনা শিক্ষার্থী, ব্যাচ ও ফি যুক্ত করে অ্যাপের সকল প্রিমিয়াম ফিচার পরখ করুন।"
                            } else {
                                "Populate realistic sample students and coaching routines with one click to test all features."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            onClick = { viewModel.loadDemoData() },
                            shape = RoundedCornerShape(10.dp),
                            color = PrimaryBlue
                        ) {
                            Text(
                                text = if (isBn) "নমুনা ডেমো ডেটা যোগ করুন" else "Load Demo Data",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showClassTypeDialog) {
        SelectClassTypeDialog(
            isBn = isBn,
            onDismiss = { showClassTypeDialog = false },
            onSelectBatch = {
                showClassTypeDialog = false
                onOpenAddBatch()
            },
            onSelectHomeTuition = {
                showClassTypeDialog = false
                onOpenAddHomeTuition()
            }
        )
    }
}

/**
 * Event item card inspired directly by the "Upcoming Events" section of the Pinterest image.
 * Features left date pill, center session details, and right status pill.
 */
@Composable
fun PinterestEventCard(
    session: UnifiedSession,
    isBn: Boolean,
    onTakeAttendance: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onTakeAttendance),
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
            // Left Date / Type Badge Box (similar to MAY 24 Fri in reference image)
            Box(
                modifier = Modifier
                    .size(width = 54.dp, height = 58.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (session.type == "COACHING") PastelBlueBg else PastelGreenBg
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (session.type == "COACHING") Icons.Default.School else Icons.Default.Home,
                        contentDescription = null,
                        tint = if (session.type == "COACHING") PastelBlueIcon else PastelGreenIcon,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (session.type == "COACHING") "কোচিং" else "হোম",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (session.type == "COACHING") PastelBlueIcon else PastelGreenIcon
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = session.timeSlot,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (session.subtitle.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = session.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Right Status Chip (like "Upcoming" in reference image)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (session.type == "COACHING") PastelBlueBg else PastelGreenBg
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (session.type == "COACHING") "হাজিরা" else "টিউশন",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (session.type == "COACHING") PastelBlueIcon else PastelGreenIcon
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = if (session.type == "COACHING") PastelBlueIcon else PastelGreenIcon,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

/**
 * Dedicated Dashboard Routine / Time Table Card Widget
 * Displays daily scheduled classes, tutoring sessions, and reminders in a structured tabular/list format.
 */
@Composable
fun DashboardRoutineTableWidget(
    todaySessions: List<UnifiedSession>,
    overlapsToday: List<com.example.ui.viewmodel.OverlapItem>,
    isBn: Boolean,
    onNavigateToRoutine: () -> Unit,
    onTakeAttendance: () -> Unit,
    onAddClass: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.5.dp),
        border = BorderStroke(1.dp, BorderSubtle.copy(alpha = 0.65f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Widget Header: Title, Icon, Today Day Pill, Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(PastelBlueBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventNote,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isBn) "দৈনিক রুটিন ও সময়সূচি" else "Routine / Time Table",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        val cal = java.util.Calendar.getInstance()
                        val dayName = BengaliDateUtils.bengaliDaysFull[cal.get(java.util.Calendar.DAY_OF_WEEK)] ?: ""
                        val engDayName = BengaliDateUtils.englishDaysFull[cal.get(java.util.Calendar.DAY_OF_WEEK)] ?: ""
                        Text(
                            text = if (isBn) "আজ: $dayName" else "Today: $engDayName",
                            style = MaterialTheme.typography.labelSmall,
                            color = PrimaryBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Surface(
                    onClick = onNavigateToRoutine,
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF1F5F9),
                    border = BorderStroke(0.5.dp, BorderSubtle)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBn) "সকল রুটিন" else "Full Routine",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Reminders / Schedule Overlap Clash Banner inside Time Table
            if (overlapsToday.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                val s1 = overlapsToday.first().session1.title
                val s2 = overlapsToday.first().session2.title
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFEF2F2),
                    border = BorderStroke(1.dp, Color(0xFFFECACA)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBn) {
                                "আজকের ক্লাসে সময়ের ওভারল্যাপ: '$s1' এবং '$s2' এর সময় মিলে যাচ্ছে!"
                            } else {
                                "Schedule clash today: '$s1' and '$s2' overlap."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFB91C1C),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (todaySessions.isEmpty()) {
                // Empty timetable card state
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE2E8F0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (isBn) "আজকে কোনো ক্লাস বা সেশন নির্ধারিত নেই" else "No classes scheduled for today",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isBn) "ছুটির দিন অথবা রুটিনে নতুন ব্যাচ যুক্ত করতে পারেন" else "Enjoy your off day or schedule a new class",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onAddClass,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryBlue,
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBn) "নতুন ক্লাস যোগ করুন" else "Add New Class",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            } else {
                // Tabular Structured Header Row
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF1F5F9),
                    border = BorderStroke(0.5.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBn) "সময়সূচি" else "Time",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569),
                            modifier = Modifier.weight(1.3f)
                        )
                        Text(
                            text = if (isBn) "ক্লাস / ব্যাচ" else "Session",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569),
                            modifier = Modifier.weight(1.7f)
                        )
                        Text(
                            text = if (isBn) "ধরন" else "Type",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569),
                            modifier = Modifier.weight(0.9f)
                        )
                        Text(
                            text = if (isBn) "হাজিরা" else "Action",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569),
                            modifier = Modifier.weight(1.1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Tabular Rows for each of today's scheduled classes
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    todaySessions.forEach { session ->
                        RoutineTableRow(
                            session = session,
                            isBn = isBn,
                            onTakeAttendance = onTakeAttendance
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom summary indicator
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(0.5.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBn) {
                                "আজ মোট ${BengaliDateUtils.toBengaliDigits(todaySessions.size)}টি নির্ধারিত সেশন"
                            } else {
                                "Total ${todaySessions.size} sessions today"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )

                        Text(
                            text = if (isBn) "হাজিরা নিতে ট্যাপ করুন ➜" else "Tap row for attendance ➜",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tabular Row for single session in the Routine Widget
 */
@Composable
fun RoutineTableRow(
    session: UnifiedSession,
    isBn: Boolean,
    onTakeAttendance: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onTakeAttendance,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFAFAFA),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time Slot Column
            Column(modifier = Modifier.weight(1.3f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = session.timeSlot,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
            }

            // Class / Batch Details Column
            Column(modifier = Modifier.weight(1.7f)) {
                Text(
                    text = session.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (session.subtitle.isNotBlank()) {
                    Text(
                        text = session.subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Type Column (Badge)
            Box(modifier = Modifier.weight(0.9f)) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (session.type == "COACHING") PastelBlueBg else PastelGreenBg,
                    border = BorderStroke(
                        0.5.dp,
                        if (session.type == "COACHING") PastelBlueIcon.copy(alpha = 0.3f) else PastelGreenIcon.copy(alpha = 0.3f)
                    )
                ) {
                    Text(
                        text = if (session.type == "COACHING") (if (isBn) "কোচিং" else "Batch") else (if (isBn) "হোম" else "Home"),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (session.type == "COACHING") PastelBlueIcon else PastelGreenIcon,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            // Action Button Column
            Box(
                modifier = Modifier.weight(1.1f),
                contentAlignment = Alignment.CenterEnd
            ) {
                Surface(
                    onClick = onTakeAttendance,
                    shape = RoundedCornerShape(8.dp),
                    color = PrimaryBlue
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBn) "হাজিরা" else "Attend",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}

