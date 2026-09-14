package com.example.ui.pregnancy

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Appointment
import com.example.ui.formatGregorianDate
import com.example.ui.SoftTheme

@Composable
fun DailyVitalsSummaryCard(
    consumedWater: Int,
    waterGoal: Int,
    currentSteps: Int,
    stepGoal: Int,
    upcomingAppt: Appointment?,
    onNavigateToTab: (Int) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("dashboard_card_expansions", Context.MODE_PRIVATE) }
    var isExpanded by remember { mutableStateOf(prefs.getBoolean("vitals_expanded", true)) }

    val waterPct = if (waterGoal > 0) (consumedWater.toFloat() / waterGoal.toFloat()).coerceIn(0f, 1f) else 0f
    val stepsPct = if (stepGoal > 0) (currentSteps.toFloat() / stepGoal.toFloat()).coerceIn(0f, 1f) else 0f

    val arrowRotation by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "vitalsArrow")

    Card(
        modifier = Modifier.fillMaxWidth().testTag("daily_vitals_summary_card"),
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, SoftTheme.CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val next = !isExpanded
                        isExpanded = next
                        prefs.edit().putBoolean("vitals_expanded", next).apply()
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "مؤشراتكِ الحيوية اليوم 🎯📈",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.TextWhite
                    )
                    if (!isExpanded) {
                        Text(
                            text = "• ${(waterPct * 100).toInt()}% ترطيب",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTheme.MintTeal,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TextButton(
                        onClick = { onNavigateToTab(2) },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("الترطيب 🥛 ↗", color = SoftTheme.MintTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    IconButton(
                        onClick = {
                            val next = !isExpanded
                            isExpanded = next
                            prefs.edit().putBoolean("vitals_expanded", next).apply()
                        },
                        modifier = Modifier.size(32.dp).testTag("vitals_expand_toggle_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "طي" else "توسيع",
                            tint = SoftTheme.SoftPink,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Water Progress Bar (clickable -> tab 2)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToTab(2) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.WaterDrop, contentDescription = null, tint = SoftTheme.MintTeal, modifier = Modifier.size(16.dp))
                                Text("شرب الماء والترطيب 🥛", style = MaterialTheme.typography.bodyMedium, color = SoftTheme.TextWhite)
                            }
                            Text("$consumedWater / $waterGoal مل ↗", style = MaterialTheme.typography.bodySmall, color = SoftTheme.MintTeal, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(SoftTheme.DeepSlate)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(waterPct)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SoftTheme.MintTeal)
                            )
                        }
                    }

                    // Steps Progress Bar (clickable -> tab 1)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToTab(1) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.AutoMirrored.Filled.DirectionsRun, contentDescription = null, tint = SoftTheme.SoftPink, modifier = Modifier.size(16.dp))
                                Text("خطوات النشاط اليومي 👣", style = MaterialTheme.typography.bodyMedium, color = SoftTheme.TextWhite)
                            }
                            Text("$currentSteps / $stepGoal خطوة ↗", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftPink, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(SoftTheme.DeepSlate)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(stepsPct)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SoftTheme.SoftPink)
                            )
                        }
                    }

                    // Upcoming appointment or message (clickable -> tab 3)
                    if (upcomingAppt != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SoftTheme.DeepSlate)
                                .border(1.dp, SoftTheme.CardBorder, RoundedCornerShape(12.dp))
                                .clickable { onNavigateToTab(3) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = SoftTheme.SoftPink, modifier = Modifier.size(18.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("موعدكِ الطبي القادم 🏥", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftPink, fontWeight = FontWeight.Bold)
                                Text("${upcomingAppt.title} - ${formatGregorianDate(upcomingAppt.dateTime)}", style = MaterialTheme.typography.bodySmall, color = SoftTheme.TextWhite)
                            }
                            Text("تفاصيل ↗", fontSize = 11.sp, color = SoftTheme.SoftPink, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SoftTheme.DeepSlate)
                                .border(1.dp, SoftTheme.CardBorder, RoundedCornerShape(12.dp))
                                .clickable { onNavigateToTab(3) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = SoftTheme.MintTeal, modifier = Modifier.size(16.dp))
                            Text("لا توجد مواعيد مسجلة. اضغطي لإضافة موعد طبي 🏥 ↗", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                        }
                    }
                }
            }
        }
    }
}

