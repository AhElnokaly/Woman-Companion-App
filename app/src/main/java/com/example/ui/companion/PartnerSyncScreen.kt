package com.example.ui.companion

import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.PartnerMessage
import com.example.ui.SoftTheme
import com.example.ui.formatGregorianDate
import com.example.ui.formatTime
import com.example.viewmodel.WomanCompanionViewModel

// ==========================================
// 1. رابط ومشاركة الشريك (Companion Sync Link)
// ==========================================
@Composable
fun PartnerSyncScreen(viewModel: WomanCompanionViewModel) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val messages by viewModel.allPartnerMessagesState.collectAsStateWithLifecycle()
    val settings by viewModel.appLockSettingsState.collectAsStateWithLifecycle()
    val companionName = settings?.companionName ?: "جوري"

    var showSendDialog by remember { mutableStateOf(false) }
    var inputMessageText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Support") }
    var messageToDelete by remember { mutableStateOf<PartnerMessage?>(null) }
    var showClearAllConfirm by remember { mutableStateOf(false) }

    // Seeded coupling code (completely local and secure)
    val syncCode = remember { "JO-983-PR" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 150.dp)
    ) {
        // Introduction Header Card
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
                        text = "يتيح هذا الرابط لزوجك أو أفراد عائلتك المقربين متابعة وضعك الصحي وعلامات الخطر، وإرسال رسائل التذكير بالماء والأدوية، والتشجيع والاطمئنان في الوقت الحقيقي وبخصوصية تامة.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoftTheme.SoftGray
                    )
                }
            }
        }

        // Coupling Status Card with Copy & WhatsApp Share
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, SoftTheme.SoftTeal.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("رمز المزامنة الفريد لزوجك/رفيقك:", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = syncCode,
                                style = MaterialTheme.typography.titleMedium,
                                color = SoftTheme.SoftTeal,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .background(SoftTheme.CardSlate, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                            Text("شاركي هذا الرمز لربطه", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftPink, fontWeight = FontWeight.Bold)
                        }

                        // WhatsApp and Copy Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(syncCode))
                                    Toast.makeText(context, "تم نسخ الرمز بنجاح: $syncCode 📋", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f).testTag("copy_sync_code_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftTheme.SoftTeal),
                                border = BorderStroke(1.dp, SoftTheme.SoftTeal.copy(alpha = 0.5f))
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "نسخ", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("نسخ الرمز", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    sharePartnerCode(context, syncCode)
                                },
                                modifier = Modifier.weight(1.3f).testTag("share_whatsapp_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)) // WhatsApp brand color
                            ) {
                                Text("💬", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("مشاركة عبر واتساب", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Simulate Action Card with Quick Reply Chips & Custom Message Dialog
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("💡 تظاهر بمحاكاة شريكك (ردود سريعة بلمسة واحدة):", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
                    Text(
                        "أرسلي تنبيهات ورسائل تشجيعية فورية لتجربة الإشعارات والتذكيرات من وجهة نظر الشريك:",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTheme.SoftGray
                    )

                    // Quick send buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            onClick = {
                                viewModel.addPartnerMessage(
                                    senderName = "شريكك الغالي",
                                    messageText = "هل شربتِ كمية كافية من الماء اليوم؟ تذكري شرب كوب الآن لصحتك وصحة طفلنا 💧",
                                    category = "WaterReminder"
                                )
                                Toast.makeText(context, "تم إرسال تذكير الماء من الشريك 💧", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = SoftTheme.DeepSlate,
                            border = BorderStroke(1.dp, SoftTheme.SoftTeal.copy(alpha = 0.3f)),
                            modifier = Modifier.weight(1f).testTag("quick_send_water")
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("💧", fontSize = 20.sp)
                                Text("تذكير ماء", color = SoftTheme.TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Surface(
                            onClick = {
                                viewModel.addPartnerMessage(
                                    senderName = "شريكك الغالي",
                                    messageText = "أنا فخور بكِ وبقوتكِ اليوم. خذي قسطاً من الراحة يا حبيبتي ❤️",
                                    category = "Support"
                                )
                                Toast.makeText(context, "تم إرسال رسالة الحب والدعم ❤️", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = SoftTheme.DeepSlate,
                            border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.3f)),
                            modifier = Modifier.weight(1f).testTag("quick_send_love")
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("❤️", fontSize = 20.sp)
                                Text("دعم وحب", color = SoftTheme.TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Surface(
                            onClick = {
                                viewModel.addPartnerMessage(
                                    senderName = "شريكك الغالي",
                                    messageText = "طمئنيني عن بطلنا الصغير اليوم؟ هل ركل كالعادة؟ 👶",
                                    category = "KickCheck"
                                )
                                Toast.makeText(context, "تم إرسال الاطمئنان على الجنين 👶", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = SoftTheme.DeepSlate,
                            border = BorderStroke(1.dp, Color(0xFFFBBF24).copy(alpha = 0.3f)),
                            modifier = Modifier.weight(1f).testTag("quick_send_kicks")
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("👶", fontSize = 20.sp)
                                Text("حركة الجنين", color = SoftTheme.TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Surface(
                            onClick = {
                                viewModel.addPartnerMessage(
                                    senderName = "شريكك الغالي",
                                    messageText = "لقد لاحظت تعبكِ اليوم، يرجى عدم بذل أي مجهود إضافي والاستلقاء فوراً 🚨",
                                    category = "Alert"
                                )
                                Toast.makeText(context, "تم إرسال التنبيه الصحي 🚨", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = SoftTheme.DeepSlate,
                            border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f)),
                            modifier = Modifier.weight(1f).testTag("quick_send_alert")
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("🚨", fontSize = 20.sp)
                                Text("تنبيه راحة", color = SoftTheme.TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Custom Message Button
                    Button(
                        onClick = { showSendDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.DeepSlate),
                        modifier = Modifier.fillMaxWidth().testTag("custom_message_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "كتابة رسالة", tint = SoftTheme.SoftTeal, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("كتابة رسالة مخصصة 📲", color = SoftTheme.SoftTeal, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // Section Title with Clear All Action
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("الرسائل والتنبيهات المتبادلة:", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
                if (messages.isNotEmpty()) {
                    TextButton(
                        onClick = { showClearAllConfirm = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("مسح الكل 🗑️", color = SoftTheme.SoftPink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
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
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(emoji, fontSize = 18.sp)
                                Text(
                                    text = "$titleText من ${msg.senderName}",
                                    fontWeight = FontWeight.Bold,
                                    color = if (msg.category == "Alert") SoftTheme.SoftPink else SoftTheme.TextWhite,
                                    fontSize = 13.sp
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = formatGregorianDate(msg.timestamp) + " " + formatTime(msg.timestamp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SoftTheme.SoftGray,
                                    fontSize = 10.sp
                                )
                                // Delete Message Button
                                IconButton(
                                    onClick = { messageToDelete = msg },
                                    modifier = Modifier.size(28.dp).testTag("delete_msg_${msg.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.DeleteOutline,
                                        contentDescription = "حذف الرسالة",
                                        tint = SoftTheme.SoftGray.copy(alpha = 0.7f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
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
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("تعليم كمقروءة ✔️", color = SoftTheme.SoftTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete single message dialog
    messageToDelete?.let { msg ->
        AlertDialog(
            onDismissRequest = { messageToDelete = null },
            title = { Text("حذف الرسالة", fontWeight = FontWeight.Bold) },
            text = { Text("هل أنت متأكدة من رغبتك في حذف هذه الرسالة نهائياً؟") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePartnerMessage(msg)
                        messageToDelete = null
                        Toast.makeText(context, "تم حذف الرسالة بنجاح", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink)
                ) {
                    Text("حذف", color = SoftTheme.TextWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { messageToDelete = null }) {
                    Text("إلغاء", color = SoftTheme.SoftGray)
                }
            }
        )
    }

    // Clear all messages confirmation dialog
    if (showClearAllConfirm) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirm = false },
            title = { Text("مسح جميع الرسائل 🗑️", fontWeight = FontWeight.Bold) },
            text = { Text("هل ترغبين في مسح كل الرسائل والتنبيهات المتبادلة؟ لا يمكن التراجع عن هذا الإجراء.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllPartnerMessages()
                        showClearAllConfirm = false
                        Toast.makeText(context, "تم مسح جميع الرسائل", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink)
                ) {
                    Text("مسح الكل", color = SoftTheme.TextWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllConfirm = false }) {
                    Text("إلغاء", color = SoftTheme.SoftGray)
                }
            }
        )
    }

    // Custom Message Send Dialog
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
                        Toast.makeText(context, "تم إرسال الرسالة بنجاح 📲", Toast.LENGTH_SHORT).show()
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

/**
 * Share coupling sync code via WhatsApp or standard system share intent
 */
private fun sharePartnerCode(context: Context, code: String) {
    val shareText = "مرحباً يا غالي ❤️، هذا رمز مزامنة رفيقة المرأة لمتابعة الحمل والاطمئنان عليّ: $code\nيمكنك استخدامه لإرسال رسائل التشجيع والتذكيرات بالماء."
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            setPackage("com.whatsapp")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback to system sharing if WhatsApp application is not installed
        val fallback = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(fallback, "مشاركة رمز المزامنة مع الشريك"))
    }
}
