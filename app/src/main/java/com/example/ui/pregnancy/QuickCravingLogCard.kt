package com.example.ui.pregnancy

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.SoftTheme
import com.example.viewmodel.WomanCompanionViewModel

@Composable
fun QuickCravingLogCard(
    viewModel: WomanCompanionViewModel,
    onNavigateToCraving: () -> Unit = {}
) {
    val cravingLogs by viewModel.allCravingLogsState.collectAsStateWithLifecycle()
    var cravingText by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Sweet") }
    var intensity by remember { mutableStateOf(5f) }

    val categories = listOf(
        "Sweet" to "حلو 🍓",
        "Sour" to "حامض 🍋",
        "Salty" to "حادق 🥨",
        "Spicy" to "حار 🌶️",
        "Chocolate" to "شوكولاتة 🍫"
    )

    Card(
        modifier = Modifier.fillMaxWidth().testTag("quick_craving_card"),
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🍓", fontSize = 22.sp)
                    Text(
                        "سجل الوحم والاشتهاء",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.TextWhite
                    )
                }
                TextButton(
                    onClick = onNavigateToCraving,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    modifier = Modifier.testTag("craving_header_goto_btn")
                ) {
                    Text("السجل الكامل 🍉 ↗", color = SoftTheme.SoftPink, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            OutlinedTextField(
                value = cravingText,
                onValueChange = { cravingText = it },
                placeholder = { Text("مثال: مانجو، شيكولاتة، مخلل...", color = SoftTheme.SoftGray, fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SoftTheme.SoftPink,
                    unfocusedBorderColor = SoftTheme.SoftGray.copy(alpha = 0.3f),
                    focusedContainerColor = SoftTheme.DeepSlate,
                    unfocusedContainerColor = SoftTheme.DeepSlate,
                    focusedTextColor = SoftTheme.TextWhite,
                    unfocusedTextColor = SoftTheme.TextWhite
                ),
                singleLine = true
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { (key, label) ->
                    FilterChip(
                        selected = selectedType == key,
                        onClick = { selectedType = key },
                        label = { Text(label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SoftTheme.SoftPink,
                            selectedLabelColor = Color.White,
                            containerColor = SoftTheme.DeepSlate,
                            labelColor = SoftTheme.TextWhite
                        )
                    )
                }
            }

            Button(
                onClick = {
                    if (cravingText.isNotBlank()) {
                        viewModel.addCravingLog(
                            cravingItem = cravingText.trim(),
                            cravingType = selectedType,
                            intensity = intensity.toInt(),
                            notes = "مسجّل سريعا من الشاشة الرئيسية"
                        )
                        cravingText = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = cravingText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink)
            ) {
                Text("حفظ الوحم في سجل جوري ✍️", color = Color.White, fontWeight = FontWeight.Bold)
            }

            if (cravingLogs.isNotEmpty()) {
                Text("الوحم المسجل حديثاً:", style = MaterialTheme.typography.labelSmall, color = SoftTheme.SoftGray)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(cravingLogs.take(5)) { log ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(SoftTheme.DeepSlate)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                "${log.cravingItem} (${log.intensity}/10)",
                                fontSize = 11.sp,
                                color = SoftTheme.MintTeal,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = onNavigateToCraving,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftTheme.SoftPink),
                border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth().testTag("craving_bottom_goto_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("فتح سجل ومحلل الوحم الكامل مع جوري 🍉 ↗", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
