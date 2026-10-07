package com.aegisauth.feature.authentication

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.aegisauth.core.camera.FaceFrameAnalyzer
import com.aegisauth.core.theme.*
import com.aegisauth.core.ui.components.AegisButton
import com.aegisauth.core.ui.components.AegisTopBar
import com.aegisauth.core.ui.components.StatusBadge
import com.aegisauth.domain.model.AuthenticationState
import com.aegisauth.domain.model.QualityFeedback
import java.util.concurrent.Executors

import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.fragment.app.FragmentActivity

@Composable
fun AuthenticationScreen(
    packageName: String = "",
    appName: String = "",
    onAuthenticationSuccess: () -> Unit,
    onCancel: () -> Unit,
    viewModel: AuthenticationViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val authState by viewModel.authState.collectAsState()

    var showRecoveryModal by remember { mutableStateOf(false) }
    var recoveryPasswordInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var recoveryError by remember { mutableStateOf(false) }
    var recoveryErrorMessage by remember { mutableStateOf("") }

    val hasSystemBiometric = remember(context) {
        viewModel.systemBiometricManager.isSystemBiometricAvailable(context)
    }
    val biometricActionLabel = remember(context) {
        viewModel.systemBiometricManager.getUiActionLabel(context)
    }
    // M15.1: Recovery only surfaced when the full biometric chain has genuinely failed for com.aegisauth
    val hasPatternRecovery = remember { viewModel.patternSecurityManager.hasPattern() }
    val hasPasswordRecovery = remember { viewModel.passwordSecurityManager.hasPassword() }
    val showRecoveryButton by remember(authState) {
        derivedStateOf {
            authState is AuthenticationState.RecoveryFallback &&
                    (hasPatternRecovery || hasPasswordRecovery)
        }
    }

    LaunchedEffect(packageName, appName) {
        viewModel.setTargetApp(packageName, appName)
    }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(authState) {
        if (authState is AuthenticationState.Authenticated) {
            kotlinx.coroutines.delay(1000)
            onAuthenticationSuccess()
        }
    }

    Scaffold(
        topBar = {
            AegisTopBar(
                title = if (appName.isNotBlank()) appName else "AEGIS Shield",
                subtitle = "BIOMETRIC VERIFICATION",
                onBackClick = onCancel
            )
        },
        containerColor = AegisDarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Status Bar
            AuthenticationStatusHeader(authState = authState)

            // Camera Scanner Center Viewport
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                if (hasCameraPermission) {
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx).apply {
                                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                            }
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            val cameraExecutor = Executors.newSingleThreadExecutor()

                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }

                                val imageAnalysis = ImageAnalysis.Builder()
                                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                    .build()
                                    .also { analysis ->
                                        analysis.setAnalyzer(
                                            cameraExecutor,
                                            FaceFrameAnalyzer { face, w, h, bmp ->
                                                viewModel.processFrame(face, w, h, bmp)
                                            }
                                        )
                                    }

                                try {
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        CameraSelector.DEFAULT_FRONT_CAMERA,
                                        preview,
                                        imageAnalysis
                                    )
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }, ContextCompat.getMainExecutor(ctx))

                            previewView
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(32.dp))
                            .border(1.5.dp, AegisBorderActive, RoundedCornerShape(32.dp))
                    )
                }

                // HUD Overlay Reticle
                AuthenticationHUDOverlay(authState = authState)
            }

            // Multi-Biometric Backup & Recovery Controls
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (hasSystemBiometric &&
                    (viewModel.isFaceAuthFailed || authState is AuthenticationState.SystemBiometricFallback || authState is AuthenticationState.SystemBiometricFailed) &&
                    authState !is AuthenticationState.Authenticated &&
                    authState !is AuthenticationState.Locked &&
                    authState !is AuthenticationState.RecoveryFallback &&
                    authState !is AuthenticationState.AccessDenied
                ) {
                    AegisButton(
                        text = biometricActionLabel,
                        onClick = {
                            val activity = context as? FragmentActivity
                            if (activity != null) {
                                viewModel.authenticateWithSystemBiometric(activity)
                            }
                        },
                        icon = Icons.Default.Fingerprint,
                        isPrimary = false
                    )
                }

                if (showRecoveryButton) {
                    TextButton(
                        onClick = {
                            recoveryError = false
                            recoveryErrorMessage = ""
                            recoveryPasswordInput = ""
                            isPasswordVisible = false
                            showRecoveryModal = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = "Recovery",
                            tint = AegisCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "USE RECOVERY CODE / PATTERN",
                            style = MaterialTheme.typography.labelSmall,
                            color = AegisCyan
                        )
                    }
                }

                if (authState is AuthenticationState.AccessDenied) {
                    Text(
                        text = "ACCESS DENIED — Recovery not available for this application.",
                        style = MaterialTheme.typography.labelSmall,
                        color = AegisCritical
                    )
                }

                // Bottom Action Area & Instruction
                AuthenticationBottomControls(
                    authState = authState,
                    onRetry = { viewModel.retryAuthentication() },
                    onCancel = onCancel
                )
            }
        }
    }

    // Recovery Dialog / Modal
    if (showRecoveryModal) {
        AlertDialog(
            onDismissRequest = {
                showRecoveryModal = false
                recoveryPasswordInput = ""
                isPasswordVisible = false
            },
            containerColor = AegisDarkSurfaceVariant,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Security Recovery",
                        tint = AegisCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Backup Recovery",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = AegisTextPrimary
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (hasPasswordRecovery) {
                        Text(
                            text = "Enter your secure backup password to verify your identity.",
                            style = MaterialTheme.typography.bodySmall,
                            color = AegisTextSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = recoveryPasswordInput,
                            onValueChange = {
                                recoveryPasswordInput = it
                                recoveryError = false
                            },
                            label = { Text("Backup Password") },
                            singleLine = true,
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                                        tint = AegisTextSecondary
                                    )
                                }
                            },
                            isError = recoveryError,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AegisCyan,
                                unfocusedBorderColor = AegisBorderActive,
                                focusedTextColor = AegisTextPrimary,
                                unfocusedTextColor = AegisTextPrimary,
                                errorBorderColor = AegisCritical
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (recoveryError) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = recoveryErrorMessage.ifBlank { "Incorrect password" },
                                style = MaterialTheme.typography.labelSmall,
                                color = AegisCritical
                            )
                        }
                    } else if (hasPatternRecovery) {
                        Text(
                            text = "Draw your backup recovery pattern.",
                            style = MaterialTheme.typography.bodySmall,
                            color = AegisTextSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        com.aegisauth.core.ui.components.PatternLockView(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            isError = recoveryError,
                            onPatternComplete = { pattern ->
                                val success = viewModel.verifyRecoveryPattern(pattern)
                                if (success) {
                                    showRecoveryModal = false
                                } else {
                                    recoveryError = true
                                    recoveryErrorMessage = "Incorrect recovery pattern"
                                }
                            }
                        )
                    }
                }
            },
            confirmButton = {
                if (hasPasswordRecovery) {
                    AegisButton(
                        text = "VERIFY",
                        onClick = {
                            if (recoveryPasswordInput.isNotBlank()) {
                                val success = viewModel.verifyRecoveryPassword(recoveryPasswordInput)
                                if (success) {
                                    showRecoveryModal = false
                                    recoveryPasswordInput = ""
                                    isPasswordVisible = false
                                } else {
                                    recoveryError = true
                                    recoveryErrorMessage = "Incorrect recovery password"
                                }
                            }
                        },
                        isPrimary = true
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRecoveryModal = false
                        recoveryPasswordInput = ""
                        isPasswordVisible = false
                    }
                ) {
                    Text("CANCEL", color = AegisTextSecondary)
                }
            }
        )
    }
}

@Composable
fun AuthenticationStatusHeader(authState: AuthenticationState) {
    val (statusText, statusColor) = when (authState) {
        is AuthenticationState.FaceSearch -> "SEARCHING FOR FACE" to AegisCyan
        is AuthenticationState.QualityCheck -> "ADJUSTING POSITION" to AegisWarning
        is AuthenticationState.PassiveLiveness -> "CHECKING LIVENESS (${(authState.score * 100).toInt()}%)" to AegisCyan
        is AuthenticationState.ActiveChallenge -> "ACTIVE CHALLENGE" to AegisNothingDot
        is AuthenticationState.BioHashGeneration -> "BIOHASH MATCHING" to AegisCyanLight
        is AuthenticationState.Comparison -> "VERIFYING TEMPLATE" to AegisCyan
        is AuthenticationState.Authenticated -> "VERIFIED & UNLOCKED" to AegisSuccess
        is AuthenticationState.Failed -> "AUTHENTICATION FAILED" to AegisCritical
        is AuthenticationState.Locked -> "SYSTEM LOCKED" to AegisCritical
        is AuthenticationState.SystemBiometricFailed -> "BIOMETRIC FAILED" to AegisCritical
        is AuthenticationState.RecoveryFallback -> "USE RECOVERY OPTION" to AegisWarning
        is AuthenticationState.AccessDenied -> "ACCESS DENIED" to AegisCritical
        else -> "INITIALIZING" to AegisTextTertiary
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatusBadge(text = statusText, color = statusColor)

        Text(
            text = "PASSIVE LIVENESS ON",
            style = MaterialTheme.typography.labelSmall,
            color = AegisTextTertiary
        )
    }
}

@Composable
fun AuthenticationHUDOverlay(authState: AuthenticationState) {
    when (authState) {
        is AuthenticationState.ActiveChallenge -> {
            val challengeState = authState.challengeState
            val animatedProgress by animateFloatAsState(
                targetValue = challengeState.progress,
                animationSpec = tween(200),
                label = "challenge_progress"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x33000000)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(240.dp)) {
                    val stroke = 6.dp.toPx()
                    drawCircle(color = Color(0x44D71920), style = Stroke(stroke))
                    drawArc(
                        color = AegisNothingDot,
                        startAngle = -90f,
                        sweepAngle = animatedProgress * 360f,
                        useCenter = false,
                        style = Stroke(stroke, cap = StrokeCap.Round)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = challengeState.challenge.prompt.uppercase(),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = AegisTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${challengeState.remainingSeconds}s remaining",
                        style = MaterialTheme.typography.labelSmall,
                        color = AegisNothingDot
                    )
                }
            }
        }

        is AuthenticationState.Authenticated -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AegisDarkBackground.copy(alpha = 0.90f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(AegisSuccess.copy(alpha = 0.15f))
                            .border(2.dp, AegisSuccess, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LockOpen,
                            contentDescription = "Success",
                            tint = AegisSuccess,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Access Granted",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = AegisTextPrimary
                    )
                    Text(
                        text = "Returning to ${authState.appName}...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AegisTextSecondary
                    )
                }
            }
        }

        is AuthenticationState.Failed -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AegisDarkBackground.copy(alpha = 0.90f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(AegisCritical.copy(alpha = 0.15f))
                            .border(2.dp, AegisCritical, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Failed",
                            tint = AegisCritical,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Access Denied",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = AegisTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = authState.reason,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AegisCritical
                    )
                    if (authState.remainingAttempts > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${authState.remainingAttempts} attempt(s) remaining",
                            style = MaterialTheme.typography.labelSmall,
                            color = AegisTextTertiary
                        )
                    }
                }
            }
        }

        is AuthenticationState.Locked -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AegisDarkBackground.copy(alpha = 0.95f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = "Locked",
                        tint = AegisCritical,
                        modifier = Modifier.size(60.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Security Lockout Active",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = AegisTextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Too many failed biometric attempts. Please try again after ${authState.remainingLockoutMinutes} minutes.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AegisTextSecondary
                    )
                }
            }
        }

        else -> {
            // Default subtle biometric targeting circle
            Canvas(modifier = Modifier.size(240.dp)) {
                drawCircle(
                    color = Color(0x4400E5FF),
                    style = Stroke(2.dp.toPx())
                )
            }
        }
    }
}

@Composable
fun AuthenticationBottomControls(
    authState: AuthenticationState,
    onRetry: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when (authState) {
            is AuthenticationState.Failed -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "FACE AUTHENTICATION FAILED",
                        style = MaterialTheme.typography.labelMedium,
                        color = AegisCritical
                    )
                    Text(
                        text = "Reason: ${authState.reason}",
                        style = MaterialTheme.typography.bodySmall,
                        color = AegisTextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                if (authState.canRetry) {
                    AegisButton(
                        text = "TRY AGAIN",
                        onClick = onRetry,
                        icon = Icons.Default.Refresh,
                        isPrimary = true
                    )
                }
                TextButton(onClick = onCancel) {
                    Text(
                        text = "CANCEL",
                        style = MaterialTheme.typography.labelSmall,
                        color = AegisTextSecondary
                    )
                }
            }

            is AuthenticationState.Locked -> {
                AegisButton(
                    text = "CLOSE",
                    onClick = onCancel,
                    isPrimary = false
                )
            }

            is AuthenticationState.RecoveryFallback,
            is AuthenticationState.AccessDenied -> {
                TextButton(onClick = onCancel) {
                    Text(
                        text = "CANCEL",
                        style = MaterialTheme.typography.labelSmall,
                        color = AegisTextSecondary
                    )
                }
            }

            else -> {
                TextButton(onClick = onCancel) {
                    Text(
                        text = "DISMISS",
                        style = MaterialTheme.typography.labelSmall,
                        color = AegisTextTertiary
                    )
                }
            }
        }
    }
}
