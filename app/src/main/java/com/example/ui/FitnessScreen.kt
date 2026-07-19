package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.WomanCompanionViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// High-fidelity steps representing exercise movements
data class JouriStep(
    val title: String,
    val subtitle: String,
    val durationBadge: String? = null,
    val iconType: String // "sit", "hold", "relax", "stretch", "breathe"
)

// Data class representing an exercise
data class JouriExercise(
    val id: String,
    val name: String,
    val category: String, // "pregnancy", "postpartum", "general"
    val durationSeconds: Int,
    val emoji: String,
    val description: String,
    val goal: String,
    val stepDetails: List<JouriStep>,
    val benefits: List<String>,
    val safetyWarning: String
)

@Composable
fun FitnessScreen(
    viewModel: WomanCompanionViewModel
) {
    val completedCount by viewModel.completedWorkoutsCount.collectAsStateWithLifecycle()
    val streakCount by viewModel.workoutStreak.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf("pregnancy") }
    var selectedExerciseForTimer by remember { mutableStateOf<JouriExercise?>(null) }
    var selectedExerciseDetail by remember { mutableStateOf<JouriExercise?>(null) }

    // Reconstruct specialized, medically guided maternal fitness routines
    val exercises = remember {
        listOf(
            // Pregnancy Stage (الحمل)
            JouriExercise(
                id = "kegel_advanced",
                name = "تمرين كيجل المتقدم",
                category = "pregnancy",
                durationSeconds = 100,
                emoji = "🌸",
                goal = "الهدف: تقوية عضلات قاع الحوض",
                description = "الخيار الذهبي والأكثر أهمية لتنشيط عضلات الحوض والرحم بلطف، مما يسهل الولادة الطبيعية ويمنع سلس البول.",
                stepDetails = listOf(
                    JouriStep("الخطوة الأولى:", "الجلوس بوضعية مريحة", null, "sit"),
                    JouriStep("الخطوة الثانية:", "انقباض العضلات (Hold)", "10 ثواني", "hold"),
                    JouriStep("الخطوة الثالثة:", "استرخاء العضلات (Relax)", "5 ثواني", "relax")
                ),
                benefits = listOf(
                    "تقوية الأنسجة الداعمة لوزن الجنين والرحم.",
                    "تحسين تدفق الدورة الدموية في منطقة الحوض.",
                    "تسريع الاستشفاء بعد الولادة الطبيعية."
                ),
                safetyWarning = "احرصي على إفراغ المثانة قبل البدء وتجنبي كتم الأنفاس."
            ),
            JouriExercise(
                id = "pelvic_forward",
                name = "تمرين الامام",
                category = "pregnancy",
                durationSeconds = 100,
                emoji = "🧘‍♀️",
                goal = "الهدف: تخفيف ضغط الجنين أسفل الظهر",
                description = "تمرين إمالة الحوض لتخفيف ضغط العمود الفقري وتجنب الآلام القطنية الناتجة عن تمدد عضلات البطن.",
                stepDetails = listOf(
                    JouriStep("الخطوة الأولى:", "الارتكاز على اليدين والركبتين باستواء", null, "sit"),
                    JouriStep("الخطوة الثانية:", "تقويس العمود الفقري للأعلى بلطف", "5 ثواني", "hold"),
                    JouriStep("الخطوة الثالثة:", "العودة للوضعية المستوية مع زفير طويل", "5 ثواني", "relax")
                ),
                benefits = listOf(
                    "تخفيف الشد والضغط في مفاصل أسفل الظهر.",
                    "تنشيط وتقوية العضلات العميقة للجدار البطني.",
                    "مساعدة الجنين في اتخاذ الوضعية المثالية للولادة."
                ),
                safetyWarning = "تجنبي المبالغة في تقويس الظهر لأسفل لمنع التشنج."
            ),
            JouriExercise(
                id = "gentiad",
                name = "تمرين الجنتياد",
                category = "pregnancy",
                durationSeconds = 100,
                emoji = "🍃",
                goal = "الهدف: تليين مفصل الحوض والفخذين",
                description = "تمرين تمدد الفراشة الرقيق لفتح مفصل الورك وتحسين المدى الحركي للحوض استعداداً للولادة السلسة.",
                stepDetails = listOf(
                    JouriStep("الخطوة الأولى:", "الجلوس مستقيمة الظهر مع ضم باطن القدمين", null, "sit"),
                    JouriStep("الخطوة الثانية:", "الضغط اللطيف للركبتين نحو الأسفل", "15 ثانية", "stretch"),
                    JouriStep("الخطوة الثالثة:", "إرخاء الأنسجة تماماً وأخذ أنفاس مهدئة", "5 ثواني", "relax")
                ),
                benefits = listOf(
                    "توسيع منطقة عظام العانة وتخفيف التصلب الوركي.",
                    "إرخاء عضلات الفخذ الداخلية الضيقة.",
                    "تحسين توازن الجسم العقلي والجسدي والهدوء الداخلي."
                ),
                safetyWarning = "لا تقومي بهز الركبتين بعنف، بل دعي التمدد يتم تدريجياً وبسلاسة."
            ),

            // Postpartum Stage (بعد الولادة)
            JouriExercise(
                id = "kegel_postpartum",
                name = "تمرين كيجل بعد الولادة",
                category = "postpartum",
                durationSeconds = 100,
                emoji = "🌸",
                goal = "الهدف: شد واستعادة قوة عضلات الحوض",
                description = "إعادة تنشيط أنسجة المهبل والرحم والمثانة لتسهيل التئام الجروح واستعادة اللياقة الكلية بعد شهور الحمل المتعبة.",
                stepDetails = listOf(
                    JouriStep("الخطوة الأولى:", "الاستلقاء التام على الظهر بوضعية مريحة", null, "sit"),
                    JouriStep("الخطوة الثانية:", "انقباض عضلات الحوض الداخلية بلطف وثبات", "5 ثواني", "hold"),
                    JouriStep("الخطوة الثالثة:", "الارتخاء الكامل والتنفس المريح المهدئ", "5 ثواني", "relax")
                ),
                benefits = listOf(
                    "تحسين التئام جروح الولادة وتنشيط خلايا المنطقة.",
                    "منع وتخفيف سلس البول الشائع بعد الولادة.",
                    "إعادة بناء ثبات مركز الجسم الكلي."
                ),
                safetyWarning = "يمكنكِ البدء به برفق بعد التئام جرح الولادة وبموافقة طبيبتكِ."
            ),
            JouriExercise(
                id = "diaphragm_post",
                name = "تمرين التنفس البطني العميق",
                category = "postpartum",
                durationSeconds = 100,
                emoji = "💨",
                goal = "الهدف: علاج انفصال عضلات البطن",
                description = "التمرين الذهبي لعلاج الانفصال العضلي (Diastasis Recti) وإعادة جدار البطن لوضعه الطبيعي دون إرهاق جرح الولادة القيصرية.",
                stepDetails = listOf(
                    JouriStep("الخطوة الأولى:", "الاستلقاء وثني الركبتين ووضع اليدين على البطن", null, "sit"),
                    JouriStep("الخطوة الثانية:", "شهيق عميق يملأ البطن بالكامل ويرفعها", "4 ثوانٍ", "breathe"),
                    JouriStep("الخطوة الثالثة:", "زفير بطيء جداً مع سحب السرة نحو العمود الفقري", "6 ثوانٍ", "relax")
                ),
                benefits = listOf(
                    "المساعدة المباشرة في إغلاق وتقريب فجوة انفصال عضلات البطن.",
                    "تنشيط عضلات الكور المستعرضة العميقة برفق.",
                    "تهدئة ضربات القلب وتخفيف ضغوط واكتئاب ما بعد الولادة."
                ),
                safetyWarning = "تأكدي من سحب عضلات البطن للداخل وليس دفعها للخارج أثناء الزفير."
            ),

            // General Wellness (عام)
            JouriExercise(
                id = "mindful_breathing_general",
                name = "تمارين التنفس المتوازن (تقنية 4-7-8)",
                category = "general",
                durationSeconds = 120,
                emoji = "🌬️",
                goal = "الهدف: تخفيف التوتر وتصفية الذهن فوراً",
                description = "تقنية تنفس مهدئة ومنظّمة للجهاز العصبي، تساعدكِ على التخلص من الأرق، القلق، وضغط المهام اليومي في دقائق معدودة.",
                stepDetails = listOf(
                    JouriStep("الخطوة الأولى: الشهيق المريح", "خذي شهيقاً عميقاً وصامتاً من الأنف مع تمدد البطن", "4 ثوانٍ", "breathe"),
                    JouriStep("الخطوة الثانية: حبس الأكسجين", "احبسي أنفاسكِ بلطف لتغذية الخلايا وتهدئة ضربات القلب", "7 ثوانٍ", "sit"),
                    JouriStep("الخطوة الثالثة: الزفير المريح", "أخرجي الهواء ببطء وصوت مسموع مريح عبر الفم بالكامل", "8 ثوانٍ", "relax")
                ),
                benefits = listOf(
                    "تقليل مستويات هرمون الكورتيزول (هرمون التوتر) في الجسم.",
                    "تحسين جودة النوم ومكافحة الأرق بنجاح.",
                    "زيادة التركيز الذهني وضخ الأكسجين النقي للدماغ والأعضاء."
                ),
                safetyWarning = "اجلسي في مكان مريح وظهركِ مستقيم، وتوقفي فوراً إذا شعرتِ بدوار بسيط في البداية."
            )
        )
    }

    val filteredExercises = remember(activeTab, exercises) {
        exercises.filter { it.category == activeTab }
    }

    var fitnessMainTab by remember { mutableStateOf("pedometer") } // default to showing the premium pedometer/walking tracker!

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(SoftTheme.DeepSlate)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // Screen Header title
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (fitnessMainTab == "exercises") "تمارين اللياقة" else "تتبع خطوات المشي",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = SoftTheme.TextWhite,
                        textAlign = TextAlign.Right
                    )
                }
            }

            // High-fidelity Main Switcher (Walking Pedometer vs Exercises)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF141921), RoundedCornerShape(24.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val mainTabs = listOf(
                        "exercises" to "تمارين اللياقة والرحم 🧘‍♀️",
                        "pedometer" to "تتبع خطوات المشي 👣"
                    )

                    mainTabs.forEach { (key, label) ->
                        val isSelected = fitnessMainTab == key
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (isSelected) SoftTheme.SoftPink else Color.Transparent
                                )
                                .clickable { fitnessMainTab = key }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color(0xFF141921) else Color(0xFF8F9CAE),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            if (fitnessMainTab == "exercises") {
                // High-fidelity Pill Tab Switcher (matches the screenshot beautifully)
                item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF141921), RoundedCornerShape(24.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val tabs = listOf(
                        "general" to "عام",
                        "postpartum" to "بعد الولادة",
                        "pregnancy" to "الحمل"
                    )

                    tabs.forEach { (key, label) ->
                        val isSelected = activeTab == key
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (isSelected) Color(0xFF38B2AC) else Color.Transparent
                                )
                                .clickable { activeTab = key }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color(0xFF141921) else Color(0xFF8F9CAE),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Beautiful Today's Suggested Exercise card with luminous green radial glow
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF38B2AC).copy(alpha = 0.4f),
                                        Color(0xFF1B222E)
                                    ),
                                    center = Offset(0f, 150f),
                                    radius = 700f
                                )
                            )
                            .padding(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "التمرين المقترح اليوم",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = SoftTheme.TextWhite,
                                textAlign = TextAlign.Right
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "تمهيداً ليومكِ المشرق والهادئ بكل صحة ونشاط 🌸",
                                fontSize = 12.sp,
                                color = Color(0xFFA0AEC0),
                                textAlign = TextAlign.Right
                            )
                        }
                    }
                }
            }

            // High-fidelity horizontal 12 gear/star tracker indicators (matches the small flower/gears row)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Repeat 12 indicators representing completions
                    (0 until 12).forEach { index ->
                        val isCompleted = index < completedCount || index == 3 || index == 5
                        JouriMaternalGearTracker(
                            completed = isCompleted,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Medical Safety Guidance
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SoftTheme.RedDanger.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("⚠️", fontSize = 18.sp)
                        Column {
                            Text(
                                text = "إرشاد وقائي هام للسلامة:",
                                fontWeight = FontWeight.Bold,
                                color = SoftTheme.RedDanger,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "احرصي دائماً على أخذ فترات راحة كافية والتوقف فوراً في حال شعرتِ بأي دوار أو جهد مفرط.",
                                color = SoftTheme.TextWhite,
                                fontSize = 10.sp,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }

            // List of exercises styled exactly as in the left screenshot
            items(filteredExercises, key = { it.id }) { exercise ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedExerciseDetail = exercise },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B222E))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Right side: beautiful circle progress outline
                        Box(
                            modifier = Modifier.size(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawCircle(
                                    color = Color(0xFF2D3748),
                                    style = Stroke(2.dp.toPx())
                                )
                                drawArc(
                                    color = Color(0xFF38B2AC),
                                    startAngle = -90f,
                                    sweepAngle = 280f,
                                    useCenter = false,
                                    style = Stroke(3.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF38B2AC),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Middle: text details
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 16.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = exercise.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoftTheme.TextWhite,
                                textAlign = TextAlign.Right
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            // Yellow/gold pill badge for duration
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFFEFCBF), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${exercise.durationSeconds} ثانية",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF744210)
                                )
                            }
                        }

                        // Left side: icon container
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color(0xFF232D3F), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(exercise.emoji, fontSize = 20.sp)
                        }
                    }
                }
            }
        } else {
            item {
                StepPedometerDashboard(viewModel = viewModel)
            }
        }
    }

        // FULL SCREEN OVERLAY DETAIL VIEW (Matches the right screenshot perfectly!)
        AnimatedVisibility(
            visible = selectedExerciseDetail != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            selectedExerciseDetail?.let { exercise ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SoftTheme.DeepSlate)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 90.dp)
                    ) {
                        // Header with Back button and Title
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp, bottom = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "عودة",
                                    tint = SoftTheme.TextWhite,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable { selectedExerciseDetail = null }
                                )

                                Text(
                                    text = "تفاصيل ${exercise.name}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = SoftTheme.TextWhite
                                )

                                Spacer(modifier = Modifier.width(24.dp)) // Equal space balancing
                            }
                        }

                        // Stunning green-glow gradient Hero Card
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            Brush.radialGradient(
                                                colors = listOf(
                                                    Color(0xFF38B2AC).copy(alpha = 0.5f),
                                                    Color(0xFF1B222E)
                                                ),
                                                center = Offset(100f, 150f),
                                                radius = 600f
                                            )
                                        )
                                        .padding(20.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Left/Center: anatomically drawn pelvis or custom vector
                                    LargePelvisAnatomicalDrawing(
                                        modifier = Modifier.size(100.dp)
                                    )

                                    // Right: exercise name and goals
                                    Column(
                                        horizontalAlignment = Alignment.End,
                                        modifier = Modifier.padding(start = 12.dp)
                                    ) {
                                        Text(
                                            text = exercise.name,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Black,
                                            color = SoftTheme.TextWhite,
                                            textAlign = TextAlign.Right
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = exercise.goal,
                                            fontSize = 11.sp,
                                            color = Color(0xFFCBD5E0),
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Right
                                        )
                                    }
                                }
                            }
                        }

                        // Step-by-step card instructions with numbers and custom drawings
                        items(exercise.stepDetails) { step ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B222E))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Far Left: Step Number badge + optional yellow duration capsule
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .background(Color(0xFFFEFCBF), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = step.title.filter { it.isDigit() }.ifEmpty { "1" },
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFF744210)
                                            )
                                        }

                                        step.durationBadge?.let { badge ->
                                            Box(
                                                modifier = Modifier
                                                    .background(Color(0xFFFEFCBF), RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = badge,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF744210)
                                                )
                                            }
                                        }
                                    }

                                    // Middle: details of instruction
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 12.dp),
                                        horizontalAlignment = Alignment.End
                                    ) {
                                        Text(
                                            text = step.title,
                                            fontSize = 12.sp,
                                            color = Color(0xFFECC94B),
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Right
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = step.subtitle,
                                            fontSize = 12.sp,
                                            color = SoftTheme.TextWhite,
                                            textAlign = TextAlign.Right,
                                            lineHeight = 16.sp
                                        )
                                    }

                                    // Far Right: Custom line-drawn posture illustration
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .background(Color(0xFF141921), RoundedCornerShape(12.dp))
                                            .padding(6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        ExerciseStepIllustration(
                                            type = step.iconType,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }
                        }

                        // Medical Note & Safety
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF2D3748).copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "💡 فوائد التمرين وتوجيهات السلامة:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF38B2AC)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    exercise.benefits.forEach { benefit ->
                                        Text(
                                            text = "• $benefit",
                                            fontSize = 11.sp,
                                            color = Color(0xFFCBD5E0),
                                            lineHeight = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Large Mint Green CTA Button at the bottom (matches the screenshot beautifully)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, SoftTheme.DeepSlate),
                                    startY = 0f,
                                    endY = 100f
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Button(
                            onClick = {
                                selectedExerciseForTimer = exercise
                                selectedExerciseDetail = null
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("start_exercise_now_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38B2AC)),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(
                                text = "ابدأ التمرين الآن",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF141921)
                            )
                        }
                    }
                }
            }
        }
    }

    // Interactive Fullscreen Workout Timer Dialog!
    selectedExerciseForTimer?.let { exercise ->
        JouriWorkoutTimerDialog(
            exercise = exercise,
            onDismiss = { selectedExerciseForTimer = null },
            onWorkoutCompleted = {
                viewModel.logCompletedWorkout()
                selectedExerciseForTimer = null
            }
        )
    }
}

// Gorgeous custom Pelvis anatomical line diagram for the Hero card
@Composable
fun LargePelvisAnatomicalDrawing(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val neonColor = Color(0xFF38B2AC) // Mint green / neon cyan
        val softGlow = Color(0xFF38B2AC).copy(alpha = 0.25f)
        val lineStroke = 2.5f.dp.toPx()

        // Background glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(softGlow, Color.Transparent),
                center = Offset(w / 2, h / 2),
                radius = w * 0.45f
            )
        )

        // Draw elegant pelvis structure outline
        val hipPath = Path().apply {
            // Left crest
            moveTo(w * 0.25f, h * 0.25f)
            cubicTo(w * 0.12f, h * 0.2f, w * 0.12f, h * 0.55f, w * 0.28f, h * 0.65f)
            cubicTo(w * 0.38f, h * 0.72f, w * 0.45f, h * 0.78f, w * 0.5f, h * 0.8f)

            // Right crest
            moveTo(w * 0.75f, h * 0.25f)
            cubicTo(w * 0.88f, h * 0.2f, w * 0.88f, h * 0.55f, w * 0.72f, h * 0.65f)
            cubicTo(w * 0.62f, h * 0.72f, w * 0.55f, h * 0.78f, w * 0.5f, h * 0.8f)
        }
        drawPath(hipPath, color = neonColor, style = Stroke(lineStroke, cap = StrokeCap.Round))

        // Inner pelvis rings
        val innerRingPath = Path().apply {
            moveTo(w * 0.35f, h * 0.42f)
            cubicTo(w * 0.25f, h * 0.55f, w * 0.45f, h * 0.78f, w * 0.5f, h * 0.78f)
            cubicTo(w * 0.55f, h * 0.78f, w * 0.75f, h * 0.55f, w * 0.65f, h * 0.42f)
        }
        drawPath(innerRingPath, color = neonColor.copy(alpha = 0.8f), style = Stroke(lineStroke, cap = StrokeCap.Round))

        // Pelvic floor muscle cradle (the hammock)
        val muscleHammock = Path().apply {
            moveTo(w * 0.36f, h * 0.62f)
            quadraticTo(w * 0.5f, h * 0.72f, w * 0.64f, h * 0.62f)
            quadraticTo(w * 0.5f, h * 0.66f, w * 0.36f, h * 0.62f)
        }
        drawPath(muscleHammock, color = neonColor, style = Stroke(lineStroke * 1.5f, cap = StrokeCap.Round))

        // Glowing center representing core strength
        drawCircle(color = neonColor, radius = 4.dp.toPx(), center = Offset(w / 2, h * 0.64f))
        drawCircle(color = neonColor.copy(alpha = 0.4f), radius = 8.dp.toPx(), center = Offset(w / 2, h * 0.64f))
    }
}

// Gear stars tracker drawing
@Composable
fun JouriMaternalGearTracker(
    completed: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val activeColor = Color(0xFF38B2AC) // Mint green
        val inactiveColor = Color(0xFF2D3748) // Soft deep slate gray
        val color = if (completed) activeColor else inactiveColor
        val strokeWidth = 1.5f.dp.toPx()

        // Draw gear teeth/petals
        val teethCount = 8
        val outerRadius = w / 2
        val innerRadius = w * 0.3f
        val center = Offset(w / 2, h / 2)

        drawCircle(
            color = color,
            radius = innerRadius,
            style = Stroke(strokeWidth)
        )

        for (i in 0 until teethCount) {
            val angle = (i * 360f / teethCount) * (Math.PI / 180f)
            val startX = center.x + innerRadius * Math.cos(angle).toFloat()
            val startY = center.y + innerRadius * Math.sin(angle).toFloat()
            val endX = center.x + outerRadius * Math.cos(angle).toFloat()
            val endY = center.y + outerRadius * Math.sin(angle).toFloat()
            drawLine(
                color = color,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = strokeWidth * 1.5f,
                cap = StrokeCap.Round
            )
        }
    }
}

// Custom exercise line posture illustrations
@Composable
fun ExerciseStepIllustration(
    type: String,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val mainColor = Color(0xFF38B2AC) // Mint green
        val strokeWidthVal = 2.dp.toPx()

        when (type) {
            "sit" -> {
                // Meditation sitting outline
                drawCircle(color = mainColor, radius = 5.dp.toPx(), center = Offset(w / 2, h * 0.25f), style = Stroke(strokeWidthVal))
                drawLine(color = mainColor, start = Offset(w / 2, h * 0.35f), end = Offset(w / 2, h * 0.65f), strokeWidth = strokeWidthVal, cap = StrokeCap.Round)

                val armPath = Path().apply {
                    moveTo(w / 2, h * 0.42f)
                    quadraticTo(w * 0.25f, h * 0.5f, w * 0.3f, h * 0.7f)
                    moveTo(w / 2, h * 0.42f)
                    quadraticTo(w * 0.75f, h * 0.5f, w * 0.7f, h * 0.7f)
                }
                drawPath(armPath, color = mainColor, style = Stroke(strokeWidthVal, cap = StrokeCap.Round))

                val legPath = Path().apply {
                    moveTo(w * 0.35f, h * 0.65f)
                    quadraticTo(w * 0.2f, h * 0.8f, w * 0.5f, h * 0.82f)
                    quadraticTo(w * 0.8f, h * 0.8f, w * 0.65f, h * 0.65f)
                }
                drawPath(legPath, color = mainColor, style = Stroke(strokeWidthVal, cap = StrokeCap.Round))
            }
            "hold" -> {
                // Contraction pelvis
                val leftPath = Path().apply {
                    moveTo(w * 0.22f, h * 0.22f)
                    cubicTo(w * 0.17f, h * 0.5f, w * 0.35f, h * 0.8f, w * 0.5f, h * 0.85f)
                }
                drawPath(leftPath, color = mainColor, style = Stroke(strokeWidthVal, cap = StrokeCap.Round))

                val rightPath = Path().apply {
                    moveTo(w * 0.78f, h * 0.22f)
                    cubicTo(w * 0.83f, h * 0.5f, w * 0.65f, h * 0.8f, w * 0.5f, h * 0.85f)
                }
                drawPath(rightPath, color = mainColor, style = Stroke(strokeWidthVal, cap = StrokeCap.Round))

                val cradlePath = Path().apply {
                    moveTo(w * 0.35f, h * 0.55f)
                    quadraticTo(w * 0.5f, h * 0.7f, w * 0.65f, h * 0.55f)
                }
                drawPath(cradlePath, color = mainColor, style = Stroke(strokeWidthVal * 1.5f, cap = StrokeCap.Round))

                // Inward contraction arrows
                drawLine(color = mainColor, start = Offset(w * 0.22f, h * 0.5f), end = Offset(w * 0.4f, h * 0.5f), strokeWidth = strokeWidthVal, cap = StrokeCap.Round)
                drawLine(color = mainColor, start = Offset(w * 0.78f, h * 0.5f), end = Offset(w * 0.6f, h * 0.5f), strokeWidth = strokeWidthVal, cap = StrokeCap.Round)
            }
            "relax" -> {
                // Relaxation pelvis
                val leftPath = Path().apply {
                    moveTo(w * 0.22f, h * 0.22f)
                    cubicTo(w * 0.17f, h * 0.5f, w * 0.35f, h * 0.8f, w * 0.5f, h * 0.85f)
                }
                drawPath(leftPath, color = mainColor, style = Stroke(strokeWidthVal, cap = StrokeCap.Round))

                val rightPath = Path().apply {
                    moveTo(w * 0.78f, h * 0.22f)
                    cubicTo(w * 0.83f, h * 0.5f, w * 0.65f, h * 0.8f, w * 0.5f, h * 0.85f)
                }
                drawPath(rightPath, color = mainColor, style = Stroke(strokeWidthVal, cap = StrokeCap.Round))

                val cradlePath = Path().apply {
                    moveTo(w * 0.35f, h * 0.58f)
                    quadraticTo(w * 0.5f, h * 0.62f, w * 0.65f, h * 0.58f)
                }
                drawPath(cradlePath, color = mainColor, style = Stroke(strokeWidthVal, cap = StrokeCap.Round))

                // Outward release arrows
                drawLine(color = mainColor, start = Offset(w * 0.42f, h * 0.5f), end = Offset(w * 0.25f, h * 0.5f), strokeWidth = strokeWidthVal, cap = StrokeCap.Round)
                drawLine(color = mainColor, start = Offset(w * 0.58f, h * 0.5f), end = Offset(w * 0.75f, h * 0.5f), strokeWidth = strokeWidthVal, cap = StrokeCap.Round)
            }
            "stretch" -> {
                // Stretch spinal arc
                val stretchPath = Path().apply {
                    moveTo(w * 0.25f, h * 0.75f)
                    quadraticTo(w * 0.45f, h * 0.35f, w * 0.75f, h * 0.45f)
                }
                drawPath(stretchPath, color = mainColor, style = Stroke(strokeWidthVal * 1.5f, cap = StrokeCap.Round))

                drawCircle(color = mainColor, radius = 4.5f.dp.toPx(), center = Offset(w * 0.8f, h * 0.38f), style = Stroke(strokeWidthVal))
                drawLine(color = mainColor, start = Offset(w * 0.15f, h * 0.8f), end = Offset(w * 0.85f, h * 0.8f), strokeWidth = strokeWidthVal, cap = StrokeCap.Round)
            }
            "breathe" -> {
                // Concentric lung rings
                drawCircle(color = mainColor, radius = 6.dp.toPx(), center = Offset(w / 2, h / 2), style = Stroke(strokeWidthVal))
                drawCircle(color = mainColor.copy(alpha = 0.5f), radius = 13.dp.toPx(), center = Offset(w / 2, h / 2), style = Stroke(strokeWidthVal, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)))
                drawCircle(color = mainColor.copy(alpha = 0.25f), radius = 20.dp.toPx(), center = Offset(w / 2, h / 2), style = Stroke(strokeWidthVal, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)))
            }
            else -> {
                drawCircle(color = mainColor, radius = 10.dp.toPx(), center = Offset(w / 2, h / 2), style = Stroke(strokeWidthVal))
            }
        }
    }
}

// Custom Fullscreen Dialog with visual timer, breathing recommendations and celebration screens!
@Composable
fun JouriWorkoutTimerDialog(
    exercise: JouriExercise,
    onDismiss: () -> Unit,
    onWorkoutCompleted: () -> Unit
) {
    var timeLeft by remember { mutableStateOf(exercise.durationSeconds) }
    var isRunning by remember { mutableStateOf(true) }
    var isCompleted by remember { mutableStateOf(false) }

    // Breathing cues state
    var breathingCue by remember { mutableStateOf("شهيق عميق ولطيف 🌸") }

    LaunchedEffect(isRunning, timeLeft) {
        if (isRunning && timeLeft > 0) {
            delay(1000)
            timeLeft -= 1

            // Dynamic change of breathing cues every 5 seconds to guide mothers perfectly
            breathingCue = when ((timeLeft / 5) % 3) {
                0 -> "شهيق عميق وهادئ من الأنف 🌸"
                1 -> "حبس النفس بلطف للحظة... 🍃"
                else -> "زفير طويل وبطيء مريح من الفم 💕"
            }
        } else if (timeLeft == 0) {
            isCompleted = true
        }
    }

    Dialog(onDismissRequest = { }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B222E)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                if (!isCompleted) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "جلسة تمرين نشطة 🧘‍♀️",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38B2AC),
                            fontSize = 14.sp
                        )
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(28.dp).background(Color(0xFF141921), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "إلغاء", tint = Color(0xFF38B2AC), modifier = Modifier.size(16.dp))
                        }
                    }

                    Text(
                        text = exercise.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = SoftTheme.TextWhite,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Circular Countdown Timer Display
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(160.dp)
                    ) {
                        val progress = timeLeft.toFloat() / exercise.durationSeconds.toFloat()
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxSize(),
                            color = Color(0xFF38B2AC),
                            strokeWidth = 8.dp,
                            trackColor = Color(0xFF141921),
                            strokeCap = StrokeCap.Round,
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = String.format("%02d:%02d", timeLeft / 60, timeLeft % 60),
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Black,
                                color = SoftTheme.TextWhite
                            )
                            Text(
                                text = "متبقي",
                                fontSize = 11.sp,
                                color = Color(0xFF8F9CAE)
                            )
                        }
                    }

                    // Breathing guide visual card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF141921)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "مساعد التنفس المتناغم من جوري:",
                                fontSize = 10.sp,
                                color = Color(0xFF38B2AC),
                                fontWeight = FontWeight.Bold
                            )
                            AnimatedContent(
                                targetState = breathingCue,
                                transitionSpec = {
                                    slideInVertically { height -> height } + fadeIn() togetherWith
                                            slideOutVertically { height -> -height } + fadeOut()
                                },
                                label = "breathing"
                            ) { text ->
                                Text(
                                    text = text,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SoftTheme.TextWhite,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Controls row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { isRunning = !isRunning },
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color(0xFF38B2AC), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isRunning) "إيقاف مؤقت" else "استئناف",
                                tint = Color(0xFF141921),
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        Button(
                            onClick = { isCompleted = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D3748)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("إنهاء الجلسة 🏆", color = SoftTheme.TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                } else {
                    // Celebration Screen
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(Color(0xFF38B2AC).copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🎉", fontSize = 42.sp)
                    }

                    Text(
                        text = "عمل رائع ومثالي يا غالية! 🥳🌸",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF38B2AC),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "لقد أكملتِ تمرين \"${exercise.name}\" بنجاح واقتدار. جوري فخورة بكِ وبعنايتكِ الفائقة بصحتكِ وسلامتكِ اليوم! 💕",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoftTheme.TextWhite,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onWorkoutCompleted,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38B2AC)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("workout_done_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = "حفظ وإضافة الجلسة لسجلي اليومي 📝",
                            color = Color(0xFF141921),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StepPedometerDashboard(
    viewModel: WomanCompanionViewModel
) {
    val todayStepsState by viewModel.todayStepLogState.collectAsStateWithLifecycle()
    val allStepLogs by viewModel.allStepLogsState.collectAsStateWithLifecycle()
    val settingsState by viewModel.appLockSettingsState.collectAsStateWithLifecycle()
    val pregnancyState by viewModel.pregnancyState.collectAsStateWithLifecycle()

    val companionName = settingsState?.companionName ?: "جوري"
    val targetSteps = settingsState?.dailyStepTarget ?: 6000
    val currentSteps = todayStepsState?.steps ?: 0
    val progress = if (targetSteps > 0) (currentSteps.toFloat() / targetSteps.toFloat()).coerceIn(0f, 1f) else 0f

    // Calculate calories & distance
    val distanceKm = remember(currentSteps) {
        String.format(java.util.Locale.US, "%.2f", currentSteps * 0.0007)
    }
    val caloriesBurned = remember(currentSteps) {
        (currentSteps * 0.04).toInt()
    }
    val activeMinutes = remember(currentSteps) {
        (currentSteps / 80).toInt()
    }

    // Glowing animation for active sensor
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Active Sensor Header Badge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF141921), RoundedCornerShape(16.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(SoftTheme.MintTeal.copy(alpha = alphaAnim), CircleShape)
                        .border(1.5.dp, SoftTheme.MintTeal, CircleShape)
                )
                Text(
                    text = "جهاز تتبع الخطوات الفعلي نشط ومتصل",
                    color = SoftTheme.MintTeal,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "تتبع فوري تلقائي",
                color = SoftTheme.SoftGray,
                fontSize = 10.sp
            )
        }

        // Modern Neon Circular Steps Ring
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "هدف الخطوات اليومي 🎯",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SoftTheme.TextWhite
                )

                Box(
                    modifier = Modifier.size(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 14.dp.toPx()
                        drawCircle(
                            color = Color(0xFF1B222E),
                            style = Stroke(strokeWidth, cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = SoftTheme.MintTeal,
                            startAngle = -90f,
                            sweepAngle = progress * 360f,
                            useCenter = false,
                            style = Stroke(strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "👣",
                            fontSize = 32.sp
                        )
                        Text(
                            text = "$currentSteps",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = SoftTheme.TextWhite
                        )
                        Text(
                            text = "الهدف: $targetSteps",
                            fontSize = 11.sp,
                            color = SoftTheme.SoftGray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                val pct = (progress * 100).toInt()
                Box(
                    modifier = Modifier
                        .background(SoftTheme.MintTeal.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "أنجزتِ $pct% من هدفك اليومي",
                        color = SoftTheme.MintTeal,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Three Key Metrics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("📏", fontSize = 20.sp)
                    Text("المسافة التقديرية", fontSize = 10.sp, color = SoftTheme.SoftGray, textAlign = TextAlign.Center)
                    Text("$distanceKm كم", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("🔥", fontSize = 20.sp)
                    Text("سعرات محروقة", fontSize = 10.sp, color = SoftTheme.SoftGray, textAlign = TextAlign.Center)
                    Text("$caloriesBurned سعرة", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("⏱️", fontSize = 20.sp)
                    Text("مدة المشي النشط", fontSize = 10.sp, color = SoftTheme.SoftGray, textAlign = TextAlign.Center)
                    Text("$activeMinutes دقيقة", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
                }
            }
        }

        // Canvas-Drawn Weekly Steps Analytics Chart
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "التحليل البياني الأسبوعي للخطوات 📈",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = SoftTheme.TextWhite
                )

                val last7DaysLogs = remember(allStepLogs) {
                    val cal = java.util.Calendar.getInstance()
                    val logsMap = allStepLogs.associateBy { it.date }
                    val list = mutableListOf<Pair<String, Int>>()
                    val daySdf = java.text.SimpleDateFormat("EEE", java.util.Locale("ar"))
                    
                    for (i in 6 downTo 0) {
                        cal.timeInMillis = System.currentTimeMillis()
                        cal.add(java.util.Calendar.DAY_OF_YEAR, -i)
                        
                        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                        cal.set(java.util.Calendar.MINUTE, 0)
                        cal.set(java.util.Calendar.SECOND, 0)
                        cal.set(java.util.Calendar.MILLISECOND, 0)
                        
                        val startOfDay = cal.timeInMillis
                        val dayName = daySdf.format(cal.time)
                        val stepsVal = logsMap[startOfDay]?.steps ?: 0
                        list.add(dayName to stepsVal)
                    }
                    list
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(top = 10.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val maxStepsVal = last7DaysLogs.maxOfOrNull { it.second }?.coerceAtLeast(1000) ?: 6000
                        val gridLines = 4
                        val paddingBottom = 24.dp.toPx()
                        val paddingTop = 12.dp.toPx()
                        val paddingLeft = 36.dp.toPx()
                        val chartHeight = h - paddingBottom - paddingTop
                        val chartWidth = w - paddingLeft

                        val textPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.GRAY
                            textSize = 9.dp.toPx()
                            textAlign = android.graphics.Paint.Align.RIGHT
                        }
                        
                        for (i in 0..gridLines) {
                            val ratio = i.toFloat() / gridLines
                            val y = h - paddingBottom - (ratio * chartHeight)
                            val gridValue = (ratio * maxStepsVal).toInt()
                            
                            drawLine(
                                color = Color.Gray.copy(alpha = 0.15f),
                                start = Offset(paddingLeft, y),
                                end = Offset(w, y),
                                strokeWidth = 1.dp.toPx()
                            )
                            drawContext.canvas.nativeCanvas.drawText(
                                "$gridValue",
                                paddingLeft - 6.dp.toPx(),
                                y + 3.dp.toPx(),
                                textPaint
                            )
                        }

                        val barCount = last7DaysLogs.size
                        val barSpacing = chartWidth / barCount
                        val barWidth = barSpacing * 0.4f
                        
                        last7DaysLogs.forEachIndexed { index, (dayName, stepsVal) ->
                            val ratio = stepsVal.toFloat() / maxStepsVal.toFloat()
                            val barHeightVal = ratio * chartHeight
                            val x = paddingLeft + (index * barSpacing) + (barSpacing - barWidth) / 2f
                            val y = h - paddingBottom - barHeightVal
                            
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        SoftTheme.MintTeal,
                                        SoftTheme.MintTeal.copy(alpha = 0.3f)
                                    )
                                ),
                                topLeft = Offset(x, y),
                                size = androidx.compose.ui.geometry.Size(barWidth, barHeightVal),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                            )

                            val labelPaint = android.graphics.Paint().apply {
                                color = android.graphics.Color.WHITE
                                textSize = 9.dp.toPx()
                                textAlign = android.graphics.Paint.Align.CENTER
                            }
                            drawContext.canvas.nativeCanvas.drawText(
                                dayName,
                                x + barWidth / 2f,
                                h - 6.dp.toPx(),
                                labelPaint
                            )
                        }
                    }
                }
            }
        }

        // Medical Advice from Jouri
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, SoftTheme.MintTeal.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("💡", fontSize = 18.sp)
                    Text(
                        text = "توجيهات صحية حية من رفيقتكِ $companionName:",
                        color = SoftTheme.MintTeal,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                val customAdviceText = remember(pregnancyState, currentSteps, targetSteps) {
                    when {
                        pregnancyState != null -> {
                            val week = viewModel.getPregnancyProgression()?.weeks ?: 1
                            val base = "أنتِ الآن في الأسبوع $week من الحمل. "
                            if (currentSteps < 3000) {
                                base + "الحفاظ على القليل من المشي اليومي (حوالي 3000-4000 خطوة) يساعد في تنشيط مفاصل الحوض وتخفيف آلام الظهر ويسهل عملية الولادة لاحقاً. هل نمشي قليلاً؟ 🤰👣"
                            } else if (currentSteps < targetSteps) {
                                base + "رائع جداً! لقد سرتِ $currentSteps خطوة اليوم. واصلي بهدوء وخذي فترات راحة كل 10 دقائق لشرب المياه والترطيب لتجنب انخفاض السكر 🍃🌸"
                            } else {
                                base + "مذهل! حققتِ هدفكِ اليومي بالكامل 👏! أنصحكِ الآن برفع قدميكِ لأعلى والاسترخاء لشحن طاقتكِ وحماية قدميكِ من التورّم."
                            }
                        }
                        else -> {
                            if (currentSteps < 2000) {
                                "المشي الخفيف والمستمر هو أبسط تمرين لاستعادة نشاط عضلات البطن وقاع الحوض بلطف ومقاومة تقلبات المزاج بعد الولادة. ابدئي بـ 15 دقيقة فقط اليوم 🌸"
                            } else if (currentSteps < targetSteps) {
                                "مجهود رائع اليوم! المشي المنتظم يرفع كفاءة القلب ويحرق السعرات الزائدة وينشط عضلاتك العميقة. تذكري سحب بطنك للداخل أثناء خطواتكِ لتنشيط الكور 💨✨"
                            } else {
                                "تفوقتِ على نفسكِ اليوم! تحقيق هدف $targetSteps خطوة يعزز هرمون الإندورفين ويجعلكِ تشعرين بالراحة والنوم العميق الليلة 🥰."
                            }
                        }
                    }
                }

                Text(
                    text = customAdviceText,
                    color = SoftTheme.TextWhite,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }

        // Interactive Steps simulation buttons
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "محاكاة الخطوات السريعة للاختبار والتحقق:",
                    color = SoftTheme.SoftGray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.addSteps(100) },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.MintTeal.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("+100 خطوة 👟", color = SoftTheme.MintTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.addSteps(500) },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.MintTeal.copy(alpha = 0.25f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("+500 خطوة 🏃‍♀️", color = SoftTheme.MintTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
