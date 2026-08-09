package com.apocalyptolabs.viking.ui.screens.scanner

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
                title = { Text("APK Security Scanner", fontWeight = FontWeight.Bold, color = VikingWhite) },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            when (val state = scanState) {
                is ScanState.Idle -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = "Pick APK",
                            tint = VikingTeal,
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Select APK File(s) to Scan",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = VikingWhite
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Viking will extract manifest permissions, signatures, and perform on-device Gemma 270M threat classification.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = VikingGray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = { filePickerLauncher.launch("application/vnd.android.package-archive") },
                            colors = ButtonDefaults.buttonColors(containerColor = VikingTeal),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, tint = VikingBlack)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("CHOOSE APK FILE(S)", color = VikingBlack, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                is ScanState.Scanning -> {
                    val infiniteTransition = rememberInfiniteTransition(label = "spin")
                    val rotation by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1500, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "rotation"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(120.dp)
                                    .rotate(rotation),
                                color = VikingTeal,
                                strokeWidth = 4.dp
                            )
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = VikingTeal,
                                modifier = Modifier.size(54.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                        Text(
                            text = "SCANNING APK",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = VikingTeal,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = state.stepMessage,
                            style = MaterialTheme.typography.bodyLarge,
                            color = VikingWhite,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                is ScanState.Success -> {
                    ResultCard(
                        result = state.result,
                        onScanAnother = { viewModel.resetScan() },
                        onExportResult = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "VIKING Security Scan Result: ${state.result.target}")
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "VIKING Security Verdict\nTarget: ${state.result.target}\nSeverity: ${state.result.severity}\nExplanation: ${state.result.explanation}\nAction: ${state.result.action}"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Security Verdict"))
                        }
                    )
                }

                is ScanState.Error -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Error",
                            tint = VikingCritical,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Scan Failed",
                            style = MaterialTheme.typography.titleLarge,
                            color = VikingCritical
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = VikingGray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.resetScan() },
                            colors = ButtonDefaults.buttonColors(containerColor = VikingTeal)
                        ) {
                            Text("Try Again", color = VikingBlack, fontWeight = FontWeight.Bold)
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
        Severity.LOW -> VikingTeal
        Severity.SAFE -> VikingSafe
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = VikingNavy),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = severityColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = result.severity.name,
                        color = severityColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                IconButton(onClick = onExportResult) {
                    Icon(Icons.Default.Share, contentDescription = "Share Result", tint = VikingTeal)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = result.target,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = VikingWhite
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Explanation",
                style = MaterialTheme.typography.titleMedium,
                color = VikingTeal,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = result.explanation,
                style = MaterialTheme.typography.bodyLarge,
                color = VikingWhite
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Recommended Action",
                style = MaterialTheme.typography.titleMedium,
                color = VikingTeal,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = result.action,
                style = MaterialTheme.typography.bodyLarge,
                color = VikingGray
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onScanAnother,
                colors = ButtonDefaults.buttonColors(containerColor = VikingTeal),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("SCAN ANOTHER FILE", color = VikingBlack, fontWeight = FontWeight.Bold)
            }
        }
    }
}
