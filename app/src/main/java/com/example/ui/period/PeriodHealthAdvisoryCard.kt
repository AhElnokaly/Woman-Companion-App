package com.example.ui.period

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PeriodLog
import com.example.viewmodel.CycleStats
import com.example.ui.theme.SoftTheme
import com.example.util.formatArabicDays

@Composable
fun PeriodHealthAdvisoryCard(
    periodLogs: List<PeriodLog>,
    stats: CycleStats,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val avgCycle = stats.averageCycleLength

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = SoftTheme.SoftPink
                    )
                    Text(
                        text = "التحليل الصحي الذكي والتوصيات 🧠",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.TextWhite
                    )
                }
                Text(
                    text = if (isExpanded) "إخفاء 🔼" else "تحليل كامل 🔽",
                    color = SoftTheme.SoftPink,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                if (periodLogs.isEmpty()) {
                    Text(
                        text = "قومي بتسجيل دورتكِ الشهرية الأولى (أو دوراتك السابقة) لنتمكن من تقديم نصائح تغذية وراحة مخصصة ذكياً.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoftTheme.SoftGray
                    )
                } else {
                    val latestLog = periodLogs.maxByOrNull { it.startDate }
                    val pain = latestLog?.painLevel ?: 5
                    val intensity = latestLog?.flowIntensity ?: "medium"
                    val symptoms = latestLog?.symptoms?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "بناءً على سجلات دورتكِ وأعراضك الأخيرة، إليكِ تقريرنا الطبي التوجيهي الذكي:",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTheme.SoftPink
                        )

                        HorizontalDivider(color = SoftTheme.DeepSlate)

                        // Pain advisory
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("🌱", fontSize = 18.sp)
                            Column {
                                Text("مستوى الألم والتقلصات:", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = when {
                                        pain >= 8 -> "ألم شديد ($pain/10). نوصي بالراحة التامة، استخدام كمادات دافئة على البطن، وتناول المغنيسيوم. إذا استمر الألم الشديد يرجى استشارة الطبيبة."
                                        pain >= 5 -> "ألم متوسط ($pain/10). كوب من القرفة أو اليانسون الدافئ قد يساعد في تخفيف الانقباضات بشكل رائع."
                                        else -> "ألم خفيف ($pain/10). مستوى ممتاز ومؤشر على توازن هرموني رائع."
                                    },
                                    color = SoftTheme.SoftGray,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        // Flow advisory
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("🩸", fontSize = 18.sp)
                            Column {
                                Text("غزارة الطمث ونقص الحديد:", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = when (intensity) {
                                        "heavy" -> "الطمث غزير. من الضروري جداً زيادة تناول الأطعمة الغنية بالحديد (مثل السبانخ واللحم الأحمر) أو فيتامين سي لتعويض الفقد وتجنب فقر الدم."
                                        "light" -> "الطمث خفيف. طبيعي جداً، استمري في شرب الماء والترطيب."
                                        else -> "الطمث متوسط ومثالي. كمية تدفق صحية تدل على بطانة رحم سليمة."
                                    },
                                    color = SoftTheme.SoftGray,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        // Symptoms advisory
                        if (symptoms.isNotEmpty()) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("🧠", fontSize = 18.sp)
                                Column {
                                    Text("التعامل مع الأعراض المرافقة:", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite, style = MaterialTheme.typography.bodyMedium)
                                    val advice = symptoms.map { sym ->
                                        when (sym) {
                                            "مغص" -> "للـ مغص: تدليك أسفل الظهر بزيت اللافندر الدافئ."
                                            "إرهاق" -> "للـ إرهاق: النوم لـ 8 ساعات وتجنب السهر والإجهاد البدني."
                                            "صداع" -> "للـ صداع: الابتعاد عن الشاشات والترطيب المستمر بشرب الماء."
                                            "تقلب مزاجي" -> "للـ تقلب المزاجي: ممارسة تمارين تنفس واسترخاء خفيفة لزيادة هرمونات السعادة."
                                            "ألم ظهر" -> "للـ ألم الظهر: الحفاظ على وضعية جلوس مستقيمة وتجنب حمل الأثقال."
                                            else -> "الراحة والترطيب الدائم."
                                        }
                                    }.joinToString("\n")
                                    Text(
                                        text = advice,
                                        color = SoftTheme.SoftGray,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }

                        // Smart pregnancy prediction advice
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("✨", fontSize = 18.sp)
                            Column {
                                Text("التنبؤ الذكي بالخصوبة القادمة:", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = "بناءً على طول دورتك المعتاد (${formatArabicDays(avgCycle)})، فإن فرصة الحمل العالية وتاريخ الإباضة القادم سيكون تقريباً في اليوم 14 من بداية دورتك القادمة. يمكنكِ التخطيط لذلكِ بسهولة بالنظر إلى النقط الخضراء في التقويم أدناه.",
                                    color = SoftTheme.SoftGray,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            } else {
                // Mini summary preview when collapsed
                Text(
                    text = if (periodLogs.isEmpty()) {
                        "قومي بتسجيل دورتكِ الشهرية الأولى لبدء التحليل الصحي التلقائي."
                    } else {
                        val latestPain = periodLogs.maxByOrNull { it.startDate }?.painLevel ?: 5
                        if (latestPain >= 7) {
                            "مستويات الألم الأخيرة مرتفعة نسبياً (${latestPain}/10). انقري لعرض التوصيات الصحية والغذائية المخصصة لراحة جسدك."
                        } else {
                            "تحليل: دورتكِ منتظمة بمتوسط ${formatArabicDays(avgCycle)} وصحتك تبدو متوازنة. انقري لعرض التفاصيل الكاملة."
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = SoftTheme.SoftGray
                )
            }
        }
    }
}
