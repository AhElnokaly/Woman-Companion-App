package com.example.ui.pregnancy

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MedicationLog
import com.example.ui.SoftTheme
import com.example.viewmodel.WomanCompanionViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * شريط الجرعة القادمة الفوري أعلى الشاشة الرئيسية (Upcoming Dose Banner)
 * ينبه الحامل بدوائها القريب مع خيارات تسجيل فورية (أخذت الجرعة أو تأجيل)
 */
@Composable
fun UpcomingDoseBanner(
    viewModel: WomanCompanionViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activeMeds by viewModel.activeMedicationsState.collectAsState()
    val adherenceLogs by viewModel.allMedicationAdherenceLogsState.collectAsState()

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

    // Identify next untaken medication for today
    val nextMed = remember(activeMeds, todayTakenMedIds) {
        activeMeds.firstOrNull { it.id !in todayTakenMedIds }
    }

    var isDismissedForSession by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = nextMed != null && !isDismissedForSession,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        nextMed?.let { med ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("upcoming_dose_banner"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                border = BorderStroke(1.2.dp, SoftTheme.SoftPink.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SoftTheme.SoftPink.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("💊", fontSize = 18.sp)
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "تذكير بالجرعة القادمة",
                                    fontSize = 11.sp,
                                    color = SoftTheme.SoftPink,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "• اليوم",
                                    fontSize = 10.sp,
                                    color = SoftTheme.SoftGray
                                )
                            }
                            Text(
                                text = "${med.name} ${if (!med.dosage.isNullOrBlank()) "(${med.dosage})" else ""}",
                                color = SoftTheme.TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Mark as Taken button
                        Button(
                            onClick = {
                                viewModel.recordMedicationAdherence(med.id, System.currentTimeMillis(), "TAKEN")
                                if (med.remainingQuantity > 0) {
                                    viewModel.decrementMedicationStock(med, 1)
                                }
                                android.widget.Toast.makeText(context, "صحة وعافية! تم تسجيل تناول ${med.name} 🌸", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.MintTeal),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("أخذتُها 🌸", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        // Snooze / Dismiss for now
                        IconButton(
                            onClick = { isDismissedForSession = true },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "إخفاء مؤقت",
                                tint = SoftTheme.SoftGray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
