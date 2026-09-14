package com.example.ui.pregnancy

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.FetalGrowthLog
import com.example.data.MaternalWeightLog
import com.example.data.PregnancyEntity
import com.example.ui.SoftTheme
import com.example.ui.fetal.FetalStandardData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MaternalWeightTrackerCard(
    pregnancy: PregnancyEntity?,
    currentWeekNumber: Int,
    maternalWeightLogs: List<MaternalWeightLog>,
    latestFetalLog: FetalGrowthLog?,
    onAddMaternalWeight: (week: Int, weightKg: Double, notes: String?) -> Unit,
    onDeleteMaternalWeight: (MaternalWeightLog) -> Unit,
    onUpdatePrePregnancyWeight: (preWeightKg: Double?, heightCm: Double?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditPreWeightDialog by remember { mutableStateOf(false) }
    var isBreakdownExpanded by remember { mutableStateOf(false) }
    var isHistoryExpanded by remember { mutableStateOf(false) }

    val preWeight = pregnancy?.prePregnancyWeight
    val heightCm = pregnancy?.heightCm
    val activePregId = pregnancy?.id ?: 0
    val filteredLogs = remember(maternalWeightLogs, activePregId) {
        maternalWeightLogs.filter { it.pregnancyId == activePregId }.sortedByDescending { it.pregnancyWeek }
    }
    val latestLog = filteredLogs.firstOrNull()

    // Determine current effective maternal weight and total gain
    val currentMaternalWeight = latestLog?.weightKg ?: preWeight
    val actualGain = if (currentMaternalWeight != null && preWeight != null) {
        Math.round((currentMaternalWeight - preWeight) * 10.0) / 10.0
    } else null

    val bmiCategory = remember(preWeight, heightCm) {
        MaternalWeightStandardData.determineBmiCategory(preWeight, heightCm)
    }
    val recommendedRange = remember(currentWeekNumber, bmiCategory) {
        MaternalWeightStandardData.getRecommendedGainRange(currentWeekNumber, bmiCategory)
    }
    val evaluation = remember(currentMaternalWeight, preWeight, currentWeekNumber, bmiCategory) {
        if (currentMaternalWeight != null && preWeight != null) {
            MaternalWeightStandardData.evaluateGain(
                currentWeightKg = currentMaternalWeight,
                prePregnancyWeightKg = preWeight,
                week = currentWeekNumber,
                category = bmiCategory
            )
        } else null
    }

    // Fetal Weight reference
    val fetalWeightGrams = latestFetalLog?.weightGrams ?: FetalStandardData.getStandardForWeek(currentWeekNumber).weightGrams
    val fetalWeightKg = Math.round((fetalWeightGrams / 1000.0) * 100.0) / 100.0

    // Weight Breakdown
    val breakdown = remember(currentWeekNumber, actualGain) {
        MaternalWeightStandardData.calculateWeightBreakdown(currentWeekNumber, actualGain)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("maternal_weight_tracker_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardBg),
        border = BorderStroke(1.dp, SoftTheme.CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // --- Header Row ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SoftTheme.MintAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Scale,
                            contentDescription = null,
                            tint = SoftTheme.EmeraldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "متابعة وزن الأم والزيادة الصحية",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.TextPrimary
                        )
                        Text(
                            text = "مقارنة علمية (IOM) بين وزنكِ ونمو طفلكِ 👶",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = SoftTheme.TextSecondaryMuted
                        )
                    }
                }

                Button(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SoftTheme.EmeraldPrimary,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("btn_log_maternal_weight")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "سجل وزناً", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            // --- Pre-Pregnancy Weight Notice if missing ---
            if (preWeight == null || preWeight <= 0.0) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = SoftTheme.MintAccent.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, SoftTheme.EmeraldPrimary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "لم يتم تحديد وزنكِ قبل الحمل",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = SoftTheme.TealDark
                            )
                            Text(
                                text = "أدخلي وزنكِ قبل الحمل لنحسب لكِ النطاق الصحي المخصص بدقة.",
                                fontSize = 11.sp,
                                color = SoftTheme.TextSecondaryMuted
                            )
                        }
                        TextButton(
                            onClick = { showEditPreWeightDialog = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = SoftTheme.EmeraldPrimary)
                        ) {
                            Text(text = "تحديد الآن", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                // --- Main Weight Snapshot & Status Banner ---
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SoftTheme.CanvasBg,
                    border = BorderStroke(1.dp, SoftTheme.CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Current Weight
                            Column {
                                Text(
                                    text = "الوزن الحالي (أسبوع $currentWeekNumber)",
                                    fontSize = 11.sp,
                                    color = SoftTheme.TextSecondaryMuted
                                )
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = if (currentMaternalWeight != null) String.format(Locale.US, "%.1f", currentMaternalWeight) else "--",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SoftTheme.TextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "كجم",
                                        fontSize = 12.sp,
                                        color = SoftTheme.TextSecondaryMuted,
                                        modifier = Modifier.padding(bottom = 3.dp)
                                    )
                                }
                            }

                            // Total Gain
                            Column(horizontalAlignment = Alignment.End) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "الزيادة منذ بداية الحمل",
                                        fontSize = 11.sp,
                                        color = SoftTheme.TextSecondaryMuted
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "تعديل وزن ما قبل الحمل",
                                        tint = SoftTheme.EmeraldPrimary.copy(alpha = 0.7f),
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clickable { showEditPreWeightDialog = true }
                                    )
                                }
                                val gainPrefix = if (actualGain != null && actualGain > 0) "+" else ""
                                Text(
                                    text = if (actualGain != null) "$gainPrefix${String.format(Locale.US, "%.1f", actualGain)} كجم" else "--",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        actualGain == null -> SoftTheme.TextPrimary
                                        actualGain in recommendedRange -> SoftTheme.EmeraldPrimary
                                        actualGain < recommendedRange.start -> SoftTheme.GoldFasting
                                        else -> SoftTheme.WarmCoral
                                    }
                                )
                                Text(
                                    text = "قبل الحمل: ${preWeight.toInt()} كجم",
                                    fontSize = 10.sp,
                                    color = SoftTheme.TextSecondaryMuted
                                )
                            }
                        }

                        // Status Badge & Advice Message
                        if (evaluation != null) {
                            val badgeBg = when (evaluation.status) {
                                WeightGainStatus.OPTIMAL -> SoftTheme.MintAccent
                                WeightGainStatus.BELOW_RECOMMENDED -> SoftTheme.GoldFasting.copy(alpha = 0.12f)
                                WeightGainStatus.ABOVE_RECOMMENDED -> SoftTheme.WarmCoral.copy(alpha = 0.12f)
                            }
                            val badgeColor = when (evaluation.status) {
                                WeightGainStatus.OPTIMAL -> SoftTheme.EmeraldPrimary
                                WeightGainStatus.BELOW_RECOMMENDED -> SoftTheme.GoldFasting
                                WeightGainStatus.ABOVE_RECOMMENDED -> SoftTheme.WarmCoral
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = badgeBg,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = evaluation.statusTitle,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = badgeColor
                                        )
                                        Text(
                                            text = "• النطاق الموصى به: ${evaluation.minRecommendedGainKg} - ${evaluation.maxRecommendedGainKg} كجم",
                                            fontSize = 10.sp,
                                            color = SoftTheme.TextSecondaryMuted
                                        )
                                    }
                                    Text(
                                        text = evaluation.adviceMessage,
                                        fontSize = 11.sp,
                                        color = SoftTheme.TextPrimary,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // --- Dual Comparison: Mother Gain vs Baby Weight ---
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = SoftTheme.MintAccent.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, SoftTheme.CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "👶 وزن طفلكِ", fontSize = 11.sp, color = SoftTheme.TextSecondaryMuted)
                            Text(
                                text = if (fetalWeightGrams >= 1000) "${String.format(Locale.US, "%.2f", fetalWeightKg)} كجم" else "${fetalWeightGrams.toInt()} جم",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = SoftTheme.EmeraldPrimary
                            )
                            Text(
                                text = if (latestFetalLog != null) "من آخر فحص سونار" else "تقديري للأسبوع $currentWeekNumber",
                                fontSize = 9.sp,
                                color = SoftTheme.TextSecondaryMuted
                            )
                        }

                        VerticalDivider(modifier = Modifier.height(36.dp), color = SoftTheme.CardBorder)

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "⚖️ زيادة وزنكِ الكلية", fontSize = 11.sp, color = SoftTheme.TextSecondaryMuted)
                            Text(
                                text = if (actualGain != null) "${String.format(Locale.US, "%.1f", actualGain)} كجم" else "--",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = SoftTheme.TextPrimary
                            )
                            val babyPct = if (actualGain != null && actualGain > 0.1) {
                                ((fetalWeightKg / actualGain) * 100).toInt().coerceIn(1, 100)
                            } else null
                            Text(
                                text = if (babyPct != null) "وزن الطفل يمثل ~$babyPct%" else "يشمل السائل والمشيمة",
                                fontSize = 9.sp,
                                color = SoftTheme.TealDark
                            )
                        }
                    }
                }
            }

            // --- Expandable: Where Does Pregnancy Weight Go? (توزيع الوزن) ---
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = SoftTheme.CanvasBg,
                border = BorderStroke(1.dp, SoftTheme.CardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isBreakdownExpanded = !isBreakdownExpanded }
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = SoftTheme.EmeraldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "أين تذهب الكيلوجرامات في الأسبوع $currentWeekNumber؟",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = SoftTheme.TextPrimary
                            )
                        }
                        Icon(
                            imageVector = if (isBreakdownExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = SoftTheme.TextSecondaryMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    AnimatedVisibility(
                        visible = isBreakdownExpanded,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "وزن الحمل ليس مجرد دهون! إليكِ التوزيع الطبيعي الفسيولوجي لكامل أعضاء الرحلة المباركة:",
                                fontSize = 11.sp,
                                color = SoftTheme.TextSecondaryMuted,
                                lineHeight = 15.sp
                            )

                            WeightItemRow(icon = "👶", title = "وزن طفلكِ الحبيب", value = "${String.format(Locale.US, "%.2f", breakdown.babyKg)} كجم", color = SoftTheme.EmeraldPrimary)
                            WeightItemRow(icon = "🩺", title = "المشيمة (رئة ومغذي الجنين)", value = "${String.format(Locale.US, "%.2f", breakdown.placentaKg)} كجم")
                            WeightItemRow(icon = "💧", title = "السائل السلوي (لحماية الجنين)", value = "${String.format(Locale.US, "%.2f", breakdown.amnioticFluidKg)} كجم")
                            WeightItemRow(icon = "🫀", title = "زيادة حجم الدم وسوائل الجسم", value = "${String.format(Locale.US, "%.2f", breakdown.bloodAndFluidsKg)} كجم")
                            WeightItemRow(icon = "🤱", title = "تضخم الرحم وأنسجة الثدي", value = "${String.format(Locale.US, "%.2f", breakdown.uterusAndBreastsKg)} كجم")
                            WeightItemRow(icon = "🌿", title = "مخزون طبيعي للرضاعة والطاقة", value = "${String.format(Locale.US, "%.1f", breakdown.maternalStoresKg)} كجم")
                        }
                    }
                }
            }

            // --- Expandable: Past Logged Weights ---
            if (filteredLogs.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = SoftTheme.CanvasBg,
                    border = BorderStroke(1.dp, SoftTheme.CardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isHistoryExpanded = !isHistoryExpanded }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "سجل الأوزان السابقة (${filteredLogs.size})",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = SoftTheme.TextPrimary
                            )
                            Icon(
                                imageVector = if (isHistoryExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = SoftTheme.TextSecondaryMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        AnimatedVisibility(
                            visible = isHistoryExpanded,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                filteredLogs.forEach { log ->
                                    val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(log.date))
                                    val logGain = if (preWeight != null) log.weightKg - preWeight else null
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = SoftTheme.CardBg,
                                        border = BorderStroke(1.dp, SoftTheme.CardBorder),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = "الأسبوع ${log.pregnancyWeek} • ${log.weightKg} كجم",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = SoftTheme.TextPrimary
                                                )
                                                if (!log.notes.isNullOrBlank()) {
                                                    Text(text = log.notes, fontSize = 10.sp, color = SoftTheme.TextSecondaryMuted)
                                                }
                                                Text(text = dateStr, fontSize = 9.sp, color = SoftTheme.TextSecondaryMuted)
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (logGain != null) {
                                                    val gainTxt = if (logGain >= 0) "+${String.format(Locale.US, "%.1f", logGain)}" else String.format(Locale.US, "%.1f", logGain)
                                                    Text(
                                                        text = "$gainTxt كجم",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = SoftTheme.EmeraldPrimary
                                                    )
                                                }
                                                IconButton(
                                                    onClick = { onDeleteMaternalWeight(log) },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "حذف القياس",
                                                        tint = SoftTheme.RedDanger.copy(alpha = 0.7f),
                                                        modifier = Modifier.size(16.dp)
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

    // --- Dialog: Add New Maternal Weight ---
    if (showAddDialog) {
        AddMaternalWeightDialog(
            currentWeekNumber = currentWeekNumber,
            initialWeight = currentMaternalWeight ?: 60.0,
            onDismiss = { showAddDialog = false },
            onConfirm = { week, weight, notes ->
                onAddMaternalWeight(week, weight, notes)
                showAddDialog = false
            }
        )
    }

    // --- Dialog: Edit Pre-Pregnancy Weight & Height ---
    if (showEditPreWeightDialog) {
        EditPrePregnancyWeightDialog(
            initialPreWeight = preWeight,
            initialHeightCm = heightCm,
            onDismiss = { showEditPreWeightDialog = false },
            onConfirm = { newWeight, newHeight ->
                onUpdatePrePregnancyWeight(newWeight, newHeight)
                showEditPreWeightDialog = false
            }
        )
    }
}

@Composable
private fun WeightItemRow(
    icon: String,
    title: String,
    value: String,
    color: Color = SoftTheme.TextPrimary
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = icon, fontSize = 13.sp)
            Text(text = title, fontSize = 11.sp, color = SoftTheme.TextPrimary)
        }
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMaternalWeightDialog(
    currentWeekNumber: Int,
    initialWeight: Double,
    onDismiss: () -> Unit,
    onConfirm: (week: Int, weightKg: Double, notes: String?) -> Unit
) {
    var weekInput by remember { mutableStateOf(currentWeekNumber.toString()) }
    var weightInput by remember { mutableStateOf(if (initialWeight > 0) String.format(Locale.US, "%.1f", initialWeight) else "") }
    var notesInput by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .testTag("dialog_add_maternal_weight"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardBg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تسجيل وزن الأم الحالي ⚖️",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.TextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Text("✕", fontSize = 16.sp, color = SoftTheme.TextSecondaryMuted)
                    }
                }

                Text(
                    text = "يُفضل قياس الوزن في الصباح بعد الاستيقاظ مباشرة للحصول على أدق قراءة منتظمة.",
                    fontSize = 11.sp,
                    color = SoftTheme.TextSecondaryMuted,
                    lineHeight = 15.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = weekInput,
                        onValueChange = { weekInput = it },
                        label = { Text("أسبوع الحمل") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { weightInput = it },
                        label = { Text("الوزن (كجم)") },
                        modifier = Modifier.weight(1.3f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("ملاحظات (اختياري، مثلاً بعد الإفطار)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (errorMsg != null) {
                    Text(
                        text = errorMsg ?: "",
                        color = SoftTheme.RedDanger,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("إلغاء")
                    }

                    Button(
                        onClick = {
                            val w = weightInput.toDoubleOrNull()
                            val wk = weekInput.toIntOrNull()
                            if (w == null || w <= 25.0 || w >= 250.0) {
                                errorMsg = "يرجى إدخال وزن صحيح بالكيلوجرام (بين 25 و 250 كجم)"
                            } else if (wk == null || wk !in 1..44) {
                                errorMsg = "يرجى إدخال أسبوع حمل بين 1 و 44"
                            } else {
                                onConfirm(wk, w, notesInput.trim().ifEmpty { null })
                            }
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("btn_confirm_add_maternal_weight"),
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.EmeraldPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("حفظ القياس ✓", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun EditPrePregnancyWeightDialog(
    initialPreWeight: Double?,
    initialHeightCm: Double?,
    onDismiss: () -> Unit,
    onConfirm: (preWeightKg: Double?, heightCm: Double?) -> Unit
) {
    var weightStr by remember { mutableStateOf(initialPreWeight?.let { String.format(Locale.US, "%.1f", it) } ?: "") }
    var heightStr by remember { mutableStateOf(initialHeightCm?.let { it.toInt().toString() } ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .testTag("dialog_edit_pre_weight"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardBg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "تعديل وزنكِ وطولكِ قبل الحمل 📏",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SoftTheme.TextPrimary
                )

                Text(
                    text = "يُستخدم وزن ما قبل الحمل والطول لحساب مؤشر كتلة الجسم (BMI) ومنحنى الزيادة الصحية الموصى بها.",
                    fontSize = 11.sp,
                    color = SoftTheme.TextSecondaryMuted,
                    lineHeight = 15.sp
                )

                OutlinedTextField(
                    value = weightStr,
                    onValueChange = { weightStr = it },
                    label = { Text("الوزن قبل الحمل (كجم)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = heightStr,
                    onValueChange = { heightStr = it },
                    label = { Text("الطول (سم)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                if (error != null) {
                    Text(text = error ?: "", color = SoftTheme.RedDanger, fontSize = 11.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("إلغاء")
                    }

                    Button(
                        onClick = {
                            val w = weightStr.toDoubleOrNull()
                            val h = heightStr.toDoubleOrNull()
                            if (w != null && (w < 25.0 || w > 250.0)) {
                                error = "يرجى إدخال وزن منطقي (بين 25 و 250)"
                            } else if (h != null && (h < 100.0 || h > 220.0)) {
                                error = "يرجى إدخال طول منطقي (بين 100 و 220 سم)"
                            } else {
                                onConfirm(w, h)
                            }
                        },
                        modifier = Modifier.weight(1.5f),
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.EmeraldPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("حفظ البيانات ✓", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
