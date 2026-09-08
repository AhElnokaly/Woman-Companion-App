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
    val settings by viewModel.appLockSettingsState.collectAsStateWithLifecycle()
    val medications by viewModel.allMedicationsState.collectAsStateWithLifecycle()
    
    val activeStart by viewModel.currentKickSessionStart.collectAsStateWithLifecycle()
    val currentCount by viewModel.currentKickCount.collectAsStateWithLifecycle()

    var showSetupDialog by remember { mutableStateOf(false) }
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

    val context = LocalContext.current
    var layoutRefreshKey by remember { mutableStateOf(0) }
    val dashPrefs = remember(layoutRefreshKey) { context.getSharedPreferences("dashboard_layout_prefs", Context.MODE_PRIVATE) }
    val showSecBabyInfo = remember(dashPrefs, layoutRefreshKey) { dashPrefs.getBoolean("sec_baby_info", true) }
    val showSecQuickActions = remember(dashPrefs, layoutRefreshKey) { dashPrefs.getBoolean("sec_quick_actions", true) }
    val showSecMeds = remember(dashPrefs, layoutRefreshKey) { dashPrefs.getBoolean("sec_meds", true) }
    val showSecVitals = remember(dashPrefs, layoutRefreshKey) { dashPrefs.getBoolean("sec_vitals", true) }
    val showSecExplore = remember(dashPrefs, layoutRefreshKey) { dashPrefs.getBoolean("sec_explore", true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                val companionName = settings?.companionName ?: "جوري"
                PregnancyHeaderCard(
                    companionName = companionName,
                    isNetworkAvailable = isNetworkAvailable,
                    weatherState = weatherState,
                    onToggleDarkMode = { viewModel.toggleDarkMode() },
                    onNavigateToSettings = onNavigateToSettings,
                    userName = pregState?.motherName,
                    onEditProfile = { showEditProfileDialog = true },
                    onOpenCustomization = { showCustomizationDialog = true }
                )
            }

            // 🌸 حالة الحمل: عرض مؤشر النمو الدائري وشبكة بينتو فوراً بعد الترحيب كأولوية بصرية أولى
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
                            onClick = { showFetalVisualizerDialog = true }
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

                // Baby Info (Gender & Name Display/Edit Card)
                val babyGender = pregState?.babyGender
                val babyName = pregState?.babyName
                val isGenderKnown = !babyGender.isNullOrEmpty()
                
                if (showSecBabyInfo && (prog.weeks >= 14 || isGenderKnown)) {
                    item {
                        PregnancyBabyInfoDisplayCard(
                            babyGender = babyGender,
                            babyName = babyName,
                            onOpenEditDialog = {
                                babyGenderInput = babyGender ?: ""
                                babyNameInput = babyName ?: ""
                                showBabyInfoDialog = true
                            }
                        )
                    }
                }
            }

            // Upcoming Dose Banner (نظام تنبيه الجرعة القادمة الفوري)
            if (showSecMeds) {
                item {
                    UpcomingDoseBanner(viewModel = viewModel)
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

        if (showSecMeds) {
            item {
                DailyVitaminsCard(
                    viewModel = viewModel,
                    onNavigateToMeds = { onNavigateToTab(3) }
                )
            }
        }

        // 🌟 Jouri Explore Cards Carousel (مجموعة بطاقات الاستكشاف الذكية في كاروسيل أفقي لتقليل التمرير الرأسي والعبء البصري)
        if (showSecExplore) {
            item {
                JouriExploreCardsCarousel(
                    viewModel = viewModel,
                    onOpenJouriChat = onOpenJouriChat,
                    onNavigateToTab = onNavigateToTab
                )
            }
        }

        // 🎯 Daily Progress & Briefing Card
        if (showSecVitals) {
            item {
                val waterGoal = viewModel.getWaterTarget()
                val consumedWater = todayWaterLog?.amountMl ?: 0
                val stepGoal = settings?.dailyStepTarget ?: 6000
                val currentSteps = todayStepLog?.steps ?: 0
                val upcomingAppt = appointments
                    .filter { it.dateTime >= System.currentTimeMillis() && !it.completed }
                    .minByOrNull { it.dateTime }

                DailyVitalsSummaryCard(
                    consumedWater = consumedWater,
                    waterGoal = waterGoal,
                    currentSteps = currentSteps,
                    stepGoal = stepGoal,
                    upcomingAppt = upcomingAppt,
                    onNavigateToTab = onNavigateToTab
                )
            }
        }

        // ⚡ Quick Actions & Logging Hub
        if (showSecQuickActions) {
            item {
                val isKickActive = activeStart != null
                PregnancyQuickActionsCard(
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

    // --- Baby Info Dialog (Gender and Name) ---
    if (showBabyInfoDialog) {
        BabyInfoDialog(
            initialGender = babyGenderInput,
            initialName = babyNameInput,
            onDismiss = { showBabyInfoDialog = false },
            onSave = { gender, name ->
                viewModel.updateBabyInfo(gender, name)
                showBabyInfoDialog = false
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
            FetalSizeVisualizerDialog(
                weekNumber = prog.weeks,
                babySizeFruit = prog.comparisonName,
                onDismiss = { showFetalVisualizerDialog = false }
            )
        }
    }
}




