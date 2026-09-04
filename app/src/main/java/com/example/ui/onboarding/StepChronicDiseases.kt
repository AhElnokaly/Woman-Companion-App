package com.example.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.ui.GlassmorphicCard
import com.example.ui.SoftTheme

@Composable
fun StepChronicDiseases(
    hasHighBp: Boolean,
    onHighBpChange: (Boolean) -> Unit,
    hasLowBp: Boolean,
    onLowBpChange: (Boolean) -> Unit,
    hasDiabetes: Boolean,
    onDiabetesChange: (Boolean) -> Unit,
    chronicOthers: String,
    onChronicOthersChange: (String) -> Unit
) {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(SoftTheme.MintTeal.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🩺", fontSize = 22.sp)
                }
                Column {
                    Text(
                        text = "الوضع الصحي والوقائي",
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.TextWhite,
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "جوري ستقوم بتعديل المحتوى بناءً على الأمراض المزمنة",
                        color = SoftTheme.SoftGray,
                        fontSize = 11.sp
                    )
                }
            }

            Text(
                text = "عزيزتي، صحتكِ فوق كل شيء. تفاصيل السكر والضغط بالغة الأهمية لتستطيع جوري تنبيهكِ لما يفيدكِ وما قد يضركِ من أغذية وتمارين ووجبات ومشروبات ساخنة.",
                color = SoftTheme.SoftGray,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp
            )

            HorizontalDivider(color = Color(0xFFFFB300).copy(alpha = 0.25f), thickness = 1.dp)

            // High Blood Pressure
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (hasHighBp) Color(0xFFFFB300).copy(alpha = 0.1f) else SoftTheme.DeepSlate.copy(alpha = 0.3f))
                    .border(1.dp, if (hasHighBp) Color(0xFFFFB300).copy(alpha = 0.5f) else Color.Transparent, RoundedCornerShape(16.dp))
                    .clickable { onHighBpChange(!hasHighBp) }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Checkbox(
                    checked = hasHighBp,
                    onCheckedChange = onHighBpChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFFFFB300),
                        checkmarkColor = Color.Black
                    )
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ضغط دم مرتفع (عالي) 📈",
                        color = SoftTheme.TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "جوري ستنبهكِ للابتعاد التام عن الموالح والصوديوم وتحثكِ على الكركديه البارد.",
                        color = SoftTheme.SoftGray,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            // Low Blood Pressure
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (hasLowBp) Color(0xFFFFB300).copy(alpha = 0.1f) else SoftTheme.DeepSlate.copy(alpha = 0.3f))
                    .border(1.dp, if (hasLowBp) Color(0xFFFFB300).copy(alpha = 0.5f) else Color.Transparent, RoundedCornerShape(16.dp))
                    .clickable { onLowBpChange(!hasLowBp) }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Checkbox(
                    checked = hasLowBp,
                    onCheckedChange = onLowBpChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFFFFB300),
                        checkmarkColor = Color.Black
                    )
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ضغط دم منخفض (واطي) 📉",
                        color = SoftTheme.TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "جوري ستذكركِ بمشروبات مرطبة لزيادة ضغط الدم وتجنب الإرهاق والدوار المفاجئ.",
                        color = SoftTheme.SoftGray,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            // Diabetes
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (hasDiabetes) Color(0xFFFFB300).copy(alpha = 0.1f) else SoftTheme.DeepSlate.copy(alpha = 0.3f))
                    .border(1.dp, if (hasDiabetes) Color(0xFFFFB300).copy(alpha = 0.5f) else Color.Transparent, RoundedCornerShape(16.dp))
                    .clickable { onDiabetesChange(!hasDiabetes) }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Checkbox(
                    checked = hasDiabetes,
                    onCheckedChange = onDiabetesChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFFFFB300),
                        checkmarkColor = Color.Black
                    )
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "مرض السكري 🩸",
                        color = SoftTheme.TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "جوري ستقوم بمراقبة الحلويات والوجبات وحساب النشويات لتجنب طفرات الأنسولين.",
                        color = SoftTheme.SoftGray,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            // Chronic Others Input
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "هل لديكِ حالات صحية أو حساسية أخرى؟",
                    color = SoftTheme.TextWhite,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                OutlinedTextField(
                    value = chronicOthers,
                    onValueChange = onChronicOthersChange,
                    placeholder = { Text("مثال: أنيميا، حساسية لاكتوز، نقص فيتامين د...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_chronic_others"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SoftTheme.DeepSlate.copy(alpha = 0.5f),
                        unfocusedContainerColor = SoftTheme.DeepSlate.copy(alpha = 0.3f),
                        focusedBorderColor = Color(0xFFFFB300),
                        unfocusedBorderColor = SoftTheme.SoftGray.copy(alpha = 0.3f),
                        focusedTextColor = SoftTheme.TextWhite,
                        unfocusedTextColor = SoftTheme.TextWhite,
                        focusedPlaceholderColor = SoftTheme.SoftGray.copy(alpha = 0.7f),
                        unfocusedPlaceholderColor = SoftTheme.SoftGray.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(14.dp)
                )
            }
        }
    }
}
