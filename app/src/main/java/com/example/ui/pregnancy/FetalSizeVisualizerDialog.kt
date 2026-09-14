package com.example.ui.pregnancy

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.FetalStandardData
import com.example.ui.SoftTheme

enum class ComparisonCategory(val label: String, val icon: String) {
    FRUITS("فواكه وخضار", "🍇"),
    EGYPTIAN("أكلات مصرية", "🥭"),
    SWEET_THINGS("أشياء لطيفة", "🧸")
}

data class FetalComparisonItem(
    val category: ComparisonCategory,
    val title: String,
    val icon: String,
    val description: String
)

/**
 * نافذة تفاعلية تتيح للأم مقارنة حجم الجنين بعدة تصنيفات ممتعة (فواكه، أكلات مصرية، ألعاب لطيفة)
 * بتصميم متناسق مع SoftTheme وربط ذكي مع القياسات الطبية الفعلية أو المرجعية.
 */
@Composable
fun FetalSizeVisualizerDialog(
    weekNumber: Int,
    babySizeFruit: String,
    onDismiss: () -> Unit,
    babyWeightGrams: String = "",
    babyLengthCm: String = "",
    isRealUltrasoundData: Boolean = false
) {
    var selectedCategory by remember { mutableStateOf(ComparisonCategory.FRUITS) }

    val comparisonData = remember(weekNumber, babySizeFruit) {
        mapOf(
            ComparisonCategory.FRUITS to FetalComparisonItem(
                category = ComparisonCategory.FRUITS,
                title = babySizeFruit.ifBlank { "حبة رمان يانعة" },
                icon = "🍎",
                description = "الجنين في الأسبوع $weekNumber يضاهي تقريباً حجم $babySizeFruit اللذيذة والغنية بالفوائد والبركة!"
            ),
            ComparisonCategory.EGYPTIAN to FetalComparisonItem(
                category = ComparisonCategory.EGYPTIAN,
                title = when (weekNumber) {
                    in 1..8 -> "حبة حمص مصري 🧆"
                    in 9..13 -> "ليمونة أضاليا معصفرة 🍋"
                    in 14..18 -> "مانجو عويس سكرية 🥭"
                    in 19..23 -> "كوز ذرة مشوي 🌽"
                    in 24..28 -> "حبة باذنجان رومي كبير 🍆"
                    in 29..34 -> "حبة شمام إسماعيلاوي حلو 🍈"
                    else -> "بطيخة صيفي بلدي حمرا 🍉"
                },
                icon = "🇪🇬",
                description = "بتشبيه بلدي مصري أصيل ومبهج، طفلكِ الآن تقريباً في حجم هذه الأكلة الطيبة المحببة!"
            ),
            ComparisonCategory.SWEET_THINGS to FetalComparisonItem(
                category = ComparisonCategory.SWEET_THINGS,
                title = when (weekNumber) {
                    in 1..12 -> "لعبة دبدوب صغيرة 🧸"
                    in 13..24 -> "كتاب قصص أطفال ملون 📖"
                    in 25..32 -> "أرنوب ناعم محشو 🐰"
                    else -> "وسادة عناق دافئة ☁️"
                },
                icon = "✨",
                description = "حجم يملأ القلب بالدفء والسكينة، يقترب يوماً بعد يوم من لحظة احتضانه بين يديكِ سالماً معافى!"
            )
        )
    }

    val currentItem = comparisonData[selectedCategory] ?: comparisonData.values.first()

    // Retrieve standard data if user hasn't provided custom measurements
    val standard = remember(weekNumber) { FetalStandardData.getStandardForWeek(weekNumber) }
    val displayWeight = when {
        babyWeightGrams.isNotBlank() -> babyWeightGrams
        standard != null -> "~${standard.weightGrams.toInt()} جم"
        else -> "~${(weekNumber * 45)} جم"
    }
    val displayLength = when {
        babyLengthCm.isNotBlank() -> babyLengthCm
        standard != null -> "~${standard.lengthCm} سم"
        else -> "~${(weekNumber * 1.1).toInt()} سم"
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardBg),
            border = BorderStroke(1.dp, SoftTheme.CardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("fetal_size_visualizer_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
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
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SoftTheme.MintAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🔬", fontSize = 18.sp)
                        }
                        Column {
                            Text(
                                text = "مقارنة حجم الجنين 🌸",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SoftTheme.TextPrimary
                            )
                            Text(
                                text = "الأسبوع $weekNumber من الحمل",
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftTheme.EmeraldPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = SoftTheme.TextSecondaryMuted
                        )
                    }
                }

                // Category Selector Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ComparisonCategory.values().forEach { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) SoftTheme.EmeraldPrimary else SoftTheme.CanvasBg,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) SoftTheme.EmeraldPrimary else SoftTheme.CardBorder
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedCategory = cat }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(cat.icon, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = cat.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) SoftTheme.TextWhite else SoftTheme.TextPrimary
                                )
                            }
                        }
                    }
                }

                // Visual Representation Center Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SoftTheme.CanvasBg),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, SoftTheme.CardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .background(SoftTheme.MintAccent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(currentItem.icon, fontSize = 40.sp)
                        }

                        Text(
                            text = currentItem.title,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SoftTheme.EmeraldPrimary,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = currentItem.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = SoftTheme.TextSecondaryMuted,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }
                }

                // Measurements Stats Row
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (isRealUltrasoundData) {
                        Text(
                            text = "قياسات آخر سونار مسجل 🩺",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTheme.EmeraldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    } else {
                        Text(
                            text = "القياسات المرجعية الطبية لهذا الأسبوع 📐",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTheme.TextSecondaryMuted,
                            fontSize = 11.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Length
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            color = SoftTheme.CanvasBg,
                            border = BorderStroke(1.dp, SoftTheme.CardBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Straighten,
                                    contentDescription = null,
                                    tint = SoftTheme.EmeraldPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text("الطول", color = SoftTheme.TextSecondaryMuted, fontSize = 10.sp)
                                    Text(
                                        text = displayLength,
                                        color = SoftTheme.TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        // Weight
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            color = SoftTheme.CanvasBg,
                            border = BorderStroke(1.dp, SoftTheme.CardBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Scale,
                                    contentDescription = null,
                                    tint = SoftTheme.EmeraldPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text("الوزن", color = SoftTheme.TextSecondaryMuted, fontSize = 10.sp)
                                    Text(
                                        text = displayWeight,
                                        color = SoftTheme.TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.EmeraldPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "ما شاء الله، سبحان الخالق 🌸",
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.TextWhite
                    )
                }
            }
        }
    }
}

