package com.example.ui.period

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PeriodLog
import com.example.ui.SoftTheme
import java.util.concurrent.TimeUnit

/**
 * Extracts and sorts the most frequently reported symptoms from historical period logs.
 */
fun computeTopPmsSymptoms(periodLogs: List<PeriodLog>, limit: Int = 3): List<String> {
    val allSymptoms = periodLogs
        .flatMap { it.symptoms.split(",").map { s -> s.trim() } }
        .filter { it.isNotBlank() }

    return if (allSymptoms.isNotEmpty()) {
        allSymptoms.groupingBy { it }.eachCount().entries
            .sortedByDescending { it.value }
            .take(limit)
            .map { it.key }
    } else {
        listOf("تقلب مزاجي خفيف", "مغص خفيف", "رغبة بالراحة")
    }
}

@Composable
fun PmsInsightsCard(
    periodLogs: List<PeriodLog>,
    nextPeriodPredictedStart: Long?,
    modifier: Modifier = Modifier
) {
    val now = System.currentTimeMillis()
    val daysUntilNextPeriod = remember(nextPeriodPredictedStart, now) {
        if (nextPeriodPredictedStart != null && nextPeriodPredictedStart > now) {
            TimeUnit.MILLISECONDS.toDays(nextPeriodPredictedStart - now).toInt()
        } else {
            null
        }
    }

    // Only show if next period is approaching within 1 to 5 days
    if (daysUntilNextPeriod == null || daysUntilNextPeriod !in 1..5) {
        return
    }

    // Analyze most frequent historical symptoms and mood patterns
    val commonSymptoms = remember(periodLogs) {
        computeTopPmsSymptoms(periodLogs, limit = 3)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("pms_insights_card"),
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                        Icons.Default.Spa,
                        contentDescription = "رعاية ما قبل الدورة",
                        tint = SoftTheme.SoftPink,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "رعاية ما قبل الدورة (PMS) 🌸",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.TextWhite
                    )
                }

                Surface(
                    color = SoftTheme.SoftPink.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "متبقي $daysUntilNextPeriod أيام",
                        color = SoftTheme.SoftPink,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = "قد تشعرين ببعض التغيرات الهرمونية خلال هذه الأيام. بناءً على سجلاتك السابقة، أكثر الأعراض المعتادة لديكِ:",
                style = MaterialTheme.typography.bodySmall,
                color = SoftTheme.SoftGray,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )

            // Historical symptom tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                commonSymptoms.forEach { symptom ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SoftTheme.DeepSlate)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "✨ $symptom",
                            color = SoftTheme.MintTeal,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Comfort Tips Box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SoftTheme.DeepSlate.copy(alpha = 0.7f))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = SoftTheme.MintTeal,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "نصائح جوري لراحتكِ الآن:",
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.MintTeal,
                        fontSize = 11.sp
                    )
                }
                Text(
                    text = "• شرب مشروبات دافئة كالينسون أو البابونج.\n• تخفيف استهلاك الكافيين والأطعمة عالية الملوحة.\n• الحصول على قسط نوم كافٍ وممارسة تمدد خفيف.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SoftTheme.TextWhite,
                    fontSize = 10.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}
