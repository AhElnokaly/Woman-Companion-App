package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.SoftTheme
import com.example.viewmodel.WomanCompanionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExactAlarmBannerCard() {
    val context = LocalContext.current
    val alarmManager = remember { context.getSystemService(android.content.Context.ALARM_SERVICE) as? android.app.AlarmManager }
    val canScheduleExact = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        alarmManager?.canScheduleExactAlarms() ?: true
    } else {
        true
    }

    if (!canScheduleExact) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SoftTheme.GoldFasting.copy(alpha = 0.15f)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, SoftTheme.GoldFasting)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("⚠️", fontSize = 22.sp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "تنبيه الإشعارات والمنبهات الدقيقة ⏰",
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.TextWhite,
                        fontSize = 13.sp
                    )
                    Text(
                        "يرجى السماح بإذن المنبهات الدقيقة لضمان وصول تذكيرات الأدوية والمواعيد في وقتها المظبوط دون تأخير.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTheme.SoftGray,
                        lineHeight = 15.sp
                    )
                }
                Button(
                    onClick = {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                            try {
                                val intent = android.content.Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                    data = android.net.Uri.parse("package:${context.packageName}")
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                val intent = android.content.Intent(android.provider.Settings.ACTION_SETTINGS)
                                context.startActivity(intent)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.GoldFasting),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("تفعيل", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DailyVitaminsCard(
    viewModel: WomanCompanionViewModel,
    onNavigateToMeds: () -> Unit = {}
) {
    val context = LocalContext.current
    val todayDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    val prefs = remember { context.getSharedPreferences("daily_meds_prefs", android.content.Context.MODE_PRIVATE) }

    var folicChecked by remember(todayDateStr) { mutableStateOf(prefs.getBoolean("med_folic_$todayDateStr", false)) }
    var ironChecked by remember(todayDateStr) { mutableStateOf(prefs.getBoolean("med_iron_$todayDateStr", false)) }
    var calciumChecked by remember(todayDateStr) { mutableStateOf(prefs.getBoolean("med_calcium_$todayDateStr", false)) }
    var multivitaminChecked by remember(todayDateStr) { mutableStateOf(prefs.getBoolean("med_multi_$todayDateStr", false)) }

    Card(
        modifier = Modifier.fillMaxWidth().testTag("daily_vitamins_card"),
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("💊", fontSize = 22.sp)
                    Text(
                        "الفيتامينات والأدوية اليومية",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.TextWhite
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    val completedCount = listOf(folicChecked, ironChecked, calciumChecked, multivitaminChecked).count { it }
                    Text(
                        "$completedCount / 4 مكتمل",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (completedCount == 4) SoftTheme.MintTeal else SoftTheme.SoftPink,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(
                        onClick = onNavigateToMeds,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier.testTag("vitamins_edit_meds_header_btn")
                    ) {
                        Text("تعديل 💊 ↗", color = SoftTheme.SoftPink, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            val meds = listOf(
                Triple("حمض الفوليك (Folic Acid) 💊", folicChecked) { checked: Boolean ->
                    folicChecked = checked
                    prefs.edit().putBoolean("med_folic_$todayDateStr", checked).apply()
                },
                Triple("مكمل الحديد (Iron) 🩸", ironChecked) { checked: Boolean ->
                    ironChecked = checked
                    prefs.edit().putBoolean("med_iron_$todayDateStr", checked).apply()
                },
                Triple("الكالسيوم (Calcium) 🥛", calciumChecked) { checked: Boolean ->
                    calciumChecked = checked
                    prefs.edit().putBoolean("med_calcium_$todayDateStr", checked).apply()
                },
                Triple("الفيتامينات المتعددة (Multivitamins) ✨", multivitaminChecked) { checked: Boolean ->
                    multivitaminChecked = checked
                    prefs.edit().putBoolean("med_multi_$todayDateStr", checked).apply()
                }
            )

            meds.forEach { (name, isChecked, onToggle) ->
                Surface(
                    onClick = { onToggle(!isChecked) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isChecked) SoftTheme.MintTeal.copy(alpha = 0.15f) else SoftTheme.DeepSlate,
                    border = BorderStroke(1.dp, if (isChecked) SoftTheme.MintTeal else Color.Transparent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isChecked) SoftTheme.MintTeal else SoftTheme.TextWhite,
                            fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal
                        )
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = onToggle,
                            colors = CheckboxDefaults.colors(
                                checkedColor = SoftTheme.MintTeal,
                                uncheckedColor = SoftTheme.SoftGray,
                                checkmarkColor = Color.White
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BloodPressureDialog(
    onDismiss: () -> Unit,
    onSave: (systolic: Int, diastolic: Int, pulse: Int?, notes: String?) -> Unit
) {
    var bpSystolic by remember { mutableStateOf("") }
    var bpDiastolic by remember { mutableStateOf("") }
    var bpPulse by remember { mutableStateOf("") }
    var bpNotes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "تسجيل قياس ضغط الدم 🩸",
                    style = MaterialTheme.typography.titleLarge,
                    color = SoftTheme.TextWhite,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "قيسي ضغطكِ أثناء الراحة وسجلي القراءات لمتابعة صحتكِ الوقائية.",
                    color = SoftTheme.SoftGray,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )

                OutlinedTextField(
                    value = bpSystolic,
                    onValueChange = { bpSystolic = it },
                    label = { Text("الضغط الانقباضي (العالي - مثال: 120)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SoftTheme.SoftPink,
                        unfocusedBorderColor = SoftTheme.SoftGray,
                        focusedTextColor = SoftTheme.TextWhite,
                        unfocusedTextColor = SoftTheme.TextWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = bpDiastolic,
                    onValueChange = { bpDiastolic = it },
                    label = { Text("الضغط الانبساطي (الواطي - مثال: 80)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SoftTheme.SoftPink,
                        unfocusedBorderColor = SoftTheme.SoftGray,
                        focusedTextColor = SoftTheme.TextWhite,
                        unfocusedTextColor = SoftTheme.TextWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = bpPulse,
                    onValueChange = { bpPulse = it },
                    label = { Text("نبضات القلب (اختياري - مثال: 72)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SoftTheme.SoftPink,
                        unfocusedBorderColor = SoftTheme.SoftGray,
                        focusedTextColor = SoftTheme.TextWhite,
                        unfocusedTextColor = SoftTheme.TextWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = bpNotes,
                    onValueChange = { bpNotes = it },
                    label = { Text("ملاحظات (مثال: بعد تناول الكركديه)") },
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
                            val sysVal = bpSystolic.toIntOrNull()
                            val diaVal = bpDiastolic.toIntOrNull()
                            if (sysVal != null && diaVal != null) {
                                onSave(sysVal, diaVal, bpPulse.toIntOrNull(), bpNotes.ifEmpty { null })
                            }
                        },
                        enabled = bpSystolic.isNotEmpty() && bpDiastolic.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SoftTheme.SoftPink,
                            disabledContainerColor = SoftTheme.SoftPink.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("حفظ")
                    }
                }
            }
        }
    }
}
