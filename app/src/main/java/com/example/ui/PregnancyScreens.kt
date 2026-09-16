package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import com.example.data.FetalKickSession
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

    var sessionToEdit by remember { mutableStateOf<FetalKickSession?>(null) }
    var sessionToDelete by remember { mutableStateOf<FetalKickSession?>(null) }
    var elapsedSeconds by remember { mutableLongStateOf(0L) }

    // Real-time timer during active kick session
    LaunchedEffect(activeStart) {
        if (activeStart != null) {
            while (true) {
                val now = System.currentTimeMillis()
                elapsedSeconds = maxOf(0L, (now - activeStart!!) / 1000)
                kotlinx.coroutines.delay(1000)
            }
        } else {
            elapsedSeconds = 0L
        }
    }

    val zeroCountSessions = remember(history) { history.count { it.kickCount == 0 } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("عداد حركات وركلات الجنين 👶🦶", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
        Text(
            "توصي الهيئات الطبية بملاحظة حركات الجنين في أوقات نشاطه. سجلي كل ركلة بضغطة واحدة حتى تصلي إلى 10 ركلات للاطمئنان الكامل.",
            color = SoftTheme.SoftGray,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )

        if (activeStart == null) {
            Button(
                onClick = { viewModel.recordKickFromDashboard() },
                colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.PrimaryPink),
                modifier = Modifier.fillMaxWidth().height(56.dp).testTag("start_kick_session_btn"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("بدء جلسة عد جديدة (تسجيل الركلة الأولى 🦶)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, SoftTheme.MintTeal.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SoftTheme.MintTeal.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "⏱️ المدة: ${elapsedSeconds / 60} د و ${elapsedSeconds % 60} ث",
                                color = SoftTheme.MintTeal,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Text("الجلسة نشطة ومستمرة 🌸", color = SoftTheme.SoftPink, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Text(
                        text = "$currentCount",
                        style = MaterialTheme.typography.displayLarge,
                        color = SoftTheme.TextWhite,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text("ركلات مسجلة في هذه الجلسة", color = SoftTheme.SoftGray, style = MaterialTheme.typography.bodySmall)

                    Button(
                        onClick = { viewModel.incrementKickCount() },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.PrimaryPink),
                        modifier = Modifier.size(110.dp).testTag("increment_kick_btn"),
                        shape = CircleShape,
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🦶", fontSize = 28.sp)
                            Text("+١ ركلة", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.cancelFetalKickSession() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftTheme.RedDanger),
                            border = BorderStroke(1.dp, SoftTheme.RedDanger.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء ❌")
                        }
                        Button(
                            onClick = { viewModel.saveFetalKickSession() },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.MintTeal),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1.3f)
                        ) {
                            Text("حفظ الجلسة ($currentCount) 🏁", fontWeight = FontWeight.Bold, color = SoftTheme.DeepSlate)
                        }
                    }
                }
            }
        }

        // Section header and Zero Clean Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("سجل جلسات الحركة السابقة 📖", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite, style = MaterialTheme.typography.titleMedium)
            if (zeroCountSessions > 0) {
                Surface(
                    onClick = { viewModel.deleteZeroKickSessions() },
                    shape = RoundedCornerShape(10.dp),
                    color = SoftTheme.RedDanger.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, SoftTheme.RedDanger.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("🧹", fontSize = 11.sp)
                        Text(
                            text = "تنظيف ($zeroCountSessions فارغة)",
                            color = SoftTheme.RedDanger,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        if (history.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SoftTheme.CardSlate.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("👶", fontSize = 32.sp)
                    Text("لا توجد جلسات مسجلة بعد", color = SoftTheme.TextWhite, fontWeight = FontWeight.Bold)
                    Text("ابدئي جلستك الأولى عند شعورك بأول ركلة لطفلكِ الحبيب.", color = SoftTheme.SoftGray, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                }
            }
        } else {
            history.forEach { ses ->
                val isOptimal = ses.kickCount >= 10
                val isMedium = ses.kickCount in 4..9
                val isZero = ses.kickCount == 0

                val badgeColor = when {
                    isOptimal -> SoftTheme.MintTeal
                    isMedium -> SoftTheme.GoldFasting
                    isZero -> SoftTheme.RedDanger
                    else -> SoftTheme.SoftPink
                }

                val badgeText = when {
                    isOptimal -> "حركة ممتازة ومطمئنة ✨"
                    isMedium -> "نشاط جيد 💛"
                    isZero -> "جلسة فارغة (٠)"
                    else -> "تتبع قصير"
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = badgeColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = badgeText,
                                    color = badgeColor,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Text(
                                text = formatGregorianDate(ses.startTime),
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftTheme.SoftPink,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("🦶", fontSize = 22.sp)
                                Column {
                                    Text(
                                        text = "${ses.kickCount} ركلات",
                                        fontWeight = FontWeight.Bold,
                                        color = SoftTheme.TextWhite,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = "المدة: ${ses.durationSeconds / 60} دقيقة و ${ses.durationSeconds % 60} ثانية",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SoftTheme.SoftGray,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(
                                    onClick = { sessionToEdit = ses },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "تعديل الجلسة",
                                        tint = SoftTheme.MintTeal,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { sessionToDelete = ses },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "حذف الجلسة",
                                        tint = SoftTheme.RedDanger,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(80.dp))
    }

    // Delete Confirmation Dialog
    sessionToDelete?.let { session ->
        AlertDialog(
            onDismissRequest = { sessionToDelete = null },
            title = { Text("حذف الجلسة؟", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite) },
            text = {
                Text(
                    "هل أنتِ متأكدة من حذف جلسة الركلات (${session.kickCount} ركلات بتاريخ ${formatGregorianDate(session.startTime)})؟",
                    color = SoftTheme.SoftGray
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteFetalKickSession(session)
                        sessionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.RedDanger)
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { sessionToDelete = null }) {
                    Text("إلغاء", color = SoftTheme.TextWhite)
                }
            },
            containerColor = SoftTheme.CardSlate
        )
    }

    // Edit Kick Session Dialog
    sessionToEdit?.let { session ->
        EditFetalKickDialog(
            session = session,
            onDismiss = { sessionToEdit = null },
            onConfirm = { updated ->
                viewModel.updateFetalKickSession(updated)
                sessionToEdit = null
            }
        )
    }
}

@Composable
fun EditFetalKickDialog(
    session: FetalKickSession,
    onDismiss: () -> Unit,
    onConfirm: (FetalKickSession) -> Unit
) {
    var count by remember { mutableIntStateOf(session.kickCount) }
    var durationMinutes by remember { mutableLongStateOf(session.durationSeconds / 60) }
    var durationSecondsRemainder by remember { mutableLongStateOf(session.durationSeconds % 60) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("تعديل جلسة الركلات ✏️", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("تعديل عدد الركلات:", color = SoftTheme.SoftGray, style = MaterialTheme.typography.bodySmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledIconButton(
                        onClick = { if (count > 0) count -= 1 },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = SoftTheme.DeepSlate)
                    ) {
                        Text("-", color = SoftTheme.TextWhite, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }

                    Text(
                        text = "$count ركلة",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.SoftPink
                    )

                    FilledIconButton(
                        onClick = { count += 1 },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = SoftTheme.PrimaryPink)
                    ) {
                        Text("+", color = SoftTheme.TextWhite, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                }

                Text("تعديل مدة الجلسة (دقائق):", color = SoftTheme.SoftGray, style = MaterialTheme.typography.bodySmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledIconButton(
                        onClick = { if (durationMinutes > 0) durationMinutes -= 1 },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = SoftTheme.DeepSlate)
                    ) {
                        Text("-", color = SoftTheme.TextWhite, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }

                    Text(
                        text = "$durationMinutes دقيقة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.MintTeal
                    )

                    FilledIconButton(
                        onClick = { durationMinutes += 1 },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = SoftTheme.MintTeal)
                    ) {
                        Text("+", color = SoftTheme.DeepSlate, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val totalSec = maxOf(1L, durationMinutes * 60 + durationSecondsRemainder)
                    onConfirm(
                        session.copy(
                            kickCount = count,
                            durationSeconds = totalSec,
                            endTime = session.startTime + (totalSec * 1000)
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.MintTeal)
            ) {
                Text("حفظ التعديل", color = SoftTheme.DeepSlate, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = SoftTheme.TextWhite)
            }
        },
        containerColor = SoftTheme.CardSlate
    )
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
