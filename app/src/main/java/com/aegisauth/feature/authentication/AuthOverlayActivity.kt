package com.aegisauth.feature.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.aegisauth.core.theme.AegisDarkBackground
import com.aegisauth.core.theme.AegisTheme
import com.aegisauth.service.AppProtectionService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthOverlayActivity : FragmentActivity() {

    private val tag = "AegisProtection"
    private var currentPackage by mutableStateOf("")
    private var currentAppName by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        currentPackage = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: ""
        currentAppName = intent.getStringExtra(EXTRA_APP_NAME) ?: ""

        Log.d(tag, "AuthOverlayActivity created for target package: $currentPackage ($currentAppName)")

        // Once AuthOverlayActivity is created, notify service to dismiss the blocking overlay view so camera and UI are visible
        AppProtectionService.notifyAuthActivityReady()

        // Return to home screen on back press — prevents bypassing auth by pressing back into the protected app
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.d(tag, "Back button pressed on overlay, navigating home to prevent unauthorized access.")
                AppProtectionService.cancelAndReturnHome(currentPackage, this@AuthOverlayActivity)
                finish()
            }
        })

        setContent {
            AegisTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AegisDarkBackground
                ) {
                    AuthenticationScreen(
                        packageName = currentPackage,
                        appName = currentAppName,
                        onAuthenticationSuccess = {
                            Log.d(tag, "Authentication SUCCESS for $currentPackage. Granting access.")
                            AppProtectionService.markPackageUnlocked(currentPackage)
                            finish()
                        },
                        onCancel = {
                            Log.d(tag, "Authentication CANCELLED for $currentPackage. Returning to home.")
                            AppProtectionService.cancelAndReturnHome(currentPackage, this@AuthOverlayActivity)
                            finish()
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Ensure blocking overlay is dismissed as soon as our activity is in foreground
        AppProtectionService.notifyAuthActivityReady()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        currentPackage = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: ""
        currentAppName = intent.getStringExtra(EXTRA_APP_NAME) ?: ""
        Log.d(tag, "AuthOverlayActivity onNewIntent for: $currentPackage ($currentAppName)")
        AppProtectionService.notifyAuthActivityReady()
    }

    override fun onDestroy() {
        super.onDestroy()
        AppProtectionService.hideBlockingOverlay()
    }

    companion object {
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
        const val EXTRA_APP_NAME = "extra_app_name"
    }
}

