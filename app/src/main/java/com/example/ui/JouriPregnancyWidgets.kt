package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Appointment
import com.example.viewmodel.PregnancyProgression
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 🌸 Jouri Signature Pregnancy Radial Gauge (مؤشر تقدم الحمل الدائري الفاخر)
 * Inspired by modern luxury health interfaces with 270° horseshoe arc, radial precision ticks,
 * animated dual-tone gradient track, glowing thumb indicator, and interactive center hub.
 */
@Composable
fun JouriPregnancyRadialGauge(
    progression: PregnancyProgression,
    activeMonth: Int,
    activeMonthProgress: Float,
    trimesterColor: Color,
    onFetalClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Total pregnancy is 40 weeks (280 days)
    val totalPregnancyDays = 280f
    val currentDays = (progression.weeks * 7 + progression.daysIntoWeek).coerceIn(1, 280)
    val targetFraction = (currentDays / totalPregnancyDays).coerceIn(0.02f, 1f)

    // Smooth entry animation for gauge
    val animatedFraction by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
        label = "PregnancyProgressAnim"
    )

    // Subtle pulsing animation for baby avatar glow
    val infiniteTransition = rememberInfiniteTransition(label = "PulseTransition")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseGlowAnim"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Phase subtitle
            Text(
                text = if (progression.weeks >= 41) "أنتِ الآن في الشهر العاشر (تخطي موعد الولادة) ⚠️" else "رحلة الأمومة المباركة 🌸",
                style = MaterialTheme.typography.labelLarge,
                color = SoftTheme.SoftGray,
                fontWeight = FontWeight.SemiBold
            )

            // 🎯 Main 270° Radial Arc Gauge with interactive center & side badges
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background Canvas: Radial Ticks + Arc Track + Glowing Thumb
                androidx.compose.foundation.Canvas(
                    modifier = Modifier
                        .size(240.dp)
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val center = Offset(canvasWidth / 2f, canvasHeight / 2f)
                    val arcRadius = (canvasWidth / 2f) - 24.dp.toPx()
                    val startAngle = 135f
                    val totalSweep = 270f
                    val currentSweep = totalSweep * animatedFraction

                    // 1. Draw Outer Precision Radial Ticks (45 ticks)
                    val totalTicks = 45
                    for (i in 0 until totalTicks) {
                        val tickFraction = i.toFloat() / (totalTicks - 1).toFloat()
                        val tickAngle = startAngle + tickFraction * totalSweep
                        val tickRad = Math.toRadians(tickAngle.toDouble())

                        val isPassed = tickFraction <= animatedFraction
                        val tickColor = if (isPassed) {
                            if (tickFraction < 0.5f) SoftTheme.SoftPink.copy(alpha = 0.7f) else SoftTheme.MintTeal.copy(alpha = 0.8f)
                        } else {
                            SoftTheme.DeepSlate.copy(alpha = 0.45f)
                        }

                        val innerTickR = arcRadius + 14.dp.toPx()
                        val outerTickR = arcRadius + (if (i % 5 == 0) 22.dp.toPx() else 18.dp.toPx())

                        val startX = center.x + innerTickR * cos(tickRad).toFloat()
                        val startY = center.y + innerTickR * sin(tickRad).toFloat()
                        val endX = center.x + outerTickR * cos(tickRad).toFloat()
                        val endY = center.y + outerTickR * sin(tickRad).toFloat()

                        drawLine(
                            color = tickColor,
                            start = Offset(startX, startY),
                            end = Offset(endX, endY),
                            strokeWidth = if (i % 5 == 0) 2.5.dp.toPx() else 1.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }

                    // 2. Draw Background Inactive Arc Track
                    drawArc(
                        color = SoftTheme.DeepSlate,
                        startAngle = startAngle,
                        sweepAngle = totalSweep,
                        useCenter = false,
                        topLeft = Offset(center.x - arcRadius, center.y - arcRadius),
                        size = Size(arcRadius * 2, arcRadius * 2),
                        style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // 3. Draw Foreground Active Glowing Gradient Arc
                    val gradientBrush = Brush.sweepGradient(
                        0.0f to SoftTheme.SoftPink,
                        0.35f to Color(0xFFF48FB1),
                        0.7f to Color(0xFF80DEEA),
                        1.0f to SoftTheme.MintTeal,
                        center = center
                    )

                    if (currentSweep > 0.1f) {
                        drawArc(
                            brush = gradientBrush,
                            startAngle = startAngle,
                            sweepAngle = currentSweep,
                            useCenter = false,
                            topLeft = Offset(center.x - arcRadius, center.y - arcRadius),
                            size = Size(arcRadius * 2, arcRadius * 2),
                            style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // 4. Draw Glowing Indicator Thumb (نقطة المؤشر المضيئة)
                        val thumbAngle = startAngle + currentSweep
                        val thumbRad = Math.toRadians(thumbAngle.toDouble())
                        val thumbX = center.x + arcRadius * cos(thumbRad).toFloat()
                        val thumbY = center.y + arcRadius * sin(thumbRad).toFloat()
                        val thumbCenter = Offset(thumbX, thumbY)

                        // Outer Soft Halo Glow
                        drawCircle(
                            color = SoftTheme.MintTeal.copy(alpha = 0.35f),
                            radius = 16.dp.toPx(),
                            center = thumbCenter
                        )
                        // Solid Ring
                        drawCircle(
                            color = SoftTheme.MintTeal,
                            radius = 9.dp.toPx(),
                            center = thumbCenter
                        )
                        // White Core
                        drawCircle(
                            color = Color.White,
                            radius = 4.dp.toPx(),
                            center = thumbCenter
                        )
                    }
                }

                // 🏷️ Left Floating Badge: Percentage (% الإنجاز)
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SoftTheme.DeepSlate.copy(alpha = 0.85f))
                        .border(1.dp, SoftTheme.SoftPink.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${(targetFraction * 100).toInt()}%",
                        color = SoftTheme.TextWhite,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                // 🏷️ Right Floating Badge: Days Remaining (الأيام المتبقية)
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SoftTheme.DeepSlate.copy(alpha = 0.85f))
                        .border(1.dp, SoftTheme.MintTeal.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${progression.remainingDays}",
                            color = SoftTheme.MintTeal,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "يوم متبقي",
                            color = SoftTheme.SoftGray,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // 👶 Center Hub: Week, Fetal Avatar & Milestone Counter
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 36.dp)
                ) {
                    // Trimester tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(trimesterColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "الثلث ${progression.trimester}",
                            color = trimesterColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Huge bold Week Number
                    Text(
                        text = "الأسبوع ${progression.weeks}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = SoftTheme.TextWhite
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Interactive Baby Avatar Button
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(SoftTheme.DeepSlate)
                            .border(
                                width = 2.dp,
                                brush = Brush.radialGradient(
                                    listOf(
                                        SoftTheme.SoftPink.copy(alpha = pulseGlow),
                                        SoftTheme.MintTeal.copy(alpha = pulseGlow * 0.5f)
                                    )
                                ),
                                shape = CircleShape
                            )
                            .clickable { onFetalClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (progression.comparisonIcon.isNotEmpty()) progression.comparisonIcon else "👶",
                            fontSize = 26.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Subtitle: Milestone days
                    val daysUntilNextWeek = 7 - progression.daysIntoWeek
                    Text(
                        text = if (progression.daysIntoWeek == 0) "مكتمل للأسبوع الحالي ✨" else "تطور الجنين ⏱️ $daysUntilNextWeek أيام",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.MintTeal
                    )
                }
            }

            // 💡 Bottom Reassurance Tip Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SoftTheme.DeepSlate.copy(alpha = 0.7f))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("💡", fontSize = 18.sp)
                    Text(
                        text = progression.developmentTip,
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTheme.TextWhite.copy(alpha = 0.9f),
                        lineHeight = 16.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // 📅 Segmented 9-Months Progress Bar
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "مخطط شهور الحمل التسعة 📅",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTheme.SoftGray
                    )
                    Text(
                        text = "أنتِ في " + when(activeMonth) {
                            1 -> "الشهر الأول"
                            2 -> "الشهر الثاني"
                            3 -> "الشهر الثالث"
                            4 -> "الشهر الرابع"
                            5 -> "الشهر الخامس"
                            6 -> "الشهر السادس"
                            7 -> "الشهر السابع"
                            8 -> "الشهر الثامن"
                            9 -> "الشهر التاسع"
                            else -> "الشهر العاشر ⚠️"
                        } + " (${(activeMonthProgress * 100).toInt()}% من الشهر)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = trimesterColor
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val totalSegments = if (progression.weeks >= 41) 10 else 9
                    for (i in 0 until totalSegments) {
                        val segProgress = when {
                            i < activeMonth - 1 -> 1f
                            i == activeMonth - 1 -> activeMonthProgress
                            else -> 0f
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(SoftTheme.DeepSlate)
                        ) {
                            if (segProgress > 0f) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(segProgress)
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(trimesterColor.copy(alpha = 0.7f), trimesterColor)
                                            )
                                        )
                                )
                            }
                        }
                    }
                }
            }

            // Quick Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("الثلث", color = SoftTheme.SoftGray, style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = if (progression.weeks >= 41) "أمان ممتد" else "${progression.trimester}",
                        color = SoftTheme.TextWhite,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                VerticalDivider(modifier = Modifier.height(22.dp).width(1.dp), color = SoftTheme.DeepSlate)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("أيام الأسبوع", color = SoftTheme.SoftGray, style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = "${progression.daysIntoWeek} / 7",
                        color = SoftTheme.TextWhite,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                VerticalDivider(modifier = Modifier.height(22.dp).width(1.dp), color = SoftTheme.DeepSlate)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("موعد الولادة", color = SoftTheme.SoftGray, style = MaterialTheme.typography.bodySmall)
                    val formatted = SimpleDateFormat("dd MMM", Locale.forLanguageTag("ar")).format(Date(progression.dueDate))
                    Text(formatted, color = SoftTheme.TextWhite, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

/**
 * 👶 Baby Development Bento Card (بطاقة تطور الجنين الفاخرة)
 */
@Composable
fun PregnancyBabyDevCard(
    progression: PregnancyProgression,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .testTag("pregnancy_baby_dev_card"),
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardBg),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, SoftTheme.CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SoftTheme.MintAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👶", fontSize = 18.sp)
                    }
                    Column {
                        Text(
                            text = "تطور الجنين ونبضه 🌸",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.TextPrimary
                        )
                        Text(
                            text = "مقارنة الحجم والأعضاء الحيوية",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTheme.TextSecondaryMuted,
                            fontSize = 11.sp
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "التفاصيل",
                    tint = SoftTheme.EmeraldPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Stats & Size comparison row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Heart Rate stat badge
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = SoftTheme.CanvasBg,
                    border = BorderStroke(1.dp, SoftTheme.CardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("❤️", fontSize = 14.sp)
                        Column {
                            Text("نبض الجنين", fontSize = 9.sp, color = SoftTheme.TextSecondaryMuted)
                            Text(
                                text = "120-160 ن/د",
                                color = SoftTheme.WarmCoral,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Size comparison badge
                Surface(
                    modifier = Modifier.weight(1.3f),
                    shape = RoundedCornerShape(12.dp),
                    color = SoftTheme.CanvasBg,
                    border = BorderStroke(1.dp, SoftTheme.CardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(progression.comparisonIcon, fontSize = 16.sp)
                        Column {
                            Text("تشبيه الحجم", fontSize = 9.sp, color = SoftTheme.TextSecondaryMuted)
                            Text(
                                text = progression.comparisonName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SoftTheme.EmeraldPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * ⚡ Daily Activity & Rest Bento Card with Smooth Sparkline (بطاقة النشاط اليومي والرسم البياني المصغر)
 */
@Composable
fun PregnancyDailyActivityCard(
    steps: Int,
    stepGoal: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progressPct = if (stepGoal > 0) (steps.toFloat() / stepGoal.toFloat()).coerceIn(0f, 1f) else 0f

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, SoftTheme.MintTeal.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("⚡", fontSize = 16.sp)
                    Text(
                        text = "النشاط والراحة",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.TextWhite
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "التفاصيل",
                    tint = SoftTheme.MintTeal,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Big Steps Stat
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = String.format(Locale.getDefault(), "%,d", steps),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = SoftTheme.MintTeal
                )
                Text(
                    text = "خطوة",
                    style = MaterialTheme.typography.labelSmall,
                    color = SoftTheme.SoftGray,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }

            // 📈 Smooth Canvas Activity Wave (Sparkline)
            androidx.compose.foundation.Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
            ) {
                val width = size.width
                val height = size.height

                val path = Path()
                path.moveTo(0f, height * 0.75f)
                path.cubicTo(
                    width * 0.25f, height * 0.2f,
                    width * 0.45f, height * 0.95f,
                    width * 0.7f, height * 0.35f
                )
                path.cubicTo(
                    width * 0.85f, height * 0.05f,
                    width * 0.95f, height * 0.6f,
                    width, height * 0.45f
                )

                // Draw stroke line
                drawPath(
                    path = path,
                    color = SoftTheme.MintTeal,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
    }
}

/**
 * 📅 Next Medical Appointment Card (بطاقة موعدك القادم)
 */
@Composable
fun PregnancyNextAppointmentCard(
    appointment: Appointment?,
    onAddOrViewAppointments: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onAddOrViewAppointments() },
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(SoftTheme.SoftPink.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🏥", fontSize = 20.sp)
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "موعدكِ الطبي القادم",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.SoftGray
                    )
                    if (appointment != null) {
                        val formattedDate = SimpleDateFormat("dd MMMM - hh:mm a", Locale.forLanguageTag("ar")).format(Date(appointment.dateTime))
                        Text(
                            text = "${appointment.title} ($formattedDate)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.TextWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Text(
                            text = "لا توجد مواعيد مجدولة قريباً • اضغطي للجدولة",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTheme.SoftPink,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "الانتقال للمواعيد",
                tint = SoftTheme.SoftPink,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
