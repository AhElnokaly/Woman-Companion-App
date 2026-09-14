package com.example.ui.period

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.formatGregorianDate
import com.example.ui.SoftTheme
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PeriodLogDialog(
    initialStartDate: Long = System.currentTimeMillis(),
    initialEndDate: Long = System.currentTimeMillis() + 5L * 24 * 60 * 60 * 1000,
    useSpecificDateRangeInitial: Boolean = false,
    isPregnant: Boolean = false,
    onSave: (startDate: Long, endDate: Long, intensity: String, symptoms: List<String>, painLevel: Int, notes: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectStartToday by remember { mutableStateOf(true) }
    var useSpecificDateRange by remember { mutableStateOf(useSpecificDateRangeInitial) }
    var useCustomStartDate by remember { mutableStateOf(initialStartDate) }
    var useCustomEndDate by remember { mutableStateOf(initialEndDate) }
    var selectedIntensity by remember { mutableStateOf("medium") }
    var painLevel by remember { mutableIntStateOf(5) }
    var notesInput by remember { mutableStateOf("") }
    val selectedSymptoms = remember { mutableStateListOf<String>() }

    val symptomsList = remember { listOf("مغص", "إرهاق", "صداع", "تقلب مزاجي", "ألم ظهر") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("add_period_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (isPregnant) "تسجيل دورة شهرية سابقة 📅" else "تسجيل تفاصيل الدورة الشهرية 🩸",
                    style = MaterialTheme.typography.titleMedium,
                    color = SoftTheme.SoftPink
                )

                // Toggle for selecting past cycle dates or quick today/yesterday
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تحديد تواريخ مخصصة (سابقاً):",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTheme.SoftGray
                    )
                    Switch(
                        checked = useSpecificDateRange,
                        onCheckedChange = { useSpecificDateRange = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SoftTheme.SoftPink,
                            checkedTrackColor = SoftTheme.DeepSlate
                        )
                    )
                }

                if (useSpecificDateRange) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("تاريخ بداية الدورة:", color = SoftTheme.TextWhite, style = MaterialTheme.typography.bodySmall)
                        Button(
                            onClick = {
                                val calendar = Calendar.getInstance().apply { timeInMillis = useCustomStartDate }
                                val d = DatePickerDialog(
                                    context,
                                    { _, y, m, day ->
                                        val cal = Calendar.getInstance().apply {
                                            set(y, m, day, 0, 0, 0)
                                        }
                                        useCustomStartDate = cal.timeInMillis
                                        if (useCustomEndDate < useCustomStartDate) {
                                            useCustomEndDate = useCustomStartDate + 5L * 24 * 60 * 60 * 1000
                                        }
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                )
                                d.datePicker.maxDate = System.currentTimeMillis()
                                d.show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.DeepSlate)
                        ) {
                            Text(formatGregorianDate(useCustomStartDate), color = SoftTheme.TextWhite)
                        }

                        Text("تاريخ نهاية الدورة:", color = SoftTheme.TextWhite, style = MaterialTheme.typography.bodySmall)
                        Button(
                            onClick = {
                                val calendar = Calendar.getInstance().apply { timeInMillis = useCustomEndDate }
                                val d2 = DatePickerDialog(
                                    context,
                                    { _, y, m, day ->
                                        val cal = Calendar.getInstance().apply {
                                            set(y, m, day, 23, 59, 59)
                                        }
                                        useCustomEndDate = cal.timeInMillis
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                )
                                d2.datePicker.maxDate = System.currentTimeMillis()
                                d2.show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.DeepSlate)
                        ) {
                            Text(formatGregorianDate(useCustomEndDate), color = SoftTheme.TextWhite)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { selectStartToday = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectStartToday) SoftTheme.SoftPink else SoftTheme.DeepSlate,
                                contentColor = if (selectStartToday) SoftTheme.DeepSlate else SoftTheme.TextWhite
                            )
                        ) {
                            Text("اليوم")
                        }
                        Button(
                            onClick = { selectStartToday = false },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!selectStartToday) SoftTheme.SoftPink else SoftTheme.DeepSlate,
                                contentColor = if (!selectStartToday) SoftTheme.DeepSlate else SoftTheme.TextWhite
                            )
                        ) {
                            Text("أمس")
                        }
                    }
                }

                Text("غزارة الطمث:", color = SoftTheme.TextWhite)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val intensities = listOf("light" to "خفيفة", "medium" to "متوسطة", "heavy" to "غزيرة")
                    intensities.forEach { (key, label) ->
                        val isSelected = selectedIntensity == key
                        Button(
                            onClick = { selectedIntensity = key },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) SoftTheme.SoftPink else SoftTheme.DeepSlate,
                                contentColor = if (isSelected) SoftTheme.DeepSlate else SoftTheme.TextWhite
                            )
                        ) {
                            Text(label, fontSize = 12.sp)
                        }
                    }
                }

                Text("مستوى الألم: $painLevel/10", color = SoftTheme.TextWhite)
                Slider(
                    value = painLevel.toFloat(),
                    onValueChange = { painLevel = it.toInt() },
                    valueRange = 1f..10f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor = SoftTheme.SoftPink,
                        activeTrackColor = SoftTheme.SoftPink,
                        inactiveTrackColor = SoftTheme.DeepSlate
                    )
                )

                Text("الأعراض المرافقة:", color = SoftTheme.TextWhite)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    symptomsList.forEach { sym ->
                        val isChecked = selectedSymptoms.contains(sym)
                        FilterChip(
                            selected = isChecked,
                            onClick = {
                                if (isChecked) selectedSymptoms.remove(sym)
                                else selectedSymptoms.add(sym)
                            },
                            label = { Text(sym) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SoftTheme.SoftPink,
                                selectedLabelColor = SoftTheme.DeepSlate
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("ملاحظات خاصة...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SoftTheme.SoftPink,
                        unfocusedBorderColor = SoftTheme.SoftGray,
                        focusedTextColor = SoftTheme.TextWhite,
                        unfocusedTextColor = SoftTheme.TextWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إلغاء", color = SoftTheme.SoftGray)
                    }

                    Button(
                        onClick = {
                            val start = if (useSpecificDateRange) {
                                useCustomStartDate
                            } else {
                                if (selectStartToday) {
                                    System.currentTimeMillis()
                                } else {
                                    System.currentTimeMillis() - 24 * 60 * 60 * 1000
                                }
                            }
                            val end = if (useSpecificDateRange) {
                                useCustomEndDate
                            } else {
                                start + 5L * 24 * 60 * 60 * 1000
                            }
                            onSave(start, end, selectedIntensity, selectedSymptoms.toList(), painLevel, notesInput)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("حفظ")
                    }
                }
            }
        }
    }
}
