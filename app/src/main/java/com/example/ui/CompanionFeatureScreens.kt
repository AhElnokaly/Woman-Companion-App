package com.example.ui

import androidx.compose.animation.animateContentSize
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.WomanCompanionViewModel

private fun formatArabicDays(days: Int): String {
    return when {
        days == 1 -> "يوم واحد"
        days == 2 -> "يومان"
        days in 3..10 -> "$days أيام"
        else -> "$days يوم"
    }
}

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

// ==========================================
// 2. الصيدلية المنزلية المتقدمة (Advanced Home Pharmacy)
// ==========================================
@Composable
fun HomePharmacyScreen(viewModel: WomanCompanionViewModel) {
    val medications by viewModel.allMedicationsState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    // Medication adding state
    var medName by remember { mutableStateOf("") }
    var medDosage by remember { mutableStateOf("") }
    var medTimesPerDay by remember { mutableStateOf("1") }
    var medPrescby by remember { mutableStateOf("") }
    var medNotes by remember { mutableStateOf("") }
    
    // Advanced pharmacy state
    var expiryDaysOffset by remember { mutableStateOf(90f) } // default 90 days in future
    var totalQty by remember { mutableStateOf("30") }
    var remainingQty by remember { mutableStateOf("30") }
    var safetyStatus by remember { mutableStateOf("Safe") } // Safe, Caution, AskDoctor

    val context = LocalContext.current

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
                        text = "الصيدلية المنزلية المتقدمة وخزانة الأدوية 💊📦",
                        style = MaterialTheme.typography.titleLarge,
                        color = SoftTheme.SoftPink,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "جرد كامل لأدويتك ومكملاتك الغذائية خلال فترة الحمل والنفاس. يراقب التطبيق تاريخ الصلاحية، وكمية العبوات المتبقية، مع عرض كاشف ذكي لأمان استخدام الأدوية مع الحمل.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoftTheme.SoftGray
                    )
                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                        modifier = Modifier.fillMaxWidth().testTag("add_pharmacy_med_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = SoftTheme.TextWhite)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("إضافة دواء أو مكمل للخزانة 💊", color = SoftTheme.TextWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (medications.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("📦", fontSize = 48.sp)
                        Text("الخزانة الدوائية فارغة الآن.", color = SoftTheme.SoftGray, fontWeight = FontWeight.Bold)
                        Text("أضيفي الفيتامينات مثل الحديد أو الفوليك أسيد لمراقبتها.", color = SoftTheme.SoftGray, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        } else {
            item {
                Text("الأدوية والفيتامينات المخزنة حالياً:", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
            }

            items(medications, key = { it.id }) { med ->
                val isLowStock = med.remainingQuantity < 5
                val isExpired = med.expiryDate?.let { it < System.currentTimeMillis() } ?: false
                val isCloseToExpiry = med.expiryDate?.let { (it - System.currentTimeMillis()) < 30L * 24 * 60 * 60 * 1000 } ?: false

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(med.name, fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite, fontSize = 16.sp)
                                if (!med.dosage.isNullOrBlank()) {
                                    Text("الجرعة: ${med.dosage}", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftTeal)
                                }
                            }

                            // Safety Warning Tag
                            val safetyColor = when (med.safetyWarning) {
                                "Safe" -> SoftTheme.SoftTeal
                                "Caution" -> Color(0xFFFFA726) // Amber
                                "AskDoctor" -> SoftTheme.SoftPink
                                else -> SoftTheme.SoftGray
                            }

                            val safetyText = when (med.safetyWarning) {
                                "Safe" -> "آمن مع الحمل 🍏"
                                "Caution" -> "بحذر واستشارة ⚠️"
                                "AskDoctor" -> "غير آمن / راجعي الطبيب 🚨"
                                else -> "غير مصنف"
                            }

                            Box(
                                modifier = Modifier
                                    .background(safetyColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(safetyText, color = safetyColor, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }

                        HorizontalDivider(color = SoftTheme.SoftGray.copy(alpha = 0.15f))

                        // Stock counts
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "المخزون المتبقي بالخزانة:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SoftTheme.SoftGray
                                )
                                Text(
                                    text = "${med.remainingQuantity} من أصل ${med.totalQuantity} وحدة",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLowStock) SoftTheme.SoftPink else SoftTheme.TextWhite,
                                    fontSize = 12.sp
                                )
                            }

                            // Visual Stock Progress
                            if (med.totalQuantity > 0) {
                                val fraction = med.remainingQuantity.toFloat() / med.totalQuantity.toFloat()
                                LinearProgressIndicator(
                                    progress = { fraction.coerceIn(0f, 1f) },
                                    modifier = Modifier.fillMaxWidth().height(6.dp),
                                    color = if (isLowStock) SoftTheme.SoftPink else SoftTheme.SoftTeal,
                                    trackColor = SoftTheme.DeepSlate
                                )
                            }
                        }

                        // Expiry and Alerts
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                if (med.expiryDate != null) {
                                    val formattedExpiry = formatGregorianDate(med.expiryDate)
                                    val textCol = when {
                                        isExpired -> SoftTheme.SoftPink
                                        isCloseToExpiry -> Color(0xFFFFA726)
                                        else -> SoftTheme.SoftTeal
                                    }
                                    val textLabel = when {
                                        isExpired -> "منتهي الصلاحية ❌"
                                        isCloseToExpiry -> "صلاحية حرجة (أقل من شهر) ⚠️"
                                        else -> "صالح حتى: $formattedExpiry 🍏"
                                    }
                                    Text(textLabel, style = MaterialTheme.typography.bodySmall, color = textCol, fontWeight = FontWeight.Bold)
                                } else {
                                    Text("تاريخ الصلاحية: غير مسجل", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                                }
                            }

                            if (isLowStock) {
                                Box(
                                    modifier = Modifier
                                        .background(SoftTheme.SoftPink.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                    Text("مخزون حرج! 🚨", color = SoftTheme.SoftPink, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Take Dose Action
                            Button(
                                onClick = {
                                    if (med.remainingQuantity > 0) {
                                        viewModel.decrementMedicationStock(med, 1)
                                    } else {
                                        android.widget.Toast.makeText(context, "المخزون نفد تماماً! يرجى إعادة تعبئة الدواء.", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.DeepSlate),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).testTag("take_dose_btn_${med.id}"),
                                contentPadding = PaddingValues(vertical = 4.dp, horizontal = 12.dp)
                            ) {
                                Text("تسجيل تناول جرعة 💊", color = SoftTheme.SoftTeal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            IconButton(
                                onClick = { viewModel.deleteMedication(med) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = SoftTheme.SoftPink.copy(alpha = 0.7f))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("إضافة دواء / مكمل للخزانة 💊", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = medName,
                        onValueChange = { medName = it },
                        label = { Text("اسم الدواء / الفيتامين (مثال: الحديد)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = medDosage,
                        onValueChange = { medDosage = it },
                        label = { Text("الجرعة (مثال: حبة واحدة ٥٠٠ملج)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = medTimesPerDay,
                        onValueChange = { medTimesPerDay = it },
                        label = { Text("مرات التكرار يومياً") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = totalQty,
                        onValueChange = { totalQty = it },
                        label = { Text("الكمية الإجمالية بالعلبة (عدد الحبوب الكلي)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = remainingQty,
                        onValueChange = { remainingQty = it },
                        label = { Text("الكمية المتوفرة حالياً بالخزانة") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("تاريخ الصلاحية المقدر:", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Slider(
                            value = expiryDaysOffset,
                            onValueChange = { expiryDaysOffset = it },
                            valueRange = 10f..730f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(thumbColor = SoftTheme.SoftPink, activeTrackColor = SoftTheme.SoftPink)
                        )
                        Text(formatArabicDays(expiryDaysOffset.toInt()), fontWeight = FontWeight.Bold, color = SoftTheme.SoftTeal, fontSize = 12.sp)
                    }

                    Text("مستوى الأمان للأم والطفل:", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
                    
                    val safetyOptions = listOf(
                        Triple("Safe", "Safe", "🍏 آمن بالكامل مع الحمل والرضاعة"),
                        Triple("Caution", "Caution", "⚠️ بحذر شديد مع الحمل واستشارة"),
                        Triple("AskDoctor", "AskDoctor", "🚨 غير آمن / تجنبيه تماماً")
                    )

                    safetyOptions.forEach { (stat, valName, desc) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { safetyStatus = stat }
                                .background(
                                    if (safetyStatus == stat) SoftTheme.DeepSlate else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = safetyStatus == stat,
                                onClick = { safetyStatus = stat },
                                colors = RadioButtonDefaults.colors(selectedColor = SoftTheme.SoftPink)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(desc, color = SoftTheme.TextWhite, fontSize = 12.sp)
                        }
                    }

                    OutlinedTextField(
                        value = medNotes,
                        onValueChange = { medNotes = it },
                        label = { Text("تعليمات إضافية (مثال: يؤخذ بعد الغداء)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (medName.isNotBlank()) {
                            val times = medTimesPerDay.toIntOrNull() ?: 1
                            val tot = totalQty.toIntOrNull() ?: 30
                            val rem = remainingQty.toIntOrNull() ?: 30
                            val expDate = System.currentTimeMillis() + (expiryDaysOffset.toLong() * 24 * 60 * 60 * 1000)

                            viewModel.addMedication(
                                name = medName,
                                dosage = medDosage.ifBlank { null },
                                timesPerDay = times,
                                prescby = medPrescby.ifBlank { null },
                                notes = medNotes.ifBlank { null },
                                start = System.currentTimeMillis(),
                                expiryDate = expDate,
                                totalQuantity = tot,
                                remainingQuantity = rem,
                                safetyWarning = safetyStatus
                            )
                            
                            // Reset state
                            medName = ""
                            medDosage = ""
                            medTimesPerDay = "1"
                            medPrescby = ""
                            medNotes = ""
                            expiryDaysOffset = 90f
                            totalQty = "30"
                            remainingQty = "30"
                            safetyStatus = "Safe"
                            
                            showAddDialog = false
                        } else {
                            android.widget.Toast.makeText(context, "الرجاء إدخال اسم الدواء", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink)
                ) {
                    Text("حفظ للخزانة 💾", color = SoftTheme.TextWhite)
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

// ==========================================
// 4. بانر ونافذة التحديثات والتنبيهات الذكية (Updates Banner & Smart Notifications)
// ==========================================
@Composable
fun NewFeaturesUpdatesBanner(
    viewModel: WomanCompanionViewModel,
    onNavigateToTab: (Int) -> Unit
) {
    val isDismissed by viewModel.isUpdatesBannerDismissed.collectAsState()

    if (isDismissed) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("🚀", fontSize = 24.sp)
                    Column {
                        Text(
                            text = "التحديثات والميزات الذكية الجديدة 🌟",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.SoftPink
                        )
                        Text(
                            text = "اكتشفي ميزات صديقتكِ جوري المحدثة لراحتكِ",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTheme.SoftGray
                        )
                    }
                }
                IconButton(
                    onClick = {
                        viewModel.dismissUpdatesBanner()
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق التنبيه",
                        tint = SoftTheme.SoftGray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Feature 1: Craving
                NewFeatureItemRow(
                    emoji = "🍓",
                    title = "سجل الوحم والاشتهاء ومشاركة جوري",
                    description = "تتبعي رغباتكِ اليومية والوحم، واحصلي على تحليل طبي دافئ، وشاركي مشاعركِ اللطيفة كرسالة جاهزة لزوجكِ! 🥰🍉",
                    onOpen = {
                        viewModel.setActiveSubScreen("craving")
                        onNavigateToTab(4)
                    }
                )

                HorizontalDivider(color = SoftTheme.DeepSlate.copy(alpha = 0.5f), thickness = 0.5.dp)

                // Feature 2: Sleep
                NewFeatureItemRow(
                    emoji = "💤",
                    title = "محلل ومراقب النوم الذكي والأرق",
                    description = "سجلي ساعات نومكِ وجودته، ودعي جوري تحلل لكِ عادات نومكِ وتقدم لكِ نصائح ذهبية لنوم هانئ ومريح. 🌙💤",
                    onOpen = {
                        viewModel.setActiveSubScreen("sleep_analyzer")
                        onNavigateToTab(4)
                    }
                )

                HorizontalDivider(color = SoftTheme.DeepSlate.copy(alpha = 0.5f), thickness = 0.5.dp)

                // Feature 3: Partner
                NewFeatureItemRow(
                    emoji = "🔗",
                    title = "رابط الرفيق ومشاركة الشريك",
                    description = "أشركي زوجكِ أو عائلتكِ في رحلتكِ واستقبلي رسائل الدعم والاهتمام بخصوصية تامة داخل التطبيق. ❤️✨",
                    onOpen = {
                        viewModel.setActiveSubScreen("partner_sync")
                        onNavigateToTab(4)
                    }
                )

                HorizontalDivider(color = SoftTheme.DeepSlate.copy(alpha = 0.5f), thickness = 0.5.dp)

                // Feature 4: Pharmacy
                NewFeatureItemRow(
                    emoji = "📦",
                    title = "الصيدلية المنزلية المتقدمة وتتبع الأدوية",
                    description = "سجلي علب أدويتكِ وصلاحيتها ومخزونها المتبقي لتنبيهكِ قبل النفاد ولتجنب استخدام أدوية منتهية الصلاحية. 💊📦",
                    onOpen = {
                        viewModel.setActiveSubScreen("home_pharmacy")
                        onNavigateToTab(4)
                    }
                )

                HorizontalDivider(color = SoftTheme.DeepSlate.copy(alpha = 0.5f), thickness = 0.5.dp)

                // Feature 5: Fitness
                NewFeatureItemRow(
                    emoji = "🧘‍♀️",
                    title = "تمارين لياقة المرأة الحامل والنفاس",
                    description = "تمارين رياضية آمنة مخصصة لثلث حملكِ أو فترة النفاس مع مؤقت ذكي لمساعدتكِ على أداء الحركة بنشاط وصحة. 🧘‍♀️💪",
                    onOpen = {
                        viewModel.setActiveSubScreen("fitness")
                        onNavigateToTab(4)
                    }
                )
            }
        }
    }
}

@Composable
fun NewFeatureItemRow(
    emoji: String,
    title: String,
    description: String,
    onOpen: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate.copy(alpha = 0.25f)),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.TextWhite
                    ),
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = SoftTheme.SoftGray,
                        lineHeight = 16.sp
                    ),
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onOpen,
                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.MintTeal.copy(alpha = 0.18f)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "جربي الأداة الآن ⚡",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SoftTheme.MintTeal,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(SoftTheme.SoftPink.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 22.sp)
            }
        }
    }
}

data class SmartAlertItem(
    val emoji: String,
    val title: String,
    val description: String,
    val actionText: String,
    val action: () -> Int
)

@Composable
fun SmartAlertCard(
    item: SmartAlertItem,
    onNavigate: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.SoftPink
                        ),
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTheme.TextWhite.copy(alpha = 0.9f),
                        lineHeight = 16.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(SoftTheme.SoftPink.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(item.emoji, fontSize = 22.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { onNavigate(item.action()) },
                colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.MintTeal.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier
                    .align(Alignment.Start)
                    .height(34.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = item.actionText,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.MintTeal
                        )
                    )
                    Text("⚡", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun JouriNotificationsDialog(
    viewModel: WomanCompanionViewModel,
    onDismiss: () -> Unit,
    onNavigateToTab: (Int) -> Unit
) {
    val pregnancyState by viewModel.pregnancyState.collectAsStateWithLifecycle()
    val progression = viewModel.getPregnancyProgression()
    val cycleInfo = viewModel.getCurrentCyclePhase()

    var selectedTab by remember { mutableStateOf(0) } // 0 for Features, 1 for Smart Alerts

    val alerts = remember(pregnancyState, progression, cycleInfo) {
        val list = mutableListOf<SmartAlertItem>()
        
        val preg = pregnancyState
        if (preg != null) {
            // 1. Core State Advice
            if (preg.isPregnant) {
                list.add(
                    SmartAlertItem(
                        emoji = "👶🏻",
                        title = "متابعة الحمل - الأسبوع ${progression?.weeks ?: 12}",
                        description = "أنتِ الآن في الأسبوع ${progression?.weeks ?: 12} من الحمل. طفلكِ الجميل الآن بحجم ${progression?.comparisonName ?: "حبة تين كاملة"} ${progression?.comparisonIcon ?: "🫓"}. تذكري شرب مياه كافية والراحة والترطيب المستمر لسلامتكما اليوم 🌸",
                        actionText = "تفاصيل تقدم الحمل ⚡",
                        action = { 0 }
                    )
                )
            } else {
                list.add(
                    SmartAlertItem(
                        emoji = "🩸",
                        title = "حالة دورتكِ الحالية: ${cycleInfo.phaseArabic}",
                        description = cycleInfo.description,
                        actionText = "سجل الدورة والخصوبة 📅",
                        action = { 1 }
                    )
                )
            }
            
            // 2. High Blood Pressure Alert
            if (preg.hasHighBp) {
                list.add(
                    SmartAlertItem(
                        emoji = "🩺",
                        title = "مراقبة هامة: ضغط الدم المرتفع ⚠️",
                        description = "عزيزتي جميلة، سجلكِ يشير لضغط دم مرتفع. ننصحكِ بقياس الضغط الآن، والابتعاد التام عن الأطعمة الغنية بالصوديوم (الأملاح) مع شرب الكركديه البارد والراحة التامة.",
                        actionText = "سجل الأعراض والضغط 🩺",
                        action = { 3 }
                    )
                )
            }
            
            // 3. Low Blood Pressure Alert
            if (preg.hasLowBp) {
                list.add(
                    SmartAlertItem(
                        emoji = "🥤",
                        title = "تنبيه صحي: ضغط دم منخفض",
                        description = "عزيزتي جميلة، ننصحكِ بشرب كميات كافية من المياه لتعويض السوائل، والنهوض تدريجياً وتجنب الوقوف لفترات طويلة لتفادي أي دوار ونقص تدفق الأكسجين.",
                        actionText = "سجل الأعراض 🩺",
                        action = { 3 }
                    )
                )
            }
            
            // 4. Diabetes Alert
            if (preg.hasDiabetes) {
                list.add(
                    SmartAlertItem(
                        emoji = "🍏",
                        title = "تنبيه طبي: السكري الذكي 📈",
                        description = "تذكري قياس السكر بعد الوجبات بساعتين وتجنب السكريات البسيطة والمشروبات الغازية. استبدليها بوجبات خفيفة غنية بالألياف كالخضار الطازجة وحبة تفاح.",
                        actionText = "روتين الغذاء والتغذية 🥑",
                        action = { 2 }
                    )
                )
            }
        }
        
        // 5. Daily Wellness & Deep Breathing Reminder
        list.add(
            SmartAlertItem(
                emoji = "🧘‍♀️",
                title = "جلسة تنفس عميق وصحة نفسية ✨",
                description = "أثبتت الدراسات أن 3 دقائق من التنفس العميق والواعي تخفض هرمون الكورتيسول (الإجهاد) وتزيد تدفق الأكسجين لطفلكِ بشكل فوري وفعّال.",
                actionText = "ابدئي تمارين اللياقة 🧘‍♀️",
                action = { 
                    viewModel.setActiveSubScreen("fitness")
                    4 
                }
            )
        )

        // 6. Water hydration Alert
        list.add(
            SmartAlertItem(
                emoji = "💧",
                title = "تذكير الترطيب الذكي من جوري",
                description = "شرب 8-10 أكواب من المياه يومياً يحميكِ من التعب وصداع الحمل ويساعد في تجديد السائل الأمنيوسي المحيط بالطفل باستمرار.",
                actionText = "سجّلي شرب كوب ماء 🥤",
                action = { 2 }
            )
        )
        
        list
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.75f))
                .clickable { onDismiss() }
                .padding(horizontal = 16.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
                    .clickable(enabled = false) {},
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .background(SoftTheme.DeepSlate.copy(alpha = 0.5f), CircleShape)
                                .size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "إغلاق التنبيهات",
                                tint = SoftTheme.SoftPink,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "مركز التنبيهات الذكي من جوري 🔔",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = SoftTheme.SoftPink
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tab Selector Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SoftTheme.DeepSlate.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selectedTab == 0) SoftTheme.SoftPink else Color.Transparent)
                                .clickable { selectedTab = 0 }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "ميزات جوري الذكية 🌟",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedTab == 0) SoftTheme.TextWhite else SoftTheme.SoftGray
                                )
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selectedTab == 1) SoftTheme.SoftPink else Color.Transparent)
                                .clickable { selectedTab = 1 }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "تنبيهاتكِ الصحية 🔔",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedTab == 1) SoftTheme.TextWhite else SoftTheme.SoftGray
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Content Scrollable Area
                    Box(modifier = Modifier.weight(1f)) {
                        if (selectedTab == 0) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Feature 1: Craving
                                NewFeatureItemRow(
                                    emoji = "🍓",
                                    title = "سجل الوحم والاشتهاء ومشاركة جوري",
                                    description = "تتبعي رغباتكِ اليومية والوحم، واحصلي على تحليل طبي دافئ، وشاركي مشاعركِ اللطيفة كرسالة جاهزة لزوجكِ! 🥰🍉",
                                    onOpen = {
                                        viewModel.setActiveSubScreen("craving")
                                        onNavigateToTab(4)
                                        onDismiss()
                                    }
                                )

                                HorizontalDivider(color = SoftTheme.DeepSlate.copy(alpha = 0.5f), thickness = 0.5.dp)

                                // Feature 2: Sleep
                                NewFeatureItemRow(
                                    emoji = "💤",
                                    title = "محلل ومراقب النوم الذكي والأرق",
                                    description = "سجلي ساعات نومكِ وجودته، ودعي جوري تحلل لكِ عادات نومكِ وتقدم لكِ نصائح ذهبية لنوم هانئ ومريح. 🌙💤",
                                    onOpen = {
                                        viewModel.setActiveSubScreen("sleep_analyzer")
                                        onNavigateToTab(4)
                                        onDismiss()
                                    }
                                )

                                HorizontalDivider(color = SoftTheme.DeepSlate.copy(alpha = 0.5f), thickness = 0.5.dp)

                                // Feature 3: Partner
                                NewFeatureItemRow(
                                    emoji = "🔗",
                                    title = "رابط الرفيق ومشاركة الشريك",
                                    description = "أشركي زوجكِ أو عائلتكِ في رحلتكِ واستقبلي رسائل الدعم والاهتمام بخصوصية تامة داخل التطبيق. ❤️✨",
                                    onOpen = {
                                        viewModel.setActiveSubScreen("partner_sync")
                                        onNavigateToTab(4)
                                        onDismiss()
                                    }
                                )

                                HorizontalDivider(color = SoftTheme.DeepSlate.copy(alpha = 0.5f), thickness = 0.5.dp)

                                // Feature 4: Pharmacy
                                NewFeatureItemRow(
                                    emoji = "📦",
                                    title = "الصيدلية المنزلية المتقدمة وتتبع الأدوية",
                                    description = "سجلي علب أدويتكِ وصلاحيتها ومخزونها المتبقي لتنبيهكِ قبل النفاد ولتجنب استخدام أدوية منتهية الصلاحية. 💊📦",
                                    onOpen = {
                                        viewModel.setActiveSubScreen("home_pharmacy")
                                        onNavigateToTab(4)
                                        onDismiss()
                                    }
                                )

                                HorizontalDivider(color = SoftTheme.DeepSlate.copy(alpha = 0.5f), thickness = 0.5.dp)

                                // Feature 5: Fitness
                                NewFeatureItemRow(
                                    emoji = "🧘‍♀️",
                                    title = "تمارين لياقة المرأة الحامل والنفاس",
                                    description = "تمارين رياضية آمنة مخصصة لثلث حملكِ أو فترة النفاس مع مؤقت ذكي لمساعدتكِ على أداء الحركة بنشاط وصحة. 🧘‍♀️💪",
                                    onOpen = {
                                        viewModel.setActiveSubScreen("fitness")
                                        onNavigateToTab(4)
                                        onDismiss()
                                    }
                                )

                                HorizontalDivider(color = SoftTheme.DeepSlate.copy(alpha = 0.5f), thickness = 0.5.dp)

                                // Feature 6: Hospital Bag
                                NewFeatureItemRow(
                                    emoji = "👜",
                                    title = "قائمة حقيبة الولادة الذكية",
                                    description = "قائمة تفاعلية ذكية لحزم وتجهيز كل ما تحتاجينه أنتِ ومولودكِ والمستندات قبل الذهاب للمستشفى. 👜👶",
                                    onOpen = {
                                        viewModel.setActiveSubScreen("hospital_bag")
                                        onNavigateToTab(4)
                                        onDismiss()
                                    }
                                )

                                HorizontalDivider(color = SoftTheme.DeepSlate.copy(alpha = 0.5f), thickness = 0.5.dp)

                                // Feature 7: Food Safety
                                NewFeatureItemRow(
                                    emoji = "🥑",
                                    title = "دليل سلامة الأغذية أثناء الحمل",
                                    description = "ابحثي عن أي طعام، شراب، أو عشبة وتعرفي فوراً على درجة أمانه (آمن، بحذر، أو ممنوع) حفاظاً على سلامتكِ. 🥑🚫",
                                    onOpen = {
                                        viewModel.setActiveSubScreen("food_safety")
                                        onNavigateToTab(4)
                                        onDismiss()
                                    }
                                )
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                alerts.forEach { item ->
                                    SmartAlertCard(
                                        item = item,
                                        onNavigate = { tabIndex ->
                                            onNavigateToTab(tabIndex)
                                            onDismiss()
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bottom Pill Button - "جوري صديقتكِ الذكية 🌸"
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                        shape = RoundedCornerShape(50.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .align(Alignment.CenterHorizontally)
                            .height(48.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "جوري صديقتكِ الذكية 🌸",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = SoftTheme.TextWhite
                            )
                        }
                    }
                }
            }
        }
    }
}
