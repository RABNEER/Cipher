package com.apocalyptolabs.viking.ui.screens.onboarding

import android.Manifest
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
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
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(VikingNavy, VikingBlack)
                )
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = "Viking Shield",
                tint = VikingTeal,
                modifier = Modifier.size(96.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "VIKING",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = VikingWhite,
                letterSpacing = 4.sp
            )

            Text(
                text = "On-Device AI Cybersecurity Agent",
                style = MaterialTheme.typography.bodyLarge,
                color = VikingTeal,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // AI Status Card
            Card(
                colors = CardDefaults.cardColors(containerColor = VikingSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isModelReady) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Ready",
                            tint = VikingSafe,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Gemma 270M Engine Ready",
                                style = MaterialTheme.typography.titleMedium,
                                color = VikingWhite
                            )
                            Text(
                                text = "100% Local Inference • Zero Network Calls",
                                style = MaterialTheme.typography.bodyMedium,
                                color = VikingGray
                            )
                        }
                    } else {
                        CircularProgressIndicator(
                            color = VikingTeal,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Initializing AI Engine…",
                                style = MaterialTheme.typography.titleMedium,
                                color = VikingWhite
                            )
                            Text(
                                text = "Loading Gemma 270M INT4 model into memory",
                                style = MaterialTheme.typography.bodyMedium,
                                color = VikingGray
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Permissions Status Card
            Card(
                colors = CardDefaults.cardColors(containerColor = VikingSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Required Security Permissions",
                        style = MaterialTheme.typography.titleMedium,
                        color = VikingWhite
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Viking needs SMS and Call Log access to analyze incoming threat patterns on-device.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = VikingGray
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (!permissionsState.allPermissionsGranted) {
                        Button(
                            onClick = { permissionsState.launchMultiplePermissionRequest() },
                            colors = ButtonDefaults.buttonColors(containerColor = VikingTeal),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Grant Security Permissions", color = VikingBlack, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Granted",
                                tint = VikingSafe
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("All permissions granted", color = VikingSafe, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }

        // Action Button at Bottom
        Button(
            onClick = onOnboardingComplete,
            colors = ButtonDefaults.buttonColors(containerColor = VikingTeal),
            enabled = permissionsState.allPermissionsGranted,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .align(Alignment.BottomCenter)
        ) {
            Text(
                text = "ENTER VIKING SHIELD",
                color = VikingBlack,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}
