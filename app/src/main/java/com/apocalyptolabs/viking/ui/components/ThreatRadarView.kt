package com.apocalyptolabs.viking.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.apocalyptolabs.viking.ui.theme.VikingCritical
import com.apocalyptolabs.viking.ui.theme.VikingSafe
import com.apocalyptolabs.viking.ui.theme.VikingTeal
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ThreatRadarView(
    isThreatActive: Boolean,
    modifier: Modifier = Modifier.size(140.dp)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )

    val radarColor = if (isThreatActive) VikingCritical else VikingTeal

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxRadius = size.minDimension / 2f

        // Concentric Circles
        drawCircle(
            color = radarColor.copy(alpha = 0.2f),
            radius = maxRadius,
            style = Stroke(width = 2.dp.toPx())
        )
        drawCircle(
            color = radarColor.copy(alpha = 0.15f),
            radius = maxRadius * 0.66f,
            style = Stroke(width = 1.5.dp.toPx())
        )
        drawCircle(
            color = radarColor.copy(alpha = 0.1f),
            radius = maxRadius * 0.33f,
            style = Stroke(width = 1.dp.toPx())
        )

        // Pulsing Ring Wave
        drawCircle(
            color = radarColor.copy(alpha = (1.0f - pulseRadius).coerceIn(0f, 1f)),
            radius = maxRadius * pulseRadius,
            style = Stroke(width = 3.dp.toPx())
        )

        // Sweeping Radar Line
        val angleRad = Math.toRadians(rotationAngle.toDouble())
        val endX = center.x + maxRadius * cos(angleRad).toFloat()
        val endY = center.y + maxRadius * sin(angleRad).toFloat()

        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(radarColor, Color.Transparent),
                start = center,
                end = Offset(endX, endY)
            ),
            start = center,
            end = Offset(endX, endY),
            strokeWidth = 3.dp.toPx()
        )

        // Center Blip Dot
        drawCircle(
            color = if (isThreatActive) VikingCritical else VikingSafe,
            radius = 6.dp.toPx()
        )
    }
}
