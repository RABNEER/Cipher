package com.apocalyptolabs.viking.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.apocalyptolabs.viking.ui.screens.dashboard.DashboardScreen
import com.apocalyptolabs.viking.ui.screens.onboarding.OnboardingScreen
import com.apocalyptolabs.viking.ui.screens.permissions.PermissionAuditScreen
import com.apocalyptolabs.viking.ui.screens.sandbox.AttackSandboxScreen
import com.apocalyptolabs.viking.ui.screens.scanner.ApkScannerScreen
import com.apocalyptolabs.viking.ui.screens.settings.SettingsScreen
import com.apocalyptolabs.viking.ui.screens.threatlog.ThreatLogScreen

object VikingDestinations {
    const val ONBOARDING = "onboarding"
    const val DASHBOARD = "dashboard"
    const val APK_SCANNER = "apk_scanner"
    const val THREAT_LOG = "threat_log?type={type}"
    const val PERMISSION_AUDIT = "permission_audit"
    const val SETTINGS = "settings"
    const val SANDBOX = "sandbox"

    fun threatLogRoute(type: String? = null): String {
        return if (type != null) "threat_log?type=$type" else "threat_log?type="
    }
}

@Composable
fun VikingNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = VikingDestinations.ONBOARDING
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(VikingDestinations.ONBOARDING) {
            OnboardingScreen(
                onOnboardingComplete = {
                    navController.navigate(VikingDestinations.DASHBOARD) {
                        popUpTo(VikingDestinations.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(VikingDestinations.DASHBOARD) {
            DashboardScreen(
                onNavigateToApkScanner = { navController.navigate(VikingDestinations.APK_SCANNER) },
                onNavigateToThreatLog = { type -> navController.navigate(VikingDestinations.threatLogRoute(type)) },
                onNavigateToPermissionAudit = { navController.navigate(VikingDestinations.PERMISSION_AUDIT) },
                onNavigateToSettings = { navController.navigate(VikingDestinations.SETTINGS) },
                onNavigateToSandbox = { navController.navigate(VikingDestinations.SANDBOX) }
            )
        }

        composable(VikingDestinations.APK_SCANNER) {
            ApkScannerScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = VikingDestinations.THREAT_LOG,
            arguments = listOf(
                androidx.navigation.navArgument("type") {
                    type = androidx.navigation.NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            ThreatLogScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(VikingDestinations.PERMISSION_AUDIT) {
            PermissionAuditScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(VikingDestinations.SETTINGS) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(VikingDestinations.SANDBOX) {
            AttackSandboxScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
