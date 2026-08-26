package com.apocalyptolabs.viking.ui.screens.dashboard

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
    val accent: Color
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
            ModuleStatusItem("APK Scanner", ThreatType.APK, Icons.Default.Android, "Install Guard", "apk_scanner", VikingPurple),
            ModuleStatusItem("UPI Link Guard", ThreatType.UPI, Icons.Default.Link, "Phishing Shield", "inspector", VikingBlue),
            ModuleStatusItem("SMS Shield", ThreatType.SMS, Icons.Default.Sms, "Scam Detector", "inspector", VikingGold),
            ModuleStatusItem("Call Fingerprint", ThreatType.CALL, Icons.Default.Phone, "Caller Guard", "inspector", VikingRose),
            ModuleStatusItem("NFC Monitor", ThreatType.NFC, Icons.Default.Nfc, "Relay Defense", "inspector", VikingTeal),
            ModuleStatusItem("Permission Audit", ThreatType.PERMISSION, Icons.Default.Security, "Privilege Scan", "permission_audit", VikingSafe)
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
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "VIKING",
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 4.sp,
                            color = VikingWhite
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSandbox) {
                        Icon(Icons.Default.Science, contentDescription = "Security Attack Sandbox", tint = VikingPurple)
                    }
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
                            CircularProgressIndicator(color = VikingTeal, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF Certificate", tint = VikingWhite)
                        }
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToApkScanner,
                icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = VikingBlack) },
                text = { Text("Scan APK", fontWeight = FontWeight.Bold, color = VikingBlack) },
                containerColor = VikingTeal,
                contentColor = VikingBlack,
                shape = RoundedCornerShape(16.dp)
            )
        },
        containerColor = VikingBlack
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
                Spacer(modifier = Modifier.height(4.dp))
                StatusHeroCard(
                    activeThreats = animatedThreatCount,
                    totalScans = uiState.totalThreatCount,
                    isModelReady = uiState.isModelReady
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SECURITY MODULES",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = VikingGray
                    )
                    TextButton(onClick = onNavigateToPermissionAudit) {
                        Text("Full Audit →", color = VikingTeal, fontWeight = FontWeight.SemiBold)
                    }
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 96.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(modules) { module ->
                        var appeared by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            delay(modules.indexOf(module) * 60L)
                            appeared = true
                        }
                        AnimatedVisibility(
                            visible = appeared,
                            enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 3 }
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
    val statusColor = if (isAtRisk) VikingCritical else VikingSafe
    val statusText = if (isAtRisk) "$activeThreats ACTIVE THREAT${if (activeThreats == 1) "" else "S"}" else "SYSTEM PROTECTED"

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = VikingSurface),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.Transparent,
                    border = androidx.compose.foundation.BorderStroke(1.dp, VikingDarkGray)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(if (isModelReady) VikingWhite else VikingGray, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isModelReady) "GEMMA 270M · LIVE" else "ENGINE WARMING UP",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isModelReady) VikingWhite else VikingGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = statusText,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp,
                    color = statusColor
                )
                Text(
                    text = "$totalScans total events analyzed · on-device only",
                    style = MaterialTheme.typography.bodyMedium,
                    color = VikingGray
                )
            }

            ThreatRadarView(
                isThreatActive = isAtRisk,
                modifier = Modifier.size(104.dp)
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
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "cardScale"
    )

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = VikingSurface),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
    ) {
        Column {
            // Hairline identity bar — brightness step per module.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(module.accent.copy(alpha = 0.85f))
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color.Transparent, CircleShape)
                        .border(1.dp, module.accent.copy(alpha = 0.45f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = module.icon,
                        contentDescription = module.name,
                        tint = VikingWhite,
                        modifier = Modifier.size(21.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = module.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = VikingWhite
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = module.status,
                    style = MaterialTheme.typography.bodyMedium,
                    color = VikingGray,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .background(VikingWhite, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ACTIVE",
                        style = MaterialTheme.typography.labelMedium,
                        fontSize = 9.sp,
                        color = VikingGray
                    )
                }
            }
        }
    }
}
