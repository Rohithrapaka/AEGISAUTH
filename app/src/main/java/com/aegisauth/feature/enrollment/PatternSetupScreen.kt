package com.aegisauth.feature.enrollment

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aegisauth.core.security.PasswordSecurityManager
import com.aegisauth.core.security.PatternSecurityManager
import com.aegisauth.core.theme.*
import com.aegisauth.core.ui.components.AegisButton
import com.aegisauth.core.ui.components.AegisTopBar
import com.aegisauth.core.ui.components.PatternLockView
import com.aegisauth.data.repository.ProtectedAppsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PatternSetupScreen(
    onPatternSetupComplete: () -> Unit,
    patternSecurityManager: PatternSecurityManager,
    passwordSecurityManager: PasswordSecurityManager,
    protectedAppsRepository: ProtectedAppsRepository
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(0) } // 0: Password, 1: Pattern

    // Password State
    var passwordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var passwordErrorMessage by remember { mutableStateOf("") }

    // Pattern State
    var resetKey by remember { mutableIntStateOf(0) }
    var isConfirmPatternStep by remember { mutableStateOf(false) }
    var firstPattern by remember { mutableStateOf<List<Int>>(emptyList()) }
    var isPatternError by remember { mutableStateOf(false) }
    var patternStatusMessage by remember { mutableStateOf("Draw a pattern connecting at least 4 dots") }

    Scaffold(
        topBar = {
            AegisTopBar(
                title = "Backup Recovery",
                subtitle = "SECURITY RECOVERY SETUP",
                onBackClick = {
                    if (isConfirmPatternStep) {
                        isConfirmPatternStep = false
                        firstPattern = emptyList()
                        resetKey++
                        patternStatusMessage = "Draw a pattern connecting at least 4 dots"
                    }
                }
            )
        },
        containerColor = AegisDarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(AegisCyan.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Recovery Security",
                        tint = AegisCyan,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Setup Recovery Credential",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = AegisTextPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Used as emergency recovery if biometrics are unavailable.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AegisTextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Mode Selector Tab (Password vs Pattern)
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = AegisDarkSurfaceVariant,
                    contentColor = AegisCyan,
                    modifier = Modifier.clip(CircleShape)
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("PASSWORD", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("PATTERN", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) }
                    )
                }
            }

            // Content Area based on Tab
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (selectedTab == 0) {
                    // Password Setup Form (Default Obscured, Reveal Toggle)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = {
                                passwordInput = it
                                passwordError = false
                            },
                            label = { Text("Enter Backup Password") },
                            placeholder = { Text("Minimum 4 characters") },
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
                            isError = passwordError,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AegisCyan,
                                unfocusedBorderColor = AegisBorderActive,
                                focusedTextColor = AegisTextPrimary,
                                unfocusedTextColor = AegisTextPrimary,
                                errorBorderColor = AegisCritical
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = confirmPasswordInput,
                            onValueChange = {
                                confirmPasswordInput = it
                                passwordError = false
                            },
                            label = { Text("Confirm Backup Password") },
                            singleLine = true,
                            visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isConfirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (isConfirmPasswordVisible) "Hide password" else "Show password",
                                        tint = AegisTextSecondary
                                    )
                                }
                            },
                            isError = passwordError,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AegisCyan,
                                unfocusedBorderColor = AegisBorderActive,
                                focusedTextColor = AegisTextPrimary,
                                unfocusedTextColor = AegisTextPrimary,
                                errorBorderColor = AegisCritical
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (passwordError) {
                            Text(
                                text = passwordErrorMessage,
                                style = MaterialTheme.typography.labelSmall,
                                color = AegisCritical
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        AegisButton(
                            text = "SAVE BACKUP PASSWORD",
                            onClick = {
                                if (passwordInput.length < PasswordSecurityManager.MIN_PASSWORD_LENGTH) {
                                    passwordError = true
                                    passwordErrorMessage = "Password must be at least ${PasswordSecurityManager.MIN_PASSWORD_LENGTH} characters."
                                } else if (passwordInput != confirmPasswordInput) {
                                    passwordError = true
                                    passwordErrorMessage = "Passwords do not match."
                                } else {
                                    coroutineScope.launch {
                                        passwordSecurityManager.savePassword(passwordInput)
                                        // Auto-protect AEGISAUTH itself
                                        protectedAppsRepository.setAppProtection(
                                            packageName = "com.aegisauth",
                                            appName = "AEGISAUTH",
                                            isProtected = true
                                        )
                                        delay(300)
                                        onPatternSetupComplete()
                                    }
                                }
                            },
                            icon = Icons.Default.Check,
                            isPrimary = true
                        )
                    }
                } else {
                    // Pattern Setup View
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = patternStatusMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isPatternError) AegisCritical else AegisTextSecondary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        key(resetKey) {
                            PatternLockView(
                                modifier = Modifier.fillMaxWidth(0.85f),
                                isError = isPatternError,
                                enabled = !isPatternError,
                                onPatternComplete = { pattern ->
                                    if (!isConfirmPatternStep) {
                                        if (pattern.size < 4) {
                                            isPatternError = true
                                            patternStatusMessage = "Connect at least 4 dots"
                                            coroutineScope.launch {
                                                delay(1200)
                                                isPatternError = false
                                                resetKey++
                                                patternStatusMessage = "Draw a pattern connecting at least 4 dots"
                                            }
                                        } else {
                                            firstPattern = pattern
                                            isConfirmPatternStep = true
                                            resetKey++
                                            patternStatusMessage = "Draw the pattern again to confirm"
                                        }
                                    } else {
                                        if (pattern == firstPattern) {
                                            patternStatusMessage = "Pattern confirmed! Securing AEGISAUTH..."
                                            coroutineScope.launch {
                                                patternSecurityManager.savePattern(pattern)
                                                // Auto-protect AEGISAUTH itself
                                                protectedAppsRepository.setAppProtection(
                                                    packageName = "com.aegisauth",
                                                    appName = "AEGISAUTH",
                                                    isProtected = true
                                                )
                                                delay(500)
                                                onPatternSetupComplete()
                                            }
                                        } else {
                                            isPatternError = true
                                            patternStatusMessage = "Patterns do not match. Try again."
                                            coroutineScope.launch {
                                                delay(1500)
                                                isPatternError = false
                                                isConfirmPatternStep = false
                                                firstPattern = emptyList()
                                                resetKey++
                                                patternStatusMessage = "Draw a pattern connecting at least 4 dots"
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Footer
            Text(
                text = "Protected with PBKDF2-HMAC-SHA256 & Android Keystore vault.",
                style = MaterialTheme.typography.labelSmall,
                color = AegisTextTertiary
            )
        }
    }
}
