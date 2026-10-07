package com.aegisauth.core.navigation

import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.aegisauth.domain.model.EnrollmentMetrics
import com.aegisauth.feature.alerts.AlertsScreen
import com.aegisauth.feature.authentication.AuthenticationScreen
import com.aegisauth.feature.biometric.BiometricUpdatesScreen
import com.aegisauth.feature.dashboard.DashboardScreen
import com.aegisauth.feature.enrollment.EnrollmentCompleteScreen
import com.aegisauth.feature.enrollment.FaceEnrollmentScreen
import com.aegisauth.feature.history.HistoryScreen
import com.aegisauth.feature.onboarding.OnboardingScreen
import com.aegisauth.feature.protectedapps.ProtectedAppsScreen
import com.aegisauth.feature.security.DeviceSecurityScreen
import com.aegisauth.feature.settings.SettingsScreen
import com.aegisauth.feature.splash.SplashScreen
import com.aegisauth.core.security.PasswordSecurityManager
import com.aegisauth.core.security.PatternSecurityManager
import com.aegisauth.data.repository.ProtectedAppsRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AegisNavHostEntryPoint {
    fun protectedAppsRepository(): ProtectedAppsRepository
    fun patternSecurityManager(): PatternSecurityManager
    fun passwordSecurityManager(): PasswordSecurityManager
}

@Composable
fun AegisNavHost(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() },
        popEnterTransition = { fadeIn() },
        popExitTransition = { fadeOut() }
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToEnrollment = {
                    navController.navigate(Screen.Enrollment.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToSelfAuthentication = {
                    navController.navigate(Screen.Authentication.createRoute("com.aegisauth", "AEGISAUTH")) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onNavigateToEnrollment = {
                    navController.navigate(Screen.Enrollment.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Enrollment.route) {
            FaceEnrollmentScreen(
                onEnrollmentComplete = { metrics ->
                    navController.navigate(Screen.PatternSetup.route) {
                        popUpTo(Screen.Enrollment.route) { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.PatternSetup.route) {
            val context = androidx.compose.ui.platform.LocalContext.current
            val entryPoint = dagger.hilt.android.EntryPointAccessors.fromApplication(
                context.applicationContext,
                AegisNavHostEntryPoint::class.java
            )

            com.aegisauth.feature.enrollment.PatternSetupScreen(
                onPatternSetupComplete = {
                    navController.navigate(Screen.EnrollmentComplete.createRoute()) {
                        popUpTo(Screen.PatternSetup.route) { inclusive = true }
                    }
                },
                patternSecurityManager = entryPoint.patternSecurityManager(),
                passwordSecurityManager = entryPoint.passwordSecurityManager(),
                protectedAppsRepository = entryPoint.protectedAppsRepository()
            )
        }

        composable(
            route = Screen.EnrollmentComplete.route,
            arguments = listOf(
                navArgument("cq") { type = NavType.IntType; defaultValue = 94 },
                navArgument("fc") { type = NavType.IntType; defaultValue = 91 },
                navArgument("lq") { type = NavType.IntType; defaultValue = 88 },
                navArgument("pc") { type = NavType.IntType; defaultValue = 96 },
                navArgument("oc") { type = NavType.IntType; defaultValue = 93 }
            )
        ) { backStackEntry ->
            val cq = backStackEntry.arguments?.getInt("cq") ?: 94
            val fc = backStackEntry.arguments?.getInt("fc") ?: 91
            val lq = backStackEntry.arguments?.getInt("lq") ?: 88
            val pc = backStackEntry.arguments?.getInt("pc") ?: 96
            val oc = backStackEntry.arguments?.getInt("oc") ?: 93

            EnrollmentCompleteScreen(
                captureQuality = cq,
                faceConsistency = fc,
                lightingQuality = lq,
                poseConsistency = pc,
                overallConfidence = oc,
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.EnrollmentComplete.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToProtectedApps = { navController.navigate(Screen.ProtectedApps.route) },
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onNavigateToAlerts = { navController.navigate(Screen.Alerts.route) },
                onNavigateToDeviceSecurity = { navController.navigate(Screen.DeviceSecurity.route) },
                onNavigateToBiometricUpdates = { navController.navigate(Screen.BiometricUpdates.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onTestAuthentication = {
                    navController.navigate(Screen.Authentication.createRoute("com.demo.testapp", "Demo Protected App"))
                }
            )
        }

        composable(Screen.ProtectedApps.route) {
            ProtectedAppsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Authentication.route,
            arguments = listOf(
                navArgument("pkg") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("appName") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val context = androidx.compose.ui.platform.LocalContext.current
            val pkg = backStackEntry.arguments?.getString("pkg") ?: ""
            val appName = backStackEntry.arguments?.getString("appName") ?: ""

            AuthenticationScreen(
                packageName = pkg,
                appName = appName,
                onAuthenticationSuccess = {
                    if (pkg == "com.aegisauth") {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Authentication.route) { inclusive = true }
                        }
                    } else {
                        navController.popBackStack()
                    }
                },
                onCancel = {
                    if (pkg == "com.aegisauth") {
                        (context as? android.app.Activity)?.finish()
                    } else {
                        navController.popBackStack()
                    }
                }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Alerts.route) {
            AlertsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.DeviceSecurity.route) {
            DeviceSecurityScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.BiometricUpdates.route) {
            BiometricUpdatesScreen(
                onReEnroll = {
                    navController.navigate(Screen.Enrollment.route)
                },
                onReEnrollForProfile = { profileId ->
                    navController.navigate(Screen.Enrollment.route)
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
