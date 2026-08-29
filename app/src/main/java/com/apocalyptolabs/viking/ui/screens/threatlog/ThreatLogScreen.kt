package com.apocalyptolabs.viking.ui.screens.threatlog

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
                                    imageVector = Icons.Default.Lock,
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
                                    " // VAULT",
                                    style = VikingMonoTelemetry,
                                    color = VikingMuted
                                )
                            }
                            Text(
                                "INCIDENT AUDIT LOGS",
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
                    if (logs.isNotEmpty()) {
                        IconButton(onClick = { csvLauncher.launch("cipher_threat_logs_${System.currentTimeMillis()}.csv") }) {
                            Icon(Icons.Default.FileDownload, contentDescription = "Export CSV", tint = VikingWhite)
                        }
                        IconButton(onClick = { viewModel.clearHistory() }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Logs", tint = VikingCritical)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VikingBlack)
            )
        },
        containerColor = VikingPitchBlack
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // National 1930 Cybercrime Immediate Action Banner
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = VikingSurface,
                border = BorderStroke(1.dp, VikingCritical.copy(alpha = 0.5f)),
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(modifier = Modifier.size(6.dp).background(VikingCritical, CircleShape))
                            Text(
                                text = "FINANCIAL FRAUD // 1-HOUR GOLDEN PERIOD",
                                style = VikingLabelCaps,
                                color = VikingCritical,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Call 1930 to freeze fraudulent bank transfers immediately.",
                            style = VikingMonoTelemetry,
                            color = VikingGray,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { CybercrimeReportExporter.dial1930Helpline(context) },
                        colors = ButtonDefaults.buttonColors(containerColor = VikingCritical, contentColor = VikingWhite),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, tint = VikingWhite, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("1930", style = VikingMonoData, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Type Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item {
                    val isSelected = selectedTypeFilter == null
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isSelected) VikingWhite else VikingNavy,
                        border = BorderStroke(1.dp, if (isSelected) VikingWhite else VikingBorder),
                        modifier = Modifier.clickable { viewModel.setTypeFilter(null) }
                    ) {
                        Text(
                            text = "ALL MODULES",
                            style = VikingLabelCaps,
                            color = if (isSelected) VikingBlack else VikingGray,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                }
                items(com.apocalyptolabs.viking.core.model.ThreatType.values()) { type ->
                    val count = logs.count { it.type == type }
                    val isSelected = selectedTypeFilter == type
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isSelected) VikingWhite else VikingNavy,
                        border = BorderStroke(1.dp, if (isSelected) VikingWhite else VikingBorder),
                        modifier = Modifier.clickable { viewModel.setTypeFilter(type) }
                    ) {
                        Text(
                            text = "${type.name} ($count)",
                            style = VikingLabelCaps,
                            color = if (isSelected) VikingBlack else VikingGray,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Severity Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item {
                    val isSelected = selectedSeverityFilter == null
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isSelected) VikingWhite else VikingNavy,
                        border = BorderStroke(1.dp, if (isSelected) VikingWhite else VikingBorder),
                        modifier = Modifier.clickable { selectedSeverityFilter = null }
                    ) {
                        Text(
                            text = "ALL VERDICTS",
                            style = VikingLabelCaps,
                            color = if (isSelected) VikingBlack else VikingGray,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                }
                items(Severity.values()) { severity ->
                    val count = logs.count { it.severity == severity }
                    val isSelected = selectedSeverityFilter == severity
                    val sevColor = when (severity) {
                        Severity.CRITICAL -> VikingCritical
                        Severity.HIGH -> VikingHigh
                        Severity.MEDIUM -> VikingMedium
                        Severity.LOW -> VikingWhite
                        Severity.SAFE -> VikingSafe
                    }
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isSelected) sevColor else VikingNavy,
                        border = BorderStroke(1.dp, if (isSelected) sevColor else VikingBorder),
                        modifier = Modifier.clickable { selectedSeverityFilter = severity }
                    ) {
                        Text(
                            text = "${severity.name} ($count)",
                            style = VikingLabelCaps,
                            color = if (isSelected) VikingBlack else sevColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (filteredLogs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = VikingNavy,
                            border = BorderStroke(1.dp, VikingBorder),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = "Empty",
                                    tint = VikingGray,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "NO DETECTION INCIDENTS",
                            style = VikingMonoData,
                            fontWeight = FontWeight.Bold,
                            color = VikingWhite
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Zero threat signatures captured by active inspection engines.",
                            style = VikingMonoTelemetry,
                            color = VikingMuted
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
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
        Severity.LOW -> VikingWhite
        Severity.SAFE -> VikingSafe
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = VikingSurface,
        border = BorderStroke(1.dp, VikingBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(3.dp),
                        color = severityColor.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, severityColor.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = result.severity.name,
                            color = severityColor,
                            style = VikingLabelCaps,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "NODE // ${result.type.name}",
                        style = VikingLabelCaps,
                        color = VikingMuted
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = result.timestamp.toFormattedDate(),
                        style = VikingMonoTelemetry,
                        color = VikingMuted,
                        fontSize = 10.sp
                    )
                    IconButton(
                        onClick = {
                            val report = CybercrimeReportExporter.formatIncidentReportText(result)
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Cipher 1930 Cybercrime Evidence Packet")
                                putExtra(Intent.EXTRA_TEXT, report)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Cybercrime Evidence"))
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Evidence",
                            tint = VikingWhite,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = result.target,
                style = VikingMonoData,
                fontWeight = FontWeight.Bold,
                color = VikingWhite,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = result.explanation,
                style = VikingMonoTelemetry,
                color = VikingWhite,
                lineHeight = 15.sp
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
                        text = "MITIGATION: ${result.action}",
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
