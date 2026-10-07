package com.aegisauth.core.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Enrollment : Screen("enrollment")
    object EnrollmentComplete : Screen("enrollment_complete?cq={cq}&fc={fc}&lq={lq}&pc={pc}&oc={oc}") {
        fun createRoute(cq: Int = 90, fc: Int = 90, lq: Int = 85, pc: Int = 92, oc: Int = 90): String {
            return "enrollment_complete?cq=$cq&fc=$fc&lq=$lq&pc=$pc&oc=$oc"
        }
    }
    object PatternSetup : Screen("pattern_setup")
    object Dashboard : Screen("dashboard")
    object ProtectedApps : Screen("protected_apps")
    object Authentication : Screen("authentication?pkg={pkg}&appName={appName}") {
        fun createRoute(pkg: String = "", appName: String = ""): String {
            return "authentication?pkg=$pkg&appName=$appName"
        }
    }
    object History : Screen("history")
    object Alerts : Screen("alerts")
    object DeviceSecurity : Screen("device_security")
    object BiometricUpdates : Screen("biometric_updates")
    object Settings : Screen("settings")
}
