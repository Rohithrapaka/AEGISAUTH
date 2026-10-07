package com.aegisauth.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aegisauth.core.theme.*
import com.aegisauth.core.ui.components.AegisCard
import com.aegisauth.core.ui.components.AegisTopBar

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            AegisTopBar(
                title = "Settings",
                subtitle = "SECURITY POLICIES",
                onBackClick = onBackClick
            )
        },
        containerColor = AegisDarkBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Biometric Sensitivity & Liveness Policy Card
            item {
                Text(
                    text = "BIOMETRIC RECOGNITION POLICIES",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold),
                    color = AegisTextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                AegisCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Passive Liveness Sensitivity",
                                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                                    color = AegisTextPrimary
                                )
                                Text(
                                    text = "${(state.livenessSensitivity * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AegisCyan
                                )
                            }
                            Text(
                                text = "Strictness of temporal micro-motion & eye dynamic verification",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                color = AegisTextTertiary
                            )
                            Slider(
                                value = state.livenessSensitivity,
                                onValueChange = { viewModel.setLivenessSensitivity(it) },
                                valueRange = 0.40f..0.85f,
                                colors = SliderDefaults.colors(
                                    thumbColor = AegisCyan,
                                    activeTrackColor = AegisCyan,
                                    inactiveTrackColor = AegisBorderActive
                                )
                            )
                        }

                        HorizontalDivider(color = AegisBorder)

                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "BioHash Match Threshold",
                                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                                    color = AegisTextPrimary
                                )
                                Text(
                                    text = "${(state.similarityThreshold * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AegisCyan
                                )
                            }
                            Text(
                                text = "Minimum biometric similarity required to allow application unlock",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                color = AegisTextTertiary
                            )
                            Slider(
                                value = state.similarityThreshold,
                                onValueChange = { viewModel.setSimilarityThreshold(it) },
                                valueRange = 0.55f..0.85f,
                                colors = SliderDefaults.colors(
                                    thumbColor = AegisCyan,
                                    activeTrackColor = AegisCyan,
                                    inactiveTrackColor = AegisBorderActive
                                )
                            )
                        }
                    }
                }
            }

            // Challenge & Lockout Behavior Card
            item {
                Text(
                    text = "ACTIVE CHALLENGE & LOCKOUT",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold),
                    color = AegisTextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                AegisCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column {
                            Text(
                                text = "Active Challenge Behavior",
                                style = MaterialTheme.typography.titleLarge.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                                color = AegisTextPrimary
                            )
                            Text(
                                text = "When to prompt user for head turns, blinks, or smile gestures",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                color = AegisTextTertiary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("ADAPTIVE", "ALWAYS", "NEVER").forEach { mode ->
                                    val isSelected = state.activeChallengeMode == mode
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.setActiveChallengeMode(mode) },
                                        label = { Text(mode, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = AegisCyan,
                                            selectedLabelColor = AegisDarkBackground,
                                            containerColor = AegisDarkSurfaceElevated,
                                            labelColor = AegisTextSecondary
                                        )
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = AegisBorder)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Max Failed Attempts",
                                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                                    color = AegisTextPrimary
                                )
                                Text(
                                    text = "Lockout triggered after ${state.maxFailedAttempts} consecutive failures",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                    color = AegisTextTertiary
                                )
                            }
                            Text(
                                text = "${state.maxFailedAttempts}",
                                style = MaterialTheme.typography.headlineMedium.copy(color = AegisCyan)
                            )
                        }

                        HorizontalDivider(color = AegisBorder)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Unlock Grace Period",
                                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                                    color = AegisTextPrimary
                                )
                                Text(
                                    text = "Allow re-opening apps within ${state.unlockGracePeriodSecs} seconds without re-scan",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                    color = AegisTextTertiary
                                )
                            }
                            Text(
                                text = "${state.unlockGracePeriodSecs}s",
                                style = MaterialTheme.typography.headlineMedium.copy(color = AegisCyan)
                            )
                        }
                    }
                }
            }

            // System Preferences
            item {
                Text(
                    text = "SYSTEM & FEEDBACK",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold),
                    color = AegisTextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                AegisCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Tactile Haptic Feedback",
                                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                                    color = AegisTextPrimary
                                )
                                Text(
                                    text = "Vibrate on successful recognition or challenge pass",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                    color = AegisTextTertiary
                                )
                            }
                            Switch(
                                checked = state.hapticFeedbackEnabled,
                                onCheckedChange = { viewModel.setHapticFeedback(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = AegisDarkBackground,
                                    checkedTrackColor = AegisCyan,
                                    uncheckedThumbColor = AegisTextTertiary,
                                    uncheckedTrackColor = AegisDarkSurfaceElevated
                                )
                            )
                        }

                        HorizontalDivider(color = AegisBorder)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Security Notification Alerts",
                                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                                    color = AegisTextPrimary
                                )
                                Text(
                                    text = "Notify when high-risk or repeated failed attempts occur",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                    color = AegisTextTertiary
                                )
                            }
                            Switch(
                                checked = state.notificationsEnabled,
                                onCheckedChange = { viewModel.setNotifications(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = AegisDarkBackground,
                                    checkedTrackColor = AegisCyan,
                                    uncheckedThumbColor = AegisTextTertiary,
                                    uncheckedTrackColor = AegisDarkSurfaceElevated
                                )
                            )
                        }
                    }
                }
            }

            // About AEGISAUTH
            item {
                AegisCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = AegisDarkSurface
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "AEGISAUTH v1.0.0",
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                            color = AegisTextPrimary
                        )
                        Text(
                            text = "Anti-Spoofing Biometric Authentication for Software Systems: Liveness Detection and Template Protection.",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                            color = AegisTextSecondary
                        )
                    }
                }
            }
        }
    }
}
