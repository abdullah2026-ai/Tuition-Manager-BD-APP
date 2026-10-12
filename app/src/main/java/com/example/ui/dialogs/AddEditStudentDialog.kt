package com.example.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.Student
import com.example.ui.components.FormModalContainer
import com.example.ui.components.modalTextFieldColors
import com.example.util.Localization

@Composable
fun AddEditStudentDialog(
    studentToEdit: Student? = null,
    isBn: Boolean = true,
    onDismiss: () -> Unit,
    onSave: (Student) -> Unit
) {
    var name by remember { mutableStateOf(studentToEdit?.name.orEmpty()) }
    var studentId by remember { mutableStateOf(studentToEdit?.studentId.orEmpty()) }
    var className by remember { mutableStateOf(studentToEdit?.className.orEmpty()) }
    var guardianName by remember { mutableStateOf(studentToEdit?.guardianName.orEmpty()) }
    var guardianPhone by remember { mutableStateOf(studentToEdit?.guardianPhone.orEmpty()) }
    var subjects by remember { mutableStateOf(studentToEdit?.subjects.orEmpty()) }
    var monthlyFeeStr by remember {
        mutableStateOf(if (studentToEdit != null && studentToEdit.monthlyFee > 0) studentToEdit.monthlyFee.toInt().toString() else "")
    }
    var notes by remember { mutableStateOf(studentToEdit?.notes.orEmpty()) }

    var isNameError by remember { mutableStateOf(false) }

    FormModalContainer(
        title = if (studentToEdit == null) Localization.get("add_student", isBn) else Localization.get("edit_student", isBn),
        purposeSubtitle = if (isBn) "শিক্ষার্থীর প্রোফাইল ও মাসিক বেতন নির্ধারণ" else "Student profile and monthly fee setup",
        icon = Icons.Default.PersonAdd,
        headerColor = Color(0xFF2563EB),
        onDismiss = onDismiss,
        confirmButtonText = Localization.get("save", isBn),
        dismissButtonText = Localization.get("cancel", isBn),
        confirmButtonTestTag = "btn_save_student",
        dismissButtonTestTag = "btn_cancel_student",
        dialogTestTag = "dialog_add_edit_student",
        onConfirm = {
            if (name.isBlank()) {
                isNameError = true
                return@FormModalContainer
            }
            val feeDouble = monthlyFeeStr.toDoubleOrNull() ?: 0.0
            val result = studentToEdit?.copy(
                name = name.trim(),
                studentId = studentId.trim(),
                className = className.trim(),
                guardianName = guardianName.trim(),
                guardianPhone = guardianPhone.trim(),
                subjects = subjects.trim(),
                monthlyFee = feeDouble,
                notes = notes.trim()
            ) ?: Student(
                name = name.trim(),
                studentId = studentId.trim(),
                className = className.trim(),
                guardianName = guardianName.trim(),
                guardianPhone = guardianPhone.trim(),
                subjects = subjects.trim(),
                monthlyFee = feeDouble,
                notes = notes.trim()
            )
            onSave(result)
        }
    ) {
        // Name
        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                if (it.isNotBlank()) isNameError = false
            },
            label = { Text(Localization.get("student_name", isBn) + " *") },
            isError = isNameError,
            supportingText = if (isNameError) {
                { Text(if (isBn) "শিক্ষার্থীর নাম আবশ্যক" else "Student name is required") }
            } else null,
            modifier = Modifier.fillMaxWidth().testTag("input_student_name"),
            shape = RoundedCornerShape(12.dp),
            colors = modalTextFieldColors(),
            singleLine = true
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Roll
            OutlinedTextField(
                value = studentId,
                onValueChange = { studentId = it },
                label = { Text(Localization.get("roll_number", isBn)) },
                modifier = Modifier.weight(1f).testTag("input_student_roll"),
                shape = RoundedCornerShape(12.dp),
                colors = modalTextFieldColors(),
                singleLine = true
            )
            // Class
            OutlinedTextField(
                value = className,
                onValueChange = { className = it },
                label = { Text(Localization.get("class_grade", isBn)) },
                placeholder = { Text("১০ম / ৯ম") },
                modifier = Modifier.weight(1f).testTag("input_student_class"),
                shape = RoundedCornerShape(12.dp),
                colors = modalTextFieldColors(),
                singleLine = true
            )
        }

        // Guardian Name
        OutlinedTextField(
            value = guardianName,
            onValueChange = { guardianName = it },
            label = { Text(Localization.get("guardian_name", isBn)) },
            modifier = Modifier.fillMaxWidth().testTag("input_guardian_name"),
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
            modifier = Modifier.fillMaxWidth().testTag("input_guardian_phone"),
            shape = RoundedCornerShape(12.dp),
            colors = modalTextFieldColors(),
            singleLine = true
        )

        // Subjects
        OutlinedTextField(
            value = subjects,
            onValueChange = { subjects = it },
            label = { Text(Localization.get("subjects", isBn)) },
            placeholder = { Text("গণিত, বিজ্ঞান") },
            modifier = Modifier.fillMaxWidth().testTag("input_student_subjects"),
            shape = RoundedCornerShape(12.dp),
            colors = modalTextFieldColors(),
            singleLine = true
        )

        // Monthly Fee
        OutlinedTextField(
            value = monthlyFeeStr,
            onValueChange = { monthlyFeeStr = it },
            label = { Text(Localization.get("monthly_fee", isBn)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            placeholder = { Text("১৫০০") },
            modifier = Modifier.fillMaxWidth().testTag("input_student_fee"),
            shape = RoundedCornerShape(12.dp),
            colors = modalTextFieldColors(),
            singleLine = true
        )

        // Notes
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text(Localization.get("notes", isBn)) },
            modifier = Modifier.fillMaxWidth().testTag("input_student_notes"),
            shape = RoundedCornerShape(12.dp),
            colors = modalTextFieldColors(),
            maxLines = 3
        )
    }
}
