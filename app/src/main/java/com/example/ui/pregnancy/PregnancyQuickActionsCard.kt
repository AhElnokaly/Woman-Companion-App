package com.example.ui.pregnancy

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
    onKickClick: () -> Unit,
    onSaveKick: () -> Unit,
    onCancelKick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("pregnancy_quick_actions_card"),
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "التسجيل الصحي السريع ⚡",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SoftTheme.TextWhite
            )

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
