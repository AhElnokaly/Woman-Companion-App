package com.example.ui.meds

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.MedicationAdherenceLog
import com.example.data.MedicationLog
import com.example.ui.SoftTheme
import com.example.ui.formatGregorianDate
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditMedicationDialog(
    editingMedication: MedicationLog?,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        dosage: String,
        timesPerDay: Int,
        prescribedBy: String?,
        notes: String,
        start: Long,
        totalQuantity: Int,
        safetyWarning: String
    ) -> Unit
) {
    var medName by rememberSaveable { mutableStateOf(editingMedication?.name ?: "") }
    var medDosage by rememberSaveable { mutableStateOf(editingMedication?.dosage ?: "") }
    var medTimes by rememberSaveable { mutableStateOf(editingMedication?.timesPerDay?.toString() ?: "1") }
    var medDoctor by rememberSaveable { mutableStateOf(editingMedication?.prescribedBy ?: "") }
    var medNotes by rememberSaveable { mutableStateOf(editingMedication?.notes ?: "") }
    var medFormType by rememberSaveable { mutableStateOf("حبوب 💊") }
    var medMealRelation by rememberSaveable { mutableStateOf("بعد الأكل 🍽️") }
    var medScheduleType by rememberSaveable { mutableStateOf("يومي 📅") }
    var medTotalStock by rememberSaveable {
        mutableStateOf(
            if ((editingMedication?.remainingQuantity ?: 0) > 0)
                editingMedication?.remainingQuantity.toString()
            else ""
        )
    }

    val initialCal = remember {
        Calendar.getInstance().apply {
            timeInMillis = editingMedication?.startDate ?: System.currentTimeMillis()
        }
    }
    val initialHour24 = remember { initialCal.get(Calendar.HOUR_OF_DAY) }
    var doseHour by rememberSaveable {
        mutableIntStateOf(if (initialHour24 % 12 == 0) 12 else initialHour24 % 12)
    }
    var doseMinute by rememberSaveable {
        mutableIntStateOf(initialCal.get(Calendar.MINUTE))
    }
    var doseIsPm by rememberSaveable {
        mutableStateOf(initialHour24 >= 12)
    }

    val quickPregnancyMeds = listOf(
        Triple("حمض الفوليك (Folic Acid)", "حبة واحدة (٤٠٠ ميكروجرام)", "حبوب 💊"),
        Triple("مكمل حديد (Iron)", "كبسولة واحدة (٢٧ ملجم)", "كبسولات 💊"),
        Triple("كالسيوم وفيتامين د", "قرص واحد (٥٠٠ ملجم)", "أقراص 🦴"),
        Triple("برينتال ملتي فيتامين", "كبسولة يومياً", "كبسولات 🤰"),
        Triple("فيتامين C فوار", "قرص فوار في نصف كوب ماء", "فوار 🫧"),
        Triple("مضاد غثيان (Navidoxine)", "قرص قبل النوم", "حبوب 🍋")
    )

    val formTypes = listOf("حبوب 💊", "كبسولات 💊", "شراب 🥄", "حقن 💉", "فوار 🫧", "نقط 💧", "أقراص 🦴", "بخاخ 💨", "مرهم 🧴")
    val mealRelations = listOf("بعد الأكل 🍽️", "قبل الأكل ⏳", "مع الأكل 🥗", "على الريق 🌅", "قبل النوم 🌙", "غير مرتبط بالطعام ✨")
    val scheduleTypes = listOf("يومي 📅", "يوم بعد يوم 🗓️", "عند اللزوم ⚡", "أسبوعي 📌", "محدد بأيام 🎯")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.25f)),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(vertical = 12.dp, horizontal = 4.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                item {
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
                                    .size(38.dp)
                                    .background(SoftTheme.SoftPink.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("💊", fontSize = 20.sp)
                            }
                            Column {
                                Text(
                                    text = if (editingMedication != null) "تعديل بيانات الدواء ✏️" else "إضافة وجدولة دواء أو فيتامين 💊",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftTheme.TextWhite
                                )
                                Text(
                                    text = "حددي الجرعة، عدد المرات، ووقت التذكير بدقة",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SoftTheme.SoftGray,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .background(SoftTheme.DeepSlate, CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = SoftTheme.SoftGray, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // 1. Quick Presets for Pregnancy
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "💊 مكملات الحمل الشائعة (اختيار سريع):",
                            fontSize = 12.sp,
                            color = SoftTheme.SoftPink,
                            fontWeight = FontWeight.Bold
                        )
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            quickPregnancyMeds.forEach { preset ->
                                val isSelected = medName == preset.first
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) SoftTheme.SoftPink else SoftTheme.DeepSlate,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) SoftTheme.SoftPink else SoftTheme.CardSlate
                                    ),
                                    modifier = Modifier.clickable {
                                        medName = preset.first
                                        medDosage = preset.second
                                        medFormType = preset.third
                                    }
                                ) {
                                    Text(
                                        text = preset.first,
                                        color = if (isSelected) Color.White else SoftTheme.TextWhite,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Med Name & Dosage
                item {
                    OutlinedTextField(
                        value = medName,
                        onValueChange = { medName = it },
                        label = { Text("اسم الدواء أو الفيتامين *") },
                        placeholder = { Text("مثال: حمض الفوليك، مكمل حديد...") },
                        leadingIcon = {
                            Icon(Icons.Default.Medication, contentDescription = null, tint = SoftTheme.SoftPink)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoftTheme.SoftPink,
                            unfocusedBorderColor = SoftTheme.SoftGray.copy(alpha = 0.4f),
                            focusedTextColor = SoftTheme.TextWhite,
                            unfocusedTextColor = SoftTheme.TextWhite,
                            focusedLabelColor = SoftTheme.SoftPink,
                            unfocusedLabelColor = SoftTheme.SoftGray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = medDosage,
                        onValueChange = { medDosage = it },
                        label = { Text("الجرعة وكمية التناول") },
                        placeholder = { Text("مثال: قرص واحد، ٥٠٠ ملجم، ملعقة ٥ مل...") },
                        leadingIcon = {
                            Icon(Icons.Default.Healing, contentDescription = null, tint = SoftTheme.MintTeal)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoftTheme.MintTeal,
                            unfocusedBorderColor = SoftTheme.SoftGray.copy(alpha = 0.4f),
                            focusedTextColor = SoftTheme.TextWhite,
                            unfocusedTextColor = SoftTheme.TextWhite,
                            focusedLabelColor = SoftTheme.MintTeal,
                            unfocusedLabelColor = SoftTheme.SoftGray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 3. Form Type selector
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("نوع وشكل العلاج 🧪:", fontSize = 12.sp, color = SoftTheme.SoftGray, fontWeight = FontWeight.SemiBold)
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            formTypes.forEach { type ->
                                val isSelected = medFormType == type
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) SoftTheme.MintTeal else SoftTheme.DeepSlate,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) SoftTheme.MintTeal else SoftTheme.CardSlate
                                    ),
                                    modifier = Modifier.clickable { medFormType = type }
                                ) {
                                    Text(
                                        text = type,
                                        color = if (isSelected) Color.White else SoftTheme.TextWhite,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Frequency & Number of Times
                item {
                    val currentTimesInt = medTimes.toIntOrNull() ?: 1
                    val frequencyIntervalText = when (currentTimesInt) {
                        1 -> "مرة واحدة يومياً (كل 24 ساعة)"
                        2 -> "مرتان يومياً (كل 12 ساعة)"
                        3 -> "3 مرات يومياً (كل 8 ساعات)"
                        4 -> "4 مرات يومياً (كل 6 ساعات)"
                        5 -> "5 مرات يومياً (كل ~4.8 ساعات)"
                        6 -> "6 مرات يومياً (كل 4 ساعات)"
                        else -> "$currentTimesInt مرات يومياً (كل ${24 / currentTimesInt} ساعة)"
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "⏰ عدد المرات والتكرار اليومي:",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SoftTheme.TextWhite
                                    )
                                    Text(
                                        text = frequencyIntervalText,
                                        fontSize = 11.sp,
                                        color = SoftTheme.MintTeal,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                // Stepper (+ / -)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SoftTheme.CardSlate)
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    IconButton(
                                        onClick = {
                                            if (currentTimesInt > 1) {
                                                medTimes = (currentTimesInt - 1).toString()
                                            }
                                        },
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Text("-", fontSize = 16.sp, color = SoftTheme.SoftPink, fontWeight = FontWeight.Bold)
                                    }
                                    Text(
                                        text = "$currentTimesInt",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = SoftTheme.TextWhite,
                                        modifier = Modifier.padding(horizontal = 6.dp)
                                    )
                                    IconButton(
                                        onClick = {
                                            if (currentTimesInt < 12) {
                                                medTimes = (currentTimesInt + 1).toString()
                                            }
                                        },
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Text("+", fontSize = 16.sp, color = SoftTheme.SoftPink, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Quick Frequency Chips (1, 2, 3, 4, 5, 6)
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val frequencies = listOf(
                                    1 to "١ يومياً",
                                    2 to "٢ مرتان",
                                    3 to "٣ مرات",
                                    4 to "٤ مرات",
                                    5 to "٥ مرات",
                                    6 to "٦ مرات"
                                )
                                frequencies.forEach { (count, label) ->
                                    val isSelected = currentTimesInt == count
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) SoftTheme.SoftPink else SoftTheme.CardSlate,
                                        border = BorderStroke(
                                            1.dp,
                                            if (isSelected) SoftTheme.SoftPink else Color.Transparent
                                        ),
                                        modifier = Modifier
                                            .clickable { medTimes = count.toString() }
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else SoftTheme.TextWhite,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(color = SoftTheme.CardSlate, thickness = 1.dp)

                            // Time Selector for First Dose
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "وقت الجرعة الأولى (بداية التنبيهات):",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SoftTheme.SoftGray
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Hour Selector
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(SoftTheme.CardSlate)
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        IconButton(
                                            onClick = { doseHour = if (doseHour > 1) doseHour - 1 else 12 },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Text("-", fontSize = 16.sp, color = SoftTheme.SoftPink, fontWeight = FontWeight.Bold)
                                        }
                                        Text(
                                            text = "%02d".format(doseHour),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SoftTheme.TextWhite,
                                            modifier = Modifier.padding(horizontal = 6.dp)
                                        )
                                        IconButton(
                                            onClick = { doseHour = if (doseHour < 12) doseHour + 1 else 1 },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Text("+", fontSize = 16.sp, color = SoftTheme.SoftPink, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Text(":", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SoftTheme.SoftGray)

                                    // Minute Selector
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(SoftTheme.CardSlate)
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        IconButton(
                                            onClick = { doseMinute = if (doseMinute >= 15) doseMinute - 15 else 45 },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Text("-", fontSize = 16.sp, color = SoftTheme.SoftPink, fontWeight = FontWeight.Bold)
                                        }
                                        Text(
                                            text = "%02d".format(doseMinute),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SoftTheme.TextWhite,
                                            modifier = Modifier.padding(horizontal = 6.dp)
                                        )
                                        IconButton(
                                            onClick = { doseMinute = if (doseMinute <= 30) doseMinute + 15 else 0 },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Text("+", fontSize = 16.sp, color = SoftTheme.SoftPink, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    // AM / PM Toggle
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(SoftTheme.CardSlate)
                                            .padding(4.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (!doseIsPm) SoftTheme.MintTeal else Color.Transparent,
                                            modifier = Modifier.clickable { doseIsPm = false }
                                        ) {
                                            Text(
                                                text = "صباحاً ☀️",
                                                color = if (!doseIsPm) Color.White else SoftTheme.SoftGray,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (doseIsPm) SoftTheme.SoftPink else Color.Transparent,
                                            modifier = Modifier.clickable { doseIsPm = true }
                                        ) {
                                            Text(
                                                text = "مساءً 🌙",
                                                color = if (doseIsPm) Color.White else SoftTheme.SoftGray,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                            )
                                        }
                                    }
                                }

                                // Quick Time Presets
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val timePresets = listOf(
                                        Triple(8, 0, false) to "٠٨:٠٠ ص 🌅",
                                        Triple(2, 0, true) to "٠٢:٠٠ م ☀️",
                                        Triple(8, 0, true) to "٠٨:٠٠ م 🌆",
                                        Triple(10, 0, true) to "١٠:٠٠ م 🌙"
                                    )
                                    timePresets.forEach { (timeVal, label) ->
                                        val isCurrent = doseHour == timeVal.first && doseMinute == timeVal.second && doseIsPm == timeVal.third
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isCurrent) SoftTheme.MintTeal.copy(alpha = 0.2f) else SoftTheme.CardSlate,
                                            border = BorderStroke(
                                                1.dp,
                                                if (isCurrent) SoftTheme.MintTeal else Color.Transparent
                                            ),
                                            modifier = Modifier.clickable {
                                                doseHour = timeVal.first
                                                doseMinute = timeVal.second
                                                doseIsPm = timeVal.third
                                            }
                                        ) {
                                            Text(
                                                text = label,
                                                fontSize = 10.sp,
                                                color = if (isCurrent) SoftTheme.MintTeal else SoftTheme.SoftGray,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 5. Meal Relation Selector
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("علاقة الجرعة بالطعام 🍽️:", fontSize = 12.sp, color = SoftTheme.SoftGray, fontWeight = FontWeight.SemiBold)
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            mealRelations.forEach { relation ->
                                val isSelected = medMealRelation == relation
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) SoftTheme.SoftPink else SoftTheme.DeepSlate,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) SoftTheme.SoftPink else SoftTheme.CardSlate
                                    ),
                                    modifier = Modifier.clickable { medMealRelation = relation }
                                ) {
                                    Text(
                                        text = relation,
                                        color = if (isSelected) Color.White else SoftTheme.TextWhite,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 6. Schedule Type (Daily, PRN, etc.)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("نوع الجدولة والتكرار 🗓️:", fontSize = 12.sp, color = SoftTheme.SoftGray, fontWeight = FontWeight.SemiBold)
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            scheduleTypes.forEach { schedule ->
                                val isSelected = medScheduleType == schedule
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) SoftTheme.MintTeal else SoftTheme.DeepSlate,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) SoftTheme.MintTeal else SoftTheme.CardSlate
                                    ),
                                    modifier = Modifier.clickable { medScheduleType = schedule }
                                ) {
                                    Text(
                                        text = schedule,
                                        color = if (isSelected) Color.White else SoftTheme.TextWhite,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 7. Stock Tracker
                item {
                    OutlinedTextField(
                        value = medTotalStock,
                        onValueChange = { medTotalStock = it },
                        label = { Text("عدد الحبات في العلبة (لتتبع المخزون 📦)") },
                        placeholder = { Text("مثال: 30 حبة - لتذكيركِ قبل النفاد") },
                        leadingIcon = {
                            Icon(Icons.Default.Inventory2, contentDescription = null, tint = SoftTheme.MintTeal)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoftTheme.MintTeal,
                            unfocusedBorderColor = SoftTheme.SoftGray.copy(alpha = 0.4f),
                            focusedTextColor = SoftTheme.TextWhite,
                            unfocusedTextColor = SoftTheme.TextWhite,
                            focusedLabelColor = SoftTheme.MintTeal,
                            unfocusedLabelColor = SoftTheme.SoftGray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 8. Doctor Name & Notes
                item {
                    OutlinedTextField(
                        value = medDoctor,
                        onValueChange = { medDoctor = it },
                        label = { Text("اسم الطبيبة الموصية (اختياري)") },
                        placeholder = { Text("مثال: د. منى أحمد") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = SoftTheme.SoftPink)
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoftTheme.SoftPink,
                            unfocusedBorderColor = SoftTheme.SoftGray.copy(alpha = 0.4f),
                            focusedTextColor = SoftTheme.TextWhite,
                            unfocusedTextColor = SoftTheme.TextWhite,
                            focusedLabelColor = SoftTheme.SoftPink,
                            unfocusedLabelColor = SoftTheme.SoftGray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = medNotes,
                        onValueChange = { medNotes = it },
                        label = { Text("ملاحظات إضافية وإرشادات") },
                        placeholder = { Text("مثال: يؤخذ مع كوب عصير برتقال لتعزيز الامتصاص") },
                        leadingIcon = {
                            Icon(Icons.Default.EditNote, contentDescription = null, tint = SoftTheme.SoftGray)
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoftTheme.SoftPink,
                            unfocusedBorderColor = SoftTheme.SoftGray.copy(alpha = 0.4f),
                            focusedTextColor = SoftTheme.TextWhite,
                            unfocusedTextColor = SoftTheme.TextWhite,
                            focusedLabelColor = SoftTheme.SoftPink,
                            unfocusedLabelColor = SoftTheme.SoftGray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Action Buttons
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftTheme.SoftGray),
                            border = BorderStroke(1.dp, SoftTheme.SoftGray.copy(alpha = 0.4f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء", fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = {
                                if (medName.isNotBlank()) {
                                    val cal = Calendar.getInstance()
                                    var targetHour = doseHour % 12
                                    if (doseIsPm) targetHour += 12
                                    cal.set(Calendar.HOUR_OF_DAY, targetHour)
                                    cal.set(Calendar.MINUTE, doseMinute)
                                    cal.set(Calendar.SECOND, 0)
                                    if (cal.timeInMillis <= System.currentTimeMillis()) {
                                        cal.add(Calendar.DAY_OF_YEAR, 1)
                                    }

                                    val stockCount = medTotalStock.toIntOrNull() ?: 0
                                    val combinedNotes = buildString {
                                        if (medMealRelation.isNotBlank()) append("🍽️ $medMealRelation")
                                        if (medScheduleType.isNotBlank()) append(" • $medScheduleType")
                                        if (medNotes.isNotBlank()) append(" • $medNotes")
                                    }

                                    val combinedWarning = "شكل العلاج: $medFormType • $medMealRelation"

                                    onSave(
                                        medName.trim(),
                                        medDosage.ifBlank { "حسب الإرشاد" },
                                        medTimes.toIntOrNull() ?: 1,
                                        medDoctor.ifBlank { null },
                                        combinedNotes,
                                        cal.timeInMillis,
                                        stockCount,
                                        combinedWarning
                                    )
                                }
                            },
                            enabled = medName.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1.8f)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text(
                                    if (editingMedication != null) "حفظ التعديلات" else "حفظ وجدولة المنبه",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MedicationDoseHistoryDialog(
    targetMed: MedicationLog,
    adherenceLogs: List<MedicationAdherenceLog>,
    onDismiss: () -> Unit
) {
    val medHistory = adherenceLogs.filter { it.medicationId == targetMed.id }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
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
                    Column {
                        Text(
                            text = "سجل جرعات: ${targetMed.name}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.SoftPink
                        )
                        Text(
                            text = "إجمالي الجرعات المسجلة: ${medHistory.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTheme.SoftGray
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = SoftTheme.SoftGray)
                    }
                }

                if (medHistory.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "لم يتم تأكيد أخذ جرعات سابقة بعد.\nعند الضغط على 'أخذت الجرعة' سيتم تسجيل الوقت هنا.",
                            color = SoftTheme.SoftGray,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(medHistory, key = { it.id }) { log ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(if (log.status == "TAKEN") "✅" else "⚠️", fontSize = 16.sp)
                                        Column {
                                            Text(
                                                text = if (log.status == "TAKEN") "تم تناول الجرعة" else "جرعة مفوتة",
                                                fontWeight = FontWeight.Bold,
                                                color = if (log.status == "TAKEN") SoftTheme.MintTeal else SoftTheme.SoftPink,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = formatGregorianDate(log.actualTime ?: log.scheduledTime),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = SoftTheme.SoftGray,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = SoftTheme.CardSlate
                                    ) {
                                        Text(
                                            text = targetMed.dosage ?: "جرعة",
                                            color = SoftTheme.TextWhite,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
