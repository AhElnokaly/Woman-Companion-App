package com.example.ui.settings

import android.app.AlarmManager
import android.content.Context
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.ui.theme.SoftTheme
import com.example.viewmodel.WomanCompanionViewModel

data class AchievementBadge(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val isUnlocked: Boolean,
    val suggestionText: String
)

@Composable
fun GentleAchievementsCard(
    viewModel: WomanCompanionViewModel
) {
    val pregState by viewModel.pregnancyState.collectAsStateWithLifecycle()
    val hasExportedBackup by viewModel.hasExportedBackupState.collectAsStateWithLifecycle()
    val hasGeneratedDoctorReport by viewModel.hasGeneratedDoctorReportState.collectAsStateWithLifecycle()
    val allFetalLogs by viewModel.allFetalGrowthLogsState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val alarmManager = remember { context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager }
    val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        alarmManager?.canScheduleExactAlarms() ?: true
    } else {
        true
    }

    val isProfileComplete = !pregState?.motherName.isNullOrBlank()
    val hasFetalLog = allFetalLogs.isNotEmpty()

    val badges = listOf(
        AchievementBadge(
            id = "ach_profile",
            title = "الملف الشخصي 👤",
            description = "تم إعداد بياناتكِ الكريمة بنجاح لتقديم رعاية مخصصة.",
            icon = "👤",
            isUnlocked = isProfileComplete,
            suggestionText = "أكملي اسمكِ الكريم في الإعدادات لتخصيص تجربتكِ 👤"
        ),
        AchievementBadge(
            id = "ach_backup",
            title = "حفظ البيانات 📦",
            description = "قام بحفظ وتصدير أول نسخة احتياطية بأمان.",
            icon = "📦",
            isUnlocked = hasExportedBackup,
            suggestionText = "جرّبي تصدير نسخة احتياطية لحفظ بياناتكِ بأمان 🔒"
        ),
        AchievementBadge(
            id = "ach_doctor_report",
            title = "تقرير الطبيبة 🩺",
            description = "أنشأتِ أول تقرير طبي شامل لمشاركته مع طبيبتكِ المعالجة.",
            icon = "🩺",
            isUnlocked = hasGeneratedDoctorReport,
            suggestionText = "أنشئي تقريراً طبياً لمشاركته مع طبيبتكِ عند الزيارة القادمة 🩺"
        ),
        AchievementBadge(
            id = "ach_fetal_growth",
            title = "متابعة نمو الجنين 👶",
            description = "سجّلتِ أول قياس لنمو طفلكِ المبارك.",
            icon = "👶",
            isUnlocked = hasFetalLog,
            suggestionText = "سجّلي أول قياس لنمو طفلكِ في شاشة نمو الجنين 👶"
        ),
        AchievementBadge(
            id = "ach_exact_alarm",
            title = "التنبيهات الدقيقة ⏰",
            description = "تم تفعيل صلاحيات التنبيهات الدقيقة للتذكيرات.",
            icon = "⏰",
            isUnlocked = canScheduleExact,
            suggestionText = "فعّلي صلاحية التنبيهات الدقيقة لتذكيركِ بأدويتكِ في وقتها ⏰"
        )
    )

    val unlockedCount = badges.count { it.isUnlocked }

    Card(
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth().testTag("achievements_card")
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
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
                    Text("🏆", fontSize = 22.sp)
                    Column {
                        Text(
                            "شارات الإنجاز الرقيقة 🌟",
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.SoftPink,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "إنجازاتكِ اللطيفة في رحلتكِ الصحية",
                            color = SoftTheme.SoftGray,
                            fontSize = 11.sp
                        )
                    }
                }
                Surface(
                    color = SoftTheme.SoftPink.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "$unlockedCount من ${badges.size}",
                        color = SoftTheme.SoftPink,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                "خطواتكِ اليومية الصغيرة تصنع فرقاً عظيماً لصحتكِ وصحة جنينكِ:",
                style = MaterialTheme.typography.bodySmall,
                color = SoftTheme.TextWhite
            )

            badges.forEach { badge ->
                if (badge.isUnlocked) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SoftTheme.DeepSlate)
                            .padding(12.dp)
                            .testTag("badge_unlocked_${badge.id}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            color = SoftTheme.SoftPink.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(badge.icon, fontSize = 20.sp)
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    badge.title,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftTheme.TextWhite,
                                    fontSize = 13.sp
                                )
                                Text("✨", fontSize = 11.sp)
                            }
                            Text(
                                badge.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftTheme.SoftGray,
                                fontSize = 11.sp
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                SoftTheme.SoftGray.copy(alpha = 0.08f),
                                RoundedCornerShape(14.dp)
                            )
                            .padding(12.dp)
                            .testTag("badge_suggestion_${badge.id}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("🔒", fontSize = 18.sp)
                        Text(
                            badge.suggestionText,
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTheme.SoftGray,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
