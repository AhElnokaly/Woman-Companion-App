package com.example.ui

import com.example.BuildConfig
import com.example.util.formatArabicDays

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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.text.selection.SelectionContainer
import android.content.Context
import android.content.Intent
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

// Custom Date Format Helpers
