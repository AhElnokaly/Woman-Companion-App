package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.WomanCompanionViewModel

@Composable
fun SleepAnalyzerScreen(viewModel: WomanCompanionViewModel) {
    val sleepLogs by viewModel.allSleepLogsState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    // Sleep recording states
    var startHoursAgo by remember { mutableStateOf(8f) }
    var sleepDurationHours by remember { mutableStateOf(8f) }
    var qualityScore by remember { mutableStateOf(80f) }
    var deepSleepPercent by remember { mutableStateOf(25f) }
    var lightSleepPercent by remember { mutableStateOf(55f) }
    var remSleepPercent by remember { mutableStateOf(20f) }
    var awakeningsCount by remember { mutableStateOf(1f) }
    var sleepNotes by remember { mutableStateOf("") }

    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "محلل ومراقب النوم الذكي 🌙💤",
                        style = MaterialTheme.typography.titleLarge,
                        color = SoftTheme.SoftPink,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "يتأثر نمط نومك بالتغيرات الهرمونية والجسدية خلال فترة الحمل والنفاس. يساعدك المحلل الذكي على تتبع صحة نومك وجرد جودته للحفاظ على نشاطك وصحة جنينك.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoftTheme.SoftGray
                    )
                    
                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                        modifier = Modifier.fillMaxWidth().testTag("add_sleep_log_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = SoftTheme.TextWhite)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تسجيل نوم الليلة الماضية ✍️", color = SoftTheme.TextWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Pregnancy sleep safe tips
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("💡 نصيحة النوم الصحي للحوامل والنفاس:", fontWeight = FontWeight.Bold, color = SoftTheme.SoftTeal)
                    Text(
                        text = "• يُنصح بشدة بالنوم على الجانب الأيسر (SOS) لتحسين تدفق الدم والتروية للجنين والرحم والكلية.\n" +
                               "• استخدمي وسائد مخصصة للحمل لتسديد الدعم لظهرك والبطن.\n" +
                               "• تجنبي شرب الكافيين والمنبهات قبل موعد النوم بـ 6 ساعات على الأقل.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTheme.SoftGray
                    )
                }
            }
        }

        if (sleepLogs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("😴", fontSize = 48.sp)
                        Text("لا يوجد سجلات نوم مسجلة بعد.", color = SoftTheme.SoftGray, fontWeight = FontWeight.Bold)
                        Text("سجلي نومك لتبدأ جوري في تحليل صحتك الحيوية.", color = SoftTheme.SoftGray, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        } else {
            // General Stats Card
            item {
                val avgQuality = sleepLogs.map { it.qualityScore }.average().toInt()
                val avgDuration = sleepLogs.map { (it.endTime - it.startTime) / (1000f * 60 * 60) }.average()
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("معدل الجودة", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                            Text("$avgQuality%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SoftTheme.SoftPink)
                        }
                        HorizontalDivider(modifier = Modifier.height(40.dp).width(1.dp), color = SoftTheme.SoftGray.copy(alpha = 0.3f))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("معدل الساعات", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                            Text(String.format("%.1f س", avgDuration), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SoftTheme.SoftTeal)
                        }
                        HorizontalDivider(modifier = Modifier.height(40.dp).width(1.dp), color = SoftTheme.SoftGray.copy(alpha = 0.3f))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("إجمالي الليالي", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                            Text("${sleepLogs.size}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
                        }
                    }
                }
            }

            item {
                Text("سجل الليالي السابقة:", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
            }

            items(sleepLogs) { log ->
                val durationMs = log.endTime - log.startTime
                val durationHours = durationMs / (1000f * 60 * 60)
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = formatGregorianDate(log.date),
                                    fontWeight = FontWeight.Bold,
                                    color = SoftTheme.TextWhite
                                )
                                Text(
                                    text = "${formatTime(log.startTime)} - ${formatTime(log.endTime)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SoftTheme.SoftGray
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (log.qualityScore >= 80) SoftTheme.SoftTeal.copy(alpha = 0.15f)
                                        else SoftTheme.SoftPink.copy(alpha = 0.15f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "جودة ${log.qualityScore}%",
                                    color = if (log.qualityScore >= 80) SoftTheme.SoftTeal else SoftTheme.SoftPink,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // Duration bar
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = SoftTheme.SoftPink, modifier = Modifier.size(16.dp))
                            Text(
                                text = String.format("مدة النوم الكلية: %.1f ساعة", durationHours),
                                style = MaterialTheme.typography.bodyMedium,
                                color = SoftTheme.TextWhite
                            )
                        }

                        // Deep/Light Sleep distribution if entered
                        if (log.deepSleepMinutes > 0 || log.lightSleepMinutes > 0) {
                            val totalMin = log.deepSleepMinutes + log.lightSleepMinutes + log.remSleepMinutes
                            if (totalMin > 0) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "عميق: ${log.deepSleepMinutes}د ( ${(log.deepSleepMinutes * 100 / totalMin)}%)",
                                            fontSize = 11.sp,
                                            color = SoftTheme.SoftTeal
                                        )
                                        Text(
                                            text = "خفيف: ${log.lightSleepMinutes}د ( ${(log.lightSleepMinutes * 100 / totalMin)}%)",
                                            fontSize = 11.sp,
                                            color = SoftTheme.SoftGray
                                        )
                                        if (log.remSleepMinutes > 0) {
                                            Text(
                                                text = "حركة سريعة: ${log.remSleepMinutes}د",
                                                fontSize = 11.sp,
                                                color = SoftTheme.SoftPink
                                            )
                                        }
                                    }
                                    
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .background(SoftTheme.DeepSlate, RoundedCornerShape(4.dp))
                                    ) {
                                        val deepWeight = log.deepSleepMinutes.toFloat() / totalMin
                                        val lightWeight = log.lightSleepMinutes.toFloat() / totalMin
                                        val remWeight = log.remSleepMinutes.toFloat() / totalMin
                                        
                                        if (deepWeight > 0) {
                                            Box(modifier = Modifier.weight(deepWeight).fillMaxHeight().background(SoftTheme.SoftTeal, RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp)))
                                        }
                                        if (lightWeight > 0) {
                                            Box(modifier = Modifier.weight(lightWeight).fillMaxHeight().background(SoftTheme.SoftGray))
                                        }
                                        if (remWeight > 0) {
                                            Box(modifier = Modifier.weight(remWeight).fillMaxHeight().background(SoftTheme.SoftPink, RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)))
                                        }
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("الاستيقاظ: ", fontSize = 11.sp, color = SoftTheme.SoftGray)
                                Text("${log.awakeningsCount} مرات", fontSize = 11.sp, color = SoftTheme.TextWhite, fontWeight = FontWeight.Bold)
                            }
                            
                            IconButton(
                                onClick = { viewModel.deleteSleepLog(log) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = SoftTheme.SoftPink.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                            }
                        }

                        if (!log.notes.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SoftTheme.DeepSlate, RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Text(text = "✍️ ملاحظات: ${log.notes}", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("تسجيل نوم الليلة الماضية 💤", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("كم ساعة نمتِ الليلة الماضية؟", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Slider(
                            value = sleepDurationHours,
                            onValueChange = { sleepDurationHours = it },
                            valueRange = 1f..16f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(thumbColor = SoftTheme.SoftPink, activeTrackColor = SoftTheme.SoftPink)
                        )
                        Text(text = String.format("%.1f س", sleepDurationHours), fontWeight = FontWeight.Bold, color = SoftTheme.SoftTeal)
                    }

                    Text("تقييم جودة النوم وعمقه:", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Slider(
                            value = qualityScore,
                            onValueChange = { qualityScore = it },
                            valueRange = 10f..100f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(thumbColor = SoftTheme.SoftTeal, activeTrackColor = SoftTheme.SoftTeal)
                        )
                        Text(text = "${qualityScore.toInt()}%", fontWeight = FontWeight.Bold, color = SoftTheme.SoftPink)
                    }

                    Text("توزيع النوم (اختياري بالدقائق):", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
                    
                    Column {
                        Text("النوم العميق (موصى به > ٢٠%): ${deepSleepPercent.toInt()} دقيقة", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                        Slider(
                            value = deepSleepPercent,
                            onValueChange = { deepSleepPercent = it },
                            valueRange = 0f..240f,
                            colors = SliderDefaults.colors(thumbColor = SoftTheme.SoftTeal, activeTrackColor = SoftTheme.SoftTeal)
                        )
                    }

                    Column {
                        Text("النوم الخفيف: ${lightSleepPercent.toInt()} دقيقة", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                        Slider(
                            value = lightSleepPercent,
                            onValueChange = { lightSleepPercent = it },
                            valueRange = 0f..480f,
                            colors = SliderDefaults.colors(thumbColor = SoftTheme.SoftGray, activeTrackColor = SoftTheme.SoftGray)
                        )
                    }

                    Column {
                        Text("نوم حركة العين السريعة (الأحلام): ${remSleepPercent.toInt()} دقيقة", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                        Slider(
                            value = remSleepPercent,
                            onValueChange = { remSleepPercent = it },
                            valueRange = 0f..180f,
                            colors = SliderDefaults.colors(thumbColor = SoftTheme.SoftPink, activeTrackColor = SoftTheme.SoftPink)
                        )
                    }

                    Column {
                        Text("عدد مرات الاستيقاظ: ${awakeningsCount.toInt()} مرات", style = MaterialTheme.typography.bodySmall, color = SoftTheme.TextWhite)
                        Slider(
                            value = awakeningsCount,
                            onValueChange = { awakeningsCount = it },
                            valueRange = 0f..10f,
                            colors = SliderDefaults.colors(thumbColor = SoftTheme.SoftPink, activeTrackColor = SoftTheme.SoftPink)
                        )
                    }

                    OutlinedTextField(
                        value = sleepNotes,
                        onValueChange = { sleepNotes = it },
                        label = { Text("ملاحظات النوم (مثال: شربت يانسون دافئ، قلق خفيف)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val durationMs = (sleepDurationHours * 60 * 60 * 1000).toLong()
                        val endTime = System.currentTimeMillis()
                        val startTime = endTime - durationMs
                        viewModel.addSleepLog(
                            startTime = startTime,
                            endTime = endTime,
                            qualityScore = qualityScore.toInt(),
                            deepSleepMin = deepSleepPercent.toInt(),
                            lightSleepMin = lightSleepPercent.toInt(),
                            remSleepMin = remSleepPercent.toInt(),
                            awakenings = awakeningsCount.toInt(),
                            notes = sleepNotes.ifBlank { null }
                        )
                        showAddDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink)
                ) {
                    Text("حفظ السجل 💾", color = SoftTheme.TextWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("إلغاء", color = SoftTheme.SoftPink)
                }
            }
        )
    }
}
