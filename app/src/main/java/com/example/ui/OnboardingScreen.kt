package com.example.ui

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.onboarding.StepChronicDiseases
import com.example.ui.onboarding.StepCyclePregnancy
import com.example.ui.onboarding.StepPersonalInfo
import com.example.ui.onboarding.StepSummaryAndSave
import com.example.ui.settings.BatteryOptimizationCard
import com.example.viewmodel.WomanCompanionViewModel
import java.util.*

@Composable
fun OnboardingScreen(
    viewModel: WomanCompanionViewModel
) {
    var currentStep by remember { mutableStateOf(1) }
    
    // Form States
    var name by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var birthDateMs by remember { mutableStateOf<Long?>(null) }
    
    var hasHighBp by remember { mutableStateOf(false) }
    var hasLowBp by remember { mutableStateOf(false) }
    var hasDiabetes by remember { mutableStateOf(false) }
    var chronicOthers by remember { mutableStateOf("") }
    
    var isPregnant by remember { mutableStateOf(false) }
    var lastPeriodDateMs by remember { mutableStateOf<Long?>(null) }
    var lastPeriodEndDateMs by remember { mutableStateOf<Long?>(null) }
    
    val context = LocalContext.current
    
    // Gradient backgrounds for extra premium visual appeal
    val backgroundBrush = SoftTheme.BackgroundBrush
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("onboarding_screen_container")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp), // +++ تم التعديل لمنع تداخل أزرار التنقل والمدخلات وضمان الاستجابة الكاملة +++
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            
            // Header Logo with animated pulse effect
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 0.98f,
                targetValue = 1.02f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulseScale"
            )
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer(scaleX = pulseScale, scaleY = pulseScale)
            ) {
                Text(
                    text = "🌸 جوري",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = when (currentStep) {
                        1 -> Color(0xFF2196F3) // Sky Blue
                        2 -> Color(0xFFFFB300) // Sunny Yellow
                        3 -> Color(0xFF00C0A5) // Organic Green/Teal
                        else -> SoftTheme.SoftPink // Radiant Pink
                    },
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "رفيقتكِ المخلصة لرحلة صحية تنبض بالرعاية وحب الذات",
                    color = SoftTheme.SoftGray,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Stepper progress indicator with elegant glass track and strong step colors
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(10.dp)
                    .clip(CircleShape)
                    .background(SoftTheme.CardSlate.copy(alpha = 0.5f))
            ) {
                val animatedProgress by animateFloatAsState(
                    targetValue = currentStep / 4f,
                    animationSpec = spring(stiffness = Spring.StiffnessLow),
                    label = "stepper_progress"
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                colors = when (currentStep) {
                                    1 -> listOf(Color(0xFF90CAF9), Color(0xFF2196F3)) // Vibrant Blue
                                    2 -> listOf(Color(0xFFFFE082), Color(0xFFFFB300)) // Strong Yellow/Amber
                                    3 -> listOf(Color(0xFF80E0D2), Color(0xFF00C0A5)) // Fresh Green/Teal
                                    else -> listOf(SoftTheme.LightPink, SoftTheme.SoftPink) // Radiant Pink
                                }
                            )
                        )
                )
            }
            
            // Elegant Steps text
            Text(
                text = "الخطوة $currentStep من 4",
                color = when (currentStep) {
                    1 -> Color(0xFF2196F3)
                    2 -> Color(0xFFFFB300)
                    3 -> Color(0xFF00C0A5)
                    else -> SoftTheme.SoftPink
                },
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Step Content container with custom animated transition
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { width -> width } + fadeIn(animationSpec = tween(300)))
                            .togetherWith(slideOutHorizontally { width -> -width } + fadeOut(animationSpec = tween(300)))
                    } else {
                        (slideInHorizontally { width -> -width } + fadeIn(animationSpec = tween(300)))
                            .togetherWith(slideOutHorizontally { width -> width } + fadeOut(animationSpec = tween(300)))
                    }
                },
                label = "step_transition",
                modifier = Modifier.fillMaxWidth()
            ) { step ->
                when (step) {
                    1 -> StepPersonalInfo(
                        name = name,
                        onNameChange = { name = it },
                        nickname = nickname,
                        onNicknameChange = { nickname = it },
                        birthDateMs = birthDateMs,
                        onBirthDateChange = { birthDateMs = it }
                    )
                    2 -> StepChronicDiseases(
                        hasHighBp = hasHighBp,
                        onHighBpChange = { hasHighBp = it },
                        hasLowBp = hasLowBp,
                        onLowBpChange = { hasLowBp = it },
                        hasDiabetes = hasDiabetes,
                        onDiabetesChange = { hasDiabetes = it },
                        chronicOthers = chronicOthers,
                        onChronicOthersChange = { chronicOthers = it }
                    )
                    3 -> StepCyclePregnancy(
                        isPregnant = isPregnant,
                        onPregnantChange = { isPregnant = it },
                        lastPeriodDateMs = lastPeriodDateMs,
                        onLastPeriodDateChange = { lastPeriodDateMs = it },
                        lastPeriodEndDateMs = lastPeriodEndDateMs,
                        onLastPeriodEndDateChange = { lastPeriodEndDateMs = it }
                    )
                    4 -> StepSummaryAndSave(
                        name = name,
                        nickname = nickname,
                        birthDateMs = birthDateMs,
                        hasHighBp = hasHighBp,
                        hasLowBp = hasLowBp,
                        hasDiabetes = hasDiabetes,
                        chronicOthers = chronicOthers,
                        isPregnant = isPregnant,
                        lastPeriodDateMs = lastPeriodDateMs,
                        lastPeriodEndDateMs = lastPeriodEndDateMs,
                        onFinish = {
                            if (name.trim().isEmpty()) {
                                Toast.makeText(context, "الرجاء إدخال اسمكِ لمتابعة الرحلة! 🌸", Toast.LENGTH_SHORT).show()
                                currentStep = 1
                            } else {
                                viewModel.saveOnboardingProfile(
                                    name = name,
                                    nickname = nickname.ifEmpty { name },
                                    birthDate = birthDateMs,
                                    hasHighBp = hasHighBp,
                                    hasLowBp = hasLowBp,
                                    hasDiabetes = hasDiabetes,
                                    chronicOthers = chronicOthers,
                                    lastPeriodDate = lastPeriodDateMs,
                                    lastPeriodEndDate = lastPeriodEndDateMs,
                                    isPregnant = isPregnant
                                )
                                Toast.makeText(context, "أهلاً بكِ في عائلتنا! تم تفعيل جوري بنجاح! 🎉🌸", Toast.LENGTH_LONG).show()
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp)) // +++ مسافة أمان أنيقة قبل أزرار التحكم +++

            // +++ تم نقل أزرار التنقل هنا لتكون جزءاً من الصفحة القابلة للتمرير وتجنب حجب حقول الإدخال +++
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep > 1) {
                    Button(
                        onClick = { currentStep-- },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SoftTheme.CardSlate,
                            contentColor = when (currentStep) {
                                2 -> Color(0xFF2196F3)
                                3 -> Color(0xFFFFB300)
                                else -> Color(0xFF00C0A5)
                            }
                        ),
                        border = BorderStroke(1.dp, (when (currentStep) {
                            2 -> Color(0xFF2196F3)
                            3 -> Color(0xFFFFB300)
                            else -> Color(0xFF00C0A5)
                        }).copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .height(52.dp)
                            .widthIn(min = 100.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = "السابق", modifier = Modifier.size(18.dp))
                            Text("السابق", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.width(100.dp))
                }

                if (currentStep < 4) {
                    Button(
                        onClick = {
                            if (currentStep == 1 && name.trim().isEmpty()) {
                                Toast.makeText(context, "الرجاء إدخال اسمكِ للبدء! 💕", Toast.LENGTH_SHORT).show()
                            } else {
                                currentStep++
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when (currentStep) {
                                1 -> Color(0xFF2196F3) // Vibrant Blue
                                2 -> Color(0xFFFFB300) // Vibrant Sunny Yellow
                                3 -> Color(0xFF00C0A5) // Vibrant Green
                                else -> SoftTheme.SoftPink
                            },
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .height(52.dp)
                            .widthIn(min = 110.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("التالي", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "التالي", modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // زر التخطي السريع وتفعيل جوري بملف افتراضي مباشر لتسهيل الدخول السريع والتجربة
            TextButton(
                onClick = {
                    val defaultName = "جميلة"
                    val defaultNickname = "جميلة"
                    val defaultBirthDate = System.currentTimeMillis() - 25L * 365 * 24 * 60 * 60 * 1000 // 25 سنة
                    val defaultLastPeriod = System.currentTimeMillis() - 12L * 7 * 24 * 60 * 60 * 1000 // الأسبوع الـ 12 من الحمل
                    val defaultLastPeriodEnd = defaultLastPeriod + 5L * 24 * 60 * 60 * 1000

                    viewModel.saveOnboardingProfile(
                        name = defaultName,
                        nickname = defaultNickname,
                        birthDate = defaultBirthDate,
                        hasHighBp = false,
                        hasLowBp = false,
                        hasDiabetes = false,
                        chronicOthers = "",
                        lastPeriodDate = defaultLastPeriod,
                        lastPeriodEndDate = defaultLastPeriodEnd,
                        isPregnant = true
                    )
                    Toast.makeText(context, "تم تفعيل جوري فوراً بالملف الافتراضي السريع! 🎉🌸", Toast.LENGTH_LONG).show()
                },
                modifier = Modifier.testTag("quick_skip_onboarding_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("⚡", fontSize = 16.sp)
                    Text(
                        text = "تخطي سريع والدخول فوراً بالملف الافتراضي 🚀",
                        color = SoftTheme.SoftPink,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

