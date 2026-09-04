package com.example.ui.meds

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.meds.MedicationDosageUnit
import com.example.ui.SoftTheme

@Composable
fun MedDosageSelector(
    dosageAmount: String,
    onDosageAmountChange: (String) -> Unit,
    selectedUnit: MedicationDosageUnit,
    onUnitSelected: (MedicationDosageUnit) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "الجرعة والكمية: 💊",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = SoftTheme.TextWhite
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Amount Input field (e.g. 1, 2, 500)
            OutlinedTextField(
                value = dosageAmount,
                onValueChange = onDosageAmountChange,
                label = { Text("الكمية (مثال: 1 أو 500)", fontSize = 11.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SoftTheme.SoftPink,
                    unfocusedBorderColor = SoftTheme.CardSlate,
                    focusedTextColor = SoftTheme.TextWhite,
                    unfocusedTextColor = SoftTheme.TextWhite,
                    focusedLabelColor = SoftTheme.SoftPink,
                    unfocusedLabelColor = SoftTheme.SoftGray
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1.2f)
            )

            // Selected Unit badge preview
            Surface(
                color = SoftTheme.CardSlate,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.4f)),
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp)) {
                    Text(
                        text = "${selectedUnit.icon} ${selectedUnit.label}",
                        color = SoftTheme.SoftPink,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Quick scrollable unit picker
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MedicationDosageUnit.values().forEach { unit ->
                val isSelected = selectedUnit == unit
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) SoftTheme.SoftPink else SoftTheme.CardSlate)
                        .border(
                            1.dp,
                            if (isSelected) SoftTheme.SoftPink else Color.Transparent,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onUnitSelected(unit) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${unit.icon} ${unit.label}",
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) SoftTheme.DeepSlate else SoftTheme.TextWhite
                    )
                }
            }
        }
    }
}
