package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DangerSubScreen() {
    val context = LocalContext.current
    val dangerSigns = listOf(
        "نزول قطرات أو بقع دم مهبلية واضحة 🩸",
        "ألم أو تشنجات شديدة في أسفل البطن لا تزول بالراحة 💔",
        "صداع شديد ومستمر ومفاجئ قد يترافق مع غباش في الرؤية 😵‍💫",
        "تورم وانتفاخ مفاجئ وكبير في اليدين أو الوجه 🫱",
        "ارتفاع درجة حرارة الجسم والحمى المصحوبة بالقشعريرة 🤒",
        "تسرب أو تدفق مفاجئ للسوائل من المهبل 💧",
        "ضعف أو انعدام مفاجئ لحركة الجنين بعد الشهر السادس 👶"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Warning, contentDescription = null, tint = SoftTheme.RedDanger, modifier = Modifier.size(64.dp))
        Text("أعراض وعلامات الخطر التحذيرية ⚠️", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SoftTheme.RedDanger)
        Text(
            "إذا واجهتكِ أو شعرتِ بأي من الأعراض التالية، يرجى التوجه فوراً لأقرب مستشفى أو الاتصال بطبيبتك المتابعة دون أي تأخير:",
            color = SoftTheme.TextWhite,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        dangerSigns.forEach { sign ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(8.dp).background(SoftTheme.RedDanger, CircleShape))
                    Text(text = sign, color = SoftTheme.TextWhite, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // Quick mock emergency call
        Button(
            onClick = {
                // emergency phone trigger or alert
            },
            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.RedDanger),
            modifier = Modifier.fillMaxWidth().height(56.dp).testTag("emergency_call_btn")
        ) {
            Icon(Icons.Default.Phone, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("اتصال فوري بالطوارئ الصحية")
        }
        Spacer(modifier = Modifier.height(80.dp))
    }
}
