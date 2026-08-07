package com.apocalyptolabs.viking.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apocalyptolabs.viking.core.model.Severity
import com.apocalyptolabs.viking.core.model.ThreatResult
import com.apocalyptolabs.viking.core.model.ThreatType
import com.apocalyptolabs.viking.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleInspectorBottomSheet(
    module: ModuleStatusItem,
    onDismiss: () -> Unit,
    onNavigateToFilteredLogs: (ThreatType) -> Unit,
    viewModel: DashboardViewModel
) {
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val testResult by viewModel.testResult.collectAsState()

    var inputField1 by remember(module) { mutableStateOf("") }
    var inputField2 by remember(module) { mutableStateOf("") }
    var durationSlider by remember(module) { mutableFloatStateOf(0f) }
    var latencySlider by remember(module) { mutableFloatStateOf(120f) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = {
            viewModel.clearTestResult()
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = VikingSurface,
        scrimColor = VikingBlack.copy(alpha = 0.7f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
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
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = module.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = VikingWhite
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(VikingSafe, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Active Guard • 100% On-Device",
                                style = MaterialTheme.typography.bodySmall,
                                color = VikingSafe,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                IconButton(onClick = {
                    viewModel.clearTestResult()
                    onDismiss()
                }) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = VikingGray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Capabilities Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = VikingNavy)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "REAL-TIME PROTECTION SCOPE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = VikingTeal,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = getModuleDescription(module.type),
                        style = MaterialTheme.typography.bodyMedium,
                        color = VikingWhite,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Live Interactive Tester Header
            Text(
                text = "INTERACTIVE LIVE TESTER",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = VikingTeal,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Input Fields based on Module Type
            when (module.type) {
                ThreatType.UPI -> {
                    OutlinedTextField(
                        value = inputField1,
                        onValueChange = { inputField1 = it },
                        label = { Text("Paste UPI / Web Link to Test") },
                        placeholder = { Text("e.g. upi://pay?pa=scam@ybl or bit.ly/bank-claim") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VikingTeal,
                            unfocusedBorderColor = VikingNavy,
                            focusedTextColor = VikingWhite,
                            unfocusedTextColor = VikingWhite
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Preset Test Scenarios:", fontSize = 11.sp, color = VikingGray)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf(
                            "Phishing" to "http://sbi-verify-kyc-update.com/login",
                            "Punycode" to "https://xn--sbi-9da.com/pay",
                            "Shortener" to "https://bit.ly/claim-bank-refund",
                            "Safe Bank" to "https://www.onlinesbi.sbi"
                        )) { (label, url) ->
                            SuggestionChip(
                                onClick = { inputField1 = url },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                ThreatType.SMS -> {
                    OutlinedTextField(
                        value = inputField1,
                        onValueChange = { inputField1 = it },
                        label = { Text("Sender ID / Number") },
                        placeholder = { Text("e.g. AD-HDFCBK or +919876543210") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VikingTeal,
                            unfocusedBorderColor = VikingNavy,
                            focusedTextColor = VikingWhite,
                            unfocusedTextColor = VikingWhite
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inputField2,
                        onValueChange = { inputField2 = it },
                        label = { Text("SMS Message Body") },
                        placeholder = { Text("Paste SMS content to analyze for fraud/urgency") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VikingTeal,
                            unfocusedBorderColor = VikingNavy,
                            focusedTextColor = VikingWhite,
                            unfocusedTextColor = VikingWhite
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Preset Test Scenarios:", fontSize = 11.sp, color = VikingGray)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf(
                            "Urgent Scam" to ("+919988776655" to "URGENT: Your SBI account suspended! Click http://bit.ly/sbi-kyc or share OTP 849201 immediately."),
                            "Lottery Fraud" to ("+919123456789" to "Congratulations! You won 25 Lakh in KBC lottery. Contact manager on WhatsApp now."),
                            "Bank OTP" to ("AX-HDFCBK" to "Your OTP for netbanking transaction of Rs 5000 is 492019. Do not share with anyone.")
                        )) { (label, data) ->
                            SuggestionChip(
                                onClick = {
                                    inputField1 = data.first
                                    inputField2 = data.second
                                },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                ThreatType.CALL -> {
                    OutlinedTextField(
                        value = inputField1,
                        onValueChange = { inputField1 = it },
                        label = { Text("Caller Phone Number") },
                        placeholder = { Text("e.g. 14098234 or +923001234567") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VikingTeal,
                            unfocusedBorderColor = VikingNavy,
                            focusedTextColor = VikingWhite,
                            unfocusedTextColor = VikingWhite
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Call Duration: ${durationSlider.toInt()} seconds",
                        fontSize = 12.sp,
                        color = VikingWhite
                    )
                    Slider(
                        value = durationSlider,
                        onValueChange = { durationSlider = it },
                        valueRange = 0f..180f,
                        colors = SliderDefaults.colors(
                            thumbColor = VikingTeal,
                            activeTrackColor = VikingTeal
                        )
                    )

                    Text("Preset Test Scenarios:", fontSize = 11.sp, color = VikingGray)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf(
                            "Telemarketer 140" to ("1409988776" to 75f),
                            "Intl Ping (+92)" to ("+923001234567" to 2f),
                            "Spoofed Helpline" to ("18001122" to 30f)
                        )) { (label, data) ->
                            SuggestionChip(
                                onClick = {
                                    inputField1 = data.first
                                    durationSlider = data.second
                                },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                ThreatType.NFC -> {
                    OutlinedTextField(
                        value = inputField1,
                        onValueChange = { inputField1 = it },
                        label = { Text("NDEF Tag Payload URL / Text") },
                        placeholder = { Text("e.g. https://malicious-nfc-tag.com/payload") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VikingTeal,
                            unfocusedBorderColor = VikingNavy,
                            focusedTextColor = VikingWhite,
                            unfocusedTextColor = VikingWhite
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Read Latency: ${latencySlider.toLong()} ms",
                        fontSize = 12.sp,
                        color = VikingWhite
                    )
                    Slider(
                        value = latencySlider,
                        onValueChange = { latencySlider = it },
                        valueRange = 50f..800f,
                        colors = SliderDefaults.colors(
                            thumbColor = VikingTeal,
                            activeTrackColor = VikingTeal
                        )
                    )

                    Text("Preset Test Scenarios:", fontSize = 11.sp, color = VikingGray)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf(
                            "Relay Attack" to ("https://payment-relay.com" to 650f),
                            "Unverified NDEF" to ("http://unverified-tag.io/data" to 120f),
                            "Standard Payment" to ("" to 90f)
                        )) { (label, data) ->
                            SuggestionChip(
                                onClick = {
                                    inputField1 = data.first
                                    latencySlider = data.second
                                },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                else -> {}
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Run Analysis Button
            Button(
                onClick = {
                    when (module.type) {
                        ThreatType.UPI -> viewModel.testUpiLink(inputField1.ifBlank { "https://bit.ly/claim-bank-refund" })
                        ThreatType.SMS -> viewModel.testSms(
                            inputField1.ifBlank { "+919988776655" },
                            inputField2.ifBlank { "URGENT: Your SBI account suspended! Update KYC at http://bit.ly/sbi-kyc now." }
                        )
                        ThreatType.CALL -> viewModel.testCall(
                            inputField1.ifBlank { "1409988776" },
                            durationSlider.toInt()
                        )
                        ThreatType.NFC -> viewModel.testNfc(
                            inputField1.ifBlank { "https://payment-relay-attack.com" },
                            latencySlider.toLong()
                        )
                        else -> {}
                    }
                },
                enabled = !isAnalyzing,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = VikingTeal),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isAnalyzing) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = VikingBlack, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Analyzing with Viking AI...", color = VikingBlack, fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.Psychology, contentDescription = null, tint = VikingBlack)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Analyze with Viking AI", color = VikingBlack, fontWeight = FontWeight.Bold)
                }
            }

            // Live Result Animation Card
            AnimatedVisibility(visible = testResult != null) {
                testResult?.let { result ->
                    Spacer(modifier = Modifier.height(14.dp))
                    TestResultCard(result = result)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = VikingNavy)
            Spacer(modifier = Modifier.height(12.dp))

            // View Module Logs Button
            OutlinedButton(
                onClick = {
                    viewModel.clearTestResult()
                    onDismiss()
                    onNavigateToFilteredLogs(module.type)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = VikingTeal)
            ) {
                Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("View ${module.name} Detection Log History →", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun TestResultCard(result: ThreatResult) {
    val severityColor = when (result.severity) {
        Severity.CRITICAL -> VikingCritical
        Severity.HIGH -> VikingHigh
        Severity.MEDIUM -> VikingMedium
        Severity.LOW -> VikingTeal
        Severity.SAFE -> VikingSafe
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = VikingBlack),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                        text = "VERDICT: ${result.severity.name}",
                        color = severityColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text = "Gemma 270M Engine",
                    fontSize = 11.sp,
                    color = VikingGray
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = result.explanation,
                style = MaterialTheme.typography.bodyMedium,
                color = VikingWhite,
                fontSize = 13.sp
            )
            if (result.action.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Recommended Action: ${result.action}",
                    style = MaterialTheme.typography.bodySmall,
                    color = VikingGray,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

private fun getModuleDescription(type: ThreatType): String {
    return when (type) {
        ThreatType.UPI -> "Detects phishing links, IDN Punycode domain spoofing (xn--), homoglyph domain attacks, and malicious payment link shorteners in real time."
        ThreatType.SMS -> "Analyzes incoming SMS messages for OTP fraud, urgency framing, banking scams, and lottery fraud across 8 regional Indian languages."
        ThreatType.CALL -> "Fingerprints incoming callers for telemarketer 140xxx prefixes, spoofed 1800 bank helplines, international ping calls (+92), and call duration risk."
        ThreatType.NFC -> "Monitors NFC tag reads for relay attacks (>500ms latency), unverified external NDEF URLs, and payment foreground app protection."
        ThreatType.APK -> "Scans sideloaded APK files for dangerous permission scope, version downgrade exploits, and signature anomalies."
        ThreatType.PERMISSION -> "Audits installed applications for over-privileged background permission combinations (Accessibility + Network)."
    }
}
