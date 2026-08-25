package com.example.ui.maonaty

import android.content.Context
import android.widget.Toast
import com.example.data.MaonatyInventoryItem

data class MaonatyRecipe(
    val name: String,
    val timeMinutes: Int,
    val difficulty: String,
    val instructions: String,
    val ingredients: List<RecipeIngredient>
)

data class RecipeIngredient(
    val name: String,
    val quantity: Double,
    val unit: String
)

data class IngredientMatchStatus(
    val name: String,
    val requiredQty: Double,
    val availableQty: Double,
    val unit: String,
    val isMatched: Boolean,
    val missingQty: Double
)

data class RecipeWithMatchStatus(
    val recipe: MaonatyRecipe,
    val matchStatuses: List<IngredientMatchStatus>,
    val matchPercentage: Int
)

val standardRecipes = listOf(
    MaonatyRecipe(
        name = "ملوخية مصرية بالدجاج 🍲",
        timeMinutes = 45,
        difficulty = "متوسط",
        instructions = "1. اسلقي صدور الدجاج مع البصل والبهارات لصنع مرقة دافئة.\n2. خرطي الملوخية جيداً حتى تصبح ناعمة.\n3. أضيفي الملوخية إلى المرقة الساخنة مع التقليب المستمر.\n4. حمري الثوم والكزبرة في السمنة لعمل الطشة، ثم اسكبيها فوق الملوخية وقدميها ساخنة.",
        ingredients = listOf(
            RecipeIngredient("صدور فراخ بانيه", 0.5, "كيلوجرام"),
            RecipeIngredient("بهارات لحمة مشكلة", 20.0, "جرام"),
            RecipeIngredient("ملح طعام ناعم", 1.0, "كيس")
        )
    ),
    MaonatyRecipe(
        name = "صينية بطاطس بالفراخ في الفرن 🥘",
        timeMinutes = 60,
        difficulty = "سهل",
        instructions = "1. قطعي البطاطس والبصل والطماطم لشرائح دائرية.\n2. تبلي صدور الدجاج بالملح والبهارات.\n3. رصي الخضار في صينية وأضيفي فوقه الدجاج والزيت والقليل من الماء.\n4. غطي الصينية بورق القصدير وضعيها في الفرن حتى تنضج تماماً ثم حمري وجهها.",
        ingredients = listOf(
            RecipeIngredient("طماطم طازجة", 1.0, "كيلوجرام"),
            RecipeIngredient("صدور فراخ بانيه", 1.0, "كيلوجرام"),
            RecipeIngredient("بهارات لحمة مشكلة", 20.0, "جرام"),
            RecipeIngredient("زيت عباد الشمس", 1.0, "لتر"),
            RecipeIngredient("ملح طعام ناعم", 1.0, "كيس")
        )
    ),
    MaonatyRecipe(
        name = "أرز أبيض مصري مفلفل 🍚",
        timeMinutes = 25,
        difficulty = "سهل",
        instructions = "1. اغسلي الأرز جيداً وصفيه من الماء.\n2. في حلة، سخني زيت عباد الشمس ثم أضيفي الأرز وشوحيه قليلاً.\n3. أضيفي الماء المغلي والملح واتركيه يغلي ثم هدي النار تماماً وغطيه حتى ينضج.",
        ingredients = listOf(
            RecipeIngredient("أرز مصري فاخر", 1.0, "كيلوجرام"),
            RecipeIngredient("زيت عباد الشمس", 1.0, "لتر"),
            RecipeIngredient("ملح طعام ناعم", 1.0, "كيس")
        )
    ),
    MaonatyRecipe(
        name = "سلطة خضراء متكاملة ومنعشة 🥗",
        timeMinutes = 10,
        difficulty = "سهل جداً",
        instructions = "1. قطعي الطماطم والخيار والخضار الورقي لقطع متوسطة.\n2. اعصري ليمونة طازجة فوق المكونات.\n3. أضيفي ملعقة زيت عباد الشمس ورشة ملح خفيفة ثم قلبيها جيداً وقدميها فوراً.",
        ingredients = listOf(
            RecipeIngredient("طماطم طازجة", 0.5, "كيلوجرام"),
            RecipeIngredient("ليمون أصفر", 0.5, "كيلوجرام"),
            RecipeIngredient("زيت عباد الشمس", 1.0, "لتر"),
            RecipeIngredient("ملح طعام ناعم", 1.0, "كيس")
        )
    ),
    MaonatyRecipe(
        name = "مكرونة قلم بالصلصة الحمراء 🍝",
        timeMinutes = 30,
        difficulty = "سهل",
        instructions = "1. اسلقي المكرونة في ماء مغلي مملح وصفيها.\n2. في حلة أخرى، شوحي الثوم المفروم مع زيت عباد الشمس.\n3. أضيفي عصير الطماطم والبهارات واتركيها لتتسبك.\n4. اخلطي المكرونة المسلوقة بالصلصة الدافئة وقدميها.",
        ingredients = listOf(
            RecipeIngredient("مكرونة قلم", 1.0, "كيس"),
            RecipeIngredient("طماطم طازجة", 1.0, "كيلوجرام"),
            RecipeIngredient("زيت عباد الشمس", 1.0, "لتر"),
            RecipeIngredient("ملح طعام ناعم", 1.0, "كيس")
        )
    ),
    MaonatyRecipe(
        name = "عشاء خفيف: جبنة فيتا بالطماطم والزيت 🧀🍅",
        timeMinutes = 5,
        difficulty = "سهل جداً",
        instructions = "1. في طبق واسع، قطعي الطماطم لقطع صغيرة جداً.\n2. أضيفي الجبنة البيضاء الفيتا واهرسيها بالشوكة.\n3. رشي زيت عباد الشمس واعصري القليل من الليمون لمذاق منعش ومغذي.",
        ingredients = listOf(
            RecipeIngredient("جبنة بيضاء فيتا", 0.5, "كيلوجرام"),
            RecipeIngredient("طماطم طازجة", 0.5, "كيلوجرام"),
            RecipeIngredient("زيت عباد الشمس", 1.0, "لتر"),
            RecipeIngredient("ليمون أصفر", 0.5, "كيلوجرام")
        )
    ),
    MaonatyRecipe(
        name = "شوربة خضار صحية دافئة 🍲🥦",
        timeMinutes = 35,
        difficulty = "سهل",
        instructions = "1. قطعي الخضار لقطع متوسطة الحجم.\n2. شوحي البصل والثوم بملعقة زيت ثم أضيفي الخضار وشوحيه قليلاً.\n3. أضيفي الماء الساخن والبهارات والملح ودعيه يغلي على نار هادئة حتى تمام النضج.",
        ingredients = listOf(
            RecipeIngredient("طماطم طازجة", 0.5, "كيلوجرام"),
            RecipeIngredient("ليمون أصفر", 0.5, "كيلوجرام"),
            RecipeIngredient("بهارات لحمة مشكلة", 20.0, "جرام"),
            RecipeIngredient("ملح طعام ناعم", 1.0, "كيس")
        )
    )
)

fun getIngredientMatchStatus(ingredient: RecipeIngredient, inventory: List<MaonatyInventoryItem>): IngredientMatchStatus {
    val matchedItem = inventory.find {
        it.name.trim().equals(ingredient.name.trim(), ignoreCase = true) ||
                it.name.trim().contains(ingredient.name.trim(), ignoreCase = true) ||
                ingredient.name.trim().contains(it.name.trim(), ignoreCase = true)
    }

    if (matchedItem == null) {
        return IngredientMatchStatus(
            name = ingredient.name,
            requiredQty = ingredient.quantity,
            availableQty = 0.0,
            unit = ingredient.unit,
            isMatched = false,
            missingQty = ingredient.quantity
        )
    }

    val available = matchedItem.quantity
    val isMatched = available >= ingredient.quantity
    val missing = if (isMatched) 0.0 else ingredient.quantity - available

    return IngredientMatchStatus(
        name = ingredient.name,
        requiredQty = ingredient.quantity,
        availableQty = available,
        unit = ingredient.unit,
        isMatched = isMatched,
        missingQty = missing
    )
}

object ScaffoldMessengerHelper {
    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}

data class PregnancyNutritionReference(
    val trimester: Int,
    val description: String,
    val totalCaloriesTargetKcal: Int,
    val extraCaloriesKcal: Int,
    val maxSodiumMg: Double,
    val targetIronMg: Double,
    val targetFolicAcidMcg: Double
) {
    companion object {
        fun getTargetForTrimester(trimester: Int): PregnancyNutritionReference {
            return when (trimester) {
                1 -> PregnancyNutritionReference(
                    trimester = 1,
                    description = "الثلث الأول: التركيز على المغذيات الدقيقة وحمض الفوليك",
                    totalCaloriesTargetKcal = 2000,
                    extraCaloriesKcal = 0,
                    maxSodiumMg = 2000.0,
                    targetIronMg = 27.0,
                    targetFolicAcidMcg = 600.0
                )
                2 -> PregnancyNutritionReference(
                    trimester = 2,
                    description = "الثلث الثاني: زيادة السعرات لدعم نمو أنسجة الجنين",
                    totalCaloriesTargetKcal = 2340,
                    extraCaloriesKcal = 340,
                    maxSodiumMg = 2000.0,
                    targetIronMg = 27.0,
                    targetFolicAcidMcg = 600.0
                )
                else -> PregnancyNutritionReference(
                    trimester = 3,
                    description = "الثلث الثالث: دعم وزن الجنين وتحضير الجسم للرضاعة",
                    totalCaloriesTargetKcal = 2450,
                    extraCaloriesKcal = 450,
                    maxSodiumMg = 2000.0,
                    targetIronMg = 27.0,
                    targetFolicAcidMcg = 600.0
                )
            }
        }
    }
}

