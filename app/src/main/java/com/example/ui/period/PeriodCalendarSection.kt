package com.example.ui.period

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PeriodLog
import com.example.data.PregnancyEntity
import com.example.ui.theme.SoftTheme
import java.util.*

@Composable
fun PeriodCalendarSection(
    periodLogs: List<PeriodLog>,
    isPregnant: PregnancyEntity?,
    allPregnancies: List<PregnancyEntity>,
    nifasDurationDays: Int,
    predictedPeriods: List<Pair<Long, Long>>,
    predictedOvulations: List<Pair<Long, Long>>,
    onDaySelected: (dayTime: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var isCalendarExpanded by remember { mutableStateOf(false) }
    val currentMonthCalendar = remember { Calendar.getInstance() }
    var monthUpdateTrigger by remember { mutableIntStateOf(0) }
    val isCurrentlyPregnant = isPregnant?.isPregnant == true

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Calendar Header Row (Collapsible)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isCalendarExpanded = !isCalendarExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🗓️", fontSize = 24.sp)
                    Column {
                        Text(
                            text = "التقويم التفاعلي الذكي",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.TextWhite
                        )
                        Text(
                            text = "تتبع وتنبؤ دقيق بالدورة وأيام الخصوبة",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTheme.SoftGray
                        )
                    }
                }
                Text(
                    text = if (isCalendarExpanded) "طي 🔼" else "عرض التقويم 🔽",
                    color = SoftTheme.SoftPink,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }

            AnimatedVisibility(visible = isCalendarExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Month selector row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            currentMonthCalendar.add(Calendar.MONTH, -1)
                            monthUpdateTrigger++
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "السابق", tint = SoftTheme.SoftPink)
                        }

                        val monthNames = listOf("يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو", "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر")
                        Text(
                            text = "${monthNames[currentMonthCalendar.get(Calendar.MONTH)]} ${currentMonthCalendar.get(Calendar.YEAR)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.TextWhite
                        )

                        IconButton(onClick = {
                            currentMonthCalendar.add(Calendar.MONTH, 1)
                            monthUpdateTrigger++
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "التالي", tint = SoftTheme.SoftPink)
                        }
                    }

                    // Days of week row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val weekdays = listOf("أحد", "اثنين", "ثلاثاء", "أربعاء", "خميس", "جمعة", "سبت")
                        weekdays.forEach { day ->
                            Text(
                                text = day,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftTheme.SoftGray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Days grid
                    val dummyDays = remember(monthUpdateTrigger) {
                        val list = mutableListOf<Long?>()
                        val tempCal = currentMonthCalendar.clone() as Calendar
                        tempCal.set(Calendar.DAY_OF_MONTH, 1)
                        val firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK) // 1 = Sunday

                        repeat(firstDayOfWeek - 1) {
                            list.add(null)
                        }

                        val maxDays = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)
                        repeat(maxDays) { idx ->
                            tempCal.set(Calendar.DAY_OF_MONTH, idx + 1)
                            list.add(tempCal.timeInMillis)
                        }
                        list
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val chunked = dummyDays.chunked(7)
                        chunked.forEach { rowDays ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                rowDays.forEach { dayTime ->
                                    if (dayTime == null) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    } else {
                                        val dayCal = Calendar.getInstance().apply { timeInMillis = dayTime }
                                        val dayNum = dayCal.get(Calendar.DAY_OF_MONTH)

                                        // Determine highlighting (actual period logs)
                                        val isPeriod = periodLogs.any { log ->
                                            val end = log.endDate ?: (log.startDate + 5L * 24 * 60 * 60 * 1000)
                                            dayTime >= log.startDate && dayTime <= end
                                        }

                                        // Smart predictions indicators
                                        val isPredictedPeriod = !isCurrentlyPregnant && !isPeriod && predictedPeriods.any { (start, end) ->
                                            dayTime >= start && dayTime <= end
                                        }
                                        val isPregnancyDay = allPregnancies.any { preg ->
                                            val start = preg.lastPeriodDate
                                            val end = if (preg.isDelivered && preg.birthDate != null) {
                                                preg.birthDate
                                            } else {
                                                preg.dueDate ?: (start?.let { it + 280L * 24 * 60 * 60 * 1000 })
                                            }
                                            if (start != null && end != null) {
                                                val startCal = Calendar.getInstance().apply { timeInMillis = start; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
                                                val endCal = Calendar.getInstance().apply { timeInMillis = end; set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999) }
                                                dayTime >= startCal.timeInMillis && dayTime <= endCal.timeInMillis
                                            } else false
                                        }

                                        val isNifasDay = !isPregnancyDay && allPregnancies.any { preg ->
                                            if (preg.isDelivered && preg.birthDate != null) {
                                                val nifasStartCal = Calendar.getInstance().apply { timeInMillis = preg.birthDate; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
                                                val nifasEndCal = Calendar.getInstance().apply { timeInMillis = preg.birthDate + (nifasDurationDays * 24L * 60 * 60 * 1000); set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999) }
                                                dayTime >= nifasStartCal.timeInMillis && dayTime <= nifasEndCal.timeInMillis
                                            } else false
                                        }

                                        val isPredictedOvulation = !isCurrentlyPregnant && !isPeriod && !isPredictedPeriod && predictedOvulations.any { (start, end) ->
                                            dayTime >= start && dayTime <= end
                                        }

                                        val todayCal = Calendar.getInstance()
                                        val isCurrent = todayCal.get(Calendar.YEAR) == dayCal.get(Calendar.YEAR) &&
                                                todayCal.get(Calendar.DAY_OF_YEAR) == dayCal.get(Calendar.DAY_OF_YEAR)

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .clip(CircleShape)
                                                .background(
                                                    when {
                                                        isPregnancyDay -> SoftTheme.PregnancyPurple
                                                        isNifasDay -> SoftTheme.NifasRose
                                                        isPeriod -> SoftTheme.DeepPink
                                                        isPredictedPeriod -> SoftTheme.DeepPink.copy(alpha = 0.25f)
                                                        isPredictedOvulation -> SoftTheme.MintTeal.copy(alpha = 0.25f)
                                                        isCurrent -> SoftTheme.MintTeal
                                                        else -> Color.Transparent
                                                    }
                                                )
                                                .border(
                                                    width = if (isCurrent && !isPeriod && !isPregnancyDay && !isNifasDay) 2.dp else if (isPredictedPeriod || isPredictedOvulation) 1.dp else 0.dp,
                                                    color = if (isCurrent && !isPeriod && !isPregnancyDay && !isNifasDay) SoftTheme.MintTeal else if (isPredictedPeriod) SoftTheme.SoftPink else if (isPredictedOvulation) SoftTheme.MintTeal else Color.Transparent,
                                                    shape = CircleShape
                                                )
                                                .clickable {
                                                    onDaySelected(dayTime)
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "$dayNum",
                                                color = when {
                                                    isPregnancyDay || isNifasDay -> Color.White
                                                    isPeriod -> SoftTheme.DeepSlate
                                                    isPredictedPeriod -> SoftTheme.SoftPink
                                                    isPredictedOvulation -> SoftTheme.MintTeal
                                                    else -> SoftTheme.TextWhite
                                                },
                                                fontWeight = if (isCurrent || isPeriod || isPregnancyDay || isNifasDay || isPredictedPeriod || isPredictedOvulation) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }
                                if (rowDays.size < 7) {
                                    repeat(7 - rowDays.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Color Legend for the Smart Calendar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SoftTheme.DeepPink))
                            Text("طمث", color = SoftTheme.SoftGray, fontSize = 9.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SoftTheme.PregnancyPurple))
                            Text("حمل 🤰", color = SoftTheme.SoftGray, fontSize = 9.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SoftTheme.NifasRose))
                            Text("نفاس 👶", color = SoftTheme.SoftGray, fontSize = 9.sp)
                        }
                        if (!isCurrentlyPregnant) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SoftTheme.DeepPink.copy(alpha = 0.25f)))
                                Text("دورة متوقعة", color = SoftTheme.SoftGray, fontSize = 9.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SoftTheme.MintTeal.copy(alpha = 0.25f)))
                                Text("إباضة متوقعة", color = SoftTheme.SoftGray, fontSize = 9.sp)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SoftTheme.MintTeal))
                            Text("اليوم", color = SoftTheme.SoftGray, fontSize = 9.sp)
                        }
                    }
                }
            }
        }
    }
}
