package com.example.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SoftTheme

@Composable
fun BatteryOptimizationCard() {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth().testTag("battery_optimization_card")
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("🔋⚡", fontSize = 24.sp)
                Column {
                    Text(
                        "ضمان عمل التذكيرات والخطوات بالخلفية ⚡",
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.SoftPink,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "استثناء البطارية وتوجيهات الهواتف (سامسونج، شاومي، أوبو)",
                        color = SoftTheme.SoftGray,
                        fontSize = 11.sp
                    )
                }
            }

            Text(
                "تقوم بعض الهواتف (مثل Xiaomi/MIUI، Samsung، Oppo/ColorOS) بإنهاء خدمات الخلفية تلقائياً مما قد يؤدي لتأخر التذكيرات أو إيقاف عداد الخطوات. استثناء التطبيق يضمن لكِ استلام كافة التنبيهات في وقتها.",
                style = MaterialTheme.typography.bodySmall,
                color = SoftTheme.TextWhite,
                lineHeight = 18.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
                        val isIgnoring = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: false
                        } else true

                        if (!isIgnoring && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            try {
                                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                try {
                                    context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                                } catch (_: Exception) {}
                            }
                        } else {
                            try {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.MintTeal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).testTag("battery_optimization_btn")
                ) {
                    Text("إلغاء قيود البطارية ⚡", color = androidx.compose.ui.graphics.Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = { isExpanded = !isExpanded },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("battery_instructions_btn")
                ) {
                    Text(if (isExpanded) "إخفاء التعليمات" else "تعليمات هاتفي 📱", color = SoftTheme.TextWhite, fontSize = 12.sp)
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SoftTheme.DeepSlate)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("📱 تعليمات خاصة بحسب نوع الهاتف:", fontWeight = FontWeight.Bold, color = SoftTheme.MintTeal, fontSize = 13.sp)
                    Text("• شاومي / Xiaomi / MIUI / POCO:\nالإعدادات ⚙️ ← التطبيقات ← إدارة التطبيقات ← جوري 🌸 ← تفعيل 'البدء التلقائي Autostart' 🟢 + اختر موفر البطارية: 'لا توجد قيود No restrictions'.", color = SoftTheme.SoftGray, fontSize = 11.sp, lineHeight = 16.sp)
                    Text("• سامسونج / Samsung:\nالإعدادات ⚙️ ← البطارية والعناية بالجهاز ← البطارية ← حدود استخدام الخلفية ← إضافة 'جوري' لقائمة 'التطبيقات التي لا توضع في وضع السكون أبداً'.", color = SoftTheme.SoftGray, fontSize = 11.sp, lineHeight = 16.sp)
                    Text("• أوبو / Oppo / Realme / ColorOS:\nالإعدادات ⚙️ ← البطارية ← إدارة إمداد الطاقة ← تفعيل 'السماح بالبدء التلقائي' و'السماح بالعمل في الخلفية'.", color = SoftTheme.SoftGray, fontSize = 11.sp, lineHeight = 16.sp)
                    Text("• فيفو / هواوي / Vivo / Huawei:\nالإعدادات ⚙️ ← البطارية ← التشغيل التلقائي للتطبيقات ← تفعيل إمكانيات الخلفية والتشغيل التلقائي.", color = SoftTheme.SoftGray, fontSize = 11.sp, lineHeight = 16.sp)
                }
            }
        }
    }
}
