package com.aegisauth.feature.biometric

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aegisauth.core.theme.*
import com.aegisauth.core.ui.components.AegisButton
import com.aegisauth.core.ui.components.AegisTopBar
import com.aegisauth.data.local.entity.BiometricProfile
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BiometricUpdatesScreen(
    onReEnroll: () -> Unit,
    onReEnrollForProfile: (Long) -> Unit = {},
    onBackClick: () -> Unit,
    viewModel: BiometricUpdatesViewModel = hiltViewModel()
) {
    val profiles by viewModel.profiles.collectAsState()
    val atLimit = profiles.size >= 5
    var showAddDialog by remember { mutableStateOf(false) }
    var showLimitDialog by remember { mutableStateOf(false) }
    var addName by remember { mutableStateOf("") }
    var selectedProfile by remember { mutableStateOf<BiometricProfile?>(null) }

    if (selectedProfile != null) {
        ProfileDetailScreen(
            profile = selectedProfile!!,
            viewModel = viewModel,
            onReEnroll = { onReEnrollForProfile(selectedProfile!!.id) },
            onBack = { selectedProfile = null }
        )
        return
    }

    Scaffold(
        topBar = {
            AegisTopBar(
                title = "Biometric Management",
                subtitle = "BIOHASH CREDENTIALS",
                onBackClick = onBackClick
            )
        },
        containerColor = AegisDarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            val context = androidx.compose.ui.platform.LocalContext.current
            val hasDeviceBiometric = remember(context) {
                viewModel.systemBiometricManager.isSystemBiometricAvailable(context)
            }
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(profiles.size, key = { profiles[it].id }) { i ->
                    val profile = profiles[i]
                    ProfileListItem(
                        profile = profile,
                        index = i + 1,
                        hasTemplate = viewModel.hasTemplateForProfile(profile.id),
                        hasDeviceBiometric = hasDeviceBiometric,
                        onClick = { selectedProfile = profile }
                    )
                }

                item {
                    if (atLimit) {
                        Text(
                            text = "Maximum of 5 biometric profiles reached.",
                            style = MaterialTheme.typography.labelSmall,
                            color = AegisWarning,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AegisButton(
                text = "+ ADD NEW BIO",
                onClick = {
                    if (atLimit) {
                        showLimitDialog = true
                    } else {
                        addName = ""
                        showAddDialog = true
                    }
                },
                icon = Icons.Default.PersonAdd,
                isPrimary = true,
                enabled = true
            )
        }
    }

    // Add New BIO dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = AegisDarkSurfaceVariant,
            title = {
                Text(
                    text = "Add New Biometric Profile",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = AegisTextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter a name for this biometric profile.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AegisTextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = addName,
                        onValueChange = { addName = it },
                        label = { Text("Profile Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AegisCyan,
                            unfocusedBorderColor = AegisBorderActive,
                            focusedTextColor = AegisTextPrimary,
                            unfocusedTextColor = AegisTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("CANCEL", color = AegisTextSecondary)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val name = addName.trim().ifBlank { "BIO ${profiles.size + 1}" }
                        viewModel.addProfile(name) { newId ->
                            showAddDialog = false
                            onReEnrollForProfile(newId)
                        }
                    }
                ) {
                    Text("CONTINUE", color = AegisCyan, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Limit dialog
    if (showLimitDialog) {
        AlertDialog(
            onDismissRequest = { showLimitDialog = false },
            containerColor = AegisDarkSurfaceVariant,
            title = {
                Text("Profile Limit Reached", color = AegisTextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Maximum of 5 biometric profiles reached.",
                    color = AegisTextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { showLimitDialog = false }) {
                    Text("OK", color = AegisCyan)
                }
            }
        )
    }
}

@Composable
fun ProfileListItem(
    profile: BiometricProfile,
    index: Int,
    hasTemplate: Boolean,
    hasDeviceBiometric: Boolean,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = AegisDarkSurfaceVariant,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(AegisCyan.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Face,
                    contentDescription = null,
                    tint = AegisCyan,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "BIO $index",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
                        color = AegisCyan
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = profile.displayName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = AegisTextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                val methodLabel = buildString {
                    if (hasTemplate) append("FACE")
                    if (hasDeviceBiometric) {
                        if (isNotEmpty()) append(" • ")
                        append("DEVICE BIOMETRIC")
                    }
                    if (isEmpty()) append("NO BIOMETRIC ENROLLED")
                }
                Text(
                    text = "$methodLabel • ACTIVE",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (hasTemplate || hasDeviceBiometric) AegisSuccess else AegisWarning
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open",
                tint = AegisTextTertiary
            )
        }
    }
}

@Composable
fun MetadataRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
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
