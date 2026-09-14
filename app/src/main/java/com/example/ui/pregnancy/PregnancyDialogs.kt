package com.example.ui.pregnancy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.PeriodLog
import com.example.data.PregnancyEntity
import com.example.ui.formatGregorianDate
import com.example.ui.SoftTheme
import java.util.Calendar

@Composable
fun PregnancyLmpPromptDialog(
    pendingPeriodStartDate: Long?,
    pregnancyInfo: PregnancyEntity? = null,
    onConfirmLmp: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    if (pendingPeriodStartDate == null) return
    val dateStr = formatGregorianDate(pendingPeriodStartDate)
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
                    text = "تحديث حسابات الحمل 🌸🤰",
                    style = MaterialTheme.typography.titleLarge,
                    color = SoftTheme.TextWhite,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "صديقتي الغالية، نلاحظ أنكِ سجلتِ حالة حمل نشطة في التطبيق.\n\nهل الدورة التي سجلتِها الآن (والتي بدأت بتاريخ $dateStr) هي الدورة الشهرية الأخيرة التي حصل بعدها الحمل مباشرة؟\n\nإذا كانت الإجابة نعم، فسيقوم رفيقكِ الذكي بتعديل تاريخ الحمل وتاريخ الولادة المتوقع تلقائياً بناءً عليها لتكون جميع الإرشادات والمعلومات الطبية دقيقة تماماً 💖",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SoftTheme.SoftGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            onConfirmLmp(pendingPeriodStartDate)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("نعم، دورة الحمل 👶", color = SoftTheme.TextWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.DeepSlate),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("لا، تسجيل عادي 📝", color = SoftTheme.SoftGray, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun BabyInfoDialog(
    initialGender: String? = null,
    initialName: String? = null,
    currentGender: String? = initialGender,
    currentName: String? = initialName,
    onSave: (String?, String?) -> Unit,
    onDismiss: () -> Unit
) {
    var babyGenderInput by remember { mutableStateOf(initialGender ?: currentGender ?: "") }
    var babyNameInput by remember { mutableStateOf(initialName ?: currentName ?: "") }

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
                    text = "تسجيل جنس واسم الجنين 👶🍼",
                    style = MaterialTheme.typography.titleLarge,
                    color = SoftTheme.TextWhite,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "شاركينا جنس واسم جنينكِ لنخصص التوجيهات باسمه العذب وندخل البهجة على رحلتكما 💖",
                    style = MaterialTheme.typography.bodySmall,
                    color = SoftTheme.SoftGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("ولد", "بنت", "مفاجأة").forEach { gender ->
                        val isSelected = babyGenderInput == gender
                        Button(
                            onClick = { babyGenderInput = gender },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) SoftTheme.SoftPink else SoftTheme.DeepSlate
                            ),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = when (gender) {
                                    "ولد" -> "ولد 💙"
                                    "بنت" -> "بنت 💗"
                                    else -> "مفاجأة 🤫"
                                },
                                color = if (isSelected) Color.White else SoftTheme.SoftGray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = babyNameInput,
                    onValueChange = { babyNameInput = it },
                    label = { Text("الاسم المقترح لجنينكِ العذب:") },
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
                            onSave(babyGenderInput.ifEmpty { null }, babyNameInput.ifEmpty { null })
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

@Composable
fun DeliveryDialog(
    onConfirmDelivery: (String) -> Unit,
    onDismiss: () -> Unit
) {
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
                    text = "مبارك مبارك يا غالية! 🥳💖👶",
                    style = MaterialTheme.typography.titleLarge,
                    color = SoftTheme.TextWhite,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "ألف الحمد لله على سلامتكِ وسلامة مولودكِ الحبيب، جعله الله ذريّة صالحة بارّة قرّة لعينيكِ.\n\nكيف كانت ولادتكِ الميمونة لكي يقدم لكِ رفيقكِ جوري أهم إرشادات التعافي والنفاس المخصصة لكِ؟",
                    style = MaterialTheme.typography.bodySmall,
                    color = SoftTheme.SoftGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            onConfirmDelivery("طبيعي")
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("ولادة طبيعية 🌸", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            onConfirmDelivery("قيصري")
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.DeepSlate),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("ولادة قيصرية 🏥", color = SoftTheme.SoftPink, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun DeliveryRecordDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    DeliveryDialog(
        onConfirmDelivery = onConfirm,
        onDismiss = onDismiss
    )
}

@Composable
fun EndPregnancyConfirmDialog(
    onOpenDelivery: () -> Unit,
    onOpenLossSupport: () -> Unit,
    onDismiss: () -> Unit
) {
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
                    text = "تأكيد إنهاء الحمل الحالي 🤰💔",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SoftTheme.TextWhite,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "هل أنتِ متأكدة من رغبتكِ في إنهاء تتبع الحمل الحالي والعودة إلى تتبع الدورة الشهرية والخصوبة؟\n\nيرجى تحديد سبب إنهاء الحمل لنتمكن من توجيهكِ وتقديم الدعم المناسب لكِ:",
                    style = MaterialTheme.typography.bodySmall,
                    color = SoftTheme.SoftGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Button(
                    onClick = {
                        onDismiss()
                        onOpenDelivery()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("الحمد لله، تمّت الولادة بسلام 🎉👶", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        onDismiss()
                        onOpenLossSupport()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.DeepSlate),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("حدثت مشكلة أو فقدان للحمل لا قدر الله 🤍", color = SoftTheme.SoftPink, fontWeight = FontWeight.Bold)
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("تراجع وإلغاء 🌸", color = SoftTheme.SoftGray)
                }
            }
        }
    }
}

@Composable
fun PregnancyLossDialog(
    onConfirmResetToPeriod: () -> Unit,
    onDismiss: () -> Unit
) {
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
                    text = "عوضكِ الله خيراً يا حبيبتي 🤍",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SoftTheme.SoftPink,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "﴿وَبَشِّرِ الصَّابِرِينَ﴾\n\nسلامة قلبكِ وجسدكِ يا غالية. لا تحزني ولا تفقدي الأمل، فالله لطيف خبير ورحيم، وعوضه جميل دائماً.\n\nنحن هنا بجانبكِ دوماً لتقديم كل الحب والدعم. سنقوم الآن بإعادة ضبط التطبيق لتتبع الدورة الشهرية والراحة لمساعدتكِ على التعافي الهادئ خطوة بخطوة.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SoftTheme.TextWhite,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Button(
                    onClick = {
                        onDismiss()
                        onConfirmResetToPeriod()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("الحمد لله على كل حال (العودة للدورة)", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PregnancySetupDialog(
    periodLogs: List<PeriodLog> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (startDate: Long, preWeight: Double?, heightCm: Double?) -> Unit
) {
    val context = LocalContext.current
    var selectedDate by remember {
        mutableStateOf(
            periodLogs.maxByOrNull { it.startDate }?.startDate ?: System.currentTimeMillis()
        )
    }
    var prePregnancyWeight by remember { mutableStateOf("") }
    var heightCm by remember { mutableStateOf("") }

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
                    text = "تهيئة تتبع الحمل 🤰✨",
                    style = MaterialTheme.typography.titleLarge,
                    color = SoftTheme.TextWhite,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "حددي تاريخ أول يوم لآخر دورة شهرية (LMP) لحساب أسابيع الحمل والولادة بدقة:",
                    color = SoftTheme.SoftGray,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )

                OutlinedButton(
                    onClick = {
                        val cal = Calendar.getInstance().apply { timeInMillis = selectedDate }
                        android.app.DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                val selected = Calendar.getInstance().apply {
                                    set(y, m, d, 0, 0, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }
                                selectedDate = selected.timeInMillis
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("📅 تاريخ آخر دورة: ${formatGregorianDate(selectedDate)}", color = SoftTheme.SoftPink)
                }

                OutlinedTextField(
                    value = prePregnancyWeight,
                    onValueChange = { prePregnancyWeight = it },
                    label = { Text("الوزن قبل الحمل (كجم - اختياري)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SoftTheme.SoftPink,
                        unfocusedBorderColor = SoftTheme.SoftGray,
                        focusedTextColor = SoftTheme.TextWhite,
                        unfocusedTextColor = SoftTheme.TextWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = heightCm,
                    onValueChange = { heightCm = it },
                    label = { Text("الطول (سم - اختياري)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("إلغاء", color = SoftTheme.SoftGray)
                    }
                    Button(
                        onClick = {
                            onSave(
                                selectedDate,
                                prePregnancyWeight.toDoubleOrNull(),
                                heightCm.toDoubleOrNull()
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("تفعيل وتتبع 🌸")
                    }
                }
            }
        }
    }
}

@Composable
fun AddPregnancyJournalDialog(
    onDismiss: () -> Unit,
    onSave: (content: String, mood: String) -> Unit
) {
    var content by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf("سعادة 💖") }
    val moods = listOf("سعادة 💖", "هدوء 🌸", "تعب 😴", "متحمسة ✨", "قلق 🤍")

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
                    text = "تدوين لحظات ومشاعر الحمل ✍️✨",
                    style = MaterialTheme.typography.titleMedium,
                    color = SoftTheme.TextWhite,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("بماذا تشعرين أو ماذا تودين قوله لطفلكِ اليوم؟") },
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SoftTheme.SoftPink,
                        unfocusedBorderColor = SoftTheme.SoftGray,
                        focusedTextColor = SoftTheme.TextWhite,
                        unfocusedTextColor = SoftTheme.TextWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("حالتكِ المزاجية:", color = SoftTheme.SoftGray, style = MaterialTheme.typography.bodySmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    moods.forEach { mood ->
                        val isSelected = selectedMood == mood
                        Surface(
                            onClick = { selectedMood = mood },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) SoftTheme.SoftPink else SoftTheme.DeepSlate,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = mood,
                                color = if (isSelected) Color.White else SoftTheme.SoftGray,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(vertical = 6.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("إلغاء", color = SoftTheme.SoftGray)
                    }
                    Button(
                        onClick = {
                            if (content.isNotBlank()) {
                                onSave(content, selectedMood)
                            }
                        },
                        enabled = content.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("حفظ التدوينة")
                    }
                }
            }
        }
    }
}

