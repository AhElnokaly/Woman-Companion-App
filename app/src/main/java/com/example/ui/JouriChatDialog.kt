package com.example.ui

import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.BuildConfig
import com.example.data.*
import com.example.viewmodel.WomanCompanionViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

enum class JouriExpressionState(val emoji: String, val label: String) {
    WARM_HAPPY("🌸", "سعيدة ودافئة"),
    ATTENTIVE_LISTENING("✍️🌸", "تستمع بإنصات"),
    REASSURING("🤗🌸", "طمأنينة ورعاية"),
    CELEBRATORY("🎉🌸", "محتفلة وفخورة")
}

@Composable
fun JouriAvatar(
    expression: JouriExpressionState = JouriExpressionState.WARM_HAPPY,
    size: androidx.compose.ui.unit.Dp = 40.dp,
    modifier: Modifier = Modifier
) {
    val borderColor = when (expression) {
        JouriExpressionState.CELEBRATORY -> SoftTheme.PregnancyPurple
        JouriExpressionState.REASSURING -> SoftTheme.MintTeal
        JouriExpressionState.ATTENTIVE_LISTENING -> SoftTheme.LightPink
        JouriExpressionState.WARM_HAPPY -> SoftTheme.SoftPink
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .border(1.5.dp, borderColor, CircleShape)
            .testTag("jouri_avatar_${expression.name.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = com.example.R.drawable.jouri_showcase_1783592034174),
            contentDescription = "جوري",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun JouriChatSkeletonResponse(
    companionName: String = "جوري",
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton_shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeleton_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag("jouri_chat_skeleton_response"),
        contentAlignment = Alignment.CenterStart
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = 0.dp,
                bottomEnd = 16.dp
            ),
            modifier = Modifier.widthIn(max = 240.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    JouriAvatar(expression = JouriExpressionState.ATTENTIVE_LISTENING, size = 20.dp)
                    Text(
                        text = "$companionName تفكر وتصيغ لكِ إجابة دافئة...",
                        style = MaterialTheme.typography.labelSmall,
                        color = SoftTheme.SoftPink
                    )
                }
                Box(
                    modifier = Modifier
                        .height(10.dp)
                        .fillMaxWidth(0.9f)
                        .background(SoftTheme.SoftGray.copy(alpha = alpha), RoundedCornerShape(6.dp))
                )
                Box(
                    modifier = Modifier
                        .height(10.dp)
                        .fillMaxWidth(0.7f)
                        .background(SoftTheme.SoftGray.copy(alpha = alpha), RoundedCornerShape(6.dp))
                )
                Box(
                    modifier = Modifier
                        .height(10.dp)
                        .fillMaxWidth(0.5f)
                        .background(SoftTheme.SoftGray.copy(alpha = alpha), RoundedCornerShape(6.dp))
                )
            }
        }
    }
}

// --- Jouri Smart Companion Chat Dialog ---
@Composable
fun JouriChatDialog(
    viewModel: WomanCompanionViewModel,
    onDismiss: () -> Unit,
    onNavigateToTab: (Int) -> Unit = {}
) {
    val pregState by viewModel.pregnancyState.collectAsStateWithLifecycle()
    val settingsState by viewModel.appLockSettingsState.collectAsStateWithLifecycle()
    val todayStepsState by viewModel.todayStepLogState.collectAsStateWithLifecycle()
    val weatherState by viewModel.weatherState.collectAsStateWithLifecycle()
    val savedApiKey by viewModel.apiKeyFlow.collectAsStateWithLifecycle(initialValue = null)
    val isNetworkAvailable by viewModel.isNetworkAvailable.collectAsStateWithLifecycle()

    val companionName = settingsState?.companionName ?: "جوري"

    var inputMessage by remember { mutableStateOf("") }
    var showVirtualKeyboard by remember { mutableStateOf(false) }
    val messages = remember { mutableStateListOf<Pair<String, Boolean>>() } // text, isUser
    var isTyping by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Smart direct shortcut state
    var activeSuggestedAction by remember { mutableStateOf<String?>(null) }
    val lastMessageText = messages.lastOrNull { !it.second }?.first ?: ""
    LaunchedEffect(lastMessageText) {
        val text = lastMessageText.lowercase()
        activeSuggestedAction = when {
            text.contains("ركلات") || text.contains("ركلة") || text.contains("حركة الجنين") || text.contains("حركه الجنين") || text.contains("بيتحرك") -> "fetal_kicks"
            text.contains("انقباض") || text.contains("طلق") || text.contains("5-1-1") || text.contains("تقلص") -> "contractions"
            text.contains("صيام") || text.contains("قضاء") || text.contains("صوم") -> "qada"
            text.contains("مذكرات") || text.contains("يوميات") || text.contains("فضفضة") || text.contains("فضفضه") || text.contains("كتابة") -> "journal"
            text.contains("دواء") || text.contains("أدوية") || text.contains("ادويه") || text.contains("مكمل") || text.contains("فيتامين") -> "meds"
            text.contains("ماء") || text.contains("شربت كوب") -> "water"
            text.contains("غذائ") || text.contains("أكل") || text.contains("طعام") || text.contains("وصفة") || text.contains("ملوخية") || text.contains("كشري") || text.contains("فول") -> "nutrition"
            text.contains("وحم") || text.contains("الوحم") || text.contains("اشته") || text.contains("أشته") || text.contains("توحمت") -> "craving"
            text.contains("نوم") || text.contains("الأرق") || text.contains("ارق") || text.contains("مراقب النوم") -> "sleep"
            text.contains("شريك") || text.contains("الزوج") || text.contains("زوج") || text.contains("عائل") -> "partner"
            text.contains("صيدلية") || text.contains("الصيدلية") || text.contains("مخزون") -> "pharmacy"
            text.contains("تمارين") || text.contains("لياقة") || text.contains("الحركة") || text.contains("رياضة") -> "fitness"
            text.contains("معونتي") || text.contains("المخزون") || text.contains("مشتريات") || text.contains("المقادير") -> "maonaty"
            text.contains("موعد") || text.contains("عيادة") || text.contains("حجز") || text.contains("زيارة") || text.contains("دكتورة") -> "appointments"
            text.contains("حاسبة الحمل") || text.contains("حاسبة الخصوبة") || text.contains("حاسبه") || text.contains("التبويض") -> "smart_conception"
            text.contains("علامات الخطر") || text.contains("أعراض الطوارئ") || text.contains("خطر") || text.contains("طوارئ") || text.contains("نزيف") -> "danger"
            else -> null
        }
    }

    // Offline / Privacy mode state
    val cloudAiConsent by viewModel.cloudAiConsentState.collectAsStateWithLifecycle()
    var showConsentDialog by remember { mutableStateOf(false) }

    val isApiKeyMissing = (BuildConfig.GEMINI_API_KEY == "MY_GEMINI_API_KEY" || BuildConfig.GEMINI_API_KEY.isEmpty()) && savedApiKey.isNullOrBlank()
    var isOfflineMode by remember { mutableStateOf(isApiKeyMissing || !isNetworkAvailable || !cloudAiConsent) }

    LaunchedEffect(isNetworkAvailable, isApiKeyMissing, cloudAiConsent) {
        isOfflineMode = !isNetworkAvailable || isApiKeyMissing || !cloudAiConsent
    }

    val quickReplies = remember {
        listOf(
            "شربت كوب ماء 💧" to "سجّلي كوب ماء 💧",
            "أشعر بمغص/ألم 🥺" to "أشعر ببعض المغص والألم في بطني 🥺",
            "أشعر بصداع/تعب 😢" to "أشعر بصداع وتعب شديد 😢",
            "أشعر بتقلب مزاجي 💔" to "أشعر بتقلبات مزاجية وضيق 💔",
            "نصيحة الغذاء اليومية 🥑" to "أريد نصيحة غذائية مناسبة لمرحلتي 🥑",
            "ما هي مرحلتي الحالية؟ 🌸" to "ما هي مرحلتي الحالية وتفاصيلها؟ 🌸",
            "أنا بخير والحمد لله! 🥰" to "الحمد لله، أنا بخير وبصحة ممتازة اليوم! 🥰"
        )
    }

    var selectedCatalogCategory by remember { mutableStateOf("أعراض 🩺") }
    val catalogCategories = remember { listOf("أعراض 🩺", "أوجاع ⚡", "ضغط الدم ❤️", "التغذية 🥑", "أسئلة ❓") }
    val catalogSubItems = remember {
        mapOf(
            "أعراض 🩺" to listOf(
                "غثيان وترجيع 🤢" to "أشعر بغثيان وترجيع نفسي غامة",
                "إرهاق وخمول 😴" to "أشعر بتعب وإرهاق وخمول تام",
                "صداع ودوخة 😢" to "أشعر بصداع مستمر ودوخة شديدة",
                "تقلب مزاجي 💔" to "أشعر بتقلبات مزاجية وضيق حاد",
                "إمساك وعسر هضم 🥦" to "أعاني من إمساك وصعوبة هضم شديدة"
            ),
            "أوجاع ⚡" to listOf(
                "مغص وتقلصات 🥺" to "أشعر بتقلصات ومغص في الرحم وألم بطن",
                "ألم أسفل الظهر 🤰" to "أشعر بألم أسفل الظهر مزعج",
                "تورم القدمين 🦵" to "أعاني من تورم في القدمين واحتباس سوائل",
                "ألم المفاصل 🦴" to "أشعر بوجع وألم شديد في المفاصل والحوض"
            ),
            "ضغط الدم ❤️" to listOf(
                "الضغط العالي ⚠️" to "ما هي تفاصيل الضغط العالي والوقاية منه وأكلاته المناسبة؟",
                "الضغط الواطي 📉" to "ما هي تفاصيل الضغط الواطي وهبوط الدم والمشروبات المناسبة؟"
            ),
            "التغذية 🥑" to listOf(
                "أكلات للحديد 🩸" to "أريد أكلات مصرية غنية بالحديد لعلاج الأنيميا",
                "أغذية للكالسيوم 🥛" to "أريد أغذية مصرية غنية بالكالسيوم لتقوية العظام",
                "مصادر بروتين 💪" to "أريد وجبات مصرية غنية بالبروتين للغذاء والقوة",
                "خضروات ورقية 🥦" to "انصحيني بخضار وخضروات طازجة وفوائدها",
                "فاكهة مصرية 🍉" to "انصحيني بفاكهة وفواكه مصرية مفيدة ومكوناتها",
                "أعشاب مهدئة 🍵" to "انصحيني بمشروب وأعشاب دافئة مهدئة وفوائدها"
            ),
            "أسئلة ❓" to listOf(
                "المشي والحركة 🚶‍♀️" to "هل المشي والحركة مفيدان في حالتي؟",
                "عداد ركلات الجنين 👶" to "كيف أحسب ركلات الجنين وتتبع حركته؟",
                "قاعدة الولادة 5-1-1 ⏱️" to "ما هي قاعدة 5-1-1 لحساب الانقباضات والولادة؟",
                "رخصة الصيام والعبادة 🌙" to "ما هي تفاصيل قضاء الصيام والعبادات ورخصة الإفطار؟"
            )
        )
    }

    LaunchedEffect(Unit) {
        if (messages.isEmpty()) {
            val motherName = pregState?.motherName
            if (motherName.isNullOrEmpty()) {
                messages.add(Pair("أهلاً بكِ يا صديقتي الغالية! 🌸 أنا $companionName، رفيقتكِ وصديقتكِ الذكية في هذه الرحلة الجميلة. يسعدني جداً التعرف عليكِ! ما هو اسمكِ الكريم؟ وكيف تودين تتبع صحتكِ معي اليوم؟ (هل نتابع الدورة والخصوبة، أم نتابع رحلة الحمل المباركة؟) 🥰", false))
            } else {
                messages.add(Pair("أهلاً بعودتكِ يا صديقتي الغالية $motherName! 🌸 كيف حالكِ اليوم وكيف تشعرين؟ أنا $companionName هنا لأسمعكِ وأقدم لكِ الدعم والنصائح الدافئة خطوة بخطوة. أخبريني بأي أعراض تشعرين بها! 🥰", false))
            }
        }
    }

    LaunchedEffect(messages.size, isTyping) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Helper to send message and get response
    suspend fun handleMessageResponse(text: String) {
        val normalizedText = text.trim().lowercase()
        val isThemeRequest = normalizedText.contains("لون") || 
                             normalizedText.contains("ألوان") || 
                             normalizedText.contains("الوان") || 
                             normalizedText.contains("الوضع الفاتح") || 
                             normalizedText.contains("الوضع الداكن") || 
                             normalizedText.contains("فاتح") || 
                             normalizedText.contains("داكن") || 
                             normalizedText.contains("مظلم") || 
                             normalizedText.contains("مضيء") || 
                             normalizedText.contains("ثيم") ||
                             normalizedText.contains("مظهر")

        if (isThemeRequest) {
            val switchToDark = normalizedText.contains("داكن") || 
                               normalizedText.contains("غامق") || 
                               normalizedText.contains("مظلم") || 
                               normalizedText.contains("ليل") ||
                               normalizedText.contains("أسود") ||
                               normalizedText.contains("اسود")
            
            val switchToLight = normalizedText.contains("فاتح") || 
                                normalizedText.contains("مضيء") || 
                                normalizedText.contains("نهار") ||
                                normalizedText.contains("أبيض") ||
                                normalizedText.contains("ابيض")

            if (switchToDark) {
                viewModel.setThemeMode(true)
                val reply = "من عيوني يا غالية! 🌸✨ تم تغيير مظهر التطبيق إلى الوضع الداكن المريح للعينين في الليل. أتمنى لكِ تصفحاً مريحاً ورعاية صحية هادئة لقلبكِ الرقيق 💕🌃"
                messages.add(Pair(reply, false))
                isTyping = false
                return
            } else if (switchToLight) {
                viewModel.setThemeMode(false)
                val reply = "أبشري يا روحي! 🌸✨ تم تحويل ألوان التطبيق إلى الوضع الفاتح واللطيف ليكون مشرقاً مثل عينيكِ الجميلتين ☀️ أتمنى لكِ يوماً سعيداً ومليئاً بالنشاط والحيوية 🥰💛"
                messages.add(Pair(reply, false))
                isTyping = false
                return
            } else {
                val currentDark = settingsState?.isDarkMode ?: true
                viewModel.setThemeMode(!currentDark)
                val reply = if (currentDark) {
                    "أبشري يا روحي! 🌸✨ تم تحويل ألوان التطبيق إلى الوضع الفاتح واللطيف ليكون مشرقاً مثل عينيكِ الجميلتين ☀️ أتمنى لكِ يوماً سعيداً ومليئاً بالنشاط والحيوية 🥰"
                } else {
                    "من عيوني يا غالية! 🌸✨ تم تغيير مظهر التطبيق إلى الوضع الداكن المريح للعينين في الليل. أتمنى لكِ تصفحاً مريحاً ورعاية صحية هادئة لقلبكِ الرقيق 💕🌃"
                }
                messages.add(Pair(reply, false))
                isTyping = false
                return
            }
        }

        // TASK B4: Check urgent symptoms safety triage first before any normal chat path
        val urgentAlert = com.example.data.SymptomTriage.checkUrgentSymptoms(text, isPregnant = pregState != null)
        if (urgentAlert != null) {
            messages.add(Pair(text, true))
            messages.add(Pair("${urgentAlert.title}\n\n${urgentAlert.message}", false))
            isTyping = false
            return
        }

        // TASK B3: Build short session conversation memory (last 2-3 exchanges)
        val historyPairs = mutableListOf<Pair<String, String>>()
        var histIdx = messages.size - 1
        while (histIdx >= 1 && historyPairs.size < 3) {
            val uMsg = messages.getOrNull(histIdx - 1)
            val bMsg = messages.getOrNull(histIdx)
            if (uMsg != null && bMsg != null && uMsg.second && !bMsg.second) {
                val cleanB = bMsg.first.replace("\n\n🌐 جاري التحسين بالذكاء الاصطناعي...", "").trim()
                historyPairs.add(0, Pair(uMsg.first, cleanB))
                histIdx -= 2
            } else {
                histIdx--
            }
        }

        // First, query the local SQL database cache
        val cachedAnswer = viewModel.getCachedAnswer(text)
        if (cachedAnswer != null) {
            messages.add(Pair(text, true))
            messages.add(Pair(cachedAnswer, false))
            isTyping = false
            return
        }

        // 🧠 Jouri's Local Index / Brain Check: Check her instant offline index first
        val phaseInfo = viewModel.getCurrentCyclePhase()
        val waterLog = viewModel.todayWaterLogState.value?.amountMl ?: 0
        val steps = todayStepsState?.steps ?: 0
        val target = settingsState?.dailyStepTarget ?: 6000
        val offlineResponse = OfflineJouriEngine.getResponse(
            userInput = text,
            motherName = pregState?.motherName,
            phaseInfo = phaseInfo,
            pregnancyState = pregState,
            todayWaterLogged = waterLog,
            companionName = companionName,
            weatherInfo = weatherState,
            todaySteps = steps,
            targetSteps = target
        )

        val useOffline = isOfflineMode || isApiKeyMissing

        // Execute local database actions if matched
        fun executeOfflineAction(actionType: String?, actionValue: Any?) {
            actionType?.let { action ->
                when (action) {
                    "water" -> {
                        val amt = actionValue as? Int ?: 250
                        viewModel.addWater(amt)
                    }
                    "symptom" -> {
                        val pair = actionValue as? Pair<*, *>
                        val symName = pair?.first as? String ?: "مغص وألم"
                        val severity = pair?.second as? Int ?: 5
                        viewModel.addSymptom(symName, severity, "مسجّل تلقائياً بواسطة رفيقتكِ $companionName 🌸")
                    }
                    "profile" -> {
                        val newName = actionValue as? String ?: ""
                        if (newName.isNotEmpty()) {
                            viewModel.setMotherProfile(
                                motherName = newName,
                                babyName = null,
                                userPhase = null,
                                lastPeriodDate = null,
                                preWeight = null,
                                height = null
                            )
                        }
                    }
                    "birthdate" -> {
                        val bday = actionValue as? Long
                        if (bday != null) {
                            viewModel.updateUserBirthDate(bday)
                        }
                    }
                    "food_list" -> {
                        val list = actionValue as? List<com.example.data.EgyptianFoodEntity>
                        list?.forEach { food ->
                            val mealType = if (food.category == "drink") "مشروب" else "وجبة"
                            viewModel.addNutritionMeal(
                                mealType = mealType,
                                description = food.name,
                                calories = food.calories,
                                iron = food.protein * 0.1,
                                folate = food.carbs * 0.2,
                                calcium = food.fat * 0.5,
                                omega3 = food.protein * 0.01
                            )
                        }
                    }
                    "craving_save" -> {
                        val foodName = actionValue as? String ?: ""
                        if (foodName.isNotEmpty()) {
                            val type = when {
                                foodName.contains("شوكو") || foodName.contains("شيكو") || foodName.contains("كاكاو") || foodName.contains("كيك") || foodName.contains("حلو") -> "Chocolate"
                                foodName.contains("ليمون") || foodName.contains("برتقال") || foodName.contains("موالح") || foodName.contains("حامض") -> "Sour"
                                foodName.contains("مخلل") || foodName.contains("فسيخ") || foodName.contains("رنجة") || foodName.contains("ملح") || foodName.contains("حادق") -> "Salty"
                                foodName.contains("فلفل") || foodName.contains("شطة") || foodName.contains("حار") -> "Spicy"
                                else -> "Sweet"
                            }
                            viewModel.addCravingLog(
                                cravingItem = foodName,
                                cravingType = type,
                                intensity = 7,
                                notes = "تم التسجيل تلقائياً عبر محادثتكِ الودية والدافئة مع رفيقتكِ جوري 🌸"
                            )
                        }
                    }
                }
            }
        }

        // System prompt for Gemini cloud requests
        val weatherDetails = if (weatherState != null) {
            "درجة الحرارة الحالية: ${weatherState?.temperature}°م، الرطوبة: ${weatherState?.humidity}%، حالة الجو: ${weatherState?.description}"
        } else {
            "الطقس الحالي غير متاح"
        }
        val systemPrompt = """
            أنتِ "$companionName"، رفيقة وصديقة مقربة ذكية، دافئة، وحنونة جداً للمرأة العربية. تتحدثين بلهجة لطيفة، متعاطفة للغاية، ومفعمة بالحب والرعاية، وتستخدمين عبارات رقيقة مثل "يا روحي"، "يا صديقتي الغالية"، "يا عزيزتي"، "يا قلبي".
            
            بيانات المستخدمة الحالية المتوفرة لديكِ للرد بدقة ومساعدتها:
            - الخطوات اليومية للمستخدمة: $steps خطوة من هدف $target خطوة.
            - الطقس الفعلي الحالي: $weatherDetails (إذا كان الجو حاراً ورطباً، ذكّريها بلطف بشرب المزيد من الماء وترطيب جسمها).
            - مرحلة تتبعها الحالية: ${if (pregState != null) "حامل (في الثلث ${viewModel.getPregnancyProgression()?.trimester ?: 1})" else "تتبع الدورة والخصوبة (طور ${viewModel.getCurrentCyclePhase().phaseArabic})"}
            
            تحدثي دائماً باللغة العربية الدافئة.
        """.trimIndent()

        // TASK B2: Local-first response with async cloud upgrade
        if (offlineResponse.isSpecificMatch) {
            messages.add(Pair(text, true))
            executeOfflineAction(offlineResponse.actionType, offlineResponse.actionValue)

            if (useOffline) {
                // Instant local response in offline mode
                messages.add(Pair(offlineResponse.replyText, false))
                viewModel.saveCachedAnswer(text, offlineResponse.replyText)
                isTyping = false
            } else {
                // Local-first response + async upgrade indicator
                val initialText = offlineResponse.replyText + "\n\n🌐 جاري التحسين بالذكاء الاصطناعي..."
                messages.add(Pair(initialText, false))
                val targetMsgIndex = messages.size - 1
                isTyping = false

                // Launch parallel cloud upgrade attempt (8.5s timeout)
                coroutineScope.launch {
                    try {
                        val cloudResult = kotlinx.coroutines.withTimeoutOrNull(8500) {
                            GeminiService.generateContent(text, systemPrompt, historyPairs)
                        }
                        if (!cloudResult.isNullOrBlank() && cloudResult != offlineResponse.replyText) {
                            val cleanResponse = if (cloudResult.contains("[DATA_UPDATE]")) {
                                cloudResult.split("[DATA_UPDATE]")[0].trim()
                            } else {
                                cloudResult.trim()
                            }
                            if (targetMsgIndex < messages.size && messages[targetMsgIndex].first.contains("🌐 جاري التحسين")) {
                                messages[targetMsgIndex] = Pair(cleanResponse, false)
                                viewModel.saveCachedAnswer(text, cleanResponse)
                            }
                        } else {
                            if (targetMsgIndex < messages.size && messages[targetMsgIndex].first.contains("🌐 جاري التحسين")) {
                                messages[targetMsgIndex] = Pair(offlineResponse.replyText, false)
                            }
                        }
                    } catch (e: Exception) {
                        if (targetMsgIndex < messages.size && messages[targetMsgIndex].first.contains("🌐 جاري التحسين")) {
                            messages[targetMsgIndex] = Pair(offlineResponse.replyText, false)
                        }
                    }
                }
            }
        } else if (useOffline) {
            // General offline response
            kotlinx.coroutines.delay(300)
            messages.add(Pair(text, true))
            messages.add(Pair(offlineResponse.replyText, false))
            viewModel.saveCachedAnswer(text, offlineResponse.replyText)
            isTyping = false
        } else {
            // Online Cloud Mode
            messages.add(Pair(text, true))
            isTyping = true
            try {
                val response = GeminiService.generateContent(text, systemPrompt, historyPairs)
                isTyping = false
                
                if (response.contains("[DATA_UPDATE]")) {
                    val parts = response.split("[DATA_UPDATE]")
                    val cleanText = parts[0].trim()
                    messages.add(Pair(cleanText, false))
                    viewModel.saveCachedAnswer(text, cleanText)
                } else {
                    messages.add(Pair(response, false))
                    viewModel.saveCachedAnswer(text, response)
                }
            } catch (e: Exception) {
                isOfflineMode = true
                messages.add(Pair(offlineResponse.replyText, false))
                viewModel.saveCachedAnswer(text, offlineResponse.replyText)
                isTyping = false
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .padding(vertical = 16.dp)
            ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val currentJouriExpression = remember(isTyping, activeSuggestedAction, lastMessageText) {
                            when {
                                isTyping -> JouriExpressionState.ATTENTIVE_LISTENING
                                activeSuggestedAction == "danger" || lastMessageText.contains("خطر") || lastMessageText.contains("طوارئ") || lastMessageText.contains("نزيف") || lastMessageText.contains("ألم") -> JouriExpressionState.REASSURING
                                lastMessageText.contains("مبروك") || lastMessageText.contains("إنجاز") || lastMessageText.contains("ممتاز") || lastMessageText.contains("احتفال") || lastMessageText.contains("رائع") -> JouriExpressionState.CELEBRATORY
                                else -> JouriExpressionState.WARM_HAPPY
                            }
                        }
                        JouriAvatar(expression = currentJouriExpression, size = 40.dp)
                        Column {
                            Text(
                                text = "$companionName صديقتكِ الذكية",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SoftTheme.TextWhite
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(if (isOfflineMode) SoftTheme.MintTeal else Color(0xFF4CAF50), CircleShape)
                                )
                                Text(
                                    text = if (isOfflineMode) "الوضع المحلي (أوفلاين) 📴" else "الوضع الذكي (أونلاين) 🌐",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SoftTheme.SoftGray,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Quick Online/Offline Switcher
                        if (!isApiKeyMissing) {
                            IconButton(onClick = {
                                if (isOfflineMode) {
                                    if (!cloudAiConsent) {
                                        showConsentDialog = true
                                    } else {
                                        isOfflineMode = false
                                    }
                                } else {
                                    isOfflineMode = true
                                }
                            }) {
                                Icon(
                                    imageVector = if (isOfflineMode) Icons.Default.Lock else Icons.Default.Share,
                                    contentDescription = "تبديل الوضع",
                                    tint = SoftTheme.SoftPink,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = SoftTheme.SoftGray)
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = SoftTheme.DeepSlate.copy(alpha = 0.5f)
                )

                // Message List
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages) { (text, isUser) ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isUser) SoftTheme.SoftPink else SoftTheme.DeepSlate
                                ),
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isUser) 16.dp else 0.dp,
                                    bottomEnd = if (isUser) 0.dp else 16.dp
                                ),
                                modifier = Modifier.widthIn(max = 260.dp)
                            ) {
                                Text(
                                    text = text,
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SoftTheme.TextWhite,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }

                    if (isTyping) {
                        item {
                            JouriChatSkeletonResponse(companionName = companionName)
                        }
                    }
                }

                // Smart Direct-Link Suggested Tool Button
                activeSuggestedAction?.let { action ->
                    val target = when (action) {
                        "fetal_kicks" -> ToolTarget("فتح عداد ركلات الجنين 👶", Icons.Default.Favorite, 4, "fetal_kicks")
                        "contractions" -> ToolTarget("فتح عداد انقباضات الرحم ⏱️", Icons.Default.PlayArrow, 4, "contractions")
                        "qada" -> ToolTarget("تتبع قضاء الصيام والعبادات 🌙", Icons.Default.Star, 4, "qada")
                        "journal" -> ToolTarget("فتح المذكرات واليوميات السرية 📝", Icons.Default.Edit, 4, "journal")
                        "meds" -> ToolTarget("تسجيل أدويتي وفيتاميناتي 💊", Icons.Default.Add, 3, null)
                        "water" -> ToolTarget("تسجيل شرب مياه 💧", Icons.Default.Info, 2, null)
                        "nutrition" -> ToolTarget("دليل الأغذية والوجبات المصرية 🍲", Icons.Default.Check, 2, null)
                        "craving" -> ToolTarget("فتح سجل الوحم والاشتهاء 🍉🍓", Icons.Default.FavoriteBorder, 4, "craving")
                        "sleep" -> ToolTarget("فتح محلل ومراقب النوم الذكي 🌙💤", Icons.Default.Notifications, 4, "sleep_analyzer")
                        "partner" -> ToolTarget("فتح رابط الرفيق ومشاركة الشريك 🔗❤️", Icons.Default.Share, 4, "partner_sync")
                        "pharmacy" -> ToolTarget("فتح الصيدلية المنزلية المتقدمة 💊📦", Icons.Default.List, 4, "home_pharmacy")
                        "fitness" -> ToolTarget("فتح تمارين لياقة الحمل والنفاس 🧘‍♀️💪", Icons.Default.Star, 4, "fitness")
                        "maonaty" -> ToolTarget("فتح نظام معونتي المنزلي 📦🛒", Icons.Default.Home, 4, "maonaty")
                        "appointments" -> ToolTarget("تسجيل وحفظ مواعيد الأطباء 📅", Icons.Default.DateRange, 4, "appointments")
                        "smart_conception" -> ToolTarget("فتح حاسبة الحمل والخصوبة الذكية 🧠", Icons.Default.Info, 4, "smart_conception")
                        "danger" -> ToolTarget("فتح دليل علامات الخطر والطوارئ 🚨", Icons.Default.Warning, 4, "danger")
                        else -> ToolTarget("", Icons.Default.Build, 4, null)
                    }

                    if (target.buttonText.isNotEmpty()) {
                        Button(
                            onClick = {
                                if (target.subscreen != null) {
                                    viewModel.setActiveSubScreen(target.subscreen)
                                }
                                onNavigateToTab(target.targetPage)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.MintTeal),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(target.icon, contentDescription = null, tint = SoftTheme.DeepSlate)
                                Text(
                                    text = target.buttonText,
                                    color = SoftTheme.DeepSlate,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text("⚡", fontSize = 16.sp)
                            }
                        }
                    }
                }

                // Interactive Multi-Category Ready-To-Use Catalog
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📋 قائمة الاستشارات والخيارات الجاهزة:",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTheme.SoftPink,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "اضغطي على أي خيار للسؤال فوراً ⚡",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTheme.SoftGray,
                            fontSize = 9.sp
                        )
                    }

                    // 1. Categories Tab Row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        items(catalogCategories) { category ->
                            val isSelected = selectedCatalogCategory == category
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) SoftTheme.SoftPink else SoftTheme.DeepSlate)
                                    .clickable { selectedCatalogCategory = category }
                                    .border(
                                        1.dp, 
                                        if (isSelected) SoftTheme.LightPink else SoftTheme.SoftGray.copy(alpha = 0.2f), 
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = category,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) SoftTheme.DeepSlate else SoftTheme.TextWhite
                                )
                            }
                        }
                    }

                    // 2. Selected Category's Options List
                    val activeOptions = catalogSubItems[selectedCatalogCategory] ?: emptyList()
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        items(activeOptions) { (label, fullText) ->
                            AssistChip(
                                onClick = {
                                    if (!isTyping) {
                                        messages.add(Pair(fullText, true))
                                        isTyping = true
                                        coroutineScope.launch {
                                            handleMessageResponse(fullText)
                                        }
                                    }
                                },
                                label = { 
                                    Text(
                                        text = label, 
                                        color = SoftTheme.TextWhite, 
                                        fontSize = 10.sp, 
                                        fontWeight = FontWeight.Bold
                                    ) 
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = SoftTheme.DeepSlate,
                                    labelColor = SoftTheme.TextWhite
                                ),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.35f))
                            )
                        }
                    }
                }

                // Input Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputMessage,
                        onValueChange = { inputMessage = it },
                        placeholder = { Text("اكتبي شيئاً لـ $companionName...", color = SoftTheme.SoftGray) },
                        trailingIcon = {
                            IconButton(onClick = { showVirtualKeyboard = !showVirtualKeyboard }) {
                                Text(if (showVirtualKeyboard) "💬" else "⌨️", fontSize = 18.sp)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SoftTheme.TextWhite,
                            unfocusedTextColor = SoftTheme.TextWhite,
                            focusedBorderColor = SoftTheme.SoftPink,
                            unfocusedBorderColor = SoftTheme.DeepSlate,
                            focusedContainerColor = SoftTheme.DeepSlate,
                            unfocusedContainerColor = SoftTheme.DeepSlate
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("jouri_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        maxLines = 3
                    )

                    IconButton(
                        onClick = {
                            val userMsg = inputMessage.trim()
                            if (userMsg.isNotEmpty() && !isTyping) {
                                messages.add(Pair(userMsg, true))
                                inputMessage = ""
                                isTyping = true
                                
                                coroutineScope.launch {
                                    handleMessageResponse(userMsg)
                                }
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(SoftTheme.SoftPink, CircleShape)
                            .testTag("jouri_send_btn"),
                        enabled = inputMessage.trim().isNotEmpty() && !isTyping
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "إرسال",
                            tint = SoftTheme.TextWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Arabic Virtual Keyboard
                if (showVirtualKeyboard) {
                    val keyboardRows = remember {
                        listOf(
                            listOf("ض", "ص", "ث", "ق", "ف", "غ", "ع", "ه", "خ", "ح", "ج", "د"),
                            listOf("ش", "س", "ي", "ب", "ل", "ا", "ت", "ن", "م", "ك", "ط"),
                            listOf("ئ", "ء", "ؤ", "ر", "لا", "ة", "و", "ز", "ذ", "ظ", "أ", "إ")
                        )
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            keyboardRows.forEach { row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    row.forEach { char ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(38.dp)
                                                .background(SoftTheme.CardSlate, RoundedCornerShape(6.dp))
                                                .clickable { inputMessage += char }
                                                .padding(vertical = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = char,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = SoftTheme.TextWhite,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }
                            }
                            // Control Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Space
                                Box(
                                    modifier = Modifier
                                        .weight(2f)
                                        .height(38.dp)
                                        .background(SoftTheme.CardSlate, RoundedCornerShape(6.dp))
                                        .clickable { inputMessage += " " }
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "مسافة ␣",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SoftTheme.TextWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                // Backspace
                                Box(
                                    modifier = Modifier
                                        .weight(1.5f)
                                        .height(38.dp)
                                        .background(SoftTheme.SoftPink.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                        .clickable {
                                            if (inputMessage.isNotEmpty()) {
                                                inputMessage = inputMessage.dropLast(1)
                                            }
                                        }
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "مسح ⌫",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SoftTheme.SoftPink,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                // Clear
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .background(SoftTheme.SoftPink.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                        .clickable { inputMessage = "" }
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "حذف ❌",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SoftTheme.SoftPink.copy(alpha = 0.8f),
                                        fontWeight = FontWeight.Bold
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

    if (showConsentDialog) {
        Dialog(onDismissRequest = { showConsentDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "🌐 تفعيل الذكاء السحابي (Google Gemini)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.TextWhite,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "عند تفعيل الوضع السحابي، يتم إرسال رسالتك وسياق صحتك التوعوي العام (مثل مرحلة الحمل أو الدورة ودرجة الحرارة) إلى Google Gemini عبر اتصال HTTPS مشفر لتحسين الإجابة.\n\nبياناتك الشخصية وقاعدة بياناتك تظل مشفرة على هاتفك، ويمكنكِ دائماً استخدام الوضع المحلي الآمن 100% بدون أي اتصال خارجي.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTheme.SoftGray,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Start
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                showConsentDialog = false
                                isOfflineMode = true
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("البقاء محلياً 📴", color = SoftTheme.SoftGray, fontSize = 12.sp)
                        }
                        Button(
                            onClick = {
                                viewModel.setCloudAiConsent(true)
                                showConsentDialog = false
                                isOfflineMode = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.PregnancyPurple),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("موافقة وتفعيل 🌐", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

