package com.example.ui.companion

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.SoftTheme
import com.example.viewmodel.WomanCompanionViewModel

// ==========================================
// 3. سجل الوحم والاشتهاء (Pregnancy Cravings Sub-screen)
// ==========================================
@Composable
fun CravingScreen(viewModel: WomanCompanionViewModel) {
    val cravingLogs by viewModel.allCravingLogsState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    // Craving log state
    var cravingItem by remember { mutableStateOf("") }
    var cravingType by remember { mutableStateOf("Sweet") }
    var intensity by remember { mutableStateOf(5f) }
    var notes by remember { mutableStateOf("") }

    val context = LocalContext.current

    val typeMapping = listOf(
        "Sweet" to "حلو (فواكه وحلويات) 🍓",
        "Salty" to "حادق (موالح ومخللات) 🥨",
        "Sour" to "حامض (ليمون وبرتقال) 🍋",
        "Spicy" to "حار (شطة وفلفل) 🌶️",
        "Chocolate" to "شوكولاتة وكاكاو 🍫",
        "NonFood" to "أخرى (غير غذائي - ثلج/تراب) 🧱"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "سجل الوحم والاشتهاء ومشاركة جوري 🍓🍉",
                        style = MaterialTheme.typography.titleLarge,
                        color = SoftTheme.SoftPink,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "الوحم والاشتهاء أثناء الحمل ليس مجرد رغبة عشوائية، بل هو تعبير رقيق من جسدكِ عن نقص بعض المعادن أو الفيتامينات أو حاجة طفلكِ للطاقة! تتبعي وحمكِ وشاركي جوري لتحصلي على تحليل طبي دافئ.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoftTheme.SoftGray
                    )
                    
                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                        modifier = Modifier.fillMaxWidth().testTag("add_craving_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = SoftTheme.TextWhite)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تسجيل وحم جديد الآن ✍️", color = SoftTheme.TextWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Educational dynamic advice banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("💡 فك شفرة الوحم مع جوري:", fontWeight = FontWeight.Bold, color = SoftTheme.SoftTeal)
                    Text(
                        text = "• **المالح والحادق**: يدل غالباً على تضغط دم واطي وحاجة لتوازن السوائل والأملاح في جسمكِ.\n" +
                               "• **الحلويات والنشويات**: تعني حاجة فورية للطاقة أو تقلبات هرمونية سريعة.\n" +
                               "• **الشوكولاتة**: ترتبط بنقص المغنيسيوم. تناولي الكاكاو الداكن باعتدال.\n" +
                               "• **غير الغذائي (ثلج/تراب/طين)**: قد يكون إشارة فقر دم شديد (أنيميا نقص الحديد). استشيري طبيبتكِ فوراً!",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTheme.SoftGray,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        if (cravingLogs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🍉", fontSize = 48.sp)
                        Text("خزانة ذكريات الوحم فارغة الآن", color = SoftTheme.SoftGray, fontWeight = FontWeight.Bold)
                        Text("سجلي وحمكِ اليوم أو تحدثي مع جوري في الشات ليتم حفظه تلقائياً!", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray.copy(alpha = 0.7f))
                    }
                }
            }
        } else {
            item {
                Text(
                    text = "خزانة ذكريات وحمكِ (${cravingLogs.size}) 📂🍓",
                    style = MaterialTheme.typography.titleMedium,
                    color = SoftTheme.TextWhite,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            items(cravingLogs, key = { it.id }) { log ->
                val emoji = when (log.cravingType) {
                    "Sweet" -> "🍓"
                    "Salty" -> "🥨"
                    "Sour" -> "🍋"
                    "Spicy" -> "🌶️"
                    "Chocolate" -> "🍫"
                    else -> "🧱"
                }

                val typeArabic = typeMapping.firstOrNull { it.first == log.cravingType }?.second ?: "عام"

                val intensityText = when (log.intensity) {
                    in 1..3 -> "لطيف"
                    in 4..6 -> "شديد"
                    in 7..8 -> "قوي جداً!"
                    else -> "جنوني! 🤯"
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(emoji, fontSize = 24.sp)
                                Column {
                                    Text(
                                        text = log.cravingItem,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = SoftTheme.TextWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = typeArabic,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SoftTheme.SoftGray
                                    )
                                }
                            }
                            
                            IconButton(onClick = { viewModel.deleteCravingLog(log) }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.Red.copy(alpha = 0.7f))
                            }
                        }

                        // Intensity and advice
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SoftTheme.DeepSlate.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("شدة الاشتهاء: $intensityText (${log.intensity}/10)", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftTeal)
                            Text(
                                text = java.text.SimpleDateFormat("yyyy/MM/dd", java.util.Locale.getDefault()).format(java.util.Date(log.date)),
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftTheme.SoftGray
                            )
                        }

                        if (!log.notes.isNullOrEmpty()) {
                            Text(
                                text = "✍️ ملاحظاتكِ: ${log.notes}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SoftTheme.SoftGray
                            )
                        }

                        // Jouri's physical/medical commentary
                        val analyticalComment = when (log.cravingType) {
                            "Sweet" -> "رغبتكِ في السكريات تدل على حاجة جسمكِ لطاقة سريعة، أو ربما الجنين يحب السعادة والتحرك! ركزي على الفاكهة الطازجة وتجنبي السكر الصناعي المفرط لحمايتكِ من سكري الحمل 🍓🍰"
                            "Salty" -> "الوحم على الموالح مثل المخللات شائع جداً بسبب تغير توازن السوائل والأملاح نتيجة هرمونات الحمل. تذكري ألا تفرطي بالملح لتجنب احتباس المياه وتورم القدمين واليدين واشربي الكثير من الماء! 🥨🥒"
                            "Sour" -> "الرغبة بالليمون والأطعمة الحامضة ترتبط بمحاولة جسمكِ الطبيعية لمقاومة الغثيان وتسهيل الهضم وزيادة حمض المعدة. الليمون رائع وآمن جداً لكِ 🍋🍏"
                            "Spicy" -> "الوحم على الأكل الحار والشطة يرجع لتأثير الهرمونات على حاستكِ التذوقية، ورغبتكِ في رفع درجة حرارة الجسم والتمثيل الغذائي. انتبهي كي لا يسبب لكِ حموضة أو حرقان المعدة! 🌶️🥵"
                            "Chocolate" -> "الرغبة الملحة في الشوكولاتة والكاكاو تدل غالباً على حاجة جسمكِ لعنصر المغنيسيوم لتهدئة عضلات الرحم ومنع التشنجات! دللي نفسكِ بقطعة شوكولاتة داكنة صحية وقليلة السكر 🍫✨"
                            else -> "الوحم غير الغذائي (مثل اشتهاء الثلج أو الطين والتراب والطباشير) هو إشارة كلاسيكية هامة لاحتمال وجود أنيميا حادة ونقص شديد بالحديد. يرجى مراجعة طبيبتكِ لعمل تحليل صورة دم كاملة فوراً! 🧱🚨"
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SoftTheme.SoftPink.copy(alpha = 0.08f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text("👩‍⚕️", fontSize = 18.sp)
                                Column {
                                    Text("تحليل جوري الذكي:", fontWeight = FontWeight.Bold, color = SoftTheme.SoftPink, style = MaterialTheme.typography.bodySmall)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(analyticalComment, style = MaterialTheme.typography.bodySmall, color = SoftTheme.TextWhite, lineHeight = 16.sp)
                                }
                            }
                        }

                        // Share with partner
                        Button(
                            onClick = {
                                val shareText = "حبيبي الغالي، أنا متوحمة النهاردة على (${log.cravingItem}) بشدة $intensityText! وجوري بتقول إن ده رد فعل طبيعي لجسمي وجنيني ومحتاجين شوية دلال واهتمام.. هتعرف تجيبهالي معاك وأنت جاي؟ 🥰🍓🍉"
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clip = android.content.ClipData.newPlainText("Craving", shareText)
                                clipboard.setPrimaryClip(clip)
                                android.widget.Toast.makeText(context, "تم نسخ رسالة شريككِ اللطيفة بنجاح! 🔗❤️", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.DeepSlate),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = SoftTheme.SoftPink, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("نسخ رسالة دلع لمشاركتها مع زوجكِ 🔗❤️", color = SoftTheme.TextWhite, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = SoftTheme.CardSlate,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "تسجيل وحم جديد 🍓",
                    style = MaterialTheme.typography.titleLarge,
                    color = SoftTheme.SoftPink,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = cravingItem,
                        onValueChange = { cravingItem = it },
                        label = { Text("إيش نفسكِ تأكلي؟ (مثال: مانجو باردة)") },
                        modifier = Modifier.fillMaxWidth().testTag("craving_item_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoftTheme.SoftPink,
                            unfocusedBorderColor = SoftTheme.SoftGray,
                            focusedTextColor = SoftTheme.TextWhite,
                            unfocusedTextColor = SoftTheme.TextWhite
                        )
                    )

                    // Type ChoiceChips
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("تصنيف الوحم والاشتهاء:", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftPink, fontWeight = FontWeight.Bold)
                        
                        val row1 = typeMapping.take(3)
                        val row2 = typeMapping.takeLast(3)

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            row1.forEach { (typeKey, displayName) ->
                                val selected = cravingType == typeKey
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { cravingType = typeKey },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selected) SoftTheme.SoftPink else SoftTheme.DeepSlate.copy(alpha = 0.5f)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Box(modifier = Modifier.padding(8.dp), contentAlignment = Alignment.Center) {
                                        Text(
                                            text = displayName.substringBefore(" "),
                                            color = if (selected) SoftTheme.TextWhite else SoftTheme.SoftGray,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            row2.forEach { (typeKey, displayName) ->
                                val selected = cravingType == typeKey
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { cravingType = typeKey },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selected) SoftTheme.SoftPink else SoftTheme.DeepSlate.copy(alpha = 0.5f)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Box(modifier = Modifier.padding(8.dp), contentAlignment = Alignment.Center) {
                                        Text(
                                            text = displayName.substringBefore(" "),
                                            color = if (selected) SoftTheme.TextWhite else SoftTheme.SoftGray,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Intensity Slider
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        val sliderText = when (intensity.toInt()) {
                            in 1..3 -> "لطيف"
                            in 4..6 -> "شديد"
                            in 7..8 -> "قوي جداً!"
                            else -> "جنوني! 🤯"
                        }
                        Text("قوة الاشتهاء: $sliderText (${intensity.toInt()}/10)", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftTeal, fontWeight = FontWeight.Bold)
                        Slider(
                            value = intensity,
                            onValueChange = { intensity = it },
                            valueRange = 1f..10f,
                            steps = 8,
                            colors = SliderDefaults.colors(
                                thumbColor = SoftTheme.SoftPink,
                                activeTrackColor = SoftTheme.SoftPink,
                                inactiveTrackColor = SoftTheme.SoftGray.copy(alpha = 0.3f)
                            )
                        )
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("ملاحظات أخرى (مثال: في أي وقت، وبم تشعرين؟)") },
                        modifier = Modifier.fillMaxWidth().testTag("craving_notes_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoftTheme.SoftPink,
                            unfocusedBorderColor = SoftTheme.SoftGray,
                            focusedTextColor = SoftTheme.TextWhite,
                            unfocusedTextColor = SoftTheme.TextWhite
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (cravingItem.isNotBlank()) {
                            viewModel.addCravingLog(
                                cravingItem = cravingItem,
                                cravingType = cravingType,
                                intensity = intensity.toInt(),
                                notes = notes.ifBlank { null }
                            )
                            cravingItem = ""
                            notes = ""
                            intensity = 5f
                            cravingType = "Sweet"
                            showAddDialog = false
                        } else {
                            android.widget.Toast.makeText(context, "الرجاء إدخال أكلة الوحم", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink)
                ) {
                    Text("حفظ في خزانة الذكريات 💾", color = SoftTheme.TextWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("إلغاء", color = SoftTheme.SoftPink)
                }
            }
        )
    }
}
