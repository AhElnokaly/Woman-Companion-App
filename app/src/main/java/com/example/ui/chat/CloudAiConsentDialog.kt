package com.example.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.SoftTheme

@Composable
fun CloudAiConsentDialog(
    onDismiss: () -> Unit,
    onKeepOffline: () -> Unit,
    onAcceptCloud: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "🌐 تفعيل الذكاء السحابي (Google Gemini)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SoftTheme.TextWhite,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "عند تفعيل الوضع السحابي، يتم إرسال رسالتك وسياق صحتك التوعوي العام (مثل مرحلة الحمل أو الدورة ودرجة الحرارة) إلى Google Gemini عبر اتصال HTTPS مشفر لتحسين الإجابة.\n\nبياناتك الشخصية وقاعدة بياناتك تظل مشفرة على هاتفك، ويمكنكِ دائماً استخدام الوضع المحلي الآمن 100% بدون أي اتصال خارجي.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SoftTheme.SoftGray,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Start
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onKeepOffline,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("البقاء محلياً 📴", color = SoftTheme.SoftGray, fontSize = 12.sp)
                    }
                    Button(
                        onClick = onAcceptCloud,
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.PregnancyPurple),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("موافقة وتفعيل 🌐", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
