package com.apocalyptolabs.viking.ui.screens.onboarding

import android.Manifest
import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.apocalyptolabs.viking.ui.theme.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val isModelReady by viewModel.isModelReady.collectAsState()

    val permissionsToRequest = remember {
        val list = mutableListOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_CALL_LOG
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            list.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        list
    }

    val permissionsState = rememberMultiplePermissionsState(permissions = permissionsToRequest)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VikingPitchBlack)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = VikingNavy,
                border = BorderStroke(1.dp, VikingBorder),
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Cipher Shield",
                        tint = VikingWhite,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "CIPHER",
                style = VikingMonoData,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = VikingWhite
            )

            Text(
                text = "ON-DEVICE ZERO-TRUST CYBERSECURITY AGENT",
                style = VikingLabelCaps,
                color = VikingMuted,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Engine status card
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = VikingSurface,
                border = BorderStroke(1.dp, VikingBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isModelReady) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = VikingNavy,
                            border = BorderStroke(1.dp, VikingBorder),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Ready",
                                    tint = VikingSafe,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Gemma 270M INT8 Engine Ready",
                                style = VikingMonoData,
                                fontWeight = FontWeight.Bold,
                                color = VikingWhite
                            )
                            Text(
                                text = "Zero network egress · resident in sandboxed RAM",
                                style = VikingMonoTelemetry,
                                color = VikingGray
                            )
                        }
                    } else {
                        CircularProgressIndicator(
                            color = VikingWhite,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Initializing Neural Core...",
                                style = VikingMonoData,
                                fontWeight = FontWeight.Bold,
                                color = VikingWhite
                            )
                            Text(
                                text = "Loading model tensors into memory",
                                style = VikingMonoTelemetry,
                                color = VikingGray
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Permission status card
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = VikingSurface,
                border = BorderStroke(1.dp, VikingBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "REQUIRED SECURITY PRIVILEGES",
                        style = VikingLabelCaps,
                        color = VikingMuted
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Local SMS & Call Log monitoring required for on-device heuristic fraud prevention. Data never leaves this device under DPDP Act 2023.",
                        style = VikingMonoTelemetry,
                        color = VikingGray,
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (!permissionsState.allPermissionsGranted) {
                        Button(
                            onClick = { permissionsState.launchMultiplePermissionRequest() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VikingWhite,
                                contentColor = VikingBlack
                            ),
                            modifier = Modifier.fillMaxWidth().height(40.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "GRANT SECURITY SCOPES",
                                style = VikingMonoData,
                                fontWeight = FontWeight.Bold,
                                color = VikingBlack
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(modifier = Modifier.size(6.dp).background(VikingSafe, CircleShape))
                            Text(
                                "ALL PRIVILEGES GRANTED // HARDWARE SECURE",
                                style = VikingLabelCaps,
                                color = VikingSafe,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = {
                viewModel.completeOnboarding()
                onOnboardingComplete()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = VikingWhite,
                contentColor = VikingBlack,
                disabledContainerColor = VikingNavy,
                disabledContentColor = VikingMuted
            ),
            enabled = permissionsState.allPermissionsGranted,
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .align(Alignment.BottomCenter)
        ) {
            Text(
                text = "ACTIVATE SYSTEM SHIELD",
                style = VikingMonoData,
                fontWeight = FontWeight.Bold,
                color = if (permissionsState.allPermissionsGranted) VikingBlack else VikingMuted
            )
        }
    }
}
