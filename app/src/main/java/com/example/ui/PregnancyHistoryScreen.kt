package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.FetalGrowthLog
import com.example.data.PregnancyEntity
import com.example.viewmodel.WomanCompanionViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PregnancyHistoryScreen(
    viewModel: WomanCompanionViewModel,
    onBack: () -> Unit = {}
) {
    val allPregnancies by viewModel.allPregnanciesState.collectAsStateWithLifecycle()
    val allLogs by viewModel.allFetalGrowthLogsState.collectAsStateWithLifecycle()
    var showStartDialog by remember { mutableStateOf(false) }
    var expandedPregnancyId by remember { mutableStateOf<Int?>(null) }
    val dateFormatter = remember { SimpleDateFormat("yyyy/MM/dd", Locale("ar")) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "سجل الأحمال والولادات 📜",
                            color = SoftTheme.TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "رجوع",
                                tint = SoftTheme.SoftPink
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = SoftTheme.DeepSlate)
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { showStartDialog = true },
                    containerColor = SoftTheme.SoftPink,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("start_new_pregnancy_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "بدء حمل جديد")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("بدء حمل جديد 🌸", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = SoftTheme.DeepSlate,
            modifier = Modifier.testTag("pregnancy_history_screen")
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Info Banner
                Card(
                    colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("🤰", fontSize = 28.sp)
                        Column {
                            Text(
                                text = "سجل متابعة الحمل التراكمي",
                                fontWeight = FontWeight.Bold,
                                color = SoftTheme.TextWhite,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "يمكنكِ الاطلاع على كافة الأحمال السابقة والقياسات المسجلة لكل فترة حمل بشكل مستقل.",
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftTheme.SoftGray
                            )
                        }
                    }
                }

                if (allPregnancies.isEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("📑", fontSize = 40.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "لا يوجد سجل أحمال سابقة محتفظ به",
                                fontWeight = FontWeight.Bold,
                                color = SoftTheme.TextWhite,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "اضغطي على زر 'بدء حمل جديد' لبدء متابعة رحلة حمل جديدة.",
                                color = SoftTheme.SoftGray,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    val sortedPregnancies = remember(allPregnancies) {
                        allPregnancies.sortedByDescending { it.id }
                    }

                    sortedPregnancies.forEach { preg ->
                        val isExpanded = expandedPregnancyId == preg.id
                        val pregLogs = remember(allLogs, preg.id) {
                            allLogs.filter { it.pregnancyId == preg.id }
                        }

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (preg.isActive) SoftTheme.CardSlate else SoftTheme.CardSlate.copy(alpha = 0.8f)
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("pregnancy_history_item_${preg.id}")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            color = if (preg.isActive) SoftTheme.MintTeal.copy(alpha = 0.2f) else SoftTheme.SoftPink.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = if (preg.isActive) "نشط الآن 🤰" else if (preg.isDelivered) "تمت الولادة 👶" else "مكتمل/سابق 👶",
                                                color = if (preg.isActive) SoftTheme.MintTeal else SoftTheme.SoftPink,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }

                                        if (!preg.babyName.isNullOrBlank()) {
                                            Text(
                                                text = preg.babyName,
                                                fontWeight = FontWeight.Bold,
                                                color = SoftTheme.TextWhite,
                                                fontSize = 15.sp
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { expandedPregnancyId = if (isExpanded) null else preg.id }
                                    ) {
                                        Icon(
                                            imageVector = if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                                            contentDescription = "التفاصيل",
                                            tint = SoftTheme.SoftGray
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "بداية الحمل (LMP):",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SoftTheme.SoftGray
                                        )
                                        Text(
                                            text = preg.lastPeriodDate?.let { dateFormatter.format(Date(it)) } ?: "غير محدد",
                                            fontWeight = FontWeight.Medium,
                                            color = SoftTheme.TextWhite,
                                            fontSize = 13.sp
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = if (preg.isDelivered) "تاريخ الولادة:" else "تاريخ الولادة المتوقع:",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SoftTheme.SoftGray
                                        )
                                        Text(
                                            text = if (preg.isDelivered && preg.birthDate != null) {
                                                dateFormatter.format(Date(preg.birthDate))
                                            } else preg.dueDate?.let { dateFormatter.format(Date(it)) } ?: "غير محدد",
                                            fontWeight = FontWeight.Medium,
                                            color = SoftTheme.GoldFasting,
                                            fontSize = 13.sp
                                        )
                                    }
                                }

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { expandedPregnancyId = if (isExpanded) null else preg.id }
                                        .padding(top = 10.dp)
                                ) {
                                    Text(
                                        text = if (isExpanded) "إخفاء قياسات السونار 🔼" else "عرض قياسات السونار (${pregLogs.size}) 🔽",
                                        color = SoftTheme.SoftPink,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                AnimatedVisibility(visible = isExpanded) {
                                    Column(modifier = Modifier.padding(top = 12.dp)) {
                                        HorizontalDivider(color = SoftTheme.DeepSlate, thickness = 1.dp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        if (pregLogs.isEmpty()) {
                                            Text(
                                                text = "لم يتم تسجيل أي قياسات سونار لهذا الحمل.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = SoftTheme.SoftGray,
                                                modifier = Modifier.padding(vertical = 4.dp)
                                            )
                                        } else {
                                            pregLogs.sortedBy { it.pregnancyWeek }.forEach { log ->
                                                Card(
                                                    colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate),
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 4.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(10.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Column {
                                                            Text(
                                                                text = "الأسبوع ${log.pregnancyWeek}",
                                                                fontWeight = FontWeight.Bold,
                                                                color = SoftTheme.TextWhite,
                                                                fontSize = 13.sp
                                                            )
                                                            if (!log.notes.isNullOrBlank()) {
                                                                Text(
                                                                    text = log.notes,
                                                                    color = SoftTheme.SoftGray,
                                                                    fontSize = 11.sp
                                                                )
                                                            }
                                                        }

                                                        Text(
                                                            text = "الوزن: ${log.weightGrams.toInt()} جم | الطول: ${log.lengthCm} سم",
                                                            color = SoftTheme.MintTeal,
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showStartDialog) {
            StartNewPregnancyDialog(
                onDismiss = { showStartDialog = false },
                onConfirm = { lastPeriodDateMs, babyName ->
                    viewModel.startNewPregnancy(lastPeriodDateMs, babyName)
                    showStartDialog = false
                }
            )
        }
    }
}
