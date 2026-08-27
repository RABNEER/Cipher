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
        scrimColor = VikingPitchBlack.copy(alpha = 0.85f),
        dragHandle = {
            Surface(
                modifier = Modifier.padding(vertical = 10.dp),
                color = VikingBorder,
                shape = RoundedCornerShape(2.dp)
            ) {
                Box(modifier = Modifier.size(width = 36.dp, height = 3.dp))
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = VikingNavy,
                        border = androidx.compose.foundation.BorderStroke(1.dp, VikingBorder),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = module.icon,
                                contentDescription = module.name,
                                tint = module.accent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = module.name,
                                style = VikingMonoData,
                                fontWeight = FontWeight.Bold,
                                color = VikingWhite
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = module.code,
                                style = VikingLabelCaps,
                                color = VikingMuted
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier.padding(top = 1.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .background(VikingSafe, CircleShape)
                            )
                            Text(
                                text = "ZERO-TRUST ACTIVE // ON-DEVICE",
                                style = VikingLabelCaps,
                                color = VikingSafe,
                                fontSize = 9.sp
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

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = VikingNavy,
                border = androidx.compose.foundation.BorderStroke(1.dp, VikingBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "PROTECTION SCOPE // HARDWARE TELEMETRY",
                        style = VikingLabelCaps,
                        color = VikingMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = getModuleDescription(module.type),
                        style = VikingMonoTelemetry,
                        color = VikingWhite,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "INTERACTIVE EVALUATION HARNESS",
                style = VikingLabelCaps,
                color = VikingMuted
            )
            Spacer(modifier = Modifier.height(8.dp))

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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VikingWhite, contentColor = VikingBlack),
                shape = RoundedCornerShape(6.dp)
            ) {
                if (isAnalyzing) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = VikingBlack, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("EVALUATING INFERENCE...", color = VikingBlack, style = VikingMonoData, fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.Psychology, contentDescription = null, tint = VikingBlack, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("RUN INFERENCE EVALUATION", color = VikingBlack, style = VikingMonoData, fontWeight = FontWeight.Bold)
                }
            }

            AnimatedVisibility(visible = testResult != null) {
                testResult?.let { result ->
                    Spacer(modifier = Modifier.height(14.dp))
                    TestResultCard(result = result)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = VikingBorder)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    viewModel.clearTestResult()
                    onDismiss()
                    onNavigateToFilteredLogs(module.type)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp),
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, VikingBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = VikingWhite)
            ) {
                Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp), tint = VikingWhite)
                Spacer(modifier = Modifier.width(8.dp))
                Text("VIEW ${module.code} DETECTION LOGS →", style = VikingLabelCaps, color = VikingWhite)
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
        Severity.LOW -> VikingWhite
        Severity.SAFE -> VikingSafe
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = VikingNavy,
        border = androidx.compose.foundation.BorderStroke(1.dp, severityColor.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(3.dp),
                    color = severityColor.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, severityColor.copy(alpha = 0.4f))
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
                    text = "GEMMA 270M INT8",
                    style = VikingLabelCaps,
                    color = VikingMuted
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = result.explanation,
                style = VikingMonoData,
                color = VikingWhite,
                fontSize = 12.sp
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
