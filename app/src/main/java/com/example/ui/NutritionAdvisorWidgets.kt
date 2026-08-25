package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.viewmodel.WomanCompanionViewModel
import kotlinx.coroutines.launch


// --- Smart Nutrition Advisor Card ---
@Composable
fun SmartNutritionAdvisorCard(
    viewModel: WomanCompanionViewModel
) {
    val phase = viewModel.getCurrentCyclePhase()
    val periodLogs by viewModel.periodLogsState.collectAsStateWithLifecycle()
    val pregnancyState by viewModel.pregnancyState.collectAsStateWithLifecycle()
    
    var adviceText by remember { mutableStateOf<String?>(null) }
    var isLoadingAdvice by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val currentPhaseName = if (pregnancyState != null) "حامل" else phase.phaseArabic
    
    val defaultAdvice = remember(pregnancyState, phase.phaseName) {
        if (pregnancyState != null) {
            "🥑 **تغذية الحمل المبارك**:\n" +
            "• **حمض الفوليك (Folate)**: ضروري لنمو الجهاز العصبي (متوفر في السبانخ، البروكلي، والعدس).\n" +
            "• **الكالسيوم والحديد**: لتعزيز عظام طفلكِ وتفادي فقر الدم (ألبان، لحوم حمراء، تين مجفف).\n" +
            "• **أوميجا-3**: لتطور ذكاء الجنين وبصره (الأسماك الدهنية كالسلمون، الجوز، وبذور الكتان)."
        } else {
            when (phase.phaseName) {
                "Menstruation" -> {
                    "🩸 **مرحلة الطمث (الدورة)**:\n" +
                    "• **زيادة الحديد**: لتعويض الفقد الحاصل (تناولي اللحوم الحمراء، العدس، الشمندر).\n" +
                    "• **فيتامين C**: يعزز امتصاص الحديد (اعصري ليموناً فوق السلطة، تناولي البرتقال والفراولة).\n" +
                    "• **الماغنسيوم**: لتسكين التشنجات والمغص (الشوكولاتة الداكنة والموز والمكسرات)."
                }
                "Follicular" -> {
                    "🌱 **الطور الجريبي (الاستعداد للبويضة)**:\n" +
                    "• **دعم هرمون الاستروجين**: تناولي الحبوب الكاملة وبذور الكتان والأفوكادو.\n" +
                    "• **مضادات الأكسدة**: لحماية الخلايا البويضية (الخضار الورقية، الحمضيات، والبيض)."
                }
                "Ovulation" -> {
                    "✨ **مرحلة الإباضة (الخصوبة العالية)**:\n" +
                    "• **طاقة وخصوبة**: بروتينات خفيفة، دهون صحية، وتقليل الكربوهيدرات المكررة.\n" +
                    "• **فيتامينات دعم الهرمونات**: الفواكه الطازجة، الأسماك، البروكلي، والكينوا."
                }
                "Luteal" -> {
                    "🍂 **الطور الأصفري (ما قبل الدورة)**:\n" +
                    "• **تقليل الصوديوم**: لمنع احتباس السوائل المزعج والانتفاخ.\n" +
                    "• **التحكم بالرغبة في السكريات**: تناولي النشويات المعقدة كالشوفان والبطاطا الحلوة.\n" +
                    "• **الماغنسيوم وفيتامين B6**: لتهدئة المزاج وتقلباته (الموز، الحمص، واللوز)."
                }
                else -> {
                    "🥑 **تغذية صحية متوازنة**:\n" +
                    "• ركّزي على شرب ٣ لتر من الماء يومياً.\n" +
                    "• تجنبي الأطعمة المصنعة والزيوت المهدرجة."
                }
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.2f))
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🥑", fontSize = 24.sp)
                    Column {
                        Text(
                            text = "مستشار التغذية الذكي بالذكاء الاصطناعي",
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.TextWhite,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "مرحلتكِ الحالية: $currentPhaseName",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTheme.SoftPink
                        )
                    }
                }
            }

            AnimatedVisibility(visible = adviceText != null) {
                adviceText?.let { text ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SoftTheme.DeepSlate, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = SoftTheme.TextWhite,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            if (adviceText == null) {
                Text(
                    text = "سجّلي دورتكِ أو حملكِ واضغطي للحصول على نصائح وجبات متوازنة مولدة بالذكاء الاصطناعي لتلبية احتياجات جسمكِ الدقيقة في هذا الطور.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SoftTheme.SoftGray,
                    lineHeight = 16.sp
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SoftTheme.DeepSlate.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("💡 نصيحة الطور الافتراضية:", style = MaterialTheme.typography.bodySmall, color = SoftTheme.MintTeal, fontWeight = FontWeight.Bold)
                        Text(text = defaultAdvice, style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                    }
                }
            }

            Button(
                onClick = {
                    isLoadingAdvice = true
                    scope.launch {
                        val recentSymptoms = periodLogs.maxByOrNull { it.startDate }?.symptoms ?: "لا توجد"
                        val prompt = """
بصفتكِ أخصائية تغذية وصحة نسائية ذكية ومتعاطفة للغاية، اقترحي لي وجبات غذائية متوازنة مخصصة ونصائح صحية بناءً على المعطيات التالية:
1. مرحلتي الحالية: $currentPhaseName
2. الأعراض الأخيرة المسجلة: $recentSymptoms
اقترحي أفكار وجبات مغذية ومحددة (فطور، غداء، عشاء، ووجبة خفيفة) مصممة لتلبية احتياجات جسمي الآن (مثل: زيادة الحديد والماغنسيوم أثناء الدورة، أو البروتينات والألياف أثناء الإباضة، إلخ).
قدمي الإجابة باللغة العربية بأسلوب حميمي ورقيق جداً كأنكِ أختي الكبرى أو صديقتي المقربة (استخدمي عبارات مثل "يا غالية"، "يا عزيزتي")، واعرضي الوجبات في نقاط منسقة وجميلة وقصيرة مع تفاصيل المكونات والفوائد الصحية لكل وجبة.
                        """.trimIndent()
                        
                        val response = GeminiService.generateContent(prompt)
                        adviceText = response
                        isLoadingAdvice = false
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("generate_nutrition_btn"),
                enabled = !isLoadingAdvice
            ) {
                if (isLoadingAdvice) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = SoftTheme.TextWhite, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("جاري استشارة الذكاء الاصطناعي...")
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (adviceText != null) "تحديث النصائح بالذكاء الاصطناعي ✨" else "توليد وجبات ذكية مخصصة بالذكاء الاصطناعي ✨")
                }
            }

            // +++ أضيف بناءً على طلبك لدعم مكتبة نصائح جوري التفاعلية المحلية لسلامة الحمل والرشاقة +++
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = SoftTheme.SoftGray.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "📚 مكتبة جوري للنصائح والتوجيهات التفاعلية:",
                fontWeight = FontWeight.Bold,
                color = SoftTheme.TextWhite,
                style = MaterialTheme.typography.bodyMedium
            )

            var selectedAdviceTab by remember { mutableStateOf("الحمل 🤰") }
            var activeDetailedAdvice by remember { mutableStateOf<AdviceCardInfo?>(null) }

            // Category Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                adviceLibrary.keys.forEach { tabName ->
                    val isSel = selectedAdviceTab == tabName
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSel) SoftTheme.SoftPink else SoftTheme.DeepSlate)
                            .clickable { selectedAdviceTab = tabName }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = tabName,
                            color = if (isSel) SoftTheme.DeepSlate else SoftTheme.TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Cards under the selected category
            val adviceList = adviceLibrary[selectedAdviceTab] ?: emptyList()
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                adviceList.forEach { cardInfo ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SoftTheme.DeepSlate)
                            .clickable { activeDetailedAdvice = cardInfo }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(cardInfo.icon, fontSize = 24.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = cardInfo.title,
                                fontWeight = FontWeight.Bold,
                                color = SoftTheme.TextWhite,
                                fontSize = 12.sp
                            )
                            Text(
                                text = cardInfo.summary,
                                color = SoftTheme.SoftGray,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text("◀️", fontSize = 10.sp, color = SoftTheme.SoftPink)
                    }
                }
            }

            // Detailed Advice Dialog
            activeDetailedAdvice?.let { advice ->
                Dialog(onDismissRequest = { activeDetailedAdvice = null }) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(20.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(advice.icon, fontSize = 32.sp)
                                Text(
                                    text = advice.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftTheme.SoftPink,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Text(
                                text = advice.details,
                                style = MaterialTheme.typography.bodyMedium,
                                color = SoftTheme.TextWhite,
                                lineHeight = 20.sp
                            )

                            Text(
                                text = "💡 خطوات عملية وتوصيات سريعة للغالية:",
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftTheme.MintTeal,
                                fontWeight = FontWeight.Bold
                            )

                            // Interactive checkable tips
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                advice.tips.forEach { tip ->
                                    var isChecked by remember(tip) { mutableStateOf(false) }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(SoftTheme.DeepSlate)
                                            .clickable { isChecked = !isChecked }
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (isChecked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                            contentDescription = null,
                                            tint = if (isChecked) SoftTheme.MintTeal else SoftTheme.SoftGray,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = tip,
                                            color = if (isChecked) SoftTheme.SoftGray else SoftTheme.TextWhite,
                                            fontSize = 11.sp,
                                            lineHeight = 16.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = { activeDetailedAdvice = null },
                                colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("شكراً لكِ جوري 🌸")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiveNutrientSimulatorWidget(
    viewModel: WomanCompanionViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFood by remember { mutableStateOf<EgyptianFoodEntity?>(null) }
    var sugarSpoons by remember { mutableStateOf(0) }
    var useWholeMilk by remember { mutableStateOf(false) }
    
    val presetFoods = remember { EgyptianFoodRepository.presetFoods }
    val filteredFoods = remember(searchQuery) {
        if (searchQuery.trim().isEmpty()) {
            emptyList()
        } else {
            val normalizedQuery = EgyptianFoodRepository.normalizeText(searchQuery)
            presetFoods.filter { food ->
                EgyptianFoodRepository.normalizeText(food.name).contains(normalizedQuery) ||
                EgyptianFoodRepository.normalizeText(food.keywords).contains(normalizedQuery)
            }.take(5)
        }
    }

    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("🧪", fontSize = 24.sp)
                Column {
                    Text(
                        text = "محاكي المغذيات والترطيب التفاعلي",
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.TextWhite,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "اختر أي طعام أو شراب من الداتابيز الكبيرة لترى فائدته وماذا ستستفيدين منه بدقة بالعدّاد!",
                        color = SoftTheme.SoftGray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it 
                    if (it.isEmpty()) selectedFood = null
                },
                label = { Text("ابحثي عن وجبة أو مشروب (مثال: قهوة، ملوخية، كشري...)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SoftTheme.SoftPink) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { 
                            searchQuery = "" 
                            selectedFood = null
                            sugarSpoons = 0
                            useWholeMilk = false
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = null, tint = SoftTheme.SoftGray)
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SoftTheme.SoftPink,
                    unfocusedBorderColor = SoftTheme.SoftGray.copy(alpha = 0.5f),
                    focusedLabelColor = SoftTheme.SoftPink
                )
            )

            // Search results autocomplete list
            if (filteredFoods.isNotEmpty() && selectedFood == null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column {
                        filteredFoods.forEach { food ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedFood = food
                                        searchQuery = food.name
                                        sugarSpoons = 0
                                        useWholeMilk = false
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(food.name, fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite, style = MaterialTheme.typography.bodyMedium)
                                    Text("الحصة: ${food.servingSize} • ${food.calories} سعرة", color = SoftTheme.SoftGray, style = MaterialTheme.typography.labelSmall)
                                }
                                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = SoftTheme.SoftPink, modifier = Modifier.size(16.dp))
                            }
                            if (food != filteredFoods.last()) {
                                Divider(color = SoftTheme.CardSlate, thickness = 1.dp)
                            }
                        }
                    }
                }
            }

            // Default suggestion helper if nothing selected
            if (selectedFood == null) {
                Text("💡 اقتراحات شائعة للتجربة:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = SoftTheme.SoftPink)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val quickSuggestions = listOf(
                        "قهوة فرنساوي بملعقة سكر واحدة",
                        "قهوة تركي سادة",
                        "عسل أسود بالسمسم",
                        "طبق ملوخية",
                        "رز مصري مطبوخ",
                        "كوب ينسون دافئ"
                    )
                    quickSuggestions.forEach { suggestionName ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(SoftTheme.DeepSlate)
                                .clickable {
                                    val match = presetFoods.firstOrNull { it.name == suggestionName }
                                    if (match != null) {
                                        selectedFood = match
                                        searchQuery = match.name
                                        sugarSpoons = if (suggestionName.contains("ملعقة سكر واحدة")) 1 else 0
                                        useWholeMilk = false
                                    } else {
                                        searchQuery = suggestionName
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .border(1.dp, SoftTheme.SoftGray.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        ) {
                            Text(suggestionName, color = SoftTheme.TextWhite, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            // Selected Food details & custom interactive modifiers
            selectedFood?.let { food ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SoftTheme.DeepSlate)
                        .padding(16.dp)
                ) {
                    // Title and Serving Size
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(food.name, fontWeight = FontWeight.Bold, color = SoftTheme.SoftPink, style = MaterialTheme.typography.titleMedium)
                            Text("الحصة الأساسية: ${food.servingSize}", color = SoftTheme.SoftGray, style = MaterialTheme.typography.bodySmall)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SoftTheme.MintTeal.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(food.category.uppercase(), color = SoftTheme.MintTeal, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Divider(color = SoftTheme.CardSlate, thickness = 1.dp)

                    // Interactive modifiers section
                    Text("⚙️ تخصيص الكوب والوجبة بالإضافات:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)

                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Sugar Spoons Counter
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("ملاعق السكر الإضافية:", color = SoftTheme.TextWhite, style = MaterialTheme.typography.bodyMedium)
                                Text("+20 سعرة و +5 جم سكر لكل ملعقة", color = SoftTheme.SoftGray, style = MaterialTheme.typography.labelSmall)
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(
                                    onClick = { if (sugarSpoons > 0) sugarSpoons-- },
                                    modifier = Modifier.size(32.dp).background(SoftTheme.CardSlate, CircleShape)
                                ) {
                                    Text("-", color = SoftTheme.TextWhite, fontWeight = FontWeight.Bold)
                                }
                                Text("$sugarSpoons ملعقة", color = SoftTheme.SoftPink, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                IconButton(
                                    onClick = { sugarSpoons++ },
                                    modifier = Modifier.size(32.dp).background(SoftTheme.CardSlate, CircleShape)
                                ) {
                                    Text("+", color = SoftTheme.TextWhite, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Milk type modifier (if it's a hot beverage / contains milk)
                        if (food.category == "drink" || food.keywords.contains("شاي") || food.keywords.contains("قهوة") || food.keywords.contains("حليب") || food.keywords.contains("لبن")) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("ترقية الحليب لكامل الدسم؟", color = SoftTheme.TextWhite, style = MaterialTheme.typography.bodyMedium)
                                    Text("+60 سعرة، +3 جم بروتين، +3 جم دهون", color = SoftTheme.SoftGray, style = MaterialTheme.typography.labelSmall)
                                }
                                Switch(
                                    checked = useWholeMilk,
                                    onCheckedChange = { useWholeMilk = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = SoftTheme.SoftPink,
                                        checkedTrackColor = SoftTheme.LightPink
                                    )
                                )
                            }
                        }
                    }

                    // Dynamically Calculated nutritional values based on modifiers
                    val calculatedCalories = food.calories + (sugarSpoons * 20) + (if (useWholeMilk) 60 else 0)
                    val calculatedProtein = food.protein + (if (useWholeMilk) 3.0 else 0.0)
                    val calculatedCarbs = food.carbs + (sugarSpoons * 5.0) + (if (useWholeMilk) 4.0 else 0.0)
                    val calculatedFat = food.fat + (if (useWholeMilk) 3.0 else 0.0)
                    val calculatedSugar = food.sugarG + (sugarSpoons * 5.0) + (if (useWholeMilk) 4.0 else 0.0)
                    val calculatedFiber = food.fiberG
                    val calculatedWaterBenefit = food.waterBenefitMl

                    Divider(color = SoftTheme.CardSlate, thickness = 1.dp)

                    // Benefits highlight text
                    Text("💡 ماذا ستستفيدين؟ الفوائد الصحية المباشرة:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = SoftTheme.MintTeal)
                    Text(
                        text = if (food.healthBenefits.isNotEmpty()) food.healthBenefits else "غذاء مغذي يمد جسمكِ بالطاقة والعناصر الحيوية الضرورية.",
                        color = SoftTheme.TextWhite,
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 20.sp
                    )

                    Divider(color = SoftTheme.CardSlate, thickness = 1.dp)

                    // Nutrient breakdown grid
                    Text("📊 الميزان الغذائي المخصّص بعد التعديلات:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
                    
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Macros
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("🔥 السعرات الحرارية: $calculatedCalories سعرة", style = MaterialTheme.typography.labelLarge, color = SoftTheme.TextWhite, fontWeight = FontWeight.Bold)
                            if (calculatedWaterBenefit > 0) {
                                Text("💧 مياه الترطيب: +$calculatedWaterBenefit مل", style = MaterialTheme.typography.labelLarge, color = SoftTheme.MintTeal, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Quick mini grid for macros
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val macroGrid = listOf(
                                "بروتين" to "${"%.1f".format(calculatedProtein)}ج",
                                "نشويات" to "${"%.1f".format(calculatedCarbs)}ج",
                                "دهون" to "${"%.1f".format(calculatedFat)}ج",
                                "سكريات" to "${"%.1f".format(calculatedSugar)}ج",
                                "ألياف" to "${"%.1f".format(calculatedFiber)}ج"
                            )
                            macroGrid.forEach { (lbl, valStr) ->
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SoftTheme.CardSlate)
                                        .padding(vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(lbl, color = SoftTheme.SoftGray, style = MaterialTheme.typography.labelSmall)
                                    Text(valStr, color = SoftTheme.TextWhite, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }

                        // Micros checklist
                        val microsList = mutableListOf<String>()
                        if (food.ironMg > 0) microsList.add("🧲 حديد: ${food.ironMg} ملجم")
                        if (food.calciumMg > 0) microsList.add("🦴 كالسيوم: ${food.calciumMg} ملجم")
                        if (food.vitaminB_Mg > 0) microsList.add("🧠 فوليك: ${"%.0f".format(food.vitaminB_Mg * 100.0)} مكجم")
                        if (food.potassiumMg > 0) microsList.add("💓 بوتاسيوم: ${food.potassiumMg} / صوديوم: ${food.sodiumMg} ملجم")
                        if (food.magnesiumMg > 0) microsList.add("🌿 ماغنسيوم: ${food.magnesiumMg} ملجم")

                        if (microsList.isNotEmpty()) {
                            Text(
                                text = "✨ الفيتامينات والمعادن الدقيقة المتوفرة: " + microsList.joinToString(" • "),
                                style = MaterialTheme.typography.labelSmall,
                                color = SoftTheme.SoftTeal
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Button to add this food directly
                    Button(
                        onClick = {
                            viewModel.addNutritionMeal(
                                mealType = if (food.category == "drink") "مشروب" else "وجبة",
                                description = "${food.name} (معدّل: $sugarSpoons ملعقة سكر${if (useWholeMilk) " + حليب كامل" else ""})",
                                calories = calculatedCalories,
                                iron = food.ironMg,
                                folate = food.vitaminB_Mg * 100.0,
                                calcium = food.calciumMg,
                                omega3 = food.vitaminD_Mcg * 0.1,
                                protein = calculatedProtein,
                                carbs = calculatedCarbs,
                                fat = calculatedFat,
                                sugar = calculatedSugar,
                                fiber = calculatedFiber,
                                waterBenefit = calculatedWaterBenefit
                            )
                            android.widget.Toast.makeText(context, "تم تسجيل ${food.name} والإضافات بنجاح! 🎉", android.widget.Toast.LENGTH_SHORT).show()
                            
                            // reset fields
                            searchQuery = ""
                            selectedFood = null
                            sugarSpoons = 0
                            useWholeMilk = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("سجلي هذا الكوب / الوجبة الآن! ✍️", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

// --- Native Analytics Dashboard ---

