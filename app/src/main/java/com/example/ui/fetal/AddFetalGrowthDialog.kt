package com.example.ui.fetal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SoftTheme

@Composable
fun AddFetalGrowthDialog(
    initialWeek: Int,
    onDismiss: () -> Unit,
    onSave: (week: Int, weightGrams: Double, lengthCm: Double, notes: String?) -> Unit
) {
    var selectedWeek by remember { mutableStateOf(initialWeek.coerceIn(4, 42)) }
    var weightInput by remember { mutableStateOf("") }
    var lengthInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }
    var isErrorMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(selectedWeek) {
        val std = FetalStandardData.getStandardForWeek(selectedWeek)
        weightInput = std.weightGrams.toInt().toString()
        lengthInput = std.lengthCm.toString()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "تسجيل فحص السونار والنمو 👶🏥",
                fontWeight = FontWeight.Bold,
                color = SoftTheme.TextWhite,
                fontSize = 18.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Right
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Week Picker Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("أسبوع الحمل الحالي:", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                        Text("الأسبوع $selectedWeek", fontWeight = FontWeight.Bold, color = SoftTheme.SoftPink)
                    }
                    Slider(
                        value = selectedWeek.toFloat(),
                        onValueChange = { selectedWeek = it.toInt() },
                        valueRange = 4f..42f,
                        steps = 38,
                        colors = SliderDefaults.colors(
                            thumbColor = SoftTheme.SoftPink,
                            activeTrackColor = SoftTheme.SoftPink,
                            inactiveTrackColor = SoftTheme.DeepSlate
                        )
                    )
                    val expectedStd = FetalStandardData.getStandardForWeek(selectedWeek)
                    Text(
                        text = "المعدل الطبيعي للأسبوع $selectedWeek: ${expectedStd.weightGrams.toInt()} جرام | ${expectedStd.lengthCm} سم",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTheme.SoftGray
                    )
                }

                // Weight Input + Estimation Action
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { weightInput = it },
                        label = { Text("وزن الجنين (بالجرام)", color = SoftTheme.SoftGray) },
                        placeholder = { Text("مثال: 500") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("baby_weight_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SoftTheme.TextWhite,
                            unfocusedTextColor = SoftTheme.TextWhite,
                            focusedBorderColor = SoftTheme.SoftPink,
                            unfocusedBorderColor = SoftTheme.DeepSlate
                        )
                    )
                    val parsedLength = lengthInput.toDoubleOrNull()
                    if (parsedLength != null && parsedLength > 0.0) {
                        val estimatedW = FetalStandardData.estimateWeightFromLength(selectedWeek, parsedLength)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "💡 الوزن المقدر من الطول: ${estimatedW.toInt()} جرام",
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftTheme.MintTeal,
                                fontSize = 11.sp
                            )
                            TextButton(
                                onClick = { weightInput = estimatedW.toInt().toString() },
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                            ) {
                                Text("تعبئة تلقائية 🪄", color = SoftTheme.SoftPink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Length Input + Estimation Action
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedTextField(
                        value = lengthInput,
                        onValueChange = { lengthInput = it },
                        label = { Text("طول الجنين (بالسنتيمتر)", color = SoftTheme.SoftGray) },
                        placeholder = { Text("مثال: 28.0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("baby_length_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SoftTheme.TextWhite,
                            unfocusedTextColor = SoftTheme.TextWhite,
                            focusedBorderColor = SoftTheme.SoftPink,
                            unfocusedBorderColor = SoftTheme.DeepSlate
                        )
                    )
                    val parsedWeight = weightInput.toDoubleOrNull()
                    if (parsedWeight != null && parsedWeight > 0.0) {
                        val estimatedL = FetalStandardData.estimateLengthFromWeight(selectedWeek, parsedWeight)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "💡 الطول المقدر من الوزن: $estimatedL سم",
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftTheme.MintTeal,
                                fontSize = 11.sp
                            )
                            TextButton(
                                onClick = { lengthInput = estimatedL.toString() },
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                            ) {
                                Text("تعبئة تلقائية 🪄", color = SoftTheme.SoftPink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Helper Note for Smart Estimation
                Card(
                    colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("✨", fontSize = 16.sp)
                        Text(
                            text = "إذا لم يتوفر لديكِ أحد القياسين من السونار (الوزن أو الطول)، اتركي خانته فارغة وسيقوم التطبيق بحسابه وتقديره تلقائياً بدقة بناءً على قياس الآخر ومنحنى النمو!",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTheme.SoftGray,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                // Notes Input
                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("ملاحظات الطبيب / العيادة (اختياري)", color = SoftTheme.SoftGray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("growth_notes_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SoftTheme.TextWhite,
                        unfocusedTextColor = SoftTheme.TextWhite,
                        focusedBorderColor = SoftTheme.SoftPink,
                        unfocusedBorderColor = SoftTheme.DeepSlate
                    )
                )

                isErrorMsg?.let {
                    Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    var weight = weightInput.toDoubleOrNull()
                    var length = lengthInput.toDoubleOrNull()

                    if ((weight == null || weight <= 0.0) && (length == null || length <= 0.0)) {
                        isErrorMsg = "برجاء إدخال قياس واحد على الأقل (الوزن أو الطول) ليتم الحساب التلقائي."
                        return@Button
                    }

                    if ((length == null || length <= 0.0) && (weight != null && weight > 0.0)) {
                        length = FetalStandardData.estimateLengthFromWeight(selectedWeek, weight)
                    } else if ((weight == null || weight <= 0.0) && (length != null && length > 0.0)) {
                        weight = FetalStandardData.estimateWeightFromLength(selectedWeek, length)
                    }

                    if (weight != null && length != null) {
                        onSave(selectedWeek, weight, length, notesInput.ifBlank { null })
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                modifier = Modifier.testTag("confirm_add_growth_log")
            ) {
                Text("حفظ 💾", color = SoftTheme.DeepSlate, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = SoftTheme.SoftGray)
            }
        },
        containerColor = SoftTheme.CardSlate,
        shape = RoundedCornerShape(24.dp)
    )
}
