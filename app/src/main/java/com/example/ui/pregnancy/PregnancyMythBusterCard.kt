package com.example.ui.pregnancy

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.OfflineJouriEngine
import com.example.ui.SoftTheme
import com.example.viewmodel.WomanCompanionViewModel
import kotlinx.coroutines.launch

@Composable
fun PregnancyMythBusterCard(
    viewModel: WomanCompanionViewModel,
    onNavigateToFoodSafety: () -> Unit = {},
    onOpenJouriChat: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var userQuestion by remember { mutableStateOf("") }
    var jouriAnswer by remember { mutableStateOf<String?>(null) }
    var isAskingJouri by remember { mutableStateOf(false) }

    val myths = remember {
        listOf(
            Triple("شرب الحليب البارد يسبب مغص للجنين؟", "❌ خرافة", "الحليب البارد آمن وممتع تماماً ولا يصل للجنين برودته لأن جسمك ينظم حرارة الطعام فور بلعه."),
            Triple("شكل البطن يحدد نوع الجنين (ولد أو بنت)؟", "❌ خرافة", "شكل البطن يعتمد فقط على قوة عضلات بطنكِ، وضعية الجنين، وعدد مرات حملك السابقة وليس له علاقة بالنوع."),
            Triple("استخدام صبغات الشعر ممنوع طوال الحمل؟", "⚠️ حقيقة جزئية", "يفضل تجنب الصبغات في الثلث الأول (أول 12 أسبوع) حمايةً لنمو الأعضاء، لكنها آمنة نسبيًا بعد ذلك بشرط تهوية المكان واستخدام أنواع خالية من الأمونيا."),
            Triple("الحامل يجب أن تأكل عن شخصين؟", "❌ خرافة", "الحامل تحتاج فقط لـ 300 سعرة حرارية إضافية يومياً (كوب لبن وموزة) بدءاً من الثلث الثاني وليس مضاعفة الأكل!"),
            Triple("تناول التمر يسبب الإجهاض في بداية الحمل؟", "❌ خرافة", "التمر غني بالألياف والحديد والسكريات الطبيعية وآمن باعتدال، ولكنه يفيد خصوصاً في الشهر الأخير لتسهيل الولادة."),
            Triple("المجهود الخفيف والمشي يضر الحامل؟", "❌ خرافة", "المشي والنشاط الخفيف المعتدل يحسن الدورة الدموية، يقلل التورم، ويساعد على ولادة أسهل وأسرع.")
        )
    }

    val filteredMyths = myths.filter {
        searchQuery.isBlank() || it.first.contains(searchQuery) || it.third.contains(searchQuery)
    }

    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth().testTag("pregnancy_myth_buster_card"),
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("💡", fontSize = 22.sp)
                Column {
                    Text(
                        "صندوق التساؤلات: خرافات وحقائق الشائعة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.TextWhite
                    )
                    Text("تصحيح المفاهيم الطبية الشائعة في المجتمع", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("ابحثي في خرافات الحمل والدورة...", color = SoftTheme.SoftGray, fontSize = 12.sp) },
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

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                filteredMyths.forEach { (question, verdict, explanation) ->
                    var isExpanded by remember { mutableStateOf(false) }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isExpanded = !isExpanded },
                        colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(question, style = MaterialTheme.typography.bodyMedium, color = SoftTheme.TextWhite, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Text(verdict, style = MaterialTheme.typography.bodySmall, color = if (verdict.contains("خرافة")) SoftTheme.RedDanger else SoftTheme.MintTeal, fontWeight = FontWeight.Bold)
                            }
                            AnimatedVisibility(visible = isExpanded) {
                                Text(explanation, style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = SoftTheme.DeepSlate, thickness = 1.dp)
            Text("اسألي جوري عن أي خرافة أو إشاعة أخرى: 🔮", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftPink, fontWeight = FontWeight.Bold)

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = userQuestion,
                    onValueChange = { userQuestion = it },
                    placeholder = { Text("مثال: هل الاستحمام بماء دافئ يضر الدورة؟", color = SoftTheme.SoftGray, fontSize = 11.sp) },
                    modifier = Modifier.weight(1f),
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
                Button(
                    onClick = {
                        if (userQuestion.isNotBlank()) {
                            isAskingJouri = true
                            scope.launch {
                                val pregState = viewModel.pregnancyState.value
                                val phaseInfo = viewModel.getCurrentCyclePhase()
                                val waterLog = viewModel.todayWaterLogState.value?.amountMl ?: 0
                                val resp = OfflineJouriEngine.getResponse(
                                    userInput = userQuestion,
                                    motherName = pregState?.motherName,
                                    phaseInfo = phaseInfo,
                                    pregnancyState = pregState,
                                    todayWaterLogged = waterLog
                                ).replyText
                                jouriAnswer = resp
                                isAskingJouri = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                    enabled = userQuestion.isNotBlank() && !isAskingJouri
                ) {
                    Text("سلي جوري", fontSize = 11.sp, color = Color.White)
                }
            }

            jouriAnswer?.let { ans ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SoftTheme.MintTeal.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("🌸 رد جوري الطبّي:", fontWeight = FontWeight.Bold, color = SoftTheme.MintTeal, fontSize = 12.sp)
                        Text(ans, style = MaterialTheme.typography.bodySmall, color = SoftTheme.TextWhite)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateToFoodSafety,
                    modifier = Modifier.weight(1f).testTag("goto_food_safety_btn"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftTheme.MintTeal),
                    border = BorderStroke(1.dp, SoftTheme.MintTeal.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text("دليل سلامة الأطعمة 🥑 ↗", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onOpenJouriChat,
                    modifier = Modifier.weight(1f).testTag("goto_jouri_chat_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink.copy(alpha = 0.35f)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text("استشيري جوري 🌸 ↗", color = SoftTheme.TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
