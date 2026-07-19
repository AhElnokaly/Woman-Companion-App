package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FetalGrowthLog
import com.example.viewmodel.WomanCompanionViewModel
import java.text.SimpleDateFormat
import java.util.*

data class FetalStandard(
    val week: Int,
    val weightGrams: Double,
    val lengthCm: Double,
    val fruitComparison: String,
    val description: String
)

object FetalStandardData {
    val standards = mapOf(
        4 to FetalStandard(4, 0.1, 0.2, "بذرة خشخاش", "بدأت الخلايا بالانقسام لتشكيل الجنين والمشيمة الغنية."),
        5 to FetalStandard(5, 0.2, 0.3, "بذرة سمسم", "يبدأ تشكل الأنبوب العصبي والقلب البدائي النابض."),
        6 to FetalStandard(6, 0.4, 0.5, "حبة عدس", "ينبض القلب الصغير الآن بمعدل 150 نبضة في الدقيقة كمعجزة صغيرة."),
        7 to FetalStandard(7, 0.8, 0.8, "حبة حمص", "تبدأ براعم الأطراف (اليدين والرجلين) في البروز والظهور بوضوح."),
        8 to FetalStandard(8, 1.0, 1.6, "حبة توت", "تتكون ملامح الوجه البدائية وتبدأ الأصابع الدقيقة في التمايز والنمو."),
        9 to FetalStandard(9, 2.0, 2.3, "حبة عنب", "يتحرك الجنين حركات خفيفة جداً ومبهجة في رحمكِ الدافئ."),
        10 to FetalStandard(10, 4.0, 3.1, "حبة مشمش مجفف", "اكتمال تشكل معظم الأعضاء الحيوية الأساسية للطفل الصغير."),
        11 to FetalStandard(11, 7.0, 4.1, "حبة تين", "تنمو الأظافر الصغيرة جداً ويبدأ الطفل ببلع السائل السلوي بلطف."),
        12 to FetalStandard(12, 14.0, 5.4, "حبة ليمون بلدي", "يمكن الآن سماع نبضات قلب جنينكِ الدافئة والجميلة عبر السونار."),
        13 to FetalStandard(13, 23.0, 7.4, "حبة خوخ", "أصابع الطفل تشكلت بالكامل وبصمات الأصابع الفريدة تبدأ بالظهور."),
        14 to FetalStandard(14, 43.0, 8.7, "حبة ليمون أضاليا", "يبدأ طفلكِ في عمل تعابير بوجهه الجميل مثل العبوس والابتسام البسيط."),
        15 to FetalStandard(15, 70.0, 10.1, "حبة تفاح صغير", "جلد الجنين رقيق جداً وشفاف وتظهر الأوعية الدموية من خلاله كالحرير."),
        16 to FetalStandard(16, 100.0, 11.6, "حبة أفوكادو", "يمكنكِ الآن أحياناً البدء في الشعور بركلات خفيفة ورقيقة كالفراشات."),
        17 to FetalStandard(17, 140.0, 13.0, "حبة رمان", "تبدأ الدهون الصحية بالتراكم التدريجي تحت جلد طفلكِ لتدفئته وحمايته."),
        18 to FetalStandard(18, 190.0, 14.2, "حبة بطاطس", "يمكن لطفلكِ الآن سماع الأصوات الخارجية، وخاصةً صوت دقات قلبكِ وصوتكِ الدافئ."),
        19 to FetalStandard(19, 240.0, 15.3, "حبة مانجو", "تتكون طبقة شمعية واقية (الطلاء الجنيني) لحماية جلد طفلكِ الحساس."),
        20 to FetalStandard(20, 300.0, 25.6, "حبة موزة", "منتصف الرحلة المباركة! طول الطفل يقاس الآن من الرأس لكعب القدم بالكامل."),
        21 to FetalStandard(21, 360.0, 26.7, "حبة جزر", "تبدأ حاسة التذوق في التطور، ويميز الطفل نكهات الأطعمة التي تتناولينها."),
        22 to FetalStandard(22, 430.0, 27.8, "حبة جوز هند هندية", "تتطور الحواجب والأشفار بوضوح ويبدو كنسخة مصغرة من وليدكِ الجميل."),
        23 to FetalStandard(23, 500.0, 28.9, "حبة باذنجان كبير", "الرئتان الصغيرتان تتطوران وتستعدان للتنفس الخارجي مستقبلاً بهمة."),
        24 to FetalStandard(24, 600.0, 30.0, "حبة ذرة طازجة", "يتفاعل الطفل مع الأصوات واللمس الخارجي وتتكامل أنماط نومه ويقظته."),
        25 to FetalStandard(25, 660.0, 34.6, "حبة قرنبيط", "يبدأ شعر رأس طفلكِ الناعم في النمو وتتضح ملامحه المبهجة أكثر."),
        26 to FetalStandard(26, 760.0, 35.6, "حبة كابوتشا (خس)", "يستطيع طفلكِ الآن فتح وإغلاق عينيه اللطيفة والاستجابة للضوء المنعكس."),
        27 to FetalStandard(27, 875.0, 36.6, "حبة شمام صغير", "تنضج شبكية العين ويقوى نشاط الدماغ والروابط العصبية الذكية باستمرار."),
        28 to FetalStandard(28, 1000.0, 37.6, "حبة باذنجان رومي", "يزداد وزن الجنين بسرعة الآن، وتبلغ فرص نموه بأمان مستويات عالية بفضل الله."),
        29 to FetalStandard(29, 1150.0, 38.6, "حبة أناناس", "تتطور العظام وتصبح أقوى، وتتراكم مستويات ممتازة من المعادن المفيدة."),
        30 to FetalStandard(30, 1320.0, 39.9, "حبة ملفوف (كرنب)", "يبدأ طفلكِ في تجميع مخزون مذهل من الحديد والكالسيوم لنمو هيكله العظمي."),
        31 to FetalStandard(31, 1500.0, 41.1, "حبة جوز هند كبيرة", "تكتمل كفاءة حواسه الخمسة ويبدأ مخه الصغير بالتحكم الفعلي في درجة حرارة جسمه."),
        32 to FetalStandard(32, 1700.0, 42.4, "حبة يقطين صغير", "يستقر طفلكِ في وضعيات مريحة وتصبح ركلاته أكثر قوة وحركة بهيجة."),
        33 to FetalStandard(33, 1900.0, 43.7, "حبة كرفس بلدي", "جهازه المناعي يستقبل الأجسام المضادة الدافئة منكِ لتوفير الحصانة الطبيعية."),
        34 to FetalStandard(34, 2150.0, 45.0, "حبة شمام كبير", "تكتمل الرئتان بشكل كامل تقريباً ويصبح جهازه التنفسي قادراً على العمل بسلام."),
        35 to FetalStandard(35, 2380.0, 46.2, "حبة بطيخ صغير", "يمتلئ جسمه بالدهون المفيدة، وتصبح أطرافه اللطيفة ممتلئة وناعمة كالحرير."),
        36 to FetalStandard(36, 2600.0, 47.4, "سلة خضار دافئة", "يقترب الجنين من الوزن المثالي الكامل والجاهز للولادة الآمنة والسهلة."),
        37 to FetalStandard(37, 2850.0, 48.6, "باقة ورد عطرة", "حملكِ الآن مكتمل تماماً! يبدأ طفلكِ بالاستعداد للنزول أسفل الحوض للولادة الميسرة."),
        38 to FetalStandard(38, 3100.0, 49.8, "حزمة سلق طازجة", "تكتمل وظائف الدماغ والأعضاء الحيوية، والطفل في شوق تام للقائكِ الدافئ."),
        39 to FetalStandard(39, 3300.0, 50.7, "بطيخة ناضجة شهية", "يفرز جسم الطفل هرمونات طبيعية ممتازة لتحفيز مخاض الولادة في الوقت المناسب."),
        40 to FetalStandard(40, 3450.0, 51.2, "باقة ياسمين دمشقي", "مبارك يا روحي! طفلكِ مكتمل تماماً ومستعد للخروج لينير حياتكِ بنوره الوضاء."),
        41 to FetalStandard(41, 3600.0, 51.7, "جنين مبارك وناضج", "فترة ترقب جميلة، يتابع فيها الأطباء نشاط الجنين لضمان كمال راحته ونموه."),
        42 to FetalStandard(42, 3750.0, 52.5, "مولود السعادة والبركة", "اكتمال الرحلة الطبية المباركة، والأطباء مستعدون لمساعدتكِ باللقاء بأمان.")
    )

    fun getStandardForWeek(week: Int): FetalStandard {
        return standards[week] ?: FetalStandard(week, 1000.0, 35.0, "فاكهة طبيعية", "ينمو طفلكِ تدريجياً وبأمان تام.")
    }
}

@Composable
fun FetalGrowthSubScreen(viewModel: WomanCompanionViewModel) {
    val logs by viewModel.allFetalGrowthLogsState.collectAsState()
    val pregnancy by viewModel.pregnancyState.collectAsState()
    
    // Guess default week based on pregnancy info if available
    val currentPregnancyWeek = remember(pregnancy) {
        if (pregnancy?.userPhase == "pregnancy" && pregnancy?.lastPeriodDate != null) {
            val diffMs = System.currentTimeMillis() - pregnancy!!.lastPeriodDate!!
            val weeks = (diffMs / (1000 * 60 * 60 * 24 * 7)).toInt()
            weeks.coerceIn(4, 42)
        } else {
            12 // Default to week 12 as a good mid-point
        }
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedWeek by remember { mutableStateOf(currentPregnancyWeek) }
    var weightInput by remember { mutableStateOf("") }
    var lengthInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }
    var isErrorMsg by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("dd MMMM yyyy", Locale("ar")) }

    // Pre-populate input helpers when selectedWeek changes
    LaunchedEffect(selectedWeek) {
        val std = FetalStandardData.getStandardForWeek(selectedWeek)
        weightInput = std.weightGrams.toInt().toString()
        lengthInput = std.lengthCm.toString()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftTheme.DeepSlate)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Card with warm companion encouragement
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(SoftTheme.SoftPink.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = SoftTheme.SoftPink,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "مُتابِع نمو الجنين الذكي 📈👶",
                                fontWeight = FontWeight.Bold,
                                color = SoftTheme.TextWhite,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "تتبع رحلة وزن وطول طفلكِ بعد كل كشف طبي وقارنيها بالمعدل الصحي!",
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftTheme.SoftGray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "يا حبيبة قلبي، تدوين قياسات طفلكِ بعد زيارات الطبيب يتيح لنا تحليل منحنى نموه بذكاء والتأكد من ملاءمته لأعلى المعايير الصحية والغذائية لسلامتكن! 💕",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoftTheme.TextWhite.copy(alpha = 0.9f),
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_growth_log_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = SoftTheme.DeepSlate)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تسجيل قياسات السونار الجديدة 🏥", color = SoftTheme.DeepSlate, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 2. Interactive growth comparison chart
        if (logs.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "مُنحنى تطور نمو الجنين (الفعلي ضد المفترض) 📊",
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.TextWhite,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "الخط الوردي يوضح القيمة القياسية، والنقاط البيضاء تعبر عن قياساتكِ الفعلية.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTheme.SoftGray
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        // Custom drawing of weight growth curve
                        FetalGrowthChart(logs = logs)
                    }
                }
            }
        }

        // 3. Quick analysis card for the latest record
        if (logs.isNotEmpty()) {
            val latestLog = logs.maxByOrNull { it.pregnancyWeek }!!
            val standard = FetalStandardData.getStandardForWeek(latestLog.pregnancyWeek)
            
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("✨", fontSize = 20.sp)
                            Text(
                                text = "تحليل جوري لنمو طفلكِ (الأسبوع ${latestLog.pregnancyWeek})",
                                fontWeight = FontWeight.Bold,
                                color = SoftTheme.SoftPink,
                                fontSize = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        val weightDiffPct = ((latestLog.weightGrams - standard.weightGrams) / standard.weightGrams) * 100.0
                        val lengthDiffPct = ((latestLog.lengthCm - standard.lengthCm) / standard.lengthCm) * 100.0

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Weight Compare Card
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("وزن الجنين ⚖️", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("${latestLog.weightGrams.toInt()} جرام", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "المفترض: ${standard.weightGrams.toInt()} جرام",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SoftTheme.SoftGray
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (weightDiffPct >= 0) "+${weightDiffPct.toInt()}%" else "${weightDiffPct.toInt()}%",
                                        fontWeight = FontWeight.Bold,
                                        color = if (weightDiffPct in -15.0..15.0) Color.Green else if (weightDiffPct > 15) SoftTheme.SoftPink else Color.Yellow,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            // Length Compare Card
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("طول الجنين 📏", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("${latestLog.lengthCm} سم", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "المفترض: ${standard.lengthCm} سم",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SoftTheme.SoftGray
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (lengthDiffPct >= 0) "+${lengthDiffPct.toInt()}%" else "${lengthDiffPct.toInt()}%",
                                        fontWeight = FontWeight.Bold,
                                        color = if (lengthDiffPct in -15.0..15.0) Color.Green else if (lengthDiffPct > 15) SoftTheme.SoftPink else Color.Yellow,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SoftTheme.DeepSlate, RoundedCornerShape(16.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = SoftTheme.SoftPink, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = "مقارنة الحجم: طفلكِ الآن بحجم (${standard.fruitComparison})",
                                        fontWeight = FontWeight.Bold,
                                        color = SoftTheme.TextWhite,
                                        fontSize = 13.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = standard.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SoftTheme.SoftGray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        val diagnosisText = when {
                            weightDiffPct in -15.0..15.0 -> "نمو طفلكِ في النطاق الذهبي الممتاز والمثالي! 🌟 الاستمرار في التغذية المتوازنة رائع جداً ويمنح جنينك القوة والصحة."
                            weightDiffPct < -15.0 -> "وزن طفلكِ أقل قليلاً من المعدل المتوسط. لا تقلقي يا غالية، قد يكون القياس بالسونار تقريبياً، لكن ننصحكِ بمناقشة طبيبتكِ في تحسين التغذية وزيادة الأطعمة الغنية بالبروتينات والحديد مثل اللحوم الحمراء والبيض والمكسرات!"
                            else -> "طفلكِ ما شاء الله ينمو بهمة ونشاط وحجم فوق المتوسط! استشيري طبيبتكِ للتأكد من توازن مستويات السكر والتغذية اللطيفة السليمة لسهولة ولادتكِ."
                        }
                        Text(
                            text = diagnosisText,
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTheme.TextWhite,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // 4. List of past growth records
        item {
            Text(
                text = "السجلات السابقة لعيادة الطبيب 📋",
                fontWeight = FontWeight.Bold,
                color = SoftTheme.TextWhite,
                fontSize = 16.sp
            )
        }

        if (logs.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = SoftTheme.SoftPink.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "لا توجد سجلات نمو حتى الآن",
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.TextWhite,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "سجلي أول قراءة لوزن وطول جنينك من السونار لنبدأ بمتابعة نموه ورسم المنحنى البياني التفاعلي!",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTheme.SoftGray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(logs.sortedByDescending { it.pregnancyWeek }) { log ->
                val std = FetalStandardData.getStandardForWeek(log.pregnancyWeek)
                Card(
                    colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "الأسبوع ${log.pregnancyWeek} من الحمل 🤰",
                                    fontWeight = FontWeight.Bold,
                                    color = SoftTheme.TextWhite,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = dateFormatter.format(Date(log.date)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SoftTheme.SoftGray
                                )
                            }
                            IconButton(
                                onClick = { viewModel.deleteFetalGrowthLog(log) },
                                modifier = Modifier.testTag("delete_growth_log_${log.id}")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف القراءة", tint = Color.Red.copy(alpha = 0.7f))
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = SoftTheme.DeepSlate)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("الوزن الفعلي:", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                                Text("${log.weightGrams.toInt()} جرام", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("المتوسط القياسي: ${std.weightGrams.toInt()} جرام", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("الطول الفعلي:", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                                Text("${log.lengthCm} سم", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("المتوسط القياسي: ${std.lengthCm} سم", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                            }
                        }

                        if (!log.notes.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SoftTheme.DeepSlate, RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text("ملاحظات الطبيب 📝", fontWeight = FontWeight.Bold, color = SoftTheme.SoftPink, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(log.notes, style = MaterialTheme.typography.bodySmall, color = SoftTheme.TextWhite)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Record Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = "تسجيل فحص السونار والنمو 👶🏥",
                    fontWeight = FontWeight.Bold,
                    color = SoftTheme.TextWhite,
                    fontSize = 18.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Week Picker Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("أسبوع الحمل الحالي:", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                            Text("الأسبوع $selectedWeek", fontWeight = FontWeight.Bold, color = SoftTheme.SoftPink)
                        }
                        Slider(
                            value = selectedWeek.toFloat(),
                            onValueChange = { selectedWeek = it.toInt() },
                            valueRange = 4f..42f,
                            steps = 38,
                            colors = SliderDefaults.colors(
                                thumbColor = SoftTheme.SoftPink,
                                activeTrackColor = SoftTheme.SoftPink,
                                inactiveTrackColor = SoftTheme.DeepSlate
                            )
                        )
                        val expectedStd = FetalStandardData.getStandardForWeek(selectedWeek)
                        Text(
                            text = "المعدل الطبيعي للأسبوع $selectedWeek: ${expectedStd.weightGrams.toInt()} جرام | ${expectedStd.lengthCm} سم",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTheme.SoftGray
                        )
                    }

                    // Weight Input
                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { weightInput = it },
                        label = { Text("وزن الجنين (بالجرام)", color = SoftTheme.SoftGray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("baby_weight_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SoftTheme.TextWhite,
                            unfocusedTextColor = SoftTheme.TextWhite,
                            focusedBorderColor = SoftTheme.SoftPink,
                            unfocusedBorderColor = SoftTheme.DeepSlate
                        )
                    )

                    // Length Input
                    OutlinedTextField(
                        value = lengthInput,
                        onValueChange = { lengthInput = it },
                        label = { Text("طول الجنين (بالسنتيمتر)", color = SoftTheme.SoftGray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("baby_length_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SoftTheme.TextWhite,
                            unfocusedTextColor = SoftTheme.TextWhite,
                            focusedBorderColor = SoftTheme.SoftPink,
                            unfocusedBorderColor = SoftTheme.DeepSlate
                        )
                    )

                    // Notes Input
                    OutlinedTextField(
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        label = { Text("ملاحظات الطبيب / العيادة", color = SoftTheme.SoftGray) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("growth_notes_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SoftTheme.TextWhite,
                            unfocusedTextColor = SoftTheme.TextWhite,
                            focusedBorderColor = SoftTheme.SoftPink,
                            unfocusedBorderColor = SoftTheme.DeepSlate
                        )
                    )

                    isErrorMsg?.let {
                        Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val weight = weightInput.toDoubleOrNull()
                        val length = lengthInput.toDoubleOrNull()
                        if (weight == null || weight <= 0.0) {
                            isErrorMsg = "برجاء إدخال وزن جنين صحيح بالجرام."
                            return@Button
                        }
                        if (length == null || length <= 0.0) {
                            isErrorMsg = "برجاء إدخال طول جنين صحيح بالسنتيمتر."
                            return@Button
                        }
                        
                        viewModel.addFetalGrowthLog(
                            week = selectedWeek,
                            weightGrams = weight,
                            lengthCm = length,
                            notes = notesInput.ifBlank { null }
                        )

                        // Reset inputs and close
                        notesInput = ""
                        isErrorMsg = null
                        showAddDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                    modifier = Modifier.testTag("confirm_add_growth_log")
                ) {
                    Text("حفظ 💾", color = SoftTheme.DeepSlate, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("إلغاء", color = SoftTheme.SoftGray)
                }
            },
            containerColor = SoftTheme.CardSlate,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun FetalGrowthChart(logs: List<FetalGrowthLog>) {
    val sortedLogs = remember(logs) { logs.sortedBy { it.pregnancyWeek } }
    
    // Determine min and max weeks to draw
    val minWeek = 4f
    val maxWeek = 42f
    
    // Max weight standard or actual to fit the chart properly
    val maxWeight = remember(sortedLogs) {
        val maxLogWeight = sortedLogs.maxOfOrNull { it.weightGrams } ?: 0.0
        val maxStdWeight = FetalStandardData.getStandardForWeek(42).weightGrams
        maxOf(maxLogWeight, maxStdWeight).toFloat()
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        val width = size.width
        val height = size.height
        val paddingLeft = 50f
        val paddingRight = 20f
        val paddingTop = 20f
        val paddingBottom = 40f
        
        val chartWidth = width - paddingLeft - paddingRight
        val chartHeight = height - paddingTop - paddingBottom

        // Draw axes lines (Deep Slate color)
        drawLine(
            color = Color.White.copy(alpha = 0.1f),
            start = Offset(paddingLeft, paddingTop),
            end = Offset(paddingLeft, height - paddingBottom),
            strokeWidth = 2f
        )
        drawLine(
            color = Color.White.copy(alpha = 0.1f),
            start = Offset(paddingLeft, height - paddingBottom),
            end = Offset(width - paddingRight, height - paddingBottom),
            strokeWidth = 2f
        )

        // Helper function to map week and weight to X,Y offsets
        fun getCoordinates(week: Float, weight: Float): Offset {
            val xRatio = (week - minWeek) / (maxWeek - minWeek)
            val yRatio = weight / maxWeight
            val x = paddingLeft + (xRatio * chartWidth)
            val y = (height - paddingBottom) - (yRatio * chartHeight)
            return Offset(x, y)
        }

        // 1. Draw expected standard curve (dotted line in SoftPink)
        val pathStd = Path()
        var firstStd = true
        for (w in 4..42) {
            val stdWeight = FetalStandardData.getStandardForWeek(w).weightGrams.toFloat()
            val pt = getCoordinates(w.toFloat(), stdWeight)
            if (firstStd) {
                pathStd.moveTo(pt.x, pt.y)
                firstStd = false
            } else {
                pathStd.lineTo(pt.x, pt.y)
            }
        }
        drawPath(
            path = pathStd,
            color = Color(0xFFFFB6C1).copy(alpha = 0.6f),
            style = Stroke(
                width = 3f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
        )

        // 2. Draw actual user logs (solid line + circular dots)
        if (sortedLogs.isNotEmpty()) {
            val pathActual = Path()
            var firstActual = true
            sortedLogs.forEach { log ->
                val pt = getCoordinates(log.pregnancyWeek.toFloat(), log.weightGrams.toFloat())
                if (firstActual) {
                    pathActual.moveTo(pt.x, pt.y)
                    firstActual = false
                } else {
                    pathActual.lineTo(pt.x, pt.y)
                }
            }
            // Draw actual line
            drawPath(
                path = pathActual,
                color = Color.White,
                style = Stroke(width = 4f)
            )

            // Draw actual dots
            sortedLogs.forEach { log ->
                val pt = getCoordinates(log.pregnancyWeek.toFloat(), log.weightGrams.toFloat())
                drawCircle(
                    color = Color.White,
                    radius = 8f,
                    center = pt
                )
                drawCircle(
                    color = Color(0xFFFFB6C1),
                    radius = 4f,
                    center = pt
                )
            }
        }
    }
}
