package com.apocalyptolabs.viking.ui.screens.permissions

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.apocalyptolabs.viking.core.model.Severity
import com.apocalyptolabs.viking.core.model.ThreatResult
import com.apocalyptolabs.viking.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionAuditScreen(
    onNavigateBack: () -> Unit,
    viewModel: PermissionAuditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        if (uiState is AuditUiState.Idle) {
            viewModel.runAudit()
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
                                    imageVector = Icons.Default.Security,
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
                                    " // PRIVILEGES",
                                    style = VikingMonoTelemetry,
                                    color = VikingMuted
                                )
                            }
                            Text(
                                "PRIVILEGE ESCALATION AUDIT",
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
                actions = {
                    IconButton(onClick = { viewModel.runAudit() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Re-audit", tint = VikingWhite)
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
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is AuditUiState.Auditing -> {
                    val infiniteTransition = rememberInfiniteTransition(label = "spin")
                    val rotation by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "spin"
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
                                        .size(72.dp)
                                        .rotate(rotation),
                                    color = VikingWhite,
                                    trackColor = VikingBorder,
                                    strokeWidth = 3.dp
                                )
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = VikingWhite,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "AUDITING APPLICATION SCOPES",
                                style = VikingMonoData,
                                fontWeight = FontWeight.Bold,
                                color = VikingWhite,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Evaluating accessibility service abuse & covert background telemetry combinations",
                                style = VikingMonoTelemetry,
                                color = VikingGray,
                                lineHeight = 15.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                is AuditUiState.Success -> {
                    if (state.flaggedApps.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = VikingSurface,
                            border = BorderStroke(1.dp, VikingBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(28.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = VikingNavy,
                                    border = BorderStroke(1.dp, VikingBorder),
                                    modifier = Modifier.size(56.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.VerifiedUser,
                                            contentDescription = "Safe",
                                            tint = VikingSafe,
                                            modifier = Modifier.size(30.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "ZERO OVER-PRIVILEGED APPS",
                                    style = VikingMonoData,
                                    fontWeight = FontWeight.Bold,
                                    color = VikingWhite
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "All installed packages comply with zero-trust permission isolation boundaries.",
                                    style = VikingMonoTelemetry,
                                    color = VikingGray,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "FLAGGED PRIVILEGE ANOMALIES (${state.flaggedApps.size})",
                                        style = VikingLabelCaps,
                                        color = VikingMuted
                                    )
                                    Text(
                                        text = "GEMMA 270M AUDIT",
                                        style = VikingLabelCaps,
                                        color = VikingWhite
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }

                            items(state.flaggedApps) { threat ->
                                AuditResultCard(threat = threat)
                            }
                        }
                    }
                }

                is AuditUiState.Error -> {
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
                                imageVector = Icons.Default.Error,
                                contentDescription = "Error",
                                tint = VikingCritical,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "PRIVILEGE AUDIT FAILED",
                                style = VikingMonoData,
                                fontWeight = FontWeight.Bold,
                                color = VikingCritical
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = state.message,
                                style = VikingMonoTelemetry,
                                color = VikingGray
                            )
                        }
                    }
                }

                else -> {}
            }
        }
    }
}

@Composable
fun AuditResultCard(threat: ThreatResult) {
    val severityColor = when (threat.severity) {
        Severity.CRITICAL -> VikingCritical
        Severity.HIGH -> VikingHigh
        Severity.MEDIUM -> VikingMedium
        Severity.LOW -> VikingWhite
        Severity.SAFE -> VikingSafe
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = VikingSurface,
        border = BorderStroke(1.dp, VikingBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = threat.target,
                    style = VikingMonoData,
                    fontWeight = FontWeight.Bold,
                    color = VikingWhite,
                    fontSize = 14.sp
                )

                Surface(
                    shape = RoundedCornerShape(3.dp),
                    color = severityColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, severityColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = threat.severity.name,
                        color = severityColor,
                        style = VikingLabelCaps,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = threat.explanation,
                style = VikingMonoTelemetry,
                color = VikingWhite,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(4.dp),
                color = VikingNavy,
                border = BorderStroke(1.dp, VikingBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "REMEDIATION: ${threat.action}",
                    style = VikingLabelCaps,
                    color = VikingGray,
                    fontSize = 9.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                )
            }
        }
    }
}
