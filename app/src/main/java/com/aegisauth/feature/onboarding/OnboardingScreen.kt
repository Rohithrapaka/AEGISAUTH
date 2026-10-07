package com.aegisauth.feature.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aegisauth.core.theme.*
import com.aegisauth.core.ui.components.AegisButton

@Composable
fun OnboardingScreen(
    onNavigateToEnrollment: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val currentPage by viewModel.currentPage.collectAsState()
    val pages = viewModel.pages
    val page = pages[currentPage]

    Scaffold(
        containerColor = AegisDarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header with Tag and Skip button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AegisDarkSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AegisBorder)
                ) {
                    Text(
                        text = page.tag,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = AegisCyan
                    )
                }

                if (currentPage < pages.size - 1) {
                    TextButton(onClick = { viewModel.completeOnboarding(onNavigateToEnrollment) }) {
                        Text(
                            text = "SKIP",
                            style = MaterialTheme.typography.labelSmall,
                            color = AegisTextSecondary
                        )
                    }
                }
            }

            // Body content
            AnimatedContent(
                targetState = currentPage,
                transitionSpec = {
                    (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                        slideOutHorizontally { width -> -width } + fadeOut()
                    )
                },
                label = "onboarding_content"
            ) { targetPage ->
                val target = pages[targetPage]
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = target.subtitle,
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = AegisCyan
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = target.title,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            lineHeight = 36.sp
                        ),
                        color = AegisTextPrimary
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = target.description,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            lineHeight = 24.sp
                        ),
                        color = AegisTextSecondary
                    )
                }
            }

            // Bottom Navigation & Indicator
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Page indicator dots (Nothing OS style)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    pages.indices.forEach { index ->
                        val isSelected = index == currentPage
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(if (isSelected) 28.dp else 6.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) AegisCyan else AegisBorderActive)
                        )
                    }
                }

                val isLastPage = currentPage == pages.size - 1
                AegisButton(
                    text = if (isLastPage) "GET STARTED" else "CONTINUE",
                    onClick = { viewModel.onNextPage(onNavigateToEnrollment) },
                    icon = if (isLastPage) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                    isPrimary = true
                )
            }
        }
    }
}
