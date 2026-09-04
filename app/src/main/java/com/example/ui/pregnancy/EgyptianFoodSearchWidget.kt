package com.example.ui.pregnancy

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.EgyptianFoodRepository
import com.example.ui.theme.SoftTheme
import com.example.viewmodel.WomanCompanionViewModel

@Composable
fun EgyptianFoodSearchWidget(
    viewModel: WomanCompanionViewModel,
    onNavigateToNutrition: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("all") }
    val presetFoods = remember { EgyptianFoodRepository.presetFoods }

    val categories = listOf(
        "all" to "الكل 🥗",
        "meal" to "وجبات 🥘",
        "drink" to "مشروبات ☕",
        "vegetable" to "خضروات 🥦",
        "fruit" to "فواكه 🍎",
        "snack" to "تسالي 🥨"
    )

    val filteredFoods = remember(searchQuery, selectedCategory) {
        val normQuery = EgyptianFoodRepository.normalizeText(searchQuery.trim().lowercase())
        presetFoods.filter { food ->
            val matchesCategory = (selectedCategory == "all" || food.category == selectedCategory)
            val matchesQuery = normQuery.isBlank() ||
                EgyptianFoodRepository.normalizeText(food.name).contains(normQuery) ||
                EgyptianFoodRepository.normalizeText(food.keywords).contains(normQuery)
            matchesCategory && matchesQuery
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth().testTag("egyptian_food_search_widget"),
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🥗", fontSize = 22.sp)
                    Column {
                        Text(
                            "أطباق متوازنة: دليل المأكولات المصرية 🇪🇬",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.TextWhite
                        )
                        Text("ابحثي في القيمة الغذائية والسعرات للمأكولات المصرية", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                    }
                }
                TextButton(
                    onClick = onNavigateToNutrition,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    modifier = Modifier.testTag("food_search_header_goto_btn")
                ) {
                    Text("صفحة الغذاء 🥗 ↗", color = SoftTheme.SoftPink, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("ابحثي عن طعام مصري (كشري، ملوخية، سبانخ...)", color = SoftTheme.SoftGray, fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SoftTheme.SoftPink,
                    unfocusedBorderColor = SoftTheme.SoftGray.copy(alpha = 0.3f),
                    focusedContainerColor = SoftTheme.DeepSlate,
                    unfocusedContainerColor = SoftTheme.DeepSlate,
                    focusedTextColor = SoftTheme.TextWhite,
                    unfocusedTextColor = SoftTheme.TextWhite
                ),
                singleLine = true
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { (catKey, catLabel) ->
                    FilterChip(
                        selected = selectedCategory == catKey,
                        onClick = { selectedCategory = catKey },
                        label = { Text(catLabel, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SoftTheme.SoftPink,
                            selectedLabelColor = Color.White,
                            containerColor = SoftTheme.DeepSlate,
                            labelColor = SoftTheme.TextWhite
                        )
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                filteredFoods.take(5).forEach { food ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(food.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
                                Text(
                                    "${food.calories} سعرة | بروتين: ${food.protein}g | حديد: ${food.ironMg}mg | كالسيوم: ${food.calciumMg}mg",
                                    fontSize = 10.sp,
                                    color = SoftTheme.SoftGray
                                )
                                if (food.healthBenefits.isNotBlank()) {
                                    Text(food.healthBenefits, fontSize = 10.sp, color = SoftTheme.MintTeal, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                            Button(
                                onClick = {
                                    viewModel.addNutritionMeal(
                                        mealType = "وجبة مصرية",
                                        description = food.name,
                                        calories = food.calories,
                                        iron = food.ironMg,
                                        folate = 0.0,
                                        calcium = food.calciumMg,
                                        omega3 = 0.0,
                                        protein = food.protein,
                                        carbs = food.carbs,
                                        fat = food.fat,
                                        sugar = food.sugarG,
                                        fiber = food.fiberG,
                                        waterBenefit = food.waterBenefitMl,
                                        potassium = food.potassiumMg,
                                        sodium = food.sodiumMg
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("+ إضافة", fontSize = 10.sp, color = Color.White)
                            }
                        }
                    }
                }
            }

            Button(
                onClick = onNavigateToNutrition,
                colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.DeepSlate),
                modifier = Modifier.fillMaxWidth().testTag("goto_nutrition_tab_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("عرض سجل الوجبات والسعرات الكامل 🥗 ↗", color = SoftTheme.MintTeal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
