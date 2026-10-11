package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.BengaliDateUtils
import java.util.Locale

@Composable
fun modalTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = PrimaryBlue,
    unfocusedBorderColor = Color(0xFFCBD5E1),
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color(0xFFF8FAFC),
    focusedLabelColor = PrimaryBlue,
    unfocusedLabelColor = Color(0xFF475569)
)

/**
 * Standard Prominent Modal Header with Close 'X' Button
 */
@Composable
fun ModalHeader(
    title: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        Surface(
            onClick = onClose,
            shape = CircleShape,
            color = Color(0xFFF1F5F9),
            border = BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Dialog",
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Interactive 12-Hour Native Time Picker Dialog with explicit AM / PM toggle.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractiveTimePickerDialog(
    initialTime: String, // "HH:mm" (24h)
    title: String,
    isBn: Boolean = true,
    onTimeSelected: (String) -> Unit, // returns "HH:mm" (24h)
    onDismiss: () -> Unit
) {
    val initialParts = initialTime.split(":")
    val initialH = initialParts.getOrNull(0)?.toIntOrNull() ?: 8
    val initialM = initialParts.getOrNull(1)?.toIntOrNull() ?: 0

    val timePickerState = rememberTimePickerState(
        initialHour = initialH,
        initialMinute = initialM,
        is24Hour = false
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            ModalHeader(
                title = title,
                onClose = onDismiss
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isBn) "১২-ঘণ্টার ফরম্যাটে সময় ও AM/PM নির্বাচন করুন:" else "Select 12-hour time and AM/PM:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = Color(0xFFF8FAFC),
                        selectorColor = PrimaryBlue,
                        periodSelectorBorderColor = PrimaryBlue,
                        periodSelectorSelectedContainerColor = PrimaryBlue,
                        periodSelectorUnselectedContainerColor = Color(0xFFF1F5F9),
                        periodSelectorSelectedContentColor = Color.White,
                        periodSelectorUnselectedContentColor = TextPrimary,
                        timeSelectorSelectedContainerColor = PrimaryBlue.copy(alpha = 0.12f),
                        timeSelectorUnselectedContainerColor = Color(0xFFF1F5F9),
                        timeSelectorSelectedContentColor = PrimaryBlue,
                        timeSelectorUnselectedContentColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Preview formatted selection
                val selectedH = timePickerState.hour
                val selectedM = timePickerState.minute
                val formatted24h = String.format(Locale.US, "%02d:%02d", selectedH, selectedM)
                val displayFormatted = BengaliDateUtils.formatTimeSlot(formatted24h, formatted24h, isBn).split("-").first().trim()

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF0FDF4),
                    border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isBn) "নির্বাচিত সময়: $displayFormatted" else "Selected: $displayFormatted",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF15803D),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val h = timePickerState.hour
                    val m = timePickerState.minute
                    val formatted = String.format(Locale.US, "%02d:%02d", h, m)
                    onTimeSelected(formatted)
                    onDismiss()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = if (isBn) "ঠিক আছে" else "Set Time",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = if (isBn) "বাতিল" else "Cancel",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

/**
 * Clickable Form Field for Time with Native Time Picker trigger.
 * Shows formatted 12-hour time with clock icon, clear thin borders, and AM/PM info.
 */
@Composable
fun TimePickerInputField(
    label: String,
    timeValue: String, // "HH:mm" (24h)
    isBn: Boolean = true,
    testTag: String = "",
    modifier: Modifier = Modifier,
    onTimeChanged: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    // Format 12-hour display: e.g. "08:00 AM" or "সকাল ০৮:০০"
    val displayTime = remember(timeValue, isBn) {
        if (timeValue.isBlank()) {
            if (isBn) "সময় নির্বাচন করুন" else "Select time"
        } else {
            val parts = timeValue.split(":")
            if (parts.size >= 2) {
                val h = parts[0].toIntOrNull() ?: 0
                val m = parts[1].toIntOrNull() ?: 0
                val isPm = h >= 12
                val h12 = when {
                    h == 0 -> 12
                    h > 12 -> h - 12
                    else -> h
                }
                val formatted = String.format(Locale.US, "%02d:%02d", h12, m)
                if (isBn) {
                    val period = when {
                        h in 5..11 -> "সকাল"
                        h in 12..14 -> "দুপুর"
                        h in 15..17 -> "বিকাল"
                        h in 18..21 -> "সন্ধ্যা"
                        else -> "রাত"
                    }
                    "$period ${BengaliDateUtils.toBengaliDigits(formatted)}"
                } else {
                    val period = if (isPm) "PM" else "AM"
                    "$formatted $period"
                }
            } else {
                timeValue
            }
        }
    }

    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF334155),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Surface(
            onClick = { showDialog = true },
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF8FAFC),
            border = BorderStroke(1.dp, Color(0xFFCBD5E1)), // Clear, crisp thin border
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 13.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = displayTime,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (timeValue.isBlank()) TextSecondary else TextPrimary
                )
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Select Time",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    if (showDialog) {
        InteractiveTimePickerDialog(
            initialTime = if (timeValue.isNotBlank()) timeValue else "08:00",
            title = label,
            isBn = isBn,
            onTimeSelected = { newTime ->
                onTimeChanged(newTime)
            },
            onDismiss = { showDialog = false }
        )
    }
}
