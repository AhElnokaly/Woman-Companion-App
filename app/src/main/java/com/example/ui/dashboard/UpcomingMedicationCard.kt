package com.example.ui.dashboard

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MedicationLog
import com.example.reminder.calculateDailyDoseHours
import com.example.ui.SoftTheme
import com.example.viewmodel.WomanCompanionViewModel
import java.util.Calendar

/**
 * بطاقة الدواء القادم التفاعلية (Upcoming Medication Card)
 * تطابق كامل للتصميم المرفق:
 * - أيقونة الدواء واسمه وجرعته
 * - مؤقت الوقت المتبقي
 * - رسمة علبة الدواء الأنيقة مع الأقراص
 * - زر "أخذت الجرعة ✓" التفاعلي
 */
@Composable
fun UpcomingMedicationCard(
    viewModel: WomanCompanionViewModel,
    onNavigateToMeds: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activeMeds by viewModel.activeMedicationsState.collectAsState()
    val adherenceLogs by viewModel.allMedicationAdherenceLogsState.collectAsState()

    // Determine active medication or fallback to sample "أملوديبين 5 مجم"
    val todayTakenMedIds = remember(adherenceLogs) {
        val todayStart = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
        adherenceLogs
            .filter { it.scheduledTime >= todayStart && it.status == "TAKEN" }
            .map { it.medicationId }
            .toSet()
    }

    val currentMed = remember(activeMeds, todayTakenMedIds) {
        activeMeds.firstOrNull { it.id !in todayTakenMedIds && it.startDate != null && it.startDate > 0L }
            ?: activeMeds.firstOrNull { it.startDate != null && it.startDate > 0L }
            ?: activeMeds.firstOrNull()
    }

    if (currentMed == null || currentMed.startDate == null || currentMed.startDate <= 0L) {
        return
    }

    val now = System.currentTimeMillis()
    val startCal = Calendar.getInstance().apply {
        timeInMillis = currentMed.startDate
    }
    val startHour = startCal.get(Calendar.HOUR_OF_DAY)
    val startMinute = startCal.get(Calendar.MINUTE)
    val count = currentMed.timesPerDay.coerceIn(1, 12)
    val doseHours = calculateDailyDoseHours(startHour, count)

    // Calculate nearest upcoming dose (today or tomorrow) matching scheduleDailyDoses
    val nextDoseMillis = doseHours.map { hour ->
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, startMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (cal.timeInMillis <= now) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        cal.timeInMillis
    }.minOrNull()

    val remainingTimeText = if (nextDoseMillis != null) {
        val diffMillis = (nextDoseMillis - now).coerceAtLeast(0L)
        val totalMinutes = (diffMillis / (1000 * 60)).toInt()
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        when {
            hours > 0 && minutes > 0 -> "$hours ساعة و $minutes دقيقة"
            hours > 0 -> "$hours ساعة"
            minutes > 0 -> "$minutes دقيقة"
            else -> "الآن"
        }
    } else {
        "لا توجد جرعة قادمة"
    }

    var isTakenToday by remember(currentMed, todayTakenMedIds) {
        mutableStateOf(currentMed.id in todayTakenMedIds)
    }

    val medName = currentMed.name
    val medDesc = if (!currentMed.dosage.isNullOrBlank()) {
        "جرعة ${currentMed.dosage} - ${currentMed.notes?.ifBlank { "قرص واحد" } ?: "قرص واحد"}"
    } else {
        currentMed.notes?.ifBlank { "جرعة مقررة" } ?: "جرعة مقررة"
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .border(1.dp, SoftTheme.CardBorder, RoundedCornerShape(22.dp))
            .testTag("upcoming_medication_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // --- Header Row: Pill icon badge + "الدواء القادم" + Arrow ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateToMeds),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(SoftTheme.MintAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MedicalServices,
                            contentDescription = null,
                            tint = SoftTheme.EmeraldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = "الدواء القادم",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = SoftTheme.TextPrimary
                        )
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "عرض الأدوية",
                    tint = SoftTheme.TextSecondaryMuted,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- Details & 3D Medicine Pack Graphic Row ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Info Section (Name, Dosage, Remaining Time)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = medName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = SoftTheme.TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = medDesc,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            color = SoftTheme.TextSecondaryMuted
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = SoftTheme.EmeraldPrimary,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "الوقت المتبقي:",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = SoftTheme.TextSecondaryMuted
                            )
                        )
                        Text(
                            text = remainingTimeText,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoftTheme.EmeraldPrimary
                            )
                        )
                    }
                }

                // Clean 3D Vector Illustration of Medicine Pack & Pills
                Box(
                    modifier = Modifier
                        .size(width = 90.dp, height = 55.dp)
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        // Medicine box base
                        val boxRect = Size(w * 0.72f, h * 0.65f)
                        val boxTopLeft = Offset(w * 0.05f, h * 0.18f)

                        // Box shadow
                        drawRoundRect(
                            color = Color(0x1A000000),
                            topLeft = Offset(boxTopLeft.x + 3.dp.toPx(), boxTopLeft.y + 4.dp.toPx()),
                            size = boxRect,
                            cornerRadius = CornerRadius(6.dp.toPx())
                        )

                        // Main box body
                        drawRoundRect(
                            color = Color(0xFFF1F8F6),
                            topLeft = boxTopLeft,
                            size = boxRect,
                            cornerRadius = CornerRadius(6.dp.toPx())
                        )

                        // Top colored strip on the medicine box
                        drawRoundRect(
                            color = Color(0xFF80CBC4),
                            topLeft = boxTopLeft,
                            size = Size(boxRect.width, boxRect.height * 0.25f),
                            cornerRadius = CornerRadius(6.dp.toPx())
                        )

                        // Medicine pill 1 (round white tablet)
                        drawCircle(
                            color = Color(0x22000000),
                            radius = 7.dp.toPx(),
                            center = Offset(w * 0.78f + 1.dp.toPx(), h * 0.65f + 1.dp.toPx())
                        )
                        drawCircle(
                            color = Color(0xFFFFFFFF),
                            radius = 7.dp.toPx(),
                            center = Offset(w * 0.78f, h * 0.65f)
                        )
                        // Pill score line
                        drawLine(
                            color = Color(0xFFE0E0E0),
                            start = Offset(w * 0.74f, h * 0.65f),
                            end = Offset(w * 0.82f, h * 0.65f),
                            strokeWidth = 1.2.dp.toPx()
                        )

                        // Medicine pill 2
                        drawCircle(
                            color = Color(0x18000000),
                            radius = 6.dp.toPx(),
                            center = Offset(w * 0.90f + 1.dp.toPx(), h * 0.74f + 1.dp.toPx())
                        )
                        drawCircle(
                            color = Color(0xFFFBFBFB),
                            radius = 6.dp.toPx(),
                            center = Offset(w * 0.90f, h * 0.74f)
                        )
                    }

                    // Text printed on box
                    Column(
                        modifier = Modifier
                            .offset(x = (-10).dp, y = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Rx",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF164E43)
                        )
                        Text(
                            text = currentMed.dosage?.take(6) ?: "Rx",
                            fontSize = 6.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF00796B)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // --- Action Button: "أخذت الجرعة ✓" ---
            Button(
                onClick = {
                    if (!isTakenToday) {
                        isTakenToday = true
                        currentMed?.let { med ->
                            viewModel.recordMedicationAdherence(
                                medicationId = med.id,
                                scheduledTime = System.currentTimeMillis(),
                                status = "TAKEN"
                            )
                            if (med.remainingQuantity > 0) {
                                viewModel.decrementMedicationStock(med, 1)
                            }
                        }
                        Toast.makeText(context, "بارك الله فيكِ! تم تسجيل تناول الجرعة بنجاح ✓", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "الجرعة مسجلة بالفعل اليوم 🌸", Toast.LENGTH_SHORT).show()
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isTakenToday) SoftTheme.MintAccent else SoftTheme.EmeraldPrimary,
                    contentColor = if (isTakenToday) SoftTheme.EmeraldPrimary else Color.White
                ),
                contentPadding = PaddingValues(vertical = 12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("take_medication_dose_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (isTakenToday) "تم أخذ الجرعة اليوم ✓" else "أخذت الجرعة ✓",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    )
                }
            }
        }
    }
}
