package com.aegisauth.feature.dashboard

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.aegisauth.core.theme.*
import com.aegisauth.core.ui.components.AegisButton
import com.aegisauth.core.ui.components.AegisCard
import com.aegisauth.core.ui.components.PulseDot
import com.aegisauth.core.ui.components.StatusBadge
import com.aegisauth.data.local.entity.AuthenticationEvent
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToProtectedApps: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToAlerts: () -> Unit,
    onNavigateToDeviceSecurity: () -> Unit,
    onNavigateToBiometricUpdates: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onTestAuthentication: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    LifecycleResumeEffect(Unit) {
        viewModel.checkAccessibilityState()
        onPauseOrDispose { }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (uiState.isServiceRunning) AegisCyan else AegisNothingDot)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AEGISAUTH",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            ),
                            color = AegisTextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToAlerts) {
                        BadgedBox(
                            badge = {
                                if (uiState.unreadAlertsCount > 0) {
                                    Badge(
                                        containerColor = AegisNothingDot,
                                        contentColor = AegisDarkBackground
                                    ) {
                                        Text(uiState.unreadAlertsCount.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Alerts",
                                tint = AegisTextPrimary
                            )
                        }
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = AegisTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = AegisDarkBackground
                )
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
            // Hero Status & Master Switch Card
            item {
                val isAccessibilityMissing = !uiState.isAccessibilityEnabledInSettings
                val isDisconnected = uiState.isAccessibilityEnabledInSettings &&
                        uiState.serviceRuntimeState == com.aegisauth.data.repository.ServiceRuntimeState.PERMISSION_GRANTED_SERVICE_DISCONNECTED
                val isServiceActive = uiState.isServiceRunning && !isDisconnected

                AegisCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (isAccessibilityMissing || isDisconnected) {
                                Modifier.clickable {
                                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    context.startActivity(intent)
                                }
                            } else Modifier
                        ),
                    backgroundColor = AegisDarkSurfaceVariant,
                    borderColor = when {
                        isAccessibilityMissing -> AegisWarning.copy(alpha = 0.6f)
                        isDisconnected -> AegisCritical.copy(alpha = 0.6f)
                        isServiceActive -> AegisCyan.copy(alpha = 0.4f)
                        else -> AegisBorder
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                when {
                                    isAccessibilityMissing -> {
                                        StatusBadge(text = "SERVICE PERMISSION REQUIRED", color = AegisWarning)
                                    }
                                    isDisconnected -> {
                                        StatusBadge(text = "SERVICE NOT RUNNING", color = AegisCritical)
                                    }
                                    isServiceActive -> {
                                        PulseDot(color = AegisSuccess)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "INTERCEPTION ACTIVE",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = AegisSuccess,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                    else -> {
                                        Text(
                                            text = "PROTECTION PAUSED",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = AegisWarning,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = when {
                                    isAccessibilityMissing -> "Interception Inactive"
                                    isDisconnected -> "Service Enabled — Service Not Running"
                                    isServiceActive -> "Active Biometric Shield"
                                    else -> "Guard Service Disabled"
                                },
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = AegisTextPrimary
                            )

                            Text(
                                text = when {
                                    isAccessibilityMissing -> "Tap here to enable AEGISAUTH in Android Accessibility Settings"
                                    isDisconnected -> "System service was unbound or stopped by OS. Tap to toggle in Settings."
                                    isServiceActive -> "${uiState.protectedAppsCount} applications under real-time interception"
                                    else -> "Toggle switch to activate interception"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = when {
                                    isAccessibilityMissing -> AegisWarning
                                    isDisconnected -> AegisCritical
                                    else -> AegisTextSecondary
                                }
                            )
                        }

                        if (!isAccessibilityMissing && !isDisconnected) {
                            Switch(
                                checked = uiState.isProtectionEnabled,
                                onCheckedChange = { viewModel.toggleProtectionService(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = AegisDarkBackground,
                                    checkedTrackColor = AegisCyan,
                                    uncheckedThumbColor = AegisTextTertiary,
                                    uncheckedTrackColor = AegisDarkSurfaceElevated
                                )
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Open Settings",
                                tint = if (isDisconnected) AegisCritical else AegisWarning
                            )
                        }
                    }
                }
            }

            // Quick Navigation Hub (2x2 Grid style)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardNavTile(
                        title = "Protected Apps",
                        subtitle = "${uiState.protectedAppsCount} Configured",
                        icon = Icons.Default.Apps,
                        color = AegisCyan,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToProtectedApps
                    )
                    DashboardNavTile(
                        title = "Device Health",
                        subtitle = uiState.securityStatus,
                        icon = Icons.Default.Security,
                        color = if (uiState.securityStatus == "SECURE") AegisSuccess else AegisWarning,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToDeviceSecurity
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardNavTile(
                        title = "Biometrics",
                        subtitle = if (uiState.isBiometricEnrolled) "Template Enrolled" else "Not Configured",
                        icon = Icons.Default.Fingerprint,
                        color = AegisCyanLight,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToBiometricUpdates
                    )
                    DashboardNavTile(
                        title = "Audit History",
                        subtitle = "View Logs",
                        icon = Icons.Default.History,
                        color = AegisTextSecondary,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToHistory
                    )
                }
            }

            // Test Authentication Live Pipeline Banner
            item {
                AegisButton(
                    text = "TEST AUTHENTICATION PIPELINE",
                    onClick = onTestAuthentication,
                    icon = Icons.Default.LockOpen,
                    isPrimary = false
                )
            }

            // Recent Biometric Activity Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECENT ACTIVITY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = AegisTextSecondary
                    )
                    TextButton(onClick = onNavigateToHistory) {
                        Text(
                            text = "SEE ALL",
                            style = MaterialTheme.typography.labelSmall,
                            color = AegisCyan
                        )
                    }
                }
            }

            // Activity List
            if (uiState.recentEvents.isEmpty()) {
                item {
                    AegisCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "No recent authentication events recorded yet. Try opening a protected app or testing the pipeline above.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AegisTextTertiary
                        )
                    }
                }
            } else {
                items(uiState.recentEvents) { event ->
                    RecentActivityRow(event = event)
                }
            }
        }
    }
}

@Composable
fun DashboardNavTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, AegisBorder, RoundedCornerShape(20.dp))
            .clickable { onClick() },
        color = AegisDarkSurface
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                    color = AegisTextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                    color = AegisTextSecondary
                )
            }
        }
    }
}

@Composable
fun RecentActivityRow(event: AuthenticationEvent) {
    val isSuccess = event.result == "SUCCESS"
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(event.timestamp))

    AegisCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = AegisDarkSurface
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isSuccess) AegisSuccess.copy(alpha = 0.15f) else AegisCritical.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSuccess) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = null,
                        tint = if (isSuccess) AegisSuccess else AegisCritical,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = event.appName.ifBlank { event.packageName },
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                        color = AegisTextPrimary
                    )
                    Text(
                        text = if (isSuccess) "Authenticated • ${(event.livenessScore * 100).toInt()}% Liveness" else (event.failureReason ?: "Authentication Failed"),
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                        color = if (isSuccess) AegisTextSecondary else AegisCritical
                    )
                }
            }

            Text(
                text = formattedTime,
                style = MaterialTheme.typography.labelSmall.copy(color = AegisTextTertiary)
            )
        }
    }
}
