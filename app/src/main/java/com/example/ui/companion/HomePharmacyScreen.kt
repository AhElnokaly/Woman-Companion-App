package com.example.ui.companion

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.SoftTheme
import com.example.ui.formatGregorianDate
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
