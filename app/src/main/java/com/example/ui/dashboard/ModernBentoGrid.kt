package com.example.ui.dashboard

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Female
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.SoftTheme
import com.example.viewmodel.WomanCompanionViewModel
import java.util.Calendar

/**
 * شبكة البينتو المتوازنة 2x2 Bento Grid
 * تضم:
 * 1. بطاقة شرب الماء (مع زر إضافة سريع)
 * 2. بطاقة الغذاء والسعرات (مع تفصيل الماكروز)
 * 3. بطاقة النوم والراحة (مع مؤشر الجودة)
 * 4. بطاقة الأعراض والنشاط
 *
 * بالإضافة إلى:
 * - بطاقة صحة المرأة والدورة الشهرية (Women's Health Card)
 * - بطاقة رؤى جوري الذكية (Jouri AI Insights)
 */
@Composable
fun ModernBentoGrid(
    viewModel: WomanCompanionViewModel,
    onNavigateToWater: () -> Unit = {},
    onNavigateToNutrition: () -> Unit = {},
    onNavigateToSleep: () -> Unit = {},
    onNavigateToSymptoms: () -> Unit = {},
    onNavigateToPeriod: () -> Unit = {},
    onNavigateToChat: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Observe today's data from ViewModel
    val allWaterLogs by viewModel.allWaterLogsState.collectAsState()
    val allNutritionLogs by viewModel.allNutritionLogsState.collectAsState()
    val allSleepLogs by viewModel.allSleepLogsState.collectAsState()
    val symptomLogs by viewModel.symptomLogsState.collectAsState()
    val pregnancy by viewModel.pregnancyState.collectAsState()
    val periodLogs by viewModel.periodLogsState.collectAsState()
    val progression = viewModel.getPregnancyProgression()
    val isPregnant = pregnancy?.isPregnant == true

    val todayStart = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    // Water calculations
    val todayWaterMl = remember(allWaterLogs, todayStart) {
        allWaterLogs.filter { it.date >= todayStart }.sumOf { it.amountMl }
    }
    val targetWaterMl = viewModel.getWaterTarget()
    val waterPercent = (todayWaterMl.toFloat() / targetWaterMl.toFloat()).coerceIn(0f, 1f)

    // Nutrition calculations
    val todayCalories = remember(allNutritionLogs, todayStart) {
        allNutritionLogs.filter { it.date >= todayStart }.sumOf { it.calories }
    }
    val todayProtein = remember(allNutritionLogs, todayStart) {
        allNutritionLogs.filter { it.date >= todayStart }.sumOf { it.proteinG }
    }
    val todayCarbs = remember(allNutritionLogs, todayStart) {
        allNutritionLogs.filter { it.date >= todayStart }.sumOf { it.carbsG }
    }
    val todayFat = remember(allNutritionLogs, todayStart) {
        allNutritionLogs.filter { it.date >= todayStart }.sumOf { it.fatG }
    }
    val targetCalories = viewModel.getCalorieTarget().target
    val caloriesPercent = if (targetCalories > 0) (todayCalories.toFloat() / targetCalories.toFloat()).coerceIn(0f, 1f) else 0f

    // Sleep calculations
    val latestSleep = remember(allSleepLogs, todayStart) {
        allSleepLogs.filter { it.date >= todayStart }.maxByOrNull { it.date }
            ?: allSleepLogs.maxByOrNull { it.date }
    }
    val sleepDurationHours = if (latestSleep != null && latestSleep.endTime > latestSleep.startTime) {
        (latestSleep.endTime - latestSleep.startTime) / (1000.0 * 60.0 * 60.0)
    } else 0.0
    val sleepHoursText = if (latestSleep != null) {
        String.format(java.util.Locale.ENGLISH, "%.1f", sleepDurationHours)
    } else "0.0"
    val sleepQualityText = if (latestSleep != null) {
        when {
            latestSleep.qualityScore >= 80 -> "عميق ومريح 🌙"
            latestSleep.qualityScore >= 60 -> "جيد ومستقر ✨"
            else -> "خفيف أو متقطع 💤"
        }
    } else "سجلي نومكِ 🌙"

    // Symptoms count
    val todaySymptomsCount = remember(symptomLogs, todayStart) {
        symptomLogs.count { it.date >= todayStart }
    }
    val symptomsStatusText = if (todaySymptomsCount > 0) "$todaySymptomsCount أعراض مسجلة" else "مستقرة وجيدة ✨"

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- 2x2 Bento Grid ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card 1: Water Intake Bento
            WaterBentoCard(
                currentMl = todayWaterMl,
                targetMl = targetWaterMl,
                progress = waterPercent,
                onAddGlass = {
                    viewModel.addWater(250)
                    Toast.makeText(context, "تمت إضافة كوب ماء (250 مل) 💧", Toast.LENGTH_SHORT).show()
                },
                onClick = onNavigateToWater,
                modifier = Modifier.weight(1f)
            )

            // Card 2: Nutrition Bento
            NutritionBentoCard(
                calories = todayCalories,
                targetCalories = targetCalories,
                progress = caloriesPercent,
                proteinG = todayProtein,
                carbsG = todayCarbs,
                fatG = todayFat,
                onClick = onNavigateToNutrition,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card 3: Sleep Bento
            SleepBentoCard(
                sleepHours = sleepHoursText,
                quality = sleepQualityText,
                onClick = onNavigateToSleep,
                modifier = Modifier.weight(1f)
            )

            // Card 4: Symptoms Bento
            SymptomsBentoCard(
                status = symptomsStatusText,
                loggedCount = todaySymptomsCount,
                onClick = onNavigateToSymptoms,
                modifier = Modifier.weight(1f)
            )
        }

        // --- Women's Health & Cycle Banner (يظهر فقط في وضع الدورة الشهرية لتجنب تكرار كروت الحمل في وضع الحمل) ---
        if (!isPregnant) {
            WomensHealthCard(
                pregnancy = pregnancy,
                periodLogs = periodLogs,
                onClick = onNavigateToPeriod,
                progression = progression
            )
        }

        // --- Jouri AI Smart Insights (تظهر لجميع الأوضاع مع رؤى مخصصة حسب بيانات اليوم) ---
        JouriAiInsightsCard(
            onOpenChat = onNavigateToChat,
            waterFraction = waterPercent,
            sleepDurationHours = sleepDurationHours,
            caloriesFraction = caloriesPercent,
            symptomsCount = todaySymptomsCount
        )
    }
}

/**
 * بطاقة الماء اليومي
 */
@Composable
fun WaterBentoCard(
    currentMl: Int,
    targetMl: Int,
    progress: Float,
    onAddGlass: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .border(1.dp, SoftTheme.CardBorder, RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .testTag("water_bento_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SoftTheme.WaterBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = SoftTheme.WaterBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Quick Add Glass Button (Haptic enabled)
                Surface(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onAddGlass()
                    },
                    shape = CircleShape,
                    color = SoftTheme.WaterBg,
                    border = BorderStroke(1.dp, SoftTheme.WaterBlue.copy(alpha = 0.25f)),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "إضافة كوب ماء",
                            tint = SoftTheme.WaterBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "الماء اليومي",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    color = SoftTheme.TextSecondaryMuted,
                    fontWeight = FontWeight.Medium
                )
            )

            Spacer(modifier = Modifier.height(2.dp))

            val currentLiters = String.format(java.util.Locale.US, "%.1f", currentMl / 1000f)
            val targetLiters = String.format(java.util.Locale.US, "%.1f", targetMl / 1000f)
            Text(
                text = "$currentLiters / $targetLiters لتر",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = SoftTheme.TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(SoftTheme.WaterBg)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(SoftTheme.WaterBlue)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${(progress * 100).toInt()}% تم إنجازه",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        color = SoftTheme.WaterBlue,
                        fontWeight = FontWeight.Bold
                    )
                )

                // Dedicated Quick Add Pill
                Surface(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onAddGlass()
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = SoftTheme.WaterBlue.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, SoftTheme.WaterBlue.copy(alpha = 0.3f)),
                    modifier = Modifier.height(24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = SoftTheme.WaterBlue,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "+250مل",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.WaterBlue
                        )
                    }
                }
            }
        }
    }
}

/**
 * بطاقة الغذاء والسعرات والماكروز
 */
@Composable
fun NutritionBentoCard(
    calories: Int,
    targetCalories: Int,
    progress: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    proteinG: Double = 0.0,
    carbsG: Double = 0.0,
    fatG: Double = 0.0
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .border(1.dp, SoftTheme.CardBorder, RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .testTag("nutrition_bento_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SoftTheme.FoodBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        tint = SoftTheme.FoodOrange,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.FoodOrange
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "السعرات والماكروز",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    color = SoftTheme.TextSecondaryMuted,
                    fontWeight = FontWeight.Medium
                )
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "$calories / $targetCalories سعرة",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = SoftTheme.TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Mini Macro bars (Protein, Carbs, Fats) dynamically proportioned if logged
            val totalMacros = (proteinG + carbsG + fatG).toFloat()
            val proteinWeight = if (totalMacros > 0f) (proteinG.toFloat() / totalMacros).coerceIn(0.1f, 0.8f) else 0.35f
            val carbsWeight = if (totalMacros > 0f) (carbsG.toFloat() / totalMacros).coerceIn(0.1f, 0.8f) else 0.45f
            val fatWeight = if (totalMacros > 0f) (fatG.toFloat() / totalMacros).coerceIn(0.1f, 0.8f) else 0.20f

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Protein
                Box(
                    modifier = Modifier
                        .weight(proteinWeight)
                        .height(5.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF4CAF50))
                )
                // Carbs
                Box(
                    modifier = Modifier
                        .weight(carbsWeight)
                        .height(5.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(SoftTheme.FoodOrange)
                )
                // Fats
                Box(
                    modifier = Modifier
                        .weight(fatWeight)
                        .height(5.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFFAB47BC))
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "بروتين • كارب • دهون",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    color = SoftTheme.TextSecondaryMuted
                )
            )
        }
    }
}

/**
 * بطاقة النوم والراحة
 */
@Composable
fun SleepBentoCard(
    sleepHours: String,
    quality: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .border(1.dp, SoftTheme.CardBorder, RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .testTag("sleep_bento_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SoftTheme.SleepBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Bedtime,
                    contentDescription = null,
                    tint = SoftTheme.SleepPurple,
                    modifier = Modifier.size(19.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "ساعات النوم",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    color = SoftTheme.TextSecondaryMuted,
                    fontWeight = FontWeight.Medium
                )
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "$sleepHours ساعة",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = SoftTheme.TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = quality,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    color = SoftTheme.SleepPurple,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

/**
 * بطاقة الأعراض والنشاط
 */
@Composable
fun SymptomsBentoCard(
    status: String,
    loggedCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .border(1.dp, SoftTheme.CardBorder, RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .testTag("symptoms_bento_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SoftTheme.SymptomsBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = SoftTheme.SymptomsPurple,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "الأعراض والصحة",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    color = SoftTheme.TextSecondaryMuted,
                    fontWeight = FontWeight.Medium
                )
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = status,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = SoftTheme.TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$loggedCount مؤشرات طبيعية اليوم",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    color = SoftTheme.SymptomsPurple,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

/**
 * بطاقة صحة المرأة والدورة الشهرية (Women's Health Card)
 */
@Composable
fun WomensHealthCard(
    pregnancy: com.example.data.PregnancyEntity?,
    periodLogs: List<com.example.data.PeriodLog>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    progression: com.example.viewmodel.PregnancyProgression? = null
) {
    val isPregnant = pregnancy?.isPregnant == true
    val headerTitle = if (isPregnant) "متابعة الحمل ونمو الجنين" else "صحة المرأة والدورة الشهرية"
    val subText = if (isPregnant) {
        if (progression != null) {
            "أنتِ في الأسبوع ${progression.weeks} • طفلكِ بحجم ${progression.comparisonName} ${progression.comparisonIcon}"
        } else {
            "متابعة دقيقة لرحلة الأمومة وتطور الجنين 🌸"
        }
    } else {
        val lastLog = periodLogs.maxByOrNull { it.startDate }
        if (lastLog != null) {
            val cycleDay = (((System.currentTimeMillis() - lastLog.startDate) / (24L * 60 * 60 * 1000)).toInt() + 1).coerceAtLeast(1)
            "اليوم $cycleDay من الدورة • صحة وتوازن مستمر 🌸"
        } else {
            "سجلي دورتكِ الشهرية لتفعيل التنبؤ الذكي 🌸"
        }
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .border(1.dp, SoftTheme.CardBorder, RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .testTag("womens_health_card")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            SoftTheme.WomenPinkBgColor.copy(alpha = 0.5f),
                            SoftTheme.CardBg
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
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
                            .background(SoftTheme.WomenPinkBgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Female,
                            contentDescription = null,
                            tint = SoftTheme.WomenPinkColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = headerTitle,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = SoftTheme.TextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subText,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                color = SoftTheme.WomenPinkColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = null,
                    tint = SoftTheme.TextSecondaryMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * توليد رؤية ذكية ديناميكية مبنية على بيانات اليوم الفعلية
 */
fun generateJouriDailyInsight(
    waterFraction: Float,
    sleepDurationHours: Double,
    caloriesFraction: Float,
    symptomsCount: Int
): String {
    return when {
        symptomsCount > 0 -> {
            "«سجلتِ $symptomsCount من الأعراض اليوم. احرصي على أخذ قسط كافٍ من الراحة وشرب السوائل الدافئة. إذا استمر أي انزعاج، استشيري طبيبكِ أو اسأليني 🌸»"
        }
        sleepDurationHours in 0.1..5.9 -> {
            val formattedSleep = String.format(java.util.Locale.ENGLISH, "%.1f", sleepDurationHours)
            "«لاحظت أن ساعات نومكِ كانت $formattedSleep ساعة فقط. حاولي أخذ قيلولة قصيرة لمدة 20 دقيقة ظهراً وتجنبي المنبهات مساءً لدعم نشاطكِ 🌙»"
        }
        waterFraction in 0.01f..0.49f -> {
            val percent = (waterFraction * 100).toInt()
            "«مستوى ترطيبكِ ما زال منخفضاً اليوم ($percent%)! احرصي على شرب رشفات منتظمة من الماء للحفاظ على طاقتكِ وحيويتكِ وتجنب الصداع 💧»"
        }
        waterFraction == 0f && sleepDurationHours == 0.0 && caloriesFraction == 0f -> {
            "«يوم جديد مفعم بالعافية! ابدئي يومكِ بكوب ماء منعش وسجلي بياناتكِ الصحية لأرافقكِ بإرشادات مخصصة تناسب يومكِ خطوة بخطوة 🌸»"
        }
        caloriesFraction in 0.01f..0.4f -> {
            "«تغذيتكِ هي مصدر طاقتكِ! احرصي على تناول وجبة خفيفة متوازنة غنية بالبروتين والألياف مثل المكسرات أو الزبادي للحفاظ على استقرار سكر الدم 🥗»"
        }
        waterFraction >= 0.75f && sleepDurationHours >= 7.0 -> {
            "«ما شاء الله، إنجازكِ الصحي اليوم رائع! توازن ممتاز بين ساعات النوم والترطيب المستمر. واصلي هذا الالتزام الجميل وخذي استراحة تمدد خفيفة ✨»"
        }
        waterFraction >= 0.75f -> {
            "«مستوى ترطيبكِ رائع جداً اليوم! جسمكِ منتعش ومتوازن. تذكري أخذ استراحة تمدد وتناول وجبة خفيفة صحية لتجديد نشاطكِ 💧»"
        }
        else -> {
            "«خطواتكِ الصحية اليوم تسير بتناغم رائع. استمري في شرب الماء وتناول وجبات متوازنة، وأنا هنا بجانبكِ دائماً لأي استشارة 🌸»"
        }
    }
}

/**
 * بطاقة رؤى جوري الذكية (Jouri AI Insights Card)
 */
@Composable
fun JouriAiInsightsCard(
    onOpenChat: () -> Unit,
    modifier: Modifier = Modifier,
    waterFraction: Float = 0f,
    sleepDurationHours: Double = 0.0,
    caloriesFraction: Float = 0f,
    symptomsCount: Int = 0
) {
    val insightText = remember(waterFraction, sleepDurationHours, caloriesFraction, symptomsCount) {
        generateJouriDailyInsight(
            waterFraction = waterFraction,
            sleepDurationHours = sleepDurationHours,
            caloriesFraction = caloriesFraction,
            symptomsCount = symptomsCount
        )
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .border(1.dp, SoftTheme.MintAccentBorder, RoundedCornerShape(22.dp))
            .testTag("jouri_ai_insights_card")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            SoftTheme.JouriBg.copy(alpha = 0.5f),
                            SoftTheme.CardBg
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, SoftTheme.JouriTurquoise, CircleShape)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_jouri_bot),
                                contentDescription = "جوري AI",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "رؤى جوري الذكية لليوم",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = SoftTheme.TextPrimary
                                    )
                                )
                                Icon(
                                    imageVector = Icons.Outlined.AutoAwesome,
                                    contentDescription = null,
                                    tint = SoftTheme.JouriTurquoise,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = "مستشارة الصحة والعافية الشخصية",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = SoftTheme.TextSecondaryMuted
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = insightText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = SoftTheme.TextPrimary,
                        fontWeight = FontWeight.Normal
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onOpenChat,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.JouriTurquoise),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "اسألي جوري الآن",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
