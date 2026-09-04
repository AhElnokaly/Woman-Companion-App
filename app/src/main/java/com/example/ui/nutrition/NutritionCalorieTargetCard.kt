package com.example.ui.nutrition

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SoftTheme
import com.example.viewmodel.CalorieGoal

@Composable
fun NutritionCalorieTargetCard(
    totalCaloriesConsumed: Int,
    calorieGoal: CalorieGoal,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "🎯 هدف السعرات الحرارية اليومي",
                fontWeight = FontWeight.Bold,
                color = SoftTheme.TextWhite,
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("المستهلك اليوم", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                    Text("$totalCaloriesConsumed سعرة حرارية", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SoftTheme.MintTeal)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("الهدف المطلوب", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                    Text("${calorieGoal.target} سعرة", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SoftTheme.SoftPink)
                }
            }

            Text(
                text = calorieGoal.details,
                style = MaterialTheme.typography.bodySmall,
                color = SoftTheme.SoftGray,
                lineHeight = 16.sp
            )

            Text(
                text = "* يُرجى مراجعة الدكتورة المشرفة أو أخصائية تغذية قبل إدخال تغييرات جذرية على طعامكِ.",
                style = MaterialTheme.typography.bodySmall,
                color = SoftTheme.RedDanger,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
