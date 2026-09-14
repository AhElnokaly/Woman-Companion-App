package com.example.ui.nutrition

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SoftTheme

@Composable
fun NutritionWaterTrackerCard(
    currentWaterAmount: Int,
    targetWater: Int,
    isHotWeather: Boolean,
    onAddWater: (Int) -> Unit,
    onResetWater: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, SoftTheme.CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "💧 تتبع شرب المياه",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SoftTheme.TextWhite
                )
                if (isHotWeather) {
                    Surface(
                        color = SoftTheme.MintTeal.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "☀️ +٣٠٠مل للطقس الحار",
                            color = SoftTheme.MintTeal,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Glass graphic with progress
            val fraction = if (targetWater > 0) (currentWaterAmount.toFloat() / targetWater.toFloat()).coerceIn(0f, 1f) else 0f
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(SoftTheme.DeepSlate)
                    .drawBehind {
                        // draw water fill
                        val height = size.height * fraction
                        drawRect(
                            color = SoftTheme.SoftTeal.copy(alpha = 0.6f),
                            topLeft = Offset(0f, size.height - height),
                            size = Size(size.width, height)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$currentWaterAmount / $targetWater مل",
                        style = MaterialTheme.typography.titleMedium,
                        color = SoftTheme.TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${(fraction * 100).toInt()}% مكتمل",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTheme.SoftGray
                    )
                }
            }

            // Buttons for quick add
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { onAddWater(250) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("add_water_250"),
                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftTeal)
                ) {
                    Text("+٢٥٠ مل 🥛", fontSize = 12.sp)
                }
                Button(
                    onClick = { onAddWater(500) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("add_water_500"),
                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.MintTeal)
                ) {
                    Text("+٥٠٠ مل 🍶", fontSize = 12.sp)
                }
                IconButton(
                    onClick = onResetWater,
                    modifier = Modifier.background(SoftTheme.DeepSlate, CircleShape)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "إعادة تعيين", tint = SoftTheme.RedDanger)
                }
            }
        }
    }
}
