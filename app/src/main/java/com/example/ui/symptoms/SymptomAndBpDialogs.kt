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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.SoftTheme
import com.example.viewmodel.*

data class BpStatus(val label: String, val color: Color, val advice: String)

fun getBpStatus(systolic: Int, diastolic: Int): BpStatus {
    return when {
        systolic < 90 || diastolic < 60 -> BpStatus(
            label = "ضغط دم منخفض 📉",
            color = Color(0xFF64B5F6),
            advice = "يرجى شرب السوائل أو شوربة دافئة لرفع الضغط وتجنب الدوار."
        )
        systolic >= 140 || diastolic >= 90 -> BpStatus(
            label = "ارتفاع ضغط الدم (مرحلة ٢) ⚠️",
            color = Color(0xFFEF5350),
            advice = "ارتفاع شديد! يرجى الاستراحة، وتجنب الأملاح، ومراجعة طبيبتك فوراً إذا شعرتِ بصداع شديد أو زغللة عين."
        )
        systolic >= 130 || diastolic >= 80 -> BpStatus(
            label = "ارتفاع ضغط الدم (مرحلة ١) ⚠️",
            color = Color(0xFFFFB74D),
            advice = "ارتفاع خفيف. يرجى التقليل من الموالح وشرب الكركديه البارد المهدئ."
        )
        systolic >= 120 -> BpStatus(
            label = "ما قبل الارتفاع ⚠️",
            color = Color(0xFFFFD54F),
            advice = "مستوى مرتفع قليلاً. انتبهي لغذائكِ وقللي الصوديوم."
        )
        else -> BpStatus(
            label = "ضغط دم مثالي وطبيعي ✨",
            color = Color(0xFF81C784),
            advice = "قراءة ممتازة! واصلي اتباع نمط الحياة الصحي وشرب المياه."
        )
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

@Composable
fun AddBloodPressureDialog(
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "تسجيل قياس ضغط الدم 🩸📈",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SoftTheme.SoftPink
                )

                Text(
                    text = "قيسي ضغطكِ أثناء الراحة وسجلي القراءات لمتابعة صحتكِ الوقائية.",
                    color = SoftTheme.SoftGray,
                    style = MaterialTheme.typography.bodySmall,
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
                    label = { Text("ملاحظات (مثال: بعد تناول الكركديه، أثناء التعب)") },
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
