package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.nutrition.NutritionAddMealDialog
import com.example.ui.nutrition.NutritionCalorieTargetCard
import com.example.ui.nutrition.NutritionNutrientBalanceCard
import com.example.ui.nutrition.NutritionWaterTrackerCard
import com.example.ui.pregnancy.EgyptianFoodSearchWidget
import com.example.viewmodel.WomanCompanionViewModel

@Composable
fun NutritionAndWaterScreen(
    viewModel: WomanCompanionViewModel
) {
    val waterLog by viewModel.todayWaterLogState.collectAsStateWithLifecycle()
    val nutritionLogs by viewModel.todayNutritionLogsState.collectAsStateWithLifecycle()
    val weatherInfo by viewModel.weatherState.collectAsStateWithLifecycle()

    val baseTargetWater = viewModel.getWaterTarget()
    // Dynamic weather water target adjustment: +300ml if temperature exceeds 30°C
    val isHotWeather = (weatherInfo?.temperature ?: 25.0) >= 30.0
    val targetWater = if (isHotWeather) baseTargetWater + 300 else baseTargetWater
    val calorieGoal = viewModel.getCalorieTarget()

    var showAddMealDialog by remember { mutableStateOf(false) }
    val currentWaterAmount = waterLog?.amountMl ?: 0

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                Text(
                    text = "الغذاء والماء اليومي 🥗🥤",
                    style = MaterialTheme.typography.titleLarge,
                    color = SoftTheme.SoftPink,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "موازنة السعرات والعناصر الأساسية لصحتكِ وجنينكِ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SoftTheme.SoftGray
                )
            }

            // Water tracker card
            item {
                NutritionWaterTrackerCard(
                    currentWaterAmount = currentWaterAmount,
                    targetWater = targetWater,
                    isHotWeather = isHotWeather,
                    onAddWater = { amount -> viewModel.addWater(amount) },
                    onResetWater = { viewModel.resetTodayWater() }
                )
            }

            // Calorie Target Card
            item {
                val totalCaloriesConsumed = nutritionLogs.sumOf { it.calories }
                NutritionCalorieTargetCard(
                    totalCaloriesConsumed = totalCaloriesConsumed,
                    calorieGoal = calorieGoal
                )
            }

            // Detailed Nutrient Balance Card
            item {
                NutritionNutrientBalanceCard(
                    nutrientTargets = viewModel.getNutrientTargets(),
                    nutritionLogs = nutritionLogs
                )
            }

            // Live Nutrient Simulator
            item {
                LiveNutrientSimulatorWidget(viewModel = viewModel)
            }

            // Smart Nutrition Advisor
            item {
                SmartNutritionAdvisorCard(viewModel = viewModel)
            }

            // Iron & Calcium Absorption Alert
            item {
                val hasHighIron = nutritionLogs.any { it.ironMg >= 2.0 } || nutritionLogs.any { meal ->
                    val desc = meal.description
                    desc.contains("كبدة") || desc.contains("سبانخ") || desc.contains("لحم") || desc.contains("عدس") || desc.contains("ملوخية")
                }
                val hasHighCalciumOrTea = nutritionLogs.any { it.calciumMg >= 100.0 } || nutritionLogs.any { meal ->
                    val desc = meal.description
                    desc.contains("حليب") || desc.contains("لبن") || desc.contains("جبن") || desc.contains("شاي") || desc.contains("قهوة")
                }

                if (hasHighIron && hasHighCalciumOrTea) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SoftTheme.GoldFasting.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, SoftTheme.GoldFasting)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("⚠️", fontSize = 22.sp)
                                Text(
                                    "تنبيه امتصاص الحديد والكالسيوم",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftTheme.TextWhite
                                )
                            }
                            Text(
                                "لاحظنا وجود وجبة غنية بالحديد مع مصادر للكالسيوم أو الشاي/القهوة. الفصل بين مصادر الحديد (كاللحوم والسبانخ والعدس والملوخية) ومصادر الكالسيوم أو الكافيين (كالألبان والجبن والشاي) بمسافة 1.5 - 2 ساعة يضاعف امتصاص جسمكِ للحديد لتفادي فقر الدم والأنيميا!",
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftTheme.TextWhite,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // Egyptian Food Search Widget
            item {
                EgyptianFoodSearchWidget(viewModel = viewModel)
            }

            // Today Meals Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "وجبات اليوم 🥘",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.TextWhite
                    )

                    Button(
                        onClick = { showAddMealDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("add_meal_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إضافة وجبة", fontSize = 12.sp)
                    }
                }
            }

            if (nutritionLogs.isEmpty()) {
                item {
                    Text(
                        text = "لم يتم تسجيل أي وجبات اليوم بعد.",
                        color = SoftTheme.SoftGray,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                    )
                }
            } else {
                items(nutritionLogs, key = { it.id }) { meal ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                val typeLabel = when(meal.mealType) {
                                    "breakfast" -> "إفطار"
                                    "lunch" -> "غداء"
                                    "dinner" -> "عشاء"
                                    else -> "سناك / خفيف"
                                }
                                Text(
                                    text = "$typeLabel — ${meal.description}",
                                    fontWeight = FontWeight.Bold,
                                    color = SoftTheme.TextWhite
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("سعرات: ${meal.calories}", color = SoftTheme.SoftPink, fontSize = 12.sp)
                                    if (meal.ironMg > 0) Text("حديد: ${meal.ironMg} ملجم", color = SoftTheme.MintTeal, fontSize = 12.sp)
                                    if (meal.folateMcg > 0) Text("فوليك: ${meal.folateMcg} ميكروجم", color = SoftTheme.SoftTeal, fontSize = 12.sp)
                                }
                            }
                            IconButton(onClick = { viewModel.deleteNutritionMeal(meal) }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = SoftTheme.RedDanger)
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Meal Dialog
    if (showAddMealDialog) {
        NutritionAddMealDialog(
            onDismissRequest = { showAddMealDialog = false },
            onSaveMeal = { mealType, description, calories, iron, folate, calcium, omega3, protein, carbs, fat, sugar, fiber, waterBenefit ->
                viewModel.addNutritionMeal(
                    mealType = mealType,
                    description = description,
                    calories = calories,
                    iron = iron,
                    folate = folate,
                    calcium = calcium,
                    omega3 = omega3,
                    protein = protein,
                    carbs = carbs,
                    fat = fat,
                    sugar = sugar,
                    fiber = fiber,
                    waterBenefit = waterBenefit
                )
                showAddMealDialog = false
            }
        )
    }
}
