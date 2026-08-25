package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.util.formatArabicDays
import com.example.viewmodel.CycleStats
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun D3NativeDashboard(
    periodLogs: List<PeriodLog>,
    stats: CycleStats
) {
    var selectedChartTab by remember { mutableStateOf(0) } // 0 = Regularity, 1 = Symptoms/Pain

    val cycleLengths = remember(periodLogs) {
        val sorted = periodLogs.sortedBy { it.startDate }
        val list = mutableListOf<Int>()
        for (i in 1 until sorted.size) {
            val diffDays = (sorted[i].startDate - sorted[i-1].startDate) / (24 * 60 * 60 * 1000)
            if (diffDays in 15..50) {
                list.add(diffDays.toInt())
            }
        }
        list
    }

    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("📊", fontSize = 24.sp)
                    Column {
                        Text(
                            text = "لوحة التحليلات والرسوم البيانية المتقدمة",
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.TextWhite,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "تتبع فترات الدورة والمزاج والانتظام تلقائياً",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTheme.SoftGray
                        )
                    }
                }
                Text(
                    text = if (isExpanded) "إخفاء 🔼" else "عرض الرسوم 🔽",
                    color = SoftTheme.SoftPink,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }

            if (isExpanded) {
                // Tab toggler
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SoftTheme.DeepSlate, RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Button(
                        onClick = { selectedChartTab = 0 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedChartTab == 0) SoftTheme.SoftPink else Color.Transparent,
                            contentColor = if (selectedChartTab == 0) SoftTheme.TextWhite else SoftTheme.SoftGray
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text("انتظام الدورة 🩸", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Button(
                        onClick = { selectedChartTab = 1 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedChartTab == 1) SoftTheme.SoftPink else Color.Transparent,
                            contentColor = if (selectedChartTab == 1) SoftTheme.TextWhite else SoftTheme.SoftGray
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text("حدة الأعراض والألم 🧠", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                if (selectedChartTab == 0) {
                    CycleRegularityChart(cycleLengths = cycleLengths, avgCycleLength = stats.averageCycleLength)
                } else {
                    MoodSymptomChart(periodLogs = periodLogs)
                }
            }
        }
    }
}

@Composable
fun CycleRegularityChart(cycleLengths: List<Int>, avgCycleLength: Int) {
    val displayLengths = if (cycleLengths.isEmpty()) {
        listOf(28, 27, 29, 28) // Placeholder mock data
    } else {
        cycleLengths.takeLast(4)
    }
    val isPlaceholder = cycleLengths.isEmpty()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(SoftTheme.DeepSlate, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val paddingLeft = 40.dp.toPx()
            val paddingBottom = 24.dp.toPx()
            val paddingTop = 16.dp.toPx()
            val paddingRight = 16.dp.toPx()

            val chartWidth = width - paddingLeft - paddingRight
            val chartHeight = height - paddingTop - paddingBottom

            val maxVal = 40f
            val stepY = maxVal / 4f

            for (i in 0..4) {
                val yVal = i * stepY
                val yPos = height - paddingBottom - (yVal / maxVal) * chartHeight
                
                drawLine(
                    color = SoftTheme.SoftGray.copy(alpha = 0.15f),
                    start = Offset(paddingLeft, yPos),
                    end = Offset(width - paddingRight, yPos),
                    strokeWidth = 1.dp.toPx()
                )
            }

            val barCount = displayLengths.size
            val barWidth = (chartWidth / barCount) * 0.45f
            val spacing = (chartWidth / barCount)

            for (idx in displayLengths.indices) {
                val days = displayLengths[idx]
                val barHeight = (days.toFloat() / maxVal) * chartHeight
                val xPos = paddingLeft + (idx * spacing) + (spacing - barWidth) / 2f
                val yPos = height - paddingBottom - barHeight

                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(SoftTheme.SoftPink, SoftTheme.DeepPink)
                    ),
                    topLeft = Offset(xPos, yPos),
                    size = Size(barWidth, barHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )
                
                if (isPlaceholder) {
                    drawRect(
                        color = SoftTheme.DeepSlate.copy(alpha = 0.4f),
                        topLeft = Offset(xPos, yPos),
                        size = Size(barWidth, barHeight)
                    )
                }
            }

            val avgY = height - paddingBottom - (avgCycleLength.toFloat() / maxVal) * chartHeight
            drawLine(
                color = SoftTheme.MintTeal,
                start = Offset(paddingLeft, avgY),
                end = Offset(width - paddingRight, avgY),
                strokeWidth = 2.dp.toPx(),
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(start = 40.dp, end = 16.dp, bottom = 2.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            for (idx in displayLengths.indices) {
                Text(
                    text = if (isPlaceholder) "دورة م ${idx + 1}" else "دورة ${idx + 1}",
                    color = SoftTheme.SoftGray,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(start = 40.dp, end = 16.dp, top = 20.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            for (idx in displayLengths.indices) {
                val days = displayLengths[idx]
                Text(
                    text = formatArabicDays(days),
                    color = if (isPlaceholder) SoftTheme.SoftGray.copy(alpha = 0.7f) else SoftTheme.TextWhite,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }

        Text(
            text = "المعدل: ${formatArabicDays(avgCycleLength)}",
            color = SoftTheme.MintTeal,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 44.dp, top = 4.dp),
            fontSize = 10.sp
        )

        if (isPlaceholder) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "سجّلي دورتين أو أكثر لعرض انتظام دورتكِ الشخصي 📊",
                    color = SoftTheme.LightPink,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun MoodSymptomChart(periodLogs: List<PeriodLog>) {
    val displayPainLevels = if (periodLogs.isEmpty()) {
        listOf(6, 4, 7, 3, 5) // Placeholder mock data
    } else {
        periodLogs.takeLast(5).map { it.painLevel }
    }
    val isPlaceholder = periodLogs.isEmpty()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(SoftTheme.DeepSlate, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val paddingLeft = 40.dp.toPx()
            val paddingBottom = 24.dp.toPx()
            val paddingTop = 16.dp.toPx()
            val paddingRight = 16.dp.toPx()

            val chartWidth = width - paddingLeft - paddingRight
            val chartHeight = height - paddingTop - paddingBottom

            val maxVal = 10f

            for (i in 0..4) {
                val yVal = i * 2.5f
                val yPos = height - paddingBottom - (yVal / maxVal) * chartHeight
                drawLine(
                    color = SoftTheme.SoftGray.copy(alpha = 0.15f),
                    start = Offset(paddingLeft, yPos),
                    end = Offset(width - paddingRight, yPos),
                    strokeWidth = 1.dp.toPx()
                )
            }

            val pointCount = displayPainLevels.size
            val spacing = chartWidth / (pointCount - 1).coerceAtLeast(1)
            val points = displayPainLevels.indices.map { idx ->
                val pain = displayPainLevels[idx]
                val x = paddingLeft + idx * spacing
                val y = height - paddingBottom - (pain.toFloat() / maxVal) * chartHeight
                Offset(x, y)
            }

            val path = androidx.compose.ui.graphics.Path()
            if (points.isNotEmpty()) {
                path.moveTo(points[0].x, points[0].y)
                for (i in 0 until points.size - 1) {
                    val p0 = points[i]
                    val p1 = points[i + 1]
                    val controlX1 = p0.x + (p1.x - p0.x) / 2f
                    val controlY1 = p0.y
                    val controlX2 = p0.x + (p1.x - p0.x) / 2f
                    val controlY2 = p1.y
                    
                    path.cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
                }
                
                drawPath(
                    path = path,
                    color = SoftTheme.SoftTeal,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                val fillPath = androidx.compose.ui.graphics.Path().apply {
                    addPath(path)
                    lineTo(points.last().x, height - paddingBottom)
                    lineTo(points.first().x, height - paddingBottom)
                    close()
                }
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(SoftTheme.SoftTeal.copy(alpha = 0.3f), Color.Transparent)
                    )
                )

                points.forEach { pt ->
                    drawCircle(
                        color = SoftTheme.MintTeal,
                        radius = 4.dp.toPx(),
                        center = pt
                    )
                    drawCircle(
                        color = SoftTheme.MintTeal.copy(alpha = 0.4f),
                        radius = 8.dp.toPx(),
                        center = pt,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(start = 40.dp, end = 16.dp, bottom = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            for (idx in displayPainLevels.indices) {
                Text(
                    text = if (isPlaceholder) "تسجيل ${idx + 1}" else "ت ${idx + 1}",
                    color = SoftTheme.SoftGray,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(start = 40.dp, end = 16.dp, top = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            for (idx in displayPainLevels.indices) {
                val score = displayPainLevels[idx]
                Text(
                    text = "$score/10",
                    color = if (isPlaceholder) SoftTheme.SoftGray.copy(alpha = 0.7f) else SoftTheme.SoftTeal,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }

        Text(
            text = "مؤشر حدة الأعراض والألم",
            color = SoftTheme.SoftTeal,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 44.dp, top = 4.dp),
            fontSize = 10.sp
        )

        if (isPlaceholder) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "سجّلي الأعراض في دورتكِ لعرض تقلباتكِ الصحية والمزاجية 🧠",
                    color = SoftTheme.SoftTeal,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

