package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TeacherProfile
import com.example.ui.components.ModalHeader
import com.example.ui.components.modalTextFieldColors
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BorderVerySubtle
import com.example.ui.theme.PastelBlueBg
import com.example.ui.theme.PastelBlueIcon
import com.example.ui.theme.PastelGreenBg
import com.example.ui.theme.PastelGreenIcon
import com.example.ui.theme.PastelPurpleBg
import com.example.ui.theme.PastelPurpleIcon
import com.example.ui.theme.PastelPurpleText
import com.example.ui.theme.PastelRoseBg
import com.example.ui.theme.PastelRoseIcon
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.TuitionViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Upload
import com.example.util.BackupManager
import com.example.util.BengaliDateUtils
import com.example.util.Localization
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MoreScreen(
    viewModel: TuitionViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isBn by viewModel.isBengali.collectAsState()
    val profile by viewModel.profile.collectAsState()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var selectedBackupValidation by remember { mutableStateOf<BackupManager.ValidationResult.Success?>(null) }
    var showValidationDialog by remember { mutableStateOf(false) }
    var showReplaceConfirmDialog by remember { mutableStateOf(false) }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val json = viewModel.exportDataJson()
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(json.toByteArray(Charsets.UTF_8))
                    }
                    Toast.makeText(context, Localization.get("export_success", isBn), Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "${Localization.get("export_error", isBn)}: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        inputStream.bufferedReader(Charsets.UTF_8).readText()
                    } ?: ""

                    when (val result = viewModel.validateBackup(jsonString)) {
                        is BackupManager.ValidationResult.Success -> {
                            selectedBackupValidation = result
                            showValidationDialog = true
                        }
                        is BackupManager.ValidationResult.Error -> {
                            Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "${Localization.get("invalid_backup_format", isBn)} (${e.message})", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun shareBackupFile() {
        scope.launch {
            try {
                val json = viewModel.exportDataJson()
                val backupsDir = File(context.cacheDir, "backups")
                if (!backupsDir.exists()) backupsDir.mkdirs()
                val timeFormat = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
                val file = File(backupsDir, "tuition_manager_backup_$timeFormat.json")
                file.writeText(json, Charsets.UTF_8)

                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "Tuition Manager BD Backup")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, if (isBn) "ব্যাকআপ ফাইল শেয়ার করুন" else "Share Backup File"))
            } catch (e: Exception) {
                Toast.makeText(context, "${Localization.get("export_error", isBn)}: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFFFFFFF),
                        Color(0xFFF8FAFC),
                        Color(0xFFF1F5F9)
                    )
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(start = 18.dp, end = 18.dp, top = 20.dp, bottom = 95.dp)
            .testTag("more_screen"),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Teacher Profile Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.5.dp),
            border = BorderStroke(1.dp, BorderSubtle.copy(alpha = 0.6f))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(PastelPurpleBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = PastelPurpleIcon, modifier = Modifier.size(26.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = profile?.name.orEmpty().ifBlank { if (isBn) "শিক্ষক / শিক্ষিকা" else "Teacher" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = profile?.coachingName.orEmpty().ifBlank { if (isBn) "টিউটর পয়েন্ট ও কোচিং" else "My Tuition & Coaching" },
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Surface(
                        onClick = { showEditProfileDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        color = PastelPurpleBg,
                        border = BorderStroke(1.dp, PastelPurpleIcon.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = if (isBn) "সম্পাদনা" else "Edit",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = PastelPurpleText,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }

                if (!profile?.phone.isNullOrBlank() || !profile?.address.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = BorderVerySubtle)
                    Spacer(modifier = Modifier.height(12.dp))
                    if (!profile?.phone.isNullOrBlank()) {
                        Text(text = "মোবাইল: ${profile?.phone}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    if (!profile?.address.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "ঠিকানা: ${profile?.address}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }
        }

        // 2. Language Toggle Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.5.dp),
            border = BorderStroke(1.dp, BorderSubtle.copy(alpha = 0.6f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(PastelBlueBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = PastelBlueIcon, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = Localization.get("language_setting", isBn),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isBn) "বাংলা (ডিফল্ট)" else "English",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Switch(
                    checked = isBn,
                    onCheckedChange = { viewModel.toggleLanguage() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = PrimaryBlue,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFCBD5E1)
                    )
                )
            }
        }

        // 3. Data Backup & Safety Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.5.dp),
            border = BorderStroke(1.dp, BorderSubtle.copy(alpha = 0.6f))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(PastelGreenBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Backup, contentDescription = null, tint = PastelGreenIcon, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = Localization.get("backup_title", isBn),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = Localization.get("backup_desc", isBn),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Save Backup File Button (SAF CreateDocument)
                        Button(
                            onClick = {
                                val timeFormat = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
                                createDocumentLauncher.launch("tuition_manager_backup_$timeFormat.json")
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_save_backup_file"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryBlue,
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = Localization.get("save_backup_file", isBn),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // Share Backup File Button (FileProvider + Intent.ACTION_SEND)
                        OutlinedButton(
                            onClick = { shareBackupFile() },
                            modifier = Modifier
                                .weight(0.9f)
                                .testTag("btn_share_backup_file"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, PrimaryBlue),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = PrimaryBlue
                            )
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = Localization.get("share_backup_file", isBn),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                        }
                    }

                    // Restore Backup Button (SAF OpenDocument)
                    Button(
                        onClick = {
                            openDocumentLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_restore_backup_file"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentGreen,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = Localization.get("restore_from_file", isBn),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Warning Notice
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFFFFBEB),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = Localization.get("data_safety_warning", isBn),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF92400E),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // 4. Demo Data & Testing Tools
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.5.dp),
            border = BorderStroke(1.dp, BorderSubtle.copy(alpha = 0.6f))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(PastelPurpleBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Science, contentDescription = null, tint = PastelPurpleIcon, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = if (isBn) "ডেমো ডেটা ও টেস্ট টুল" else "Demo Data & Tools",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = Localization.get("demo_data_desc", isBn),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Load Demo Data Button (High contrast, White on Vibrant Sky Blue)
                    Button(
                        onClick = { viewModel.loadDemoData() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlueLight,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Icon(Icons.Default.Science, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = Localization.get("demo_data_button", isBn),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Clear All Data Button (High contrast, White on Red)
                    Button(
                        onClick = { showResetDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFDC2626),
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = Localization.get("clear_all_data", isBn),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // 5. Compact Developer Information
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.5.dp),
            border = BorderStroke(1.dp, BorderSubtle.copy(alpha = 0.6f))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(PastelBlueBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Code, contentDescription = null, tint = PastelBlueIcon, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isBn) "ডেভেলপার পরিচিতি" else "Developer Info",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Developer: Abdullah Ibne Ali",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = BorderVerySubtle)
                Spacer(modifier = Modifier.height(12.dp))

                // Portfolio Link (High contrast clear button container)
                Surface(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://abdullah-ibne-ali.netlify.app"))
                        context.startActivity(intent)
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFEFF6FF),
                    border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Public, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Portfolio: abdullah-ibne-ali.netlify.app",
                                style = MaterialTheme.typography.bodySmall,
                                color = PrimaryBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Icon(
                            Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "Open Portfolio",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // GitHub Link (High contrast clear button container)
                Surface(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/abdnimit1203"))
                        context.startActivity(intent)
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFEFF6FF),
                    border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Code, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "GitHub: github.com/abdnimit1203",
                                style = MaterialTheme.typography.bodySmall,
                                color = PrimaryBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Icon(
                            Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "Open GitHub",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // 6. About App Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.5.dp),
            border = BorderStroke(1.dp, BorderSubtle.copy(alpha = 0.6f))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tuition Manager BD • v1.0",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isBn) {
                        "বাংলাদেশের শিক্ষক ও গৃহশিক্ষকদের জন্য শতভাগ নির্ভরযোগ্য অফলাইন অ্যান্ড্রয়েড অ্যাপ। আপনার সকল ডেটা সম্পূর্ণ আপনার ডিভাইসে সুরক্ষিতভাবে সংরক্ষিত থাকে।"
                    } else {
                        "A 100% offline-first Android application designed for Bangladeshi teachers and tutors."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 20.sp
                )
            }
        }
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        val currentProf = profile ?: TeacherProfile()
        var teacherName by remember { mutableStateOf(currentProf.name) }
        var coachingName by remember { mutableStateOf(currentProf.coachingName) }
        var phone by remember { mutableStateOf(currentProf.phone) }
        var address by remember { mutableStateOf(currentProf.address) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = {
                ModalHeader(
                    title = Localization.get("teacher_profile", isBn),
                    onClose = { showEditProfileDialog = false }
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = teacherName,
                        onValueChange = { teacherName = it },
                        label = { Text("শিক্ষকের নাম") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = modalTextFieldColors()
                    )
                    OutlinedTextField(
                        value = coachingName,
                        onValueChange = { coachingName = it },
                        label = { Text("প্রতিষ্ঠান / টিউশন নাম") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = modalTextFieldColors()
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("মোবাইল নম্বর") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = modalTextFieldColors()
                    )
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("ঠিকানা") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = modalTextFieldColors()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateProfile(
                            currentProf.copy(
                                name = teacherName.trim(),
                                coachingName = coachingName.trim(),
                                phone = phone.trim(),
                                address = address.trim()
                            )
                        )
                        showEditProfileDialog = false
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue, contentColor = Color.White),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text(Localization.get("save", isBn), color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text(Localization.get("cancel", isBn), color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Pre-Restore Validation & Method Selection Dialog
    if (showValidationDialog && selectedBackupValidation != null) {
        val backupData = selectedBackupValidation!!
        AlertDialog(
            onDismissRequest = { showValidationDialog = false },
            modifier = Modifier.testTag("dialog_backup_validation"),
            title = {
                ModalHeader(
                    title = Localization.get("backup_summary", isBn),
                    onClose = { showValidationDialog = false }
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Export Date & Version Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PastelBlueBg,
                        border = BorderStroke(1.dp, BorderSubtle.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (isBn) "তারিখ: ${backupData.exportDateFormatted}" else "Date: ${backupData.exportDateFormatted}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                            Text(
                                text = if (isBn) "ফরম্যাট: v${backupData.version} ${if (backupData.isLegacy) "(পূর্ববর্তী ব্যাকআপ)" else ""}" else "Format: v${backupData.version} ${if (backupData.isLegacy) "(Legacy)" else ""}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }

                    // Entity Counts Breakdown
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, BorderVerySubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (isBn) "ফাইলে পাওয়া রেকর্ডসমূহ:" else "Records Found in File:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = if (isBn) "• শিক্ষার্থী:" else "• Students:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Text(text = if (isBn) BengaliDateUtils.toBengaliDigits(backupData.studentCount) else backupData.studentCount.toString(), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = if (isBn) "• কোচিং ব্যাচ:" else "• Coaching Batches:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Text(text = if (isBn) BengaliDateUtils.toBengaliDigits(backupData.batchCount) else backupData.batchCount.toString(), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = if (isBn) "• ব্যাচ ভর্তি:" else "• Enrollments:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Text(text = if (isBn) BengaliDateUtils.toBengaliDigits(backupData.enrollmentCount) else backupData.enrollmentCount.toString(), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = if (isBn) "• হোম টিউশন:" else "• Home Tuitions:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Text(text = if (isBn) BengaliDateUtils.toBengaliDigits(backupData.homeTuitionCount) else backupData.homeTuitionCount.toString(), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = if (isBn) "• উপস্থিতি রেকর্ড:" else "• Attendance Records:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Text(text = if (isBn) BengaliDateUtils.toBengaliDigits(backupData.attendanceCount) else backupData.attendanceCount.toString(), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = if (isBn) "• পেমেন্ট রেকর্ড:" else "• Payment Records:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Text(text = if (isBn) BengaliDateUtils.toBengaliDigits(backupData.paymentCount) else backupData.paymentCount.toString(), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = if (isBn) "• বকেয়া হিসাব:" else "• Fee Overrides:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Text(text = if (isBn) BengaliDateUtils.toBengaliDigits(backupData.dueCount) else backupData.dueCount.toString(), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Choice 1: Safe Merge
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = PastelGreenBg.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, BorderSubtle.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = Localization.get("merge_data", isBn),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = com.example.ui.theme.PastelGreenText
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = Localization.get("merge_desc", isBn),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    scope.launch {
                                        val res = viewModel.restoreMerge(backupData.rawJson)
                                        if (res.isSuccess) {
                                            Toast.makeText(context, Localization.get("restore_success", isBn), Toast.LENGTH_LONG).show()
                                            showValidationDialog = false
                                            selectedBackupValidation = null
                                        } else {
                                            Toast.makeText(context, "ত্রুটি: ${res.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen, contentColor = Color.White),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_confirm_merge")
                            ) {
                                Text(
                                    text = if (isBn) "মার্জ করুন (Merge Data)" else "Merge with Existing Data",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Choice 2: Replace All
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = PastelRoseBg.copy(alpha = 0.4f),
                        border = BorderStroke(1.dp, Color(0xFFFECDD3)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = Localization.get("replace_all_data", isBn),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = com.example.ui.theme.PastelRoseText
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = Localization.get("replace_all_warning", isBn),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { showReplaceConfirmDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48), contentColor = Color.White),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_start_replace_all")
                            ) {
                                Text(
                                    text = if (isBn) "সব প্রতিস্থাপন করুন (Replace All)" else "Replace All Data",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showValidationDialog = false }) {
                    Text(Localization.get("close", isBn), color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Explicit Confirmation Dialog for Replace All
    if (showReplaceConfirmDialog && selectedBackupValidation != null) {
        val backupData = selectedBackupValidation!!
        AlertDialog(
            onDismissRequest = { showReplaceConfirmDialog = false },
            modifier = Modifier.testTag("dialog_replace_all_confirm"),
            title = {
                ModalHeader(
                    title = Localization.get("replace_all_data", isBn),
                    onClose = { showReplaceConfirmDialog = false }
                )
            },
            text = {
                Text(
                    text = Localization.get("replace_all_warning", isBn),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val res = viewModel.restoreReplaceAll(backupData.rawJson)
                            if (res.isSuccess) {
                                Toast.makeText(context, Localization.get("restore_success", isBn), Toast.LENGTH_LONG).show()
                                showReplaceConfirmDialog = false
                                showValidationDialog = false
                                selectedBackupValidation = null
                            } else {
                                Toast.makeText(context, "ত্রুটি: ${res.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    modifier = Modifier.testTag("btn_confirm_replace_all_final"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48), contentColor = Color.White)
                ) {
                    Text(
                        text = if (isBn) "হ্যাঁ, সম্পূর্ণ প্রতিস্থাপন করুন" else "Yes, Replace All",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showReplaceConfirmDialog = false }) {
                    Text(Localization.get("cancel", isBn), color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Reset All Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                ModalHeader(
                    title = Localization.get("clear_all_data", isBn),
                    onClose = { showResetDialog = false }
                )
            },
            text = {
                Text(
                    text = if (isBn) "আপনি কি নিশ্চিত সকল শিক্ষার্থী ও হিসাব মুছে ফেলতে চান?" else "Are you sure you want to delete all data?",
                    color = TextPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showResetDialog = false
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626), contentColor = Color.White),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text(if (isBn) "হ্যাঁ, সব মুছুন" else "Yes, Delete All", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(Localization.get("cancel", isBn), color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
