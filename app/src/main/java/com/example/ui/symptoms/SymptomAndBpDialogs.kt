package com.example.ui.symptoms

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.SoftTheme
import com.example.viewmodel.*

data class BpStatus(val label: String, val color: Color, val advice: String)

fun getBpStatus(systolic: Int, diastolic: Int, isChronicLowBp: Boolean = false): BpStatus {
    return when {
        // Stage 2 Severe Hypertension (>= 140 / >= 90)
        systolic >= 140 || diastolic >= 90 -> BpStatus(
            label = "ارتفاع ضغط الدم (مرحلة ٢) ⚠️",
            color = Color(0xFFEF5350),
            advice = if (isChronicLowBp) {
                "ارتفاع غير معتاد لطبيعة جسمكِ! استريحي تماماً، اشربي ماء نقياً، وراجعي طبيبتكِ فوراً إذا ترافق مع صداع أو زغللة عين."
            } else {
                "ارتفاع شديد! يرجى الاستراحة، وتجنب الأملاح، ومراجعة طبيبتك فوراً إذا شعرتِ بصداع شديد أو زغللة عين."
            }
        )
        // Stage 1 Hypertension (systolic 130..139 OR diastolic 81..89 - strictly > 80, not >= 80)
        systolic >= 130 || diastolic > 80 -> BpStatus(
            label = "ارتفاع ضغط الدم (مرحلة ١) ⚠️",
            color = Color(0xFFFFB74D),
            advice = if (isChronicLowBp) {
                "ارتفاع طفيف بالنسبة لمعدلكِ المنخفض المعتاد. تنفسي بعمق، استرخي، وتجنبي الإجهاد والمنبهات."
            } else {
                "ارتفاع خفيف. يرجى الاستراحة والتقليل من الموالح والمتابعة الدورية."
            }
        )
        // Elevated / ما قبل الارتفاع (systolic 121..129 and diastolic <= 80)
        systolic > 120 && diastolic <= 80 -> BpStatus(
            label = if (isChronicLowBp) "ضغط نشط ومتوازن 🌟" else "ما قبل الارتفاع ⚠️",
            color = if (isChronicLowBp) Color(0xFF81C784) else Color(0xFFFFD54F),
            advice = if (isChronicLowBp) {
                "قراءة ممتازة وحيوية! دورتكِ الدموية نشطة اليوم مع استقرار تام دون هبوط."
            } else {
                "مستوى مائل للارتفاع قليلاً. انتبهي لغذائكِ وقللي الصوديوم واشربي الماء."
            }
        )
        // Low blood pressure (< 90 systolic OR < 60 diastolic)
        systolic < 90 || diastolic < 60 -> BpStatus(
            label = "ضغط دم منخفض 📉",
            color = Color(0xFF64B5F6),
            advice = if (isChronicLowBp) {
                "هبوط ملحوظ عن المعتاد. تناولي وجبة خفيفة مع سوائل أو شوربة دافئة، وارتاحي وتجنبي الوقوف المفاجئ."
            } else {
                "يرجى شرب السوائل أو شوربة دافئة لرفع الضغط وتجنب الدوار."
            }
        )
        // Ideal / Normal (90..120 systolic AND 60..80 diastolic - including 120/80 exactly!)
        else -> {
            if (isChronicLowBp) {
                if (systolic >= 110 || diastolic >= 75) {
                    BpStatus(
                        label = "ضغط مثالي ومتوازن 🌟",
                        color = Color(0xFF81C784),
                        advice = "قراءة ممتازة ومتوازنة! دورتكِ الدموية نشطة وضغطكِ في أفضل حالاته دون هبوط."
                    )
                } else {
                    BpStatus(
                        label = "نطاقكِ المعتاد المستقر 🌿",
                        color = Color(0xFF81C784),
                        advice = "قراءة مألوفة ومستقرة لطبيعتكِ. استمري بالترطيب الجيد وتوزيع الوجبات الخفيفة."
                    )
                }
            } else {
                BpStatus(
                    label = "ضغط دم مثالي وطبيعي ✨",
                    color = Color(0xFF81C784),
                    advice = "قراءة ممتازة! واصلي اتباع نمط الحياة الصحي وشرب المياه."
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddSymptomDialog(
    onDismiss: () -> Unit,
    onSave: (symptom: String, severity: Int, notes: String) -> Unit
) {
    var selectedSymptom by remember { mutableStateOf("مغص") }
    var symptomSeverity by remember { mutableIntStateOf(5) }
    var symptomNotes by remember { mutableStateOf("") }
    val coreSymptoms = listOf("غثيان", "صداع", "ألم أسفل الظهر", "مغص", "دوار", "إمساك", "حرقان معدة", "إرهاق")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "تسجيل عرض صحي 🩺",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SoftTheme.SoftPink
                )

                Text("اختر العرض:", color = SoftTheme.TextWhite)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    coreSymptoms.forEach { s ->
                        val isSel = selectedSymptom == s
                        FilterChip(
                            selected = isSel,
                            onClick = { selectedSymptom = s },
                            label = { Text(s) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SoftTheme.SoftPink,
                                selectedLabelColor = SoftTheme.DeepSlate
                            )
                        )
                    }
                }

                Text("مستوى الشدة والتعب: $symptomSeverity/10", color = SoftTheme.TextWhite)
                Slider(
                    value = symptomSeverity.toFloat(),
                    onValueChange = { symptomSeverity = it.toInt() },
                    valueRange = 1f..10f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor = SoftTheme.SoftPink,
                        activeTrackColor = SoftTheme.SoftPink,
                        inactiveTrackColor = SoftTheme.DeepSlate
                    )
                )

                OutlinedTextField(
                    value = symptomNotes,
                    onValueChange = { symptomNotes = it },
                    label = { Text("أي تفاصيل إضافية...") },
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
                            onSave(selectedSymptom, symptomSeverity, symptomNotes)
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddBloodPressureDialog(
    onDismiss: () -> Unit,
    onSave: (systolic: Int, diastolic: Int, pulse: Int?, notes: String?) -> Unit,
    isLowBp: Boolean = false,
    availableMedications: List<String> = emptyList()
) {
    var bpSystolic by remember { mutableStateOf("") }
    var bpDiastolic by remember { mutableStateOf("") }
    var bpPulse by remember { mutableStateOf("") }
    var bpNotes by remember { mutableStateOf("") }

    // خيارات الدواء والكورس العلاجي
    var hasMedication by remember { mutableStateOf(false) }
    var selectedMedName by remember { mutableStateOf("") }
    var customMedName by remember { mutableStateOf("") }
    var medTiming by remember { mutableStateOf("بعد الدواء (1-2 ساعة)") }
    var courseDuration by remember { mutableStateOf("") }

    val sysVal = bpSystolic.toIntOrNull()
    val diaVal = bpDiastolic.toIntOrNull()
    val liveStatus = if (sysVal != null && diaVal != null) {
        getBpStatus(sysVal, diaVal, isChronicLowBp = isLowBp)
    } else null

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ترويسة النافذة
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "تسجيل قياس ضغط الدم 🩸📈",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.SoftPink
                    )
                }

                Text(
                    text = if (isLowBp) {
                        "✨ متابعة مخصصة لطبيعة ضغطكِ المنخفض لدعم استقراركِ وتجنب الدوار والإجهاد."
                    } else {
                        "قيسي ضغطكِ أثناء الراحة وسجلي القراءات لمتابعة صحتكِ الوقائية."
                    },
                    color = SoftTheme.SoftGray,
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 16.sp
                )

                // بطاقة التقييم الفوري عند كتابة الأرقام
                if (liveStatus != null) {
                    Surface(
                        color = liveStatus.color.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, liveStatus.color.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(liveStatus.color, CircleShape)
                            )
                            Column {
                                Text(
                                    text = liveStatus.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = liveStatus.color
                                )
                                Text(
                                    text = liveStatus.advice,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SoftTheme.TextWhite.copy(alpha = 0.9f),
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }

                // حقول الأرقام
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

                // --- قسم سياق الدواء والكورس العلاجي ---
                HorizontalDivider(color = SoftTheme.SoftGray.copy(alpha = 0.2f), thickness = 1.dp)

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "علاقة القياس بالدواء والكورس 💊",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.MintTeal
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FilterChip(
                            selected = !hasMedication,
                            onClick = { hasMedication = false },
                            label = { Text("بدون دواء", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SoftTheme.MintTeal.copy(alpha = 0.2f),
                                selectedLabelColor = SoftTheme.MintTeal
                            )
                        )
                        FilterChip(
                            selected = hasMedication,
                            onClick = { hasMedication = true },
                            label = { Text("مرتبط بدواء ضغط / علاج", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SoftTheme.SoftPink.copy(alpha = 0.2f),
                                selectedLabelColor = SoftTheme.SoftPink
                            )
                        )
                    }

                    if (hasMedication) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SoftTheme.DeepSlate.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "اختاري الدواء أو العلاج:",
                                fontSize = 11.sp,
                                color = SoftTheme.SoftGray
                            )

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                availableMedications.forEach { med ->
                                    FilterChip(
                                        selected = (selectedMedName == med),
                                        onClick = {
                                            selectedMedName = med
                                            customMedName = ""
                                        },
                                        label = { Text(med, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = SoftTheme.SoftPink,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                                FilterChip(
                                    selected = (selectedMedName == "custom"),
                                    onClick = { selectedMedName = "custom" },
                                    label = { Text("دواء آخر...", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SoftTheme.SoftPink,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }

                            if (selectedMedName == "custom" || (availableMedications.isEmpty() && selectedMedName.isEmpty())) {
                                OutlinedTextField(
                                    value = customMedName,
                                    onValueChange = { customMedName = it },
                                    label = { Text("اسم دواء الضغط (مثال: كوراسور، إيفورتيل، نقط)") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = SoftTheme.SoftPink,
                                        unfocusedBorderColor = SoftTheme.SoftGray,
                                        focusedTextColor = SoftTheme.TextWhite,
                                        unfocusedTextColor = SoftTheme.TextWhite
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Text(
                                text = "توقيت القياس بالنسبة للدواء:",
                                fontSize = 11.sp,
                                color = SoftTheme.SoftGray
                            )

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    "بعد الدواء (1-2 ساعة)",
                                    "قبل تناول الدواء",
                                    "أثناء شعور بهبوط/دوار"
                                ).forEach { timing ->
                                    FilterChip(
                                        selected = (medTiming == timing),
                                        onClick = { medTiming = timing },
                                        label = { Text(timing, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = SoftTheme.MintTeal,
                                            selectedLabelColor = Color.Black
                                        )
                                    )
                                }
                            }

                            Text(
                                text = "مدة الكورس العلاجي (اختياري):",
                                fontSize = 11.sp,
                                color = SoftTheme.SoftGray
                            )

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("أسبوع (7 أيام)", "10 أيام", "أسبوعين (14 يوم)", "مستمر").forEach { duration ->
                                    FilterChip(
                                        selected = (courseDuration == duration),
                                        onClick = { courseDuration = if (courseDuration == duration) "" else duration },
                                        label = { Text(duration, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = SoftTheme.SoftPink.copy(alpha = 0.3f),
                                            selectedLabelColor = SoftTheme.SoftPink
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = bpNotes,
                    onValueChange = { bpNotes = it },
                    label = { Text("ملاحظات إضافية (مثال: بعد مجهود، بعد وجبة)") },
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إلغاء", color = SoftTheme.SoftGray)
                    }

                    Button(
                        onClick = {
                            if (sysVal != null && diaVal != null) {
                                val finalNotes = buildString {
                                    if (hasMedication) {
                                        val med = if (selectedMedName.isNotBlank() && selectedMedName != "custom") {
                                            selectedMedName
                                        } else {
                                            customMedName.trim().ifEmpty { "دواء الضغط" }
                                        }
                                        append("💊 $med ($medTiming")
                                        if (courseDuration.isNotBlank()) {
                                            append(" • كورس $courseDuration")
                                        }
                                        append(")")
                                        if (bpNotes.isNotBlank()) {
                                            append(" | ${bpNotes.trim()}")
                                        }
                                    } else {
                                        if (bpNotes.isNotBlank()) {
                                            append(bpNotes.trim())
                                        }
                                    }
                                }.ifBlank { null }

                                onSave(sysVal, diaVal, bpPulse.toIntOrNull(), finalNotes)
                            }
                        },
                        enabled = sysVal != null && diaVal != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SoftTheme.SoftPink,
                            disabledContainerColor = SoftTheme.SoftPink.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("حفظ القياس")
                    }
                }
            }
        }
    }
}

@Composable
fun MedicalReportDialog(
    viewModel: WomanCompanionViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val reportText = remember { viewModel.generateMedicalReportText() }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("📋", fontSize = 20.sp)
                        Text(
                            text = "التقرير الطبي للزيارة",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.MintTeal
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = SoftTheme.SoftGray)
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        item {
                            SelectionContainer {
                                Text(
                                    text = reportText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SoftTheme.TextWhite,
                                    lineHeight = 20.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, reportText)
                                putExtra(Intent.EXTRA_TITLE, "تقرير المتابعة الطبية")
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "مشاركة التقرير مع الطبيبة عبر:")
                            context.startActivity(shareIntent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.MintTeal),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("مشاركة التقرير 📤", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("Medical Report", reportText)
                            clipboard.setPrimaryClip(clip)
                            android.widget.Toast.makeText(context, "تم نسخ التقرير الطبي بنجاح 📋", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftTheme.TextWhite),
                        border = BorderStroke(1.dp, SoftTheme.SoftGray.copy(alpha = 0.4f))
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("نسخ النص 📋")
                    }
                }
            }
        }
    }
}
