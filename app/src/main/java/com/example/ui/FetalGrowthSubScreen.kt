package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FetalGrowthLog
import com.example.data.PregnancyEntity
import com.example.ui.fetal.*
import com.example.viewmodel.WomanCompanionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Backward-compatibility aliases so existing callers in the app remain unbroken
typealias FetalStandard = com.example.ui.fetal.FetalStandard
val FetalStandardData = com.example.ui.fetal.FetalStandardData

@Composable
fun FetalGrowthSubScreen(viewModel: WomanCompanionViewModel) {
    val logs by viewModel.allFetalGrowthLogsState.collectAsState()
    val activePregnancy by viewModel.pregnancyState.collectAsState()
    val allPregnancies by viewModel.allPregnanciesState.collectAsState()

    val activePregnancyLogs = remember(logs, activePregnancy) {
        val targetId = activePregnancy?.id ?: 1
        logs.filter { it.pregnancyId == targetId }
    }

    val pregnancyProgression = viewModel.getPregnancyProgression()

    var showAddDialog by remember { mutableStateOf(false) }
    var showStartNewPregnancyDialog by remember { mutableStateOf(false) }
    var selectedWeek by remember { mutableStateOf(12) }

    val dateFormatter = remember { SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftTheme.DeepSlate)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Card with warm companion encouragement
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(SoftTheme.SoftPink.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = SoftTheme.SoftPink,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "مُتابِع نمو الجنين الذكي 📈👶",
                                fontWeight = FontWeight.Bold,
                                color = SoftTheme.TextWhite,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "تتبع رحلة وزن وطول طفلكِ بعد كل كشف طبي وقارنيها بالمعدل الصحي!",
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftTheme.SoftGray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "يا حبيبة قلبي، تدوين قياسات طفلكِ بعد زيارات الطبيب يتيح لنا تحليل منحنى نموه بذكاء والتأكد من ملاءمته لأعلى المعايير الصحية والغذائية لسلامتكن! 💕",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoftTheme.TextWhite.copy(alpha = 0.9f),
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val liveWeek = viewModel.getPregnancyProgression()?.weeks ?: 12
                            selectedWeek = liveWeek.coerceIn(4, 42)
                            showAddDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_growth_log_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = SoftTheme.DeepSlate)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تسجيل قياسات السونار الجديدة 🏥", color = SoftTheme.DeepSlate, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 1.5 Live Current-Week Card (Independent of manual sonogram logs)
        item {
            if (pregnancyProgression != null) {
                val currentWeek = pregnancyProgression.weeks.coerceIn(1, 42)
                val liveStandard = FetalStandardData.getStandardForWeek(currentWeek)
                val weightDeviationPercent = viewModel.getWeightDeviationForCurrentPregnancy() ?: 0.0
                val estimatedWeightGrams = FetalStandardData.getEstimatedWeightGrams(currentWeek, weightDeviationPercent)
                Card(
                    colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("live_fetal_current_week_card")
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "الأسبوع الحالي ($currentWeek) ⏳",
                                        fontWeight = FontWeight.Bold,
                                        color = SoftTheme.SoftPink,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = liveStandard.icon,
                                        fontSize = 18.sp
                                    )
                                }
                                Text(
                                    text = "التقدير الحي الذكي لحجم ووزن طفلكِ الآن",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SoftTheme.SoftGray
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .background(SoftTheme.DeepSlate, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = liveStandard.fruitComparison,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftTheme.TextWhite,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Live estimation metrics
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("الوزن التقديري الآن ⚖️", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${estimatedWeightGrams.toInt()} جرام",
                                        fontWeight = FontWeight.Bold,
                                        color = SoftTheme.MintTeal,
                                        fontSize = 16.sp
                                    )
                                    if (weightDeviationPercent != null && Math.abs(weightDeviationPercent) > 1.0) {
                                        val sign = if (weightDeviationPercent > 0) "+" else ""
                                        Text(
                                            text = "($sign${String.format(Locale.US, "%.1f", weightDeviationPercent)}% عن المتوسط)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SoftTheme.GoldFasting,
                                            fontSize = 10.sp
                                        )
                                    } else {
                                        Text(
                                            text = "(مطابق للمتوسط الطبيعي)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SoftTheme.SoftGray,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("الطول التقريبي 📏", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${liveStandard.lengthCm} سم",
                                        fontWeight = FontWeight.Bold,
                                        color = SoftTheme.TextWhite,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = "من الرأس إلى الكعب",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SoftTheme.SoftGray,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SoftTheme.DeepSlate.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "💡 نصيحة الأسبوع: ${liveStandard.description}",
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftTheme.TextWhite.copy(alpha = 0.9f),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }

        // 2. Visual Growth Curve Chart
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("fetal_growth_chart_card")
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "منحنى الوزن البياني التفاعلي 📊",
                                fontWeight = FontWeight.Bold,
                                color = SoftTheme.TextWhite,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "مقارنة وزن طفلكِ الحقيقي بالخط المعياري المرجعي",
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftTheme.SoftGray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.size(12.dp).background(Color(0xFFFFB6C1), CircleShape))
                            Text("المعدل المرجعي الطبيعي", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.size(12.dp).background(Color.White, CircleShape))
                            Text("قياسات طفلكِ الفعلية", style = MaterialTheme.typography.bodySmall, color = SoftTheme.TextWhite)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    FetalGrowthChart(logs = activePregnancyLogs)
                }
            }
        }

        // 3. Detailed Latest Status & Diagnosis Card
        val latestLog = activePregnancyLogs.maxByOrNull { it.pregnancyWeek }
        if (latestLog != null) {
            item {
                val standard = FetalStandardData.getStandardForWeek(latestLog.pregnancyWeek)
                Card(
                    colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(standard.icon, fontSize = 24.sp)
                                Column {
                                    Text(
                                        text = "آخر تقييم: الأسبوع ${latestLog.pregnancyWeek}",
                                        fontWeight = FontWeight.Bold,
                                        color = SoftTheme.TextWhite,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = "حجم طفلكِ الآن يقارب: ${standard.fruitComparison}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SoftTheme.SoftPink
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Comparison Metrics
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Weight comparison
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("وزن الجنين ⚖️", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${latestLog.weightGrams.toInt()} جم",
                                        fontWeight = FontWeight.Bold,
                                        color = SoftTheme.TextWhite,
                                        fontSize = 16.sp
                                    )
                                    val weightDiffPct = FetalStandardData.calculateWeightDeviation(latestLog.pregnancyWeek, latestLog.weightGrams)
                                    val sign = if (weightDiffPct > 0) "+" else ""
                                    Text(
                                        text = "$sign${String.format(Locale.US, "%.1f", weightDiffPct)}% عن المعدل",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (weightDiffPct in -15.0..15.0) SoftTheme.MintTeal else SoftTheme.GoldFasting
                                    )
                                }
                            }

                            // Length comparison
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("طول الجنين 📏", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${latestLog.lengthCm} سم",
                                        fontWeight = FontWeight.Bold,
                                        color = SoftTheme.TextWhite,
                                        fontSize = 16.sp
                                    )
                                    val lengthDiffPct = FetalStandardData.calculateLengthDeviation(latestLog.pregnancyWeek, latestLog.lengthCm)
                                    val sign = if (lengthDiffPct > 0) "+" else ""
                                    Text(
                                        text = "$sign${String.format(Locale.US, "%.1f", lengthDiffPct)}% عن المعدل",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (lengthDiffPct in -15.0..15.0) SoftTheme.MintTeal else SoftTheme.GoldFasting
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Fruit Comparison Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate.copy(alpha = 0.7f)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(standard.icon, fontSize = 20.sp)
                                    Text(
                                        text = "المقارنة اللطيفة: كبر حجم ${standard.fruitComparison}",
                                        fontWeight = FontWeight.Bold,
                                        color = SoftTheme.TextWhite,
                                        fontSize = 13.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = standard.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SoftTheme.SoftGray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        val weightDiffPct = FetalStandardData.calculateWeightDeviation(latestLog.pregnancyWeek, latestLog.weightGrams)
                        val diagnosisText = when {
                            weightDiffPct in -15.0..15.0 -> "نمو طفلكِ في النطاق الذهبي الممتاز والمثالي! 🌟 الاستمرار في التغذية المتوازنة رائع جداً ويمنح جنينك القوة والصحة."
                            weightDiffPct < -15.0 -> "وزن طفلكِ أقل قليلاً من المعدل المتوسط. لا تقلقي يا غالية، قد يكون القياس بالسونار تقريبياً، لكن ننصحكِ بمناقشة طبيبتكِ في تحسين التغذية وزيادة الأطعمة الغنية بالبروتينات والحديد مثل اللحوم الحمراء والبيض والمكسرات!"
                            else -> "طفلكِ ما شاء الله ينمو بهمة ونشاط وحجم فوق المتوسط! استشيري طبيبتكِ للتأكد من توازن مستويات السكر والتغذية اللطيفة السليمة لسهولة ولادتكِ."
                        }
                        Text(
                            text = diagnosisText,
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTheme.TextWhite,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // 4. List of past growth records
        item {
            Text(
                text = "السجلات السابقة لعيادة الطبيب 📋",
                fontWeight = FontWeight.Bold,
                color = SoftTheme.TextWhite,
                fontSize = 16.sp
            )
        }

        if (activePregnancyLogs.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = SoftTheme.SoftPink.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "لا توجد سجلات نمو للحمل الحالي حتى الآن",
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.TextWhite,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "سجلي أول قراءة لوزن وطول جنينك من السونار لنبدأ بمتابعة نموه ورسم المنحنى البياني التفاعلي!",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTheme.SoftGray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(activePregnancyLogs.sortedByDescending { it.pregnancyWeek }) { log ->
                val std = FetalStandardData.getStandardForWeek(log.pregnancyWeek)
                Card(
                    colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "الأسبوع ${log.pregnancyWeek} من الحمل 🤰",
                                    fontWeight = FontWeight.Bold,
                                    color = SoftTheme.TextWhite,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = dateFormatter.format(Date(log.date)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SoftTheme.SoftGray
                                )
                            }
                            IconButton(
                                onClick = { viewModel.deleteFetalGrowthLog(log) },
                                modifier = Modifier.testTag("delete_growth_log_${log.id}")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف القراءة", tint = Color.Red.copy(alpha = 0.7f))
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SoftTheme.DeepSlate)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("الوزن الفعلي:", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                                Text("${log.weightGrams.toInt()} جرام", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("المتوسط القياسي: ${std.weightGrams.toInt()} جرام", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("الطول الفعلي:", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                                Text("${log.lengthCm} سم", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("المتوسط القياسي: ${std.lengthCm} سم", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                            }
                        }

                        if (!log.notes.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SoftTheme.DeepSlate, RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text("ملاحظات الطبيب 📝", fontWeight = FontWeight.Bold, color = SoftTheme.SoftPink, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(log.notes, style = MaterialTheme.typography.bodySmall, color = SoftTheme.TextWhite)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Pregnancy History Section
        item {
            PregnancyHistorySection(
                allPregnancies = allPregnancies,
                allLogs = logs,
                onStartNewPregnancy = { showStartNewPregnancyDialog = true }
            )
        }
    }

    // Add Record Dialog
    if (showAddDialog) {
        AddFetalGrowthDialog(
            initialWeek = selectedWeek,
            onDismiss = { showAddDialog = false },
            onSave = { week, weight, length, notes ->
                viewModel.addFetalGrowthLog(
                    week = week,
                    weightGrams = weight,
                    lengthCm = length,
                    notes = notes
                )
                showAddDialog = false
            }
        )
    }

    if (showStartNewPregnancyDialog) {
        StartNewPregnancyDialog(
            onDismiss = { showStartNewPregnancyDialog = false },
            onConfirm = { lastPeriodDateMs, babyName ->
                viewModel.startNewPregnancy(
                    lastPeriodDate = lastPeriodDateMs,
                    babyName = babyName
                )
                showStartNewPregnancyDialog = false
            }
        )
    }
}
