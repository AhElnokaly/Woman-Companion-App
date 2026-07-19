package com.example.ui

import com.example.BuildConfig

import android.text.format.DateFormat
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.viewmodel.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.rememberLazyListState
import org.json.JSONObject
import android.util.Log
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed

// +++ أضيف بناءً على طلبك لدعم قاعدة البيانات الهرمية للأغذية ومستويات السلامة +++
val hierarchicalDatabase = mapOf(
    "🥚 بيض" to listOf(
        SubcategoryInfo("بيض مسلوق", 70, SafetyLevel.SAFE, "🥚 البيض المسلوق ممتاز وغني بالبروتين والكولين الهام لنمو دماغ جنينكِ.", listOf("ملح", "فلفل أسود", "كمون", "طماطم"), carbs = 0.6, protein = 6.3, fat = 5.0, iron = 1.0, calcium = 25.0, folate = 22.0),
        SubcategoryInfo("بيض مقلي", 90, SafetyLevel.SAFE, "🍳 البيض المقلي آمن بشرط طهيه بالكامل وتجنب الأجزاء السائلة لسلامتكِ.", listOf("ملح", "فلفل أسود", "جبنة موزاريلا"), carbs = 0.6, protein = 6.3, fat = 7.5, iron = 1.0, calcium = 25.0, folate = 22.0),
        SubcategoryInfo("أومليت خضار", 110, SafetyLevel.SAFE, "🍳 أومليت غني بالفيتامينات والمعادن الهامة وصديق للهضم.", listOf("ملح", "فلفل أسود", "طماطم", "بصل", "فلفل ألوان"), carbs = 2.5, protein = 7.0, fat = 8.5, iron = 1.4, calcium = 35.0, folate = 30.0)
    ),
    "🧀 جبن" to listOf(
        SubcategoryInfo("جبن قريش", 80, SafetyLevel.SAFE, "🧀 الجبن القريش رائع غني بالكالسيوم والبروتين وسهل الهضم للجميع.", listOf("ملح", "فلفل أسود", "كمون", "زعتر", "زيت زيتون", "طماطم", "خيار"), carbs = 3.0, protein = 11.0, fat = 4.3, iron = 0.1, calcium = 83.0, folate = 12.0),
        SubcategoryInfo("جبن رومي/فيتا مبستر", 120, SafetyLevel.SAFE, "🧀 تأكدي دوماً أن الأجبان مصنوعة من حليب مبستر لسلامتكِ الكاملة.", listOf("ملح", "زعتر", "زيت زيتون", "خيار"), carbs = 1.5, protein = 6.0, fat = 10.0, iron = 0.2, calcium = 120.0, folate = 8.0),
        SubcategoryInfo("جبن أزرق ريكفورد", 150, SafetyLevel.AVOID, "⚠️ تجنبي الأجبان الزرقاء غير المبسترة لاحتمالية احتوائها على بكتيريا الليستيريا الخطيرة على الجنين.", emptyList(), carbs = 1.0, protein = 6.0, fat = 12.0)
    ),
    "☕ مشروبات وأعشاب" to listOf(
        SubcategoryInfo("شاي أحمر/أخضر", 40, SafetyLevel.CAUTION, "☕ تجنبي شربه بعد الوجبات مباشرة بمسافة ساعة ونصف لضمان امتصاص الحديد بالكامل.", listOf("سكر", "عسل نحل", "محلى صناعي", "نعناع", "قرنفل"), carbs = 8.0),
        SubcategoryInfo("قهوة فرنساوي", 65, SafetyLevel.CAUTION, "☕ الكافيين آمن باعتدال (حدود ٢٠٠ ملغ يومياً، كوب قهوة)، تجنبي زيادة الاستهلاك عن ذلك.", listOf("سكر", "عسل نحل", "محلى صناعي", "حليب"), carbs = 5.0, protein = 2.0, fat = 3.0, calcium = 50.0),
        SubcategoryInfo("ينسون وأعشاب دافئة", 30, SafetyLevel.SAFE, "🍵 مشروب دافئ مهدئ للأعصاب ويساعد على الهضم والاسترخاء.", listOf("سكر", "عسل نحل", "محلى صناعي"), carbs = 6.0, waterBenefit = 200),
        SubcategoryInfo("كوب كركديه بارد", 50, SafetyLevel.SAFE, "🥤 رائع لتوسيع الأوعية وتخفيض الضغط وممتاز للضغط العالي ولذيذ كمرطب.", listOf("سكر", "عسل نحل", "محلى صناعي", "ثلج"), carbs = 12.0, waterBenefit = 200)
    ),
    "🥣 شوربات وأكلات مرقة" to listOf(
        SubcategoryInfo("شوربة ملوخية مصرية", 90, SafetyLevel.SAFE, "🥬 الملوخية غنية بالحديد والورقيات وممتازة للهضم والدورة الدموية.", listOf("فلفل أسود", "كمون", "عصير ليمون"), carbs = 8.0, protein = 3.0, fat = 4.0, iron = 1.8),
        SubcategoryInfo("شوربة لحمة/فراخ مرقة", 110, SafetyLevel.SAFE, "🥣 شوربة مرقة دافئة ومغذية تساعد على الترطيب واستعادة الفيتامينات الكولاجينية.", listOf("فلفل أسود", "عصير ليمون", "بقدونس"), carbs = 2.0, protein = 8.0, fat = 6.0, calcium = 15.0),
        SubcategoryInfo("شوربة خضار مشكل", 75, SafetyLevel.SAFE, "🥕 شوربة خضار دافئة مليئة بالفيتامينات والمعادن وسهلة الهضم.", listOf("فلفل أسود", "كمون", "عصير ليمون"), carbs = 12.0, protein = 2.0, fat = 1.5, iron = 1.0, calcium = 25.0, folate = 20.0),
        SubcategoryInfo("شوربة ماشروم (مشروم)", 120, SafetyLevel.SAFE, "🍄 شوربة دافئة ولذيذة تمنحكِ شعوراً بالدفء والشبع وغنية بالألياف.", listOf("فلفل أسود", "بقدونس"), carbs = 8.0, protein = 4.0, fat = 7.0)
    ),
    "🥗 سلطات" to listOf(
        SubcategoryInfo("سلطة خضراء مصرية", 60, SafetyLevel.SAFE, "🥬 غنية جداً بالألياف الهامة وممتازة كوقاية وعلاج للإمساك.", listOf("طماطم", "خيار", "ليمون", "زيت زيتون", "ملح", "كمون"), carbs = 6.0, protein = 1.5, fat = 3.0, iron = 1.5, calcium = 40.0, folate = 45.0, waterBenefit = 150),
        SubcategoryInfo("سلطة طحينة", 120, SafetyLevel.SAFE, "🥗 غنية بالدهون الصحية والكالسيوم والحديد ومغذية جداً.", listOf("كمون", "شطة", "ملح"), carbs = 4.0, protein = 3.0, fat = 11.0, iron = 2.0, calcium = 90.0),
        SubcategoryInfo("سلطة بابا غنوج", 100, SafetyLevel.SAFE, "🍆 الباذنجان غني بالحديد والألياف المفيدة ومغذي ومنشط للدورة.", listOf("طحينة", "ثوم", "ملح", "كمون"), carbs = 7.0, protein = 2.0, fat = 8.0, iron = 1.2, calcium = 30.0)
    ),
    "🐟 أسماك ولحوم" to listOf(
        SubcategoryInfo("سمك بلطي مشوي", 160, SafetyLevel.SAFE, "🐟 السمك المشوي سهل الهضم وغني بالفوسفور والبروتين المغذي لكِ ولطفلكِ.", listOf("عصير ليمون", "طماطم وجرجير", "ملح", "كمون"), carbs = 0.0, protein = 26.0, fat = 5.0, iron = 1.0, calcium = 30.0, folate = 15.0),
        SubcategoryInfo("تونة معلبة صفيت", 180, SafetyLevel.CAUTION, "🐟 التونة آمنة ومفيدة جداً، ولكن ينصح بعدم تجاوز علبتين أسبوعياً تجنباً لارتفاع مادة الزئبق.", listOf("بصل", "ليمون", "خل", "فلفل أسود"), carbs = 0.0, protein = 24.0, fat = 8.0, iron = 1.5, calcium = 15.0, folate = 10.0),
        SubcategoryInfo("فسيخ ورنجة مملحة", 350, SafetyLevel.AVOID, "⚠️ تجنبي الأسماك المملحة والنيئة كلياً لاحتمالية تسممها أو احتوائها على طفيليات قد تضر بالحمل.", emptyList(), carbs = 0.0, protein = 20.0, fat = 25.0)
    ),
    "🌾 نشويات" to listOf(
        SubcategoryInfo("أرز مصري مطبوخ", 140, SafetyLevel.SAFE, "🍚 مصدر رائع ومغذي للطاقة المعتدلة وسهل الهضم جداً.", listOf("شعرية", "سمن بلدي", "ملح"), carbs = 30.0, protein = 2.5, fat = 1.0, calcium = 10.0),
        SubcategoryInfo("عيش بلدي ربع رغيف", 80, SafetyLevel.SAFE, "🌾 العيش البلدي غني بالردة والألياف المفيدة للهضم وامتصاص السكر المتوازن.", listOf("ردة"), carbs = 18.0, protein = 3.0, fat = 0.5, iron = 0.8, calcium = 15.0),
        SubcategoryInfo("مكرونة مسلوقة", 150, SafetyLevel.SAFE, "🍝 نشويات معقدة تمدكِ بالطاقة لفترات طويلة وتساعد على الشبع.", listOf("صلصة طماطم", "جبنة رومي", "ملح"), carbs = 32.0, protein = 5.0, fat = 0.6, calcium = 12.0)
    )
)

// +++ أضيف بناءً على طلبك لدعم مكتبة نصائح جوري التفاعلية +++
data class AdviceCardInfo(
    val title: String,
    val summary: String,
    val details: String,
    val icon: String,
    val tips: List<String>
)

val adviceLibrary = mapOf(
    "الحمل 🤰" to listOf(
        AdviceCardInfo(
            title = "غثيان الصباح وكيفية تخفيفه طبيعياً 🍋",
            summary = "طرق سريعة للتعامل مع غثيان الثلث الأول من الحمل بدون أدوية.",
            details = "غثيان الصباح من الأعراض الشائعة والناتجة عن ارتفاع هرمون الحمل (hCG) وهرمون الاستروجين. لتخفيفه، اعتمدي على الخيارات المنزلية الفعالة التالية:",
            icon = "🍋",
            tips = listOf(
                "تناولي بسكويتاً مالحاً جافاً أو بقسماط فور الاستيقاظ وقبل مغادرة الفراش بـ ١٥ دقيقة.",
                "قسّمي وجباتكِ إلى ٥-٦ وجبات صغيرة جداً طوال اليوم حتى لا تبقى معدتكِ فارغة أبداً.",
                "منقوع الزنجبيل الدافئ مع بضع قطرات من الليمون فعال جداً ومثبت علمياً لتسكين الغثيان.",
                "تجنبي الروائح القوية أو الأطعمة المقلية والدسمة التي تهيج جدار المعدة."
            )
        ),
        AdviceCardInfo(
            title = "أهمية حمض الفوليك والجرعات الموصى بها 💊",
            summary = "لماذا يعتبر حمض الفوليك بطلاً لرحلة الحمل وصحة طفلك؟",
            details = "حمض الفوليك (فيتامين ب٩) ضروري جداً لمنع العيوب الخلقية في الجهاز العصبي والعمود القبلي للجنين (Neural Tube Defects). يوصى بالبدء فيه قبل الحمل بثلاثة أشهر وخلال الثلث الأول على الأقل.",
            icon = "💊",
            tips = listOf(
                "الجرعة اليومية القياسية هي ٤٠٠ ميكروجرام وتصل لـ ٨٠٠ ميكروجرام في بعض الحالات تحت إشراف الطبيب.",
                "تناولي الأطعمة الغنية بالفولات كالسبانخ، البروكلي، البقوليات، وعصير البرتقال الطازج.",
                "لا تغفلي تناول المكمل الموصوف من طبيبتكِ بانتظام يومياً في نفس الموعد."
            )
        )
    ),
    "الدورة 🩸" to listOf(
        AdviceCardInfo(
            title = "تسكين ألم الدورة والتقلصات بدون أدوية ☕",
            summary = "نصائح دافئة لتخفيف تشنجات الطمث والاسترخاء.",
            details = "تقلصات الدورة ناتجة عن إفراز مادة البروستاجلاندين التي تسبب انقباض عضلات الرحم لطرد البطانة. يمكنكِ تلطيف هذا الألم بطرق طبيعية آمنة:",
            icon = "☕",
            tips = listOf(
                "استخدمي قربة ماء دافئ على منطقة أسفل البطن أو أسفل الظهر لإرخاء العضلات فوراً.",
                "اشربي مشروبات عشبية دافئة مهدئة ومسكنة كالقرفة والبابونج والينسون الدافيء.",
                "الاستحمام بماء دافئ يساعد على تنشيط الدورة الدموية وإزالة الاحتقان والتشنج.",
                "احرصي على المشي الخفيف لتهدئة عضلات الحوض ورفع هرمونات السعادة."
            )
        )
    ),
    "النفسية 🧠" to listOf(
        AdviceCardInfo(
            title = "التعامل مع تقلبات المزاج وهرمونات الحمل 🌸",
            summary = "كيف تواجهين التغيرات المزاجية والقلق بكل هدوء وتوازن؟",
            details = "تقلب المزاج أمر طبيعي تماماً نتيجة للتغيرات الهرمونية الحادة وتأثير الاستروجين والبروجسترون على كيمياء الدماغ، بالإضافة للتفكير الزائد في المسؤوليات الجديدة.",
            icon = "🌸",
            tips = listOf(
                "تقبلي مشاعركِ تماماً واعلمي أنها مؤقتة وطبيعية وليست عيباً فيكِ.",
                "مارسي التنفس العميق (شهيق من الأنف لـ ٤ ثوانٍ، كتم لـ ٤ ثوانٍ، زفير من الفم لـ ٦ ثوانٍ).",
                "عبري عن مشاعركِ واكتبي مذكراتكِ في قسم الفضفضة واليوميات السري الخاص بنا للتنفيس والراحة.",
                "تحدثي مع شريككِ أو صديقتكِ المقربة عن مخاوفكِ ولا تترددي في طلب الدعم والدلال."
            )
        )
    ),
    "التغذية 🥑" to listOf(
        AdviceCardInfo(
            title = "أقوى المصادر الغذائية لعلاج ومنع الأنيميا 🩸",
            summary = "دليلكِ لرفع مخزون الحديد والهموجلوبين بطريقة صحية ولذيذة.",
            details = "خلال الحمل، يتضاعف حجم الدم في جسمكِ، مما يجعلكِ بحاجة ماسة لزيادة استهلاك الحديد لتفادي الأنيميا التي تسبب الهبوط المستمر وضيق التنفس.",
            icon = "🩸",
            tips = listOf(
                "تناولي الكبدة (باعتدال)، اللحوم الحمراء، العسل الأسود، السبانخ، والعدس.",
                "القاعدة الذهبية: اعصري ليمونة (فيتامين سي) على وجبة الحديد لمضاعفة امتصاصه عدة مرات.",
                "تجنبي تماماً شرب الشاي أو القهوة بعد الوجبات مباشرة (انتظري ساعة ونصف على الأقل) لأن التانين والكافيين يمنعان امتصاص الحديد."
            )
        )
    )
)

// --- Custom Colors for Woman Companion Theme mapped to Design Tokens ---
object SoftTheme {
    private val _isDark = androidx.compose.runtime.mutableStateOf(true)
    var isDark: Boolean
        get() = _isDark.value
        set(value) { _isDark.value = value }

    val DeepSlate: Color
        get() = if (isDark) Primitives.CosmicNavy else Primitives.BlossomCream

    val CardSlate: Color
        get() = AppLayers.getContainerSurface(isDark)

    val LightPink: Color
        get() = if (isDark) Primitives.LightPinkGlow else Primitives.SoftRoseLight

    val SoftPink: Color
        get() = AppLayers.getInteractiveAccent(isDark)

    val DeepPink: Color
        get() = if (isDark) Primitives.CoralRoseDark else Primitives.MagentaPinkLight

    val MintTeal: Color
        get() = if (isDark) Primitives.OceanMintDark else Primitives.ForestTealLight

    val SoftTeal: Color
        get() = if (isDark) Primitives.SeafoamMintDark else Primitives.EmeraldTealLight

    val GoldFasting: Color
        get() = if (isDark) Primitives.AmberWarningDark else Primitives.AmberWarningLight

    val RedDanger: Color
        get() = if (isDark) Primitives.RedErrorDark else Primitives.RedErrorLight

    val SoftGray: Color
        get() = if (isDark) Primitives.NeutralGrayDark else Primitives.NeutralGrayLight

    val TextWhite: Color
        get() = if (isDark) Color(0xFFF5F6F8) else Primitives.SlateBlueLight

    val BackgroundBrush: Brush
        get() = AppLayers.getBaseCanvas(isDark)
}

// Custom Date Format Helpers
fun formatGregorianDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("ar"))
    return sdf.format(Date(timestamp))
}

fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.forLanguageTag("ar"))
    return sdf.format(Date(timestamp))
}

// --- App Lock / PIN Screen ---
@Composable
fun AppLockScreen(
    viewModel: WomanCompanionViewModel,
    onSuccess: () -> Unit
) {
    val settings by viewModel.appLockSettingsState.collectAsStateWithLifecycle()
    var pinInput by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SoftTheme.BackgroundBrush)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = "قفل التطبيق",
                    tint = SoftTheme.SoftPink,
                    modifier = Modifier.size(72.dp)
                )

                Text(
                    text = "رفيق المرأة 🌸",
                    style = MaterialTheme.typography.headlineMedium,
                    color = SoftTheme.TextWhite,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = if (settings?.isStealthModeEnabled == true) "تأكيد الهوية للوصول" else "الرجاء إدخال رمز المرور PIN لحماية خصوصيتك",
                    style = MaterialTheme.typography.bodyLarge,
                    color = SoftTheme.SoftGray,
                    textAlign = TextAlign.Center
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    repeat(4) { idx ->
                        val active = idx < pinInput.length
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(if (active) SoftTheme.SoftPink else SoftTheme.CardSlate)
                                .border(1.dp, SoftTheme.SoftGray, CircleShape)
                        )
                    }
                }

                if (showError) {
                    Text(
                        text = "رمز PIN غير صحيح، يرجى المحاولة مرة أخرى",
                        color = SoftTheme.RedDanger,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Custom keypad
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.width(280.dp)
                ) {
                    val rows = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf("مسح", "0", "موافق")
                    )
                    rows.forEach { row ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            row.forEach { char ->
                                Button(
                                    onClick = {
                                        showError = false
                                        when (char) {
                                            "مسح" -> {
                                                if (pinInput.isNotEmpty()) pinInput = pinInput.dropLast(1)
                                            }
                                            "موافق" -> {
                                                if (viewModel.unlockApp(pinInput)) {
                                                    onSuccess()
                                                } else {
                                                    showError = true
                                                    pinInput = ""
                                                }
                                            }
                                            else -> {
                                                if (pinInput.length < 4) {
                                                    pinInput += char
                                                    if (pinInput.length == 4) {
                                                        if (viewModel.unlockApp(pinInput)) {
                                                            onSuccess()
                                                        } else {
                                                            showError = true
                                                            pinInput = ""
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(60.dp)
                                        .testTag("keypad_$char"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SoftTheme.CardSlate,
                                        contentColor = SoftTheme.TextWhite
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text(
                                        text = char,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// +++ أضيف بناءً على طلبك لحساب تقدم الشهور الطبية للحمل +++
data class MonthProgress(
    val monthNumber: Int,
    val monthName: String,
    val progressFraction: Float,
    val totalMonths: Int
)

fun calculateMonthProgress(weeks: Int, daysIntoWeek: Int): MonthProgress {
    val ranges = listOf(
        1 to 4,    // Month 1
        5 to 8,    // Month 2
        9 to 13,   // Month 3
        14 to 17,  // Month 4
        18 to 22,  // Month 5
        23 to 27,  // Month 6
        28 to 31,  // Month 7
        32 to 35,  // Month 8
        36 to 40,  // Month 9
        41 to 42   // Month 10 (Post-term)
    )
    
    var currentMonth = 9
    var progressFraction = 0f
    
    for (i in ranges.indices) {
        val (startWeek, endWeek) = ranges[i]
        if (weeks in startWeek..endWeek) {
            currentMonth = i + 1
            val totalWeeksInMonth = (endWeek - startWeek + 1)
            val totalDaysInMonth = totalWeeksInMonth * 7
            val daysCompleted = ((weeks - startWeek) * 7 + daysIntoWeek).coerceIn(0, totalDaysInMonth)
            progressFraction = daysCompleted.toFloat() / totalDaysInMonth.toFloat()
            break
        }
    }
    
    if (weeks >= 41) {
        currentMonth = 10
        val daysCompleted = ((weeks - 41) * 7 + daysIntoWeek).coerceIn(0, 14)
        progressFraction = daysCompleted.toFloat() / 14f
    }
    
    val monthNames = listOf(
        "الشهر الأول", "الشهر الثاني", "الشهر الثالث",
        "الشهر الرابع", "الشهر الخامس", "الشهر السادس",
        "الشهر السابع", "الشهر الثامن", "الشهر التاسع", "الشهر العاشر ⚠️"
    )
    
    val name = if (currentMonth <= monthNames.size) monthNames[currentMonth - 1] else "الشهر العاشر ⚠️"
    val total = if (weeks >= 41) 10 else 9
    
    return MonthProgress(currentMonth, name, progressFraction, total)
}

// --- Pregnancy Dashboard Screen ---
@Composable
fun PregnancyDashboardScreen(
    viewModel: WomanCompanionViewModel,
    onNavigateToSettings: () -> Unit,
    onOpenJouriChat: () -> Unit = {},
    onNavigateToTab: (Int) -> Unit = {}
) {
    val pregState by viewModel.pregnancyState.collectAsStateWithLifecycle()
    val progression = viewModel.getPregnancyProgression()
    val weatherState by viewModel.weatherState.collectAsStateWithLifecycle()
    val periodLogs by viewModel.periodLogsState.collectAsStateWithLifecycle()
    val isUpdateAvailable by viewModel.isGitHubUpdateAvailable.collectAsStateWithLifecycle()
    val isNetworkAvailable by viewModel.isNetworkAvailable.collectAsStateWithLifecycle()

    val todayWaterLog by viewModel.todayWaterLogState.collectAsStateWithLifecycle()
    val todayStepLog by viewModel.todayStepLogState.collectAsStateWithLifecycle()
    val appointments by viewModel.appointmentsState.collectAsStateWithLifecycle()
    val settings by viewModel.appLockSettingsState.collectAsStateWithLifecycle()
    
    val activeStart by viewModel.currentKickSessionStart.collectAsStateWithLifecycle()
    val currentCount by viewModel.currentKickCount.collectAsStateWithLifecycle()

    var showSetupDialog by remember { mutableStateOf(false) }
    var showAddBpDialog by remember { mutableStateOf(false) }
    var showAddJournalDialog by remember { mutableStateOf(false) }

    // Live ticking Arabic clock & date states
    var currentTimeString by remember {
        mutableStateOf(
            java.text.SimpleDateFormat("hh:mm a", java.util.Locale("ar")).format(java.util.Calendar.getInstance().time)
        )
    }
    var currentDateString by remember {
        mutableStateOf(
            java.text.SimpleDateFormat("EEEE، d MMMM", java.util.Locale("ar")).format(java.util.Calendar.getInstance().time)
        )
    }

    LaunchedEffect(Unit) {
        val timeSdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale("ar"))
        val dateSdf = java.text.SimpleDateFormat("EEEE، d MMMM", java.util.Locale("ar"))
        while (true) {
            val cal = java.util.Calendar.getInstance()
            currentTimeString = timeSdf.format(cal.time)
            currentDateString = dateSdf.format(cal.time)
            kotlinx.coroutines.delay(1000)
        }
    }

    var selectedLastPeriod by remember { mutableStateOf<Long?>(null) }
    var weightInput by remember { mutableStateOf("") }
    var heightInput by remember { mutableStateOf("") }

    var bpSystolic by remember { mutableStateOf("") }
    var bpDiastolic by remember { mutableStateOf("") }
    var bpPulse by remember { mutableStateOf("") }
    var bpNotes by remember { mutableStateOf("") }

    var journalContent by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf("🌸 سعيدة") }
    val moods = listOf("🌸 سعيدة", "🌱 هادئة", "🪵 تعبة", "🩸 قلقة", "✨ متحمسة")

    // +++ أضيف بناءً على طلبك لإدارة حوارات نوع الجنين والولادة +++
    var showBabyInfoDialog by remember { mutableStateOf(false) }
    var babyGenderInput by remember { mutableStateOf(pregState?.babyGender ?: "") }
    var babyNameInput by remember { mutableStateOf(pregState?.babyName ?: "") }
    var showDeliveryDialog by remember { mutableStateOf(false) }

    LaunchedEffect(showSetupDialog, periodLogs) {
        if (showSetupDialog && (selectedLastPeriod == null || selectedLastPeriod == System.currentTimeMillis())) {
            val mostRecent = periodLogs.maxByOrNull { it.startDate }
            if (mostRecent != null) {
                selectedLastPeriod = mostRecent.startDate
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            val companionName = settings?.companionName ?: "جوري"
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val greeting = when {
                hour in 5..11 -> "صباح الورد والرضا يا غالية ☀️"
                hour in 12..17 -> "أهلاً بكِ يا صديقتي ✨"
                else -> "مساء الهدوء والسكينة يا غالية 🌙"
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = greeting,
                                style = MaterialTheme.typography.titleMedium,
                                color = SoftTheme.SoftPink,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "رفيق المرأة 🌸",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = SoftTheme.TextWhite,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isNetworkAvailable) SoftTheme.MintTeal.copy(alpha = 0.15f) else SoftTheme.RedDanger.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(if (isNetworkAvailable) SoftTheme.MintTeal else SoftTheme.RedDanger, CircleShape)
                                        )
                                        Text(
                                            text = if (isNetworkAvailable) "متصل" else "أوفلاين",
                                            color = if (isNetworkAvailable) SoftTheme.MintTeal else SoftTheme.RedDanger,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = { viewModel.toggleDarkMode() },
                                modifier = Modifier.background(SoftTheme.DeepSlate, CircleShape)
                            ) {
                                Icon(
                                    imageVector = if (SoftTheme.isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = "تبديل المظهر",
                                    tint = SoftTheme.SoftPink
                                )
                            }
                            IconButton(
                                onClick = onNavigateToSettings,
                                modifier = Modifier.background(SoftTheme.DeepSlate, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "الإعدادات",
                                    tint = SoftTheme.SoftGray
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "صديقتكِ الوفية $companionName تسهر على راحتكِ الروحية والصحية وتدعمكِ في كل خطوة ومرحلة 💖",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTheme.SoftGray,
                        lineHeight = 16.sp
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = SoftTheme.DeepSlate,
                        thickness = 1.dp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Clock & Date (Right side)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(SoftTheme.SoftPink.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🕒", fontSize = 18.sp)
                            }
                            Column {
                                Text(
                                    text = currentTimeString,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SoftTheme.TextWhite
                                )
                                Text(
                                    text = currentDateString,
                                    fontSize = 10.sp,
                                    color = SoftTheme.SoftGray
                                )
                            }
                        }

                        // Weather Panel (Left side)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (weatherState != null) "${weatherState?.temperature?.toInt()}°م" else "--°م",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SoftTheme.MintTeal
                                )
                                Text(
                                    text = weatherState?.description ?: "جاري الجلب...",
                                    fontSize = 10.sp,
                                    color = SoftTheme.SoftGray
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(SoftTheme.MintTeal.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                val temp = weatherState?.temperature ?: 25.0
                                val emoji = when {
                                    temp > 32 -> "☀️"
                                    temp < 18 -> "🌧️"
                                    else -> "🍃"
                                }
                                Text(emoji, fontSize = 18.sp)
                            }
                        }
                    }
                }
            }
        }

        if (isUpdateAvailable) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(),
                    colors = CardDefaults.cardColors(containerColor = SoftTheme.MintTeal.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, SoftTheme.MintTeal.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("✨", fontSize = 22.sp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "تحديث مصفوفة نصائح جوري متوفر! 🔄",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftTheme.MintTeal
                                )
                                Text(
                                    text = "تتوفر نصائح جديدة ومخصصة ومصفوفة ميزات محدثة على GitHub. اضغطي للترقية الفورية والاستمتاع بأحدث ميزات صديقتكِ جوري!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SoftTheme.TextWhite.copy(alpha = 0.85f),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                        
                        var isSyncing by remember { mutableStateOf(false) }
                        val syncStatus by viewModel.gitHubSyncStatus.collectAsStateWithLifecycle()
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (syncStatus != null) {
                                Text(
                                    text = syncStatus ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SoftTheme.MintTeal,
                                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                                )
                            }
                            Button(
                                onClick = {
                                    isSyncing = true
                                    viewModel.syncJouriMatrix()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.MintTeal),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = if (isSyncing && syncStatus?.contains("مكتملة") != true) "جاري التحديث..." else "تحديث الآن 🚀",
                                    color = SoftTheme.DeepSlate,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }

        // Standalone JouriWeatherHeader removed to avoid duplication since JouriWellnessNotificationCard contains local weather and hydration recommendations
        
        // Dynamic, highly interactive Jouri Wellness & Notification Center
        item {
            JouriWellnessNotificationCard(
                viewModel = viewModel,
                onOpenJouriChat = onOpenJouriChat
            )
        }



        // 🎯 Daily Progress & Briefing Card
        item {
            val waterGoal = viewModel.getWaterTarget()
            val consumedWater = todayWaterLog?.amountMl ?: 0
            val waterPct = if (waterGoal > 0) (consumedWater.toFloat() / waterGoal.toFloat()).coerceIn(0f, 1f) else 0f
            
            val stepGoal = settings?.dailyStepTarget ?: 6000
            val currentSteps = todayStepLog?.steps ?: 0
            val stepsPct = if (stepGoal > 0) (currentSteps.toFloat() / stepGoal.toFloat()).coerceIn(0f, 1f) else 0f
            
            val upcomingAppt = appointments
                .filter { it.dateTime >= System.currentTimeMillis() && !it.completed }
                .minByOrNull { it.dateTime }
                
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "مؤشراتكِ الحيوية اليوم 🎯📈",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.TextWhite
                    )
                    
                    // Water Progress Bar
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.WaterDrop, contentDescription = null, tint = SoftTheme.MintTeal, modifier = Modifier.size(16.dp))
                                Text("شرب الماء والترطيب 🥛", style = MaterialTheme.typography.bodyMedium, color = SoftTheme.TextWhite)
                            }
                            Text("$consumedWater / $waterGoal مل", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(SoftTheme.DeepSlate)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(waterPct)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SoftTheme.MintTeal)
                            )
                        }
                    }
                    
                    // Steps Progress Bar
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.DirectionsRun, contentDescription = null, tint = SoftTheme.SoftPink, modifier = Modifier.size(16.dp))
                                Text("خطوات النشاط اليومي 👣", style = MaterialTheme.typography.bodyMedium, color = SoftTheme.TextWhite)
                            }
                            Text("$currentSteps / $stepGoal خطوة", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(SoftTheme.DeepSlate)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(stepsPct)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SoftTheme.SoftPink)
                            )
                        }
                    }
                    
                    // Upcoming appointment or message
                    if (upcomingAppt != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SoftTheme.DeepSlate)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = SoftTheme.SoftPink, modifier = Modifier.size(18.dp))
                            Column {
                                Text("موعدكِ الطبي القادم 🏥", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftPink, fontWeight = FontWeight.Bold)
                                Text("${upcomingAppt.title} - ${formatGregorianDate(upcomingAppt.dateTime)}", style = MaterialTheme.typography.bodySmall, color = SoftTheme.TextWhite)
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SoftTheme.DeepSlate)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = SoftTheme.MintTeal, modifier = Modifier.size(16.dp))
                            Text("لا توجد مواعيد طبية قادمة مسجلة حالياً.", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                        }
                    }
                }
            }
        }

        // ⚡ Quick Actions & Logging Hub
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    val isKickActive = activeStart != null
                    Text(
                        text = "التسجيل الصحي السريع ⚡",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.TextWhite
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Water Quick Log Button
                        Button(
                            onClick = { viewModel.addWater(250) },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.DeepSlate),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f).height(64.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("🥛 +٢٥٠مل", fontWeight = FontWeight.Bold, color = SoftTheme.MintTeal, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("سجل ماء", style = MaterialTheme.typography.bodySmall, color = SoftTheme.TextWhite, fontSize = 10.sp)
                            }
                        }
                        
                        // Blood Pressure Dialog Trigger
                        Button(
                            onClick = { showAddBpDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.DeepSlate),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f).height(64.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("🩸 قياس", fontWeight = FontWeight.Bold, color = SoftTheme.SoftPink, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("ضغط الدم", style = MaterialTheme.typography.bodySmall, color = SoftTheme.TextWhite, fontSize = 10.sp)
                            }
                        }
                        
                        // Journal/Diary Dialog Trigger
                        Button(
                            onClick = { showAddJournalDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.DeepSlate),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f).height(64.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("📝 تدوين", fontWeight = FontWeight.Bold, color = SoftTheme.SoftPink, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("يومياتي", style = MaterialTheme.typography.bodySmall, color = SoftTheme.TextWhite, fontSize = 10.sp)
                            }
                        }
                        
                        // Fetal Kicks Quick Log
                        Button(
                            onClick = {
                                if (isKickActive) {
                                    viewModel.incrementKickCount()
                                } else {
                                    viewModel.startFetalKickSession()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isKickActive) SoftTheme.SoftPink else SoftTheme.DeepSlate),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1.2f).height(64.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                if (isKickActive) {
                                    Text("🦶 ركلة! ($currentCount)", fontWeight = FontWeight.ExtraBold, color = SoftTheme.DeepSlate, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("احفظ", style = MaterialTheme.typography.bodySmall, color = SoftTheme.DeepSlate, fontSize = 9.sp, modifier = Modifier.clickable { viewModel.saveFetalKickSession() })
                                } else {
                                    Text("🤰 حركة", fontWeight = FontWeight.Bold, color = SoftTheme.MintTeal, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("ركلات الجنين", style = MaterialTheme.typography.bodySmall, color = SoftTheme.TextWhite, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                    
                    if (isKickActive) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("جلسة عد حركة الجنين نشطة حالياً ✨", color = SoftTheme.SoftPink, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "إلغاء ❌",
                                    color = SoftTheme.RedDanger,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable { viewModel.cancelFetalKickSession() }
                                )
                                Text(
                                    text = "حفظ وحساب 🏁",
                                    color = SoftTheme.MintTeal,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable { viewModel.saveFetalKickSession() }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (pregState == null || pregState?.isPregnant != true) {
            // Not Pregnant View - Call to Action
            item {
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
                            text = "تتبع الحمل الشخصي 🤰",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.TextWhite,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "ابدئي تتبع مراحل نمو جنينكِ أسبوعياً، مع حساب تلقائي لموعد الولادة المقدر، وتوجيهات السعرات الغذائية والعناصر الحرجة المناسبة لكِ.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SoftTheme.SoftGray,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                        Button(
                            onClick = { showSetupDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("start_pregnancy_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("البدء في تتبع الحمل الآن 🌸")
                        }
                    }
                }
            }

            // Simple Offline Tips Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "💡 معلومات الدورة الشهرية",
                            color = SoftTheme.MintTeal,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "انتقلي إلى علامة تبويب 'الدورة والخصوبة' لتسجيل دورتكِ الشهرية والتنبؤ بتواريخ الخصوبة والإباضة المستقبلية بمجرد تسجيل ٣ دورات متتالية.",
                            color = SoftTheme.SoftGray,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        } else if (pregState?.isDelivered == true) {
            // +++ أضيف بناءً على طلبك لتقديم نصائح ودعم فترة النفاس والتعافي +++
            item {
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
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("💧", fontSize = 28.sp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "تنبيه شرب الماء للمرضع",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = SoftTheme.TextWhite
                                    )
                                    Text(
                                        text = "تحتاج الأم المرضعة إلى زيادة شرب المياه بمقدار 500-1000 مل إضافية يومياً للحفاظ على كمية إدرار الحليب وصحتها العامة.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SoftTheme.SoftGray,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = { viewModel.switchToPeriodTracking() },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("العودة لتتبع الدورة الطبيعية 🔄", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // Pregnant View - Progression Dashboard
            progression?.let { prog ->
                // Calculate dynamic trimester color
                val trimesterColor = when {
                    prog.weeks >= 41 -> Color(0xFFFFB300) // Month 10: Gold Amber
                    prog.trimester == 1 -> Color(0xFF9575CD) // Trimester 1: Lavender
                    prog.trimester == 2 -> SoftTheme.MintTeal // Trimester 2: Mint Teal
                    else -> SoftTheme.SoftPink // Trimester 3: Soft Pink
                }

                val monthProg = calculateMonthProgress(prog.weeks, prog.daysIntoWeek)
                val activeMonth = monthProg.monthNumber
                val activeMonthProgress = monthProg.progressFraction

                // Baby Info (Gender & Name Display/Edit Card)
                val babyGender = pregState?.babyGender
                val babyName = pregState?.babyName
                val isGenderKnown = !babyGender.isNullOrEmpty()
                
                if (prog.weeks >= 14 || isGenderKnown) {
                    item {
                        if (isGenderKnown) {
                            // Compact space-saving version of Baby Info Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(SoftTheme.DeepSlate),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(if (babyGender == "ولد") "👶" else if (babyGender == "بنت") "👧" else "🤰", fontSize = 18.sp)
                                        }
                                        Column {
                                            val genderEmoji = if (babyGender == "ولد") "💙" else if (babyGender == "بنت") "💗" else "✨"
                                            val genderLabel = if (babyGender == "ولد") "ولد صالح معافى" else if (babyGender == "بنت") "بنت صالحة معافاة" else "مفاجأة مباركة"
                                            val nameLabel = if (!babyName.isNullOrBlank()) "الاسم المقترح: $babyName" else "لم يتم اختيار اسم بعد"
                                            
                                            Text(
                                                text = "$genderLabel $genderEmoji",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = SoftTheme.TextWhite
                                            )
                                            Text(
                                                text = nameLabel,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = SoftTheme.SoftPink
                                            )
                                        }
                                    }
                                    Text(
                                        text = "تعديل 📝",
                                        color = SoftTheme.SoftPink,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.clickable {
                                            babyGenderInput = babyGender ?: ""
                                            babyNameInput = babyName ?: ""
                                            showBabyInfoDialog = true
                                        }
                                    )
                                }
                            }
                        } else {
                            // Expandable standard registration Card (visible only after week 14)
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("🧸", fontSize = 22.sp)
                                            Text(
                                                text = "جنينكِ الغالي",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = SoftTheme.TextWhite
                                            )
                                        }
                                        Text(
                                            text = "تسجيل 📝",
                                            color = SoftTheme.SoftPink,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.clickable {
                                                babyGenderInput = babyGender ?: ""
                                                babyNameInput = babyName ?: ""
                                                showBabyInfoDialog = true
                                            }
                                        )
                                    }
                                    Text(
                                        text = "لقد دخلتِ الأسبوع ١٤ من الحمل 🌸 هل عرفتِ جنس جنينكِ؟ اضغطي لتسجيله واقتراح اسمه لكي يتفاعل رفيقكِ مع جنينكِ بالاسم والتهنئة اللطيفة! 💕",
                                        color = SoftTheme.SoftGray,
                                        style = MaterialTheme.typography.bodySmall,
                                        lineHeight = 16.sp
                                    )
                                    Button(
                                        onClick = { showBabyInfoDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.DeepSlate),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("تسجيل جنس واسم الجنين 👶🍼", color = SoftTheme.TextWhite, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }

                // Primary Weeks Circle Card
                item {
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
                                text = if (prog.weeks >= 41) "أنتِ الآن في الشهر العاشر (تخطي موعد الولادة) ⚠️" else "أنتِ الآن في الأسبوع",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SoftTheme.SoftGray,
                                textAlign = TextAlign.Center
                            )

                            // Week Huge Display Circle (Trimester-Based Dynamic Color)
                            Box(
                                modifier = Modifier
                                    .size(160.dp)
                                    .drawBehind {
                                        drawCircle(
                                            color = SoftTheme.DeepSlate,
                                            radius = size.minDimension / 2
                                        )
                                        // Draw arc with dynamic color
                                        drawArc(
                                            color = trimesterColor,
                                            startAngle = -90f,
                                            sweepAngle = if (prog.weeks >= 41) 360f else ((prog.weeks.toFloat() / 40f) * 360f).coerceIn(0f, 360f),
                                            useCenter = false,
                                            style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${prog.weeks}",
                                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 58.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = SoftTheme.TextWhite
                                    )
                                    Text(
                                        text = "الأيام: ${prog.daysIntoWeek}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = trimesterColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Medically Accurate Segmented Month progress bar
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val monthLabels = listOf("ش1", "ش2", "ش3", "ش4", "ش5", "ش6", "ش7", "ش8", "ش9") + if (prog.weeks >= 41) listOf("ش10") else emptyList()
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "مخطط شهور الحمل التسعة 📅",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SoftTheme.SoftGray
                                    )
                                    Text(
                                        text = "أنتِ في " + when(activeMonth) {
                                            1 -> "الشهر الأول"
                                            2 -> "الشهر الثاني"
                                            3 -> "الشهر الثالث"
                                            4 -> "الشهر الرابع"
                                            5 -> "الشهر الخامس"
                                            6 -> "الشهر السادس"
                                            7 -> "الشهر السابع"
                                            8 -> "الشهر الثامن"
                                            9 -> "الشهر التاسع"
                                            else -> "الشهر العاشر ⚠️"
                                        } + " (${(activeMonthProgress * 100).toInt()}% من الشهر)",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = trimesterColor
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val totalSegments = if (prog.weeks >= 41) 10 else 9
                                    for (i in 0 until totalSegments) {
                                        val segProgress = when {
                                            i < activeMonth - 1 -> 1f
                                            i == activeMonth - 1 -> activeMonthProgress
                                            else -> 0f
                                        }
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(10.dp)
                                                .clip(RoundedCornerShape(5.dp))
                                                .background(SoftTheme.DeepSlate)
                                        ) {
                                            if (segProgress > 0f) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxHeight()
                                                        .fillMaxWidth(segProgress)
                                                        .background(
                                                            Brush.horizontalGradient(
                                                                colors = listOf(trimesterColor.copy(alpha = 0.7f), trimesterColor)
                                                            )
                                                        )
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("الثلث", color = SoftTheme.SoftGray, style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        text = if (prog.weeks >= 41) "أمان ممتد" else "${prog.trimester}",
                                        color = SoftTheme.TextWhite,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                                VerticalDivider(modifier = Modifier.height(24.dp).width(1.dp), color = SoftTheme.SoftGray)
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("الأيام المتبقية", color = SoftTheme.SoftGray, style = MaterialTheme.typography.bodySmall)
                                    Text("${prog.remainingDays}", color = SoftTheme.TextWhite, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                }
                                VerticalDivider(modifier = Modifier.height(24.dp).width(1.dp), color = SoftTheme.SoftGray)
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("موعد الولادة", color = SoftTheme.SoftGray, style = MaterialTheme.typography.bodySmall)
                                    val formatted = SimpleDateFormat("dd MMM", Locale.forLanguageTag("ar")).format(Date(prog.dueDate))
                                    Text(formatted, color = SoftTheme.TextWhite, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                }
                            }
                        }
                    }
                }

                // Post-term Pregnancy (الشهر العاشر) Supportive Card
                if (prog.weeks >= 40) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(1.5.dp, Color(0xFFFFB300))
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("📢", fontSize = 24.sp)
                                    Text(
                                        text = "الولادة بعد موعدكِ المقدر (الشهر العاشر) 🌸🏥",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFB300)
                                    )
                                }

                                Text(
                                    text = "صديقتي الغالية، تخطي موعد الولادة المتوقع (الأسبوع 40) هو أمر طبيعي جداً وشائع ويحدث لـ 10% من الأمهات، ولا يدعو للقلق أبداً. إليكِ أهم الإرشادات الطبية والآمنة للتعامل مع هذا الموعد بروية وأمان:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SoftTheme.TextWhite,
                                    lineHeight = 18.sp
                                )

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "📋 الخطوات المطلوبة فوراً:",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = SoftTheme.MintTeal
                                        )
                                        Text(
                                            text = "• المتابعة الطبية الفائقة: زوري الطبيبة كل يومين لفحص حجم السائل الأمنيوسي بالسونار وعمل تخطيط قلب الجنين للتأكد التام من سلامته ونشاط المشيمة.\n• مراقبة حركة الجنين: تأكدي من أن الجنين يتحرك بشكل طبيعي (الحد الأدنى 10 حركات واضحة خلال 12 ساعة).\n• المحفزات الطبيعية الآمنة: ممارسة المشي الخفيف لتسهيل نزول الرأس للحوض، العلاقة الزوجية لفرز هرمونات تليين عنق الرحم الطبيعية، وتناول 6 حبات تمر يومياً.\n• متى تذهبين فوراً للمستشفى؟ عند نزول ماء الجنين (انفجار كيس الماء) حتى بدون ألم، وجود نزيف، أو تراجع ملحوظ في الحركة.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SoftTheme.SoftGray,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Baby Size Visual Comparison Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(SoftTheme.DeepSlate),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = prog.comparisonIcon,
                                    fontSize = 36.sp
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "طفلكِ الآن بحجم:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SoftTheme.SoftGray
                                )
                                Text(
                                    text = prog.comparisonName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftTheme.SoftPink
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = prog.developmentTip,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SoftTheme.TextWhite,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }

                // Safety Birth Baby Announcement Card (Show only late in pregnancy, Week 36 or later)
                if (prog.weeks >= 36) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Text(
                                    text = "بشرى ولادة جديدة؟ ✨👶🎉",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftTheme.MintTeal
                                )
                                Text(
                                    text = "إذا منّ الله عليكِ بالولادة بفضله، شاركينا لنحتفي بكِ ونقدم لكِ إرشادات فترة النفاس والتعافي المثالية الخاصة بطريقة ولادتكِ 💖",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SoftTheme.SoftGray,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 16.sp
                                )
                                Button(
                                    onClick = { showDeliveryDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text("الحمد لله، وضعتُ مولودي بالسلامة! 🥰", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }

                // Discard Pregnancy Info Button
                item {
                    OutlinedButton(
                        onClick = { viewModel.switchToPeriodTracking() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftTheme.RedDanger),
                        border = BorderStroke(1.dp, SoftTheme.RedDanger.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("إنهاء تتبع الحمل الحالي والعودة للدورة")
                    }
                }
            }
        }

        // 📖✨ Spiritual & Quietude Reflection Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate.copy(alpha = 0.8f)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "الجانب الروحي والسكينة 📖✨",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.SoftPink,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "﴿رَبِّ هَبْ لِي مِن لَّدُنكَ ذُرِّيَّةً طَيِّبَةً ۖ إِنَّكَ سَمِيعُ الدُّعَاءِ﴾",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.TextWhite,
                        textAlign = TextAlign.Center,
                        lineHeight = 26.sp
                    )
                    Text(
                        text = "طمأنينة قلبكِ وراحتكِ النفسية تنعكس حباً وسلاماً على صحتكِ ونمو طفلكِ. استودعي نفسكِ وجنينكِ الله الخالق العليم كل يوم.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTheme.SoftGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }

    // Pregnancy setup Dialog
    if (showSetupDialog) {
        Dialog(onDismissRequest = { showSetupDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "تهانينا! ابدئي رحلة حمل آمنة 🌸",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.SoftPink
                    )

                    Text(
                        text = "تاريخ أول يوم لآخر دورة شهرية:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoftTheme.TextWhite
                    )

                    // Dynamic period selection based on actual recorded periods, with fallback to computed dates
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (periodLogs.isNotEmpty()) {
                            Text(
                                text = "اختاري تاريخاً من دوراتكِ الشهرية السابقة: 👇",
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftTheme.SoftPink,
                                fontWeight = FontWeight.Bold
                            )
                            val sortedPeriods = periodLogs.sortedByDescending { it.startDate }.take(4)
                            sortedPeriods.forEach { log ->
                                val startTime = log.startDate
                                val isStartSelected = selectedLastPeriod == startTime
                                val startLabel = "بداية الدورة: ${formatGregorianDate(startTime)}"
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isStartSelected) SoftTheme.SoftPink else SoftTheme.DeepSlate)
                                        .clickable { selectedLastPeriod = startTime }
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = startLabel,
                                            color = if (isStartSelected) SoftTheme.DeepSlate else SoftTheme.TextWhite,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                    if (isStartSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = SoftTheme.DeepSlate
                                        )
                                    }
                                }

                                if (log.endDate != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val endTime = log.endDate
                                    val isEndSelected = selectedLastPeriod == endTime
                                    val endLabel = "نهاية الدورة: ${formatGregorianDate(endTime)} 🏁"
                                    
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isEndSelected) SoftTheme.SoftPink else SoftTheme.DeepSlate)
                                            .clickable { selectedLastPeriod = endTime }
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = endLabel,
                                                color = if (isEndSelected) SoftTheme.DeepSlate else SoftTheme.TextWhite,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        }
                                        if (isEndSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = SoftTheme.DeepSlate
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "أو اختاري تاريخاً مخصصاً آخر:",
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftTheme.SoftGray,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        val dates = remember {
                            (0..4).map { weeksAgo ->
                                val cal = Calendar.getInstance()
                                cal.add(Calendar.WEEK_OF_YEAR, -weeksAgo)
                                cal.timeInMillis
                            }
                        }
                        dates.forEach { time ->
                            // Avoid duplicating if this exact time is already shown as a period log
                            val alreadyShown = periodLogs.any { it.startDate == time }
                            if (!alreadyShown) {
                                val isSelected = selectedLastPeriod == time
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) SoftTheme.SoftPink else SoftTheme.DeepSlate)
                                        .clickable { selectedLastPeriod = time }
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        formatGregorianDate(time),
                                        color = if (isSelected) SoftTheme.DeepSlate else SoftTheme.TextWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = SoftTheme.DeepSlate
                                        )
                                    }
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { weightInput = it },
                        label = { Text("الوزن قبل الحمل (كجم)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoftTheme.SoftPink,
                            unfocusedBorderColor = SoftTheme.SoftGray,
                            focusedTextColor = SoftTheme.TextWhite,
                            unfocusedTextColor = SoftTheme.TextWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = heightInput,
                        onValueChange = { heightInput = it },
                        label = { Text("الطول (سم)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoftTheme.SoftPink,
                            unfocusedBorderColor = SoftTheme.SoftGray,
                            focusedTextColor = SoftTheme.TextWhite,
                            unfocusedTextColor = SoftTheme.TextWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(
                            onClick = { showSetupDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء", color = SoftTheme.SoftGray)
                        }

                        Button(
                            onClick = {
                                val date = selectedLastPeriod ?: System.currentTimeMillis()
                                val weight = weightInput.toDoubleOrNull()
                                val height = heightInput.toDoubleOrNull()
                                viewModel.setPregnancy(date, weight, height)
                                showSetupDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("حفظ")
                        }
                    }
                }
            }
        }
    }

    // --- Blood Pressure Quick Logging Dialog ---
    if (showAddBpDialog) {
        Dialog(onDismissRequest = { showAddBpDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "تسجيل قياس ضغط الدم 🩸",
                        style = MaterialTheme.typography.titleLarge,
                        color = SoftTheme.TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "قيسي ضغطكِ أثناء الراحة وسجلي القراءات لمتابعة صحتكِ الوقائية.",
                        color = SoftTheme.SoftGray,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )

                    OutlinedTextField(
                        value = bpSystolic,
                        onValueChange = { bpSystolic = it },
                        label = { Text("الضغط الانقباضي (العالي - مثال: 120)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoftTheme.SoftPink,
                            unfocusedBorderColor = SoftTheme.SoftGray,
                            focusedTextColor = SoftTheme.TextWhite,
                            unfocusedTextColor = SoftTheme.TextWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = bpDiastolic,
                        onValueChange = { bpDiastolic = it },
                        label = { Text("الضغط الانبساطي (الواطي - مثال: 80)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoftTheme.SoftPink,
                            unfocusedBorderColor = SoftTheme.SoftGray,
                            focusedTextColor = SoftTheme.TextWhite,
                            unfocusedTextColor = SoftTheme.TextWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = bpPulse,
                        onValueChange = { bpPulse = it },
                        label = { Text("نبضات القلب (اختياري - مثال: 72)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoftTheme.SoftPink,
                            unfocusedBorderColor = SoftTheme.SoftGray,
                            focusedTextColor = SoftTheme.TextWhite,
                            unfocusedTextColor = SoftTheme.TextWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = bpNotes,
                        onValueChange = { bpNotes = it },
                        label = { Text("ملاحظات (مثال: بعد تناول الكركديه)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoftTheme.SoftPink,
                            unfocusedBorderColor = SoftTheme.SoftGray,
                            focusedTextColor = SoftTheme.TextWhite,
                            unfocusedTextColor = SoftTheme.TextWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(
                            onClick = { showAddBpDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء", color = SoftTheme.SoftGray)
                        }

                        Button(
                            onClick = {
                                val sysVal = bpSystolic.toIntOrNull()
                                val diaVal = bpDiastolic.toIntOrNull()
                                if (sysVal != null && diaVal != null) {
                                    viewModel.addBloodPressureLog(
                                        systolic = sysVal,
                                        diastolic = diaVal,
                                        pulse = bpPulse.toIntOrNull(),
                                        notes = bpNotes.ifEmpty { null }
                                    )
                                    showAddBpDialog = false
                                    bpSystolic = ""
                                    bpDiastolic = ""
                                    bpPulse = ""
                                    bpNotes = ""
                                }
                            },
                            enabled = bpSystolic.isNotEmpty() && bpDiastolic.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SoftTheme.SoftPink,
                                disabledContainerColor = SoftTheme.SoftPink.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("حفظ")
                        }
                    }
                }
            }
        }
    }

    // --- Journal Quick Logging Dialog ---
    if (showAddJournalDialog) {
        var selectedMood by remember { mutableStateOf("🌸") }
        val moods = listOf("🌸", "😊", "😴", "😔", "🤰")
        Dialog(onDismissRequest = { showAddJournalDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "تدوين يومية جديدة 📝",
                        style = MaterialTheme.typography.titleLarge,
                        color = SoftTheme.TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Mood Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        moods.forEach { mood ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (selectedMood == mood) SoftTheme.SoftPink else SoftTheme.DeepSlate)
                                    .clickable { selectedMood = mood },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(mood, fontSize = 20.sp)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = journalContent,
                        onValueChange = { journalContent = it },
                        label = { Text("اكتبي مشاعركِ أو ملاحظاتكِ هنا...") },
                        minLines = 3,
                        maxLines = 5,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoftTheme.SoftPink,
                            unfocusedBorderColor = SoftTheme.SoftGray,
                            focusedTextColor = SoftTheme.TextWhite,
                            unfocusedTextColor = SoftTheme.TextWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(
                            onClick = { showAddJournalDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء", color = SoftTheme.SoftGray)
                        }

                        Button(
                            onClick = {
                                if (journalContent.isNotEmpty()) {
                                    viewModel.addJournalEntry(journalContent, selectedMood)
                                    showAddJournalDialog = false
                                    journalContent = ""
                                }
                            },
                            enabled = journalContent.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SoftTheme.SoftPink,
                                disabledContainerColor = SoftTheme.SoftPink.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("حفظ")
                        }
                    }
                }
            }
        }
    }

    // +++ أضيف بناءً على طلبك لتقديم حوار تسجيل جنس الجنين واسمه المقترح +++
    if (showBabyInfoDialog) {
        Dialog(onDismissRequest = { showBabyInfoDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "تسجيل جنس واسم الجنين 👶🍼",
                        style = MaterialTheme.typography.titleLarge,
                        color = SoftTheme.TextWhite,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "شاركينا جنس واسم جنينكِ لنخصص التوجيهات باسمه العذب وندخل البهجة على رحلتكما 💖",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTheme.SoftGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )

                    // Gender Selection Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("ولد", "بنت", "مفاجأة").forEach { gender ->
                            val isSelected = babyGenderInput == gender
                            Button(
                                onClick = { babyGenderInput = gender },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) SoftTheme.SoftPink else SoftTheme.DeepSlate
                                ),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = when (gender) {
                                        "ولد" -> "ولد 💙"
                                        "بنت" -> "بنت 💗"
                                        else -> "مفاجأة 🤫"
                                    },
                                    color = if (isSelected) Color.White else SoftTheme.SoftGray,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Baby Name Text Field
                    OutlinedTextField(
                        value = babyNameInput,
                        onValueChange = { babyNameInput = it },
                        label = { Text("الاسم المقترح لجنينكِ العذب:") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoftTheme.SoftPink,
                            unfocusedBorderColor = SoftTheme.SoftGray,
                            focusedTextColor = SoftTheme.TextWhite,
                            unfocusedTextColor = SoftTheme.TextWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(
                            onClick = { showBabyInfoDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء", color = SoftTheme.SoftGray)
                        }

                        Button(
                            onClick = {
                                viewModel.updateBabyInfo(babyGenderInput.ifEmpty { null }, babyNameInput.ifEmpty { null })
                                showBabyInfoDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("حفظ")
                        }
                    }
                }
            }
        }
    }

    // +++ أضيف بناءً على طلبك لتقديم حوار اختيار طريقة الولادة طبيعي/قيصري +++
    if (showDeliveryDialog) {
        Dialog(onDismissRequest = { showDeliveryDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "مبارك مبارك يا غالية! 🥳💖👶",
                        style = MaterialTheme.typography.titleLarge,
                        color = SoftTheme.TextWhite,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "ألف الحمد لله على سلامتكِ وسلامة مولودكِ الحبيب، جعله الله ذريّة صالحة بارّة قرّة لعينيكِ.\n\nكيف كانت ولادتكِ الميمونة لكي يقدم لكِ رفيقكِ جوري أهم إرشادات التعافي والنفاس المخصصة لكِ؟",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTheme.SoftGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                viewModel.updateDeliveryInfo(isDelivered = true, birthMethod = "طبيعي")
                                showDeliveryDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("ولادة طبيعية 🌸", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.updateDeliveryInfo(isDelivered = true, birthMethod = "قيصري")
                                showDeliveryDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.DeepSlate),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("ولادة قيصرية 🏥", color = SoftTheme.SoftPink, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// --- Period Tracker & Smart Calendar Screen ---
@Composable
fun PeriodTrackerScreen(
    viewModel: WomanCompanionViewModel,
    onNavigateToTab: (Int) -> Unit = {}
) {
    val periodLogs by viewModel.periodLogsState.collectAsStateWithLifecycle()
    val isPregnant by viewModel.pregnancyState.collectAsStateWithLifecycle()
    val weatherState by viewModel.weatherState.collectAsStateWithLifecycle()

    var showAddPeriodDialog by remember { mutableStateOf(false) }
    var selectedIntensity by remember { mutableStateOf("medium") }
    var painLevel by remember { mutableStateOf(5) }
    var notesInput by remember { mutableStateOf("") }
    val selectedSymptoms = remember { mutableStateListOf<String>() }

    // State for custom previous/past cycle dates
    var useCustomStartDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var useCustomEndDate by remember { mutableStateOf(System.currentTimeMillis() + 5L * 24 * 60 * 60 * 1000) }
    var useSpecificDateRange by remember { mutableStateOf(false) }

    // +++ أضيف بناءً على طلبك لقناة توجيه الحمل +++
    var showPregnancyLmpPromptDialog by remember { mutableStateOf(false) }
    var pendingPeriodStartDate by remember { mutableStateOf<Long?>(null) }

    // +++ أضيف بناءً على طلبك لتتبع الحمل والولادة بشكل ديناميكي +++
    var showBabyInfoDialog by remember { mutableStateOf(false) }
    var babyGenderInput by remember(isPregnant) { mutableStateOf(isPregnant?.babyGender ?: "") }
    var babyNameInput by remember(isPregnant) { mutableStateOf(isPregnant?.babyName ?: "") }
    var showDeliveryDialog by remember { mutableStateOf(false) }
    var showEndPregnancyConfirmDialog by remember { mutableStateOf(false) }
    var showLossSupportDialog by remember { mutableStateOf(false) }

    val symptomsList = listOf("مغص", "إرهاق", "صداع", "تقلب مزاجي", "ألم ظهر")

    // Collapsible/interactive states for a clean, professional, and compact main dashboard
    var isWeatherExpanded by remember { mutableStateOf(false) }
    var isHealthAnalysisExpanded by remember { mutableStateOf(false) }
    var isCalendarExpanded by remember { mutableStateOf(false) }
    var isHistoryExpanded by remember { mutableStateOf(false) }
    var isBabyDetailsExpanded by remember { mutableStateOf(false) }

    val stats = viewModel.getCycleStats()
    val currentPhase = viewModel.getCurrentCyclePhase()

    // Smart Calendar State
    val currentMonthCalendar = remember { Calendar.getInstance() }
    var monthUpdateTrigger by remember { mutableStateOf(0) }

    // Advanced smart predictions: predicted next 4 cycles & ovulation windows
    val lastLog = periodLogs.maxByOrNull { it.startDate }
    val avgCycle = stats.averageCycleLength
    val avgDuration = stats.averagePeriodDuration

    val predictedPeriods = remember(periodLogs, avgCycle, avgDuration) {
        val list = mutableListOf<Pair<Long, Long>>()
        if (lastLog != null && avgCycle > 0) {
            var currentStart = lastLog.startDate
            repeat(4) {
                val nextStart = currentStart + avgCycle.toLong() * 24 * 60 * 60 * 1000
                val nextEnd = nextStart + avgDuration.toLong() * 24 * 60 * 60 * 1000
                list.add(Pair(nextStart, nextEnd))
                currentStart = nextStart
            }
        }
        list
    }

    val predictedOvulations = remember(predictedPeriods, avgCycle) {
        val list = mutableListOf<Pair<Long, Long>>()
        predictedPeriods.forEach { (pStart, _) ->
            val oStart = pStart - 16L * 24 * 60 * 60 * 1000
            val oEnd = pStart - 12L * 24 * 60 * 60 * 1000
            list.add(Pair(oStart, oEnd))
        }
        list
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                Text(
                    text = if (isPregnant?.isPregnant == true) "تتبع الحمل والولادة 🤰💖" else "الدورة والخصوبة 🩸",
                    style = MaterialTheme.typography.titleLarge,
                    color = SoftTheme.SoftPink,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isPregnant?.isPregnant == true) "رحلتكِ الرائعة للعناية بنفسكِ وبجنينكِ خطوة بخطوة مع حساب دقيق لأسابيع الحمل" else "تتبع ذكي وتنبؤ بمراحل الخصوبة بخصوصية كاملة مع إمكانية تسجيل دوراتك السابقة بالكامل",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SoftTheme.SoftGray
                )
            }



            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                        .clickable { isWeatherExpanded = !isWeatherExpanded },
                    colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("🌦️", fontSize = 20.sp)
                                Column {
                                    Text(
                                        text = "الطقس ونصائح الترطيب اليومي",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = SoftTheme.TextWhite
                                    )
                                    Text(
                                        text = "توصيات مخصصة لدرجات الحرارة اليوم",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SoftTheme.SoftGray
                                    )
                                }
                            }
                            Text(
                                text = if (isWeatherExpanded) "إخفاء 🔼" else "عرض التفاصيل 🔽",
                                color = SoftTheme.SoftPink,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (isWeatherExpanded) {
                            Spacer(modifier = Modifier.height(14.dp))
                            JouriWeatherHeader(weatherInfo = weatherState)
                        }
                    }
                }
            }

            if (isPregnant?.isPregnant == true) {
                val pregState = isPregnant
                val progression = viewModel.getPregnancyProgression()
                
                progression?.let { prog ->
                    val trimesterColor = when {
                        prog.weeks >= 41 -> Color(0xFFFFB300) // Month 10: Gold Amber
                        prog.trimester == 1 -> Color(0xFF9575CD) // Trimester 1: Lavender
                        prog.trimester == 2 -> SoftTheme.MintTeal // Trimester 2: Mint Teal
                        else -> SoftTheme.SoftPink // Trimester 3: Soft Pink
                    }

                    val monthProg = calculateMonthProgress(prog.weeks, prog.daysIntoWeek)
                    val activeMonth = monthProg.monthNumber
                    val activeMonthProgress = monthProg.progressFraction

                    val babyGender = pregState?.babyGender
                    val babyName = pregState?.babyName
                    val isGenderKnown = !babyGender.isNullOrEmpty()

                    // Baby Info Card
                    if (prog.weeks >= 14 || isGenderKnown) {
                        item {
                            if (isGenderKnown) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(SoftTheme.DeepSlate),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(if (babyGender == "ولد") "👶" else if (babyGender == "بنت") "👧" else "🤰", fontSize = 18.sp)
                                            }
                                            Column {
                                                val genderEmoji = if (babyGender == "ولد") "💙" else if (babyGender == "بنت") "💗" else "✨"
                                                val genderLabel = if (babyGender == "ولد") "ولد صالح معافى" else if (babyGender == "بنت") "بنت صالحة معافاة" else "مفاجأة مباركة"
                                                val nameLabel = if (!babyName.isNullOrBlank()) "الاسم المقترح: $babyName" else "لم يتم اختيار اسم بعد"
                                                
                                                Text(
                                                    text = "$genderLabel $genderEmoji",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SoftTheme.TextWhite
                                                )
                                                Text(
                                                    text = nameLabel,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = SoftTheme.SoftPink
                                                )
                                            }
                                        }
                                        Text(
                                            text = "تعديل 📝",
                                            color = SoftTheme.SoftPink,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.clickable {
                                                babyGenderInput = babyGender ?: ""
                                                babyNameInput = babyName ?: ""
                                                showBabyInfoDialog = true
                                            }
                                        )
                                    }
                                }
                            } else {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                                    shape = RoundedCornerShape(24.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("🧸", fontSize = 22.sp)
                                                Text(
                                                    text = "جنينكِ الغالي",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SoftTheme.TextWhite
                                                )
                                            }
                                            Text(
                                                text = "تسجيل 📝",
                                                color = SoftTheme.SoftPink,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.clickable {
                                                    babyGenderInput = babyGender ?: ""
                                                    babyNameInput = babyName ?: ""
                                                    showBabyInfoDialog = true
                                                }
                                            )
                                        }
                                        Text(
                                            text = "لقد دخلتِ الأسبوع ١٤ من الحمل 🌸 هل عرفتِ جنس جنينكِ؟ اضغطي لتسجيله واقتراح اسمه لكي يتفاعل رفيقكِ مع جنينكِ بالاسم والتهنئة اللطيفة! 💕",
                                            color = SoftTheme.SoftGray,
                                            style = MaterialTheme.typography.bodySmall,
                                            lineHeight = 16.sp
                                        )
                                        Button(
                                            onClick = { showBabyInfoDialog = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.DeepSlate),
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("تسجيل جنس واسم الجنين 👶🍼", color = SoftTheme.SoftPink, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Primary Weeks Circle Card
                    item {
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
                                    text = if (prog.weeks >= 41) "أنتِ الآن في الشهر العاشر (تخطي موعد الولادة) ⚠️" else "أنتِ الآن في الأسبوع",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SoftTheme.SoftGray,
                                    textAlign = TextAlign.Center
                                )

                                Box(
                                    modifier = Modifier
                                        .size(160.dp)
                                        .drawBehind {
                                            drawCircle(
                                                color = SoftTheme.DeepSlate,
                                                radius = size.minDimension / 2
                                            )
                                            drawArc(
                                                color = trimesterColor,
                                                startAngle = -90f,
                                                sweepAngle = if (prog.weeks >= 41) 360f else ((prog.weeks.toFloat() / 40f) * 360f).coerceIn(0f, 360f),
                                                useCenter = false,
                                                style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                                            )
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "${prog.weeks}",
                                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 58.sp),
                                            fontWeight = FontWeight.Bold,
                                            color = SoftTheme.TextWhite
                                        )
                                        Text(
                                            text = "الأيام: ${prog.daysIntoWeek}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = trimesterColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "مخطط شهور الحمل التسعة 📅",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SoftTheme.SoftGray
                                        )
                                        Text(
                                            text = "أنتِ في " + when(activeMonth) {
                                                1 -> "الشهر الأول"
                                                2 -> "الشهر الثاني"
                                                3 -> "الشهر الثالث"
                                                4 -> "الشهر الرابع"
                                                5 -> "الشهر الخامس"
                                                6 -> "الشهر السادس"
                                                7 -> "الشهر السابع"
                                                8 -> "الشهر الثامن"
                                                9 -> "الشهر التاسع"
                                                else -> "الشهر العاشر ⚠️"
                                            } + " (${(activeMonthProgress * 100).toInt()}% من الشهر)",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = trimesterColor
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val totalSegments = if (prog.weeks >= 41) 10 else 9
                                        for (i in 0 until totalSegments) {
                                            val segProgress = when {
                                                i < activeMonth - 1 -> 1f
                                                i == activeMonth - 1 -> activeMonthProgress
                                                else -> 0f
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(10.dp)
                                                    .clip(RoundedCornerShape(5.dp))
                                                    .background(SoftTheme.DeepSlate)
                                            ) {
                                                if (segProgress > 0f) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxHeight()
                                                            .fillMaxWidth(segProgress)
                                                            .background(
                                                                Brush.horizontalGradient(
                                                                    colors = listOf(trimesterColor.copy(alpha = 0.7f), trimesterColor)
                                                                )
                                                            )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("الثلث", color = SoftTheme.SoftGray, style = MaterialTheme.typography.bodySmall)
                                        Text(
                                            text = if (prog.weeks >= 41) "أمان ممتد" else "${prog.trimester}",
                                            color = SoftTheme.TextWhite,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }
                                    VerticalDivider(modifier = Modifier.height(24.dp).width(1.dp), color = SoftTheme.SoftGray)
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("الأيام المتبقية", color = SoftTheme.SoftGray, style = MaterialTheme.typography.bodySmall)
                                        Text("${prog.remainingDays}", color = SoftTheme.TextWhite, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    }
                                    VerticalDivider(modifier = Modifier.height(24.dp).width(1.dp), color = SoftTheme.SoftGray)
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("موعد الولادة", color = SoftTheme.SoftGray, style = MaterialTheme.typography.bodySmall)
                                        val formatted = SimpleDateFormat("dd MMM", Locale.forLanguageTag("ar")).format(Date(prog.dueDate))
                                        Text(formatted, color = SoftTheme.TextWhite, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    }
                                }
                            }
                        }
                    }

                    // Post-term Pregnancy (الشهر العاشر) Supportive Card
                    if (prog.weeks >= 40) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                                shape = RoundedCornerShape(24.dp),
                                border = BorderStroke(1.5.dp, Color(0xFFFFB300))
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("📢", fontSize = 24.sp)
                                        Text(
                                            text = "الولادة بعد موعدكِ المقدر (الشهر العاشر) 🌸🏥",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFFB300)
                                        )
                                    }

                                    Text(
                                        text = "صديقتي الغالية، تخطي موعد الولادة المتوقع (الأسبوع 40) هو أمر طبيعي جداً وشائع ويحدث لـ 10% من الأمهات، ولا يدعو للقلق أبداً. إليكِ أهم الإرشادات الطبية والآمنة للتعامل مع هذا الموعد بروية وأمان:",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SoftTheme.TextWhite,
                                        lineHeight = 18.sp
                                    )

                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "📋 الخطوات المطلوبة فوراً:",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = SoftTheme.MintTeal
                                            )
                                            Text(
                                                text = "• المتابعة الطبية الفائقة: زوري الطبيبة كل يومين لفحص حجم السائل الأمنيوسي بالسونار وعمل تخطيط قلب الجنين للتأكد التام من سلامته ونشاط المشيمة.\n• مراقبة حركة الجنين: تأكدي من أن الجنين يتحرك بشكل طبيعي (الحد الأدنى 10 حركات واضحة خلال 12 ساعة).\n• المحفزات الطبيعية الآمنة: ممارسة المشي الخفيف لتسهيل نزول الرأس للحوض، العلاقة الزوجية لفرز هرمونات تليين عنق الرحم الطبيعية، وتناول 6 حبات تمر يومياً.\n• متى تذهبين فوراً للمستشفى؟ عند نزول ماء الجنين (انفجار كيس الماء) حتى بدون ألم، وجود نزيف، أو تراجع ملحوظ في الحركة.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = SoftTheme.SoftGray,
                                                lineHeight = 18.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Baby Size Visual Comparison Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(SoftTheme.DeepSlate),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = prog.comparisonIcon,
                                        fontSize = 36.sp
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "طفلكِ الآن بحجم:",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SoftTheme.SoftGray
                                    )
                                    Text(
                                        text = prog.comparisonName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SoftTheme.SoftPink
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = prog.developmentTip,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SoftTheme.TextWhite,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }

                    // Safety Birth Baby Announcement Card (Show only late in pregnancy, Week 36 or later)
                    if (prog.weeks >= 36) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Text(
                                        text = "بشرى ولادة جديدة؟ ✨👶🎉",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = SoftTheme.MintTeal
                                    )
                                    Text(
                                        text = "إذا منّ الله عليكِ بالولادة بفضله، شاركينا لنحتفي بكِ ونقدم لكِ إرشادات فترة النفاس والتعافي المثالية الخاصة بطريقة ولادتكِ 💖",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SoftTheme.SoftGray,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 16.sp
                                    )
                                    Button(
                                        onClick = { showDeliveryDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Text("الحمد لله، وضعتُ مولودي بالسلامة! 🥰", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }

                    // Discard Pregnancy Info Button
                    item {
                        OutlinedButton(
                            onClick = { showEndPregnancyConfirmDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftTheme.RedDanger),
                            border = BorderStroke(1.dp, SoftTheme.RedDanger.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("إنهاء تتبع الحمل الحالي والعودة للدورة")
                        }
                    }
                }
            } else {
                // Interactive Late Period Alert Card when period is late
                val logsSorted = periodLogs.sortedByDescending { it.startDate }
                val lastLog = logsSorted.firstOrNull()
                val daysSinceStart = if (lastLog != null) {
                    ((System.currentTimeMillis() - lastLog.startDate) / (24L * 60 * 60 * 1000)).toInt().coerceAtLeast(0)
                } else 0
                val delayDays = if (lastLog != null) daysSinceStart - stats.averageCycleLength else 0
                val isLateDetected = lastLog != null && delayDays >= 1

                if (isLateDetected) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize(),
                            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(1.5.dp, SoftTheme.SoftPink)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text("⚠️", fontSize = 24.sp)
                                    Column {
                                        Text(
                                            text = "الدورة متأخرة عن موعدها المتوقع بـ $delayDays يوماً! 🌸",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = SoftTheme.SoftPink
                                        )
                                        Text(
                                            text = "ملاحظة ذكية من جوري",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SoftTheme.SoftGray
                                        )
                                    }
                                }

                                Text(
                                    text = "يا روحي، دورتكِ متأخرة عن المتوسط المعتاد ($daysSinceStart يوماً منذ بداية آخر طمث). هل تعتقدين أن هناك احتمال وجود حمل مبارك 🤰، أم أنها مجرد تأخر في تسجيل دورتكِ الجديدة؟",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SoftTheme.TextWhite,
                                    lineHeight = 20.sp
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Button 1: Yes, pregnant!
                                    Button(
                                        onClick = {
                                            viewModel.setPregnancy(
                                                lastPeriodDate = lastLog?.startDate,
                                                preWeight = null,
                                                height = null
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1.2f)
                                    ) {
                                        Text("🤰 نعم، أنا حامل!", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }

                                    // Button 2: No, log new period
                                    Button(
                                        onClick = {
                                            useCustomStartDate = System.currentTimeMillis()
                                            useCustomEndDate = System.currentTimeMillis() + 5L * 24 * 60 * 60 * 1000
                                            useSpecificDateRange = true
                                            showAddPeriodDialog = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.DeepSlate),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("🩸 تسجيل دورة جديدة", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SoftTheme.MintTeal)
                                    }
                                }
                            }
                        }
                    }
                }

                // Cycle Phase Progress Visual Segment Bar (ALWAYS VISIBLE AT TOP OF CYCLE TRACKER!)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("الطور الحالي للجسد", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                                    Text(currentPhase.phaseArabic, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (currentPhase.isLate) SoftTheme.RedDanger.copy(alpha = 0.2f) else SoftTheme.MintTeal.copy(alpha = 0.2f))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "اليوم ${currentPhase.daysInPhase}",
                                        color = if (currentPhase.isLate) SoftTheme.RedDanger else SoftTheme.MintTeal,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Cycle Segmented progress bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(16.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SoftTheme.DeepSlate)
                            ) {
                                Row(modifier = Modifier.fillMaxSize()) {
                                    // Menstruation Segment (Red/Pink)
                                    Box(
                                        modifier = Modifier
                                            .weight(stats.averagePeriodDuration.toFloat())
                                            .fillMaxHeight()
                                            .background(SoftTheme.DeepPink)
                                    )
                                    // Follicular Segment (Teal)
                                    Box(
                                        modifier = Modifier
                                            .weight((stats.averageCycleLength - 16 - stats.averagePeriodDuration).toFloat().coerceAtLeast(1f))
                                            .fillMaxHeight()
                                            .background(SoftTheme.SoftTeal)
                                    )
                                    // Ovulation Segment (Teal Highlight)
                                    Box(
                                        modifier = Modifier
                                            .weight(5f)
                                            .fillMaxHeight()
                                            .background(SoftTheme.MintTeal)
                                    )
                                    // Luteal Segment (Slate Light)
                                    Box(
                                        modifier = Modifier
                                            .weight(11f)
                                            .fillMaxHeight()
                                            .background(SoftTheme.CardSlate)
                                    )
                                }

                                // Current Day indicator slider
                                val ratio = currentPhase.progressFraction.coerceIn(0f, 1f)
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(ratio)
                                        .background(Color.White.copy(alpha = 0.25f))
                                )
                            }

                            Text(
                                text = currentPhase.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftTheme.SoftGray,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // Add Log Action Button (ALWAYS PROMINENT!)
                item {
                    Button(
                        onClick = {
                            if (isPregnant != null && isPregnant?.isPregnant == true) {
                                useSpecificDateRange = true
                                useCustomStartDate = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000 // default to 30 days ago
                                useCustomEndDate = useCustomStartDate + 5L * 24 * 60 * 60 * 1000
                            } else {
                                useSpecificDateRange = false
                            }
                            showAddPeriodDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_period_log_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (isPregnant != null && isPregnant?.isPregnant == true) {
                                "تسجيل دورة شهرية سابقة 📅"
                            } else {
                                "تسجيل تاريخ الدورة الشهرية (جديدة أو سابقة)"
                            }
                        )
                    }
                }

                // Smart Health Analysis Card (لوحة التحليل الصحي والذكاء التوجيهي)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().animateContentSize(),
                        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isHealthAnalysisExpanded = !isHealthAnalysisExpanded },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = SoftTheme.SoftPink
                                    )
                                    Text(
                                        text = "التحليل الصحي الذكي والتوصيات 🧠",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SoftTheme.TextWhite
                                    )
                                }
                                Text(
                                    text = if (isHealthAnalysisExpanded) "إخفاء 🔼" else "تحليل كامل 🔽",
                                    color = SoftTheme.SoftPink,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (isHealthAnalysisExpanded) {
                                Spacer(modifier = Modifier.height(8.dp))
                                if (periodLogs.isEmpty()) {
                                    Text(
                                        text = "قومي بتسجيل دورتكِ الشهرية الأولى (أو دوراتك السابقة) لنتمكن من تقديم نصائح تغذية وراحة مخصصة ذكياً.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = SoftTheme.SoftGray
                                    )
                                } else {
                                    val latestLog = periodLogs.maxByOrNull { it.startDate }
                                    val pain = latestLog?.painLevel ?: 5
                                    val intensity = latestLog?.flowIntensity ?: "medium"
                                    val symptoms = latestLog?.symptoms?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()

                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text(
                                            text = "بناءً على سجلات دورتكِ وأعراضك الأخيرة، إليكِ تقريرنا الطبي التوجيهي الذكي:",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SoftTheme.SoftPink
                                        )

                                        HorizontalDivider(color = SoftTheme.DeepSlate)

                                        // Pain advisory
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text("🌱", fontSize = 18.sp)
                                            Column {
                                                Text("مستوى الألم والتقلصات:", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite, style = MaterialTheme.typography.bodyMedium)
                                                Text(
                                                    text = when {
                                                        pain >= 8 -> "ألم شديد ($pain/10). نوصي بالراحة التامة، استخدام كمادات دافئة على البطن، وتناول المغنيسيوم. إذا استمر الألم الشديد يرجى استشارة الطبيبة."
                                                        pain >= 5 -> "ألم متوسط ($pain/10). كوب من القرفة أو اليانسون الدافئ قد يساعد في تخفيف الانقباضات بشكل رائع."
                                                        else -> "ألم خفيف ($pain/10). مستوى ممتاز ومؤشر على توازن هرموني رائع."
                                                    },
                                                    color = SoftTheme.SoftGray,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        }

                                        // Flow advisory
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text("🩸", fontSize = 18.sp)
                                            Column {
                                                Text("غزارة الطمث ونقص الحديد:", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite, style = MaterialTheme.typography.bodyMedium)
                                                Text(
                                                    text = when (intensity) {
                                                        "heavy" -> "الطمث غزير. من الضروري جداً زيادة تناول الأطعمة الغنية بالحديد (مثل السبانخ واللحم الأحمر) أو فيتامين سي لتعويض الفقد وتجنب فقر الدم."
                                                        "light" -> "الطمث خفيف. طبيعي جداً، استمري في شرب الماء والترطيب."
                                                        else -> "الطمث متوسط ومثالي. كمية تدفق صحية تدل على بطانة رحم سليمة."
                                                    },
                                                    color = SoftTheme.SoftGray,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        }

                                        // Symptoms advisory
                                        if (symptoms.isNotEmpty()) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Text("🧠", fontSize = 18.sp)
                                                Column {
                                                    Text("التعامل مع الأعراض المرافقة:", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite, style = MaterialTheme.typography.bodyMedium)
                                                    val advice = symptoms.map { sym ->
                                                        when (sym) {
                                                            "مغص" -> "للـ مغص: تدليك أسفل الظهر بزيت اللافندر الدافئ."
                                                            "إرهاق" -> "للـ إرهاق: النوم لـ 8 ساعات وتجنب السهر والإجهاد البدني."
                                                            "صداع" -> "للـ صداع: الابتعاد عن الشاشات والترطيب المستمر بشرب الماء."
                                                            "تقلب مزاجي" -> "للـ تقلب المزاجي: ممارسة تمارين تنفس واسترخاء خفيفة لزيادة هرمونات السعادة."
                                                            "ألم ظهر" -> "للـ ألم الظهر: الحفاظ على وضعية جلوس مستقيمة وتجنب حمل الأثقال."
                                                            else -> "الراحة والترطيب الدائم."
                                                        }
                                                    }.joinToString("\n")
                                                    Text(
                                                        text = advice,
                                                        color = SoftTheme.SoftGray,
                                                        style = MaterialTheme.typography.bodySmall
                                                    )
                                                }
                                            }
                                        }

                                        // Smart pregnancy prediction advice
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text("✨", fontSize = 18.sp)
                                            Column {
                                                Text("التنبؤ الذكي بالخصوبة القادمة:", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite, style = MaterialTheme.typography.bodyMedium)
                                                Text(
                                                    text = "بناءً على طول دورتك المعتاد ($avgCycle يوماً)، فإن فرصة الحمل العالية وتاريخ الإباضة القادم سيكون تقريباً في اليوم 14 من بداية دورتك القادمة. يمكنكِ التخطيط لذلكِ بسهولة بالنظر إلى النقط الخضراء في التقويم أدناه.",
                                                    color = SoftTheme.SoftGray,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Mini summary preview when collapsed
                                Text(
                                    text = if (periodLogs.isEmpty()) {
                                        "قومي بتسجيل دورتكِ الشهرية الأولى لبدء التحليل الصحي التلقائي."
                                    } else {
                                        val latestPain = periodLogs.maxByOrNull { it.startDate }?.painLevel ?: 5
                                        if (latestPain >= 7) {
                                            "مستويات الألم الأخيرة مرتفعة نسبياً (${latestPain}/10). انقري لعرض التوصيات الصحية والغذائية المخصصة لراحة جسدك."
                                        } else {
                                            "تحليل: دورتكِ منتظمة بمتوسط $avgCycle يوماً وصحتك تبدو متوازنة. انقري لعرض التفاصيل الكاملة."
                                        }
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SoftTheme.SoftGray
                                )
                            }
                        }
                    }
                }

                item {
                    D3NativeDashboard(periodLogs = periodLogs, stats = stats)
                }
            }

                // Smart Interactive Calendar Component
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize(),
                        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isCalendarExpanded = !isCalendarExpanded },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("📅", fontSize = 24.sp)
                                    Column {
                                        Text(
                                            text = "تقويم الدورة والخصوبة التفاعلي",
                                            fontWeight = FontWeight.Bold,
                                            color = SoftTheme.TextWhite,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            text = "توقعات الإباضة والخصوبة والحيض",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SoftTheme.SoftGray
                                        )
                                    }
                                }
                                Text(
                                    text = if (isCalendarExpanded) "إخفاء 🔼" else "عرض التقويم 🔽",
                                    color = SoftTheme.SoftPink,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (isCalendarExpanded) {
                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = SoftTheme.DeepSlate.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(8.dp))

                                // Month selector row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = {
                                        currentMonthCalendar.add(Calendar.MONTH, -1)
                                        monthUpdateTrigger++
                                    }) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "السابق", tint = SoftTheme.SoftPink)
                                    }

                                val monthNames = listOf("يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو", "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر")
                                Text(
                                    text = "${monthNames[currentMonthCalendar.get(Calendar.MONTH)]} ${currentMonthCalendar.get(Calendar.YEAR)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftTheme.TextWhite
                                )

                                IconButton(onClick = {
                                    currentMonthCalendar.add(Calendar.MONTH, 1)
                                    monthUpdateTrigger++
                                }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "التالي", tint = SoftTheme.SoftPink)
                                }
                            }

                            // Days of week row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val weekdays = listOf("أحد", "اثنين", "ثلاثاء", "أربعاء", "خميس", "جمعة", "سبت")
                                weekdays.forEach { day ->
                                    Text(
                                        text = day,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SoftTheme.SoftGray,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Days grid
                            val dummyDays = remember(monthUpdateTrigger) {
                                val list = mutableListOf<Long?>()
                                val tempCal = currentMonthCalendar.clone() as Calendar
                                tempCal.set(Calendar.DAY_OF_MONTH, 1)
                                val firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK) // 1 = Sunday

                                repeat(firstDayOfWeek - 1) {
                                    list.add(null)
                                }

                                val maxDays = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)
                                repeat(maxDays) { idx ->
                                    tempCal.set(Calendar.DAY_OF_MONTH, idx + 1)
                                    list.add(tempCal.timeInMillis)
                                }
                                list
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                val chunked = dummyDays.chunked(7)
                                chunked.forEach { rowDays ->
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        rowDays.forEach { dayTime ->
                                            if (dayTime == null) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            } else {
                                                val dayCal = Calendar.getInstance().apply { timeInMillis = dayTime }
                                                val dayNum = dayCal.get(Calendar.DAY_OF_MONTH)

                                                // Determine highlighting (actual period logs)
                                                val isPeriod = periodLogs.any { log ->
                                                    val end = log.endDate ?: (log.startDate + 5L * 24 * 60 * 60 * 1000)
                                                    dayTime >= log.startDate && dayTime <= end
                                                }

                                                // Smart predictions indicators
                                                val isPredictedPeriod = (isPregnant == null || isPregnant?.isPregnant != true) && !isPeriod && predictedPeriods.any { (start, end) ->
                                                    dayTime >= start && dayTime <= end
                                                }
                                                val isPredictedDevice = false
                                                val isPredictedOvulation = (isPregnant == null || isPregnant?.isPregnant != true) && !isPeriod && !isPredictedPeriod && predictedOvulations.any { (start, end) ->
                                                    dayTime >= start && dayTime <= end
                                                }

                                                val todayCal = Calendar.getInstance()
                                                val isCurrent = todayCal.get(Calendar.YEAR) == dayCal.get(Calendar.YEAR) &&
                                                                todayCal.get(Calendar.DAY_OF_YEAR) == dayCal.get(Calendar.DAY_OF_YEAR)

                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .aspectRatio(1f)
                                                        .clip(CircleShape)
                                                        .background(
                                                            when {
                                                                isPeriod -> SoftTheme.DeepPink
                                                                isPredictedPeriod -> SoftTheme.DeepPink.copy(alpha = 0.25f)
                                                                isPredictedOvulation -> SoftTheme.MintTeal.copy(alpha = 0.25f)
                                                                isCurrent -> SoftTheme.MintTeal
                                                                else -> Color.Transparent
                                                            }
                                                        )
                                                        .border(
                                                            width = if (isCurrent && !isPeriod) 2.dp else if (isPredictedPeriod || isPredictedOvulation) 1.dp else 0.dp,
                                                            color = if (isCurrent && !isPeriod) SoftTheme.MintTeal else if (isPredictedPeriod) SoftTheme.SoftPink else if (isPredictedOvulation) SoftTheme.MintTeal else Color.Transparent,
                                                            shape = CircleShape
                                                        )
                                                        .clickable {
                                                            // Day selected, smart autofill for previous cycle logs
                                                            useCustomStartDate = dayTime
                                                            useCustomEndDate = dayTime + 5L * 24 * 60 * 60 * 1000
                                                            useSpecificDateRange = true
                                                            showAddPeriodDialog = true
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "$dayNum",
                                                        color = if (isPeriod) SoftTheme.DeepSlate else if (isPredictedPeriod) SoftTheme.SoftPink else if (isPredictedOvulation) SoftTheme.MintTeal else SoftTheme.TextWhite,
                                                        fontWeight = if (isCurrent || isPeriod || isPredictedPeriod || isPredictedOvulation) FontWeight.Bold else FontWeight.Normal,
                                                        fontSize = 14.sp
                                                    )
                                                }
                                            }
                                        }
                                        // pad row if necessary
                                        if (rowDays.size < 7) {
                                            repeat(7 - rowDays.size) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Color Legend for the Smart Calendar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SoftTheme.DeepPink))
                                    Text("طمث", color = SoftTheme.SoftGray, fontSize = 9.sp)
                                }
                                if (isPregnant == null || isPregnant?.isPregnant != true) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SoftTheme.DeepPink.copy(alpha = 0.25f)))
                                        Text("دورة متوقعة", color = SoftTheme.SoftGray, fontSize = 9.sp)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SoftTheme.MintTeal.copy(alpha = 0.25f)))
                                        Text("إباضة متوقعة", color = SoftTheme.SoftGray, fontSize = 9.sp)
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SoftTheme.MintTeal))
                                    Text("اليوم", color = SoftTheme.SoftGray, fontSize = 9.sp)
                                }
                            }
                        }
                    }
                }
            }

                // History List Header
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isHistoryExpanded = !isHistoryExpanded },
                        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.15f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("📖", fontSize = 24.sp)
                                Column {
                                    Text(
                                        text = "السجل التاريخي للدورات",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SoftTheme.TextWhite
                                    )
                                    Text(
                                        text = "عرض وتعديل الدورات والتقلصات السابقة",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SoftTheme.SoftGray
                                    )
                                }
                            }
                            Text(
                                text = if (isHistoryExpanded) "إخفاء 🔼" else "عرض السجلات 🔽",
                                color = SoftTheme.SoftPink,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (isHistoryExpanded) {
                    if (periodLogs.isEmpty()) {
                        item {
                            Text(
                                text = "لا توجد دورات مسجلة بعد. قومي بإضافة دورتك لبدء الحساب التلقائي والتحليل.",
                                color = SoftTheme.SoftGray,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                            )
                        }
                    } else {
                        items(periodLogs) { log ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate.copy(alpha = 0.7f)),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(0.5.dp, SoftTheme.SoftPink.copy(alpha = 0.1f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "البداية: ${formatGregorianDate(log.startDate)}",
                                                fontWeight = FontWeight.Bold,
                                                color = SoftTheme.TextWhite
                                            )
                                            log.endDate?.let {
                                                Text(
                                                    text = "النهاية: ${formatGregorianDate(it)}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = SoftTheme.SoftGray
                                                )
                                            }
                                        }

                                        IconButton(onClick = { viewModel.deletePeriod(log) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = SoftTheme.RedDanger)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(SoftTheme.DeepSlate)
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "الشدة: ${if (log.flowIntensity == "heavy") "غزيرة" else if (log.flowIntensity == "medium") "متوسطة" else "خفيفة"}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = SoftTheme.SoftPink
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(SoftTheme.DeepSlate)
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "الألم: ${log.painLevel}/10",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = SoftTheme.MintTeal
                                            )
                                        }
                                        if (!log.notes.isNullOrBlank()) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(SoftTheme.DeepSlate)
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = "ملاحظات 📝",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = SoftTheme.LightPink
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

    // Add Period Dialog (Upgraded with past cycle & custom date range support)
    if (showAddPeriodDialog) {
        Dialog(onDismissRequest = { showAddPeriodDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "تسجيل الدورة الشهرية 🩸",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.SoftPink
                    )

                    val context = LocalContext.current
                    var selectStartToday by remember { mutableStateOf(true) }

                    // Date Type Selectors (Now supporting highly custom past/previous ranges)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                useSpecificDateRange = false
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!useSpecificDateRange) SoftTheme.SoftPink else SoftTheme.DeepSlate,
                                contentColor = if (!useSpecificDateRange) SoftTheme.DeepSlate else SoftTheme.TextWhite
                            )
                        ) {
                            Text("اليوم/أمس", fontSize = 11.sp)
                        }
                        Button(
                            onClick = {
                                useSpecificDateRange = true
                            },
                            modifier = Modifier.weight(1.5f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (useSpecificDateRange) SoftTheme.SoftPink else SoftTheme.DeepSlate,
                                contentColor = if (useSpecificDateRange) SoftTheme.DeepSlate else SoftTheme.TextWhite
                            )
                        ) {
                            Text("تاريخ مخصص / سابق 📅", fontSize = 11.sp)
                        }
                    }

                    if (useSpecificDateRange) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("تاريخ البدء:", color = SoftTheme.TextWhite, style = MaterialTheme.typography.bodySmall)
                            Button(
                                onClick = {
                                    val calendar = Calendar.getInstance().apply { timeInMillis = useCustomStartDate }
                                    android.app.DatePickerDialog(
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
                                            useCustomStartDate = cal.timeInMillis
                                            // Automatically pre-fill expected end date (5 days later)
                                            useCustomEndDate = cal.timeInMillis + 5L * 24 * 60 * 60 * 1000
                                        },
                                        calendar.get(Calendar.YEAR),
                                        calendar.get(Calendar.MONTH),
                                        calendar.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.DeepSlate)
                            ) {
                                Text(formatGregorianDate(useCustomStartDate), color = SoftTheme.TextWhite)
                            }

                            Text("تاريخ الانتهاء:", color = SoftTheme.TextWhite, style = MaterialTheme.typography.bodySmall)
                            Button(
                                onClick = {
                                    val calendar = Calendar.getInstance().apply { timeInMillis = useCustomEndDate }
                                    android.app.DatePickerDialog(
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
                                            useCustomEndDate = cal.timeInMillis
                                        },
                                        calendar.get(Calendar.YEAR),
                                        calendar.get(Calendar.MONTH),
                                        calendar.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.DeepSlate)
                            ) {
                                Text(formatGregorianDate(useCustomEndDate), color = SoftTheme.TextWhite)
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { selectStartToday = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectStartToday) SoftTheme.SoftPink else SoftTheme.DeepSlate,
                                    contentColor = if (selectStartToday) SoftTheme.DeepSlate else SoftTheme.TextWhite
                                )
                            ) {
                                Text("اليوم")
                            }
                            Button(
                                onClick = { selectStartToday = false },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (!selectStartToday) SoftTheme.SoftPink else SoftTheme.DeepSlate,
                                    contentColor = if (!selectStartToday) SoftTheme.DeepSlate else SoftTheme.TextWhite
                                )
                            ) {
                                Text("أمس")
                            }
                        }
                    }

                    Text("غزارة الطمث:", color = SoftTheme.TextWhite)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val intensities = listOf("light" to "خفيفة", "medium" to "متوسطة", "heavy" to "غزيرة")
                        intensities.forEach { (key, label) ->
                            val isSelected = selectedIntensity == key
                            Button(
                                onClick = { selectedIntensity = key },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) SoftTheme.SoftPink else SoftTheme.DeepSlate,
                                    contentColor = if (isSelected) SoftTheme.DeepSlate else SoftTheme.TextWhite
                                )
                            ) {
                                Text(label, fontSize = 12.sp)
                            }
                        }
                    }

                    Text("مستوى الألم: $painLevel/10", color = SoftTheme.TextWhite)
                    Slider(
                        value = painLevel.toFloat(),
                        onValueChange = { painLevel = it.toInt() },
                        valueRange = 1f..10f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = SoftTheme.SoftPink,
                            activeTrackColor = SoftTheme.SoftPink,
                            inactiveTrackColor = SoftTheme.DeepSlate
                        )
                    )

                    Text("الأعراض المرافقة:", color = SoftTheme.TextWhite)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        symptomsList.forEach { sym ->
                            val isChecked = selectedSymptoms.contains(sym)
                            FilterChip(
                                selected = isChecked,
                                onClick = {
                                    if (isChecked) selectedSymptoms.remove(sym)
                                    else selectedSymptoms.add(sym)
                                },
                                label = { Text(sym) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SoftTheme.SoftPink,
                                    selectedLabelColor = SoftTheme.DeepSlate
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        label = { Text("ملاحظات خاصة...") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoftTheme.SoftPink,
                            unfocusedBorderColor = SoftTheme.SoftGray,
                            focusedTextColor = SoftTheme.TextWhite,
                            unfocusedTextColor = SoftTheme.TextWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(
                            onClick = { showAddPeriodDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء", color = SoftTheme.SoftGray)
                        }

                        Button(
                            onClick = {
                                val start = if (useSpecificDateRange) {
                                    useCustomStartDate
                                } else {
                                    if (selectStartToday) {
                                        System.currentTimeMillis()
                                    } else {
                                        System.currentTimeMillis() - 24 * 60 * 60 * 1000
                                    }
                                }
                                val end = if (useSpecificDateRange) {
                                    useCustomEndDate
                                } else {
                                    start + 5L * 24 * 60 * 60 * 1000
                                }
                                viewModel.addPeriodLog(
                                    startDate = start,
                                    endDate = end,
                                    intensity = selectedIntensity,
                                    symptoms = selectedSymptoms.toList(),
                                    painLevel = painLevel,
                                    notes = notesInput
                                )
                                if (isPregnant != null && isPregnant?.isPregnant == true) {
                                    pendingPeriodStartDate = start
                                    showPregnancyLmpPromptDialog = true
                                }
                                showAddPeriodDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("حفظ")
                        }
                    }
                }
            }
        }
    }

    // --- حوار تأكيد تاريخ دورة الحمل المكتشفة تلقائياً ---
    if (showPregnancyLmpPromptDialog && pendingPeriodStartDate != null) {
        val dateStr = formatGregorianDate(pendingPeriodStartDate!!)
        Dialog(onDismissRequest = { showPregnancyLmpPromptDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "تحديث حسابات الحمل 🌸🤰",
                        style = MaterialTheme.typography.titleLarge,
                        color = SoftTheme.TextWhite,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "صديقتي الغالية، نلاحظ أنكِ سجلتِ حالة حمل نشطة في التطبيق.\n\nهل الدورة التي سجلتِها الآن (والتي بدأت بتاريخ $dateStr) هي الدورة الشهرية الأخيرة التي حصل بعدها الحمل مباشرة؟\n\nإذا كانت الإجابة نعم، فسيقوم رفيقكِ الذكي بتعديل تاريخ الحمل وتاريخ الولادة المتوقع تلقائياً بناءً عليها لتكون جميع الإرشادات والمعلومات الطبية دقيقة تماماً 💖",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoftTheme.SoftGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                isPregnant?.let { preg ->
                                    viewModel.setPregnancy(pendingPeriodStartDate, preg.prePregnancyWeight, preg.heightCm)
                                }
                                showPregnancyLmpPromptDialog = false
                                pendingPeriodStartDate = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("نعم، دورة الحمل 👶", color = SoftTheme.TextWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                showPregnancyLmpPromptDialog = false
                                pendingPeriodStartDate = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.DeepSlate),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("لا، تسجيل عادي 📝", color = SoftTheme.SoftGray, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }

    // +++ حوار تعديل جنس واسم الجنين داخل صفحة الحمل +++
    if (showBabyInfoDialog) {
        Dialog(onDismissRequest = { showBabyInfoDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "تسجيل جنس واسم الجنين 👶🍼",
                        style = MaterialTheme.typography.titleLarge,
                        color = SoftTheme.TextWhite,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "شاركينا جنس واسم جنينكِ لنخصص التوجيهات باسمه العذب وندخل البهجة على رحلتكما 💖",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTheme.SoftGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("ولد", "بنت", "مفاجأة").forEach { gender ->
                            val isSelected = babyGenderInput == gender
                            Button(
                                onClick = { babyGenderInput = gender },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) SoftTheme.SoftPink else SoftTheme.DeepSlate
                                ),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = when (gender) {
                                        "ولد" -> "ولد 💙"
                                        "بنت" -> "بنت 💗"
                                        else -> "مفاجأة 🤫"
                                    },
                                    color = if (isSelected) Color.White else SoftTheme.SoftGray,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = babyNameInput,
                        onValueChange = { babyNameInput = it },
                        label = { Text("الاسم المقترح لجنينكِ العذب:") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoftTheme.SoftPink,
                            unfocusedBorderColor = SoftTheme.SoftGray,
                            focusedTextColor = SoftTheme.TextWhite,
                            unfocusedTextColor = SoftTheme.TextWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(
                            onClick = { showBabyInfoDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء", color = SoftTheme.SoftGray)
                        }

                        Button(
                            onClick = {
                                viewModel.updateBabyInfo(babyGenderInput.ifEmpty { null }, babyNameInput.ifEmpty { null })
                                showBabyInfoDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("حفظ")
                        }
                    }
                }
            }
        }
    }

    // +++ حوار مباركة الولادة وتحديد طريقتها داخل صفحة الحمل +++
    if (showDeliveryDialog) {
        Dialog(onDismissRequest = { showDeliveryDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "مبارك مبارك يا غالية! 🥳💖👶",
                        style = MaterialTheme.typography.titleLarge,
                        color = SoftTheme.TextWhite,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "ألف الحمد لله على سلامتكِ وسلامة مولودكِ الحبيب، جعله الله ذريّة صالحة بارّة قرّة لعينيكِ.\n\nكيف كانت ولادتكِ الميمونة لكي يقدم لكِ رفيقكِ جوري أهم إرشادات التعافي والنفاس المخصصة لكِ؟",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTheme.SoftGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                viewModel.updateDeliveryInfo(isDelivered = true, birthMethod = "طبيعي")
                                showDeliveryDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("ولادة طبيعية 🌸", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.updateDeliveryInfo(isDelivered = true, birthMethod = "قيصري")
                                showDeliveryDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.DeepSlate),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("ولادة قيصرية 🏥", color = SoftTheme.SoftPink, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }

    // +++ حوار تأكيد إنهاء الحمل ومعرفة السبب +++
    if (showEndPregnancyConfirmDialog) {
        Dialog(onDismissRequest = { showEndPregnancyConfirmDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "تأكيد إنهاء الحمل الحالي 🤰💔",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.TextWhite,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "هل أنتِ متأكدة من رغبتكِ في إنهاء تتبع الحمل الحالي والعودة إلى تتبع الدورة الشهرية والخصوبة؟\n\nيرجى تحديد سبب إنهاء الحمل لنتمكن من توجيهكِ وتقديم الدعم المناسب لكِ:",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTheme.SoftGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Button(
                        onClick = {
                            showEndPregnancyConfirmDialog = false
                            showDeliveryDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("الحمد لله، تمّت الولادة بسلام 🎉👶", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            showEndPregnancyConfirmDialog = false
                            showLossSupportDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.DeepSlate),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("حدثت مشكلة أو فقدان للحمل لا قدر الله 🤍", color = SoftTheme.SoftPink, fontWeight = FontWeight.Bold)
                    }

                    TextButton(
                        onClick = { showEndPregnancyConfirmDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("تراجع وإلغاء 🌸", color = SoftTheme.SoftGray)
                    }
                }
            }
        }
    }

    // +++ حوار المواساة والدعم في حالة الفقدان +++
    if (showLossSupportDialog) {
        Dialog(onDismissRequest = { showLossSupportDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "عوضكِ الله خيراً يا حبيبتي 🤍",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.SoftPink,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "﴿وَبَشِّرِ الصَّابِرِينَ﴾\n\nسلامة قلبكِ وجسدكِ يا غالية. لا تحزني ولا تفقدي الأمل، فالله لطيف خبير ورحيم، وعوضه جميل دائماً.\n\nنحن هنا بجانبكِ دوماً لتقديم كل الحب والدعم. سنقوم الآن بإعادة ضبط التطبيق لتتبع الدورة الشهرية والراحة لمساعدتكِ على التعافي الهادئ خطوة بخطوة.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTheme.TextWhite,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Button(
                        onClick = {
                            showLossSupportDialog = false
                            viewModel.switchToPeriodTracking()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("الحمد لله على كل حال (العودة للدورة)", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
}

// --- Nutrition & Water Tracker Screen ---
@Composable
fun NutritionAndWaterScreen(
    viewModel: WomanCompanionViewModel
) {
    val waterLog by viewModel.todayWaterLogState.collectAsStateWithLifecycle()
    val nutritionLogs by viewModel.todayNutritionLogsState.collectAsStateWithLifecycle()

    val targetWater = viewModel.getWaterTarget()
    val calorieGoal = viewModel.getCalorieTarget()

    var showAddMealDialog by remember { mutableStateOf(false) }
    var mealTypeInput by remember { mutableStateOf("breakfast") }
    var mealDescInput by remember { mutableStateOf("") }
    var calorieInput by remember { mutableStateOf("") }

    var ironInput by remember { mutableStateOf("") }
    var folateInput by remember { mutableStateOf("") }
    var calciumInput by remember { mutableStateOf("") }
    var omega3Input by remember { mutableStateOf("") }

    var proteinInput by remember { mutableStateOf("") }
    var carbsInput by remember { mutableStateOf("") }
    var fatInput by remember { mutableStateOf("") }
    var sugarInput by remember { mutableStateOf("") }
    var fiberInput by remember { mutableStateOf("") }
    var waterBenefitInput by remember { mutableStateOf("") }

    val currentWaterAmount = waterLog?.amountMl ?: 0

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                Text(
                    text = "الغذاء والماء اليومي 🥗🥤",
                    style = MaterialTheme.typography.titleLarge,
                    color = SoftTheme.SoftPink,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "موازنة السعرات والعناصر الأساسية لصحتكِ وجنينكِ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SoftTheme.SoftGray
                )
            }

            // Water tracker card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "💧 تتبع شرب المياه",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.TextWhite
                        )

                        // Glass graphic with progress
                        val fraction = (currentWaterAmount.toFloat() / targetWater.toFloat()).coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .size(130.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(SoftTheme.DeepSlate)
                                .drawBehind {
                                    // draw water fill
                                    val height = size.height * fraction
                                    drawRect(
                                        color = SoftTheme.SoftTeal.copy(alpha = 0.6f),
                                        topLeft = Offset(0f, size.height - height),
                                        size = Size(size.width, height)
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$currentWaterAmount / $targetWater مل",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = SoftTheme.TextWhite,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${(fraction * 100).toInt()}% مكتمل",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SoftTheme.SoftGray
                                )
                            }
                        }

                        // Buttons for quick add
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { viewModel.addWater(250) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("add_water_250"),
                                colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftTeal)
                            ) {
                                Text("+٢٥٠ مل 🥛", fontSize = 12.sp)
                            }
                            Button(
                                onClick = { viewModel.addWater(500) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("add_water_500"),
                                colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.MintTeal)
                            ) {
                                Text("+٥٠٠ مل 🍶", fontSize = 12.sp)
                            }
                            IconButton(
                                onClick = { viewModel.resetTodayWater() },
                                modifier = Modifier.background(SoftTheme.DeepSlate, CircleShape)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "إعادة تعيين", tint = SoftTheme.RedDanger)
                            }
                        }
                    }
                }
            }

            // Calorie & Nutrient Guidance Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "🎯 هدف السعرات الحرارية اليومي",
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.TextWhite,
                            style = MaterialTheme.typography.titleMedium
                        )

                        val totalCaloriesConsumed = nutritionLogs.sumOf { it.calories }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("المستهلك اليوم", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                                Text("$totalCaloriesConsumed سعرة حرارية", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SoftTheme.MintTeal)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("الهدف المطلوب", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                                Text("${calorieGoal.target} سعرة", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SoftTheme.SoftPink)
                            }
                        }

                        Text(
                            text = calorieGoal.details,
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTheme.SoftGray,
                            lineHeight = 16.sp
                        )

                        Text(
                            text = "* يُرجى مراجعة الدكتورة المشرفة أو أخصائية تغذية قبل إدخال تغييرات جذرية على طعامكِ.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTheme.RedDanger,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Detailed Nutrient Balance and Remaining Requirements Card
            item {
                val nutrientTargets = viewModel.getNutrientTargets()
                val totalProtein = nutritionLogs.sumOf { it.proteinG }
                val totalCarbs = nutritionLogs.sumOf { it.carbsG }
                val totalFat = nutritionLogs.sumOf { it.fatG }
                val totalSugar = nutritionLogs.sumOf { it.sugarG }
                val totalFiber = nutritionLogs.sumOf { it.fiberG }
                val totalIron = nutritionLogs.sumOf { it.ironMg }
                val totalCalcium = nutritionLogs.sumOf { it.calciumMg }
                val totalFolate = nutritionLogs.sumOf { it.folateMcg }
                val totalPotassium = nutritionLogs.sumOf { it.potassiumMg }
                val totalSodium = nutritionLogs.sumOf { it.sodiumMg }
                val totalMagnesium = nutritionLogs.sumOf { it.magnesiumMg }
                val totalVitaminC = nutritionLogs.sumOf { it.vitaminC_Mg }
                val totalVitaminA = nutritionLogs.sumOf { it.vitaminA_Mcg }

                var showAllNutrients by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.15f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("📈", fontSize = 20.sp)
                                Column {
                                    Text(
                                        text = "ميزان العناصر والمتبقي اليومي",
                                        fontWeight = FontWeight.Bold,
                                        color = SoftTheme.TextWhite,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "متابعة دقيقة للمغذيات الكبرى والصغرى",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SoftTheme.SoftGray
                                    )
                                }
                            }
                            TextButton(onClick = { showAllNutrients = !showAllNutrients }) {
                                Text(
                                    text = if (showAllNutrients) "عرض أقل" else "عرض الكل 🔍",
                                    color = SoftTheme.SoftPink,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Macros section
                        Text("الماكروز والمغذيات الكبرى:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = SoftTheme.SoftPink)

                        val macrosList = listOf(
                            Triple("البروتين (لبناء الأنسجة)", totalProtein, nutrientTargets["protein"]),
                            Triple("النشويات (مصدر الطاقة)", totalCarbs, nutrientTargets["carbs"]),
                            Triple("الدهون (الامتصاص والذكاء)", totalFat, nutrientTargets["fat"]),
                            Triple("السكريات المستهلكة", totalSugar, nutrientTargets["sugar"]),
                            Triple("الألياف (لصحة الهضم)", totalFiber, nutrientTargets["fiber"])
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            macrosList.forEach { (label, consumed, targetInfo) ->
                                if (targetInfo != null) {
                                    val pct = if (targetInfo.targetVal > 0) (consumed / targetInfo.targetVal).coerceIn(0.0, 1.0).toFloat() else 0f
                                    val remaining = targetInfo.targetVal - consumed
                                    val pctText = "${(pct * 100).toInt()}%"

                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(label, style = MaterialTheme.typography.bodySmall, color = SoftTheme.TextWhite)
                                            Text(
                                                text = "%.1f / %.0f %s (%s)".format(consumed, targetInfo.targetVal, targetInfo.unit, pctText),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (targetInfo.isLimit && remaining < 0) SoftTheme.RedDanger else SoftTheme.MintTeal,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        
                                        // Progress bar
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(SoftTheme.DeepSlate)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxHeight()
                                                    .fillMaxWidth(pct)
                                                    .background(
                                                        if (targetInfo.isLimit && remaining < 0) SoftTheme.RedDanger else SoftTheme.SoftTeal
                                                    )
                                            )
                                        }

                                        // Remaining description text
                                        val remainingText = when {
                                            targetInfo.isLimit -> {
                                                if (remaining >= 0) "متبقي للاستهلاك الآمن: %.1f %s".format(remaining, targetInfo.unit)
                                                else "⚠️ تخطيتِ الحد الآمن بـ %.1f %s!".format(-remaining, targetInfo.unit)
                                            }
                                            else -> {
                                                if (remaining > 0) "المتبقي لتحقيق الهدف: %.1f %s".format(remaining, targetInfo.unit)
                                                else "🎉 تم تلبية احتياجكِ اليومي بالكامل!"
                                            }
                                        }
                                        Text(remainingText, style = MaterialTheme.typography.labelSmall, color = if (remaining < 0 && targetInfo.isLimit) SoftTheme.RedDanger else SoftTheme.SoftGray)
                                    }
                                }
                            }
                        }

                        // Micros section
                        AnimatedVisibility(visible = showAllNutrients) {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Divider(color = SoftTheme.DeepSlate, thickness = 1.dp)
                                Text("العناصر الدقيقة والفيتامينات والمعادن:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = SoftTheme.MintTeal)

                                val microsList = listOf(
                                    Triple("الحديد (لمنع الأنيميا)", totalIron, nutrientTargets["iron"]),
                                    Triple("الكالسيوم (لالعظام والأسنان)", totalCalcium, nutrientTargets["calcium"]),
                                    Triple("حمض الفوليك (للنمو العصبي)", totalFolate, nutrientTargets["folate"]),
                                    Triple("البوتاسيوم (لتوازن الضغط)", totalPotassium, nutrientTargets["potassium"]),
                                    Triple("الماغنسيوم (لتسكين التقلصات)", totalMagnesium, nutrientTargets["magnesium"]),
                                    Triple("فيتامين سي (للإمتصاص والمناعة)", totalVitaminC, nutrientTargets["vitaminC"]),
                                    Triple("فيتامين أ (لنمو الخلايا والنظر)", totalVitaminA, nutrientTargets["vitaminA"])
                                )

                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    microsList.forEach { (label, consumed, targetInfo) ->
                                        if (targetInfo != null) {
                                            val pct = if (targetInfo.targetVal > 0) (consumed / targetInfo.targetVal).coerceIn(0.0, 1.0).toFloat() else 0f
                                            val remaining = targetInfo.targetVal - consumed
                                            val pctText = "${(pct * 100).toInt()}%"

                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(label, style = MaterialTheme.typography.bodySmall, color = SoftTheme.TextWhite)
                                                    Text(
                                                        text = "%.1f / %.0f %s (%s)".format(consumed, targetInfo.targetVal, targetInfo.unit, pctText),
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = SoftTheme.MintTeal,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(6.dp)
                                                        .clip(RoundedCornerShape(3.dp))
                                                        .background(SoftTheme.DeepSlate)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxHeight()
                                                            .fillMaxWidth(pct)
                                                            .background(SoftTheme.MintTeal)
                                                    )
                                                }

                                                val remainingText = if (remaining > 0) "المتبقي لتحقيق الهدف: %.1f %s".format(remaining, targetInfo.unit) else "🎉 تم تلبية الاحتياج اليومي!"
                                                Text(remainingText, style = MaterialTheme.typography.labelSmall, color = SoftTheme.SoftGray)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                LiveNutrientSimulatorWidget(viewModel = viewModel)
            }

            item {
                SmartNutritionAdvisorCard(viewModel = viewModel)
            }

            // Today Meals section header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "وجبات اليوم 🥘",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.TextWhite
                    )

                    Button(
                        onClick = { showAddMealDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("add_meal_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إضافة وجبة", fontSize = 12.sp)
                    }
                }
            }

            if (nutritionLogs.isEmpty()) {
                item {
                    Text(
                        text = "لم يتم تسجيل أي وجبات اليوم بعد.",
                        color = SoftTheme.SoftGray,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                    )
                }
            } else {
                items(nutritionLogs) { meal ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                val typeLabel = when(meal.mealType) {
                                    "breakfast" -> "إفطار"
                                    "lunch" -> "غداء"
                                    "dinner" -> "عشاء"
                                    else -> "سناك / خفيف"
                                }
                                Text(
                                    text = "$typeLabel — ${meal.description}",
                                    fontWeight = FontWeight.Bold,
                                    color = SoftTheme.TextWhite
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("سعرات: ${meal.calories}", color = SoftTheme.SoftPink, fontSize = 12.sp)
                                    if (meal.ironMg > 0) Text("حديد: ${meal.ironMg} ملجم", color = SoftTheme.MintTeal, fontSize = 12.sp)
                                    if (meal.folateMcg > 0) Text("فوليك: ${meal.folateMcg} ميكروجم", color = SoftTheme.SoftTeal, fontSize = 12.sp)
                                }
                            }
                            IconButton(onClick = { viewModel.deleteNutritionMeal(meal) }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = SoftTheme.RedDanger)
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Meal Dialog
    if (showAddMealDialog) {
        var activeDialogTab by remember { mutableStateOf("smart") } // Default to "smart" to showcase the selector
        var activeCategory by remember { mutableStateOf<String?>(null) }
        var activeSubcategory by remember { mutableStateOf<String?>(null) }
        var quantityValue by remember { mutableStateOf(1) }
        val selectedAdditions = remember { mutableStateMapOf<String, Boolean>() }

        Dialog(
            onDismissRequest = { showAddMealDialog = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.85f)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "إضافة وجبة طعام 🥗",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.SoftPink
                    )

                    // Tab selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SoftTheme.DeepSlate)
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Button(
                            onClick = { activeDialogTab = "smart" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (activeDialogTab == "smart") SoftTheme.SoftPink else Color.Transparent,
                                contentColor = if (activeDialogTab == "smart") SoftTheme.DeepSlate else SoftTheme.TextWhite
                            ),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Text("منتقي الأغذية الذكي 🥚", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { activeDialogTab = "manual" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (activeDialogTab == "manual") SoftTheme.SoftPink else Color.Transparent,
                                contentColor = if (activeDialogTab == "manual") SoftTheme.DeepSlate else SoftTheme.TextWhite
                            ),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Text("إدخال يدوي ✍️", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Meal Type row (Common for both)
                    Text("تصنيف وقت الوجبة:", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val types = listOf("breakfast" to "إفطار", "lunch" to "غداء", "dinner" to "عشاء", "snack" to "خفيف")
                        types.forEach { (key, label) ->
                            val isSel = mealTypeInput == key
                            Button(
                                onClick = { mealTypeInput = key },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSel) SoftTheme.SoftPink else SoftTheme.DeepSlate,
                                    contentColor = if (isSel) SoftTheme.DeepSlate else SoftTheme.TextWhite
                                )
                            ) {
                                Text(label, fontSize = 10.sp)
                            }
                        }
                    }

                    if (activeDialogTab == "manual") {
                        // Original Manual input layout
                        OutlinedTextField(
                            value = mealDescInput,
                            onValueChange = { mealDescInput = it },
                            label = { Text("تفاصيل الوجبة (مثال: طبق سلطة وبيضة مسلوقة)") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SoftTheme.SoftPink,
                                unfocusedBorderColor = SoftTheme.SoftGray,
                                focusedTextColor = SoftTheme.TextWhite,
                                unfocusedTextColor = SoftTheme.TextWhite
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = calorieInput,
                            onValueChange = { calorieInput = it },
                            label = { Text("السعرات المقدرة (سعرة)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SoftTheme.SoftPink,
                                unfocusedBorderColor = SoftTheme.SoftGray,
                                focusedTextColor = SoftTheme.TextWhite,
                                unfocusedTextColor = SoftTheme.TextWhite
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("العناصر الدقيقة الاختيارية:", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = ironInput,
                                onValueChange = { ironInput = it },
                                label = { Text("حديد ملجم") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = folateInput,
                                onValueChange = { folateInput = it },
                                label = { Text("فوليك مكجم") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = calciumInput,
                                onValueChange = { calciumInput = it },
                                label = { Text("كالسيوم ملجم") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = omega3Input,
                                onValueChange = { omega3Input = it },
                                label = { Text("أوميجا3 جم") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Text("العناصر الكبرى والماكروز الاختيارية:", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = proteinInput,
                                onValueChange = { proteinInput = it },
                                label = { Text("بروتين جم") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = carbsInput,
                                onValueChange = { carbsInput = it },
                                label = { Text("نشويات جم") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = fatInput,
                                onValueChange = { fatInput = it },
                                label = { Text("دهون جم") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = sugarInput,
                                onValueChange = { sugarInput = it },
                                label = { Text("سكريات جم") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = fiberInput,
                                onValueChange = { fiberInput = it },
                                label = { Text("ألياف جم") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = waterBenefitInput,
                                onValueChange = { waterBenefitInput = it },
                                label = { Text("مياه مستفادة مل") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(
                                onClick = { showAddMealDialog = false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("إلغاء", color = SoftTheme.SoftGray)
                            }

                            Button(
                                onClick = {
                                    viewModel.addNutritionMeal(
                                        mealType = mealTypeInput,
                                        description = mealDescInput,
                                        calories = calorieInput.toIntOrNull() ?: 0,
                                        iron = ironInput.toDoubleOrNull() ?: 0.0,
                                        folate = folateInput.toDoubleOrNull() ?: 0.0,
                                        calcium = calciumInput.toDoubleOrNull() ?: 0.0,
                                        omega3 = omega3Input.toDoubleOrNull() ?: 0.0,
                                        protein = proteinInput.toDoubleOrNull() ?: 0.0,
                                        carbs = carbsInput.toDoubleOrNull() ?: 0.0,
                                        fat = fatInput.toDoubleOrNull() ?: 0.0,
                                        sugar = sugarInput.toDoubleOrNull() ?: 0.0,
                                        fiber = fiberInput.toDoubleOrNull() ?: 0.0,
                                        waterBenefit = waterBenefitInput.toIntOrNull() ?: 0
                                    )
                                    showAddMealDialog = false
                                    mealDescInput = ""
                                    calorieInput = ""
                                    ironInput = ""
                                    folateInput = ""
                                    calciumInput = ""
                                    omega3Input = ""
                                    proteinInput = ""
                                    carbsInput = ""
                                    fatInput = ""
                                    sugarInput = ""
                                    fiberInput = ""
                                    waterBenefitInput = ""
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("حفظ")
                            }
                        }
                    } else {
                        // Smart Food Selector layout
                        Text("1. اختاري التصنيف الرئيسي: 📁", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftPink, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            hierarchicalDatabase.keys.forEach { category ->
                                val isSel = activeCategory == category
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSel) SoftTheme.SoftPink else SoftTheme.DeepSlate)
                                        .border(1.dp, if (isSel) SoftTheme.SoftPink else SoftTheme.SoftGray.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                        .clickable {
                                            activeCategory = category
                                            activeSubcategory = null
                                            quantityValue = 1
                                            selectedAdditions.clear()
                                        }
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Text(category, color = if (isSel) SoftTheme.DeepSlate else SoftTheme.TextWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }

                        if (activeCategory != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("2. اختاري الصنف الفرعي: 🍽️", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftPink, fontWeight = FontWeight.Bold)
                            val subcategories = hierarchicalDatabase[activeCategory] ?: emptyList()
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                subcategories.forEach { subInfo ->
                                    val isSel = activeSubcategory == subInfo.name
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(if (isSel) SoftTheme.SoftTeal.copy(alpha = 0.15f) else SoftTheme.DeepSlate)
                                            .border(1.dp, if (isSel) SoftTheme.SoftTeal else Color.Transparent, RoundedCornerShape(14.dp))
                                            .clickable {
                                                activeSubcategory = subInfo.name
                                                selectedAdditions.clear()
                                                subInfo.defaultAdditions.forEach { add ->
                                                    selectedAdditions[add] = false
                                                }
                                            }
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(subInfo.name, color = SoftTheme.TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text("الحصة: ${subInfo.caloriesPerUnit} سعرة حرارية", color = SoftTheme.SoftGray, fontSize = 10.sp)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    when (subInfo.safety) {
                                                        SafetyLevel.SAFE -> SoftTheme.MintTeal.copy(alpha = 0.15f)
                                                        SafetyLevel.CAUTION -> Color(0xFFFFF3CD)
                                                        SafetyLevel.AVOID -> Color(0xFFF8D7DA)
                                                    }
                                                )
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = when (subInfo.safety) {
                                                    SafetyLevel.SAFE -> "آمن ✓"
                                                    SafetyLevel.CAUTION -> "انتباه ⚠️"
                                                    SafetyLevel.AVOID -> "تجنبي 🚫"
                                                },
                                                color = when (subInfo.safety) {
                                                    SafetyLevel.SAFE -> SoftTheme.MintTeal
                                                    SafetyLevel.CAUTION -> Color(0xFF856404)
                                                    SafetyLevel.AVOID -> Color(0xFF721C24)
                                                },
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (activeSubcategory != null) {
                            val selectedSubInfo = hierarchicalDatabase[activeCategory]?.firstOrNull { it.name == activeSubcategory }
                            if (selectedSubInfo != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("3. تحديد الكمية (عدد الحصص): 🔢", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftPink, fontWeight = FontWeight.Bold)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    IconButton(
                                        onClick = { if (quantityValue > 1) quantityValue-- },
                                        modifier = Modifier.background(SoftTheme.DeepSlate, CircleShape)
                                    ) {
                                        Text("-", color = SoftTheme.TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Text("$quantityValue حصة", color = SoftTheme.TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    IconButton(
                                        onClick = { if (quantityValue < 10) quantityValue++ },
                                        modifier = Modifier.background(SoftTheme.DeepSlate, CircleShape)
                                    ) {
                                        Text("+", color = SoftTheme.TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (selectedSubInfo.defaultAdditions.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val questionText = when {
                                        activeCategory?.contains("مشروبات") == true -> "هل ترغبين بإضافة محلي أو نكهة؟ 🍯☕"
                                        activeCategory?.contains("جبن") == true -> "هل ترغبين بإضافة توابل أو زيت؟ 🧂🌿"
                                        activeCategory?.contains("شوربات") == true -> "هل ترغبين بإضافة ليمون أو فلفل؟ 🥣🍋"
                                        else -> "إضافات مخصصة اختيارية: ⚙️"
                                    }
                                    Text(
                                        text = "4. $questionText",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SoftTheme.SoftPink,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        selectedSubInfo.defaultAdditions.forEach { addition ->
                                            val isChecked = selectedAdditions[addition] ?: false
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(SoftTheme.DeepSlate)
                                                    .clickable { selectedAdditions[addition] = !isChecked }
                                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    val icon = when (addition) {
                                                        "سكر" -> "🍬"
                                                        "عسل نحل" -> "🍯"
                                                        "محلى صناعي" -> "💊"
                                                        "ملح" -> "🧂"
                                                        "فلفل أسود" -> "🌶️"
                                                        "كمون" -> "🌿"
                                                        "زعتر" -> "🍃"
                                                        "زيت زيتون" -> "🫒"
                                                        "عصير ليمون" -> "🍋"
                                                        "بقدونس" -> "🌱"
                                                        "حليب" -> "🥛"
                                                        else -> "✨"
                                                    }
                                                    Text(icon, fontSize = 14.sp)
                                                    Text(addition, color = SoftTheme.TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                                }
                                                Checkbox(
                                                    checked = isChecked,
                                                    onCheckedChange = { selectedAdditions[addition] = it },
                                                    colors = CheckboxDefaults.colors(checkedColor = SoftTheme.SoftPink)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                // Inline safety advice card
                                if (selectedSubInfo.safety == SafetyLevel.CAUTION) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text("💡", fontSize = 16.sp)
                                            Text(
                                                text = selectedSubInfo.safetyAdvice ?: "يرجى الاعتدال في تناول هذا الصنف.",
                                                color = Color(0xFF856404),
                                                fontSize = 11.sp,
                                                lineHeight = 16.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                } else if (selectedSubInfo.safety == SafetyLevel.AVOID) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8D7DA)),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text("🚫", fontSize = 16.sp)
                                            Text(
                                                text = selectedSubInfo.safetyAdvice ?: "يفضل تجنب هذا الصنف في فترات الحمل والرضاعة حرصاً على سلامتكِ.",
                                                color = Color(0xFF721C24),
                                                fontSize = 11.sp,
                                                lineHeight = 16.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                // Live metrics
                                val finalDesc = buildString {
                                    append(selectedSubInfo.name)
                                    append(" ($quantityValue حصة)")
                                    val activeAdds = selectedAdditions.filter { it.value }.keys
                                    if (activeAdds.isNotEmpty()) {
                                        append(" مع: ")
                                        append(activeAdds.joinToString("، "))
                                    }
                                }
                                val caloriesAdditions = selectedAdditions.filter { it.value }.size * 15
                                val calculatedCalories = selectedSubInfo.caloriesPerUnit * quantityValue + caloriesAdditions
                                val calculatedIron = selectedSubInfo.iron * quantityValue
                                val calculatedFolate = selectedSubInfo.folate * quantityValue
                                val calculatedCalcium = selectedSubInfo.calcium * quantityValue
                                val calculatedWaterBenefit = selectedSubInfo.waterBenefit * quantityValue

                                Spacer(modifier = Modifier.height(4.dp))
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("📊 ملخص الوجبة الذكية التقديري:", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite, fontSize = 11.sp)
                                        Text("الوصف: $finalDesc", color = SoftTheme.SoftGray, fontSize = 10.sp)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("السعرات: $calculatedCalories سعرة", color = SoftTheme.SoftPink, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            if (calculatedIron > 0) Text("الحديد: $calculatedIron ملجم", color = SoftTheme.MintTeal, fontSize = 10.sp)
                                            if (calculatedFolate > 0) Text("فوليك: $calculatedFolate مكجم", color = SoftTheme.SoftTeal, fontSize = 10.sp)
                                            if (calculatedCalcium > 0) Text("كالسيوم: $calculatedCalcium ملجم", color = SoftTheme.TextWhite, fontSize = 10.sp)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    TextButton(
                                        onClick = { showAddMealDialog = false },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("إلغاء", color = SoftTheme.SoftGray)
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.addNutritionMeal(
                                                mealType = mealTypeInput,
                                                description = finalDesc,
                                                calories = calculatedCalories,
                                                iron = calculatedIron,
                                                folate = calculatedFolate,
                                                calcium = calculatedCalcium,
                                                omega3 = 0.0,
                                                protein = selectedSubInfo.protein * quantityValue,
                                                carbs = selectedSubInfo.carbs * quantityValue,
                                                fat = selectedSubInfo.fat * quantityValue,
                                                sugar = 0.0,
                                                fiber = 0.0,
                                                waterBenefit = calculatedWaterBenefit
                                            )
                                            showAddMealDialog = false
                                            activeCategory = null
                                            activeSubcategory = null
                                            quantityValue = 1
                                            selectedAdditions.clear()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.MintTeal),
                                        modifier = Modifier.weight(1.5f)
                                    ) {
                                        Text("حفظ الوجبة الذكية ✓", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Symptoms & Medications Screen ---
@Composable
fun SymptomAndMedsScreen(
    viewModel: WomanCompanionViewModel
) {
    val medications by viewModel.allMedicationsState.collectAsStateWithLifecycle()
    val symptoms by viewModel.symptomLogsState.collectAsStateWithLifecycle()
    val bpLogs by viewModel.bloodPressureLogsState.collectAsStateWithLifecycle()

    var showAddMedDialog by remember { mutableStateOf(false) }
    var showAddSymptomDialog by remember { mutableStateOf(false) }
    var showAddBpDialog by remember { mutableStateOf(false) }

    var medName by remember { mutableStateOf("") }
    var medDosage by remember { mutableStateOf("") }
    var medTimes by remember { mutableStateOf("1") }
    var medDoctor by remember { mutableStateOf("") }
    var medNotes by remember { mutableStateOf("") }

    var selectedSymptom by remember { mutableStateOf("مغص") }
    var symptomSeverity by remember { mutableStateOf(5) }
    var symptomNotes by remember { mutableStateOf("") }

    var bpSystolic by remember { mutableStateOf("") }
    var bpDiastolic by remember { mutableStateOf("") }
    var bpPulse by remember { mutableStateOf("") }
    var bpNotes by remember { mutableStateOf("") }

    val coreSymptoms = listOf("غثيان", "صداع", "ألم أسفل الظهر", "مغص", "دوار", "إمساك", "حرقان معدة", "إرهاق")

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                Text(
                    text = "الأعراض والأدوية 💊🩺",
                    style = MaterialTheme.typography.titleLarge,
                    color = SoftTheme.SoftPink,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "متابعة حالتك الصحية والأدوية الموصوفة من طبيبتك",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SoftTheme.SoftGray
                )
            }

            // Medications list card
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "الأدوية والمكملات الموصوفة 💊",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.TextWhite
                    )
                    IconButton(
                        onClick = { showAddMedDialog = true },
                        modifier = Modifier.background(SoftTheme.CardSlate, CircleShape)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "إضافة دواء", tint = SoftTheme.SoftPink)
                    }
                }
            }

            if (medications.isEmpty()) {
                item {
                    Text(
                        text = "لا توجد أدوية مسجلة بعد. قومي بإضافة أدوية موصوفة من طبيبتكِ.",
                        color = SoftTheme.SoftGray,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                    )
                }
            } else {
                items(medications) { med ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = med.name,
                                    fontWeight = FontWeight.Bold,
                                    color = if (med.isActive) SoftTheme.TextWhite else SoftTheme.SoftGray
                                )
                                Text(
                                    text = "الجرعة: ${med.dosage ?: "غير محددة"} — ${med.timesPerDay} مرات يومياً",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SoftTheme.SoftGray
                                )
                                med.prescribedBy?.let {
                                    Text(
                                        text = "بواسطة: د. $it",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SoftTheme.MintTeal
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Checkbox(
                                    checked = med.isActive,
                                    onCheckedChange = { viewModel.toggleMedicationActive(med) },
                                    colors = CheckboxDefaults.colors(checkedColor = SoftTheme.MintTeal)
                                )
                                IconButton(onClick = { viewModel.deleteMedication(med) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = SoftTheme.RedDanger)
                                }
                            }
                        }
                    }
                }
            }

            // Symptom tracker section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "سجل الأعراض اليومي 🩺",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.TextWhite
                    )
                    IconButton(
                        onClick = { showAddSymptomDialog = true },
                        modifier = Modifier.background(SoftTheme.CardSlate, CircleShape)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "إضافة عرض", tint = SoftTheme.SoftPink)
                    }
                }
            }

            if (symptoms.isEmpty()) {
                item {
                    Text(
                        text = "لم تقومي بتسجيل أي عرض صحي اليوم بعد.",
                        color = SoftTheme.SoftGray,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                    )
                }
            } else {
                items(symptoms) { sym ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = sym.symptom,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftTheme.TextWhite
                                )
                                Text(
                                    text = "الشدة: ${sym.severity}/10",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SoftTheme.SoftPink
                                )
                                sym.notes?.let {
                                    Text(
                                        text = "ملاحظة: $it",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SoftTheme.SoftGray
                                    )
                                }
                            }
                            IconButton(onClick = { viewModel.deleteSymptom(sym) }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = SoftTheme.RedDanger)
                            }
                        }
                    }
                }
            }

            // --- Blood pressure tracker section ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "سجل قياسات ضغط الدم 🩸📈",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.TextWhite
                    )
                    IconButton(
                        onClick = { showAddBpDialog = true },
                        modifier = Modifier.background(SoftTheme.CardSlate, CircleShape)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "إضافة قياس", tint = SoftTheme.SoftPink)
                    }
                }
            }

            if (bpLogs.isEmpty()) {
                item {
                    Text(
                        text = "لم تقومي بتسجيل أي قياس لضغط الدم بعد. انقري على (+) للبدء بالوقاية والمتابعة.",
                        color = SoftTheme.SoftGray,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                    )
                }
            } else {
                items(bpLogs) { log ->
                    val status = getBpStatus(log.systolic, log.diastolic)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left status circle
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(status.color, CircleShape)
                                )
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "${log.systolic} / ${log.diastolic}",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 20.sp,
                                            color = SoftTheme.TextWhite
                                        )
                                        Text(
                                            text = "ملم زئبق",
                                            fontSize = 12.sp,
                                            color = SoftTheme.SoftGray
                                        )
                                    }
                                    Text(
                                        text = status.label,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = status.color
                                    )
                                    Text(
                                        text = status.advice,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SoftTheme.SoftGray,
                                        lineHeight = 16.sp,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                    Row(
                                        modifier = Modifier.padding(top = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = formatGregorianDate(log.date),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SoftTheme.SoftGray.copy(alpha = 0.7f)
                                        )
                                        if (log.pulse != null) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.Favorite,
                                                    contentDescription = "النبض",
                                                    tint = SoftTheme.SoftPink,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text(
                                                    text = "${log.pulse} ن/د",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = SoftTheme.SoftPink
                                                )
                                            }
                                        }
                                    }
                                    if (!log.notes.isNullOrEmpty()) {
                                        Text(
                                            text = "📝 ملاحظة: ${log.notes}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SoftTheme.SoftGray,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                            }
                            IconButton(onClick = { viewModel.deleteBloodPressureLog(log) }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف القياس", tint = SoftTheme.RedDanger)
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Med Dialog
    if (showAddMedDialog) {
        Dialog(onDismissRequest = { showAddMedDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "تسجيل دواء موصوف 💊",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.SoftPink
                    )

                    OutlinedTextField(
                        value = medName,
                        onValueChange = { medName = it },
                        label = { Text("اسم الدواء (مثال: حمض الفوليك)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoftTheme.SoftPink,
                            unfocusedBorderColor = SoftTheme.SoftGray,
                            focusedTextColor = SoftTheme.TextWhite,
                            unfocusedTextColor = SoftTheme.TextWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = medDosage,
                        onValueChange = { medDosage = it },
                        label = { Text("الجرعة (مثال: حبة واحدة، ٥٠٠ ملجم)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoftTheme.SoftPink,
                            unfocusedBorderColor = SoftTheme.SoftGray,
                            focusedTextColor = SoftTheme.TextWhite,
                            unfocusedTextColor = SoftTheme.TextWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = medTimes,
                        onValueChange = { medTimes = it },
                        label = { Text("مرات التكرار يومياً") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = medDoctor,
                        onValueChange = { medDoctor = it },
                        label = { Text("اسم الطبيبة الموصية") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = medNotes,
                        onValueChange = { medNotes = it },
                        label = { Text("ملاحظات الدكتورة") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(
                            onClick = { showAddMedDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء", color = SoftTheme.SoftGray)
                        }

                        Button(
                            onClick = {
                                viewModel.addMedication(
                                    name = medName,
                                    dosage = medDosage,
                                    timesPerDay = medTimes.toIntOrNull() ?: 1,
                                    prescby = medDoctor,
                                    notes = medNotes,
                                    start = System.currentTimeMillis()
                                )
                                showAddMedDialog = false
                                medName = ""
                                medDosage = ""
                                medTimes = "1"
                                medDoctor = ""
                                medNotes = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("حفظ")
                        }
                    }
                }
            }
        }
    }

    // Add Symptom Dialog
    if (showAddSymptomDialog) {
        Dialog(onDismissRequest = { showAddSymptomDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "تسجيل عرض صحي 🩺",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.SoftPink
                    )

                    Text("اختر العرض:", color = SoftTheme.TextWhite)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        coreSymptoms.forEach { s ->
                            val isSel = selectedSymptom == s
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedSymptom = s },
                                label = { Text(s) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SoftTheme.SoftPink,
                                    selectedLabelColor = SoftTheme.DeepSlate
                                )
                            )
                        }
                    }

                    Text("مستوى الشدة والتعب: $symptomSeverity/10", color = SoftTheme.TextWhite)
                    Slider(
                        value = symptomSeverity.toFloat(),
                        onValueChange = { symptomSeverity = it.toInt() },
                        valueRange = 1f..10f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = SoftTheme.SoftPink,
                            activeTrackColor = SoftTheme.SoftPink,
                            inactiveTrackColor = SoftTheme.DeepSlate
                        )
                    )

                    OutlinedTextField(
                        value = symptomNotes,
                        onValueChange = { symptomNotes = it },
                        label = { Text("أي تفاصيل إضافية...") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoftTheme.SoftPink,
                            unfocusedBorderColor = SoftTheme.SoftGray,
                            focusedTextColor = SoftTheme.TextWhite,
                            unfocusedTextColor = SoftTheme.TextWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(
                            onClick = { showAddSymptomDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء", color = SoftTheme.SoftGray)
                        }

                        Button(
                            onClick = {
                                viewModel.addSymptom(selectedSymptom, symptomSeverity, symptomNotes)
                                showAddSymptomDialog = false
                                symptomNotes = ""
                                symptomSeverity = 5
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("حفظ")
                        }
                    }
                }
            }
        }
    }

    // Add Blood Pressure Dialog
    if (showAddBpDialog) {
        Dialog(onDismissRequest = { showAddBpDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "تسجيل قياس ضغط الدم 🩸📈",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.SoftPink
                    )

                    Text(
                        text = "قيسي ضغطكِ أثناء الراحة وسجلي القراءات لمتابعة صحتكِ الوقائية.",
                        color = SoftTheme.SoftGray,
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 16.sp
                    )

                    OutlinedTextField(
                        value = bpSystolic,
                        onValueChange = { bpSystolic = it },
                        label = { Text("الضغط الانقباضي (العالي - مثال: 120)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoftTheme.SoftPink,
                            unfocusedBorderColor = SoftTheme.SoftGray,
                            focusedTextColor = SoftTheme.TextWhite,
                            unfocusedTextColor = SoftTheme.TextWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = bpDiastolic,
                        onValueChange = { bpDiastolic = it },
                        label = { Text("الضغط الانبساطي (الواطي - مثال: 80)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoftTheme.SoftPink,
                            unfocusedBorderColor = SoftTheme.SoftGray,
                            focusedTextColor = SoftTheme.TextWhite,
                            unfocusedTextColor = SoftTheme.TextWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = bpPulse,
                        onValueChange = { bpPulse = it },
                        label = { Text("نبضات القلب (اختياري - مثال: 72)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoftTheme.SoftPink,
                            unfocusedBorderColor = SoftTheme.SoftGray,
                            focusedTextColor = SoftTheme.TextWhite,
                            unfocusedTextColor = SoftTheme.TextWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = bpNotes,
                        onValueChange = { bpNotes = it },
                        label = { Text("ملاحظات (مثال: بعد تناول الكركديه، أثناء التعب)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoftTheme.SoftPink,
                            unfocusedBorderColor = SoftTheme.SoftGray,
                            focusedTextColor = SoftTheme.TextWhite,
                            unfocusedTextColor = SoftTheme.TextWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(
                            onClick = { showAddBpDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء", color = SoftTheme.SoftGray)
                        }

                        Button(
                            onClick = {
                                val sysVal = bpSystolic.toIntOrNull()
                                val diaVal = bpDiastolic.toIntOrNull()
                                if (sysVal != null && diaVal != null) {
                                    viewModel.addBloodPressureLog(
                                        systolic = sysVal,
                                        diastolic = diaVal,
                                        pulse = bpPulse.toIntOrNull(),
                                        notes = bpNotes.ifEmpty { null }
                                    )
                                    showAddBpDialog = false
                                    bpSystolic = ""
                                    bpDiastolic = ""
                                    bpPulse = ""
                                    bpNotes = ""
                                }
                            },
                            enabled = bpSystolic.isNotEmpty() && bpDiastolic.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SoftTheme.SoftPink,
                                disabledContainerColor = SoftTheme.SoftPink.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("حفظ")
                        }
                    }
                }
            }
        }
    }
}

fun getBpStatus(systolic: Int, diastolic: Int): BpStatus {
    return when {
        systolic < 90 || diastolic < 60 -> BpStatus(
            label = "ضغط دم منخفض 📉",
            color = Color(0xFF64B5F6), // Blue
            advice = "يرجى شرب السوائل أو شوربة دافئة لرفع الضغط وتجنب الدوار."
        )
        systolic >= 140 || diastolic >= 90 -> BpStatus(
            label = "ارتفاع ضغط الدم (مرحلة ٢) ⚠️",
            color = Color(0xFFEF5350), // Red
            advice = "ارتفاع شديد! يرجى الاستراحة، وتجنب الأملاح، ومراجعة طبيبتك فوراً إذا شعرتِ بصداع شديد أو زغللة عين."
        )
        systolic >= 130 || diastolic >= 80 -> BpStatus(
            label = "ارتفاع ضغط الدم (مرحلة ١) ⚠️",
            color = Color(0xFFFFB74D), // Orange
            advice = "ارتفاع خفيف. يرجى التقليل من الموالح وشرب الكركديه البارد المهدئ."
        )
        systolic >= 120 -> BpStatus(
            label = "ما قبل الارتفاع ⚠️",
            color = Color(0xFFFFD54F), // Yellow
            advice = "مستوى مرتفع قليلاً. انتبهي لغذائكِ وقللي الصوديوم."
        )
        else -> BpStatus(
            label = "ضغط دم مثالي وطبيعي ✨",
            color = Color(0xFF81C784), // Green
            advice = "قراءة ممتازة! واصلي اتباع نمط الحياة الصحي وشرب المياه."
        )
    }
}

data class BpStatus(val label: String, val color: Color, val advice: String)

// --- Tools & Services Screen ---
@Composable
fun ToolsScreen(
    viewModel: WomanCompanionViewModel
) {
    var selectedToolSubScreen by remember { mutableStateOf<String?>(null) }
    
    val deepLinkScreen by viewModel.activeSubScreen.collectAsStateWithLifecycle()
    val pregnancyState by viewModel.pregnancyState.collectAsStateWithLifecycle()
    LaunchedEffect(deepLinkScreen) {
        if (deepLinkScreen != null) {
            selectedToolSubScreen = deepLinkScreen
        }
    }

    val isPregnant = pregnancyState?.isPregnant == true
    val filteredTools = remember(isPregnant) {
        val list = mutableListOf<Triple<String, String, String>>()
        if (isPregnant) {
            list.add(Triple("fetal_growth", "مُتابع نمو الجنين (الوزن والحجم) 📈👶", "تتبع وتحليل وزن وطول طفلكِ ومقارنتهما بالمنحنى الطبيعي للأسبوع"))
            list.add(Triple("fetal_kicks", "عداد ركلات الجنين 👶", "حساب حركة ونشاط طفلك بمؤقت دقيق"))
            list.add(Triple("craving", "سجل الوحم والاشتهاء ومشاركة جوري 🍉🍓", "شاركي جوري ما تشتهينه اليوم، وسجلي ذكريات حملك مع تحليل طبي دافئ"))
            list.add(Triple("fitness", "لياقة المرأة الحامل والنفاس 🧘‍♀️💪", "تمارين آمنة ومدروسة بمؤقت تفاعلي للاستشفاء والحفاظ على نشاطك"))
            list.add(Triple("contractions", "مؤقت انقباضات الولادة ⏱️", "تسجيل التقلصات مع كاشف لقاعدة ٥-١-١"))
            list.add(Triple("appointments", "جدول زيارات الدكتورة 🏥", "إدارة وتذكير بمواعيد الكشوف والتحاليل"))
            list.add(Triple("danger", "علامات الخطر التحذيرية ⚠️", "قائمة الأعراض الحرجة التي تستدعي اتصالاً عاجلاً"))
        } else {
            list.add(Triple("smart_conception", "حاسبة التخطيط والحمل الذكي 🎯", "توقع تواريخ الإباضة والولادة برج طفلك المستقبلي"))
            list.add(Triple("qada", "قضاء أيام الصيام 🌙", "عداد أيام الصيام المتبقية عليك لقضائها"))
        }
        
        list.add(Triple("sleep_analyzer", "محلل ومراقب النوم الذكي 🌙💤", "تتبع وتحليل جودة نومك اليومي وأثره على صحتك الحيوية"))
        list.add(Triple("maonaty", "مؤونتي الذكية لإدارة المنزل 📦🍳", "إدارة مخزون مطبخك، مشترياتك، وصفاتك الذكية، ومهام الترتيب المنزلي"))
        list.add(Triple("home_pharmacy", "الصيدلية المنزلية المتقدمة 💊📦", "تتبع مخزون أدويتك المتبقي وصلاحيتها وتحذيرات الأمان للحمل والرضاعة"))
        list.add(Triple("partner_sync", "رابط الرفيق ومشاركة الشريك 🔗❤️", "ربط زوجك أو عائلتك لمتابعة حالتك ودعمك بعبارات حية ومؤثرة"))
        list.add(Triple("journal", "يوميات مذكراتي الجميلة ✍️", "تدوين مشاعرك ورسائلك للطفل القادم"))
        list
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        if (selectedToolSubScreen == null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                item {
                    Text(
                        text = "الأدوات والمساعدة 🌸🛠️",
                        style = MaterialTheme.typography.titleLarge,
                        color = SoftTheme.SoftPink,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "أدوات مخصصة لتتبع الجنين والعبادات وحماية خصوصيتك",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoftTheme.SoftGray
                    )
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleDarkMode() }
                            .testTag("tools_quick_theme_toggle"),
                        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(SoftTheme.SoftPink.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (SoftTheme.isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = "تبديل المظهر",
                                    tint = SoftTheme.SoftPink
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (SoftTheme.isDark) "تفعيل المظهر المضيء ☀️" else "تفعيل المظهر الداكن 🌙",
                                    fontWeight = FontWeight.Bold,
                                    color = SoftTheme.TextWhite,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "بدلي مظهر التطبيق بالكامل بلمسة واحدة سريعة",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SoftTheme.SoftGray
                                )
                            }
                        }
                    }
                }

                // Grid list of sub-tools
                items(filteredTools) { (key, title, subtitle) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedToolSubScreen = key }
                            .testTag("tool_card_$key"),
                        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = title, fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = SoftTheme.SoftPink)
                        }
                    }
                }
            }
        } else {
            // Display sub-screen
            Column(modifier = Modifier.fillMaxSize()) {
                IconButton(
                    onClick = { 
                        selectedToolSubScreen = null
                        viewModel.setActiveSubScreen(null)
                    },
                    modifier = Modifier.padding(16.dp).background(SoftTheme.CardSlate, CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = SoftTheme.SoftPink)
                }

                when (selectedToolSubScreen) {
                    "maonaty" -> MaonatySubScreen(viewModel)
                    "fitness" -> FitnessScreen(viewModel)
                    "sleep_analyzer" -> SleepAnalyzerScreen(viewModel)
                    "craving" -> CravingScreen(viewModel)
                    "partner_sync" -> PartnerSyncScreen(viewModel)
                    "home_pharmacy" -> HomePharmacyScreen(viewModel)
                    "fetal_kicks" -> FetalKicksSubScreen(viewModel)
                    "fetal_growth" -> FetalGrowthSubScreen(viewModel)
                    "contractions" -> ContractionsSubScreen(viewModel)
                    "smart_conception" -> SmartConceptionSubScreen()
                    "qada" -> QadaSubScreen(viewModel)
                    "appointments" -> AppointmentsSubScreen(viewModel)
                    "journal" -> JournalSubScreen(viewModel)
                    "danger" -> DangerSubScreen()
                }
            }
        }
    }
}

// --- Smart Conception and Baby prediction Sub-screen ---
@Composable
fun SmartConceptionSubScreen() {
    var lastPeriodDate by remember { mutableStateOf(System.currentTimeMillis()) }
    val context = LocalContext.current

    // Calculations based on Last Period Date
    val predictedDueDate = remember(lastPeriodDate) {
        lastPeriodDate + 280L * 24 * 60 * 60 * 1000
    }
    val ovulationStart = remember(lastPeriodDate) {
        lastPeriodDate + 11L * 24 * 60 * 60 * 1000
    }
    val ovulationEnd = remember(lastPeriodDate) {
        lastPeriodDate + 16L * 24 * 60 * 60 * 1000
    }

    // Trimester timeline
    val tri1End = remember(lastPeriodDate) { lastPeriodDate + 84L * 24 * 60 * 60 * 1000 }
    val tri2End = remember(lastPeriodDate) { lastPeriodDate + 182L * 24 * 60 * 60 * 1000 }

    // Child expected Western Zodiac and descriptions
    val zodiacInfo = remember(predictedDueDate) {
        val cal = Calendar.getInstance().apply { timeInMillis = predictedDueDate }
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = cal.get(Calendar.MONTH) + 1 // 1-indexed

        when (month) {
            1 -> if (day < 20) Pair("الجدي ♑", "هادئ، ذكي، طموح ومحب للتعلم والاستقلالية.") else Pair("الدلو ♒", "ودود، مبتكر، يحب التفكير خارج الصندوق وحر الشخصية.")
            2 -> if (day < 19) Pair("الدلو ♒", "ودود، مبتكر، يحب التفكير خارج الصندوق وحر الشخصية.") else Pair("الحوت ♓", "عاطفي للغاية، ذو خيال واسع وحنون ومحب للفنون.")
            3 -> if (day < 21) Pair("الحوت ♓", "عاطفي للغاية، ذو خيال واسع وحنون ومحب للفنون.") else Pair("الحمل ♈", "نشيط، شجاع وقوي الشخصية، فضولي ومحب للاستكشاف.")
            4 -> if (day < 20) Pair("الحمل ♈", "نشيط، شجاع وقوي الشخصية، فضولي ومحب للاستكشاف.") else Pair("الثور ♉", "صبور وعنيد إيجابياً، يحب الاستقرار وصاحب عزيمة قوية.")
            5 -> if (day < 21) Pair("الثور ♉", "صبور وعنيد إيجابياً، يحب الاستقرار وصاحب عزيمة قوية.") else Pair("الجوزاء ♊", "ذكي للغاية، اجتماعي، سريع التعلم ولديه موهبة الحديث والمرح.")
            6 -> if (day < 21) Pair("الجوزاء ♊", "ذكي للغاية، اجتماعي، سريع التعلم ولديه موهبة الحديث والمرح.") else Pair("السرطان ♋", "حنون جداً، مرتبط بالعائلة، ذو إحساس مرهف ومحب للسلام.")
            7 -> if (day < 23) Pair("السرطان ♋", "حنون جداً، مرتبط بالعائلة، ذو إحساس مرهف ومحب للسلام.") else Pair("الأسد ♌", "قيادي بطبعه، شجاع وصاحب حضور قوي، كريم ومحب للظهور.")
            8 -> if (day < 23) Pair("الأسد ♌", "قيادي بطبعه، شجاع وصاحب حضور قوي، كريم ومحب للظهور.") else Pair("العذراء ♍", "دقيق ومنظم، يحب الترتيب والتفاصيل، ذكي ومساعد ممتاز للآخرين.")
            9 -> if (day < 23) Pair("العذراء ♍", "دقيق ومنظم، يحب الترتيب والتفاصيل، ذكي ومساعد ممتاز للآخرين.") else Pair("الميزان ♎", "لطيف ودبلوماسي، يعشق الجمال والتوازن، محبوب واجتماعي جداً.")
            10 -> if (day < 23) Pair("الميزان ♎", "لطيف ودبلوماسي، يعشق الجمال والتوازن، محبوب واجتماعي جداً.") else Pair("العقرب ♏", "قوي الملاحظة، شغوف، كتوم ومخلص جداً ولديه شخصية جذابة.")
            11 -> if (day < 22) Pair("العقرب ♏", "قوي الملاحظة، شغوف، كتوم ومخلص جداً ولديه شخصية جذابة.") else Pair("القوس ♐", "مرح ومتفائل، يعشق السفر واللعب، شجاع ومحب للحرية والضحك.")
            12 -> if (day < 22) Pair("القوس ♐", "مرح ومتفائل، يعشق السفر واللعب، شجاع ومحب للحرية والضحك.") else Pair("الجدي ♑", "هادئ، ذكي، طموح ومحب للتعلم والاستقلالية.")
            else -> Pair("غير معروف", "")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("حاسبة التخطيط والحمل الذكي 🎯", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
        Text(
            "أدخلي تاريخ آخر دورة شهرية لحساب مواعيد التبويض والولادة والتعرف على السمات المتوقعة للطفل القادم ذكياً بالكامل دون إنترنت.",
            color = SoftTheme.SoftGray,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("تاريخ أول يوم لآخر دورة شهرية (LMP):", color = SoftTheme.TextWhite, fontWeight = FontWeight.Bold)
                Button(
                    onClick = {
                        val calendar = Calendar.getInstance().apply { timeInMillis = lastPeriodDate }
                        android.app.DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                val cal = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, y)
                                    set(Calendar.MONTH, m)
                                    set(Calendar.DAY_OF_MONTH, d)
                                }
                                lastPeriodDate = cal.timeInMillis
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.DeepSlate)
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = SoftTheme.SoftPink)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(formatGregorianDate(lastPeriodDate), color = SoftTheme.TextWhite)
                }
            }
        }

        // Output results in beautiful timeline cards
        Text("نتائج التخطيط والتوقعات الذكية 🔮", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SoftTheme.SoftPink, modifier = Modifier.align(Alignment.Start))

        // 1. Expected Due Date
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("📅", fontSize = 28.sp)
                Column {
                    Text("تاريخ الولادة المتوقع:", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite, style = MaterialTheme.typography.bodyMedium)
                    Text(formatGregorianDate(predictedDueDate), color = SoftTheme.MintTeal, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
            }
        }

        // 2. Fertility window
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("✨", fontSize = 28.sp)
                Column {
                    Text("أيام التبويض القصوى والخصوبة:", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite, style = MaterialTheme.typography.bodyMedium)
                    Text("من ${formatGregorianDate(ovulationStart)} إلى ${formatGregorianDate(ovulationEnd)}", color = SoftTheme.SoftPink, style = MaterialTheme.typography.bodyMedium)
                    Text("هذه هي الفترة الذهبية لفرص حدوث الحمل بمشيئة الله.", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                }
            }
        }

        // 3. predicted Zodiac of the baby
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("👶", fontSize = 28.sp)
                Column {
                    Text("البرج والسمات المتوقعة للطفل:", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite, style = MaterialTheme.typography.bodyMedium)
                    Text("برج ${zodiacInfo.first}", color = SoftTheme.GoldFasting, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(zodiacInfo.second, style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                }
            }
        }

        // 4. Trimesters timeline
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("جدول الفترات الثلاث للحمل القادم:", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite, style = MaterialTheme.typography.bodyMedium)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("الثلث الأول (تثبيت):", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                    Text("حتى ${formatGregorianDate(tri1End)}", style = MaterialTheme.typography.bodySmall, color = SoftTheme.TextWhite)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("الثلث الثاني (نمو):", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                    Text("حتى ${formatGregorianDate(tri2End)}", style = MaterialTheme.typography.bodySmall, color = SoftTheme.TextWhite)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("الثلث الثالث (استعداد):", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                    Text("حتى الولادة بمشيئة الله", style = MaterialTheme.typography.bodySmall, color = SoftTheme.TextWhite)
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

// --- Fetal Kicks Sub-screen ---
@Composable
fun FetalKicksSubScreen(viewModel: WomanCompanionViewModel) {
    val activeStart by viewModel.currentKickSessionStart.collectAsStateWithLifecycle()
    val currentCount by viewModel.currentKickCount.collectAsStateWithLifecycle()
    val history by viewModel.fetalKickSessionsState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("عداد حركات وركلات الجنين 👶", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
        Text(
            "توصي الهيئات الصحية بعد عشر ركلات أو حركات واضحة خلال جلسة تتبع في أوقات نشاط الجنين المعتادة.",
            color = SoftTheme.SoftGray,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )

        if (activeStart == null) {
            Button(
                onClick = { viewModel.startFetalKickSession() },
                colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                modifier = Modifier.fillMaxWidth().height(56.dp).testTag("start_kick_session_btn")
            ) {
                Text("بدء جلسة عد جديدة")
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("الجلسة نشطة ومستمرة", color = SoftTheme.MintTeal, fontWeight = FontWeight.Bold)

                    Text(
                        text = "$currentCount",
                        style = MaterialTheme.typography.displayLarge,
                        color = SoftTheme.TextWhite,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = { viewModel.incrementKickCount() },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                        modifier = Modifier.size(100.dp).testTag("increment_kick_btn"),
                        shape = CircleShape
                    ) {
                        Text("ركلة! 🦶", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.cancelFetalKickSession() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء", color = SoftTheme.RedDanger)
                        }
                        Button(
                            onClick = { viewModel.saveFetalKickSession() },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.MintTeal),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("حفظ الجلسة")
                        }
                    }
                }
            }
        }

        Text("سجل جلسات الحركة السابقة 📖", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite, modifier = Modifier.align(Alignment.Start))

        if (history.isEmpty()) {
            Text("لا توجد جلسات مسجلة بعد.", color = SoftTheme.SoftGray)
        } else {
            history.forEach { ses ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("الركلات: ${ses.kickCount}", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
                            Text("المدة: ${ses.durationSeconds / 60} دقيقة و ${ses.durationSeconds % 60} ثانية", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                        }
                        Text(formatGregorianDate(ses.startTime), style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftPink)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(80.dp))
    }
}

// --- Contractions Sub-screen ---
@Composable
fun ContractionsSubScreen(viewModel: WomanCompanionViewModel) {
    val activeStart by viewModel.activeContractionStart.collectAsStateWithLifecycle()
    val history by viewModel.contractionLogsState.collectAsStateWithLifecycle()
    val warning = viewModel.checkContractionWarning()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("مؤقت تقلصات وانقباضات الولادة ⏱️", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)

        if (warning) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.RedDanger),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("⚠️ تنبيه هام (قاعدة 5-1-1)", fontWeight = FontWeight.Bold, color = Color.White)
                    Text(
                        "التقلصات تحدث بمعدل متقارب (كل ٥ دقائق أو أقل) وتستمر لـ دقيقة على الأقل منذ ساعة. قد تكونين في مرحلة الولادة النشطة. يرجى الاتصال بطبيبتك فوراً والتوجه للمستشفى بأمان 🌸.",
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        if (activeStart == null) {
            Button(
                onClick = { viewModel.startContraction() },
                colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.RedDanger),
                modifier = Modifier.fillMaxWidth().height(56.dp).testTag("start_contraction_btn")
            ) {
                Text("بدء انقباضة الآن 🔴")
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("التقلص مستمر...", color = SoftTheme.RedDanger, fontWeight = FontWeight.Bold)

                    Button(
                        onClick = { viewModel.stopAndSaveContraction() },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.RedDanger),
                        modifier = Modifier.size(100.dp).testTag("stop_contraction_btn"),
                        shape = CircleShape
                    ) {
                        Text("إيقاف وحفظ", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("السجل الأخير للتقلصات 📋", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
            TextButton(onClick = { viewModel.clearContractions() }) {
                Text("تصفير القائمة", color = SoftTheme.SoftGray)
            }
        }

        if (history.isEmpty()) {
            Text("لا توجد تقلصات مسجلة بعد.", color = SoftTheme.SoftGray)
        } else {
            history.forEach { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("المدة: ${log.durationSeconds} ثانية", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
                            if (log.intervalSeconds > 0) {
                                Text("الفاصل الزمني: ${log.intervalSeconds / 60} دقيقة", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                            }
                        }
                        Text(formatTime(log.startTime), style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftPink)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(80.dp))
    }
}

// --- Qada Fast Sub-screen ---
@Composable
fun QadaSubScreen(viewModel: WomanCompanionViewModel) {
    val qadaList by viewModel.qadaFastsState.collectAsStateWithLifecycle()
    val currentPhase = viewModel.getCurrentCyclePhase()
    val periodLogs by viewModel.periodLogsState.collectAsStateWithLifecycle()
    val pregState by viewModel.pregnancyState.collectAsStateWithLifecycle()

    var showAddQadaDialog by remember { mutableStateOf(false) }
    var yearInput by remember { mutableStateOf("") }
    var missedInput by remember { mutableStateOf("") }

    var completedPrayers by remember { mutableStateOf(setOf<String>()) }

    // Adaptive logic:
    val userPhase = pregState?.userPhase ?: "period"
    val isOnPeriod = currentPhase.phaseName == "Menstruation" && pregState == null
    val isInPostpartum = userPhase == "postpartum"
    val isExempt = isOnPeriod || isInPostpartum

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("مساعد العبادات والصلوات الذكي 🌙", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
        Text(
            "يتكيف تلقائياً مع فترات دورتكِ الشهرية ونفاسكِ لضمان حماية سجلاتك الدينية بدقة.",
            color = SoftTheme.SoftGray,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )

        if (isExempt) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, SoftTheme.GoldFasting.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("🌸 رخصة شرعية وعذر رحيم", fontWeight = FontWeight.Bold, color = SoftTheme.GoldFasting, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = if (isInPostpartum) {
                            "أنتِ الآن في فترة النفاس المباركة (رخصة شرعية من رب العالمين) 🥰 ارتاحي واعتني بنفسك وبطفلكِ، واذكري الله واستغفري. لا صلاة ولا صيام عليكِ الآن."
                        } else {
                            "أنتِ في رخصة شرعية رقيقة بسبب العذر الشرعي (الحيض) 🥰 ارتاحي واحتسبي الأجر في الاستغفار والذكر والعبادات القلبية. لا صلاة عليكِ ولا صيام."
                        },
                        textAlign = TextAlign.Center,
                        color = SoftTheme.TextWhite,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp
                    )

                    if (!isInPostpartum) {
                        Button(
                            onClick = {
                                val currentHijriYear = 1447 // Current Hijri Year
                                val existing = qadaList.find { it.yearHijri == currentHijriYear }
                                if (existing != null) {
                                    viewModel.addQadaFast(currentHijriYear, existing.missedDays + 1, existing.completedDays)
                                } else {
                                    viewModel.addQadaFast(currentHijriYear, 1, 0)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.GoldFasting),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("تسجيل يوم فطر تلقائي لقضائه لاحقاً 📅", color = SoftTheme.DeepSlate, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("🕌 جدول صلواتكِ لليوم (طهر ونشاط)", fontWeight = FontWeight.Bold, color = SoftTheme.MintTeal, style = MaterialTheme.typography.titleMedium)
                    Text("حافظي على صلواتكِ الخمس اليومية وتابعي التزامكِ الروحي الجميل:", color = SoftTheme.SoftGray, style = MaterialTheme.typography.bodySmall)

                    val prayers = listOf("الفجر", "الظهر", "العصر", "المغرب", "العشاء")
                    prayers.forEach { prayer ->
                        val isChecked = completedPrayers.contains(prayer)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    completedPrayers = if (isChecked) {
                                        completedPrayers - prayer
                                    } else {
                                        completedPrayers + prayer
                                    }
                                }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(
                                    imageVector = if (isChecked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (isChecked) SoftTheme.MintTeal else SoftTheme.SoftGray
                                )
                                Text(text = prayer, color = SoftTheme.TextWhite, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text = if (isChecked) "مكتملة ✨" else "لم تُؤدَّ بعد",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isChecked) SoftTheme.MintTeal else SoftTheme.SoftGray
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = SoftTheme.CardSlate)

        Text("تتبع قضاء أيام صيام رمضان 🌙", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
        Text(
            "سجلي الأيام الفائتة بسبب الحيض أو رخصة الفطر في الحمل في سنوات رمضان المختلفة وتابعي تقدمكِ في القضاء بيسر وسهولة.",
            color = SoftTheme.SoftGray,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )

        Button(
            onClick = { showAddQadaDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
            modifier = Modifier.fillMaxWidth().testTag("add_qada_btn")
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("إضافة سنة جديدة")
        }

        if (qadaList.isEmpty()) {
            Text("لا توجد سجلات قضاء بعد.", color = SoftTheme.SoftGray)
        } else {
            qadaList.forEach { fast ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("رمضان هجري: ${fast.yearHijri}", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
                            IconButton(onClick = { viewModel.deleteQadaFast(fast) }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = SoftTheme.RedDanger)
                            }
                        }

                        LinearProgressIndicator(
                            progress = { fast.completedDays.toFloat() / fast.missedDays.toFloat().coerceAtLeast(1f) },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = SoftTheme.GoldFasting,
                            trackColor = SoftTheme.DeepSlate
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "المكتمل: ${fast.completedDays} من أصل ${fast.missedDays} أيام",
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftTheme.SoftGray
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { viewModel.updateQadaFastProgress(fast, increment = false) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.DeepSlate),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                                ) {
                                    Text("-", color = SoftTheme.TextWhite, fontSize = 16.sp)
                                }
                                Button(
                                    onClick = { viewModel.updateQadaFastProgress(fast, increment = true) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.GoldFasting),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                                ) {
                                    Text("+", color = SoftTheme.DeepSlate, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(80.dp))
    }

    if (showAddQadaDialog) {
        Dialog(onDismissRequest = { showAddQadaDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("إضافة قضاء صيام 🌙", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SoftTheme.SoftPink)

                    OutlinedTextField(
                        value = yearInput,
                        onValueChange = { yearInput = it },
                        label = { Text("السنة الهجرية (مثال: ١٤٤٧)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = missedInput,
                        onValueChange = { missedInput = it },
                        label = { Text("عدد الأيام الفائتة") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TextButton(onClick = { showAddQadaDialog = false }, modifier = Modifier.weight(1f)) {
                            Text("إلغاء", color = SoftTheme.SoftGray)
                        }
                        Button(
                            onClick = {
                                val year = yearInput.toIntOrNull() ?: 1447
                                val missed = missedInput.toIntOrNull() ?: 7
                                viewModel.addQadaFast(year, missed, 0)
                                showAddQadaDialog = false
                                yearInput = ""
                                missedInput = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("حفظ")
                        }
                    }
                }
            }
        }
    }
}

// --- Appointments Sub-screen ---
@Composable
fun AppointmentsSubScreen(viewModel: WomanCompanionViewModel) {
    val appointments by viewModel.appointmentsState.collectAsStateWithLifecycle()

    var showAddApptDialog by remember { mutableStateOf(false) }
    var titleInput by remember { mutableStateOf("") }
    var doctorInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("جدول زيارات ومواعيد الدكتورة 🏥", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)

        Button(
            onClick = { showAddApptDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
            modifier = Modifier.fillMaxWidth().testTag("add_appt_btn")
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("إضافة موعد كشف جديد")
        }

        if (appointments.isEmpty()) {
            Text("لا توجد مواعيد مسجلة بعد.", color = SoftTheme.SoftGray)
        } else {
            appointments.forEach { appt ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = appt.title,
                                fontWeight = FontWeight.Bold,
                                color = if (appt.completed) SoftTheme.SoftGray else SoftTheme.TextWhite
                            )
                            appt.doctorName?.let {
                                Text("مع: د. $it", style = MaterialTheme.typography.bodySmall, color = SoftTheme.MintTeal)
                            }
                            appt.notes?.let {
                                Text("تفاصيل: $it", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                            }
                            Text("التاريخ: ${formatGregorianDate(appt.dateTime)}", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftPink)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Checkbox(
                                checked = appt.completed,
                                onCheckedChange = { viewModel.toggleAppointmentCompleted(appt) },
                                colors = CheckboxDefaults.colors(checkedColor = SoftTheme.MintTeal)
                            )
                            IconButton(onClick = { viewModel.deleteAppointment(appt) }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = SoftTheme.RedDanger)
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(80.dp))
    }

    if (showAddApptDialog) {
        Dialog(onDismissRequest = { showAddApptDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("إضافة موعد طبي 🏥", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SoftTheme.SoftPink)

                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("عنوان الموعد (مثال: سونار الثلث الثاني)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = doctorInput,
                        onValueChange = { doctorInput = it },
                        label = { Text("اسم الطبيبة") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        label = { Text("ملاحظات الكشف أو التحاليل المطلوبة") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TextButton(onClick = { showAddApptDialog = false }, modifier = Modifier.weight(1f)) {
                            Text("إلغاء", color = SoftTheme.SoftGray)
                        }
                        Button(
                            onClick = {
                                viewModel.addAppointment(
                                    title = titleInput,
                                    dateTime = System.currentTimeMillis() + 3L * 24 * 60 * 60 * 1000, // simple mock: 3 days in future
                                    doctor = doctorInput,
                                    notes = notesInput
                                )
                                showAddApptDialog = false
                                titleInput = ""
                                doctorInput = ""
                                notesInput = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("حفظ")
                        }
                    }
                }
            }
        }
    }
}

// --- Journal Sub-screen ---
@Composable
fun JournalSubScreen(viewModel: WomanCompanionViewModel) {
    val journals by viewModel.journalEntriesState.collectAsStateWithLifecycle()

    var journalContent by remember { mutableStateOf("") }
    val moods = listOf("🌸 سعيدة", "🌱 هادئة", "🪵 تعبة", "🩸 قلقة", "✨ متحمسة")
    var selectedMood by remember { mutableStateOf("🌸 سعيدة") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("يوميات ومذكرات الأمومة والطفل ✍️", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)

        Card(
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("اكتبي خواطركِ أو رسالة لطفلكِ القادم:", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    moods.forEach { m ->
                        val isSel = selectedMood == m
                        Button(
                            onClick = { selectedMood = m },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSel) SoftTheme.SoftPink else SoftTheme.DeepSlate,
                                contentColor = if (isSel) SoftTheme.DeepSlate else SoftTheme.TextWhite
                            )
                        ) {
                            Text(m, fontSize = 10.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = journalContent,
                    onValueChange = { journalContent = it },
                    placeholder = { Text("أهلاً طفلي الحبيب، اليوم سمعت صوت قلبك اللطيف لأول مرة...") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SoftTheme.SoftPink,
                        unfocusedBorderColor = SoftTheme.SoftGray,
                        focusedTextColor = SoftTheme.TextWhite,
                        unfocusedTextColor = SoftTheme.TextWhite
                    )
                )

                Button(
                    onClick = {
                        if (journalContent.isNotEmpty()) {
                            viewModel.addJournalEntry(journalContent, selectedMood)
                            journalContent = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                    modifier = Modifier.fillMaxWidth().testTag("add_journal_btn")
                ) {
                    Text("حفظ في المذكرات")
                }
            }
        }

        Text("سجل ذكرياتكِ المكتوبة 📖", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite, modifier = Modifier.align(Alignment.Start))

        if (journals.isEmpty()) {
            Text("لا توجد مذكرات مسجلة بعد.", color = SoftTheme.SoftGray)
        } else {
            journals.forEach { entry ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("المزاج: ${entry.mood ?: "طبيعي"}", fontWeight = FontWeight.Bold, color = SoftTheme.SoftPink)
                            IconButton(onClick = { viewModel.deleteJournal(entry) }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = SoftTheme.RedDanger)
                            }
                        }
                        Text(entry.content, color = SoftTheme.TextWhite, style = MaterialTheme.typography.bodyMedium)
                        Text(formatGregorianDate(entry.date), style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray, modifier = Modifier.align(Alignment.End))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(80.dp))
    }
}

// --- Danger Signs Sub-screen (Fixed Medical Guidelines) ---
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

// --- Settings Screen ---
@Composable
fun SettingsScreen(
    viewModel: WomanCompanionViewModel,
    onNavigateBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val settings by viewModel.appLockSettingsState.collectAsStateWithLifecycle()
    val savedApiKey by viewModel.apiKeyFlow.collectAsStateWithLifecycle(initialValue = null)
    val savedBaseUrl by viewModel.apiBaseUrlFlow.collectAsStateWithLifecycle(initialValue = "https://generativelanguage.googleapis.com/")
    val savedModelName by viewModel.modelNameFlow.collectAsStateWithLifecycle(initialValue = "gemini-3.5-flash")

    var pinCodeInput by remember { mutableStateOf("") }
    var isLockEnabled by remember { mutableStateOf(false) }
    var isStealthEnabled by remember { mutableStateOf(false) }
    var companionNameInput by remember { mutableStateOf("جوري") }
    var stepTargetInput by remember { mutableStateOf("6000") }
    var gitHubUrlInput by remember { mutableStateOf("https://raw.githubusercontent.com/your_username/your_repo/main/matrix.json") }
    var userApiKeyInput by remember { mutableStateOf("") }
    var userApiBaseUrlInput by remember { mutableStateOf("https://generativelanguage.googleapis.com/") }
    var userModelNameInput by remember { mutableStateOf("gemini-3.5-flash") }
    var isDarkModeLocal by remember { mutableStateOf(true) }
    var isApiKeySavedShow by remember { mutableStateOf(false) }

    val syncStatus by viewModel.gitHubSyncStatus.collectAsStateWithLifecycle()

    LaunchedEffect(savedApiKey) {
        savedApiKey?.let {
            userApiKeyInput = it
        }
    }

    LaunchedEffect(savedBaseUrl) {
        userApiBaseUrlInput = savedBaseUrl
    }

    LaunchedEffect(savedModelName) {
        userModelNameInput = savedModelName
    }

    LaunchedEffect(settings) {
        settings?.let {
            isLockEnabled = it.isLockEnabled
            isStealthEnabled = it.isStealthModeEnabled
            pinCodeInput = it.pinHash ?: ""
            companionNameInput = it.companionName
            stepTargetInput = it.dailyStepTarget.toString()
            gitHubUrlInput = it.gitHubRepoUrl ?: "https://raw.githubusercontent.com/your_username/your_repo/main/matrix.json"
            isDarkModeLocal = it.isDarkMode
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = SoftTheme.SoftPink)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "إعدادات الخصوصية والأمان 🔐",
                    style = MaterialTheme.typography.titleLarge,
                    color = SoftTheme.TextWhite,
                    fontWeight = FontWeight.Bold
                )
            }

            // Security PIN Card
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("قفل التطبيق بـ PIN حماية", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("تفعيل قفل رمز المرور", color = SoftTheme.SoftGray)
                        Switch(
                            checked = isLockEnabled,
                            onCheckedChange = { isLockEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = SoftTheme.SoftPink)
                        )
                    }

                    if (isLockEnabled) {
                        OutlinedTextField(
                            value = pinCodeInput,
                            onValueChange = { if (it.length <= 4) pinCodeInput = it },
                            label = { Text("رمز PIN (٤ أرقام)") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("وضع التخفي (Stealth mode)", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
                                Text("إخفاء العناوين الواضحة وصور الحمل في شاشة القفل لمنع المتطفلين", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                            }
                            Switch(
                                checked = isStealthEnabled,
                                onCheckedChange = { isStealthEnabled = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = SoftTheme.SoftPink)
                              )
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.configureAppLock(
                                pin = if (pinCodeInput.isNotEmpty()) pinCodeInput else null,
                                isEnabled = isLockEnabled,
                                isStealth = isStealthEnabled,
                                companionName = if (companionNameInput.isNotEmpty()) companionNameInput else "جوري",
                                dailyStepTarget = stepTargetInput.toIntOrNull() ?: 6000,
                                isDarkMode = isDarkModeLocal,
                                gitHubRepoUrl = gitHubUrlInput,
                                userApiKey = if (userApiKeyInput.isNotEmpty()) userApiKeyInput else null
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                        modifier = Modifier.fillMaxWidth().testTag("save_settings_btn")
                    ) {
                        Text("حفظ التغييرات الأمنية")
                    }
                }
            }

            // Companion & Steps Configuration Card
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("تخصيص الصديقة الذكية والنشاط 🌸🚶‍♀️", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite, style = MaterialTheme.typography.titleMedium)
                    
                    OutlinedTextField(
                        value = companionNameInput,
                        onValueChange = { companionNameInput = it },
                        label = { Text("اسم صديقتكِ الذكية (مثال: جوري)", color = SoftTheme.SoftGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SoftTheme.TextWhite,
                            unfocusedTextColor = SoftTheme.TextWhite,
                            focusedBorderColor = SoftTheme.SoftPink,
                            unfocusedBorderColor = SoftTheme.DeepSlate,
                            focusedLabelColor = SoftTheme.SoftPink,
                            unfocusedLabelColor = SoftTheme.SoftGray,
                            focusedContainerColor = SoftTheme.DeepSlate,
                            unfocusedContainerColor = SoftTheme.DeepSlate
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = stepTargetInput,
                        onValueChange = { stepTargetInput = it },
                        label = { Text("هدف الخطوات اليومي (خطوة)", color = SoftTheme.SoftGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SoftTheme.TextWhite,
                            unfocusedTextColor = SoftTheme.TextWhite,
                            focusedBorderColor = SoftTheme.SoftPink,
                            unfocusedBorderColor = SoftTheme.DeepSlate,
                            focusedLabelColor = SoftTheme.SoftPink,
                            unfocusedLabelColor = SoftTheme.SoftGray,
                            focusedContainerColor = SoftTheme.DeepSlate,
                            unfocusedContainerColor = SoftTheme.DeepSlate
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("تفعيل المظهر الداكن (Dark Mode) 🌙", color = SoftTheme.TextWhite, fontSize = 14.sp)
                        Switch(
                            checked = isDarkModeLocal,
                            onCheckedChange = { isDarkModeLocal = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = SoftTheme.SoftPink)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("مزامنة مصفوفة النصائح وتطوير التطبيق 🔄", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite, style = MaterialTheme.typography.titleSmall)

                    OutlinedTextField(
                        value = gitHubUrlInput,
                        onValueChange = { gitHubUrlInput = it },
                        label = { Text("رابط GitHub لتحديث مصفوفة النصائح (Raw JSON)", color = SoftTheme.SoftGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SoftTheme.TextWhite,
                            unfocusedTextColor = SoftTheme.TextWhite,
                            focusedBorderColor = SoftTheme.SoftPink,
                            unfocusedBorderColor = SoftTheme.DeepSlate,
                            focusedLabelColor = SoftTheme.SoftPink,
                            unfocusedLabelColor = SoftTheme.SoftGray,
                            focusedContainerColor = SoftTheme.DeepSlate,
                            unfocusedContainerColor = SoftTheme.DeepSlate
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            viewModel.syncJouriMatrix()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.MintTeal),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("تحديث مصفوفة جوري من جيت هاب 🔄")
                    }

                    syncStatus?.let { status ->
                        Text(
                            text = status,
                            color = if (status.contains("نجاح") || status.contains("جاري")) SoftTheme.MintTeal else SoftTheme.SoftPink,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("إعدادات الذكاء الاصطناعي (مفتاح الـ API) 🔑", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite, style = MaterialTheme.typography.titleSmall)

                    val hasBuiltInKey = BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY" && BuildConfig.GEMINI_API_KEY.isNotEmpty()
                    
                    if (hasBuiltInKey && savedApiKey.isNullOrBlank()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SoftTheme.MintTeal.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("🤖", fontSize = 18.sp)
                                    Text(
                                        text = "مفتاح Gemini الذكي مدمج ونشط!",
                                        fontWeight = FontWeight.Bold,
                                        color = SoftTheme.MintTeal,
                                        fontSize = 13.sp
                                    )
                                }
                                Text(
                                    text = "تم دمج مفتاح ذكاء اصطناعي افتراضي مسبقاً في التطبيق. جوري جاهزة ومستعدة تماماً للرد على جميع تساؤلاتكِ فوراً دون الحاجة لإدخال مفتاح مخصص.",
                                    color = SoftTheme.TextWhite,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    val apiTestStatus by viewModel.apiKeyTestStatus.collectAsStateWithLifecycle()
                    var isEditingApiKey by remember { mutableStateOf(true) }

                    LaunchedEffect(savedApiKey) {
                        isEditingApiKey = savedApiKey.isNullOrBlank()
                    }

                    if (!isEditingApiKey && !savedApiKey.isNullOrBlank()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("🟢", fontSize = 16.sp)
                                    Text(
                                        text = "مفتاح الـ API محفوظ ومؤمّن محلياً",
                                        fontWeight = FontWeight.Bold,
                                        color = SoftTheme.MintTeal,
                                        fontSize = 14.sp
                                    )
                                }
                                Text(
                                    text = "مفتاح الـ API الخاص بكِ نشط وجاهز لتشغيل جميع ميزات الذكاء الاصطناعي في جوري.",
                                    color = SoftTheme.SoftGray,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            isEditingApiKey = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("تعديل المفتاح ✍️", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = {
                                            viewModel.testAndSaveApiKey(savedApiKey ?: "", savedBaseUrl, savedModelName)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.MintTeal),
                                        modifier = Modifier.weight(1.2f)
                                    ) {
                                        Text("فحص جودة الاتصال 🔄", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = userApiKeyInput,
                            onValueChange = { 
                                userApiKeyInput = it
                                isApiKeySavedShow = false // Reset success message on change
                            },
                            label = { Text("مفتاح API الخاص بك (Gemini API)", color = SoftTheme.SoftGray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = SoftTheme.TextWhite,
                                unfocusedTextColor = SoftTheme.TextWhite,
                                focusedBorderColor = SoftTheme.SoftPink,
                                unfocusedBorderColor = SoftTheme.DeepSlate,
                                focusedLabelColor = SoftTheme.SoftPink,
                                unfocusedLabelColor = SoftTheme.SoftGray,
                                focusedContainerColor = SoftTheme.DeepSlate,
                                unfocusedContainerColor = SoftTheme.DeepSlate
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("gemini_api_key_input")
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        OutlinedTextField(
                            value = userApiBaseUrlInput,
                            onValueChange = { userApiBaseUrlInput = it },
                            label = { Text("رابط الخادم / البروكسي (Base URL)", color = SoftTheme.SoftGray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = SoftTheme.TextWhite,
                                unfocusedTextColor = SoftTheme.TextWhite,
                                focusedBorderColor = SoftTheme.SoftPink,
                                unfocusedBorderColor = SoftTheme.DeepSlate,
                                focusedLabelColor = SoftTheme.SoftPink,
                                unfocusedLabelColor = SoftTheme.SoftGray,
                                focusedContainerColor = SoftTheme.DeepSlate,
                                unfocusedContainerColor = SoftTheme.DeepSlate
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val defaultUrl = "https://generativelanguage.googleapis.com/"
                            
                            Button(
                                onClick = { userApiBaseUrlInput = defaultUrl },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (userApiBaseUrlInput == defaultUrl) SoftTheme.SoftPink else SoftTheme.DeepSlate
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("الرسمي الافتراضي 🌐", fontSize = 10.sp)
                             }
                         }

                         Spacer(modifier = Modifier.height(4.dp))

                         OutlinedTextField(
                             value = userModelNameInput,
                             onValueChange = { userModelNameInput = it },
                             label = { Text("اسم الموديل (Model Name)", color = SoftTheme.SoftGray) },
                             colors = OutlinedTextFieldDefaults.colors(
                                 focusedTextColor = SoftTheme.TextWhite,
                                 unfocusedTextColor = SoftTheme.TextWhite,
                                 focusedBorderColor = SoftTheme.SoftPink,
                                 unfocusedBorderColor = SoftTheme.DeepSlate,
                                 focusedLabelColor = SoftTheme.SoftPink,
                                 unfocusedLabelColor = SoftTheme.SoftGray,
                                 focusedContainerColor = SoftTheme.DeepSlate,
                                 unfocusedContainerColor = SoftTheme.DeepSlate
                             ),
                             modifier = Modifier.fillMaxWidth()
                         )

                         Row(
                             horizontalArrangement = Arrangement.spacedBy(6.dp),
                             modifier = Modifier.fillMaxWidth()
                         ) {
                             listOf("gemini-3.5-flash", "gemini-2.5-flash", "gemini-1.5-flash").forEach { model ->
                                 Button(
                                     onClick = { userModelNameInput = model },
                                     colors = ButtonDefaults.buttonColors(
                                         containerColor = if (userModelNameInput == model) SoftTheme.SoftPink else SoftTheme.DeepSlate
                                     ),
                                     contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                     modifier = Modifier.weight(1f)
                                 ) {
                                     Text(model, fontSize = 9.sp)
                                 }
                             }
                         }

                         Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                if (userApiKeyInput.isBlank()) {
                                    android.widget.Toast.makeText(context, "الرجاء إدخال مفتاح API صالح أولاً", android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.testAndSaveApiKey(userApiKeyInput, userApiBaseUrlInput, userModelNameInput)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.MintTeal),
                            modifier = Modifier.fillMaxWidth().testTag("save_api_key_btn"),
                            enabled = apiTestStatus != "testing"
                        ) {
                            if (apiTestStatus == "testing") {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = SoftTheme.DeepSlate, strokeWidth = 2.dp)
                            } else {
                                Text("فحص وحفظ الإعدادات بالكامل 🔑")
                            }
                        }
                    }

                    // Show validation status feedback
                    apiTestStatus?.let { status ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    status == "testing" -> SoftTheme.DeepSlate
                                    status == "success" -> SoftTheme.MintTeal.copy(alpha = 0.15f)
                                    status.startsWith("error") -> SoftTheme.RedDanger.copy(alpha = 0.15f)
                                    else -> SoftTheme.DeepSlate
                                }
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                when {
                                    status == "testing" -> {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = SoftTheme.SoftPink, strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("جاري فحص الاتصال بالخادم والتحقق من صحة المفتاح... ⏳", color = SoftTheme.TextWhite, fontSize = 11.sp)
                                    }
                                    status == "success" -> {
                                        Text("🟢", fontSize = 14.sp)
                                        Text("الاتصال ناجح! تم حفظ وتأمين مفتاح الـ API بنجاح وميزات الذكاء الاصطناعي نشطة الآن. 🎉", color = SoftTheme.MintTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    status.startsWith("error") -> {
                                        Text("🔴", fontSize = 14.sp)
                                        val errorDetail = status.removePrefix("error:").trim()
                                        Text("خطأ في الاتصال: $errorDetail\nيرجى التحقق من صحة المفتاح وجودة اتصال الإنترنت الخاص بك.", color = SoftTheme.RedDanger, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.configureAppLock(
                                pin = if (pinCodeInput.isNotEmpty()) pinCodeInput else null,
                                isEnabled = isLockEnabled,
                                isStealth = isStealthEnabled,
                                companionName = if (companionNameInput.isNotEmpty()) companionNameInput else "جوري",
                                dailyStepTarget = stepTargetInput.toIntOrNull() ?: 6000,
                                isDarkMode = isDarkModeLocal,
                                gitHubRepoUrl = gitHubUrlInput,
                                userApiKey = null
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                        modifier = Modifier.fillMaxWidth().testTag("save_companion_btn")
                    ) {
                        Text("حفظ التخصيص والمظهر")
                    }
                }
            }

            // Factory Reset / Nuke Card
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("حذف جميع البيانات (Factory Reset) ⚠️", fontWeight = FontWeight.Bold, color = SoftTheme.RedDanger)
                    Text(
                        "تطبيق رفيق المرأة يعمل بشكل أوفلاين بالكامل. نسيان رمز الـ PIN أو حذف التطبيق سيؤدي لضياع بياناتك المكتوبة. يمكنك تصفير كافة السجلات الحالية من هنا.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTheme.SoftGray
                    )

                    Button(
                        onClick = { viewModel.factoryReset() },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.RedDanger),
                        modifier = Modifier.fillMaxWidth().testTag("factory_reset_btn")
                    ) {
                        Text("مسح كافة البيانات نهائياً")
                    }
                }
            }
        }
    }
}

// FlowRow wrapper for compatibility since flow layouts are experimental or standard in newer compose versions
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement
    ) {
        content()
    }
}

data class ToolTarget(val buttonText: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val targetPage: Int, val subscreen: String?)

     // --- Jouri Smart Companion Chat Dialog ---
@Composable
fun JouriChatDialog(
    viewModel: WomanCompanionViewModel,
    onDismiss: () -> Unit,
    onNavigateToTab: (Int) -> Unit = {}
) {
    val pregState by viewModel.pregnancyState.collectAsStateWithLifecycle()
    val settingsState by viewModel.appLockSettingsState.collectAsStateWithLifecycle()
    val todayStepsState by viewModel.todayStepLogState.collectAsStateWithLifecycle()
    val weatherState by viewModel.weatherState.collectAsStateWithLifecycle()
    val savedApiKey by viewModel.apiKeyFlow.collectAsStateWithLifecycle(initialValue = null)
    val isNetworkAvailable by viewModel.isNetworkAvailable.collectAsStateWithLifecycle()

    val companionName = settingsState?.companionName ?: "جوري"

    var inputMessage by remember { mutableStateOf("") }
    var showVirtualKeyboard by remember { mutableStateOf(false) }
    val messages = remember { mutableStateListOf<Pair<String, Boolean>>() } // text, isUser
    var isTyping by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Smart direct shortcut state
    var activeSuggestedAction by remember { mutableStateOf<String?>(null) }
    val lastMessageText = messages.lastOrNull { !it.second }?.first ?: ""
    LaunchedEffect(lastMessageText) {
        val text = lastMessageText.lowercase()
        activeSuggestedAction = when {
            text.contains("ركلات") || text.contains("ركلة") || text.contains("حركة الجنين") || text.contains("حركه الجنين") || text.contains("بيتحرك") -> "fetal_kicks"
            text.contains("انقباض") || text.contains("طلق") || text.contains("5-1-1") || text.contains("تقلص") -> "contractions"
            text.contains("صيام") || text.contains("قضاء") || text.contains("صوم") -> "qada"
            text.contains("مذكرات") || text.contains("يوميات") || text.contains("فضفضة") || text.contains("فضفضه") || text.contains("كتابة") -> "journal"
            text.contains("دواء") || text.contains("أدوية") || text.contains("ادويه") || text.contains("مكمل") || text.contains("فيتامين") -> "meds"
            text.contains("ماء") || text.contains("شربت كوب") -> "water"
            text.contains("غذائ") || text.contains("أكل") || text.contains("طعام") || text.contains("وصفة") || text.contains("ملوخية") || text.contains("كشري") || text.contains("فول") -> "nutrition"
            text.contains("وحم") || text.contains("الوحم") || text.contains("اشته") || text.contains("أشته") || text.contains("توحمت") -> "craving"
            text.contains("نوم") || text.contains("الأرق") || text.contains("ارق") || text.contains("مراقب النوم") -> "sleep"
            text.contains("شريك") || text.contains("الزوج") || text.contains("زوج") || text.contains("عائل") -> "partner"
            text.contains("صيدلية") || text.contains("الصيدلية") || text.contains("مخزون") -> "pharmacy"
            text.contains("تمارين") || text.contains("لياقة") || text.contains("الحركة") || text.contains("رياضة") -> "fitness"
            text.contains("معونتي") || text.contains("المخزون") || text.contains("مشتريات") || text.contains("المقادير") -> "maonaty"
            text.contains("موعد") || text.contains("عيادة") || text.contains("حجز") || text.contains("زيارة") || text.contains("دكتورة") -> "appointments"
            text.contains("حاسبة الحمل") || text.contains("حاسبة الخصوبة") || text.contains("حاسبه") || text.contains("التبويض") -> "smart_conception"
            text.contains("علامات الخطر") || text.contains("أعراض الطوارئ") || text.contains("خطر") || text.contains("طوارئ") || text.contains("نزيف") -> "danger"
            else -> null
        }
    }

    // Offline / Privacy mode state
    val isApiKeyMissing = (BuildConfig.GEMINI_API_KEY == "MY_GEMINI_API_KEY" || BuildConfig.GEMINI_API_KEY.isEmpty()) && savedApiKey.isNullOrBlank()
    var isOfflineMode by remember { mutableStateOf(isApiKeyMissing || !isNetworkAvailable) }

    LaunchedEffect(isNetworkAvailable, isApiKeyMissing) {
        if (!isNetworkAvailable || isApiKeyMissing) {
            isOfflineMode = true
        }
    }

    val quickReplies = remember {
        listOf(
            "شربت كوب ماء 💧" to "سجّلي كوب ماء 💧",
            "أشعر بمغص/ألم 🥺" to "أشعر ببعض المغص والألم في بطني 🥺",
            "أشعر بصداع/تعب 😢" to "أشعر بصداع وتعب شديد 😢",
            "أشعر بتقلب مزاجي 💔" to "أشعر بتقلبات مزاجية وضيق 💔",
            "نصيحة الغذاء اليومية 🥑" to "أريد نصيحة غذائية مناسبة لمرحلتي 🥑",
            "ما هي مرحلتي الحالية؟ 🌸" to "ما هي مرحلتي الحالية وتفاصيلها؟ 🌸",
            "أنا بخير والحمد لله! 🥰" to "الحمد لله، أنا بخير وبصحة ممتازة اليوم! 🥰"
        )
    }

    var selectedCatalogCategory by remember { mutableStateOf("أعراض 🩺") }
    val catalogCategories = remember { listOf("أعراض 🩺", "أوجاع ⚡", "ضغط الدم ❤️", "التغذية 🥑", "أسئلة ❓") }
    val catalogSubItems = remember {
        mapOf(
            "أعراض 🩺" to listOf(
                "غثيان وترجيع 🤢" to "أشعر بغثيان وترجيع نفسي غامة",
                "إرهاق وخمول 😴" to "أشعر بتعب وإرهاق وخمول تام",
                "صداع ودوخة 😢" to "أشعر بصداع مستمر ودوخة شديدة",
                "تقلب مزاجي 💔" to "أشعر بتقلبات مزاجية وضيق حاد",
                "إمساك وعسر هضم 🥦" to "أعاني من إمساك وصعوبة هضم شديدة"
            ),
            "أوجاع ⚡" to listOf(
                "مغص وتقلصات 🥺" to "أشعر بتقلصات ومغص في الرحم وألم بطن",
                "ألم أسفل الظهر 🤰" to "أشعر بألم أسفل الظهر مزعج",
                "تورم القدمين 🦵" to "أعاني من تورم في القدمين واحتباس سوائل",
                "ألم المفاصل 🦴" to "أشعر بوجع وألم شديد في المفاصل والحوض"
            ),
            "ضغط الدم ❤️" to listOf(
                "الضغط العالي ⚠️" to "ما هي تفاصيل الضغط العالي والوقاية منه وأكلاته المناسبة؟",
                "الضغط الواطي 📉" to "ما هي تفاصيل الضغط الواطي وهبوط الدم والمشروبات المناسبة؟"
            ),
            "التغذية 🥑" to listOf(
                "أكلات للحديد 🩸" to "أريد أكلات مصرية غنية بالحديد لعلاج الأنيميا",
                "أغذية للكالسيوم 🥛" to "أريد أغذية مصرية غنية بالكالسيوم لتقوية العظام",
                "مصادر بروتين 💪" to "أريد وجبات مصرية غنية بالبروتين للغذاء والقوة",
                "خضروات ورقية 🥦" to "انصحيني بخضار وخضروات طازجة وفوائدها",
                "فاكهة مصرية 🍉" to "انصحيني بفاكهة وفواكه مصرية مفيدة ومكوناتها",
                "أعشاب مهدئة 🍵" to "انصحيني بمشروب وأعشاب دافئة مهدئة وفوائدها"
            ),
            "أسئلة ❓" to listOf(
                "المشي والحركة 🚶‍♀️" to "هل المشي والحركة مفيدان في حالتي؟",
                "عداد ركلات الجنين 👶" to "كيف أحسب ركلات الجنين وتتبع حركته؟",
                "قاعدة الولادة 5-1-1 ⏱️" to "ما هي قاعدة 5-1-1 لحساب الانقباضات والولادة؟",
                "رخصة الصيام والعبادة 🌙" to "ما هي تفاصيل قضاء الصيام والعبادات ورخصة الإفطار؟"
            )
        )
    }

    LaunchedEffect(Unit) {
        if (messages.isEmpty()) {
            val motherName = pregState?.motherName
            if (motherName.isNullOrEmpty()) {
                messages.add(Pair("أهلاً بكِ يا صديقتي الغالية! 🌸 أنا $companionName، رفيقتكِ وصديقتكِ الذكية في هذه الرحلة الجميلة. يسعدني جداً التعرف عليكِ! ما هو اسمكِ الكريم؟ وكيف تودين تتبع صحتكِ معي اليوم؟ (هل نتابع الدورة والخصوبة، أم نتابع رحلة الحمل المباركة؟) 🥰", false))
            } else {
                messages.add(Pair("أهلاً بعودتكِ يا صديقتي الغالية $motherName! 🌸 كيف حالكِ اليوم وكيف تشعرين؟ أنا $companionName هنا لأسمعكِ وأقدم لكِ الدعم والنصائح الدافئة خطوة بخطوة. أخبريني بأي أعراض تشعرين بها! 🥰", false))
            }
        }
    }

    LaunchedEffect(messages.size, isTyping) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Helper to send message and get response
    suspend fun handleMessageResponse(text: String) {
        val normalizedText = text.trim().lowercase()
        val isThemeRequest = normalizedText.contains("لون") || 
                             normalizedText.contains("ألوان") || 
                             normalizedText.contains("الوان") || 
                             normalizedText.contains("الوضع الفاتح") || 
                             normalizedText.contains("الوضع الداكن") || 
                             normalizedText.contains("فاتح") || 
                             normalizedText.contains("داكن") || 
                             normalizedText.contains("مظلم") || 
                             normalizedText.contains("مضيء") || 
                             normalizedText.contains("ثيم") ||
                             normalizedText.contains("مظهر")

        if (isThemeRequest) {
            val switchToDark = normalizedText.contains("داكن") || 
                               normalizedText.contains("غامق") || 
                               normalizedText.contains("مظلم") || 
                               normalizedText.contains("ليل") ||
                               normalizedText.contains("أسود") ||
                               normalizedText.contains("اسود")
            
            val switchToLight = normalizedText.contains("فاتح") || 
                                normalizedText.contains("مضيء") || 
                                normalizedText.contains("نهار") ||
                                normalizedText.contains("أبيض") ||
                                normalizedText.contains("ابيض")

            if (switchToDark) {
                viewModel.setThemeMode(true)
                val reply = "من عيوني يا غالية! 🌸✨ تم تغيير مظهر التطبيق إلى الوضع الداكن المريح للعينين في الليل. أتمنى لكِ تصفحاً مريحاً ورعاية صحية هادئة لقلبكِ الرقيق 💕🌃"
                messages.add(Pair(reply, false))
                isTyping = false
                return
            } else if (switchToLight) {
                viewModel.setThemeMode(false)
                val reply = "أبشري يا روحي! 🌸✨ تم تحويل ألوان التطبيق إلى الوضع الفاتح واللطيف ليكون مشرقاً مثل عينيكِ الجميلتين ☀️ أتمنى لكِ يوماً سعيداً ومليئاً بالنشاط والحيوية 🥰💛"
                messages.add(Pair(reply, false))
                isTyping = false
                return
            } else {
                val currentDark = settingsState?.isDarkMode ?: true
                viewModel.setThemeMode(!currentDark)
                val reply = if (currentDark) {
                    "أبشري يا روحي! 🌸✨ تم تحويل ألوان التطبيق إلى الوضع الفاتح واللطيف ليكون مشرقاً مثل عينيكِ الجميلتين ☀️ أتمنى لكِ يوماً سعيداً ومليئاً بالنشاط والحيوية 🥰"
                } else {
                    "من عيوني يا غالية! 🌸✨ تم تغيير مظهر التطبيق إلى الوضع الداكن المريح للعينين في الليل. أتمنى لكِ تصفحاً مريحاً ورعاية صحية هادئة لقلبكِ الرقيق 💕🌃"
                }
                messages.add(Pair(reply, false))
                isTyping = false
                return
            }
        }

        // First, query the local SQL database cache
        val cachedAnswer = viewModel.getCachedAnswer(text)
        if (cachedAnswer != null) {
            messages.add(Pair(cachedAnswer, false))
            isTyping = false
            return
        }

        // 🧠 Jouri's Local Index / Brain Check: Check her instant offline index first
        val phaseInfo = viewModel.getCurrentCyclePhase()
        val waterLog = viewModel.todayWaterLogState.value?.amountMl ?: 0
        val steps = todayStepsState?.steps ?: 0
        val target = settingsState?.dailyStepTarget ?: 6000
        val offlineResponse = OfflineJouriEngine.getResponse(
            userInput = text,
            motherName = pregState?.motherName,
            phaseInfo = phaseInfo,
            pregnancyState = pregState,
            todayWaterLogged = waterLog,
            companionName = companionName,
            weatherInfo = weatherState,
            todaySteps = steps,
            targetSteps = target
        )

        if (offlineResponse.isSpecificMatch) {
            // Respond instantly with her elegant localized knowledge
            kotlinx.coroutines.delay(150) // Tiny human-like delay for visual comfort
            messages.add(Pair(offlineResponse.replyText, false))
            viewModel.saveCachedAnswer(text, offlineResponse.replyText) // Cache the result
            isTyping = false

            // Execute local database actions instantly
            offlineResponse.actionType?.let { action ->
                when (action) {
                    "water" -> {
                        val amt = offlineResponse.actionValue as? Int ?: 250
                        viewModel.addWater(amt)
                    }
                    "symptom" -> {
                        val pair = offlineResponse.actionValue as? Pair<*, *>
                        val symName = pair?.first as? String ?: "مغص وألم"
                        val severity = pair?.second as? Int ?: 5
                        viewModel.addSymptom(symName, severity, "مسجّل تلقائياً بواسطة رفيقتكِ $companionName 🌸")
                    }
                    "profile" -> {
                        val newName = offlineResponse.actionValue as? String ?: ""
                        if (newName.isNotEmpty()) {
                            viewModel.setMotherProfile(
                                motherName = newName,
                                babyName = null,
                                userPhase = null,
                                lastPeriodDate = null,
                                preWeight = null,
                                height = null
                            )
                        }
                    }
                    "birthdate" -> {
                        val bday = offlineResponse.actionValue as? Long
                        if (bday != null) {
                            viewModel.updateUserBirthDate(bday)
                        }
                    }
                    "food_list" -> {
                        val list = offlineResponse.actionValue as? List<com.example.data.EgyptianFoodEntity>
                        list?.forEach { food ->
                            val mealType = if (food.category == "drink") "مشروب" else "وجبة"
                            viewModel.addNutritionMeal(
                                mealType = mealType,
                                description = food.name,
                                calories = food.calories,
                                iron = food.protein * 0.1,
                                folate = food.carbs * 0.2,
                                calcium = food.fat * 0.5,
                                omega3 = food.protein * 0.01
                            )
                        }
                    }
                    "craving_save" -> {
                        val foodName = offlineResponse.actionValue as? String ?: ""
                        if (foodName.isNotEmpty()) {
                            val type = when {
                                foodName.contains("شوكو") || foodName.contains("شيكو") || foodName.contains("كاكاو") || foodName.contains("كيك") || foodName.contains("حلو") -> "Chocolate"
                                foodName.contains("ليمون") || foodName.contains("برتقال") || foodName.contains("موالح") || foodName.contains("حامض") -> "Sour"
                                foodName.contains("مخلل") || foodName.contains("فسيخ") || foodName.contains("رنجة") || foodName.contains("ملح") || foodName.contains("حادق") -> "Salty"
                                foodName.contains("فلفل") || foodName.contains("شطة") || foodName.contains("حار") -> "Spicy"
                                else -> "Sweet"
                            }
                            viewModel.addCravingLog(
                                cravingItem = foodName,
                                cravingType = type,
                                intensity = 7,
                                notes = "تم التسجيل تلقائياً عبر محادثتكِ الودية والدافئة مع رفيقتكِ جوري 🌸"
                            )
                        }
                    }
                }
            }
            return
        }

        val useOffline = isOfflineMode || isApiKeyMissing
        if (useOffline) {
            // Wait 600ms to simulate thinking
            kotlinx.coroutines.delay(600)
            val phaseInfo = viewModel.getCurrentCyclePhase()
            val waterLog = viewModel.todayWaterLogState.value?.amountMl ?: 0
            val response = OfflineJouriEngine.getResponse(
                userInput = text,
                motherName = pregState?.motherName,
                phaseInfo = phaseInfo,
                pregnancyState = pregState,
                todayWaterLogged = waterLog,
                companionName = companionName,
                weatherInfo = weatherState,
                todaySteps = steps,
                targetSteps = target
            )
            
            messages.add(Pair(response.replyText, false))
            viewModel.saveCachedAnswer(text, response.replyText) // Cache the result
            isTyping = false
            
            // Execute offline database actions instantly
            response.actionType?.let { action ->
                when (action) {
                    "water" -> {
                        val amt = response.actionValue as? Int ?: 250
                        viewModel.addWater(amt)
                    }
                    "symptom" -> {
                        val pair = response.actionValue as? Pair<*, *>
                        val symName = pair?.first as? String ?: "مغص وألم"
                        val severity = pair?.second as? Int ?: 5
                        viewModel.addSymptom(symName, severity, "مسجّل تلقائياً بواسطة رفيقتكِ $companionName 🌸")
                    }
                    "profile" -> {
                        val newName = response.actionValue as? String ?: ""
                        if (newName.isNotEmpty()) {
                            viewModel.setMotherProfile(
                                motherName = newName,
                                babyName = null,
                                userPhase = null,
                                lastPeriodDate = null,
                                preWeight = null,
                                height = null
                            )
                        }
                    }
                    "food_list" -> {
                        val list = response.actionValue as? List<com.example.data.EgyptianFoodEntity>
                        list?.forEach { food ->
                            val mealType = if (food.category == "drink") "مشروب" else "وجبة"
                            viewModel.addNutritionMeal(
                                mealType = mealType,
                                description = food.name,
                                calories = food.calories,
                                iron = food.protein * 0.1,
                                folate = food.carbs * 0.2,
                                calcium = food.fat * 0.5,
                                omega3 = food.protein * 0.01
                            )
                        }
                    }
                    "craving_save" -> {
                        val foodName = response.actionValue as? String ?: ""
                        if (foodName.isNotEmpty()) {
                            val type = when {
                                foodName.contains("شوكو") || foodName.contains("شيكو") || foodName.contains("كاكاو") || foodName.contains("كيك") || foodName.contains("حلو") -> "Chocolate"
                                foodName.contains("ليمون") || foodName.contains("برتقال") || foodName.contains("موالح") || foodName.contains("حامض") -> "Sour"
                                foodName.contains("مخلل") || foodName.contains("فسيخ") || foodName.contains("رنجة") || foodName.contains("ملح") || foodName.contains("حادق") -> "Salty"
                                foodName.contains("فلفل") || foodName.contains("شطة") || foodName.contains("حار") -> "Spicy"
                                else -> "Sweet"
                            }
                            viewModel.addCravingLog(
                                cravingItem = foodName,
                                cravingType = type,
                                intensity = 7,
                                notes = "تم التسجيل تلقائياً عبر محادثتكِ الودية والدافئة مع رفيقتكِ جوري 🌸"
                            )
                        }
                    }
                }
            }
        } else {
            // Online Mode using Gemini API
            val currentSteps = steps
            val targetSteps = target
            val weatherDetails = if (weatherState != null) {
                "درجة الحرارة الحالية: ${weatherState?.temperature}°م، الرطوبة: ${weatherState?.humidity}%، حالة الجو: ${weatherState?.description}"
            } else {
                "الطقس الحالي غير متاح"
            }

            val systemPrompt = """
                أنتِ "$companionName"، رفيقة وصديقة مقربة ذكية، دافئة، وحنونة جداً للمرأة العربية. تتحدثين بلهجة لطيفة، متعاطفة للغاية، ومفعمة بالحب والرعاية، وتستخدمين عبارات رقيقة مثل "يا روحي"، "يا صديقتي الغالية"، "يا عزيزتي"، "يا قلبي".
                
                بيانات المستخدمة الحالية المتوفرة لديكِ للرد بدقة ومساعدتها:
                - الخطوات اليومية للمستخدمة: $currentSteps خطوة من هدف $targetSteps خطوة.
                - الطقس الفعلي الحالي: $weatherDetails (إذا كان الجو حاراً ورطباً، ذكّريها بلطف بشرب المزيد من الماء وترطيب جسمها).
                - مرحلة تتبعها الحالية: ${if (pregState != null) "حامل (في الثلث ${viewModel.getPregnancyProgression()?.trimester ?: 1})" else "تتبع الدورة والخصوبة (طور ${viewModel.getCurrentCyclePhase().phaseArabic})"}
                
                تساعدين المستخدمة في:
                1. السؤال عن أعراضها وتقديم نصائح صحية ومريحة مخصصة بأسلوب دافئ، مشجعةً إياها على المشي المعتدل والراحة عند الحاجة بناءً على طورها المذكور أعلاه وعدد خطواتها والطقس.
                2. مرافقتها خطوة بخطوة وإرشادها.
                3. تذكيرها بشرب الماء والراحة وتغذية جسدها بالمعادن اللازمة خصوصاً في الأجواء الحارة والرطبة.
                4. إذا كانت المستخدمة حاملاً، يجب عليكِ بشكل استباقي وودود للغاية من وقت لآخر أن تسأليها عن "الوحم" وتشاركيها مشاعرها وتسأليها بدلال: "اتوحمتِ على إيه النهاردة يا روحي؟" كنوع من المشاركة الوجدانية الممتعة والتفاعل الاجتماعي.
                5. إذا صرحت لكِ بما توحمت عليه، تفاعلي معها بحب شديد وبطريقة فكاهية ودافئة، وحللي لها رغبتها طبياً بشكل بسيط (مثل ارتباط الحوادق بالسوائل والأملاح، والحلويات بالطاقة السريعة)، واقترحي عليها تصفح "سجل الوحم" في التطبيق.

                مهم جداً (التسجيل والتحديث التلقائي للملف):
                إذا كانت هذه هي بداية التعارف ولم تحدد المستخدمة اسمها أو طورها بعد، اسأليها بلطف شديد عن اسمها وطور تتبعها الحالي (متابعة الدورة والخصوبة أو تتبع الحمل).
                بمجرد أن تخبركِ بالاسم أو مرحلة التتبع، يجب أن ترفقي ردكِ الدافئ بكود JSON مخفي تماماً في نهاية الإجابة يبدأ بدقة بـ [DATA_UPDATE] (بدون أي أسطر فارغة قبله)، وتضعي فيه البيانات التي صرحت بها لتحديث داتا التطبيق تلقائياً كالتالي:
                [DATA_UPDATE]{"motherName": "الاسم المستخرج", "userPhase": "period أو pregnancy أو postpartum", "babyName": "اسم الجنين إن صرحت به"}

                إذا صرحت المستخدمة عن أعراض تشعر بها مثل (صداع، مغص، غثيان، ألم ظهر، تقلبات مزاج)، تعاطفي معها بقوة وقدمي لها توجيهات منزلية طبيعية للراحة والغذاء المتوازن والترطيب.
                تحدثي دائماً باللغة العربية الدافئة.
            """.trimIndent()
            
            try {
                val response = GeminiService.generateContent(text, systemPrompt)
                isTyping = false
                
                if (response.contains("[DATA_UPDATE]")) {
                    val parts = response.split("[DATA_UPDATE]")
                    val cleanText = parts[0].trim()
                    val jsonPart = parts.getOrNull(1)?.trim()
                    
                    messages.add(Pair(cleanText, false))
                    viewModel.saveCachedAnswer(text, cleanText) // Cache the result
                    
                    if (!jsonPart.isNullOrEmpty()) {
                        try {
                           val json = JSONObject(jsonPart)
                           val mName = if (json.has("motherName")) json.optString("motherName") else null
                           val bName = if (json.has("babyName")) json.optString("babyName") else null
                           val uPhase = if (json.has("userPhase")) json.optString("userPhase") else null
                           
                           viewModel.setMotherProfile(
                               motherName = mName,
                               babyName = bName,
                               userPhase = uPhase,
                               lastPeriodDate = null,
                               preWeight = null,
                               height = null
                           )
                        } catch (e: Exception) {
                            Log.e("JouriChat", "Failed to parse json metadata", e)
                        }
                    }
                } else {
                    messages.add(Pair(response, false))
                    viewModel.saveCachedAnswer(text, response) // Cache the result
                }
            } catch (e: Exception) {
                // Network failed, switch to offline mode automatically
                isOfflineMode = true
                messages.add(Pair("يا روحي، لم أستطع الاتصال بالذكاء الاصطناعي السحابي الآن. قمتُ بتفعيل الوضع المحلي الآمن (أوفلاين) فوراً لأبقى بجانبكِ بدون انقطاع! 📴🌸", false))
                
                val phaseInfo = viewModel.getCurrentCyclePhase()
                val waterLog = viewModel.todayWaterLogState.value?.amountMl ?: 0
                val offlineResp = OfflineJouriEngine.getResponse(
                    userInput = text,
                    motherName = pregState?.motherName,
                    phaseInfo = phaseInfo,
                    pregnancyState = pregState,
                    todayWaterLogged = waterLog
                )
                messages.add(Pair(offlineResp.replyText, false))
                viewModel.saveCachedAnswer(text, offlineResp.replyText) // Cache the result
                isTyping = false
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .padding(vertical = 16.dp)
            ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(SoftTheme.SoftPink.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🌸", fontSize = 20.sp)
                        }
                        Column {
                            Text(
                                text = "$companionName صديقتكِ الذكية",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SoftTheme.TextWhite
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(if (isOfflineMode) SoftTheme.MintTeal else Color(0xFF4CAF50), CircleShape)
                                )
                                Text(
                                    text = if (isOfflineMode) "الوضع المحلي (أوفلاين) 📴" else "الوضع الذكي (أونلاين) 🌐",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SoftTheme.SoftGray,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Quick Online/Offline Switcher
                        if (!isApiKeyMissing) {
                            IconButton(onClick = { isOfflineMode = !isOfflineMode }) {
                                Icon(
                                    imageVector = if (isOfflineMode) Icons.Default.Lock else Icons.Default.Share,
                                    contentDescription = "تبديل الوضع",
                                    tint = SoftTheme.SoftPink,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = SoftTheme.SoftGray)
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = SoftTheme.DeepSlate.copy(alpha = 0.5f)
                )

                // Message List
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages) { (text, isUser) ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isUser) SoftTheme.SoftPink else SoftTheme.DeepSlate
                                ),
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isUser) 16.dp else 0.dp,
                                    bottomEnd = if (isUser) 0.dp else 16.dp
                                ),
                                modifier = Modifier.widthIn(max = 260.dp)
                            ) {
                                Text(
                                    text = text,
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SoftTheme.TextWhite,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }

                    if (isTyping) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(SoftTheme.SoftPink, CircleShape)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(SoftTheme.SoftPink.copy(alpha = 0.6f), CircleShape)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(SoftTheme.SoftPink.copy(alpha = 0.3f), CircleShape)
                                )
                                Text(
                                    text = "$companionName تفكر...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SoftTheme.SoftGray,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Smart Direct-Link Suggested Tool Button
                activeSuggestedAction?.let { action ->
                    val target = when (action) {
                        "fetal_kicks" -> ToolTarget("فتح عداد ركلات الجنين 👶", Icons.Default.Favorite, 4, "fetal_kicks")
                        "contractions" -> ToolTarget("فتح عداد انقباضات الرحم ⏱️", Icons.Default.PlayArrow, 4, "contractions")
                        "qada" -> ToolTarget("تتبع قضاء الصيام والعبادات 🌙", Icons.Default.Star, 4, "qada")
                        "journal" -> ToolTarget("فتح المذكرات واليوميات السرية 📝", Icons.Default.Edit, 4, "journal")
                        "meds" -> ToolTarget("تسجيل أدويتي وفيتاميناتي 💊", Icons.Default.Add, 3, null)
                        "water" -> ToolTarget("تسجيل شرب مياه 💧", Icons.Default.Info, 2, null)
                        "nutrition" -> ToolTarget("دليل الأغذية والوجبات المصرية 🍲", Icons.Default.Check, 2, null)
                        "craving" -> ToolTarget("فتح سجل الوحم والاشتهاء 🍉🍓", Icons.Default.FavoriteBorder, 4, "craving")
                        "sleep" -> ToolTarget("فتح محلل ومراقب النوم الذكي 🌙💤", Icons.Default.Notifications, 4, "sleep_analyzer")
                        "partner" -> ToolTarget("فتح رابط الرفيق ومشاركة الشريك 🔗❤️", Icons.Default.Share, 4, "partner_sync")
                        "pharmacy" -> ToolTarget("فتح الصيدلية المنزلية المتقدمة 💊📦", Icons.Default.List, 4, "home_pharmacy")
                        "fitness" -> ToolTarget("فتح تمارين لياقة الحمل والنفاس 🧘‍♀️💪", Icons.Default.Star, 4, "fitness")
                        "maonaty" -> ToolTarget("فتح نظام معونتي المنزلي 📦🛒", Icons.Default.Home, 4, "maonaty")
                        "appointments" -> ToolTarget("تسجيل وحفظ مواعيد الأطباء 📅", Icons.Default.DateRange, 4, "appointments")
                        "smart_conception" -> ToolTarget("فتح حاسبة الحمل والخصوبة الذكية 🧠", Icons.Default.Info, 4, "smart_conception")
                        "danger" -> ToolTarget("فتح دليل علامات الخطر والطوارئ 🚨", Icons.Default.Warning, 4, "danger")
                        else -> ToolTarget("", Icons.Default.Build, 4, null)
                    }

                    if (target.buttonText.isNotEmpty()) {
                        Button(
                            onClick = {
                                if (target.subscreen != null) {
                                    viewModel.setActiveSubScreen(target.subscreen)
                                }
                                onNavigateToTab(target.targetPage)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.MintTeal),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(target.icon, contentDescription = null, tint = SoftTheme.DeepSlate)
                                Text(
                                    text = target.buttonText,
                                    color = SoftTheme.DeepSlate,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text("⚡", fontSize = 16.sp)
                            }
                        }
                    }
                }

                // Interactive Multi-Category Ready-To-Use Catalog
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📋 قائمة الاستشارات والخيارات الجاهزة:",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTheme.SoftPink,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "اضغطي على أي خيار للسؤال فوراً ⚡",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTheme.SoftGray,
                            fontSize = 9.sp
                        )
                    }

                    // 1. Categories Tab Row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        items(catalogCategories) { category ->
                            val isSelected = selectedCatalogCategory == category
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) SoftTheme.SoftPink else SoftTheme.DeepSlate)
                                    .clickable { selectedCatalogCategory = category }
                                    .border(
                                        1.dp, 
                                        if (isSelected) SoftTheme.LightPink else SoftTheme.SoftGray.copy(alpha = 0.2f), 
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = category,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) SoftTheme.DeepSlate else SoftTheme.TextWhite
                                )
                            }
                        }
                    }

                    // 2. Selected Category's Options List
                    val activeOptions = catalogSubItems[selectedCatalogCategory] ?: emptyList()
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        items(activeOptions) { (label, fullText) ->
                            AssistChip(
                                onClick = {
                                    if (!isTyping) {
                                        messages.add(Pair(fullText, true))
                                        isTyping = true
                                        coroutineScope.launch {
                                            handleMessageResponse(fullText)
                                        }
                                    }
                                },
                                label = { 
                                    Text(
                                        text = label, 
                                        color = SoftTheme.TextWhite, 
                                        fontSize = 10.sp, 
                                        fontWeight = FontWeight.Bold
                                    ) 
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = SoftTheme.DeepSlate,
                                    labelColor = SoftTheme.TextWhite
                                ),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.35f))
                            )
                        }
                    }
                }

                // Input Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputMessage,
                        onValueChange = { inputMessage = it },
                        placeholder = { Text("اكتبي شيئاً لـ $companionName...", color = SoftTheme.SoftGray) },
                        trailingIcon = {
                            IconButton(onClick = { showVirtualKeyboard = !showVirtualKeyboard }) {
                                Text(if (showVirtualKeyboard) "💬" else "⌨️", fontSize = 18.sp)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SoftTheme.TextWhite,
                            unfocusedTextColor = SoftTheme.TextWhite,
                            focusedBorderColor = SoftTheme.SoftPink,
                            unfocusedBorderColor = SoftTheme.DeepSlate,
                            focusedContainerColor = SoftTheme.DeepSlate,
                            unfocusedContainerColor = SoftTheme.DeepSlate
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("jouri_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        maxLines = 3
                    )

                    IconButton(
                        onClick = {
                            val userMsg = inputMessage.trim()
                            if (userMsg.isNotEmpty() && !isTyping) {
                                messages.add(Pair(userMsg, true))
                                inputMessage = ""
                                isTyping = true
                                
                                coroutineScope.launch {
                                    handleMessageResponse(userMsg)
                                }
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(SoftTheme.SoftPink, CircleShape)
                            .testTag("jouri_send_btn"),
                        enabled = inputMessage.trim().isNotEmpty() && !isTyping
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "إرسال",
                            tint = SoftTheme.TextWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Arabic Virtual Keyboard
                if (showVirtualKeyboard) {
                    val keyboardRows = remember {
                        listOf(
                            listOf("ض", "ص", "ث", "ق", "ف", "غ", "ع", "ه", "خ", "ح", "ج", "د"),
                            listOf("ش", "س", "ي", "ب", "ل", "ا", "ت", "ن", "م", "ك", "ط"),
                            listOf("ئ", "ء", "ؤ", "ر", "لا", "ة", "و", "ز", "ذ", "ظ", "أ", "إ")
                        )
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            keyboardRows.forEach { row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    row.forEach { char ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(38.dp)
                                                .background(SoftTheme.CardSlate, RoundedCornerShape(6.dp))
                                                .clickable { inputMessage += char }
                                                .padding(vertical = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = char,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = SoftTheme.TextWhite,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }
                            }
                            // Control Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Space
                                Box(
                                    modifier = Modifier
                                        .weight(2f)
                                        .height(38.dp)
                                        .background(SoftTheme.CardSlate, RoundedCornerShape(6.dp))
                                        .clickable { inputMessage += " " }
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "مسافة ␣",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SoftTheme.TextWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                // Backspace
                                Box(
                                    modifier = Modifier
                                        .weight(1.5f)
                                        .height(38.dp)
                                        .background(SoftTheme.SoftPink.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                        .clickable {
                                            if (inputMessage.isNotEmpty()) {
                                                inputMessage = inputMessage.dropLast(1)
                                            }
                                        }
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "مسح ⌫",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SoftTheme.SoftPink,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                // Clear
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .background(SoftTheme.SoftPink.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                        .clickable { inputMessage = "" }
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "حذف ❌",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SoftTheme.SoftPink.copy(alpha = 0.8f),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        }
    }
}

// --- Smart Nutrition Advisor Card ---
@Composable
fun SmartNutritionAdvisorCard(
    viewModel: WomanCompanionViewModel
) {
    val phase = viewModel.getCurrentCyclePhase()
    val periodLogs by viewModel.periodLogsState.collectAsStateWithLifecycle()
    val pregnancyState by viewModel.pregnancyState.collectAsStateWithLifecycle()
    
    var adviceText by remember { mutableStateOf<String?>(null) }
    var isLoadingAdvice by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val currentPhaseName = if (pregnancyState != null) "حامل" else phase.phaseArabic
    
    val defaultAdvice = remember(pregnancyState, phase.phaseName) {
        if (pregnancyState != null) {
            "🥑 **تغذية الحمل المبارك**:\n" +
            "• **حمض الفوليك (Folate)**: ضروري لنمو الجهاز العصبي (متوفر في السبانخ، البروكلي، والعدس).\n" +
            "• **الكالسيوم والحديد**: لتعزيز عظام طفلكِ وتفادي فقر الدم (ألبان، لحوم حمراء، تين مجفف).\n" +
            "• **أوميجا-3**: لتطور ذكاء الجنين وبصره (الأسماك الدهنية كالسلمون، الجوز، وبذور الكتان)."
        } else {
            when (phase.phaseName) {
                "Menstruation" -> {
                    "🩸 **مرحلة الطمث (الدورة)**:\n" +
                    "• **زيادة الحديد**: لتعويض الفقد الحاصل (تناولي اللحوم الحمراء، العدس، الشمندر).\n" +
                    "• **فيتامين C**: يعزز امتصاص الحديد (اعصري ليموناً فوق السلطة، تناولي البرتقال والفراولة).\n" +
                    "• **الماغنسيوم**: لتسكين التشنجات والمغص (الشوكولاتة الداكنة والموز والمكسرات)."
                }
                "Follicular" -> {
                    "🌱 **الطور الجريبي (الاستعداد للبويضة)**:\n" +
                    "• **دعم هرمون الاستروجين**: تناولي الحبوب الكاملة وبذور الكتان والأفوكادو.\n" +
                    "• **مضادات الأكسدة**: لحماية الخلايا البويضية (الخضار الورقية، الحمضيات، والبيض)."
                }
                "Ovulation" -> {
                    "✨ **مرحلة الإباضة (الخصوبة العالية)**:\n" +
                    "• **طاقة وخصوبة**: بروتينات خفيفة، دهون صحية، وتقليل الكربوهيدرات المكررة.\n" +
                    "• **فيتامينات دعم الهرمونات**: الفواكه الطازجة، الأسماك، البروكلي، والكينوا."
                }
                "Luteal" -> {
                    "🍂 **الطور الأصفري (ما قبل الدورة)**:\n" +
                    "• **تقليل الصوديوم**: لمنع احتباس السوائل المزعج والانتفاخ.\n" +
                    "• **التحكم بالرغبة في السكريات**: تناولي النشويات المعقدة كالشوفان والبطاطا الحلوة.\n" +
                    "• **الماغنسيوم وفيتامين B6**: لتهدئة المزاج وتقلباته (الموز، الحمص، واللوز)."
                }
                else -> {
                    "🥑 **تغذية صحية متوازنة**:\n" +
                    "• ركّزي على شرب ٣ لتر من الماء يومياً.\n" +
                    "• تجنبي الأطعمة المصنعة والزيوت المهدرجة."
                }
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🥑", fontSize = 24.sp)
                    Column {
                        Text(
                            text = "مستشار التغذية الذكي بالذكاء الاصطناعي",
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.TextWhite,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "مرحلتكِ الحالية: $currentPhaseName",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTheme.SoftPink
                        )
                    }
                }
            }

            AnimatedVisibility(visible = adviceText != null) {
                adviceText?.let { text ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SoftTheme.DeepSlate, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = SoftTheme.TextWhite,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            if (adviceText == null) {
                Text(
                    text = "سجّلي دورتكِ أو حملكِ واضغطي للحصول على نصائح وجبات متوازنة مولدة بالذكاء الاصطناعي لتلبية احتياجات جسمكِ الدقيقة في هذا الطور.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SoftTheme.SoftGray,
                    lineHeight = 16.sp
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SoftTheme.DeepSlate.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("💡 نصيحة الطور الافتراضية:", style = MaterialTheme.typography.bodySmall, color = SoftTheme.MintTeal, fontWeight = FontWeight.Bold)
                        Text(text = defaultAdvice, style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                    }
                }
            }

            Button(
                onClick = {
                    isLoadingAdvice = true
                    scope.launch {
                        val recentSymptoms = periodLogs.maxByOrNull { it.startDate }?.symptoms ?: "لا توجد"
                        val prompt = """
بصفتكِ أخصائية تغذية وصحة نسائية ذكية ومتعاطفة للغاية، اقترحي لي وجبات غذائية متوازنة مخصصة ونصائح صحية بناءً على المعطيات التالية:
1. مرحلتي الحالية: $currentPhaseName
2. الأعراض الأخيرة المسجلة: $recentSymptoms
اقترحي أفكار وجبات مغذية ومحددة (فطور، غداء، عشاء، ووجبة خفيفة) مصممة لتلبية احتياجات جسمي الآن (مثل: زيادة الحديد والماغنسيوم أثناء الدورة، أو البروتينات والألياف أثناء الإباضة، إلخ).
قدمي الإجابة باللغة العربية بأسلوب حميمي ورقيق جداً كأنكِ أختي الكبرى أو صديقتي المقربة (استخدمي عبارات مثل "يا غالية"، "يا عزيزتي")، واعرضي الوجبات في نقاط منسقة وجميلة وقصيرة مع تفاصيل المكونات والفوائد الصحية لكل وجبة.
                        """.trimIndent()
                        
                        val response = GeminiService.generateContent(prompt)
                        adviceText = response
                        isLoadingAdvice = false
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("generate_nutrition_btn"),
                enabled = !isLoadingAdvice
            ) {
                if (isLoadingAdvice) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = SoftTheme.TextWhite, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("جاري استشارة الذكاء الاصطناعي...")
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (adviceText != null) "تحديث النصائح بالذكاء الاصطناعي ✨" else "توليد وجبات ذكية مخصصة بالذكاء الاصطناعي ✨")
                }
            }

            // +++ أضيف بناءً على طلبك لدعم مكتبة نصائح جوري التفاعلية المحلية لسلامة الحمل والرشاقة +++
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = SoftTheme.SoftGray.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "📚 مكتبة جوري للنصائح والتوجيهات التفاعلية:",
                fontWeight = FontWeight.Bold,
                color = SoftTheme.TextWhite,
                style = MaterialTheme.typography.bodyMedium
            )

            var selectedAdviceTab by remember { mutableStateOf("الحمل 🤰") }
            var activeDetailedAdvice by remember { mutableStateOf<AdviceCardInfo?>(null) }

            // Category Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                adviceLibrary.keys.forEach { tabName ->
                    val isSel = selectedAdviceTab == tabName
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSel) SoftTheme.SoftPink else SoftTheme.DeepSlate)
                            .clickable { selectedAdviceTab = tabName }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = tabName,
                            color = if (isSel) SoftTheme.DeepSlate else SoftTheme.TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Cards under the selected category
            val adviceList = adviceLibrary[selectedAdviceTab] ?: emptyList()
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                adviceList.forEach { cardInfo ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SoftTheme.DeepSlate)
                            .clickable { activeDetailedAdvice = cardInfo }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(cardInfo.icon, fontSize = 24.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = cardInfo.title,
                                fontWeight = FontWeight.Bold,
                                color = SoftTheme.TextWhite,
                                fontSize = 12.sp
                            )
                            Text(
                                text = cardInfo.summary,
                                color = SoftTheme.SoftGray,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text("◀️", fontSize = 10.sp, color = SoftTheme.SoftPink)
                    }
                }
            }

            // Detailed Advice Dialog
            if (activeDetailedAdvice != null) {
                val advice = activeDetailedAdvice!!
                Dialog(onDismissRequest = { activeDetailedAdvice = null }) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(20.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(advice.icon, fontSize = 32.sp)
                                Text(
                                    text = advice.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftTheme.SoftPink,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Text(
                                text = advice.details,
                                style = MaterialTheme.typography.bodyMedium,
                                color = SoftTheme.TextWhite,
                                lineHeight = 20.sp
                            )

                            Text(
                                text = "💡 خطوات عملية وتوصيات سريعة للغالية:",
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftTheme.MintTeal,
                                fontWeight = FontWeight.Bold
                            )

                            // Interactive checkable tips
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                advice.tips.forEach { tip ->
                                    var isChecked by remember(tip) { mutableStateOf(false) }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(SoftTheme.DeepSlate)
                                            .clickable { isChecked = !isChecked }
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (isChecked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                            contentDescription = null,
                                            tint = if (isChecked) SoftTheme.MintTeal else SoftTheme.SoftGray,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = tip,
                                            color = if (isChecked) SoftTheme.SoftGray else SoftTheme.TextWhite,
                                            fontSize = 11.sp,
                                            lineHeight = 16.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = { activeDetailedAdvice = null },
                                colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("شكراً لكِ جوري 🌸")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiveNutrientSimulatorWidget(
    viewModel: WomanCompanionViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFood by remember { mutableStateOf<EgyptianFoodEntity?>(null) }
    var sugarSpoons by remember { mutableStateOf(0) }
    var useWholeMilk by remember { mutableStateOf(false) }
    
    val presetFoods = remember { EgyptianFoodRepository.presetFoods }
    val filteredFoods = remember(searchQuery) {
        if (searchQuery.trim().isEmpty()) {
            emptyList()
        } else {
            val normalizedQuery = EgyptianFoodRepository.normalizeText(searchQuery)
            presetFoods.filter { food ->
                EgyptianFoodRepository.normalizeText(food.name).contains(normalizedQuery) ||
                EgyptianFoodRepository.normalizeText(food.keywords).contains(normalizedQuery)
            }.take(5)
        }
    }

    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("🧪", fontSize = 24.sp)
                Column {
                    Text(
                        text = "محاكي المغذيات والترطيب التفاعلي",
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.TextWhite,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "اختر أي طعام أو شراب من الداتابيز الكبيرة لترى فائدته وماذا ستستفيدين منه بدقة بالعدّاد!",
                        color = SoftTheme.SoftGray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it 
                    if (it.isEmpty()) selectedFood = null
                },
                label = { Text("ابحثي عن وجبة أو مشروب (مثال: قهوة، ملوخية، كشري...)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SoftTheme.SoftPink) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { 
                            searchQuery = "" 
                            selectedFood = null
                            sugarSpoons = 0
                            useWholeMilk = false
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = null, tint = SoftTheme.SoftGray)
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SoftTheme.SoftPink,
                    unfocusedBorderColor = SoftTheme.SoftGray.copy(alpha = 0.5f),
                    focusedLabelColor = SoftTheme.SoftPink
                )
            )

            // Search results autocomplete list
            if (filteredFoods.isNotEmpty() && selectedFood == null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column {
                        filteredFoods.forEach { food ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedFood = food
                                        searchQuery = food.name
                                        sugarSpoons = 0
                                        useWholeMilk = false
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(food.name, fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite, style = MaterialTheme.typography.bodyMedium)
                                    Text("الحصة: ${food.servingSize} • ${food.calories} سعرة", color = SoftTheme.SoftGray, style = MaterialTheme.typography.labelSmall)
                                }
                                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = SoftTheme.SoftPink, modifier = Modifier.size(16.dp))
                            }
                            if (food != filteredFoods.last()) {
                                Divider(color = SoftTheme.CardSlate, thickness = 1.dp)
                            }
                        }
                    }
                }
            }

            // Default suggestion helper if nothing selected
            if (selectedFood == null) {
                Text("💡 اقتراحات شائعة للتجربة:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = SoftTheme.SoftPink)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val quickSuggestions = listOf(
                        "قهوة فرنساوي بملعقة سكر واحدة",
                        "قهوة تركي سادة",
                        "عسل أسود بالسمسم",
                        "طبق ملوخية",
                        "رز مصري مطبوخ",
                        "كوب ينسون دافئ"
                    )
                    quickSuggestions.forEach { suggestionName ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(SoftTheme.DeepSlate)
                                .clickable {
                                    val match = presetFoods.firstOrNull { it.name == suggestionName }
                                    if (match != null) {
                                        selectedFood = match
                                        searchQuery = match.name
                                        sugarSpoons = if (suggestionName.contains("ملعقة سكر واحدة")) 1 else 0
                                        useWholeMilk = false
                                    } else {
                                        searchQuery = suggestionName
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .border(1.dp, SoftTheme.SoftGray.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        ) {
                            Text(suggestionName, color = SoftTheme.TextWhite, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            // Selected Food details & custom interactive modifiers
            selectedFood?.let { food ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SoftTheme.DeepSlate)
                        .padding(16.dp)
                ) {
                    // Title and Serving Size
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(food.name, fontWeight = FontWeight.Bold, color = SoftTheme.SoftPink, style = MaterialTheme.typography.titleMedium)
                            Text("الحصة الأساسية: ${food.servingSize}", color = SoftTheme.SoftGray, style = MaterialTheme.typography.bodySmall)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SoftTheme.MintTeal.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(food.category.uppercase(), color = SoftTheme.MintTeal, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Divider(color = SoftTheme.CardSlate, thickness = 1.dp)

                    // Interactive modifiers section
                    Text("⚙️ تخصيص الكوب والوجبة بالإضافات:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)

                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Sugar Spoons Counter
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("ملاعق السكر الإضافية:", color = SoftTheme.TextWhite, style = MaterialTheme.typography.bodyMedium)
                                Text("+20 سعرة و +5 جم سكر لكل ملعقة", color = SoftTheme.SoftGray, style = MaterialTheme.typography.labelSmall)
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(
                                    onClick = { if (sugarSpoons > 0) sugarSpoons-- },
                                    modifier = Modifier.size(32.dp).background(SoftTheme.CardSlate, CircleShape)
                                ) {
                                    Text("-", color = SoftTheme.TextWhite, fontWeight = FontWeight.Bold)
                                }
                                Text("$sugarSpoons ملعقة", color = SoftTheme.SoftPink, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                IconButton(
                                    onClick = { sugarSpoons++ },
                                    modifier = Modifier.size(32.dp).background(SoftTheme.CardSlate, CircleShape)
                                ) {
                                    Text("+", color = SoftTheme.TextWhite, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Milk type modifier (if it's a hot beverage / contains milk)
                        if (food.category == "drink" || food.keywords.contains("شاي") || food.keywords.contains("قهوة") || food.keywords.contains("حليب") || food.keywords.contains("لبن")) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("ترقية الحليب لكامل الدسم؟", color = SoftTheme.TextWhite, style = MaterialTheme.typography.bodyMedium)
                                    Text("+60 سعرة، +3 جم بروتين، +3 جم دهون", color = SoftTheme.SoftGray, style = MaterialTheme.typography.labelSmall)
                                }
                                Switch(
                                    checked = useWholeMilk,
                                    onCheckedChange = { useWholeMilk = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = SoftTheme.SoftPink,
                                        checkedTrackColor = SoftTheme.LightPink
                                    )
                                )
                            }
                        }
                    }

                    // Dynamically Calculated nutritional values based on modifiers
                    val calculatedCalories = food.calories + (sugarSpoons * 20) + (if (useWholeMilk) 60 else 0)
                    val calculatedProtein = food.protein + (if (useWholeMilk) 3.0 else 0.0)
                    val calculatedCarbs = food.carbs + (sugarSpoons * 5.0) + (if (useWholeMilk) 4.0 else 0.0)
                    val calculatedFat = food.fat + (if (useWholeMilk) 3.0 else 0.0)
                    val calculatedSugar = food.sugarG + (sugarSpoons * 5.0) + (if (useWholeMilk) 4.0 else 0.0)
                    val calculatedFiber = food.fiberG
                    val calculatedWaterBenefit = food.waterBenefitMl

                    Divider(color = SoftTheme.CardSlate, thickness = 1.dp)

                    // Benefits highlight text
                    Text("💡 ماذا ستستفيدين؟ الفوائد الصحية المباشرة:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = SoftTheme.MintTeal)
                    Text(
                        text = if (food.healthBenefits.isNotEmpty()) food.healthBenefits else "غذاء مغذي يمد جسمكِ بالطاقة والعناصر الحيوية الضرورية.",
                        color = SoftTheme.TextWhite,
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 20.sp
                    )

                    Divider(color = SoftTheme.CardSlate, thickness = 1.dp)

                    // Nutrient breakdown grid
                    Text("📊 الميزان الغذائي المخصّص بعد التعديلات:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
                    
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Macros
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("🔥 السعرات الحرارية: $calculatedCalories سعرة", style = MaterialTheme.typography.labelLarge, color = SoftTheme.TextWhite, fontWeight = FontWeight.Bold)
                            if (calculatedWaterBenefit > 0) {
                                Text("💧 مياه الترطيب: +$calculatedWaterBenefit مل", style = MaterialTheme.typography.labelLarge, color = SoftTheme.MintTeal, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Quick mini grid for macros
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val macroGrid = listOf(
                                "بروتين" to "${"%.1f".format(calculatedProtein)}ج",
                                "نشويات" to "${"%.1f".format(calculatedCarbs)}ج",
                                "دهون" to "${"%.1f".format(calculatedFat)}ج",
                                "سكريات" to "${"%.1f".format(calculatedSugar)}ج",
                                "ألياف" to "${"%.1f".format(calculatedFiber)}ج"
                            )
                            macroGrid.forEach { (lbl, valStr) ->
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SoftTheme.CardSlate)
                                        .padding(vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(lbl, color = SoftTheme.SoftGray, style = MaterialTheme.typography.labelSmall)
                                    Text(valStr, color = SoftTheme.TextWhite, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }

                        // Micros checklist
                        val microsList = mutableListOf<String>()
                        if (food.ironMg > 0) microsList.add("🧲 حديد: ${food.ironMg} ملجم")
                        if (food.calciumMg > 0) microsList.add("🦴 كالسيوم: ${food.calciumMg} ملجم")
                        if (food.vitaminB_Mg > 0) microsList.add("🧠 فوليك: ${"%.0f".format(food.vitaminB_Mg * 100.0)} مكجم")
                        if (food.potassiumMg > 0) microsList.add("💓 بوتاسيوم: ${food.potassiumMg} / صوديوم: ${food.sodiumMg} ملجم")
                        if (food.magnesiumMg > 0) microsList.add("🌿 ماغنسيوم: ${food.magnesiumMg} ملجم")

                        if (microsList.isNotEmpty()) {
                            Text(
                                text = "✨ الفيتامينات والمعادن الدقيقة المتوفرة: " + microsList.joinToString(" • "),
                                style = MaterialTheme.typography.labelSmall,
                                color = SoftTheme.SoftTeal
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Button to add this food directly
                    Button(
                        onClick = {
                            viewModel.addNutritionMeal(
                                mealType = if (food.category == "drink") "مشروب" else "وجبة",
                                description = "${food.name} (معدّل: $sugarSpoons ملعقة سكر${if (useWholeMilk) " + حليب كامل" else ""})",
                                calories = calculatedCalories,
                                iron = food.ironMg,
                                folate = food.vitaminB_Mg * 100.0,
                                calcium = food.calciumMg,
                                omega3 = food.vitaminD_Mcg * 0.1,
                                protein = calculatedProtein,
                                carbs = calculatedCarbs,
                                fat = calculatedFat,
                                sugar = calculatedSugar,
                                fiber = calculatedFiber,
                                waterBenefit = calculatedWaterBenefit
                            )
                            android.widget.Toast.makeText(context, "تم تسجيل ${food.name} والإضافات بنجاح! 🎉", android.widget.Toast.LENGTH_SHORT).show()
                            
                            // reset fields
                            searchQuery = ""
                            selectedFood = null
                            sugarSpoons = 0
                            useWholeMilk = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("سجلي هذا الكوب / الوجبة الآن! ✍️", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

// --- Native Analytics Dashboard ---
@Composable
fun D3NativeDashboard(
    periodLogs: List<PeriodLog>,
    stats: CycleStats
) {
    var selectedChartTab by remember { mutableStateOf(0) } // 0 = Regularity, 1 = Symptoms/Pain

    val cycleLengths = remember(periodLogs) {
        val sorted = periodLogs.sortedBy { it.startDate }
        val list = mutableListOf<Int>()
        for (i in 1 until sorted.size) {
            val diffDays = (sorted[i].startDate - sorted[i-1].startDate) / (24 * 60 * 60 * 1000)
            if (diffDays in 15..50) {
                list.add(diffDays.toInt())
            }
        }
        list
    }

    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("📊", fontSize = 24.sp)
                    Column {
                        Text(
                            text = "لوحة التحليلات والرسوم البيانية المتقدمة",
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.TextWhite,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "تتبع فترات الدورة والمزاج والانتظام تلقائياً",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTheme.SoftGray
                        )
                    }
                }
                Text(
                    text = if (isExpanded) "إخفاء 🔼" else "عرض الرسوم 🔽",
                    color = SoftTheme.SoftPink,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }

            if (isExpanded) {
                // Tab toggler
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SoftTheme.DeepSlate, RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Button(
                        onClick = { selectedChartTab = 0 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedChartTab == 0) SoftTheme.SoftPink else Color.Transparent,
                            contentColor = if (selectedChartTab == 0) SoftTheme.TextWhite else SoftTheme.SoftGray
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text("انتظام الدورة 🩸", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Button(
                        onClick = { selectedChartTab = 1 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedChartTab == 1) SoftTheme.SoftPink else Color.Transparent,
                            contentColor = if (selectedChartTab == 1) SoftTheme.TextWhite else SoftTheme.SoftGray
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text("حدة الأعراض والألم 🧠", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                if (selectedChartTab == 0) {
                    CycleRegularityChart(cycleLengths = cycleLengths, avgCycleLength = stats.averageCycleLength)
                } else {
                    MoodSymptomChart(periodLogs = periodLogs)
                }
            }
        }
    }
}

@Composable
fun CycleRegularityChart(cycleLengths: List<Int>, avgCycleLength: Int) {
    val displayLengths = if (cycleLengths.isEmpty()) {
        listOf(28, 27, 29, 28) // Placeholder mock data
    } else {
        cycleLengths.takeLast(4)
    }
    val isPlaceholder = cycleLengths.isEmpty()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(SoftTheme.DeepSlate, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val paddingLeft = 40.dp.toPx()
            val paddingBottom = 24.dp.toPx()
            val paddingTop = 16.dp.toPx()
            val paddingRight = 16.dp.toPx()

            val chartWidth = width - paddingLeft - paddingRight
            val chartHeight = height - paddingTop - paddingBottom

            val maxVal = 40f
            val stepY = maxVal / 4f

            for (i in 0..4) {
                val yVal = i * stepY
                val yPos = height - paddingBottom - (yVal / maxVal) * chartHeight
                
                drawLine(
                    color = SoftTheme.SoftGray.copy(alpha = 0.15f),
                    start = Offset(paddingLeft, yPos),
                    end = Offset(width - paddingRight, yPos),
                    strokeWidth = 1.dp.toPx()
                )
            }

            val barCount = displayLengths.size
            val barWidth = (chartWidth / barCount) * 0.45f
            val spacing = (chartWidth / barCount)

            for (idx in displayLengths.indices) {
                val days = displayLengths[idx]
                val barHeight = (days.toFloat() / maxVal) * chartHeight
                val xPos = paddingLeft + (idx * spacing) + (spacing - barWidth) / 2f
                val yPos = height - paddingBottom - barHeight

                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(SoftTheme.SoftPink, SoftTheme.DeepPink)
                    ),
                    topLeft = Offset(xPos, yPos),
                    size = Size(barWidth, barHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )
                
                if (isPlaceholder) {
                    drawRect(
                        color = SoftTheme.DeepSlate.copy(alpha = 0.4f),
                        topLeft = Offset(xPos, yPos),
                        size = Size(barWidth, barHeight)
                    )
                }
            }

            val avgY = height - paddingBottom - (avgCycleLength.toFloat() / maxVal) * chartHeight
            drawLine(
                color = SoftTheme.MintTeal,
                start = Offset(paddingLeft, avgY),
                end = Offset(width - paddingRight, avgY),
                strokeWidth = 2.dp.toPx(),
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(start = 40.dp, end = 16.dp, bottom = 2.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            for (idx in displayLengths.indices) {
                Text(
                    text = if (isPlaceholder) "دورة م ${idx + 1}" else "دورة ${idx + 1}",
                    color = SoftTheme.SoftGray,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(start = 40.dp, end = 16.dp, top = 20.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            for (idx in displayLengths.indices) {
                val days = displayLengths[idx]
                Text(
                    text = "$days يوم",
                    color = if (isPlaceholder) SoftTheme.SoftGray.copy(alpha = 0.7f) else SoftTheme.TextWhite,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }

        Text(
            text = "المعدل: $avgCycleLength يوم",
            color = SoftTheme.MintTeal,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 44.dp, top = 4.dp),
            fontSize = 10.sp
        )

        if (isPlaceholder) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "سجّلي دورتين أو أكثر لعرض انتظام دورتكِ الشخصي 📊",
                    color = SoftTheme.LightPink,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun MoodSymptomChart(periodLogs: List<PeriodLog>) {
    val displayPainLevels = if (periodLogs.isEmpty()) {
        listOf(6, 4, 7, 3, 5) // Placeholder mock data
    } else {
        periodLogs.takeLast(5).map { it.painLevel }
    }
    val isPlaceholder = periodLogs.isEmpty()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(SoftTheme.DeepSlate, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val paddingLeft = 40.dp.toPx()
            val paddingBottom = 24.dp.toPx()
            val paddingTop = 16.dp.toPx()
            val paddingRight = 16.dp.toPx()

            val chartWidth = width - paddingLeft - paddingRight
            val chartHeight = height - paddingTop - paddingBottom

            val maxVal = 10f

            for (i in 0..4) {
                val yVal = i * 2.5f
                val yPos = height - paddingBottom - (yVal / maxVal) * chartHeight
                drawLine(
                    color = SoftTheme.SoftGray.copy(alpha = 0.15f),
                    start = Offset(paddingLeft, yPos),
                    end = Offset(width - paddingRight, yPos),
                    strokeWidth = 1.dp.toPx()
                )
            }

            val pointCount = displayPainLevels.size
            val spacing = chartWidth / (pointCount - 1).coerceAtLeast(1)
            val points = displayPainLevels.indices.map { idx ->
                val pain = displayPainLevels[idx]
                val x = paddingLeft + idx * spacing
                val y = height - paddingBottom - (pain.toFloat() / maxVal) * chartHeight
                Offset(x, y)
            }

            val path = androidx.compose.ui.graphics.Path()
            if (points.isNotEmpty()) {
                path.moveTo(points[0].x, points[0].y)
                for (i in 0 until points.size - 1) {
                    val p0 = points[i]
                    val p1 = points[i + 1]
                    val controlX1 = p0.x + (p1.x - p0.x) / 2f
                    val controlY1 = p0.y
                    val controlX2 = p0.x + (p1.x - p0.x) / 2f
                    val controlY2 = p1.y
                    
                    path.cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
                }
                
                drawPath(
                    path = path,
                    color = SoftTheme.SoftTeal,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                val fillPath = androidx.compose.ui.graphics.Path().apply {
                    addPath(path)
                    lineTo(points.last().x, height - paddingBottom)
                    lineTo(points.first().x, height - paddingBottom)
                    close()
                }
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(SoftTheme.SoftTeal.copy(alpha = 0.3f), Color.Transparent)
                    )
                )

                points.forEach { pt ->
                    drawCircle(
                        color = SoftTheme.MintTeal,
                        radius = 4.dp.toPx(),
                        center = pt
                    )
                    drawCircle(
                        color = SoftTheme.MintTeal.copy(alpha = 0.4f),
                        radius = 8.dp.toPx(),
                        center = pt,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(start = 40.dp, end = 16.dp, bottom = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            for (idx in displayPainLevels.indices) {
                Text(
                    text = if (isPlaceholder) "تسجيل ${idx + 1}" else "ت ${idx + 1}",
                    color = SoftTheme.SoftGray,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(start = 40.dp, end = 16.dp, top = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            for (idx in displayPainLevels.indices) {
                val score = displayPainLevels[idx]
                Text(
                    text = "$score/10",
                    color = if (isPlaceholder) SoftTheme.SoftGray.copy(alpha = 0.7f) else SoftTheme.SoftTeal,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }

        Text(
            text = "مؤشر حدة الأعراض والألم",
            color = SoftTheme.SoftTeal,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 44.dp, top = 4.dp),
            fontSize = 10.sp
        )

        if (isPlaceholder) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "سجّلي الأعراض في دورتكِ لعرض تقلباتكِ الصحية والمزاجية 🧠",
                    color = SoftTheme.SoftTeal,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// --- Dynamic, Highly Interactive Jouri Wellness Notification Card ---
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
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(SoftTheme.SoftPink.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🌸", fontSize = 18.sp)
                    }
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

// ==========================================
// 1. محلل النوم الذكي (Smart Sleep Analyzer)
// ==========================================
@Composable
fun SleepAnalyzerScreen(viewModel: WomanCompanionViewModel) {
    val sleepLogs by viewModel.allSleepLogsState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    // Sleep recording states
    var startHoursAgo by remember { mutableStateOf(8f) } // Duration since start of sleep
    var sleepDurationHours by remember { mutableStateOf(8f) }
    var qualityScore by remember { mutableStateOf(80f) }
    var deepSleepPercent by remember { mutableStateOf(25f) }
    var lightSleepPercent by remember { mutableStateOf(55f) }
    var remSleepPercent by remember { mutableStateOf(20f) }
    var awakeningsCount by remember { mutableStateOf(1f) }
    var sleepNotes by remember { mutableStateOf("") }

    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "محلل ومراقب النوم الذكي 🌙💤",
                        style = MaterialTheme.typography.titleLarge,
                        color = SoftTheme.SoftPink,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "يتأثر نمط نومك بالتغيرات الهرمونية والجسدية خلال فترة الحمل والنفاس. يساعدك المحلل الذكي على تتبع صحة نومك وجرد جودته للحفاظ على نشاطك وصحة جنينك.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoftTheme.SoftGray
                    )
                    
                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                        modifier = Modifier.fillMaxWidth().testTag("add_sleep_log_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = SoftTheme.TextWhite)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تسجيل نوم الليلة الماضية ✍️", color = SoftTheme.TextWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Pregnancy sleep safe tips
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("💡 نصيحة النوم الصحي للحوامل والنفاس:", fontWeight = FontWeight.Bold, color = SoftTheme.SoftTeal)
                    Text(
                        text = "• يُنصح بشدة بالنوم على الجانب الأيسر (SOS) لتحسين تدفق الدم والتروية للجنين والرحم والكلية.\n" +
                               "• استخدمي وسائد مخصصة للحمل لتسديد الدعم لظهرك والبطن.\n" +
                               "• تجنبي شرب الكافيين والمنبهات قبل موعد النوم بـ 6 ساعات على الأقل.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTheme.SoftGray
                    )
                }
            }
        }

        if (sleepLogs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("😴", fontSize = 48.sp)
                        Text("لا يوجد سجلات نوم مسجلة بعد.", color = SoftTheme.SoftGray, fontWeight = FontWeight.Bold)
                        Text("سجلي نومك لتبدأ جوري في تحليل صحتك الحيوية.", color = SoftTheme.SoftGray, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        } else {
            // General Stats Card
            item {
                val avgQuality = sleepLogs.map { it.qualityScore }.average().toInt()
                val avgDuration = sleepLogs.map { (it.endTime - it.startTime) / (1000f * 60 * 60) }.average()
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("معدل الجودة", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                            Text("$avgQuality%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SoftTheme.SoftPink)
                        }
                        Divider(modifier = Modifier.height(40.dp).width(1.dp), color = SoftTheme.SoftGray.copy(alpha = 0.3f))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("معدل الساعات", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                            Text(String.format("%.1f س", avgDuration), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SoftTheme.SoftTeal)
                        }
                        Divider(modifier = Modifier.height(40.dp).width(1.dp), color = SoftTheme.SoftGray.copy(alpha = 0.3f))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("إجمالي الليالي", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                            Text("${sleepLogs.size}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
                        }
                    }
                }
            }

            item {
                Text("سجل الليالي السابقة:", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
            }

            items(sleepLogs) { log ->
                val durationMs = log.endTime - log.startTime
                val durationHours = durationMs / (1000f * 60 * 60)
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = formatGregorianDate(log.date),
                                    fontWeight = FontWeight.Bold,
                                    color = SoftTheme.TextWhite
                                )
                                Text(
                                    text = "${formatTime(log.startTime)} - ${formatTime(log.endTime)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SoftTheme.SoftGray
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (log.qualityScore >= 80) SoftTheme.SoftTeal.copy(alpha = 0.15f)
                                        else SoftTheme.SoftPink.copy(alpha = 0.15f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "جودة ${log.qualityScore}%",
                                    color = if (log.qualityScore >= 80) SoftTheme.SoftTeal else SoftTheme.SoftPink,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // Duration bar
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = SoftTheme.SoftPink, modifier = Modifier.size(16.dp))
                            Text(
                                text = String.format("مدة النوم الكلية: %.1f ساعة", durationHours),
                                style = MaterialTheme.typography.bodyMedium,
                                color = SoftTheme.TextWhite
                            )
                        }

                        // Deep/Light Sleep distribution if entered
                        if (log.deepSleepMinutes > 0 || log.lightSleepMinutes > 0) {
                            val totalMin = log.deepSleepMinutes + log.lightSleepMinutes + log.remSleepMinutes
                            if (totalMin > 0) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "عميق: ${log.deepSleepMinutes}د ( ${(log.deepSleepMinutes * 100 / totalMin)}%)",
                                            fontSize = 11.sp,
                                            color = SoftTheme.SoftTeal
                                        )
                                        Text(
                                            text = "خفيف: ${log.lightSleepMinutes}د ( ${(log.lightSleepMinutes * 100 / totalMin)}%)",
                                            fontSize = 11.sp,
                                            color = SoftTheme.SoftGray
                                        )
                                        if (log.remSleepMinutes > 0) {
                                            Text(
                                                text = "حركة سريعة: ${log.remSleepMinutes}د",
                                                fontSize = 11.sp,
                                                color = SoftTheme.SoftPink
                                            )
                                        }
                                    }
                                    
                                    // Custom visual distribution bar
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .background(SoftTheme.DeepSlate, RoundedCornerShape(4.dp))
                                    ) {
                                        val deepWeight = log.deepSleepMinutes.toFloat() / totalMin
                                        val lightWeight = log.lightSleepMinutes.toFloat() / totalMin
                                        val remWeight = log.remSleepMinutes.toFloat() / totalMin
                                        
                                        if (deepWeight > 0) {
                                            Box(modifier = Modifier.weight(deepWeight).fillMaxHeight().background(SoftTheme.SoftTeal, RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp)))
                                        }
                                        if (lightWeight > 0) {
                                            Box(modifier = Modifier.weight(lightWeight).fillMaxHeight().background(SoftTheme.SoftGray))
                                        }
                                        if (remWeight > 0) {
                                            Box(modifier = Modifier.weight(remWeight).fillMaxHeight().background(SoftTheme.SoftPink, RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)))
                                        }
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("الاستيقاظ: ", fontSize = 11.sp, color = SoftTheme.SoftGray)
                                Text("${log.awakeningsCount} مرات", fontSize = 11.sp, color = SoftTheme.TextWhite, fontWeight = FontWeight.Bold)
                            }
                            
                            IconButton(
                                onClick = { viewModel.deleteSleepLog(log) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = SoftTheme.SoftPink.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                            }
                        }

                        if (!log.notes.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SoftTheme.DeepSlate, RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Text(text = "✍️ ملاحظات: ${log.notes}", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("تسجيل نوم الليلة الماضية 💤", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("كم ساعة نمتِ الليلة الماضية؟", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Slider(
                            value = sleepDurationHours,
                            onValueChange = { sleepDurationHours = it },
                            valueRange = 1f..16f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(thumbColor = SoftTheme.SoftPink, activeTrackColor = SoftTheme.SoftPink)
                        )
                        Text(text = String.format("%.1f س", sleepDurationHours), fontWeight = FontWeight.Bold, color = SoftTheme.SoftTeal)
                    }

                    Text("تقييم جودة النوم وعمقه:", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Slider(
                            value = qualityScore,
                            onValueChange = { qualityScore = it },
                            valueRange = 10f..100f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(thumbColor = SoftTheme.SoftTeal, activeTrackColor = SoftTheme.SoftTeal)
                        )
                        Text(text = "${qualityScore.toInt()}%", fontWeight = FontWeight.Bold, color = SoftTheme.SoftPink)
                    }

                    Text("توزيع النوم (اختياري بالدقائق):", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
                    
                    // Deep sleep slider
                    Column {
                        Text("النوم العميق (موصى به > ٢٠%): ${deepSleepPercent.toInt()} دقيقة", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                        Slider(
                            value = deepSleepPercent,
                            onValueChange = { deepSleepPercent = it },
                            valueRange = 0f..240f,
                            colors = SliderDefaults.colors(thumbColor = SoftTheme.SoftTeal, activeTrackColor = SoftTheme.SoftTeal)
                        )
                    }

                    // Light sleep slider
                    Column {
                        Text("النوم الخفيف: ${lightSleepPercent.toInt()} دقيقة", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                        Slider(
                            value = lightSleepPercent,
                            onValueChange = { lightSleepPercent = it },
                            valueRange = 0f..480f,
                            colors = SliderDefaults.colors(thumbColor = SoftTheme.SoftGray, activeTrackColor = SoftTheme.SoftGray)
                        )
                    }

                    // REM sleep
                    Column {
                        Text("نوم حركة العين السريعة (الأحلام): ${remSleepPercent.toInt()} دقيقة", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                        Slider(
                            value = remSleepPercent,
                            onValueChange = { remSleepPercent = it },
                            valueRange = 0f..180f,
                            colors = SliderDefaults.colors(thumbColor = SoftTheme.SoftPink, activeTrackColor = SoftTheme.SoftPink)
                        )
                    }

                    Column {
                        Text("عدد مرات الاستيقاظ: ${awakeningsCount.toInt()} مرات", style = MaterialTheme.typography.bodySmall, color = SoftTheme.TextWhite)
                        Slider(
                            value = awakeningsCount,
                            onValueChange = { awakeningsCount = it },
                            valueRange = 0f..10f,
                            colors = SliderDefaults.colors(thumbColor = SoftTheme.SoftPink, activeTrackColor = SoftTheme.SoftPink)
                        )
                    }

                    OutlinedTextField(
                        value = sleepNotes,
                        onValueChange = { sleepNotes = it },
                        label = { Text("ملاحظات النوم (مثال: شربت يانسون دافئ، قلق خفيف)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val durationMs = (sleepDurationHours * 60 * 60 * 1000).toLong()
                        val endTime = System.currentTimeMillis()
                        val startTime = endTime - durationMs
                        viewModel.addSleepLog(
                            startTime = startTime,
                            endTime = endTime,
                            qualityScore = qualityScore.toInt(),
                            deepSleepMin = deepSleepPercent.toInt(),
                            lightSleepMin = lightSleepPercent.toInt(),
                            remSleepMin = remSleepPercent.toInt(),
                            awakenings = awakeningsCount.toInt(),
                            notes = sleepNotes.ifBlank { null }
                        )
                        showAddDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink)
                ) {
                    Text("حفظ السجل 💾", color = SoftTheme.TextWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("إلغاء", color = SoftTheme.SoftPink)
                }
            }
        )
    }
}

// ==========================================
// 2. ربط ومشاركة الشريك (Companion Sync Link)
// ==========================================
@Composable
fun PartnerSyncScreen(viewModel: WomanCompanionViewModel) {
    val messages by viewModel.allPartnerMessagesState.collectAsStateWithLifecycle()
    val settings by viewModel.appLockSettingsState.collectAsStateWithLifecycle()
    val companionName = settings?.companionName ?: "جوري"

    var showSendDialog by remember { mutableStateOf(false) }
    var inputMessageText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Support") }
    
    // Seeded coupling code (completely local and secure)
    val syncCode = remember { "JO-983-PR" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "رابط الرفيق ومشاركة الشريك 🔗❤️",
                        style = MaterialTheme.typography.titleLarge,
                        color = SoftTheme.SoftPink,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "يتيح هذا الرابط لزوجك أو أفراد عائلتك المقربين بمتابعة وضعك الصحي وعلامات الخطر، وإرسال رسائل التذكير بالماء والأدوية، والتشجيع والاطمئنان في الوقت الحقيقي وبخصوصية تامة.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoftTheme.SoftGray
                    )
                }
            }
        }

        // Coupling Status Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, SoftTheme.SoftTeal.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier.size(12.dp).background(SoftTheme.SoftTeal, CircleShape)
                            )
                            Text("رابط المزامنة نشط ومؤمن 🟢", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
                        }
                        Text("محلي ومشفر", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                    }

                    Divider(color = SoftTheme.SoftGray.copy(alpha = 0.15f))

                    Column {
                        Text("رمز المزامنة الفريد لزوجك/رفيقك:", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = syncCode,
                                style = MaterialTheme.typography.titleMedium,
                                color = SoftTheme.SoftTeal,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.background(SoftTheme.CardSlate, RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                            Text("شاركي هذا الرمز لربطه", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftPink, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Simulate Action Card for testing
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("💡 تظاهر بمحاكاة شريكك:", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
                    Text(
                        "هذه لوحة تحكم سريعة لإرسال رسالة من هاتف الشريك لتجربة نظام الإشعارات والتنبيه المحلي فوريًا.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTheme.SoftGray
                    )
                    Button(
                        onClick = { showSendDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.DeepSlate),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("إرسال رسالة دعم من الشريك 📲", color = SoftTheme.SoftTeal, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Text("الرسائل والتنبيهات المتبادلة:", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
        }

        if (messages.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("💬", fontSize = 48.sp)
                        Text("لا توجد رسائل دعم من شريكك بعد.", color = SoftTheme.SoftGray, fontWeight = FontWeight.Bold)
                        Text("أي رسائل يرسلها الرفيق ستظهر هنا لتشجيعك ومتابعة نشاطك.", color = SoftTheme.SoftGray, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                    }
                }
            }
        } else {
            items(messages) { msg ->
                val emoji = when (msg.category) {
                    "Support" -> "❤️"
                    "Alert" -> "🚨"
                    "WaterReminder" -> "💧"
                    "KickCheck" -> "👶"
                    else -> "💌"
                }

                val titleText = when (msg.category) {
                    "Support" -> "رسالة تشجيع ودعم"
                    "Alert" -> "تنبيه هام ومستعجل"
                    "WaterReminder" -> "تذكير بشرب الماء"
                    "KickCheck" -> "تذكير بعد حركة الجنين"
                    else -> "مزامنة الشريك"
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (msg.isRead) SoftTheme.CardSlate else SoftTheme.CardSlate.copy(alpha = 0.9f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = if (!msg.isRead) BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.4f)) else null
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(emoji, fontSize = 18.sp)
                                Text(
                                    text = "$titleText من ${msg.senderName}",
                                    fontWeight = FontWeight.Bold,
                                    color = if (msg.category == "Alert") SoftTheme.SoftPink else SoftTheme.TextWhite
                                )
                            }
                            Text(
                                text = formatGregorianDate(msg.timestamp) + " " + formatTime(msg.timestamp),
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftTheme.SoftGray
                            )
                        }

                        Text(
                            text = msg.messageText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = SoftTheme.TextWhite
                        )

                        if (!msg.isRead) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = { viewModel.markPartnerMessageAsRead(msg.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.DeepSlate),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("تعليم كمقروءة ✔️", color = SoftTheme.SoftTeal, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSendDialog) {
        AlertDialog(
            onDismissRequest = { showSendDialog = false },
            title = { Text("إرسال رسالة كشريك 📱", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("اختر نوع الرسالة والتذكير الموجه لحبيبتك:", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                    
                    val categories = listOf(
                        Pair("Support", "❤️ رسالة حب ودعم"),
                        Pair("WaterReminder", "💧 تذكير بشرب الماء"),
                        Pair("KickCheck", "👶 اطمئنان على حركة الجنين"),
                        Pair("Alert", "🚨 تنبيه صحي عاجل")
                    )

                    categories.forEach { (cat, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedCategory = cat }
                                .background(
                                    if (selectedCategory == cat) SoftTheme.DeepSlate else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                colors = RadioButtonDefaults.colors(selectedColor = SoftTheme.SoftPink)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label, color = SoftTheme.TextWhite)
                        }
                    }

                    OutlinedTextField(
                        value = inputMessageText,
                        onValueChange = { inputMessageText = it },
                        label = { Text("اكتب نص الرسالة هنا...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val sender = "شريكك الغالي"
                        val defaultText = when (selectedCategory) {
                            "Support" -> "أنا فخور بكِ وبقوتكِ اليوم. خذي قسطاً من الراحة يا حبيبتي ❤️"
                            "WaterReminder" -> "هل شربتِ كمية كافية من الماء اليوم؟ تذكري شرب كوب الآن لصحتك وصحة طفلنا 💧"
                            "KickCheck" -> "طمئنيني عن بطلنا الصغير اليوم؟ هل ركل كالعادة؟ 👶"
                            "Alert" -> "لقد لاحظت تعبكِ اليوم، يرجى عدم بذل أي مجهود إضافي والاستلقاء فوراً 🚨"
                            else -> "مساندة لكِ"
                        }
                        
                        viewModel.addPartnerMessage(
                            senderName = sender,
                            messageText = inputMessageText.ifBlank { defaultText },
                            category = selectedCategory
                        )
                        inputMessageText = ""
                        showSendDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink)
                ) {
                    Text("إرسال فوري", color = SoftTheme.TextWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSendDialog = false }) {
                    Text("إلغاء", color = SoftTheme.SoftPink)
                }
            }
        )
    }
}

// ==========================================
// 3. الصيدلية المنزلية المتقدمة (Advanced Home Pharmacy)
// ==========================================
@Composable
fun HomePharmacyScreen(viewModel: WomanCompanionViewModel) {
    val medications by viewModel.allMedicationsState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    // Medication adding state
    var medName by remember { mutableStateOf("") }
    var medDosage by remember { mutableStateOf("") }
    var medTimesPerDay by remember { mutableStateOf("1") }
    var medPrescby by remember { mutableStateOf("") }
    var medNotes by remember { mutableStateOf("") }
    
    // Advanced pharmacy state
    var expiryDaysOffset by remember { mutableStateOf(90f) } // default 90 days in future
    var totalQty by remember { mutableStateOf("30") }
    var remainingQty by remember { mutableStateOf("30") }
    var safetyStatus by remember { mutableStateOf("Safe") } // Safe, Caution, AskDoctor

    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "الصيدلية المنزلية المتقدمة وخزانة الأدوية 💊📦",
                        style = MaterialTheme.typography.titleLarge,
                        color = SoftTheme.SoftPink,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "جرد كامل لأدويتك ومكملاتك الغذائية خلال فترة الحمل والنفاس. يراقب التطبيق تاريخ الصلاحية، وكمية العبوات المتبقية، مع عرض كاشف ذكي لأمان استخدام الأدوية مع الحمل.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoftTheme.SoftGray
                    )
                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                        modifier = Modifier.fillMaxWidth().testTag("add_pharmacy_med_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = SoftTheme.TextWhite)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("إضافة دواء أو مكمل للخزانة 💊", color = SoftTheme.TextWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (medications.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("📦", fontSize = 48.sp)
                        Text("الخزانة الدوائية فارغة الآن.", color = SoftTheme.SoftGray, fontWeight = FontWeight.Bold)
                        Text("أضيفي الفيتامينات مثل الحديد أو الفوليك أسيد لمراقبتها.", color = SoftTheme.SoftGray, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        } else {
            item {
                Text("الأدوية والفيتامينات المخزنة حالياً:", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
            }

            items(medications) { med ->
                val isLowStock = med.remainingQuantity < 5
                val isExpired = med.expiryDate?.let { it < System.currentTimeMillis() } ?: false
                val isCloseToExpiry = med.expiryDate?.let { (it - System.currentTimeMillis()) < 30L * 24 * 60 * 60 * 1000 } ?: false

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(med.name, fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite, fontSize = 16.sp)
                                if (!med.dosage.isNullOrBlank()) {
                                    Text("الجرعة: ${med.dosage}", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftTeal)
                                }
                            }

                            // Safety Warning Tag
                            val safetyColor = when (med.safetyWarning) {
                                "Safe" -> SoftTheme.SoftTeal
                                "Caution" -> Color(0xFFFFA726) // Amber
                                "AskDoctor" -> SoftTheme.SoftPink
                                else -> SoftTheme.SoftGray
                            }

                            val safetyText = when (med.safetyWarning) {
                                "Safe" -> "آمن مع الحمل 🍏"
                                "Caution" -> "بحذر واستشارة ⚠️"
                                "AskDoctor" -> "غير آمن / راجعي الطبيب 🚨"
                                else -> "غير مصنف"
                            }

                            Box(
                                modifier = Modifier
                                    .background(safetyColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(safetyText, color = safetyColor, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }

                        Divider(color = SoftTheme.SoftGray.copy(alpha = 0.15f))

                        // Stock counts
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "المخزون المتبقي بالخزانة:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SoftTheme.SoftGray
                                )
                                Text(
                                    text = "${med.remainingQuantity} من أصل ${med.totalQuantity} وحدة",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLowStock) SoftTheme.SoftPink else SoftTheme.TextWhite,
                                    fontSize = 12.sp
                                )
                            }

                            // Visual Stock Progress
                            if (med.totalQuantity > 0) {
                                val fraction = med.remainingQuantity.toFloat() / med.totalQuantity.toFloat()
                                LinearProgressIndicator(
                                    progress = fraction.coerceIn(0f, 1f),
                                    modifier = Modifier.fillMaxWidth().height(6.dp),
                                    color = if (isLowStock) SoftTheme.SoftPink else SoftTheme.SoftTeal,
                                    trackColor = SoftTheme.DeepSlate
                                )
                            }
                        }

                        // Expiry and Alerts
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                if (med.expiryDate != null) {
                                    val formattedExpiry = formatGregorianDate(med.expiryDate)
                                    val textCol = when {
                                        isExpired -> SoftTheme.SoftPink
                                        isCloseToExpiry -> Color(0xFFFFA726)
                                        else -> SoftTheme.SoftTeal
                                    }
                                    val textLabel = when {
                                        isExpired -> "منتهي الصلاحية ❌"
                                        isCloseToExpiry -> "صلاحية حرجة (أقل من شهر) ⚠️"
                                        else -> "صالح حتى: $formattedExpiry 🍏"
                                    }
                                    Text(textLabel, style = MaterialTheme.typography.bodySmall, color = textCol, fontWeight = FontWeight.Bold)
                                } else {
                                    Text("تاريخ الصلاحية: غير مسجل", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                                }
                            }

                            if (isLowStock) {
                                Box(
                                    modifier = Modifier
                                        .background(SoftTheme.SoftPink.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("مخزون حرج! 🚨", color = SoftTheme.SoftPink, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Take Dose Action
                            Button(
                                onClick = {
                                    if (med.remainingQuantity > 0) {
                                        viewModel.decrementMedicationStock(med, 1)
                                    } else {
                                        android.widget.Toast.makeText(context, "المخزون نفد تماماً! يرجى إعادة تعبئة الدواء.", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.DeepSlate),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).testTag("take_dose_btn_${med.id}"),
                                contentPadding = PaddingValues(vertical = 4.dp, horizontal = 12.dp)
                            ) {
                                Text("تسجيل تناول جرعة 💊", color = SoftTheme.SoftTeal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            IconButton(
                                onClick = { viewModel.deleteMedication(med) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = SoftTheme.SoftPink.copy(alpha = 0.7f))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("إضافة دواء / مكمل للخزانة 💊", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = medName,
                        onValueChange = { medName = it },
                        label = { Text("اسم الدواء / الفيتامين (مثال: الحديد)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = medDosage,
                        onValueChange = { medDosage = it },
                        label = { Text("الجرعة (مثال: حبة واحدة ٥٠٠ملج)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = medTimesPerDay,
                        onValueChange = { medTimesPerDay = it },
                        label = { Text("مرات التكرار يومياً") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = totalQty,
                        onValueChange = { totalQty = it },
                        label = { Text("الكمية الإجمالية بالعلبة (عدد الحبوب الكلي)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = remainingQty,
                        onValueChange = { remainingQty = it },
                        label = { Text("الكمية المتوفرة حالياً بالخزانة") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("تاريخ الصلاحية المقدر:", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Slider(
                            value = expiryDaysOffset,
                            onValueChange = { expiryDaysOffset = it },
                            valueRange = 10f..730f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(thumbColor = SoftTheme.SoftPink, activeTrackColor = SoftTheme.SoftPink)
                        )
                        Text("${expiryDaysOffset.toInt()} يوم", fontWeight = FontWeight.Bold, color = SoftTheme.SoftTeal, fontSize = 12.sp)
                    }

                    Text("مستوى الأمان للأم والطفل:", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
                    
                    val safetyOptions = listOf(
                        Triple("Safe", "Safe", "🍏 آمن بالكامل مع الحمل والرضاعة"),
                        Triple("Caution", "Caution", "⚠️ بحذر شديد مع الحمل واستشارة"),
                        Triple("AskDoctor", "AskDoctor", "🚨 غير آمن / تجنبيه تماماً")
                    )

                    safetyOptions.forEach { (stat, valName, desc) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { safetyStatus = stat }
                                .background(
                                    if (safetyStatus == stat) SoftTheme.DeepSlate else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = safetyStatus == stat,
                                onClick = { safetyStatus = stat },
                                colors = RadioButtonDefaults.colors(selectedColor = SoftTheme.SoftPink)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(desc, color = SoftTheme.TextWhite, fontSize = 12.sp)
                        }
                    }

                    OutlinedTextField(
                        value = medNotes,
                        onValueChange = { medNotes = it },
                        label = { Text("تعليمات إضافية (مثال: يؤخذ بعد الغداء)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (medName.isNotBlank()) {
                            val times = medTimesPerDay.toIntOrNull() ?: 1
                            val tot = totalQty.toIntOrNull() ?: 30
                            val rem = remainingQty.toIntOrNull() ?: 30
                            val expDate = System.currentTimeMillis() + (expiryDaysOffset.toLong() * 24 * 60 * 60 * 1000)

                            viewModel.addMedication(
                                name = medName,
                                dosage = medDosage.ifBlank { null },
                                timesPerDay = times,
                                prescby = medPrescby.ifBlank { null },
                                notes = medNotes.ifBlank { null },
                                start = System.currentTimeMillis(),
                                expiryDate = expDate,
                                totalQuantity = tot,
                                remainingQuantity = rem,
                                safetyWarning = safetyStatus
                            )
                            
                            // Reset state
                            medName = ""
                            medDosage = ""
                            medTimesPerDay = "1"
                            medPrescby = ""
                            medNotes = ""
                            expiryDaysOffset = 90f
                            totalQty = "30"
                            remainingQty = "30"
                            safetyStatus = "Safe"
                            
                            showAddDialog = false
                        } else {
                            android.widget.Toast.makeText(context, "الرجاء إدخال اسم الدواء", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink)
                ) {
                    Text("حفظ للخزانة 💾", color = SoftTheme.TextWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("إلغاء", color = SoftTheme.SoftPink)
                }
            }
        )
    }
}

// --- سجل الوحم والاشتهاء ومشاركة جوري (Pregnancy Cravings Sub-screen) ---
@Composable
fun CravingScreen(viewModel: WomanCompanionViewModel) {
    val cravingLogs by viewModel.allCravingLogsState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    // Craving log state
    var cravingItem by remember { mutableStateOf("") }
    var cravingType by remember { mutableStateOf("Sweet") }
    var intensity by remember { mutableStateOf(5f) }
    var notes by remember { mutableStateOf("") }

    val context = LocalContext.current

    val typeMapping = listOf(
        "Sweet" to "حلو (فواكه وحلويات) 🍓",
        "Salty" to "حادق (موالح ومخللات) 🥨",
        "Sour" to "حامض (ليمون وبرتقال) 🍋",
        "Spicy" to "حار (شطة وفلفل) 🌶️",
        "Chocolate" to "شوكولاتة وكاكاو 🍫",
        "NonFood" to "أخرى (غير غذائي - ثلج/تراب) 🧱"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "سجل الوحم والاشتهاء ومشاركة جوري 🍓🍉",
                        style = MaterialTheme.typography.titleLarge,
                        color = SoftTheme.SoftPink,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "الوحم والاشتهاء أثناء الحمل ليس مجرد رغبة عشوائية، بل هو تعبير رقيق من جسدكِ عن نقص بعض المعادن أو الفيتامينات أو حاجة طفلكِ للطاقة! تتبعي وحمكِ وشاركي جوري لتحصلي على تحليل طبي دافئ.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoftTheme.SoftGray
                    )
                    
                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                        modifier = Modifier.fillMaxWidth().testTag("add_craving_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = SoftTheme.TextWhite)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تسجيل وحم جديد الآن ✍️", color = SoftTheme.TextWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Educational dynamic advice banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("💡 فك شفرة الوحم مع جوري:", fontWeight = FontWeight.Bold, color = SoftTheme.SoftTeal)
                    Text(
                        text = "• **المالح والحادق**: يدل غالباً على تضغط دم واطي وحاجة لتوازن السوائل والأملاح في جسمكِ.\n" +
                               "• **الحلويات والنشويات**: تعني حاجة فورية للطاقة أو تقلبات هرمونية سريعة.\n" +
                               "• **الشوكولاتة**: ترتبط بنقص المغنيسيوم. تناولي الكاكاو الداكن باعتدال.\n" +
                               "• **غير الغذائي (ثلج/تراب/طين)**: قد يكون إشارة فقر دم شديد (أنيميا نقص الحديد). استشيري طبيبتكِ فوراً!",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTheme.SoftGray,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        if (cravingLogs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🍉", fontSize = 48.sp)
                        Text("خزانة ذكريات الوحم فارغة الآن", color = SoftTheme.SoftGray, fontWeight = FontWeight.Bold)
                        Text("سجلي وحمكِ اليوم أو تحدثي مع جوري في الشات ليتم حفظه تلقائياً!", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray.copy(alpha = 0.7f))
                    }
                }
            }
        } else {
            item {
                Text(
                    text = "خزانة ذكريات وحمكِ (${cravingLogs.size}) 📂🍓",
                    style = MaterialTheme.typography.titleMedium,
                    color = SoftTheme.TextWhite,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            items(cravingLogs) { log ->
                val emoji = when (log.cravingType) {
                    "Sweet" -> "🍓"
                    "Salty" -> "🥨"
                    "Sour" -> "🍋"
                    "Spicy" -> "🌶️"
                    "Chocolate" -> "🍫"
                    else -> "🧱"
                }

                val typeArabic = typeMapping.firstOrNull { it.first == log.cravingType }?.second ?: "عام"

                val intensityText = when (log.intensity) {
                    in 1..3 -> "لطيف"
                    in 4..6 -> "شديد"
                    in 7..8 -> "قوي جداً!"
                    else -> "جنوني! 🤯"
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(emoji, fontSize = 24.sp)
                                Column {
                                    Text(
                                        text = log.cravingItem,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = SoftTheme.TextWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = typeArabic,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SoftTheme.SoftGray
                                    )
                                }
                            }
                            
                            IconButton(onClick = { viewModel.deleteCravingLog(log) }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.Red.copy(alpha = 0.7f))
                            }
                        }

                        // Intensity and advice
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SoftTheme.DeepSlate.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("شدة الاشتهاء: $intensityText (${log.intensity}/10)", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftTeal)
                            Text(
                                text = java.text.SimpleDateFormat("yyyy/MM/dd", java.util.Locale.getDefault()).format(java.util.Date(log.date)),
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftTheme.SoftGray
                            )
                        }

                        if (!log.notes.isNullOrEmpty()) {
                            Text(
                                text = "✍️ ملاحظاتكِ: ${log.notes}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SoftTheme.SoftGray
                            )
                        }

                        // Jouri's physical/medical commentary
                        val analyticalComment = when (log.cravingType) {
                            "Sweet" -> "رغبتكِ في السكريات تدل على حاجة جسمكِ لطاقة سريعة، أو ربما الجنين يحب السعادة والتحرك! ركزي على الفاكهة الطازجة وتجنبي السكر الصناعي المفرط لحمايتكِ من سكري الحمل 🍓🍰"
                            "Salty" -> "الوحم على الموالح مثل المخللات شائع جداً بسبب تغير توازن السوائل والأملاح نتيجة هرمونات الحمل. تذكري ألا تفرطي بالملح لتجنب احتباس المياه وتورم القدمين واليدين واشربي الكثير من الماء! 🥨🥒"
                            "Sour" -> "الرغبة بالليمون والأطعمة الحامضة ترتبط بمحاولة جسمكِ الطبيعية لمقاومة الغثيان وتسهيل الهضم وزيادة حمض المعدة. الليمون رائع وآمن جداً لكِ 🍋🍏"
                            "Spicy" -> "الوحم على الأكل الحار والشطة يرجع لتأثير الهرمونات على حاستكِ التذوقية، ورغبتكِ في رفع درجة حرارة الجسم والتمثيل الغذائي. انتبهي كي لا يسبب لكِ حموضة أو حرقان المعدة! 🌶️🥵"
                            "Chocolate" -> "الرغبة الملحة في الشوكولاتة والكاكاو تدل غالباً على حاجة جسمكِ لعنصر المغنيسيوم لتهدئة عضلات الرحم ومنع التشنجات! دللي نفسكِ بقطعة شوكولاتة داكنة صحية وقليلة السكر 🍫✨"
                            else -> "الوحم غير الغذائي (مثل اشتهاء الثلج أو الطين والتراب والطباشير) هو إشارة كلاسيكية هامة لاحتمال وجود أنيميا حادة ونقص شديد بالحديد. يرجى مراجعة طبيبتكِ لعمل تحليل صورة دم كاملة فوراً! 🧱🚨"
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SoftTheme.SoftPink.copy(alpha = 0.08f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text("👩‍⚕️", fontSize = 18.sp)
                                Column {
                                    Text("تحليل جوري الذكي:", fontWeight = FontWeight.Bold, color = SoftTheme.SoftPink, style = MaterialTheme.typography.bodySmall)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(analyticalComment, style = MaterialTheme.typography.bodySmall, color = SoftTheme.TextWhite, lineHeight = 16.sp)
                                }
                            }
                        }

                        // Share with partner
                        Button(
                            onClick = {
                                val shareText = "حبيبي الغالي، أنا متوحمة النهاردة على (${log.cravingItem}) بشدة $intensityText! وجوري بتقول إن ده رد فعل طبيعي لجسمي وجنيني ومحتاجين شوية دلال واهتمام.. هتعرف تجيبهالي معاك وأنت جاي؟ 🥰🍓🍉"
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clip = android.content.ClipData.newPlainText("Craving", shareText)
                                clipboard.setPrimaryClip(clip)
                                android.widget.Toast.makeText(context, "تم نسخ رسالة شريككِ اللطيفة بنجاح! 🔗❤️", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.DeepSlate),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = SoftTheme.SoftPink, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("نسخ رسالة دلع لمشاركتها مع زوجكِ 🔗❤️", color = SoftTheme.TextWhite, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = SoftTheme.CardSlate,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "تسجيل وحم جديد 🍓",
                    style = MaterialTheme.typography.titleLarge,
                    color = SoftTheme.SoftPink,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = cravingItem,
                        onValueChange = { cravingItem = it },
                        label = { Text("إيش نفسكِ تأكلي؟ (مثال: مانجو باردة)") },
                        modifier = Modifier.fillMaxWidth().testTag("craving_item_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoftTheme.SoftPink,
                            unfocusedBorderColor = SoftTheme.SoftGray,
                            focusedTextColor = SoftTheme.TextWhite,
                            unfocusedTextColor = SoftTheme.TextWhite
                        )
                    )

                    // Type ChoiceChips
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("تصنيف الوحم والاشتهاء:", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftPink, fontWeight = FontWeight.Bold)
                        
                        val row1 = typeMapping.take(3)
                        val row2 = typeMapping.takeLast(3)

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            row1.forEach { (typeKey, displayName) ->
                                val selected = cravingType == typeKey
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { cravingType = typeKey },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selected) SoftTheme.SoftPink else SoftTheme.DeepSlate.copy(alpha = 0.5f)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Box(modifier = Modifier.padding(8.dp), contentAlignment = Alignment.Center) {
                                        Text(
                                            text = displayName.substringBefore(" "),
                                            color = if (selected) SoftTheme.TextWhite else SoftTheme.SoftGray,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            row2.forEach { (typeKey, displayName) ->
                                val selected = cravingType == typeKey
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { cravingType = typeKey },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selected) SoftTheme.SoftPink else SoftTheme.DeepSlate.copy(alpha = 0.5f)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Box(modifier = Modifier.padding(8.dp), contentAlignment = Alignment.Center) {
                                        Text(
                                            text = displayName.substringBefore(" "),
                                            color = if (selected) SoftTheme.TextWhite else SoftTheme.SoftGray,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Intensity Slider
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        val sliderText = when (intensity.toInt()) {
                            in 1..3 -> "لطيف"
                            in 4..6 -> "شديد"
                            in 7..8 -> "قوي جداً!"
                            else -> "جنوني! 🤯"
                        }
                        Text("قوة الاشتهاء: $sliderText (${intensity.toInt()}/10)", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftTeal, fontWeight = FontWeight.Bold)
                        Slider(
                            value = intensity,
                            onValueChange = { intensity = it },
                            valueRange = 1f..10f,
                            steps = 8,
                            colors = SliderDefaults.colors(
                                thumbColor = SoftTheme.SoftPink,
                                activeTrackColor = SoftTheme.SoftPink,
                                inactiveTrackColor = SoftTheme.SoftGray.copy(alpha = 0.3f)
                            )
                        )
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("ملاحظات أخرى (مثال: في أي وقت، وبم تشعرين؟)") },
                        modifier = Modifier.fillMaxWidth().testTag("craving_notes_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoftTheme.SoftPink,
                            unfocusedBorderColor = SoftTheme.SoftGray,
                            focusedTextColor = SoftTheme.TextWhite,
                            unfocusedTextColor = SoftTheme.TextWhite
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (cravingItem.isNotBlank()) {
                            viewModel.addCravingLog(
                                cravingItem = cravingItem,
                                cravingType = cravingType,
                                intensity = intensity.toInt(),
                                notes = notes.ifBlank { null }
                            )
                            cravingItem = ""
                            notes = ""
                            intensity = 5f
                            cravingType = "Sweet"
                            showAddDialog = false
                        } else {
                            android.widget.Toast.makeText(context, "الرجاء إدخال أكلة الوحم", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink)
                ) {
                    Text("حفظ في خزانة الذكريات 💾", color = SoftTheme.TextWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("إلغاء", color = SoftTheme.SoftPink)
                }
            }
        )
    }
}

@Composable
fun NewFeaturesUpdatesBanner(
    viewModel: WomanCompanionViewModel,
    onNavigateToTab: (Int) -> Unit
) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("jouri_behavior_prefs", android.content.Context.MODE_PRIVATE) }
    var isDismissed by remember {
        mutableStateOf(sharedPrefs.getBoolean("new_updates_banner_dismissed_v2", false))
    }

    if (isDismissed) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("🚀", fontSize = 24.sp)
                    Column {
                        Text(
                            text = "التحديثات والميزات الذكية الجديدة 🌟",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.SoftPink
                        )
                        Text(
                            text = "اكتشفي ميزات صديقتكِ جوري المحدثة لراحتكِ",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTheme.SoftGray
                        )
                    }
                }
                IconButton(
                    onClick = {
                        isDismissed = true
                        sharedPrefs.edit().putBoolean("new_updates_banner_dismissed_v2", true).apply()
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق التنبيه",
                        tint = SoftTheme.SoftGray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Feature 1: Craving
                NewFeatureItemRow(
                    emoji = "🍓",
                    title = "سجل الوحم والاشتهاء ومشاركة جوري",
                    description = "تتبعي رغباتكِ اليومية والوحم، واحصلي على تحليل طبي دافئ، وشاركي مشاعركِ اللطيفة كرسالة جاهزة لزوجكِ! 🥰🍉",
                    onOpen = {
                        viewModel.setActiveSubScreen("craving")
                        onNavigateToTab(4)
                    }
                )

                HorizontalDivider(color = SoftTheme.DeepSlate.copy(alpha = 0.5f), thickness = 0.5.dp)

                // Feature 2: Sleep
                NewFeatureItemRow(
                    emoji = "💤",
                    title = "محلل ومراقب النوم الذكي والأرق",
                    description = "سجلي ساعات نومكِ وجودته، ودعي جوري تحلل لكِ عادات نومكِ وتقدم لكِ نصائح ذهبية لنوم هانئ ومريح. 🌙💤",
                    onOpen = {
                        viewModel.setActiveSubScreen("sleep_analyzer")
                        onNavigateToTab(4)
                    }
                )

                HorizontalDivider(color = SoftTheme.DeepSlate.copy(alpha = 0.5f), thickness = 0.5.dp)

                // Feature 3: Partner
                NewFeatureItemRow(
                    emoji = "🔗",
                    title = "رابط الرفيق ومشاركة الشريك",
                    description = "أشركي زوجكِ أو عائلتكِ في رحلتكِ واستقبلي رسائل الدعم والاهتمام بخصوصية تامة داخل التطبيق. ❤️✨",
                    onOpen = {
                        viewModel.setActiveSubScreen("partner_sync")
                        onNavigateToTab(4)
                    }
                )

                HorizontalDivider(color = SoftTheme.DeepSlate.copy(alpha = 0.5f), thickness = 0.5.dp)

                // Feature 4: Pharmacy
                NewFeatureItemRow(
                    emoji = "📦",
                    title = "الصيدلية المنزلية المتقدمة وتتبع الأدوية",
                    description = "سجلي علب أدويتكِ وصلاحيتها ومخزونها المتبقي لتنبيهكِ قبل النفاد ولتجنب استخدام أدوية منتهية الصلاحية. 💊📦",
                    onOpen = {
                        viewModel.setActiveSubScreen("home_pharmacy")
                        onNavigateToTab(4)
                    }
                )

                HorizontalDivider(color = SoftTheme.DeepSlate.copy(alpha = 0.5f), thickness = 0.5.dp)

                // Feature 5: Fitness
                NewFeatureItemRow(
                    emoji = "🧘‍♀️",
                    title = "تمارين لياقة المرأة الحامل والنفاس",
                    description = "تمارين رياضية آمنة مخصصة لثلث حملكِ أو فترة النفاس مع مؤقت ذكي لمساعدتكِ على أداء الحركة بنشاط وصحة. 🧘‍♀️💪",
                    onOpen = {
                        viewModel.setActiveSubScreen("fitness")
                        onNavigateToTab(4)
                    }
                )
            }
        }
    }
}

@Composable
fun NewFeatureItemRow(
    emoji: String,
    title: String,
    description: String,
    onOpen: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate.copy(alpha = 0.25f)),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.TextWhite
                    ),
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = SoftTheme.SoftGray,
                        lineHeight = 16.sp
                    ),
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onOpen,
                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.MintTeal.copy(alpha = 0.18f)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "جربي الأداة الآن ⚡",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SoftTheme.MintTeal,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(SoftTheme.SoftPink.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 22.sp)
            }
        }
    }
}

data class SmartAlertItem(
    val emoji: String,
    val title: String,
    val description: String,
    val actionText: String,
    val action: () -> Int
)

@Composable
fun SmartAlertCard(
    item: SmartAlertItem,
    onNavigate: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.SoftPink
                        ),
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTheme.TextWhite.copy(alpha = 0.9f),
                        lineHeight = 16.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(SoftTheme.SoftPink.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(item.emoji, fontSize = 22.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { onNavigate(item.action()) },
                colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.MintTeal.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier
                    .align(Alignment.Start)
                    .height(34.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = item.actionText,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.MintTeal
                        )
                    )
                    Text("⚡", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun JouriNotificationsDialog(
    viewModel: WomanCompanionViewModel,
    onDismiss: () -> Unit,
    onNavigateToTab: (Int) -> Unit
) {
    val pregnancyState by viewModel.pregnancyState.collectAsStateWithLifecycle()
    val progression = viewModel.getPregnancyProgression()
    val cycleInfo = viewModel.getCurrentCyclePhase()

    var selectedTab by remember { mutableStateOf(0) } // 0 for Features, 1 for Smart Alerts

    val alerts = remember(pregnancyState, progression, cycleInfo) {
        val list = mutableListOf<SmartAlertItem>()
        
        val preg = pregnancyState
        if (preg != null) {
            // 1. Core State Advice
            if (preg.isPregnant) {
                list.add(
                    SmartAlertItem(
                        emoji = "👶🏻",
                        title = "متابعة الحمل - الأسبوع ${progression?.weeks ?: 12}",
                        description = "أنتِ الآن في الأسبوع ${progression?.weeks ?: 12} من الحمل. طفلكِ الجميل الآن بحجم ${progression?.comparisonName ?: "حبة تين كاملة"} ${progression?.comparisonIcon ?: "🫓"}. تذكري شرب مياه كافية والراحة والترطيب المستمر لسلامتكما اليوم 🌸",
                        actionText = "تفاصيل تقدم الحمل ⚡",
                        action = { 0 }
                    )
                )
            } else {
                list.add(
                    SmartAlertItem(
                        emoji = "🩸",
                        title = "حالة دورتكِ الحالية: ${cycleInfo.phaseArabic}",
                        description = cycleInfo.description,
                        actionText = "سجل الدورة والخصوبة 📅",
                        action = { 1 }
                    )
                )
            }
            
            // 2. High Blood Pressure Alert
            if (preg.hasHighBp) {
                list.add(
                    SmartAlertItem(
                        emoji = "🩺",
                        title = "مراقبة هامة: ضغط الدم المرتفع ⚠️",
                        description = "عزيزتي جميلة، سجلكِ يشير لضغط دم مرتفع. ننصحكِ بقياس الضغط الآن، والابتعاد التام عن الأطعمة الغنية بالصوديوم (الأملاح) مع شرب الكركديه البارد والراحة التامة.",
                        actionText = "سجل الأعراض والضغط 🩺",
                        action = { 3 }
                    )
                )
            }
            
            // 3. Low Blood Pressure Alert
            if (preg.hasLowBp) {
                list.add(
                    SmartAlertItem(
                        emoji = "🥤",
                        title = "تنبيه صحي: ضغط دم منخفض",
                        description = "عزيزتي جميلة، ننصحكِ بشرب كميات كافية من المياه لتعويض السوائل، والنهوض تدريجياً وتجنب الوقوف لفترات طويلة لتفادي أي دوار ونقص تدفق الأكسجين.",
                        actionText = "سجل الأعراض 🩺",
                        action = { 3 }
                    )
                )
            }
            
            // 4. Diabetes Alert
            if (preg.hasDiabetes) {
                list.add(
                    SmartAlertItem(
                        emoji = "🍏",
                        title = "تنبيه طبي: السكري الذكي 📈",
                        description = "تذكري قياس السكر بعد الوجبات بساعتين وتجنب السكريات البسيطة والمشروبات الغازية. استبدليها بوجبات خفيفة غنية بالألياف كالخضار الطازجة وحبة تفاح.",
                        actionText = "روتين الغذاء والتغذية 🥑",
                        action = { 2 }
                    )
                )
            }
        }
        
        // 5. Daily Wellness & Deep Breathing Reminder
        list.add(
            SmartAlertItem(
                emoji = "🧘‍♀️",
                title = "جلسة تنفس عميق وصحة نفسية ✨",
                description = "أثبتت الدراسات أن 3 دقائق من التنفس العميق والواعي تخفض هرمون الكورتيسول (الإجهاد) وتزيد تدفق الأكسجين لطفلكِ بشكل فوري وفعّال.",
                actionText = "ابدئي تمارين اللياقة 🧘‍♀️",
                action = { 
                    viewModel.setActiveSubScreen("fitness")
                    4 
                }
            )
        )

        // 6. Water hydration Alert
        list.add(
            SmartAlertItem(
                emoji = "💧",
                title = "تذكير الترطيب الذكي من جوري",
                description = "شرب 8-10 أكواب من المياه يومياً يحميكِ من التعب وصداع الحمل ويساعد في تجديد السائل الأمنيوسي المحيط بالطفل باستمرار.",
                actionText = "سجّلي شرب كوب ماء 🥤",
                action = { 2 }
            )
        )
        
        list
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.75f))
                .clickable { onDismiss() }
                .padding(horizontal = 16.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
                    .clickable(enabled = false) {},
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.3f))
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
                            onClick = onDismiss,
                            modifier = Modifier
                                .background(SoftTheme.DeepSlate.copy(alpha = 0.5f), CircleShape)
                                .size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "إغلاق التنبيهات",
                                tint = SoftTheme.SoftPink,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "مركز التنبيهات الذكي من جوري 🔔",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = SoftTheme.SoftPink
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tab Selector Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SoftTheme.DeepSlate.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selectedTab == 0) SoftTheme.SoftPink else Color.Transparent)
                                .clickable { selectedTab = 0 }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "ميزات جوري الذكية 🌟",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedTab == 0) SoftTheme.TextWhite else SoftTheme.SoftGray
                                )
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selectedTab == 1) SoftTheme.SoftPink else Color.Transparent)
                                .clickable { selectedTab = 1 }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "تنبيهاتكِ الصحية 🔔",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedTab == 1) SoftTheme.TextWhite else SoftTheme.SoftGray
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Content Scrollable Area
                    Box(modifier = Modifier.weight(1f)) {
                        if (selectedTab == 0) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Feature 1: Craving
                                NewFeatureItemRow(
                                    emoji = "🍓",
                                    title = "سجل الوحم والاشتهاء ومشاركة جوري",
                                    description = "تتبعي رغباتكِ اليومية والوحم، واحصلي على تحليل طبي دافئ، وشاركي مشاعركِ اللطيفة كرسالة جاهزة لزوجكِ! 🥰🍉",
                                    onOpen = {
                                        viewModel.setActiveSubScreen("craving")
                                        onNavigateToTab(4)
                                        onDismiss()
                                    }
                                )

                                HorizontalDivider(color = SoftTheme.DeepSlate.copy(alpha = 0.5f), thickness = 0.5.dp)

                                // Feature 2: Sleep
                                NewFeatureItemRow(
                                    emoji = "💤",
                                    title = "محلل ومراقب النوم الذكي والأرق",
                                    description = "سجلي ساعات نومكِ وجودته، ودعي جوري تحلل لكِ عادات نومكِ وتقدم لكِ نصائح ذهبية لنوم هانئ ومريح. 🌙💤",
                                    onOpen = {
                                        viewModel.setActiveSubScreen("sleep_analyzer")
                                        onNavigateToTab(4)
                                        onDismiss()
                                    }
                                )

                                HorizontalDivider(color = SoftTheme.DeepSlate.copy(alpha = 0.5f), thickness = 0.5.dp)

                                // Feature 3: Partner
                                NewFeatureItemRow(
                                    emoji = "🔗",
                                    title = "رابط الرفيق ومشاركة الشريك",
                                    description = "أشركي زوجكِ أو عائلتكِ في رحلتكِ واستقبلي رسائل الدعم والاهتمام بخصوصية تامة داخل التطبيق. ❤️✨",
                                    onOpen = {
                                        viewModel.setActiveSubScreen("partner_sync")
                                        onNavigateToTab(4)
                                        onDismiss()
                                    }
                                )

                                HorizontalDivider(color = SoftTheme.DeepSlate.copy(alpha = 0.5f), thickness = 0.5.dp)

                                // Feature 4: Pharmacy
                                NewFeatureItemRow(
                                    emoji = "📦",
                                    title = "الصيدلية المنزلية المتقدمة وتتبع الأدوية",
                                    description = "سجلي علب أدويتكِ وصلاحيتها ومخزونها المتبقي لتنبيهكِ قبل النفاد ولتجنب استخدام أدوية منتهية الصلاحية. 💊📦",
                                    onOpen = {
                                        viewModel.setActiveSubScreen("home_pharmacy")
                                        onNavigateToTab(4)
                                        onDismiss()
                                    }
                                )

                                HorizontalDivider(color = SoftTheme.DeepSlate.copy(alpha = 0.5f), thickness = 0.5.dp)

                                // Feature 5: Fitness
                                NewFeatureItemRow(
                                    emoji = "🧘‍♀️",
                                    title = "تمارين لياقة المرأة الحامل والنفاس",
                                    description = "تمارين رياضية آمنة مخصصة لثلث حملكِ أو فترة النفاس مع مؤقت ذكي لمساعدتكِ على أداء الحركة بنشاط وصحة. 🧘‍♀️💪",
                                    onOpen = {
                                        viewModel.setActiveSubScreen("fitness")
                                        onNavigateToTab(4)
                                        onDismiss()
                                    }
                                )
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                alerts.forEach { item ->
                                    SmartAlertCard(
                                        item = item,
                                        onNavigate = { tabIndex ->
                                            onNavigateToTab(tabIndex)
                                            onDismiss()
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bottom Pill Button - "جوري صديقتكِ الذكية 🌸"
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                        shape = RoundedCornerShape(50.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .align(Alignment.CenterHorizontally)
                            .height(48.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "جوري صديقتكِ الذكية 🌸",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = SoftTheme.TextWhite
                            )
                        }
                    }
                }
            }
        }
    }
}
