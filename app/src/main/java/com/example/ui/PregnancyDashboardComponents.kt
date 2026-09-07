package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.MedicationLog
import com.example.ui.SoftTheme
import com.example.viewmodel.WomanCompanionViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ExactAlarmBannerCard() {
    val context = LocalContext.current
    val alarmManager = remember { context.getSystemService(android.content.Context.ALARM_SERVICE) as? android.app.AlarmManager }
    val canScheduleExact = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        alarmManager?.canScheduleExactAlarms() ?: true
    } else {
        true
    }

    if (!canScheduleExact) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SoftTheme.GoldFasting.copy(alpha = 0.15f)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, SoftTheme.GoldFasting)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("⚠️", fontSize = 22.sp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "تنبيه الإشعارات والمنبهات الدقيقة ⏰",
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.TextWhite,
                        fontSize = 13.sp
                    )
                    Text(
                        "يرجى السماح بإذن المنبهات الدقيقة لضمان وصول تذكيرات الأدوية والمواعيد في وقتها المظبوط دون تأخير.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTheme.SoftGray,
                        lineHeight = 15.sp
                    )
                }
                Button(
                    onClick = {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                            try {
                                val intent = android.content.Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                    data = android.net.Uri.parse("package:${context.packageName}")
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                val intent = android.content.Intent(android.provider.Settings.ACTION_SETTINGS)
                                context.startActivity(intent)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.GoldFasting),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("تفعيل", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DailyVitaminsCard(
    viewModel: WomanCompanionViewModel,
    onNavigateToMeds: () -> Unit = {}
) {
    val context = LocalContext.current
    val activeMedications by viewModel.activeMedicationsState.collectAsStateWithLifecycle()
    val adherenceLogs by viewModel.allMedicationAdherenceLogsState.collectAsStateWithLifecycle()

    // Persistent or remembered expansion state (Default expanded if medications exist, else compact)
    var isExpanded by remember { mutableStateOf(true) }

    // Calculate today's adherence for active medications
    val todayStartMillis = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val todayTakenMedIds = remember(adherenceLogs, todayStartMillis) {
        adherenceLogs
            .filter { it.scheduledTime >= todayStartMillis && it.status == "TAKEN" }
            .map { it.medicationId }
            .toSet()
    }

    val totalActiveMeds = activeMedications.size
    val takenMedsCount = activeMedications.count { it.id in todayTakenMedIds }
    val progressFraction = if (totalActiveMeds > 0) takenMedsCount.toFloat() / totalActiveMeds.toFloat() else 0f

    // Check for Iron and Calcium conflict in active medications
    val hasIronMed = remember(activeMedications) {
        activeMedications.any { med ->
            med.name.contains("حديد", ignoreCase = true) || 
            med.name.contains("iron", ignoreCase = true) ||
            med.name.contains("fer", ignoreCase = true)
        }
    }
    val hasCalciumMed = remember(activeMedications) {
        activeMedications.any { med ->
            med.name.contains("كالسيوم", ignoreCase = true) || 
            med.name.contains("calcium", ignoreCase = true) ||
            med.name.contains("كلس", ignoreCase = true)
        }
    }

    val arrowRotation by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "arrowRotation")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("daily_vitamins_card"),
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row (Clickable to Expand / Collapse)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("💊", fontSize = 22.sp)
                    Column {
                        Text(
                            text = "الأدوية والفيتامينات اليومية",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.TextWhite
                        )
                        if (!isExpanded && totalActiveMeds > 0) {
                            Text(
                                text = "مكتمل $takenMedsCount من $totalActiveMeds • اضغطي للتفاصيل 🌸",
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftTheme.SoftGray,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (totalActiveMeds > 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (takenMedsCount == totalActiveMeds) SoftTheme.MintTeal.copy(alpha = 0.2f) else SoftTheme.SoftPink.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "$takenMedsCount / $totalActiveMeds مكتمل",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (takenMedsCount == totalActiveMeds) SoftTheme.MintTeal else SoftTheme.SoftPink,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Expand / Collapse Chevron Button
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(32.dp).testTag("vitamins_expand_toggle_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "طي البطاقة" else "توسيع البطاقة",
                            tint = SoftTheme.SoftPink,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Compact Progress Bar when Collapsed
            if (!isExpanded && totalActiveMeds > 0) {
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = SoftTheme.MintTeal,
                    trackColor = SoftTheme.DeepSlate,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }

            // Expanded Content
            AnimatedVisibility(visible = isExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (activeMedications.isEmpty()) {
                        // Empty State with Call To Action to Add Medicine
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("✨", fontSize = 24.sp)
                                Text(
                                    text = "لا توجد أدوية أو فيتامينات نشطة حالياً",
                                    color = SoftTheme.TextWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "أضيفي فيتامينات الحمل أو أدويتكِ اليومية لظهورها ومتابعتها هنا مباشرة 💊",
                                    color = SoftTheme.SoftGray,
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center,
                                    fontSize = 11.sp
                                )
                                Button(
                                    onClick = onNavigateToMeds,
                                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("إضافة فيتامين أو دواء 💊", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        // Dynamic Live Medications from Database
                        activeMedications.forEach { med ->
                            val isTaken = med.id in todayTakenMedIds
                            Surface(
                                onClick = {
                                    if (isTaken) {
                                        viewModel.recordMedicationAdherence(med.id, System.currentTimeMillis(), "MISSED")
                                    } else {
                                        viewModel.recordMedicationAdherence(med.id, System.currentTimeMillis(), "TAKEN")
                                        if (med.remainingQuantity > 0) {
                                            viewModel.decrementMedicationStock(med, 1)
                                        }
                                        android.widget.Toast.makeText(context, "صحة وعافية! تم تسجيل تناول ${med.name} 🌸", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isTaken) SoftTheme.MintTeal.copy(alpha = 0.15f) else SoftTheme.DeepSlate,
                                border = BorderStroke(1.dp, if (isTaken) SoftTheme.MintTeal else Color.Transparent),
                                modifier = Modifier.fillMaxWidth().testTag("med_item_row_${med.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = med.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (isTaken) SoftTheme.MintTeal else SoftTheme.TextWhite,
                                            fontWeight = if (isTaken) FontWeight.Bold else FontWeight.Medium
                                        )
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (!med.dosage.isNullOrBlank()) {
                                                Text(
                                                    text = "الجرعة: ${med.dosage}",
                                                    color = SoftTheme.SoftGray,
                                                    fontSize = 11.sp
                                                )
                                            }
                                            Text(
                                                text = "${med.timesPerDay} مرات يومياً",
                                                color = SoftTheme.SoftGray,
                                                fontSize = 11.sp
                                            )
                                            if (med.remainingQuantity > 0) {
                                                Text(
                                                    text = "• متبقي: ${med.remainingQuantity}",
                                                    color = if (med.remainingQuantity <= 5) SoftTheme.RedDanger else SoftTheme.SoftGray,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (med.remainingQuantity <= 5) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                        }
                                    }

                                    Checkbox(
                                        checked = isTaken,
                                        onCheckedChange = { checked ->
                                            if (checked) {
                                                viewModel.recordMedicationAdherence(med.id, System.currentTimeMillis(), "TAKEN")
                                                if (med.remainingQuantity > 0) {
                                                    viewModel.decrementMedicationStock(med, 1)
                                                }
                                                android.widget.Toast.makeText(context, "صحة وعافية! تم تسجيل تناول ${med.name} 🌸", android.widget.Toast.LENGTH_SHORT).show()
                                            } else {
                                                viewModel.recordMedicationAdherence(med.id, System.currentTimeMillis(), "MISSED")
                                            }
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = SoftTheme.MintTeal,
                                            uncheckedColor = SoftTheme.SoftGray,
                                            checkmarkColor = Color.White
                                        )
                                    )
                                }
                            }
                        }

                        // Conflict Warning Banner between Iron and Calcium (Dynamically detected from user's actual meds)
                        if (hasIronMed && hasCalciumMed) {
                            com.example.ui.meds.DrugNutrientConflictBanner(
                                isIronTakenOrScheduled = true,
                                isCalciumTakenOrScheduled = true
                            )
                        }

                        // Footer Action: Go to Full Medication Manager
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = onNavigateToMeds,
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                modifier = Modifier.testTag("vitamins_manage_all_btn")
                            ) {
                                Text("إدارة ومواعيد الأدوية الكاملة ⚙️ ↗", color = SoftTheme.SoftPink, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            TextButton(
                                onClick = { isExpanded = false },
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("طي البطاقة ⌃", color = SoftTheme.SoftGray, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BloodPressureDialog(
    onDismiss: () -> Unit,
    onSave: (systolic: Int, diastolic: Int, pulse: Int?, notes: String?) -> Unit,
    isLowBp: Boolean = false,
    availableMedications: List<String> = emptyList()
) {
    com.example.ui.symptoms.AddBloodPressureDialog(
        onDismiss = onDismiss,
        onSave = onSave,
        isLowBp = isLowBp,
        availableMedications = availableMedications
    )
}
