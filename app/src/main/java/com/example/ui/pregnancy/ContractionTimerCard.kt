package com.example.ui.pregnancy

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SoftTheme
import kotlinx.coroutines.delay
import java.util.Locale

data class ContractionRecord(
    val id: Long = System.currentTimeMillis(),
    val startTime: Long,
    val durationSeconds: Int,
    val intervalSeconds: Int? = null // Time since previous contraction start
)

@Composable
fun ContractionTimerCard(
    modifier: Modifier = Modifier
) {
    var isRecording by remember { mutableStateOf(false) }
    var currentContractionStart by remember { mutableStateOf<Long?>(null) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    var records by remember { mutableStateOf(listOf<ContractionRecord>()) }

    // Active Timer Loop
    LaunchedEffect(isRecording) {
        if (isRecording) {
            elapsedSeconds = 0
            while (isRecording) {
                delay(1000L)
                elapsedSeconds++
            }
        }
    }

    // 5-1-1 Rule Evaluation
    val isHospitalRecommended = remember(records) {
        if (records.size >= 4) {
            val last4 = records.takeLast(4)
            val avgDuration = last4.map { it.durationSeconds }.average()
            val validIntervals = last4.mapNotNull { it.intervalSeconds }
            val avgInterval = if (validIntervals.isNotEmpty()) validIntervals.average() else 0.0
            avgDuration in 45.0..90.0 && avgInterval in 240.0..360.0 // ~1 min duration, ~5 min apart
        } else {
            false
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("contraction_timer_card"),
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, if (isHospitalRecommended) SoftTheme.RedDanger else SoftTheme.SoftPink.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
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
                        Icons.Default.Timer,
                        contentDescription = "مؤقت انقباضات الولادة",
                        tint = SoftTheme.SoftPink,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "مؤقت انقباضات الولادة (الطلق) ⏱️",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.TextWhite
                    )
                }

                if (records.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            records = emptyList()
                            isRecording = false
                            currentContractionStart = null
                            elapsedSeconds = 0
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "مسح سجل الانقباضات",
                            tint = SoftTheme.SoftGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // 5-1-1 Rule Alert Banner
            if (isHospitalRecommended) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SoftTheme.RedDanger.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, SoftTheme.RedDanger)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("🏥", fontSize = 24.sp)
                        Column {
                            Text(
                                "تنبيه قاعدة (5-1-1) للولادة!",
                                fontWeight = FontWeight.Bold,
                                color = SoftTheme.RedDanger,
                                fontSize = 13.sp
                            )
                            Text(
                                "الانقباضات تحدث كل 5 دقائق وتستمر لدقيقة تقريباً. يوصى بالتواصل مع طبيبتكِ أو التوجه للمستشفى.",
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftTheme.TextWhite,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Main Interactive Timer Display
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(SoftTheme.DeepSlate)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (isRecording) "الانقباضة جارية حالياً..." else "اضغطي عند بدء الانقباضة",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isRecording) SoftTheme.SoftPink else SoftTheme.SoftGray,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = formatDuration(elapsedSeconds),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isRecording) SoftTheme.SoftPink else SoftTheme.TextWhite,
                    letterSpacing = 2.sp
                )

                Button(
                    onClick = {
                        val now = System.currentTimeMillis()
                        if (isRecording) {
                            // Stop and record
                            val start = currentContractionStart ?: (now - elapsedSeconds * 1000L)
                            val prevStart = records.lastOrNull()?.startTime
                            val interval = if (prevStart != null) ((start - prevStart) / 1000L).toInt() else null
                            val newRecord = ContractionRecord(
                                startTime = start,
                                durationSeconds = elapsedSeconds,
                                intervalSeconds = interval
                            )
                            records = records + newRecord
                            isRecording = false
                            currentContractionStart = null
                            elapsedSeconds = 0
                        } else {
                            // Start
                            currentContractionStart = now
                            isRecording = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRecording) SoftTheme.RedDanger else SoftTheme.SoftPink
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("toggle_contraction_btn")
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = SoftTheme.TextWhite
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isRecording) "إيقاف الانقباضة (انتهت)" else "بدء تسجيل انقباضة جديدة ⏱️",
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.TextWhite,
                        fontSize = 13.sp
                    )
                }
            }

            // Recent Contractions List
            if (records.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "سجل الانقباضات الأخيرة (${records.size})",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.MintTeal
                    )

                    records.takeLast(4).reversed().forEachIndexed { index, record ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(SoftTheme.DeepSlate.copy(alpha = 0.6f))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "المدة: ${record.durationSeconds} ثانية",
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftTheme.TextWhite,
                                fontWeight = FontWeight.Bold
                            )
                            val intervalText = if (record.intervalSeconds != null) {
                                val min = record.intervalSeconds / 60
                                val sec = record.intervalSeconds % 60
                                "الفارق: ${min}د ${sec}ث"
                            } else "أول انقباضة"

                            Text(
                                text = intervalText,
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftTheme.SoftPink
                            )
                        }
                    }
                }
            }

            // Clinical Rule Note
            Text(
                text = "💡 قاعدة (5-1-1): انقباضات تتكرر كل 5 دقائق، تستمر كل منها لمدة دقيقة واحدة، وتتواصل على هذا النمط لمدة ساعة كاملة. استشيري طبيبتكِ دائماً عند الشك.",
                style = MaterialTheme.typography.bodySmall,
                color = SoftTheme.SoftGray,
                fontSize = 10.sp,
                lineHeight = 14.sp
            )
        }
    }
}

private fun formatDuration(totalSecs: Int): String {
    val minutes = totalSecs / 60
    val seconds = totalSecs % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}
