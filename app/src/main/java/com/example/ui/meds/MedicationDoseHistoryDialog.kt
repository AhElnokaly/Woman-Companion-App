package com.example.ui.meds

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.MedicationAdherenceLog
import com.example.data.MedicationLog
import com.example.ui.SoftTheme
import com.example.ui.formatGregorianDate

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
