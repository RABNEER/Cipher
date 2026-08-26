package com.apocalyptolabs.viking.ui.screens.threatlog

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.apocalyptolabs.viking.core.model.Severity
import com.apocalyptolabs.viking.core.model.ThreatResult
import com.apocalyptolabs.viking.core.util.CybercrimeReportExporter
import com.apocalyptolabs.viking.core.util.toFormattedDate
import com.apocalyptolabs.viking.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreatLogScreen(
    onNavigateBack: () -> Unit,
    viewModel: ThreatLogViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val logs by viewModel.threatLogs.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val selectedTypeFilter by viewModel.selectedTypeFilter.collectAsState()
    var selectedSeverityFilter by remember { mutableStateOf<Severity?>(null) }

    val filteredLogs = remember(logs, selectedSeverityFilter, selectedTypeFilter) {
        logs.filter { log ->
            (selectedSeverityFilter == null || log.severity == selectedSeverityFilter) &&
            (selectedTypeFilter == null || log.type == selectedTypeFilter)
        }
    }

    val csvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        uri?.let { destUri ->
            viewModel.exportCsv(destUri, logs) { success ->
                scope.launch {
                    snackbarHostState.showSnackbar(
                        if (success) "Threat log exported to CSV" else "Failed to export threat log"
                    )
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (selectedTypeFilter != null) "${selectedTypeFilter!!.name} Logs" else "Detection Log History",
                        fontWeight = FontWeight.Bold,
                        color = VikingWhite
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = VikingWhite)
                    }
                },
                actions = {
                    if (logs.isNotEmpty()) {
                        IconButton(onClick = { csvLauncher.launch("viking_threat_logs_${System.currentTimeMillis()}.csv") }) {
                            Icon(Icons.Default.FileDownload, contentDescription = "Export CSV", tint = VikingTeal)
                        }
                        IconButton(onClick = { viewModel.clearHistory() }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Logs", tint = VikingCritical)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VikingNavy)
            )
        },
        containerColor = VikingBlack
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = VikingCritical.copy(alpha = 0.15f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Victim of Financial Fraud?",
                            style = MaterialTheme.typography.titleMedium,
                            color = VikingWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Act within the 1-hour Golden Period. Dial National Cybercrime Helpline 1930.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = VikingGray,
                            fontSize = 11.sp
                        )
                    }
                    Button(
                        onClick = { CybercrimeReportExporter.dial1930Helpline(context) },
                        colors = ButtonDefaults.buttonColors(containerColor = VikingCritical),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, tint = VikingWhite, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("1930", color = VikingWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedTypeFilter == null,
                        onClick = { viewModel.setTypeFilter(null) },
                        label = { Text("ALL MODULES") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VikingTeal,
                            selectedLabelColor = VikingBlack
                        )
                    )
                }
                items(com.apocalyptolabs.viking.core.model.ThreatType.values()) { type ->
                    val count = logs.count { it.type == type }
                    FilterChip(
                        selected = selectedTypeFilter == type,
                        onClick = { viewModel.setTypeFilter(type) },
                        label = { Text("${type.name} ($count)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VikingTeal,
                            selectedLabelColor = VikingBlack
                        )
                    )
                }
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedSeverityFilter == null,
                        onClick = { selectedSeverityFilter = null },
                        label = { Text("ALL SEVERITIES") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VikingTeal,
                            selectedLabelColor = VikingBlack
                        )
                    )
                }
                items(Severity.values()) { severity ->
                    val count = logs.count { it.severity == severity }
                    FilterChip(
                        selected = selectedSeverityFilter == severity,
                        onClick = { selectedSeverityFilter = severity },
                        label = { Text("${severity.name} ($count)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VikingTeal,
                            selectedLabelColor = VikingBlack
                        )
                    )
                }
            }

            if (filteredLogs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Empty",
                            tint = VikingGray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Threat Logs Recorded",
                            style = MaterialTheme.typography.titleMedium,
                            color = VikingWhite
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Detections from SMS, Call, UPI, APK & NFC will appear here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = VikingGray
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredLogs, key = { it.id }) { result ->
                        ThreatLogCard(result = result)
                    }
                }
            }
        }
    }
}

@Composable
fun ThreatLogCard(result: ThreatResult) {
    val context = LocalContext.current
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = result.type.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = VikingTeal,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = result.timestamp.toFormattedDate(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = VikingGray,
                        fontSize = 11.sp
                    )
                    IconButton(
                        onClick = {
                            val report = CybercrimeReportExporter.formatIncidentReportText(result)
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Viking 1930 Cybercrime Evidence Packet")
                                putExtra(Intent.EXTRA_TEXT, report)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Cybercrime Evidence"))
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share Evidence", tint = VikingTeal, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = result.target,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = VikingWhite
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = result.explanation,
                style = MaterialTheme.typography.bodyLarge,
                color = VikingWhite,
                fontSize = 13.sp
            )

            if (result.action.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
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
