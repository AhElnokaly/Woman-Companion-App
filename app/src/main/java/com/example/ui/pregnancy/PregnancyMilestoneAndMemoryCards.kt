package com.example.ui.pregnancy

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FetalGrowthLog
import com.example.data.PregnancyEntity
import com.example.ui.SoftTheme
import java.util.Calendar

@Composable
fun MilestoneCelebrationCard(
    weeks: Int,
    onDismiss: () -> Unit
) {
    val (title, body, icon) = when {
        weeks >= 37 -> Triple("إنجاز الأسبوع 37 👶🎁", "مبروك الوصول للأسبوع 37! الجنين الآن مكتمل النمو وجاهز للقاء بمشيئة الله.", "👶")
        weeks >= 28 -> Triple("إنجاز الأسبوع 28 🌸", "مبروك بداية الثلث الثالث والأخير! خطوة جديدة تقربك أكثر من ضم طفلك.", "🌸")
        weeks >= 24 -> Triple("إنجاز الأسبوع 24 👶✨", "مبروك الأسبوع 24! مرحلة حيوية رائعة واستجابة الجنين في تزايد مستمر.", "✨")
        weeks >= 20 -> Triple("منتصف الرحلة! الأسبوع 20 🍌🎉", "ألف مبروك الوصول للأسبوع 20! قطعتم نصف الرحلة المباركة بحفظ الله.", "🎉")
        weeks >= 12 -> Triple("إنجاز الأسبوع 12 🌿", "مبروك تجاوز الأسبوع 12! انتهى الثلث الأول بنجاح وتبدأ مرحلة أكثر استقراراً.", "🌿")
        else -> Triple("", "", "")
    }

    if (title.isNotEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = SoftTheme.CardBg
            ),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(
                1.dp,
                SoftTheme.EmeraldPrimary.copy(alpha = 0.35f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(icon, fontSize = 24.sp)
                        Text(
                            title,
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.EmeraldPrimary,
                            fontSize = 16.sp
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = SoftTheme.TextSecondaryMuted)
                    }
                }
                Text(body, style = MaterialTheme.typography.bodyMedium, color = SoftTheme.TextPrimary)
            }
        }
    }
}

@Composable
fun PastPregnancyMemoryCard(
    allPregnancies: List<PregnancyEntity>,
    allFetalGrowthLogs: List<FetalGrowthLog>,
    onDismiss: () -> Unit,
    onNavigateToHistory: () -> Unit = {}
) {
    val memoryInfo = remember(allPregnancies, allFetalGrowthLogs) {
        val nowMs = System.currentTimeMillis()
        val calNow = Calendar.getInstance().apply { timeInMillis = nowMs }

        allPregnancies.filter { !it.isActive && it.lastPeriodDate != null }.mapNotNull { pastPreg ->
            val lmp = pastPreg.lastPeriodDate ?: return@mapNotNull null
            val calLmp = Calendar.getInstance().apply { timeInMillis = lmp }
            val yearDiff = (calNow.get(Calendar.YEAR) - calLmp.get(Calendar.YEAR)).coerceAtLeast(1)
            val projectedLmp = Calendar.getInstance().apply {
                timeInMillis = lmp
                add(Calendar.YEAR, yearDiff)
            }.timeInMillis

            val diffMs = nowMs - projectedLmp
            val diffDays = (diffMs / (24 * 60 * 60 * 1000L)).toInt()
            if (diffDays in 0..280) {
                val pastWeek = (diffDays / 7) + 1
                val matchedLog = allFetalGrowthLogs.firstOrNull { it.pregnancyId == pastPreg.id && it.pregnancyWeek == pastWeek }
                Triple(pastPreg, pastWeek, matchedLog)
            } else null
        }.firstOrNull()
    }

    memoryInfo?.let { (pastPreg, pastWeek, matchedLog) ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .testTag("past_pregnancy_memory_card"),
            colors = CardDefaults.cardColors(
                containerColor = SoftTheme.CardBg
            ),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(
                1.dp,
                SoftTheme.CardBorder
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🌸", fontSize = 22.sp)
                        Text(
                            "من فترة كانت هنا 🌸",
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.EmeraldPrimary,
                            fontSize = 16.sp
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = SoftTheme.TextSecondaryMuted)
                    }
                }
                val babyText = if (!pastPreg.babyName.isNullOrBlank()) " بـ (${pastPreg.babyName})" else ""
                Text(
                    "في مثل هذا الوقت من السنة، كنتِ في الأسبوع $pastWeek من حملكِ السابق$babyText.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SoftTheme.TextPrimary
                )
                if (matchedLog != null) {
                    Text(
                        "آخر قياس مسجّل لهذا الأسبوع: الوزن ${matchedLog.weightGrams} جرام، الطول ${matchedLog.lengthCm} سم.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTheme.EmeraldPrimary
                    )
                }

                Button(
                    onClick = onNavigateToHistory,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SoftTheme.CanvasBg
                    ),
                    border = BorderStroke(1.dp, SoftTheme.CardBorder),
                    modifier = Modifier.fillMaxWidth().testTag("goto_past_pregnancy_history_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("عرض سجل وتفاصيل الأحمال السابقة 📜 ↗", color = SoftTheme.EmeraldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
