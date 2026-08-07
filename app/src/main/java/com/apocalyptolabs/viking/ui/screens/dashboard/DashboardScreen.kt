package com.apocalyptolabs.viking.ui.screens.dashboard

import android.content.Intent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.apocalyptolabs.viking.core.model.ThreatType
import com.apocalyptolabs.viking.core.util.PdfReportGenerator
import com.apocalyptolabs.viking.ui.components.ThreatRadarView
import com.apocalyptolabs.viking.ui.theme.*

data class ModuleStatusItem(
    val name: String,
    val type: ThreatType,
    val icon: ImageVector,
    val status: String,
    val route: String
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
    val uiState by viewModel.uiState.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }
    var selectedInspectorModule by remember { mutableStateOf<ModuleStatusItem?>(null) }

    val animatedThreatCount by animateIntAsState(
        targetValue = uiState.activeThreatCount,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "threatCount"
    )

    val modules = remember {
        listOf(
            ModuleStatusItem("APK Scanner", ThreatType.APK, Icons.Default.Android, "Active Guard", "apk_scanner"),
            ModuleStatusItem("UPI Link Guard", ThreatType.UPI, Icons.Default.Link, "Phishing Protection", "inspector"),
            ModuleStatusItem("SMS Shield", ThreatType.SMS, Icons.Default.Sms, "Scam Detector", "inspector"),
            ModuleStatusItem("Call Fingerprint", ThreatType.CALL, Icons.Default.Phone, "Scam Caller Guard", "inspector"),
            ModuleStatusItem("NFC Monitor", ThreatType.NFC, Icons.Default.Nfc, "Tag Sanitizer", "inspector"),
            ModuleStatusItem("Permission Audit", ThreatType.PERMISSION, Icons.Default.Security, "Privilege Inspector", "permission_audit")
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Viking",
                            tint = VikingTeal,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("VIKING SHIELD", fontWeight = FontWeight.Bold, letterSpacing = 2.sp, color = VikingWhite)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSandbox) {
                        Icon(Icons.Default.Science, contentDescription = "Jury Sandbox", tint = VikingTeal)
                    }
                    IconButton(onClick = {
                        val pdf = PdfReportGenerator.generateSecurityCertificatePdf(
                            context, uiState.recentThreats, uiState.activeThreatCount
                        )
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdf)
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/pdf"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Security Certificate PDF"))
                    }) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF Certificate", tint = VikingWhite)
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = VikingWhite)
                    }
                    IconButton(onClick = { onNavigateToThreatLog(null) }) {
                        BadgedBox(
                            badge = {
                                if (animatedThreatCount > 0) {
                                    Badge(containerColor = VikingCritical) {
                                        Text("$animatedThreatCount")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.History, contentDescription = "Threat Log", tint = VikingWhite)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VikingNavy)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToApkScanner,
                icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = VikingBlack) },
                text = { Text("Scan APK File", fontWeight = FontWeight.Bold, color = VikingBlack) },
                containerColor = VikingTeal,
                contentColor = VikingBlack,
                shape = RoundedCornerShape(16.dp)
            )
        },
        containerColor = VikingBlack
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                isRefreshing = false
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                val isAtRisk = animatedThreatCount > 0
                val statusColor = if (isAtRisk) VikingCritical else VikingSafe
                val statusText = if (isAtRisk) "AT RISK ($animatedThreatCount THREATS)" else "SYSTEM PROTECTED"

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = VikingNavy),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(statusColor.copy(alpha = 0.25f), Color.Transparent),
                                    radius = 400f
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "ON-DEVICE AI AGENT",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = VikingTeal,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = statusText,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (uiState.isModelReady) "Gemma 270M • Zero Network Calls" else "Initializing Engine…",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = VikingGray
                                )
                            }

                            ThreatRadarView(
                                isThreatActive = isAtRisk,
                                modifier = Modifier.size(100.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SECURITY MODULES",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = VikingWhite
                    )
                    TextButton(onClick = onNavigateToPermissionAudit) {
                        Text("Permission Audit →", color = VikingTeal)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(modules) { module ->
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

@Composable
fun ModuleCard(
    module: ModuleStatusItem,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = VikingSurface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(VikingNavy, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = module.icon,
                    contentDescription = module.name,
                    tint = VikingTeal,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = module.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = VikingWhite
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = module.status,
                style = MaterialTheme.typography.bodyMedium,
                color = VikingGray,
                fontSize = 12.sp
            )
        }
    }
}
