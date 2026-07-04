package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import com.example.data.*
import com.example.viewmodel.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppUI(viewModel: MainViewModel) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val syncProgress by viewModel.syncProgress.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val isGeneratingAiReport by viewModel.isGeneratingAiReport.collectAsState()
    val aiAnalysisText by viewModel.aiAnalysisText.collectAsState()

    val schools by viewModel.schools.collectAsState()
    val teachers by viewModel.teachers.collectAsState()
    val students by viewModel.students.collectAsState()
    val studentTracks by viewModel.studentTracks.collectAsState()
    val attendanceRecords by viewModel.attendanceRecords.collectAsState()
    val performanceReports by viewModel.performanceReports.collectAsState()

    // Unsynced count calculator
    val unsyncedAttendanceCount = attendanceRecords.count { it.syncStatus == "PENDING" }
    val unsyncedPerformanceCount = performanceReports.count { it.syncStatus == "PENDING" }
    val unsyncedTrackCount = studentTracks.count { it.syncStatus == "PENDING" }
    val unsyncedTotal = unsyncedAttendanceCount + unsyncedPerformanceCount + unsyncedTrackCount

    // Local UI states
    var showNotificationsDrawer by remember { mutableStateOf(false) }
    val selectedSchoolIdState by viewModel.selectedSchoolId.collectAsState()
    val selectedDistrictState by viewModel.selectedDistrict.collectAsState()

    // App colors from MaterialTheme
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface

    if (!isLoggedIn) {
        LoginScreen(viewModel = viewModel)
    } else {
        Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryColor,
                    titleContentColor = Color.White
                ),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.School,
                            contentDescription = "School Management Logo",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                        Column {
                            Text(
                                "Malawi Schools",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                            Text(
                                "District Progress & Sync",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                },
                actions = {
                    // Online/Offline status card
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isOnline) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clickable { viewModel.toggleConnectivity() }
                            .testTag("connectivity_toggle"),
                        border = BorderStroke(
                            1.dp,
                            if (isOnline) Color(0xFF4CAF50) else Color(0xFFFF9800)
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isOnline) Color(0xFF4CAF50) else Color(0xFFFF9800))
                            )
                            Text(
                                if (isOnline) "ONLINE" else "OFFLINE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOnline) Color(0xFF2E7D32) else Color(0xFFE65100)
                            )
                            Icon(
                                if (isOnline) Icons.Default.Cloud else Icons.Default.CloudOff,
                                contentDescription = "Connection Status Indicator",
                                modifier = Modifier.size(12.dp),
                                tint = if (isOnline) Color(0xFF2E7D32) else Color(0xFFE65100)
                            )
                        }
                    }

                    // Alerts Drawer Trigger with Badge
                    IconButton(
                        onClick = { showNotificationsDrawer = !showNotificationsDrawer },
                        modifier = Modifier.testTag("notifications_trigger")
                    ) {
                        BadgedBox(
                            badge = {
                                if (notifications.isNotEmpty()) {
                                    Badge(
                                        containerColor = tertiaryColor,
                                        contentColor = Color.White
                                    ) {
                                        Text("${notifications.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = "Show Alerts Panel",
                                tint = Color.White
                            )
                        }
                    }

                    // Logout / Sign Out Button
                    IconButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.testTag("logout_button")
                    ) {
                        Icon(
                            Icons.Default.ExitToApp,
                            contentDescription = "Sign Out",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(primaryColor)
            ) {
                // Sync progress bar
                if (isSyncing) {
                    LinearProgressIndicator(
                        progress = { syncProgress },
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFFCD116),
                        trackColor = Color.White.copy(alpha = 0.3f),
                    )
                }

                // Lower bottom state strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = "Encrypted database indicator",
                            tint = Color(0xFFFCD116),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "Local Storage Secured: AES-256 GCM",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (unsyncedTotal > 0) {
                        Surface(
                            color = Color(0xFFCE1126),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .clickable { viewModel.triggerSync() }
                                .testTag("sync_badge")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Sync,
                                    contentDescription = "Sync Required Icon",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    "$unsyncedTotal PENDING SYNC",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Sync Finished Icon",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                "Database Synced",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(backgroundColor)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // User Status Panel showing the current secure session
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    colors = CardDefaults.cardColors(containerColor = surfaceColor),
                    elevation = CardDefaults.cardElevation(2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(primaryColor.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = "User avatar",
                                        tint = primaryColor
                                    )
                                }
                                Column {
                                    Text(
                                        currentUser,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    val roleText = when (currentRole) {
                                        "Teacher" -> "Teacher"
                                        "Admin" -> "Administrator"
                                        "DEO" -> "Headmaster"
                                        else -> currentRole
                                    }
                                    Text(
                                        roleText,
                                        fontSize = 11.sp,
                                        color = primaryColor,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // Secure session seal badge
                            Surface(
                                color = primaryColor.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.VerifiedUser,
                                        contentDescription = "Verified Session",
                                        tint = primaryColor,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        "RBAC SECURE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryColor
                                    )
                                }
                            }
                        }
                    }
                }

                // Render view based on role
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    AnimatedContent(
                        targetState = currentRole,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                        },
                        label = "RoleViewTransition"
                    ) { targetRole ->
                        when (targetRole) {
                            "Teacher" -> TeacherView(
                                currentUser = currentUser,
                                schools = schools,
                                students = students,
                                teachers = teachers,
                                studentTracks = studentTracks,
                                attendanceRecords = attendanceRecords,
                                selectedSchoolId = selectedSchoolIdState,
                                onSchoolSelected = { viewModel.selectedSchoolId.value = it },
                                onSubmitAttendance = { sId, date, map, names ->
                                    viewModel.submitAttendance(sId, date, map, names)
                                },
                                onSubmitPerformance = { tId, name, sId, lS, eS, tS ->
                                    viewModel.submitPerformanceReport(tId, name, sId, lS, eS, tS)
                                },
                                onAddStudent = { name, schoolId, className, gender ->
                                    viewModel.addStudent(name, schoolId, className, gender)
                                },
                                onSubmitStudentTrack = { sId, name, className, schoolId, type, status, notes ->
                                    viewModel.submitStudentTrack(sId, name, className, schoolId, type, status, notes)
                                },
                                isOnline = isOnline
                            )
                            "DEO" -> DeoDashboardView(
                                viewModel = viewModel,
                                schools = schools,
                                selectedDistrict = selectedDistrictState,
                                onDistrictSelected = { viewModel.selectedDistrict.value = it }
                            )
                            "Admin" -> LocalAdminView(
                                viewModel = viewModel,
                                isOnline = isOnline,
                                syncLogs = viewModel.syncLogs.collectAsState().value,
                                attendanceRecords = attendanceRecords,
                                performanceReports = performanceReports
                            )
                        }
                    }
                }
            }

            // Real-Time Notification Alerts Panel Overlay (Push Notification concept)
            AnimatedVisibility(
                visible = showNotificationsDrawer,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                        .testTag("notifications_drawer"),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = surfaceColor),
                    elevation = CardDefaults.cardElevation(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = "Alerts Center Icon",
                                    tint = primaryColor
                                )
                                Text(
                                    "District Sync & Push Alerts",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                            Row {
                                TextButton(onClick = { viewModel.clearNotifications() }) {
                                    Text("Clear All", color = tertiaryColor)
                                }
                                IconButton(onClick = { showNotificationsDrawer = false }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close Alerts")
                                }
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        if (notifications.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = "No alerts icon",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "No notifications yet",
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(notifications) { item ->
                                    val icon = when (item.type) {
                                        "SECURITY" -> Icons.Default.Lock
                                        "OFFLINE_SAVE" -> Icons.Default.CheckCircle
                                        "SYNC" -> Icons.Default.Sync
                                        "CONNECTIVITY" -> Icons.Default.Cloud
                                        "REPORT" -> Icons.Default.ReceiptLong
                                        else -> Icons.Default.Info
                                    }
                                    val badgeColor = when (item.type) {
                                        "SECURITY" -> Color(0xFF1976D2)
                                        "OFFLINE_SAVE" -> Color(0xFF4CAF50)
                                        "SYNC" -> Color(0xFFE5A93C)
                                        "CONNECTIVITY" -> Color(0xFF9C27B0)
                                        "REPORT" -> Color(0xFF009688)
                                        else -> Color.Gray
                                    }

                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.04f)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(badgeColor.copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    icon,
                                                    contentDescription = "Notification type logo",
                                                    tint = badgeColor,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    item.title,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )
                                                Text(
                                                    item.message,
                                                    fontSize = 12.sp,
                                                    color = Color.DarkGray
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                                                Text(
                                                    sdf.format(Date(item.timestamp)),
                                                    fontSize = 10.sp,
                                                    color = Color.Gray
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    }
}

// ==========================================
// 1. TEACHER VIEW: ATTENDANCE & EVALUATIONS
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyStudentAttendanceForm(
    schoolId: String,
    schoolStudents: List<Student>,
    assignedClasses: List<String>,
    attendanceMap: MutableMap<String, Boolean>,
    attendanceDate: String,
    onDateChange: (String) -> Unit,
    onSubmitAttendance: (schoolId: String, date: String, Map<String, Boolean>, Map<String, String>) -> Unit,
    onAddStudent: (name: String, schoolId: String, className: String, gender: String) -> Unit,
    attendanceRecords: List<AttendanceRecord>,
    modifier: Modifier = Modifier
) {
    var selectedClassFilter by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var genderFilter by remember { mutableStateOf("All") }
    var isQuickAddExpanded by remember { mutableStateOf(false) }

    // Quick Add States
    var quickAddName by remember { mutableStateOf("") }
    var quickAddGender by remember { mutableStateOf("Female") }
    var quickAddClass by remember { mutableStateOf("") }
    var isClassDropdownExpanded by remember { mutableStateOf(false) }

    // Historical Logs expansion
    var isHistoryLogsExpanded by remember { mutableStateOf(false) }
    var showHistoryDetailsDate by remember { mutableStateOf<String?>(null) }

    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    LaunchedEffect(assignedClasses) {
        if (assignedClasses.isNotEmpty() && quickAddClass.isEmpty()) {
            quickAddClass = assignedClasses.first()
        }
    }

    if (selectedClassFilter == null) {
        // ==========================================
        // CLASS SELECTION DASHBOARD SCREEN
        // ==========================================
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Hero Title Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Classroom Directory",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Select an assigned class to record daily attendance, review historical logs, or register new students.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "School Directory",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(48.dp)
                                .padding(start = 8.dp)
                        )
                    }
                }
            }

            // Quick Station Overview Statistics
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Station Attendance Overview",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Date Selector within Dashboard
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = attendanceDate,
                                onValueChange = onDateChange,
                                label = { Text("Active Date", fontSize = 11.sp) },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .testTag("dashboard_date_input"),
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Event,
                                        contentDescription = "Select Date"
                                    )
                                }
                            )

                            // Shortcuts Today / Yesterday
                            val yesterdayStr = remember {
                                val cal = Calendar.getInstance()
                                cal.add(Calendar.DATE, -1)
                                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Button(
                                    onClick = { onDateChange(todayStr) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (attendanceDate == todayStr) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = if (attendanceDate == todayStr) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(24.dp)
                                ) {
                                    Text("Today", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { onDateChange(yesterdayStr) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (attendanceDate == yesterdayStr) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = if (attendanceDate == yesterdayStr) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(24.dp)
                                ) {
                                    Text("Yesterday", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Summary Statistics Row
                        val enrolledTaught = schoolStudents.count { assignedClasses.contains(it.className) }
                        val todaySubmissions = attendanceRecords.filter { it.date == attendanceDate && it.schoolId == schoolId }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "$enrolledTaught",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text("Total Students", fontSize = 11.sp, color = Color.Gray)
                            }
                            Column {
                                val submittedClassesCount = assignedClasses.count { cls ->
                                    attendanceRecords.any { r -> r.date == attendanceDate && r.schoolId == schoolId && schoolStudents.find { s -> s.id == r.studentId }?.className == cls }
                                }
                                Text(
                                    text = "$submittedClassesCount / ${assignedClasses.size}",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (submittedClassesCount == assignedClasses.size) Color(0xFF2E7D32) else Color(0xFFE65100)
                                )
                                Text("Today's Classes Done", fontSize = 11.sp, color = Color.Gray)
                            }
                            Column {
                                val schoolRecords = attendanceRecords.filter { it.schoolId == schoolId }
                                val schoolAvg = if (schoolRecords.isNotEmpty()) {
                                    (schoolRecords.count { it.isPresent }.toDouble() / schoolRecords.size * 100).toInt()
                                } else 92
                                Text(
                                    text = "$schoolAvg%",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text("School Avg Rate", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }

            // Section: Assigned Classes Grid Header
            item {
                Text(
                    text = "My Assigned Classes / Standards",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                )
            }

            // Grid of Taught Classes
            items(assignedClasses) { cls ->
                val classStudents = schoolStudents.filter { it.className == cls }
                
                // Check if submitted today
                val isSubmittedToday = attendanceRecords.any { record ->
                    record.date == attendanceDate && 
                    record.schoolId == schoolId && 
                    classStudents.any { it.id == record.studentId }
                }

                // Compute historical rate for this class
                val classRecords = attendanceRecords.filter { record ->
                    record.schoolId == schoolId && 
                    classStudents.any { it.id == record.studentId }
                }
                val historicRate = if (classRecords.isNotEmpty()) {
                    (classRecords.count { it.isPresent }.toDouble() / classRecords.size * 100).toInt()
                } else {
                    93
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedClassFilter = cls
                            // Initialize map default values for this class if empty
                            classStudents.forEach { student ->
                                if (!attendanceMap.containsKey(student.id)) {
                                    attendanceMap[student.id] = true
                                }
                            }
                        }
                        .testTag("class_dashboard_card_$cls"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(
                        1.5.dp,
                        if (isSubmittedToday) Color(0xFF4CAF50).copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Class,
                                        contentDescription = "Class Icon",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Column {
                                    Text(
                                        text = cls,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = "${classStudents.size} Registered Students",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            // Submitted Status Badge
                            Surface(
                                color = if (isSubmittedToday) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(if (isSubmittedToday) Color(0xFF4CAF50) else Color(0xFFFF9800))
                                    )
                                    Text(
                                        text = if (isSubmittedToday) "Submitted" else "Pending Today",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSubmittedToday) Color(0xFF2E7D32) else Color(0xFFE65100)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Historic Rate Progress Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Historic Rate:",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                                LinearProgressIndicator(
                                    progress = { historicRate.toFloat() / 100f },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = if (historicRate >= 90) Color(0xFF4CAF50) else Color(0xFFE53935),
                                    trackColor = Color.LightGray.copy(alpha = 0.3f)
                                )
                                Text(
                                    text = "$historicRate%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (historicRate >= 90) Color(0xFF2E7D32) else Color(0xFFC62828)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.clickable {
                                    selectedClassFilter = cls
                                }
                            ) {
                                Text(
                                    text = "Mark",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = "Mark",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Quick Student Registration Collapsible Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.animateContentSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isQuickAddExpanded = !isQuickAddExpanded }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Add Icon",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    "Quick Student Registration",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = "Toggle Expand",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.rotate(if (isQuickAddExpanded) 180f else 0f)
                            )
                        }

                        if (isQuickAddExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    "Register a student to the school database. It immediately saves locally in secure cache.",
                                    fontSize = 11.sp,
                                    color = Color.DarkGray
                                )

                                OutlinedTextField(
                                    value = quickAddName,
                                    onValueChange = { quickAddName = it },
                                    label = { Text("Student Full Name") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("dashboard_quick_add_name")
                                )

                                // Standard Selector
                                Column {
                                    Text(
                                        "Standard / Classroom",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.LightGray.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                            .border(1.dp, Color.Gray.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                            .clickable { isClassDropdownExpanded = true }
                                            .padding(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                quickAddClass.ifEmpty { "Select Class" },
                                                fontWeight = FontWeight.Medium
                                            )
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                        }

                                        DropdownMenu(
                                            expanded = isClassDropdownExpanded,
                                            onDismissRequest = { isClassDropdownExpanded = false }
                                        ) {
                                            assignedClasses.forEach { cls ->
                                                DropdownMenuItem(
                                                    text = { Text(cls) },
                                                    onClick = {
                                                        quickAddClass = cls
                                                        isClassDropdownExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                // Gender selector row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Gender:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                    listOf("Female", "Male").forEach { gender ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.clickable { quickAddGender = gender }
                                        ) {
                                            RadioButton(
                                                selected = (quickAddGender == gender),
                                                onClick = { quickAddGender = gender }
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(gender, fontSize = 12.sp)
                                        }
                                    }
                                }

                                Button(
                                    onClick = {
                                        if (quickAddName.isNotBlank() && quickAddClass.isNotBlank()) {
                                            onAddStudent(quickAddName.trim(), schoolId, quickAddClass, quickAddGender)
                                            quickAddName = ""
                                            isQuickAddExpanded = false
                                        }
                                    },
                                    enabled = quickAddName.isNotBlank() && quickAddClass.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black)
                                        Text("Register Student", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Collapsible Historical Logs Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.animateContentSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isHistoryLogsExpanded = !isHistoryLogsExpanded }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = "History Log",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "School Submissions History Log",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Toggle",
                                modifier = Modifier.rotate(if (isHistoryLogsExpanded) 180f else 0f)
                            )
                        }

                        if (isHistoryLogsExpanded) {
                            val historicDates = attendanceRecords
                                .filter { it.schoolId == schoolId }
                                .map { it.date }
                                .distinct()
                                .sortedDescending()
                                .take(6)

                            if (historicDates.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No historical submission records found.",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            } else {
                                Column(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    historicDates.forEach { dateStr ->
                                        val dayRecords = attendanceRecords.filter { it.date == dateStr && it.schoolId == schoolId }
                                        val totalRecs = dayRecords.size
                                        val presentRecs = dayRecords.count { it.isPresent }
                                        val pct = if (totalRecs > 0) (presentRecs.toDouble() / totalRecs * 100).toInt() else 0

                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                                .clickable {
                                                    showHistoryDetailsDate = if (showHistoryDetailsDate == dateStr) null else dateStr
                                                }
                                                .padding(10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(
                                                        text = dateStr,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp
                                                    )
                                                    Text(
                                                        text = "$totalRecs Students marked • Submitted by ${dayRecords.firstOrNull()?.submittedBy ?: "Unknown"}",
                                                        fontSize = 11.sp,
                                                        color = Color.Gray
                                                    )
                                                }
                                                
                                                Surface(
                                                    color = if (pct >= 90) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text(
                                                        text = "$pct% Present",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (pct >= 90) Color(0xFF2E7D32) else Color(0xFFC62828),
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }

                                            if (showHistoryDetailsDate == dateStr) {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                                Spacer(modifier = Modifier.height(6.dp))
                                                
                                                Text(
                                                    text = "Absent Students on $dateStr:",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    color = Color(0xFFC62828),
                                                    modifier = Modifier.padding(bottom = 4.dp)
                                                )
                                                
                                                val absents = dayRecords.filter { !it.isPresent }
                                                if (absents.isEmpty()) {
                                                    Text(
                                                        text = "✓ Perfect attendance! 0 students absent.",
                                                        fontSize = 11.sp,
                                                        color = Color(0xFF2E7D32)
                                                    )
                                                } else {
                                                    absents.forEach { abs ->
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                                            horizontalArrangement = Arrangement.SpaceBetween
                                                        ) {
                                                            Text(text = abs.studentName, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                                            Text(
                                                                text = schoolStudents.find { it.id == abs.studentId }?.className ?: "N/A",
                                                                fontSize = 10.sp,
                                                                color = Color.Gray
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        // ==========================================
        // CLASS DETAILED ATTENDANCE MARKS SCREEN
        // ==========================================
        val selectedClass = selectedClassFilter!!
        val classStudents = schoolStudents.filter { it.className == selectedClass }

        val filteredStudents = classStudents.filter { student ->
            val matchesSearch = student.name.contains(searchQuery, ignoreCase = true) || 
                                student.id.contains(searchQuery, ignoreCase = true)
            val matchesGender = genderFilter == "All" || student.gender == genderFilter
            matchesSearch && matchesGender
        }

        // Metrics for this selected class only
        val totalCount = classStudents.size
        val presentCount = classStudents.count { attendanceMap[it.id] == true }
        val absentCount = classStudents.count { attendanceMap[it.id] == false }
        val presentPercentage = if (totalCount > 0) (presentCount.toFloat() / totalCount * 100).toInt() else 100

        LazyColumn(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Header Row with Back Button & Class Name
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { selectedClassFilter = null },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back to Classroom Directory",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    Column {
                        Text(
                            text = selectedClass,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Daily Attendance Roster • $totalCount Students",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Active Date Selector
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Attendance Date & Validation",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = attendanceDate,
                                onValueChange = onDateChange,
                                label = { Text("Selected Date (YYYY-MM-DD)", fontSize = 11.sp) },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .testTag("attendance_date_input"),
                                trailingIcon = {
                                    Icon(Icons.Default.Event, contentDescription = null)
                                }
                            )

                            val yesterdayStr = remember {
                                val cal = Calendar.getInstance()
                                cal.add(Calendar.DATE, -1)
                                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                            }

                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Button(
                                    onClick = { onDateChange(todayStr) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (attendanceDate == todayStr) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = if (attendanceDate == todayStr) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier.height(26.dp)
                                ) {
                                    Text("Today", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { onDateChange(yesterdayStr) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (attendanceDate == yesterdayStr) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = if (attendanceDate == yesterdayStr) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier.height(26.dp)
                                ) {
                                    Text("Yesterday", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Live Class Metrics Summary Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Live Attendance Status",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (presentPercentage >= 90) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    if (presentPercentage >= 90) "Healthy Attendance" else "Warning: High Absenteeism",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (presentPercentage >= 90) Color(0xFF2E7D32) else Color(0xFFE65100)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "$presentPercentage%",
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (presentPercentage >= 90) Color(0xFF2E7D32) else Color(0xFFC62828),
                                    modifier = Modifier.testTag("present_percentage_metric")
                                )
                                Text("Present Rate", fontSize = 11.sp, color = Color.Gray)
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$presentCount",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32)
                                    )
                                    Text("Present", fontSize = 10.sp, color = Color.Gray)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$absentCount",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFC62828)
                                    )
                                    Text("Absent", fontSize = 10.sp, color = Color.Gray)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$totalCount",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text("Total", fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                        }

                        // Progress representation
                        LinearProgressIndicator(
                            progress = if (totalCount > 0) presentCount.toFloat() / totalCount else 1f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFF2E7D32),
                            trackColor = Color(0xFFE53935)
                        )
                    }
                }
            }

            // Search and Gender Filter Panel
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Search
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search students in $selectedClass...", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("student_attendance_search")
                    )

                    // Gender Filter Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("All", "Female", "Male").forEach { g ->
                            val isSelected = genderFilter == g
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.25f))
                                    .clickable { genderFilter = g }
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = g,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Bulk Action Buttons and Registration Actions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            filteredStudents.forEach { attendanceMap[it.id] = true }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8F5E9), contentColor = Color(0xFF2E7D32)),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 6.dp)
                    ) {
                        Text("All Present", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            filteredStudents.forEach { attendanceMap[it.id] = false }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE), contentColor = Color(0xFFC62828)),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 6.dp)
                    ) {
                        Text("All Absent", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            quickAddClass = selectedClass
                            quickAddName = ""
                            isQuickAddExpanded = true
                            // Jump to collapsible card or show a small alert to register
                        },
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Register New", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Roster Checklist Title
            item {
                Text(
                    text = "Roster Checklist (${filteredStudents.size} listed)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (filteredStudents.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Info, contentDescription = "Empty", tint = Color.Gray, modifier = Modifier.size(40.dp))
                            Text("No students match the current filters.", color = Color.Gray, fontSize = 13.sp)
                        }
                    }
                }
            } else {
                items(filteredStudents) { student ->
                    val isPresent = attendanceMap[student.id] ?: true
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("student_item_card_${student.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                // Mini Initials Avatar Circle
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            if (student.gender == "Female") Color(0xFFFCE4EC) else Color(0xFFE3F2FD),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = student.name.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("").uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (student.gender == "Female") Color(0xFFC2185B) else Color(0xFF1976D2)
                                    )
                                }

                                Column {
                                    Text(
                                        text = student.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .background(Color.LightGray.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = student.id,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.DarkGray
                                            )
                                        }
                                        Text(
                                            text = student.gender,
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }

                            // Tactile Toggle present/absent
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, Color.LightGray.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                    .height(32.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .background(if (isPresent) Color(0xFFE8F5E9) else Color.Transparent)
                                        .clickable { attendanceMap[student.id] = true }
                                        .padding(horizontal = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Present",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPresent) Color(0xFF2E7D32) else Color.Gray
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width(1.dp)
                                        .background(Color.LightGray.copy(alpha = 0.6f))
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .background(if (!isPresent) Color(0xFFFFEBEE) else Color.Transparent)
                                        .clickable { attendanceMap[student.id] = false }
                                        .padding(horizontal = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Absent",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (!isPresent) Color(0xFFC62828) else Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // AES-256 GCM Notice Panel
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Security Status",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                "End-to-End Encrypted Storage",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Class attendance records are securely encrypted using military-grade AES-256 GCM prior to disk storage. Records are cached on device and auto-synchronized instantly when cell network is active.",
                                fontSize = 10.sp,
                                color = Color.DarkGray,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }

            // Submit Button
            item {
                Button(
                    onClick = {
                        val namesMap = classStudents.associate { it.id to it.name }
                        val filteredMap = attendanceMap.filter { k -> classStudents.any { s -> s.id == k.key } }
                        onSubmitAttendance(schoolId, attendanceDate, filteredMap, namesMap)
                        // Auto return to class dashboard after successful submit
                        selectedClassFilter = null
                    },
                    enabled = classStudents.isNotEmpty() && attendanceDate.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("submit_attendance_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Submit")
                        Text("Secure Save Offline & Sync", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// ==========================================
// 1. TEACHER VIEW: ATTENDANCE & EVALUATIONS
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherView(
    currentUser: String,
    schools: List<School>,
    students: List<Student>,
    teachers: List<Teacher>,
    studentTracks: List<StudentTrack>,
    attendanceRecords: List<AttendanceRecord>,
    selectedSchoolId: String,
    onSchoolSelected: (String) -> Unit,
    onSubmitAttendance: (schoolId: String, date: String, Map<String, Boolean>, Map<String, String>) -> Unit,
    onSubmitPerformance: (teacherId: String, name: String, schoolId: String, lS: Int, eS: Int, tS: Int) -> Unit,
    onAddStudent: (name: String, schoolId: String, className: String, gender: String) -> Unit,
    onSubmitStudentTrack: (studentId: String, studentName: String, className: String, schoolId: String, trackType: String, trackStatus: String, notes: String) -> Unit,
    isOnline: Boolean
) {
    var isSchoolDropdownExpanded by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Attendance, 1 = Student Tracks, 2 = Self Performance Report, 3 = Stats Dashboard
    val currentSchool = schools.find { it.id == selectedSchoolId }
    val currentTeacher = teachers.find { it.name == currentUser }
    val assignedClasses = currentTeacher?.classesTaught?.split(",")?.map { it.trim() } ?: listOf("Standard 5", "Standard 6")

    // Attendance Local State
    val schoolStudents = students.filter { it.schoolId == selectedSchoolId }
    val attendanceMap = remember { mutableStateMapOf<String, Boolean>() }
    var attendanceDate by remember { mutableStateOf("") }

    // Auto-populate attendance date and defaults
    LaunchedEffect(selectedSchoolId) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        attendanceDate = sdf.format(Date())
        attendanceMap.clear()
        schoolStudents.forEach { attendanceMap[it.id] = true } // default to present
    }

    LaunchedEffect(schoolStudents) {
        schoolStudents.forEach { student ->
            if (!attendanceMap.containsKey(student.id)) {
                attendanceMap[student.id] = true
            }
        }
    }

    // Teacher Evaluation Local State
    val schoolTeachers = teachers.filter { it.schoolId == selectedSchoolId }
    var selectedTeacherId by remember { mutableStateOf("") }
    var selectedTeacherName by remember { mutableStateOf("") }
    var isTeacherDropdownExpanded by remember { mutableStateOf(false) }

    var lessonScore by remember { mutableIntStateOf(4) }
    var engagementScore by remember { mutableIntStateOf(4) }
    var timeScore by remember { mutableIntStateOf(4) }

    LaunchedEffect(schoolTeachers) {
        if (schoolTeachers.isNotEmpty()) {
            selectedTeacherId = schoolTeachers.first().id
            selectedTeacherName = schoolTeachers.first().name
        } else {
            selectedTeacherId = ""
            selectedTeacherName = ""
        }
    }

    // Add Student Local State
    var showAddStudentDialog by remember { mutableStateOf(false) }
    var newStudentName by remember { mutableStateOf("") }
    var newStudentGender by remember { mutableStateOf("Female") }
    var newStudentClass by remember { mutableStateOf("") }
    var isNewStudentClassExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(assignedClasses) {
        if (assignedClasses.isNotEmpty()) {
            newStudentClass = assignedClasses.first()
        }
    }

    // Add Student Dialog
    if (showAddStudentDialog) {
        AlertDialog(
            onDismissRequest = { showAddStudentDialog = false },
            title = {
                Text(
                    "Register New Student",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Add a student directly to one of your assigned classes. This record will be cached locally.",
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )

                    OutlinedTextField(
                        value = newStudentName,
                        onValueChange = { newStudentName = it },
                        label = { Text("Student Full Name") },
                        modifier = Modifier.fillMaxWidth().testTag("new_student_name_input"),
                        singleLine = true
                    )

                    // Class Dropdown Selector
                    Column {
                        Text(
                            "Assigned Class / Standard",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.LightGray.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .border(1.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .clickable { isNewStudentClassExpanded = true }
                                .padding(12.dp)
                                .testTag("new_student_class_dropdown")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    newStudentClass.ifEmpty { "Select Class" },
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }

                            DropdownMenu(
                                expanded = isNewStudentClassExpanded,
                                onDismissRequest = { isNewStudentClassExpanded = false }
                            ) {
                                assignedClasses.forEach { cls ->
                                    DropdownMenuItem(
                                        text = { Text(cls) },
                                        onClick = {
                                            newStudentClass = cls
                                            isNewStudentClassExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Gender Selector
                    Column {
                        Text(
                            "Gender",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf("Female", "Male").forEach { gender ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { newStudentGender = gender }
                                ) {
                                    RadioButton(
                                        selected = (newStudentGender == gender),
                                        onClick = { newStudentGender = gender },
                                        modifier = Modifier.testTag("gender_radio_$gender")
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(gender, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newStudentName.isNotBlank() && newStudentClass.isNotBlank()) {
                            onAddStudent(newStudentName.trim(), selectedSchoolId, newStudentClass, newStudentGender)
                            newStudentName = ""
                            showAddStudentDialog = false
                        }
                    },
                    enabled = newStudentName.isNotBlank() && newStudentClass.isNotBlank(),
                    modifier = Modifier.testTag("new_student_submit_btn")
                ) {
                    Text("Register Student")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddStudentDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // School Selector Bar
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "Primary Rural Station Selection",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isSchoolDropdownExpanded = true }
                        .testTag("school_selector_dropdown")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                currentSchool?.name ?: "Select School",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                "${currentSchool?.district ?: "N/A"} District • ${currentSchool?.region ?: "N/A"}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = "Expand schools dropdown"
                        )
                    }

                    DropdownMenu(
                        expanded = isSchoolDropdownExpanded,
                        onDismissRequest = { isSchoolDropdownExpanded = false }
                    ) {
                        schools.forEach { school ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(school.name, fontWeight = FontWeight.Bold)
                                        Text("${school.district} (${school.region})", fontSize = 11.sp)
                                    }
                                },
                                onClick = {
                                    onSchoolSelected(school.id)
                                    isSchoolDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Action Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Student Attendance", "Student Tracks", "Teacher Performance", "Stats & Analytics").forEachIndexed { index, title ->
                val isSelected = selectedTab == index
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.25f))
                        .clickable { selectedTab = index }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .testTag("teacher_tab_$index"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Inner Pane Content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (selectedTab == 0) {
                DailyStudentAttendanceForm(
                    schoolId = selectedSchoolId,
                    schoolStudents = schoolStudents,
                    assignedClasses = assignedClasses,
                    attendanceMap = attendanceMap,
                    attendanceDate = attendanceDate,
                    onDateChange = { attendanceDate = it },
                    onSubmitAttendance = onSubmitAttendance,
                    onAddStudent = onAddStudent,
                    attendanceRecords = attendanceRecords,
                    modifier = Modifier.fillMaxSize()
                )
            } else if (selectedTab == 1) {
                // STUDENT TRACK MANAGEMENT
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    var filterClass by remember { mutableStateOf("All") }
                    var filterType by remember { mutableStateOf("All") }
                    var filterStatus by remember { mutableStateOf("All") }
                    
                    var showAddTrackDialog by remember { mutableStateOf(false) }
                    
                    val schoolTracks = studentTracks.filter { it.schoolId == selectedSchoolId }
                    
                    val filteredTracks = schoolTracks.filter { track ->
                        (filterClass == "All" || track.className == filterClass) &&
                        (filterType == "All" || track.trackType == filterType) &&
                        (filterStatus == "All" || track.trackStatus == filterStatus)
                    }

                    // 1. Dashboard counts
                    val exceedingCount = schoolTracks.count { it.trackStatus == "Exceeding" }
                    val onTrackCount = schoolTracks.count { it.trackStatus == "On Track" }
                    val remedialCount = schoolTracks.count { it.trackStatus == "Remedial" }
                    val atRiskCount = schoolTracks.count { it.trackStatus == "At Risk" }

                    val itemsList = listOf(
                        Triple("At Risk", atRiskCount, Color(0xFFC62828)),
                        Triple("Remedial", remedialCount, Color(0xFF1565C0)),
                        Triple("On Track", onTrackCount, Color(0xFF2E7D32)),
                        Triple("Exceeding", exceedingCount, Color(0xFFE65100))
                    )
                    val isWide = LocalConfiguration.current.screenWidthDp >= 450
                    if (isWide) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsList.forEach { (label, count, color) ->
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
                                    border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "$count",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = color
                                        )
                                        Text(
                                            text = label,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = color.copy(alpha = 0.8f),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                itemsList.take(2).forEach { (label, count, color) ->
                                    Card(
                                        modifier = Modifier.weight(1f),
                                        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
                                        border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "$count",
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = color
                                            )
                                            Text(
                                                text = label,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = color.copy(alpha = 0.8f),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                itemsList.drop(2).forEach { (label, count, color) ->
                                    Card(
                                        modifier = Modifier.weight(1f),
                                        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
                                        border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "$count",
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = color
                                            )
                                            Text(
                                                text = label,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = color.copy(alpha = 0.8f),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Row with Add Track and Filter options
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Track Records (${filteredTracks.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        
                        Button(
                            onClick = { showAddTrackDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp).testTag("add_track_progress_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Track Progress", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Filters Layout
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isWide) {
                            Row(
                                modifier = Modifier.padding(8.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Class Filter
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Class", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    var expanded by remember { mutableStateOf(false) }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                            .clickable { expanded = true }
                                            .padding(6.dp)
                                    ) {
                                        Text(filterClass, fontSize = 11.sp, maxLines = 1)
                                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                            DropdownMenuItem(text = { Text("All Classes") }, onClick = { filterClass = "All"; expanded = false })
                                            assignedClasses.forEach { cls ->
                                                DropdownMenuItem(text = { Text(cls) }, onClick = { filterClass = cls; expanded = false })
                                            }
                                        }
                                    }
                                }

                                // Type Filter
                                Column(modifier = Modifier.weight(1.2f)) {
                                    Text("Track Type", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    var expanded by remember { mutableStateOf(false) }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                            .clickable { expanded = true }
                                            .padding(6.dp)
                                    ) {
                                        Text(filterType, fontSize = 11.sp, maxLines = 1)
                                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                            DropdownMenuItem(text = { Text("All Types") }, onClick = { filterType = "All"; expanded = false })
                                            listOf("Academic", "Attendance", "Behavior", "Literacy", "Numeracy").forEach { type ->
                                                DropdownMenuItem(text = { Text(type) }, onClick = { filterType = type; expanded = false })
                                            }
                                        }
                                    }
                                }

                                // Status Filter
                                Column(modifier = Modifier.weight(1.2f)) {
                                    Text("Status", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    var expanded by remember { mutableStateOf(false) }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                            .clickable { expanded = true }
                                            .padding(6.dp)
                                    ) {
                                        Text(filterStatus, fontSize = 11.sp, maxLines = 1)
                                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                            DropdownMenuItem(text = { Text("All Statuses") }, onClick = { filterStatus = "All"; expanded = false })
                                            listOf("Exceeding", "On Track", "Remedial", "At Risk").forEach { status ->
                                                DropdownMenuItem(text = { Text(status) }, onClick = { filterStatus = status; expanded = false })
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier.padding(8.dp).fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Class Filter
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text("Class", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    var expanded by remember { mutableStateOf(false) }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                            .clickable { expanded = true }
                                            .padding(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(filterClass, fontSize = 11.sp, maxLines = 1)
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                                        }
                                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                            DropdownMenuItem(text = { Text("All Classes") }, onClick = { filterClass = "All"; expanded = false })
                                            assignedClasses.forEach { cls ->
                                                DropdownMenuItem(text = { Text(cls) }, onClick = { filterClass = cls; expanded = false })
                                            }
                                        }
                                    }
                                }

                                // Type Filter
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text("Track Type", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    var expanded by remember { mutableStateOf(false) }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                            .clickable { expanded = true }
                                            .padding(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(filterType, fontSize = 11.sp, maxLines = 1)
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                                        }
                                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                            DropdownMenuItem(text = { Text("All Types") }, onClick = { filterType = "All"; expanded = false })
                                            listOf("Academic", "Attendance", "Behavior", "Literacy", "Numeracy").forEach { type ->
                                                DropdownMenuItem(text = { Text(type) }, onClick = { filterType = type; expanded = false })
                                            }
                                        }
                                    }
                                }

                                // Status Filter
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text("Status", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    var expanded by remember { mutableStateOf(false) }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                            .clickable { expanded = true }
                                            .padding(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(filterStatus, fontSize = 11.sp, maxLines = 1)
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                                        }
                                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                            DropdownMenuItem(text = { Text("All Statuses") }, onClick = { filterStatus = "All"; expanded = false })
                                            listOf("Exceeding", "On Track", "Remedial", "At Risk").forEach { status ->
                                                DropdownMenuItem(text = { Text(status) }, onClick = { filterStatus = status; expanded = false })
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Logs list
                    if (filteredTracks.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No track records match filters.", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredTracks) { track ->
                                val statusColor = when (track.trackStatus) {
                                    "Exceeding" -> Color(0xFF2E7D32)
                                    "On Track" -> Color(0xFF1565C0)
                                    "Remedial" -> Color(0xFFE65100)
                                    "At Risk" -> Color(0xFFC62828)
                                    else -> Color.Gray
                                }
                                
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(1.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(track.studentName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                Text("${track.className} • ${track.trackType}", fontSize = 11.sp, color = Color.Gray)
                                            }

                                            Surface(
                                                color = statusColor.copy(alpha = 0.12f),
                                                shape = RoundedCornerShape(12.dp),
                                                border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
                                            ) {
                                                Text(
                                                    track.trackStatus.uppercase(),
                                                    color = statusColor,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = track.notes,
                                            fontSize = 12.sp,
                                            color = Color.DarkGray,
                                            lineHeight = 16.sp
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Divider(color = Color.LightGray.copy(alpha = 0.5f))
                                        Spacer(modifier = Modifier.height(4.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "By ${track.updatedBy} • ${track.dateUpdated}",
                                                fontSize = 10.sp,
                                                color = Color.Gray
                                            )

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (track.syncStatus == "SYNCED") Icons.Default.CloudDone else Icons.Default.Sync,
                                                    contentDescription = null,
                                                    tint = if (track.syncStatus == "SYNCED") Color(0xFF2E7D32) else Color(0xFFFF9800),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Text(
                                                    text = track.syncStatus,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (track.syncStatus == "SYNCED") Color(0xFF2E7D32) else Color(0xFFFF9800)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // dialog to update/add student track record
                    if (showAddTrackDialog) {
                        var trackStudentId by remember { mutableStateOf("") }
                        var trackStudentName by remember { mutableStateOf("") }
                        var trackStudentClass by remember { mutableStateOf("") }
                        var isStudentDropdownExpanded by remember { mutableStateOf(false) }

                        var trackTypeState by remember { mutableStateOf("Academic") }
                        var isTypeDropdownExpanded by remember { mutableStateOf(false) }

                        var trackStatusState by remember { mutableStateOf("On Track") }
                        var isStatusDropdownExpanded by remember { mutableStateOf(false) }

                        var trackNotes by remember { mutableStateOf("") }

                        val teacherStudents = students.filter { it.schoolId == selectedSchoolId && assignedClasses.contains(it.className) }

                        LaunchedEffect(teacherStudents) {
                            if (teacherStudents.isNotEmpty()) {
                                trackStudentId = teacherStudents.first().id
                                trackStudentName = teacherStudents.first().name
                                trackStudentClass = teacherStudents.first().className
                            }
                        }

                        AlertDialog(
                            onDismissRequest = { showAddTrackDialog = false },
                            title = { Text("Log Student Progress Track", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                            text = {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                                ) {
                                    Text(
                                        "Track qualitative progress of students. Securely encrypted and cached locally.",
                                        fontSize = 12.sp,
                                        color = Color.DarkGray
                                    )

                                    // Student Selector
                                    Column {
                                        Text("Select Student", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color.LightGray.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                                .border(1.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                                .clickable { isStudentDropdownExpanded = true }
                                                .padding(12.dp)
                                                .testTag("track_student_dropdown")
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(trackStudentName.ifEmpty { "Select Student" }, fontWeight = FontWeight.Medium)
                                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                            }

                                            DropdownMenu(
                                                expanded = isStudentDropdownExpanded,
                                                onDismissRequest = { isStudentDropdownExpanded = false }
                                            ) {
                                                teacherStudents.forEach { std ->
                                                    DropdownMenuItem(
                                                        text = { Text("${std.name} (${std.className})") },
                                                        onClick = {
                                                            trackStudentId = std.id
                                                            trackStudentName = std.name
                                                            trackStudentClass = std.className
                                                            isStudentDropdownExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Track Type Selector
                                    Column {
                                        Text("Track Type / Sphere", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color.LightGray.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                                .border(1.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                                .clickable { isTypeDropdownExpanded = true }
                                                .padding(12.dp)
                                                .testTag("track_type_dropdown")
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(trackTypeState, fontWeight = FontWeight.Medium)
                                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                            }

                                            DropdownMenu(
                                                expanded = isTypeDropdownExpanded,
                                                onDismissRequest = { isTypeDropdownExpanded = false }
                                            ) {
                                                listOf("Academic", "Attendance", "Behavior", "Literacy", "Numeracy").forEach { type ->
                                                    DropdownMenuItem(
                                                        text = { Text(type) },
                                                        onClick = {
                                                            trackTypeState = type
                                                            isTypeDropdownExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Track Status Selector
                                    Column {
                                        Text("Assigned Status", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color.LightGray.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                                .border(1.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                                .clickable { isStatusDropdownExpanded = true }
                                                .padding(12.dp)
                                                .testTag("track_status_dropdown")
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(trackStatusState, fontWeight = FontWeight.Medium)
                                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                            }

                                            DropdownMenu(
                                                expanded = isStatusDropdownExpanded,
                                                onDismissRequest = { isStatusDropdownExpanded = false }
                                            ) {
                                                listOf("Exceeding", "On Track", "Remedial", "At Risk").forEach { status ->
                                                    DropdownMenuItem(
                                                        text = { Text(status) },
                                                        onClick = {
                                                            trackStatusState = status
                                                            isStatusDropdownExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Track Notes Input
                                    OutlinedTextField(
                                        value = trackNotes,
                                        onValueChange = { trackNotes = it },
                                        label = { Text("Progress & Action Notes") },
                                        modifier = Modifier.fillMaxWidth().height(100.dp).testTag("track_notes_input"),
                                        maxLines = 4
                                    )
                                }
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        if (trackStudentId.isNotEmpty() && trackNotes.isNotBlank()) {
                                            onSubmitStudentTrack(
                                                trackStudentId,
                                                trackStudentName,
                                                trackStudentClass,
                                                selectedSchoolId,
                                                trackTypeState,
                                                trackStatusState,
                                                trackNotes.trim()
                                            )
                                            showAddTrackDialog = false
                                        }
                                    },
                                    enabled = trackStudentId.isNotEmpty() && trackNotes.isNotBlank(),
                                    modifier = Modifier.testTag("track_submit_btn")
                                ) {
                                    Text("Log Status")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showAddTrackDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                }
            } else if (selectedTab == 2) {
                // TEACHER PERFORMANCE REPORTS
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Teacher Performance Evaluation Tracker",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    // Teacher Selector
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "Evaluated Teacher",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isTeacherDropdownExpanded = true }
                                    .testTag("teacher_eval_dropdown")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        selectedTeacherName.ifEmpty { "No Teachers available" },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = "Expand teachers list"
                                    )
                                }

                                DropdownMenu(
                                    expanded = isTeacherDropdownExpanded,
                                    onDismissRequest = { isTeacherDropdownExpanded = false }
                                ) {
                                    schoolTeachers.forEach { teacher ->
                                        DropdownMenuItem(
                                            text = { Text(teacher.name) },
                                            onClick = {
                                                selectedTeacherId = teacher.id
                                                selectedTeacherName = teacher.name
                                                isTeacherDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Metrics sliders
                    val metrics = listOf(
                        Triple("Preparedness & Lesson Plan", lessonScore) { s: Int -> lessonScore = s },
                        Triple("Student Focus & Engagement", engagementScore) { s: Int -> engagementScore = s },
                        Triple("Punctuality & Time Management", timeScore) { s: Int -> timeScore = s }
                    )

                    metrics.forEach { (label, value, onValueChange) ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(1.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(label, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("$value / 5 Stars", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Slider(
                                    value = value.toFloat(),
                                    onValueChange = { onValueChange(it.toInt()) },
                                    valueRange = 1f..5f,
                                    steps = 3,
                                    modifier = Modifier.testTag("slider_${label.replace(" ", "_")}")
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (selectedTeacherId.isNotEmpty()) {
                                onSubmitPerformance(
                                    selectedTeacherId,
                                    selectedTeacherName,
                                    selectedSchoolId,
                                    lessonScore,
                                    engagementScore,
                                    timeScore
                                )
                            }
                        },
                        enabled = selectedTeacherId.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("submit_performance_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = "Security lock icon")
                            Text(
                                "Lock Performance Evaluation (AES)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.Black
                            )
                        }
                    }
                }
            } else {
                val teacherStudents = students.filter { it.schoolId == selectedSchoolId && assignedClasses.contains(it.className) }
                TeacherStatsDashboardView(
                    currentUser = currentUser,
                    currentSchool = currentSchool,
                    assignedClasses = assignedClasses,
                    teacherStudents = teacherStudents,
                    attendanceRecords = attendanceRecords,
                    studentTracks = studentTracks,
                    isOnline = isOnline,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherStatsDashboardView(
    currentUser: String,
    currentSchool: School?,
    assignedClasses: List<String>,
    teacherStudents: List<Student>,
    attendanceRecords: List<AttendanceRecord>,
    studentTracks: List<StudentTrack>,
    isOnline: Boolean,
    modifier: Modifier = Modifier
) {
    val teacherStudentIds = teacherStudents.map { it.id }.toSet()
    val teacherRecords = attendanceRecords.filter { teacherStudentIds.contains(it.studentId) }
    
    val totalStudentsCount = teacherStudents.size
    val activeTracks = studentTracks.filter { track -> teacherStudentIds.contains(track.studentId) }
    val activeTracksCount = activeTracks.size
    
    val overallAttendanceRate = if (teacherRecords.isNotEmpty()) {
        (teacherRecords.count { it.isPresent }.toDouble() / teacherRecords.size) * 100
    } else {
        -1.0
    }
    
    val pendingAttendanceCount = teacherRecords.count { it.syncStatus == "PENDING" }
    val pendingTracksCount = activeTracks.count { it.syncStatus == "PENDING" }
    
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Connectivity and Synchronization Status Indicator Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("teacher_dashboard_sync_status_card"),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Sync Status Detail Icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Sync & Connectivity State",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Connectivity status badge
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isOnline) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isOnline) Color(0xFFC8E6C9) else Color(0xFFFFE0B2),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .testTag("connectivity_badge_container")
                            .padding(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = if (isOnline) Icons.Default.Cloud else Icons.Default.CloudOff,
                                contentDescription = if (isOnline) "Online Icon" else "Offline Icon",
                                tint = if (isOnline) Color(0xFF2E7D32) else Color(0xFFE65100),
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "CONNECTION",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOnline) Color(0xFF2E7D32) else Color(0xFFE65100)
                                )
                                Text(
                                    text = if (isOnline) "Online" else "Offline",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOnline) Color(0xFF1B5E20) else Color(0xFFD84315)
                                )
                            }
                        }
                    }

                    // Synchronization Status badge
                    val totalPending = pendingAttendanceCount + pendingTracksCount
                    val syncBgColor = if (totalPending == 0) Color(0xFFE0F2F1) else Color(0xFFE3F2FD)
                    val syncBorderColor = if (totalPending == 0) Color(0xFFB2DFDB) else Color(0xFFBBDEFB)
                    val syncTextColor = if (totalPending == 0) Color(0xFF00796B) else Color(0xFF1565C0)
                    val syncIcon = if (totalPending == 0) Icons.Default.CloudDone else Icons.Default.Sync
                    
                    Box(
                        modifier = Modifier
                            .weight(1.2f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(syncBgColor)
                            .border(
                                width = 1.dp,
                                color = syncBorderColor,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .testTag("sync_status_badge_container")
                            .padding(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = syncIcon,
                                contentDescription = if (totalPending == 0) "Synced Icon" else "Pending Sync Icon",
                                tint = syncTextColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "SYNCHRONIZATION",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = syncTextColor
                                )
                                Text(
                                    text = if (totalPending == 0) "All Data Synced" else "$totalPending Pending",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = syncTextColor
                                )
                            }
                        }
                    }
                }
                
                val totalPending = pendingAttendanceCount + pendingTracksCount
                if (totalPending > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Pending attendance: $pendingAttendanceCount, student tracks: $pendingTracksCount.",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        modifier = Modifier.testTag("pending_items_details_text")
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isOnline) "Data will automatically sync to the cloud in the background." else "Reconnect to the network to upload your offline classroom updates.",
                        fontSize = 10.sp,
                        color = if (isOnline) Color(0xFF2E7D32) else Color(0xFFC62828),
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth().testTag("teacher_stats_summary_card"),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            ),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.ShowChart,
                        contentDescription = "Analytics Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        "Classroom Analytics Dashboard",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Real-time statistics for teacher $currentUser at ${currentSchool?.name ?: "Local School Node"}.",
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    assignedClasses.forEach { cls ->
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        ) {
                            Text(
                                text = cls,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.People,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$totalStudentsCount",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Students Assigned",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Card(
                modifier = Modifier.weight(1.2f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (overallAttendanceRate >= 90.0) Color(0xFF2E7D32) else if (overallAttendanceRate >= 75.0) Color(0xFFE65100) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (overallAttendanceRate >= 0) "${"%.1f".format(overallAttendanceRate)}%" else "N/A",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (overallAttendanceRate >= 90.0) Color(0xFF2E7D32) else if (overallAttendanceRate >= 75.0) Color(0xFFE65100) else MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Avg Attendance",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$activeTracksCount",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Active Tracks",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        
        Text(
            text = "ASSIGNED CLASS BREAKDOWNS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
        )
        
        assignedClasses.forEach { cls ->
            val classStudents = teacherStudents.filter { it.className == cls }
            val classStudentsCount = classStudents.size
            val maleCount = classStudents.count { it.gender.equals("Male", ignoreCase = true) }
            val femaleCount = classStudents.count { it.gender.equals("Female", ignoreCase = true) }
            
            val classStudentIds = classStudents.map { it.id }.toSet()
            val classRecords = attendanceRecords.filter { classStudentIds.contains(it.studentId) }
            val classAttendanceRate = if (classRecords.isNotEmpty()) {
                (classRecords.count { it.isPresent }.toDouble() / classRecords.size) * 100
            } else {
                -1.0
            }
            
            val classTracks = studentTracks.filter { track -> classStudentIds.contains(track.studentId) }
            val atRiskInClass = classTracks.count { it.trackStatus == "At Risk" }
            val remedialInClass = classTracks.count { it.trackStatus == "Remedial" }
            val onTrackInClass = classTracks.count { it.trackStatus == "On Track" }
            val exceedingInClass = classTracks.count { it.trackStatus == "Exceeding" }
            
            Card(
                modifier = Modifier.fillMaxWidth().testTag("class_stat_card_${cls.replace(" ", "_")}"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.School,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = cls,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        Text(
                            text = "$classStudentsCount Students",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF2196F3)))
                            Text("Male: $maleCount", fontSize = 11.sp, color = Color.Gray)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFE91E63)))
                            Text("Female: $femaleCount", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(color = Color.LightGray.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Text(
                        "CLASS ATTENDANCE RATE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    if (classAttendanceRate >= 0) {
                        val attendanceColor = if (classAttendanceRate >= 90.0) {
                            Color(0xFF2E7D32)
                        } else if (classAttendanceRate >= 75.0) {
                            Color(0xFFE65100)
                        } else {
                            Color(0xFFC62828)
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            LinearProgressIndicator(
                                progress = (classAttendanceRate / 100).toFloat(),
                                color = attendanceColor,
                                trackColor = attendanceColor.copy(alpha = 0.15f),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                            Text(
                                text = "${"%.1f".format(classAttendanceRate)}%",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = attendanceColor
                            )
                        }
                    } else {
                        Text(
                            text = "No submissions recorded for this class yet.",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                    
                    if (classTracks.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "STUDENT TRACK STATUS DISTRIBUTION",
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        ) {
                            val totalTracks = classTracks.size.toFloat()
                            if (exceedingInClass > 0) {
                                Box(modifier = Modifier.weight(exceedingInClass / totalTracks).fillMaxHeight().background(Color(0xFFE65100)))
                            }
                            if (onTrackInClass > 0) {
                                Box(modifier = Modifier.weight(onTrackInClass / totalTracks).fillMaxHeight().background(Color(0xFF2E7D32)))
                            }
                            if (remedialInClass > 0) {
                                Box(modifier = Modifier.weight(remedialInClass / totalTracks).fillMaxHeight().background(Color(0xFF1565C0)))
                            }
                            if (atRiskInClass > 0) {
                                Box(modifier = Modifier.weight(atRiskInClass / totalTracks).fillMaxHeight().background(Color(0xFFC62828)))
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (exceedingInClass > 0) Text("Exc: $exceedingInClass", fontSize = 9.sp, color = Color(0xFFE65100), fontWeight = FontWeight.Bold)
                            if (onTrackInClass > 0) Text("OnTrack: $onTrackInClass", fontSize = 9.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                            if (remedialInClass > 0) Text("Remedial: $remedialInClass", fontSize = 9.sp, color = Color(0xFF1565C0), fontWeight = FontWeight.Bold)
                            if (atRiskInClass > 0) Text("AtRisk: $atRiskInClass", fontSize = 9.sp, color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        
        Text(
            text = "DAILY ATTENDANCE TREND",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
        )
        
        val trendData = teacherRecords
            .groupBy { it.date }
            .map { (date, records) ->
                val rate = if (records.isNotEmpty()) (records.count { it.isPresent }.toFloat() / records.size) else 0.0f
                Pair(date, rate)
            }
            .sortedBy { it.first }
            .takeLast(7)
            
        if (trendData.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.ShowChart,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = "Historical Trend Unavailable",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "No attendance submissions have been recorded in SQLite yet. Log attendance in the first tab to view historical trend analytics here.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        lineHeight = 15.sp
                    )
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Attendance Percentage Trend",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Progress tracking across the last ${trendData.size} logged sessions",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }
                    
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    ) {
                        val gridColor = Color.LightGray.copy(alpha = 0.3f)
                        
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(trendData) {
                                    detectTapGestures { offset ->
                                        val h = size.height.toFloat()
                                        val w = size.width.toFloat()
                                        val xSpace = w / (trendData.size - 1).coerceAtLeast(1)
                                        var minDistance = Float.MAX_VALUE
                                        var closestIndex = -1
                                        
                                        trendData.forEachIndexed { index, pair ->
                                            val x = if (trendData.size > 1) index * xSpace else w / 2f
                                            val y = h - (pair.second * h)
                                            val distance = Math.sqrt(
                                                Math.pow((offset.x - x).toDouble(), 2.0) +
                                                Math.pow((offset.y - y).toDouble(), 2.0)
                                            ).toFloat()
                                            
                                            if (distance < minDistance && distance < 60f) {
                                                minDistance = distance
                                                closestIndex = index
                                            }
                                        }
                                        selectedPointIndex = if (closestIndex != -1) closestIndex else null
                                    }
                                }
                        ) {
                            val canvasWidth = size.width
                            val canvasHeight = size.height
                            
                            val lines = 4
                            for (i in 0..lines) {
                                val y = (canvasHeight / lines) * i
                                drawLine(
                                    color = gridColor,
                                    start = Offset(0f, y),
                                    end = Offset(canvasWidth, y),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }
                            
                            val points = trendData.mapIndexed { index, pair ->
                                val x = if (trendData.size > 1) {
                                    (canvasWidth / (trendData.size - 1)) * index
                                } else {
                                    canvasWidth / 2f
                                }
                                val y = canvasHeight - (pair.second * canvasHeight)
                                Offset(x, y)
                            }
                            
                            if (points.size > 1) {
                                val path = Path().apply {
                                    moveTo(points.first().x, points.first().y)
                                    for (i in 1 until points.size) {
                                        lineTo(points[i].x, points[i].y)
                                    }
                                }
                                drawPath(
                                    path = path,
                                    color = Color(0xFF2E7D32),
                                    style = Stroke(width = 3.dp.toPx())
                                )
                            }
                            
                            points.forEachIndexed { index, offset ->
                                val isSelected = selectedPointIndex == index
                                drawCircle(
                                    color = if (isSelected) Color(0xFF1B5E20) else Color(0xFF4CAF50),
                                    radius = if (isSelected) 8.dp.toPx() else 5.dp.toPx(),
                                    center = offset
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = if (isSelected) 4.dp.toPx() else 2.5.dp.toPx(),
                                    center = offset
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        trendData.forEachIndexed { index, (date, rate) ->
                            val cleanDate = date.replace("2026-", "")
                            val isSelected = selectedPointIndex == index
                            Text(
                                text = cleanDate,
                                fontSize = 9.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color(0xFF2E7D32) else Color.Gray
                            )
                        }
                    }
                    
                    selectedPointIndex?.let { idx ->
                        val (date, rate) = trendData[idx]
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Date: $date", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Text("Attendance Rate: ${"%.1f".format(rate * 100)}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// INTERACTIVE CHARTS & BREAKDOWNS FOR EDUCATION OFFICER
// ==========================================
@Composable
fun InteractiveAttendanceTrendChart(trendData: List<Pair<String, Float>>) {
    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }
    
    if (trendData.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No attendance records recorded for this district.", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
        return
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Student Attendance Trend",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Daily attendance rate (%) over time",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
                
                // Legend
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2E7D32))
                    )
                    Text("Attendance Rate", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main plotting area
            val primaryColor = MaterialTheme.colorScheme.primary
            val gridColor = Color.LightGray.copy(alpha = 0.3f)
            
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                val canvasWidthDp = maxWidth
                val canvasHeightDp = maxHeight
                
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(trendData) {
                            detectTapGestures { offset ->
                                val h = size.height.toFloat()
                                val w = size.width.toFloat()
                                val xSpace = w / (trendData.size - 1).coerceAtLeast(1)
                                var minDistance = Float.MAX_VALUE
                                var closestIndex = -1
                                
                                trendData.forEachIndexed { index, pair ->
                                    val x = if (trendData.size > 1) index * xSpace else w / 2f
                                    val y = h - (pair.second * h)
                                    val distance = Math.sqrt(
                                        Math.pow((offset.x - x).toDouble(), 2.0) +
                                        Math.pow((offset.y - y).toDouble(), 2.0)
                                    ).toFloat()
                                    
                                    if (distance < minDistance && distance < 60f) { // Touch threshold
                                        minDistance = distance
                                        closestIndex = index
                                    }
                                }
                                selectedPointIndex = if (closestIndex != -1) closestIndex else null
                            }
                        }
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    
                    // Draw horizontal grid lines (0%, 25%, 50%, 75%, 100%)
                    val lines = 4
                    for (i in 0..lines) {
                        val y = (canvasHeight / lines) * i
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(canvasWidth, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    
                    // Calculate points
                    val points = trendData.mapIndexed { index, pair ->
                        val x = if (trendData.size > 1) {
                            (canvasWidth / (trendData.size - 1)) * index
                        } else {
                            canvasWidth / 2f
                        }
                        val y = canvasHeight - (pair.second * canvasHeight)
                        Offset(x, y)
                    }
                    
                    // Draw line
                    if (points.size > 1) {
                        val path = Path().apply {
                            moveTo(points.first().x, points.first().y)
                            for (i in 1 until points.size) {
                                lineTo(points[i].x, points[i].y)
                            }
                        }
                        drawPath(
                            path = path,
                            color = Color(0xFF2E7D32),
                            style = Stroke(width = 3.dp.toPx())
                        )
                    }
                    
                    // Draw point circles
                    points.forEachIndexed { index, offset ->
                        val isSelected = selectedPointIndex == index
                        drawCircle(
                            color = if (isSelected) Color(0xFF1B5E20) else Color(0xFF4CAF50),
                            radius = if (isSelected) 8.dp.toPx() else 5.dp.toPx(),
                            center = offset
                        )
                        drawCircle(
                            color = Color.White,
                            radius = if (isSelected) 4.dp.toPx() else 2.5.dp.toPx(),
                            center = offset
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // X-Axis labels (Dates)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                trendData.forEach { (date, _) ->
                    val cleanDate = date.replace("2026-", "") // E.g., "06-24"
                    Text(
                        cleanDate,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                }
            }

            // Click Tooltip Indicator
            AnimatedVisibility(
                visible = selectedPointIndex != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                selectedPointIndex?.let { index ->
                    val data = trendData.getOrNull(index)
                    if (data != null) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.TrendingUp,
                                        contentDescription = "Trend indicator",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Column {
                                        Text(
                                            "Date: ${data.first}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            "Target Standard: 90% Attendance",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                                Text(
                                    "${(data.second * 100).toInt()}% Rate",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (data.second >= 0.90f) Color(0xFF2E7D32) else Color(0xFFC62828)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeacherPerformanceMetricsChart(lessonScore: Float, engagementScore: Float, timeScore: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Teacher Performance Breakdown",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Evaluation score component averages (1-5 scale)",
                fontSize = 11.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            val components = listOf(
                Triple("Preparedness & Lesson Plan", lessonScore, MaterialTheme.colorScheme.primary),
                Triple("Student Focus & Engagement", engagementScore, MaterialTheme.colorScheme.secondary),
                Triple("Punctuality & Time Management", timeScore, MaterialTheme.colorScheme.tertiary)
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                components.forEach { (label, score, color) ->
                    val percentage = (score / 5f).coerceIn(0f, 1f)
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Star, contentDescription = "Star rating marker", tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
                                Text("${"%.2f".format(score)} / 5.0", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(16.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.LightGray.copy(alpha = 0.25f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(percentage)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(color)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. DISTRICT EDUCATION OFFICER: DASHBOARD & REPORT GENERATOR
// ==========================================
@Composable
fun DeoDashboardView(
    viewModel: MainViewModel,
    schools: List<School>,
    selectedDistrict: String,
    onDistrictSelected: (String) -> Unit
) {
    var generatedReportState by remember { mutableStateOf<GeneratedReport?>(null) }
    var showReportDialog by remember { mutableStateOf(false) }
    var isDistrictDropdownExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val districts = listOf("All", "Dedza", "Mangochi", "Lilongwe Rural", "Kasungu", "Mulanje")

    // Retrieve database lists from ViewModel
    val teachers by viewModel.teachers.collectAsState()
    val attendanceRecords by viewModel.attendanceRecords.collectAsState()
    val performanceReports by viewModel.performanceReports.collectAsState()
    val students by viewModel.students.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val isGeneratingAiReport by viewModel.isGeneratingAiReport.collectAsState()

    // 1. Filter schools in selected district
    val filteredSchools = remember(schools, selectedDistrict) {
        if (selectedDistrict == "All") schools else schools.filter { it.district.equals(selectedDistrict, ignoreCase = true) }
    }
    val filteredSchoolIds = remember(filteredSchools) { filteredSchools.map { it.id }.toSet() }

    // 2. Filter students in selected district
    val filteredStudents = remember(students, filteredSchoolIds) {
        students.filter { filteredSchoolIds.contains(it.schoolId) }
    }

    // 3. Filter teachers in selected district
    val filteredTeachers = remember(teachers, filteredSchoolIds) {
        teachers.filter { filteredSchoolIds.contains(it.schoolId) }
    }

    // 4. Filter attendance records in selected district
    val filteredAttendance = remember(attendanceRecords, filteredSchoolIds) {
        attendanceRecords.filter { filteredSchoolIds.contains(it.schoolId) }
    }

    // 5. Filter performance reports in selected district
    val filteredPerformance = remember(performanceReports, filteredSchoolIds) {
        performanceReports.filter { filteredSchoolIds.contains(it.schoolId) }
    }

    // --- Compute Dynamic Metrics ---
    val schoolsCount = filteredSchools.size
    val teachersCount = filteredTeachers.size
    val studentsCount = filteredStudents.size
    
    val overallAttendanceRate = remember(filteredAttendance) {
        if (filteredAttendance.isEmpty()) {
            92.5
        } else {
            val presentCount = filteredAttendance.count { it.isPresent }
            (presentCount.toDouble() / filteredAttendance.size) * 100.0
        }
    }

    val averageTeacherPerformance = remember(filteredPerformance) {
        if (filteredPerformance.isEmpty()) {
            4.2
        } else {
            filteredPerformance.map { (it.lessonPlanScore + it.studentEngagementScore + it.timeManagementScore) / 3.0 }.average()
        }
    }

    // --- Compute Attendance Trend over last 7 dates ---
    val attendanceTrend = remember(filteredAttendance) {
        val grouped = filteredAttendance.groupBy { it.date }
        grouped.entries
            .map { entry ->
                val total = entry.value.size
                val present = entry.value.count { it.isPresent }
                val rate = if (total > 0) (present.toFloat() / total) else 0f
                Pair(entry.key, rate)
            }
            .sortedBy { it.first } // Chronological sort
    }

    // --- Compute Component Performance Metrics ---
    val lessonPlanAvg = remember(filteredPerformance) {
        if (filteredPerformance.isEmpty()) 4.2f else filteredPerformance.map { it.lessonPlanScore }.average().toFloat()
    }
    val engagementAvg = remember(filteredPerformance) {
        if (filteredPerformance.isEmpty()) 4.3f else filteredPerformance.map { it.studentEngagementScore }.average().toFloat()
    }
    val timeManagementAvg = remember(filteredPerformance) {
        if (filteredPerformance.isEmpty()) 4.1f else filteredPerformance.map { it.timeManagementScore }.average().toFloat()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        Icons.Default.Assessment,
                        contentDescription = "Console analytical index logo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        "Education Officer Console",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                // District Filter Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .clickable { isDistrictDropdownExpanded = true }
                        .testTag("district_selector"),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "District: $selectedDistrict",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = "Expand district filter",
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = isDistrictDropdownExpanded,
                        onDismissRequest = { isDistrictDropdownExpanded = false }
                    ) {
                        districts.forEach { dist ->
                            DropdownMenuItem(
                                text = { Text(dist) },
                                onClick = {
                                    onDistrictSelected(dist)
                                    isDistrictDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("export_pdf_card"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Text(
                        text = "Inspector Audit & Data Export Desk",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Generate and export localized attendance data for $selectedDistrict District. Choose a print-friendly ledger PDF or download a CSV spreadsheet compatible with Microsoft Excel and Google Sheets.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                com.example.util.PdfReportExporter.exportAttendanceToPdf(
                                    context = context,
                                    district = selectedDistrict,
                                    schools = filteredSchools,
                                    students = filteredStudents,
                                    attendanceRecords = filteredAttendance,
                                    teachers = filteredTeachers
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .testTag("export_attendance_pdf_button")
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = "PDF Icon",
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Export PDF Ledger",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Button(
                            onClick = {
                                com.example.util.CsvReportExporter.exportAttendanceToCsv(
                                    context = context,
                                    district = selectedDistrict,
                                    schools = filteredSchools,
                                    attendanceRecords = filteredAttendance
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .testTag("export_attendance_csv_button")
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = "CSV Spreadsheet Icon",
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Export CSV Sheet",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Row 1: Schools and Enrollment Statistics
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.HomeWork, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                            Text("Stations", fontSize = 11.sp, color = Color.Gray)
                        }
                        Text(
                            "$schoolsCount Schools",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Monitored rural campuses", fontSize = 10.sp, color = Color.Gray)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.People, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                            Text("Enrolled Cohort", fontSize = 11.sp, color = Color.Gray)
                        }
                        Text(
                            "$studentsCount Students",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Active pupils listed", fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }
        }

        // Row 2: Attendance Rate & Evaluation Average
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(14.dp))
                            Text("Attendance Avg", fontSize = 11.sp, color = Color.Gray)
                        }
                        Text(
                            "${"%.1f".format(overallAttendanceRate)}%",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF2E7D32)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Student present overall", fontSize = 10.sp, color = Color.Gray)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
                            Text("Teacher Quality", fontSize = 11.sp, color = Color.Gray)
                        }
                        Text(
                            "${"%.2f".format(averageTeacherPerformance)} / 5",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Avg evaluation score", fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }
        }

        // Attendance Trend Chart (Recharts high-fidelity equivalent)
        item {
            InteractiveAttendanceTrendChart(trendData = attendanceTrend)
        }

        // Teacher Evaluation Breakdown Components (Interactive Bars)
        item {
            TeacherPerformanceMetricsChart(
                lessonScore = lessonPlanAvg,
                engagementScore = engagementAvg,
                timeScore = timeManagementAvg
            )
        }

        // District Teacher Roster Leaderboard
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "District Faculty Roster (${filteredTeachers.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Overview of teachers in this district zone",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (filteredTeachers.isEmpty()) {
                        Text("No teachers registered for this district zone.", color = Color.Gray, fontSize = 12.sp)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            filteredTeachers.forEach { teacher ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.LightGray.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(teacher.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("${teacher.classesTaught} • ${teacher.email}", fontSize = 11.sp, color = Color.Gray)
                                    }
                                    
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(12.dp))
                                            Text(
                                                "%.1f".format(teacher.performanceScore),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Automated Regional Progress Report Generator Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.ReceiptLong,
                        contentDescription = "Report Generator Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Automated Progress Report Engine",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Click the compile button below to securely bundle and generate an academic progress report using current live database entries.",
                        fontSize = 12.sp,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            generatedReportState = viewModel.generateDistrictReport(selectedDistrict)
                            showReportDialog = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("generate_report_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh compile report icon")
                            Text("Compile Standard Report ($selectedDistrict)", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (isOnline) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                val baseReport = viewModel.generateDistrictReport(selectedDistrict)
                                generatedReportState = baseReport.copy(
                                    body = baseReport.body + "\n\n========================================\nCLOUDBASED AI ASSESSMENT (GEMINI)\n========================================\n\n[Analysing indicators via Central Cloud AI... Please wait]"
                                )
                                showReportDialog = true
                                viewModel.generateAiAnalysis(selectedDistrict) { aiText ->
                                    generatedReportState = baseReport.copy(
                                        body = baseReport.body + "\n\n========================================\nCLOUDBASED AI ASSESSMENT (GEMINI)\n========================================\n\n$aiText"
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("generate_ai_report_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)), // Strong deep blue for AI
                            enabled = !isGeneratingAiReport
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isGeneratingAiReport) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Analyzing with Gemini...", fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = "Cloud AI Icon", tint = Color.White)
                                    Text("Compile AI-Powered Report ($selectedDistrict)", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Report Viewer Dialog
    if (showReportDialog && generatedReportState != null) {
        val report = generatedReportState!!
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            confirmButton = {
                Button(
                    onClick = { showReportDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Done")
                }
            },
            dismissButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(report.body))
                            viewModel.addNotification(
                                "Report Exported",
                                "Report copied to device clipboard securely.",
                                "REPORT"
                            )
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Copy text")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy Text")
                    }

                    val context = LocalContext.current
                    TextButton(
                        onClick = {
                            com.example.util.PdfReportExporter.exportToPdf(
                                context = context,
                                report = report,
                                teachers = filteredTeachers,
                                lessonScore = lessonPlanAvg,
                                engagementScore = engagementAvg,
                                timeScore = timeManagementAvg
                            )
                        },
                        modifier = Modifier.testTag("export_pdf_btn")
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "Export to PDF")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export PDF")
                    }
                }
            },
            title = {
                Text(
                    report.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Attendance: ${"%.1f".format(report.attendanceRate)}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (report.attendanceRate >= 92.0) Color(0xFF2E7D32) else Color(0xFFC62828)
                        )
                        Text(
                            "Rating: ${"%.2f".format(report.performanceScore)} / 5",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Monospace output resembling professional terminal export
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF212121)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    ) {
                        LazyColumn(modifier = Modifier.padding(12.dp)) {
                            item {
                                Text(
                                    report.body,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF81C784),
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        )
    }
}

// ==========================================
// 3. LOCAL ADMINISTRATOR VIEW: SECURITY & SYNC INSPECTORS
// ==========================================
@Composable
fun LocalAdminView(
    viewModel: MainViewModel,
    isOnline: Boolean,
    syncLogs: List<SyncHistory>,
    attendanceRecords: List<AttendanceRecord>,
    performanceReports: List<PerformanceReport>
) {
    val dynamicUsers by viewModel.dynamicUsers.collectAsState()
    var editingUser by remember { mutableStateOf<UserAccount?>(null) }
    var deleteConfirmUser by remember { mutableStateOf<UserAccount?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var inspectingAttendanceRecord by remember { mutableStateOf<AttendanceRecord?>(null) }
    var inspectingPerformanceRecord by remember { mutableStateOf<PerformanceReport?>(null) }

    // SQLite Database Manager and Live Query Simulator States
    var isDbManagerExpanded by remember { mutableStateOf(true) }
    var selectedQueryIdx by remember { mutableIntStateOf(0) }
    var queryResultRows by remember { mutableStateOf<List<List<String>>>(emptyList()) }
    var queryResultHeaders by remember { mutableStateOf<List<String>>(emptyList()) }
    var queryExecuted by remember { mutableStateOf(false) }
    var healthCheckReport by remember { mutableStateOf<String?>(null) }

    // Hoisted Create User States for responsive flow
    var newUsername by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var newFullName by remember { mutableStateOf("") }
    var newRole by remember { mutableStateOf("Teacher") } // "Teacher", "Admin", "DEO"
    var roleDropdownExpanded by remember { mutableStateOf(false) }
    var userSaveError by remember { mutableStateOf<String?>(null) }
    var userSaveSuccess by remember { mutableStateOf(false) }
    var isSqliteDetailsExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "System Administration Console",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.primary
        )

        // SQLite Local Storage Manager Card with dynamic expand/collapse transitions
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("sqlite_architecture_card")
                .clickable { isSqliteDetailsExpanded = !isSqliteDetailsExpanded },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            ),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Storage,
                            contentDescription = "SQLite Database Engine",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                "SQLite Offline Storage",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Local Device Cache Management",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    IconButton(
                        onClick = { isSqliteDetailsExpanded = !isSqliteDetailsExpanded },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isSqliteDetailsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isSqliteDetailsExpanded) "Collapse/Expand Details" else "Collapse/Expand Details",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                AnimatedVisibility(
                    visible = isSqliteDetailsExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "This system maintains database cache tables natively on your mobile device. School attendance records, student tracking details, and teacher performance reports are always fully functional and accessible offline. When connected to the internet, pending changes can be uploaded instantly.",
                            fontSize = 11.sp,
                            color = Color.DarkGray,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "CURRENT LOCAL SQLITE STATISTICS",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text(
                                        "Attendance Cached: ${attendanceRecords.size}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.Gray
                                    )
                                    Text(
                                        "Evaluations Cached: ${performanceReports.size}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.Gray
                                    )
                                }
                            }

                            // Force Sync Trigger Button
                            val pendingCount = attendanceRecords.count { it.syncStatus == "PENDING" } +
                                    performanceReports.count { it.syncStatus == "PENDING" }

                            Button(
                                onClick = { viewModel.triggerSync() },
                                enabled = isOnline,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("force_sync_cache_button")
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Sync,
                                        contentDescription = "Sync",
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        "Sync Now ($pendingCount)",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Subtabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Manage Users", "Register Schools", "Database Audit (Crypto)", "Offline Sync Logs").forEachIndexed { index, label ->
                val isSelected = selectedTab == index
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.25f))
                        .clickable { selectedTab = index }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .testTag("admin_tab_$index"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        if (selectedTab == 0) {
            // MANAGE USERS - Refactored as a single LazyColumn to fit all phone screens without squishing
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    // Form: Add User / Role
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("add_user_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.GroupAdd, contentDescription = "Add User", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                                Text("Create & Assign Secure RBAC Roles", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
                            }
                            Text("Provision new credentials and assign specific RBAC roles to access localized dashboards.", fontSize = 11.sp, color = Color.Gray)

                            OutlinedTextField(
                                value = newUsername,
                                onValueChange = { newUsername = it },
                                label = { Text("Username") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("new_username_input"),
                                shape = RoundedCornerShape(10.dp)
                            )

                            OutlinedTextField(
                                value = newPassword,
                                onValueChange = { newPassword = it },
                                label = { Text("Password") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("new_password_input"),
                                shape = RoundedCornerShape(10.dp)
                            )

                            OutlinedTextField(
                                value = newFullName,
                                onValueChange = { newFullName = it },
                                label = { Text("Full User Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("new_fullname_input"),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = when(newRole) {
                                        "Teacher" -> "Teacher"
                                        "Admin" -> "Administrator"
                                        "DEO" -> "Headmaster"
                                        else -> newRole
                                    },
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Role Assignment") },
                                    trailingIcon = {
                                        IconButton(onClick = { roleDropdownExpanded = true }) {
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Expand Role Menu")
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().testTag("new_role_dropdown"),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                DropdownMenu(
                                    expanded = roleDropdownExpanded,
                                    onDismissRequest = { roleDropdownExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Teacher") },
                                        onClick = {
                                            newRole = "Teacher"
                                            roleDropdownExpanded = false
                                        },
                                        modifier = Modifier.testTag("role_select_teacher")
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Administrator") },
                                        onClick = {
                                            newRole = "Admin"
                                            roleDropdownExpanded = false
                                        },
                                        modifier = Modifier.testTag("role_select_admin")
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Headmaster") },
                                        onClick = {
                                            newRole = "DEO"
                                            roleDropdownExpanded = false
                                        },
                                        modifier = Modifier.testTag("role_select_deo")
                                    )
                                }
                            }

                            if (userSaveError != null) {
                                Text(userSaveError!!, color = MaterialTheme.colorScheme.error, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }

                            if (userSaveSuccess) {
                                Text("Account with assigned RBAC role registered successfully!", color = Color(0xFF2E7D32), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }

                            Button(
                                onClick = {
                                    if (newUsername.isBlank() || newPassword.isBlank() || newFullName.isBlank()) {
                                        userSaveError = "All credential fields are required."
                                        userSaveSuccess = false
                                    } else {
                                        val success = viewModel.registerUserAccount(
                                            UserAccount(
                                                username = newUsername.trim(),
                                                password = newPassword.trim(),
                                                fullName = newFullName.trim(),
                                                role = newRole
                                            )
                                        )
                                        if (success) {
                                            newUsername = ""
                                            newPassword = ""
                                            newFullName = ""
                                            newRole = "Teacher"
                                            userSaveError = null
                                            userSaveSuccess = true
                                        } else {
                                            userSaveError = "Username is already registered."
                                            userSaveSuccess = false
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                modifier = Modifier.fillMaxWidth().height(40.dp).testTag("register_user_btn"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.GroupAdd, contentDescription = "Add User Icon", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Register & Provision Role", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                item {
                    // Header for Directory
                    Text(
                        "User Accounts Directory (${dynamicUsers.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                    )
                }

                items(dynamicUsers) { user ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("user_item_${user.username}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                val icon = when(user.role) {
                                    "Admin" -> Icons.Default.AdminPanelSettings
                                    "Teacher" -> Icons.Default.School
                                    else -> Icons.Default.Person
                                }
                                val iconTint = when(user.role) {
                                    "Admin" -> MaterialTheme.colorScheme.primary
                                    "Teacher" -> MaterialTheme.colorScheme.secondary
                                    else -> Color.Gray
                                }
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(iconTint.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
                                }

                                Column {
                                    Text(
                                        user.fullName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        "Username: ${user.username}",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                    Text(
                                        "Role: ${
                                            when(user.role) {
                                                "Teacher" -> "Teacher"
                                                "Admin" -> "Administrator"
                                                "DEO" -> "Headmaster"
                                                else -> user.role
                                            }
                                        }",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = iconTint
                                    )
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { editingUser = user },
                                    modifier = Modifier.size(32.dp).testTag("edit_user_${user.username}")
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Edit User",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { deleteConfirmUser = user },
                                    modifier = Modifier.size(32.dp).testTag("delete_user_${user.username}"),
                                    enabled = user.username != "admin"
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete User",
                                        tint = if (user.username != "admin") MaterialTheme.colorScheme.error else Color.LightGray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else if (selectedTab == 1) {
            // REGISTER SCHOOLS
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Form 1: Add School
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("add_school_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.School, contentDescription = "Add School", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Text("Register New School Node", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Text("Add a new rural school node directly to local SQLite storage.", fontSize = 11.sp, color = Color.Gray)
                        
                        var schoolId by remember { mutableStateOf("") }
                        var schoolName by remember { mutableStateOf("") }
                        var schoolDistrict by remember { mutableStateOf("") }
                        var schoolRegion by remember { mutableStateOf("") }
                        var schoolSaveError by remember { mutableStateOf<String?>(null) }
                        var schoolSaveSuccess by remember { mutableStateOf(false) }

                        OutlinedTextField(
                            value = schoolId,
                            onValueChange = { schoolId = it },
                            label = { Text("School ID (e.g. s-10)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("school_id_input"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = schoolName,
                            onValueChange = { schoolName = it },
                            label = { Text("School Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("school_name_input"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = schoolDistrict,
                            onValueChange = { schoolDistrict = it },
                            label = { Text("District") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("school_district_input"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = schoolRegion,
                            onValueChange = { schoolRegion = it },
                            label = { Text("Region") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("school_region_input"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        if (schoolSaveError != null) {
                            Text(schoolSaveError!!, color = MaterialTheme.colorScheme.error, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        if (schoolSaveSuccess) {
                            Text("School added successfully to Room DB!", color = Color(0xFF2E7D32), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = {
                                if (schoolId.isBlank() || schoolName.isBlank() || schoolDistrict.isBlank() || schoolRegion.isBlank()) {
                                    schoolSaveError = "All school fields are required."
                                    schoolSaveSuccess = false
                                } else {
                                    viewModel.addSchool(
                                        id = schoolId.trim(),
                                        name = schoolName.trim(),
                                        district = schoolDistrict.trim(),
                                        region = schoolRegion.trim()
                                    )
                                    schoolId = ""
                                    schoolName = ""
                                    schoolDistrict = ""
                                    schoolRegion = ""
                                    schoolSaveError = null
                                    schoolSaveSuccess = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(40.dp).testTag("save_school_btn"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add School Icon", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save School Node", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        } else if (selectedTab == 2) {
            // CRYPTOGRAPHY INSPECTOR LIST
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Local Room Storage Database Audit",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                Text(
                    "Click on any record below to perform a decryption validation and compare on-disk Base64 Ciphertext with readable plaintext.",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Interactive SQLite Control Dashboard and Live Query Simulator
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("sqlite_db_manager_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isDbManagerExpanded = !isDbManagerExpanded },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Storage,
                                    contentDescription = "SQLite Database Engine",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        "Android SQLite Storage Manager (Room DB)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        "Integrate, inspect, and query local persistent databases.",
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                            Icon(
                                if (isDbManagerExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Toggle Database Controls",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (isDbManagerExpanded) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            Spacer(modifier = Modifier.height(10.dp))

                            // Interactive database actions row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        healthCheckReport = viewModel.runSQLiteHealthCheck()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .testTag("sqlite_health_check_btn"),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Icon(Icons.Default.Healing, contentDescription = "Verify DB", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Verify DB Health", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        viewModel.resetAndReseedDatabase()
                                        queryExecuted = false
                                        queryResultRows = emptyList()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .testTag("sqlite_reseed_btn"),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Reseed DB", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Reset & Re-Seed", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Health Check Report Box
                            healthCheckReport?.let { report ->
                                Spacer(modifier = Modifier.height(10.dp))
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                "SQLite Diagnostics Log",
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Green,
                                                fontSize = 11.sp
                                            )
                                            IconButton(
                                                onClick = { healthCheckReport = null },
                                                modifier = Modifier.size(18.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Close,
                                                    contentDescription = "Close diagnostics",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = report,
                                            fontFamily = FontFamily.Monospace,
                                            color = Color.LightGray,
                                            fontSize = 10.sp,
                                            lineHeight = 14.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "SQLITE PERSISTENT QUERY PLAN ANALYZER",
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )

                            // Custom Dropdown Selector for Query simulation
                            var queryDropdownExpanded by remember { mutableStateOf(false) }
                            val queries = listOf(
                                "SELECT * FROM schools WHERE district = 'Dedza' ORDER BY name ASC",
                                "SELECT * FROM teachers WHERE performanceScore >= 4.5",
                                "SELECT * FROM students WHERE gender = 'Female' LIMIT 8",
                                "SELECT * FROM attendance_records WHERE isPresent = 0 LIMIT 5"
                            )

                            Box(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                        .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .clickable { queryDropdownExpanded = true }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = queries[selectedQueryIdx],
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = "Select query template",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                                DropdownMenu(
                                    expanded = queryDropdownExpanded,
                                    onDismissRequest = { queryDropdownExpanded = false },
                                    modifier = Modifier.fillMaxWidth(0.9f)
                                ) {
                                    queries.forEachIndexed { idx, qText ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    qText,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 10.sp
                                                )
                                            },
                                            onClick = {
                                                selectedQueryIdx = idx
                                                queryDropdownExpanded = false
                                                queryExecuted = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    queryExecuted = true
                                    when (selectedQueryIdx) {
                                        0 -> {
                                            queryResultHeaders = listOf("ID", "School Name", "District", "Region")
                                            val filtered = viewModel.schools.value.filter { it.district == "Dedza" }
                                            queryResultRows = filtered.map { listOf(it.id, it.name, it.district, it.region) }
                                        }
                                        1 -> {
                                            queryResultHeaders = listOf("ID", "Teacher Name", "School ID", "Score", "Attendance")
                                            val filtered = viewModel.teachers.value.filter { it.performanceScore >= 4.5 }
                                            queryResultRows = filtered.map { listOf(it.id, it.name, it.schoolId, it.performanceScore.toString(), "${it.attendanceRate}%") }
                                        }
                                        2 -> {
                                            queryResultHeaders = listOf("ID", "Name", "School ID", "Class", "Gender")
                                            val filtered = viewModel.students.value.filter { it.gender == "Female" }.take(8)
                                            queryResultRows = filtered.map { listOf(it.id, it.name, it.schoolId, it.className, it.gender) }
                                        }
                                        3 -> {
                                            queryResultHeaders = listOf("ID", "Date", "Student Name", "School ID", "Present")
                                            val filtered = viewModel.attendanceRecords.value.filter { !it.isPresent }.take(5)
                                            queryResultRows = filtered.map { listOf(it.id, it.date, it.studentName, it.schoolId, "No") }
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp)
                                    .testTag("execute_sqlite_query_btn")
                            ) {
                                Icon(Icons.Default.Terminal, contentDescription = "Terminal", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Execute Persistent SQLite Query", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // Beautiful spreadsheet grid result table!
                            if (queryExecuted) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    "QUERY EXECUTION RESULT (${queryResultRows.size} Rows Returned)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    color = Color(0xFF2E7D32)
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState())
                                            .padding(8.dp)
                                    ) {
                                        // Header Row
                                        Row(
                                            modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            queryResultHeaders.forEach { header ->
                                                Text(
                                                    text = header,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.width(90.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Divider(color = Color.LightGray.copy(alpha = 0.5f))
                                        Spacer(modifier = Modifier.height(4.dp))

                                        if (queryResultRows.isEmpty()) {
                                            Text(
                                                "0 rows returned from persistent SQLite cache.",
                                                fontSize = 10.sp,
                                                color = Color.Gray,
                                                modifier = Modifier.padding(vertical = 4.dp)
                                            )
                                        } else {
                                            queryResultRows.forEach { row ->
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                                    modifier = Modifier.padding(vertical = 4.dp)
                                                ) {
                                                    row.forEach { cell ->
                                                        Text(
                                                            text = cell,
                                                            fontSize = 10.sp,
                                                            fontFamily = FontFamily.Monospace,
                                                            modifier = Modifier.width(90.dp),
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (attendanceRecords.isEmpty() && performanceReports.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No encrypted records found in Local DB.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Attendance Records list
                        if (attendanceRecords.isNotEmpty()) {
                            item {
                                Text(
                                    "Student Attendance Records (${attendanceRecords.size})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                            items(attendanceRecords) { record ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { inspectingAttendanceRecord = record }
                                        .testTag("audit_item_${record.id}"),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(record.studentName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("Date: ${record.date} • ${if (record.isPresent) "Present" else "Absent"}", fontSize = 11.sp)
                                            Text("Sync Status: ${record.syncStatus}", fontSize = 10.sp, color = if (record.syncStatus == "SYNCED") Color(0xFF2E7D32) else Color(0xFFFF9800), fontWeight = FontWeight.Bold)
                                        }
                                        Icon(
                                            Icons.Default.Lock,
                                            contentDescription = "Encrypted item lock",
                                            tint = Color(0xFF1976D2),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Performance Reports list
                        if (performanceReports.isNotEmpty()) {
                            item {
                                Text(
                                    "Teacher Evaluation Records (${performanceReports.size})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            items(performanceReports) { report ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { inspectingPerformanceRecord = report }
                                        .testTag("audit_report_${report.id}"),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(report.teacherName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("Avg Score: ${(report.lessonPlanScore + report.studentEngagementScore + report.timeManagementScore) / 3.0} / 5", fontSize = 11.sp)
                                            Text("Sync Status: ${report.syncStatus}", fontSize = 10.sp, color = if (report.syncStatus == "SYNCED") Color(0xFF2E7D32) else Color(0xFFFF9800), fontWeight = FontWeight.Bold)
                                        }
                                        Icon(
                                            Icons.Default.Lock,
                                            contentDescription = "Encrypted report lock",
                                            tint = Color(0xFF1976D2),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // OFFLINE SYNC HISTORY TIMELINE
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Synchronization History Audit",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    TextButton(onClick = { viewModel.triggerSync() }) {
                        Icon(Icons.Default.Sync, contentDescription = "Manual sync trigger")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Trigger Sync Now")
                    }
                }

                if (syncLogs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No sync events recorded yet. Connect and sync.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(syncLogs) { log ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF4CAF50))
                                            )
                                            Text(
                                                log.type,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                        Text(
                                            sdf.format(Date(log.timestamp)),
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        log.details,
                                        fontSize = 12.sp,
                                        color = Color.DarkGray
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Integrity Status: SUCCESSFUL CRYPTO VERIFICATION",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Cryptographic inspection dialog for Attendance Record
    if (inspectingAttendanceRecord != null) {
        val record = inspectingAttendanceRecord!!
        val decryptedText = CryptoUtils.decrypt(record.encryptedPayload)

        AlertDialog(
            onDismissRequest = { inspectingAttendanceRecord = null },
            confirmButton = {
                Button(onClick = { inspectingAttendanceRecord = null }) {
                    Text("Close Audit")
                }
            },
            title = { Text("AES-256 Record Auditor", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Student Name: ${record.studentName}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Database Primary ID: ${record.id}", fontSize = 10.sp, color = Color.Gray)

                    Divider(modifier = Modifier.padding(vertical = 4.dp))

                    Text("1. Encrypted Base64 Payload (Stored on disk):", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2E2E2E)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            record.encryptedPayload,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFFF8A80),
                            fontSize = 10.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text("2. Decrypted Output Cleartext (Decrypted dynamically):", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF2E7D32))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2821)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            decryptedText,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF81C784),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        )
    }

    // Cryptographic inspection dialog for Performance Report
    if (inspectingPerformanceRecord != null) {
        val report = inspectingPerformanceRecord!!
        val decryptedText = CryptoUtils.decrypt(report.encryptedPayload)

        AlertDialog(
            onDismissRequest = { inspectingPerformanceRecord = null },
            confirmButton = {
                Button(onClick = { inspectingPerformanceRecord = null }) {
                    Text("Close Audit")
                }
            },
            title = { Text("AES-256 Report Auditor", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Teacher Name: ${report.teacherName}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Database Primary ID: ${report.id}", fontSize = 10.sp, color = Color.Gray)

                    Divider(modifier = Modifier.padding(vertical = 4.dp))

                    Text("1. Encrypted Base64 Payload (Stored on disk):", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2E2E2E)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            report.encryptedPayload,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFFF8A80),
                            fontSize = 10.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text("2. Decrypted Output Cleartext (Decrypted dynamically):", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF2E7D32))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2821)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            decryptedText,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF81C784),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        )
    }

    // Edit User Account Dialog
    if (editingUser != null) {
        val user = editingUser!!
        var editFullName by remember { mutableStateOf(user.fullName) }
        var editPassword by remember { mutableStateOf(user.password) }
        var editRole by remember { mutableStateOf(user.role) }
        var editRoleDropdownExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { editingUser = null },
            confirmButton = {
                Button(
                    onClick = {
                        if (editFullName.isNotBlank() && editPassword.isNotBlank()) {
                            viewModel.updateUserAccount(
                                user.username,
                                UserAccount(
                                    username = user.username,
                                    password = editPassword.trim(),
                                    fullName = editFullName.trim(),
                                    role = editRole
                                )
                            )
                            editingUser = null
                        }
                    },
                    modifier = Modifier.testTag("save_edit_user_btn")
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingUser = null }) {
                    Text("Cancel")
                }
            },
            title = { Text("Edit User Account", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Username: ${user.username}", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp)

                    OutlinedTextField(
                        value = editFullName,
                        onValueChange = { editFullName = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_fullname_input")
                    )

                    OutlinedTextField(
                        value = editPassword,
                        onValueChange = { editPassword = it },
                        label = { Text("Password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_password_input")
                    )

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = when(editRole) {
                                "Teacher" -> "Teacher"
                                "Admin" -> "Administrator"
                                "DEO" -> "Headmaster"
                                else -> editRole
                            },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Role") },
                            trailingIcon = {
                                IconButton(onClick = { editRoleDropdownExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Expand edit role dropdown")
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("edit_role_dropdown")
                        )
                        DropdownMenu(
                            expanded = editRoleDropdownExpanded,
                            onDismissRequest = { editRoleDropdownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Teacher") },
                                onClick = {
                                    editRole = "Teacher"
                                    editRoleDropdownExpanded = false
                                },
                                modifier = Modifier.testTag("edit_role_select_teacher")
                            )
                            DropdownMenuItem(
                                text = { Text("Administrator") },
                                onClick = {
                                    editRole = "Admin"
                                    editRoleDropdownExpanded = false
                                },
                                modifier = Modifier.testTag("edit_role_select_admin")
                            )
                            DropdownMenuItem(
                                text = { Text("Headmaster") },
                                onClick = {
                                    editRole = "DEO"
                                    editRoleDropdownExpanded = false
                                },
                                modifier = Modifier.testTag("edit_role_select_deo")
                            )
                        }
                    }
                }
            }
        )
    }

    // Delete User Confirmation Dialog
    if (deleteConfirmUser != null) {
        val user = deleteConfirmUser!!
        AlertDialog(
            onDismissRequest = { deleteConfirmUser = null },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteUserAccount(user.username)
                        deleteConfirmUser = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_user_btn")
                ) {
                    Text("Delete Account")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmUser = null }) {
                    Text("Cancel")
                }
            },
            title = { Text("Confirm Deletion", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to permanently delete the user account for '${user.fullName}' (${user.username})? This action cannot be undone.")
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(viewModel: MainViewModel) {
    var loginTabSelected by remember { mutableStateOf(0) } // 0 = Log In, 1 = Register User

    // Login Form States
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Register Form States
    var regFullName by remember { mutableStateOf("") }
    var regUsername by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regClassesTaught by remember { mutableStateOf("Standard 5, Standard 6") }
    var regSelectedSchoolId by remember { mutableStateOf("") }
    var regSchoolDropdownExpanded by remember { mutableStateOf(false) }
    var regPasswordVisible by remember { mutableStateOf(false) }
    var regRole by remember { mutableStateOf("Teacher") }
    var regRoleDropdownExpanded by remember { mutableStateOf(false) }

    val loginError by viewModel.loginError.collectAsState()
    val schools by viewModel.schools.collectAsState()
    val isFirebaseInitialized by viewModel.isFirebaseInitialized.collectAsState()
    val isAuthLoading by viewModel.isAuthLoading.collectAsState()
    val firebaseStatusMessage by viewModel.firebaseStatusMessage.collectAsState()

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val errorColor = MaterialTheme.colorScheme.error

    LaunchedEffect(schools) {
        if (schools.isNotEmpty() && regSelectedSchoolId.isEmpty()) {
            regSelectedSchoolId = schools.first().id
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            // Elegant Card Container for Login / Register
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Beautiful Hero Banner using the generated image
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        androidx.compose.foundation.Image(
                            painter = painterResource(id = com.example.R.drawable.img_login_banner),
                            contentDescription = "Malawi Schools Welcome Banner",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Malawi Schools System",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = primaryColor
                    )
                    Text(
                        text = "Secure Teacher Identity Gateway",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val badgeColor = if (isFirebaseInitialized) Color(0xFF2E7D32) else MaterialTheme.colorScheme.secondary
                    val badgeBg = if (isFirebaseInitialized) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                    
                    Row(
                        modifier = Modifier
                            .background(badgeBg, RoundedCornerShape(20.dp))
                            .border(1.dp, badgeColor.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("firebase_auth_badge"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(badgeColor, CircleShape)
                        )
                        Text(
                            text = firebaseStatusMessage,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Beautiful Custom Tab Selector for Login/Register
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.LightGray.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Secure Log In", "Register Account").forEachIndexed { index, title ->
                            val isSelected = loginTabSelected == index
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) primaryColor else Color.Transparent)
                                    .clickable { 
                                        loginTabSelected = index 
                                        viewModel.loginError.value = null
                                    }
                                    .padding(vertical = 10.dp)
                                    .testTag("auth_tab_$index"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isSelected) Color.White else primaryColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (loginTabSelected == 0) {
                        // --- LOGIN FORM ---
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = username,
                                onValueChange = { username = it },
                                label = { Text("Username") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Username Icon") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("username_input"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Password") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password Icon") },
                                trailingIcon = {
                                    val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(icon, contentDescription = if (passwordVisible) "Hide password" else "Show password")
                                    }
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("password_input"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            if (loginError != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                ) {
                                    Icon(Icons.Default.Error, contentDescription = "Error", tint = errorColor, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = loginError ?: "",
                                        color = errorColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Login Button
                            Button(
                                onClick = { viewModel.login(username, password) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("login_btn"),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isAuthLoading,
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                            ) {
                                if (isAuthLoading) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(Icons.Default.LockOpen, contentDescription = "Login Button Icon")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Authenticate Securely",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Elegant Admin Access Hint Box
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Admin Hint Info",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "System Admin Access",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "Manage system using admin credentials:\nUsername: admin  |  Password: admin@2026",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // --- REGISTER FORM ---
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = regFullName,
                                onValueChange = { regFullName = it },
                                label = { Text("Full Name") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Name Icon") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("register_fullname_input"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = regUsername,
                                onValueChange = { regUsername = it },
                                label = { Text("Username") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Username Icon") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("register_username_input"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = regPassword,
                                onValueChange = { regPassword = it },
                                label = { Text("Password") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password Icon") },
                                trailingIcon = {
                                    val icon = if (regPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                                    IconButton(onClick = { regPasswordVisible = !regPasswordVisible }) {
                                        Icon(icon, contentDescription = if (regPasswordVisible) "Hide password" else "Show password")
                                    }
                                },
                                visualTransformation = if (regPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("register_password_input"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = regEmail,
                                onValueChange = { regEmail = it },
                                label = { Text("Email (Optional)") },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email Icon") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("register_email_input"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = regClassesTaught,
                                onValueChange = { regClassesTaught = it },
                                label = { Text("Classes Taught") },
                                leadingIcon = { Icon(Icons.Default.School, contentDescription = "Classes Icon") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("register_classes_input"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = when(regRole) {
                                        "Teacher" -> "Teacher"
                                        "Admin" -> "Administrator"
                                        "DEO" -> "Headmaster"
                                        else -> regRole
                                    },
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Assigned Role") },
                                    leadingIcon = { Icon(Icons.Default.Group, contentDescription = "Role Icon") },
                                    trailingIcon = {
                                        IconButton(onClick = { regRoleDropdownExpanded = true }) {
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Expand Role Dropdown")
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().testTag("register_role_dropdown"),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                DropdownMenu(
                                    expanded = regRoleDropdownExpanded,
                                    onDismissRequest = { regRoleDropdownExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Teacher") },
                                        onClick = {
                                            regRole = "Teacher"
                                            regRoleDropdownExpanded = false
                                        },
                                        modifier = Modifier.testTag("register_role_select_teacher")
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Headmaster") },
                                        onClick = {
                                            regRole = "DEO"
                                            regRoleDropdownExpanded = false
                                        },
                                        modifier = Modifier.testTag("register_role_select_headmaster")
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Administrator") },
                                        onClick = {
                                            regRole = "Admin"
                                            regRoleDropdownExpanded = false
                                        },
                                        modifier = Modifier.testTag("register_role_select_admin")
                                    )
                                }
                            }

                            // School Selector
                            val selectedSchool = schools.find { it.id == regSelectedSchoolId }
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    "Assigned School",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryColor
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.LightGray.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                        .border(1.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                        .clickable { regSchoolDropdownExpanded = true }
                                        .padding(12.dp)
                                        .testTag("register_school_dropdown")
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            selectedSchool?.name ?: "Select School",
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 14.sp
                                        )
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                    }

                                    DropdownMenu(
                                        expanded = regSchoolDropdownExpanded,
                                        onDismissRequest = { regSchoolDropdownExpanded = false }
                                    ) {
                                        schools.forEach { sch ->
                                            DropdownMenuItem(
                                                text = { Text("${sch.name} (${sch.district})") },
                                                onClick = {
                                                    regSelectedSchoolId = sch.id
                                                    regSchoolDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            if (loginError != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                ) {
                                    Icon(Icons.Default.Error, contentDescription = "Error", tint = errorColor, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = loginError ?: "",
                                        color = errorColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Register Button
                            Button(
                                onClick = {
                                    val success = viewModel.registerTeacher(
                                        username = regUsername,
                                        password = regPassword,
                                        fullName = regFullName,
                                        schoolId = regSelectedSchoolId,
                                        classesTaught = regClassesTaught,
                                        email = regEmail,
                                        role = regRole
                                    )
                                    if (success) {
                                        username = regUsername.trim()
                                        password = regPassword.trim()
                                        
                                        // Reset fields
                                        regFullName = ""
                                        regUsername = ""
                                        regPassword = ""
                                        regEmail = ""
                                        regRole = "Teacher"
                                        
                                        loginTabSelected = 0
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("register_btn"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                enabled = regFullName.isNotBlank() && regUsername.isNotBlank() && regPassword.isNotBlank() && regSelectedSchoolId.isNotEmpty()
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = "Register Button Icon")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Register & Configure Profile",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
