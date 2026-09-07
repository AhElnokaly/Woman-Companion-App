package com.example

import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AppDatabase
import com.example.data.WomanCompanionRepository
import com.example.ui.*
import com.example.ui.companion.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.WomanCompanionViewModel
import com.example.viewmodel.WomanCompanionViewModelFactory

import android.content.Context
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions

import com.example.R
import com.example.reminder.ReminderScheduler
import com.example.worker.MedicationMonitorWorker

import android.util.Log
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

class MainActivity : ComponentActivity() {

    private val isExactAlarmDeniedState = mutableStateOf(false)

    override fun onResume() {
        super.onResume()
        val isDenied = !ReminderScheduler.canScheduleExact(this)
        isExactAlarmDeniedState.value = isDenied
        if (!isDenied) {
            ReminderScheduler.rescheduleAllReminders(applicationContext)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        RequestMultiplePermissions()
    ) { permissions ->
        val activityGranted = permissions[Manifest.permission.ACTIVITY_RECOGNITION] ?: true
        val postNotificationsGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions[Manifest.permission.POST_NOTIFICATIONS] ?: true
        } else {
            true
        }
        
        // Start the step counting service
        startStepCounterService()
        
        // Also let the ViewModel reinitialize if possible
        try {
            val database = AppDatabase.getDatabase(applicationContext)
            val repository = WomanCompanionRepository(database.womanCompanionDao())
            val factory = WomanCompanionViewModelFactory(application, repository)
            val viewModel = ViewModelProvider(this, factory)[WomanCompanionViewModel::class.java]
            viewModel.reinitializeSensors()
        } catch (e: Exception) {
            com.example.util.AppLogger.w("MainActivity", "Failed to reinitialize sensors on resume", e)
        }
    }

    private fun startStepCounterService() {
        val serviceIntent = Intent(this, com.example.service.StepCounterService::class.java)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        } catch (e: Exception) {
            com.example.util.AppLogger.e("MainActivity", "Failed to start StepCounterService", e)
        }
    }

    private fun setupLocalCrashHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val crashFile = File(filesDir, "last_crash.log")
                val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date())
                val writer = StringWriter()
                throwable.printStackTrace(PrintWriter(writer))
                val logContent = "[$timestamp] Uncaught exception in thread ${thread.name}:\n$writer\n\n"
                crashFile.appendText(logContent)
                Log.e("WomanCompanionCrash", "Recorded local uncaught crash: ${throwable.localizedMessage}")
            } catch (e: Exception) {
                Log.e("WomanCompanionCrash", "Failed to write local crash log: ${e.localizedMessage}")
            } finally {
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupLocalCrashHandler()
        enableEdgeToEdge()

        // Initialize local database, repository and ViewModel factory
        val database = AppDatabase.getDatabase(applicationContext)
        com.example.data.GeminiService.init(applicationContext)
        ReminderScheduler.rescheduleAllReminders(applicationContext)
        MedicationMonitorWorker.enqueuePeriodicWork(applicationContext)
        com.example.worker.WeeklyDigestWorker.enqueuePeriodicWork(applicationContext)
        val repository = WomanCompanionRepository(database.womanCompanionDao())
        val factory = WomanCompanionViewModelFactory(application, repository)
        val viewModel = ViewModelProvider(this, factory)[WomanCompanionViewModel::class.java]

        // Start step counter foreground service
        startStepCounterService()

        // Request Activity Recognition & Post Notifications permissions if needed
        val permissionsToRequest = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }

        setContent {
            val settings by viewModel.appLockSettingsState.collectAsStateWithLifecycle()
            val isDarkTheme = settings?.isDarkMode ?: true

            LaunchedEffect(settings) {
                SoftTheme.isDark = isDarkTheme
            }

            MyApplicationTheme(darkTheme = isDarkTheme, dynamicColor = false) {
                val isLocked by viewModel.isLocked.collectAsStateWithLifecycle()
                val isInitialLoadComplete by viewModel.isInitialLoadComplete.collectAsStateWithLifecycle()
                val pregState by viewModel.pregnancyState.collectAsStateWithLifecycle()
                val companionName = settings?.companionName ?: "جوري"
                
                val isExactAlarmDenied by isExactAlarmDeniedState
                val mainContext = LocalContext.current
                val hasCompletedOnboardingPref = remember {
                    mainContext.getSharedPreferences("woman_companion_prefs", Context.MODE_PRIVATE)
                        .getBoolean("onboarding_completed_v1", false)
                }
                var exactAlarmDismissedUntil by remember {
                    mutableStateOf(
                        mainContext.getSharedPreferences("woman_companion_prefs", Context.MODE_PRIVATE)
                            .getLong("exact_alarm_dismissed_until", 0L)
                    )
                }
                var isExactAlarmPromptDismissed by remember { mutableStateOf(false) }
                val isExactAlarmSuppressed = remember(exactAlarmDismissedUntil) {
                    System.currentTimeMillis() < exactAlarmDismissedUntil
                }

                val tabsList = listOf("dashboard", "period", "nutrition", "symptoms", "tools")
                val pagerState = rememberPagerState(
                    initialPage = 0,
                    pageCount = { tabsList.size }
                )
                val coroutineScope = rememberCoroutineScope()
                var isViewingSettings by remember { mutableStateOf(false) }
                var showJouriChat by remember { mutableStateOf(false) }
                var showNotificationsDialog by remember { mutableStateOf(false) }
                var hasUnreadNotifications by remember { mutableStateOf(true) }

                if (isLocked) {
                    AppLockScreen(viewModel = viewModel, onSuccess = {})
                } else if (!isInitialLoadComplete && !hasCompletedOnboardingPref) {
                    // شاشة البداية الهادئة ريثما يتم التحقق من قاعدة البيانات دون أي وميض لشاشة التهيئة
                    AppSplashScreen()
                } else if (pregState == null || pregState?.isOnboardingCompleted == false) {
                    if (hasCompletedOnboardingPref) {
                        // الملف مسجل بالفعل في التفضيلات ولكن يتم تحميله من قاعدة البيانات
                        AppSplashScreen()
                    } else {
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                            OnboardingScreen(viewModel = viewModel)
                        }
                    }
                } else {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

                        BackHandler(enabled = drawerState.isOpen || showJouriChat || showNotificationsDialog || isViewingSettings || pagerState.currentPage != 0) {
                            if (drawerState.isOpen) {
                                coroutineScope.launch { drawerState.close() }
                            } else if (showJouriChat) {
                                showJouriChat = false
                            } else if (showNotificationsDialog) {
                                showNotificationsDialog = false
                            } else if (isViewingSettings) {
                                isViewingSettings = false
                            } else if (pagerState.currentPage != 0) {
                                coroutineScope.launch { pagerState.animateScrollToPage(0) }
                            }
                        }
                        
                        ModalNavigationDrawer(
                            drawerState = drawerState,
                            drawerContent = {
                                ModalDrawerSheet(
                                    drawerContainerColor = SoftTheme.CardSlate,
                                    drawerContentColor = SoftTheme.TextWhite,
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width(310.dp),
                                    drawerShape = RoundedCornerShape(
                                        topStart = 0.dp,
                                        bottomStart = 0.dp,
                                        topEnd = 24.dp,
                                        bottomEnd = 24.dp
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .verticalScroll(rememberScrollState())
                                    ) {
                                        // 🌟 Drawer Header
                                        val progression = viewModel.getPregnancyProgression()
                                        val phaseInfo = viewModel.getCurrentCyclePhase()
                                        
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    androidx.compose.ui.graphics.Brush.linearGradient(
                                                        colors = listOf(SoftTheme.DeepSlate, SoftTheme.CardSlate)
                                                    )
                                                )
                                                .padding(horizontal = 20.dp, vertical = 24.dp)
                                        ) {
                                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Text(
                                                    text = "جوري رفيقتكِ الذكية 🌸",
                                                    style = MaterialTheme.typography.titleLarge.copy(
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = SoftTheme.SoftPink,
                                                        fontSize = 20.sp
                                                    )
                                                )
                                                Text(
                                                    text = if (!pregState?.motherName.isNullOrEmpty()) {
                                                        "أهلاً بكِ يا ${pregState?.motherName} 💕"
                                                    } else {
                                                        "أهلاً بكِ يا صديقتي الغالية 💕"
                                                    },
                                                    style = MaterialTheme.typography.bodyLarge.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = SoftTheme.TextWhite
                                                    )
                                                )
                                                
                                                // Current Stage Summary
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(SoftTheme.SoftPink.copy(alpha = 0.15f))
                                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                                ) {
                                                    Text(
                                                        text = if (pregState?.isPregnant == true) "🤰" else "🩸",
                                                        fontSize = 16.sp
                                                    )
                                                    Text(
                                                        text = if (pregState?.isPregnant == true) {
                                                            "الأسبوع الـ ${progression?.weeks ?: 1} • ${progression?.comparisonName ?: "نمو مستمر"}"
                                                        } else {
                                                            "طور: ${phaseInfo.phaseArabic}"
                                                        },
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = SoftTheme.LightPink,
                                                            fontSize = 11.sp
                                                        )
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))
                                        
                                        // 📂 Navigation Section 1: Core Navigation
                                        Text(
                                            text = "المتابعة اليومية",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = SoftTheme.SoftPink,
                                                fontSize = 12.sp
                                            ),
                                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                                        )

                                        val coreTabs = listOf(
                                            Triple("الرئيسية والمؤشرات", Icons.Default.Home, 0),
                                            Triple(if (pregState?.isPregnant == true) "رحلة الحمل المبارك" else "الدورة والخصوبة", if (pregState?.isPregnant == true) Icons.Default.Favorite else Icons.Default.DateRange, 1),
                                            Triple("الغذاء والمياه الصحية", Icons.Default.LocalCafe, 2),
                                            Triple("الأعراض والروتين الطبي", Icons.Default.Thermostat, 3)
                                        )

                                        coreTabs.forEach { (title, icon, index) ->
                                            NavigationDrawerItem(
                                                icon = { Icon(icon, contentDescription = null, tint = if (pagerState.currentPage == index) SoftTheme.SoftPink else SoftTheme.SoftGray) },
                                                label = { Text(title, fontWeight = FontWeight.Bold, color = if (pagerState.currentPage == index) SoftTheme.TextWhite else SoftTheme.TextWhite.copy(alpha = 0.8f)) },
                                                selected = pagerState.currentPage == index,
                                                onClick = {
                                                    coroutineScope.launch {
                                                        drawerState.close()
                                                        pagerState.animateScrollToPage(index)
                                                    }
                                                },
                                                colors = NavigationDrawerItemDefaults.colors(
                                                    selectedContainerColor = SoftTheme.SoftPink.copy(alpha = 0.2f),
                                                    unselectedContainerColor = androidx.compose.ui.graphics.Color.Transparent
                                                ),
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                                            )
                                        }

                                        HorizontalDivider(
                                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                                            color = SoftTheme.SoftGray.copy(alpha = 0.2f)
                                        )

                                        // 🛠️ Navigation Section 2: Smart Helper Tools
                                        val isPregnant = pregState?.isPregnant == true

                                        val pregnancyTools = listOf(
                                            Triple("📈 متابع نمو الجنين والوزن", "fetal_growth", Icons.Default.Info),
                                            Triple("👶 عداد ركلات الجنين", "fetal_kicks", Icons.Default.Favorite),
                                            Triple("🍉 سجل الوحم والاشتهاء", "craving", Icons.Default.FavoriteBorder),
                                            Triple("🧘‍♀️ لياقة الحمل والنفاس", "fitness", Icons.Default.Star),
                                            Triple("⏱️ مؤقت انقباضات الولادة", "contractions", Icons.Default.PlayArrow),
                                            Triple("🏥 جدول زيارات الطبيبة", "appointments", Icons.Default.DateRange)
                                        )

                                        val cycleTools = listOf(
                                            Triple("🎯 حاسبة التخطيط والحمل الذكي", "smart_conception", Icons.Default.DateRange),
                                            Triple("🌙 قضاء أيام الصيام", "qada", Icons.Default.Star)
                                        )

                                        val generalTools = listOf(
                                            Triple("🌙 مراقب ومحلل النوم", "sleep_analyzer", Icons.Default.Notifications),
                                            Triple("📦 مؤونتي الذكية والمطبخ", "maonaty", Icons.Default.Home),
                                            Triple("📦 الصيدلية المنزلية المتقدمة", "home_pharmacy", Icons.AutoMirrored.Filled.List),
                                            Triple("🔗 مزامنة الرفيق والزوج", "partner_sync", Icons.Default.Share),
                                            Triple("✍️ مذكراتي واليوميات الجميلة", "journal", Icons.Default.Edit)
                                        )

                                        if (isPregnant) {
                                            Text(
                                                text = "أدوات الحمل والمتابعة الذكية 🤰",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = SoftTheme.MintTeal,
                                                    fontSize = 12.sp
                                                ),
                                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                                            )

                                            pregnancyTools.forEach { (title, key, icon) ->
                                                NavigationDrawerItem(
                                                    icon = { Icon(icon, contentDescription = null, tint = SoftTheme.SoftGray) },
                                                    label = { Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = SoftTheme.TextWhite.copy(alpha = 0.9f)) },
                                                    selected = false,
                                                    onClick = {
                                                        coroutineScope.launch {
                                                            drawerState.close()
                                                            viewModel.setActiveSubScreen(key)
                                                            pagerState.animateScrollToPage(4) // Tools Tab
                                                        }
                                                    },
                                                    colors = NavigationDrawerItemDefaults.colors(
                                                        selectedContainerColor = SoftTheme.SoftPink.copy(alpha = 0.2f),
                                                        unselectedContainerColor = androidx.compose.ui.graphics.Color.Transparent
                                                    ),
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 1.dp)
                                                )
                                            }
                                        } else {
                                            Text(
                                                text = "أدوات الدورة والخصوبة والعبادات 🩸",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = SoftTheme.MintTeal,
                                                    fontSize = 12.sp
                                                ),
                                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                                            )

                                            cycleTools.forEach { (title, key, icon) ->
                                                NavigationDrawerItem(
                                                    icon = { Icon(icon, contentDescription = null, tint = SoftTheme.SoftGray) },
                                                    label = { Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = SoftTheme.TextWhite.copy(alpha = 0.9f)) },
                                                    selected = false,
                                                    onClick = {
                                                        coroutineScope.launch {
                                                            drawerState.close()
                                                            viewModel.setActiveSubScreen(key)
                                                            pagerState.animateScrollToPage(4) // Tools Tab
                                                        }
                                                    },
                                                    colors = NavigationDrawerItemDefaults.colors(
                                                        selectedContainerColor = SoftTheme.SoftPink.copy(alpha = 0.2f),
                                                        unselectedContainerColor = androidx.compose.ui.graphics.Color.Transparent
                                                    ),
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 1.dp)
                                                )
                                            }
                                        }

                                        HorizontalDivider(
                                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                            color = SoftTheme.SoftGray.copy(alpha = 0.15f)
                                        )

                                        Text(
                                            text = "الأدوات العامة والخدمية 🛠️",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = SoftTheme.SoftPink,
                                                fontSize = 12.sp
                                            ),
                                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                                        )

                                        generalTools.forEach { (title, key, icon) ->
                                            NavigationDrawerItem(
                                                icon = { Icon(icon, contentDescription = null, tint = SoftTheme.SoftGray) },
                                                label = { Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = SoftTheme.TextWhite.copy(alpha = 0.9f)) },
                                                selected = false,
                                                onClick = {
                                                    coroutineScope.launch {
                                                        drawerState.close()
                                                        viewModel.setActiveSubScreen(key)
                                                        pagerState.animateScrollToPage(4) // Tools Tab
                                                    }
                                                },
                                                colors = NavigationDrawerItemDefaults.colors(
                                                    selectedContainerColor = SoftTheme.SoftPink.copy(alpha = 0.2f),
                                                    unselectedContainerColor = androidx.compose.ui.graphics.Color.Transparent
                                                ),
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 1.dp)
                                            )
                                        }

                                        HorizontalDivider(
                                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                                            color = SoftTheme.SoftGray.copy(alpha = 0.2f)
                                        )

                                        // ⚙️ Navigation Section 3: App Control
                                        Text(
                                            text = "إعدادات ودعم",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = SoftTheme.SoftGray,
                                                fontSize = 12.sp
                                            ),
                                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                                        )

                                        NavigationDrawerItem(
                                            icon = { Icon(Icons.Default.Settings, contentDescription = null, tint = SoftTheme.SoftGray) },
                                            label = { Text("إعدادات التطبيق والملف الشخصي", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite) },
                                            selected = isViewingSettings,
                                            onClick = {
                                                coroutineScope.launch {
                                                    drawerState.close()
                                                    isViewingSettings = true
                                                }
                                            },
                                            colors = NavigationDrawerItemDefaults.colors(
                                                selectedContainerColor = SoftTheme.SoftPink.copy(alpha = 0.2f),
                                                unselectedContainerColor = androidx.compose.ui.graphics.Color.Transparent
                                            ),
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                                        )

                                        NavigationDrawerItem(
                                            icon = { Text("🌸", fontSize = 18.sp, modifier = Modifier.padding(horizontal = 2.dp)) },
                                            label = { Text("الدردشة مع جوري الذكية", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite) },
                                            selected = false,
                                            onClick = {
                                                coroutineScope.launch {
                                                    drawerState.close()
                                                    showJouriChat = true
                                                }
                                            },
                                            colors = NavigationDrawerItemDefaults.colors(
                                                selectedContainerColor = SoftTheme.SoftPink.copy(alpha = 0.2f),
                                                unselectedContainerColor = androidx.compose.ui.graphics.Color.Transparent
                                            ),
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                                        )

                                        Spacer(modifier = Modifier.height(30.dp))
                                    }
                                }
                            }
                        ) {
                            Scaffold(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(SoftTheme.BackgroundBrush),
                                containerColor = MaterialTheme.colorScheme.background,
                                topBar = {
                                    if (!isViewingSettings) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .statusBarsPadding()
                                                .background(SoftTheme.CardSlate)
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    coroutineScope.launch {
                                                        if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                                    }
                                                },
                                                modifier = Modifier.testTag("hamburger_menu_button")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Menu,
                                                    contentDescription = "القائمة الجانبية",
                                                    tint = SoftTheme.SoftPink
                                                )
                                            }
                                            
                                            Text(
                                                text = when (pagerState.currentPage) {
                                                    0 -> "الرئيسية 🌸"
                                                    1 -> if (pregState?.isPregnant == true) "تتبع الحمل المبارك 👶" else "الدورة والخصوبة 🩸"
                                                    2 -> "الغذاء والمياه الصحّية 🥑"
                                                    3 -> "الأعراض والروتين الطبي 🩺"
                                                    4 -> "الأدوات الذكية المساعدة 🛠️"
                                                    else -> "جوري"
                                                },
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 18.sp,
                                                    color = SoftTheme.TextWhite
                                                )
                                            )
                                            
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Box {
                                                    IconButton(
                                                        onClick = { 
                                                            showNotificationsDialog = true 
                                                            hasUnreadNotifications = false
                                                        },
                                                        modifier = Modifier.testTag("top_notifications_button")
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Notifications,
                                                            contentDescription = "التنبيهات والميزات الجديدة",
                                                            tint = SoftTheme.SoftPink
                                                        )
                                                    }
                                                    if (hasUnreadNotifications) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(9.dp)
                                                                .background(androidx.compose.ui.graphics.Color(0xFFE91E63), CircleShape)
                                                                .align(Alignment.TopEnd)
                                                                .offset(x = (-4).dp, y = (4).dp)
                                                        )
                                                    }
                                                }

                                                IconButton(
                                                    onClick = { showJouriChat = true },
                                                    modifier = Modifier.testTag("top_chat_jouri_button")
                                                ) {
                                                    Text("🌸", fontSize = 22.sp)
                                                }
                                            }
                                        }
                                    }
                                },
                                floatingActionButton = {
                                    if (!isViewingSettings) {
                                        FloatingActionButton(
                                            onClick = { showJouriChat = true },
                                            containerColor = SoftTheme.SoftPink,
                                            contentColor = SoftTheme.TextWhite,
                                            shape = RoundedCornerShape(24.dp),
                                            modifier = Modifier.testTag("jouri_fab")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text("🌸", fontSize = 20.sp)
                                                Text("$companionName صديقتكِ الذكية", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
                                            }
                                        }
                                    }
                                },
                                bottomBar = {
                                    if (!isViewingSettings) {
                                        NavigationBar(
                                            modifier = Modifier.testTag("main_navigation_bar"),
                                            containerColor = SoftTheme.CardSlate,
                                            tonalElevation = 8.dp
                                        ) {
                                            NavigationBarItem(
                                                selected = pagerState.currentPage == 0,
                                                onClick = {
                                                    coroutineScope.launch {
                                                        pagerState.animateScrollToPage(0)
                                                    }
                                                },
                                                icon = { Icon(Icons.Default.Home, contentDescription = "الرئيسية") },
                                                label = { Text("الرئيسية") },
                                                colors = NavigationBarItemDefaults.colors(
                                                    selectedIconColor = SoftTheme.DeepSlate,
                                                    selectedTextColor = SoftTheme.SoftPink,
                                                    indicatorColor = SoftTheme.SoftPink,
                                                    unselectedIconColor = SoftTheme.SoftGray,
                                                    unselectedTextColor = SoftTheme.SoftGray
                                                ),
                                                modifier = Modifier.testTag("tab_dashboard")
                                            )

                                            NavigationBarItem(
                                                selected = pagerState.currentPage == 1,
                                                onClick = {
                                                    coroutineScope.launch {
                                                        pagerState.animateScrollToPage(1)
                                                    }
                                                },
                                                icon = { 
                                                    Icon(
                                                        imageVector = if (pregState?.isPregnant == true) Icons.Default.Favorite else Icons.Default.DateRange, 
                                                        contentDescription = if (pregState?.isPregnant == true) "الحمل" else "الدورة والخصوبة"
                                                    ) 
                                                },
                                                label = { Text(if (pregState?.isPregnant == true) "الحمل" else "الدورة") },
                                                colors = NavigationBarItemDefaults.colors(
                                                    selectedIconColor = SoftTheme.DeepSlate,
                                                    selectedTextColor = SoftTheme.SoftPink,
                                                    indicatorColor = SoftTheme.SoftPink,
                                                    unselectedIconColor = SoftTheme.SoftGray,
                                                    unselectedTextColor = SoftTheme.SoftGray
                                                ),
                                                modifier = Modifier.testTag("tab_period")
                                            )

                                            NavigationBarItem(
                                                selected = pagerState.currentPage == 2,
                                                onClick = {
                                                    coroutineScope.launch {
                                                        pagerState.animateScrollToPage(2)
                                                    }
                                                },
                                                icon = { Icon(Icons.Default.LocalCafe, contentDescription = "الغذاء والماء") },
                                                label = { Text("الغذاء") },
                                                colors = NavigationBarItemDefaults.colors(
                                                    selectedIconColor = SoftTheme.DeepSlate,
                                                    selectedTextColor = SoftTheme.SoftPink,
                                                    indicatorColor = SoftTheme.SoftPink,
                                                    unselectedIconColor = SoftTheme.SoftGray,
                                                    unselectedTextColor = SoftTheme.SoftGray
                                                ),
                                                modifier = Modifier.testTag("tab_nutrition")
                                            )

                                            NavigationBarItem(
                                                selected = pagerState.currentPage == 3,
                                                onClick = {
                                                    coroutineScope.launch {
                                                        pagerState.animateScrollToPage(3)
                                                    }
                                                },
                                                icon = { Icon(Icons.Default.Thermostat, contentDescription = "الأعراض والأدوية") },
                                                label = { Text("الأعراض") },
                                                colors = NavigationBarItemDefaults.colors(
                                                    selectedIconColor = SoftTheme.DeepSlate,
                                                    selectedTextColor = SoftTheme.SoftPink,
                                                    indicatorColor = SoftTheme.SoftPink,
                                                    unselectedIconColor = SoftTheme.SoftGray,
                                                    unselectedTextColor = SoftTheme.SoftGray
                                                ),
                                                modifier = Modifier.testTag("tab_symptoms")
                                            )

                                            NavigationBarItem(
                                                selected = pagerState.currentPage == 4,
                                                onClick = {
                                                    coroutineScope.launch {
                                                        pagerState.animateScrollToPage(4)
                                                    }
                                                },
                                                icon = { Icon(Icons.Default.Build, contentDescription = "الأدوات والمساعدة") },
                                                label = { Text("الأدوات") },
                                                colors = NavigationBarItemDefaults.colors(
                                                    selectedIconColor = SoftTheme.DeepSlate,
                                                    selectedTextColor = SoftTheme.SoftPink,
                                                    indicatorColor = SoftTheme.SoftPink,
                                                    unselectedIconColor = SoftTheme.SoftGray,
                                                    unselectedTextColor = SoftTheme.SoftGray
                                                ),
                                                modifier = Modifier.testTag("tab_tools")
                                            )
                                        }
                                    }
                                }
                            ) { innerPadding ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(SoftTheme.BackgroundBrush)
                                        .padding(innerPadding)
                                ) {
                                    if (isExactAlarmDenied && !isExactAlarmPromptDismissed && !isExactAlarmSuppressed) {
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFF822727)),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                                .testTag("exact_alarm_permission_card")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = stringResource(R.string.exact_alarm_permission_title),
                                                        fontWeight = FontWeight.Bold,
                                                        color = androidx.compose.ui.graphics.Color.White,
                                                        fontSize = 13.sp
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = stringResource(R.string.exact_alarm_permission_desc),
                                                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f),
                                                        fontSize = 11.sp,
                                                        lineHeight = 15.sp
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Button(
                                                    onClick = {
                                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                                            try {
                                                                val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                                                    data = android.net.Uri.parse("package:$packageName")
                                                                }
                                                                startActivity(intent)
                                                            } catch (e: Exception) {
                                                                val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                                                startActivity(intent)
                                                            }
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color.White),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text(stringResource(R.string.exact_alarm_permission_button), color = androidx.compose.ui.graphics.Color(0xFF822727), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                                TextButton(
                                                    onClick = {
                                                        isExactAlarmPromptDismissed = true
                                                        val snoozeTime = System.currentTimeMillis() + (3L * 24 * 60 * 60 * 1000)
                                                        mainContext.getSharedPreferences("woman_companion_prefs", Context.MODE_PRIVATE)
                                                            .edit()
                                                            .putLong("exact_alarm_dismissed_until", snoozeTime)
                                                            .apply()
                                                        exactAlarmDismissedUntil = snoozeTime
                                                    },
                                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                                ) {
                                                    Text("تذكرني بعدين", color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f), fontSize = 10.sp)
                                                }
                                                IconButton(
                                                    onClick = {
                                                        isExactAlarmPromptDismissed = true
                                                        val snoozeTime = System.currentTimeMillis() + (3L * 24 * 60 * 60 * 1000)
                                                        mainContext.getSharedPreferences("woman_companion_prefs", Context.MODE_PRIVATE)
                                                            .edit()
                                                            .putLong("exact_alarm_dismissed_until", snoozeTime)
                                                            .apply()
                                                        exactAlarmDismissedUntil = snoozeTime
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close_action), tint = androidx.compose.ui.graphics.Color.White)
                                                }
                                            }
                                        }
                                    }

                                    Box(modifier = Modifier.weight(1f)) {
                                        if (isViewingSettings) {
                                        SettingsScreen(
                                            viewModel = viewModel,
                                            onNavigateBack = { isViewingSettings = false }
                                        )
                                    } else {
                                        HorizontalPager(
                                            state = pagerState,
                                            modifier = Modifier.fillMaxSize(),
                                            userScrollEnabled = true
                                        ) { page ->
                                            when (tabsList[page]) {
                                                "dashboard" -> PregnancyDashboardScreen(
                                                    viewModel = viewModel,
                                                    onNavigateToSettings = { isViewingSettings = true },
                                                    onOpenJouriChat = { showJouriChat = true },
                                                    onNavigateToTab = { targetPage ->
                                                        coroutineScope.launch {
                                                            pagerState.animateScrollToPage(targetPage)
                                                        }
                                                    }
                                                )
                                                "period" -> PeriodTrackerScreen(
                                                    viewModel = viewModel,
                                                    onNavigateToTab = { targetPage ->
                                                        coroutineScope.launch {
                                                            pagerState.animateScrollToPage(targetPage)
                                                        }
                                                    }
                                                )
                                                "nutrition" -> NutritionAndWaterScreen(viewModel = viewModel)
                                                "symptoms" -> SymptomAndMedsScreen(viewModel = viewModel)
                                                "tools" -> ToolsScreen(viewModel = viewModel)
                                            }
                                        }
                                    }

                                    if (showJouriChat) {
                                        JouriChatDialog(
                                            viewModel = viewModel,
                                            onDismiss = { showJouriChat = false },
                                            onNavigateToTab = { page ->
                                                coroutineScope.launch {
                                                    pagerState.animateScrollToPage(page)
                                                }
                                            }
                                        )
                                    }

                                    if (showNotificationsDialog) {
                                        JouriNotificationsDialog(
                                            viewModel = viewModel,
                                            onDismiss = { showNotificationsDialog = false },
                                            onNavigateToTab = { page ->
                                                coroutineScope.launch {
                                                    pagerState.animateScrollToPage(page)
                                                }
                                            }
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
}
}
