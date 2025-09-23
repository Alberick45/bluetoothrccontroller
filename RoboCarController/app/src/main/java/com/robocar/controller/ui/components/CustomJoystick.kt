package com.robocar.controller.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.*

@Composable
fun CustomJoystick(
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    knobSize: Dp = 60.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    knobColor: Color = MaterialTheme.colorScheme.primary,
    onMove: (angle: Int, strength: Int) -> Unit = { _, _ -> }
) {
    val density = LocalDensity.current
    val sizePx = with(density) { size.toPx() }
    val knobSizePx = with(density) { knobSize.toPx() }
    val radius = sizePx / 2f
    val knobRadius = knobSizePx / 2f
    
    var knobPosition by remember { mutableStateOf(Offset(radius, radius)) }
    var isDragging by remember { mutableStateOf(false) }
    
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            knobPosition = offset
                        },
                        onDragEnd = {
                            isDragging = false
                            knobPosition = Offset(radius, radius)
                            onMove(0, 0) // Reset when released
                        },
                        onDrag = { _, dragAmount ->
                            val newPosition = knobPosition + dragAmount
                            val centerOffset = newPosition - Offset(radius, radius)
                            val distance = sqrt(centerOffset.x.pow(2) + centerOffset.y.pow(2))
                            
                            knobPosition = if (distance <= radius - knobRadius) {
                                newPosition
                            } else {
                                val angle = atan2(centerOffset.y, centerOffset.x)
                                val maxDistance = radius - knobRadius
                                Offset(
                                    radius + cos(angle) * maxDistance,
                                    radius + sin(angle) * maxDistance
                                )
                            }
                            
                            // Calculate angle and strength
                            val finalCenterOffset = knobPosition - Offset(radius, radius)
                            val finalDistance = sqrt(finalCenterOffset.x.pow(2) + finalCenterOffset.y.pow(2))
                            val finalAngle = atan2(-finalCenterOffset.y, finalCenterOffset.x) * 180 / PI
                            val normalizedAngle = ((finalAngle + 360) % 360).toInt()
                            val strength = ((finalDistance / (radius - knobRadius)) * 100).toInt().coerceAtMost(100)
                            
                            onMove(normalizedAngle, strength)
                        }
                    )
                }
        ) {
            drawJoystick(
                size = this.size,
                knobPosition = knobPosition,
                knobRadius = knobRadius,
                backgroundColor = backgroundColor,
                knobColor = knobColor,
                isDragging = isDragging
            )
        }
    }
}

private fun DrawScope.drawJoystick(
    size: androidx.compose.ui.geometry.Size,
    knobPosition: Offset,
    knobRadius: Float,
    backgroundColor: Color,
    knobColor: Color,
    isDragging: Boolean
) {
    val radius = size.minDimension / 2f
    
    // Draw background circle
    drawCircle(
        color = backgroundColor,
        radius = radius,
        center = size.center
    )
    
    // Draw outer ring
    drawCircle(
        color = knobColor.copy(alpha = 0.3f),
        radius = radius,
        center = size.center,
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx())
    )
    
    // Draw center dot
    drawCircle(
        color = knobColor.copy(alpha = 0.5f),
        radius = 8.dp.toPx(),
        center = size.center
    )
    
    // Draw knob
    val knobAlpha = if (isDragging) 0.9f else 0.7f
    drawCircle(
        color = knobColor.copy(alpha = knobAlpha),
        radius = knobRadius,
        center = knobPosition
    )
    
    // Draw knob border
    drawCircle(
        color = Color.White.copy(alpha = 0.8f),
        radius = knobRadius,
        center = knobPosition,
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
    )
}