package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import com.example.ui.components.FormModalContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.ui.components.ModalHeader
import com.example.ui.components.TimePickerInputField
import com.example.ui.components.modalTextFieldColors
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.CoachingBatch
import com.example.data.model.Student
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.BengaliDateUtils
import com.example.util.Localization

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditBatchDialog(
    batchToEdit: CoachingBatch? = null,
    allStudents: List<Student>,
    assignedStudentIds: List<Long> = emptyList(),
    isBn: Boolean = true,
    onDismiss: () -> Unit,
    onSave: (batch: CoachingBatch, studentIds: List<Long>) -> Unit
) {
    var batchName by remember { mutableStateOf(batchToEdit?.batchName.orEmpty()) }
    var className by remember { mutableStateOf(batchToEdit?.className.orEmpty()) }
    var subject by remember { mutableStateOf(batchToEdit?.subject.orEmpty()) }
    var startTime by remember { mutableStateOf(batchToEdit?.startTime ?: "08:00") }
    var endTime by remember { mutableStateOf(batchToEdit?.endTime ?: "09:30") }
    var feeStr by remember {
        mutableStateOf(if (batchToEdit != null && batchToEdit.monthlyFee > 0) batchToEdit.monthlyFee.toInt().toString() else "")
    }

    val selectedDays = remember {
        mutableStateListOf<String>().apply {
            if (batchToEdit != null && batchToEdit.daysOfWeek.isNotBlank()) {
                addAll(batchToEdit.daysOfWeek.split(",").map { it.trim() })
            } else {
                addAll(listOf("SAT", "MON", "WED"))
            }
        }
    }

    val selectedStudents = remember {
        mutableStateListOf<Long>().apply {
            addAll(assignedStudentIds)
        }
    }

    var isNameError by remember { mutableStateOf(false) }

    FormModalContainer(
        title = if (batchToEdit == null) Localization.get("add_batch", isBn) else Localization.get("batch_name", isBn),
        purposeSubtitle = if (isBn) "কোচিং ব্যাচ, ক্লাসের রুটিন ও শিক্ষার্থী নির্বাচন" else "Batch routine, schedule & student enrollment",
        icon = Icons.Default.School,
        headerColor = Color(0xFFEA580C),
        onDismiss = onDismiss,
        confirmButtonText = Localization.get("save", isBn),
        dismissButtonText = Localization.get("cancel", isBn),
        confirmButtonTestTag = "btn_save_batch",
        dismissButtonTestTag = "btn_cancel_batch",
        dialogTestTag = "dialog_add_edit_batch",
        onConfirm = {
            if (batchName.isBlank()) {
                isNameError = true
                return@FormModalContainer
            }
            val days = selectedDays.joinToString(",")
            val feeDouble = feeStr.toDoubleOrNull() ?: 0.0
            val result = batchToEdit?.copy(
                batchName = batchName.trim(),
                className = className.trim(),
                subject = subject.trim(),
                daysOfWeek = days,
                startTime = startTime.trim(),
                endTime = endTime.trim(),
                monthlyFee = feeDouble
            ) ?: CoachingBatch(
                batchName = batchName.trim(),
                className = className.trim(),
                subject = subject.trim(),
                daysOfWeek = days,
                startTime = startTime.trim(),
                endTime = endTime.trim(),
                monthlyFee = feeDouble
            )
            onSave(result, selectedStudents.toList())
        }
    ) {
        // Batch Name
        OutlinedTextField(
                    value = batchName,
                    onValueChange = {
                        batchName = it
                        if (it.isNotBlank()) isNameError = false
                    },
                    label = { Text(Localization.get("batch_name", isBn) + " *") },
                    placeholder = { Text("সকাল - এসএসসি গণিত ব্যাচ") },
                    isError = isNameError,
                    supportingText = if (isNameError) {
                        { Text(if (isBn) "ব্যাচের নাম আবশ্যক" else "Batch name is required") }
                    } else null,
                    modifier = Modifier.fillMaxWidth().testTag("input_batch_name"),
                    shape = RoundedCornerShape(12.dp),
                    colors = modalTextFieldColors(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = className,
                        onValueChange = { className = it },
                        label = { Text(Localization.get("class_grade", isBn)) },
                        placeholder = { Text("১০ম শ্রেণী") },
                        modifier = Modifier.weight(1f).testTag("input_batch_class"),
                        shape = RoundedCornerShape(12.dp),
                        colors = modalTextFieldColors(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text(Localization.get("subjects", isBn)) },
                        placeholder = { Text("গণিত") },
                        modifier = Modifier.weight(1f).testTag("input_batch_subject"),
                        shape = RoundedCornerShape(12.dp),
                        colors = modalTextFieldColors(),
                        singleLine = true
                    )
                }

                // Days Selection
                Text(
                    text = Localization.get("select_days", isBn),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    BengaliDateUtils.dayCodes.forEach { code ->
                        val isSelected = selectedDays.contains(code)
                        val label = if (isBn) BengaliDateUtils.dayCodeToBengaliShort[code] ?: code
                        else BengaliDateUtils.dayCodeToEnglishShort[code] ?: code

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) PrimaryBlue else Color(0xFFF1F5F9),
                            modifier = Modifier.clickable {
                                if (isSelected) selectedDays.remove(code) else selectedDays.add(code)
                            }
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

                // Time (Start & End) with Native 12-Hour AM/PM Time Picker Dialogs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TimePickerInputField(
                        label = Localization.get("start_time", isBn),
                        timeValue = startTime,
                        isBn = isBn,
                        testTag = "input_batch_start_time",
                        modifier = Modifier.weight(1f),
                        onTimeChanged = { startTime = it }
                    )
                    TimePickerInputField(
                        label = Localization.get("end_time", isBn),
                        timeValue = endTime,
                        isBn = isBn,
                        testTag = "input_batch_end_time",
                        modifier = Modifier.weight(1f),
                        onTimeChanged = { endTime = it }
                    )
                }

                // Monthly fee
                OutlinedTextField(
                    value = feeStr,
                    onValueChange = { feeStr = it },
                    label = { Text(Localization.get("monthly_fee", isBn)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("১৫০০") },
                    modifier = Modifier.fillMaxWidth().testTag("input_batch_fee"),
                    shape = RoundedCornerShape(12.dp),
                    colors = modalTextFieldColors(),
                    singleLine = true
                )

                // Assign Students checklist
                Divider(color = BorderSubtle)
                Text(
                    text = "${Localization.get("assigned_students", isBn)} (${selectedStudents.size})",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                if (allStudents.isEmpty()) {
                    Text(
                        text = if (isBn) "কোন শিক্ষার্থী পাওয়া যায়নি। প্রথমে শিক্ষার্থী যোগ করুন।" else "No students found.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                            .padding(6.dp)
                    ) {
                        allStudents.forEach { s ->
                            val isChecked = selectedStudents.contains(s.id)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isChecked) selectedStudents.remove(s.id) else selectedStudents.add(s.id)
                                    }
                                    .padding(vertical = 4.dp, horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { check ->
                                        if (check) selectedStudents.add(s.id) else selectedStudents.remove(s.id)
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = PrimaryBlue)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = s.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${s.className} ${if (s.studentId.isNotBlank()) "• রোল: ${s.studentId}" else ""}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditHomeTuitionDialog(
    tuitionToEdit: com.example.data.model.HomeTuition? = null,
    allStudents: List<Student>,
    isBn: Boolean = true,
    onDismiss: () -> Unit,
    onSave: (com.example.data.model.HomeTuition) -> Unit
) {
    var selectedStudentId by remember {
        mutableStateOf(tuitionToEdit?.studentId ?: (allStudents.firstOrNull()?.id ?: 0L))
    }
    var subject by remember { mutableStateOf(tuitionToEdit?.subject.orEmpty()) }
    var address by remember { mutableStateOf(tuitionToEdit?.address.orEmpty()) }
    var guardianPhone by remember { mutableStateOf(tuitionToEdit?.guardianPhone.orEmpty()) }
    var startTime by remember { mutableStateOf(tuitionToEdit?.startTime ?: "16:00") }
    var endTime by remember { mutableStateOf(tuitionToEdit?.endTime ?: "17:30") }
    var feeStr by remember {
        mutableStateOf(if (tuitionToEdit != null && tuitionToEdit.monthlyFee > 0) tuitionToEdit.monthlyFee.toInt().toString() else "")
    }

    val selectedDays = remember {
        mutableStateListOf<String>().apply {
            if (tuitionToEdit != null && tuitionToEdit.daysOfWeek.isNotBlank()) {
                addAll(tuitionToEdit.daysOfWeek.split(",").map { it.trim() })
            } else {
                addAll(listOf("SAT", "MON", "WED"))
            }
        }
    }

    FormModalContainer(
        title = if (tuitionToEdit == null) Localization.get("add_home_tuition", isBn) else Localization.get("badge_home", isBn),
        purposeSubtitle = if (isBn) "বাসায় ব্যক্তিগত ১-অন-১ টিউশনের রুটিন ও মাসিক ফি" else "Private 1-on-1 tuition schedule & monthly fee",
        icon = Icons.Default.HomeWork,
        headerColor = Color(0xFF0D9488),
        onDismiss = onDismiss,
        confirmButtonText = Localization.get("save", isBn),
        dismissButtonText = Localization.get("cancel", isBn),
        confirmButtonTestTag = "btn_save_tuition",
        dismissButtonTestTag = "btn_cancel_tuition",
        dialogTestTag = "dialog_add_edit_home_tuition",
        onConfirm = {
            if (selectedStudentId == 0L) return@FormModalContainer
            val days = selectedDays.joinToString(",")
            val feeDouble = feeStr.toDoubleOrNull() ?: 0.0
            val result = tuitionToEdit?.copy(
                studentId = selectedStudentId,
                subject = subject.trim(),
                address = address.trim(),
                guardianPhone = guardianPhone.trim(),
                daysOfWeek = days,
                startTime = startTime.trim(),
                endTime = endTime.trim(),
                monthlyFee = feeDouble
            ) ?: com.example.data.model.HomeTuition(
                studentId = selectedStudentId,
                subject = subject.trim(),
                address = address.trim(),
                guardianPhone = guardianPhone.trim(),
                daysOfWeek = days,
                startTime = startTime.trim(),
                endTime = endTime.trim(),
                monthlyFee = feeDouble
            )
            onSave(result)
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
                        text = if (isBn) "কোন শিক্ষার্থী নেই। অনুগ্রহ করে প্রথমে শিক্ষার্থী যোগ করুন।" else "No students available. Please add student first.",
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
                                        if (guardianPhone.isBlank() && s.guardianPhone.isNotBlank()) {
                                            guardianPhone = s.guardianPhone
                                        }
                                        if (feeStr.isBlank() && s.monthlyFee > 0) {
                                            feeStr = s.monthlyFee.toInt().toString()
                                        }
                                    }
                                    .padding(vertical = 6.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (isChosen) PrimaryBlue else Color.Transparent,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isChosen) PrimaryBlue else TextSecondary),
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    if (isChosen) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = s.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${s.className} ${if (s.guardianPhone.isNotBlank()) "• ${s.guardianPhone}" else ""}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // Subject
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text(Localization.get("subjects", isBn)) },
                    placeholder = { Text("ইংরেজি ও গণিত") },
                    modifier = Modifier.fillMaxWidth().testTag("input_tuition_subject"),
                    shape = RoundedCornerShape(12.dp),
                    colors = modalTextFieldColors(),
                    singleLine = true
                )

                // Address
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text(Localization.get("address_location", isBn)) },
                    placeholder = { Text("বাড়ি নং ১২, রোড ৪, ধানমন্ডি") },
                    modifier = Modifier.fillMaxWidth().testTag("input_tuition_address"),
                    shape = RoundedCornerShape(12.dp),
                    colors = modalTextFieldColors(),
                    singleLine = true
                )

                // Guardian Phone
                OutlinedTextField(
                    value = guardianPhone,
                    onValueChange = { guardianPhone = it },
                    label = { Text(Localization.get("guardian_phone", isBn)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth().testTag("input_tuition_phone"),
                    shape = RoundedCornerShape(12.dp),
                    colors = modalTextFieldColors(),
                    singleLine = true
                )

                // Days Selection
                Text(
                    text = Localization.get("select_days", isBn),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    BengaliDateUtils.dayCodes.forEach { code ->
                        val isSelected = selectedDays.contains(code)
                        val label = if (isBn) BengaliDateUtils.dayCodeToBengaliShort[code] ?: code
                        else BengaliDateUtils.dayCodeToEnglishShort[code] ?: code

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) com.example.ui.theme.AccentGreen else Color(0xFFF1F5F9),
                            modifier = Modifier.clickable {
                                if (isSelected) selectedDays.remove(code) else selectedDays.add(code)
                            }
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

                // Time (Start & End) with Native 12-Hour AM/PM Time Picker Dialogs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TimePickerInputField(
                        label = Localization.get("start_time", isBn),
                        timeValue = startTime,
                        isBn = isBn,
                        testTag = "input_tuition_start_time",
                        modifier = Modifier.weight(1f),
                        onTimeChanged = { startTime = it }
                    )
                    TimePickerInputField(
                        label = Localization.get("end_time", isBn),
                        timeValue = endTime,
                        isBn = isBn,
                        testTag = "input_tuition_end_time",
                        modifier = Modifier.weight(1f),
                        onTimeChanged = { endTime = it }
                    )
                }

                // Agreed Monthly Fee
                OutlinedTextField(
                    value = feeStr,
                    onValueChange = { feeStr = it },
                    label = { Text(Localization.get("monthly_fee", isBn)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("৪০০০") },
                    modifier = Modifier.fillMaxWidth().testTag("input_tuition_fee"),
                    shape = RoundedCornerShape(12.dp),
                    colors = modalTextFieldColors(),
                    singleLine = true
                )
    }
}
