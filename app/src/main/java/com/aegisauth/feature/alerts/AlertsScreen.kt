package com.aegisauth.feature.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aegisauth.core.theme.*
import com.aegisauth.core.ui.components.AegisCard
import com.aegisauth.core.ui.components.AegisTopBar
import com.aegisauth.core.ui.components.StatusBadge
import com.aegisauth.data.local.entity.SecurityAlert
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AlertsScreen(
    onBackClick: () -> Unit,
    viewModel: AlertsViewModel = hiltViewModel()
) {
    val alerts by viewModel.alerts.collectAsState()
    val unreadCount = alerts.count { !it.isRead }

    Scaffold(
        topBar = {
            AegisTopBar(
                title = "Security Alerts",
                subtitle = if (unreadCount > 0) "$unreadCount UNREAD ALERTS" else "ALL CLEAR",
                onBackClick = onBackClick,
                actions = {
                    if (alerts.isNotEmpty()) {
                        IconButton(onClick = { viewModel.markAllAsRead() }) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Mark All Read",
                                tint = AegisCyan
                            )
                        }
                    }
                }
            )
        },
        containerColor = AegisDarkBackground
    ) { padding ->
        if (alerts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = AegisSuccess,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Security Alerts",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = AegisTextPrimary
                    )
                    Text(
                        text = "Your device protection and biometric environment are clean.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AegisTextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(alerts, key = { it.id }) { alert ->
                    SecurityAlertItem(
                        alert = alert,
                        onMarkRead = { viewModel.markAsRead(alert.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun SecurityAlertItem(
    alert: SecurityAlert,
    onMarkRead: () -> Unit
) {
    val (severityColor, icon) = when (alert.severity) {
        "CRITICAL" -> AegisCritical to Icons.Default.Warning
        "WARNING" -> AegisWarning to Icons.Default.WarningAmber
        else -> AegisCyan to Icons.Default.Info
    }

    val timeFormat = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(alert.timestamp))

    AegisCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = if (!alert.isRead) AegisDarkSurfaceVariant else AegisDarkSurface,
        borderColor = if (!alert.isRead) severityColor.copy(alpha = 0.4f) else AegisBorder
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(severityColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = severityColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusBadge(text = alert.severity, color = severityColor)
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelSmall.copy(color = AegisTextTertiary)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = alert.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                    color = AegisTextPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = alert.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AegisTextSecondary
                )

                if (!alert.isRead) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = onMarkRead,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "MARK AS READ",
                            style = MaterialTheme.typography.labelSmall.copy(color = AegisCyan)
                        )
                    }
                }
            }
        }
    }
}
