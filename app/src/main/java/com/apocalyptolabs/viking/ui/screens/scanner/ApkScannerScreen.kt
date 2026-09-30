package com.apocalyptolabs.viking.ui.screens.scanner

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.apocalyptolabs.viking.core.model.Severity
import com.apocalyptolabs.viking.core.model.ThreatResult
import com.apocalyptolabs.viking.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApkScannerScreen(
    onNavigateBack: () -> Unit,
    viewModel: ScannerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scanState by viewModel.scanState.collectAsState()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            uris.forEach { viewModel.scanApk(it) }
        }
    }

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
                                    imageVector = Icons.Default.QrCodeScanner,
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
                                    " // SCANNER",
                                    style = VikingMonoTelemetry,
                                    color = VikingMuted
                                )
                            }
                            Text(
                                "BINARY INGRESS CHAMBER",
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            when (val state = scanState) {
                is ScanState.Idle -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Tactical Drop Target Box
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = VikingSurface,
                            border = BorderStroke(1.dp, VikingBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = VikingNavy,
                                    border = BorderStroke(1.dp, VikingBorder),
                                    modifier = Modifier.size(64.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.FolderOpen,
                                            contentDescription = "Pick APK",
                                            tint = VikingWhite,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "SELECT TARGET APK BINARY",
                                    style = VikingMonoData,
                                    fontWeight = FontWeight.Bold,
                                    color = VikingWhite,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Extract AndroidManifest permissions, DEX bytecode, cryptographic certificates, and evaluate threat vectors on-device.",
                                    style = VikingMonoTelemetry,
                                    color = VikingGray,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.height(20.dp))

                                // Spec matrix chips
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(3.dp),
                                        color = VikingNavy,
                                        border = BorderStroke(1.dp, VikingBorder)
                                    ) {
                                        Text(
                                            text = "FORMAT: .APK",
                                            style = VikingLabelCaps,
                                            color = VikingMuted,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(3.dp),
                                        color = VikingNavy,
                                        border = BorderStroke(1.dp, VikingBorder)
                                    ) {
                                        Text(
                                            text = "INT4 NEURAL SCAN",
                                            style = VikingLabelCaps,
                                            color = VikingSafe,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { filePickerLauncher.launch("application/vnd.android.package-archive") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VikingWhite,
                                contentColor = VikingBlack
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, tint = VikingBlack, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "INGEST APK TARGET FILE",
                                style = VikingMonoData,
                                fontWeight = FontWeight.Bold,
                                color = VikingBlack
                            )
                        }
                    }
                }

                is ScanState.Scanning -> {
                    val infiniteTransition = rememberInfiniteTransition(label = "spin")
                    val rotation by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1400, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "rotation"
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = VikingSurface,
                        border = BorderStroke(1.dp, VikingBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .rotate(rotation),
                                    color = VikingWhite,
                                    trackColor = VikingBorder,
                                    strokeWidth = 3.dp
                                )
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = VikingWhite,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "ANALYZING BINARY INGRESS",
                                style = VikingMonoData,
                                fontWeight = FontWeight.Bold,
                                color = VikingWhite,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = state.stepMessage,
                                style = VikingMonoTelemetry,
                                color = VikingSafe,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Zero-egress isolation active · sandbox memory bound",
                                style = VikingLabelCaps,
                                color = VikingMuted
                            )
                        }
                    }
                }

                is ScanState.Success -> {
                    ResultCard(
                        result = state.result,
                        onScanAnother = { viewModel.resetScan() },
                        onExportResult = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "CIPHER Security Scan Result: ${state.result.target}")
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "CIPHER Security Verdict\nTarget: ${state.result.target}\nSeverity: ${state.result.severity}\nExplanation: ${state.result.explanation}\nAction: ${state.result.action}"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Security Verdict"))
                        }
                    )
                }

                is ScanState.Error -> {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = VikingSurface,
                        border = BorderStroke(1.dp, VikingCritical.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = "Error",
                                tint = VikingCritical,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "SCAN INGESTION FAILED",
                                style = VikingMonoData,
                                fontWeight = FontWeight.Bold,
                                color = VikingCritical
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = state.message,
                                style = VikingMonoTelemetry,
                                color = VikingGray,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = { viewModel.resetScan() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = VikingWhite,
                                    contentColor = VikingBlack
                                ),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("RETRY SCAN", style = VikingMonoData, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResultCard(
    result: ThreatResult,
    onScanAnother: () -> Unit,
    onExportResult: () -> Unit
) {
    val severityColor = when (result.severity) {
        Severity.CRITICAL -> VikingCritical
        Severity.HIGH -> VikingHigh
        Severity.MEDIUM -> VikingMedium
        Severity.LOW -> VikingWhite
        Severity.SAFE -> VikingSafe
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = VikingSurface,
        border = BorderStroke(1.dp, severityColor.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(3.dp),
                    color = severityColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, severityColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "VERDICT // ${result.severity.name}",
                        color = severityColor,
                        style = VikingLabelCaps,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                IconButton(onClick = onExportResult) {
                    Icon(Icons.Default.Share, contentDescription = "Share Result", tint = VikingWhite)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = result.target,
                style = VikingMonoData,
                fontWeight = FontWeight.Bold,
                color = VikingWhite,
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "DETECTION EXPLANATION",
                style = VikingLabelCaps,
                color = VikingMuted
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = result.explanation,
                style = VikingMonoTelemetry,
                color = VikingWhite,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "RECOMMENDED ACTION",
                style = VikingLabelCaps,
                color = VikingMuted
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = result.action,
                style = VikingMonoTelemetry,
                color = VikingGray,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onScanAnother,
                colors = ButtonDefaults.buttonColors(
                    containerColor = VikingWhite,
                    contentColor = VikingBlack
                ),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Text(
                    "SCAN ANOTHER TARGET",
                    style = VikingMonoData,
                    fontWeight = FontWeight.Bold,
                    color = VikingBlack
                )
            }
        }
    }
}
