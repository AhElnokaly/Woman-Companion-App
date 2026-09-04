package com.example.ui.fitness

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.SoftTheme
import com.example.viewmodel.WomanCompanionViewModel

@Composable
fun FitnessScreen(
    viewModel: WomanCompanionViewModel
) {
    val completedCount by viewModel.completedWorkoutsCount.collectAsStateWithLifecycle()
    val streakCount by viewModel.workoutStreak.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf("pregnancy") }
    var selectedExerciseForTimer by remember { mutableStateOf<JouriExercise?>(null) }
    var selectedExerciseDetail by remember { mutableStateOf<JouriExercise?>(null) }

    val pregnancyProgression = viewModel.getPregnancyProgression()
    var selectedTrimesterFilter by remember(pregnancyProgression?.trimester) {
        mutableStateOf(pregnancyProgression?.trimester ?: 0) // 0 = All trimesters
    }

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
                safetyWarning = "احرصي على إفراغ المثانة قبل البدء وتجنبي كتم الأنفاس.",
                recommendedTrimesters = setOf(1, 2, 3)
            ),
            JouriExercise(
                id = "cat_cow_tri1",
                name = "تمرين القطة والبقرة المهدئ",
                category = "pregnancy",
                durationSeconds = 90,
                emoji = "🐈",
                goal = "الهدف: مرونة العمود الفقري وتهدئة أسفل الظهر",
                description = "تمرين لطيف وآمن جداً في المرحلة الأولى والثانية من الحمل لتخفيف تيبّس الظهر وتحسين مدة التنفس العميق.",
                stepDetails = listOf(
                    JouriStep("الخطوة الأولى:", "الارتكاز على اليدين والركبتين باستواء", null, "sit"),
                    JouriStep("الخطوة الثانية:", "رفع الرأس بلطف مع زفير مريح", "5 ثوانٍ", "stretch"),
                    JouriStep("الخطوة الثالثة:", "تقويس الظهر للأعلى شهيقاً", "5 ثوانٍ", "relax")
                ),
                benefits = listOf(
                    "تخفيف آلام أعلى وأسفل الظهر المبكرة.",
                    "تنشيط الدورة الدموية للجنين والرحم."
                ),
                safetyWarning = "تجنبي تقويس الظهر بشدة أو الشعور بالدوار.",
                recommendedTrimesters = setOf(1, 2)
            ),
            JouriExercise(
                id = "pelvic_forward",
                name = "تمرين إمالة الحوض",
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
                safetyWarning = "تجنبي المبالغة في تقويس الظهر لأسفل لمنع التشنج.",
                recommendedTrimesters = setOf(2, 3)
            ),
            JouriExercise(
                id = "gentiad",
                name = "تمرين الجنتياد (تفريج الحوض)",
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
                safetyWarning = "لا تقومي بهز الركبتين بعنف، بل دعي التمدد يتم تدريجياً وبسلاسة.",
                recommendedTrimesters = setOf(2, 3)
            ),
            JouriExercise(
                id = "squats_birth_prep",
                name = "تمرين القرفصاء المدعوم للولادة",
                category = "pregnancy",
                durationSeconds = 120,
                emoji = "🧱",
                goal = "الهدف: فتح الحوض وتمهيد نزول الجنين",
                description = "مخصص بشكل خاص للثلث الثالث من الحمل لمساعدة رأس الجنين على الاستقرار في الحوض وتسهيل مخاض الولادة.",
                stepDetails = listOf(
                    JouriStep("الخطوة الأولى:", "الاستناد على كرسي متين والنزول ببطء", null, "sit"),
                    JouriStep("الخطوة الثانية:", "الثبات في وضعية القرفصاء المنخفضة", "10 ثوانٍ", "hold"),
                    JouriStep("الخطوة الثالثة:", "الارتفاع ببطء باستعمال عضلات الفخذين", "5 ثوانٍ", "relax")
                ),
                benefits = listOf(
                    "زيادة اتساع قطر مخرج الحوض.",
                    "تقوية عضلات الساقين والركبتين للولادة."
                ),
                safetyWarning = "ممنوع في حال وجود نزيف أو انخفاض المشيمة دون استشارة طبيبتك.",
                recommendedTrimesters = setOf(3)
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

    val filteredExercises = remember(activeTab, exercises, selectedTrimesterFilter) {
        exercises.filter { ex ->
            if (ex.category != activeTab) return@filter false
            if (activeTab == "pregnancy" && selectedTrimesterFilter in 1..3) {
                selectedTrimesterFilter in ex.recommendedTrimesters
            } else {
                true
            }
        }
    }

    var fitnessMainTab by remember { mutableStateOf("pedometer") } // default to showing the pedometer

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

            // Main Switcher (Walking Pedometer vs Exercises)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF141921), RoundedCornerShape(16.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (fitnessMainTab == "pedometer") Color(0xFF38B2AC) else Color.Transparent)
                            .clickable { fitnessMainTab = "pedometer" }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "👣 خطوات المشي",
                            color = if (fitnessMainTab == "pedometer") Color(0xFF141921) else SoftTheme.SoftGray,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (fitnessMainTab == "exercises") Color(0xFF38B2AC) else Color.Transparent)
                            .clickable { fitnessMainTab = "exercises" }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🧘‍♀️ تمارين متخصصة",
                            color = if (fitnessMainTab == "exercises") Color(0xFF141921) else SoftTheme.SoftGray,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            if (fitnessMainTab == "pedometer") {
                item {
                    StepPedometerDashboard(viewModel = viewModel)
                }
            } else {
                // Exercises Tab Content
                item {
                    // Hero Card: Custom Anatomical Drawing of Pelvic Floor
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B222E)),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                            LargePelvisAnatomicalDrawing(modifier = Modifier.fillMaxSize())

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFF38B2AC).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "مخطط الحوض التشريحي",
                                            color = Color(0xFF38B2AC),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Streak and stats badge
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .background(Color(0xFF141921).copy(alpha = 0.7f), CircleShape)
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("🔥", fontSize = 12.sp)
                                        Text(
                                            text = "$streakCount أيام متتالية",
                                            color = SoftTheme.TextWhite,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Column {
                                    Text(
                                        text = "عضلات قاع الحوض",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black,
                                        color = SoftTheme.TextWhite
                                    )
                                    Text(
                                        text = "تمارين كيجل والاسترخاء لتقوية عضلات الحوض وتسهيل الولادة",
                                        fontSize = 11.sp,
                                        color = Color(0xFF8F9CAE)
                                    )
                                }
                            }
                        }
                    }
                }

                // Stage tabs (Pregnancy / Postpartum / General)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1B222E), RoundedCornerShape(16.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf(
                            Triple("pregnancy", "🤰 مرحلة الحمل", "pregnancy_tab"),
                            Triple("postpartum", "🤱 بعد الولادة", "postpartum_tab"),
                            Triple("general", "🌸 عام واستسترخاء", "general_tab")
                        ).forEach { (catKey, catLabel, testTag) ->
                            val isSelected = activeTab == catKey
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) Color(0xFF38B2AC) else Color.Transparent)
                                    .clickable { activeTab = catKey }
                                    .padding(vertical = 10.dp)
                                    .testTag(testTag),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = catLabel,
                                    color = if (isSelected) Color(0xFF141921) else Color(0xFF8F9CAE),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                // Trimester filter pills (only visible when in pregnancy tab)
                if (activeTab == "pregnancy") {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "تصفية التمارين حسب ثلث الحمل:",
                                color = SoftTheme.SoftGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    0 to "الكل 🌟",
                                    1 to "الثلث الأول 🌱",
                                    2 to "الثلث الثاني 🌿",
                                    3 to "الثلث الثالث 🌸"
                                ).forEach { (triIndex, label) ->
                                    val isSelected = selectedTrimesterFilter == triIndex
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (isSelected) Color(0xFF38B2AC).copy(alpha = 0.2f)
                                                else Color(0xFF1B222E)
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (isSelected) Color(0xFF38B2AC) else Color.Transparent,
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            .clickable { selectedTrimesterFilter = triIndex }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            color = if (isSelected) Color(0xFF38B2AC) else SoftTheme.TextWhite,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 10.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Exercises List
                items(filteredExercises) { exercise ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedExerciseDetail = exercise },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B222E)),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, Color(0xFF38B2AC).copy(alpha = 0.15f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Icon Box
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .background(Color(0xFF141921), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(exercise.emoji, fontSize = 24.sp)
                            }

                            // Info column
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = exercise.name,
                                        fontWeight = FontWeight.Bold,
                                        color = SoftTheme.TextWhite,
                                        fontSize = 15.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(3.dp))

                                Text(
                                    text = exercise.goal,
                                    color = Color(0xFF38B2AC),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "⏱️ ${exercise.durationSeconds} ثانية",
                                        color = Color(0xFF8F9CAE),
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "• ${exercise.stepDetails.size} خطوات",
                                        color = Color(0xFF8F9CAE),
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            // Start Action Button
                            Button(
                                onClick = { selectedExerciseForTimer = exercise },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38B2AC)),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text(
                                    text = "ابدأ ⚡",
                                    color = Color(0xFF141921),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Fullscreen Exercise Details & Instructions Modal
        selectedExerciseDetail?.let { exercise ->
            Dialog(onDismissRequest = { selectedExerciseDetail = null }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.85f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B222E)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                    ) {
                        // Header Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { selectedExerciseDetail = null },
                                modifier = Modifier.size(32.dp).background(Color(0xFF141921), CircleShape)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color(0xFF38B2AC), modifier = Modifier.size(18.dp))
                            }

                            Text(
                                text = "تفاصيل وإرشادات التمرين 🌸",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF38B2AC),
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(exercise.emoji, fontSize = 28.sp)
                                    Column {
                                        Text(
                                            text = exercise.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = SoftTheme.TextWhite
                                        )
                                        Text(
                                            text = exercise.goal,
                                            color = Color(0xFF38B2AC),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            item {
                                Text(
                                    text = exercise.description,
                                    color = SoftTheme.TextWhite,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            }

                            item {
                                Text(
                                    text = "خطوات الأداء المتسلسلة:",
                                    fontWeight = FontWeight.Bold,
                                    color = SoftTheme.TextWhite,
                                    fontSize = 13.sp
                                )
                            }

                            items(exercise.stepDetails) { step ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141921)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        ExerciseStepIllustration(
                                            type = step.iconType,
                                            modifier = Modifier.size(44.dp)
                                        )

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = step.title,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF38B2AC),
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                text = step.subtitle,
                                                color = SoftTheme.TextWhite,
                                                fontSize = 12.sp
                                            )
                                        }

                                        step.durationBadge?.let { badge ->
                                            Box(
                                                modifier = Modifier
                                                    .background(Color(0xFF1B222E), RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = badge,
                                                    color = Color(0xFF38B2AC),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            item {
                                Text(
                                    text = "الفوائد الصحية المثبتة:",
                                    fontWeight = FontWeight.Bold,
                                    color = SoftTheme.TextWhite,
                                    fontSize = 13.sp
                                )
                            }

                            items(exercise.benefits) { benefit ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text("✨", fontSize = 12.sp)
                                    Text(
                                        text = benefit,
                                        color = SoftTheme.TextWhite,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }

                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2C1E24)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("⚠️", fontSize = 16.sp)
                                        Text(
                                            text = exercise.safetyWarning,
                                            color = Color(0xFFFFB4A2),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                selectedExerciseForTimer = exercise
                                selectedExerciseDetail = null
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("start_exercise_now_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38B2AC)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = "ابدأ التمرين الآن ⚡",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF141921)
                            )
                        }
                    }
                }
            }
        }

        // Interactive Workout Timer Dialog
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
}
