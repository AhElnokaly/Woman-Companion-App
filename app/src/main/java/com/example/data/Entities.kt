package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pregnancy")
data class PregnancyEntity(
    @PrimaryKey val id: Int = 1,
    val lastPeriodDate: Long? = null,
    val dueDate: Long? = null,
    val babyName: String? = null,
    val prePregnancyWeight: Double? = null,
    val heightCm: Double? = null,
    val bmiCategory: String? = null,
    val motherName: String? = null,
    val userPhase: String? = null,
    val birthDate: Long? = null,
    val age: Int? = null,
    // +++ أضيف بناءً على طلبك لتسجيل بيانات التهيئة والأسئلة الصحية +++
    val nickname: String? = null,
    val hasHighBp: Boolean = false,
    val hasLowBp: Boolean = false,
    val hasDiabetes: Boolean = false,
    val chronicOthers: String? = null,
    val lastPeriodEndDate: Long? = null,
    val isPregnant: Boolean = false,
    val isOnboardingCompleted: Boolean = false,
    // +++ أضيف بناءً على طلبك لتسجيل جنس الجنين والاسم المقترح وطريقة الولادة +++
    val babyGender: String? = null,
    val birthMethod: String? = null,
    val isDelivered: Boolean = false
)

@Entity(tableName = "period_logs")
data class PeriodLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val startDate: Long,
    val endDate: Long? = null,
    val flowIntensity: String, // light, medium, heavy
    val symptoms: String, // Comma separated list (e.g. "Cramps, Fatigue")
    val painLevel: Int, // 1 to 10
    val notes: String? = null
)

@Entity(tableName = "water_logs")
data class WaterLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long, // Start of day timestamp
    val amountMl: Int
)

@Entity(tableName = "nutrition_logs")
data class NutritionLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long,
    val mealType: String, // Breakfast, Lunch, Dinner, Snack
    val description: String,
    val calories: Int,
    val ironMg: Double = 0.0,
    val folateMcg: Double = 0.0,
    val calciumMg: Double = 0.0,
    val omega3G: Double = 0.0,
    val proteinG: Double = 0.0,
    val carbsG: Double = 0.0,
    val fatG: Double = 0.0,
    val sugarG: Double = 0.0,
    val fiberG: Double = 0.0,
    val waterBenefitMl: Int = 0,
    val potassiumMg: Double = 0.0,
    val sodiumMg: Double = 0.0,
    val magnesiumMg: Double = 0.0,
    val vitaminC_Mg: Double = 0.0,
    val vitaminA_Mcg: Double = 0.0
)

@Entity(tableName = "medications")
data class MedicationLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val dosage: String? = null,
    val timesPerDay: Int = 1,
    val prescribedBy: String? = null,
    val notes: String? = null,
    val startDate: Long? = null,
    val isActive: Boolean = true,
    // --- امتداد الخزانة الدوائية الذكية (Home Pharmacy Upgrade) ---
    val expiryDate: Long? = null,        // تاريخ الصلاحية
    val totalQuantity: Int = 0,          // الكمية الكلية المشتراة
    val remainingQuantity: Int = 0,      // الكمية المتبقية بالوحدة
    val safetyWarning: String? = null     // تحذير طبي مخصص (آمن أثناء الحمل، استشارة طبيب، إلخ)
)

// --- الكيان الجديد: السجل الذكي لتحليل النوم (Smart Sleep Analyzer) ---
@Entity(tableName = "sleep_logs")
data class SleepLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long,                     // تاريخ الليلة (بداية اليوم)
    val startTime: Long,                // وقت بداية النوم (بالمللي ثانية)
    val endTime: Long,                  // وقت الاستيقاظ (بالمللي ثانية)
    val qualityScore: Int,              // تقييم الجودة من 1 إلى 100
    val deepSleepMinutes: Int = 0,      // دقائق النوم العميق
    val lightSleepMinutes: Int = 0,     // دقائق النوم الخفيف
    val remSleepMinutes: Int = 0,       // دقائق نوم حركة العين السريعة
    val awakeningsCount: Int = 0,       // عدد مرات الاستيقاظ أثناء الليل
    val notes: String? = null           // ملاحظات (شربت قهوة قبل النوم، أحلام يقظة، إلخ)
)

// --- الكيان الجديد: رسائل ربط الشريك والرفيق (Companion Sync Link Messages) ---
@Entity(tableName = "partner_messages")
data class PartnerMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val senderName: String,             // الشريك / الرفيق (الزوج، الأم، إلخ)
    val messageText: String,            // نص الرسالة التشجيعية أو الاستفسارية
    val isRead: Boolean = false,
    val category: String = "Support"    // الفئة: Support, Alert, WaterReminder, KickCheck
)

@Entity(tableName = "symptom_logs")
data class SymptomLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long,
    val symptom: String,
    val severity: Int, // 1 to 10
    val notes: String? = null
)

@Entity(tableName = "fetal_kick_sessions")
data class FetalKickSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val startTime: Long,
    val endTime: Long,
    val kickCount: Int,
    val durationSeconds: Long
)

@Entity(tableName = "contraction_logs")
data class ContractionLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val startTime: Long,
    val endTime: Long,
    val durationSeconds: Long,
    val intervalSeconds: Long // Interval since previous contraction start
)

@Entity(tableName = "appointments")
data class Appointment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dateTime: Long,
    val title: String,
    val doctorName: String? = null,
    val notes: String? = null,
    val completed: Boolean = false
)

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long,
    val title: String? = null,
    val content: String,
    val mood: String? = null
)

@Entity(tableName = "qada_fasts")
data class QadaFast(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val yearHijri: Int,
    val missedDays: Int,
    val completedDays: Int = 0
)

@Entity(tableName = "app_lock_settings")
data class AppLockSettings(
    @PrimaryKey val id: Int = 1,
    val pinHash: String? = null,
    val isLockEnabled: Boolean = false,
    val isStealthModeEnabled: Boolean = false,
    val companionName: String = "جوري",
    val dailyStepTarget: Int = 6000,
    val isDarkMode: Boolean = true,
    val gitHubRepoUrl: String? = "https://raw.githubusercontent.com/your_username/your_repo/main/matrix.json",
    val userApiKey: String? = null
)

@Entity(tableName = "step_logs")
data class StepLog(
    @PrimaryKey val date: Long, // Start of day timestamp
    val steps: Int,
    val targetSteps: Int = 6000
)

@Entity(tableName = "blood_pressure_logs")
data class BloodPressureLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long,
    val systolic: Int,
    val diastolic: Int,
    val pulse: Int? = null,
    val notes: String? = null
)

// +++ أضيف بناءً على طلبك لتحديد مستوى سلامة الوجبات أثناء الحمل والرضاعة +++
enum class SafetyLevel {
    SAFE,
    CAUTION,
    AVOID
}

// +++ أضيف بناءً على طلبك لدعم منتقي الأطعمة الهرمي في وجبات اليوم +++
data class SubcategoryInfo(
    val name: String,
    val caloriesPerUnit: Int,
    val safety: SafetyLevel,
    val safetyAdvice: String?,
    val defaultAdditions: List<String>,
    val carbs: Double = 0.0,
    val protein: Double = 0.0,
    val fat: Double = 0.0,
    val iron: Double = 0.0,
    val calcium: Double = 0.0,
    val folate: Double = 0.0,
    val waterBenefit: Int = 0
)

// +++ أضيف بناءً على طلبك لذاكرة تخزين الأسئلة الشائعة وتوليد الإجابات الذكية محلياً وسحابياً +++
@Entity(tableName = "cached_qa")
data class CachedQA(
    @PrimaryKey val questionKey: String,
    val originalQuestion: String,
    val answerText: String,
    val timestamp: Long = System.currentTimeMillis()
)

// --- الكيان الجديد: سجل الوحم والاشتهاء الذكي (Smart Pregnancy Cravings Log) ---
@Entity(tableName = "craving_logs")
data class CravingLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long = System.currentTimeMillis(),
    val cravingItem: String,             // الأكلة أو المادة المتوحم عليها (مانجا، ليمون، إلخ)
    val cravingType: String = "Sweet",    // التصنيف: Sweet (حلو), Salty (حادق), Sour (حامض), Spicy (حار), Chocolate (شوكولاتة), NonFood (غير غذائي)
    val intensity: Int = 5,              // شدة الاشتهاء من 1 لـ 10
    val notes: String? = null            // ملاحظات أو شعورها
)

// --- كيانات مؤونتي (Maonaty Inventory, Shopping, Tasks) ---
@Entity(tableName = "maonaty_inventory")
data class MaonatyInventoryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String, // معلبات، بهارات، خضار وفواكه، لحوم ودواجن، منتجات ألبان، منظفات، أخرى
    val quantity: Double,
    val minQuantity: Double, // كمية حرجة لتوليد المشتريات تلقائياً
    val unit: String, // جرام، كيلوجرام، لتر، علبة، حبة، كيس، إلخ
    val priceEstimate: Double = 0.0 // السعر التقريبي للحبة/الوحدة
)

@Entity(tableName = "maonaty_shopping")
data class MaonatyShoppingItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String,
    val quantity: Double,
    val unit: String,
    val price: Double = 0.0,
    val isBought: Boolean = false,
    val autoGenerated: Boolean = false // هل تم توليدها تلقائياً من النواقص؟
)

@Entity(tableName = "maonaty_tasks")
data class MaonatyHouseholdTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String, // 🧼 تنظيف وترتيب، 🛠️ صيانة وأعطال، 📦 جرد وتخزين، 📅 شؤون منزلية
    val priority: String, // 🔴 عاجل، ⚡ متوسط، 🟢 عادي
    val dueDate: Long, // تاريخ الاستحقاق
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

// --- الكيان الجديد: تتبع نمو الجنين (Fetal Growth Tracker) ---
@Entity(tableName = "fetal_growth_logs")
data class FetalGrowthLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long = System.currentTimeMillis(),
    val pregnancyWeek: Int,              // أسبوع الحمل (مثلاً من 4 إلى 42)
    val weightGrams: Double,             // الوزن المدخل بالجرام (مثلاً 350.0 جرام)
    val lengthCm: Double,                // الطول المدخل بالسم (مثلاً 15.2 سم)
    val notes: String? = null            // ملاحظات أو تفاصيل زيارة الطبيب
)




