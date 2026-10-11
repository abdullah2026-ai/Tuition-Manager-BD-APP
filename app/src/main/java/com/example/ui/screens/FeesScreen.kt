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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
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
import com.example.data.model.FeePayment
import com.example.data.model.Student
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.StatCard
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.BkashPink
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BorderVerySubtle
import com.example.ui.theme.CashGreen
import com.example.ui.theme.NagadOrange
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
fun FeesScreen(
    viewModel: TuitionViewModel,
    onOpenRecordPayment: (studentId: Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    val isBn by viewModel.isBengali.collectAsState()
    val activeStudents by viewModel.activeStudents.collectAsState()
    val allPayments by viewModel.allPayments.collectAsState()
    val allDues by viewModel.allDues.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Student Ledger, 1: Payment History
    val currentMonthYear = BengaliDateUtils.getCurrentMonthYear()

    val currentMonthPayments = allPayments.filter { it.monthYear == currentMonthYear }
    val totalCollected = currentMonthPayments.sumOf { it.amount }

    val currentMonthDues = allDues.filter { it.monthYear == currentMonthYear }
    val totalObligation = currentMonthDues.sumOf { it.dueAmount }
    val totalOutstanding = (totalObligation - totalCollected).coerceAtLeast(0.0)

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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Localization.get("fees_title", isBn),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PastelBlueBg
                        ) {
                            Text(
                                text = BengaliDateUtils.formatMonthYearDisplay(currentMonthYear, isBn),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = PastelBlueIcon,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = Localization.get("monthly_collected", isBn),
                            value = BengaliDateUtils.formatCurrency(totalCollected, isBn),
                            icon = Icons.Default.Payments,
                            iconTint = PastelYellowIcon,
                            containerColor = PastelYellowBg,
                            modifier = Modifier.weight(1f),
                            testTag = "fee_stat_collected"
                        )
                        StatCard(
                            title = Localization.get("monthly_dues", isBn),
                            value = BengaliDateUtils.formatCurrency(totalOutstanding, isBn),
                            icon = Icons.Default.Payments,
                            iconTint = if (totalOutstanding > 0) PastelRoseIcon else PastelGreenIcon,
                            containerColor = if (totalOutstanding > 0) PastelRoseBg else PastelGreenBg,
                            modifier = Modifier.weight(1f),
                            testTag = "fee_stat_dues"
                        )
                    }

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
                // Student Ledger List
                if (activeStudents.isEmpty()) {
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
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 90.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(activeStudents, key = { it.id }) { student ->
                            val paid = currentMonthPayments.filter { it.studentId == student.id }.sumOf { it.amount }
                            val dueObligation = currentMonthDues.find { it.studentId == student.id }?.dueAmount ?: student.monthlyFee
                            val balance = (dueObligation - paid).coerceAtLeast(0.0)

                            StudentFeeLedgerCard(
                                student = student,
                                paid = paid,
                                dueObligation = dueObligation,
                                balance = balance,
                                isBn = isBn,
                                onCollectFee = { onOpenRecordPayment(student.id) }
                            )
                        }
                    }
                }
            } else {
                // Payment History Log
                if (allPayments.isEmpty()) {
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
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 90.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(allPayments, key = { it.id }) { payment ->
                            val student = activeStudents.find { it.id == payment.studentId }
                                ?: com.example.data.model.Student(id = payment.studentId, name = "শিক্ষার্থী")

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
    }
}

@Composable
fun StudentFeeLedgerCard(
    student: Student,
    paid: Double,
    dueObligation: Double,
    balance: Double,
    isBn: Boolean,
    onCollectFee: () -> Unit
) {
    val status = when {
        paid >= dueObligation && dueObligation > 0 -> "PAID"
        paid > 0 -> "PARTIAL"
        else -> "DUE"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("student_fee_card_${student.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.5.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle.copy(alpha = 0.6f))
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
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleMedium,
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

                // Pastel Status Badge
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
                    Text(text = "নির্ধারিত ফি", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = BengaliDateUtils.formatCurrency(dueObligation, isBn),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Column {
                    Text(text = "পরিশোধিত", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
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
            .testTag("payment_item_${payment.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.5.dp),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Method pill
                    val methodColor = when (payment.paymentMethod) {
                        "BKASH" -> BkashPink
                        "NAGAD" -> NagadOrange
                        "CASH" -> CashGreen
                        else -> PrimaryBlue
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = methodColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = when (payment.paymentMethod) {
                                "BKASH" -> "bKash"
                                "NAGAD" -> "Nagad"
                                "CASH" -> "Cash"
                                "BANK" -> "Bank"
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
