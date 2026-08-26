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
import com.apocalyptolabs.viking.ui.theme.*
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
                colors = listOf(radarColor.copy(alpha = 0.08f), Color.Transparent),
                center = center,
                radius = maxRadius
            ),
            radius = maxRadius
        )

        // Concentric grid rings (Stitch Obsidian Cipher vector specs)
        val dashEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(6f, 8f), 0f)
        drawCircle(
            color = VikingBorder,
            radius = maxRadius,
            center = center,
            style = Stroke(width = 1.dp.toPx(), pathEffect = dashEffect)
        )
        drawCircle(
            color = VikingBorder,
            radius = maxRadius * 0.68f,
            center = center,
            style = Stroke(width = 1.dp.toPx())
        )
        drawCircle(
            color = VikingBorder,
            radius = maxRadius * 0.38f,
            center = center,
            style = Stroke(width = 1.dp.toPx(), pathEffect = dashEffect)
        )
        drawCircle(
            color = VikingBorder,
            radius = maxRadius * 0.12f,
            center = center,
            style = Stroke(width = 1.dp.toPx())
        )

        // Crosshair axes (dashed)
        val crosshairDash = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(4f, 6f), 0f)
        drawLine(
            color = VikingBorder,
            start = Offset(center.x - maxRadius, center.y),
            end = Offset(center.x + maxRadius, center.y),
            strokeWidth = 1.dp.toPx(),
            pathEffect = crosshairDash
        )
        drawLine(
            color = VikingBorder,
            start = Offset(center.x, center.y - maxRadius),
            end = Offset(center.x, center.y + maxRadius),
            strokeWidth = 1.dp.toPx(),
            pathEffect = crosshairDash
        )

        // Expanding sonar pulse ring
        drawCircle(
            color = radarColor.copy(alpha = (1.0f - pulseRadius).coerceIn(0f, 0.45f)),
            radius = maxRadius * pulseRadius,
            center = center,
            style = Stroke(width = 1.5.dp.toPx())
        )

        // Sweeping radar beam with fading sector
        val angleRad = Math.toRadians(rotationAngle.toDouble())
        val endX = center.x + maxRadius * cos(angleRad).toFloat()
        val endY = center.y + maxRadius * sin(angleRad).toFloat()
        val trailDeg = 45.0

        for (i in 1..4) {
            val trailAngle = Math.toRadians(rotationAngle - trailDeg * i / 4)
            val tx = center.x + maxRadius * cos(trailAngle).toFloat()
            val ty = center.y + maxRadius * sin(trailAngle).toFloat()
            drawLine(
                color = radarColor.copy(alpha = 0.18f * (5 - i) / 4),
                start = center,
                end = Offset(tx, ty),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        drawLine(
            color = radarColor.copy(alpha = 0.85f),
            start = center,
            end = Offset(endX, endY),
            strokeWidth = 1.5.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Monitored endpoint blips (hardware subsystems)
        drawCircle(VikingWhite.copy(alpha = 0.65f), radius = 2.dp.toPx(), center = Offset(center.x + maxRadius * 0.45f, center.y - maxRadius * 0.35f))
        drawCircle(VikingWhite.copy(alpha = 0.65f), radius = 2.dp.toPx(), center = Offset(center.x - maxRadius * 0.50f, center.y - maxRadius * 0.20f))
        drawCircle(VikingWhite.copy(alpha = 0.65f), radius = 2.dp.toPx(), center = Offset(center.x - maxRadius * 0.25f, center.y + maxRadius * 0.55f))
        drawCircle(VikingWhite.copy(alpha = 0.65f), radius = 2.dp.toPx(), center = Offset(center.x + maxRadius * 0.30f, center.y + maxRadius * 0.45f))

        // Flagged threat blip if active
        if (isThreatActive) {
            val threatBlip = Offset(center.x + maxRadius * 0.55f, center.y - maxRadius * 0.55f)
            drawCircle(VikingCritical.copy(alpha = (1f - pulseRadius).coerceIn(0f, 0.8f)), radius = 7.dp.toPx() * pulseRadius, center = threatBlip)
            drawCircle(VikingCritical, radius = 3.dp.toPx(), center = threatBlip)
        }

        // Central host node core
        drawCircle(VikingSurface, radius = 6.dp.toPx(), center = center)
        drawCircle(
            color = if (isThreatActive) VikingCritical else VikingSafe,
            radius = 3.5.dp.toPx() * corePulse,
            center = center
        )
    }
}
