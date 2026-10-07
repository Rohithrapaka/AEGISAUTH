package com.aegisauth.feature.security

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aegisauth.core.theme.*
import com.aegisauth.core.ui.components.AegisButton
import com.aegisauth.core.ui.components.AegisCard
import com.aegisauth.core.ui.components.AegisTopBar
import com.aegisauth.core.ui.components.StatusBadge

@Composable
fun DeviceSecurityScreen(
    onBackClick: () -> Unit,
    viewModel: DeviceSecurityViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val overallColor = when (state.overallStatus) {
        "CRITICAL" -> AegisCritical
        "WARNING" -> AegisWarning
        else -> AegisSuccess
    }

    Scaffold(
        topBar = {
            AegisTopBar(
                title = "Device Security",
                subtitle = "INTEGRITY AUDIT",
                onBackClick = onBackClick,
                actions = {
                    IconButton(onClick = { viewModel.performSecurityAudit() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = AegisCyan
                        )
                    }
                }
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
            // Overall Status Banner
            item {
                AegisCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = AegisDarkSurfaceVariant,
                    borderColor = overallColor.copy(alpha = 0.4f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            StatusBadge(text = state.overallStatus, color = overallColor)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (state.overallStatus == "SECURE") "Enclave Protection Optimal" else "Attention Recommended",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = AegisTextPrimary
                            )
                            Text(
                                text = "Continuous inspection of hardware keys and service posture",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AegisTextSecondary
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "SECURITY CHECKS",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold),
                    color = AegisTextSecondary
                )
            }

            items(state.checks) { check ->
                SecurityCheckRow(check = check)
            }

            // Button to open accessibility settings if service is disabled
            val accessibilityCheck = state.checks.firstOrNull { it.title.contains("App Protection") }
            if (accessibilityCheck != null && !accessibilityCheck.isSecure) {
                item {
                    AegisButton(
                        text = "ENABLE ACCESSIBILITY SERVICE",
                        onClick = {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            context.startActivity(intent)
                        },
                        isPrimary = true
                    )
                }
            }
        }
    }
}

@Composable
fun SecurityCheckRow(check: SecurityCheckItem) {
    val statusColor = if (check.isSecure) AegisSuccess else if (check.isCritical) AegisCritical else AegisWarning

    AegisCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = AegisDarkSurface
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (check.isSecure) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = check.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                        color = AegisTextPrimary
                    )
                    StatusBadge(text = check.statusText, color = statusColor)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = check.description,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                    color = AegisTextSecondary
                )
            }
        }
    }
}
