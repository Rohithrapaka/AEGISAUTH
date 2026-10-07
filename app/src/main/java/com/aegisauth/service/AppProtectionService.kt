package com.aegisauth.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import android.widget.LinearLayout
import android.widget.TextView
import com.aegisauth.data.datastore.AegisPreferences
import com.aegisauth.data.repository.AppProtectionStateRepository
import com.aegisauth.data.repository.ProtectedAppsRepository
import com.aegisauth.feature.authentication.AuthOverlayActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

@AndroidEntryPoint
class AppProtectionService : AccessibilityService() {

    @Inject
    lateinit var protectedAppsRepository: ProtectedAppsRepository

    @Inject
    lateinit var preferences: AegisPreferences

    @Inject
    lateinit var appProtectionStateRepository: AppProtectionStateRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mainHandler = Handler(Looper.getMainLooper())
    private val inMemoryProtectedPackages = Collections.newSetFromMap(ConcurrentHashMap<String, Boolean>())
    private var lastInterceptedPackage: String? = null
    private var lastInterceptTime: Long = 0L
    private var autoDismissRunnable: Runnable? = null
    private var heartbeatJob: Job? = null

    companion object {
        private const val TAG = "AegisProtection"
        private const val LAUNCH_DEBOUNCE_MS = 600L
        private const val CANCEL_COOLDOWN_MS = 4000L
        private val unlockedSessionCache = ConcurrentHashMap<String, Long>()

        @Volatile
        var lastCancelledPackage: String? = null
            private set

        @Volatile
        var lastCancelTime: Long = 0L
            private set

        private var instance: AppProtectionService? = null
        var isServiceRunning = false
            private set

        fun markPackageUnlocked(packageName: String) {
            unlockedSessionCache[packageName] = System.currentTimeMillis()
            Log.d(TAG, "Package marked unlocked (grace period started): $packageName")
            hideBlockingOverlay()
        }

        fun clearGracePeriod(packageName: String) {
            unlockedSessionCache.remove(packageName)
            Log.d(TAG, "Grace period cleared for package: $packageName")
            hideBlockingOverlay()
        }

        fun hideBlockingOverlay() {
            instance?.removeBlockingOverlayView()
        }

        fun notifyAuthActivityReady() {
            Log.d(TAG, "AuthOverlayActivity ready/visible. Dismissing instant blocking layer.")
            hideBlockingOverlay()
        }

        fun cancelAndReturnHome(packageName: String?, context: Context? = null) {
            Log.d(TAG, "cancelAndReturnHome invoked for package: $packageName")
            if (!packageName.isNullOrBlank()) {
                clearGracePeriod(packageName)
                lastCancelledPackage = packageName
                lastCancelTime = System.currentTimeMillis()
            }
            hideBlockingOverlay()

            // 1. Primary mechanism: AccessibilityService global action HOME
            val handledGlobally = instance?.performGlobalAction(GLOBAL_ACTION_HOME) ?: false

            // 2. Redundant fallback mechanism: Start Intent.ACTION_MAIN with CATEGORY_HOME
            val targetContext = context ?: instance
            if (targetContext != null) {
                try {
                    val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_HOME)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    targetContext.startActivity(homeIntent)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to start home activity via Intent: ${e.localizedMessage}")
                }
            }
        }

        fun isPackageInGracePeriod(packageName: String, gracePeriodMillis: Long): Boolean {
            val lastUnlocked = unlockedSessionCache[packageName] ?: return false
            val elapsed = System.currentTimeMillis() - lastUnlocked
            return elapsed < gracePeriodMillis
        }

        /**
         * Checks whether AEGISAUTH AccessibilityService is actually enabled in Android system settings.
         */
        fun isAccessibilityServiceEnabled(context: Context): Boolean {
            // 1. If service instance is live and running in memory, it is connected
            if (isServiceRunning && instance != null) {
                Log.d(TAG, "SERVICE_ENABLED_CHECK: isServiceRunning=true -> ENABLED")
                return true
            }

            val expectedPackage = context.packageName
            val expectedServiceSimple = AppProtectionService::class.java.simpleName
            val expectedServiceFull = AppProtectionService::class.java.name

            // 2. Primary Android system check via Settings.Secure (authoritative across all Android versions and OEMs)
            try {
                val accessibilityEnabled = Settings.Secure.getInt(
                    context.contentResolver,
                    Settings.Secure.ACCESSIBILITY_ENABLED,
                    0
                )
                if (accessibilityEnabled == 1) {
                    val settingValue = Settings.Secure.getString(
                        context.contentResolver,
                        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
                    )
                    if (!settingValue.isNullOrBlank()) {
                        val colonSplitter = TextUtils.SimpleStringSplitter(':')
                        colonSplitter.setString(settingValue)
                        while (colonSplitter.hasNext()) {
                            val componentNameStr = colonSplitter.next()
                            if (componentNameStr.contains(expectedPackage, ignoreCase = true) &&
                                (componentNameStr.contains(expectedServiceSimple, ignoreCase = true) ||
                                 componentNameStr.contains(expectedServiceFull, ignoreCase = true))
                            ) {
                                Log.d(TAG, "SERVICE_ENABLED_CHECK: Found in Settings.Secure ($componentNameStr) -> ENABLED")
                                return true
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking Settings.Secure accessibility: ${e.localizedMessage}")
            }

            // 3. Fallback check via AccessibilityManager enabled services list
            try {
                val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
                if (am != null) {
                    val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
                    for (service in enabledServices) {
                        val serviceId = service.id ?: ""
                        val packageName = service.resolveInfo?.serviceInfo?.packageName ?: ""
                        val serviceName = service.resolveInfo?.serviceInfo?.name ?: ""

                        if (serviceId.contains(expectedPackage, ignoreCase = true) &&
                            (serviceId.contains(expectedServiceSimple, ignoreCase = true) || serviceId.contains(expectedServiceFull, ignoreCase = true))
                        ) {
                            Log.d(TAG, "SERVICE_ENABLED_CHECK: Found in AccessibilityManager ID ($serviceId) -> ENABLED")
                            return true
                        }
                        if (packageName.equals(expectedPackage, ignoreCase = true) &&
                            (serviceName.contains(expectedServiceSimple, ignoreCase = true) || serviceName.equals(expectedServiceFull, ignoreCase = true))
                        ) {
                            Log.d(TAG, "SERVICE_ENABLED_CHECK: Found in AccessibilityManager ResolveInfo ($packageName/$serviceName) -> ENABLED")
                            return true
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking AccessibilityManager: ${e.localizedMessage}")
            }

            Log.d(TAG, "SERVICE_ENABLED_CHECK: Not found -> DISABLED")
            return false
        }
    }

    private var windowManager: WindowManager? = null
    private var blockingView: View? = null
    private var isBlockingViewAttached = false

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "SERVICE_CREATED: AppProtectionService initialized")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        isServiceRunning = true
        windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager

        val info = serviceInfo ?: AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_WINDOWS_CHANGED
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        info.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        info.notificationTimeout = 20
        serviceInfo = info

        Log.d(TAG, "onServiceConnected: AppProtectionService connected and configured with flags=${info.flags}")

        // Update persistent state repository & start periodic heartbeat loop
        appProtectionStateRepository.updateServiceConnected(true)
        heartbeatJob?.cancel()
        Log.d(TAG, "HEARTBEAT_STARTED: starting 2000ms heartbeat loop")
        heartbeatJob = serviceScope.launch {
            while (true) {
                appProtectionStateRepository.recordHeartbeat()
                Log.d(TAG, "HEARTBEAT_UPDATED: timestamp=${System.currentTimeMillis()}")
                delay(2000L)
            }
        }

        // Continuously sync protected apps cache for instant O(1) checks on incoming events
        serviceScope.launch {
            try {
                protectedAppsRepository.protectedAppsFlow.collect { apps ->
                    inMemoryProtectedPackages.clear()
                    apps.filter { it.isProtected }.forEach {
                        inMemoryProtectedPackages.add(it.packageName)
                    }
                    Log.d(TAG, "PROTECTED_APPS_LOADED: ${inMemoryProtectedPackages.size} packages protected.")
                    appProtectionStateRepository.updateInitializationState(true, inMemoryProtectedPackages.size)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error synchronizing protected apps cache: ${e.localizedMessage}")
                appProtectionStateRepository.updateErrorState("Sync error: ${e.localizedMessage}")
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        appProtectionStateRepository.recordHeartbeat()

        val pkgName = event.packageName?.toString() ?: return
        val myPackage = packageName

        Log.d(TAG, "ACCESSIBILITY_EVENT_RECEIVED: package=$pkgName")

        // 1. Keyguard / Lockscreen check — Never intercept when device is locked or waking on keyguard
        val keyguard = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        if (keyguard?.isKeyguardLocked == true) {
            Log.d(TAG, "Device screen is locked/keyguard active. Ignoring window event for $pkgName.")
            return
        }

        // 2. Ignore AEGISAUTH itself (self-protection is handled internally via MainActivity gate to avoid recursion loops),
        // Android system UI, system framework, common keyboards/launchers
        if (pkgName == myPackage ||
            pkgName == "com.android.systemui" ||
            pkgName == "android" ||
            pkgName == "com.google.android.inputmethod.latin" ||
            pkgName == "com.samsung.android.honeyboard" ||
            pkgName == "com.sec.android.inputmethod" ||
            isHomeLauncher(pkgName)
        ) {
            return
        }

        // 3. Cancellation Cooldown check — Prevents infinite re-trigger when returning to Home
        val now = System.currentTimeMillis()
        if (pkgName == lastCancelledPackage && (now - lastCancelTime) < CANCEL_COOLDOWN_MS) {
            Log.d(TAG, "Package $pkgName is in cancellation cooldown (${CANCEL_COOLDOWN_MS}ms). Ignoring event.")
            return
        }

        Log.d(TAG, "Detected foreground package: $pkgName")

        serviceScope.launch {
            try {
                val isProtectionEnabled = preferences.isProtectionServiceEnabled.first()
                if (!isProtectionEnabled) {
                    return@launch
                }

                // Instant cache check
                val isProtected = inMemoryProtectedPackages.contains(pkgName) ||
                        protectedAppsRepository.isPackageProtected(pkgName)

                if (!isProtected) return@launch

                val gracePeriodSecs = preferences.unlockGracePeriodSecs.first()
                val gracePeriodMillis = gracePeriodSecs * 1000L
                val inGracePeriod = isPackageInGracePeriod(pkgName, gracePeriodMillis)

                Log.d(TAG, "Package $pkgName isProtected=true, inGracePeriod=$inGracePeriod (${gracePeriodSecs}s)")
                if (inGracePeriod) {
                    return@launch
                }

                // Debounce to prevent multiple concurrent triggers for the same app
                if (lastInterceptedPackage == pkgName && (now - lastInterceptTime) < LAUNCH_DEBOUNCE_MS) {
                    return@launch
                }
                lastInterceptedPackage = pkgName
                lastInterceptTime = now

                // Resolve user-facing application name
                val pm = packageManager
                val appName = try {
                    val info = pm.getApplicationInfo(pkgName, 0)
                    pm.getApplicationLabel(info).toString()
                } catch (e: Exception) {
                    pkgName
                }

                Log.d(TAG, "ZERO-BYPASS INTERCEPTION: Instant blocking layer + starting AuthOverlay for $pkgName ($appName)")

                // STEP 1: Immediately attach full-screen blocking overlay view with failsafe controls
                mainHandler.post {
                    showBlockingOverlayView(appName, pkgName)
                }

                // STEP 2: Launch full AuthOverlayActivity immediately
                val overlayIntent = Intent(this@AppProtectionService, AuthOverlayActivity::class.java).apply {
                    putExtra(AuthOverlayActivity.EXTRA_PACKAGE_NAME, pkgName)
                    putExtra(AuthOverlayActivity.EXTRA_APP_NAME, appName)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP or
                            Intent.FLAG_ACTIVITY_NO_ANIMATION
                }
                startActivity(overlayIntent)
            } catch (e: Exception) {
                Log.e(TAG, "Error during accessibility event interception for $pkgName: ${e.localizedMessage}")
            }
        }
    }

    private fun isHomeLauncher(pkgName: String): Boolean {
        if (pkgName == "com.android.launcher3" ||
            pkgName == "com.google.android.apps.nexuslauncher" ||
            pkgName == "com.google.android.googlequicksearchbox" ||
            pkgName == "com.sec.android.app.launcher" ||
            pkgName == "com.samsung.android.app.honeyboard" ||
            pkgName == "com.miui.home" ||
            pkgName == "com.oppo.launcher" ||
            pkgName == "com.oneplus.launcher" ||
            pkgName == "com.huawei.android.launcher"
        ) {
            return true
        }
        return try {
            val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
            val resolveInfo = packageManager.resolveActivity(intent, 0)
            resolveInfo?.activityInfo?.packageName == pkgName
        } catch (e: Exception) {
            false
        }
    }

    private fun showBlockingOverlayView(appName: String, pkgName: String) {
        if (isBlockingViewAttached) return

        try {
            val wm = windowManager ?: return

            val layout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setBackgroundColor(Color.parseColor("#0A0E14")) // Dark solid background blocking underlying app
                isClickable = true
                isFocusable = true
                isFocusableInTouchMode = true

                val titleView = TextView(context).apply {
                    text = "AEGISAUTH SHIELD"
                    textSize = 18f
                    setTextColor(Color.parseColor("#00E5FF"))
                    typeface = Typeface.DEFAULT_BOLD
                    gravity = Gravity.CENTER
                }

                val subtitleView = TextView(context).apply {
                    text = "Biometric Verification Required\nSecuring $appName..."
                    textSize = 14f
                    setTextColor(Color.parseColor("#CBD5E1"))
                    gravity = Gravity.CENTER
                    setPadding(0, 16, 0, 32)
                }

                // Failsafe Emergency Exit Button on the blocking overlay view
                val cancelButton = TextView(context).apply {
                    text = "CANCEL & RETURN HOME"
                    textSize = 13f
                    setTextColor(Color.parseColor("#FFFFFF"))
                    typeface = Typeface.DEFAULT_BOLD
                    gravity = Gravity.CENTER
                    val paddingHorizontal = (28 * resources.displayMetrics.density).toInt()
                    val paddingVertical = (12 * resources.displayMetrics.density).toInt()
                    setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical)

                    val shape = GradientDrawable().apply {
                        cornerRadius = 24 * resources.displayMetrics.density
                        setColor(Color.parseColor("#1C2333"))
                        setStroke((1.5f * resources.displayMetrics.density).toInt(), Color.parseColor("#00E5FF"))
                    }
                    background = shape

                    isClickable = true
                    isFocusable = true
                    setOnClickListener {
                        Log.d(TAG, "Blocking overlay failsafe Cancel button clicked.")
                        cancelAndReturnHome(pkgName, context)
                    }
                }

                val helperView = TextView(context).apply {
                    text = "Press Back or tap button to return to Home"
                    textSize = 11f
                    setTextColor(Color.parseColor("#64748B"))
                    gravity = Gravity.CENTER
                    setPadding(0, 12, 0, 0)
                }

                addView(titleView)
                addView(subtitleView)
                addView(cancelButton)
                addView(helperView)

                // Back key listener directly on the blocking overlay layout
                setOnKeyListener { _, keyCode, keyEvent ->
                    if (keyCode == KeyEvent.KEYCODE_BACK && keyEvent.action == KeyEvent.ACTION_UP) {
                        Log.d(TAG, "Back key intercepted by blocking overlay view.")
                        cancelAndReturnHome(pkgName, context)
                        true
                    } else {
                        false
                    }
                }
            }

            val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                overlayType,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                        WindowManager.LayoutParams.FLAG_FULLSCREEN or
                        WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.CENTER
            }

            wm.addView(layout, params)
            layout.requestFocus()
            blockingView = layout
            isBlockingViewAttached = true
            Log.d(TAG, "Instant blocking overlay attached to WindowManager successfully.")

            // Safety timeout: Auto-dismiss after 12s if AuthOverlay fails to take over
            autoDismissRunnable?.let { mainHandler.removeCallbacks(it) }
            val timeoutRunnable = Runnable {
                Log.w(TAG, "Auto-dismiss timer triggered on blocking view.")
                removeBlockingOverlayView()
            }
            autoDismissRunnable = timeoutRunnable
            mainHandler.postDelayed(timeoutRunnable, 12000L)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to attach instant blocking overlay: ${e.localizedMessage}")
        }
    }

    private fun removeBlockingOverlayView() {
        mainHandler.post {
            autoDismissRunnable?.let {
                mainHandler.removeCallbacks(it)
                autoDismissRunnable = null
            }
            if (isBlockingViewAttached && blockingView != null) {
                try {
                    windowManager?.removeView(blockingView)
                    Log.d(TAG, "Blocking overlay removed from WindowManager.")
                } catch (e: Exception) {
                    Log.e(TAG, "Error removing blocking overlay: ${e.localizedMessage}")
                } finally {
                    blockingView = null
                    isBlockingViewAttached = false
                }
            }
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "SERVICE_DISCONNECTED: AppProtectionService onUnbind called")
        heartbeatJob?.cancel()
        appProtectionStateRepository.updateServiceConnected(false)
        instance = null
        isServiceRunning = false
        removeBlockingOverlayView()
        return super.onUnbind(intent)
    }

    override fun onInterrupt() {
        Log.w(TAG, "SERVICE_DISCONNECTED: AppProtectionService interrupted by system.")
        removeBlockingOverlayView()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "SERVICE_DESTROYED: AppProtectionService destroyed.")
        heartbeatJob?.cancel()
        appProtectionStateRepository.updateServiceConnected(false)
        instance = null
        isServiceRunning = false
        removeBlockingOverlayView()
    }
}


