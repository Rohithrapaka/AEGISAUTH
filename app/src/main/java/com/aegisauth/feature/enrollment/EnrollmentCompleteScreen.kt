package com.aegisauth.feature.enrollment

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aegisauth.core.theme.*
import com.aegisauth.core.ui.components.AegisButton
import com.aegisauth.core.ui.components.AegisCard

@Composable
fun EnrollmentCompleteScreen(
    captureQuality: Int = 94,
    faceConsistency: Int = 91,
    lightingQuality: Int = 88,
    poseConsistency: Int = 96,
    overallConfidence: Int = 93,
    onNavigateToDashboard: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AegisDarkBackground)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Success Icon with animated rings
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(90.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(AegisSuccess.copy(alpha = 0.12f))
                        .border(1.5.dp, AegisSuccess, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Success",
                        tint = AegisSuccess,
                        modifier = Modifier.size(42.dp)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "BIOMETRIC ENROLLMENT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 2.sp,
                        color = AegisSuccess,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Your biometric profile is ready.",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = AegisTextPrimary
                )
            }

            // Enrollment Quality Assessment Metrics Card
            AegisCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = AegisDarkSurfaceVariant,
                borderColor = AegisCyan.copy(alpha = 0.35f)
            ) {
                Text(
                    text = "ENROLLMENT QUALITY ASSESSMENT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = AegisCyan
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                QualityMetricRow("Capture Quality", captureQuality)
                QualityMetricRow("Face Consistency", faceConsistency)
                QualityMetricRow("Lighting Quality", lightingQuality)
                QualityMetricRow("Pose Consistency", poseConsistency)

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = AegisBorder
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Overall Enrollment Confidence",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = AegisTextPrimary
                        )
                        Text(
                            text = "Derived from 5 validated frames",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                            color = AegisTextSecondary
                        )
                    }
                    Text(
                        text = "$overallConfidence%",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (overallConfidence >= 80) AegisSuccess else AegisWarning
                        )
                    )
                }
            }

            // Key Security Details Card
            AegisCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = AegisCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Hardware Encrypted Template",
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                            color = AegisTextPrimary
                        )
                        Text(
                            text = "Orthonormal BioHash (128-bit) protected with AES-256-GCM in Android Keystore enclave. Raw frames discarded.",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                            color = AegisTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            AegisButton(
                text = "OPEN DASHBOARD",
                onClick = onNavigateToDashboard,
                isPrimary = true
            )
        }
    }
}

@Composable
fun QualityMetricRow(label: String, percentage: Int) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = AegisTextSecondary
            )
            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                ),
                color = AegisTextPrimary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { (percentage / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = if (percentage >= 80) AegisCyan else AegisWarning,
            trackColor = AegisDarkSurfaceElevated
        )
    }
}

