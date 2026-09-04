package com.example.ui.fitness

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.SoftTheme
import com.example.viewmodel.WomanCompanionViewModel

@Composable
fun StepPedometerDashboard(
    viewModel: WomanCompanionViewModel
) {
    val todayStepsState by viewModel.todayStepLogState.collectAsStateWithLifecycle()
    val allStepLogs by viewModel.allStepLogsState.collectAsStateWithLifecycle()
    val settingsState by viewModel.appLockSettingsState.collectAsStateWithLifecycle()
    val pregnancyState by viewModel.pregnancyState.collectAsStateWithLifecycle()

    val companionName = settingsState?.companionName ?: "جوري"
    val targetSteps = settingsState?.dailyStepTarget ?: 6000
    val currentSteps = todayStepsState?.steps ?: 0
    val progress = if (targetSteps > 0) (currentSteps.toFloat() / targetSteps.toFloat()).coerceIn(0f, 1f) else 0f

    // Calculate calories & distance
    val distanceKm = remember(currentSteps) {
        String.format(java.util.Locale.US, "%.2f", currentSteps * 0.0007)
    }
    val caloriesBurned = remember(currentSteps) {
        (currentSteps * 0.04).toInt()
    }
    val activeMinutes = remember(currentSteps) {
        (currentSteps / 80).toInt()
    }

    // Glowing animation for active sensor
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Active Sensor Header Badge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF141921), RoundedCornerShape(16.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(SoftTheme.MintTeal.copy(alpha = alphaAnim), CircleShape)
                        .border(1.5.dp, SoftTheme.MintTeal, CircleShape)
                )
                Text(
                    text = "جهاز تتبع الخطوات الفعلي نشط ومتصل",
                    color = SoftTheme.MintTeal,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "تتبع فوري تلقائي",
                color = SoftTheme.SoftGray,
                fontSize = 10.sp
            )
        }

        // Modern Neon Circular Steps Ring
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "هدف الخطوات اليومي 🎯",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SoftTheme.TextWhite
                )

                Box(
                    modifier = Modifier.size(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 14.dp.toPx()
                        drawCircle(
                            color = Color(0xFF1B222E),
                            style = Stroke(strokeWidth, cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = SoftTheme.MintTeal,
                            startAngle = -90f,
                            sweepAngle = progress * 360f,
                            useCenter = false,
                            style = Stroke(strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "👣",
                            fontSize = 32.sp
                        )
                        Text(
                            text = "$currentSteps",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = SoftTheme.TextWhite
                        )
                        Text(
                            text = "الهدف: $targetSteps",
                            fontSize = 11.sp,
                            color = SoftTheme.SoftGray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                val pct = (progress * 100).toInt()
                Box(
                    modifier = Modifier
                        .background(SoftTheme.MintTeal.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "أنجزتِ $pct% من هدفك اليومي",
                        color = SoftTheme.MintTeal,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Three Key Metrics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("📏", fontSize = 20.sp)
                    Text("المسافة التقديرية", fontSize = 10.sp, color = SoftTheme.SoftGray, textAlign = TextAlign.Center)
                    Text("$distanceKm كم", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("🔥", fontSize = 20.sp)
                    Text("سعرات محروقة", fontSize = 10.sp, color = SoftTheme.SoftGray, textAlign = TextAlign.Center)
                    Text("$caloriesBurned سعرة", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("⏱️", fontSize = 20.sp)
                    Text("مدة المشي النشط", fontSize = 10.sp, color = SoftTheme.SoftGray, textAlign = TextAlign.Center)
                    Text("$activeMinutes دقيقة", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
                }
            }
        }

        // Canvas-Drawn Weekly Steps Analytics Chart
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "التحليل البياني الأسبوعي للخطوات 📈",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = SoftTheme.TextWhite
                )

                val last7DaysLogs = remember(allStepLogs) {
                    val cal = java.util.Calendar.getInstance()
                    val logsMap = allStepLogs.associateBy { it.date }
                    val list = mutableListOf<Pair<String, Int>>()
                    val daySdf = java.text.SimpleDateFormat("EEE", java.util.Locale.forLanguageTag("ar"))
                    
                    for (i in 6 downTo 0) {
                        cal.timeInMillis = System.currentTimeMillis()
                        cal.add(java.util.Calendar.DAY_OF_YEAR, -i)
                        
                        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                        cal.set(java.util.Calendar.MINUTE, 0)
                        cal.set(java.util.Calendar.SECOND, 0)
                        cal.set(java.util.Calendar.MILLISECOND, 0)
                        
                        val startOfDay = cal.timeInMillis
                        val dayName = daySdf.format(cal.time)
                        val stepsVal = logsMap[startOfDay]?.steps ?: 0
                        list.add(dayName to stepsVal)
                    }
                    list
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(top = 10.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val maxStepsVal = last7DaysLogs.maxOfOrNull { it.second }?.coerceAtLeast(1000) ?: 6000
                        val gridLines = 4
                        val paddingBottom = 24.dp.toPx()
                        val paddingTop = 12.dp.toPx()
                        val paddingLeft = 36.dp.toPx()
                        val chartHeight = h - paddingBottom - paddingTop
                        val chartWidth = w - paddingLeft

                        val textPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.GRAY
                            textSize = 9.dp.toPx()
                            textAlign = android.graphics.Paint.Align.RIGHT
                        }
                        
                        for (i in 0..gridLines) {
                            val ratio = i.toFloat() / gridLines
                            val y = h - paddingBottom - (ratio * chartHeight)
                            val gridValue = (ratio * maxStepsVal).toInt()
                            
                            drawLine(
                                color = Color.Gray.copy(alpha = 0.15f),
                                start = Offset(paddingLeft, y),
                                end = Offset(w, y),
                                strokeWidth = 1.dp.toPx()
                            )
                            drawContext.canvas.nativeCanvas.drawText(
                                "$gridValue",
                                paddingLeft - 6.dp.toPx(),
                                y + 3.dp.toPx(),
                                textPaint
                            )
                        }

                        val barCount = last7DaysLogs.size
                        val barSpacing = chartWidth / barCount
                        val barWidth = barSpacing * 0.4f
                        
                        last7DaysLogs.forEachIndexed { index, (dayName, stepsVal) ->
                            val ratio = stepsVal.toFloat() / maxStepsVal.toFloat()
                            val barHeightVal = ratio * chartHeight
                            val x = paddingLeft + (index * barSpacing) + (barSpacing - barWidth) / 2f
                            val y = h - paddingBottom - barHeightVal
                            
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        SoftTheme.MintTeal,
                                        SoftTheme.MintTeal.copy(alpha = 0.3f)
                                    )
                                ),
                                topLeft = Offset(x, y),
                                size = androidx.compose.ui.geometry.Size(barWidth, barHeightVal),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                            )

                            val labelPaint = android.graphics.Paint().apply {
                                color = android.graphics.Color.WHITE
                                textSize = 9.dp.toPx()
                                textAlign = android.graphics.Paint.Align.CENTER
                            }
                            drawContext.canvas.nativeCanvas.drawText(
                                dayName,
                                x + barWidth / 2f,
                                h - 6.dp.toPx(),
                                labelPaint
                            )
                        }
                    }
                }
            }
        }

        // Medical Advice from Jouri
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, SoftTheme.MintTeal.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("💡", fontSize = 18.sp)
                    Text(
                        text = "توجيهات صحية حية من رفيقتكِ $companionName:",
                        color = SoftTheme.MintTeal,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                val customAdviceText = remember(pregnancyState, currentSteps, targetSteps) {
                    when {
                        pregnancyState != null -> {
                            val week = viewModel.getPregnancyProgression()?.weeks ?: 1
                            val base = "أنتِ الآن في الأسبوع $week من الحمل. "
                            if (currentSteps < 3000) {
                                base + "الحفاظ على القليل من المشي اليومي (حوالي 3000-4000 خطوة) يساعد في تنشيط مفاصل الحوض وتخفيف آلام الظهر ويسهل عملية الولادة لاحقاً. هل نمشي قليلاً؟ 🤰👣"
                            } else if (currentSteps < targetSteps) {
                                base + "رائع جداً! لقد سرتِ $currentSteps خطوة اليوم. واصلي بهدوء وخذي فترات راحة كل 10 دقائق لشرب المياه والترطيب لتجنب انخفاض السكر 🍃🌸"
                            } else {
                                base + "مذهل! حققتِ هدفكِ اليومي بالكامل 👏! أنصحكِ الآن برفع قدميكِ لأعلى والاسترخاء لشحن طاقتكِ وحماية قدميكِ من التورّم."
                            }
                        }
                        else -> {
                            if (currentSteps < 2000) {
                                "المشي الخفيف والمستمر هو أبسط تمرين لاستعادة نشاط عضلات البطن وقاع الحوض بلطف ومقاومة تقلبات المزاج بعد الولادة. ابدئي بـ 15 دقيقة فقط اليوم 🌸"
                            } else if (currentSteps < targetSteps) {
                                "مجهود رائع اليوم! المشي المنتظم يرفع كفاءة القلب ويحرق السعرات الزائدة وينشط عضلاتك العميقة. تذكري سحب بطنك للداخل أثناء خطواتكِ لتنشيط الكور 💨✨"
                            } else {
                                "تفوقتِ على نفسكِ اليوم! تحقيق هدف $targetSteps خطوة يعزز هرمون الإندورفين ويجعلكِ تشعرين بالراحة والنوم العميق الليلة 🥰."
                            }
                        }
                    }
                }

                Text(
                    text = customAdviceText,
                    color = SoftTheme.TextWhite,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }

        // Interactive Steps simulation buttons
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "محاكاة الخطوات السريعة للاختبار والتحقق:",
                    color = SoftTheme.SoftGray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.addSteps(100) },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.MintTeal.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("+100 خطوة 👟", color = SoftTheme.MintTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.addSteps(500) },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.MintTeal.copy(alpha = 0.25f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("+500 خطوة 🏃‍♀️", color = SoftTheme.MintTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
