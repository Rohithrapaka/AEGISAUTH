package com.aegisauth

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.aegisauth.core.navigation.AegisNavHost
import com.aegisauth.core.theme.AegisDarkBackground
import com.aegisauth.core.theme.AegisTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            AegisTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AegisDarkBackground
                ) {
                    val navController = rememberNavController()
                    AegisNavHost(navController = navController)
                }
            }
        }
    }
}
