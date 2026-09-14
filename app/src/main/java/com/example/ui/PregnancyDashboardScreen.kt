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
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.example.ui.pregnancy.*
import com.example.ui.profile.EditProfileDialog
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
    val allFetalGrowthLogs by viewModel.allFetalGrowthLogsState.collectAsStateWithLifecycle()
    val allMaternalWeightLogs by viewModel.allMaternalWeightLogsState.collectAsStateWithLifecycle()
    val settings by viewModel.appLockSettingsState.collectAsStateWithLifecycle()
    val medications by viewModel.allMedicationsState.collectAsStateWithLifecycle()
    val activeMedications by viewModel.activeMedicationsState.collectAsStateWithLifecycle()
    val allWaterLogs by viewModel.allWaterLogsState.collectAsStateWithLifecycle()
    val allNutritionLogs by viewModel.allNutritionLogsState.collectAsStateWithLifecycle()
    val allSleepLogs by viewModel.allSleepLogsState.collectAsStateWithLifecycle()
    val adherenceLogs by viewModel.allMedicationAdherenceLogsState.collectAsStateWithLifecycle()
    
    val activeStart by viewModel.currentKickSessionStart.collectAsStateWithLifecycle()
    val currentCount by viewModel.currentKickCount.collectAsStateWithLifecycle()

    var showSetupDialog by remember { mutableStateOf(false) }
    var showScoreBreakdownDialog by remember { mutableStateOf(false) }
    var showAddBpDialog by remember { mutableStateOf(false) }
    var showAddJournalDialog by remember { mutableStateOf(false) }

    var showBabyInfoDialog by remember { mutableStateOf(false) }
    var babyGenderInput by remember { mutableStateOf(pregState?.babyGender ?: "") }
    var babyNameInput by remember { mutableStateOf(pregState?.babyName ?: "") }
    var showDeliveryDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showBreathingDialog by remember { mutableStateOf(false) }
    var showCustomizationDialog by remember { mutableStateOf(false) }
    var showFetalVisualizerDialog by remember { mutableStateOf(false) }

    var selectedFilterTab by rememberSaveable { mutableIntStateOf(0) } // 0: الكل, 1: صحتي ويومي, 2: طفلي وحملي

    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val contextualSubtitle = remember(currentHour, pregState?.isPregnant) {
        if (pregState?.isPregnant == true) {
            when (currentHour) {
                in 5..11 -> "رطبي جسمكِ برشفة ماء وتناولي جرعة الصباح 💧"
                in 12..16 -> "احرصي على شرب الماء وأخذ قسط من الراحة 🌿"
                in 17..21 -> "تفقدي خطواتكِ لليوم وسجلي ركلات طفلكِ ونسب التغذية ✨"
                else -> "استرخي مع تمارين التنفس الهادئة لتهيئة نوم صحي وعميق 🌙"
            }
        } else {
            when (currentHour) {
                in 5..11 -> "ابدئي صباحكِ بنشاط وترطيب متوازن 💧"
                in 12..16 -> "حافظي على طاقتكِ وخذي استراحة خفيفة 🌿"
                in 17..21 -> "راجعي إنجاز أهدافكِ اليومية والنشاط البدني ✨"
                else -> "أمسية هادئة لنوم عميق وتجديد الحيوية 🌙"
            }
        }
    }

    val context = LocalContext.current
    var layoutRefreshKey by remember { mutableStateOf(0) }
    val dashPrefs = remember(layoutRefreshKey) { context.getSharedPreferences("dashboard_layout_prefs", Context.MODE_PRIVATE) }
    val showSecBabyInfo = remember(dashPrefs, layoutRefreshKey) { dashPrefs.getBoolean("sec_baby_info", true) }
    val showSecQuickActions = remember(dashPrefs, layoutRefreshKey) { dashPrefs.getBoolean("sec_quick_actions", true) }
    val showSecMeds = remember(dashPrefs, layoutRefreshKey) { dashPrefs.getBoolean("sec_meds", true) }
    val showSecVitals = remember(dashPrefs, layoutRefreshKey) { dashPrefs.getBoolean("sec_vitals", true) }
    val showSecExplore = remember(dashPrefs, layoutRefreshKey) { dashPrefs.getBoolean("sec_explore", true) }

    // --- Dynamic Daily Goal Progress Calculations (مرتبط مباشرة ببيانات اليوم الحقيقية) ---
    val todayStartMillis = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val todayWaterMl = remember(allWaterLogs, todayStartMillis) {
        allWaterLogs.filter { it.date >= todayStartMillis }.sumOf { it.amountMl }
    }
    val waterTarget = viewModel.getWaterTarget()
    val waterFraction = if (waterTarget > 0) (todayWaterMl.toFloat() / waterTarget.toFloat()).coerceIn(0f, 1f) else 0f

    val todayCalories = remember(allNutritionLogs, todayStartMillis) {
        allNutritionLogs.filter { it.date >= todayStartMillis }.sumOf { it.calories }
    }
    val calorieTarget = viewModel.getCalorieTarget().target
    val caloriesFraction = if (calorieTarget > 0) (todayCalories.toFloat() / calorieTarget.toFloat()).coerceIn(0f, 1f) else 0f

    val latestSleep = remember(allSleepLogs, todayStartMillis) {
        allSleepLogs.filter { it.date >= todayStartMillis - 14 * 3600 * 1000L }.maxByOrNull { it.date }
    }
    val sleepDurationHours = remember(latestSleep) {
        if (latestSleep != null && latestSleep.endTime > latestSleep.startTime) {
            (latestSleep.endTime - latestSleep.startTime) / (1000f * 3600f)
        } else 0f
    }
    val sleepFraction = if (sleepDurationHours > 0f) {
        (sleepDurationHours / 7.5f).coerceIn(0f, 1f)
    } else 0f

    val stepGoal = settings?.dailyStepTarget ?: 6000
    val currentSteps = todayStepLog?.steps ?: 0
    val stepsFraction = if (stepGoal > 0) (currentSteps.toFloat() / stepGoal.toFloat()).coerceIn(0f, 1f) else 0f

    val todayTakenMedCount = remember(adherenceLogs, activeMedications, todayStartMillis) {
        val takenIds = adherenceLogs
            .filter { it.scheduledTime >= todayStartMillis && it.status == "TAKEN" }
            .map { it.medicationId }
            .toSet()
        activeMedications.count { it.id in takenIds }
    }
    val medsFraction = if (activeMedications.isNotEmpty()) {
        (todayTakenMedCount.toFloat() / activeMedications.size.toFloat()).coerceIn(0f, 1f)
    } else null

    val dailyProgressScore = remember(waterFraction, caloriesFraction, sleepFraction, stepsFraction, medsFraction) {
        val score = if (medsFraction != null) {
            (waterFraction * 0.25f + caloriesFraction * 0.25f + sleepFraction * 0.20f + stepsFraction * 0.15f + medsFraction * 0.15f) * 100f
        } else {
            (waterFraction * 0.30f + caloriesFraction * 0.30f + sleepFraction * 0.20f + stepsFraction * 0.20f) * 100f
        }
        kotlin.math.round(score).toInt().coerceIn(0, 100)
    }

    val dynamicHeroTitle = remember(dailyProgressScore, pregState?.isPregnant) {
        if (dailyProgressScore == 0) {
            if (pregState?.isPregnant == true) "ابدئي يومكِ الصحي بالرعاية 🌸" else "ابدئي يومكِ الصحي بكل حيوية 🌸"
        } else if (dailyProgressScore < 50) {
            if (pregState?.isPregnant == true) "بداية موفقة لصحتكِ وصحة طفلكِ ✨" else "بداية موفقة لروتينكِ الصحي ✨"
        } else if (dailyProgressScore < 85) {
            if (pregState?.isPregnant == true) "حملكِ يسير بصحة وعافية وتقدم رائع 🌿" else "أنتِ على الطريق الصحيح وتوازن رائع 🌿"
        } else {
            if (pregState?.isPregnant == true) "يوم مثالي وصحة متألقة لكِ ولطفلكِ 🌟" else "يوم رائع وإنجاز صحي متكامل 🌟"
        }
    }

    val dynamicHeroSubtitle = remember(dailyProgressScore, todayWaterMl, todayCalories, currentSteps) {
        if (dailyProgressScore == 0) {
            "سجلي شرب الماء، الوجبات، والأنشطة لتتبع إنجاز أهدافكِ خطوة بخطوة 💧"
        } else {
            "أنجزتِ $dailyProgressScore% من أهدافكِ اليومية .. واصلي روتينكِ الصحي 🌸"
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // 🌿 1. Modern Health Header
        item {
            com.example.ui.dashboard.ModernHealthHeader(
                userName = pregState?.motherName,
                onAvatarClick = { showEditProfileDialog = true },
                onNotificationsClick = onNavigateToSettings,
                onToggleTheme = { viewModel.toggleDarkMode() },
                hasUnreadNotifications = true
            )
        }

        // 🌿 2. Modern Health Score Hero Card (Dynamic score linked to real data)
        item {
            com.example.ui.dashboard.HealthScoreHeroCard(
                scorePercent = dailyProgressScore,
                titleText = dynamicHeroTitle,
                subtitleText = dynamicHeroSubtitle,
                onClick = { showScoreBreakdownDialog = true }
            )
        }

        // 🎛️ Segmented Filter & Customization Action Bar
        item {
            val isPreg = pregState?.isPregnant == true
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SoftTheme.CardBg)
                    .border(1.dp, SoftTheme.CardBorder, RoundedCornerShape(16.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val filterOptions = if (isPreg) {
                    listOf("الكل 🌟", "صحتي ويومي 🌿", "طفلي وحملي 🤰")
                } else {
                    listOf("الكل 🌟", "صحتي ويومي 🌿", "رحلة الحمل 🤰")
                }
                filterOptions.forEachIndexed { index, label ->
                    val isSelected = selectedFilterTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) SoftTheme.MintTeal else Color.Transparent
                            )
                            .clickable { selectedFilterTab = index },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp,
                                color = if (isSelected) Color.White else SoftTheme.TextSecondaryMuted
                            ),
                            maxLines = 1
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SoftTheme.MintAccent)
                        .border(1.dp, SoftTheme.CardBorder, RoundedCornerShape(12.dp))
                        .clickable { showCustomizationDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "تخصيص لوحة التحكم",
                        tint = SoftTheme.EmeraldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // 🌿 4. Modern Bento Grid (2x2 + Cards)
        if (selectedFilterTab == 0 || selectedFilterTab == 1) {
            item {
                com.example.ui.dashboard.ModernBentoGrid(
                    viewModel = viewModel,
                    onNavigateToWater = { onNavigateToTab(2) },
                    onNavigateToNutrition = { onNavigateToTab(2) },
                    onNavigateToSleep = { onNavigateToTab(3) },
                    onNavigateToSymptoms = { onNavigateToTab(3) },
                    onNavigateToPeriod = { onNavigateToTab(1) },
                    onNavigateToChat = onOpenJouriChat
                )
            }
        }

        // 🌸 حالة الحمل: عرض مؤشر النمو الدائري وشبكة بينتو بعد شبكة الصحة الأساسية
        if (pregState?.isPregnant == true && pregState?.isDelivered != true && progression != null) {
            val prog = progression!!
            val trimesterColor = when {
                prog.weeks >= 41 -> Color(0xFFFFB300) // Month 10: Gold Amber
                prog.trimester == 1 -> Color(0xFF9575CD) // Trimester 1: Lavender
                prog.trimester == 2 -> SoftTheme.MintTeal // Trimester 2: Mint Teal
                else -> SoftTheme.SoftPink // Trimester 3: Soft Pink
            }

            val monthProg = calculateMonthProgress(prog.weeks, prog.daysIntoWeek)
            val activeMonth = monthProg.monthNumber
            val activeMonthProgress = monthProg.progressFraction

            // 🌸 Jouri Signature Pregnancy Radial Gauge Card
            if (selectedFilterTab == 0 || selectedFilterTab == 2) {
                item {
                    JouriPregnancyRadialGauge(
                        progression = prog,
                        activeMonth = activeMonth,
                        activeMonthProgress = activeMonthProgress,
                        trimesterColor = trimesterColor,
                        onFetalClick = {
                            showFetalVisualizerDialog = true
                        }
                    )
                }
            }

            // 🌟 Jouri Pregnancy Baby Development Card
            if (selectedFilterTab == 0 || selectedFilterTab == 2) {
                item {
                    PregnancyBabyDevCard(
                        progression = prog,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { showFetalVisualizerDialog = true }
                    )
                }
            }

            // Baby Info & Doctor Care Hub (Gender, Name, Ultrasound Weight & Length, Doctor Appointment)
            val babyGender = pregState?.babyGender
            val babyName = pregState?.babyName
            val activePregId = pregState?.id ?: 0
            val latestFetalLog = allFetalGrowthLogs
                .filter { it.pregnancyId == activePregId }
                .maxByOrNull { it.pregnancyWeek }
            val nextDoctorAppt = appointments
                .filter { it.dateTime >= System.currentTimeMillis() && !it.completed }
                .minByOrNull { it.dateTime }
            
            if ((selectedFilterTab == 0 || selectedFilterTab == 2) && showSecBabyInfo) {
                item {
                    PregnancyBabyInfoDisplayCard(
                        babyGender = babyGender,
                        babyName = babyName,
                        currentWeekNumber = prog.weeks,
                        latestFetalLog = latestFetalLog,
                        upcomingDoctorAppointment = nextDoctorAppt,
                        onOpenCareDialog = {
                            babyGenderInput = babyGender ?: ""
                            babyNameInput = babyName ?: ""
                            showBabyInfoDialog = true
                        }
                    )
                }

                item {
                    val context = LocalContext.current
                    MaternalWeightTrackerCard(
                        pregnancy = pregState,
                        currentWeekNumber = prog.weeks,
                        maternalWeightLogs = allMaternalWeightLogs,
                        latestFetalLog = latestFetalLog,
                        onAddMaternalWeight = { week, weight, notes ->
                            viewModel.addMaternalWeightLog(week, weight, notes)
                            Toast.makeText(context, "تم تسجيل وزنكِ بنجاح ⚖️✨", Toast.LENGTH_SHORT).show()
                        },
                        onDeleteMaternalWeight = { log ->
                            viewModel.deleteMaternalWeightLog(log)
                            Toast.makeText(context, "تم حذف قياس الوزن", Toast.LENGTH_SHORT).show()
                        },
                        onUpdatePrePregnancyWeight = { preWeight, height ->
                            viewModel.updatePrePregnancyWeightAndHeight(preWeight, height)
                            Toast.makeText(context, "تم حفظ بيانات ما قبل الحمل بنجاح 🌸", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        if (selectedFilterTab == 0 || selectedFilterTab == 2) {
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
        if (selectedFilterTab == 0 || selectedFilterTab == 1) {
            item {
                ExactAlarmBannerCard()
            }
        }

        if ((selectedFilterTab == 0 || selectedFilterTab == 1) && showSecMeds && activeMedications.isNotEmpty()) {
            item {
                DailyVitaminsCard(
                    viewModel = viewModel,
                    onNavigateToMeds = { onNavigateToTab(3) }
                )
            }
        }

        // 🌟 Jouri Explore Cards Carousel (مجموعة بطاقات الاستكشاف الذكية في كاروسيل أفقي لتقليل التمرير الرأسي والعبء البصري)
        if ((selectedFilterTab == 0 || selectedFilterTab == 2) && showSecExplore) {
            item {
                JouriExploreCardsCarousel(
                    viewModel = viewModel,
                    onOpenJouriChat = onOpenJouriChat,
                    onNavigateToTab = onNavigateToTab
                )
            }
        }

        // ⚡ Quick Actions & Logging Hub
        if (showSecQuickActions) {
            item {
                val isKickActive = activeStart != null
                PregnancyQuickActionsCard(
                    isPregnant = pregState?.isPregnant == true,
                    isKickActive = isKickActive,
                    currentCount = currentCount,
                    onAddWater = { viewModel.addWater(250) },
                    onOpenBpDialog = { showAddBpDialog = true },
                    onOpenJournalDialog = { showAddJournalDialog = true },
                    onOpenBreathingDialog = { showBreathingDialog = true },
                    onKickClick = {
                        if (isKickActive) {
                            viewModel.incrementKickCount()
                        } else {
                            viewModel.startFetalKickSession()
                        }
                    },
                    onSaveKick = { viewModel.saveFetalKickSession() },
                    onCancelKick = { viewModel.cancelFetalKickSession() }
                )
            }
        }

        if (pregState == null || pregState?.isPregnant != true) {
            if (selectedFilterTab == 0 || selectedFilterTab == 2) {
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
            }

            if (selectedFilterTab == 0 || selectedFilterTab == 1) {
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
                                text = "💡 معلومات الدورة والخصوبة",
                                color = SoftTheme.MintTeal,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "انتقلي إلى علامة تبويب 'الدورة' لتسجيل دورتكِ الشهرية والتنبؤ بتواريخ الخصوبة والإباضة المستقبلية بمجرد تسجيل ٣ دورات متتالية.",
                                color = SoftTheme.SoftGray,
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        } else if (pregState?.isDelivered == true) {
            // +++ أضيف بناءً على طلبك لتقديم نصائح ودعم فترة النفاس والتعافي +++
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    PostpartumRecoveryCard(
                        pregState = pregState,
                        viewModel = viewModel,
                        onNavigateToNutrition = { onNavigateToTab(2) }
                    )

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
        } else {
            // Pregnant View - Progression Details & Late Pregnancy Actions
            progression?.let { prog ->
                // Post-term Pregnancy (الشهر العاشر) Supportive Card
                if (prog.weeks >= 40) {
                    item {
                        PregnancyPostTermCard()
                    }
                }

                // Smart Contraction Timer (Show late in pregnancy, Week 36 or later)
                if (prog.weeks >= 36) {
                    item {
                        ContractionTimerCard()
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

        // ⚙️ Dashboard Customization Banner Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardBg),
                border = BorderStroke(1.dp, SoftTheme.CardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_customization_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = SoftTheme.MintTeal,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "تخصيص وترتيب البطاقات",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SoftTheme.TextPrimary
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "تحكمي في إظهار أو إخفاء بطاقات لوحة التحكم بحسب احتياجكِ",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SoftTheme.TextSecondaryMuted,
                                fontSize = 11.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { showCustomizationDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.MintTeal),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "تخصيص ⚙️",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // 📖✨ Spiritual & Quietude Reflection Section
        item {
            PregnancySpiritualCard()
        }
    }

    // Pregnancy setup Dialog
    if (showSetupDialog) {
        PregnancySetupDialog(
            periodLogs = periodLogs,
            onDismiss = { showSetupDialog = false },
            onSave = { date, weight, height ->
                viewModel.setPregnancy(date, weight, height)
                showSetupDialog = false
            }
        )
    }

    // --- Blood Pressure Quick Logging Dialog ---
    if (showAddBpDialog) {
        BloodPressureDialog(
            onDismiss = { showAddBpDialog = false },
            isLowBp = pregState?.hasLowBp == true,
            availableMedications = medications.filter { it.isActive }.map { it.name },
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
        AddPregnancyJournalDialog(
            onDismiss = { showAddJournalDialog = false },
            onSave = { content, mood ->
                viewModel.addJournalEntry(content, mood)
                showAddJournalDialog = false
            }
        )
    }

    // --- Baby Info & Doctor Care Dialog (Gender, Name, Ultrasound Measurements, Doctor Appointment) ---
    if (showBabyInfoDialog) {
        val currentWeeks = progression?.weeks ?: 14
        val activePregId = pregState?.id ?: 0
        val currentPregFetalLogs = allFetalGrowthLogs.filter { it.pregnancyId == activePregId }

        BabyAndDoctorCareDialog(
            initialGender = babyGenderInput,
            initialName = babyNameInput,
            currentWeekNumber = currentWeeks,
            fetalGrowthLogs = currentPregFetalLogs,
            appointments = appointments,
            onDismissRequest = { showBabyInfoDialog = false },
            onSaveBabyInfo = { gender, name ->
                viewModel.updateBabyInfo(gender, name)
            },
            onAddFetalGrowth = { week, weight, length, notes ->
                viewModel.addFetalGrowthLog(week, weight, length, notes)
                Toast.makeText(context, "تم حفظ قياسات السونار بنجاح 📏✨", Toast.LENGTH_SHORT).show()
            },
            onAddDoctorAppointment = { title, dateTime, doctor, notes ->
                viewModel.addAppointment(title, dateTime, doctor, notes)
                Toast.makeText(context, "تم حجز وتذكير موعد الطبيبة بنجاح 🏥✨", Toast.LENGTH_SHORT).show()
            },
            onToggleAppointment = { appt ->
                viewModel.toggleAppointmentCompleted(appt)
            }
        )
    }

    // --- Delivery Record Dialog ---
    if (showDeliveryDialog) {
        DeliveryRecordDialog(
            onDismiss = { showDeliveryDialog = false },
            onConfirm = { birthMethod ->
                viewModel.updateDeliveryInfo(isDelivered = true, birthMethod = birthMethod)
                showDeliveryDialog = false
            }
        )
    }

    // --- Profile Edit Dialog ---
    if (showEditProfileDialog) {
        val currentContext = LocalContext.current
        EditProfileDialog(
            currentProfile = pregState,
            onDismiss = { showEditProfileDialog = false },
            onSave = { motherName, nickname, birthDate, heightCm, preWeight, hasHighBp, hasLowBp, hasDiabetes, chronicOthers, babyName ->
                viewModel.updateFullProfile(
                    motherName = motherName,
                    nickname = nickname,
                    birthDate = birthDate,
                    heightCm = heightCm,
                    prePregnancyWeight = preWeight,
                    hasHighBp = hasHighBp,
                    hasLowBp = hasLowBp,
                    hasDiabetes = hasDiabetes,
                    chronicOthers = chronicOthers,
                    babyName = babyName
                )
                Toast.makeText(currentContext, "تم حفظ بياناتك بنجاح 🌸", Toast.LENGTH_SHORT).show()
                showEditProfileDialog = false
            }
        )
    }

    // --- SOS Guided Breathing Dialog ---
    if (showBreathingDialog) {
        GuidedBreathingDialog(
            onDismiss = { showBreathingDialog = false }
        )
    }

    // --- Dashboard Customization Dialog ---
    if (showCustomizationDialog) {
        DashboardCustomizationDialog(
            onDismiss = { showCustomizationDialog = false },
            onSettingsChanged = {
                layoutRefreshKey++
            }
        )
    }

    // --- Fetal Size Visualizer Dialog ---
    if (showFetalVisualizerDialog && progression != null) {
        progression?.let { prog ->
            val activePregId = pregState?.id ?: 0
            val latestFetalLog = allFetalGrowthLogs
                .filter { it.pregnancyId == activePregId }
                .maxByOrNull { it.pregnancyWeek }

            FetalSizeVisualizerDialog(
                weekNumber = prog.weeks,
                babySizeFruit = prog.comparisonName,
                babyWeightGrams = if (latestFetalLog != null && latestFetalLog.weightGrams > 0) "${latestFetalLog.weightGrams.toInt()} جم (أسبوع ${latestFetalLog.pregnancyWeek})" else "",
                babyLengthCm = if (latestFetalLog != null && latestFetalLog.lengthCm > 0) "${latestFetalLog.lengthCm} سم" else "",
                isRealUltrasoundData = latestFetalLog != null && (latestFetalLog.weightGrams > 0 || latestFetalLog.lengthCm > 0),
                onDismiss = { showFetalVisualizerDialog = false }
            )
        }
    }

    // --- Health Score Breakdown Dialog (تفاصيل مؤشر إنجاز اليوم) ---
    if (showScoreBreakdownDialog) {
        AlertDialog(
            onDismissRequest = { showScoreBreakdownDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("📊", fontSize = 22.sp)
                    Text(
                        text = "تفاصيل إنجاز اليوم ($dailyProgressScore%)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.TextPrimary
                        )
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "يتم احتساب هذا المؤشر تلقائياً وبشكل حي بناءً على إنجاز أهدافكِ الصحية المسجلة لليوم:",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SoftTheme.TextSecondaryMuted,
                            lineHeight = 18.sp
                        )
                    )

                    // 1. Water
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardBg),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("💧 شرب الماء", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("$todayWaterMl / $waterTarget مل", color = SoftTheme.EmeraldPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            }
                            LinearProgressIndicator(
                                progress = { waterFraction },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = SoftTheme.EmeraldPrimary,
                                trackColor = SoftTheme.CardBorder
                            )
                        }
                    }

                    // 2. Nutrition
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardBg),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🥗 السعرات والتغذية", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("$todayCalories / $calorieTarget سعرة", color = SoftTheme.EmeraldPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            }
                            LinearProgressIndicator(
                                progress = { caloriesFraction },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = SoftTheme.EmeraldPrimary,
                                trackColor = SoftTheme.CardBorder
                            )
                        }
                    }

                    // 3. Sleep
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardBg),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🌙 النوم والراحة", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    if (sleepDurationHours > 0f) "${String.format(java.util.Locale.ENGLISH, "%.1f", sleepDurationHours)} ساعة" else "لم يُسجل اليوم",
                                    color = SoftTheme.EmeraldPrimary,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            LinearProgressIndicator(
                                progress = { sleepFraction },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = SoftTheme.EmeraldPrimary,
                                trackColor = SoftTheme.CardBorder
                            )
                        }
                    }

                    // 4. Activity / Steps
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardBg),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🚶‍♀️ خطوات النشاط", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("$currentSteps / $stepGoal خطوة", color = SoftTheme.EmeraldPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            }
                            LinearProgressIndicator(
                                progress = { stepsFraction },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = SoftTheme.EmeraldPrimary,
                                trackColor = SoftTheme.CardBorder
                            )
                        }
                    }

                    // 5. Medications (only if user registered medications)
                    if (activeMedications.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardBg),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("💊 الأدوية المجدولة", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Text("$todayTakenMedCount / ${activeMedications.size} جرعة", color = SoftTheme.EmeraldPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                }
                                LinearProgressIndicator(
                                    progress = { medsFraction ?: 0f },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                    color = SoftTheme.EmeraldPrimary,
                                    trackColor = SoftTheme.CardBorder
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showScoreBreakdownDialog = false }) {
                    Text("إغلاق", fontWeight = FontWeight.Bold, color = SoftTheme.EmeraldPrimary)
                }
            }
        )
    }
}




