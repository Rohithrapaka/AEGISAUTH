package com.aegisauth.feature.history

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aegisauth.core.theme.*
import com.aegisauth.core.ui.components.AegisCard
import com.aegisauth.core.ui.components.AegisTopBar
import com.aegisauth.core.ui.components.StatusBadge
import com.aegisauth.data.local.entity.AuthenticationEvent
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBackClick: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val events by viewModel.events.collectAsState()
    var selectedEvent by remember { mutableStateOf<AuthenticationEvent?>(null) }

    Scaffold(
        topBar = {
            AegisTopBar(
                title = "Authentication History",
                subtitle = "${events.size} LOGGED SESSIONS",
                onBackClick = onBackClick,
                actions = {
                    if (events.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearAllHistory() }) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear History",
                                tint = AegisTextSecondary
                            )
                        }
                    }
                }
            )
        },
        containerColor = AegisDarkBackground
    ) { padding ->
        if (events.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No authentication records found.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = AegisTextTertiary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(events, key = { it.id }) { event ->
                    HistoryItemCard(
                        event = event,
                        onClick = { selectedEvent = event }
                    )
                }
            }
        }

        // Detailed Audit Bottom Sheet
        if (selectedEvent != null) {
            ModalBottomSheet(
                onDismissRequest = { selectedEvent = null },
                containerColor = AegisDarkSurfaceVariant,
                dragHandle = { BottomSheetDefaults.DragHandle(color = AegisBorderActive) }
            ) {
                EventDetailAuditSheet(
                    event = selectedEvent!!,
                    onLoadEvidence = { path -> viewModel.loadEvidenceBitmap(path) },
                    onDismiss = { selectedEvent = null }
                )
            }
        }
    }
}

@Composable
fun HistoryItemCard(
    event: AuthenticationEvent,
    onClick: () -> Unit
) {
    val isSuccess = event.result == "SUCCESS"
    val timeFormat = SimpleDateFormat("MMM dd, hh:mm:ss a", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(event.timestamp))

    AegisCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
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
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isSuccess) AegisSuccess.copy(alpha = 0.15f) else AegisCritical.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSuccess) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = null,
                        tint = if (isSuccess) AegisSuccess else AegisCritical,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = event.appName.ifBlank { event.packageName },
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                            color = AegisTextPrimary
                        )
                        if (!event.evidencePath.isNullOrBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Evidence Available",
                                tint = AegisCyan,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                        color = AegisTextTertiary
                    )
                }
            }

            StatusBadge(
                text = if (isSuccess) "PASSED" else "REJECTED",
                color = if (isSuccess) AegisSuccess else AegisCritical
            )
        }
    }
}

@Composable
fun EventDetailAuditSheet(
    event: AuthenticationEvent,
    onLoadEvidence: suspend (String) -> Bitmap?,
    onDismiss: () -> Unit
) {
    var evidenceBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoadingEvidence by remember { mutableStateOf(false) }

    LaunchedEffect(event.evidencePath) {
        if (!event.evidencePath.isNullOrBlank()) {
            isLoadingEvidence = true
            evidenceBitmap = onLoadEvidence(event.evidencePath)
            isLoadingEvidence = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Session Audit Trail",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = AegisTextPrimary
        )

        HorizontalDivider(color = AegisBorder)

        AuditDetailRow("Target Application", event.appName.ifBlank { event.packageName })
        AuditDetailRow("Package ID", event.packageName)
        AuditDetailRow("Result State", event.result)
        AuditDetailRow("Passive Liveness Score", "${(event.livenessScore * 100).toInt()}%")
        AuditDetailRow("Quality Index", "${(event.qualityScore * 100).toInt()}%")
        AuditDetailRow("BioHash Similarity", "${(event.similarityScore * 100).toInt()}%")
        AuditDetailRow("Risk Level", event.riskLevel)
        AuditDetailRow("Active Challenge", if (event.challengeRequired) "TRIGGERED (${event.challengeResult ?: "N/A"})" else "BYPASS")
        if (event.failureReason != null) {
            AuditDetailRow("Failure Reason", event.failureReason)
        }

        // Evidence Capture Section
        if (!event.evidencePath.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = AegisBorder)

            Text(
                text = "Captured Probe Evidence (Encrypted Artifact)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = AegisCyan
            )

            if (isLoadingEvidence) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AegisCyan, modifier = Modifier.size(32.dp))
                }
            } else if (evidenceBitmap != null) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, AegisCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .background(AegisDarkSurface)
                    ) {
                        Image(
                            bitmap = evidenceBitmap!!.asImageBitmap(),
                            contentDescription = "Intruder Evidence Frame",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "AES-256-GCM Decrypted Probe Frame (Auto-purges in 7 days)",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = AegisTextTertiary
                    )
                }
            } else {
                Text(
                    text = "Encrypted evidence artifact could not be decrypted or was purged.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AegisTextTertiary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun AuditDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = AegisTextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
            color = AegisTextPrimary
        )
    }
}
