package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.example.ui.components.FormModalContainer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.components.ModalHeader
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BorderVerySubtle
import com.example.ui.theme.PastelBlueBg
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.BengaliDateUtils
import com.example.util.Localization
import java.util.Calendar

@Composable
fun MonthYearPickerDialog(
    initialMonthYear: String,
    isBn: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val initial = remember(initialMonthYear) { BengaliDateUtils.parseYearMonth(initialMonthYear) }
    var selectedYear by remember { mutableIntStateOf(initial.first) }
    var selectedMonth by remember { mutableIntStateOf(initial.second) } // 1..12

    val currentCal = remember { Calendar.getInstance() }
    val currentYear = currentCal.get(Calendar.YEAR)
    val currentMonth = currentCal.get(Calendar.MONTH) + 1

    FormModalContainer(
        title = Localization.get("select_month_year", isBn),
        purposeSubtitle = if (isBn) "হিসাব ও রসিদ দেখতে নির্দিষ্ট মাস ও বছর নির্বাচন করুন" else "Select billing month & year to view records",
        icon = Icons.Default.DateRange,
        headerColor = Color(0xFF4F46E5),
        onDismiss = onDismiss,
        confirmButtonText = Localization.get("save", isBn),
        dismissButtonText = Localization.get("cancel", isBn),
        confirmButtonTestTag = "btn_confirm_month_year",
        dismissButtonTestTag = "btn_cancel_month_year",
        dialogTestTag = "dialog_month_year_picker",
        onConfirm = {
            val result = BengaliDateUtils.formatYearMonth(selectedYear, selectedMonth)
            onConfirm(result)
        }
    ) {
        // Year Selector Header
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = PastelBlueBg.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, BorderSubtle.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { selectedYear -= 1 },
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("btn_prev_year")
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = if (isBn) "পূর্ববর্তী বছর" else "Previous Year",
                                tint = PrimaryBlue
                            )
                        }

                        Text(
                            text = if (isBn) BengaliDateUtils.toBengaliDigits(selectedYear) else selectedYear.toString(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )

                        IconButton(
                            onClick = { selectedYear += 1 },
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("btn_next_year")
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = if (isBn) "পরবর্তী বছর" else "Next Year",
                                tint = PrimaryBlue
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick selector buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            selectedYear = currentYear
                            selectedMonth = currentMonth
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                        border = BorderStroke(1.dp, if (selectedYear == currentYear && selectedMonth == currentMonth) PrimaryBlue else BorderSubtle)
                    ) {
                        Text(
                            text = if (isBn) "বর্তমান মাস" else "Current",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedYear == currentYear && selectedMonth == currentMonth) PrimaryBlue else TextSecondary,
                            maxLines = 1
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            val prev = BengaliDateUtils.getPreviousMonthYear(BengaliDateUtils.formatYearMonth(selectedYear, selectedMonth))
                            val (py, pm) = BengaliDateUtils.parseYearMonth(prev)
                            selectedYear = py
                            selectedMonth = pm
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                        border = BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Text(
                            text = if (isBn) "পূর্ববর্তী" else "Previous",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            val next = BengaliDateUtils.getNextMonthYear(BengaliDateUtils.formatYearMonth(selectedYear, selectedMonth))
                            val (ny, nm) = BengaliDateUtils.parseYearMonth(next)
                            selectedYear = ny
                            selectedMonth = nm
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                        border = BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Text(
                            text = if (isBn) "পরবর্তী" else "Next",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 12 Months Grid (3 rows x 4 columns)
                val months = if (isBn) BengaliDateUtils.bengaliMonths else BengaliDateUtils.englishMonths
                for (row in 0 until 3) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (col in 0 until 4) {
                            val monthIdx = row * 4 + col // 0..11
                            val monthNum = monthIdx + 1
                            val isSelected = selectedMonth == monthNum
                            val isCurrent = selectedYear == currentYear && monthNum == currentMonth

                            Surface(
                                onClick = { selectedMonth = monthNum },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 4.dp)
                                    .testTag("month_btn_$monthNum"),
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) PrimaryBlue else if (isCurrent) PastelBlueBg else Color(0xFFF8FAFC),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) PrimaryBlue else if (isCurrent) BorderSubtle else BorderVerySubtle
                                ),
                                shadowElevation = if (isSelected) 2.dp else 0.dp
                            ) {
                                Text(
                                    text = months[monthIdx],
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isSelected || isCurrent) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else if (isCurrent) PrimaryBlue else TextPrimary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 2.dp),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
