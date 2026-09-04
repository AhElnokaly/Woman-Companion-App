package com.example.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SoftTheme

@Composable
fun ArabicVirtualKeyboard(
    onCharClick: (String) -> Unit,
    onSpaceClick: () -> Unit,
    onBackspaceClick: () -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardRows = remember {
        listOf(
            listOf("ض", "ص", "ث", "ق", "ف", "غ", "ع", "ه", "خ", "ح", "ج", "د"),
            listOf("ش", "س", "ي", "ب", "ل", "ا", "ت", "ن", "م", "ك", "ط"),
            listOf("ئ", "ء", "ؤ", "ر", "لا", "ة", "و", "ز", "ذ", "ظ", "أ", "إ")
        )
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            keyboardRows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    row.forEach { char ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .background(SoftTheme.CardSlate, RoundedCornerShape(6.dp))
                                .clickable { onCharClick(char) }
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = char,
                                style = MaterialTheme.typography.bodyMedium,
                                color = SoftTheme.TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
            // Control Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Space
                Box(
                    modifier = Modifier
                        .weight(2f)
                        .height(38.dp)
                        .background(SoftTheme.CardSlate, RoundedCornerShape(6.dp))
                        .clickable { onSpaceClick() }
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "مسافة ␣",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTheme.TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                }
                // Backspace
                Box(
                    modifier = Modifier
                        .weight(1.5f)
                        .height(38.dp)
                        .background(SoftTheme.SoftPink.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                        .clickable { onBackspaceClick() }
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "مسح ⌫",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTheme.SoftPink,
                        fontWeight = FontWeight.Bold
                    )
                }
                // Clear
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .background(SoftTheme.SoftPink.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                        .clickable { onClearClick() }
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "حذف ❌",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTheme.SoftPink.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
