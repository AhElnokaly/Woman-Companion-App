package com.example.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.GlassmorphicCard
import com.example.ui.SoftTheme
import com.example.ui.formatGregorianDate
import com.example.ui.settings.BatteryOptimizationCard

@Composable
fun StepSummaryAndSave(
    name: String,
    nickname: String,
    birthDateMs: Long?,
    hasHighBp: Boolean,
    hasLowBp: Boolean,
    hasDiabetes: Boolean,
    chronicOthers: String,
    isPregnant: Boolean,
    lastPeriodDateMs: Long?,
    lastPeriodEndDateMs: Long?,
    onFinish: () -> Unit
) {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🎉", fontSize = 52.sp)
            Text(
                text = "ملفكِ الصحي جاهز ومكتمل!",
                fontWeight = FontWeight.Bold,
                color = SoftTheme.TextWhite,
                style = MaterialTheme.typography.titleMedium,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = "تفاصيل رائعة! قمنا بحفظ وتأمين بياناتكِ محلياً وبكل سرية وخصوصية. إليكِ بطاقتكِ الصحية الترحيبية من جوري:",
                color = SoftTheme.SoftGray,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            // Dynamic Card resembling a Premium Medical Passport
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(SoftTheme.DeepSlate, SoftTheme.CardSlate)
                        )
                    )
                    .border(1.dp, SoftTheme.SoftPink.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("صديقة التطبيق:", color = SoftTheme.SoftGray, style = MaterialTheme.typography.bodyMedium)
                    Text(name, color = SoftTheme.TextWhite, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("الاسم المحبب (الدلع):", color = SoftTheme.SoftGray, style = MaterialTheme.typography.bodyMedium)
                    Text(nickname.ifEmpty { name }, color = SoftTheme.SoftPink, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                }

                if (birthDateMs != null) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("تاريخ الميلاد:", color = SoftTheme.SoftGray, style = MaterialTheme.typography.bodyMedium)
                        Text(formatGregorianDate(birthDateMs), color = SoftTheme.TextWhite, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                val bpText = when {
                    hasHighBp -> "ارتفاع ضغط الدم 📈"
                    hasLowBp -> "انخفاض ضغط الدم 📉"
                    else -> "سليم وطبيعي ✨"
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("الحالة الوقائية للضغط:", color = SoftTheme.SoftGray, style = MaterialTheme.typography.bodyMedium)
                    Text(bpText, color = SoftTheme.TextWhite, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("مرض السكري:", color = SoftTheme.SoftGray, style = MaterialTheme.typography.bodyMedium)
                    Text(if (hasDiabetes) "نعم، يحتاج موازنة 🩸" else "سليم ولله الحمد ✨", color = SoftTheme.TextWhite, style = MaterialTheme.typography.bodyMedium)
                }

                if (chronicOthers.trim().isNotEmpty()) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("ملاحظات وحساسية أخرى:", color = SoftTheme.SoftGray, style = MaterialTheme.typography.bodyMedium)
                        Text(chronicOthers, color = SoftTheme.TextWhite, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("النظام والطور الفعّال:", color = SoftTheme.SoftGray, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = if (isPregnant) "تتبع أسابيع الحمل 🤰" else "متابعة الدورة الشهرية والتبويض 🌸",
                        color = SoftTheme.MintTeal,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (lastPeriodDateMs != null) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("موعد آخر دورة مسجل:", color = SoftTheme.SoftGray, style = MaterialTheme.typography.bodyMedium)
                        Text(formatGregorianDate(lastPeriodDateMs), color = SoftTheme.TextWhite, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Battery Optimization Exemption Card (Optional recommendation during onboarding)
            BatteryOptimizationCard()

            Spacer(modifier = Modifier.height(12.dp))

            // Pulse-animated entry button
            Button(
                onClick = onFinish,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SoftTheme.SoftPink,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("onboarding_finish_button"),
                shape = RoundedCornerShape(18.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Text(
                    text = "ابدئي رحلتكِ الجميلة مع جوري! ✨🌸",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}
