package com.example.ui.pregnancy

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.SoftTheme

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
 */
@Composable
fun FetalSizeVisualizerDialog(
    weekNumber: Int,
    babySizeFruit: String,
    onDismiss: () -> Unit,
    babyWeightGrams: String = "",
    babyLengthCm: String = ""
) {
    var selectedCategory by remember { mutableStateOf(ComparisonCategory.FRUITS) }

    val comparisonData = remember(weekNumber, babySizeFruit) {
        mapOf(
            ComparisonCategory.FRUITS to FetalComparisonItem(
                category = ComparisonCategory.FRUITS,
                title = babySizeFruit.ifBlank { "حبة رمان يانعة" },
                icon = "🍎",
                description = "الجنين في الأسبوع $weekNumber يضاهي تقريباً حجم $babySizeFruit اللذيذة والغنية بالفوائد!"
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
                description = "بتشبيه بلدي مصري أصيل، طفلكِ الآن تقريباً في حجم هذه الأكلة الطيبة!"
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
                description = "حجم يملأ القلب بالدفء والحنان، يقترب يوماً بعد يوم من لحظة احتضانه بين يديكِ!"
            )
        )
    }

    val currentItem = comparisonData[selectedCategory] ?: comparisonData.values.first()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
            border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.4f)),
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
                        Text("🔬", fontSize = 22.sp)
                        Column {
                            Text(
                                text = "مقارنة حجم الجنين 🌸",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SoftTheme.TextWhite
                            )
                            Text(
                                text = "الأسبوع $weekNumber من الحمل",
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftTheme.SoftPink
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = SoftTheme.SoftGray)
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
                            color = if (isSelected) SoftTheme.SoftPink else SoftTheme.DeepSlate,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedCategory = cat }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(cat.icon, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = cat.label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) SoftTheme.DeepSlate else SoftTheme.TextWhite
                                )
                            }
                        }
                    }
                }

                // Visual Representation Center Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, SoftTheme.MintTeal.copy(alpha = 0.3f))
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
                                .background(SoftTheme.MintTeal.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(currentItem.icon, fontSize = 42.sp)
                        }

                        Text(
                            text = currentItem.title,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SoftTheme.MintTeal,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = currentItem.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = SoftTheme.TextWhite.copy(alpha = 0.9f),
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }
                }

                // Measurements Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        color = SoftTheme.DeepSlate
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("📏 الطول التقريبي", color = SoftTheme.SoftGray, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (babyLengthCm.isNotBlank()) babyLengthCm else "~${(weekNumber * 1.1).toInt()} سم",
                                color = SoftTheme.SoftPink,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        color = SoftTheme.DeepSlate
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("⚖️ الوزن التقريبي", color = SoftTheme.SoftGray, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (babyWeightGrams.isNotBlank()) babyWeightGrams else "~${(weekNumber * 45)} غرام",
                                color = SoftTheme.MintTeal,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("ما شاء الله، سبحان الخالق 🌸", fontWeight = FontWeight.Bold, color = SoftTheme.DeepSlate)
                }
            }
        }
    }
}
