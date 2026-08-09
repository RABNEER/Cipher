package com.apocalyptolabs.viking.ui.screens.permissions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
                title = { Text("Permission Privilege Audit", fontWeight = FontWeight.Bold, color = VikingWhite) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = VikingWhite)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.runAudit() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Re-audit", tint = VikingTeal)
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
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is AuditUiState.Auditing -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = VikingTeal, strokeWidth = 3.dp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Auditing Installed Application Privileges…",
                            style = MaterialTheme.typography.titleMedium,
                            color = VikingWhite
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Gemma 270M evaluating dangerous permission combinations",
                            style = MaterialTheme.typography.bodyMedium,
                            color = VikingGray
                        )
                    }
                }

                is AuditUiState.Success -> {
                    if (state.flaggedApps.isEmpty()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = "Safe",
                                tint = VikingSafe,
                                modifier = Modifier.size(72.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No Over-Privileged Apps Found",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = VikingWhite
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "All user applications are requesting appropriate permission scopes.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = VikingGray
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                Text(
                                    text = "FLAGGED SUSPICIOUS COMBINATIONS (${state.flaggedApps.size})",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = VikingTeal,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            items(state.flaggedApps) { threat ->
                                AuditResultCard(threat = threat)
                            }
                        }
                    }
                }

                is AuditUiState.Error -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            tint = VikingCritical,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Audit Failed",
                            style = MaterialTheme.typography.titleLarge,
                            color = VikingCritical
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = VikingGray
                        )
                    }
                }

                else -> {}
            }
        }
    }
}

@Composable
fun AuditResultCard(threat: ThreatResult) {
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
                Text(
                    text = threat.target,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = VikingWhite
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = VikingHigh.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = threat.severity.name,
                        color = VikingHigh,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = threat.explanation,
                style = MaterialTheme.typography.bodyLarge,
                color = VikingWhite,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Recommendation: ${threat.action}",
                style = MaterialTheme.typography.bodyMedium,
                color = VikingGray,
                fontSize = 12.sp
            )
        }
    }
}
