package com.example.ui.nutrition

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.NutritionLog
import com.example.ui.SoftTheme
import com.example.viewmodel.NutrientTarget

data class MacroDisplayItem(
    val label: String,
    val consumed: Double,
    val target: NutrientTarget?
)

@Composable
fun NutritionNutrientBalanceCard(
    nutrientTargets: Map<String, NutrientTarget>,
    nutritionLogs: List<NutritionLog>,
    modifier: Modifier = Modifier
) {
    val totalProtein = nutritionLogs.sumOf { it.proteinG }
    val totalCarbs = nutritionLogs.sumOf { it.carbsG }
    val totalFat = nutritionLogs.sumOf { it.fatG }
    val totalSugar = nutritionLogs.sumOf { it.sugarG }
    val totalFiber = nutritionLogs.sumOf { it.fiberG }
    val totalIron = nutritionLogs.sumOf { it.ironMg }
    val totalCalcium = nutritionLogs.sumOf { it.calciumMg }
    val totalFolate = nutritionLogs.sumOf { it.folateMcg }
    val totalPotassium = nutritionLogs.sumOf { it.potassiumMg }
    val totalSodium = nutritionLogs.sumOf { it.sodiumMg }
    val totalMagnesium = nutritionLogs.sumOf { it.magnesiumMg }
    val totalVitaminC = nutritionLogs.sumOf { it.vitaminC_Mg }
    val totalVitaminA = nutritionLogs.sumOf { it.vitaminA_Mcg }

    var showAllNutrients by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("📈", fontSize = 20.sp)
                    Column {
                        Text(
                            text = "ميزان العناصر والمتبقي اليومي",
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.TextWhite,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "متابعة دقيقة للمغذيات الكبرى والصغرى",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTheme.SoftGray
                        )
                    }
                }
                TextButton(onClick = { showAllNutrients = !showAllNutrients }) {
                    Text(
                        text = if (showAllNutrients) "عرض أقل" else "عرض الكل 🔍",
                        color = SoftTheme.SoftPink,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Macros section
            Text("الماكروز والمغذيات الكبرى:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = SoftTheme.SoftPink)

            val macrosList = listOf(
                MacroDisplayItem("البروتين (لبناء الأنسجة)", totalProtein, nutrientTargets["protein"]),
                MacroDisplayItem("النشويات (مصدر الطاقة)", totalCarbs, nutrientTargets["carbs"]),
                MacroDisplayItem("الدهون (الامتصاص والذكاء)", totalFat, nutrientTargets["fat"]),
                MacroDisplayItem("السكريات المستهلكة", totalSugar, nutrientTargets["sugar"]),
                MacroDisplayItem("الألياف (لصحة الهضم)", totalFiber, nutrientTargets["fiber"])
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                macrosList.forEach { item ->
                    val targetInfo = item.target
                    if (targetInfo != null) {
                        val pct = if (targetInfo.targetVal > 0) (item.consumed / targetInfo.targetVal).coerceIn(0.0, 1.0).toFloat() else 0f
                        val remaining = targetInfo.targetVal - item.consumed
                        val pctText = "${(pct * 100).toInt()}%"

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(item.label, style = MaterialTheme.typography.bodySmall, color = SoftTheme.TextWhite)
                                Text(
                                    text = "%.1f / %.0f %s (%s)".format(item.consumed, targetInfo.targetVal, targetInfo.unit, pctText),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (targetInfo.isLimit && remaining < 0.0) SoftTheme.RedDanger else SoftTheme.MintTeal,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Progress bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(SoftTheme.DeepSlate)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(pct)
                                        .background(
                                            if (targetInfo.isLimit && remaining < 0.0) SoftTheme.RedDanger else SoftTheme.SoftTeal
                                        )
                                )
                            }

                            // Remaining description text
                            val remainingText = when {
                                targetInfo.isLimit -> {
                                    if (remaining >= 0.0) "متبقي للاستهلاك الآمن: %.1f %s".format(remaining, targetInfo.unit)
                                    else "⚠️ تخطيتِ الحد الآمن بـ %.1f %s!".format(-remaining, targetInfo.unit)
                                }
                                else -> {
                                    if (remaining > 0.0) "المتبقي لتحقيق الهدف: %.1f %s".format(remaining, targetInfo.unit)
                                    else "🎉 تم تلبية احتياجكِ اليومي بالكامل!"
                                }
                            }
                            Text(remainingText, style = MaterialTheme.typography.labelSmall, color = if (remaining < 0.0 && targetInfo.isLimit) SoftTheme.RedDanger else SoftTheme.SoftGray)
                        }
                    }
                }
            }

            // Micros section
            AnimatedVisibility(visible = showAllNutrients) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    HorizontalDivider(color = SoftTheme.DeepSlate, thickness = 1.dp)
                    Text("العناصر الدقيقة والفيتامينات والمعادن:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = SoftTheme.MintTeal)

                    val microsList = listOf(
                        MacroDisplayItem("الحديد (لمنع الأنيميا)", totalIron, nutrientTargets["iron"]),
                        MacroDisplayItem("الكالسيوم (لالعظام والأسنان)", totalCalcium, nutrientTargets["calcium"]),
                        MacroDisplayItem("حمض الفوليك (للنمو العصبي)", totalFolate, nutrientTargets["folate"]),
                        MacroDisplayItem("البوتاسيوم (لتوازن الضغط)", totalPotassium, nutrientTargets["potassium"]),
                        MacroDisplayItem("الماغنسيوم (لتسكين التقلصات)", totalMagnesium, nutrientTargets["magnesium"]),
                        MacroDisplayItem("فيتامين سي (للإمتصاص والمناعة)", totalVitaminC, nutrientTargets["vitaminC"]),
                        MacroDisplayItem("فيتامين أ (لنمو الخلايا والنظر)", totalVitaminA, nutrientTargets["vitaminA"])
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        microsList.forEach { item ->
                            val targetInfo = item.target
                            if (targetInfo != null) {
                                val pct = if (targetInfo.targetVal > 0) (item.consumed / targetInfo.targetVal).coerceIn(0.0, 1.0).toFloat() else 0f
                                val remaining = targetInfo.targetVal - item.consumed
                                val pctText = "${(pct * 100).toInt()}%"

                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(item.label, style = MaterialTheme.typography.bodySmall, color = SoftTheme.TextWhite)
                                        Text(
                                            text = "%.1f / %.0f %s (%s)".format(item.consumed, targetInfo.targetVal, targetInfo.unit, pctText),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SoftTheme.MintTeal,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(SoftTheme.DeepSlate)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth(pct)
                                                .background(SoftTheme.MintTeal)
                                        )
                                    }

                                    val remainingText = if (remaining > 0.0) "المتبقي لتحقيق الهدف: %.1f %s".format(remaining, targetInfo.unit) else "🎉 تم تلبية الاحتياج اليومي!"
                                    Text(remainingText, style = MaterialTheme.typography.labelSmall, color = SoftTheme.SoftGray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
