package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.WomanCompanionViewModel
import java.util.Calendar

// --- Smart Conception and Baby prediction Sub-screen ---
@Composable
fun SmartConceptionSubScreen() {
    var lastPeriodDate by remember { mutableStateOf(System.currentTimeMillis()) }
    val context = LocalContext.current

    // Calculations based on Last Period Date
    val predictedDueDate = remember(lastPeriodDate) {
        lastPeriodDate + 280L * 24 * 60 * 60 * 1000
    }
    val ovulationStart = remember(lastPeriodDate) {
        lastPeriodDate + 11L * 24 * 60 * 60 * 1000
    }
    val ovulationEnd = remember(lastPeriodDate) {
        lastPeriodDate + 16L * 24 * 60 * 60 * 1000
    }

    // Trimester timeline
    val tri1End = remember(lastPeriodDate) { lastPeriodDate + 84L * 24 * 60 * 60 * 1000 }
    val tri2End = remember(lastPeriodDate) { lastPeriodDate + 182L * 24 * 60 * 60 * 1000 }

    // Child expected Western Zodiac and descriptions
    val zodiacInfo = remember(predictedDueDate) {
        val cal = Calendar.getInstance().apply { timeInMillis = predictedDueDate }
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = cal.get(Calendar.MONTH) + 1 // 1-indexed

        when (month) {
            1 -> if (day < 20) Pair("الجدي ♑", "هادئ، ذكي، طموح ومحب للتعلم والاستقلالية.") else Pair("الدلو ♒", "ودود، مبتكر، يحب التفكير خارج الصندوق وحر الشخصية.")
            2 -> if (day < 19) Pair("الدلو ♒", "ودود، مبتكر، يحب التفكير خارج الصندوق وحر الشخصية.") else Pair("الحوت ♓", "عاطفي للغاية، ذو خيال واسع وحنون ومحب للفنون.")
            3 -> if (day < 21) Pair("الحوت ♓", "عاطفي للغاية، ذو خيال واسع وحنون ومحب للفنون.") else Pair("الحمل ♈", "نشيط، شجاع وقوي الشخصية، فضولي ومحب للاستكشاف.")
            4 -> if (day < 20) Pair("الحمل ♈", "نشيط، شجاع وقوي الشخصية، فضولي ومحب للاستكشاف.") else Pair("الثور ♉", "صبور وعنيد إيجابياً، يحب الاستقرار وصاحب عزيمة قوية.")
            5 -> if (day < 21) Pair("الثور ♉", "صبور وعنيد إيجابياً، يحب الاستقرار وصاحب عزيمة قوية.") else Pair("الجوزاء ♊", "ذكي للغاية، اجتماعي، سريع التعلم ولديه موهبة الحديث والمرح.")
            6 -> if (day < 21) Pair("الجوزاء ♊", "ذكي للغاية، اجتماعي، سريع التعلم ولديه موهبة الحديث والمرح.") else Pair("السرطان ♋", "حنون جداً، مرتبط بالعائلة، ذو إحساس مرهف ومحب للسلام.")
            7 -> if (day < 23) Pair("السرطان ♋", "حنون جداً، مرتبط بالعائلة، ذو إحساس مرهف ومحب للسلام.") else Pair("الأسد ♌", "قيادي بطبعه، شجاع وصاحب حضور قوي، كريم ومحب للظهور.")
            8 -> if (day < 23) Pair("الأسد ♌", "قيادي بطبعه، شجاع وصاحب حضور قوي، كريم ومحب للظهور.") else Pair("العذراء ♍", "دقيق ومنظم، يحب الترتيب والتفاصيل، ذكي ومساعد ممتاز للآخرين.")
            9 -> if (day < 23) Pair("العذراء ♍", "دقيق ومنظم، يحب الترتيب والتفاصيل، ذكي ومساعد ممتاز للآخرين.") else Pair("الميزان ♎", "لطيف ودبلوماسي، يعشق الجمال والتوازن، محبوب واجتماعي جداً.")
            10 -> if (day < 23) Pair("الميزان ♎", "لطيف ودبلوماسي، يعشق الجمال والتوازن، محبوب واجتماعي جداً.") else Pair("العقرب ♏", "قوي الملاحظة، شغوف، كتوم ومخلص جداً ولديه شخصية جذابة.")
            11 -> if (day < 22) Pair("العقرب ♏", "قوي الملاحظة، شغوف، كتوم ومخلص جداً ولديه شخصية جذابة.") else Pair("القوس ♐", "مرح ومتفائل، يعشق السفر واللعب، شجاع ومحب للحرية والضحك.")
            12 -> if (day < 22) Pair("القوس ♐", "مرح ومتفائل، يعشق السفر واللعب، شجاع ومحب للحرية والضحك.") else Pair("الجدي ♑", "هادئ، ذكي، طموح ومحب للتعلم والاستقلالية.")
            else -> Pair("غير معروف", "")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("حاسبة التخطيط والحمل الذكي 🎯", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
        Text(
            "أدخلي تاريخ آخر دورة شهرية لحساب مواعيد التبويض والولادة والتعرف على السمات المتوقعة للطفل القادم ذكياً بالكامل دون إنترنت.",
            color = SoftTheme.SoftGray,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("تاريخ أول يوم لآخر دورة شهرية (LMP):", color = SoftTheme.TextWhite, fontWeight = FontWeight.Bold)
                Button(
                    onClick = {
                        val calendar = Calendar.getInstance().apply { timeInMillis = lastPeriodDate }
                        val dialog = android.app.DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                val cal = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, y)
                                    set(Calendar.MONTH, m)
                                    set(Calendar.DAY_OF_MONTH, d)
                                }
                                lastPeriodDate = cal.timeInMillis
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        )
                        dialog.datePicker.maxDate = System.currentTimeMillis()
                        dialog.datePicker.minDate = System.currentTimeMillis() - 300L * 24 * 60 * 60 * 1000
                        dialog.show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.DeepSlate)
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = SoftTheme.SoftPink)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(formatGregorianDate(lastPeriodDate), color = SoftTheme.TextWhite)
                }
            }
        }

        // Output results in beautiful timeline cards
        Text("نتائج التخطيط والتوقعات الذكية 🔮", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SoftTheme.SoftPink, modifier = Modifier.align(Alignment.Start))

        // 1. Expected Due Date
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("📅", fontSize = 28.sp)
                Column {
                    Text("تاريخ الولادة المتوقع:", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite, style = MaterialTheme.typography.bodyMedium)
                    Text(formatGregorianDate(predictedDueDate), color = SoftTheme.MintTeal, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
            }
        }

        // 2. Fertility window
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("✨", fontSize = 28.sp)
                Column {
                    Text("أيام التبويض القصوى والخصوبة:", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite, style = MaterialTheme.typography.bodyMedium)
                    Text("من ${formatGregorianDate(ovulationStart)} إلى ${formatGregorianDate(ovulationEnd)}", color = SoftTheme.SoftPink, style = MaterialTheme.typography.bodyMedium)
                    Text("هذه هي الفترة الذهبية لفرص حدوث الحمل بمشيئة الله.", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                }
            }
        }

        // 3. predicted Zodiac of the baby
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("👶", fontSize = 28.sp)
                Column {
                    Text("البرج والسمات المتوقعة للطفل:", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite, style = MaterialTheme.typography.bodyMedium)
                    Text("برج ${zodiacInfo.first}", color = SoftTheme.GoldFasting, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(zodiacInfo.second, style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                }
            }
        }

        // 4. Trimesters timeline
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("جدول الفترات الثلاث للحمل القادم:", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite, style = MaterialTheme.typography.bodyMedium)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("الثلث الأول (تثبيت):", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                    Text("حتى ${formatGregorianDate(tri1End)}", style = MaterialTheme.typography.bodySmall, color = SoftTheme.TextWhite)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("الثلث الثاني (نمو):", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                    Text("حتى ${formatGregorianDate(tri2End)}", style = MaterialTheme.typography.bodySmall, color = SoftTheme.TextWhite)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("الثلث الثالث (استعداد):", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                    Text("حتى الولادة بمشيئة الله", style = MaterialTheme.typography.bodySmall, color = SoftTheme.TextWhite)
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

// --- Fetal Kicks Sub-screen ---
@Composable
fun FetalKicksSubScreen(viewModel: WomanCompanionViewModel) {
    val activeStart by viewModel.currentKickSessionStart.collectAsStateWithLifecycle()
    val currentCount by viewModel.currentKickCount.collectAsStateWithLifecycle()
    val history by viewModel.fetalKickSessionsState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("عداد حركات وركلات الجنين 👶", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
        Text(
            "توصي الهيئات الصحية بعد عشر ركلات أو حركات واضحة خلال جلسة تتبع في أوقات نشاط الجنين المعتادة.",
            color = SoftTheme.SoftGray,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )

        if (activeStart == null) {
            Button(
                onClick = { viewModel.startFetalKickSession() },
                colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                modifier = Modifier.fillMaxWidth().height(56.dp).testTag("start_kick_session_btn")
            ) {
                Text("بدء جلسة عد جديدة")
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("الجلسة نشطة ومستمرة", color = SoftTheme.MintTeal, fontWeight = FontWeight.Bold)

                    Text(
                        text = "$currentCount",
                        style = MaterialTheme.typography.displayLarge,
                        color = SoftTheme.TextWhite,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = { viewModel.incrementKickCount() },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                        modifier = Modifier.size(100.dp).testTag("increment_kick_btn"),
                        shape = CircleShape
                    ) {
                        Text("ركلة! 🦶", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.cancelFetalKickSession() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء", color = SoftTheme.RedDanger)
                        }
                        Button(
                            onClick = { viewModel.saveFetalKickSession() },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.MintTeal),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("حفظ الجلسة")
                        }
                    }
                }
            }
        }

        Text("سجل جلسات الحركة السابقة 📖", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite, modifier = Modifier.align(Alignment.Start))

        if (history.isEmpty()) {
            Text("لا توجد جلسات مسجلة بعد.", color = SoftTheme.SoftGray)
        } else {
            history.forEach { ses ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("الركلات: ${ses.kickCount}", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
                            Text("المدة: ${ses.durationSeconds / 60} دقيقة و ${ses.durationSeconds % 60} ثانية", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                        }
                        Text(formatGregorianDate(ses.startTime), style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftPink)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(80.dp))
    }
}

// --- Contractions Sub-screen ---
@Composable
fun ContractionsSubScreen(viewModel: WomanCompanionViewModel) {
    val activeStart by viewModel.activeContractionStart.collectAsStateWithLifecycle()
    val history by viewModel.contractionLogsState.collectAsStateWithLifecycle()
    val warning = viewModel.checkContractionWarning()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("مؤقت تقلصات وانقباضات الولادة ⏱️", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)

        if (warning) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.RedDanger),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("⚠️ تنبيه هام (قاعدة 5-1-1)", fontWeight = FontWeight.Bold, color = Color.White)
                    Text(
                        "التقلصات تحدث بمعدل متقارب (كل ٥ دقائق أو أقل) وتستمر لـ دقيقة على الأقل منذ ساعة. قد تكونين في مرحلة الولادة النشطة. يرجى الاتصال بطبيبتك فوراً والتوجه للمستشفى بأمان 🌸.",
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        if (activeStart == null) {
            Button(
                onClick = { viewModel.startContraction() },
                colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.RedDanger),
                modifier = Modifier.fillMaxWidth().height(56.dp).testTag("start_contraction_btn")
            ) {
                Text("بدء انقباضة الآن 🔴")
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("التقلص مستمر...", color = SoftTheme.RedDanger, fontWeight = FontWeight.Bold)

                    Button(
                        onClick = { viewModel.stopAndSaveContraction() },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.RedDanger),
                        modifier = Modifier.size(100.dp).testTag("stop_contraction_btn"),
                        shape = CircleShape
                    ) {
                        Text("إيقاف وحفظ", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("السجل الأخير للتقلصات 📋", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
            TextButton(onClick = { viewModel.clearContractions() }) {
                Text("تصفير القائمة", color = SoftTheme.SoftGray)
            }
        }

        if (history.isEmpty()) {
            Text("لا توجد تقلصات مسجلة بعد.", color = SoftTheme.SoftGray)
        } else {
            history.forEach { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("المدة: ${log.durationSeconds} ثانية", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
                            if (log.intervalSeconds > 0) {
                                Text("الفاصل الزمني: ${log.intervalSeconds / 60} دقيقة", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                            }
                        }
                        Text(formatTime(log.startTime), style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftPink)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(80.dp))
    }
}
