package com.aegisauth.feature.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aegisauth.core.theme.*

@Composable
fun SplashScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToEnrollment: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToSelfAuthentication: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val destination by viewModel.destination.collectAsState()

    LaunchedEffect(destination) {
        when (destination) {
            SplashDestination.Onboarding -> onNavigateToOnboarding()
            SplashDestination.Enrollment -> onNavigateToEnrollment()
            SplashDestination.Dashboard -> onNavigateToDashboard()
            SplashDestination.SelfAuthentication -> onNavigateToSelfAuthentication()
            SplashDestination.Loading -> Unit
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "splash_ring")
    val ringScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AegisDarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(140.dp)
            ) {
                // Animated pulse halo
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .scale(ringScale)
                        .clip(CircleShape)
                        .border(1.5.dp, AegisCyan.copy(alpha = 0.35f), CircleShape)
                )

                // Outer container
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(AegisDarkSurfaceElevated)
                        .border(1.5.dp, AegisCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "AEGIS Shield",
                        tint = AegisCyan,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "AEGISAUTH",
                style = MaterialTheme.typography.displayMedium.copy(
                    letterSpacing = 4.sp,
                    fontWeight = FontWeight.Black
                ),
                color = AegisTextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "BIOMETRIC SHIELD // PASSIVE LIVENESS",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = AegisCyan
            )
        }

        // Bottom version/status tag
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "SECURE ENCLAVE HARDENED • V1.0",
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp),
                color = AegisTextTertiary
            )
        }
    }
}
