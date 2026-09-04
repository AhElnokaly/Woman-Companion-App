package com.example.ui.meds

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.MedicationLog
import com.example.data.meds.MealRelation
import com.example.data.meds.MedicationDosageUnit
import com.example.data.meds.MedicationRecurrenceType
import com.example.data.meds.SpecificDoseTime
import com.example.ui.SoftTheme
import java.util.Calendar

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
    var dosageAmount by rememberSaveable {
        val initialDosage = editingMedication?.dosage ?: ""
        val numOnly = initialDosage.filter { it.isDigit() || it == '.' }
        mutableStateOf(if (numOnly.isNotBlank()) numOnly else "1")
    }
    var selectedUnit by remember {
        val currentDosage = editingMedication?.dosage ?: ""
        val foundUnit = MedicationDosageUnit.values().find { currentDosage.contains(it.label) }
        mutableStateOf(foundUnit ?: MedicationDosageUnit.TABLET)
    }

    var selectedMealRelation by remember {
        val warning = editingMedication?.safetyWarning ?: ""
        val foundRelation = MealRelation.values().find { warning.contains(it.label) }
        mutableStateOf(foundRelation ?: MealRelation.AFTER_MEAL)
    }

    var selectedRecurrence by remember {
        val notes = editingMedication?.notes ?: ""
        val foundRec = MedicationRecurrenceType.values().find { notes.contains(it.label) }
        mutableStateOf(foundRec ?: MedicationRecurrenceType.DAILY)
    }

    // Dose times state
    val initialCal = remember {
        Calendar.getInstance().apply {
            timeInMillis = editingMedication?.startDate ?: System.currentTimeMillis()
        }
    }
    var doseTimes by remember {
        val startH = initialCal.get(Calendar.HOUR_OF_DAY)
        val startM = initialCal.get(Calendar.MINUTE)
        val initialTimesCount = (editingMedication?.timesPerDay ?: 1).coerceAtLeast(1)
        val list = (0 until initialTimesCount).map { i ->
            val h = (startH + (i * (24 / initialTimesCount))) % 24
            SpecificDoseTime(h, startM, "جرعة ${i + 1}")
        }
        mutableStateOf(list)
    }

    var intervalHours by rememberSaveable { mutableIntStateOf(8) }
    var intervalDays by rememberSaveable { mutableIntStateOf(2) }
    var selectedWeekDays by remember { mutableStateOf(setOf(7, 1, 3, 5)) } // Default Sat, Sun, Tue, Thu

    // Advanced options state
    var isAdvancedExpanded by rememberSaveable { mutableStateOf(false) }
    var prescribedDoctor by rememberSaveable { mutableStateOf(editingMedication?.prescribedBy ?: "") }
    var medNotes by rememberSaveable { mutableStateOf(editingMedication?.notes ?: "") }
    var stockCount by rememberSaveable {
        mutableStateOf(
            if ((editingMedication?.remainingQuantity ?: 0) > 0)
                editingMedication?.remainingQuantity.toString()
            else ""
        )
    }
    var courseDurationDays by rememberSaveable { mutableStateOf("") }

    val quickPregnancyMeds = listOf(
        Triple("حمض الفوليك (Folic Acid)", "1", MedicationDosageUnit.TABLET),
        Triple("مكمل حديد (Iron)", "1", MedicationDosageUnit.CAPSULE),
        Triple("كالسيوم وفيتامين د", "1", MedicationDosageUnit.TABLET),
        Triple("برينتال ملتي فيتامين", "1", MedicationDosageUnit.CAPSULE),
        Triple("فيتامين C فوار", "1", MedicationDosageUnit.SACHET),
        Triple("مضاد غثيان (Navidoxine)", "1", MedicationDosageUnit.TABLET)
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.25f)),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.90f)
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (editingMedication != null) "تعديل جدول الدواء 💊" else "إضافة دواء جديد 💊",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.SoftPink
                        )
                        Text(
                            text = "منبه دقيق ومخصص لمواعيد الجرعات",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTheme.SoftGray
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = SoftTheme.SoftGray)
                    }
                }

                HorizontalDivider(color = SoftTheme.DeepSlate, thickness = 1.dp)

                // Scrollable Form Body
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Quick suggestion chips
                    if (editingMedication == null) {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "أدوية ومكملات شائعة (اختيار سريع):",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SoftTheme.SoftGray
                                )
                                androidx.compose.foundation.lazy.LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    items(quickPregnancyMeds) { (quickName, qDosage, qUnit) ->
                                        Surface(
                                            color = SoftTheme.DeepSlate,
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.3f)),
                                            modifier = Modifier.clickable {
                                                medName = quickName
                                                dosageAmount = qDosage
                                                selectedUnit = qUnit
                                            }
                                        ) {
                                            Text(
                                                text = quickName,
                                                fontSize = 10.sp,
                                                color = SoftTheme.TextWhite,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 2. Medication Name
                    item {
                        OutlinedTextField(
                            value = medName,
                            onValueChange = { medName = it },
                            label = { Text("اسم الدواء / الفيتامين *", fontSize = 12.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SoftTheme.SoftPink,
                                unfocusedBorderColor = SoftTheme.DeepSlate,
                                focusedTextColor = SoftTheme.TextWhite,
                                unfocusedTextColor = SoftTheme.TextWhite
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // 3. Structured Dosage & Unit
                    item {
                        MedDosageSelector(
                            dosageAmount = dosageAmount,
                            onDosageAmountChange = { dosageAmount = it },
                            selectedUnit = selectedUnit,
                            onUnitSelected = { selectedUnit = it }
                        )
                    }

                    // 4. Meal Timing
                    item {
                        MedMealTimingSelector(
                            selectedMealRelation = selectedMealRelation,
                            onMealRelationSelected = { selectedMealRelation = it }
                        )
                    }

                    // 5. Recurrence and Times Engine
                    item {
                        MedRecurrenceSelector(
                            selectedRecurrence = selectedRecurrence,
                            onRecurrenceSelected = { selectedRecurrence = it },
                            doseTimes = doseTimes,
                            onDoseTimesChanged = { doseTimes = it },
                            intervalHours = intervalHours,
                            onIntervalHoursChanged = { intervalHours = it },
                            intervalDays = intervalDays,
                            onIntervalDaysChanged = { intervalDays = it },
                            selectedWeekDays = selectedWeekDays,
                            onWeekDaysChanged = { selectedWeekDays = it }
                        )
                    }

                    // 6. Advanced Options (Accordion)
                    item {
                        MedAdvancedOptionsSection(
                            isExpanded = isAdvancedExpanded,
                            onToggleExpand = { isAdvancedExpanded = !isAdvancedExpanded },
                            prescribedDoctor = prescribedDoctor,
                            onPrescribedDoctorChange = { prescribedDoctor = it },
                            medNotes = medNotes,
                            onMedNotesChange = { medNotes = it },
                            stockCount = stockCount,
                            onStockCountChange = { stockCount = it },
                            courseDurationDays = courseDurationDays,
                            onCourseDurationDaysChange = { courseDurationDays = it }
                        )
                    }
                }

                // Footer Save Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftTheme.SoftGray),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إلغاء", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            if (medName.isNotBlank()) {
                                val amountStr = dosageAmount.ifBlank { "1" }
                                val formattedDosage = "$amountStr ${selectedUnit.label}"
                                val stock = stockCount.toIntOrNull() ?: 0

                                val firstDose = doseTimes.firstOrNull() ?: SpecificDoseTime(8, 0)
                                val cal = Calendar.getInstance().apply {
                                    set(Calendar.HOUR_OF_DAY, firstDose.hour24)
                                    set(Calendar.MINUTE, firstDose.minute)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }

                                val timesListFormatted = doseTimes.joinToString("، ") { it.formatFormattedArabic() }
                                val recurrenceDetail = when (selectedRecurrence) {
                                    MedicationRecurrenceType.DAILY -> "يومياً ($timesListFormatted)"
                                    MedicationRecurrenceType.EVERY_X_HOURS -> "كل $intervalHours ساعات ($timesListFormatted)"
                                    MedicationRecurrenceType.EVERY_X_DAYS -> "كل $intervalDays أيام ($timesListFormatted)"
                                    MedicationRecurrenceType.SPECIFIC_WEEK_DAYS -> "أيام محددة أسبوعياً ($timesListFormatted)"
                                    MedicationRecurrenceType.MONTHLY -> "شهرياً ($timesListFormatted)"
                                    MedicationRecurrenceType.AS_NEEDED -> "عند اللزوم ⚡"
                                }

                                val combinedNotes = buildString {
                                    append(recurrenceDetail)
                                    if (courseDurationDays.isNotBlank()) append(" • لمدة $courseDurationDays يوم")
                                    if (medNotes.isNotBlank()) append(" • $medNotes")
                                }

                                val combinedWarning = "${selectedMealRelation.icon} ${selectedMealRelation.label} • ${selectedUnit.icon} ${selectedUnit.label}"

                                onSave(
                                    medName.trim(),
                                    formattedDosage,
                                    doseTimes.size.coerceAtLeast(1),
                                    prescribedDoctor.ifBlank { null },
                                    combinedNotes,
                                    cal.timeInMillis,
                                    stock,
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
                                if (editingMedication != null) "حفظ التعديلات" else "حفظ وجدولة المنبه ⏰",
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
