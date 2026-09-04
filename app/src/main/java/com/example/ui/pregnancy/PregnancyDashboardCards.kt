package com.example.ui.pregnancy

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.PregnancyProgression
import com.example.ui.calculateMonthProgress
import com.example.ui.theme.SoftTheme
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PregnancyBabyInfoCard(
    isGenderKnown: Boolean,
    babyGender: String?,
    babyName: String?,
    onEditBabyInfo: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isGenderKnown) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SoftTheme.DeepSlate),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (babyGender == "ولد") "👶" else if (babyGender == "بنت") "👧" else "🤰", fontSize = 18.sp)
                    }
                    Column {
                        val genderEmoji = if (babyGender == "ولد") "💙" else if (babyGender == "بنت") "💗" else "✨"
                        val genderLabel = if (babyGender == "ولد") "ولد صالح معافى" else if (babyGender == "بنت") "بنت صالحة معافاة" else "مفاجأة مباركة"
                        val nameLabel = if (!babyName.isNullOrBlank()) "الاسم المقترح: $babyName" else "لم يتم اختيار اسم بعد"

                        Text(
                            text = "$genderLabel $genderEmoji",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.TextWhite
                        )
                        Text(
                            text = nameLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTheme.SoftPink
                        )
                    }
                }
                Text(
                    text = "تعديل 📝",
                    color = SoftTheme.SoftPink,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onEditBabyInfo() }
                )
            }
        }
    } else {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
            shape = RoundedCornerShape(24.dp)
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🧸", fontSize = 22.sp)
                        Text(
                            text = "جنينكِ الغالي",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.TextWhite
                        )
                    }
                    Text(
                        text = "تسجيل 📝",
                        color = SoftTheme.SoftPink,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onEditBabyInfo() }
                    )
                }
                Text(
                    text = "لقد دخلتِ الأسبوع ١٤ من الحمل 🌸 هل عرفتِ جنس جنينكِ؟ اضغطي لتسجيله واقتراح اسمه لكي يتفاعل رفيقكِ مع جنينكِ بالاسم والتهنئة اللطيفة! 💕",
                    color = SoftTheme.SoftGray,
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 16.sp
                )
                Button(
                    onClick = onEditBabyInfo,
                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.DeepSlate),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("تسجيل جنس واسم الجنين 👶🍼", color = SoftTheme.SoftPink, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PregnancyProgressCard(
    prog: PregnancyProgression,
    modifier: Modifier = Modifier
) {
    val trimesterColor = when {
        prog.weeks >= 41 -> Color(0xFFFFB300) // Month 10: Gold Amber
        prog.trimester == 1 -> Color(0xFF9575CD) // Trimester 1: Lavender
        prog.trimester == 2 -> SoftTheme.MintTeal // Trimester 2: Mint Teal
        else -> SoftTheme.SoftPink // Trimester 3: Soft Pink
    }

    val monthProg = calculateMonthProgress(prog.weeks, prog.daysIntoWeek)
    val activeMonth = monthProg.monthNumber
    val activeMonthProgress = monthProg.progressFraction

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = if (prog.weeks >= 41) "أنتِ الآن في الشهر العاشر (تخطي موعد الولادة) ⚠️" else "أنتِ الآن في الأسبوع",
                style = MaterialTheme.typography.bodyMedium,
                color = SoftTheme.SoftGray,
                textAlign = TextAlign.Center
            )

            Box(
                modifier = Modifier
                    .size(160.dp)
                    .drawBehind {
                        drawCircle(
                            color = SoftTheme.DeepSlate,
                            radius = size.minDimension / 2
                        )
                        drawArc(
                            color = trimesterColor,
                            startAngle = -90f,
                            sweepAngle = if (prog.weeks >= 41) 360f else ((prog.weeks.toFloat() / 40f) * 360f).coerceIn(0f, 360f),
                            useCenter = false,
                            style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${prog.weeks}",
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 58.sp),
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.TextWhite
                    )
                    Text(
                        text = "الأيام: ${prog.daysIntoWeek}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = trimesterColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "مخطط شهور الحمل التسعة 📅",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTheme.SoftGray
                    )
                    Text(
                        text = "أنتِ في " + when(activeMonth) {
                            1 -> "الشهر الأول"
                            2 -> "الشهر الثاني"
                            3 -> "الشهر الثالث"
                            4 -> "الشهر الرابع"
                            5 -> "الشهر الخامس"
                            6 -> "الشهر السادس"
                            7 -> "الشهر السابع"
                            8 -> "الشهر الثامن"
                            9 -> "الشهر التاسع"
                            else -> "الشهر العاشر ⚠️"
                        } + " (${(activeMonthProgress * 100).toInt()}% من الشهر)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = trimesterColor
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val totalSegments = if (prog.weeks >= 41) 10 else 9
                    for (i in 0 until totalSegments) {
                        val segProgress = when {
                            i < activeMonth - 1 -> 1f
                            i == activeMonth - 1 -> activeMonthProgress
                            else -> 0f
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(SoftTheme.DeepSlate)
                        ) {
                            if (segProgress > 0f) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(segProgress)
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(trimesterColor.copy(alpha = 0.7f), trimesterColor)
                                            )
                                        )
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("الثلث", color = SoftTheme.SoftGray, style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = if (prog.weeks >= 41) "أمان ممتد" else "${prog.trimester}",
                        color = SoftTheme.TextWhite,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                VerticalDivider(modifier = Modifier.height(24.dp).width(1.dp), color = SoftTheme.SoftGray)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("الأيام المتبقية", color = SoftTheme.SoftGray, style = MaterialTheme.typography.bodySmall)
                    Text("${prog.remainingDays}", color = SoftTheme.TextWhite, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
                VerticalDivider(modifier = Modifier.height(24.dp).width(1.dp), color = SoftTheme.SoftGray)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("موعد الولادة", color = SoftTheme.SoftGray, style = MaterialTheme.typography.bodySmall)
                    val formatted = SimpleDateFormat("dd MMM", Locale.forLanguageTag("ar")).format(Date(prog.dueDate))
                    Text(formatted, color = SoftTheme.TextWhite, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
fun PostTermSupportCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.5.dp, Color(0xFFFFB300))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📢", fontSize = 24.sp)
                Text(
                    text = "الولادة بعد موعدكِ المقدر (الشهر العاشر) 🌸🏥",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFB300)
                )
            }

            Text(
                text = "صديقتي الغالية، تخطي موعد الولادة المتوقع (الأسبوع 40) هو أمر شائع يحدث لكثير من الأمهات. إليكِ أهم الإرشادات التوعوية للتعامل مع هذه المرحلة ومتابعتها مع طبيبكِ:",
                style = MaterialTheme.typography.bodySmall,
                color = SoftTheme.TextWhite,
                lineHeight = 18.sp
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "📋 إرشادات توعوية عامة:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.MintTeal
                    )
                    Text(
                        text = "• المتابعة مع الطبيبة: التنسيق مع طبيبتكِ المعالجة لتقييم صحة الجنين والمشيمة بحسب الخطة الطبية المناسبة لحالتكِ.\n• متابعة حركة الجنين: الانتباه لنمط حركة الجنين المعتاد، والتواصل الفوري مع الفريق الطبي عند ملاحظة أي تراجع أو تغير ملحوظ في الحركة.\n• الراحة والاسترخاء: الحفاظ على الترطيب الكافي والراحة التامة والنشاط البدني الخفيف وفق إرشادات الطبيب.\n• متى تتوجهين فوراً للمستشفى أو الطوارئ؟ عند نزول السائل الأمنيوسي (ماء الجنين)، حدوث نزيف مهبلي، آلام حادة مستمرة، أو انخفاض ملحوظ في حركة الجنين.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTheme.SoftGray,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun BabySizeComparisonCard(
    prog: PregnancyProgression,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SoftTheme.DeepSlate),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = prog.comparisonIcon,
                    fontSize = 36.sp
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "طفلكِ الآن بحجم:",
                    style = MaterialTheme.typography.bodySmall,
                    color = SoftTheme.SoftGray
                )
                Text(
                    text = prog.comparisonName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SoftTheme.SoftPink
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = prog.developmentTip,
                    style = MaterialTheme.typography.bodySmall,
                    color = SoftTheme.TextWhite,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun BirthAnnouncementPromptCard(
    onAnnounceBirth: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "بشرى ولادة جديدة؟ ✨👶🎉",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = SoftTheme.MintTeal
            )
            Text(
                text = "إذا منّ الله عليكِ بالولادة بفضله، شاركينا لنحتفي بكِ ونقدم لكِ إرشادات فترة النفاس والتعافي المثالية الخاصة بطريقة ولادتكِ 💖",
                style = MaterialTheme.typography.bodySmall,
                color = SoftTheme.SoftGray,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
            Button(
                onClick = onAnnounceBirth,
                colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("الحمد لله، وضعتُ مولودي بالسلامة! 🥰", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
