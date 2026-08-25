package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.viewmodel.*
import kotlinx.coroutines.launch

// ==========================================
// 👜 HOSPITAL BAG CHECKLIST SCREEN
// ==========================================
data class HospitalBagItem(
    val id: String,
    val title: String,
    val category: String, // "MOM", "BABY", "DOCS"
    val isChecked: Boolean = false,
    val tip: String? = null
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HospitalBagScreen(viewModel: WomanCompanionViewModel) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("hospital_bag_prefs", Context.MODE_PRIVATE) }
    
    val initialItems = remember {
        listOf(
            // Mom
            HospitalBagItem("m1", "ملابس مريحة وفضفاضة للولادة والاستقبال (٢-٣ أطقم)", "MOM", tip = "يفضل أزرار أمامية لتسهيل الرضاعة"),
            HospitalBagItem("m2", "فوط صحية قطنية فائقة الامتصاص لما بعد الولادة", "MOM"),
            HospitalBagItem("m3", "حمالات صدر مخصصة للرضاعة ووسادات قطنية للثدي", "MOM"),
            HospitalBagItem("m4", "حذاء منزلي مريح (شبشب) وجوارب قطنية دافئة", "MOM"),
            HospitalBagItem("m5", "حقيبة عناية شخصية (فرشاة أسنان، غسول، مرطب شفاه، مشط)", "MOM"),
            HospitalBagItem("m6", "شاحن هاتف بكيبل طويل وباور بانك", "MOM"),
            HospitalBagItem("m7", "وجبات خفيفة صحية ومياه وتمر لاستعادة الطاقة", "MOM"),
            
            // Baby
            HospitalBagItem("b1", "أطقم ملابس للمولود (داخلية وخارجية مقاس 0-3)", "BABY", tip = "مغسولة ومجهزة مسبقاً"),
            HospitalBagItem("b2", "بطانية / قماط قطني دافئ للف الطفل", "BABY"),
            HospitalBagItem("b3", "حفاضات مقاس حديثي الولادة (Newborn)", "BABY"),
            HospitalBagItem("b4", "مناديل مبللة نقية خالية من العطور (Water Wipes)", "BABY"),
            HospitalBagItem("b5", "كريم الحماية من التسلخات وزيت مرطب لطيف", "BABY"),
            HospitalBagItem("b6", "قبعات قطنية وقفازات لليدين وجوارب صغيرة", "BABY"),
            HospitalBagItem("b7", "كرسي السيارة للرضع (Car Seat) للعودة للمنزل بأمان", "BABY", tip = "إلزامي للسلامة"),

            // Docs
            HospitalBagItem("d1", "بطاقة الهوية الوطنية / الإقامة للزوجين", "DOCS"),
            HospitalBagItem("d2", "بطاقة التأمين الطبي أو أوراق المستشفى المعتمدة", "DOCS"),
            HospitalBagItem("d3", "ملف متابعة الحمل وتقارير التحاليل والسونار الأخيرة", "DOCS"),
            HospitalBagItem("d4", "كارت فصيلة الدم وسجل أية أمراض أو حساسيات مزمنة", "DOCS")
        )
    }

    val loadSavedItems = remember {
        val customCount = prefs.getInt("custom_items_count", 0)
        val loadedCustomList = (0 until customCount).mapNotNull { idx ->
            val id = prefs.getString("custom_${idx}_id", null) ?: return@mapNotNull null
            val title = prefs.getString("custom_${idx}_title", "") ?: ""
            val category = prefs.getString("custom_${idx}_cat", "MOM") ?: "MOM"
            val tip = prefs.getString("custom_${idx}_tip", null)
            val isChecked = prefs.getBoolean("item_$id", false)
            HospitalBagItem(id = id, title = title, category = category, isChecked = isChecked, tip = tip)
        }

        val baseList = initialItems.map { item ->
            val savedState = prefs.getBoolean("item_${item.id}", false)
            item.copy(isChecked = savedState)
        }
        baseList + loadedCustomList
    }

    var itemsList by remember { mutableStateOf(loadSavedItems) }

    var selectedCategory by remember { mutableStateOf("ALL") }
    var newItemText by remember { mutableStateOf("") }
    var showAddItemDialog by remember { mutableStateOf(false) }

    val completedCount = itemsList.count { it.isChecked }
    val totalCount = itemsList.size
    val progressPercent = if (totalCount > 0) (completedCount * 100 / totalCount) else 0

    val filteredList = when (selectedCategory) {
        "MOM" -> itemsList.filter { it.category == "MOM" }
        "BABY" -> itemsList.filter { it.category == "BABY" }
        "DOCS" -> itemsList.filter { it.category == "DOCS" }
        else -> itemsList
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Header
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "حقيبة الولادة الذكية 👜👶",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = SoftTheme.TextWhite
                            )
                            Text(
                                text = "جهزي أغراض المستشفى واطمئني بالكامل",
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftTheme.SoftGray
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SoftTheme.SoftPink.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "$completedCount / $totalCount جاهز",
                                color = SoftTheme.SoftPink,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LinearProgressIndicator(
                        progress = { progressPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = SoftTheme.MintTeal,
                        trackColor = SoftTheme.DeepSlate
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (progressPercent == 100) "حقيبتكِ جاهزة بالكامل لولادة آمنة ومريحة بإذن الله! 🎉🌸"
                               else "نسبة جاهزية الحقيبة: $progressPercent%",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (progressPercent == 100) SoftTheme.MintTeal else SoftTheme.SoftGray
                    )
                }
            }
        }

        // Category Filter Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val categories = listOf(
                    "ALL" to "الكل 📋",
                    "MOM" to "للأم 🌸",
                    "BABY" to "للمولود 👶",
                    "DOCS" to "المستندات 📄"
                )
                categories.forEach { (catKey, catLabel) ->
                    val isSelected = selectedCategory == catKey
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) SoftTheme.SoftPink else SoftTheme.CardSlate,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedCategory = catKey }
                    ) {
                        Text(
                            text = catLabel,
                            color = if (isSelected) Color.White else SoftTheme.TextWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    }
                }
            }
        }

        // Add custom item action button
        item {
            OutlinedButton(
                onClick = { showAddItemDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftTheme.SoftPink),
                border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = SoftTheme.SoftPink)
                Spacer(modifier = Modifier.width(6.dp))
                Text("إضافة غرض جديد مخصص للحقيبة ➕", fontWeight = FontWeight.Bold)
            }
        }

        // Items list
        items(filteredList, key = { it.id }) { item ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (item.isChecked) SoftTheme.CardSlate.copy(alpha = 0.6f) else SoftTheme.CardSlate
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    1.dp,
                    if (item.isChecked) SoftTheme.MintTeal.copy(alpha = 0.3f) else SoftTheme.SoftGray.copy(alpha = 0.1f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val newChecked = !item.isChecked
                        prefs.edit().putBoolean("item_${item.id}", newChecked).apply()
                        itemsList = itemsList.map {
                            if (it.id == item.id) it.copy(isChecked = newChecked) else it
                        }
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Checkbox(
                        checked = item.isChecked,
                        onCheckedChange = { newChecked ->
                            prefs.edit().putBoolean("item_${item.id}", newChecked).apply()
                            itemsList = itemsList.map {
                                if (it.id == item.id) it.copy(isChecked = newChecked) else it
                            }
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = SoftTheme.MintTeal,
                            uncheckedColor = SoftTheme.SoftGray
                        )
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (item.isChecked) FontWeight.Normal else FontWeight.SemiBold,
                            color = if (item.isChecked) SoftTheme.SoftGray else SoftTheme.TextWhite,
                            textDecoration = if (item.isChecked) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                        )
                        if (!item.tip.isNullOrBlank()) {
                            Text(
                                text = "💡 ${item.tip}",
                                style = MaterialTheme.typography.labelSmall,
                                color = SoftTheme.SoftPink.copy(alpha = 0.9f),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    if (item.id.startsWith("custom_")) {
                        IconButton(
                            onClick = {
                                val updated = itemsList.filter { it.id != item.id }
                                itemsList = updated
                                val customOnly = updated.filter { it.id.startsWith("custom_") }
                                val editor = prefs.edit()
                                editor.putInt("custom_items_count", customOnly.size)
                                customOnly.forEachIndexed { index, cItem ->
                                    editor.putString("custom_${index}_id", cItem.id)
                                    editor.putString("custom_${index}_title", cItem.title)
                                    editor.putString("custom_${index}_cat", cItem.category)
                                    editor.putString("custom_${index}_tip", cItem.tip)
                                }
                                editor.remove("item_${item.id}")
                                editor.apply()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "حذف الغرض المخصص",
                                tint = SoftTheme.RedDanger.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddItemDialog) {
        var itemCategory by remember { mutableStateOf(if (selectedCategory == "ALL") "MOM" else selectedCategory) }
        var itemTip by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showAddItemDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "إضافة غرض جديد للحقيبة 👜✨",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.SoftPink
                    )

                    OutlinedTextField(
                        value = newItemText,
                        onValueChange = { newItemText = it },
                        label = { Text("اسم الغرض (مثال: شال مريح، زيت مساج)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoftTheme.SoftPink,
                            unfocusedBorderColor = SoftTheme.SoftGray,
                            focusedTextColor = SoftTheme.TextWhite,
                            unfocusedTextColor = SoftTheme.TextWhite
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = itemTip,
                        onValueChange = { itemTip = it },
                        label = { Text("ملاحظة أو تذكير (اختياري)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoftTheme.SoftPink,
                            unfocusedBorderColor = SoftTheme.SoftGray,
                            focusedTextColor = SoftTheme.TextWhite,
                            unfocusedTextColor = SoftTheme.TextWhite
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Text("التصنيف:", fontSize = 12.sp, color = SoftTheme.SoftGray, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("MOM" to "للأم", "BABY" to "للمولود", "DOCS" to "مستند").forEach { (catKey, catName) ->
                            val isSel = itemCategory == catKey
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSel) SoftTheme.SoftPink else SoftTheme.DeepSlate,
                                modifier = Modifier.weight(1f).clickable { itemCategory = catKey }
                            ) {
                                Text(
                                    text = catName,
                                    color = if (isSel) Color.White else SoftTheme.TextWhite,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = { showAddItemDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء", color = SoftTheme.SoftGray)
                        }

                        Button(
                            onClick = {
                                if (newItemText.isNotBlank()) {
                                    val newId = "custom_${System.currentTimeMillis()}"
                                    val createdItem = HospitalBagItem(
                                        id = newId,
                                        title = newItemText.trim(),
                                        category = itemCategory,
                                        tip = itemTip.ifBlank { null }
                                    )
                                    val updated = itemsList + createdItem
                                    itemsList = updated
                                    val customOnly = updated.filter { it.id.startsWith("custom_") }
                                    val editor = prefs.edit()
                                    editor.putInt("custom_items_count", customOnly.size)
                                    customOnly.forEachIndexed { index, cItem ->
                                        editor.putString("custom_${index}_id", cItem.id)
                                        editor.putString("custom_${index}_title", cItem.title)
                                        editor.putString("custom_${index}_cat", cItem.category)
                                        editor.putString("custom_${index}_tip", cItem.tip)
                                    }
                                    editor.apply()
                                    newItemText = ""
                                    itemTip = ""
                                    showAddItemDialog = false
                                }
                            },
                            enabled = newItemText.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("إضافة الغرض ✓", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 🥑 FOOD SAFETY GUIDE IN PREGNANCY SCREEN
// ==========================================
data class FoodSafetyItem(
    val name: String,
    val category: String, // "DAIRY", "MEAT", "DRINKS", "FRUITS", "HERBS"
    val safetyLevel: String, // "SAFE", "CAUTION", "FORBIDDEN"
    val reason: String,
    val icon: String
)

@Composable
fun FoodSafetyScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }

    val foodList = remember {
        listOf(
            // SAFE
            FoodSafetyItem("الحليب واللبن المبستر", "DAIRY", "SAFE", "غني بالكالسيوم والبروتين ومبستر وآمن تماماً لصحة العظام وتطور الجنين.", "🥛"),
            FoodSafetyItem("الزبادي اليوناني المبستر", "DAIRY", "SAFE", "يحتوي على البروبيوتيك لدعم الهضم وتقوية المناعة.", "🥣"),
            FoodSafetyItem("البيض المطهو جيداً", "MEAT", "SAFE", "مصدر ممتاز للكولين والبروتين. تأكدي أن الصفار والبياض متماسك تماماً.", "🍳"),
            FoodSafetyItem("سمك السلمون المطهو", "MEAT", "SAFE", "غني بالأوميجا 3 (DHA) الضروري لنمو دماغ وعيني الجنين. حصتان أسبوعياً.", "🐟"),
            FoodSafetyItem("الدجاج واللحوم المطهوة جيداً (Well-Done)", "MEAT", "SAFE", "مصدر أساسي للحديد والبروتين، تأكدي من نضجها التام.", "🍗"),
            FoodSafetyItem("الأفوكادو والمكسرات النيئة", "FRUITS", "SAFE", "دهون غير مشبعة ممتازة لحماية المشيمة وتطور الجهاز العصبي.", "🥑"),
            FoodSafetyItem("السبانخ والبروكلي والخضار الورقية المغسولة", "FRUITS", "SAFE", "غنية بحمض الفوليك والحديد والألياف. يجب غسلها جيداً بالخل والماء.", "🥦"),
            FoodSafetyItem("العدس والبقوليات والحمص", "MEAT", "SAFE", "غنية بحمض الفوليك والألياف والبروتين النباتي لمنع الإمساك وفقر الدم.", "🍲"),
            FoodSafetyItem("الماء وعصير البرتقال الطازج", "DRINKS", "SAFE", "ترطيب ممتاز ومصدر لفيتامين C لتعزيز امتصاص الحديد وتقوية المشيمة.", "🍊"),
            FoodSafetyItem("مشروب النعناع والبابونج الخفيف", "HERBS", "SAFE", "مهدئ للمعدة ويخفف الغثيان الصباحي باعتدال.", "🍵"),

            // CAUTION
            FoodSafetyItem("القهوة والشاي (الكافيين)", "DRINKS", "CAUTION", "الحد الأقصى المسموح 200 مجم يومياً (فنجان قهوة صغير أو كوب شاي واحد).", "☕"),
            FoodSafetyItem("التونة المعلبة (لايت)", "MEAT", "CAUTION", "مسموح بكمية معتدلة (علبة واحدة أسبوعياً) لتجنب تراكم الزئبق في الجسم.", "🥫"),
            FoodSafetyItem("الكبدة الحيوانية", "MEAT", "CAUTION", "تحتوي على نسب مركزة جداً من فيتامين A. تجنبيها بالثلث الأول ومقدار ملعقة نادراً لاحقاً.", "🥩"),
            FoodSafetyItem("القرفة والزنجبيل", "HERBS", "CAUTION", "مسموح كرشة توابل خفيفة بالطهي، ولكن تجنبي شرب المغلي المركز يومياً بالشهور الأولى.", "🌿"),
            FoodSafetyItem("الأطعمة الحارة والمخللات", "FRUITS", "CAUTION", "قد تفاقم حموضة المعدة، الارتجاع المريئي، واحتباس السوائل في الجسم.", "🌶️"),

            // FORBIDDEN
            FoodSafetyItem("الأجبان غير المبسترة (الفيتا الخام، الروكفور العفني، البري)", "DAIRY", "FORBIDDEN", "قد تحتوي على بكتيريا اللستيريا الخطيرة جداً على سلامة الجنين والمشيمة.", "🧀"),
            FoodSafetyItem("السوشي واللحوم النيئة (الستيك النصف مطهو، الكبة النيئة)", "MEAT", "FORBIDDEN", "خطر الإصابة بداء المقوسات (التوكسوبلازما) وبكتيريا السالمونيلا القاتلة للجنين.", "🍣"),
            FoodSafetyItem("اللحوم المصنعة (اللانشون، الهوت دوج، البسطرمة، السلامي)", "MEAT", "FORBIDDEN", "تحتوي على نترات ومواد حافظة وبكتيريا ضارة إذا لم تسخن لدرجة الغليان التام.", "🌭"),
            FoodSafetyItem("الأسماك عالية الزئبق (الماكريل الملكي، سمك القرش، أبو سيف)", "MEAT", "FORBIDDEN", "الزئبق يتراكم في الدم ويدمر الخلايا العصبية النامية في دماغ الجنين.", "🦈"),
            FoodSafetyItem("أعشاب تنشيط الرحم (الميرمية المركزة، الحلبة بجرعات عالية، الزعتر المركز)", "HERBS", "FORBIDDEN", "تحفز انقباضات عضلات الرحم وقد تؤدي لخطر الولادة المبكرة أو الإجهاض.", "🌱"),
            FoodSafetyItem("المشروبات الغازية ومشروبات الطاقة", "DRINKS", "FORBIDDEN", "نسب سكر وكافيين هائلة ومواد كيميائية ترفع ضغط الدم وتضر الكلى.", "🥤")
        )
    }

    val filteredList = remember(searchQuery, selectedFilter) {
        foodList.filter { item ->
            val matchesSearch = item.name.contains(searchQuery.trim(), ignoreCase = true) ||
                                item.reason.contains(searchQuery.trim(), ignoreCase = true)
            val matchesFilter = when (selectedFilter) {
                "SAFE" -> item.safetyLevel == "SAFE"
                "CAUTION" -> item.safetyLevel == "CAUTION"
                "FORBIDDEN" -> item.safetyLevel == "FORBIDDEN"
                else -> true
            }
            matchesSearch && matchesFilter
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            Text(
                text = "دليل سلامة الأغذية أثناء الحمل 🥑🚫",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = SoftTheme.SoftPink
            )
            Text(
                text = "محرك بحث وتصنيف فوري لما هو آمن، مشروط، أو ممنوع على صحتكِ وصحة جنينكِ",
                style = MaterialTheme.typography.bodySmall,
                color = SoftTheme.SoftGray
            )
        }

        // Search bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("ابحثي عن أي طعام أو شراب أو عشبة...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SoftTheme.SoftPink) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SoftTheme.SoftPink,
                    unfocusedBorderColor = SoftTheme.SoftGray.copy(alpha = 0.5f),
                    focusedTextColor = SoftTheme.TextWhite,
                    unfocusedTextColor = SoftTheme.TextWhite
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )
        }

        // Filter chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "ALL" to "الكل 🍽️",
                    "SAFE" to "آمن تماماً ✨",
                    "CAUTION" to "بحذر واعتدال ⚠️",
                    "FORBIDDEN" to "ممنوع وتجنبيه 🚫"
                ).forEach { (fKey, fLabel) ->
                    val isSelected = selectedFilter == fKey
                    val chipColor = when (fKey) {
                        "SAFE" -> SoftTheme.MintTeal
                        "CAUTION" -> Color(0xFFFFB74D)
                        "FORBIDDEN" -> SoftTheme.RedDanger
                        else -> SoftTheme.SoftPink
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) chipColor else SoftTheme.CardSlate,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedFilter = fKey }
                    ) {
                        Text(
                            text = fLabel,
                            color = if (isSelected) Color.White else SoftTheme.TextWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    }
                }
            }
        }

        // Items
        items(filteredList, key = { it.name }) { item ->
            val statusColor = when (item.safetyLevel) {
                "SAFE" -> SoftTheme.MintTeal
                "CAUTION" -> Color(0xFFFFB74D)
                else -> SoftTheme.RedDanger
            }
            val statusLabel = when (item.safetyLevel) {
                "SAFE" -> "آمن ومفيد ✨"
                "CAUTION" -> "مسموح باعتدال ⚠️"
                else -> "يجب تجنبه تماماً 🚫"
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, statusColor.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(statusColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(item.icon, fontSize = 22.sp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = SoftTheme.TextWhite
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = statusColor.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = statusLabel,
                                    color = statusColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = item.reason,
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

// ==========================================
// 💾 BACKUP & RESTORE SUB-SCREEN
// ==========================================
@Composable
fun BackupRestoreSubScreen(
    viewModel: WomanCompanionViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var exportedJsonText by remember { mutableStateOf("") }
    var importInputText by remember { mutableStateOf("") }
    var importStatusMessage by remember { mutableStateOf<String?>(null) }
    var showImportConfirmDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, SoftTheme.MintTeal.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("💾", fontSize = 24.sp)
                    Column {
                        Text(
                            text = "النسخ الاحتياطي وتصدير البيانات 📤",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.TextWhite
                        )
                        Text(
                            text = "احفظي نسخة كاملة من سجلاتك الطبية وأدويتك وقراءات ضغطك",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTheme.SoftGray
                        )
                    }
                }

                Button(
                    onClick = {
                        val json = viewModel.exportDataAsJson()
                        exportedJsonText = json
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, json)
                            putExtra(Intent.EXTRA_TITLE, "نسخة احتياطية - رفيقة المرأة")
                            type = "application/json"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "حفظ أو إرسال ملف النسخة الاحتياطية عبر:")
                        context.startActivity(shareIntent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.MintTeal),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("تصدير ومشاركة النسخة الاحتياطية 📤", fontWeight = FontWeight.Bold)
                }

                if (exportedJsonText.isNotBlank()) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Backup JSON", exportedJsonText)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "تم نسخ نص النسخة الاحتياطية بنجاح 📋", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftTheme.TextWhite),
                        border = BorderStroke(1.dp, SoftTheme.SoftGray.copy(alpha = 0.4f))
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("نسخ كود النسخة الاحتياطية 📋")
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, SoftTheme.SoftPink.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("📥", fontSize = 24.sp)
                    Column {
                        Text(
                            text = "استعادة البيانات من نسخة احتياطية",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.SoftPink
                        )
                        Text(
                            text = "الصقي نص النسخة الاحتياطية لاسترجاع سجلاتك على أي جهاز",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTheme.SoftGray
                        )
                    }
                }

                OutlinedTextField(
                    value = importInputText,
                    onValueChange = { importInputText = it },
                    label = { Text("الصقي نص النسخة الاحتياطية (JSON) هنا") },
                    placeholder = { Text("{\"version\": 1, ...}") },
                    minLines = 4,
                    maxLines = 8,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SoftTheme.SoftPink,
                        unfocusedBorderColor = SoftTheme.SoftGray,
                        focusedTextColor = SoftTheme.TextWhite,
                        unfocusedTextColor = SoftTheme.TextWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                importStatusMessage?.let { msg ->
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (msg.contains("نجاح")) SoftTheme.MintTeal else SoftTheme.SoftPink,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = { showImportConfirmDialog = true },
                    enabled = importInputText.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("استعادة وإدراج البيانات الآن 🔄", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showImportConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showImportConfirmDialog = false },
            title = { Text("تأكيد استعادة البيانات ⚠️", fontWeight = FontWeight.Bold, color = SoftTheme.SoftPink) },
            text = { Text("سيتم قراءة البيانات من النسخة المدخلة وإضافتها إلى سجلاتك الطبية والحمل والأدوية. هل ترغبين في المتابعة؟", color = SoftTheme.TextWhite) },
            confirmButton = {
                Button(
                    onClick = {
                        showImportConfirmDialog = false
                        coroutineScope.launch {
                            val res = viewModel.importDataFromJson(importInputText)
                            res.onSuccess { count ->
                                importStatusMessage = "تم استعادة $count سجلاً بنجاح! ✓"
                                importInputText = ""
                                Toast.makeText(context, "تمت الاستعادة بنجاح ($count عنصر)", Toast.LENGTH_LONG).show()
                            }.onFailure { _ ->
                                importStatusMessage = "فشل في استعادة البيانات: صيغة الملف غير صالحة"
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink)
                ) {
                    Text("تأكيد الاستعادة ✓")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportConfirmDialog = false }) {
                    Text("إلغاء", color = SoftTheme.SoftGray)
                }
            },
            containerColor = SoftTheme.CardSlate
        )
    }
}
