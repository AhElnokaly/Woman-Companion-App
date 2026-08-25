package com.example.ui.maonaty

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MaonatyInventoryItem
import com.example.data.NutritionLog
import com.example.ui.SoftTheme
import com.example.viewmodel.PregnancyProgression

@Composable
fun SmartRecipesTab(
    inventory: List<MaonatyInventoryItem>,
    onAddMissingToShopping: (String, String, Double, String, Double) -> Unit
) {
    val recipes = remember(inventory) {
        standardRecipes.map { recipe ->
            val matchStatuses = recipe.ingredients.map { getIngredientMatchStatus(it, inventory) }
            val matchedCount = matchStatuses.count { it.isMatched }
            val matchPercentage = if (recipe.ingredients.isEmpty()) 0 else (matchedCount * 100) / recipe.ingredients.size
            RecipeWithMatchStatus(recipe, matchStatuses, matchPercentage)
        }.sortedByDescending { it.matchPercentage }
    }

    var expandedRecipeName by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.SoftTeal.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = SoftTheme.SoftTeal)
                    Text(
                        text = "محرك المطبخ يطابق الوصفات بنشاط مع خزائنك ومخزونك المسجل حالياً في علامة التبويب الأولى live!",
                        fontSize = 12.sp,
                        color = SoftTheme.TextWhite,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        items(recipes) { item ->
            val recipe = item.recipe
            val isExpanded = expandedRecipeName == recipe.name

            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedRecipeName = if (isExpanded) null else recipe.name }
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = recipe.name, fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite, fontSize = 15.sp)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Text("⏱️ ${recipe.timeMinutes} دقيقة", fontSize = 11.sp, color = SoftTheme.SoftGray)
                                Text("•", fontSize = 11.sp, color = SoftTheme.SoftGray)
                                Text("📊 ${recipe.difficulty}", fontSize = 11.sp, color = SoftTheme.SoftGray)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    when {
                                        item.matchPercentage >= 100 -> SoftTheme.SoftTeal.copy(alpha = 0.2f)
                                        item.matchPercentage >= 50 -> SoftTheme.GoldFasting.copy(alpha = 0.2f)
                                        else -> SoftTheme.RedDanger.copy(alpha = 0.2f)
                                    }
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "تطابق ${item.matchPercentage}%",
                                color = when {
                                    item.matchPercentage >= 100 -> SoftTheme.SoftTeal
                                    item.matchPercentage >= 50 -> SoftTheme.GoldFasting
                                    else -> SoftTheme.RedDanger
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    AnimatedVisibility(visible = isExpanded) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            HorizontalDivider(color = SoftTheme.DeepSlate, thickness = 1.dp)

                            Text("المكونات والمطابقة:", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite, fontSize = 13.sp)
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                item.matchStatuses.forEach { ing ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (ing.isMatched) Icons.Default.CheckCircle else Icons.Default.Warning,
                                                contentDescription = null,
                                                tint = if (ing.isMatched) SoftTheme.SoftTeal else SoftTheme.RedDanger,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = ing.name,
                                                color = if (ing.isMatched) SoftTheme.TextWhite else SoftTheme.SoftGray,
                                                fontSize = 13.sp
                                            )
                                        }

                                        Text(
                                            text = if (ing.isMatched) "متوفر (${ing.availableQty} ${ing.unit})" else "ناقص ${ing.missingQty} ${ing.unit}",
                                            color = if (ing.isMatched) SoftTheme.SoftTeal else SoftTheme.RedDanger,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(color = SoftTheme.DeepSlate, thickness = 1.dp)

                            Text("طريقة التحضير:", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite, fontSize = 13.sp)
                            Text(
                                text = recipe.instructions,
                                color = SoftTheme.TextWhite,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )

                            val missing = item.matchStatuses.filter { !it.isMatched }
                            if (missing.isNotEmpty()) {
                                Button(
                                    onClick = {
                                        missing.forEach { ing ->
                                            onAddMissingToShopping(
                                                ing.name,
                                                "خضار وفواكه",
                                                ing.missingQty,
                                                ing.unit,
                                                15.0
                                            )
                                        }
                                        expandedRecipeName = null
                                        ScaffoldMessengerHelper.showToast(context, "تم إضافة ${missing.size} سلع ناقصة لقائمة المشتريات!")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("إضافة النواقص (${missing.size}) لقائمة التسوق 🛒", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            } else {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SoftTheme.SoftTeal.copy(alpha = 0.15f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "كل المكونات متوفرة في مطبخك! ابدئي الطهي فوراً وبالهناء والشفاء! 😍🍳",
                                        color = SoftTheme.SoftTeal,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(10.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PregnancyNutritionGuidanceCard(
    pregnancyProgression: PregnancyProgression,
    todayLogs: List<NutritionLog>
) {
    val target = remember(pregnancyProgression.trimester) {
        PregnancyNutritionReference.getTargetForTrimester(pregnancyProgression.trimester)
    }

    val totalCalories = remember(todayLogs) { todayLogs.sumOf { it.calories } }
    val totalSodium = remember(todayLogs) { todayLogs.sumOf { it.sodiumMg } }
    val totalIron = remember(todayLogs) { todayLogs.sumOf { it.ironMg } }
    val totalFolate = remember(todayLogs) { todayLogs.sumOf { it.folateMcg } }

    Card(
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFF38B2AC).copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("pregnancy_nutrition_guidance_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF38B2AC).copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🥗", fontSize = 18.sp)
                    }
                    Column {
                        Text(
                            text = "التوجيه التغذوي لمرحلة الحمل 🤰",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.TextWhite
                        )
                        Text(
                            text = target.description,
                            fontSize = 10.sp,
                            color = Color(0xFFA0AEC0),
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                NutrientProgressBar(
                    label = "السعرات اليومية",
                    currentVal = totalCalories.toDouble(),
                    targetVal = target.totalCaloriesTargetKcal.toDouble(),
                    unit = "سعرة",
                    extraInfo = if (target.extraCaloriesKcal > 0) "+${target.extraCaloriesKcal} سعرة إضافية للثلث ${target.trimester}" else "السعرات الأساسية للثلث 1",
                    accentColor = Color(0xFFED8936)
                )

                val isSodiumExceeded = totalSodium > target.maxSodiumMg
                NutrientProgressBar(
                    label = "الصوديوم (حد أقصى وقائي)",
                    currentVal = totalSodium,
                    targetVal = target.maxSodiumMg,
                    unit = "ملجم",
                    isExceeded = isSodiumExceeded,
                    extraInfo = if (isSodiumExceeded) "⚠️ تجاوزت الحد الأقصى للصوديوم اليوم (${totalSodium.toInt()} ملجم)!" else "الصوديوم اليوم: ${totalSodium.toInt()} من ${target.maxSodiumMg.toInt()} ملجم المسموح بها",
                    accentColor = if (isSodiumExceeded) SoftTheme.RedDanger else Color(0xFF38B2AC)
                )

                NutrientProgressBar(
                    label = "الحديد اليومي",
                    currentVal = totalIron,
                    targetVal = target.targetIronMg,
                    unit = "ملجم",
                    extraInfo = "الهدف: ${target.targetIronMg} ملجم لتجنب أنيميا الحمل",
                    accentColor = Color(0xFF9F7AEA)
                )

                NutrientProgressBar(
                    label = "حمض الفوليك",
                    currentVal = totalFolate,
                    targetVal = target.targetFolicAcidMcg,
                    unit = "مكجم",
                    extraInfo = "الهدف: ${target.targetFolicAcidMcg.toInt()} مكجم لتطور الجهاز العصبي",
                    accentColor = SoftTheme.SoftPink
                )
            }
        }
    }
}

@Composable
fun NutrientProgressBar(
    label: String,
    currentVal: Double,
    targetVal: Double,
    unit: String,
    isExceeded: Boolean = false,
    extraInfo: String = "",
    accentColor: Color
) {
    val progress = if (targetVal > 0) (currentVal / targetVal).coerceIn(0.0, 1.0).toFloat() else 0f
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SoftTheme.TextWhite
            )
            Text(
                text = "${currentVal.toInt()} / ${targetVal.toInt()} $unit",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isExceeded) SoftTheme.RedDanger else accentColor
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = if (isExceeded) SoftTheme.RedDanger else accentColor,
            trackColor = SoftTheme.DeepSlate
        )
        if (extraInfo.isNotEmpty()) {
            Text(
                text = extraInfo,
                fontSize = 9.sp,
                color = if (isExceeded) SoftTheme.RedDanger else Color(0xFFA0AEC0)
            )
        }
    }
}
