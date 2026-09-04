package com.example.ui.onboarding

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.GlassmorphicCard
import com.example.ui.SoftTheme
import com.example.ui.formatGregorianDate
import java.util.*

@Composable
fun StepCyclePregnancy(
    isPregnant: Boolean,
    onPregnantChange: (Boolean) -> Unit,
    lastPeriodDateMs: Long?,
    onLastPeriodDateChange: (Long?) -> Unit,
    lastPeriodEndDateMs: Long?,
    onLastPeriodEndDateChange: (Long?) -> Unit
) {
    val context = LocalContext.current

    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00C0A5).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🤰", fontSize = 22.sp)
                }
                Column {
                    Text(
                        text = "تتبع الحمل والدورة الشهرية",
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.TextWhite,
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "حساب دقيق لأسابيع الحمل أو نافذة الخصوبة القادمة",
                        color = SoftTheme.SoftGray,
                        fontSize = 11.sp
                    )
                }
            }

            Text(
                text = "دعينا نحدد وضعكِ الحالي بدقة. تفعيل طور الحمل سيفتح لكِ أدلة أسبوعية مفصلة عن نمو جنينكِ، بينما تتبع الدورة سيحسب مواعيد التبويض والخصوبة.",
                color = SoftTheme.SoftGray,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp
            )

            // Dual Mode Split Showcase Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate.copy(alpha = 0.6f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.app_showcase_split_1783590277277),
                        contentDescription = "استعراض وضعي الدورة والحمل",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.75f)
                                    )
                                )
                            )
                    )
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("✨", fontSize = 18.sp)
                        Column {
                            Text(
                                text = "تطبيق واحد.. لرحلتين متكاملتين",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "تتبع ذكي للدورة والخصوبة 🩸 أو رحلة حمل مباركة 🤰",
                                color = SoftTheme.MintTeal,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = Color(0xFF00C0A5).copy(alpha = 0.25f), thickness = 1.dp)

            // Pregnancy Toggle Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SoftTheme.DeepSlate.copy(alpha = 0.5f))
                    .border(1.dp, Color(0xFF00C0A5).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .clickable { onPregnantChange(!isPregnant) }
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "هل يوجد حمل حالياً؟ 🤰",
                        color = SoftTheme.TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "نعم، أريد تشغيل حاسبة الحمل وأدوات صحة الجنين اليومية.",
                        color = SoftTheme.SoftGray,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
                Switch(
                    checked = isPregnant,
                    onCheckedChange = onPregnantChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF00C0A5),
                        checkedTrackColor = Color(0xFF80E0D2),
                        uncheckedThumbColor = SoftTheme.SoftGray,
                        uncheckedTrackColor = SoftTheme.DeepSlate
                    )
                )
            }

            // Last Period Date Picker
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (isPregnant) "تاريخ بداية آخر دورة شهرية (لحساب موعد الولادة المتوقع):" else "تاريخ بداية آخر دورة شهرية (لحساب دورتكِ القادمة):",
                    color = SoftTheme.TextWhite,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Button(
                    onClick = {
                        val calendar = Calendar.getInstance().apply {
                            if (lastPeriodDateMs != null) timeInMillis = lastPeriodDateMs
                        }
                        val dateDialog = DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                val cal = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, y)
                                    set(Calendar.MONTH, m)
                                    set(Calendar.DAY_OF_MONTH, d)
                                    set(Calendar.HOUR_OF_DAY, 0)
                                    set(Calendar.MINUTE, 0)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }
                                onLastPeriodDateChange(cal.timeInMillis)
                                if (lastPeriodEndDateMs == null) {
                                    onLastPeriodEndDateChange(cal.timeInMillis + 5L * 24 * 60 * 60 * 1000)
                                }
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        )
                        dateDialog.datePicker.maxDate = System.currentTimeMillis()
                        dateDialog.datePicker.minDate = System.currentTimeMillis() - 300L * 24 * 60 * 60 * 1000
                        dateDialog.show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SoftTheme.DeepSlate.copy(alpha = 0.6f),
                        contentColor = SoftTheme.TextWhite
                    ),
                    border = BorderStroke(1.dp, Color(0xFF00C0A5).copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("🗓️", fontSize = 18.sp)
                            Text(
                                text = if (lastPeriodDateMs != null) formatGregorianDate(lastPeriodDateMs) else "انقري لتحديد تاريخ بدء آخر دورة",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = Color(0xFF00C0A5), modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Last Period End Date Picker
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "تاريخ انتهاء الدورة الشهرية الأخيرة (إن كنتِ تذكرينه):",
                    color = SoftTheme.TextWhite,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Button(
                    onClick = {
                        val calendar = Calendar.getInstance().apply {
                            if (lastPeriodEndDateMs != null) timeInMillis = lastPeriodEndDateMs
                        }
                        val dateDialog = DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                val cal = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, y)
                                    set(Calendar.MONTH, m)
                                    set(Calendar.DAY_OF_MONTH, d)
                                    set(Calendar.HOUR_OF_DAY, 0)
                                    set(Calendar.MINUTE, 0)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }
                                onLastPeriodEndDateChange(cal.timeInMillis)
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        )
                        dateDialog.datePicker.maxDate = System.currentTimeMillis()
                        dateDialog.datePicker.minDate = System.currentTimeMillis() - 300L * 24 * 60 * 60 * 1000
                        dateDialog.show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SoftTheme.DeepSlate.copy(alpha = 0.6f),
                        contentColor = SoftTheme.TextWhite
                    ),
                    border = BorderStroke(1.dp, Color(0xFF00C0A5).copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("🏁", fontSize = 18.sp)
                            Text(
                                text = if (lastPeriodEndDateMs != null) formatGregorianDate(lastPeriodEndDateMs) else "انقري لتحديد تاريخ انتهاء الدورة",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Icon(Icons.Default.Done, contentDescription = null, tint = Color(0xFF00C0A5), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
