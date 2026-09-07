package com.example.ui.profile

import android.app.DatePickerDialog
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.PregnancyEntity
import com.example.ui.SoftTheme
import com.example.ui.formatGregorianDate
import java.util.*

@Composable
fun EditProfileDialog(
    currentProfile: PregnancyEntity?,
    onDismiss: () -> Unit,
    onSave: (
        motherName: String,
        nickname: String,
        birthDate: Long?,
        heightCm: Double?,
        preWeight: Double?,
        hasHighBp: Boolean,
        hasLowBp: Boolean,
        hasDiabetes: Boolean,
        chronicOthers: String,
        babyName: String?
    ) -> Unit
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf(currentProfile?.motherName.orEmpty()) }
    var nickname by remember { mutableStateOf(currentProfile?.nickname.orEmpty()) }
    var birthDateMs by remember { mutableStateOf(currentProfile?.birthDate) }
    var heightText by remember { mutableStateOf(currentProfile?.heightCm?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() }.orEmpty()) }
    var weightText by remember { mutableStateOf(currentProfile?.prePregnancyWeight?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() }.orEmpty()) }
    var hasHighBp by remember { mutableStateOf(currentProfile?.hasHighBp == true) }
    var hasLowBp by remember { mutableStateOf(currentProfile?.hasLowBp == true) }
    var hasDiabetes by remember { mutableStateOf(currentProfile?.hasDiabetes == true) }
    var chronicOthers by remember { mutableStateOf(currentProfile?.chronicOthers.orEmpty()) }
    var babyName by remember { mutableStateOf(currentProfile?.babyName.orEmpty()) }

    // حساب الـ BMI التلقائي المباشر
    val heightVal = heightText.toDoubleOrNull()
    val weightVal = weightText.toDoubleOrNull()
    val computedBmi = remember(heightVal, weightVal) {
        if (heightVal != null && weightVal != null && heightVal > 50 && weightVal > 20) {
            val hM = heightVal / 100.0
            val bmi = weightVal / (hM * hM)
            String.format(Locale.US, "%.1f", bmi)
        } else null
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .testTag("edit_profile_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardDark),
            border = BorderStroke(1.dp, SoftTheme.CardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // رأس النافذة
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(SoftTheme.PrimaryPink.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = SoftTheme.PrimaryPink
                            )
                        }
                        Column {
                            Text(
                                text = "تعديل الملف الشخصي 👤",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SoftTheme.TextWhite,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "تحديث بياناتكِ الشخصية والصحية بدقة",
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftTheme.SoftGray,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SoftTheme.DeepSlate.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = SoftTheme.TextWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 14.dp),
                    color = SoftTheme.CardBorder,
                    thickness = 1.dp
                )

                // المحتوى القابل للتمرير
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. البيانات الشخصية
                    Text(
                        text = "البيانات الأساسية 🌸",
                        color = SoftTheme.PrimaryPink,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    // الاسم الكامل
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("الاسم الكريم") },
                        placeholder = { Text("مثال: سارة أحمد") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_profile_name_input"),
                        singleLine = true,
                        colors = outlinedTextFieldColors(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    // اسم الدلع المحبب
                    OutlinedTextField(
                        value = nickname,
                        onValueChange = { nickname = it },
                        label = { Text("اسم الدلع المحبب (تناديكِ به جوري) 💕") },
                        placeholder = { Text("مثال: سوسو، رورو...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_profile_nickname_input"),
                        singleLine = true,
                        colors = outlinedTextFieldColors(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    // تاريخ الميلاد
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "تاريخ الميلاد (لحساب العمر تلقائياً):",
                            color = SoftTheme.SoftGray,
                            fontSize = 12.sp
                        )
                        Button(
                            onClick = {
                                val calendar = Calendar.getInstance().apply {
                                    if (birthDateMs != null) timeInMillis = birthDateMs!!
                                }
                                val dateDialog = DatePickerDialog(
                                    context,
                                    { _, y, m, d ->
                                        val cal = Calendar.getInstance().apply {
                                            set(Calendar.YEAR, y)
                                            set(Calendar.MONTH, m)
                                            set(Calendar.DAY_OF_MONTH, d)
                                            set(Calendar.HOUR_OF_DAY, 0)
                                            set(Calendar.MINUTE, 0)
                                            set(Calendar.SECOND, 0)
                                            set(Calendar.MILLISECOND, 0)
                                        }
                                        birthDateMs = cal.timeInMillis
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                )
                                dateDialog.datePicker.maxDate = System.currentTimeMillis()
                                dateDialog.show()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("edit_profile_dob_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SoftTheme.DeepSlate.copy(alpha = 0.6f),
                                contentColor = SoftTheme.TextWhite
                            ),
                            border = BorderStroke(1.dp, SoftTheme.CardBorder),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = null,
                                        tint = Color(0xFF2196F3)
                                    )
                                    Text(
                                        text = birthDateMs?.let { formatGregorianDate(it) } ?: "اختاري تاريخ الميلاد",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                }
                                Text(
                                    text = "تغيير 📅",
                                    color = Color(0xFF2196F3),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = SoftTheme.CardBorder.copy(alpha = 0.5f))

                    // 2. القياسات الجسدية (الطول، الوزن)
                    Text(
                        text = "القياسات الجسدية ومؤشر الكتلة ⚖️",
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = heightText,
                            onValueChange = { heightText = it },
                            label = { Text("الطول (سم)") },
                            placeholder = { Text("مثال: 165") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("edit_profile_height_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = outlinedTextFieldColors(),
                            shape = RoundedCornerShape(14.dp)
                        )

                        OutlinedTextField(
                            value = weightText,
                            onValueChange = { weightText = it },
                            label = { Text("الوزن (كجم)") },
                            placeholder = { Text("مثال: 62") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("edit_profile_weight_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = outlinedTextFieldColors(),
                            shape = RoundedCornerShape(14.dp)
                        )
                    }

                    if (computedBmi != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF4CAF50).copy(alpha = 0.12f))
                                .border(1.dp, Color(0xFF4CAF50).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "مؤشر كتلة الجسم (BMI) المحسوب:",
                                    color = SoftTheme.TextWhite,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "$computedBmi كجم/م²",
                                    color = Color(0xFF4CAF50),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = SoftTheme.CardBorder.copy(alpha = 0.5f))

                    // 3. الحالة الصحية والأمراض المزمنة
                    Text(
                        text = "الوضع الصحي والوقائي 🩺",
                        color = Color(0xFFFFB300),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    // ضغط دم مرتفع
                    ChronicConditionCheckbox(
                        title = "ضغط دم مرتفع (عالي) 📈",
                        subtitle = "تنبيهات للأطعمة الغنية بالصوديوم وتوصيات بالكركديه البارد",
                        checked = hasHighBp,
                        onCheckedChange = { hasHighBp = it }
                    )

                    // ضغط دم منخفض
                    ChronicConditionCheckbox(
                        title = "ضغط دم منخفض (واطي) 📉",
                        subtitle = "تنبيهات لترطيب الجسم وتجنب الدوار المفاجئ",
                        checked = hasLowBp,
                        onCheckedChange = { hasLowBp = it }
                    )

                    // السكري
                    ChronicConditionCheckbox(
                        title = "مرض السكري أو سكر الحمل 🩸",
                        subtitle = "حساب دقيق للكربوهيدرات ومراقبة السكريات",
                        checked = hasDiabetes,
                        onCheckedChange = { hasDiabetes = it }
                    )

                    // أمراض أو ملاحظات أخرى
                    OutlinedTextField(
                        value = chronicOthers,
                        onValueChange = { chronicOthers = it },
                        label = { Text("أي أمراض مزمنة أو ملاحظات صحية أخرى") },
                        placeholder = { Text("مثال: حساسية البنسلين، خمول الغدة الدرقية...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_profile_chronic_others_input"),
                        singleLine = false,
                        maxLines = 3,
                        colors = outlinedTextFieldColors(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    // اسم الجنين (إن وجد)
                    OutlinedTextField(
                        value = babyName,
                        onValueChange = { babyName = it },
                        label = { Text("اسم الجنين المفضل (اختياري) 👶") },
                        placeholder = { Text("مثال: يوسف، نور، مريم...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_profile_baby_name_input"),
                        singleLine = true,
                        colors = outlinedTextFieldColors(),
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                // أزرار الحفظ والإلغاء
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, SoftTheme.CardBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftTheme.TextWhite)
                    ) {
                        Text("إلغاء", fontSize = 14.sp)
                    }

                    Button(
                        onClick = {
                            val h = heightText.toDoubleOrNull()
                            val w = weightText.toDoubleOrNull()
                            onSave(
                                name,
                                nickname,
                                birthDateMs,
                                h,
                                w,
                                hasHighBp,
                                hasLowBp,
                                hasDiabetes,
                                chronicOthers,
                                babyName
                            )
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(50.dp)
                            .testTag("save_profile_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SoftTheme.PrimaryPink,
                            contentColor = Color.White
                        )
                    ) {
                        Text("حفظ التعديلات ✨", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChronicConditionCheckbox(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (checked) Color(0xFFFFB300).copy(alpha = 0.1f) else SoftTheme.DeepSlate.copy(alpha = 0.3f))
            .border(1.dp, if (checked) Color(0xFFFFB300).copy(alpha = 0.5f) else SoftTheme.CardBorder.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = Color(0xFFFFB300),
                checkmarkColor = Color.Black
            )
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = SoftTheme.TextWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = subtitle,
                color = SoftTheme.SoftGray,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun outlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = SoftTheme.DeepSlate.copy(alpha = 0.5f),
    unfocusedContainerColor = SoftTheme.DeepSlate.copy(alpha = 0.3f),
    focusedBorderColor = SoftTheme.PrimaryPink,
    unfocusedBorderColor = SoftTheme.CardBorder,
    focusedTextColor = SoftTheme.TextWhite,
    unfocusedTextColor = SoftTheme.TextWhite,
    focusedLabelColor = SoftTheme.PrimaryPink,
    unfocusedLabelColor = SoftTheme.SoftGray,
    focusedPlaceholderColor = SoftTheme.SoftGray.copy(alpha = 0.7f),
    unfocusedPlaceholderColor = SoftTheme.SoftGray.copy(alpha = 0.4f)
)
