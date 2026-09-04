package com.example.ui.pregnancy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PregnancyEntity
import com.example.ui.theme.SoftTheme
import com.example.viewmodel.WomanCompanionViewModel

@Composable
fun PostpartumRecoveryCard(
    pregState: PregnancyEntity?,
    viewModel: WomanCompanionViewModel,
    onNavigateToNutrition: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "الحمد لله على سلامتكِ يا أميرة! 🎉🤱",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = SoftTheme.SoftPink
            )

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(SoftTheme.DeepSlate),
                contentAlignment = Alignment.Center
            ) {
                Text("👶🍼", fontSize = 48.sp)
            }

            val bName = pregState?.babyName
            val bNameGreeting = if (!bName.isNullOrBlank()) " ومولودكِ الغالي ($bName)" else " ومولودكِ الغالي"
            Text(
                text = "الحمد لله الذي وهبكِ$bNameGreeting بالسلامة وأقرّ عينكِ به. رحلتكِ كأمّ تبدأ الآن، وجوري معكِ خطوة بخطوة للعناية بصحتكِ الجسدية والنفسية في فترة النفاس والتعافي.",
                style = MaterialTheme.typography.bodyMedium,
                color = SoftTheme.TextWhite,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            val method = pregState?.birthMethod ?: "طبيعي"
            val isNatural = method == "طبيعي"

            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "إرشادات التعافي بعد الولادة ال${if (isNatural) "طبيعية 🌸" else "قيصرية 🏥"}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.MintTeal
                    )

                    val recoveryTips = if (isNatural) {
                        listOf(
                            "🧘‍♀️ العناية بمنطقة العجان: استخدمي مغاطس دافئة ومسكنات موضعية لتخفيف آلام الغرز وسرعة التئامها.",
                            "🚶‍♀️ الحركة الخفيفة: المشي الخفيف يومياً ينشط الدورة الدموية ويمنع التجلطات ويساعد الرحم على العودة لحجمه الطبيعي.",
                            "💪 تمارين قاع الحوض (كيجل): ابدئي بممارستها بلطف بمجرد زوال الألم لتقوية عضلات الحوض والتحكم الفعال.",
                            "🍼 الرضاعة الطبيعية: الرضاعة المبكرة تساعد على انقباض الرحم وإفراز هرمون السعادة وتقوية مناعة طفلكِ."
                        )
                    } else {
                        listOf(
                            "🩹 العناية بجرح العملية: الحفاظ على الجرح جافاً ونظيفاً، وتجنب رفع أي شيء أثقل من طفلكِ لحماية الغرز الداخلية والخارجية.",
                            "💊 تخفيف الآلام: الالتزام بالمسكنات الموصوفة من الطبيبة لتتمكني من التحرك وإرضاع طفلكِ براحة وبدون ضغوط جسدية.",
                            "🥑 الوقاية من الغازات والإمساك: شرب السوائل بكثرة وتناول الألياف والمشي اللطيف لتنشيط الأمعاء بعد التخدير.",
                            "🧸 دعم البطن: استخدمي وسادة ناعمة لدعم بطنكِ عند السعال أو العطس أو الضحك لتخفيف الضغط المفاجئ على جرح القيصرية."
                        )
                    }

                    recoveryTips.forEach { tip ->
                        Text(
                            text = tip,
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTheme.SoftGray,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }

            // Water reminder customization for breastfeeding
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("💧", fontSize = 28.sp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text("الترطيب والرضاعة 🍼", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite, style = MaterialTheme.typography.titleSmall)
                        Text("تذكري شرب كوب ماء قبل وبعد كل جلسة رضاعة لمساعدة جسمكِ على إدرار الحليب الطبيعي.", color = SoftTheme.SoftGray, style = MaterialTheme.typography.bodySmall)
                    }
                    Button(
                        onClick = { viewModel.addWater(250) },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.MintTeal),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("+ ٢٥٠ مل", color = SoftTheme.DeepSlate, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
