package com.aegisauth.feature.enrollment

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
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
import com.aegisauth.core.ui.components.AegisCard
import com.aegisauth.core.ui.components.AegisTopBar
import com.aegisauth.core.ui.components.StatusBadge
import com.aegisauth.domain.model.EnrollmentMetrics
import com.aegisauth.domain.model.QualityFeedback
import java.util.concurrent.Executors

@Composable
fun FaceEnrollmentScreen(
    onEnrollmentComplete: (EnrollmentMetrics) -> Unit,
    onBackClick: (() -> Unit)? = null,
    viewModel: EnrollmentViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()

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

    LaunchedEffect(uiState) {
        if (uiState is EnrollmentUiState.Success) {
            val metrics = (uiState as EnrollmentUiState.Success).metrics
            onEnrollmentComplete(metrics)
        }
    }

    Scaffold(
        topBar = {
            AegisTopBar(
                title = "Face Registration",
                subtitle = "BIOHASH ENROLLMENT",
                onBackClick = onBackClick
            )
        },
        containerColor = AegisDarkBackground
    ) { padding ->
        if (!hasCameraPermission) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Camera Permission Required",
                        style = MaterialTheme.typography.headlineMedium,
                        color = AegisTextPrimary
                    )
                    Text(
                        text = "AEGISAUTH requires front camera access to perform facial detection, passive liveness analysis, and generate your protected BioHash template.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AegisTextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AegisButton(
                        text = "GRANT CAMERA ACCESS",
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Status Badge & Instruction derived directly from Diagnostic State
                val scanningState = uiState as? EnrollmentUiState.Scanning
                val diagnostics = scanningState?.diagnostics
                val quality = scanningState?.quality
                val feedback = quality?.feedback ?: QualityFeedback.NO_FACE_DETECTED

                val (badgeText, badgeColor) = when {
                    diagnostics == null -> Pair("INITIALIZING", AegisTextTertiary)
                    feedback == QualityFeedback.NO_FACE_DETECTED -> Pair("NO FACE DETECTED", AegisWarning)
                    !diagnostics.qualityValid && feedback == QualityFeedback.TOO_DARK -> Pair("LOW LIGHT", AegisCritical)
                    !diagnostics.qualityValid && feedback == QualityFeedback.TOO_BRIGHT -> Pair("OVEREXPOSED", AegisCritical)
                    !diagnostics.qualityValid && feedback == QualityFeedback.IMAGE_BLURRY -> Pair("BLURRY FRAME", AegisWarning)
                    !diagnostics.qualityValid && (feedback == QualityFeedback.FACE_TOO_FAR || feedback == QualityFeedback.FACE_TOO_SMALL) -> Pair("MOVE CLOSER", AegisWarning)
                    !diagnostics.qualityValid && feedback == QualityFeedback.FACE_TOO_CLOSE -> Pair("MOVE BACK", AegisWarning)
                    !diagnostics.poseValid -> Pair("POSE: ${diagnostics.expectedPose}", AegisCyan)
                    diagnostics.stabilizationComplete -> Pair("RECORDING SAMPLE", AegisSuccess)
                    else -> Pair("HOLD POSITION", AegisSuccess)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusBadge(text = badgeText, color = badgeColor)

                    val sampleCount = scanningState?.sampleCount ?: 1
                    val maxSamples = scanningState?.maxSamples ?: 7
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AegisDarkSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, AegisBorder)
                    ) {
                        Text(
                            text = "SAMPLE $sampleCount / $maxSamples",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = AegisCyan
                        )
                    }
                }

                // Camera Preview Container with Facial Target Overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // CameraX View
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

                    // Reticle Circular Progress & Guidance Box
                    val progress = scanningState?.progress ?: 0f
                    val animatedProgress by animateFloatAsState(
                        targetValue = progress,
                        animationSpec = tween(300, easing = FastOutSlowInEasing),
                        label = "enrollment_progress"
                    )

                    Canvas(
                        modifier = Modifier
                            .size(260.dp)
                    ) {
                        val strokeWidth = 5.dp.toPx()
                        // Background track
                        drawCircle(
                            color = Color(0x3300E5FF),
                            style = Stroke(strokeWidth)
                        )
                        // Active Progress Arc
                        drawArc(
                            color = AegisCyan,
                            startAngle = -90f,
                            sweepAngle = animatedProgress * 360f,
                            useCenter = false,
                            style = Stroke(strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    if (uiState is EnrollmentUiState.Processing) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(AegisDarkBackground.copy(alpha = 0.85f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    color = AegisCyan,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "GENERATING BIOHASH TEMPLATE...",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AegisCyan
                                )
                            }
                        }
                    }
                }

                // Instruction & Metrics Bar
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val dynamicText = scanningState?.userInstruction ?: "Center your face inside the targeting reticle"
                    Text(
                        text = dynamicText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = AegisTextPrimary
                    )

                    // Error recovery button if state is Error
                    if (uiState is EnrollmentUiState.Error) {
                        val errorMsg = (uiState as EnrollmentUiState.Error).message
                        Text(
                            text = errorMsg,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AegisCritical
                        )
                        AegisButton(
                            text = "RETRY ENROLLMENT",
                            onClick = { viewModel.retryEnrollment() },
                            icon = Icons.Default.Refresh,
                            isPrimary = false
                        )
                    }

                    // Low Quality Assessment Recovery Card
                    if (uiState is EnrollmentUiState.LowQuality) {
                        val lowQ = uiState as EnrollmentUiState.LowQuality
                        AegisCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = AegisDarkSurfaceVariant,
                            borderColor = AegisWarning
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "ENROLLMENT QUALITY LOW",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = AegisWarning,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = lowQ.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AegisTextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                AegisButton(
                                    text = "RETRY ENROLLMENT",
                                    onClick = { viewModel.retryEnrollment() },
                                    icon = Icons.Default.Refresh,
                                    isPrimary = true
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
