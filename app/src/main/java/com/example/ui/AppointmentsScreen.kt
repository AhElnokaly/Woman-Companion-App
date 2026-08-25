package com.example.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.WomanCompanionViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun AppointmentsSubScreen(viewModel: WomanCompanionViewModel) {
    val appointments by viewModel.appointmentsState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showAddApptDialog by remember { mutableStateOf(false) }
    var titleInput by remember { mutableStateOf("") }
    var doctorInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }
    var selectedDateTimeMillis by remember { mutableStateOf(System.currentTimeMillis() + 3L * 24 * 60 * 60 * 1000) }

    val dateTimeFormatter = remember { SimpleDateFormat("EEEE d MMMM yyyy - hh:mm a", Locale("ar")) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("جدول زيارات ومواعيد الدكتورة 🏥", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)

        // بطاقة توضيحية لنظام التنبيهات والإنذارات المتقدم
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, SoftTheme.MintTeal.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = SoftTheme.MintTeal,
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(
                        text = "نظام التنبيه الذكي للزيارات الطبية ⏰✨",
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.TextWhite,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "يتم تنبيهكِ استباقياً قبل الموعد بـ ٢٤ ساعة، ثم قبل الموعد بساعتين للتجهيز، ثم في نفس وقت الموعد لتتذكري تحاليلكِ وأسئلتكِ للدكتورة 🌸",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTheme.SoftGray,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Button(
            onClick = {
                selectedDateTimeMillis = System.currentTimeMillis() + 3L * 24 * 60 * 60 * 1000
                showAddApptDialog = true
            },
            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
            modifier = Modifier.fillMaxWidth().testTag("add_appt_btn")
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("إضافة موعد كشف جديد")
        }

        if (appointments.isEmpty()) {
            Text("لا توجد مواعيد مسجلة بعد.", color = SoftTheme.SoftGray)
        } else {
            appointments.forEach { appt ->
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
                                text = appt.title,
                                fontWeight = FontWeight.Bold,
                                color = if (appt.completed) SoftTheme.SoftGray else SoftTheme.TextWhite
                            )
                            appt.doctorName?.let {
                                Text("مع: د. $it", style = MaterialTheme.typography.bodySmall, color = SoftTheme.MintTeal)
                            }
                            appt.notes?.let {
                                Text("تفاصيل: $it", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                            }
                            Text(
                                text = "📅 ${dateTimeFormatter.format(appt.dateTime)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftTheme.SoftPink
                            )
                            if (!appt.completed && appt.dateTime > System.currentTimeMillis()) {
                                Text(
                                    text = "🔔 المنبه الذكي مفعّل (قبل يوم + قبل ساعتين + عند الموعد)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SoftTheme.MintTeal,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Checkbox(
                                checked = appt.completed,
                                onCheckedChange = { viewModel.toggleAppointmentCompleted(appt) },
                                colors = CheckboxDefaults.colors(checkedColor = SoftTheme.MintTeal)
                            )
                            IconButton(onClick = { viewModel.deleteAppointment(appt) }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = SoftTheme.RedDanger)
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(80.dp))
    }

    if (showAddApptDialog) {
        Dialog(onDismissRequest = { showAddApptDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("إضافة موعد طبي 🏥", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SoftTheme.SoftPink)

                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("عنوان الموعد (مثال: سونار الثلث الثاني)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = doctorInput,
                        onValueChange = { doctorInput = it },
                        label = { Text("اسم الطبيبة") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        label = { Text("ملاحظات الكشف أو التحاليل المطلوبة") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // اختيار التاريخ والوقت المخصص
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "موعد الكشف والزيارة 🗓️⏰:",
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftTheme.SoftGray
                            )
                            Text(
                                text = dateTimeFormatter.format(selectedDateTimeMillis),
                                fontWeight = FontWeight.Bold,
                                color = SoftTheme.SoftPink,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        val cal = Calendar.getInstance().apply { timeInMillis = selectedDateTimeMillis }
                                        DatePickerDialog(
                                            context,
                                            { _, y, m, d ->
                                                cal.set(Calendar.YEAR, y)
                                                cal.set(Calendar.MONTH, m)
                                                cal.set(Calendar.DAY_OF_MONTH, d)
                                                selectedDateTimeMillis = cal.timeInMillis
                                            },
                                            cal.get(Calendar.YEAR),
                                            cal.get(Calendar.MONTH),
                                            cal.get(Calendar.DAY_OF_MONTH)
                                        ).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("تغيير اليوم", fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        val cal = Calendar.getInstance().apply { timeInMillis = selectedDateTimeMillis }
                                        TimePickerDialog(
                                            context,
                                            { _, hour, minute ->
                                                cal.set(Calendar.HOUR_OF_DAY, hour)
                                                cal.set(Calendar.MINUTE, minute)
                                                cal.set(Calendar.SECOND, 0)
                                                selectedDateTimeMillis = cal.timeInMillis
                                            },
                                            cal.get(Calendar.HOUR_OF_DAY),
                                            cal.get(Calendar.MINUTE),
                                            false
                                        ).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("تغيير الساعة", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    // اختصارات سريعة للمواعيد
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SuggestionChip(
                            onClick = {
                                val cal = Calendar.getInstance()
                                cal.add(Calendar.DAY_OF_YEAR, 1)
                                cal.set(Calendar.HOUR_OF_DAY, 18)
                                cal.set(Calendar.MINUTE, 0)
                                selectedDateTimeMillis = cal.timeInMillis
                            },
                            label = { Text("غداً ٦م", fontSize = 11.sp) }
                        )
                        SuggestionChip(
                            onClick = {
                                val cal = Calendar.getInstance()
                                cal.add(Calendar.DAY_OF_YEAR, 3)
                                cal.set(Calendar.HOUR_OF_DAY, 18)
                                cal.set(Calendar.MINUTE, 0)
                                selectedDateTimeMillis = cal.timeInMillis
                            },
                            label = { Text("بعد ٣ أيام", fontSize = 11.sp) }
                        )
                        SuggestionChip(
                            onClick = {
                                val cal = Calendar.getInstance()
                                cal.add(Calendar.WEEK_OF_YEAR, 1)
                                cal.set(Calendar.HOUR_OF_DAY, 18)
                                cal.set(Calendar.MINUTE, 0)
                                selectedDateTimeMillis = cal.timeInMillis
                            },
                            label = { Text("بعد أسبوع", fontSize = 11.sp) }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TextButton(onClick = { showAddApptDialog = false }, modifier = Modifier.weight(1f)) {
                            Text("إلغاء", color = SoftTheme.SoftGray)
                        }
                        Button(
                            onClick = {
                                if (titleInput.isNotBlank()) {
                                    viewModel.addAppointment(
                                        title = titleInput,
                                        dateTime = selectedDateTimeMillis,
                                        doctor = doctorInput.ifBlank { null },
                                        notes = notesInput.ifBlank { null }
                                    )
                                    showAddApptDialog = false
                                    titleInput = ""
                                    doctorInput = ""
                                    notesInput = ""
                                }
                            },
                            enabled = titleInput.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("حفظ وتفعيل المنبه 🔔")
                        }
                    }
                }
            }
        }
    }
}

