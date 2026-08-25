package com.example.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ContraceptiveMethod
import com.example.data.ContraceptiveSymptomContext
import com.example.data.SymptomLog
import com.example.viewmodel.WomanCompanionViewModel
import java.text.SimpleDateFormat
import java.util.*

data class CorrelatedSymptomItem(
    val symptom: SymptomLog,
    val contextInfo: ContraceptiveSymptomContext
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContraceptiveScreen(
    viewModel: WomanCompanionViewModel,
    onBackClick: () -> Unit = {}
) {
    val activeMethod by viewModel.activeContraceptiveMethodState.collectAsStateWithLifecycle()
    val allMethods by viewModel.allContraceptiveMethodsState.collectAsStateWithLifecycle()
    val symptomLogs by viewModel.symptomLogsState.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEndConfirmDialog by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }

    val correlatedSymptoms = remember(symptomLogs, allMethods) {
        symptomLogs.mapNotNull { symptom ->
            val contextInfo = viewModel.getContraceptiveContextForSymptom(symptom.date)
            if (contextInfo != null) CorrelatedSymptomItem(symptom, contextInfo) else null
        }.sortedByDescending { it.symptom.date }
    }

    val pastMethods = remember(allMethods) { allMethods.filter { it.endDate != null } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "تتبع وسيلة منع الحمل 🛡️",
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.TextWhite
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = SoftTheme.TextWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SoftTheme.DeepSlate)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = SoftTheme.MintTeal,
                contentColor = Color.Black,
                modifier = Modifier.testTag("add_contraceptive_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة وسيلة جديدة")
            }
        },
        containerColor = SoftTheme.DeepSlate
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp, top = 8.dp)
        ) {
            // --- 1. Active Method Card ---
            item {
                Text(
                    "الوسيلة النشطة حالياً 🌸",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = SoftTheme.MintTeal
                )
            }

            item {
                val currentActive = activeMethod
                if (currentActive != null) {
                    val daysActive = viewModel.calculateDaysBetween(currentActive.startDate, System.currentTimeMillis())
                    val typeName = viewModel.getContraceptiveTypeName(currentActive.type)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("active_contraceptive_card"),
                        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.5.dp, SoftTheme.MintTeal.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(SoftTheme.MintTeal.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("🛡️", fontSize = 20.sp)
                                    }
                                    Column {
                                        Text(
                                            typeName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = SoftTheme.TextWhite
                                        )
                                        Text(
                                            "تاريخ البدء: ${dateFormat.format(Date(currentActive.startDate))}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SoftTheme.SoftGray
                                        )
                                    }
                                }

                                Surface(
                                    color = SoftTheme.MintTeal.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        "نشطة منذ $daysActive يوماً",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = SoftTheme.MintTeal
                                    )
                                }
                            }

                            if (!currentActive.notes.isNullOrBlank()) {
                                Text(
                                    "ملاحظات: ${currentActive.notes}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SoftTheme.TextWhite
                                )
                            }

                            ContraceptiveReferenceInfoCard(type = currentActive.type)

                            Button(
                                onClick = { showEndConfirmDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("end_contraceptive_btn")
                            ) {
                                Text("إنهاء استخدام الوسيلة 🛑", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, SoftTheme.SoftGray.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("🛡️", fontSize = 32.sp)
                            Text(
                                "لا توجد وسيلة منع حمل نشطة حالياً",
                                fontWeight = FontWeight.Bold,
                                color = SoftTheme.TextWhite
                            )
                            Text(
                                "يمكنكِ تسجيل وسيلتك الحالية لمتابعة مدتها والأعراض المصاحبة لها.",
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftTheme.SoftGray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = { showAddDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.MintTeal)
                            ) {
                                Text("إضافة وسيلة جديدة ➕", fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                        }
                    }
                }
            }

            // --- 2. Task 3: Symptoms Linked to Contraceptive Timeline ---
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "الأعراض المسجلة مع خطة وسيلة منع الحمل 📊",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = SoftTheme.SoftPink
                )
            }

            if (correlatedSymptoms.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            "لم يتم تسجيل أعراض بالتزامن مع استخدام وسيلة منع الحمل حتى الآن.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTheme.SoftGray
                        )
                    }
                }
            } else {
                items(
                    items = correlatedSymptoms,
                    key = { item -> item.symptom.id }
                ) { item ->
                    val symptom = item.symptom
                    val contextInfo = item.contextInfo

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("correlated_symptom_card_${symptom.id}"),
                        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    symptom.symptom,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = SoftTheme.TextWhite
                                )
                                Text(
                                    dateFormat.format(Date(symptom.date)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SoftTheme.SoftGray
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = SoftTheme.SoftPink.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        contextInfo.formattedLabel,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = SoftTheme.SoftPink
                                    )
                                }

                                Text(
                                    "شدة العرض: ${symptom.severity}/10",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SoftTheme.SoftGray
                                )
                            }
                        }
                    }
                }
            }

            // --- 3. Past Methods List ---
            if (pastMethods.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "الوسائل السابقة 📜",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = SoftTheme.TextWhite
                    )
                }

                items(
                    items = pastMethods,
                    key = { past -> past.id }
                ) { past ->
                    val typeName = viewModel.getContraceptiveTypeName(past.type)
                    val durationDays = viewModel.calculateDaysBetween(past.startDate, past.endDate ?: past.startDate)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("past_contraceptive_card_${past.id}"),
                        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate.copy(alpha = 0.7f)),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, SoftTheme.SoftGray.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    typeName,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftTheme.TextWhite
                                )
                                Text(
                                    "من ${dateFormat.format(Date(past.startDate))} إلى ${dateFormat.format(Date(past.endDate ?: past.startDate))}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SoftTheme.SoftGray
                                )
                            }

                            Surface(
                                color = SoftTheme.SoftGray.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "$durationDays يوماً",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 12.sp,
                                    color = SoftTheme.SoftGray
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // --- End Active Method Confirmation Dialog ---
    if (showEndConfirmDialog) {
        activeMethod?.let { currentMethod ->
            AlertDialog(
                onDismissRequest = { showEndConfirmDialog = false },
                title = { Text("تأكيد إنهاء استخدام الوسيلة 🛑", fontWeight = FontWeight.Bold, color = SoftTheme.SoftPink) },
                text = {
                    Text(
                        "هل أنتِ متأكدة من التوقف عن استخدام ${viewModel.getContraceptiveTypeName(currentMethod.type)}؟ سيتم حفظ السجل في قائمة الوسائل السابقة.",
                        color = SoftTheme.TextWhite
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.endContraceptiveMethod(currentMethod.id)
                            showEndConfirmDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink)
                    ) {
                        Text("تأكيد الإنهاء", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEndConfirmDialog = false }) {
                        Text("إلغاء", color = SoftTheme.SoftGray)
                    }
                },
                containerColor = SoftTheme.DeepSlate
            )
        }
    }

    // --- Add Method Dialog ---
    if (showAddDialog) {
        AddContraceptiveDialog(
            viewModel = viewModel,
            onDismiss = { showAddDialog = false },
            onConfirm = { type, startDateMs, notes ->
                viewModel.addContraceptiveMethod(type, startDateMs, notes)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AddContraceptiveDialog(
    viewModel: WomanCompanionViewModel,
    onDismiss: () -> Unit,
    onConfirm: (type: String, startDateMs: Long, notes: String?) -> Unit
) {
    val context = LocalContext.current
    var selectedType by remember { mutableStateOf("PILL") }
    var startDateMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var notesText by remember { mutableStateOf("") }

    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }

    val typesList = remember {
        listOf(
            "PILL" to "حبوب منع الحمل 💊",
            "HORMONAL_IUD" to "اللولب الهرموني 🛡️",
            "COPPER_IUD" to "اللولب النحاسي ⚓",
            "INJECTION" to "حقنة منع الحمل 💉",
            "IMPLANT" to "شريحة تحت الجلد 🩹",
            "CONDOM" to "الواقي الذكري 🛑",
            "OTHER" to "وسيلة أخرى 🌿"
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تسجيل وسيلة منع حمل جديدة 🛡️", fontWeight = FontWeight.Bold, color = SoftTheme.MintTeal) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("اختر نوع الوسيلة:", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)

                // Type Chips Selection
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    typesList.chunked(2).forEach { rowPair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rowPair.forEach { pair ->
                                val typeKey = pair.first
                                val label = pair.second
                                FilterChip(
                                    selected = (selectedType == typeKey),
                                    onClick = { selectedType = typeKey },
                                    label = { Text(label, fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SoftTheme.MintTeal,
                                        selectedLabelColor = Color.Black,
                                        containerColor = SoftTheme.CardSlate,
                                        labelColor = SoftTheme.TextWhite
                                    )
                                )
                            }
                            if (rowPair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                // Reference Guidance Info Card
                ContraceptiveReferenceInfoCard(type = selectedType)

                // Date Picker trigger
                OutlinedButton(
                    onClick = {
                        val cal = Calendar.getInstance().apply { timeInMillis = startDateMs }
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val selectedCal = Calendar.getInstance().apply {
                                    set(year, month, dayOfMonth)
                                }
                                startDateMs = selectedCal.timeInMillis
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftTheme.MintTeal)
                ) {
                    Text("تاريخ البدء: ${dateFormat.format(Date(startDateMs))}")
                }

                // Notes text field
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("ملاحظات إضافية (اختياري)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SoftTheme.MintTeal,
                        unfocusedBorderColor = SoftTheme.SoftGray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(selectedType, startDateMs, notesText.ifBlank { null })
                },
                colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.MintTeal)
            ) {
                Text("حفظ الوسيلة", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = SoftTheme.SoftGray)
            }
        },
        containerColor = SoftTheme.DeepSlate
    )
}

// AHMED-REVIEW: Medical guidance text for contraceptive methods needs final physician review
@Composable
fun ContraceptiveReferenceInfoCard(type: String) {
    val guidanceText = remember(type) {
        when (type) {
            "PILL" -> "حبوب منع الحمل: وسيلة هرمونية يومية. يتطلب الحفاظ على الفاعلية الالتزام بالموعد اليومي المعتاد."
            "HORMONAL_IUD" -> "اللولب الهرموني: وسيلة طويلة المدى يركبها الطبيب. قد يقلل من غزوات الدورة وآلامها بالتدريج."
            "COPPER_IUD" -> "اللولب النحاسي: وسيلة خالية من الهرمونات تدوم لسنوات. قد يسبب زيادة مؤقتة في تدفق الدورة أو المغص في الشهور الأولى."
            "INJECTION" -> "حقنة منع الحمل: وسيلة هرمونية تؤخذ كل 3 أشهر مع متابعة من الطبيبة."
            "IMPLANT" -> "شريحة منع الحمل: كبسولة هرمونية صغيرة تزرع تحت الجلد وتوفر حماية مستمرة لعدة سنوات."
            "CONDOM" -> "الواقي الذكري: وسيلة عازلة خالية من الهرمونات للحماية الموضعية المباشرة."
            else -> "وسيلة أخرى: يوصى دائماً بمراجعة وتوجيهات الطبيبة المعالجة لضمان السلامة والفاعلية."
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate.copy(alpha = 0.8f)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, SoftTheme.MintTeal.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = "معلومات استرشادية",
                tint = SoftTheme.MintTeal,
                modifier = Modifier.size(20.dp)
            )
            Text(
                guidanceText,
                style = MaterialTheme.typography.bodySmall,
                color = SoftTheme.TextWhite.copy(alpha = 0.9f),
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }
    }
}
