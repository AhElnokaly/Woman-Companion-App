package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.viewmodel.*

// --- Nutrition & Water Tracker Screen ---
@Composable
fun NutritionAndWaterScreen(
    viewModel: WomanCompanionViewModel
) {
    val waterLog by viewModel.todayWaterLogState.collectAsStateWithLifecycle()
    val nutritionLogs by viewModel.todayNutritionLogsState.collectAsStateWithLifecycle()

    val targetWater = viewModel.getWaterTarget()
    val calorieGoal = viewModel.getCalorieTarget()

    var showAddMealDialog by remember { mutableStateOf(false) }
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "💧 تتبع شرب المياه",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.TextWhite
                        )

                        // Glass graphic with progress
                        val fraction = (currentWaterAmount.toFloat() / targetWater.toFloat()).coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .size(130.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(SoftTheme.DeepSlate)
                                .drawBehind {
                                    // draw water fill
                                    val height = size.height * fraction
                                    drawRect(
                                        color = SoftTheme.SoftTeal.copy(alpha = 0.6f),
                                        topLeft = Offset(0f, size.height - height),
                                        size = Size(size.width, height)
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$currentWaterAmount / $targetWater مل",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = SoftTheme.TextWhite,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${(fraction * 100).toInt()}% مكتمل",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SoftTheme.SoftGray
                                )
                            }
                        }

                        // Buttons for quick add
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { viewModel.addWater(250) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("add_water_250"),
                                colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftTeal)
                            ) {
                                Text("+٢٥٠ مل 🥛", fontSize = 12.sp)
                            }
                            Button(
                                onClick = { viewModel.addWater(500) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("add_water_500"),
                                colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.MintTeal)
                            ) {
                                Text("+٥٠٠ مل 🍶", fontSize = 12.sp)
                            }
                            IconButton(
                                onClick = { viewModel.resetTodayWater() },
                                modifier = Modifier.background(SoftTheme.DeepSlate, CircleShape)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "إعادة تعيين", tint = SoftTheme.RedDanger)
                            }
                        }
                    }
                }
            }

            // Calorie & Nutrient Guidance Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "🎯 هدف السعرات الحرارية اليومي",
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.TextWhite,
                            style = MaterialTheme.typography.titleMedium
                        )

                        val totalCaloriesConsumed = nutritionLogs.sumOf { it.calories }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("المستهلك اليوم", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                                Text("$totalCaloriesConsumed سعرة حرارية", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SoftTheme.MintTeal)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("الهدف المطلوب", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                                Text("${calorieGoal.target} سعرة", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SoftTheme.SoftPink)
                            }
                        }

                        Text(
                            text = calorieGoal.details,
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTheme.SoftGray,
                            lineHeight = 16.sp
                        )

                        Text(
                            text = "* يُرجى مراجعة الدكتورة المشرفة أو أخصائية تغذية قبل إدخال تغييرات جذرية على طعامكِ.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTheme.RedDanger,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Detailed Nutrient Balance and Remaining Requirements Card
            item {
                val nutrientTargets = viewModel.getNutrientTargets()
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
                    modifier = Modifier.fillMaxWidth(),
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
                            Triple("البروتين (لبناء الأنسجة)", totalProtein, nutrientTargets["protein"]),
                            Triple("النشويات (مصدر الطاقة)", totalCarbs, nutrientTargets["carbs"]),
                            Triple("الدهون (الامتصاص والذكاء)", totalFat, nutrientTargets["fat"]),
                            Triple("السكريات المستهلكة", totalSugar, nutrientTargets["sugar"]),
                            Triple("الألياف (لصحة الهضم)", totalFiber, nutrientTargets["fiber"])
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            macrosList.forEach { (label, consumed, targetInfo) ->
                                if (targetInfo != null) {
                                    val pct = if (targetInfo.targetVal > 0) (consumed / targetInfo.targetVal).coerceIn(0.0, 1.0).toFloat() else 0f
                                    val remaining = targetInfo.targetVal - consumed
                                    val pctText = "${(pct * 100).toInt()}%"

                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(label, style = MaterialTheme.typography.bodySmall, color = SoftTheme.TextWhite)
                                            Text(
                                                text = "%.1f / %.0f %s (%s)".format(consumed, targetInfo.targetVal, targetInfo.unit, pctText),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (targetInfo.isLimit && remaining < 0) SoftTheme.RedDanger else SoftTheme.MintTeal,
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
                                                        if (targetInfo.isLimit && remaining < 0) SoftTheme.RedDanger else SoftTheme.SoftTeal
                                                    )
                                            )
                                        }

                                        // Remaining description text
                                        val remainingText = when {
                                            targetInfo.isLimit -> {
                                                if (remaining >= 0) "متبقي للاستهلاك الآمن: %.1f %s".format(remaining, targetInfo.unit)
                                                else "⚠️ تخطيتِ الحد الآمن بـ %.1f %s!".format(-remaining, targetInfo.unit)
                                            }
                                            else -> {
                                                if (remaining > 0) "المتبقي لتحقيق الهدف: %.1f %s".format(remaining, targetInfo.unit)
                                                else "🎉 تم تلبية احتياجكِ اليومي بالكامل!"
                                            }
                                        }
                                        Text(remainingText, style = MaterialTheme.typography.labelSmall, color = if (remaining < 0 && targetInfo.isLimit) SoftTheme.RedDanger else SoftTheme.SoftGray)
                                    }
                                }
                            }
                        }

                        // Micros section
                        AnimatedVisibility(visible = showAllNutrients) {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Divider(color = SoftTheme.DeepSlate, thickness = 1.dp)
                                Text("العناصر الدقيقة والفيتامينات والمعادن:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = SoftTheme.MintTeal)

                                val microsList = listOf(
                                    Triple("الحديد (لمنع الأنيميا)", totalIron, nutrientTargets["iron"]),
                                    Triple("الكالسيوم (لالعظام والأسنان)", totalCalcium, nutrientTargets["calcium"]),
                                    Triple("حمض الفوليك (للنمو العصبي)", totalFolate, nutrientTargets["folate"]),
                                    Triple("البوتاسيوم (لتوازن الضغط)", totalPotassium, nutrientTargets["potassium"]),
                                    Triple("الماغنسيوم (لتسكين التقلصات)", totalMagnesium, nutrientTargets["magnesium"]),
                                    Triple("فيتامين سي (للإمتصاص والمناعة)", totalVitaminC, nutrientTargets["vitaminC"]),
                                    Triple("فيتامين أ (لنمو الخلايا والنظر)", totalVitaminA, nutrientTargets["vitaminA"])
                                )

                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    microsList.forEach { (label, consumed, targetInfo) ->
                                        if (targetInfo != null) {
                                            val pct = if (targetInfo.targetVal > 0) (consumed / targetInfo.targetVal).coerceIn(0.0, 1.0).toFloat() else 0f
                                            val remaining = targetInfo.targetVal - consumed
                                            val pctText = "${(pct * 100).toInt()}%"

                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(label, style = MaterialTheme.typography.bodySmall, color = SoftTheme.TextWhite)
                                                    Text(
                                                        text = "%.1f / %.0f %s (%s)".format(consumed, targetInfo.targetVal, targetInfo.unit, pctText),
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

                                                val remainingText = if (remaining > 0) "المتبقي لتحقيق الهدف: %.1f %s".format(remaining, targetInfo.unit) else "🎉 تم تلبية الاحتياج اليومي!"
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

            item {
                LiveNutrientSimulatorWidget(viewModel = viewModel)
            }

            item {
                SmartNutritionAdvisorCard(viewModel = viewModel)
            }

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

            item {
                EgyptianFoodSearchWidget(viewModel = viewModel)
            }

            // Today Meals section header
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
        var activeDialogTab by remember { mutableStateOf("smart") } // Default to "smart" to showcase the selector
        var activeCategory by remember { mutableStateOf<String?>(null) }
        var activeSubcategory by remember { mutableStateOf<String?>(null) }
        var quantityValue by remember { mutableStateOf(1) }
        val selectedAdditions = remember { mutableStateMapOf<String, Boolean>() }

        Dialog(
            onDismissRequest = { showAddMealDialog = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.85f)
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
                        // Original Manual input layout
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
                                onClick = { showAddMealDialog = false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("إلغاء", color = SoftTheme.SoftGray)
                            }

                            Button(
                                onClick = {
                                    viewModel.addNutritionMeal(
                                        mealType = mealTypeInput,
                                        description = mealDescInput,
                                        calories = calorieInput.toIntOrNull() ?: 0,
                                        iron = ironInput.toDoubleOrNull() ?: 0.0,
                                        folate = folateInput.toDoubleOrNull() ?: 0.0,
                                        calcium = calciumInput.toDoubleOrNull() ?: 0.0,
                                        omega3 = omega3Input.toDoubleOrNull() ?: 0.0,
                                        protein = proteinInput.toDoubleOrNull() ?: 0.0,
                                        carbs = carbsInput.toDoubleOrNull() ?: 0.0,
                                        fat = fatInput.toDoubleOrNull() ?: 0.0,
                                        sugar = sugarInput.toDoubleOrNull() ?: 0.0,
                                        fiber = fiberInput.toDoubleOrNull() ?: 0.0,
                                        waterBenefit = waterBenefitInput.toIntOrNull() ?: 0
                                    )
                                    showAddMealDialog = false
                                    mealDescInput = ""
                                    calorieInput = ""
                                    ironInput = ""
                                    folateInput = ""
                                    calciumInput = ""
                                    omega3Input = ""
                                    proteinInput = ""
                                    carbsInput = ""
                                    fatInput = ""
                                    sugarInput = ""
                                    fiberInput = ""
                                    waterBenefitInput = ""
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
                            hierarchicalDatabase.keys.forEach { category ->
                                val isSel = activeCategory == category
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSel) SoftTheme.SoftPink else SoftTheme.DeepSlate)
                                        .border(1.dp, if (isSel) SoftTheme.SoftPink else SoftTheme.SoftGray.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                        .clickable {
                                            activeCategory = category
                                             activeSubcategory = null
                                            quantityValue = 1
                                            selectedAdditions.clear()
                                        }
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Text(category, color = if (isSel) SoftTheme.DeepSlate else SoftTheme.TextWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }

                        if (activeCategory != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("2. اختاري الصنف الفرعي: 🍽️", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftPink, fontWeight = FontWeight.Bold)
                            val subcategories = hierarchicalDatabase[activeCategory] ?: emptyList()
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                subcategories.forEach { subInfo ->
                                    val isSel = activeSubcategory == subInfo.name
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(if (isSel) SoftTheme.SoftTeal.copy(alpha = 0.15f) else SoftTheme.DeepSlate)
                                            .border(1.dp, if (isSel) SoftTheme.SoftTeal else Color.Transparent, RoundedCornerShape(14.dp))
                                            .clickable {
                                                activeSubcategory = subInfo.name
                                                selectedAdditions.clear()
                                                subInfo.defaultAdditions.forEach { add ->
                                                    selectedAdditions[add] = false
                                                }
                                            }
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(subInfo.name, color = SoftTheme.TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text("الحصة: ${subInfo.caloriesPerUnit} سعرة حرارية", color = SoftTheme.SoftGray, fontSize = 10.sp)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    when (subInfo.safety) {
                                                        SafetyLevel.SAFE -> SoftTheme.MintTeal.copy(alpha = 0.15f)
                                                        SafetyLevel.CAUTION -> Color(0xFFFFF3CD)
                                                        SafetyLevel.AVOID -> Color(0xFFF8D7DA)
                                                    }
                                                )
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = when (subInfo.safety) {
                                                    SafetyLevel.SAFE -> "آمن ✓"
                                                    SafetyLevel.CAUTION -> "انتباه ⚠️"
                                                    SafetyLevel.AVOID -> "تجنبي 🚫"
                                                },
                                                color = when (subInfo.safety) {
                                                    SafetyLevel.SAFE -> SoftTheme.MintTeal
                                                    SafetyLevel.CAUTION -> Color(0xFF856404)
                                                    SafetyLevel.AVOID -> Color(0xFF721C24)
                                                },
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (activeSubcategory != null) {
                            val selectedSubInfo = hierarchicalDatabase[activeCategory]?.firstOrNull { it.name == activeSubcategory }
                            if (selectedSubInfo != null) {
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
                                    Text("$quantityValue حصة", color = SoftTheme.TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    IconButton(
                                        onClick = { if (quantityValue < 10) quantityValue++ },
                                        modifier = Modifier.background(SoftTheme.DeepSlate, CircleShape)
                                    ) {
                                        Text("+", color = SoftTheme.TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (selectedSubInfo.defaultAdditions.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val questionText = when {
                                        activeCategory?.contains("مشروبات") == true -> "هل ترغبين بإضافة محلي أو نكهة؟ 🍯☕"
                                        activeCategory?.contains("جبن") == true -> "هل ترغبين بإضافة توابل أو زيت؟ 🧂🌿"
                                        activeCategory?.contains("شوربات") == true -> "هل ترغبين بإضافة ليمون أو فلفل؟ 🥣🍋"
                                        else -> "إضافات مخصصة اختيارية: ⚙️"
                                    }
                                    Text(
                                        text = "4. $questionText",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SoftTheme.SoftPink,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        selectedSubInfo.defaultAdditions.forEach { addition ->
                                            val isChecked = selectedAdditions[addition] ?: false
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(SoftTheme.DeepSlate)
                                                    .clickable { selectedAdditions[addition] = !isChecked }
                                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    val icon = when (addition) {
                                                        "سكر" -> "🍬"
                                                        "عسل نحل" -> "🍯"
                                                        "محلى صناعي" -> "💊"
                                                        "ملح" -> "🧂"
                                                        "فلفل أسود" -> "🌶️"
                                                        "كمون" -> "🌿"
                                                        "زعتر" -> "🍃"
                                                        "زيت زيتون" -> "🫒"
                                                        "عصير ليمون" -> "🍋"
                                                        "بقدونس" -> "🌱"
                                                        "حليب" -> "🥛"
                                                        else -> "✨"
                                                    }
                                                    Text(icon, fontSize = 14.sp)
                                                    Text(addition, color = SoftTheme.TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                                }
                                                Checkbox(
                                                    checked = isChecked,
                                                    onCheckedChange = { selectedAdditions[addition] = it },
                                                    colors = CheckboxDefaults.colors(checkedColor = SoftTheme.SoftPink)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                // Inline safety advice card
                                if (selectedSubInfo.safety == SafetyLevel.CAUTION) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text("💡", fontSize = 16.sp)
                                            Text(
                                                text = selectedSubInfo.safetyAdvice ?: "يرجى الاعتدال في تناول هذا الصنف.",
                                                color = Color(0xFF856404),
                                                fontSize = 11.sp,
                                                lineHeight = 16.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                } else if (selectedSubInfo.safety == SafetyLevel.AVOID) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8D7DA)),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text("🚫", fontSize = 16.sp)
                                            Text(
                                                text = selectedSubInfo.safetyAdvice ?: "يفضل تجنب هذا الصنف في فترات الحمل والرضاعة حرصاً على سلامتكِ.",
                                                color = Color(0xFF721C24),
                                                fontSize = 11.sp,
                                                lineHeight = 16.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                // Live metrics
                                val finalDesc = buildString {
                                    append(selectedSubInfo.name)
                                    append(" ($quantityValue حصة)")
                                    val activeAdds = selectedAdditions.filter { it.value }.keys
                                    if (activeAdds.isNotEmpty()) {
                                        append(" مع: ")
                                        append(activeAdds.joinToString("، "))
                                    }
                                }
                                val caloriesAdditions = selectedAdditions.filter { it.value }.size * 15
                                val calculatedCalories = selectedSubInfo.caloriesPerUnit * quantityValue + caloriesAdditions
                                val calculatedIron = selectedSubInfo.iron * quantityValue
                                val calculatedFolate = selectedSubInfo.folate * quantityValue
                                val calculatedCalcium = selectedSubInfo.calcium * quantityValue
                                val calculatedWaterBenefit = selectedSubInfo.waterBenefit * quantityValue

                                Spacer(modifier = Modifier.height(4.dp))
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("📊 ملخص الوجبة الذكية التقديري:", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite, fontSize = 11.sp)
                                        Text("الوصف: $finalDesc", color = SoftTheme.SoftGray, fontSize = 10.sp)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("السعرات: $calculatedCalories سعرة", color = SoftTheme.SoftPink, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            if (calculatedIron > 0) Text("الحديد: $calculatedIron ملجم", color = SoftTheme.MintTeal, fontSize = 10.sp)
                                            if (calculatedFolate > 0) Text("فوليك: $calculatedFolate مكجم", color = SoftTheme.SoftTeal, fontSize = 10.sp)
                                            if (calculatedCalcium > 0) Text("كالسيوم: $calculatedCalcium ملجم", color = SoftTheme.TextWhite, fontSize = 10.sp)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    TextButton(
                                        onClick = { showAddMealDialog = false },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("إلغاء", color = SoftTheme.SoftGray)
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.addNutritionMeal(
                                                mealType = mealTypeInput,
                                                description = finalDesc,
                                                calories = calculatedCalories,
                                                iron = calculatedIron,
                                                folate = calculatedFolate,
                                                calcium = calculatedCalcium,
                                                omega3 = 0.0,
                                                protein = selectedSubInfo.protein * quantityValue,
                                                carbs = selectedSubInfo.carbs * quantityValue,
                                                fat = selectedSubInfo.fat * quantityValue,
                                                sugar = 0.0,
                                                fiber = 0.0,
                                                waterBenefit = calculatedWaterBenefit
                                            )
                                            showAddMealDialog = false
                                            activeCategory = null
                                            activeSubcategory = null
                                            quantityValue = 1
                                            selectedAdditions.clear()
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
    }
}
