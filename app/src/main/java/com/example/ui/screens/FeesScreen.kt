package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FeePayment
import com.example.data.model.Student
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.StatCard
import com.example.ui.dialogs.MonthYearPickerDialog
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

enum class PaymentStatusFilter {
    ALL, PAID, PARTIAL, DUE
}

data class StudentFeeCalculation(
    val student: Student,
    val expectedFee: Double,
    val paid: Double,
    val balance: Double,
    val advance: Double,
    val status: String // "PAID", "PARTIAL", "DUE"
)

@Composable
fun FeesScreen(
    viewModel: TuitionViewModel,
    onOpenRecordPayment: (studentId: Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    val isBn by viewModel.isBengali.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()
    val activeStudents by viewModel.activeStudents.collectAsState()
    val allPayments by viewModel.allPayments.collectAsState()
    val allDues by viewModel.allDues.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Student Ledger, 1: Payment History
    var selectedMonthYear by remember { mutableStateOf(BengaliDateUtils.getCurrentMonthYear()) }
    var showMonthPicker by remember { mutableStateOf(false) }
    var selectedStatusFilter by remember { mutableStateOf(PaymentStatusFilter.ALL) }
    var showAllPaymentsInHistory by remember { mutableStateOf(false) }

    val isFutureMonth = remember(selectedMonthYear) { BengaliDateUtils.isFutureMonthYear(selectedMonthYear) }

    // Relevant students for selectedMonthYear: active students plus any archived student with history in this month
    val relevantStudents = remember(activeStudents, allStudents, allPayments, allDues, selectedMonthYear) {
        val archivedWithHistory = allStudents.filter { s ->
            s.isArchived && (
                allPayments.any { it.studentId == s.id && it.monthYear == selectedMonthYear } ||
                allDues.any { it.studentId == s.id && it.monthYear == selectedMonthYear }
            )
        }
        activeStudents + archivedWithHistory
    }

    // Calculate fee breakdown for each student in the selected billing period
    val studentFeeItems = remember(relevantStudents, allPayments, allDues, selectedMonthYear) {
        relevantStudents.map { student ->
            val monthDueOverride = allDues.find { it.studentId == student.id && it.monthYear == selectedMonthYear }
            val expectedFee = monthDueOverride?.dueAmount ?: student.monthlyFee
            val studentPayments = allPayments.filter { it.studentId == student.id && it.monthYear == selectedMonthYear }
            val paid = studentPayments.sumOf { it.amount }
            val balance = (expectedFee - paid).coerceAtLeast(0.0)
            val advance = (paid - expectedFee).coerceAtLeast(0.0)

            val status = when {
                expectedFee <= 0.0 -> "PAID"
                paid >= expectedFee -> "PAID"
                paid > 0.0 -> "PARTIAL"
                else -> "DUE"
            }

            StudentFeeCalculation(
                student = student,
                expectedFee = expectedFee,
                paid = paid,
                balance = balance,
                advance = advance,
                status = status
            )
        }
    }

    // Overall summary metrics for selected month
    val totalExpected = remember(studentFeeItems) { studentFeeItems.sumOf { it.expectedFee } }
    val currentMonthPayments = remember(allPayments, selectedMonthYear) {
        allPayments.filter { it.monthYear == selectedMonthYear }
    }
    val totalCollected = remember(currentMonthPayments) { currentMonthPayments.sumOf { it.amount } }
    val totalOutstanding = remember(totalExpected, totalCollected) {
        (totalExpected - totalCollected).coerceAtLeast(0.0)
    }

    // Counts for filter chips
    val allCount = studentFeeItems.size
    val paidCount = studentFeeItems.count { it.status == "PAID" }
    val partialCount = studentFeeItems.count { it.status == "PARTIAL" }
    val dueCount = studentFeeItems.count { it.status == "DUE" }

    // Combined filtered student list
    val filteredStudents = remember(studentFeeItems, selectedStatusFilter) {
        when (selectedStatusFilter) {
            PaymentStatusFilter.ALL -> studentFeeItems
            PaymentStatusFilter.PAID -> studentFeeItems.filter { it.status == "PAID" }
            PaymentStatusFilter.PARTIAL -> studentFeeItems.filter { it.status == "PARTIAL" }
            PaymentStatusFilter.DUE -> studentFeeItems.filter { it.status == "DUE" }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("fees_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Summary Header Card
            Surface(
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header title & Month Picker
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = Localization.get("fees_title", isBn),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            if (isFutureMonth) {
                                Text(
                                    text = "(${Localization.get("future_month_tag", isBn)})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PrimaryBlue,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Month & Year Selector Controls
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            IconButton(
                                onClick = { selectedMonthYear = BengaliDateUtils.getPreviousMonthYear(selectedMonthYear) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("btn_fee_prev_month")
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Previous Month",
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Surface(
                                onClick = { showMonthPicker = true },
                                shape = RoundedCornerShape(12.dp),
                                color = PastelBlueBg,
                                border = BorderStroke(1.dp, BorderSubtle.copy(alpha = 0.5f)),
                                modifier = Modifier.testTag("btn_fee_month_picker")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = PastelBlueIcon,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = BengaliDateUtils.formatMonthYearDisplay(selectedMonthYear, isBn),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = PastelBlueIcon
                                    )
                                }
                            }

                            IconButton(
                                onClick = { selectedMonthYear = BengaliDateUtils.getNextMonthYear(selectedMonthYear) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("btn_fee_next_month")
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Next Month",
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Dedicated Earning & Fee Collection Dashboard Card
                    FeeEarningDashboardWidget(
                        totalExpected = totalExpected,
                        totalCollected = totalCollected,
                        totalOutstanding = totalOutstanding,
                        isBn = isBn
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Tab Row
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
                                    if (isBn) "শিক্ষার্থীভিত্তিক হিসাব" else "Student Ledger",
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = {
                                Text(
                                    if (isBn) "পরিশোধের ইতিহাস" else "Payment Log",
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }

            // Tab Content
            if (selectedTab == 0) {
                // Payment Status Filter Chips Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val countAllStr = if (isBn) BengaliDateUtils.toBengaliDigits(allCount) else allCount.toString()
                    val countPaidStr = if (isBn) BengaliDateUtils.toBengaliDigits(paidCount) else paidCount.toString()
                    val countPartialStr = if (isBn) BengaliDateUtils.toBengaliDigits(partialCount) else partialCount.toString()
                    val countDueStr = if (isBn) BengaliDateUtils.toBengaliDigits(dueCount) else dueCount.toString()

                    FilterChip(
                        selected = selectedStatusFilter == PaymentStatusFilter.ALL,
                        onClick = { selectedStatusFilter = PaymentStatusFilter.ALL },
                        label = { Text("${Localization.get("filter_all", isBn)} ($countAllStr)") },
                        modifier = Modifier.testTag("filter_chip_all"),
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryBlue,
                            selectedLabelColor = Color.White
                        )
                    )

                    FilterChip(
                        selected = selectedStatusFilter == PaymentStatusFilter.PAID,
                        onClick = { selectedStatusFilter = PaymentStatusFilter.PAID },
                        label = { Text("${Localization.get("filter_paid", isBn)} ($countPaidStr)") },
                        modifier = Modifier.testTag("filter_chip_paid"),
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = com.example.ui.theme.PastelGreenText,
                            selectedLabelColor = Color.White
                        )
                    )

                    FilterChip(
                        selected = selectedStatusFilter == PaymentStatusFilter.PARTIAL,
                        onClick = { selectedStatusFilter = PaymentStatusFilter.PARTIAL },
                        label = { Text("${Localization.get("filter_partial", isBn)} ($countPartialStr)") },
                        modifier = Modifier.testTag("filter_chip_partial"),
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = com.example.ui.theme.PastelYellowText,
                            selectedLabelColor = Color.White
                        )
                    )

                    FilterChip(
                        selected = selectedStatusFilter == PaymentStatusFilter.DUE,
                        onClick = { selectedStatusFilter = PaymentStatusFilter.DUE },
                        label = { Text("${Localization.get("filter_due", isBn)} ($countDueStr)") },
                        modifier = Modifier.testTag("filter_chip_due"),
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = com.example.ui.theme.PastelRoseText,
                            selectedLabelColor = Color.White
                        )
                    )
                }

                // Student Ledger List
                if (relevantStudents.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyStateCard(
                            message = if (isBn) "কোন শিক্ষার্থী নেই। প্রথমে শিক্ষার্থী যোগ করুন।" else "No students enrolled.",
                            icon = Icons.Default.Payments
                        )
                    }
                } else if (filteredStudents.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyStateCard(
                            message = if (isBn) "এই ফিল্টারে কোন শিক্ষার্থী নেই" else "No students match this filter",
                            icon = Icons.Default.Payments
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 90.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredStudents, key = { it.student.id }) { item ->
                            StudentFeeLedgerCard(
                                student = item.student,
                                paid = item.paid,
                                dueObligation = item.expectedFee,
                                balance = item.balance,
                                advance = item.advance,
                                isBn = isBn,
                                onCollectFee = { onOpenRecordPayment(item.student.id) }
                            )
                        }
                    }
                }
            } else {
                // Payment History Log Tab with Period Filter Toggle
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (showAllPaymentsInHistory)
                                Localization.get("all_time_payments", isBn)
                            else
                                "${Localization.get("month_payments", isBn)} (${BengaliDateUtils.formatMonthYearDisplay(selectedMonthYear, isBn)})",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )

                        Surface(
                            onClick = { showAllPaymentsInHistory = !showAllPaymentsInHistory },
                            shape = RoundedCornerShape(10.dp),
                            color = PastelBlueBg,
                            border = BorderStroke(1.dp, BorderSubtle.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = if (showAllPaymentsInHistory)
                                    (if (isBn) "শুধু চলতি মাস" else "Current Month Only")
                                else
                                    (if (isBn) "সব ইতিহাস দেখুন" else "View All History"),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    val displayPayments = if (showAllPaymentsInHistory) allPayments else currentMonthPayments

                    if (displayPayments.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            EmptyStateCard(
                                message = if (isBn) "এখনও কোন বেতন পরিশোধের তথ্য রেকর্ড করা হয়নি" else "No payment records found.",
                                icon = Icons.Default.Payments
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 90.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(displayPayments, key = { it.id }) { payment ->
                                val student = allStudents.find { it.id == payment.studentId }
                                    ?: Student(id = payment.studentId, name = "শিক্ষার্থী")

                                PaymentHistoryItemCard(
                                    payment = payment,
                                    student = student,
                                    isBn = isBn,
                                    onDelete = { viewModel.deletePayment(payment.id) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { onOpenRecordPayment(null) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 18.dp, bottom = 82.dp)
                .testTag("fab_record_payment"),
            containerColor = PrimaryBlue,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = Localization.get("record_payment", isBn))
        }

        // Month & Year Picker Dialog
        if (showMonthPicker) {
            MonthYearPickerDialog(
                initialMonthYear = selectedMonthYear,
                isBn = isBn,
                onDismiss = { showMonthPicker = false },
                onConfirm = { newMonthYear ->
                    selectedMonthYear = newMonthYear
                    showMonthPicker = false
                }
            )
        }
    }
}

@Composable
fun StudentFeeLedgerCard(
    student: Student,
    paid: Double,
    dueObligation: Double,
    balance: Double,
    advance: Double = 0.0,
    isBn: Boolean,
    onCollectFee: () -> Unit
) {
    val status = when {
        dueObligation <= 0.0 -> "PAID"
        paid >= dueObligation -> "PAID"
        paid > 0.0 -> "PARTIAL"
        else -> "DUE"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("student_fee_card_${student.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.5.dp),
        border = BorderStroke(1.dp, BorderSubtle.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = student.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        if (student.isArchived) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFF1F5F9)
                            ) {
                                Text(
                                    text = "[${Localization.get("archived_tag", isBn)}]",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${student.className} ${if (student.studentId.isNotBlank()) "• রোল: ${student.studentId}" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                // Pastel Status Badge
                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = when (status) {
                            "PAID" -> PastelGreenBg
                            "PARTIAL" -> PastelYellowBg
                            else -> PastelRoseBg
                        }
                    ) {
                        Text(
                            text = when (status) {
                                "PAID" -> Localization.get("paid_status", isBn)
                                "PARTIAL" -> Localization.get("partial_status", isBn)
                                else -> Localization.get("due_status", isBn)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = when (status) {
                                "PAID" -> com.example.ui.theme.PastelGreenText
                                "PARTIAL" -> com.example.ui.theme.PastelYellowText
                                else -> com.example.ui.theme.PastelRoseText
                            },
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    if (advance > 0.0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "(${Localization.get("advance_amount", isBn)}: ${BengaliDateUtils.formatCurrency(advance, isBn)})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = com.example.ui.theme.PastelGreenText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = BorderVerySubtle)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = Localization.get("total_expected", isBn), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = BengaliDateUtils.formatCurrency(dueObligation, isBn),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Column {
                    Text(text = Localization.get("amount_paid", isBn), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = BengaliDateUtils.formatCurrency(paid, isBn),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = com.example.ui.theme.PastelGreenText
                    )
                }

                Column {
                    Text(text = "বাকি", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = BengaliDateUtils.formatCurrency(balance, isBn),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (balance > 0) com.example.ui.theme.PastelRoseText else com.example.ui.theme.PastelGreenText
                    )
                }

                // Action button: Pay
                Surface(
                    onClick = onCollectFee,
                    shape = RoundedCornerShape(12.dp),
                    color = PrimaryBlue,
                    shadowElevation = 2.dp
                ) {
                    Text(
                        text = Localization.get("record_payment", isBn),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PaymentHistoryItemCard(
    payment: FeePayment,
    student: Student,
    isBn: Boolean,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("payment_history_card_${payment.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.5.dp),
        border = BorderStroke(1.dp, BorderSubtle.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    val methodBg: Color
                    val methodColor: Color
                    when (payment.paymentMethod) {
                        "BKASH" -> {
                            methodBg = com.example.ui.theme.BkashBg
                            methodColor = com.example.ui.theme.BkashPink
                        }
                        "NAGAD" -> {
                            methodBg = com.example.ui.theme.NagadBg
                            methodColor = com.example.ui.theme.NagadOrange
                        }
                        "CASH" -> {
                            methodBg = PastelGreenBg
                            methodColor = com.example.ui.theme.PastelGreenText
                        }
                        else -> {
                            methodBg = PastelBlueBg
                            methodColor = PrimaryBlue
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = methodBg
                    ) {
                        Text(
                            text = when (payment.paymentMethod) {
                                "BKASH" -> "bKash"
                                "NAGAD" -> "Nagad"
                                "CASH" -> if (isBn) "নগদ" else "Cash"
                                "BANK" -> if (isBn) "ব্যাংক" else "Bank"
                                else -> "Other"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = methodColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "মাস: ${BengaliDateUtils.formatMonthYearDisplay(payment.monthYear, isBn)} • তারিখ: ${payment.paymentDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                if (payment.notes.isNotBlank()) {
                    Text(
                        text = "নোট: ${payment.notes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = BengaliDateUtils.formatCurrency(payment.amount, isBn),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PastelGreenIcon
                )

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

/**
 * Modern, uncropped Earning & Collection Dashboard Card with clear layer management:
 * 1. Progress header displaying collection percentage and visual indicator.
 * 2. Three vertically structured metric boxes (Total Expected, Collected, and Due)
 *    ensuring numbers and titles are 100% visible, crisp, and never cut off.
 */
@Composable
fun FeeEarningDashboardWidget(
    totalExpected: Double,
    totalCollected: Double,
    totalOutstanding: Double,
    isBn: Boolean,
    modifier: Modifier = Modifier
) {
    val progress = if (totalExpected > 0) {
        (totalCollected / totalExpected).toFloat().coerceIn(0f, 1f)
    } else 0f
    val percentInt = (progress * 100).toInt()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("fee_earning_dashboard"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        border = BorderStroke(1.dp, BorderSubtle.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Layer 1: Earning & Collection Progress Bar Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlue.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = if (isBn) "আয় ও আদায় অগ্রগতি" else "Earning & Collection Rate",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (percentInt >= 100) PastelGreenIcon.copy(alpha = 0.14f) else PrimaryBlue.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = if (isBn) "${BengaliDateUtils.toBengaliDigits(percentInt)}% আদায়" else "$percentInt% Collected",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (percentInt >= 100) PastelGreenIcon else PrimaryBlue,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Smooth Rounded Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFFE2E8F0))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = progress)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (percentInt >= 100) PastelGreenIcon else PrimaryBlue)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Layer 2: Adaptive Responsive Stat Cards (Grid on compact screens, Row on wider screens)
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val availableWidth = maxWidth
                if (availableWidth < 360.dp) {
                    // 2-Row Adaptive Grid layout for small screens to prevent text cropping
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FeeMetricBox(
                                title = Localization.get("total_expected", isBn),
                                value = BengaliDateUtils.formatCurrency(totalExpected, isBn),
                                icon = Icons.Default.AccountBalanceWallet,
                                tint = PastelBlueIcon,
                                bg = PastelBlueBg,
                                modifier = Modifier.weight(1f),
                                testTag = "fee_stat_expected"
                            )
                            FeeMetricBox(
                                title = Localization.get("total_collected", isBn),
                                value = BengaliDateUtils.formatCurrency(totalCollected, isBn),
                                icon = Icons.Default.CheckCircle,
                                tint = PastelGreenIcon,
                                bg = PastelGreenBg,
                                modifier = Modifier.weight(1f),
                                testTag = "fee_stat_collected"
                            )
                        }

                        FeeMetricBox(
                            title = Localization.get("total_outstanding", isBn),
                            value = BengaliDateUtils.formatCurrency(totalOutstanding, isBn),
                            icon = Icons.Default.ReceiptLong,
                            tint = if (totalOutstanding > 0) PastelRoseIcon else PastelGreenIcon,
                            bg = if (totalOutstanding > 0) PastelRoseBg else PastelGreenBg,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "fee_stat_dues"
                        )
                    }
                } else {
                    // Standard 3-column Row layout on wider displays
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FeeMetricBox(
                            title = Localization.get("total_expected", isBn),
                            value = BengaliDateUtils.formatCurrency(totalExpected, isBn),
                            icon = Icons.Default.AccountBalanceWallet,
                            tint = PastelBlueIcon,
                            bg = PastelBlueBg,
                            modifier = Modifier.weight(1f),
                            testTag = "fee_stat_expected"
                        )
                        FeeMetricBox(
                            title = Localization.get("total_collected", isBn),
                            value = BengaliDateUtils.formatCurrency(totalCollected, isBn),
                            icon = Icons.Default.CheckCircle,
                            tint = PastelGreenIcon,
                            bg = PastelGreenBg,
                            modifier = Modifier.weight(1f),
                            testTag = "fee_stat_collected"
                        )
                        FeeMetricBox(
                            title = Localization.get("total_outstanding", isBn),
                            value = BengaliDateUtils.formatCurrency(totalOutstanding, isBn),
                            icon = Icons.Default.ReceiptLong,
                            tint = if (totalOutstanding > 0) PastelRoseIcon else PastelGreenIcon,
                            bg = if (totalOutstanding > 0) PastelRoseBg else PastelGreenBg,
                            modifier = Modifier.weight(1f),
                            testTag = "fee_stat_dues"
                        )
                    }
                }
            }
        }
    }
}

/**
 * Single metric box for fee summary cards.
 * Avoids horizontal cramming by using a vertical stack:
 * Icon badge (top) -> Bold Value (middle) -> 2-line Label (bottom).
 * Features responsive dynamic typography and adaptive padding to prevent cropping.
 */
@Composable
fun FeeMetricBox(
    title: String,
    value: String,
    icon: ImageVector,
    tint: Color,
    bg: Color,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Surface(
        modifier = modifier.testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        color = bg,
        border = BorderStroke(1.dp, tint.copy(alpha = 0.22f))
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isCompact = maxWidth < 115.dp
            val padH = if (isCompact) 7.dp else 10.dp
            val padV = if (isCompact) 8.dp else 10.dp
            val badgeSize = if (isCompact) 24.dp else 27.dp
            val iconSize = if (isCompact) 13.dp else 15.dp

            val valSize = when {
                isCompact && value.length > 7 -> 12.5.sp
                value.length > 9 -> 13.5.sp
                isCompact || value.length > 7 -> 14.5.sp
                else -> 16.sp
            }
            val titleSize = if (isCompact || title.length > 12) 10.sp else 11.5.sp
            val titleLineHeight = if (isCompact || title.length > 12) 12.5.sp else 14.sp

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = padH, vertical = padV),
                horizontalAlignment = Alignment.Start
            ) {
                // Layer 1: Compact circular icon badge at top
                Box(
                    modifier = Modifier
                        .size(badgeSize)
                        .clip(CircleShape)
                        .background(tint.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = tint,
                        modifier = Modifier.size(iconSize)
                    )
                }

                Spacer(modifier = Modifier.height(if (isCompact) 5.dp else 7.dp))

                // Layer 2: Main Value - bold, clear, dynamically scaled
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = valSize,
                        fontWeight = FontWeight.Bold,
                        lineHeight = (valSize.value + 3).sp
                    ),
                    color = TextPrimary,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.5.dp))

                // Layer 3: Title / Label - wraps to 2 lines so never cropped
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = titleSize,
                        fontWeight = FontWeight.Medium,
                        lineHeight = titleLineHeight
                    ),
                    color = TextSecondary,
                    maxLines = 2,
                    minLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
