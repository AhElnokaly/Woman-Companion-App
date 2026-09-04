package com.example.ui.fitness

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

// Gorgeous custom Pelvis anatomical line diagram for the Hero card
@Composable
fun LargePelvisAnatomicalDrawing(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val neonColor = Color(0xFF38B2AC) // Mint green / neon cyan
        val softGlow = Color(0xFF38B2AC).copy(alpha = 0.25f)
        val lineStroke = 2.5f.dp.toPx()

        // Background glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(softGlow, Color.Transparent),
                center = Offset(w / 2, h / 2),
                radius = w * 0.45f
            )
        )

        // Draw elegant pelvis structure outline
        val hipPath = Path().apply {
            // Left crest
            moveTo(w * 0.25f, h * 0.25f)
            cubicTo(w * 0.12f, h * 0.2f, w * 0.12f, h * 0.55f, w * 0.28f, h * 0.65f)
            cubicTo(w * 0.38f, h * 0.72f, w * 0.45f, h * 0.78f, w * 0.5f, h * 0.8f)

            // Right crest
            moveTo(w * 0.75f, h * 0.25f)
            cubicTo(w * 0.88f, h * 0.2f, w * 0.88f, h * 0.55f, w * 0.72f, h * 0.65f)
            cubicTo(w * 0.62f, h * 0.72f, w * 0.55f, h * 0.78f, w * 0.5f, h * 0.8f)
        }
        drawPath(hipPath, color = neonColor, style = Stroke(lineStroke, cap = StrokeCap.Round))

        // Inner pelvis rings
        val innerRingPath = Path().apply {
            moveTo(w * 0.35f, h * 0.42f)
            cubicTo(w * 0.25f, h * 0.55f, w * 0.45f, h * 0.78f, w * 0.5f, h * 0.78f)
            cubicTo(w * 0.55f, h * 0.78f, w * 0.75f, h * 0.55f, w * 0.65f, h * 0.42f)
        }
        drawPath(innerRingPath, color = neonColor.copy(alpha = 0.8f), style = Stroke(lineStroke, cap = StrokeCap.Round))

        // Pelvic floor muscle cradle (the hammock)
        val muscleHammock = Path().apply {
            moveTo(w * 0.36f, h * 0.62f)
            quadraticTo(w * 0.5f, h * 0.72f, w * 0.64f, h * 0.62f)
            quadraticTo(w * 0.5f, h * 0.66f, w * 0.36f, h * 0.62f)
        }
        drawPath(muscleHammock, color = neonColor, style = Stroke(lineStroke * 1.5f, cap = StrokeCap.Round))

        // Glowing center representing core strength
        drawCircle(color = neonColor, radius = 4.dp.toPx(), center = Offset(w / 2, h * 0.64f))
        drawCircle(color = neonColor.copy(alpha = 0.4f), radius = 8.dp.toPx(), center = Offset(w / 2, h * 0.64f))
    }
}

// Gear stars tracker drawing
@Composable
fun JouriMaternalGearTracker(
    completed: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val activeColor = Color(0xFF38B2AC) // Mint green
        val inactiveColor = Color(0xFF2D3748) // Soft deep slate gray
        val color = if (completed) activeColor else inactiveColor
        val strokeWidth = 1.5f.dp.toPx()

        // Draw gear teeth/petals
        val teethCount = 8
        val outerRadius = w / 2
        val innerRadius = w * 0.3f
        val center = Offset(w / 2, h / 2)

        drawCircle(
            color = color,
            radius = innerRadius,
            style = Stroke(strokeWidth)
        )

        for (i in 0 until teethCount) {
            val angle = (i * 360f / teethCount) * (Math.PI / 180f)
            val startX = center.x + innerRadius * Math.cos(angle).toFloat()
            val startY = center.y + innerRadius * Math.sin(angle).toFloat()
            val endX = center.x + outerRadius * Math.cos(angle).toFloat()
            val endY = center.y + outerRadius * Math.sin(angle).toFloat()
            drawLine(
                color = color,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = strokeWidth * 1.5f,
                cap = StrokeCap.Round
            )
        }
    }
}

// Custom exercise line posture illustrations
@Composable
fun ExerciseStepIllustration(
    type: String,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val mainColor = Color(0xFF38B2AC) // Mint green
        val strokeWidthVal = 2.dp.toPx()

        when (type) {
            "sit" -> {
                // Meditation sitting outline
                drawCircle(color = mainColor, radius = 5.dp.toPx(), center = Offset(w / 2, h * 0.25f), style = Stroke(strokeWidthVal))
                drawLine(color = mainColor, start = Offset(w / 2, h * 0.35f), end = Offset(w / 2, h * 0.65f), strokeWidth = strokeWidthVal, cap = StrokeCap.Round)

                val armPath = Path().apply {
                    moveTo(w / 2, h * 0.42f)
                    quadraticTo(w * 0.25f, h * 0.5f, w * 0.3f, h * 0.7f)
                    moveTo(w / 2, h * 0.42f)
                    quadraticTo(w * 0.75f, h * 0.5f, w * 0.7f, h * 0.7f)
                }
                drawPath(armPath, color = mainColor, style = Stroke(strokeWidthVal, cap = StrokeCap.Round))

                val legPath = Path().apply {
                    moveTo(w * 0.35f, h * 0.65f)
                    quadraticTo(w * 0.2f, h * 0.8f, w * 0.5f, h * 0.82f)
                    quadraticTo(w * 0.8f, h * 0.8f, w * 0.65f, h * 0.65f)
                }
                drawPath(legPath, color = mainColor, style = Stroke(strokeWidthVal, cap = StrokeCap.Round))
            }
            "hold" -> {
                // Contraction pelvis
                val leftPath = Path().apply {
                    moveTo(w * 0.22f, h * 0.22f)
                    cubicTo(w * 0.17f, h * 0.5f, w * 0.35f, h * 0.8f, w * 0.5f, h * 0.85f)
                }
                drawPath(leftPath, color = mainColor, style = Stroke(strokeWidthVal, cap = StrokeCap.Round))

                val rightPath = Path().apply {
                    moveTo(w * 0.78f, h * 0.22f)
                    cubicTo(w * 0.83f, h * 0.5f, w * 0.65f, h * 0.8f, w * 0.5f, h * 0.85f)
                }
                drawPath(rightPath, color = mainColor, style = Stroke(strokeWidthVal, cap = StrokeCap.Round))

                val cradlePath = Path().apply {
                    moveTo(w * 0.35f, h * 0.55f)
                    quadraticTo(w * 0.5f, h * 0.7f, w * 0.65f, h * 0.55f)
                }
                drawPath(cradlePath, color = mainColor, style = Stroke(strokeWidthVal * 1.5f, cap = StrokeCap.Round))

                // Inward contraction arrows
                drawLine(color = mainColor, start = Offset(w * 0.22f, h * 0.5f), end = Offset(w * 0.4f, h * 0.5f), strokeWidth = strokeWidthVal, cap = StrokeCap.Round)
                drawLine(color = mainColor, start = Offset(w * 0.78f, h * 0.5f), end = Offset(w * 0.6f, h * 0.5f), strokeWidth = strokeWidthVal, cap = StrokeCap.Round)
            }
            "relax" -> {
                // Relaxation pelvis
                val leftPath = Path().apply {
                    moveTo(w * 0.22f, h * 0.22f)
                    cubicTo(w * 0.17f, h * 0.5f, w * 0.35f, h * 0.8f, w * 0.5f, h * 0.85f)
                }
                drawPath(leftPath, color = mainColor, style = Stroke(strokeWidthVal, cap = StrokeCap.Round))

                val rightPath = Path().apply {
                    moveTo(w * 0.78f, h * 0.22f)
                    cubicTo(w * 0.83f, h * 0.5f, w * 0.65f, h * 0.8f, w * 0.5f, h * 0.85f)
                }
                drawPath(rightPath, color = mainColor, style = Stroke(strokeWidthVal, cap = StrokeCap.Round))

                val cradlePath = Path().apply {
                    moveTo(w * 0.35f, h * 0.58f)
                    quadraticTo(w * 0.5f, h * 0.62f, w * 0.65f, h * 0.58f)
                }
                drawPath(cradlePath, color = mainColor, style = Stroke(strokeWidthVal, cap = StrokeCap.Round))

                // Outward release arrows
                drawLine(color = mainColor, start = Offset(w * 0.42f, h * 0.5f), end = Offset(w * 0.25f, h * 0.5f), strokeWidth = strokeWidthVal, cap = StrokeCap.Round)
                drawLine(color = mainColor, start = Offset(w * 0.58f, h * 0.5f), end = Offset(w * 0.75f, h * 0.5f), strokeWidth = strokeWidthVal, cap = StrokeCap.Round)
            }
            "stretch" -> {
                // Stretch spinal arc
                val stretchPath = Path().apply {
                    moveTo(w * 0.25f, h * 0.75f)
                    quadraticTo(w * 0.45f, h * 0.35f, w * 0.75f, h * 0.45f)
                }
                drawPath(stretchPath, color = mainColor, style = Stroke(strokeWidthVal * 1.5f, cap = StrokeCap.Round))

                drawCircle(color = mainColor, radius = 4.5f.dp.toPx(), center = Offset(w * 0.8f, h * 0.38f), style = Stroke(strokeWidthVal))
                drawLine(color = mainColor, start = Offset(w * 0.15f, h * 0.8f), end = Offset(w * 0.85f, h * 0.8f), strokeWidth = strokeWidthVal, cap = StrokeCap.Round)
            }
            "breathe" -> {
                // Concentric lung rings
                drawCircle(color = mainColor, radius = 6.dp.toPx(), center = Offset(w / 2, h / 2), style = Stroke(strokeWidthVal))
                drawCircle(color = mainColor.copy(alpha = 0.5f), radius = 13.dp.toPx(), center = Offset(w / 2, h / 2), style = Stroke(strokeWidthVal, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)))
                drawCircle(color = mainColor.copy(alpha = 0.25f), radius = 20.dp.toPx(), center = Offset(w / 2, h / 2), style = Stroke(strokeWidthVal, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)))
            }
            else -> {
                drawCircle(color = mainColor, radius = 10.dp.toPx(), center = Offset(w / 2, h / 2), style = Stroke(strokeWidthVal))
            }
        }
    }
}
