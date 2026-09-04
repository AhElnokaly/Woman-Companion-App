package com.example.ui.companion

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.SoftTheme
import com.example.viewmodel.WomanCompanionViewModel

// ==========================================
// 4. بانر ونافذة التحديثات والتنبيهات الذكية (Updates Banner & Smart Notifications)
// ==========================================
@Composable
fun NewFeaturesUpdatesBanner(
    viewModel: WomanCompanionViewModel,
    onNavigateToTab: (Int) -> Unit
) {
    val isDismissed by viewModel.isUpdatesBannerDismissed.collectAsState()

    if (isDismissed) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("🚀", fontSize = 24.sp)
                    Column {
                        Text(
                            text = "التحديثات والميزات الذكية الجديدة 🌟",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.SoftPink
                        )
                        Text(
                            text = "اكتشفي ميزات صديقتكِ جوري المحدثة لراحتكِ",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTheme.SoftGray
                        )
                    }
                }
                IconButton(
                    onClick = {
                        viewModel.dismissUpdatesBanner()
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق التنبيه",
                        tint = SoftTheme.SoftGray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Feature 1: Craving
                NewFeatureItemRow(
                    emoji = "🍓",
                    title = "سجل الوحم والاشتهاء ومشاركة جوري",
                    description = "تتبعي رغباتكِ اليومية والوحم، واحصلي على تحليل طبي دافئ، وشاركي مشاعركِ اللطيفة كرسالة جاهزة لزوجكِ! 🥰🍉",
                    onOpen = {
                        viewModel.setActiveSubScreen("craving")
                        onNavigateToTab(4)
                    }
                )

                HorizontalDivider(color = SoftTheme.DeepSlate.copy(alpha = 0.5f), thickness = 0.5.dp)

                // Feature 2: Sleep
                NewFeatureItemRow(
                    emoji = "💤",
                    title = "محلل ومراقب النوم الذكي والأرق",
                    description = "سجلي ساعات نومكِ وجودته، ودعي جوري تحلل لكِ عادات نومكِ وتقدم لكِ نصائح ذهبية لنوم هانئ ومريح. 🌙💤",
                    onOpen = {
                        viewModel.setActiveSubScreen("sleep_analyzer")
                        onNavigateToTab(4)
                    }
                )

                HorizontalDivider(color = SoftTheme.DeepSlate.copy(alpha = 0.5f), thickness = 0.5.dp)

                // Feature 3: Partner
                NewFeatureItemRow(
                    emoji = "🔗",
                    title = "رابط الرفيق ومشاركة الشريك",
                    description = "أشركي زوجكِ أو عائلتكِ في رحلتكِ واستقبلي رسائل الدعم والاهتمام بخصوصية تامة داخل التطبيق. ❤️✨",
                    onOpen = {
                        viewModel.setActiveSubScreen("partner_sync")
                        onNavigateToTab(4)
                    }
                )

                HorizontalDivider(color = SoftTheme.DeepSlate.copy(alpha = 0.5f), thickness = 0.5.dp)

                // Feature 4: Pharmacy
                NewFeatureItemRow(
                    emoji = "📦",
                    title = "الصيدلية المنزلية المتقدمة وتتبع الأدوية",
                    description = "سجلي علب أدويتكِ وصلاحيتها ومخزونها المتبقي لتنبيهكِ قبل النفاد ولتجنب استخدام أدوية منتهية الصلاحية. 💊📦",
                    onOpen = {
                        viewModel.setActiveSubScreen("home_pharmacy")
                        onNavigateToTab(4)
                    }
                )

                HorizontalDivider(color = SoftTheme.DeepSlate.copy(alpha = 0.5f), thickness = 0.5.dp)

                // Feature 5: Fitness
                NewFeatureItemRow(
                    emoji = "🧘‍♀️",
                    title = "تمارين لياقة المرأة الحامل والنفاس",
                    description = "تمارين رياضية آمنة مخصصة لثلث حملكِ أو فترة النفاس مع مؤقت ذكي لمساعدتكِ على أداء الحركة بنشاط وصحة. 🧘‍♀️💪",
                    onOpen = {
                        viewModel.setActiveSubScreen("fitness")
                        onNavigateToTab(4)
                    }
                )
            }
        }
    }
}

@Composable
fun NewFeatureItemRow(
    emoji: String,
    title: String,
    description: String,
    onOpen: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate.copy(alpha = 0.25f)),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.TextWhite
                    ),
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = SoftTheme.SoftGray,
                        lineHeight = 16.sp
                    ),
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onOpen,
                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.MintTeal.copy(alpha = 0.18f)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "جربي الأداة الآن ⚡",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SoftTheme.MintTeal,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(SoftTheme.SoftPink.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 22.sp)
            }
        }
    }
}

data class SmartAlertItem(
    val emoji: String,
    val title: String,
    val description: String,
    val actionText: String,
    val action: () -> Int
)

@Composable
fun SmartAlertCard(
    item: SmartAlertItem,
    onNavigate: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.SoftPink
                        ),
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTheme.TextWhite.copy(alpha = 0.9f),
                        lineHeight = 16.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(SoftTheme.SoftPink.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(item.emoji, fontSize = 22.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { onNavigate(item.action()) },
                colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.MintTeal.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier
                    .align(Alignment.Start)
                    .height(34.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = item.actionText,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.MintTeal
                        )
                    )
                    Text("⚡", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun JouriNotificationsDialog(
    viewModel: WomanCompanionViewModel,
    onDismiss: () -> Unit,
    onNavigateToTab: (Int) -> Unit
) {
    val pregnancyState by viewModel.pregnancyState.collectAsStateWithLifecycle()
    val progression = viewModel.getPregnancyProgression()
    val cycleInfo = viewModel.getCurrentCyclePhase()

    var selectedTab by remember { mutableStateOf(0) } // 0 for Features, 1 for Smart Alerts

    val alerts = remember(pregnancyState, progression, cycleInfo) {
        val list = mutableListOf<SmartAlertItem>()
        
        val preg = pregnancyState
        if (preg != null) {
            // 1. Core State Advice
            if (preg.isPregnant) {
                list.add(
                    SmartAlertItem(
                        emoji = "👶🏻",
                        title = "متابعة الحمل - الأسبوع ${progression?.weeks ?: 12}",
                        description = "أنتِ الآن في الأسبوع ${progression?.weeks ?: 12} من الحمل. طفلكِ الجميل الآن بحجم ${progression?.comparisonName ?: "حبة تين كاملة"} ${progression?.comparisonIcon ?: "🫓"}. تذكري شرب مياه كافية والراحة والترطيب المستمر لسلامتكما اليوم 🌸",
                        actionText = "تفاصيل تقدم الحمل ⚡",
                        action = { 0 }
                    )
                )
            } else {
                list.add(
                    SmartAlertItem(
                        emoji = "🩸",
                        title = "حالة دورتكِ الحالية: ${cycleInfo.phaseArabic}",
                        description = cycleInfo.description,
                        actionText = "سجل الدورة والخصوبة 📅",
                        action = { 1 }
                    )
                )
            }
            
            // 2. High Blood Pressure Alert
            if (preg.hasHighBp) {
                list.add(
                    SmartAlertItem(
                        emoji = "🩺",
                        title = "مراقبة هامة: ضغط الدم المرتفع ⚠️",
                        description = "عزيزتي جميلة، سجلكِ يشير لضغط دم مرتفع. ننصحكِ بقياس الضغط الآن، والابتعاد التام عن الأطعمة الغنية بالصوديوم (الأملاح) مع شرب الكركديه البارد والراحة التامة.",
                        actionText = "سجل الأعراض والضغط 🩺",
                        action = { 3 }
                    )
                )
            }
            
            // 3. Low Blood Pressure Alert
            if (preg.hasLowBp) {
                list.add(
                    SmartAlertItem(
                        emoji = "🥤",
                        title = "تنبيه صحي: ضغط دم منخفض",
                        description = "عزيزتي جميلة، ننصحكِ بشرب كميات كافية من المياه لتعويض السوائل، والنهوض تدريجياً وتجنب الوقوف لفترات طويلة لتفادي أي دوار ونقص تدفق الأكسجين.",
                        actionText = "سجل الأعراض 🩺",
                        action = { 3 }
                    )
                )
            }
            
            // 4. Diabetes Alert
            if (preg.hasDiabetes) {
                list.add(
                    SmartAlertItem(
                        emoji = "🍏",
                        title = "تنبيه طبي: السكري الذكي 📈",
                        description = "تذكري قياس السكر بعد الوجبات بساعتين وتجنب السكريات البسيطة والمشروبات الغازية. استبدليها بوجبات خفيفة غنية بالألياف كالخضار الطازجة وحبة تفاح.",
                        actionText = "روتين الغذاء والتغذية 🥑",
                        action = { 2 }
                    )
                )
            }
        }
        
        // 5. Daily Wellness & Deep Breathing Reminder
        list.add(
            SmartAlertItem(
                emoji = "🧘‍♀️",
                title = "جلسة تنفس عميق وصحة نفسية ✨",
                description = "أثبتت الدراسات أن 3 دقائق من التنفس العميق والواعي تخفض هرمون الكورتيسول (الإجهاد) وتزيد تدفق الأكسجين لطفلكِ بشكل فوري وفعّال.",
                actionText = "ابدئي تمارين اللياقة 🧘‍♀️",
                action = { 
                    viewModel.setActiveSubScreen("fitness")
                    4 
                }
            )
        )

        // 6. Water hydration Alert
        list.add(
            SmartAlertItem(
                emoji = "💧",
                title = "تذكير الترطيب الذكي من جوري",
                description = "شرب 8-10 أكواب من المياه يومياً يحميكِ من التعب وصداع الحمل ويساعد في تجديد السائل الأمنيوسي المحيط بالطفل باستمرار.",
                actionText = "سجّلي شرب كوب ماء 🥤",
                action = { 2 }
            )
        )
        
        list
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.75f))
                .clickable { onDismiss() }
                .padding(horizontal = 16.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
                    .clickable(enabled = false) {},
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .background(SoftTheme.DeepSlate.copy(alpha = 0.5f), CircleShape)
                                .size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "إغلاق التنبيهات",
                                tint = SoftTheme.SoftPink,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "مركز التنبيهات الذكي من جوري 🔔",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = SoftTheme.SoftPink
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tab Selector Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SoftTheme.DeepSlate.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selectedTab == 0) SoftTheme.SoftPink else Color.Transparent)
                                .clickable { selectedTab = 0 }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "ميزات جوري الذكية 🌟",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedTab == 0) SoftTheme.TextWhite else SoftTheme.SoftGray
                                )
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selectedTab == 1) SoftTheme.SoftPink else Color.Transparent)
                                .clickable { selectedTab = 1 }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "تنبيهاتكِ الصحية 🔔",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedTab == 1) SoftTheme.TextWhite else SoftTheme.SoftGray
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Content Scrollable Area
                    Box(modifier = Modifier.weight(1f)) {
                        if (selectedTab == 0) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Feature 1: Craving
                                NewFeatureItemRow(
                                    emoji = "🍓",
                                    title = "سجل الوحم والاشتهاء ومشاركة جوري",
                                    description = "تتبعي رغباتكِ اليومية والوحم، واحصلي على تحليل طبي دافئ، وشاركي مشاعركِ اللطيفة كرسالة جاهزة لزوجكِ! 🥰🍉",
                                    onOpen = {
                                        viewModel.setActiveSubScreen("craving")
                                        onNavigateToTab(4)
                                        onDismiss()
                                    }
                                )

                                HorizontalDivider(color = SoftTheme.DeepSlate.copy(alpha = 0.5f), thickness = 0.5.dp)

                                // Feature 2: Sleep
                                NewFeatureItemRow(
                                    emoji = "💤",
                                    title = "محلل ومراقب النوم الذكي والأرق",
                                    description = "سجلي ساعات نومكِ وجودته، ودعي جوري تحلل لكِ عادات نومكِ وتقدم لكِ نصائح ذهبية لنوم هانئ ومريح. 🌙💤",
                                    onOpen = {
                                        viewModel.setActiveSubScreen("sleep_analyzer")
                                        onNavigateToTab(4)
                                        onDismiss()
                                    }
                                )

                                HorizontalDivider(color = SoftTheme.DeepSlate.copy(alpha = 0.5f), thickness = 0.5.dp)

                                // Feature 3: Partner
                                NewFeatureItemRow(
                                    emoji = "🔗",
                                    title = "رابط الرفيق ومشاركة الشريك",
                                    description = "أشركي زوجكِ أو عائلتكِ في رحلتكِ واستقبلي رسائل الدعم والاهتمام بخصوصية تامة داخل التطبيق. ❤️✨",
                                    onOpen = {
                                        viewModel.setActiveSubScreen("partner_sync")
                                        onNavigateToTab(4)
                                        onDismiss()
                                    }
                                )

                                HorizontalDivider(color = SoftTheme.DeepSlate.copy(alpha = 0.5f), thickness = 0.5.dp)

                                // Feature 4: Pharmacy
                                NewFeatureItemRow(
                                    emoji = "📦",
                                    title = "الصيدلية المنزلية المتقدمة وتتبع الأدوية",
                                    description = "سجلي علب أدويتكِ وصلاحيتها ومخزونها المتبقي لتنبيهكِ قبل النفاد ولتجنب استخدام أدوية منتهية الصلاحية. 💊📦",
                                    onOpen = {
                                        viewModel.setActiveSubScreen("home_pharmacy")
                                        onNavigateToTab(4)
                                        onDismiss()
                                    }
                                )

                                HorizontalDivider(color = SoftTheme.DeepSlate.copy(alpha = 0.5f), thickness = 0.5.dp)

                                // Feature 5: Fitness
                                NewFeatureItemRow(
                                    emoji = "🧘‍♀️",
                                    title = "تمارين لياقة المرأة الحامل والنفاس",
                                    description = "تمارين رياضية آمنة مخصصة لثلث حملكِ أو فترة النفاس مع مؤقت ذكي لمساعدتكِ على أداء الحركة بنشاط وصحة. 🧘‍♀️💪",
                                    onOpen = {
                                        viewModel.setActiveSubScreen("fitness")
                                        onNavigateToTab(4)
                                        onDismiss()
                                    }
                                )

                                HorizontalDivider(color = SoftTheme.DeepSlate.copy(alpha = 0.5f), thickness = 0.5.dp)

                                // Feature 6: Hospital Bag
                                NewFeatureItemRow(
                                    emoji = "👜",
                                    title = "قائمة حقيبة الولادة الذكية",
                                    description = "قائمة تفاعلية ذكية لحزم وتجهيز كل ما تحتاجينه أنتِ ومولودكِ والمستندات قبل الذهاب للمستشفى. 👜👶",
                                    onOpen = {
                                        viewModel.setActiveSubScreen("hospital_bag")
                                        onNavigateToTab(4)
                                        onDismiss()
                                    }
                                )

                                HorizontalDivider(color = SoftTheme.DeepSlate.copy(alpha = 0.5f), thickness = 0.5.dp)

                                // Feature 7: Food Safety
                                NewFeatureItemRow(
                                    emoji = "🥑",
                                    title = "دليل سلامة الأغذية أثناء الحمل",
                                    description = "ابحثي عن أي طعام، شراب، أو عشبة وتعرفي فوراً على درجة أمانه (آمن، بحذر، أو ممنوع) حفاظاً على سلامتكِ. 🥑🚫",
                                    onOpen = {
                                        viewModel.setActiveSubScreen("food_safety")
                                        onNavigateToTab(4)
                                        onDismiss()
                                    }
                                )
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                alerts.forEach { item ->
                                    SmartAlertCard(
                                        item = item,
                                        onNavigate = { tabIndex ->
                                            onNavigateToTab(tabIndex)
                                            onDismiss()
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bottom Pill Button - "جوري صديقتكِ الذكية 🌸"
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                        shape = RoundedCornerShape(50.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .align(Alignment.CenterHorizontally)
                            .height(48.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "جوري صديقتكِ الذكية 🌸",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = SoftTheme.TextWhite
                            )
                        }
                    }
                }
            }
        }
    }
}
