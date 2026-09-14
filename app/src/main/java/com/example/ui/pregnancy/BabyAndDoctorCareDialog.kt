package com.example.ui.pregnancy

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Straighten
import com.example.data.Appointment
import com.example.data.FetalGrowthLog
import com.example.ui.FetalStandardData
import com.example.ui.SoftTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BabyAndDoctorCareDialog(
    initialGender: String?,
    initialName: String?,
    currentWeekNumber: Int,
    fetalGrowthLogs: List<FetalGrowthLog> = emptyList(),
    appointments: List<Appointment> = emptyList(),
    onDismissRequest: () -> Unit,
    onSaveBabyInfo: (gender: String?, name: String?) -> Unit,
    onAddFetalGrowth: (week: Int, weightGrams: Double, lengthCm: Double, notes: String?) -> Unit,
    onAddDoctorAppointment: (title: String, dateTime: Long, doctor: String?, notes: String?) -> Unit,
    onToggleAppointment: ((Appointment) -> Unit)? = null
) {
    val context = LocalContext.current
    var activeTab by remember { mutableIntStateOf(0) } // 0: بيانات الجنين، 1: قياسات السونار، 2: موعد الطبيب

    // --- Tab 0: Baby Info State ---
    var babyGender by remember { mutableStateOf(initialGender ?: "") }
    var babyName by remember { mutableStateOf(initialName ?: "") }

    // --- Tab 1: Fetal Growth State ---
    val standardData = remember(currentWeekNumber) {
        FetalStandardData.getStandardForWeek(currentWeekNumber)
    }
    var weekNumberInput by remember { mutableStateOf(currentWeekNumber.coerceIn(1, 42).toString()) }
    var weightInput by remember { mutableStateOf("") }
    var lengthInput by remember { mutableStateOf("") }
    var growthNotes by remember { mutableStateOf("") }

    // --- Tab 2: Doctor Appointment State ---
    val calendar = remember { Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 7); set(Calendar.HOUR_OF_DAY, 11); set(Calendar.MINUTE, 0) } }
    var apptDateMs by remember { mutableLongStateOf(calendar.timeInMillis) }
    var doctorName by remember { mutableStateOf("") }
    var apptTitle by remember { mutableStateOf("متابعة فحص الحمل وسونار الدكتورة") }
    var apptNotes by remember { mutableStateOf("") }

    var showSuccessToast by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .testTag("baby_and_doctor_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardBg),
            border = BorderStroke(1.dp, SoftTheme.CardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = SoftTheme.TextSecondaryMuted
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "ملف رعاية الجنين والطبيبة 🩺👶",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.TextPrimary
                        )
                        Text(
                            text = "الأسبوع الحالي: $currentWeekNumber",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTheme.MintTeal
                        )
                    }
                    Spacer(modifier = Modifier.size(36.dp))
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Navigation Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SoftTheme.CanvasBg)
                        .border(1.dp, SoftTheme.CardBorder, RoundedCornerShape(14.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val tabs = listOf(
                        "بيانات الجنين 👶",
                        "قياسات السونار 📏",
                        "موعد الطبيبة 🏥"
                    )
                    tabs.forEachIndexed { index, label ->
                        val isSelected = activeTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) SoftTheme.EmeraldPrimary else Color.Transparent)
                                .clickable { activeTab = index },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else SoftTheme.TextSecondaryMuted,
                                    fontSize = 11.sp
                                ),
                                maxLines = 1,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (activeTab) {
                        0 -> {
                            // --- TAB 0: Baby Information ---
                            Text(
                                text = "تحديد جنس الجنين المقترح واسمه 🌸",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = SoftTheme.TextPrimary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val genders = listOf(
                                    Triple("ولد", "ولد صالح 💙", "👶"),
                                    Triple("بنت", "بنت صالحة 💗", "👧"),
                                    Triple("مفاجأة", "مفاجأة ✨", "🤫")
                                )
                                genders.forEach { (key, label, emoji) ->
                                    val isSelected = babyGender == key
                                    OutlinedCard(
                                        onClick = { babyGender = key },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.outlinedCardColors(
                                            containerColor = if (isSelected) SoftTheme.MintAccent else SoftTheme.CardBg
                                        ),
                                        border = BorderStroke(
                                            if (isSelected) 1.5.dp else 1.dp,
                                            if (isSelected) SoftTheme.EmeraldPrimary else SoftTheme.CardBorder
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(emoji, fontSize = 22.sp)
                                            Text(
                                                text = label,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) SoftTheme.EmeraldPrimary else SoftTheme.TextPrimary
                                            )
                                        }
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = babyName,
                                onValueChange = { babyName = it },
                                label = { Text("الاسم المقترح للجنين (اختياري)") },
                                placeholder = { Text("مثال: يوسف، مريم، عمر...") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("baby_name_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SoftTheme.EmeraldPrimary,
                                    unfocusedBorderColor = SoftTheme.CardBorder,
                                    focusedLabelColor = SoftTheme.EmeraldPrimary,
                                    unfocusedLabelColor = SoftTheme.TextSecondaryMuted
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SoftTheme.MintAccent.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("💡", fontSize = 18.sp)
                                    Text(
                                        text = "الاسم المسجل هنا سيتفاعل معه رفيقكِ جوري ومذكرات الحمل لتخصيص البطاقات ومتابعة النمو باسم طفلكِ.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SoftTheme.TealDark,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }

                        1 -> {
                            // --- TAB 1: Fetal Growth & Ultrasound ---
                            Text(
                                text = "تسجيل قياسات السونار الأخيرة 📏🩺",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = SoftTheme.TextPrimary
                            )

                            // Quick autofill banner using reference standard
                            standardData?.let { std ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = SoftTheme.CanvasBg),
                                    border = BorderStroke(1.dp, SoftTheme.CardBorder),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "المتوسط الطبيعي للأسبوع $currentWeekNumber:",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = SoftTheme.TextPrimary
                                            )
                                            TextButton(
                                                onClick = {
                                                    weightInput = std.weightGrams.toInt().toString()
                                                    lengthInput = std.lengthCm.toString()
                                                },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text("استخدام المتوسط ✨", fontSize = 11.sp, color = SoftTheme.EmeraldPrimary)
                                            }
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceAround
                                        ) {
                                            Text("⚖️ الوزن: ~${std.weightGrams.toInt()} جم", fontSize = 12.sp, color = SoftTheme.TextSecondaryMuted)
                                            Text("📏 الطول: ~${std.lengthCm} سم", fontSize = 12.sp, color = SoftTheme.TextSecondaryMuted)
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = weightInput,
                                    onValueChange = { if (it.all { ch -> ch.isDigit() || ch == '.' }) weightInput = it },
                                    label = { Text("الوزن (جرام)") },
                                    placeholder = { Text("مثال: 450") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("fetal_weight_input"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = SoftTheme.EmeraldPrimary,
                                        unfocusedBorderColor = SoftTheme.CardBorder
                                    )
                                )

                                OutlinedTextField(
                                    value = lengthInput,
                                    onValueChange = { if (it.all { ch -> ch.isDigit() || ch == '.' }) lengthInput = it },
                                    label = { Text("الطول (سم)") },
                                    placeholder = { Text("مثال: 24.5") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("fetal_length_input"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = SoftTheme.EmeraldPrimary,
                                        unfocusedBorderColor = SoftTheme.CardBorder
                                    )
                                )
                            }

                            OutlinedTextField(
                                value = growthNotes,
                                onValueChange = { growthNotes = it },
                                label = { Text("ملاحظات تقرير السونار (اختياري)") },
                                placeholder = { Text("مثال: نبض ممتاز، حركة نشطة، المشيمة أمامية...") },
                                minLines = 2,
                                maxLines = 3,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SoftTheme.EmeraldPrimary,
                                    unfocusedBorderColor = SoftTheme.CardBorder
                                )
                            )

                            // --- Past Fetal Logs List ---
                            if (fetalGrowthLogs.isNotEmpty()) {
                                HorizontalDivider(color = SoftTheme.CardBorder.copy(alpha = 0.6f), thickness = 1.dp)
                                Text(
                                    text = "سجل فحوصات السونار السابقة 📜",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftTheme.TextPrimary
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    fetalGrowthLogs.sortedByDescending { it.pregnancyWeek }.forEach { log ->
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = SoftTheme.CanvasBg,
                                            border = BorderStroke(1.dp, SoftTheme.CardBorder),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "الأسبوع ${log.pregnancyWeek}",
                                                        fontWeight = FontWeight.Bold,
                                                        color = SoftTheme.EmeraldPrimary,
                                                        fontSize = 13.sp
                                                    )
                                                    val dateLabel = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(log.date))
                                                    Text(
                                                        text = dateLabel,
                                                        fontSize = 10.sp,
                                                        color = SoftTheme.TextSecondaryMuted
                                                    )
                                                }
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                                ) {
                                                    if (log.weightGrams > 0) {
                                                        Text(
                                                            text = "⚖️ ${log.weightGrams.toInt()} جم",
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = SoftTheme.TextPrimary
                                                        )
                                                    }
                                                    if (log.lengthCm > 0) {
                                                        Text(
                                                            text = "📏 ${log.lengthCm} سم",
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = SoftTheme.TextPrimary
                                                        )
                                                    }
                                                }
                                                if (!log.notes.isNullOrBlank()) {
                                                    Text(
                                                        text = "📝 ${log.notes}",
                                                        fontSize = 11.sp,
                                                        color = SoftTheme.TextSecondaryMuted
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        2 -> {
                            // --- TAB 2: Doctor Appointment ---
                            Text(
                                text = "حجز وتذكير موعد زيارة الطبيبة 🏥",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = SoftTheme.TextPrimary
                            )

                            OutlinedTextField(
                                value = apptTitle,
                                onValueChange = { apptTitle = it },
                                label = { Text("عنوان الموعد / الغرض") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SoftTheme.EmeraldPrimary,
                                    unfocusedBorderColor = SoftTheme.CardBorder
                                )
                            )

                            OutlinedTextField(
                                value = doctorName,
                                onValueChange = { doctorName = it },
                                label = { Text("اسم الطبيبة / العيادة (اختياري)") },
                                placeholder = { Text("مثال: د. سارة - مركز النخبة") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SoftTheme.EmeraldPrimary,
                                    unfocusedBorderColor = SoftTheme.CardBorder
                                )
                            )

                            // Date & Time Picker Triggers
                            val dateStr = SimpleDateFormat("EEEE، dd MMMM yyyy", Locale.forLanguageTag("ar")).format(Date(apptDateMs))
                            val timeStr = SimpleDateFormat("hh:mm a", Locale.forLanguageTag("ar")).format(Date(apptDateMs))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedCard(
                                    onClick = {
                                        val cal = Calendar.getInstance().apply { timeInMillis = apptDateMs }
                                        DatePickerDialog(
                                            context,
                                            { _, y, m, d ->
                                                cal.set(Calendar.YEAR, y)
                                                cal.set(Calendar.MONTH, m)
                                                cal.set(Calendar.DAY_OF_MONTH, d)
                                                apptDateMs = cal.timeInMillis
                                            },
                                            cal.get(Calendar.YEAR),
                                            cal.get(Calendar.MONTH),
                                            cal.get(Calendar.DAY_OF_MONTH)
                                        ).show()
                                    },
                                    modifier = Modifier.weight(1.3f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, SoftTheme.CardBorder)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = SoftTheme.EmeraldPrimary, modifier = Modifier.size(18.dp))
                                        Column {
                                            Text("التاريخ", fontSize = 10.sp, color = SoftTheme.TextSecondaryMuted)
                                            Text(dateStr, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SoftTheme.TextPrimary, maxLines = 1)
                                        }
                                    }
                                }

                                OutlinedCard(
                                    onClick = {
                                        val cal = Calendar.getInstance().apply { timeInMillis = apptDateMs }
                                        TimePickerDialog(
                                            context,
                                            { _, hour, minute ->
                                                cal.set(Calendar.HOUR_OF_DAY, hour)
                                                cal.set(Calendar.MINUTE, minute)
                                                apptDateMs = cal.timeInMillis
                                            },
                                            cal.get(Calendar.HOUR_OF_DAY),
                                            cal.get(Calendar.MINUTE),
                                            false
                                        ).show()
                                    },
                                    modifier = Modifier.weight(0.9f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, SoftTheme.CardBorder)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Default.Schedule, contentDescription = null, tint = SoftTheme.EmeraldPrimary, modifier = Modifier.size(18.dp))
                                        Column {
                                            Text("الوقت", fontSize = 10.sp, color = SoftTheme.TextSecondaryMuted)
                                            Text(timeStr, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SoftTheme.TextPrimary)
                                        }
                                    }
                                }
                            }

                            // Reminder pill indicator
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = SoftTheme.MintAccent,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = null,
                                        tint = SoftTheme.EmeraldPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "سيتم تفعيل تنبيه إشعار تلقائي قبل الموعد بـ 24 ساعة وساعتين 🔔",
                                        fontSize = 11.sp,
                                        color = SoftTheme.TealDark,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = apptNotes,
                                onValueChange = { apptNotes = it },
                                label = { Text("ملاحظات وأسئلة للطبيبة (اختياري)") },
                                placeholder = { Text("مثال: السؤال عن الفيتامينات، فحص السكر...") },
                                minLines = 2,
                                maxLines = 3,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SoftTheme.EmeraldPrimary,
                                    unfocusedBorderColor = SoftTheme.CardBorder
                                )
                            )

                            // --- List of All Appointments ---
                            if (appointments.isNotEmpty()) {
                                HorizontalDivider(color = SoftTheme.CardBorder.copy(alpha = 0.6f), thickness = 1.dp)
                                Text(
                                    text = "قائمة المواعيد والزيارات المسجلة 📅",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftTheme.TextPrimary
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    appointments.sortedBy { it.dateTime }.forEach { apptItem ->
                                        val isUpcoming = apptItem.dateTime >= System.currentTimeMillis()
                                        val itemDateStr = SimpleDateFormat("EEEE، dd MMMM - hh:mm a", Locale.forLanguageTag("ar")).format(Date(apptItem.dateTime))
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (apptItem.completed) SoftTheme.CanvasBg.copy(alpha = 0.6f) else SoftTheme.MintAccent.copy(alpha = 0.4f),
                                            border = BorderStroke(1.dp, SoftTheme.CardBorder),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Text(
                                                            text = apptItem.title,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 12.sp,
                                                            color = SoftTheme.TextPrimary
                                                        )
                                                        if (apptItem.completed) {
                                                            Surface(
                                                                shape = RoundedCornerShape(6.dp),
                                                                color = SoftTheme.EmeraldPrimary.copy(alpha = 0.15f)
                                                            ) {
                                                                Text("تمت الزيارة ✓", fontSize = 9.sp, color = SoftTheme.EmeraldPrimary, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                                            }
                                                        }
                                                    }
                                                    if (!apptItem.doctorName.isNullOrBlank()) {
                                                        Text(
                                                            text = "مع: ${apptItem.doctorName}",
                                                            fontSize = 11.sp,
                                                            color = SoftTheme.EmeraldPrimary
                                                        )
                                                    }
                                                    Text(
                                                        text = itemDateStr,
                                                        fontSize = 10.sp,
                                                        color = SoftTheme.TextSecondaryMuted
                                                    )
                                                }
                                                if (onToggleAppointment != null) {
                                                    Checkbox(
                                                        checked = apptItem.completed,
                                                        onCheckedChange = { onToggleAppointment(apptItem) },
                                                        colors = CheckboxDefaults.colors(
                                                            checkedColor = SoftTheme.EmeraldPrimary,
                                                            uncheckedColor = SoftTheme.CardBorder
                                                        )
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

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, SoftTheme.CardBorder)
                    ) {
                        Text("إلغاء", color = SoftTheme.TextSecondaryMuted)
                    }

                    Button(
                        onClick = {
                            // 1. Save Baby Gender & Name if present
                            if (babyGender.isNotBlank() || babyName.isNotBlank()) {
                                onSaveBabyInfo(babyGender.ifBlank { null }, babyName.ifBlank { null })
                            }

                            // 2. Save Fetal Growth Log if weight or length entered
                            val w = weightInput.toDoubleOrNull() ?: 0.0
                            val l = lengthInput.toDoubleOrNull() ?: 0.0
                            val wk = weekNumberInput.toIntOrNull() ?: currentWeekNumber
                            if (w > 0.0 || l > 0.0) {
                                onAddFetalGrowth(wk, w, l, growthNotes.ifBlank { null })
                            }

                            // 3. Save Appointment if activeTab was appointment or title was filled
                            if (activeTab == 2 && apptTitle.isNotBlank()) {
                                onAddDoctorAppointment(
                                    apptTitle.trim(),
                                    apptDateMs,
                                    doctorName.ifBlank { null },
                                    apptNotes.ifBlank { null }
                                )
                            }

                            onDismissRequest()
                        },
                        modifier = Modifier
                            .weight(1.6f)
                            .testTag("save_baby_doctor_care_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.EmeraldPrimary)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("حفظ التحديثات ✨", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
