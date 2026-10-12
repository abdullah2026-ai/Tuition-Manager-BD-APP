package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.CoachingBatch
import com.example.data.model.HomeTuition
import com.example.data.model.Student
import com.example.ui.dialogs.AddEditBatchDialog
import com.example.ui.dialogs.AddEditHomeTuitionDialog
import com.example.ui.dialogs.AddEditStudentDialog
import com.example.ui.dialogs.RecordPaymentDialog
import com.example.ui.screens.AttendanceScreen
import com.example.ui.screens.FeesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MoreScreen
import com.example.ui.screens.RoutineScreen
import com.example.ui.screens.StudentsScreen
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TuitionManagerTheme
import com.example.ui.viewmodel.TuitionViewModel
import com.example.util.Localization

class MainActivity : ComponentActivity() {

    private val viewModel: TuitionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TuitionManagerTheme {
                androidx.compose.material3.Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    TuitionManagerApp(viewModel = viewModel)
                }
            }
        }
    }
}

enum class NavigationTab(val titleKey: String, val icon: ImageVector) {
    HOME("nav_home", Icons.Default.Home),
    STUDENTS("nav_students", Icons.Default.People),
    ROUTINE("nav_routine", Icons.Default.Schedule),
    FEES("nav_fees", Icons.Default.Payments),
    MORE("nav_more", Icons.Default.MoreHoriz)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TuitionManagerApp(viewModel: TuitionViewModel) {
    val isBn by viewModel.isBengali.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()
    val activeStudents by viewModel.activeStudents.collectAsState()
    val crossRefs by viewModel.batchCrossRefs.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var currentTabIndex by remember { mutableIntStateOf(0) }

    // Dialog States
    var showStudentDialog by remember { mutableStateOf(false) }
    var studentToEdit by remember { mutableStateOf<Student?>(null) }

    var showBatchDialog by remember { mutableStateOf(false) }
    var batchToEdit by remember { mutableStateOf<CoachingBatch?>(null) }

    var showTuitionDialog by remember { mutableStateOf(false) }
    var tuitionToEdit by remember { mutableStateOf<HomeTuition?>(null) }

    var showPaymentDialog by remember { mutableStateOf(false) }
    var preselectedPaymentStudentId by remember { mutableStateOf<Long?>(null) }

    // State for navigating directly to attendance for a specific session
    var attendanceTargetSessionId by remember { mutableStateOf<Long?>(null) }
    var attendanceTargetSessionType by remember { mutableStateOf<String?>(null) }

    // Show Snackbar when userMessage changes
    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    // Back handling: if not on Home, go back to Home
    BackHandler(enabled = currentTabIndex != 0) {
        currentTabIndex = 0
    }

    val tabs = NavigationTab.values()

    Scaffold(
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
        containerColor = Color(0xFFF8FAFC),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            if (currentTabIndex != 0) {
                TopAppBar(
                    title = {
                        Text(
                            text = when (currentTabIndex) {
                                1 -> Localization.get("students_title", isBn)
                                2 -> Localization.get("routine_title", isBn)
                                3 -> Localization.get("fees_title", isBn)
                                4 -> Localization.get("more_title", isBn)
                                else -> Localization.get("app_title", isBn)
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White,
                        titleContentColor = PrimaryBlue
                    )
                )
            }
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderSubtle.copy(alpha = 0.5f))
            ) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 0.dp,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = currentTabIndex == index
                    val label = Localization.get(tab.titleKey, isBn)
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTabIndex = index },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = label
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryBlue,
                            selectedTextColor = PrimaryBlue,
                            indicatorColor = com.example.ui.theme.PastelBlueBg,
                            unselectedIconColor = TextSecondary.copy(alpha = 0.7f),
                            unselectedTextColor = TextSecondary.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.testTag("nav_item_${tab.name.lowercase()}")
                    )
                }
            }
        }
    },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTabIndex) {
                0 -> HomeScreen(
                    viewModel = viewModel,
                    onNavigateToStudents = { currentTabIndex = 1 },
                    onNavigateToRoutine = { currentTabIndex = 2 },
                    onNavigateToAttendance = {
                        // Go to routine or attendance view
                        currentTabIndex = 2
                    },
                    onNavigateToFees = { currentTabIndex = 3 },
                    onOpenAddStudentDialog = {
                        studentToEdit = null
                        showStudentDialog = true
                    },
                    onOpenAddClassDialog = {
                        batchToEdit = null
                        showBatchDialog = true
                    },
                    onOpenAddBatch = {
                        batchToEdit = null
                        showBatchDialog = true
                    },
                    onOpenAddHomeTuition = {
                        tuitionToEdit = null
                        showTuitionDialog = true
                    },
                    onOpenRecordFeeDialog = {
                        preselectedPaymentStudentId = null
                        showPaymentDialog = true
                    }
                )

                1 -> StudentsScreen(
                    viewModel = viewModel,
                    onOpenAddStudent = {
                        studentToEdit = null
                        showStudentDialog = true
                    },
                    onEditStudent = { student ->
                        studentToEdit = student
                        showStudentDialog = true
                    }
                )

                2 -> RoutineScreen(
                    viewModel = viewModel,
                    onOpenAddBatch = {
                        batchToEdit = null
                        showBatchDialog = true
                    },
                    onOpenAddHomeTuition = {
                        tuitionToEdit = null
                        showTuitionDialog = true
                    },
                    onEditBatch = { batch ->
                        batchToEdit = batch
                        showBatchDialog = true
                    },
                    onEditTuition = { tuition ->
                        tuitionToEdit = tuition
                        showTuitionDialog = true
                    },
                    onNavigateToAttendanceForSession = { sId, sType ->
                        attendanceTargetSessionId = sId
                        attendanceTargetSessionType = sType
                        // Temporary switch to in-line attendance
                        // We can show attendance screen modal or tab
                    }
                )

                3 -> FeesScreen(
                    viewModel = viewModel,
                    onOpenRecordPayment = { studentId ->
                        preselectedPaymentStudentId = studentId
                        showPaymentDialog = true
                    }
                )

                4 -> MoreScreen(
                    viewModel = viewModel
                )
            }

            // Attendance overlay if triggered from Routine
            if (attendanceTargetSessionId != null) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            androidx.compose.material3.TextButton(
                                onClick = {
                                    attendanceTargetSessionId = null
                                    attendanceTargetSessionType = null
                                }
                            ) {
                                Text(Localization.get("close", isBn), fontWeight = FontWeight.Bold)
                            }
                        }
                        AttendanceScreen(
                            viewModel = viewModel,
                            initialSessionId = attendanceTargetSessionId,
                            initialSessionType = attendanceTargetSessionType
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Student Dialog
    if (showStudentDialog) {
        AddEditStudentDialog(
            studentToEdit = studentToEdit,
            isBn = isBn,
            onDismiss = {
                showStudentDialog = false
                studentToEdit = null
            },
            onSave = { updatedStudent ->
                viewModel.saveStudent(updatedStudent) {
                    showStudentDialog = false
                    studentToEdit = null
                }
            }
        )
    }

    // Add / Edit Coaching Batch Dialog
    if (showBatchDialog) {
        val assignedIds = if (batchToEdit != null) {
            crossRefs.filter { it.batchId == batchToEdit!!.id }.map { it.studentId }
        } else emptyList()

        AddEditBatchDialog(
            batchToEdit = batchToEdit,
            allStudents = activeStudents,
            assignedStudentIds = assignedIds,
            isBn = isBn,
            onDismiss = {
                showBatchDialog = false
                batchToEdit = null
            },
            onSave = { updatedBatch, selectedIds ->
                viewModel.saveBatch(updatedBatch, selectedIds) {
                    showBatchDialog = false
                    batchToEdit = null
                }
            }
        )
    }

    // Add / Edit Home Tuition Dialog
    if (showTuitionDialog) {
        AddEditHomeTuitionDialog(
            tuitionToEdit = tuitionToEdit,
            allStudents = activeStudents,
            isBn = isBn,
            onDismiss = {
                showTuitionDialog = false
                tuitionToEdit = null
            },
            onSave = { updatedTuition ->
                viewModel.saveHomeTuition(updatedTuition) {
                    showTuitionDialog = false
                    tuitionToEdit = null
                }
            }
        )
    }

    // Record Payment Dialog
    if (showPaymentDialog) {
        RecordPaymentDialog(
            allStudents = activeStudents,
            preselectedStudentId = preselectedPaymentStudentId,
            isBn = isBn,
            onDismiss = {
                showPaymentDialog = false
                preselectedPaymentStudentId = null
            },
            onSavePayment = { payment ->
                viewModel.recordPayment(payment) {
                    showPaymentDialog = false
                    preselectedPaymentStudentId = null
                }
            }
        )
    }
}
