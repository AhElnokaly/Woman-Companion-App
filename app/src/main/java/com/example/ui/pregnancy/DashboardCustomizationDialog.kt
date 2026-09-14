package com.example.ui.pregnancy

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
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
import androidx.compose.ui.window.Dialog
import com.example.ui.SoftTheme

data class DashboardSectionConfig(
    val key: String,
    val title: String,
    val description: String,
    val icon: String,
    val isMandatory: Boolean = false
)

val DEFAULT_DASHBOARD_SECTIONS = listOf(
    DashboardSectionConfig("sec_gauge", "عداد ومؤشر عمر الحمل", "القوس الدائري ومرحلة الحمل الحالية", "✨", isMandatory = true),
    DashboardSectionConfig("sec_baby_info", "تطور ومعلومات الجنين", "حجم الطفل وطوله ومراحل نمو أعضائه", "👶"),
    DashboardSectionConfig("sec_quick_actions", "التسجيل الصحي السريع", "تسجيل الماء، ضغط الدم، ركلات الجنين، وتنفس جوري", "⚡"),
    DashboardSectionConfig("sec_meds", "الأدوية والفيتامينات اليومية", "تتبع جرعات الحديد، الكالسيوم، ومخزون الأدوية", "💊"),
    DashboardSectionConfig("sec_vitals", "ملخص النشاط والترطيب", "عداد شرب الماء اليومي والخطوات والمواعيد", "💧"),
    DashboardSectionConfig("sec_explore", "بطاقات استكشف مع جوري", "نصائح الطقس، فحص الخرافات، والأكلات المصرية", "🧭")
)

/**
 * نافذة تخصيص بطاقات الشاشة الرئيسية
 * تسمح للمرأة الحامل باختيار ما تود رؤيته وحفظ تفضيلاتها مباشرة
 */
@Composable
fun DashboardCustomizationDialog(
    onDismiss: () -> Unit,
    onSettingsChanged: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("dashboard_layout_prefs", Context.MODE_PRIVATE) }

    // State map for each section visibility
    val sectionStates = remember {
        mutableStateMapOf<String, Boolean>().apply {
            DEFAULT_DASHBOARD_SECTIONS.forEach { sec ->
                put(sec.key, prefs.getBoolean(sec.key, true))
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
            border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dashboard_customization_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
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
                        Text("🎛️", fontSize = 22.sp)
                        Column {
                            Text(
                                text = "تخصيص الشاشة الرئيسية",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SoftTheme.TextWhite
                            )
                            Text(
                                text = "اختاري البطاقات المناسبة لاحتياجكِ 🌸",
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftTheme.SoftGray,
                                fontSize = 11.sp
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = SoftTheme.SoftGray)
                    }
                }

                // Section Toggle List
                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(DEFAULT_DASHBOARD_SECTIONS) { sec ->
                        val isChecked = sectionStates[sec.key] ?: true
                        Surface(
                            onClick = {
                                if (!sec.isMandatory) {
                                    val newVal = !isChecked
                                    sectionStates[sec.key] = newVal
                                    prefs.edit().putBoolean(sec.key, newVal).apply()
                                    onSettingsChanged()
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isChecked) SoftTheme.DeepSlate else SoftTheme.DeepSlate.copy(alpha = 0.5f),
                            border = BorderStroke(
                                1.dp,
                                if (isChecked) SoftTheme.SoftPink.copy(alpha = 0.3f) else Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(sec.icon, fontSize = 20.sp)
                                    Column {
                                        Text(
                                            text = sec.title,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isChecked) SoftTheme.TextWhite else SoftTheme.SoftGray,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = if (sec.isMandatory) "قسم رئيسي أساسي ✨" else sec.description,
                                            fontSize = 10.sp,
                                            color = if (sec.isMandatory) SoftTheme.SoftPink else SoftTheme.SoftGray
                                        )
                                    }
                                }

                                Switch(
                                    checked = isChecked,
                                    enabled = !sec.isMandatory,
                                    onCheckedChange = { newVal ->
                                        sectionStates[sec.key] = newVal
                                        prefs.edit().putBoolean(sec.key, newVal).apply()
                                        onSettingsChanged()
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = SoftTheme.DeepSlate,
                                        checkedTrackColor = SoftTheme.MintTeal,
                                        uncheckedThumbColor = SoftTheme.SoftGray,
                                        uncheckedTrackColor = SoftTheme.CardSlate
                                    )
                                )
                            }
                        }
                    }
                }

                // Save / Close Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Done, contentDescription = null, tint = SoftTheme.DeepSlate, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("حفظ التخصيص والعودة 🌸", fontWeight = FontWeight.Bold, color = SoftTheme.DeepSlate)
                }
            }
        }
    }
}
