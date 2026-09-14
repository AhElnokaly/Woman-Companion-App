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
import com.example.ui.period.PmsInsightsCard
import com.example.ui.period.PeriodLogDialog
import com.example.ui.period.PeriodCalendarSection
import com.example.ui.period.PeriodHistorySection
import com.example.ui.period.PeriodHealthAdvisoryCard
import com.example.ui.pregnancy.*
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
    var showStartPregnancySetupDialog by remember { mutableStateOf(false) }
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

            // PMS Insights and Care Card
            if (isPregnant?.isPregnant != true) {
                val nextStartPredicted = predictedPeriods.firstOrNull()?.first
                item {
                    PmsInsightsCard(
                        periodLogs = periodLogs,
                        nextPeriodPredictedStart = nextStartPredicted
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

                // 🤰 بطاقة الانتقال السلس لوضع الحمل
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showStartPregnancySetupDialog = true }
                            .testTag("start_pregnancy_mode_card"),
                        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.5.dp, SoftTheme.MintTeal.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(SoftTheme.MintAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🤰", fontSize = 24.sp)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "أنا حامل الآن 🤰✨",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftTheme.SoftPink
                                )
                                Text(
                                    text = "انتقلي لوضع تتبع الحمل لمتابعة نمو جنينكِ أسبوعاً بأسبوع وحساب موعد الولادة المتوقع.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SoftTheme.TextWhite
                                )
                            }
                            Button(
                                onClick = { showStartPregnancySetupDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.MintTeal),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("ابدئي", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
                            PregnancyBabyInfoCard(
                                isGenderKnown = isGenderKnown,
                                babyGender = babyGender,
                                babyName = babyName,
                                onEditBabyInfo = {
                                    babyGenderInput = babyGender ?: ""
                                    babyNameInput = babyName ?: ""
                                    showBabyInfoDialog = true
                                }
                            )
                        }
                    }

                    // Primary Weeks Circle Card
                    item {
                        PregnancyProgressCard(prog = prog)
                    }

                    // Post-term Pregnancy (الشهر العاشر) Supportive Card
                    if (prog.weeks >= 40) {
                        item {
                            PostTermSupportCard()
                        }
                    }

                    // Baby Size Visual Comparison Card
                    item {
                        BabySizeComparisonCard(prog = prog)
                    }

                    // Safety Birth Baby Announcement Card (Show only late in pregnancy, Week 36 or later)
                    if (prog.weeks >= 36) {
                        item {
                            BirthAnnouncementPromptCard(
                                onAnnounceBirth = { showDeliveryDialog = true }
                            )
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
                    PeriodHealthAdvisoryCard(
                        periodLogs = periodLogs,
                        stats = stats
                    )
                }

                item {
                    D3NativeDashboard(periodLogs = periodLogs, stats = stats, viewModel = viewModel)
                }
            }

            // Smart Interactive Calendar Component (Cycle Mode Only)
            if (isPregnant?.isPregnant != true) {
                item {
                    PeriodCalendarSection(
                        periodLogs = periodLogs,
                        isPregnant = isPregnant,
                        allPregnancies = allPregnancies,
                        nifasDurationDays = nifasDurationDays,
                        predictedPeriods = predictedPeriods,
                        predictedOvulations = predictedOvulations,
                        onDaySelected = { dayTime ->
                            useCustomStartDate = dayTime
                            useCustomEndDate = dayTime + 5L * 24 * 60 * 60 * 1000
                            useSpecificDateRange = true
                            showAddPeriodDialog = true
                        }
                    )
                }

                // History List Section
                item {
                    PeriodHistorySection(
                        periodLogs = periodLogs,
                        onDeleteLog = { log -> viewModel.deletePeriod(log) }
                    )
                }
            }
        }
    }

    // Add Period Dialog
    if (showAddPeriodDialog) {
        PeriodLogDialog(
            initialStartDate = useCustomStartDate,
            initialEndDate = useCustomEndDate,
            useSpecificDateRangeInitial = useSpecificDateRange,
            isPregnant = isPregnant?.isPregnant == true,
            onSave = { start, end, intensity, symptoms, pain, notes ->
                viewModel.addPeriodLog(
                    startDate = start,
                    endDate = end,
                    intensity = intensity,
                    symptoms = symptoms,
                    painLevel = pain,
                    notes = notes
                )
                if (isPregnant?.isPregnant == true) {
                    pendingPeriodStartDate = start
                    showPregnancyLmpPromptDialog = true
                }
                showAddPeriodDialog = false
            },
            onDismiss = { showAddPeriodDialog = false }
        )
    }

    // Pregnancy Dialogs
    if (showPregnancyLmpPromptDialog && pendingPeriodStartDate != null) {
        PregnancyLmpPromptDialog(
            pendingPeriodStartDate = pendingPeriodStartDate,
            pregnancyInfo = isPregnant,
            onConfirmLmp = { startDate ->
                isPregnant?.let { preg ->
                    viewModel.setPregnancy(startDate, preg.prePregnancyWeight, preg.heightCm)
                }
                showPregnancyLmpPromptDialog = false
                pendingPeriodStartDate = null
            },
            onDismiss = {
                showPregnancyLmpPromptDialog = false
                pendingPeriodStartDate = null
            }
        )
    }

    if (showBabyInfoDialog) {
        BabyInfoDialog(
            initialGender = babyGenderInput,
            initialName = babyNameInput,
            onSave = { gender, name ->
                viewModel.updateBabyInfo(gender, name)
                showBabyInfoDialog = false
            },
            onDismiss = { showBabyInfoDialog = false }
        )
    }

    if (showDeliveryDialog) {
        DeliveryDialog(
            onConfirmDelivery = { method ->
                viewModel.updateDeliveryInfo(isDelivered = true, birthMethod = method)
                showDeliveryDialog = false
            },
            onDismiss = { showDeliveryDialog = false }
        )
    }

    if (showEndPregnancyConfirmDialog) {
        EndPregnancyConfirmDialog(
            onOpenDelivery = {
                showEndPregnancyConfirmDialog = false
                showDeliveryDialog = true
            },
            onOpenLossSupport = {
                showEndPregnancyConfirmDialog = false
                showLossSupportDialog = true
            },
            onDismiss = { showEndPregnancyConfirmDialog = false }
        )
    }

    if (showLossSupportDialog) {
        PregnancyLossDialog(
            onConfirmResetToPeriod = {
                showLossSupportDialog = false
                viewModel.switchToPeriodTracking()
            },
            onDismiss = { showLossSupportDialog = false }
        )
    }

    if (showStartPregnancySetupDialog) {
        PregnancySetupDialog(
            periodLogs = periodLogs,
            onDismiss = { showStartPregnancySetupDialog = false },
            onSave = { date, weight, height ->
                viewModel.setPregnancy(date, weight, height)
                showStartPregnancySetupDialog = false
            }
        )
    }
}
