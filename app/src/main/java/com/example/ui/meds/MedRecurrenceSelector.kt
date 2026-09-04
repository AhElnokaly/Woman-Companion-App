package com.example.ui.meds

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.meds.MedicationRecurrenceType
import com.example.data.meds.SpecificDoseTime
import com.example.ui.SoftTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MedRecurrenceSelector(
    selectedRecurrence: MedicationRecurrenceType,
    onRecurrenceSelected: (MedicationRecurrenceType) -> Unit,
    doseTimes: List<SpecificDoseTime>,
    onDoseTimesChanged: (List<SpecificDoseTime>) -> Unit,
    intervalHours: Int,
    onIntervalHoursChanged: (Int) -> Unit,
    intervalDays: Int,
    onIntervalDaysChanged: (Int) -> Unit,
    selectedWeekDays: Set<Int>, // 1 = Sunday, 2 = Monday, etc.
    onWeekDaysChanged: (Set<Int>) -> Unit,
    modifier: Modifier = Modifier
) {
    val weekDaysList = listOf(
        7 to "السبت",
        1 to "الأحد",
        2 to "الإثنين",
        3 to "الثلاثاء",
        4 to "الأربعاء",
        5 to "الخميس",
        6 to "الجمعة"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "تكرار المنبه وجدول المواعيد: ⏰",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = SoftTheme.TextWhite
        )

        // Horizontal Recurrence Type Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MedicationRecurrenceType.values().forEach { recType ->
                val isSelected = selectedRecurrence == recType
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) SoftTheme.SoftPink else SoftTheme.CardSlate)
                        .border(
                            1.dp,
                            if (isSelected) SoftTheme.SoftPink else Color.Transparent,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onRecurrenceSelected(recType) }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = "${recType.icon} ${recType.label}",
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) SoftTheme.DeepSlate else SoftTheme.TextWhite
                    )
                }
            }
        }

        // Sub-panels based on selected recurrence
        when (selectedRecurrence) {
            MedicationRecurrenceType.DAILY -> {
                DailyDoseTimesSubSection(
                    doseTimes = doseTimes,
                    onDoseTimesChanged = onDoseTimesChanged
                )
            }
            MedicationRecurrenceType.EVERY_X_HOURS -> {
                EveryXHoursSubSection(
                    intervalHours = intervalHours,
                    onIntervalHoursChanged = onIntervalHoursChanged,
                    doseTimes = doseTimes,
                    onDoseTimesChanged = onDoseTimesChanged
                )
            }
            MedicationRecurrenceType.EVERY_X_DAYS -> {
                EveryXDaysSubSection(
                    intervalDays = intervalDays,
                    onIntervalDaysChanged = onIntervalDaysChanged,
                    doseTimes = doseTimes,
                    onDoseTimesChanged = onDoseTimesChanged
                )
            }
            MedicationRecurrenceType.SPECIFIC_WEEK_DAYS -> {
                SpecificWeekDaysSubSection(
                    weekDaysList = weekDaysList,
                    selectedWeekDays = selectedWeekDays,
                    onWeekDaysChanged = onWeekDaysChanged,
                    doseTimes = doseTimes,
                    onDoseTimesChanged = onDoseTimesChanged
                )
            }
            MedicationRecurrenceType.MONTHLY -> {
                MonthlySubSection(
                    doseTimes = doseTimes,
                    onDoseTimesChanged = onDoseTimesChanged
                )
            }
            MedicationRecurrenceType.AS_NEEDED -> {
                Surface(
                    color = SoftTheme.CardSlate,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "⚡ هذا الدواء سيُحفظ للاستخدام عند اللزوم (PRN) دون إطلاق منبهات يومية متكررة، مع إمكانية تسجيل الجرعة في أي وقت بنقرة واحدة.",
                        fontSize = 11.sp,
                        color = SoftTheme.SoftGray,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyDoseTimesSubSection(
    doseTimes: List<SpecificDoseTime>,
    onDoseTimesChanged: (List<SpecificDoseTime>) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SoftTheme.CardSlate.copy(alpha = 0.6f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "مواعيد الجرعات اليومية (${doseTimes.size} جرعة):",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SoftTheme.MintTeal
            )

            if (doseTimes.size < 6) {
                TextButton(
                    onClick = {
                        val nextHour = if (doseTimes.isEmpty()) 9 else (doseTimes.last().hour24 + 4) % 24
                        onDoseTimesChanged(doseTimes + SpecificDoseTime(nextHour, 0, "جرعة ${doseTimes.size + 1}"))
                    },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = SoftTheme.SoftPink)
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("إضافة موعد جرعة", fontSize = 10.sp, color = SoftTheme.SoftPink, fontWeight = FontWeight.Bold)
                }
            }
        }

        // List of editable dose times
        doseTimes.forEachIndexed { index, doseTime ->
            SingleDoseTimePickerRow(
                doseNumber = index + 1,
                doseTime = doseTime,
                canDelete = doseTimes.size > 1,
                onTimeUpdated = { updated ->
                    val newList = doseTimes.toMutableList()
                    newList[index] = updated
                    onDoseTimesChanged(newList)
                },
                onDelete = {
                    if (doseTimes.size > 1) {
                        val newList = doseTimes.toMutableList()
                        newList.removeAt(index)
                        onDoseTimesChanged(newList)
                    }
                }
            )
        }
    }
}

@Composable
private fun EveryXHoursSubSection(
    intervalHours: Int,
    onIntervalHoursChanged: (Int) -> Unit,
    doseTimes: List<SpecificDoseTime>,
    onDoseTimesChanged: (List<SpecificDoseTime>) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SoftTheme.CardSlate.copy(alpha = 0.6f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("تحديد الفاصل بالساعات:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SoftTheme.MintTeal)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(4, 6, 8, 12).forEach { hrs ->
                val isSel = intervalHours == hrs
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSel) SoftTheme.MintTeal else SoftTheme.DeepSlate)
                        .clickable {
                            onIntervalHoursChanged(hrs)
                            // Auto calculate dose times
                            val firstDose = doseTimes.firstOrNull() ?: SpecificDoseTime(8, 0)
                            val count = 24 / hrs
                            val newTimes = (0 until count).map { i ->
                                val h = (firstDose.hour24 + (i * hrs)) % 24
                                SpecificDoseTime(h, firstDose.minute, "الجرعة ${i + 1}")
                            }
                            onDoseTimesChanged(newTimes)
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "كل $hrs ساعات",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSel) SoftTheme.DeepSlate else SoftTheme.TextWhite
                    )
                }
            }
        }

        // Show start dose time
        val first = doseTimes.firstOrNull() ?: SpecificDoseTime(8, 0)
        SingleDoseTimePickerRow(
            doseNumber = 1,
            doseTime = first,
            labelOverride = "وقت الجرعة الأولى (نقطة البداية):",
            canDelete = false,
            onTimeUpdated = { updated ->
                val count = if (intervalHours > 0) 24 / intervalHours else 1
                val newTimes = (0 until count).map { i ->
                    val h = (updated.hour24 + (i * intervalHours)) % 24
                    SpecificDoseTime(h, updated.minute, "الجرعة ${i + 1}")
                }
                onDoseTimesChanged(newTimes)
            },
            onDelete = {}
        )
    }
}

@Composable
private fun EveryXDaysSubSection(
    intervalDays: Int,
    onIntervalDaysChanged: (Int) -> Unit,
    doseTimes: List<SpecificDoseTime>,
    onDoseTimesChanged: (List<SpecificDoseTime>) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SoftTheme.CardSlate.copy(alpha = 0.6f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("تحديد الفاصل بالأيام:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SoftTheme.MintTeal)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(2 to "يوم بعد يوم (كل ٢ يوم)", 3 to "كل ٣ أيام", 5 to "كل ٥ أيام").forEach { (days, label) ->
                val isSel = intervalDays == days
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSel) SoftTheme.MintTeal else SoftTheme.DeepSlate)
                        .clickable { onIntervalDaysChanged(days) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSel) SoftTheme.DeepSlate else SoftTheme.TextWhite
                    )
                }
            }
        }

        val first = doseTimes.firstOrNull() ?: SpecificDoseTime(9, 0)
        SingleDoseTimePickerRow(
            doseNumber = 1,
            doseTime = first,
            labelOverride = "وقت المنبه في يوم الجرعة:",
            canDelete = false,
            onTimeUpdated = { onDoseTimesChanged(listOf(it)) },
            onDelete = {}
        )
    }
}

@Composable
private fun SpecificWeekDaysSubSection(
    weekDaysList: List<Pair<Int, String>>,
    selectedWeekDays: Set<Int>,
    onWeekDaysChanged: (Set<Int>) -> Unit,
    doseTimes: List<SpecificDoseTime>,
    onDoseTimesChanged: (List<SpecificDoseTime>) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SoftTheme.CardSlate.copy(alpha = 0.6f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("اختاري أيام الأسبوع المستهدفة:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SoftTheme.MintTeal)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weekDaysList.forEach { (dayIndex, dayLabel) ->
                val isSel = selectedWeekDays.contains(dayIndex)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isSel) SoftTheme.MintTeal else SoftTheme.DeepSlate)
                        .clickable {
                            val newSet = if (isSel) selectedWeekDays - dayIndex else selectedWeekDays + dayIndex
                            onWeekDaysChanged(newSet)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = dayLabel.take(2),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSel) SoftTheme.DeepSlate else SoftTheme.TextWhite
                    )
                }
            }
        }

        val first = doseTimes.firstOrNull() ?: SpecificDoseTime(9, 0)
        SingleDoseTimePickerRow(
            doseNumber = 1,
            doseTime = first,
            labelOverride = "وقت المنبه في الأيام المحددة:",
            canDelete = false,
            onTimeUpdated = { onDoseTimesChanged(listOf(it)) },
            onDelete = {}
        )
    }
}

@Composable
private fun MonthlySubSection(
    doseTimes: List<SpecificDoseTime>,
    onDoseTimesChanged: (List<SpecificDoseTime>) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SoftTheme.CardSlate.copy(alpha = 0.6f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("تذكير شهري دوري في نفس اليوم من كل شهر 📆", fontSize = 11.sp, color = SoftTheme.MintTeal, fontWeight = FontWeight.Bold)

        val first = doseTimes.firstOrNull() ?: SpecificDoseTime(10, 0)
        SingleDoseTimePickerRow(
            doseNumber = 1,
            doseTime = first,
            labelOverride = "وقت المنبه الشهري:",
            canDelete = false,
            onTimeUpdated = { onDoseTimesChanged(listOf(it)) },
            onDelete = {}
        )
    }
}

@Composable
fun SingleDoseTimePickerRow(
    doseNumber: Int,
    doseTime: SpecificDoseTime,
    labelOverride: String? = null,
    canDelete: Boolean = false,
    onTimeUpdated: (SpecificDoseTime) -> Unit,
    onDelete: () -> Unit
) {
    val isPm = doseTime.hour24 >= 12
    val hour12 = when {
        doseTime.hour24 == 0 -> 12
        doseTime.hour24 > 12 -> doseTime.hour24 - 12
        else -> doseTime.hour24
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SoftTheme.DeepSlate)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = labelOverride ?: "الجرعة $doseNumber:",
            fontSize = 11.sp,
            color = SoftTheme.TextWhite,
            fontWeight = FontWeight.Medium
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Hour adjust
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(SoftTheme.CardSlate)
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                IconButton(
                    onClick = {
                        val newH12 = if (hour12 > 1) hour12 - 1 else 12
                        val newH24 = if (isPm) (if (newH12 == 12) 12 else newH12 + 12) else (if (newH12 == 12) 0 else newH12)
                        onTimeUpdated(doseTime.copy(hour24 = newH24))
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Text("-", fontSize = 14.sp, color = SoftTheme.SoftPink, fontWeight = FontWeight.Bold)
                }
                Text("%02d".format(hour12), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
                IconButton(
                    onClick = {
                        val newH12 = if (hour12 < 12) hour12 + 1 else 1
                        val newH24 = if (isPm) (if (newH12 == 12) 12 else newH12 + 12) else (if (newH12 == 12) 0 else newH12)
                        onTimeUpdated(doseTime.copy(hour24 = newH24))
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Text("+", fontSize = 14.sp, color = SoftTheme.SoftPink, fontWeight = FontWeight.Bold)
                }
            }

            Text(":", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SoftTheme.SoftGray)

            // Minute adjust (step by 5/15)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(SoftTheme.CardSlate)
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                IconButton(
                    onClick = {
                        val newMin = if (doseTime.minute >= 15) doseTime.minute - 15 else 45
                        onTimeUpdated(doseTime.copy(minute = newMin))
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Text("-", fontSize = 14.sp, color = SoftTheme.SoftPink, fontWeight = FontWeight.Bold)
                }
                Text("%02d".format(doseTime.minute), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
                IconButton(
                    onClick = {
                        val newMin = if (doseTime.minute <= 30) doseTime.minute + 15 else 0
                        onTimeUpdated(doseTime.copy(minute = newMin))
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Text("+", fontSize = 14.sp, color = SoftTheme.SoftPink, fontWeight = FontWeight.Bold)
                }
            }

            // AM / PM toggle
            Surface(
                color = if (isPm) SoftTheme.MintTeal.copy(alpha = 0.2f) else SoftTheme.SoftPink.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.clickable {
                    val toggledH24 = if (isPm) doseTime.hour24 - 12 else doseTime.hour24 + 12
                    onTimeUpdated(doseTime.copy(hour24 = toggledH24))
                }
            ) {
                Text(
                    text = if (isPm) "م 🌙" else "ص ☀️",
                    color = if (isPm) SoftTheme.MintTeal else SoftTheme.SoftPink,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            if (canDelete) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "حذف الموعد", tint = SoftTheme.RedDanger, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
