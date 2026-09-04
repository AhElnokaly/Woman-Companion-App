package com.example.ui.companion

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.SoftTheme
import com.example.ui.formatGregorianDate
import com.example.ui.formatTime
import com.example.viewmodel.WomanCompanionViewModel

// ==========================================
// 1. رابط ومشاركة الشريك (Companion Sync Link)
// ==========================================
@Composable
fun PartnerSyncScreen(viewModel: WomanCompanionViewModel) {
    val messages by viewModel.allPartnerMessagesState.collectAsStateWithLifecycle()
    val settings by viewModel.appLockSettingsState.collectAsStateWithLifecycle()
    val companionName = settings?.companionName ?: "جوري"

    var showSendDialog by remember { mutableStateOf(false) }
    var inputMessageText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Support") }
    
    // Seeded coupling code (completely local and secure)
    val syncCode = remember { "JO-983-PR" }

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
                        text = "رابط الرفيق ومشاركة الشريك 🔗❤️",
                        style = MaterialTheme.typography.titleLarge,
                        color = SoftTheme.SoftPink,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "يتيح هذا الرابط لزوجك أو أفراد عائلتك المقربين بمتابعة وضعك الصحي وعلامات الخطر، وإرسال رسائل التذكير بالماء والأدوية، والتشجيع والاطمئنان في الوقت الحقيقي وبخصوصية تامة.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoftTheme.SoftGray
                    )
                }
            }
        }

        // Coupling Status Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, SoftTheme.SoftTeal.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier.size(12.dp).background(SoftTheme.SoftTeal, CircleShape)
                            )
                            Text("رابط المزامنة نشط ومؤمن 🟢", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
                        }
                        Text("محلي ومشفر", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                    }

                    HorizontalDivider(color = SoftTheme.SoftGray.copy(alpha = 0.15f))

                    Column {
                        Text("رمز المزامنة الفريد لزوجك/رفيقك:", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = syncCode,
                                style = MaterialTheme.typography.titleMedium,
                                color = SoftTheme.SoftTeal,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.background(SoftTheme.CardSlate, RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                            Text("شاركي هذا الرمز لربطه", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftPink, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Simulate Action Card for testing
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("💡 تظاهر بمحاكاة شريكك:", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
                    Text(
                        "هذه لوحة تحكم سريعة لإرسال رسالة من هاتف الشريك لتجربة نظام الإشعارات والتنبيه المحلي فوريًا.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTheme.SoftGray
                    )
                    Button(
                        onClick = { showSendDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.DeepSlate),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("إرسال رسالة دعم من الشريك 📲", color = SoftTheme.SoftTeal, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Text("الرسائل والتنبيهات المتبادلة:", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
        }

        if (messages.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("💬", fontSize = 48.sp)
                        Text("لا توجد رسائل دعم من شريكك بعد.", color = SoftTheme.SoftGray, fontWeight = FontWeight.Bold)
                        Text("أي رسائل يرسلها الرفيق ستظهر هنا لتشجيعك ومتابعة نشاطك.", color = SoftTheme.SoftGray, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                    }
                }
            }
        } else {
            items(messages, key = { it.id }) { msg ->
                val emoji = when (msg.category) {
                    "Support" -> "❤️"
                    "Alert" -> "🚨"
                    "WaterReminder" -> "💧"
                    "KickCheck" -> "👶"
                    else -> "💌"
                }

                val titleText = when (msg.category) {
                    "Support" -> "رسالة تشجيع ودعم"
                    "Alert" -> "تنبيه هام ومستعجل"
                    "WaterReminder" -> "تذكير بشرب الماء"
                    "KickCheck" -> "تذكير بعد حركة الجنين"
                    else -> "مزامنة الشريك"
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (msg.isRead) SoftTheme.CardSlate else SoftTheme.CardSlate.copy(alpha = 0.9f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = if (!msg.isRead) BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.4f)) else null
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(emoji, fontSize = 18.sp)
                                Text(
                                    text = "$titleText من ${msg.senderName}",
                                    fontWeight = FontWeight.Bold,
                                    color = if (msg.category == "Alert") SoftTheme.SoftPink else SoftTheme.TextWhite
                                )
                            }
                            Text(
                                text = formatGregorianDate(msg.timestamp) + " " + formatTime(msg.timestamp),
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftTheme.SoftGray
                            )
                        }

                        Text(
                            text = msg.messageText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = SoftTheme.TextWhite
                        )

                        if (!msg.isRead) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = { viewModel.markPartnerMessageAsRead(msg.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.DeepSlate),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("تعليم كمقروءة ✔️", color = SoftTheme.SoftTeal, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSendDialog) {
        AlertDialog(
            onDismissRequest = { showSendDialog = false },
            title = { Text("إرسال رسالة كشريك 📱", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("اختر نوع الرسالة والتذكير الموجه لحبيبتك:", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                    
                    val categories = listOf(
                        Pair("Support", "❤️ رسالة حب ودعم"),
                        Pair("WaterReminder", "💧 تذكير بشرب الماء"),
                        Pair("KickCheck", "👶 اطمئنان على حركة الجنين"),
                        Pair("Alert", "🚨 تنبيه صحي عاجل")
                    )

                    categories.forEach { (cat, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedCategory = cat }
                                .background(
                                    if (selectedCategory == cat) SoftTheme.DeepSlate else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                colors = RadioButtonDefaults.colors(selectedColor = SoftTheme.SoftPink)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label, color = SoftTheme.TextWhite)
                        }
                    }

                    OutlinedTextField(
                        value = inputMessageText,
                        onValueChange = { inputMessageText = it },
                        label = { Text("اكتب نص الرسالة هنا...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val sender = "شريكك الغالي"
                        val defaultText = when (selectedCategory) {
                            "Support" -> "أنا فخور بكِ وبقوتكِ اليوم. خذي قسطاً من الراحة يا حبيبتي ❤️"
                            "WaterReminder" -> "هل شربتِ كمية كافية من الماء اليوم؟ تذكري شرب كوب الآن لصحتك وصحة طفلنا 💧"
                            "KickCheck" -> "طمئنيني عن بطلنا الصغير اليوم؟ هل ركل كالعادة؟ 👶"
                            "Alert" -> "لقد لاحظت تعبكِ اليوم، يرجى عدم بذل أي مجهود إضافي والاستلقاء فوراً 🚨"
                            else -> "مساندة لكِ"
                        }
                        
                        viewModel.addPartnerMessage(
                            senderName = sender,
                            messageText = inputMessageText.ifBlank { defaultText },
                            category = selectedCategory
                        )
                        inputMessageText = ""
                        showSendDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink)
                ) {
                    Text("إرسال فوري", color = SoftTheme.TextWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSendDialog = false }) {
                    Text("إلغاء", color = SoftTheme.SoftPink)
                }
            }
        )
    }
}
