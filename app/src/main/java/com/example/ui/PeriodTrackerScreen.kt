package com.example.ui

import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import com.example.util.formatArabicDays
import com.example.viewmodel.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.launch

// --- Period Tracker & Smart Calendar Screen ---
@Composable
fun PeriodTrackerScreen(
    viewModel: WomanCompanionViewModel,
    onNavigateToTab: (Int) -> Unit = {}
) {
    val periodLogs by viewModel.periodLogsState.collectAsStateWithLifecycle()
    val isPregnant by viewModel.pregnancyState.collectAsStateWithLifecycle()
    val allPregnancies by viewModel.allPregnanciesState.collectAsStateWithLifecycle()
    val nifasDurationDays by viewModel.nifasDurationDaysState.collectAsStateWithLifecycle()
    val weatherState by viewModel.weatherState.collectAsStateWithLifecycle()

    var showAddPeriodDialog by remember { mutableStateOf(false) }
    var selectedIntensity by remember { mutableStateOf("medium") }
    var painLevel by remember { mutableStateOf(5) }
    var notesInput by remember { mutableStateOf("") }
    val selectedSymptoms = remember { mutableStateListOf<String>() }

    // State for custom previous/past cycle dates
    var useCustomStartDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var useCustomEndDate by remember { mutableStateOf(System.currentTimeMillis() + 5L * 24 * 60 * 60 * 1000) }
    var useSpecificDateRange by remember { mutableStateOf(false) }

    // +++ أضيف بناءً على طلبك لقناة توجيه الحمل +++
    var showPregnancyLmpPromptDialog by remember { mutableStateOf(false) }
    var pendingPeriodStartDate by remember { mutableStateOf<Long?>(null) }

    // +++ أضيف بناءً على طلبك لتتبع الحمل والولادة بشكل ديناميكي +++
    var showBabyInfoDialog by remember { mutableStateOf(false) }
    var babyGenderInput by remember(isPregnant) { mutableStateOf(isPregnant?.babyGender ?: "") }
    var babyNameInput by remember(isPregnant) { mutableStateOf(isPregnant?.babyName ?: "") }
    var showDeliveryDialog by remember { mutableStateOf(false) }
    var showEndPregnancyConfirmDialog by remember { mutableStateOf(false) }
    var showLossSupportDialog by remember { mutableStateOf(false) }

    val symptomsList = listOf("مغص", "إرهاق", "صداع", "تقلب مزاجي", "ألم ظهر")

    // Collapsible/interactive states for a clean, professional, and compact main dashboard
    var isWeatherExpanded by remember { mutableStateOf(false) }
    var isHealthAnalysisExpanded by remember { mutableStateOf(false) }
    var isCalendarExpanded by remember { mutableStateOf(false) }
    var isHistoryExpanded by remember { mutableStateOf(false) }
    var isBabyDetailsExpanded by remember { mutableStateOf(false) }

    val stats = viewModel.getCycleStats()
    val currentPhase = viewModel.getCurrentCyclePhase()

    // Smart Calendar State
    val currentMonthCalendar = remember { Calendar.getInstance() }
    var monthUpdateTrigger by remember { mutableStateOf(0) }

    // Advanced smart predictions: predicted next 4 cycles & ovulation windows
    val lastLog = periodLogs.maxByOrNull { it.startDate }
    val avgCycle = stats.averageCycleLength
    val avgDuration = stats.averagePeriodDuration

    val predictedPeriods = remember(periodLogs, avgCycle, avgDuration) {
        val list = mutableListOf<Pair<Long, Long>>()
        if (lastLog != null && avgCycle > 0) {
            var currentStart = lastLog.startDate
            repeat(4) {
                val nextStart = currentStart + avgCycle.toLong() * 24 * 60 * 60 * 1000
                val nextEnd = nextStart + avgDuration.toLong() * 24 * 60 * 60 * 1000
                list.add(Pair(nextStart, nextEnd))
                currentStart = nextStart
            }
        }
        list
    }

    val predictedOvulations = remember(predictedPeriods, avgCycle) {
        val list = mutableListOf<Pair<Long, Long>>()
        predictedPeriods.forEach { (pStart, _) ->
            val oStart = pStart - 16L * 24 * 60 * 60 * 1000
            val oEnd = pStart - 12L * 24 * 60 * 60 * 1000
            list.add(Pair(oStart, oEnd))
        }
        list
    }

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
                    text = if (isPregnant?.isPregnant == true) "تتبع الحمل والولادة 🤰💖" else "الدورة والخصوبة 🩸",
                    style = MaterialTheme.typography.titleLarge,
                    color = SoftTheme.SoftPink,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isPregnant?.isPregnant == true) "رحلتكِ الرائعة للعناية بنفسكِ وبجنينكِ خطوة بخطوة مع حساب دقيق لأسابيع الحمل" else "تتبع ذكي وتنبؤ بمراحل الخصوبة بخصوصية كاملة مع إمكانية تسجيل دوراتك السابقة بالكامل",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SoftTheme.SoftGray
                )
            }

            val irregularityNotices = viewModel.detectCycleIrregularityPatterns()
            if (irregularityNotices.isNotEmpty()) {
                items(irregularityNotices) { notice ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("cycle_irregularity_notice_card_${notice.id}"),
                        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("🌸", fontSize = 24.sp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = notice.title,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftTheme.SoftPink,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = notice.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SoftTheme.TextWhite
                                )
                            }
                        }
                    }
                }
            }



            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                        .clickable { isWeatherExpanded = !isWeatherExpanded },
                    colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("🌦️", fontSize = 20.sp)
                                Column {
                                    Text(
                                        text = "الطقس ونصائح الترطيب اليومي",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = SoftTheme.TextWhite
                                    )
                                    Text(
                                        text = "توصيات مخصصة لدرجات الحرارة اليوم",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SoftTheme.SoftGray
                                    )
                                }
                            }
                            Text(
                                text = if (isWeatherExpanded) "إخفاء 🔼" else "عرض التفاصيل 🔽",
                                color = SoftTheme.SoftPink,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (isWeatherExpanded) {
                            Spacer(modifier = Modifier.height(14.dp))
                            JouriWeatherHeader(weatherInfo = weatherState)
                        }
                    }
                }
            }

            if (isPregnant?.isPregnant == true) {
                val pregState = isPregnant
                val progression = viewModel.getPregnancyProgression()
                
                progression?.let { prog ->
                    val trimesterColor = when {
                        prog.weeks >= 41 -> Color(0xFFFFB300) // Month 10: Gold Amber
                        prog.trimester == 1 -> Color(0xFF9575CD) // Trimester 1: Lavender
                        prog.trimester == 2 -> SoftTheme.MintTeal // Trimester 2: Mint Teal
                        else -> SoftTheme.SoftPink // Trimester 3: Soft Pink
                    }

                    val monthProg = calculateMonthProgress(prog.weeks, prog.daysIntoWeek)
                    val activeMonth = monthProg.monthNumber
                    val activeMonthProgress = monthProg.progressFraction

                    val babyGender = pregState?.babyGender
                    val babyName = pregState?.babyName
                    val isGenderKnown = !babyGender.isNullOrEmpty()

                    // Baby Info Card
                    if (prog.weeks >= 14 || isGenderKnown) {
                        item {
                            if (isGenderKnown) {
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
                                            Text("تسجيل جنس واسم الجنين 👶🍼", color = SoftTheme.SoftPink, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Primary Weeks Circle Card
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
                                    text = if (prog.weeks >= 41) "أنتِ الآن في الشهر العاشر (تخطي موعد الولادة) ⚠️" else "أنتِ الآن في الأسبوع",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SoftTheme.SoftGray,
                                    textAlign = TextAlign.Center
                                )

                                Box(
                                    modifier = Modifier
                                        .size(160.dp)
                                        .drawBehind {
                                            drawCircle(
                                                color = SoftTheme.DeepSlate,
                                                radius = size.minDimension / 2
                                            )
                                            drawArc(
                                                color = trimesterColor,
                                                startAngle = -90f,
                                                sweepAngle = if (prog.weeks >= 41) 360f else ((prog.weeks.toFloat() / 40f) * 360f).coerceIn(0f, 360f),
                                                useCenter = false,
                                                style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                                            )
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "${prog.weeks}",
                                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 58.sp),
                                            fontWeight = FontWeight.Bold,
                                            color = SoftTheme.TextWhite
                                        )
                                        Text(
                                            text = "الأيام: ${prog.daysIntoWeek}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = trimesterColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "مخطط شهور الحمل التسعة 📅",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SoftTheme.SoftGray
                                        )
                                        Text(
                                            text = "أنتِ في " + when(activeMonth) {
                                                1 -> "الشهر الأول"
                                                2 -> "الشهر الثاني"
                                                3 -> "الشهر الثالث"
                                                4 -> "الشهر الرابع"
                                                5 -> "الشهر الخامس"
                                                6 -> "الشهر السادس"
                                                7 -> "الشهر السابع"
                                                8 -> "الشهر الثامن"
                                                9 -> "الشهر التاسع"
                                                else -> "الشهر العاشر ⚠️"
                                            } + " (${(activeMonthProgress * 100).toInt()}% من الشهر)",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = trimesterColor
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val totalSegments = if (prog.weeks >= 41) 10 else 9
                                        for (i in 0 until totalSegments) {
                                            val segProgress = when {
                                                i < activeMonth - 1 -> 1f
                                                i == activeMonth - 1 -> activeMonthProgress
                                                else -> 0f
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(10.dp)
                                                    .clip(RoundedCornerShape(5.dp))
                                                    .background(SoftTheme.DeepSlate)
                                            ) {
                                                if (segProgress > 0f) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxHeight()
                                                            .fillMaxWidth(segProgress)
                                                            .background(
                                                                Brush.horizontalGradient(
                                                                    colors = listOf(trimesterColor.copy(alpha = 0.7f), trimesterColor)
                                                                )
                                                            )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("الثلث", color = SoftTheme.SoftGray, style = MaterialTheme.typography.bodySmall)
                                        Text(
                                            text = if (prog.weeks >= 41) "أمان ممتد" else "${prog.trimester}",
                                            color = SoftTheme.TextWhite,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }
                                    VerticalDivider(modifier = Modifier.height(24.dp).width(1.dp), color = SoftTheme.SoftGray)
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("الأيام المتبقية", color = SoftTheme.SoftGray, style = MaterialTheme.typography.bodySmall)
                                        Text("${prog.remainingDays}", color = SoftTheme.TextWhite, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    }
                                    VerticalDivider(modifier = Modifier.height(24.dp).width(1.dp), color = SoftTheme.SoftGray)
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("موعد الولادة", color = SoftTheme.SoftGray, style = MaterialTheme.typography.bodySmall)
                                        val formatted = SimpleDateFormat("dd MMM", Locale.forLanguageTag("ar")).format(Date(prog.dueDate))
                                        Text(formatted, color = SoftTheme.TextWhite, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    }
                                }
                            }
                        }
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

                    // Baby Size Visual Comparison Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(SoftTheme.DeepSlate),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = prog.comparisonIcon,
                                        fontSize = 36.sp
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "طفلكِ الآن بحجم:",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SoftTheme.SoftGray
                                    )
                                    Text(
                                        text = prog.comparisonName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SoftTheme.SoftPink
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = prog.developmentTip,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SoftTheme.TextWhite,
                                        lineHeight = 16.sp
                                    )
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
                            onClick = { showEndPregnancyConfirmDialog = true },
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
            } else {
                // Interactive Late Period Alert Card when period is late
                val logsSorted = periodLogs.sortedByDescending { it.startDate }
                val lastLog = logsSorted.firstOrNull()
                val daysSinceStart = if (lastLog != null) {
                    ((System.currentTimeMillis() - lastLog.startDate) / (24L * 60 * 60 * 1000)).toInt().coerceAtLeast(0)
                } else 0
                val delayDays = if (lastLog != null) daysSinceStart - stats.averageCycleLength else 0
                val isLateDetected = lastLog != null && delayDays >= 1

                if (isLateDetected) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize(),
                            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(1.5.dp, SoftTheme.SoftPink)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text("⚠️", fontSize = 24.sp)
                                    Column {
                                        Text(
                                            text = "الدورة متأخرة عن موعدها المتوقع بـ ${formatArabicDays(delayDays)}! 🌸",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = SoftTheme.SoftPink
                                        )
                                        Text(
                                            text = "ملاحظة ذكية من جوري",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SoftTheme.SoftGray
                                        )
                                    }
                                }

                                Text(
                                    text = "يا روحي، دورتكِ متأخرة عن المتوسط المعتاد (${formatArabicDays(daysSinceStart)} منذ بداية آخر طمث). هل تعتقدين أن هناك احتمال وجود حمل مبارك 🤰، أم أنها مجرد تأخر في تسجيل دورتكِ الجديدة؟",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SoftTheme.TextWhite,
                                    lineHeight = 20.sp
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Button 1: Yes, pregnant!
                                    Button(
                                        onClick = {
                                            viewModel.setPregnancy(
                                                lastPeriodDate = lastLog?.startDate,
                                                preWeight = null,
                                                height = null
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1.2f)
                                    ) {
                                        Text("🤰 نعم، أنا حامل!", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }

                                    // Button 2: No, log new period
                                    Button(
                                        onClick = {
                                            useCustomStartDate = System.currentTimeMillis()
                                            useCustomEndDate = System.currentTimeMillis() + 5L * 24 * 60 * 60 * 1000
                                            useSpecificDateRange = true
                                            showAddPeriodDialog = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.DeepSlate),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("🩸 تسجيل دورة جديدة", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SoftTheme.MintTeal)
                                    }
                                }
                            }
                        }
                    }
                }

                // Cycle Phase Progress Visual Segment Bar (ALWAYS VISIBLE AT TOP OF CYCLE TRACKER!)
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("الطور الحالي للجسد", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                                    Text(currentPhase.phaseArabic, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (currentPhase.isLate) SoftTheme.RedDanger.copy(alpha = 0.2f) else SoftTheme.MintTeal.copy(alpha = 0.2f))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "اليوم ${currentPhase.daysInPhase}",
                                        color = if (currentPhase.isLate) SoftTheme.RedDanger else SoftTheme.MintTeal,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Cycle Segmented progress bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(16.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SoftTheme.DeepSlate)
                            ) {
                                Row(modifier = Modifier.fillMaxSize()) {
                                    // Menstruation Segment (Red/Pink)
                                    Box(
                                        modifier = Modifier
                                            .weight(stats.averagePeriodDuration.toFloat())
                                            .fillMaxHeight()
                                            .background(SoftTheme.DeepPink)
                                    )
                                    // Follicular Segment (Teal)
                                    Box(
                                        modifier = Modifier
                                            .weight((stats.averageCycleLength - 16 - stats.averagePeriodDuration).toFloat().coerceAtLeast(1f))
                                            .fillMaxHeight()
                                            .background(SoftTheme.SoftTeal)
                                    )
                                    // Ovulation Segment (Teal Highlight)
                                    Box(
                                        modifier = Modifier
                                            .weight(5f)
                                            .fillMaxHeight()
                                            .background(SoftTheme.MintTeal)
                                    )
                                    // Luteal Segment (Slate Light)
                                    Box(
                                        modifier = Modifier
                                            .weight(11f)
                                            .fillMaxHeight()
                                            .background(SoftTheme.CardSlate)
                                    )
                                }

                                // Current Day indicator slider
                                val ratio = currentPhase.progressFraction.coerceIn(0f, 1f)
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(ratio)
                                        .background(Color.White.copy(alpha = 0.25f))
                                )
                            }

                            Text(
                                text = currentPhase.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftTheme.SoftGray,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // Add Log Action Button (ALWAYS PROMINENT!)
                item {
                    Button(
                        onClick = {
                            if (isPregnant != null && isPregnant?.isPregnant == true) {
                                useSpecificDateRange = true
                                useCustomStartDate = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000 // default to 30 days ago
                                useCustomEndDate = useCustomStartDate + 5L * 24 * 60 * 60 * 1000
                            } else {
                                useSpecificDateRange = false
                            }
                            showAddPeriodDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_period_log_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (isPregnant != null && isPregnant?.isPregnant == true) {
                                "تسجيل دورة شهرية سابقة 📅"
                            } else {
                                "تسجيل تاريخ الدورة الشهرية (جديدة أو سابقة)"
                            }
                        )
                    }
                }

                // Smart Health Analysis Card (لوحة التحليل الصحي والذكاء التوجيهي)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().animateContentSize(),
                        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isHealthAnalysisExpanded = !isHealthAnalysisExpanded },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = SoftTheme.SoftPink
                                    )
                                    Text(
                                        text = "التحليل الصحي الذكي والتوصيات 🧠",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SoftTheme.TextWhite
                                    )
                                }
                                Text(
                                    text = if (isHealthAnalysisExpanded) "إخفاء 🔼" else "تحليل كامل 🔽",
                                    color = SoftTheme.SoftPink,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (isHealthAnalysisExpanded) {
                                Spacer(modifier = Modifier.height(8.dp))
                                if (periodLogs.isEmpty()) {
                                    Text(
                                        text = "قومي بتسجيل دورتكِ الشهرية الأولى (أو دوراتك السابقة) لنتمكن من تقديم نصائح تغذية وراحة مخصصة ذكياً.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = SoftTheme.SoftGray
                                    )
                                } else {
                                    val latestLog = periodLogs.maxByOrNull { it.startDate }
                                    val pain = latestLog?.painLevel ?: 5
                                    val intensity = latestLog?.flowIntensity ?: "medium"
                                    val symptoms = latestLog?.symptoms?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()

                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text(
                                            text = "بناءً على سجلات دورتكِ وأعراضك الأخيرة، إليكِ تقريرنا الطبي التوجيهي الذكي:",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SoftTheme.SoftPink
                                        )

                                        HorizontalDivider(color = SoftTheme.DeepSlate)

                                        // Pain advisory
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text("🌱", fontSize = 18.sp)
                                            Column {
                                                Text("مستوى الألم والتقلصات:", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite, style = MaterialTheme.typography.bodyMedium)
                                                Text(
                                                    text = when {
                                                        pain >= 8 -> "ألم شديد ($pain/10). نوصي بالراحة التامة، استخدام كمادات دافئة على البطن، وتناول المغنيسيوم. إذا استمر الألم الشديد يرجى استشارة الطبيبة."
                                                        pain >= 5 -> "ألم متوسط ($pain/10). كوب من القرفة أو اليانسون الدافئ قد يساعد في تخفيف الانقباضات بشكل رائع."
                                                        else -> "ألم خفيف ($pain/10). مستوى ممتاز ومؤشر على توازن هرموني رائع."
                                                    },
                                                    color = SoftTheme.SoftGray,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        }

                                        // Flow advisory
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text("🩸", fontSize = 18.sp)
                                            Column {
                                                Text("غزارة الطمث ونقص الحديد:", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite, style = MaterialTheme.typography.bodyMedium)
                                                Text(
                                                    text = when (intensity) {
                                                        "heavy" -> "الطمث غزير. من الضروري جداً زيادة تناول الأطعمة الغنية بالحديد (مثل السبانخ واللحم الأحمر) أو فيتامين سي لتعويض الفقد وتجنب فقر الدم."
                                                        "light" -> "الطمث خفيف. طبيعي جداً، استمري في شرب الماء والترطيب."
                                                        else -> "الطمث متوسط ومثالي. كمية تدفق صحية تدل على بطانة رحم سليمة."
                                                    },
                                                    color = SoftTheme.SoftGray,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        }

                                        // Symptoms advisory
                                        if (symptoms.isNotEmpty()) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Text("🧠", fontSize = 18.sp)
                                                Column {
                                                    Text("التعامل مع الأعراض المرافقة:", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite, style = MaterialTheme.typography.bodyMedium)
                                                    val advice = symptoms.map { sym ->
                                                        when (sym) {
                                                            "مغص" -> "للـ مغص: تدليك أسفل الظهر بزيت اللافندر الدافئ."
                                                            "إرهاق" -> "للـ إرهاق: النوم لـ 8 ساعات وتجنب السهر والإجهاد البدني."
                                                            "صداع" -> "للـ صداع: الابتعاد عن الشاشات والترطيب المستمر بشرب الماء."
                                                            "تقلب مزاجي" -> "للـ تقلب المزاجي: ممارسة تمارين تنفس واسترخاء خفيفة لزيادة هرمونات السعادة."
                                                            "ألم ظهر" -> "للـ ألم الظهر: الحفاظ على وضعية جلوس مستقيمة وتجنب حمل الأثقال."
                                                            else -> "الراحة والترطيب الدائم."
                                                        }
                                                    }.joinToString("\n")
                                                    Text(
                                                        text = advice,
                                                        color = SoftTheme.SoftGray,
                                                        style = MaterialTheme.typography.bodySmall
                                                    )
                                                }
                                            }
                                        }

                                        // Smart pregnancy prediction advice
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text("✨", fontSize = 18.sp)
                                            Column {
                                                Text("التنبؤ الذكي بالخصوبة القادمة:", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite, style = MaterialTheme.typography.bodyMedium)
                                                Text(
                                                    text = "بناءً على طول دورتك المعتاد (${formatArabicDays(avgCycle)})، فإن فرصة الحمل العالية وتاريخ الإباضة القادم سيكون تقريباً في اليوم 14 من بداية دورتك القادمة. يمكنكِ التخطيط لذلكِ بسهولة بالنظر إلى النقط الخضراء في التقويم أدناه.",
                                                    color = SoftTheme.SoftGray,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Mini summary preview when collapsed
                                Text(
                                    text = if (periodLogs.isEmpty()) {
                                        "قومي بتسجيل دورتكِ الشهرية الأولى لبدء التحليل الصحي التلقائي."
                                    } else {
                                        val latestPain = periodLogs.maxByOrNull { it.startDate }?.painLevel ?: 5
                                        if (latestPain >= 7) {
                                            "مستويات الألم الأخيرة مرتفعة نسبياً (${latestPain}/10). انقري لعرض التوصيات الصحية والغذائية المخصصة لراحة جسدك."
                                        } else {
                                            "تحليل: دورتكِ منتظمة بمتوسط ${formatArabicDays(avgCycle)} وصحتك تبدو متوازنة. انقري لعرض التفاصيل الكاملة."
                                        }
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SoftTheme.SoftGray
                                )
                            }
                        }
                    }
                }

                item {
                    D3NativeDashboard(periodLogs = periodLogs, stats = stats)
                }
            }

                // Smart Interactive Calendar Component
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize(),
                        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isCalendarExpanded = !isCalendarExpanded },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("📅", fontSize = 24.sp)
                                    Column {
                                        Text(
                                            text = "تقويم الدورة والخصوبة التفاعلي",
                                            fontWeight = FontWeight.Bold,
                                            color = SoftTheme.TextWhite,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            text = "توقعات الإباضة والخصوبة والحيض",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SoftTheme.SoftGray
                                        )
                                    }
                                }
                                Text(
                                    text = if (isCalendarExpanded) "إخفاء 🔼" else "عرض التقويم 🔽",
                                    color = SoftTheme.SoftPink,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (isCalendarExpanded) {
                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = SoftTheme.DeepSlate.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(8.dp))

                                // Month selector row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = {
                                        currentMonthCalendar.add(Calendar.MONTH, -1)
                                        monthUpdateTrigger++
                                    }) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "السابق", tint = SoftTheme.SoftPink)
                                    }

                                val monthNames = listOf("يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو", "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر")
                                Text(
                                    text = "${monthNames[currentMonthCalendar.get(Calendar.MONTH)]} ${currentMonthCalendar.get(Calendar.YEAR)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftTheme.TextWhite
                                )

                                IconButton(onClick = {
                                    currentMonthCalendar.add(Calendar.MONTH, 1)
                                    monthUpdateTrigger++
                                }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "التالي", tint = SoftTheme.SoftPink)
                                }
                            }

                            // Days of week row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val weekdays = listOf("أحد", "اثنين", "ثلاثاء", "أربعاء", "خميس", "جمعة", "سبت")
                                weekdays.forEach { day ->
                                    Text(
                                        text = day,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SoftTheme.SoftGray,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Days grid
                            val dummyDays = remember(monthUpdateTrigger) {
                                val list = mutableListOf<Long?>()
                                val tempCal = currentMonthCalendar.clone() as Calendar
                                tempCal.set(Calendar.DAY_OF_MONTH, 1)
                                val firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK) // 1 = Sunday

                                repeat(firstDayOfWeek - 1) {
                                    list.add(null)
                                }

                                val maxDays = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)
                                repeat(maxDays) { idx ->
                                    tempCal.set(Calendar.DAY_OF_MONTH, idx + 1)
                                    list.add(tempCal.timeInMillis)
                                }
                                list
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                val chunked = dummyDays.chunked(7)
                                chunked.forEach { rowDays ->
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        rowDays.forEach { dayTime ->
                                            if (dayTime == null) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            } else {
                                                val dayCal = Calendar.getInstance().apply { timeInMillis = dayTime }
                                                val dayNum = dayCal.get(Calendar.DAY_OF_MONTH)

                                                // Determine highlighting (actual period logs)
                                                val isPeriod = periodLogs.any { log ->
                                                    val end = log.endDate ?: (log.startDate + 5L * 24 * 60 * 60 * 1000)
                                                    dayTime >= log.startDate && dayTime <= end
                                                }

                                                // Smart predictions indicators
                                                val isPredictedPeriod = (isPregnant == null || isPregnant?.isPregnant != true) && !isPeriod && predictedPeriods.any { (start, end) ->
                                                    dayTime >= start && dayTime <= end
                                                }
                                                val isPregnancyDay = allPregnancies.any { preg ->
                                                    val start = preg.lastPeriodDate
                                                    val end = if (preg.isDelivered && preg.birthDate != null) {
                                                        preg.birthDate
                                                    } else {
                                                        preg.dueDate ?: (start?.let { it + 280L * 24 * 60 * 60 * 1000 })
                                                    }
                                                    if (start != null && end != null) {
                                                        val startCal = Calendar.getInstance().apply { timeInMillis = start; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
                                                        val endCal = Calendar.getInstance().apply { timeInMillis = end; set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999) }
                                                        dayTime >= startCal.timeInMillis && dayTime <= endCal.timeInMillis
                                                    } else false
                                                }

                                                val isNifasDay = !isPregnancyDay && allPregnancies.any { preg ->
                                                    if (preg.isDelivered && preg.birthDate != null) {
                                                        val nifasStartCal = Calendar.getInstance().apply { timeInMillis = preg.birthDate; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
                                                        val nifasEndCal = Calendar.getInstance().apply { timeInMillis = preg.birthDate + (nifasDurationDays * 24L * 60 * 60 * 1000); set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999) }
                                                        dayTime >= nifasStartCal.timeInMillis && dayTime <= nifasEndCal.timeInMillis
                                                    } else false
                                                }

                                                val isPredictedDevice = false
                                                val isPredictedOvulation = (isPregnant == null || isPregnant?.isPregnant != true) && !isPeriod && !isPredictedPeriod && predictedOvulations.any { (start, end) ->
                                                    dayTime >= start && dayTime <= end
                                                }

                                                val todayCal = Calendar.getInstance()
                                                val isCurrent = todayCal.get(Calendar.YEAR) == dayCal.get(Calendar.YEAR) &&
                                                                todayCal.get(Calendar.DAY_OF_YEAR) == dayCal.get(Calendar.DAY_OF_YEAR)

                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .aspectRatio(1f)
                                                        .clip(CircleShape)
                                                        .background(
                                                            when {
                                                                isPregnancyDay -> SoftTheme.PregnancyPurple
                                                                isNifasDay -> SoftTheme.NifasRose
                                                                isPeriod -> SoftTheme.DeepPink
                                                                isPredictedPeriod -> SoftTheme.DeepPink.copy(alpha = 0.25f)
                                                                isPredictedOvulation -> SoftTheme.MintTeal.copy(alpha = 0.25f)
                                                                isCurrent -> SoftTheme.MintTeal
                                                                else -> Color.Transparent
                                                            }
                                                        )
                                                        .border(
                                                            width = if (isCurrent && !isPeriod && !isPregnancyDay && !isNifasDay) 2.dp else if (isPredictedPeriod || isPredictedOvulation) 1.dp else 0.dp,
                                                            color = if (isCurrent && !isPeriod && !isPregnancyDay && !isNifasDay) SoftTheme.MintTeal else if (isPredictedPeriod) SoftTheme.SoftPink else if (isPredictedOvulation) SoftTheme.MintTeal else Color.Transparent,
                                                            shape = CircleShape
                                                        )
                                                        .clickable {
                                                            // Day selected, smart autofill for previous cycle logs
                                                            useCustomStartDate = dayTime
                                                            useCustomEndDate = dayTime + 5L * 24 * 60 * 60 * 1000
                                                            useSpecificDateRange = true
                                                            showAddPeriodDialog = true
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "$dayNum",
                                                        color = when {
                                                            isPregnancyDay || isNifasDay -> Color.White
                                                            isPeriod -> SoftTheme.DeepSlate
                                                            isPredictedPeriod -> SoftTheme.SoftPink
                                                            isPredictedOvulation -> SoftTheme.MintTeal
                                                            else -> SoftTheme.TextWhite
                                                        },
                                                        fontWeight = if (isCurrent || isPeriod || isPregnancyDay || isNifasDay || isPredictedPeriod || isPredictedOvulation) FontWeight.Bold else FontWeight.Normal,
                                                        fontSize = 14.sp
                                                    )
                                                }
                                            }
                                        }
                                        // pad row if necessary
                                        if (rowDays.size < 7) {
                                            repeat(7 - rowDays.size) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Color Legend for the Smart Calendar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SoftTheme.DeepPink))
                                    Text("طمث", color = SoftTheme.SoftGray, fontSize = 9.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SoftTheme.PregnancyPurple))
                                    Text("حمل 🤰", color = SoftTheme.SoftGray, fontSize = 9.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SoftTheme.NifasRose))
                                    Text("نفاس 👶", color = SoftTheme.SoftGray, fontSize = 9.sp)
                                }
                                if (isPregnant == null || isPregnant?.isPregnant != true) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SoftTheme.DeepPink.copy(alpha = 0.25f)))
                                        Text("دورة متوقعة", color = SoftTheme.SoftGray, fontSize = 9.sp)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SoftTheme.MintTeal.copy(alpha = 0.25f)))
                                        Text("إباضة متوقعة", color = SoftTheme.SoftGray, fontSize = 9.sp)
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SoftTheme.MintTeal))
                                    Text("اليوم", color = SoftTheme.SoftGray, fontSize = 9.sp)
                                }
                            }
                        }
                    }
                }
            }

                // History List Header
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isHistoryExpanded = !isHistoryExpanded },
                        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.15f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("📖", fontSize = 24.sp)
                                Column {
                                    Text(
                                        text = "السجل التاريخي للدورات",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SoftTheme.TextWhite
                                    )
                                    Text(
                                        text = "عرض وتعديل الدورات والتقلصات السابقة",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SoftTheme.SoftGray
                                    )
                                }
                            }
                            Text(
                                text = if (isHistoryExpanded) "إخفاء 🔼" else "عرض السجلات 🔽",
                                color = SoftTheme.SoftPink,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (isHistoryExpanded) {
                    if (periodLogs.isEmpty()) {
                        item {
                            Text(
                                text = "لا توجد دورات مسجلة بعد. قومي بإضافة دورتك لبدء الحساب التلقائي والتحليل.",
                                color = SoftTheme.SoftGray,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                            )
                        }
                    } else {
                        items(periodLogs) { log ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate.copy(alpha = 0.7f)),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(0.5.dp, SoftTheme.SoftPink.copy(alpha = 0.1f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "البداية: ${formatGregorianDate(log.startDate)}",
                                                fontWeight = FontWeight.Bold,
                                                color = SoftTheme.TextWhite
                                            )
                                            log.endDate?.let {
                                                Text(
                                                    text = "النهاية: ${formatGregorianDate(it)}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = SoftTheme.SoftGray
                                                )
                                            }
                                        }

                                        IconButton(onClick = { viewModel.deletePeriod(log) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = SoftTheme.RedDanger)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(SoftTheme.DeepSlate)
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "الشدة: ${if (log.flowIntensity == "heavy") "غزيرة" else if (log.flowIntensity == "medium") "متوسطة" else "خفيفة"}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = SoftTheme.SoftPink
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(SoftTheme.DeepSlate)
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "الألم: ${log.painLevel}/10",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = SoftTheme.MintTeal
                                            )
                                        }
                                        if (!log.notes.isNullOrBlank()) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(SoftTheme.DeepSlate)
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = "ملاحظات 📝",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = SoftTheme.LightPink
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

    // Add Period Dialog (Upgraded with past cycle & custom date range support)
    if (showAddPeriodDialog) {
        Dialog(onDismissRequest = { showAddPeriodDialog = false }) {
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
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "تسجيل الدورة الشهرية 🩸",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.SoftPink
                    )

                    val context = LocalContext.current
                    var selectStartToday by remember { mutableStateOf(true) }

                    // Date Type Selectors (Now supporting highly custom past/previous ranges)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                useSpecificDateRange = false
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!useSpecificDateRange) SoftTheme.SoftPink else SoftTheme.DeepSlate,
                                contentColor = if (!useSpecificDateRange) SoftTheme.DeepSlate else SoftTheme.TextWhite
                            )
                        ) {
                            Text("اليوم/أمس", fontSize = 11.sp)
                        }
                        Button(
                            onClick = {
                                useSpecificDateRange = true
                            },
                            modifier = Modifier.weight(1.5f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (useSpecificDateRange) SoftTheme.SoftPink else SoftTheme.DeepSlate,
                                contentColor = if (useSpecificDateRange) SoftTheme.DeepSlate else SoftTheme.TextWhite
                            )
                        ) {
                            Text("تاريخ مخصص / سابق 📅", fontSize = 11.sp)
                        }
                    }

                    if (useSpecificDateRange) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("تاريخ البدء:", color = SoftTheme.TextWhite, style = MaterialTheme.typography.bodySmall)
                            Button(
                                onClick = {
                                    val calendar = Calendar.getInstance().apply { timeInMillis = useCustomStartDate }
                                    val d1 = android.app.DatePickerDialog(
                                        context,
                                        { _, y, m, d ->
                                            val cal = Calendar.getInstance().apply {
                                                set(Calendar.YEAR, y)
                                                set(Calendar.MONTH, m)
                                                set(Calendar.DAY_OF_MONTH, d)
                                                set(Calendar.HOUR_OF_DAY, 0)
                                                set(Calendar.MINUTE, 0)
                                                set(Calendar.SECOND, 0)
                                                set(Calendar.MILLISECOND, 0)
                                            }
                                            useCustomStartDate = cal.timeInMillis
                                            // Automatically pre-fill expected end date (5 days later)
                                            useCustomEndDate = cal.timeInMillis + 5L * 24 * 60 * 60 * 1000
                                        },
                                        calendar.get(Calendar.YEAR),
                                        calendar.get(Calendar.MONTH),
                                        calendar.get(Calendar.DAY_OF_MONTH)
                                    )
                                    d1.datePicker.maxDate = System.currentTimeMillis()
                                    d1.show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.DeepSlate)
                            ) {
                                Text(formatGregorianDate(useCustomStartDate), color = SoftTheme.TextWhite)
                            }

                            Text("تاريخ الانتهاء:", color = SoftTheme.TextWhite, style = MaterialTheme.typography.bodySmall)
                            Button(
                                onClick = {
                                    val calendar = Calendar.getInstance().apply { timeInMillis = useCustomEndDate }
                                    val d2 = android.app.DatePickerDialog(
                                        context,
                                        { _, y, m, d ->
                                            val cal = Calendar.getInstance().apply {
                                                set(Calendar.YEAR, y)
                                                set(Calendar.MONTH, m)
                                                set(Calendar.DAY_OF_MONTH, d)
                                                set(Calendar.HOUR_OF_DAY, 0)
                                                set(Calendar.MINUTE, 0)
                                                set(Calendar.SECOND, 0)
                                                set(Calendar.MILLISECOND, 0)
                                            }
                                            useCustomEndDate = cal.timeInMillis
                                        },
                                        calendar.get(Calendar.YEAR),
                                        calendar.get(Calendar.MONTH),
                                        calendar.get(Calendar.DAY_OF_MONTH)
                                    )
                                    d2.datePicker.maxDate = System.currentTimeMillis()
                                    d2.show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.DeepSlate)
                            ) {
                                Text(formatGregorianDate(useCustomEndDate), color = SoftTheme.TextWhite)
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { selectStartToday = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectStartToday) SoftTheme.SoftPink else SoftTheme.DeepSlate,
                                    contentColor = if (selectStartToday) SoftTheme.DeepSlate else SoftTheme.TextWhite
                                )
                            ) {
                                Text("اليوم")
                            }
                            Button(
                                onClick = { selectStartToday = false },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (!selectStartToday) SoftTheme.SoftPink else SoftTheme.DeepSlate,
                                    contentColor = if (!selectStartToday) SoftTheme.DeepSlate else SoftTheme.TextWhite
                                )
                            ) {
                                Text("أمس")
                            }
                        }
                    }

                    Text("غزارة الطمث:", color = SoftTheme.TextWhite)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val intensities = listOf("light" to "خفيفة", "medium" to "متوسطة", "heavy" to "غزيرة")
                        intensities.forEach { (key, label) ->
                            val isSelected = selectedIntensity == key
                            Button(
                                onClick = { selectedIntensity = key },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) SoftTheme.SoftPink else SoftTheme.DeepSlate,
                                    contentColor = if (isSelected) SoftTheme.DeepSlate else SoftTheme.TextWhite
                                )
                            ) {
                                Text(label, fontSize = 12.sp)
                            }
                        }
                    }

                    Text("مستوى الألم: $painLevel/10", color = SoftTheme.TextWhite)
                    Slider(
                        value = painLevel.toFloat(),
                        onValueChange = { painLevel = it.toInt() },
                        valueRange = 1f..10f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = SoftTheme.SoftPink,
                            activeTrackColor = SoftTheme.SoftPink,
                            inactiveTrackColor = SoftTheme.DeepSlate
                        )
                    )

                    Text("الأعراض المرافقة:", color = SoftTheme.TextWhite)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        symptomsList.forEach { sym ->
                            val isChecked = selectedSymptoms.contains(sym)
                            FilterChip(
                                selected = isChecked,
                                onClick = {
                                    if (isChecked) selectedSymptoms.remove(sym)
                                    else selectedSymptoms.add(sym)
                                },
                                label = { Text(sym) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SoftTheme.SoftPink,
                                    selectedLabelColor = SoftTheme.DeepSlate
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        label = { Text("ملاحظات خاصة...") },
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
                            onClick = { showAddPeriodDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء", color = SoftTheme.SoftGray)
                        }

                        Button(
                            onClick = {
                                val start = if (useSpecificDateRange) {
                                    useCustomStartDate
                                } else {
                                    if (selectStartToday) {
                                        System.currentTimeMillis()
                                    } else {
                                        System.currentTimeMillis() - 24 * 60 * 60 * 1000
                                    }
                                }
                                val end = if (useSpecificDateRange) {
                                    useCustomEndDate
                                } else {
                                    start + 5L * 24 * 60 * 60 * 1000
                                }
                                viewModel.addPeriodLog(
                                    startDate = start,
                                    endDate = end,
                                    intensity = selectedIntensity,
                                    symptoms = selectedSymptoms.toList(),
                                    painLevel = painLevel,
                                    notes = notesInput
                                )
                                if (isPregnant != null && isPregnant?.isPregnant == true) {
                                    pendingPeriodStartDate = start
                                    showPregnancyLmpPromptDialog = true
                                }
                                showAddPeriodDialog = false
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

    // --- حوار تأكيد تاريخ دورة الحمل المكتشفة تلقائياً ---
    if (showPregnancyLmpPromptDialog) {
        pendingPeriodStartDate?.let { pendingDate ->
            val dateStr = formatGregorianDate(pendingDate)
            Dialog(onDismissRequest = { showPregnancyLmpPromptDialog = false }) {
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
                        text = "تحديث حسابات الحمل 🌸🤰",
                        style = MaterialTheme.typography.titleLarge,
                        color = SoftTheme.TextWhite,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "صديقتي الغالية، نلاحظ أنكِ سجلتِ حالة حمل نشطة في التطبيق.\n\nهل الدورة التي سجلتِها الآن (والتي بدأت بتاريخ $dateStr) هي الدورة الشهرية الأخيرة التي حصل بعدها الحمل مباشرة؟\n\nإذا كانت الإجابة نعم، فسيقوم رفيقكِ الذكي بتعديل تاريخ الحمل وتاريخ الولادة المتوقع تلقائياً بناءً عليها لتكون جميع الإرشادات والمعلومات الطبية دقيقة تماماً 💖",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoftTheme.SoftGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                isPregnant?.let { preg ->
                                    viewModel.setPregnancy(pendingPeriodStartDate, preg.prePregnancyWeight, preg.heightCm)
                                }
                                showPregnancyLmpPromptDialog = false
                                pendingPeriodStartDate = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("نعم، دورة الحمل 👶", color = SoftTheme.TextWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                showPregnancyLmpPromptDialog = false
                                pendingPeriodStartDate = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.DeepSlate),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("لا، تسجيل عادي 📝", color = SoftTheme.SoftGray, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
        }
    }

    // +++ حوار تعديل جنس واسم الجنين داخل صفحة الحمل +++
    if (showBabyInfoDialog) {
        Dialog(onDismissRequest = { showBabyInfoDialog = false }) {
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

    // +++ حوار مباركة الولادة وتحديد طريقتها داخل صفحة الحمل +++
    if (showDeliveryDialog) {
        Dialog(onDismissRequest = { showDeliveryDialog = false }) {
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

    // +++ حوار تأكيد إنهاء الحمل ومعرفة السبب +++
    if (showEndPregnancyConfirmDialog) {
        Dialog(onDismissRequest = { showEndPregnancyConfirmDialog = false }) {
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
                        text = "تأكيد إنهاء الحمل الحالي 🤰💔",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.TextWhite,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "هل أنتِ متأكدة من رغبتكِ في إنهاء تتبع الحمل الحالي والعودة إلى تتبع الدورة الشهرية والخصوبة؟\n\nيرجى تحديد سبب إنهاء الحمل لنتمكن من توجيهكِ وتقديم الدعم المناسب لكِ:",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTheme.SoftGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Button(
                        onClick = {
                            showEndPregnancyConfirmDialog = false
                            showDeliveryDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("الحمد لله، تمّت الولادة بسلام 🎉👶", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            showEndPregnancyConfirmDialog = false
                            showLossSupportDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.DeepSlate),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("حدثت مشكلة أو فقدان للحمل لا قدر الله 🤍", color = SoftTheme.SoftPink, fontWeight = FontWeight.Bold)
                    }

                    TextButton(
                        onClick = { showEndPregnancyConfirmDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("تراجع وإلغاء 🌸", color = SoftTheme.SoftGray)
                    }
                }
            }
        }
    }

    // +++ حوار المواساة والدعم في حالة الفقدان +++
    if (showLossSupportDialog) {
        Dialog(onDismissRequest = { showLossSupportDialog = false }) {
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
                        text = "عوضكِ الله خيراً يا حبيبتي 🤍",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.SoftPink,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "﴿وَبَشِّرِ الصَّابِرِينَ﴾\n\nسلامة قلبكِ وجسدكِ يا غالية. لا تحزني ولا تفقدي الأمل، فالله لطيف خبير ورحيم، وعوضه جميل دائماً.\n\nنحن هنا بجانبكِ دوماً لتقديم كل الحب والدعم. سنقوم الآن بإعادة ضبط التطبيق لتتبع الدورة الشهرية والراحة لمساعدتكِ على التعافي الهادئ خطوة بخطوة.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTheme.TextWhite,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Button(
                        onClick = {
                            showLossSupportDialog = false
                            viewModel.switchToPeriodTracking()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("الحمد لله على كل حال (العودة للدورة)", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
}
