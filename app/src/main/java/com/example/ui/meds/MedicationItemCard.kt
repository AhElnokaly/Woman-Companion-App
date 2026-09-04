package com.example.ui.meds

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MedicationLog
import com.example.ui.SoftTheme
import com.example.ui.formatGregorianDate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MedicationItemCard(
    med: MedicationLog,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleActive: (Boolean) -> Unit,
    onTakeDose: () -> Unit,
    onSnoozeDose: () -> Unit,
    onViewHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isStockLow = com.example.reminder.isMedicationStockLow(med.remainingQuantity)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp,
            if (med.isActive) SoftTheme.SoftPink.copy(alpha = 0.35f)
            else SoftTheme.SoftGray.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Med Name, Dosage Badge, Active Switch, Edit, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = med.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (med.isActive) SoftTheme.TextWhite else SoftTheme.SoftGray
                    )
                    med.dosage?.let { dos ->
                        if (dos.isNotBlank()) {
                            Text(
                                text = "💊 الجرعة: $dos",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SoftTheme.SoftPink
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Switch(
                        checked = med.isActive,
                        onCheckedChange = onToggleActive,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SoftTheme.SoftPink,
                            checkedTrackColor = SoftTheme.SoftPink.copy(alpha = 0.3f),
                            uncheckedThumbColor = SoftTheme.SoftGray,
                            uncheckedTrackColor = SoftTheme.DeepSlate
                        )
                    )
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "تعديل",
                            tint = SoftTheme.MintTeal.copy(alpha = 0.9f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "حذف الدواء",
                            tint = SoftTheme.RedDanger.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Meta tags: Timing / Meal relation / Stock / Doctor
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Safety warning / Meal relation tag
                med.safetyWarning?.let { warn ->
                    if (warn.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SoftTheme.SoftPink.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = warn,
                                color = SoftTheme.SoftPink,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                // Prescribed doctor tag
                med.prescribedBy?.let { doc ->
                    if (doc.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SoftTheme.MintTeal.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "🩺 د. $doc",
                                color = SoftTheme.MintTeal,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                // Stock indicator tag
                if (med.remainingQuantity > 0 || med.totalQuantity > 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isStockLow) SoftTheme.RedDanger.copy(alpha = 0.2f) else SoftTheme.DeepSlate
                    ) {
                        Text(
                            text = if (isStockLow) "⚠️ متبقي ${med.remainingQuantity} فقط (شارفي على الشراء)"
                            else "📦 المتبقي: ${med.remainingQuantity}",
                            color = if (isStockLow) SoftTheme.RedDanger else SoftTheme.TextWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Schedule & Notes Details
            if (!med.notes.isNullOrEmpty()) {
                Surface(
                    color = SoftTheme.DeepSlate,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🗓️ ${med.notes}",
                        fontSize = 11.sp,
                        color = SoftTheme.TextWhite.copy(alpha = 0.9f),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            // Interactive Actions Bar (Take Dose / Snooze / History)
            if (med.isActive) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onViewHistory,
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text("📋 سجل الجرعات", fontSize = 11.sp, color = SoftTheme.SoftPink)
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onSnoozeDose,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftTheme.SoftGray),
                            border = BorderStroke(1.dp, SoftTheme.SoftGray.copy(alpha = 0.4f)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("تأجيل ١٥ د ⏳", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = onTakeDose,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = SoftTheme.MintTeal
                            ),
                            border = BorderStroke(1.dp, SoftTheme.MintTeal.copy(alpha = 0.6f)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("أخذت الجرعة ✓", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
