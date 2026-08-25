package com.example.ui

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import com.example.util.formatArabicDays
import com.example.viewmodel.CycleStats
import com.example.viewmodel.WomanCompanionViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

fun formatGregorianDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("ar"))
    return sdf.format(Date(timestamp))
}

fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.forLanguageTag("ar"))
    return sdf.format(Date(timestamp))
}

// --- App Lock / PIN Screen moved to SettingsScreen.kt ---

// +++ أضيف بناءً على طلبك لحساب تقدم الشهور الطبية للحمل +++
data class MonthProgress(
    val monthNumber: Int,
    val monthName: String,
    val progressFraction: Float,
    val totalMonths: Int
)

fun calculateMonthProgress(weeks: Int, daysIntoWeek: Int): MonthProgress {
    val ranges = listOf(
        1 to 4,    // Month 1
        5 to 8,    // Month 2
        9 to 13,   // Month 3
        14 to 17,  // Month 4
        18 to 22,  // Month 5
        23 to 27,  // Month 6
        28 to 31,  // Month 7
        32 to 35,  // Month 8
        36 to 40,  // Month 9
        41 to 42   // Month 10 (Post-term)
    )
    
    var currentMonth = 9
    var progressFraction = 0f
    
    for (i in ranges.indices) {
        val (startWeek, endWeek) = ranges[i]
        if (weeks in startWeek..endWeek) {
            currentMonth = i + 1
            val totalWeeksInMonth = (endWeek - startWeek + 1)
            val totalDaysInMonth = totalWeeksInMonth * 7
            val daysCompleted = ((weeks - startWeek) * 7 + daysIntoWeek).coerceIn(0, totalDaysInMonth)
            progressFraction = daysCompleted.toFloat() / totalDaysInMonth.toFloat()
            break
        }
    }
    
    if (weeks >= 41) {
        currentMonth = 10
        val daysCompleted = ((weeks - 41) * 7 + daysIntoWeek).coerceIn(0, 14)
        progressFraction = daysCompleted.toFloat() / 14f
    }
    
    val monthNames = listOf(
        "الشهر الأول", "الشهر الثاني", "الشهر الثالث",
        "الشهر الرابع", "الشهر الخامس", "الشهر السادس",
        "الشهر السابع", "الشهر الثامن", "الشهر التاسع", "الشهر العاشر ⚠️"
    )
    
    val name = if (currentMonth <= monthNames.size) monthNames[currentMonth - 1] else "الشهر العاشر ⚠️"
    val total = if (weeks >= 41) 10 else 9
    
    return MonthProgress(currentMonth, name, progressFraction, total)
}

// --- Quick Craving Log Card (Task B8) ---
@Composable
fun QuickCravingLogCard(
    viewModel: WomanCompanionViewModel,
    onNavigateToCraving: () -> Unit = {}
) {
    val cravingLogs by viewModel.allCravingLogsState.collectAsStateWithLifecycle()
    var cravingText by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Sweet") }
    var intensity by remember { mutableStateOf(5f) }

    val categories = listOf(
        "Sweet" to "حلو 🍓",
        "Sour" to "حامض 🍋",
        "Salty" to "حادق 🥨",
        "Spicy" to "حار 🌶️",
        "Chocolate" to "شوكولاتة 🍫"
    )

    Card(
        modifier = Modifier.fillMaxWidth().testTag("quick_craving_card"),
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
                    Text("🍓", fontSize = 22.sp)
                    Text(
                        "سجل الوحم والاشتهاء",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.TextWhite
                    )
                }
                TextButton(
                    onClick = onNavigateToCraving,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    modifier = Modifier.testTag("craving_header_goto_btn")
                ) {
                    Text("السجل الكامل 🍉 ↗", color = SoftTheme.SoftPink, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            OutlinedTextField(
                value = cravingText,
                onValueChange = { cravingText = it },
                placeholder = { Text("مثال: مانجو، شيكولاتة، مخلل...", color = SoftTheme.SoftGray, fontSize = 12.sp) },
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
                items(categories) { (key, label) ->
                    FilterChip(
                        selected = selectedType == key,
                        onClick = { selectedType = key },
                        label = { Text(label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SoftTheme.SoftPink,
                            selectedLabelColor = Color.White,
                            containerColor = SoftTheme.DeepSlate,
                            labelColor = SoftTheme.TextWhite
                        )
                    )
                }
            }

            Button(
                onClick = {
                    if (cravingText.isNotBlank()) {
                        viewModel.addCravingLog(
                            cravingItem = cravingText.trim(),
                            cravingType = selectedType,
                            intensity = intensity.toInt(),
                            notes = "مسجّل سريعا من الشاشة الرئيسية"
                        )
                        cravingText = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = cravingText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink)
            ) {
                Text("حفظ الوحم في سجل جوري ✍️", color = Color.White, fontWeight = FontWeight.Bold)
            }

            if (cravingLogs.isNotEmpty()) {
                Text("الوحم المسجل حديثاً:", style = MaterialTheme.typography.labelSmall, color = SoftTheme.SoftGray)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(cravingLogs.take(5)) { log ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(SoftTheme.DeepSlate)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                "${log.cravingItem} (${log.intensity}/10)",
                                fontSize = 11.sp,
                                color = SoftTheme.MintTeal,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = onNavigateToCraving,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftTheme.SoftPink),
                border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth().testTag("craving_bottom_goto_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("فتح سجل ومحلل الوحم الكامل مع جوري 🍉 ↗", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- AI Pregnancy Myth Buster Card (Task B7) ---
@Composable
fun PregnancyMythBusterCard(
    viewModel: WomanCompanionViewModel,
    onNavigateToFoodSafety: () -> Unit = {},
    onOpenJouriChat: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var userQuestion by remember { mutableStateOf("") }
    var jouriAnswer by remember { mutableStateOf<String?>(null) }
    var isAskingJouri by remember { mutableStateOf(false) }

    val myths = remember {
        listOf(
            Triple("شرب الحليب البارد يسبب مغص للجنين؟", "❌ خرافة", "الحليب البارد آمن وممتع تماماً ولا يصل للجنين برودته لأن جسمك ينظم حرارة الطعام فور بلعه."),
            Triple("شكل البطن يحدد نوع الجنين (ولد أو بنت)؟", "❌ خرافة", "شكل البطن يعتمد فقط على قوة عضلات بطنكِ، وضعية الجنين، وعدد مرات حملك السابقة وليس له علاقة بالنوع."),
            Triple("استخدام صبغات الشعر ممنوع طوال الحمل؟", "⚠️ حقيقة جزئية", "يفضل تجنب الصبغات في الثلث الأول (أول 12 أسبوع) حمايةً لنمو الأعضاء، لكنها آمنة نسبيًا بعد ذلك بشرط تهوية المكان واستخدام أنواع خالية من الأمونيا."),
            Triple("الحامل يجب أن تأكل عن شخصين؟", "❌ خرافة", "الحامل تحتاج فقط لـ 300 سعرة حرارية إضافية يومياً (كوب لبن وموزة) بدءاً من الثلث الثاني وليس مضاعفة الأكل!"),
            Triple("تناول التمر يسبب الإجهاض في بداية الحمل؟", "❌ خرافة", "التمر غني بالألياف والحديد والسكريات الطبيعية وآمن باعتدال، ولكنه يفيد خصوصاً في الشهر الأخير لتسهيل الولادة."),
            Triple("المجهود الخفيف والمشي يضر الحامل؟", "❌ خرافة", "المشي والنشاط الخفيف المعتدل يحسن الدورة الدموية، يقلل التورم، ويساعد على ولادة أسهل وأسرع.")
        )
    }

    val filteredMyths = myths.filter {
        searchQuery.isBlank() || it.first.contains(searchQuery) || it.third.contains(searchQuery)
    }

    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth().testTag("pregnancy_myth_buster_card"),
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("💡", fontSize = 22.sp)
                Column {
                    Text(
                        "صندوق التساؤلات: خرافات وحقائق الشائعة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.TextWhite
                    )
                    Text("تصحيح المفاهيم الطبية الشائعة في المجتمع", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("ابحثي في خرافات الحمل والدورة...", color = SoftTheme.SoftGray, fontSize = 12.sp) },
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

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                filteredMyths.forEach { (question, verdict, explanation) ->
                    var isExpanded by remember { mutableStateOf(false) }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isExpanded = !isExpanded },
                        colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(question, style = MaterialTheme.typography.bodyMedium, color = SoftTheme.TextWhite, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Text(verdict, style = MaterialTheme.typography.bodySmall, color = if (verdict.contains("خرافة")) SoftTheme.RedDanger else SoftTheme.GoldFasting, fontWeight = FontWeight.Bold)
                            }
                            AnimatedVisibility(visible = isExpanded) {
                                Text(explanation, style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = SoftTheme.DeepSlate, thickness = 1.dp)
            Text("اسألي جوري عن أي خرافة أو إشاعة أخرى: 🔮", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftPink, fontWeight = FontWeight.Bold)

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = userQuestion,
                    onValueChange = { userQuestion = it },
                    placeholder = { Text("مثال: هل الاستحمام بماء دافئ يضر الدورة؟", color = SoftTheme.SoftGray, fontSize = 11.sp) },
                    modifier = Modifier.weight(1f),
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
                Button(
                    onClick = {
                        if (userQuestion.isNotBlank()) {
                            isAskingJouri = true
                            scope.launch {
                                val pregState = viewModel.pregnancyState.value
                                val phaseInfo = viewModel.getCurrentCyclePhase()
                                val waterLog = viewModel.todayWaterLogState.value?.amountMl ?: 0
                                val resp = OfflineJouriEngine.getResponse(
                                    userInput = userQuestion,
                                    motherName = pregState?.motherName,
                                    phaseInfo = phaseInfo,
                                    pregnancyState = pregState,
                                    todayWaterLogged = waterLog
                                ).replyText
                                jouriAnswer = resp
                                isAskingJouri = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                    enabled = userQuestion.isNotBlank() && !isAskingJouri
                ) {
                    Text("سلي جوري", fontSize = 11.sp, color = Color.White)
                }
            }

            jouriAnswer?.let { ans ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SoftTheme.MintTeal.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("🌸 رد جوري الطبّي:", fontWeight = FontWeight.Bold, color = SoftTheme.MintTeal, fontSize = 12.sp)
                        Text(ans, style = MaterialTheme.typography.bodySmall, color = SoftTheme.TextWhite)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateToFoodSafety,
                    modifier = Modifier.weight(1f).testTag("goto_food_safety_btn"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftTheme.MintTeal),
                    border = BorderStroke(1.dp, SoftTheme.MintTeal.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text("دليل سلامة الأطعمة 🥑 ↗", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onOpenJouriChat,
                    modifier = Modifier.weight(1f).testTag("goto_jouri_chat_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink.copy(alpha = 0.35f)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text("استشيري جوري 🌸 ↗", color = SoftTheme.TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- Egyptian Food Search Widget (Task B6) ---
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

// --- Milestone Celebration Card for Pregnancy ---
@Composable
fun MilestoneCelebrationCard(
    weeks: Int,
    onDismiss: () -> Unit
) {
    val (title, body, icon) = when {
        weeks >= 37 -> Triple("إنجاز الأسبوع 37 👶🎁", "مبروك الوصول للأسبوع 37! الجنين الآن مكتمل النمو وجاهز للقاء بمشيئة الله.", "👶")
        weeks >= 28 -> Triple("إنجاز الأسبوع 28 🌸", "مبروك بداية الثلث الثالث والأخير! خطوة جديدة تقربك أكثر من ضم طفلك.", "🌸")
        weeks >= 24 -> Triple("إنجاز الأسبوع 24 👶✨", "مبروك الأسبوع 24! مرحلة حيوية رائعة واستجابة الجنين في تزايد مستمر.", "✨")
        weeks >= 20 -> Triple("منتصف الرحلة! الأسبوع 20 🍌🎉", "ألف مبروك الوصول للأسبوع 20! قطعتم نصف الرحلة المباركة بحفظ الله.", "🎉")
        weeks >= 12 -> Triple("إنجاز الأسبوع 12 🌿", "مبروك تجاوز الأسبوع 12! انتهى الثلث الأول بنجاح وتبدأ مرحلة أكثر استقراراً.", "🌿")
        else -> Triple("", "", "")
    }

    if (title.isNotEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = SoftTheme.PregnancyPurple.copy(alpha = 0.15f)),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, SoftTheme.PregnancyPurple.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(icon, fontSize = 24.sp)
                        Text(title, fontWeight = FontWeight.Bold, color = SoftTheme.PregnancyPurple, fontSize = 16.sp)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = SoftTheme.SoftGray)
                    }
                }
                Text(body, style = MaterialTheme.typography.bodyMedium, color = SoftTheme.TextWhite)
            }
        }
    }
}

// --- Past Pregnancy Memory Card ("من فترة كانت هنا 🌸") ---
@Composable
fun PastPregnancyMemoryCard(
    allPregnancies: List<com.example.data.PregnancyEntity>,
    allFetalGrowthLogs: List<com.example.data.FetalGrowthLog>,
    onDismiss: () -> Unit,
    onNavigateToHistory: () -> Unit = {}
) {
    val memoryInfo = remember(allPregnancies, allFetalGrowthLogs) {
        val nowMs = System.currentTimeMillis()
        val calNow = java.util.Calendar.getInstance().apply { timeInMillis = nowMs }

        allPregnancies.filter { !it.isActive && it.lastPeriodDate != null }.mapNotNull { pastPreg ->
            val lmp = pastPreg.lastPeriodDate ?: return@mapNotNull null
            val calLmp = java.util.Calendar.getInstance().apply { timeInMillis = lmp }
            val yearDiff = (calNow.get(java.util.Calendar.YEAR) - calLmp.get(java.util.Calendar.YEAR)).coerceAtLeast(1)
            val projectedLmp = java.util.Calendar.getInstance().apply {
                timeInMillis = lmp
                add(java.util.Calendar.YEAR, yearDiff)
            }.timeInMillis

            val diffMs = nowMs - projectedLmp
            val diffDays = (diffMs / (24 * 60 * 60 * 1000L)).toInt()
            if (diffDays in 0..280) {
                val pastWeek = (diffDays / 7) + 1
                val matchedLog = allFetalGrowthLogs.firstOrNull { it.pregnancyId == pastPreg.id && it.pregnancyWeek == pastWeek }
                Triple(pastPreg, pastWeek, matchedLog)
            } else null
        }.firstOrNull()
    }

    memoryInfo?.let { (pastPreg, pastWeek, matchedLog) ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .testTag("past_pregnancy_memory_card"),
            colors = CardDefaults.cardColors(containerColor = SoftTheme.PregnancyPurple.copy(alpha = 0.12f)),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, SoftTheme.PregnancyPurple.copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🌸", fontSize = 22.sp)
                        Text(
                            "من فترة كانت هنا 🌸",
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.PregnancyPurple,
                            fontSize = 16.sp
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = SoftTheme.SoftGray)
                    }
                }
                val babyText = if (!pastPreg.babyName.isNullOrBlank()) " بـ (${pastPreg.babyName})" else ""
                Text(
                    "في مثل هذا الوقت من السنة، كنتِ في الأسبوع $pastWeek من حملكِ السابق$babyText.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SoftTheme.TextWhite
                )
                if (matchedLog != null) {
                    Text(
                        "آخر قياس مسجّل لهذا الأسبوع: الوزن ${matchedLog.weightGrams} جرام، الطول ${matchedLog.lengthCm} سم.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTheme.MintTeal
                    )
                }

                Button(
                    onClick = onNavigateToHistory,
                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.DeepSlate),
                    modifier = Modifier.fillMaxWidth().testTag("goto_past_pregnancy_history_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("عرض سجل وتفاصيل الأحمال السابقة 📜 ↗", color = SoftTheme.SoftPink, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- Pregnancy Dashboard Screen ---
@Composable
fun PregnancyDashboardScreen(
    viewModel: WomanCompanionViewModel,
    onNavigateToSettings: () -> Unit,
    onOpenJouriChat: () -> Unit = {},
    onNavigateToTab: (Int) -> Unit = {}
) {
    val pregState by viewModel.pregnancyState.collectAsStateWithLifecycle()
    val progression = viewModel.getPregnancyProgression()
    val weatherState by viewModel.weatherState.collectAsStateWithLifecycle()
    val periodLogs by viewModel.periodLogsState.collectAsStateWithLifecycle()
    val isUpdateAvailable by viewModel.isGitHubUpdateAvailable.collectAsStateWithLifecycle()
    val isNetworkAvailable by viewModel.isNetworkAvailable.collectAsStateWithLifecycle()

    val todayWaterLog by viewModel.todayWaterLogState.collectAsStateWithLifecycle()
    val todayStepLog by viewModel.todayStepLogState.collectAsStateWithLifecycle()
    val appointments by viewModel.appointmentsState.collectAsStateWithLifecycle()
    val settings by viewModel.appLockSettingsState.collectAsStateWithLifecycle()
    
    val activeStart by viewModel.currentKickSessionStart.collectAsStateWithLifecycle()
    val currentCount by viewModel.currentKickCount.collectAsStateWithLifecycle()

    var showSetupDialog by remember { mutableStateOf(false) }
    var showAddBpDialog by remember { mutableStateOf(false) }
    var showAddJournalDialog by remember { mutableStateOf(false) }

    // Live ticking Arabic clock & date states
    var currentTimeString by remember {
        mutableStateOf(
            java.text.SimpleDateFormat("hh:mm a", java.util.Locale.forLanguageTag("ar")).format(java.util.Calendar.getInstance().time)
        )
    }
    var currentDateString by remember {
        mutableStateOf(
            java.text.SimpleDateFormat("EEEE، d MMMM", java.util.Locale.forLanguageTag("ar")).format(java.util.Calendar.getInstance().time)
        )
    }

    LaunchedEffect(Unit) {
        val timeSdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.forLanguageTag("ar"))
        val dateSdf = java.text.SimpleDateFormat("EEEE، d MMMM", java.util.Locale.forLanguageTag("ar"))
        while (true) {
            val cal = java.util.Calendar.getInstance()
            currentTimeString = timeSdf.format(cal.time)
            currentDateString = dateSdf.format(cal.time)
            kotlinx.coroutines.delay(1000)
        }
    }

    var selectedLastPeriod by remember { mutableStateOf<Long?>(null) }
    var weightInput by remember { mutableStateOf("") }
    var heightInput by remember { mutableStateOf("") }

    var journalContent by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf("🌸 سعيدة") }
    val moods = listOf("🌸 سعيدة", "🌱 هادئة", "🪵 تعبة", "🩸 قلقة", "✨ متحمسة")

    // +++ أضيف بناءً على طلبك لإدارة حوارات نوع الجنين والولادة +++
    var showBabyInfoDialog by remember { mutableStateOf(false) }
    var babyGenderInput by remember { mutableStateOf(pregState?.babyGender ?: "") }
    var babyNameInput by remember { mutableStateOf(pregState?.babyName ?: "") }
    var showDeliveryDialog by remember { mutableStateOf(false) }

    LaunchedEffect(showSetupDialog, periodLogs) {
        if (showSetupDialog && (selectedLastPeriod == null || selectedLastPeriod == System.currentTimeMillis())) {
            val mostRecent = periodLogs.maxByOrNull { it.startDate }
            if (mostRecent != null) {
                selectedLastPeriod = mostRecent.startDate
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            val companionName = settings?.companionName ?: "جوري"
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val greeting = when {
                hour in 5..11 -> "صباح الورد والرضا يا غالية ☀️"
                hour in 12..17 -> "أهلاً بكِ يا صديقتي ✨"
                else -> "مساء الهدوء والسكينة يا غالية 🌙"
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = greeting,
                                style = MaterialTheme.typography.titleMedium,
                                color = SoftTheme.SoftPink,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "جوري 🌸",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = SoftTheme.TextWhite,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isNetworkAvailable) SoftTheme.MintTeal.copy(alpha = 0.15f) else SoftTheme.RedDanger.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(if (isNetworkAvailable) SoftTheme.MintTeal else SoftTheme.RedDanger, CircleShape)
                                        )
                                        Text(
                                            text = if (isNetworkAvailable) "متصل" else "أوفلاين",
                                            color = if (isNetworkAvailable) SoftTheme.MintTeal else SoftTheme.RedDanger,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = { viewModel.toggleDarkMode() },
                                modifier = Modifier.background(SoftTheme.DeepSlate, CircleShape)
                            ) {
                                Icon(
                                    imageVector = if (SoftTheme.isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = "تبديل المظهر",
                                    tint = SoftTheme.SoftPink
                                )
                            }
                            IconButton(
                                onClick = onNavigateToSettings,
                                modifier = Modifier.background(SoftTheme.DeepSlate, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "الإعدادات",
                                    tint = SoftTheme.SoftGray
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "صديقتكِ الوفية $companionName تسهر على راحتكِ الروحية والصحية وتدعمكِ في كل خطوة ومرحلة 💖",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTheme.SoftGray,
                        lineHeight = 16.sp
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = SoftTheme.DeepSlate,
                        thickness = 1.dp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Clock & Date (Right side)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(SoftTheme.SoftPink.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🕒", fontSize = 18.sp)
                            }
                            Column {
                                Text(
                                    text = currentTimeString,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SoftTheme.TextWhite
                                )
                                Text(
                                    text = currentDateString,
                                    fontSize = 10.sp,
                                    color = SoftTheme.SoftGray
                                )
                            }
                        }

                        // Weather Panel (Left side)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (weatherState != null) "${weatherState?.temperature?.toInt()}°م" else "--°م",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SoftTheme.MintTeal
                                )
                                Text(
                                    text = weatherState?.description ?: "جاري الجلب...",
                                    fontSize = 10.sp,
                                    color = SoftTheme.SoftGray
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(SoftTheme.MintTeal.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                val temp = weatherState?.temperature ?: 25.0
                                val emoji = when {
                                    temp > 32 -> "☀️"
                                    temp < 18 -> "🌧️"
                                    else -> "🍃"
                                }
                                Text(emoji, fontSize = 18.sp)
                            }
                        }
                    }
                }
            }
        }

        item {
            val currentWeeks = progression?.weeks ?: 0
            val pregId = pregState?.id ?: 0
            val milestones = listOf(37, 28, 24, 20, 12)
            val context = LocalContext.current
            val prefs = remember { context.getSharedPreferences("milestone_prefs", android.content.Context.MODE_PRIVATE) }

            var activeMilestone by remember(currentWeeks, pregId) {
                mutableStateOf(
                    milestones.firstOrNull { mWeek ->
                        currentWeeks >= mWeek && !prefs.getBoolean("milestone_dismissed_${pregId}_$mWeek", false)
                    }
                )
            }

            activeMilestone?.let { mWeek ->
                MilestoneCelebrationCard(
                    weeks = mWeek,
                    onDismiss = {
                        prefs.edit().putBoolean("milestone_dismissed_${pregId}_$mWeek", true).apply()
                        activeMilestone = null
                    }
                )
            }
        }

        item {
            val showPastMemories by viewModel.showPastPregnancyMemoriesState.collectAsStateWithLifecycle()
            val allPregnancies by viewModel.allPregnanciesState.collectAsStateWithLifecycle()
            val allFetalLogs by viewModel.allFetalGrowthLogsState.collectAsStateWithLifecycle()
            var isMemoryDismissedToday by remember { mutableStateOf(false) }

            if (showPastMemories && !isMemoryDismissedToday) {
                PastPregnancyMemoryCard(
                    allPregnancies = allPregnancies,
                    allFetalGrowthLogs = allFetalLogs,
                    onDismiss = { isMemoryDismissedToday = true },
                    onNavigateToHistory = { onNavigateToTab(4) }
                )
            }
        }

        if (isUpdateAvailable) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(),
                    colors = CardDefaults.cardColors(containerColor = SoftTheme.MintTeal.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, SoftTheme.MintTeal.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("✨", fontSize = 22.sp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "تحديث مصفوفة نصائح جوري متوفر! 🔄",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftTheme.MintTeal
                                )
                                Text(
                                    text = "تتوفر نصائح جديدة ومخصصة ومصفوفة ميزات محدثة على GitHub. اضغطي للترقية الفورية والاستمتاع بأحدث ميزات صديقتكِ جوري!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SoftTheme.TextWhite.copy(alpha = 0.85f),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                        
                        var isSyncing by remember { mutableStateOf(false) }
                        val syncStatus by viewModel.gitHubSyncStatus.collectAsStateWithLifecycle()
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (syncStatus != null) {
                                Text(
                                    text = syncStatus ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SoftTheme.MintTeal,
                                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                                )
                            }
                            Button(
                                onClick = {
                                    isSyncing = true
                                    viewModel.syncJouriMatrix()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.MintTeal),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = if (isSyncing && syncStatus?.contains("مكتملة") != true) "جاري التحديث..." else "تحديث الآن 🚀",
                                    color = SoftTheme.DeepSlate,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }

        // Standalone JouriWeatherHeader removed to avoid duplication since JouriWellnessNotificationCard contains local weather and hydration recommendations
        
        // Dynamic, highly interactive Jouri Wellness & Notification Center
        item {
            ExactAlarmBannerCard()
        }

        item {
            DailyVitaminsCard(
                viewModel = viewModel,
                onNavigateToMeds = { onNavigateToTab(3) }
            )
        }

        item {
            QuickCravingLogCard(
                viewModel = viewModel,
                onNavigateToCraving = { onNavigateToTab(2) }
            )
        }

        item {
            PregnancyMythBusterCard(
                viewModel = viewModel,
                onNavigateToFoodSafety = { onNavigateToTab(2) },
                onOpenJouriChat = onOpenJouriChat
            )
        }

        item {
            EgyptianFoodSearchWidget(
                viewModel = viewModel,
                onNavigateToNutrition = { onNavigateToTab(2) }
            )
        }

        item {
            JouriWellnessNotificationCard(
                viewModel = viewModel,
                onOpenJouriChat = onOpenJouriChat
            )
        }

        // 🎯 Daily Progress & Briefing Card
        item {
            val waterGoal = viewModel.getWaterTarget()
            val consumedWater = todayWaterLog?.amountMl ?: 0
            val waterPct = if (waterGoal > 0) (consumedWater.toFloat() / waterGoal.toFloat()).coerceIn(0f, 1f) else 0f
            
            val stepGoal = settings?.dailyStepTarget ?: 6000
            val currentSteps = todayStepLog?.steps ?: 0
            val stepsPct = if (stepGoal > 0) (currentSteps.toFloat() / stepGoal.toFloat()).coerceIn(0f, 1f) else 0f
            
            val upcomingAppt = appointments
                .filter { it.dateTime >= System.currentTimeMillis() && !it.completed }
                .minByOrNull { it.dateTime }
                
            Card(
                modifier = Modifier.fillMaxWidth().testTag("daily_vitals_summary_card"),
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "مؤشراتكِ الحيوية اليوم 🎯📈",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.TextWhite
                        )
                        TextButton(
                            onClick = { onNavigateToTab(2) },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("الترطيب والغذاء 🥛 ↗", color = SoftTheme.MintTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    // Water Progress Bar (clickable -> tab 2)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToTab(2) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.WaterDrop, contentDescription = null, tint = SoftTheme.MintTeal, modifier = Modifier.size(16.dp))
                                Text("شرب الماء والترطيب 🥛", style = MaterialTheme.typography.bodyMedium, color = SoftTheme.TextWhite)
                            }
                            Text("$consumedWater / $waterGoal مل ↗", style = MaterialTheme.typography.bodySmall, color = SoftTheme.MintTeal, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(SoftTheme.DeepSlate)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(waterPct)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SoftTheme.MintTeal)
                            )
                        }
                    }
                    
                    // Steps Progress Bar (clickable -> tab 1)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToTab(1) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.DirectionsRun, contentDescription = null, tint = SoftTheme.SoftPink, modifier = Modifier.size(16.dp))
                                Text("خطوات النشاط اليومي 👣", style = MaterialTheme.typography.bodyMedium, color = SoftTheme.TextWhite)
                            }
                            Text("$currentSteps / $stepGoal خطوة ↗", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftPink, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(SoftTheme.DeepSlate)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(stepsPct)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SoftTheme.SoftPink)
                            )
                        }
                    }
                    
                    // Upcoming appointment or message (clickable -> tab 3)
                    if (upcomingAppt != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SoftTheme.DeepSlate)
                                .clickable { onNavigateToTab(3) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = SoftTheme.SoftPink, modifier = Modifier.size(18.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("موعدكِ الطبي القادم 🏥", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftPink, fontWeight = FontWeight.Bold)
                                Text("${upcomingAppt.title} - ${formatGregorianDate(upcomingAppt.dateTime)}", style = MaterialTheme.typography.bodySmall, color = SoftTheme.TextWhite)
                            }
                            Text("تفاصيل ↗", fontSize = 11.sp, color = SoftTheme.SoftPink, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SoftTheme.DeepSlate)
                                .clickable { onNavigateToTab(3) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = SoftTheme.MintTeal, modifier = Modifier.size(16.dp))
                            Text("لا توجد مواعيد مسجلة. اضغطي لإضافة موعد طبي 🏥 ↗", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                        }
                    }
                }
            }
        }

        // ⚡ Quick Actions & Logging Hub
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    val isKickActive = activeStart != null
                    Text(
                        text = "التسجيل الصحي السريع ⚡",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.TextWhite
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Water Quick Log Button
                        Button(
                            onClick = { viewModel.addWater(250) },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.DeepSlate),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f).height(64.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("🥛 +٢٥٠مل", fontWeight = FontWeight.Bold, color = SoftTheme.MintTeal, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("سجل ماء", style = MaterialTheme.typography.bodySmall, color = SoftTheme.TextWhite, fontSize = 10.sp)
                            }
                        }
                        
                        // Blood Pressure Dialog Trigger
                        Button(
                            onClick = { showAddBpDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.DeepSlate),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f).height(64.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("🩸 قياس", fontWeight = FontWeight.Bold, color = SoftTheme.SoftPink, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("ضغط الدم", style = MaterialTheme.typography.bodySmall, color = SoftTheme.TextWhite, fontSize = 10.sp)
                            }
                        }
                        
                        // Journal/Diary Dialog Trigger
                        Button(
                            onClick = { showAddJournalDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.DeepSlate),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f).height(64.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("📝 تدوين", fontWeight = FontWeight.Bold, color = SoftTheme.SoftPink, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("يومياتي", style = MaterialTheme.typography.bodySmall, color = SoftTheme.TextWhite, fontSize = 10.sp)
                            }
                        }
                        
                        // Fetal Kicks Quick Log
                        Button(
                            onClick = {
                                if (isKickActive) {
                                    viewModel.incrementKickCount()
                                } else {
                                    viewModel.startFetalKickSession()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isKickActive) SoftTheme.SoftPink else SoftTheme.DeepSlate),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1.2f).height(64.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                if (isKickActive) {
                                    Text("🦶 ركلة! ($currentCount)", fontWeight = FontWeight.ExtraBold, color = SoftTheme.DeepSlate, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("احفظ", style = MaterialTheme.typography.bodySmall, color = SoftTheme.DeepSlate, fontSize = 9.sp, modifier = Modifier.clickable { viewModel.saveFetalKickSession() })
                                } else {
                                    Text("🤰 حركة", fontWeight = FontWeight.Bold, color = SoftTheme.MintTeal, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("ركلات الجنين", style = MaterialTheme.typography.bodySmall, color = SoftTheme.TextWhite, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                    
                    if (isKickActive) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("جلسة عد حركة الجنين نشطة حالياً ✨", color = SoftTheme.SoftPink, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "إلغاء ❌",
                                    color = SoftTheme.RedDanger,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable { viewModel.cancelFetalKickSession() }
                                )
                                Text(
                                    text = "حفظ وحساب 🏁",
                                    color = SoftTheme.MintTeal,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable { viewModel.saveFetalKickSession() }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (pregState == null || pregState?.isPregnant != true) {
            // Not Pregnant View - Call to Action
            item {
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
                            text = "تتبع الحمل الشخصي 🤰",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.TextWhite,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "ابدئي تتبع مراحل نمو جنينكِ أسبوعياً، مع حساب تلقائي لموعد الولادة المقدر، وتوجيهات السعرات الغذائية والعناصر الحرجة المناسبة لكِ.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SoftTheme.SoftGray,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                        Button(
                            onClick = { showSetupDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("start_pregnancy_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("البدء في تتبع الحمل الآن 🌸")
                        }
                    }
                }
            }

            // Simple Offline Tips Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "💡 معلومات الدورة الشهرية",
                            color = SoftTheme.MintTeal,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "انتقلي إلى علامة تبويب 'الدورة والخصوبة' لتسجيل دورتكِ الشهرية والتنبؤ بتواريخ الخصوبة والإباضة المستقبلية بمجرد تسجيل ٣ دورات متتالية.",
                            color = SoftTheme.SoftGray,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        } else if (pregState?.isDelivered == true) {
            // +++ أضيف بناءً على طلبك لتقديم نصائح ودعم فترة النفاس والتعافي +++
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Text(
                            text = "الحمد لله على سلامتكِ يا أميرة! 🎉🤱",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.SoftPink
                        )
                        
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(SoftTheme.DeepSlate),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("👶🍼", fontSize = 48.sp)
                        }

                        val bName = pregState?.babyName
                        val bNameGreeting = if (!bName.isNullOrBlank()) " ومولودكِ الغالي ($bName)" else " ومولودكِ الغالي"
                        Text(
                            text = "الحمد لله الذي وهبكِ$bNameGreeting بالسلامة وأقرّ عينكِ به. رحلتكِ كأمّ تبدأ الآن، وجوري معكِ خطوة بخطوة للعناية بصحتكِ الجسدية والنفسية في فترة النفاس والتعافي.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SoftTheme.TextWhite,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )

                        val method = pregState?.birthMethod ?: "طبيعي"
                        val isNatural = method == "طبيعي"
                        
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "إرشادات التعافي بعد الولادة ال${if (isNatural) "طبيعية 🌸" else "قيصرية 🏥"}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftTheme.MintTeal
                                )
                                
                                val recoveryTips = if (isNatural) {
                                    listOf(
                                        "🧘‍♀️ العناية بمنطقة العجان: استخدمي مغاطس دافئة ومسكنات موضعية لتخفيف آلام الغرز وسرعة التئامها.",
                                        "🚶‍♀️ الحركة الخفيفة: المشي الخفيف يومياً ينشط الدورة الدموية ويمنع التجلطات ويساعد الرحم على العودة لحجمه الطبيعي.",
                                        "💪 تمارين قاع الحوض (كيجل): ابدئي بممارستها بلطف بمجرد زوال الألم لتقوية عضلات الحوض والتحكم الفعال.",
                                        "🍼 الرضاعة الطبيعية: الرضاعة المبكرة تساعد على انقباض الرحم وإفراز هرمون السعادة وتقوية مناعة طفلكِ."
                                    )
                                } else {
                                    listOf(
                                        "🩹 العناية بجرح العملية: الحفاظ على الجرح جافاً ونظيفاً، وتجنب رفع أي شيء أثقل من طفلكِ لحماية الغرز الداخلية والخارجية.",
                                        "💊 تخفيف الآلام: الالتزام بالمسكنات الموصوفة من الطبيبة لتتمكني من التحرك وإرضاع طفلكِ براحة وبدون ضغوط جسدية.",
                                        "🥑 الوقاية من الغازات والإمساك: شرب السوائل بكثرة وتناول الألياف والمشي اللطيف لتنشيط الأمعاء بعد التخدير.",
                                        "🧸 دعم البطن: استخدمي وسادة ناعمة لدعم بطنكِ عند السعال أو العطس أو الضحك لتخفيف الضغط المفاجئ على جرح القيصرية."
                                    )
                                }
                                
                                recoveryTips.forEach { tip ->
                                    Text(
                                        text = tip,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SoftTheme.SoftGray,
                                        lineHeight = 20.sp,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        // Water reminder customization for breastfeeding
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("💧", fontSize = 28.sp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "تنبيه شرب الماء للمرضع",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = SoftTheme.TextWhite
                                    )
                                    Text(
                                        text = "تحتاج الأم المرضعة إلى زيادة شرب المياه بمقدار 500-1000 مل إضافية يومياً للحفاظ على كمية إدرار الحليب وصحتها العامة.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SoftTheme.SoftGray,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = { viewModel.switchToPeriodTracking() },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("العودة لتتبع الدورة الطبيعية 🔄", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // Pregnant View - Progression Dashboard
            progression?.let { prog ->
                // Calculate dynamic trimester color
                val trimesterColor = when {
                    prog.weeks >= 41 -> Color(0xFFFFB300) // Month 10: Gold Amber
                    prog.trimester == 1 -> Color(0xFF9575CD) // Trimester 1: Lavender
                    prog.trimester == 2 -> SoftTheme.MintTeal // Trimester 2: Mint Teal
                    else -> SoftTheme.SoftPink // Trimester 3: Soft Pink
                }

                val monthProg = calculateMonthProgress(prog.weeks, prog.daysIntoWeek)
                val activeMonth = monthProg.monthNumber
                val activeMonthProgress = monthProg.progressFraction

                // Baby Info (Gender & Name Display/Edit Card)
                val babyGender = pregState?.babyGender
                val babyName = pregState?.babyName
                val isGenderKnown = !babyGender.isNullOrEmpty()
                
                if (prog.weeks >= 14 || isGenderKnown) {
                    item {
                        if (isGenderKnown) {
                            // Compact space-saving version of Baby Info Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(SoftTheme.DeepSlate),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(if (babyGender == "ولد") "👶" else if (babyGender == "بنت") "👧" else "🤰", fontSize = 18.sp)
                                        }
                                        Column {
                                            val genderEmoji = if (babyGender == "ولد") "💙" else if (babyGender == "بنت") "💗" else "✨"
                                            val genderLabel = if (babyGender == "ولد") "ولد صالح معافى" else if (babyGender == "بنت") "بنت صالحة معافاة" else "مفاجأة مباركة"
                                            val nameLabel = if (!babyName.isNullOrBlank()) "الاسم المقترح: $babyName" else "لم يتم اختيار اسم بعد"
                                            
                                            Text(
                                                text = "$genderLabel $genderEmoji",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = SoftTheme.TextWhite
                                            )
                                            Text(
                                                text = nameLabel,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = SoftTheme.SoftPink
                                            )
                                        }
                                    }
                                    Text(
                                        text = "تعديل 📝",
                                        color = SoftTheme.SoftPink,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.clickable {
                                            babyGenderInput = babyGender ?: ""
                                            babyNameInput = babyName ?: ""
                                            showBabyInfoDialog = true
                                        }
                                    )
                                }
                            }
                        } else {
                            // Expandable standard registration Card (visible only after week 14)
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("🧸", fontSize = 22.sp)
                                            Text(
                                                text = "جنينكِ الغالي",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = SoftTheme.TextWhite
                                            )
                                        }
                                        Text(
                                            text = "تسجيل 📝",
                                            color = SoftTheme.SoftPink,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.clickable {
                                                babyGenderInput = babyGender ?: ""
                                                babyNameInput = babyName ?: ""
                                                showBabyInfoDialog = true
                                            }
                                        )
                                    }
                                    Text(
                                        text = "لقد دخلتِ الأسبوع ١٤ من الحمل 🌸 هل عرفتِ جنس جنينكِ؟ اضغطي لتسجيله واقتراح اسمه لكي يتفاعل رفيقكِ مع جنينكِ بالاسم والتهنئة اللطيفة! 💕",
                                        color = SoftTheme.SoftGray,
                                        style = MaterialTheme.typography.bodySmall,
                                        lineHeight = 16.sp
                                    )
                                    Button(
                                        onClick = { showBabyInfoDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.DeepSlate),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("تسجيل جنس واسم الجنين 👶🍼", color = SoftTheme.TextWhite, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }

                // 🌸 Jouri Signature Pregnancy Radial Gauge Card
                item {
                    JouriPregnancyRadialGauge(
                        progression = prog,
                        activeMonth = activeMonth,
                        activeMonthProgress = activeMonthProgress,
                        trimesterColor = trimesterColor,
                        onFetalClick = {
                            onNavigateToTab(4) // Navigate to Tools tab
                        }
                    )
                }

                // 🌟 Jouri Pregnancy Bento Grid (Baby Dev + Daily Activity Sparkline)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Card 1: Baby Development (تطور الجنين)
                        PregnancyBabyDevCard(
                            progression = prog,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToTab(4) }
                        )

                        // Card 2: Daily Activity Sparkline (النشاط والراحة)
                        PregnancyDailyActivityCard(
                            steps = todayStepLog?.steps ?: 0,
                            stepGoal = settings?.dailyStepTarget ?: 6000,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToTab(1) } // Navigate to Fitness/Water tab
                        )
                    }
                }

                // 📅 Next Appointment Card (موعدكِ القادم)
                item {
                    val upcomingAppt = appointments
                        .filter { it.dateTime >= System.currentTimeMillis() && !it.completed }
                        .minByOrNull { it.dateTime }

                    PregnancyNextAppointmentCard(
                        appointment = upcomingAppt,
                        onAddOrViewAppointments = { onNavigateToTab(3) } // Navigate to Symptoms/Appointments tab
                    )
                }

                // Post-term Pregnancy (الشهر العاشر) Supportive Card
                if (prog.weeks >= 40) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(1.5.dp, Color(0xFFFFB300))
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("📢", fontSize = 24.sp)
                                    Text(
                                        text = "الولادة بعد موعدكِ المقدر (الشهر العاشر) 🌸🏥",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFB300)
                                    )
                                }

                                Text(
                                    text = "صديقتي الغالية، تخطي موعد الولادة المتوقع (الأسبوع 40) هو أمر شائع يحدث لكثير من الأمهات. إليكِ أهم الإرشادات التوعوية للتعامل مع هذه المرحلة ومتابعتها مع طبيبكِ:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SoftTheme.TextWhite,
                                    lineHeight = 18.sp
                                )

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "📋 إرشادات توعوية عامة:",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = SoftTheme.MintTeal
                                        )
                                        Text(
                                            text = "• المتابعة مع الطبيبة: التنسيق مع طبيبتكِ المعالجة لتقييم صحة الجنين والمشيمة بحسب الخطة الطبية المناسبة لحالتكِ.\n• متابعة حركة الجنين: الانتباه لنمط حركة الجنين المعتاد، والتواصل الفوري مع الفريق الطبي عند ملاحظة أي تراجع أو تغير ملحوظ في الحركة.\n• الراحة والاسترخاء: الحفاظ على الترطيب الكافي والراحة التامة والنشاط البدني الخفيف وفق إرشادات الطبيب.\n• متى تتوجهين فوراً للمستشفى أو الطوارئ؟ عند نزول السائل الأمنيوسي (ماء الجنين)، حدوث نزيف مهبلي، آلام حادة مستمرة، أو انخفاض ملحوظ في حركة الجنين.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SoftTheme.SoftGray,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Safety Birth Baby Announcement Card (Show only late in pregnancy, Week 36 or later)
                if (prog.weeks >= 36) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Text(
                                    text = "بشرى ولادة جديدة؟ ✨👶🎉",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftTheme.MintTeal
                                )
                                Text(
                                    text = "إذا منّ الله عليكِ بالولادة بفضله، شاركينا لنحتفي بكِ ونقدم لكِ إرشادات فترة النفاس والتعافي المثالية الخاصة بطريقة ولادتكِ 💖",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SoftTheme.SoftGray,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 16.sp
                                )
                                Button(
                                    onClick = { showDeliveryDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text("الحمد لله، وضعتُ مولودي بالسلامة! 🥰", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }

                // Discard Pregnancy Info Button
                item {
                    OutlinedButton(
                        onClick = { viewModel.switchToPeriodTracking() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftTheme.RedDanger),
                        border = BorderStroke(1.dp, SoftTheme.RedDanger.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("إنهاء تتبع الحمل الحالي والعودة للدورة")
                    }
                }
            }
        }

        // 📖✨ Spiritual & Quietude Reflection Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate.copy(alpha = 0.8f)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "الجانب الروحي والسكينة 📖✨",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.SoftPink,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "﴿رَبِّ هَبْ لِي مِن لَّدُنكَ ذُرِّيَّةً طَيِّبَةً ۖ إِنَّكَ سَمِيعُ الدُّعَاءِ﴾",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.TextWhite,
                        textAlign = TextAlign.Center,
                        lineHeight = 26.sp
                    )
                    Text(
                        text = "طمأنينة قلبكِ وراحتكِ النفسية تنعكس حباً وسلاماً على صحتكِ ونمو طفلكِ. استودعي نفسكِ وجنينكِ الله الخالق العليم كل يوم.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTheme.SoftGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }

    // Pregnancy setup Dialog
    if (showSetupDialog) {
        Dialog(onDismissRequest = { showSetupDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "تهانينا! ابدئي رحلة حمل آمنة 🌸",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.SoftPink
                    )

                    Text(
                        text = "تاريخ أول يوم لآخر دورة شهرية:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoftTheme.TextWhite
                    )

                    // Dynamic period selection based on actual recorded periods, with fallback to computed dates
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (periodLogs.isNotEmpty()) {
                            Text(
                                text = "اختاري تاريخاً من دوراتكِ الشهرية السابقة: 👇",
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftTheme.SoftPink,
                                fontWeight = FontWeight.Bold
                            )
                            val sortedPeriods = periodLogs.sortedByDescending { it.startDate }.take(4)
                            sortedPeriods.forEach { log ->
                                val startTime = log.startDate
                                val isStartSelected = selectedLastPeriod == startTime
                                val startLabel = "بداية الدورة: ${formatGregorianDate(startTime)}"
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isStartSelected) SoftTheme.SoftPink else SoftTheme.DeepSlate)
                                        .clickable { selectedLastPeriod = startTime }
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = startLabel,
                                            color = if (isStartSelected) SoftTheme.DeepSlate else SoftTheme.TextWhite,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                    if (isStartSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = SoftTheme.DeepSlate
                                        )
                                    }
                                }

                                if (log.endDate != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val endTime = log.endDate
                                    val isEndSelected = selectedLastPeriod == endTime
                                    val endLabel = "نهاية الدورة: ${formatGregorianDate(endTime)} 🏁"
                                    
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isEndSelected) SoftTheme.SoftPink else SoftTheme.DeepSlate)
                                            .clickable { selectedLastPeriod = endTime }
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = endLabel,
                                                color = if (isEndSelected) SoftTheme.DeepSlate else SoftTheme.TextWhite,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        }
                                        if (isEndSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = SoftTheme.DeepSlate
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "أو اختاري تاريخاً مخصصاً آخر:",
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftTheme.SoftGray,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        val dates = remember {
                            (0..4).map { weeksAgo ->
                                val cal = Calendar.getInstance()
                                cal.add(Calendar.WEEK_OF_YEAR, -weeksAgo)
                                cal.timeInMillis
                            }
                        }
                        dates.forEach { time ->
                            // Avoid duplicating if this exact time is already shown as a period log
                            val alreadyShown = periodLogs.any { it.startDate == time }
                            if (!alreadyShown) {
                                val isSelected = selectedLastPeriod == time
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) SoftTheme.SoftPink else SoftTheme.DeepSlate)
                                        .clickable { selectedLastPeriod = time }
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        formatGregorianDate(time),
                                        color = if (isSelected) SoftTheme.DeepSlate else SoftTheme.TextWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = SoftTheme.DeepSlate
                                        )
                                    }
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { weightInput = it },
                        label = { Text("الوزن قبل الحمل (كجم)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoftTheme.SoftPink,
                            unfocusedBorderColor = SoftTheme.SoftGray,
                            focusedTextColor = SoftTheme.TextWhite,
                            unfocusedTextColor = SoftTheme.TextWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = heightInput,
                        onValueChange = { heightInput = it },
                        label = { Text("الطول (سم)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoftTheme.SoftPink,
                            unfocusedBorderColor = SoftTheme.SoftGray,
                            focusedTextColor = SoftTheme.TextWhite,
                            unfocusedTextColor = SoftTheme.TextWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(
                            onClick = { showSetupDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء", color = SoftTheme.SoftGray)
                        }

                        Button(
                            onClick = {
                                val date = selectedLastPeriod ?: System.currentTimeMillis()
                                val weight = weightInput.toDoubleOrNull()
                                val height = heightInput.toDoubleOrNull()
                                viewModel.setPregnancy(date, weight, height)
                                showSetupDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("حفظ")
                        }
                    }
                }
            }
        }
    }

    // --- Blood Pressure Quick Logging Dialog ---
    if (showAddBpDialog) {
        BloodPressureDialog(
            onDismiss = { showAddBpDialog = false },
            onSave = { sys, dia, pulse, notes ->
                viewModel.addBloodPressureLog(
                    systolic = sys,
                    diastolic = dia,
                    pulse = pulse,
                    notes = notes
                )
                showAddBpDialog = false
            }
        )
    }

    // --- Journal Quick Logging Dialog ---
    if (showAddJournalDialog) {
        var selectedMood by remember { mutableStateOf("🌸") }
        val moods = listOf("🌸", "😊", "😴", "😔", "🤰")
        Dialog(onDismissRequest = { showAddJournalDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "تدوين يومية جديدة 📝",
                        style = MaterialTheme.typography.titleLarge,
                        color = SoftTheme.TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Mood Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        moods.forEach { mood ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (selectedMood == mood) SoftTheme.SoftPink else SoftTheme.DeepSlate)
                                    .clickable { selectedMood = mood },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(mood, fontSize = 20.sp)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = journalContent,
                        onValueChange = { journalContent = it },
                        label = { Text("اكتبي مشاعركِ أو ملاحظاتكِ هنا...") },
                        minLines = 3,
                        maxLines = 5,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoftTheme.SoftPink,
                            unfocusedBorderColor = SoftTheme.SoftGray,
                            focusedTextColor = SoftTheme.TextWhite,
                            unfocusedTextColor = SoftTheme.TextWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(
                            onClick = { showAddJournalDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء", color = SoftTheme.SoftGray)
                        }

                        Button(
                            onClick = {
                                if (journalContent.isNotEmpty()) {
                                    viewModel.addJournalEntry(journalContent, selectedMood)
                                    showAddJournalDialog = false
                                    journalContent = ""
                                }
                            },
                            enabled = journalContent.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SoftTheme.SoftPink,
                                disabledContainerColor = SoftTheme.SoftPink.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("حفظ")
                        }
                    }
                }
            }
        }
    }

    // +++ أضيف بناءً على طلبك لتقديم حوار تسجيل جنس الجنين واسمه المقترح +++
    if (showBabyInfoDialog) {
        Dialog(onDismissRequest = { showBabyInfoDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "تسجيل جنس واسم الجنين 👶🍼",
                        style = MaterialTheme.typography.titleLarge,
                        color = SoftTheme.TextWhite,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "شاركينا جنس واسم جنينكِ لنخصص التوجيهات باسمه العذب وندخل البهجة على رحلتكما 💖",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTheme.SoftGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )

                    // Gender Selection Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("ولد", "بنت", "مفاجأة").forEach { gender ->
                            val isSelected = babyGenderInput == gender
                            Button(
                                onClick = { babyGenderInput = gender },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) SoftTheme.SoftPink else SoftTheme.DeepSlate
                                ),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = when (gender) {
                                        "ولد" -> "ولد 💙"
                                        "بنت" -> "بنت 💗"
                                        else -> "مفاجأة 🤫"
                                    },
                                    color = if (isSelected) Color.White else SoftTheme.SoftGray,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Baby Name Text Field
                    OutlinedTextField(
                        value = babyNameInput,
                        onValueChange = { babyNameInput = it },
                        label = { Text("الاسم المقترح لجنينكِ العذب:") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoftTheme.SoftPink,
                            unfocusedBorderColor = SoftTheme.SoftGray,
                            focusedTextColor = SoftTheme.TextWhite,
                            unfocusedTextColor = SoftTheme.TextWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(
                            onClick = { showBabyInfoDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء", color = SoftTheme.SoftGray)
                        }

                        Button(
                            onClick = {
                                viewModel.updateBabyInfo(babyGenderInput.ifEmpty { null }, babyNameInput.ifEmpty { null })
                                showBabyInfoDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("حفظ")
                        }
                    }
                }
            }
        }
    }

    // +++ أضيف بناءً على طلبك لتقديم حوار اختيار طريقة الولادة طبيعي/قيصري +++
    if (showDeliveryDialog) {
        Dialog(onDismissRequest = { showDeliveryDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "مبارك مبارك يا غالية! 🥳💖👶",
                        style = MaterialTheme.typography.titleLarge,
                        color = SoftTheme.TextWhite,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "ألف الحمد لله على سلامتكِ وسلامة مولودكِ الحبيب، جعله الله ذريّة صالحة بارّة قرّة لعينيكِ.\n\nكيف كانت ولادتكِ الميمونة لكي يقدم لكِ رفيقكِ جوري أهم إرشادات التعافي والنفاس المخصصة لكِ؟",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTheme.SoftGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                viewModel.updateDeliveryInfo(isDelivered = true, birthMethod = "طبيعي")
                                showDeliveryDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("ولادة طبيعية 🌸", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.updateDeliveryInfo(isDelivered = true, birthMethod = "قيصري")
                                showDeliveryDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.DeepSlate),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("ولادة قيصرية 🏥", color = SoftTheme.SoftPink, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}




