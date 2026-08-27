package com.apocalyptolabs.viking.ui.screens.dashboard

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.apocalyptolabs.viking.core.model.ThreatType
import com.apocalyptolabs.viking.core.util.PdfReportGenerator
import com.apocalyptolabs.viking.ui.components.ThreatRadarView
import com.apocalyptolabs.viking.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ModuleStatusItem(
    val name: String,
    val type: ThreatType,
    val icon: ImageVector,
    val status: String,
    val route: String,
    val accent: Color,
    val code: String = "SEC-MOD"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToApkScanner: () -> Unit,
    onNavigateToThreatLog: (String?) -> Unit,
    onNavigateToPermissionAudit: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSandbox: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    var selectedInspectorModule by remember { mutableStateOf<ModuleStatusItem?>(null) }
    var isExportingPdf by remember { mutableStateOf(false) }

    val animatedThreatCount by animateIntAsState(
        targetValue = uiState.activeThreatCount,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "threatCount"
    )

    val modules = remember {
        listOf(
            ModuleStatusItem("APK Scanner", ThreatType.APK, Icons.Default.Android, "Install Guard", "apk_scanner", VikingApk, "SEC-APK"),
            ModuleStatusItem("UPI Link Guard", ThreatType.UPI, Icons.Default.Link, "Phishing Shield", "inspector", VikingBlue, "SEC-UPI"),
            ModuleStatusItem("SMS Shield", ThreatType.SMS, Icons.Default.Sms, "Scam Detector", "inspector", VikingGold, "SEC-SMS"),
            ModuleStatusItem("Call Fingerprint", ThreatType.CALL, Icons.Default.Phone, "Caller Guard", "inspector", VikingRose, "SEC-CALL"),
            ModuleStatusItem("NFC Monitor", ThreatType.NFC, Icons.Default.Nfc, "Relay Defense", "inspector", VikingCyan, "SEC-NFC"),
            ModuleStatusItem("Permission Audit", ThreatType.PERMISSION, Icons.Default.Security, "Privilege Scan", "permission_audit", VikingPurple, "SEC-PERM")
        )
    }

    selectedInspectorModule?.let { module ->
        ModuleInspectorBottomSheet(
            module = module,
            onDismiss = { selectedInspectorModule = null },
            onNavigateToFilteredLogs = { type ->
                selectedInspectorModule = null
                onNavigateToThreatLog(type.name)
            },
            viewModel = viewModel
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = VikingNavy,
                            border = BorderStroke(1.dp, VikingBorder),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = "Cipher Shield",
                                    tint = VikingWhite,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                "CIPHER",
                                style = VikingMonoData,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                color = VikingWhite
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                modifier = Modifier.padding(top = 1.dp)
                            ) {
                                val isSafe = animatedThreatCount == 0
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(if (isSafe) VikingSafe else VikingCritical, CircleShape)
                                )
                                Text(
                                    text = if (isSafe) "SYS.SECURE" else "SYS.ALERT",
                                    style = VikingLabelCaps,
                                    color = if (isSafe) VikingSafe else VikingCritical
                                )
                            }
                        }
                    }
                },
                actions = {
                    // Indian Cybercrime Helpline quick dial button (1930)
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = VikingSurfaceLow,
                        border = BorderStroke(1.dp, VikingBorder),
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .clickable {
                                try {
                                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:1930"))
                                    context.startActivity(dialIntent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Helpline: 1930 (National Cybercrime)", Toast.LENGTH_LONG).show()
                                }
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Helpline",
                                tint = VikingRose,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                "1930",
                                style = VikingMonoTelemetry,
                                fontWeight = FontWeight.Bold,
                                color = VikingWhite
                            )
                        }
                    }

                    // Sandbox Lab
                    IconButton(onClick = onNavigateToSandbox) {
                        Icon(Icons.Default.Science, contentDescription = "Security Attack Sandbox", tint = VikingWhite)
                    }

                    // PDF Audit Export
                    IconButton(
                        enabled = !isExportingPdf,
                        onClick = {
                            isExportingPdf = true
                            scope.launch {
                                try {
                                    val pdf = withContext(Dispatchers.IO) {
                                        PdfReportGenerator.generateSecurityCertificatePdf(
                                            context, uiState.recentThreats, uiState.activeThreatCount
                                        )
                                    }
                                    val uri = FileProvider.getUriForFile(
                                        context, "${context.packageName}.fileprovider", pdf
                                    )
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/pdf"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(
                                        Intent.createChooser(shareIntent, "Share Security Certificate PDF")
                                    )
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Failed to generate certificate", Toast.LENGTH_SHORT).show()
                                } finally {
                                    isExportingPdf = false
                                }
                            }
                        }
                    ) {
                        if (isExportingPdf) {
                            CircularProgressIndicator(color = VikingWhite, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF Certificate", tint = VikingWhite)
                        }
                    }

                    // Settings
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = VikingWhite)
                    }

                    // Threat History / Badge
                    IconButton(onClick = { onNavigateToThreatLog(null) }) {
                        BadgedBox(
                            badge = {
                                if (animatedThreatCount > 0) {
                                    Badge(
                                        containerColor = VikingCritical,
                                        contentColor = VikingWhite
                                    ) {
                                        Text("$animatedThreatCount")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.History, contentDescription = "Threat Log", tint = VikingWhite)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VikingBlack)
            )
        },
        floatingActionButton = {
            Button(
                onClick = onNavigateToApkScanner,
                colors = ButtonDefaults.buttonColors(
                    containerColor = VikingWhite,
                    contentColor = VikingBlack
                ),
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, VikingBorderInteractive),
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .height(46.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = VikingBlack,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        "SCAN INGRESS APK",
                        style = VikingMonoData,
                        fontWeight = FontWeight.Bold,
                        color = VikingBlack
                    )
                }
            }
        },
        containerColor = VikingPitchBlack
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(6.dp))

                // Tactical Operations Console
                StatusHeroCard(
                    activeThreats = animatedThreatCount,
                    totalScans = uiState.totalThreatCount,
                    isModelReady = uiState.isModelReady
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Ambient Micro-Terminal Output (Stitch Sniffer)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = VikingNavy,
                    border = BorderStroke(1.dp, VikingBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                ">",
                                style = VikingMonoTelemetry,
                                color = VikingWhite,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "SNIFFER_THREAD: ZERO-EGRESS PROMISCUOUS ACTIVE",
                                style = VikingLabelCaps,
                                color = VikingMuted,
                                maxLines = 1
                            )
                        }
                        Text(
                            "■",
                            style = VikingMonoTelemetry,
                            color = VikingSafe,
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section Title Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SECURITY PROTOCOLS (6 ARMED)",
                        style = VikingLabelCaps,
                        color = VikingMuted
                    )
                    Text(
                        text = "PRIVILEGE AUDIT →",
                        style = VikingLabelCaps,
                        color = VikingWhite,
                        modifier = Modifier
                            .clickable(onClick = onNavigateToPermissionAudit)
                            .padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2x3 Machined Module Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 96.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(modules) { module ->
                        var appeared by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            delay(modules.indexOf(module) * 40L)
                            appeared = true
                        }
                        AnimatedVisibility(
                            visible = appeared,
                            enter = fadeIn(tween(250)) + slideInVertically(tween(250)) { it / 4 }
                        ) {
                            ModuleCard(
                                module = module,
                                onClick = {
                                    when (module.route) {
                                        "apk_scanner" -> onNavigateToApkScanner()
                                        "permission_audit" -> onNavigateToPermissionAudit()
                                        else -> selectedInspectorModule = module
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusHeroCard(
    activeThreats: Int,
    totalScans: Int,
    isModelReady: Boolean
) {
    val isAtRisk = activeThreats > 0
    val headlineText = if (isAtRisk) "$activeThreats THREAT${if (activeThreats == 1) "" else "S"} DETECTED" else "99.8% INTEGRITY"

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = VikingSurface,
        border = BorderStroke(1.dp, if (isAtRisk) VikingCritical.copy(alpha = 0.6f) else VikingBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Tactical Status Badge
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = VikingNavy,
                    border = BorderStroke(1.dp, VikingBorder)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(if (isAtRisk) VikingCritical else VikingSafe, CircleShape)
                        )
                        Text(
                            text = if (isAtRisk) "ISOLATION REQUIRED" else "UP-LINK STABLE",
                            style = VikingLabelCaps,
                            color = if (isAtRisk) VikingCritical else VikingSafe
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = headlineText,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp,
                    color = if (isAtRisk) VikingCritical else VikingWhite
                )

                Text(
                    text = "HARDWARE-BOUND PROTOCOL // ZERO-LEAK ACTIVE",
                    style = VikingLabelCaps,
                    color = VikingMuted,
                    fontSize = 9.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Telemetry Stats Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = VikingNavy,
                        border = BorderStroke(1.dp, VikingBorder)
                    ) {
                        Text(
                            text = if (isModelReady) "GEMMA 270M" else "MODEL LOADING",
                            style = VikingMonoTelemetry,
                            fontWeight = FontWeight.SemiBold,
                            color = VikingWhite,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = "$totalScans EVENTS LOGGED",
                        style = VikingLabelCaps,
                        color = VikingGray
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            ThreatRadarView(
                isThreatActive = isAtRisk,
                modifier = Modifier.size(94.dp)
            )
        }
    }
}

@Composable
fun ModuleCard(
    module: ModuleStatusItem,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "cardScale"
    )

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = VikingSurface,
        border = BorderStroke(1.dp, VikingBorder),
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
    ) {
        Column {
            // Machined top identity hairline
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(module.accent)
            )

            Column(modifier = Modifier.padding(14.dp)) {
                // Header with Module Code & Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = module.code,
                        style = VikingLabelCaps,
                        color = VikingMuted
                    )
                    Surface(
                        shape = RoundedCornerShape(3.dp),
                        color = VikingNavy,
                        border = BorderStroke(1.dp, VikingBorder)
                    ) {
                        Text(
                            text = "ARMED",
                            style = VikingLabelCaps,
                            color = VikingSafe,
                            fontSize = 8.sp,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Module Icon Box
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = VikingNavy,
                    border = BorderStroke(1.dp, VikingBorder),
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = module.icon,
                            contentDescription = module.name,
                            tint = module.accent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = module.name,
                    style = VikingMonoData,
                    fontWeight = FontWeight.Bold,
                    color = VikingWhite
                )

                Text(
                    text = module.status,
                    style = VikingMonoTelemetry,
                    color = VikingGray,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Footer with Active Ping
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .background(VikingSafe, CircleShape)
                    )
                    Text(
                        text = "ZERO-TRUST ACTIVE",
                        style = VikingLabelCaps,
                        fontSize = 8.sp,
                        color = VikingMuted
                    )
                }
            }
        }
    }
}
