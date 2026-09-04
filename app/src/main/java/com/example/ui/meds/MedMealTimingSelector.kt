package com.example.ui.meds

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.meds.MealRelation
import com.example.ui.SoftTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MedMealTimingSelector(
    selectedMealRelation: MealRelation,
    onMealRelationSelected: (MealRelation) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "التوقيت بالنسبة للطعام: 🍽️",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = SoftTheme.TextWhite
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MealRelation.values().forEach { relation ->
                val isSelected = selectedMealRelation == relation
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) SoftTheme.SoftPink else SoftTheme.CardSlate)
                        .border(
                            1.dp,
                            if (isSelected) SoftTheme.SoftPink else SoftTheme.SoftGray.copy(alpha = 0.2f),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onMealRelationSelected(relation) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(relation.icon, fontSize = 12.sp)
                        Text(
                            text = relation.label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) SoftTheme.DeepSlate else SoftTheme.TextWhite
                        )
                    }
                }
            }
        }

        // Informative tip banner for the selected relation
        if (selectedMealRelation.description.isNotBlank()) {
            Surface(
                color = SoftTheme.DeepSlate,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "💡 ${selectedMealRelation.description}",
                    fontSize = 10.sp,
                    color = SoftTheme.SoftGray,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}
