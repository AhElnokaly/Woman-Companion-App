package com.example.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.MedicationLog
import com.example.ui.meds.AddEditMedicationDialog
import com.example.ui.meds.MedicationDoseHistoryDialog
import com.example.ui.symptoms.AddBloodPressureDialog
import com.example.ui.symptoms.AddSymptomDialog
import com.example.ui.symptoms.BpStatus
import com.example.ui.symptoms.MedicalReportDialog
import com.example.ui.symptoms.getBpStatus
import com.example.viewmodel.WomanCompanionViewModel

// --- Symptoms & Medications Screen ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SymptomAndMedsScreen(
    viewModel: WomanCompanionViewModel
) {
    val context = LocalContext.current
    val medications by viewModel.allMedicationsState.collectAsStateWithLifecycle()
    val symptoms by viewModel.symptomLogsState.collectAsStateWithLifecycle()
    val bpLogs by viewModel.bloodPressureLogsState.collectAsStateWithLifecycle()
    val isWaterEnabled by viewModel.isWaterReminderEnabled.collectAsStateWithLifecycle()
    val adherenceLogs by viewModel.allMedicationAdherenceLogsState.collectAsStateWithLifecycle()

    var showAddMedDialog by remember { mutableStateOf(false) }
    var editingMedication by remember { mutableStateOf<MedicationLog?>(null) }
    var showAddSymptomDialog by remember { mutableStateOf(false) }
    var showAddBpDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var viewingDoseHistoryMed by remember { mutableStateOf<MedicationLog?>(null) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "الأعراض والأدوية 💊🩺",
                            style = MaterialTheme.typography.titleLarge,
                            color = SoftTheme.SoftPink,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "متابعة شاملة للروتين الدوائي، مواعيد الجرعات، المخزون، والأعراض الصحية",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SoftTheme.SoftGray
                        )
                    }
                    Button(
                        onClick = { showReportDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.MintTeal),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("📄 التقرير الطبي", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Quick Hydration Water Reminder Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFF64B5F6).copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("💧", fontSize = 24.sp)
                            Column {
                                Text(
                                    text = "تنبيهات شرب الماء النهارية 🥛",
                                    fontWeight = FontWeight.Bold,
                                    color = SoftTheme.TextWhite,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (isWaterEnabled) "مفعّلة: تذكير كل ساعتين من ٨ ص حتى ١٠ م" else "تذكير دوري هادئ للحفاظ على ترطيب جسمكِ وجنينكِ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SoftTheme.SoftGray,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Switch(
                            checked = isWaterEnabled,
                            onCheckedChange = { viewModel.toggleWaterReminders(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF64B5F6),
                                uncheckedTrackColor = SoftTheme.DeepSlate
                            )
                        )
                    }
                }
            }

            // Medications list header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "الأدوية والمكملات الموصوفة 💊",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.TextWhite
                        )
                        if (medications.isNotEmpty()) {
                            Surface(
                                shape = CircleShape,
                                color = SoftTheme.SoftPink.copy(alpha = 0.2f),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = "${medications.size}",
                                    color = SoftTheme.SoftPink,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Button(
                        onClick = {
                            editingMedication = null
                            showAddMedDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "إضافة دواء", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إضافة دواء", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (medications.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, SoftTheme.SoftGray.copy(alpha = 0.15f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("💊", fontSize = 36.sp)
                            Text(
                                text = "لا توجد أدوية مسجلة بعد",
                                fontWeight = FontWeight.Bold,
                                color = SoftTheme.TextWhite
                            )
                            Text(
                                text = "أضيفي مكملات الحمل أو أدويتكِ الموصوفة مع أوقات الجرعات لتذكيركِ بدقة ومتابعة التزامك ومخزون العلبة.",
                                color = SoftTheme.SoftGray,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            } else {
                items(medications, key = { it.id }) { med ->
                    com.example.ui.meds.MedicationItemCard(
                        med = med,
                        onEdit = {
                            editingMedication = med
                            showAddMedDialog = true
                        },
                        onDelete = { viewModel.deleteMedication(med) },
                        onToggleActive = { viewModel.toggleMedicationActive(med) },
                        onTakeDose = {
                            viewModel.recordMedicationAdherence(med.id, System.currentTimeMillis(), "TAKEN")
                            if (med.remainingQuantity > 0) {
                                viewModel.decrementMedicationStock(med, 1)
                            }
                            Toast.makeText(context, "صحة وعافية! تم تأكيد أخذ الجرعة ✓", Toast.LENGTH_SHORT).show()
                        },
                        onSnoozeDose = {
                            val snoozeTime = System.currentTimeMillis() + (15 * 60 * 1000L)
                            com.example.reminder.ReminderScheduler.scheduleReminder(
                                context = context,
                                id = med.id * 100 + 99,
                                triggerTimeMillis = snoozeTime,
                                title = "تأجيل منبه: ${med.name} ⏰",
                                body = "تذكير مؤجل لتناول جرعة ${med.dosage ?: ""} الآن 🌸",
                                recurrence = com.example.reminder.RecurrenceType.NONE,
                                medicationId = med.id
                            )
                            Toast.makeText(context, "تم تأجيل المنبه ١٥ دقيقة ⏰", Toast.LENGTH_SHORT).show()
                        },
                        onViewHistory = { viewingDoseHistoryMed = med }
                    )
                }
            }

            // Weekly medication adherence stats card
            item {
                MedicationAdherenceWeeklyCard(viewModel = viewModel)
            }

            // Symptom tracker section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "سجل الأعراض اليومي 🩺",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.TextWhite
                    )
                    IconButton(
                        onClick = { showAddSymptomDialog = true },
                        modifier = Modifier.background(SoftTheme.CardSlate, CircleShape)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "إضافة عرض", tint = SoftTheme.SoftPink)
                    }
                }
            }

            if (symptoms.isEmpty()) {
                item {
                    Text(
                        text = "لم تقومي بتسجيل أي عرض صحي اليوم بعد.",
                        color = SoftTheme.SoftGray,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                    )
                }
            } else {
                items(symptoms, key = { it.id }) { sym ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = sym.symptom,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftTheme.TextWhite
                                )
                                Text(
                                    text = "الشدة: ${sym.severity}/10",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SoftTheme.SoftPink
                                )
                                sym.notes?.let {
                                    Text(
                                        text = "ملاحظة: $it",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SoftTheme.SoftGray
                                    )
                                }
                            }
                            IconButton(onClick = { viewModel.deleteSymptom(sym) }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = SoftTheme.RedDanger)
                            }
                        }
                    }
                }
            }

            // --- Blood pressure tracker section ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "سجل قياسات ضغط الدم 🩸📈",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.TextWhite
                    )
                    IconButton(
                        onClick = { showAddBpDialog = true },
                        modifier = Modifier.background(SoftTheme.CardSlate, CircleShape)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "إضافة قياس", tint = SoftTheme.SoftPink)
                    }
                }
            }

            if (bpLogs.isEmpty()) {
                item {
                    Text(
                        text = "لم تقومي بتسجيل أي قياس لضغط الدم بعد. انقري على (+) للبدء بالوقاية والمتابعة.",
                        color = SoftTheme.SoftGray,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                    )
                }
            } else {
                items(bpLogs, key = { it.id }) { log ->
                    val status = getBpStatus(log.systolic, log.diastolic)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(status.color, CircleShape)
                                )
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "${log.systolic} / ${log.diastolic}",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 20.sp,
                                            color = SoftTheme.TextWhite
                                        )
                                        Text(
                                            text = "ملم زئبق",
                                            fontSize = 12.sp,
                                            color = SoftTheme.SoftGray
                                        )
                                    }
                                    Text(
                                        text = status.label,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = status.color
                                    )
                                    Text(
                                        text = status.advice,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SoftTheme.SoftGray,
                                        lineHeight = 16.sp,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                    Row(
                                        modifier = Modifier.padding(top = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = formatGregorianDate(log.date),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SoftTheme.SoftGray.copy(alpha = 0.7f)
                                        )
                                        if (log.pulse != null) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.Favorite,
                                                    contentDescription = "النبض",
                                                    tint = SoftTheme.SoftPink,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text(
                                                    text = "${log.pulse} ن/د",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = SoftTheme.SoftPink
                                                )
                                            }
                                        }
                                    }
                                    if (!log.notes.isNullOrEmpty()) {
                                        Text(
                                            text = "📝 ملاحظة: ${log.notes}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SoftTheme.SoftGray,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                            }
                            IconButton(onClick = { viewModel.deleteBloodPressureLog(log) }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف القياس", tint = SoftTheme.RedDanger)
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Dialog Implementations ---
    if (showAddMedDialog) {
        AddEditMedicationDialog(
            editingMedication = editingMedication,
            onDismiss = {
                showAddMedDialog = false
                editingMedication = null
            },
            onSave = { name, dosage, times, prescby, notes, start, stock, warning ->
                val existing = editingMedication
                if (existing != null) {
                    viewModel.updateMedication(
                        existing.copy(
                            name = name,
                            dosage = dosage,
                            timesPerDay = times,
                            prescribedBy = prescby,
                            notes = notes,
                            startDate = start,
                            totalQuantity = if (stock > 0) stock else existing.totalQuantity,
                            remainingQuantity = if (stock > 0) stock else existing.remainingQuantity,
                            safetyWarning = warning
                        )
                    )
                } else {
                    viewModel.addMedication(
                        name = name,
                        dosage = dosage,
                        timesPerDay = times,
                        prescby = prescby,
                        notes = notes,
                        start = start,
                        totalQuantity = stock,
                        remainingQuantity = stock,
                        safetyWarning = warning
                    )
                }
                showAddMedDialog = false
                editingMedication = null
            }
        )
    }

    if (showAddSymptomDialog) {
        AddSymptomDialog(
            onDismiss = { showAddSymptomDialog = false },
            onSave = { symptom, severity, notes ->
                viewModel.addSymptom(symptom, severity, notes)
                showAddSymptomDialog = false
            }
        )
    }

    if (showAddBpDialog) {
        AddBloodPressureDialog(
            onDismiss = { showAddBpDialog = false },
            onSave = { systolic, diastolic, pulse, notes ->
                viewModel.addBloodPressureLog(systolic, diastolic, pulse, notes)
                showAddBpDialog = false
            }
        )
    }

    if (showReportDialog) {
        MedicalReportDialog(
            viewModel = viewModel,
            onDismiss = { showReportDialog = false }
        )
    }

    viewingDoseHistoryMed?.let { targetMed ->
        MedicationDoseHistoryDialog(
            targetMed = targetMed,
            adherenceLogs = adherenceLogs,
            onDismiss = { viewingDoseHistoryMed = null }
        )
    }
}

@Composable
fun MedicationAdherenceWeeklyCard(
    viewModel: WomanCompanionViewModel,
    modifier: Modifier = Modifier
) {
    val medications by viewModel.allMedicationsState.collectAsStateWithLifecycle()
    val adherenceLogs by viewModel.allMedicationAdherenceLogsState.collectAsStateWithLifecycle()

    val sevenDaysAgo = remember { System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000 }
    val recentLogs = remember(adherenceLogs, sevenDaysAgo) {
        adherenceLogs.filter { it.scheduledTime >= sevenDaysAgo }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("medication_adherence_weekly_card"),
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, SoftTheme.MintTeal.copy(alpha = 0.25f))
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
                    Text("📊", fontSize = 20.sp)
                    Column {
                        Text(
                            text = "نسبة الالتزام بالأدوية (آخر 7 أيام) 💊",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.TextWhite
                        )
                        Text(
                            text = "تحليل الجرعات المتناولة في موعدها بناءً على سجلات التنبيهات الفعلية",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTheme.SoftGray,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (medications.isEmpty()) {
                Text(
                    text = "أضيفي أدوية موصوفة لتتبع نسبة الالتزام الأسبوعية بها تلقائياً.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SoftTheme.SoftGray
                )
            } else {
                val activeMeds = medications.filter { it.isActive }
                if (activeMeds.isEmpty()) {
                    Text(
                        text = "لا توجد أدوية نشطة تـُتـَبّع حالياً.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTheme.SoftGray
                    )
                } else {
                    activeMeds.forEach { med ->
                        val medLogs = recentLogs.filter { it.medicationId == med.id }
                        val takenLogs = medLogs.count { it.status == "TAKEN" }
                        val totalLogs = medLogs.size
                        val percentage = if (totalLogs > 0) (takenLogs * 100 / totalLogs) else 100

                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = med.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftTheme.TextWhite
                                )
                                Text(
                                    text = if (totalLogs > 0) "$percentage% ($takenLogs/$totalLogs جرعة)" else "100% (ممتاز ✨)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (percentage >= 80) SoftTheme.MintTeal else SoftTheme.SoftPink
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { (percentage / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (percentage >= 80) SoftTheme.MintTeal else SoftTheme.SoftPink,
                                trackColor = SoftTheme.DeepSlate
                            )
                        }
                    }
                }
            }
        }
    }
}
