package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.WomanCompanionViewModel

@Composable
fun JournalSubScreen(viewModel: WomanCompanionViewModel) {
    val journals by viewModel.journalEntriesState.collectAsStateWithLifecycle()

    var journalContent by remember { mutableStateOf("") }
    val moods = listOf("🌸 سعيدة", "🌱 هادئة", "🪵 تعبة", "🩸 قلقة", "✨ متحمسة")
    var selectedMood by remember { mutableStateOf("🌸 سعيدة") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("يوميات ومذكرات الأمومة والطفل ✍️", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)

        Card(
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("اكتبي خواطركِ أو رسالة لطفلكِ القادم:", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    moods.forEach { m ->
                        val isSel = selectedMood == m
                        Button(
                            onClick = { selectedMood = m },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSel) SoftTheme.SoftPink else SoftTheme.DeepSlate,
                                contentColor = if (isSel) SoftTheme.DeepSlate else SoftTheme.TextWhite
                            )
                        ) {
                            Text(m, fontSize = 10.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = journalContent,
                    onValueChange = { journalContent = it },
                    placeholder = { Text("أهلاً طفلي الحبيب، اليوم سمعت صوت قلبك اللطيف لأول مرة...") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SoftTheme.SoftPink,
                        unfocusedBorderColor = SoftTheme.SoftGray,
                        focusedTextColor = SoftTheme.TextWhite,
                        unfocusedTextColor = SoftTheme.TextWhite
                    )
                )

                Button(
                    onClick = {
                        if (journalContent.isNotEmpty()) {
                            viewModel.addJournalEntry(journalContent, selectedMood)
                            journalContent = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                    modifier = Modifier.fillMaxWidth().testTag("add_journal_btn")
                ) {
                    Text("حفظ في المذكرات")
                }
            }
        }

        Text("سجل ذكرياتكِ المكتوبة 📖", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite, modifier = Modifier.align(Alignment.Start))

        if (journals.isEmpty()) {
            Text("لا توجد مذكرات مسجلة بعد.", color = SoftTheme.SoftGray)
        } else {
            journals.forEach { entry ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("المزاج: ${entry.mood ?: "طبيعي"}", fontWeight = FontWeight.Bold, color = SoftTheme.SoftPink)
                            IconButton(onClick = { viewModel.deleteJournal(entry) }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = SoftTheme.RedDanger)
                            }
                        }
                        Text(entry.content, color = SoftTheme.TextWhite, style = MaterialTheme.typography.bodyMedium)
                        Text(formatGregorianDate(entry.date), style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray, modifier = Modifier.align(Alignment.End))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(80.dp))
    }
}
