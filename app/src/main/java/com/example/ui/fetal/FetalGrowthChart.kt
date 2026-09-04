package com.example.ui.fetal

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.data.FetalGrowthLog

@Composable
fun FetalGrowthChart(logs: List<FetalGrowthLog>) {
    val sortedLogs = remember(logs) { logs.sortedBy { it.pregnancyWeek } }
    
    // Determine min and max weeks to draw
    val minWeek = 4f
    val maxWeek = 42f
    
    // Max weight standard or actual to fit the chart properly
    val maxWeight = remember(sortedLogs) {
        val maxLogWeight = sortedLogs.maxOfOrNull { it.weightGrams } ?: 0.0
        val maxStdWeight = FetalStandardData.getStandardForWeek(42).weightGrams
        maxOf(maxLogWeight, maxStdWeight).toFloat()
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        val width = size.width
        val height = size.height
        val paddingLeft = 50f
        val paddingRight = 20f
        val paddingTop = 20f
        val paddingBottom = 40f
        
        val chartWidth = width - paddingLeft - paddingRight
        val chartHeight = height - paddingTop - paddingBottom

        // Draw axes lines (Deep Slate color)
        drawLine(
            color = Color.White.copy(alpha = 0.1f),
            start = Offset(paddingLeft, paddingTop),
            end = Offset(paddingLeft, height - paddingBottom),
            strokeWidth = 2f
        )
        drawLine(
            color = Color.White.copy(alpha = 0.1f),
            start = Offset(paddingLeft, height - paddingBottom),
            end = Offset(width - paddingRight, height - paddingBottom),
            strokeWidth = 2f
        )

        // Helper function to map week and weight to X,Y offsets
        fun getCoordinates(week: Float, weight: Float): Offset {
            val xRatio = (week - minWeek) / (maxWeek - minWeek)
            val yRatio = weight / maxWeight
            val x = paddingLeft + (xRatio * chartWidth)
            val y = (height - paddingBottom) - (yRatio * chartHeight)
            return Offset(x, y)
        }

        // Draw Y axis guidelines and labels (every 1000g up to maxWeight)
        val weightStep = 1000f
        var currentW = 1000f
        while (currentW <= maxWeight) {
            val pt = getCoordinates(minWeek, currentW)
            drawLine(
                color = Color.White.copy(alpha = 0.05f),
                start = Offset(paddingLeft, pt.y),
                end = Offset(width - paddingRight, pt.y),
                strokeWidth = 1f
            )
            currentW += weightStep
        }

        // Draw X axis guidelines for major weeks (12, 20, 28, 36, 40)
        val markerWeeks = listOf(12f, 20f, 28f, 36f, 40f)
        markerWeeks.forEach { mw ->
            val pt = getCoordinates(mw, 0f)
            drawLine(
                color = Color.White.copy(alpha = 0.05f),
                start = Offset(pt.x, paddingTop),
                end = Offset(pt.x, height - paddingBottom),
                strokeWidth = 1f
            )
        }

        // 1. Draw expected standard curve (dotted line in SoftPink)
        val pathStd = Path()
        var firstStd = true
        for (w in 4..42) {
            val stdWeight = FetalStandardData.getStandardForWeek(w).weightGrams.toFloat()
            val pt = getCoordinates(w.toFloat(), stdWeight)
            if (firstStd) {
                pathStd.moveTo(pt.x, pt.y)
                firstStd = false
            } else {
                pathStd.lineTo(pt.x, pt.y)
            }
        }
        drawPath(
            path = pathStd,
            color = Color(0xFFFFB6C1).copy(alpha = 0.6f),
            style = Stroke(
                width = 3f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
        )

        // 2. Draw actual user logs (solid line + circular dots)
        if (sortedLogs.isNotEmpty()) {
            val pathActual = Path()
            var firstActual = true
            sortedLogs.forEach { log ->
                val pt = getCoordinates(log.pregnancyWeek.toFloat(), log.weightGrams.toFloat())
                if (firstActual) {
                    pathActual.moveTo(pt.x, pt.y)
                    firstActual = false
                } else {
                    pathActual.lineTo(pt.x, pt.y)
                }
            }
            // Draw actual line
            drawPath(
                path = pathActual,
                color = Color.White,
                style = Stroke(width = 4f)
            )

            // Draw actual dots
            sortedLogs.forEach { log ->
                val pt = getCoordinates(log.pregnancyWeek.toFloat(), log.weightGrams.toFloat())
                drawCircle(
                    color = Color.White,
                    radius = 8f,
                    center = pt
                )
                drawCircle(
                    color = Color(0xFFFFB6C1),
                    radius = 4f,
                    center = pt
                )
            }
        }
    }
}
