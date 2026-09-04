package com.example.ui.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SoftTheme

@Composable
fun JouriConsultationCatalog(
    categories: List<String>,
    subItems: Map<String, List<Pair<String, String>>>,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    onSelectOption: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📋 قائمة الاستشارات والخيارات الجاهزة:",
                style = MaterialTheme.typography.bodySmall,
                color = SoftTheme.SoftPink,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
            Text(
                text = "اضغطي على أي خيار للسؤال فوراً ⚡",
                style = MaterialTheme.typography.bodySmall,
                color = SoftTheme.SoftGray,
                fontSize = 9.sp
            )
        }

        // 1. Categories Tab Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(categories) { category ->
                val isSelected = selectedCategory == category
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) SoftTheme.SoftPink else SoftTheme.DeepSlate)
                        .clickable { onSelectCategory(category) }
                        .border(
                            1.dp,
                            if (isSelected) SoftTheme.LightPink else SoftTheme.SoftGray.copy(alpha = 0.2f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) SoftTheme.DeepSlate else SoftTheme.TextWhite
                    )
                }
            }
        }

        // 2. Selected Category's Options List
        val activeOptions = subItems[selectedCategory] ?: emptyList()
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(activeOptions) { (label, fullText) ->
                AssistChip(
                    onClick = { onSelectOption(fullText) },
                    label = {
                        Text(
                            text = label,
                            color = SoftTheme.TextWhite,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = SoftTheme.DeepSlate,
                        labelColor = SoftTheme.TextWhite
                    ),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.35f))
                )
            }
        }
    }
}
