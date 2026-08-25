package com.example.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.viewmodel.WomanCompanionViewModel

@Composable
fun JouriWellnessNotificationCard(
    viewModel: WomanCompanionViewModel,
    onOpenJouriChat: () -> Unit
) {
    val pregState by viewModel.pregnancyState.collectAsStateWithLifecycle()
    val waterLog by viewModel.todayWaterLogState.collectAsStateWithLifecycle()
    val periodLogs by viewModel.periodLogsState.collectAsStateWithLifecycle()
    val settingsState by viewModel.appLockSettingsState.collectAsStateWithLifecycle()
    val todayStepsState by viewModel.todayStepLogState.collectAsStateWithLifecycle()
    val weatherState by viewModel.weatherState.collectAsStateWithLifecycle()
    val phaseInfo = viewModel.getCurrentCyclePhase()
    
    val companionName = settingsState?.companionName ?: "جوري"
    val targetSteps = settingsState?.dailyStepTarget ?: 6000
    val steps = todayStepsState?.steps ?: 0

    val nameToUse = pregState?.motherName ?: "يا غالية"
    val isPregnant = pregState != null
    val waterLogged = waterLog?.amountMl ?: 0
    val waterTarget = viewModel.getWaterTarget()
    
    // Calculate dynamic message
    val dynamicMessage = remember(pregState, waterLog, phaseInfo, companionName, waterTarget) {
        when {
            // Case 1: Low water intake
            waterLogged < waterTarget / 2 -> {
                "مرحباً بكِ يا $nameToUse! 💧 لاحظتُ أنكِ شربتِ $waterLogged مل فقط من هدفكِ اليومي المعدّل ($waterTarget مل). جسدكِ يحتاج إلى الترطيب لزيادة الطاقة والنشاط. هل نشرب كوباً معاً الآن؟ 🥰"
            }
            // Case 2: In period and high pain recorded
            !isPregnant && phaseInfo.phaseName == "Menstruation" -> {
                val latestPeriod = periodLogs.maxByOrNull { it.startDate }
                val pain = latestPeriod?.painLevel ?: 0
                if (pain >= 6) {
                    "سلامة قلبكِ يا حبيبتي $nameToUse! 🥺 dلقد سجلتِ مستوى ألم مرتفع ($pain/10). هل تشعرين بالمغص؟ أنصحكِ بكوب دافئ من البابونج وراحة تامة. تحدثي معي للتخفيف عنكِ 💕"
                } else {
                    "أهلاً بكِ يا $nameToUse يا ريحانة قلب $companionName 🌸 أنتِ اليوم في طور الطمث. كيف هي معنوياتكِ وصحتكِ اليوم؟ أنا هنا لمرافقتكِ والاستماع إليكِ خطوة بخطوة."
                }
            }
            // Case 3: Pregnancy check-in
            isPregnant -> {
                "مرحباً بكِ يا أُمّنا الجميلة $nameToUse! 🤰 كيف تشعرين اليوم وكيف هي حركة طفلكِ؟ تذكري تناول الحديد وحمض الفوليك والراحة التامة خطوة بخطوة. أنا بجانبكِ دوماً للاطمئنان عليكِ 🌸"
            }
            // Case 4: Default welcoming alert
            else -> {
                "صباحكِ سكر يا غالية $nameToUse! 🌸 رفيقتكِ الذكية $companionName تود الاطمئنان عليكِ اليوم. كيف تشعرين الآن؟ هل تودين تتبع صحتكِ أو الحصول على نصيحة غذائية سريعة لطوركِ الحالي؟ 🥰"
            }
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("jouri_notification_card"),
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.5.dp, SoftTheme.SoftPink.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                    val cardExpression = remember(dynamicMessage) {
                        when {
                            dynamicMessage.contains("سلامة") || dynamicMessage.contains("ألم") || dynamicMessage.contains("مغص") || dynamicMessage.contains("طوارئ") -> JouriExpressionState.REASSURING
                            dynamicMessage.contains("مبروك") || dynamicMessage.contains("سعيد") || dynamicMessage.contains("ممتاز") -> JouriExpressionState.CELEBRATORY
                            else -> JouriExpressionState.WARM_HAPPY
                        }
                    }
                    JouriAvatar(expression = cardExpression, size = 36.dp)
                    Column {
                        Text(
                            text = "صديقتكِ $companionName تود الاطمئنان عليكِ",
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.SoftPink,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "تفاعل فوري محلي وبديل للتنبيهات الكلاسيكية",
                            color = SoftTheme.SoftGray,
                            fontSize = 10.sp
                        )
                    }
                }
                // Interactive pulse dot
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(SoftTheme.MintTeal, CircleShape)
                )
            }
            
            Text(
                text = dynamicMessage,
                color = SoftTheme.TextWhite,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp
            )

            // Weather and hydration section
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("🌤️", fontSize = 16.sp)
                            Text("حالة الجو الفعلي والترطيب:", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite, fontSize = 12.sp)
                        }
                        weatherState?.let { w ->
                            Text(
                                text = "${w.temperature}°م | رطوبة ${w.humidity}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftTheme.SoftPink,
                                fontWeight = FontWeight.Bold
                            )
                        } ?: Text("جاري جلب الطقس...", color = SoftTheme.SoftGray, fontSize = 11.sp)
                    }

                    weatherState?.let { w ->
                        Text(
                            text = "الطقس الحالي في مدينتكِ هو ${w.description}." + 
                                   if (w.temperature > 28) " ⚠️ الجو حار اليوم! تم زيادة هدف شرب الماء اليومي لترطيب جسمكِ ومكافحة التعب." else " 🍃 الجو معتدل ومنعش ومناسب لممارسة المشي وشرب كوب ماء نقي.",
                            color = SoftTheme.SoftGray,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Steps and movement tracking section
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val advicePair = viewModel.getActivityAdvice(steps, targetSteps)
                val adviceTitle = advicePair.first
                val adviceText = advicePair.second
                val progress = if (targetSteps > 0) (steps.toFloat() / targetSteps.toFloat()).coerceIn(0f, 1f) else 0f

                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("🚶‍♀️", fontSize = 16.sp)
                            Text("حركتكِ ونشاطكِ اليومي:", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite, fontSize = 12.sp)
                        }
                        Text(
                            text = "$steps / $targetSteps خطوة",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTheme.MintTeal,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Progress bar
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = SoftTheme.MintTeal,
                        trackColor = SoftTheme.DeepSlate,
                        strokeCap = StrokeCap.Round
                    )

                    // Simulated steps trigger button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                viewModel.addSteps(100)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.MintTeal.copy(alpha = 0.15f)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("👟", fontSize = 11.sp)
                                Text("محاكاة حركة خطوة المشي (+100 خطوة)", color = SoftTheme.MintTeal, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Custom Advice from custom companion name
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SoftTheme.CardSlate.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "💡 $adviceTitle",
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.SoftPink,
                            fontSize = 11.sp
                        )
                        Text(
                            text = adviceText.replace("جوري", companionName),
                            color = SoftTheme.SoftGray,
                            fontSize = 10.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
            
            // Inline Interactive Quick Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quick Action 1: Log Water
                Button(
                    onClick = {
                        viewModel.addWater(250)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.DeepSlate),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("شربت كوب ماء 💧", color = SoftTheme.SoftTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                
                // Quick Action 2: Chat
                Button(
                    onClick = onOpenJouriChat,
                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("تحدثي مع $companionName 🌸", color = SoftTheme.TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

