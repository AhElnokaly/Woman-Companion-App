package com.example.ui.pregnancy

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SoftTheme

@Composable
fun PregnancyQuickActionsCard(
    isKickActive: Boolean,
    currentCount: Int,
    onAddWater: () -> Unit,
    onOpenBpDialog: () -> Unit,
    onOpenJournalDialog: () -> Unit,
    onOpenBreathingDialog: () -> Unit,
    onKickClick: () -> Unit,
    onSaveKick: () -> Unit,
    onCancelKick: () -> Unit
) {
    // Keep expanded if kick session is actively running, otherwise allow folding
    var isExpanded by remember { mutableStateOf(true) }
    LaunchedEffect(isKickActive) {
        if (isKickActive) isExpanded = true
    }

    val arrowRotation by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "arrowRotation")

    Card(
        modifier = Modifier.fillMaxWidth().testTag("pregnancy_quick_actions_card"),
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "التسجيل الصحي السريع ⚡",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.TextWhite
                    )
                    if (!isExpanded) {
                        Text(
                            text = if (isKickActive) "• جلسة ركلات نشطة ($currentCount)" else "• ماء، ضغط، تدوين",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isKickActive) SoftTheme.MintTeal else SoftTheme.SoftGray,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // SOS Breathing Quick Trigger
                    Surface(
                        onClick = onOpenBreathingDialog,
                        shape = RoundedCornerShape(12.dp),
                        color = SoftTheme.MintTeal.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SoftTheme.MintTeal.copy(alpha = 0.35f)),
                        modifier = Modifier.testTag("quick_breathing_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("🌸", fontSize = 12.sp)
                            Text(
                                text = "تنفسي",
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftTheme.MintTeal,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(32.dp).testTag("quick_actions_expand_toggle_btn")
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Water Quick Log Button
                        Button(
                            onClick = onAddWater,
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.DeepSlate),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f).height(64.dp).testTag("quick_water_log_btn")
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("🥛 +٢٥٠مل", fontWeight = FontWeight.Bold, color = SoftTheme.MintTeal, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("سجل ماء", style = MaterialTheme.typography.bodySmall, color = SoftTheme.TextWhite, fontSize = 10.sp)
                            }
                        }

                        // Blood Pressure Dialog Trigger
                        Button(
                            onClick = onOpenBpDialog,
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.DeepSlate),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f).height(64.dp).testTag("quick_bp_dialog_btn")
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("🩸 قياس", fontWeight = FontWeight.Bold, color = SoftTheme.SoftPink, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("ضغط الدم", style = MaterialTheme.typography.bodySmall, color = SoftTheme.TextWhite, fontSize = 10.sp)
                            }
                        }

                        // Journal/Diary Dialog Trigger
                        Button(
                            onClick = onOpenJournalDialog,
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.DeepSlate),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f).height(64.dp).testTag("quick_journal_dialog_btn")
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("📝 تدوين", fontWeight = FontWeight.Bold, color = SoftTheme.SoftPink, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("يومياتي", style = MaterialTheme.typography.bodySmall, color = SoftTheme.TextWhite, fontSize = 10.sp)
                            }
                        }

                        // Fetal Kicks Quick Log
                        Button(
                            onClick = onKickClick,
                            colors = ButtonDefaults.buttonColors(containerColor = if (isKickActive) SoftTheme.SoftPink else SoftTheme.DeepSlate),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1.2f).height(64.dp).testTag("quick_fetal_kick_btn")
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                if (isKickActive) {
                                    Text("🦶 ركلة! ($currentCount)", fontWeight = FontWeight.ExtraBold, color = SoftTheme.DeepSlate, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("احفظ", style = MaterialTheme.typography.bodySmall, color = SoftTheme.DeepSlate, fontSize = 9.sp, modifier = Modifier.clickable { onSaveKick() })
                                } else {
                                    Text("🤰 حركة", fontWeight = FontWeight.Bold, color = SoftTheme.MintTeal, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("ركلات الجنين", style = MaterialTheme.typography.bodySmall, color = SoftTheme.TextWhite, fontSize = 10.sp)
                                }
                            }
                        }
                    }

                    if (isKickActive) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("جلسة عد حركة الجنين نشطة حالياً ✨", color = SoftTheme.SoftPink, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "إلغاء ❌",
                                    color = SoftTheme.RedDanger,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable { onCancelKick() }
                                )
                                Text(
                                    text = "حفظ وحساب 🏁",
                                    color = SoftTheme.MintTeal,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable { onSaveKick() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
