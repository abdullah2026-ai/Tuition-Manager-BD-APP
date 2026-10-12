package com.example.ui.dialogs

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.example.ui.components.FormModalContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.components.ModalHeader
import com.example.ui.components.modalTextFieldColors
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.FeePayment
import com.example.data.model.Student
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.BkashPink
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CashGreen
import com.example.ui.theme.NagadOrange
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.BengaliDateUtils
import com.example.util.Localization

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecordPaymentDialog(
    allStudents: List<Student>,
    preselectedStudentId: Long? = null,
    isBn: Boolean = true,
    onDismiss: () -> Unit,
    onSavePayment: (FeePayment) -> Unit
) {
    var selectedStudentId by remember {
        mutableStateOf(preselectedStudentId ?: (allStudents.firstOrNull()?.id ?: 0L))
    }

    val currentMonthYear = BengaliDateUtils.getCurrentMonthYear()
    var selectedMonthYear by remember { mutableStateOf(currentMonthYear) }

    val student = allStudents.find { it.id == selectedStudentId }

    var amountStr by remember(student) {
        mutableStateOf(if (student != null && student.monthlyFee > 0) student.monthlyFee.toInt().toString() else "")
    }

    var selectedMethod by remember { mutableStateOf("CASH") } // "CASH", "BKASH", "NAGAD", "BANK", "OTHER"
    var paymentDate by remember { mutableStateOf(BengaliDateUtils.getTodayDateString()) }
    var notes by remember { mutableStateOf("") }

    var isSubmitting by remember { mutableStateOf(false) }

    val paymentMethods = listOf(
        "CASH" to Localization.get("cash", isBn),
        "BKASH" to Localization.get("bkash", isBn),
        "NAGAD" to Localization.get("nagad", isBn),
        "BANK" to Localization.get("bank", isBn),
        "OTHER" to Localization.get("other", isBn)
    )

    FormModalContainer(
        title = Localization.get("record_payment", isBn),
        purposeSubtitle = if (isBn) "শিক্ষার্থীর মাসিক বেতন আদায় ও রসিদ সংরক্ষণ" else "Collect student monthly fee & save receipt",
        icon = Icons.Default.Payments,
        headerColor = Color(0xFF16A34A),
        onDismiss = onDismiss,
        confirmButtonText = Localization.get("save", isBn),
        dismissButtonText = Localization.get("cancel", isBn),
        confirmButtonTestTag = "btn_submit_payment",
        dismissButtonTestTag = "btn_cancel_payment",
        dialogTestTag = "dialog_record_payment",
        confirmButtonEnabled = !isSubmitting,
        onConfirm = {
            if (isSubmitting || selectedStudentId == 0L) return@FormModalContainer
            val amt = amountStr.toDoubleOrNull() ?: 0.0
            if (amt <= 0) return@FormModalContainer

            isSubmitting = true
            val payment = FeePayment(
                studentId = selectedStudentId,
                monthYear = selectedMonthYear.trim(),
                amount = amt,
                paymentDate = paymentDate.trim(),
                paymentMethod = selectedMethod,
                notes = notes.trim()
            )
            onSavePayment(payment)
        }
    ) {
        // Select Student
        Text(
                    text = Localization.get("student_name", isBn) + " *",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                if (allStudents.isEmpty()) {
                    Text(
                        text = if (isBn) "কোন শিক্ষার্থী নেই। প্রথমে শিক্ষার্থী যোগ করুন।" else "No students found.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFDC2626)
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                            .padding(6.dp)
                    ) {
                        allStudents.forEach { s ->
                            val isChosen = s.id == selectedStudentId
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedStudentId = s.id
                                        if (s.monthlyFee > 0) {
                                            amountStr = s.monthlyFee.toInt().toString()
                                        }
                                    }
                                    .padding(vertical = 5.dp, horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (isChosen) PrimaryBlue else Color.Transparent,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isChosen) PrimaryBlue else TextSecondary),
                                    modifier = Modifier.size(16.dp)
                                ) {
                                    if (isChosen) {
                                        androidx.compose.material3.Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${s.name} (${s.className})",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }

                // Month
                OutlinedTextField(
                    value = selectedMonthYear,
                    onValueChange = { selectedMonthYear = it },
                    label = { Text(Localization.get("fee_month", isBn) + " (YYYY-MM)") },
                    supportingText = {
                        Text(BengaliDateUtils.formatMonthYearDisplay(selectedMonthYear, isBn))
                    },
                    modifier = Modifier.fillMaxWidth().testTag("input_fee_month"),
                    shape = RoundedCornerShape(12.dp),
                    colors = modalTextFieldColors(),
                    singleLine = true
                )

                // Amount
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text(Localization.get("amount_paid", isBn) + " (টাকা) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("input_payment_amount"),
                    shape = RoundedCornerShape(12.dp),
                    colors = modalTextFieldColors(),
                    singleLine = true
                )

                // Payment Method Selector
                Text(
                    text = Localization.get("payment_method", isBn),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    paymentMethods.forEach { (methodCode, label) ->
                        val isSelected = selectedMethod == methodCode
                        val badgeColor = when (methodCode) {
                            "BKASH" -> BkashPink
                            "NAGAD" -> NagadOrange
                            "CASH" -> CashGreen
                            else -> PrimaryBlue
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) badgeColor else Color(0xFFF1F5F9),
                            modifier = Modifier.clickable { selectedMethod = methodCode }
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else TextPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Date
                OutlinedTextField(
                    value = paymentDate,
                    onValueChange = { paymentDate = it },
                    label = { Text(Localization.get("payment_date", isBn) + " (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth().testTag("input_payment_date"),
                    shape = RoundedCornerShape(12.dp),
                    colors = modalTextFieldColors(),
                    singleLine = true
                )

                // Notes / TrxID
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("রশিদ বা ট্রানজাকশন নোট (ঐচ্ছিক)") },
                    placeholder = { Text("নগদ প্রাপ্ত / বিকাশ TrxID") },
                    modifier = Modifier.fillMaxWidth().testTag("input_payment_notes"),
                    shape = RoundedCornerShape(12.dp),
                    colors = modalTextFieldColors(),
                    singleLine = true
                )
    }
}
