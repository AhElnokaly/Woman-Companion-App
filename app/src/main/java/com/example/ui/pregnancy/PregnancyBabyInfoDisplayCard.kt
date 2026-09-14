package com.example.ui.pregnancy

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Appointment
import com.example.data.FetalGrowthLog
import com.example.ui.FetalStandardData
import com.example.ui.SoftTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PregnancyBabyInfoDisplayCard(
    babyGender: String?,
    babyName: String?,
    currentWeekNumber: Int,
    latestFetalLog: FetalGrowthLog?,
    upcomingDoctorAppointment: Appointment?,
    onOpenCareDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isGenderKnown = !babyGender.isNullOrEmpty()
    val hasFetalLog = latestFetalLog != null && (latestFetalLog.weightGrams > 0 || latestFetalLog.lengthCm > 0)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("baby_health_hub_card"),
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardBg),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, SoftTheme.CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // --- 1. Header: Baby Identity & Edit CTA ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(SoftTheme.MintAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (babyGender == "ولد") "👶" else if (babyGender == "بنت") "👧" else "🧸",
                            fontSize = 20.sp
                        )
                    }
                    Column {
                        val genderLabel = when (babyGender) {
                            "ولد" -> "جنينكِ الغالي (ولد صالح) 💙"
                            "بنت" -> "جنينكِ الغالية (بنت صالحة) 💗"
                            else -> "جنينكِ الغالي (مفاجأة) ✨"
                        }
                        val nameLabel = if (!babyName.isNullOrBlank()) "الاسم: $babyName" else "لم يتم اختيار اسم بعد"

                        Text(
                            text = genderLabel,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.TextPrimary
                        )
                        Text(
                            text = nameLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTheme.EmeraldPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Edit Button
                OutlinedButton(
                    onClick = onOpenCareDialog,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, SoftTheme.EmeraldPrimary.copy(alpha = 0.5f)),
                    modifier = Modifier.testTag("open_baby_care_dialog_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = SoftTheme.EmeraldPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "تعديل وفحص 🩺",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.EmeraldPrimary,
                        fontSize = 11.sp
                    )
                }
            }

            HorizontalDivider(color = SoftTheme.CardBorder.copy(alpha = 0.6f), thickness = 1.dp)

            // --- 2. Fetal Measurements Section (Weight & Length) ---
            if (hasFetalLog) {
                val log = latestFetalLog!!
                val std = FetalStandardData.getStandardForWeek(log.pregnancyWeek)
                val isWeightNormal = std == null || (log.weightGrams >= std.weightGrams * 0.8 && log.weightGrams <= std.weightGrams * 1.25)

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "آخر قياسات سونار (الأسبوع ${log.pregnancyWeek})",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTheme.TextSecondaryMuted,
                            fontSize = 11.sp
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isWeightNormal) SoftTheme.MintAccent else SoftTheme.WarmCoral.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (isWeightNormal) "نمو طبيعي ممتاز 🌸" else "متابعة مع الطبيبة 🩺",
                                color = if (isWeightNormal) SoftTheme.EmeraldPrimary else SoftTheme.WarmCoral,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Weight Box
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = SoftTheme.CanvasBg,
                            border = BorderStroke(1.dp, SoftTheme.CardBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Scale, contentDescription = null, tint = SoftTheme.EmeraldPrimary, modifier = Modifier.size(20.dp))
                                Column {
                                    Text("وزن الجنين", fontSize = 10.sp, color = SoftTheme.TextSecondaryMuted)
                                    Text(
                                        text = if (log.weightGrams > 0) "${log.weightGrams.toInt()} جم" else "غير مسجل",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SoftTheme.TextPrimary
                                    )
                                }
                            }
                        }

                        // Length Box
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = SoftTheme.CanvasBg,
                            border = BorderStroke(1.dp, SoftTheme.CardBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Straighten, contentDescription = null, tint = SoftTheme.EmeraldPrimary, modifier = Modifier.size(20.dp))
                                Column {
                                    Text("طول الجنين", fontSize = 10.sp, color = SoftTheme.TextSecondaryMuted)
                                    Text(
                                        text = if (log.lengthCm > 0) "${log.lengthCm} سم" else "غير مسجل",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SoftTheme.TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Call to action when no ultrasound logs yet
                Surface(
                    onClick = onOpenCareDialog,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = SoftTheme.CanvasBg,
                    border = BorderStroke(1.dp, SoftTheme.CardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("📏", fontSize = 18.sp)
                            Column {
                                Text(
                                    text = "لم يتم تسجيل قياسات سونار بعد",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftTheme.TextPrimary
                                )
                                Text(
                                    text = "سجلي وزن وطول الجنين لمتابعة منحنى نموه بدقة",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SoftTheme.TextSecondaryMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                        Text(
                            text = "+ إضافة 🩺",
                            color = SoftTheme.EmeraldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // --- 3. Upcoming Doctor Appointment Row ---
            if (upcomingDoctorAppointment != null) {
                val appt = upcomingDoctorAppointment
                val formattedDate = SimpleDateFormat("EEEE، dd MMMM - hh:mm a", Locale.forLanguageTag("ar")).format(Date(appt.dateTime))
                val docLabel = if (!appt.doctorName.isNullOrBlank()) "عند ${appt.doctorName}" else "العيادة"

                Surface(
                    onClick = onOpenCareDialog,
                    shape = RoundedCornerShape(12.dp),
                    color = SoftTheme.MintAccent.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, SoftTheme.CardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = SoftTheme.EmeraldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "زيارة الطبيبة القادمة 🏥",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoftTheme.TealDark
                            )
                            Text(
                                text = "${appt.title} ($docLabel) • $formattedDate",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SoftTheme.TextPrimary,
                                maxLines = 1
                            )
                        }
                    }
                }
            } else {
                Surface(
                    onClick = onOpenCareDialog,
                    shape = RoundedCornerShape(10.dp),
                    color = SoftTheme.CanvasBg.copy(alpha = 0.7f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = SoftTheme.TextSecondaryMuted, modifier = Modifier.size(16.dp))
                            Text(
                                text = "لا يوجد موعد زيارة طبيبة قادم محجوز",
                                fontSize = 11.sp,
                                color = SoftTheme.TextSecondaryMuted
                            )
                        }
                        Text(
                            text = "+ حجز موعد 📅",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.EmeraldPrimary
                        )
                    }
                }
            }
        }
    }
}
