package com.example.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PregnancyEntity
import com.example.ui.SoftTheme
import com.example.ui.formatGregorianDate

@Composable
fun ProfileCard(
    pregnancy: PregnancyEntity?,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("user_profile_card"),
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, SoftTheme.CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row: Avatar + Name + Edit Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(SoftTheme.PrimaryPink.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = SoftTheme.PrimaryPink,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column {
                        val displayName = pregnancy?.motherName?.takeIf { it.isNotBlank() } ?: "صديقة جوري"
                        val nickname = pregnancy?.nickname?.takeIf { it.isNotBlank() }
                        Text(
                            text = displayName,
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.TextWhite,
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 17.sp
                        )
                        if (nickname != null) {
                            Text(
                                text = "اسم الدلع: $nickname 💕",
                                color = SoftTheme.PrimaryPink,
                                fontSize = 12.sp
                            )
                        } else {
                            Text(
                                text = "الملف الشخصي والبيانات الصحية 🌸",
                                color = SoftTheme.SoftGray,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Button(
                    onClick = onEditClick,
                    modifier = Modifier.testTag("edit_profile_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SoftTheme.PrimaryPink.copy(alpha = 0.2f),
                        contentColor = SoftTheme.PrimaryPink
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                        Text("تعديل ✏️", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            HorizontalDivider(color = SoftTheme.CardBorder.copy(alpha = 0.5f), thickness = 1.dp)

            // Grid Details (Age, Height, Weight, BMI)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Age / BirthDate
                ProfileStatBadge(
                    label = "العمر",
                    value = pregnancy?.age?.let { "$it سنة" } ?: pregnancy?.birthDate?.let { formatGregorianDate(it) } ?: "غير محدد",
                    icon = "🎂",
                    modifier = Modifier.weight(1f)
                )

                // Height
                ProfileStatBadge(
                    label = "الطول",
                    value = pregnancy?.heightCm?.let { "${it.toInt()} سم" } ?: "غير محدد",
                    icon = "📏",
                    modifier = Modifier.weight(1f)
                )

                // Weight
                ProfileStatBadge(
                    label = "الوزن",
                    value = pregnancy?.prePregnancyWeight?.let { "${it.toInt()} كجم" } ?: "غير محدد",
                    icon = "⚖️",
                    modifier = Modifier.weight(1f)
                )

                // BMI Category
                val bmi = pregnancy?.bmiCategory ?: "طبيعي"
                val bmiColor = when (bmi) {
                    "Underweight" -> Color(0xFF2196F3)
                    "Normal" -> Color(0xFF4CAF50)
                    "Overweight" -> Color(0xFFFFB300)
                    else -> SoftTheme.RedDanger
                }
                ProfileStatBadge(
                    label = "الكتلة",
                    value = translateBmi(bmi),
                    icon = "🩺",
                    valueColor = bmiColor,
                    modifier = Modifier.weight(1f)
                )
            }

            // Health Conditions Chips (If any)
            val hasConditions = pregnancy?.hasHighBp == true || pregnancy?.hasLowBp == true || pregnancy?.hasDiabetes == true || !pregnancy?.chronicOthers.isNullOrBlank()
            if (hasConditions) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("الحالة الصحية:", fontSize = 11.sp, color = SoftTheme.SoftGray)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (pregnancy?.hasHighBp == true) {
                            ConditionChip("ضغط عالي 📈", Color(0xFFFFB300))
                        }
                        if (pregnancy?.hasLowBp == true) {
                            ConditionChip("ضغط واطي 📉", Color(0xFF00ACC1))
                        }
                        if (pregnancy?.hasDiabetes == true) {
                            ConditionChip("سكري 🩸", Color(0xFFE91E63))
                        }
                        if (!pregnancy?.chronicOthers.isNullOrBlank()) {
                            ConditionChip("ملاحظات أخرى 📝", SoftTheme.SoftGray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileStatBadge(
    label: String,
    value: String,
    icon: String,
    modifier: Modifier = Modifier,
    valueColor: Color = SoftTheme.TextWhite
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(SoftTheme.DeepSlate.copy(alpha = 0.5f))
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(icon, fontSize = 14.sp)
            Text(label, fontSize = 10.sp, color = SoftTheme.SoftGray)
            Text(
                text = value,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ConditionChip(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun translateBmi(category: String): String {
    return when (category) {
        "Underweight" -> "نحيف"
        "Normal" -> "مثالي"
        "Overweight" -> "زيادة"
        "Obese" -> "سمنة"
        else -> category
    }
}
