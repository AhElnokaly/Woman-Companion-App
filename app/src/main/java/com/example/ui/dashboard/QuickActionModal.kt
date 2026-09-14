package com.example.ui.dashboard

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.SoftTheme
import com.example.viewmodel.WomanCompanionViewModel

/**
 * نافذة التسجيل السريع عند الضغط على الزر المركزي (+)
 */
@Composable
fun QuickActionModal(
    viewModel: WomanCompanionViewModel,
    onDismiss: () -> Unit,
    onNavigateToPage: (Int) -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .clip(RoundedCornerShape(26.dp))
                .border(1.dp, SoftTheme.MintAccentBorder, RoundedCornerShape(26.dp))
                .testTag("quick_action_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
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
                        Text(text = "⚡", fontSize = 18.sp)
                        Text(
                            text = "تسجيل صحي سريع",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = SoftTheme.TextPrimary
                            )
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = SoftTheme.TextSecondaryMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action 1: Add Water Glass
                QuickActionRowItem(
                    icon = Icons.Outlined.WaterDrop,
                    iconBg = SoftTheme.WaterBg,
                    iconTint = SoftTheme.WaterBlue,
                    title = "إضافة كوب ماء",
                    subtitle = "تسجيل 250 مل فوري 💧",
                    onClick = {
                        viewModel.addWater(250)
                        Toast.makeText(context, "صحة وهنا! تم تسجيل كوب ماء بنجاح 💧", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Action 2: Add Nutrition / Meal
                QuickActionRowItem(
                    icon = Icons.Outlined.Restaurant,
                    iconBg = SoftTheme.FoodBg,
                    iconTint = SoftTheme.FoodOrange,
                    title = "تسجيل وجبة جديدة",
                    subtitle = "حساب السعرات والماكروز 🥑",
                    onClick = {
                        onDismiss()
                        onNavigateToPage(2)
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Action 3: Medication Dose
                QuickActionRowItem(
                    icon = Icons.Outlined.MedicalServices,
                    iconBg = SoftTheme.MintAccent,
                    iconTint = SoftTheme.EmeraldPrimary,
                    title = "تسجيل تناول الدواء",
                    subtitle = "تأكيد الجرعة وتحديث المتبقي 💊",
                    onClick = {
                        onDismiss()
                        onNavigateToPage(3)
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Action 4: Symptoms Log
                QuickActionRowItem(
                    icon = Icons.Outlined.FavoriteBorder,
                    iconBg = SoftTheme.SymptomsBg,
                    iconTint = SoftTheme.SymptomsPurple,
                    title = "تسجيل أعراض وملاحظات",
                    subtitle = "متابعة الحالة الصحية والنبض 🩺",
                    onClick = {
                        onDismiss()
                        onNavigateToPage(3)
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Action 5: Ask Jouri AI
                QuickActionRowItem(
                    icon = Icons.Outlined.ChatBubbleOutline,
                    iconBg = SoftTheme.JouriBg,
                    iconTint = SoftTheme.JouriTurquoise,
                    title = "محادثة فورية مع جوري AI",
                    subtitle = "استشارتكِ الصحية الذكية 💬",
                    onClick = {
                        onDismiss()
                        onNavigateToPage(4)
                    }
                )
            }
        }
    }
}

@Composable
private fun QuickActionRowItem(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = SoftTheme.DeepSlate.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(1.dp, SoftTheme.CardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = SoftTheme.TextPrimary
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = SoftTheme.TextSecondaryMuted
                    )
                )
            }
        }
    }
}
