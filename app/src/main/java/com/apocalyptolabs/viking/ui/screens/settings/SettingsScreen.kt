package com.apocalyptolabs.viking.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.apocalyptolabs.viking.core.ai.DownloadState
import com.apocalyptolabs.viking.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val moduleStatus by viewModel.moduleStatus.collectAsState()
    val downloadState by viewModel.downloadState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings & AI Model", fontWeight = FontWeight.Bold, color = VikingWhite) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = VikingWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VikingNavy)
            )
        },
        containerColor = VikingBlack
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Gemma AI Model Installation Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = VikingNavy),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Memory, contentDescription = null, tint = VikingTeal)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "GEMMA 270M AI MODEL",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = VikingWhite
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        when (val state = downloadState) {
                            is DownloadState.NotInstalled -> {
                                Text(
                                    text = "Status: Not Downloaded (Running on Static Rule Engine)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = VikingGray
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { viewModel.startModelDownload() },
                                    colors = ButtonDefaults.buttonColors(containerColor = VikingTeal),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, tint = VikingBlack)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Download Gemma AI Model (On-Device)", color = VikingBlack, fontWeight = FontWeight.Bold)
                                }
                            }
                            is DownloadState.Downloading -> {
                                Text(
                                    text = "Downloading Gemma 270M AI Model: ${state.progressPercent}%",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = VikingTeal,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { state.progressPercent / 100f },
                                    modifier = Modifier.fillMaxWidth().height(8.dp),
                                    color = VikingTeal,
                                    trackColor = VikingSurface
                                )
                            }
                            is DownloadState.Installed -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Status: Installed & Ready ✓",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = VikingSafe,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "MediaPipe LLM Inference Active",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = VikingGray,
                                            fontSize = 11.sp
                                        )
                                    }
                                    OutlinedButton(
                                        onClick = { viewModel.deleteModel() },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Remove", color = VikingCritical)
                                    }
                                }
                            }
                            is DownloadState.Error -> {
                                Text(
                                    text = "Error: ${state.message}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = VikingCritical
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { viewModel.startModelDownload() },
                                    colors = ButtonDefaults.buttonColors(containerColor = VikingTeal)
                                ) {
                                    Text("Retry Download", color = VikingBlack)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "SECURITY SHIELD TOGGLES",
                    style = MaterialTheme.typography.bodyMedium,
                    color = VikingTeal,
                    fontWeight = FontWeight.Bold
                )
            }

            val moduleKeys = listOf(
                "APK" to "APK Scanner Guard",
                "UPI" to "UPI Phishing Protection",
                "SMS" to "SMS Scam Detector",
                "CALL" to "Call Fingerprint Guard",
                "NFC" to "NFC Tag Sanitizer",
                "PERM" to "Permission Privilege Auditor"
            )

            items(moduleKeys.size) { index ->
                val (key, title) = moduleKeys[index]
                val isChecked = moduleStatus[key] ?: true

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = VikingSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = VikingWhite
                        )
                        Switch(
                            checked = isChecked,
                            onCheckedChange = { viewModel.toggleModule(key, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = VikingBlack,
                                checkedTrackColor = VikingTeal,
                                uncheckedThumbColor = VikingGray,
                                uncheckedTrackColor = VikingNavy
                            )
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        viewModel.exportDiagnosticReport { chooserIntent ->
                            context.startActivity(chooserIntent)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VikingNavy),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Icon(Icons.Default.BugReport, contentDescription = null, tint = VikingTeal)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export System Diagnostic ZIP", color = VikingWhite, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
