package com.apocalyptolabs.viking.ui.screens.sandbox

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                title = { Text("Jury Threat Sandbox", fontWeight = FontWeight.Bold, color = VikingWhite) },
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
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = VikingSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Science, contentDescription = null, tint = VikingTeal)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "LIVE THREAT SIMULATOR",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = VikingWhite
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap any button below to trigger real-time on-device AI feature extraction and threat evaluation.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = VikingGray
                        )
                    }
                }
            }

            item {
                Text(
                    text = "SIMULATION CONTROL PANEL",
                    style = MaterialTheme.typography.bodyMedium,
                    color = VikingTeal,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Button(
                    onClick = { viewModel.simulateSbiPhishingSms() },
                    colors = ButtonDefaults.buttonColors(containerColor = VikingNavy),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Icon(Icons.Default.Sms, contentDescription = null, tint = VikingHigh)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("1. Simulate SBI Phishing SMS", color = VikingWhite, fontWeight = FontWeight.Bold)
                }
            }

            item {
                Button(
                    onClick = { viewModel.simulateHomoglyphUpiLink() },
                    colors = ButtonDefaults.buttonColors(containerColor = VikingNavy),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Icon(Icons.Default.Link, contentDescription = null, tint = VikingHigh)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("2. Simulate Homoglyph UPI Link", color = VikingWhite, fontWeight = FontWeight.Bold)
                }
            }

            item {
                Button(
                    onClick = { viewModel.simulateSpoofedBankCall() },
                    colors = ButtonDefaults.buttonColors(containerColor = VikingNavy),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = VikingCritical)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("3. Simulate Spoofed Bank Call", color = VikingWhite, fontWeight = FontWeight.Bold)
                }
            }

            item {
                Button(
                    onClick = { viewModel.simulateNfcRelayAttack() },
                    colors = ButtonDefaults.buttonColors(containerColor = VikingNavy),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Icon(Icons.Default.Nfc, contentDescription = null, tint = VikingCritical)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("4. Simulate NFC Relay Attack", color = VikingWhite, fontWeight = FontWeight.Bold)
                }
            }

            item {
                Button(
                    onClick = { viewModel.simulateOverPrivilegedAppAudit() },
                    colors = ButtonDefaults.buttonColors(containerColor = VikingNavy),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = VikingTeal)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("5. Simulate Over-Privileged App Audit", color = VikingWhite, fontWeight = FontWeight.Bold)
                }
            }

            lastResult?.let { result ->
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "LAST SIMULATED VERDICT",
                        style = MaterialTheme.typography.bodyMedium,
                        color = VikingTeal,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val severityColor = when (result.severity) {
                        Severity.CRITICAL -> VikingCritical
                        Severity.HIGH -> VikingHigh
                        Severity.MEDIUM -> VikingMedium
                        Severity.LOW -> VikingTeal
                        Severity.SAFE -> VikingSafe
                    }

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = VikingSurface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = severityColor.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = result.severity.name,
                                        color = severityColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Text(
                                    text = result.type.name,
                                    color = VikingTeal,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = result.target,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = VikingWhite
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = result.explanation,
                                style = MaterialTheme.typography.bodyMedium,
                                color = VikingWhite
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Action: ${result.action}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = VikingGray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
