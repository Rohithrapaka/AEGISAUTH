package com.aegisauth.feature.biometric

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import android.provider.Settings
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import com.aegisauth.core.theme.*
import com.aegisauth.core.ui.components.AegisButton
import com.aegisauth.core.ui.components.AegisCard
import com.aegisauth.core.ui.components.AegisTopBar
import com.aegisauth.data.local.entity.BiometricProfile
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProfileDetailScreen(
    profile: BiometricProfile,
    viewModel: BiometricUpdatesViewModel,
    onReEnroll: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val hasTemplate = viewModel.hasTemplateForProfile(profile.id)
    val isBiometricEnrolled = remember(context) {
        viewModel.systemBiometricManager.isSystemBiometricAvailable(context)
    }
    var biometricVerifyStatus by remember { mutableStateOf<String?>(null) }
    var showDeleteBioDialog by remember { mutableStateOf(false) }
    var showDeleteProfileDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameInput by remember { mutableStateOf(profile.displayName) }
    val dateFormat = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())

    Scaffold(
        topBar = {
            AegisTopBar(
                title = profile.displayName,
                subtitle = "BIOMETRIC PROFILE DETAIL",
                onBackClick = onBack
            )
        },
        containerColor = AegisDarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Face section
            AegisCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "FACE BIOMETRIC",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                    color = AegisTextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (hasTemplate) {
                    MetadataRow("Status", "BioHash Active")
                    MetadataRow("Enrolled", dateFormat.format(Date(profile.createdAt)))
                } else {
                    Text(
                        text = "No face template enrolled for this profile.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AegisWarning
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                AegisButton(
                    text = if (hasTemplate) "RE-ENROLL FACE" else "ENROLL FACE",
                    onClick = onReEnroll,
                    icon = Icons.Default.Refresh,
                    isPrimary = true
                )
                if (hasTemplate) {
                    Spacer(modifier = Modifier.height(8.dp))
                    AegisButton(
                        text = "DELETE FACE TEMPLATE",
                        onClick = { showDeleteBioDialog = true },
                        icon = Icons.Default.DeleteForever,
                        isPrimary = false
                    )
                }
            }

            // Device Biometric section
            AegisCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "DEVICE BIOMETRIC",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                    color = AegisTextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (isBiometricEnrolled) {
                    MetadataRow("Status", "✓ Android System Biometric Enrolled")
                    if (biometricVerifyStatus != null) {
                        Text(
                            text = biometricVerifyStatus!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (biometricVerifyStatus!!.contains("Verified", ignoreCase = true)) AegisSuccess else AegisCritical
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    AegisButton(
                        text = "VERIFY FINGERPRINT",
                        onClick = {
                            val activity = context as? FragmentActivity
                            if (activity != null) {
                                viewModel.systemBiometricManager.authenticate(
                                    activity = activity,
                                    title = "Verify Fingerprint",
                                    subtitle = "Confirm device biometric for ${profile.displayName}"
                                ) { result ->
                                    biometricVerifyStatus = when (result) {
                                        is com.aegisauth.core.biometric.SystemBiometricManager.BiometricPromptResult.Success ->
                                            "✓ Fingerprint Verified Successfully"
                                        is com.aegisauth.core.biometric.SystemBiometricManager.BiometricPromptResult.Failed ->
                                            "Fingerprint not recognized"
                                        is com.aegisauth.core.biometric.SystemBiometricManager.BiometricPromptResult.UserCanceled ->
                                            null
                                        else -> "Biometric verification error"
                                    }
                                }
                            }
                        },
                        icon = Icons.Default.Fingerprint,
                        isPrimary = false
                    )
                } else {
                    Text(
                        text = "Not enrolled in Android System",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AegisWarning
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AegisButton(
                        text = "ENROLL FINGERPRINT",
                        onClick = {
                            try {
                                val enrollIntent = Intent(Settings.ACTION_SECURITY_SETTINGS)
                                context.startActivity(enrollIntent)
                            } catch (e: Exception) {
                                val genericIntent = Intent(Settings.ACTION_SETTINGS)
                                context.startActivity(genericIntent)
                            }
                        },
                        icon = Icons.Default.Fingerprint,
                        isPrimary = false
                    )
                }
            }

            // Profile management section
            AegisCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "PROFILE MANAGEMENT",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                    color = AegisTextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                AegisButton(
                    text = "RENAME PROFILE",
                    onClick = {
                        renameInput = profile.displayName
                        showRenameDialog = true
                    },
                    icon = Icons.Default.Edit,
                    isPrimary = false
                )
                Spacer(modifier = Modifier.height(8.dp))
                AegisButton(
                    text = "DELETE PROFILE",
                    onClick = { showDeleteProfileDialog = true },
                    icon = Icons.Default.Delete,
                    isPrimary = false
                )
            }
        }
    }

    // Delete face template dialog
    if (showDeleteBioDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteBioDialog = false },
            containerColor = AegisDarkSurfaceVariant,
            title = {
                Text(
                    "Delete Face Template?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = AegisTextPrimary
                )
            },
            text = {
                Text(
                    "This will permanently erase the BioHash template for '${profile.displayName}'.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AegisTextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteBioDialog = false
                    viewModel.deleteFaceTemplate(profile.id, onBack)
                }) { Text("DELETE", color = AegisCritical, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteBioDialog = false }) {
                    Text("CANCEL", color = AegisTextSecondary)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Delete profile dialog
    if (showDeleteProfileDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteProfileDialog = false },
            containerColor = AegisDarkSurfaceVariant,
            title = {
                Text(
                    "Delete Profile?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = AegisTextPrimary
                )
            },
            text = {
                Text(
                    "This will permanently delete the biometric profile '${profile.displayName}' and its face template.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AegisTextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteProfileDialog = false
                    viewModel.deleteProfile(profile.id, onBack)
                }) { Text("DELETE PROFILE", color = AegisCritical, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteProfileDialog = false }) {
                    Text("CANCEL", color = AegisTextSecondary)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Rename dialog
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            containerColor = AegisDarkSurfaceVariant,
            title = {
                Text(
                    "Rename Profile",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = AegisTextPrimary
                )
            },
            text = {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
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
            },
            confirmButton = {
                TextButton(onClick = {
                    val name = renameInput.trim().ifBlank { profile.displayName }
                    viewModel.renameProfile(profile.id, name)
                    showRenameDialog = false
                }) { Text("SAVE", color = AegisCyan, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("CANCEL", color = AegisTextSecondary)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}
