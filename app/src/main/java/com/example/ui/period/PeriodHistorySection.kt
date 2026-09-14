package com.example.ui.period

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PeriodLog
import com.example.ui.formatGregorianDate
import com.example.ui.SoftTheme

@Composable
fun PeriodHistorySection(
    periodLogs: List<PeriodLog>,
    onDeleteLog: (PeriodLog) -> Unit,
    modifier: Modifier = Modifier
) {
    var isHistoryExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isHistoryExpanded = !isHistoryExpanded },
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.15f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("📖", fontSize = 24.sp)
                    Column {
                        Text(
                            text = "السجل التاريخي للدورات",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.TextWhite
                        )
                        Text(
                            text = "عرض وتعديل الدورات والتقلصات السابقة",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTheme.SoftGray
                        )
                    }
                }
                Text(
                    text = if (isHistoryExpanded) "إخفاء 🔼" else "عرض السجلات 🔽",
                    color = SoftTheme.SoftPink,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        AnimatedVisibility(visible = isHistoryExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (periodLogs.isEmpty()) {
                    Text(
                        text = "لا توجد دورات مسجلة بعد. قومي بإضافة دورتك لبدء الحساب التلقائي والتحليل.",
                        color = SoftTheme.SoftGray,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    )
                } else {
                    periodLogs.forEach { log ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate.copy(alpha = 0.7f)),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(0.5.dp, SoftTheme.SoftPink.copy(alpha = 0.1f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "البداية: ${formatGregorianDate(log.startDate)}",
                                            fontWeight = FontWeight.Bold,
                                            color = SoftTheme.TextWhite
                                        )
                                        log.endDate?.let {
                                            Text(
                                                text = "النهاية: ${formatGregorianDate(it)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = SoftTheme.SoftGray
                                            )
                                        }
                                    }

                                    IconButton(onClick = { onDeleteLog(log) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = SoftTheme.RedDanger)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(SoftTheme.DeepSlate)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "الشدة: ${
                                                when (log.flowIntensity) {
                                                    "heavy" -> "غزيرة"
                                                    "medium" -> "متوسطة"
                                                    else -> "خفيفة"
                                                }
                                            }",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SoftTheme.SoftPink
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(SoftTheme.DeepSlate)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "الألم: ${log.painLevel}/10",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SoftTheme.MintTeal
                                        )
                                    }
                                    if (!log.notes.isNullOrBlank()) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(SoftTheme.DeepSlate)
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "ملاحظات 📝",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = SoftTheme.LightPink
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
}
