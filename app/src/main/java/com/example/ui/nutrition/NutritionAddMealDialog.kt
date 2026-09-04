package com.example.ui.nutrition

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.EgyptianFoodEntity
import com.example.data.EgyptianFoodRepository
import com.example.ui.SoftTheme

@Composable
fun NutritionAddMealDialog(
    onDismissRequest: () -> Unit,
    onSaveMeal: (
        mealType: String,
        description: String,
        calories: Int,
        iron: Double,
        folate: Double,
        calcium: Double,
        omega3: Double,
        protein: Double,
        carbs: Double,
        fat: Double,
        sugar: Double,
        fiber: Double,
        waterBenefit: Int
    ) -> Unit
) {
    var activeDialogTab by remember { mutableStateOf("smart") }
    var selectedCategoryKey by remember { mutableStateOf("meal") }
    var selectedFood by remember { mutableStateOf<EgyptianFoodEntity?>(null) }
    var quantityValue by remember { mutableStateOf(1) }
    var sugarSpoons by remember { mutableStateOf(0) }
    var addOliveOil by remember { mutableStateOf(false) }
    var addLemon by remember { mutableStateOf(false) }

    var mealTypeInput by remember { mutableStateOf("breakfast") }
    var mealDescInput by remember { mutableStateOf("") }
    var calorieInput by remember { mutableStateOf("") }

    var ironInput by remember { mutableStateOf("") }
    var folateInput by remember { mutableStateOf("") }
    var calciumInput by remember { mutableStateOf("") }
    var omega3Input by remember { mutableStateOf("") }

    var proteinInput by remember { mutableStateOf("") }
    var carbsInput by remember { mutableStateOf("") }
    var fatInput by remember { mutableStateOf("") }
    var sugarInput by remember { mutableStateOf("") }
    var fiberInput by remember { mutableStateOf("") }
    var waterBenefitInput by remember { mutableStateOf("") }

    val presetFoods = remember { EgyptianFoodRepository.presetFoods }
    val categories = listOf(
        "meal" to "وجبات رئيسية 🥘",
        "drink" to "مشروبات ☕",
        "vegetable" to "خضار وسلطة 🥗",
        "fruit" to "فواكه 🍎",
        "snack" to "تسالي 🥨"
    )

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "إضافة وجبة طعام 🥗",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SoftTheme.SoftPink
                )

                // Tab selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SoftTheme.DeepSlate)
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Button(
                        onClick = { activeDialogTab = "smart" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeDialogTab == "smart") SoftTheme.SoftPink else Color.Transparent,
                            contentColor = if (activeDialogTab == "smart") SoftTheme.DeepSlate else SoftTheme.TextWhite
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text("منتقي الأغذية الذكي 🥚", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { activeDialogTab = "manual" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeDialogTab == "manual") SoftTheme.SoftPink else Color.Transparent,
                            contentColor = if (activeDialogTab == "manual") SoftTheme.DeepSlate else SoftTheme.TextWhite
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text("إدخال يدوي ✍️", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Meal Type row (Common for both)
                Text("تصنيف وقت الوجبة:", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val types = listOf("breakfast" to "إفطار", "lunch" to "غداء", "dinner" to "عشاء", "snack" to "خفيف")
                    types.forEach { (key, label) ->
                        val isSel = mealTypeInput == key
                        Button(
                            onClick = { mealTypeInput = key },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSel) SoftTheme.SoftPink else SoftTheme.DeepSlate,
                                contentColor = if (isSel) SoftTheme.DeepSlate else SoftTheme.TextWhite
                            )
                        ) {
                            Text(label, fontSize = 10.sp)
                        }
                    }
                }

                if (activeDialogTab == "manual") {
                    // Manual input layout
                    OutlinedTextField(
                        value = mealDescInput,
                        onValueChange = { mealDescInput = it },
                        label = { Text("تفاصيل الوجبة (مثال: طبق سلطة وبيضة مسلوقة)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoftTheme.SoftPink,
                            unfocusedBorderColor = SoftTheme.SoftGray,
                            focusedTextColor = SoftTheme.TextWhite,
                            unfocusedTextColor = SoftTheme.TextWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = calorieInput,
                        onValueChange = { calorieInput = it },
                        label = { Text("السعرات المقدرة (سعرة)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoftTheme.SoftPink,
                            unfocusedBorderColor = SoftTheme.SoftGray,
                            focusedTextColor = SoftTheme.TextWhite,
                            unfocusedTextColor = SoftTheme.TextWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("العناصر الدقيقة الاختيارية:", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = ironInput,
                            onValueChange = { ironInput = it },
                            label = { Text("حديد ملجم") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = folateInput,
                            onValueChange = { folateInput = it },
                            label = { Text("فوليك مكجم") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = calciumInput,
                            onValueChange = { calciumInput = it },
                            label = { Text("كالسيوم ملجم") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = omega3Input,
                            onValueChange = { omega3Input = it },
                            label = { Text("أوميجا3 جم") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text("العناصر الكبرى والماكروز الاختيارية:", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = proteinInput,
                            onValueChange = { proteinInput = it },
                            label = { Text("بروتين جم") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = carbsInput,
                            onValueChange = { carbsInput = it },
                            label = { Text("نشويات جم") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = fatInput,
                            onValueChange = { fatInput = it },
                            label = { Text("دهون جم") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = sugarInput,
                            onValueChange = { sugarInput = it },
                            label = { Text("سكريات جم") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = fiberInput,
                            onValueChange = { fiberInput = it },
                            label = { Text("ألياف جم") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = waterBenefitInput,
                            onValueChange = { waterBenefitInput = it },
                            label = { Text("مياه مستفادة مل") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(
                            onClick = onDismissRequest,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء", color = SoftTheme.SoftGray)
                        }

                        Button(
                            onClick = {
                                onSaveMeal(
                                    mealTypeInput,
                                    mealDescInput,
                                    calorieInput.toIntOrNull() ?: 0,
                                    ironInput.toDoubleOrNull() ?: 0.0,
                                    folateInput.toDoubleOrNull() ?: 0.0,
                                    calciumInput.toDoubleOrNull() ?: 0.0,
                                    omega3Input.toDoubleOrNull() ?: 0.0,
                                    proteinInput.toDoubleOrNull() ?: 0.0,
                                    carbsInput.toDoubleOrNull() ?: 0.0,
                                    fatInput.toDoubleOrNull() ?: 0.0,
                                    sugarInput.toDoubleOrNull() ?: 0.0,
                                    fiberInput.toDoubleOrNull() ?: 0.0,
                                    waterBenefitInput.toIntOrNull() ?: 0
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("حفظ")
                        }
                    }
                } else {
                    // Smart Food Selector layout
                    Text("1. اختاري التصنيف الرئيسي: 📁", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftPink, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { (catKey, catLabel) ->
                            val isSel = selectedCategoryKey == catKey
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSel) SoftTheme.SoftPink else SoftTheme.DeepSlate)
                                    .border(1.dp, if (isSel) SoftTheme.SoftPink else SoftTheme.SoftGray.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                    .clickable {
                                        selectedCategoryKey = catKey
                                        selectedFood = null
                                        quantityValue = 1
                                        sugarSpoons = 0
                                        addOliveOil = false
                                        addLemon = false
                                    }
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Text(catLabel, color = if (isSel) SoftTheme.DeepSlate else SoftTheme.TextWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("2. اختاري الصنف الفرعي: 🍽️", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftPink, fontWeight = FontWeight.Bold)
                    val currentCategoryFoods = presetFoods.filter { it.category == selectedCategoryKey }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        currentCategoryFoods.take(15).forEach { food ->
                            val isSel = selectedFood?.id == food.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isSel) SoftTheme.SoftTeal.copy(alpha = 0.15f) else SoftTheme.DeepSlate)
                                    .border(1.dp, if (isSel) SoftTheme.SoftTeal else Color.Transparent, RoundedCornerShape(14.dp))
                                    .clickable {
                                        selectedFood = food
                                        quantityValue = 1
                                        sugarSpoons = 0
                                    }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(food.name, color = SoftTheme.TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("الحصة (${food.servingSize}): ${food.calories} سعرة حرارية", color = SoftTheme.SoftGray, fontSize = 10.sp)
                                }
                                Surface(
                                    color = SoftTheme.MintTeal.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "${food.calories} د",
                                        color = SoftTheme.MintTeal,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    selectedFood?.let { food ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("3. تحديد الكمية (عدد الحصص): 🔢", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftPink, fontWeight = FontWeight.Bold)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(
                                onClick = { if (quantityValue > 1) quantityValue-- },
                                modifier = Modifier.background(SoftTheme.DeepSlate, CircleShape)
                            ) {
                                Text("-", color = SoftTheme.TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                            Text("$quantityValue حصة (${food.servingSize})", color = SoftTheme.TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            IconButton(
                                onClick = { if (quantityValue < 10) quantityValue++ },
                                modifier = Modifier.background(SoftTheme.DeepSlate, CircleShape)
                            ) {
                                Text("+", color = SoftTheme.TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Optional modifiers
                        if (food.category == "drink") {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("4. ملاعق السكر الإضافية: ☕", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftPink, fontWeight = FontWeight.Bold)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                IconButton(
                                    onClick = { if (sugarSpoons > 0) sugarSpoons-- },
                                    modifier = Modifier.background(SoftTheme.DeepSlate, CircleShape)
                                ) {
                                    Text("-", color = SoftTheme.TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                }
                                Text("$sugarSpoons ملعقة سكر", color = SoftTheme.TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                IconButton(
                                    onClick = { if (sugarSpoons < 5) sugarSpoons++ },
                                    modifier = Modifier.background(SoftTheme.DeepSlate, CircleShape)
                                ) {
                                    Text("+", color = SoftTheme.TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Calculated summary
                        val addedCalories = (sugarSpoons * 20) + (if (addOliveOil) 45 else 0)
                        val totalCalories = (food.calories * quantityValue) + addedCalories
                        val totalIron = food.ironMg * quantityValue
                        val totalFolate = food.vitaminB_Mg * 100.0 * quantityValue
                        val totalCalcium = food.calciumMg * quantityValue
                        val totalProtein = food.protein * quantityValue
                        val totalCarbs = (food.carbs * quantityValue) + (sugarSpoons * 5.0)
                        val totalFat = food.fat * quantityValue + (if (addOliveOil) 5.0 else 0.0)
                        val totalSugar = food.sugarG * quantityValue + (sugarSpoons * 5.0)
                        val totalFiber = food.fiberG * quantityValue
                        val totalWater = food.waterBenefitMl * quantityValue

                        val desc = buildString {
                            append(food.name)
                            append(" ($quantityValue حصة)")
                            if (sugarSpoons > 0) append(" مع $sugarSpoons ملعقة سكر")
                            if (addOliveOil) append(" مع زيت زيتون")
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("📊 ملخص الوجبة الذكية التقديري:", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite, fontSize = 11.sp)
                                Text("الوصف: $desc", color = SoftTheme.SoftGray, fontSize = 10.sp)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("السعرات: $totalCalories سعرة", color = SoftTheme.SoftPink, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    if (totalIron > 0.0) Text("الحديد: %.1f ملجم".format(totalIron), color = SoftTheme.MintTeal, fontSize = 10.sp)
                                    if (totalCalcium > 0.0) Text("الكالسيوم: %.0f ملجم".format(totalCalcium), color = SoftTheme.TextWhite, fontSize = 10.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            TextButton(
                                onClick = onDismissRequest,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("إلغاء", color = SoftTheme.SoftGray)
                            }

                            Button(
                                onClick = {
                                    onSaveMeal(
                                        mealTypeInput,
                                        desc,
                                        totalCalories,
                                        totalIron,
                                        totalFolate,
                                        totalCalcium,
                                        food.vitaminD_Mcg * 0.1 * quantityValue,
                                        totalProtein,
                                        totalCarbs,
                                        totalFat,
                                        totalSugar,
                                        totalFiber,
                                        totalWater
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.MintTeal),
                                modifier = Modifier.weight(1.5f)
                            ) {
                                Text("حفظ الوجبة الذكية ✓", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
