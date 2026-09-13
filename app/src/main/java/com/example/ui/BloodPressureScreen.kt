package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.BloodPressureClassifier
import com.example.ui.components.BpCategoryCard
import com.example.ui.components.BpInputField
import com.example.ui.components.BpReadingItem
import com.example.ui.components.BpStatsSummary
import com.example.ui.components.GoogleSheetsConfigDialog
import com.example.ui.theme.PrimaryRose

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BloodPressureScreen(
    viewModel: BloodPressureViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val systolic by viewModel.systolicInput.collectAsStateWithLifecycle()
    val diastolic by viewModel.diastolicInput.collectAsStateWithLifecycle()
    val pulse by viewModel.pulseInput.collectAsStateWithLifecycle()
    val notes by viewModel.notesInput.collectAsStateWithLifecycle()
    val selectedArm by viewModel.selectedArm.collectAsStateWithLifecycle()
    val selectedTag by viewModel.selectedTag.collectAsStateWithLifecycle()

    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    val allReadings by viewModel.allReadings.collectAsStateWithLifecycle()
    val sheetsUrl by viewModel.sheetsUrl.collectAsStateWithLifecycle()
    val autoSyncEnabled by viewModel.autoSyncEnabled.collectAsStateWithLifecycle()
    val isSheetsConfigured by viewModel.isSheetsConfigured.collectAsStateWithLifecycle()
    val pendingSyncCount by viewModel.pendingSyncCount.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showConfigDialog by remember { mutableStateOf(false) }

    // Live classification of currently entered numbers
    val currentSys = systolic.toIntOrNull() ?: 0
    val currentDia = diastolic.toIntOrNull() ?: 0
    val liveCategory = remember(currentSys, currentDia) {
        BloodPressureClassifier.classify(currentSys, currentDia)
    }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissUserMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = PrimaryRose,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Nani BP Tracker",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "Daily Blood Pressure & Sheets Log",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                },
                actions = {
                    // Google Sheets Settings Button
                    IconButton(
                        onClick = { showConfigDialog = true },
                        modifier = Modifier.testTag("sheets_settings_icon_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.TableChart,
                            contentDescription = "Google Sheets Settings",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryRose,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Google Sheets Status Banner
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showConfigDialog = true }
                    .testTag("sheets_status_banner"),
                color = if (isSheetsConfigured) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (isSheetsConfigured) Icons.Default.CloudDone else Icons.Default.CloudOff,
                            contentDescription = null,
                            tint = if (isSheetsConfigured) Color(0xFF2E7D32) else Color(0xFFE65100),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isSheetsConfigured) "Google Sheets: Connected" else "Google Sheets: Not connected yet",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSheetsConfigured) Color(0xFF2E7D32) else Color(0xFFE65100),
                                    fontSize = 13.sp
                                )
                            )
                            Text(
                                text = if (isSheetsConfigured) "Every reading auto-syncs to your spreadsheet" else "Tap here to connect spreadsheet or export CSV",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = if (isSheetsConfigured) Color(0xFF388E3C) else Color(0xFFBF360C)
                                )
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSheetsConfigured) Color(0xFF2E7D32).copy(alpha = 0.15f) else Color(0xFFE65100).copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isSheetsConfigured) "Settings" else "Setup",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSheetsConfigured) Color(0xFF2E7D32) else Color(0xFFE65100)
                            )
                        )
                    }
                }
            }

            // Tab Navigation
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = PrimaryRose
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("New Reading", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    },
                    modifier = Modifier.testTag("tab_new_reading")
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (allReadings.isNotEmpty()) "History (${allReadings.size})" else "History",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    },
                    modifier = Modifier.testTag("tab_history")
                )
            }

            // Tab Content
            if (selectedTabIndex == 0) {
                // New Reading Entry Form
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Live Health Category Indicator
                    BpCategoryCard(
                        categoryInfo = liveCategory,
                        systolic = currentSys,
                        diastolic = currentDia
                    )

                    // Systolic (Top Number) Input
                    BpInputField(
                        label = "Systolic (Top Number)",
                        subLabel = "Pressure while heart is beating",
                        unit = "mmHg",
                        value = systolic,
                        onValueChange = { viewModel.onSystolicChange(it) },
                        onIncrement = { viewModel.incrementSystolic() },
                        onDecrement = { viewModel.decrementSystolic() },
                        accentColor = liveCategory.color,
                        testTag = "systolic_input"
                    )

                    // Diastolic (Bottom Number) Input
                    BpInputField(
                        label = "Diastolic (Bottom Number)",
                        subLabel = "Pressure while heart is resting",
                        unit = "mmHg",
                        value = diastolic,
                        onValueChange = { viewModel.onDiastolicChange(it) },
                        onIncrement = { viewModel.incrementDiastolic() },
                        onDecrement = { viewModel.decrementDiastolic() },
                        accentColor = liveCategory.color,
                        testTag = "diastolic_input"
                    )

                    // Pulse (Heart Rate) Input
                    BpInputField(
                        label = "Pulse / Heart Rate",
                        subLabel = "Beats per minute (optional)",
                        unit = "bpm",
                        value = pulse,
                        onValueChange = { viewModel.onPulseChange(it) },
                        onIncrement = { viewModel.incrementPulse() },
                        onDecrement = { viewModel.decrementPulse() },
                        accentColor = Color(0xFFC62828),
                        testTag = "pulse_input"
                    )

                    // Presets: Arm selection
                    Column {
                        Text(
                            text = "Measured Arm:",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Left Arm", "Right Arm").forEach { arm ->
                                FilterChip(
                                    selected = selectedArm == arm,
                                    onClick = { viewModel.onArmSelect(arm) },
                                    label = { Text(arm, fontSize = 14.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryRose.copy(alpha = 0.15f),
                                        selectedLabelColor = PrimaryRose
                                    ),
                                    modifier = Modifier.testTag("chip_arm_${arm.lowercase().replace(" ", "_")}")
                                )
                            }
                        }
                    }

                    // Presets: Time of day / Context
                    Column {
                        Text(
                            text = "Time of Day / Context:",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val tags = listOf("Morning", "Afternoon", "Evening", "Night", "After Meds", "Before Meds", "After Walk")
                            items(tags) { tag ->
                                FilterChip(
                                    selected = selectedTag == tag,
                                    onClick = { viewModel.onTagSelect(tag) },
                                    label = { Text(tag, fontSize = 14.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryRose.copy(alpha = 0.15f),
                                        selectedLabelColor = PrimaryRose
                                    ),
                                    modifier = Modifier.testTag("chip_tag_${tag.lowercase().replace(" ", "_")}")
                                )
                            }
                        }
                    }

                    // Optional Notes
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { viewModel.onNotesChange(it) },
                        label = { Text("Note (e.g., Felt dizzy, Doctor checkup, Drank tea)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("notes_input"),
                        shape = RoundedCornerShape(14.dp),
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Big Senior-friendly Save Button
                    Button(
                        onClick = { viewModel.saveReading() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .testTag("save_bp_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryRose
                        ),
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Saving & Syncing...", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (isSheetsConfigured) "Save & Send to Google Sheet" else "Save Reading to Log",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            } else {
                // History & Trends View
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Summary card at top of history
                    item {
                        BpStatsSummary(readings = allReadings)
                    }

                    // Action buttons (Sync All Pending & Share/Export)
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (pendingSyncCount > 0) {
                                Button(
                                    onClick = { viewModel.syncAllPending() },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("sync_pending_history_button"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                                    enabled = !isSyncing
                                ) {
                                    Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Sync $pendingSyncCount Unsent", fontSize = 13.sp)
                                }
                            }

                            Button(
                                onClick = { viewModel.shareAsCsv(context) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("share_csv_button"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                )
                            ) {
                                Icon(imageVector = Icons.Default.IosShare, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share to Sheets (CSV)", fontSize = 13.sp)
                            }
                        }
                    }

                    if (allReadings.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(CircleShape)
                                            .background(PrimaryRose.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MedicalServices,
                                            contentDescription = null,
                                            tint = PrimaryRose,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = "No BP Readings Yet",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Tap 'New Reading' above to record Nani's first blood pressure reading.",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = { selectedTabIndex = 0 },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryRose)
                                    ) {
                                        Text("Record Blood Pressure")
                                    }
                                }
                            }
                        }
                    } else {
                        items(allReadings, key = { it.id }) { reading ->
                            BpReadingItem(
                                reading = reading,
                                onDelete = { viewModel.deleteReading(reading) },
                                onRetrySync = { viewModel.retrySyncReading(reading) },
                                isSyncing = isSyncing
                            )
                        }
                    }
                }
            }
        }
    }

    // Google Sheets configuration dialog
    if (showConfigDialog) {
        GoogleSheetsConfigDialog(
            currentUrl = sheetsUrl,
            autoSyncEnabled = autoSyncEnabled,
            pendingSyncCount = pendingSyncCount,
            onSaveConfig = { url, autoSync ->
                viewModel.updateSheetsConfig(url, autoSync)
            },
            onTestConnection = { url, onResult ->
                viewModel.testConnection(url, onResult)
            },
            onSyncAllPending = {
                viewModel.syncAllPending()
            },
            onExportCsv = {
                viewModel.shareAsCsv(context)
            },
            onDismiss = {
                showConfigDialog = false
            }
        )
    }
}
