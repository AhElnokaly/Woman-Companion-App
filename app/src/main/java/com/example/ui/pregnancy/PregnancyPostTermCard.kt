package com.example.ui.pregnancy

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SoftTheme

@Composable
fun PregnancyPostTermCard(
    modifier: Modifier = Modifier
) {
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
