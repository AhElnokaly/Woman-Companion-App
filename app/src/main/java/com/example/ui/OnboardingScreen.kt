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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                            Icon(Icons.Rounded.ArrowForward, contentDescription = "السابق", modifier = Modifier.size(18.dp))
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
                            Icon(Icons.Rounded.ArrowBack, contentDescription = "التالي", modifier = Modifier.size(18.dp))
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

@Composable
fun StepPersonalInfo(
    name: String,
    onNameChange: (String) -> Unit,
    nickname: String,
    onNicknameChange: (String) -> Unit,
    birthDateMs: Long?,
    onBirthDateChange: (Long?) -> Unit
) {
    val context = LocalContext.current
    
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2196F3).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📝", fontSize = 22.sp)
                }
                Column {
                    Text(
                        text = "الترحيب بكِ والاسم المحبب",
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.TextWhite,
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "دعي جوري تتعرف عليكِ لتخصيص كامل للغة الخطاب",
                        color = SoftTheme.SoftGray,
                        fontSize = 11.sp
                    )
                }
            }
            
            Text(
                text = "سلامتكِ يا غالية تهمني جداً، أنا صديقتكِ جوري وأريد أن أناديكِ دائماً بألطف الأسماء وأقربها لقلبكِ لتقديم الدعم الدافئ.",
                color = SoftTheme.SoftGray,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp
            )

            Divider(color = Color(0xFF2196F3).copy(alpha = 0.25f), thickness = 1.dp)

            // Full Name Input
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "ما هو اسمكِ الكريم؟",
                    color = SoftTheme.TextWhite,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    placeholder = { Text("أدخلي اسمكِ هنا...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_name_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SoftTheme.DeepSlate.copy(alpha = 0.5f),
                        unfocusedContainerColor = SoftTheme.DeepSlate.copy(alpha = 0.3f),
                        focusedBorderColor = Color(0xFF2196F3),
                        unfocusedBorderColor = SoftTheme.SoftGray.copy(alpha = 0.3f),
                        focusedTextColor = SoftTheme.TextWhite,
                        unfocusedTextColor = SoftTheme.TextWhite,
                        focusedPlaceholderColor = SoftTheme.SoftGray.copy(alpha = 0.7f),
                        unfocusedPlaceholderColor = SoftTheme.SoftGray.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(14.dp)
                )
            }

            // Nickname Input
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "اسم الدلع (كيف تحبين أن أناديكِ؟) 💕",
                    color = SoftTheme.TextWhite,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                OutlinedTextField(
                    value = nickname,
                    onValueChange = onNicknameChange,
                    placeholder = { Text("مثال: رورو، لولو، ميمي...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_nickname_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SoftTheme.DeepSlate.copy(alpha = 0.5f),
                        unfocusedContainerColor = SoftTheme.DeepSlate.copy(alpha = 0.3f),
                        focusedBorderColor = Color(0xFF2196F3),
                        unfocusedBorderColor = SoftTheme.SoftGray.copy(alpha = 0.3f),
                        focusedTextColor = SoftTheme.TextWhite,
                        unfocusedTextColor = SoftTheme.TextWhite,
                        focusedPlaceholderColor = SoftTheme.SoftGray.copy(alpha = 0.7f),
                        unfocusedPlaceholderColor = SoftTheme.SoftGray.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(14.dp)
                )
            }

            // Birth Date Picker (Large tap target 48dp+)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "تاريخ ميلادكِ المبارك:",
                    color = SoftTheme.TextWhite,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Button(
                    onClick = {
                        val calendar = Calendar.getInstance().apply {
                            if (birthDateMs != null) timeInMillis = birthDateMs
                        }
                        DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                val cal = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, y)
                                    set(Calendar.MONTH, m)
                                    set(Calendar.DAY_OF_MONTH, d)
                                    set(Calendar.HOUR_OF_DAY, 0)
                                    set(Calendar.MINUTE, 0)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }
                                onBirthDateChange(cal.timeInMillis)
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("onboarding_dob_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SoftTheme.DeepSlate.copy(alpha = 0.6f),
                        contentColor = SoftTheme.TextWhite
                    ),
                    border = BorderStroke(1.dp, Color(0xFF2196F3).copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("🗓️", fontSize = 18.sp)
                            Text(
                                text = if (birthDateMs != null) formatGregorianDate(birthDateMs) else "انقري لتحديد تاريخ ميلادكِ بدقة",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF2196F3), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun StepChronicDiseases(
    hasHighBp: Boolean,
    onHighBpChange: (Boolean) -> Unit,
    hasLowBp: Boolean,
    onLowBpChange: (Boolean) -> Unit,
    hasDiabetes: Boolean,
    onDiabetesChange: (Boolean) -> Unit,
    chronicOthers: String,
    onChronicOthersChange: (String) -> Unit
) {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(SoftTheme.MintTeal.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🩺", fontSize = 22.sp)
                }
                Column {
                    Text(
                        text = "الوضع الصحي والوقائي",
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.TextWhite,
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "جوري ستقوم بتعديل المحتوى بناءً على الأمراض المزمنة",
                        color = SoftTheme.SoftGray,
                        fontSize = 11.sp
                    )
                }
            }
            
            Text(
                text = "عزيزتي، صحتكِ فوق كل شيء. تفاصيل السكر والضغط بالغة الأهمية لتستطيع جوري تنبيهكِ لما يفيدكِ وما قد يضركِ من أغذية وتمارين ووجبات ومشروبات ساخنة.",
                color = SoftTheme.SoftGray,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp
            )

            Divider(color = Color(0xFFFFB300).copy(alpha = 0.25f), thickness = 1.dp)

            // High Blood Pressure (Fully Interactive Row Target Compliance)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (hasHighBp) Color(0xFFFFB300).copy(alpha = 0.1f) else SoftTheme.DeepSlate.copy(alpha = 0.3f))
                    .border(1.dp, if (hasHighBp) Color(0xFFFFB300).copy(alpha = 0.5f) else Color.Transparent, RoundedCornerShape(16.dp))
                    .clickable { onHighBpChange(!hasHighBp) }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Checkbox(
                    checked = hasHighBp,
                    onCheckedChange = onHighBpChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFFFFB300),
                        checkmarkColor = Color.Black
                    )
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ضغط دم مرتفع (عالي) 📈",
                        color = SoftTheme.TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "جوري ستنبهكِ للابتعاد التام عن الموالح والصوديوم وتحثكِ على الكركديه البارد.",
                        color = SoftTheme.SoftGray,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            // Low Blood Pressure (Fully Interactive Row Target Compliance)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (hasLowBp) Color(0xFFFFB300).copy(alpha = 0.1f) else SoftTheme.DeepSlate.copy(alpha = 0.3f))
                    .border(1.dp, if (hasLowBp) Color(0xFFFFB300).copy(alpha = 0.5f) else Color.Transparent, RoundedCornerShape(16.dp))
                    .clickable { onLowBpChange(!hasLowBp) }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Checkbox(
                    checked = hasLowBp,
                    onCheckedChange = onLowBpChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFFFFB300),
                        checkmarkColor = Color.Black
                    )
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ضغط دم منخفض (واطي) 📉",
                        color = SoftTheme.TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "جوري ستذكركِ بمشروبات مرطبة لزيادة ضغط الدم وتجنب الإرهاق والدوار المفاجئ.",
                        color = SoftTheme.SoftGray,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            // Diabetes (Fully Interactive Row Target Compliance)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (hasDiabetes) Color(0xFFFFB300).copy(alpha = 0.1f) else SoftTheme.DeepSlate.copy(alpha = 0.3f))
                    .border(1.dp, if (hasDiabetes) Color(0xFFFFB300).copy(alpha = 0.5f) else Color.Transparent, RoundedCornerShape(16.dp))
                    .clickable { onDiabetesChange(!hasDiabetes) }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Checkbox(
                    checked = hasDiabetes,
                    onCheckedChange = onDiabetesChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFFFFB300),
                        checkmarkColor = Color.Black
                    )
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "مرض السكري 🩸",
                        color = SoftTheme.TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "جوري ستقوم بمراقبة الحلويات والوجبات وحساب النشويات لتجنب طفرات الأنسولين.",
                        color = SoftTheme.SoftGray,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            // Chronic Others Input
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "هل لديكِ حالات صحية أو حساسية أخرى؟",
                    color = SoftTheme.TextWhite,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                OutlinedTextField(
                    value = chronicOthers,
                    onValueChange = onChronicOthersChange,
                    placeholder = { Text("مثال: أنيميا، حساسية لاكتوز، نقص فيتامين د...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_chronic_others"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SoftTheme.DeepSlate.copy(alpha = 0.5f),
                        unfocusedContainerColor = SoftTheme.DeepSlate.copy(alpha = 0.3f),
                        focusedBorderColor = Color(0xFFFFB300),
                        unfocusedBorderColor = SoftTheme.SoftGray.copy(alpha = 0.3f),
                        focusedTextColor = SoftTheme.TextWhite,
                        unfocusedTextColor = SoftTheme.TextWhite,
                        focusedPlaceholderColor = SoftTheme.SoftGray.copy(alpha = 0.7f),
                        unfocusedPlaceholderColor = SoftTheme.SoftGray.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(14.dp)
                )
            }
        }
    }
}

@Composable
fun StepCyclePregnancy(
    isPregnant: Boolean,
    onPregnantChange: (Boolean) -> Unit,
    lastPeriodDateMs: Long?,
    onLastPeriodDateChange: (Long?) -> Unit,
    lastPeriodEndDateMs: Long?,
    onLastPeriodEndDateChange: (Long?) -> Unit
) {
    val context = LocalContext.current
    
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00C0A5).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🤰", fontSize = 22.sp)
                }
                Column {
                    Text(
                        text = "تتبع الحمل والدورة الشهرية",
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.TextWhite,
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "حساب دقيق لأسابيع الحمل أو نافذة الخصوبة القادمة",
                        color = SoftTheme.SoftGray,
                        fontSize = 11.sp
                    )
                }
            }
            
            Text(
                text = "دعينا نحدد وضعكِ الحالي بدقة. تفعيل طور الحمل سيفتح لكِ أدلة أسبوعية مفصلة عن نمو جنينكِ، بينما تتبع الدورة سيحسب مواعيد التبويض والخصوبة.",
                color = SoftTheme.SoftGray,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp
            )

            Divider(color = Color(0xFF00C0A5).copy(alpha = 0.25f), thickness = 1.dp)

            // Pregnancy Toggle Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SoftTheme.DeepSlate.copy(alpha = 0.5f))
                    .border(1.dp, Color(0xFF00C0A5).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .clickable { onPregnantChange(!isPregnant) }
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "هل يوجد حمل حالياً؟ 🤰",
                        color = SoftTheme.TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "نعم، أريد تشغيل حاسبة الحمل وأدوات صحة الجنين اليومية.",
                        color = SoftTheme.SoftGray,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
                Switch(
                    checked = isPregnant,
                    onCheckedChange = onPregnantChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF00C0A5),
                        checkedTrackColor = Color(0xFF80E0D2),
                        uncheckedThumbColor = SoftTheme.SoftGray,
                        uncheckedTrackColor = SoftTheme.DeepSlate
                    )
                )
            }

            // Last Period Date Picker
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (isPregnant) "تاريخ بداية آخر دورة شهرية (لحساب موعد الولادة المتوقع):" else "تاريخ بداية آخر دورة شهرية (لحساب دورتكِ القادمة):",
                    color = SoftTheme.TextWhite,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Button(
                    onClick = {
                        val calendar = Calendar.getInstance().apply {
                            if (lastPeriodDateMs != null) timeInMillis = lastPeriodDateMs
                        }
                        DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                val cal = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, y)
                                    set(Calendar.MONTH, m)
                                    set(Calendar.DAY_OF_MONTH, d)
                                    set(Calendar.HOUR_OF_DAY, 0)
                                    set(Calendar.MINUTE, 0)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }
                                onLastPeriodDateChange(cal.timeInMillis)
                                // Prepopulate end date 5 days later
                                if (lastPeriodEndDateMs == null) {
                                    onLastPeriodEndDateChange(cal.timeInMillis + 5L * 24 * 60 * 60 * 1000)
                                }
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SoftTheme.DeepSlate.copy(alpha = 0.6f),
                        contentColor = SoftTheme.TextWhite
                    ),
                    border = BorderStroke(1.dp, Color(0xFF00C0A5).copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("🗓️", fontSize = 18.sp)
                            Text(
                                text = if (lastPeriodDateMs != null) formatGregorianDate(lastPeriodDateMs) else "انقري لتحديد تاريخ بدء آخر دورة",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = Color(0xFF00C0A5), modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Last Period End Date Picker
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "تاريخ انتهاء الدورة الشهرية الأخيرة (إن كنتِ تذكرينه):",
                    color = SoftTheme.TextWhite,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Button(
                    onClick = {
                        val calendar = Calendar.getInstance().apply {
                            if (lastPeriodEndDateMs != null) timeInMillis = lastPeriodEndDateMs
                        }
                        DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                val cal = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, y)
                                    set(Calendar.MONTH, m)
                                    set(Calendar.DAY_OF_MONTH, d)
                                    set(Calendar.HOUR_OF_DAY, 0)
                                    set(Calendar.MINUTE, 0)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }
                                onLastPeriodEndDateChange(cal.timeInMillis)
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SoftTheme.DeepSlate.copy(alpha = 0.6f),
                        contentColor = SoftTheme.TextWhite
                    ),
                    border = BorderStroke(1.dp, Color(0xFF00C0A5).copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("🏁", fontSize = 18.sp)
                            Text(
                                text = if (lastPeriodEndDateMs != null) formatGregorianDate(lastPeriodEndDateMs) else "انقري لتحديد تاريخ انتهاء الدورة",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Icon(Icons.Default.Done, contentDescription = null, tint = Color(0xFF00C0A5), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

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
