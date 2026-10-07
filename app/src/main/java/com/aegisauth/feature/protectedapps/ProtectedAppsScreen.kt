package com.aegisauth.feature.protectedapps

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aegisauth.core.theme.*
import com.aegisauth.core.ui.components.AegisCard
import com.aegisauth.core.ui.components.AegisTopBar
import com.aegisauth.data.repository.AppItem

@Composable
fun ProtectedAppsScreen(
    onBackClick: () -> Unit,
    viewModel: ProtectedAppsViewModel = hiltViewModel()
) {
    val apps by viewModel.installedApps.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val filteredApps = apps.filter {
        it.appName.contains(searchQuery, ignoreCase = true) ||
                it.packageName.contains(searchQuery, ignoreCase = true)
    }

    val protectedCount = apps.count { it.isProtected }

    Scaffold(
        topBar = {
            AegisTopBar(
                title = "Protected Apps",
                subtitle = "$protectedCount APPS SECURED",
                onBackClick = onBackClick
            )
        },
        containerColor = AegisDarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Search installed applications...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AegisTextTertiary
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = AegisCyan
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AegisCyan,
                    unfocusedBorderColor = AegisBorder,
                    focusedContainerColor = AegisDarkSurface,
                    unfocusedContainerColor = AegisDarkSurface,
                    focusedTextColor = AegisTextPrimary,
                    unfocusedTextColor = AegisTextPrimary
                ),
                singleLine = true
            )

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AegisCyan)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredApps, key = { it.packageName }) { app ->
                        AppProtectionRow(
                            app = app,
                            onToggle = { viewModel.toggleAppProtection(app, it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppProtectionRow(
    app: AppItem,
    onToggle: (Boolean) -> Unit
) {
    AegisCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = if (app.isProtected) AegisDarkSurfaceVariant else AegisDarkSurface,
        borderColor = if (app.isProtected) AegisCyan.copy(alpha = 0.35f) else AegisBorder
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
                // App Icon
                if (app.icon != null) {
                    val bitmap = drawableToBitmap(app.icon)
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = app.appName,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                    } else {
                        DefaultAppIcon()
                    }
                } else {
                    DefaultAppIcon()
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = AegisTextPrimary
                    )
                    Text(
                        text = app.packageName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 11.sp
                        ),
                        color = AegisTextTertiary,
                        maxLines = 1
                    )
                }
            }

            Switch(
                checked = app.isProtected,
                onCheckedChange = onToggle,
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

@Composable
fun DefaultAppIcon() {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(AegisDarkSurfaceElevated),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Apps,
            contentDescription = null,
            tint = AegisCyan,
            modifier = Modifier.size(24.dp)
        )
    }
}

fun drawableToBitmap(drawable: Drawable): Bitmap? {
    if (drawable is BitmapDrawable) {
        return drawable.bitmap
    }
    val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 96
    val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 96
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}
