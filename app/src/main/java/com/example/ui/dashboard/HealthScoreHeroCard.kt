package com.example.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SoftTheme

/**
 * بطاقة مؤشر الصحة والعافية العام (Health Score Hero Card)
 * بطاقة نعناعية متدرجة وناعمة مع مؤشر دائري جذاب ورسمة نباتية مهدئة
 */
@Composable
fun HealthScoreHeroCard(
    scorePercent: Int = 0,
    titleText: String = "أنتِ على الطريق الصحيح",
    subtitleText: String = "توازن أفضل .. حياة أكثر صحة",
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, SoftTheme.MintAccentBorder, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .testTag("health_score_hero_card")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = if (SoftTheme.isDark) {
                            listOf(Color(0xFF162D27), Color(0xFF1B3B33))
                        } else {
                            listOf(Color(0xFFE8F7F2), Color(0xFFD4F2E7))
                        }
                    )
                )
                .padding(horizontal = 18.dp, vertical = 18.dp)
        ) {
            // Background artistic botanical elements on the end side
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.CenterEnd)
            ) {
                val w = size.width
                val h = size.height
                val hillColor = if (SoftTheme.isDark) Color(0x3300A884) else Color(0x2E00A884)
                val sunGlow = if (SoftTheme.isDark) Color(0x22FFD54F) else Color(0x44FFE082)

                // Gentle sun in upper background
                drawCircle(
                    color = sunGlow,
                    radius = 32.dp.toPx(),
                    center = Offset(w * 0.88f, h * 0.25f)
                )

                // Soft background hill wave
                val wavePath = Path().apply {
                    moveTo(w * 0.65f, h)
                    cubicTo(
                        w * 0.75f, h * 0.45f,
                        w * 0.85f, h * 0.65f,
                        w, h * 0.35f
                    )
                    lineTo(w, h)
                    close()
                }
                drawPath(wavePath, color = hillColor)

                // Second foreground hill
                val frontHillPath = Path().apply {
                    moveTo(w * 0.70f, h)
                    cubicTo(
                        w * 0.80f, h * 0.70f,
                        w * 0.90f, h * 0.50f,
                        w, h * 0.60f
                    )
                    lineTo(w, h)
                    close()
                }
                drawPath(frontHillPath, color = hillColor.copy(alpha = 0.5f))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // --- Start Side: Circular Progress Gauge ---
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(94.dp)
                ) {
                    Canvas(modifier = Modifier.size(86.dp)) {
                        val strokeW = 7.5.dp.toPx()
                        val diameter = size.minDimension - strokeW
                        val topLeft = Offset(strokeW / 2, strokeW / 2)
                        val arcSize = Size(diameter, diameter)

                        // Background track
                        drawArc(
                            color = if (SoftTheme.isDark) Color(0xFF224D43) else Color(0xFFC7EFE2),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeW, cap = StrokeCap.Round)
                        )

                        // Progress arc
                        val sweep = (scorePercent.coerceIn(0, 100) / 100f) * 360f
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(
                                    SoftTheme.EmeraldPrimary,
                                    Color(0xFF00C49F),
                                    SoftTheme.EmeraldPrimary
                                )
                            ),
                            startAngle = -90f,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeW, cap = StrokeCap.Round)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "$scorePercent%",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 23.sp,
                                color = SoftTheme.TextPrimary
                            )
                        )
                        Text(
                            text = "إنجاز أهداف اليوم",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoftTheme.TextSecondaryMuted
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // --- End Side: Motivational Text & Botanical Accent ---
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "🌿",
                            fontSize = 16.sp
                        )
                        Text(
                            text = titleText,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = SoftTheme.TextPrimary
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitleText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = SoftTheme.TextSecondaryMuted,
                            lineHeight = 17.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "اضغطي لمعرفة تفاصيل المؤشر 📊",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.EmeraldPrimary
                        )
                    )
                }
            }
        }
    }
}
