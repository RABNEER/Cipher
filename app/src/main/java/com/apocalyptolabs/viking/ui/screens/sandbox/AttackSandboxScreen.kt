package com.apocalyptolabs.viking.ui.screens.sandbox

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.apocalyptolabs.viking.core.model.Severity
import com.apocalyptolabs.viking.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttackSandboxScreen(
    onNavigateBack: () -> Unit,
    viewModel: AttackSandboxViewModel = hiltViewModel()
) {
    val lastResult by viewModel.lastSimulatedResult.collectAsState()

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
                                    imageVector = Icons.Default.Science,
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
                                    " // SANDBOX",
                                    style = VikingMonoTelemetry,
                                    color = VikingMuted
                                )
                            }
                            Text(
                                "ISOLATED THREAT SIMULATOR",
                                style = VikingLabelCaps,
                                color = VikingSafe,
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
            // Header Info Card
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
                                text = "ISOLATED TESTBED // ZERO ESCAPE",
                                style = VikingLabelCaps,
                                color = VikingMuted
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(modifier = Modifier.size(5.dp).background(VikingSafe, CircleShape))
                                Text(
                                    text = "CONTAINED",
                                    style = VikingLabelCaps,
                                    color = VikingSafe,
                                    fontSize = 9.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Synthetic Attack Injector",
                            style = VikingMonoData,
                            fontWeight = FontWeight.Bold,
                            color = VikingWhite,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Trigger controlled adversarial threat payloads against the on-device Gemma 270M neural classifier without exposing device hardware.",
                            style = VikingMonoTelemetry,
                            color = VikingGray,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            item {
                Text(
                    text = "THREAT SIMULATION PAYLOADS",
                    style = VikingLabelCaps,
                    color = VikingMuted,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            item {
                SimulationButton(
                    code = "SIM-01-SMS",
                    title = "SBI Suspended KYC Phishing SMS",
                    subtitle = "Urgency framing + Malicious bit.ly credential harvester",
                    icon = Icons.Default.Sms,
                    accent = VikingGold,
                    onClick = { viewModel.simulateSbiPhishingSms() }
                )
            }

            item {
                SimulationButton(
                    code = "SIM-02-UPI",
                    title = "Homoglyph IDN Punycode Payment Link",
                    subtitle = "Cyrillic character substitution (xn--sbi) banking fraud",
                    icon = Icons.Default.Link,
                    accent = VikingBlue,
                    onClick = { viewModel.simulateHomoglyphUpiLink() }
                )
            }

            item {
                SimulationButton(
                    code = "SIM-03-CALL",
                    title = "Spoofed Bank Helpline + Digital Arrest",
                    subtitle = "Toll-free length mismatch + impersonation fingerprint",
                    icon = Icons.Default.Phone,
                    accent = VikingRose,
                    onClick = { viewModel.simulateSpoofedBankCall() }
                )
            }

            item {
                SimulationButton(
                    code = "SIM-04-NFC",
                    title = "NFC Relay Attack Simulation",
                    subtitle = "Round-trip APDU latency anomaly (>750ms relay threshold)",
                    icon = Icons.Default.Nfc,
                    accent = VikingCyan,
                    onClick = { viewModel.simulateNfcRelayAttack() }
                )
            }

            item {
                SimulationButton(
                    code = "SIM-05-PERM",
                    title = "Over-Privileged Malicious Background App",
                    subtitle = "Excessive dangerous scope: Accessibility + Network + SMS",
                    icon = Icons.Default.Security,
                    accent = VikingPurple,
                    onClick = { viewModel.simulateOverPrivilegedAppAudit() }
                )
            }

            lastResult?.let { result ->
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "CLASSIFIER INFERENCE TELEMETRY",
                        style = VikingLabelCaps,
                        color = VikingMuted
                    )
                    Spacer(modifier = Modifier.height(6.dp))

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
                        Column(modifier = Modifier.padding(16.dp)) {
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
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = "TARGET // ${result.type.name}",
                                    style = VikingLabelCaps,
                                    color = VikingMuted
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = result.target,
                                style = VikingMonoData,
                                fontWeight = FontWeight.Bold,
                                color = VikingWhite,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = result.explanation,
                                style = VikingMonoTelemetry,
                                color = VikingWhite,
                                lineHeight = 16.sp
                            )
                            if (result.action.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = VikingNavy,
                                    border = BorderStroke(1.dp, VikingBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "ACTION: ${result.action}",
                                        style = VikingLabelCaps,
                                        color = VikingGray,
                                        fontSize = 9.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SimulationButton(
    code: String,
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = VikingSurface,
        border = BorderStroke(1.dp, VikingBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = VikingNavy,
                border = BorderStroke(1.dp, VikingBorder),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = code,
                        style = VikingLabelCaps,
                        color = VikingMuted
                    )
                }
                Text(
                    text = title,
                    style = VikingMonoData,
                    fontWeight = FontWeight.Bold,
                    color = VikingWhite,
                    fontSize = 13.sp
                )
                Text(
                    text = subtitle,
                    style = VikingMonoTelemetry,
                    color = VikingGray,
                    fontSize = 10.sp,
                    lineHeight = 13.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(3.dp),
                color = VikingNavy,
                border = BorderStroke(1.dp, VikingBorder)
            ) {
                Text(
                    text = "RUN",
                    style = VikingLabelCaps,
                    color = VikingWhite,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
