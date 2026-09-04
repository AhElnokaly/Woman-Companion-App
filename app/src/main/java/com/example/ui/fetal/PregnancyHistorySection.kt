package com.example.ui.fetal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.ui.SoftTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PregnancyHistorySection(
    allPregnancies: List<PregnancyEntity>,
    allLogs: List<FetalGrowthLog>,
    onStartNewPregnancy: () -> Unit
) {
    var expandedPregnancyId by remember { mutableStateOf<Int?>(null) }
    val dateFormatter = remember { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }

    Card(
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth().testTag("pregnancy_history_card")
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("📜", fontSize = 20.sp)
                    Text(
                        text = "سجل الأحمال والقياسات السابقة",
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.TextWhite,
                        fontSize = 16.sp
                    )
                }
                Button(
                    onClick = onStartNewPregnancy,
                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("start_new_pregnancy_btn")
                ) {
                    Text("حمل جديد ➕", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (allPregnancies.isEmpty()) {
                Text(
                    text = "لا يوجد سجل أحمال سابقة محتفظ به.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SoftTheme.SoftGray
                )
            } else {
                allPregnancies.forEach { preg ->
                    val isExpanded = expandedPregnancyId == preg.id
                    val pregLogs = allLogs.filter { it.pregnancyId == preg.id }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = if (preg.isActive) "الحمل الحالي 🤰" else "حمل سابق 👶",
                                            fontWeight = FontWeight.Bold,
                                            color = if (preg.isActive) SoftTheme.MintTeal else SoftTheme.SoftPink,
                                            fontSize = 14.sp
                                        )
                                        if (!preg.babyName.isNullOrBlank()) {
                                            Text(
                                                text = "(${preg.babyName})",
                                                color = SoftTheme.TextWhite,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    if (preg.lastPeriodDate != null) {
                                        Text(
                                            text = "تاريخ الدورة الأخيرة: ${dateFormatter.format(Date(preg.lastPeriodDate))}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SoftTheme.SoftGray
                                        )
                                    }
                                    if (preg.dueDate != null) {
                                        Text(
                                            text = "تاريخ الولادة المتوقع: ${dateFormatter.format(Date(preg.dueDate))}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SoftTheme.SoftGray
                                        )
                                    }
                                }

                                TextButton(onClick = { expandedPregnancyId = if (isExpanded) null else preg.id }) {
                                    Text(
                                        text = if (isExpanded) "إخفاء 🔼" else "القياسات (${pregLogs.size}) 🔽",
                                        color = SoftTheme.GoldFasting,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (isExpanded) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = SoftTheme.CardSlate)
                                if (pregLogs.isEmpty()) {
                                    Text(
                                        text = "لم تسجل قياسات سونار لهذا الحمل.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SoftTheme.SoftGray,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                } else {
                                    pregLogs.sortedBy { it.pregnancyWeek }.forEach { l ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "أسبوع ${l.pregnancyWeek}: ${l.weightGrams.toInt()} جرام | ${l.lengthCm} سم",
                                                color = SoftTheme.TextWhite,
                                                fontSize = 12.sp
                                            )
                                            if (!l.notes.isNullOrBlank()) {
                                                Text(
                                                    text = l.notes,
                                                    color = SoftTheme.SoftGray,
                                                    fontSize = 11.sp
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
}

@Composable
fun StartNewPregnancyDialog(
    onDismiss: () -> Unit,
    onConfirm: (lastPeriodDateMs: Long, babyName: String?) -> Unit
) {
    var babyNameInput by remember { mutableStateOf("") }
    var selectedDateMs by remember { mutableStateOf(System.currentTimeMillis() - 28L * 86400000L) }
    val dateFormatter = remember { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "بدء تسجيل حمل جديد 🌸",
                fontWeight = FontWeight.Bold,
                color = SoftTheme.TextWhite,
                fontSize = 18.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "سيتم حفظ بيانات وسجلات الحمل السابق تلقائياً في السجل التاريخي وبدء متابعة مخصصة للحمل الجديد.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SoftTheme.SoftGray
                )

                OutlinedTextField(
                    value = babyNameInput,
                    onValueChange = { babyNameInput = it },
                    label = { Text("اسم المولود المتوقع (اختياري)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SoftTheme.SoftPink,
                        unfocusedBorderColor = SoftTheme.SoftGray,
                        focusedLabelColor = SoftTheme.SoftPink,
                        unfocusedLabelColor = SoftTheme.SoftGray,
                        focusedTextColor = SoftTheme.TextWhite,
                        unfocusedTextColor = SoftTheme.TextWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("تاريخ أول يوم من آخر دورة (LMP):", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                        Text(dateFormatter.format(Date(selectedDateMs)), fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
                    }
                    Button(
                        onClick = {
                            val cal = java.util.Calendar.getInstance()
                            cal.timeInMillis = selectedDateMs
                            android.app.DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    val newCal = java.util.Calendar.getInstance()
                                    newCal.set(year, month, dayOfMonth, 0, 0, 0)
                                    selectedDateMs = newCal.timeInMillis
                                },
                                cal.get(java.util.Calendar.YEAR),
                                cal.get(java.util.Calendar.MONTH),
                                cal.get(java.util.Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.CardSlate)
                    ) {
                        Text("تغيير 📅", color = SoftTheme.SoftPink, fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(selectedDateMs, babyNameInput.ifBlank { null })
                },
                colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink)
            ) {
                Text("تأكيد وبدء الحمل 🌸", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = SoftTheme.SoftGray)
            }
        },
        containerColor = SoftTheme.CardSlate
    )
}
