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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.apocalyptolabs.viking.ui.theme.VikingCritical
import com.apocalyptolabs.viking.ui.theme.VikingGray
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
            animation = tween(if (isThreatActive) 1200 else 2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )

    val corePulse by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "corePulse"
    )

    val radarColor = if (isThreatActive) VikingCritical else VikingTeal

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxRadius = size.minDimension / 2f

        // Outer ambient glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(radarColor.copy(alpha = 0.12f), Color.Transparent),
                center = center,
                radius = maxRadius
            ),
            radius = maxRadius
        )

        // Concentric grid rings
        drawCircle(radarColor.copy(alpha = 0.25f), maxRadius, center, style = Stroke(2.dp.toPx()))
        drawCircle(radarColor.copy(alpha = 0.16f), maxRadius * 0.66f, center, style = Stroke(1.5.dp.toPx()))
        drawCircle(radarColor.copy(alpha = 0.10f), maxRadius * 0.33f, center, style = Stroke(1.dp.toPx()))

        // Crosshair axes
        drawLine(VikingGray.copy(alpha = 0.14f), Offset(center.x - maxRadius, center.y), Offset(center.x + maxRadius, center.y), 1.dp.toPx())
        drawLine(VikingGray.copy(alpha = 0.14f), Offset(center.x, center.y - maxRadius), Offset(center.x, center.y + maxRadius), 1.dp.toPx())

        // Expanding sonar pulse ring
        drawCircle(
            color = radarColor.copy(alpha = (1.0f - pulseRadius).coerceIn(0f, 0.85f)),
            radius = maxRadius * pulseRadius,
            center = center,
            style = Stroke(width = (1.5f + 2f * pulseRadius).dp.toPx())
        )

        // Sweeping radar beam with fading trail
        val angleRad = Math.toRadians(rotationAngle.toDouble())
        val endX = center.x + maxRadius * cos(angleRad).toFloat()
        val endY = center.y + maxRadius * sin(angleRad).toFloat()
        val trailDeg = 55.0

        for (i in 1..5) {
            val trailAngle = Math.toRadians(rotationAngle - trailDeg * i / 5)
            val tx = center.x + maxRadius * cos(trailAngle).toFloat()
            val ty = center.y + maxRadius * sin(trailAngle).toFloat()
            drawLine(
                color = radarColor.copy(alpha = 0.30f * (6 - i) / 5),
                start = center,
                end = Offset(tx, ty),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        drawLine(
            brush = Brush.linearGradient(listOf(radarColor, radarColor.copy(alpha = 0.4f)), start = center, end = Offset(endX, endY)),
            start = center,
            end = Offset(endX, endY),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Pulsing status core
        drawCircle(radarColor.copy(alpha = 0.25f), radius = 9.dp.toPx() * corePulse)
        drawCircle(
            color = if (isThreatActive) VikingCritical else VikingSafe,
            radius = 5.5f.dp.toPx() * corePulse
        )
    }
}
