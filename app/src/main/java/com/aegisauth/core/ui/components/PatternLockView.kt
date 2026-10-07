package com.aegisauth.core.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.aegisauth.core.theme.AegisCritical
import com.aegisauth.core.theme.AegisCyan
import com.aegisauth.core.theme.AegisCyanLight
import kotlin.math.sqrt

@Composable
fun PatternLockView(
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    enabled: Boolean = true,
    onPatternComplete: (List<Int>) -> Unit
) {
    val selectedNodes = remember { mutableStateListOf<Int>() }
    var currentTouchPosition by remember { mutableStateOf<Offset?>(null) }
    val view = LocalView.current

    val activeColor = if (isError) AegisCritical else AegisCyan
    val glowColor = if (isError) AegisCritical.copy(alpha = 0.3f) else AegisCyanLight.copy(alpha = 0.25f)
    val idleDotColor = Color(0xFF2A364F)

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(16.dp)
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectDragGestures(
                    onDragStart = { offset ->
                        selectedNodes.clear()
                        currentTouchPosition = offset
                        val node = findNodeAtPosition(offset, size.width.toFloat(), size.height.toFloat())
                        if (node != null && !selectedNodes.contains(node)) {
                            selectedNodes.add(node)
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        }
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        currentTouchPosition = change.position
                        val node = findNodeAtPosition(change.position, size.width.toFloat(), size.height.toFloat())
                        if (node != null && !selectedNodes.contains(node)) {
                            selectedNodes.add(node)
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        }
                    },
                    onDragEnd = {
                        if (selectedNodes.isNotEmpty()) {
                            onPatternComplete(selectedNodes.toList())
                        }
                        currentTouchPosition = null
                    },
                    onDragCancel = {
                        selectedNodes.clear()
                        currentTouchPosition = null
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val nodeRadius = 10.dp.toPx()
            val selectedRadius = 14.dp.toPx()
            val glowRadius = 26.dp.toPx()
            val strokeWidth = 5.dp.toPx()

            // Calculate coordinates for 3x3 grid
            val nodePositions = (0..8).map { index ->
                val row = index / 3
                val col = index % 3
                val x = (col + 0.5f) * (width / 3f)
                val y = (row + 0.5f) * (height / 3f)
                Offset(x, y)
            }

            // 1. Draw connecting lines between selected nodes
            if (selectedNodes.size > 1) {
                for (i in 0 until selectedNodes.size - 1) {
                    val start = nodePositions[selectedNodes[i]]
                    val end = nodePositions[selectedNodes[i + 1]]
                    drawLine(
                        color = activeColor,
                        start = start,
                        end = end,
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                }
            }

            // 2. Draw line from last selected node to current touch position
            if (selectedNodes.isNotEmpty() && currentTouchPosition != null) {
                val lastNodePos = nodePositions[selectedNodes.last()]
                drawLine(
                    color = activeColor.copy(alpha = 0.7f),
                    start = lastNodePos,
                    end = currentTouchPosition!!,
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }

            // 3. Draw nodes
            nodePositions.forEachIndexed { index, pos ->
                val isSelected = selectedNodes.contains(index)
                if (isSelected) {
                    // Glow outer circle
                    drawCircle(
                        color = glowColor,
                        radius = glowRadius,
                        center = pos
                    )
                    // Active outer node
                    drawCircle(
                        color = activeColor,
                        radius = selectedRadius,
                        center = pos
                    )
                    // Inner dot
                    drawCircle(
                        color = Color.White,
                        radius = 5.dp.toPx(),
                        center = pos
                    )
                } else {
                    // Idle dot
                    drawCircle(
                        color = idleDotColor,
                        radius = nodeRadius,
                        center = pos
                    )
                }
            }
        }
    }
}

private fun findNodeAtPosition(pos: Offset, width: Float, height: Float): Int? {
    val hitRadius = (width / 6f) * 0.85f
    for (index in 0..8) {
        val row = index / 3
        val col = index % 3
        val centerX = (col + 0.5f) * (width / 3f)
        val centerY = (row + 0.5f) * (height / 3f)
        val dx = pos.x - centerX
        val dy = pos.y - centerY
        if (sqrt(dx * dx + dy * dy) <= hitRadius) {
            return index
        }
    }
    return null
}
