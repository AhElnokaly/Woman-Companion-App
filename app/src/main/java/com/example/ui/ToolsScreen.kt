package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.viewmodel.WomanCompanionViewModel


// --- Tools & Services Screen ---
@Composable
fun ToolsScreen(
    viewModel: WomanCompanionViewModel
) {
    var selectedToolSubScreen by remember { mutableStateOf<String?>(null) }
    
    val deepLinkScreen by viewModel.activeSubScreen.collectAsStateWithLifecycle()
    val pregnancyState by viewModel.pregnancyState.collectAsStateWithLifecycle()
    LaunchedEffect(deepLinkScreen) {
        if (deepLinkScreen != null) {
            selectedToolSubScreen = deepLinkScreen
        }
    }

    val isPregnant = pregnancyState?.isPregnant == true
    val filteredTools = remember(isPregnant) {
        val list = mutableListOf<Triple<String, String, String>>()
        if (isPregnant) {
            list.add(Triple("fetal_growth", "مُتابع نمو الجنين (الوزن والحجم) 📈👶", "تتبع وتحليل وزن وطول طفلكِ ومقارنتهما بالمنحنى الطبيعي للأسبوع"))
            list.add(Triple("fetal_kicks", "عداد ركلات الجنين 👶", "حساب حركة ونشاط طفلك بمؤقت دقيق"))
            list.add(Triple("hospital_bag", "قائمة حقيبة الولادة الذكية 👜👶", "قائمة تفاعلية لتجهيز مستلزمات الأم والمولود والأوراق الرسمية"))
            list.add(Triple("food_safety", "دليل سلامة الأطعمة والمشروبات 🥑🚫", "دليل فوري لمعرفة المسموح والممنوع أثناء الحمل والرضاعة"))
            list.add(Triple("craving", "سجل الوحم والاشتهاء ومشاركة جوري 🍉🍓", "شاركي جوري ما تشتهينه اليوم، وسجلي ذكريات حملك مع تحليل طبي دافئ"))
            list.add(Triple("fitness", "لياقة المرأة الحامل والنفاس 🧘‍♀️💪", "تمارين آمنة ومدروسة بمؤقت تفاعلي للاستشفاء والحفاظ على نشاطك"))
            list.add(Triple("contractions", "مؤقت انقباضات الولادة ⏱️", "تسجيل التقلصات مع كاشف لقاعدة ٥-١-١"))
            list.add(Triple("appointments", "جدول زيارات الدكتورة 🏥", "إدارة وتذكير بمواعيد الكشوف والتحاليل"))
            list.add(Triple("danger", "علامات الخطر التحذيرية ⚠️", "قائمة الأعراض الحرجة التي تستدعي اتصالاً عاجلاً"))
        } else {
            list.add(Triple("smart_conception", "حاسبة التخطيط والحمل الذكي 🎯", "توقع تواريخ الإباضة والولادة برج طفلك المستقبلي"))
            list.add(Triple("contraceptive", "تتبع وسيلة منع الحمل 🛡️", "تسجيل ومتابعة وسيلة منع الحمل الحالية والسابقة والمدة والأعراض المصاحبة"))
            list.add(Triple("qada", "قضاء أيام الصيام 🌙", "عداد أيام الصيام المتبقية عليك لقضائها"))
        }
        
        list.add(Triple("pregnancy_history", "سجل الأحمال والولادات 📜", "تصفح وتتبع سجل جميع الأحمال السابقة والولادات في مكان واحد"))
        list.add(Triple("sleep_analyzer", "محلل ومراقب النوم الذكي 🌙💤", "تتبع وتحليل جودة نومك اليومي وأثره على صحتك الحيوية"))
        list.add(Triple("maonaty", "مؤونتي الذكية لإدارة المنزل 📦🍳", "إدارة مخزون مطبخك، مشترياتك، وصفاتك الذكية، ومهام الترتيب المنزلي"))
        list.add(Triple("home_pharmacy", "الصيدلية المنزلية المتقدمة 💊📦", "تتبع مخزون أدويتك المتبقي وصلاحيتها وتحذيرات الأمان للحمل والرضاعة"))
        list.add(Triple("partner_sync", "رابط الرفيق ومشاركة الشريك 🔗❤️", "ربط زوجك أو عائلتك لمتابعة حالتك ودعمك بعبارات حية ومؤثرة"))
        list.add(Triple("journal", "يوميات مذكراتي الجميلة ✍️", "تدوين مشاعرك ورسائلك للطفل القادم"))
        list.add(Triple("backup_restore", "النسخ الاحتياطي واستعادة البيانات 💾📦", "تصدير أو استيراد كامل بياناتكِ الصحية والطبية بصيغة آمنة دون اتصال"))
        list
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        if (selectedToolSubScreen == null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                item {
                    Text(
                        text = "الأدوات والمساعدة 🌸🛠️",
                        style = MaterialTheme.typography.titleLarge,
                        color = SoftTheme.SoftPink,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "أدوات مخصصة لتتبع الجنين والعبادات وحماية خصوصيتك",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoftTheme.SoftGray
                    )
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleDarkMode() }
                            .testTag("tools_quick_theme_toggle"),
                        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                .size(40.dp)
                                .background(SoftTheme.SoftPink.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (SoftTheme.isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = "تبديل المظهر",
                                    tint = SoftTheme.SoftPink
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (SoftTheme.isDark) "تفعيل المظهر المضيء ☀️" else "تفعيل المظهر الداكن 🌙",
                                    fontWeight = FontWeight.Bold,
                                    color = SoftTheme.TextWhite,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "بدلي مظهر التطبيق بالكامل بلمسة واحدة سريعة",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SoftTheme.SoftGray
                                )
                            }
                        }
                    }
                }

                // Grid list of sub-tools
                items(filteredTools) { (key, title, subtitle) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedToolSubScreen = key }
                            .testTag("tool_card_$key"),
                        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = title, fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = SoftTheme.SoftPink)
                        }
                    }
                }
            }
        } else {
            // Display sub-screen
            Column(modifier = Modifier.fillMaxSize()) {
                IconButton(
                    onClick = { 
                        selectedToolSubScreen = null
                        viewModel.setActiveSubScreen(null)
                    },
                    modifier = Modifier.padding(16.dp).background(SoftTheme.CardSlate, CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = SoftTheme.SoftPink)
                }

                when (selectedToolSubScreen) {
                    "pregnancy_history" -> PregnancyHistoryScreen(viewModel, onBack = { selectedToolSubScreen = null; viewModel.setActiveSubScreen(null) })
                    "maonaty" -> MaonatySubScreen(viewModel)
                    "fitness" -> FitnessScreen(viewModel)
                    "sleep_analyzer" -> SleepAnalyzerScreen(viewModel)
                    "craving" -> CravingScreen(viewModel)
                    "partner_sync" -> PartnerSyncScreen(viewModel)
                    "home_pharmacy" -> HomePharmacyScreen(viewModel)
                    "fetal_kicks" -> FetalKicksSubScreen(viewModel)
                    "fetal_growth" -> FetalGrowthSubScreen(viewModel)
                    "hospital_bag" -> HospitalBagScreen(viewModel)
                    "food_safety" -> FoodSafetyScreen()
                    "contractions" -> ContractionsSubScreen(viewModel)
                    "smart_conception" -> SmartConceptionSubScreen()
                    "contraceptive" -> ContraceptiveScreen(viewModel, onBackClick = { selectedToolSubScreen = null; viewModel.setActiveSubScreen(null) })
                    "qada" -> QadaSubScreen(viewModel)
                    "appointments" -> AppointmentsSubScreen(viewModel)
                    "journal" -> JournalSubScreen(viewModel)
                    "danger" -> DangerSubScreen()
                    "backup_restore" -> BackupRestoreSubScreen(viewModel)
                }
            }
        }
    }
}

// --- Smart Conception and Baby prediction Sub-screen moved to PregnancyScreens.kt ---

// --- Fetal Kicks Sub-screen moved to PregnancyScreens.kt ---

// --- Contractions Sub-screen moved to PregnancyScreens.kt ---

// --- Qada Fast Sub-screen moved to QadaFastScreen.kt ---

// --- Appointments Sub-screen moved to AppointmentsScreen.kt ---

// --- Journal Sub-screen moved to JournalScreen.kt ---

// --- Danger Signs Sub-screen moved to DangerSignalsScreen.kt ---

// --- Settings Screen moved to SettingsScreen.kt ---

// FlowRow wrapper for compatibility since flow layouts are experimental or standard in newer compose versions
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement
    ) {
        content()
    }
}

data class ToolTarget(val buttonText: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val targetPage: Int, val subscreen: String?)

