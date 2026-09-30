package com.apocalyptolabs.viking.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = VikingNavy,
                            border = BorderStroke(1.dp, VikingBorder),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = VikingWhite,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "CIPHER",
                                    style = VikingMonoData,
                                    fontWeight = FontWeight.Bold,
                                    color = VikingWhite
                                )
                                Text(
                                    " // PROTOCOLS",
                                    style = VikingMonoTelemetry,
                                    color = VikingMuted
                                )
                            }
                            Text(
                                "ZERO-TRUST SYSTEM CONFIG",
                                style = VikingLabelCaps,
                                color = VikingMuted,
                                fontSize = 8.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = VikingWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VikingBlack)
            )
        },
        containerColor = VikingPitchBlack
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // AI Engine Card
            item {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = VikingSurface,
                    border = BorderStroke(1.dp, VikingBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "ON-DEVICE NEURAL CORE",
                                style = VikingLabelCaps,
                                color = VikingMuted
                            )
                            Surface(
                                shape = RoundedCornerShape(3.dp),
                                color = VikingNavy,
                                border = BorderStroke(1.dp, VikingBorder)
                            ) {
                                Text(
                                    text = "DPDP ACT 2023",
                                    style = VikingLabelCaps,
                                    color = VikingSafe,
                                    fontSize = 8.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Gemma 270M INT4 Engine",
                            style = VikingMonoData,
                            fontWeight = FontWeight.Bold,
                            color = VikingWhite,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Zero network egress guaranteed. Full weights executing purely in sandboxed application memory.",
                            style = VikingMonoTelemetry,
                            color = VikingGray,
                            lineHeight = 15.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        when (val state = downloadState) {
                            is DownloadState.Installed -> {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = VikingNavy,
                                    border = BorderStroke(1.dp, VikingBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.padding(10.dp)
                                    ) {
                                        Box(modifier = Modifier.size(6.dp).background(VikingSafe, CircleShape))
                                        Text(
                                            "WEIGHTS LOADED // RESIDENT IN MEMORY",
                                            style = VikingLabelCaps,
                                            color = VikingSafe,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            is DownloadState.Downloading -> {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    LinearProgressIndicator(
                                        progress = { state.progressPercent / 100f },
                                        modifier = Modifier.fillMaxWidth().height(4.dp),
                                        color = VikingWhite,
                                        trackColor = VikingBorder
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        "DOWNLOADING WEIGHTS: ${state.progressPercent}%",
                                        style = VikingLabelCaps,
                                        color = VikingWhite
                                    )
                                }
                            }
                            is DownloadState.NotInstalled -> {
                                Button(
                                    onClick = { viewModel.startModelDownload() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = VikingWhite,
                                        contentColor = VikingBlack
                                    ),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth().height(42.dp)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, tint = VikingBlack, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "DOWNLOAD GEMMA 270M WEIGHTS (~180MB)",
                                        style = VikingMonoData,
                                        color = VikingBlack,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            is DownloadState.Error -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = VikingCritical, modifier = Modifier.size(16.dp))
                                    Text("Engine Error: ${state.message}", style = VikingMonoTelemetry, color = VikingCritical)
                                }
                            }
                        }
                    }
                }
            }

            // Real-Time Background Shields
            item {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = VikingSurface,
                    border = BorderStroke(1.dp, VikingBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "REAL-TIME PROTECTION PROTOCOLS",
                            style = VikingLabelCaps,
                            color = VikingMuted
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Module 1: APK Sideload
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("APK Sideload Ingress Guard", style = VikingMonoData, fontWeight = FontWeight.SemiBold, color = VikingWhite)
                                Text("Monitors Downloads directory for unverified packages", style = VikingMonoTelemetry, color = VikingGray)
                            }
                            Switch(
                                checked = isAutoScanApk,
                                onCheckedChange = { viewModel.toggleModule("APK", it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = VikingBlack,
                                    checkedTrackColor = VikingWhite,
                                    uncheckedThumbColor = VikingGray,
                                    uncheckedTrackColor = VikingNavy
                                )
                            )
                        }

                        HorizontalDivider(color = VikingBorder, modifier = Modifier.padding(vertical = 12.dp))

                        // Module 2: Clipboard
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Clipboard Link & UPI Inspection", style = VikingMonoData, fontWeight = FontWeight.SemiBold, color = VikingWhite)
                                Text("Real-time homoglyph & malicious shortener inspection", style = VikingMonoTelemetry, color = VikingGray)
                            }
                            Switch(
                                checked = isClipboardGuard,
                                onCheckedChange = { viewModel.toggleModule("UPI", it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = VikingBlack,
                                    checkedTrackColor = VikingWhite,
                                    uncheckedThumbColor = VikingGray,
                                    uncheckedTrackColor = VikingNavy
                                )
                            )
                        }

                        HorizontalDivider(color = VikingBorder, modifier = Modifier.padding(vertical = 12.dp))

                        // Module 3: NFC
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("NFC Payment & Tag Guard", style = VikingMonoData, fontWeight = FontWeight.SemiBold, color = VikingWhite)
                                Text("APDU relay attack defense & tag sanitizer", style = VikingMonoTelemetry, color = VikingGray)
                            }
                            Switch(
                                checked = isNfcGuard,
                                onCheckedChange = { viewModel.toggleModule("NFC", it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = VikingBlack,
                                    checkedTrackColor = VikingWhite,
                                    uncheckedThumbColor = VikingGray,
                                    uncheckedTrackColor = VikingNavy
                                )
                            )
                        }
                    }
                }
            }

            // Compliance & Audit Exports
            item {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = VikingSurface,
                    border = BorderStroke(1.dp, VikingBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "COMPLIANCE & FORENSIC EXPORTS",
                            style = VikingLabelCaps,
                            color = VikingMuted
                        )
                        Spacer(modifier = Modifier.height(14.dp))

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
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VikingWhite,
                                contentColor = VikingBlack
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth().height(42.dp)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = VikingBlack, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "EXPORT SECURITY CERTIFICATE (PDF)",
                                style = VikingMonoData,
                                color = VikingBlack,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = {
                                viewModel.exportDiagnosticReport { chooserIntent ->
                                    context.startActivity(chooserIntent)
                                }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = VikingWhite),
                            border = BorderStroke(1.dp, VikingBorder),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth().height(42.dp)
                        ) {
                            Icon(Icons.Default.BugReport, contentDescription = null, tint = VikingWhite, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("EXPORT FORENSIC AUDIT PACKET (ZIP)", style = VikingMonoData, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
