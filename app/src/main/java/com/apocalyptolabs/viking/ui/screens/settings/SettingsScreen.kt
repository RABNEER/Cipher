package com.apocalyptolabs.viking.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    val downloadState by viewModel.downloadState.collectAsState()
    val moduleStatus by viewModel.moduleStatus.collectAsState()

    val isAutoScanApk = moduleStatus["APK"] ?: true
    val isClipboardGuard = moduleStatus["UPI"] ?: true
    val isNfcGuard = moduleStatus["NFC"] ?: true

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings & AI Engine", fontWeight = FontWeight.Bold, color = VikingWhite) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = VikingWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VikingNavy)
            )
        },
        containerColor = VikingBlack
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VikingSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ON-DEVICE AI ENGINE",
                        style = MaterialTheme.typography.labelSmall,
                        color = VikingTeal,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Gemma 270M INT4 (CPU Local)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = VikingWhite
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Zero Network Calls • 100% On-Device Privacy Guaranteed under India DPDP Act 2023.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = VikingGray
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    when (val state = downloadState) {
                        is DownloadState.Installed -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = VikingSafe)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Model installed & active in memory", color = VikingSafe, fontWeight = FontWeight.Medium)
                            }
                        }
                        is DownloadState.Downloading -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(color = VikingTeal, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Downloading model weights: ${state.progressPercent}%", color = VikingTeal)
                            }
                        }
                        is DownloadState.NotInstalled -> {
                            Button(
                                onClick = { viewModel.startModelDownload() },
                                colors = ButtonDefaults.buttonColors(containerColor = VikingTeal),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, tint = VikingBlack)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Download Gemma 270M (~180MB)", color = VikingBlack, fontWeight = FontWeight.Bold)
                            }
                        }
                        is DownloadState.Error -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = VikingCritical)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Engine Error: ${state.message}", color = VikingCritical)
                            }
                        }
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VikingSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "REAL-TIME BACKGROUND SHIELDS",
                        style = MaterialTheme.typography.labelSmall,
                        color = VikingTeal,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Automatic APK Sideload Scan", color = VikingWhite, fontWeight = FontWeight.SemiBold)
                            Text("Watches Downloads directory for untrusted .apk files", color = VikingGray, fontSize = 12.sp)
                        }
                        Switch(
                            checked = isAutoScanApk,
                            onCheckedChange = { viewModel.toggleModule("APK", it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = VikingTeal, checkedTrackColor = VikingNavy)
                        )
                    }

                    Divider(color = VikingBlack, modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Clipboard Phishing Shield", color = VikingWhite, fontWeight = FontWeight.SemiBold)
                            Text("Inspects copied URLs & UPI collect links on-device", color = VikingGray, fontSize = 12.sp)
                        }
                        Switch(
                            checked = isClipboardGuard,
                            onCheckedChange = { viewModel.toggleModule("UPI", it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = VikingTeal, checkedTrackColor = VikingNavy)
                        )
                    }

                    Divider(color = VikingBlack, modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("NFC Payment & Tag Guard", color = VikingWhite, fontWeight = FontWeight.SemiBold)
                            Text("Blocks malicious tag triggers and relay attacks", color = VikingGray, fontSize = 12.sp)
                        }
                        Switch(
                            checked = isNfcGuard,
                            onCheckedChange = { viewModel.toggleModule("NFC", it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = VikingTeal, checkedTrackColor = VikingNavy)
                        )
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VikingSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "AUDIT & EXPORT",
                        style = MaterialTheme.typography.labelSmall,
                        color = VikingTeal,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            viewModel.generateCertificatePdf { pdfFile ->
                                if (pdfFile != null) {
                                    Toast.makeText(context, "Certificate PDF generated: ${pdfFile.name}", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "Failed to generate PDF report", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VikingNavy),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = VikingTeal)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export Security Certificate (PDF)", color = VikingWhite, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            viewModel.exportDiagnosticReport { chooserIntent ->
                                context.startActivity(chooserIntent)
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = VikingTeal),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.BugReport, contentDescription = null, tint = VikingTeal)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export Diagnostic Bug Report (ZIP)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
