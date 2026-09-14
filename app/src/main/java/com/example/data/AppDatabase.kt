package com.example.data

import android.content.Context
import android.util.Base64
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import net.sqlcipher.database.SupportFactory
import java.security.SecureRandom

val MIGRATION_14_15 = object : Migration(14, 15) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `medication_adherence_logs` (" +
            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "`medicationId` INTEGER NOT NULL, " +
            "`scheduledTime` INTEGER NOT NULL, " +
            "`actualTime` INTEGER, " +
            "`status` TEXT NOT NULL, " +
            "FOREIGN KEY(`medicationId`) REFERENCES `medications`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_medication_adherence_logs_medicationId` ON `medication_adherence_logs` (`medicationId`)")
    }
}

val MIGRATION_15_16 = object : Migration(15, 16) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1. Recreate pregnancy table with autoGenerate id and isActive column
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `pregnancy_new` (" +
            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "`lastPeriodDate` INTEGER, " +
            "`dueDate` INTEGER, " +
            "`babyName` TEXT, " +
            "`prePregnancyWeight` REAL, " +
            "`heightCm` REAL, " +
            "`bmiCategory` TEXT, " +
            "`motherName` TEXT, " +
            "`userPhase` TEXT, " +
            "`birthDate` INTEGER, " +
            "`age` INTEGER, " +
            "`nickname` TEXT, " +
            "`hasHighBp` INTEGER NOT NULL DEFAULT 0, " +
            "`hasLowBp` INTEGER NOT NULL DEFAULT 0, " +
            "`hasDiabetes` INTEGER NOT NULL DEFAULT 0, " +
            "`chronicOthers` TEXT, " +
            "`lastPeriodEndDate` INTEGER, " +
            "`isPregnant` INTEGER NOT NULL DEFAULT 0, " +
            "`isOnboardingCompleted` INTEGER NOT NULL DEFAULT 0, " +
            "`babyGender` TEXT, " +
            "`birthMethod` TEXT, " +
            "`isDelivered` INTEGER NOT NULL DEFAULT 0, " +
            "`isActive` INTEGER NOT NULL DEFAULT 1)"
        )
        db.execSQL(
            "INSERT INTO `pregnancy_new` (" +
            "id, lastPeriodDate, dueDate, babyName, prePregnancyWeight, heightCm, bmiCategory, " +
            "motherName, userPhase, birthDate, age, nickname, hasHighBp, hasLowBp, hasDiabetes, " +
            "chronicOthers, lastPeriodEndDate, isPregnant, isOnboardingCompleted, babyGender, birthMethod, isDelivered, isActive) " +
            "SELECT id, lastPeriodDate, dueDate, babyName, prePregnancyWeight, heightCm, bmiCategory, " +
            "motherName, userPhase, birthDate, age, nickname, hasHighBp, hasLowBp, hasDiabetes, " +
            "chronicOthers, lastPeriodEndDate, isPregnant, isOnboardingCompleted, babyGender, birthMethod, isDelivered, 1 FROM `pregnancy`"
        )
        db.execSQL("DROP TABLE `pregnancy`")
        db.execSQL("ALTER TABLE `pregnancy_new` RENAME TO `pregnancy`")

        // 2. Recreate fetal_growth_logs with pregnancyId foreign key.
        // Look up the pre-existing pregnancy.id dynamically (COALESCE to 1 if no row exists)
        // rather than assuming id is always 1.
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `fetal_growth_logs_new` (" +
            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "`pregnancyId` INTEGER NOT NULL DEFAULT 1, " +
            "`date` INTEGER NOT NULL, " +
            "`pregnancyWeek` INTEGER NOT NULL, " +
            "`weightGrams` REAL NOT NULL, " +
            "`lengthCm` REAL NOT NULL, " +
            "`notes` TEXT, " +
            "FOREIGN KEY(`pregnancyId`) REFERENCES `pregnancy`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_fetal_growth_logs_pregnancyId` ON `fetal_growth_logs_new` (`pregnancyId`)")
        db.execSQL(
            "INSERT INTO `fetal_growth_logs_new` (id, pregnancyId, date, pregnancyWeek, weightGrams, lengthCm, notes) " +
            "SELECT id, COALESCE((SELECT id FROM pregnancy ORDER BY id LIMIT 1), 1), date, pregnancyWeek, weightGrams, lengthCm, notes FROM `fetal_growth_logs`"
        )
        db.execSQL("DROP TABLE `fetal_growth_logs`")
        db.execSQL("ALTER TABLE `fetal_growth_logs_new` RENAME TO `fetal_growth_logs`")
    }
}

val MIGRATION_16_17 = object : Migration(16, 17) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `contraceptive_methods` (" +
            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "`type` TEXT NOT NULL, " +
            "`startDate` INTEGER NOT NULL, " +
            "`endDate` INTEGER, " +
            "`notes` TEXT)"
        )
    }
}

val MIGRATION_17_18 = object : Migration(17, 18) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `maternal_weight_logs` (" +
            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "`pregnancyId` INTEGER NOT NULL, " +
            "`date` INTEGER NOT NULL, " +
            "`pregnancyWeek` INTEGER NOT NULL, " +
            "`weightKg` REAL NOT NULL, " +
            "`notes` TEXT, " +
            "FOREIGN KEY(`pregnancyId`) REFERENCES `pregnancy`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_maternal_weight_logs_pregnancyId` ON `maternal_weight_logs` (`pregnancyId`)")
    }
}

// Version 18 ships with explicit MIGRATION_17_18.
@Database(
    entities = [
        PregnancyEntity::class,
        PeriodLog::class,
        WaterLog::class,
        NutritionLog::class,
        MedicationLog::class,
        MedicationAdherenceLog::class,
        SymptomLog::class,
        FetalKickSession::class,
        ContractionLog::class,
        Appointment::class,
        JournalEntry::class,
        QadaFast::class,
        AppLockSettings::class,
        StepLog::class,
        BloodPressureLog::class,
        CachedQA::class,
        SleepLog::class,
        PartnerMessage::class,
        CravingLog::class,
        MaonatyInventoryItem::class,
        MaonatyShoppingItem::class,
        MaonatyHouseholdTask::class,
        FetalGrowthLog::class,
        ContraceptiveMethod::class,
        MaternalWeightLog::class
    ],
    version = 18,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun womanCompanionDao(): WomanCompanionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        internal fun getDatabasePassphrase(context: Context): ByteArray {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val sharedPreferences = EncryptedSharedPreferences.create(
                context,
                "secure_db_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            var keyString = sharedPreferences.getString("db_passphrase", null)
            if (keyString == null) {
                val randomBytes = ByteArray(32)
                SecureRandom().nextBytes(randomBytes)
                keyString = Base64.encodeToString(randomBytes, Base64.DEFAULT)
                sharedPreferences.edit().putString("db_passphrase", keyString).apply()
            }

            return Base64.decode(keyString, Base64.DEFAULT)
        }

        fun closeDatabase() {
            synchronized(this) {
                if (INSTANCE?.isOpen == true) {
                    INSTANCE?.close()
                }
                INSTANCE = null
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val passphrase = getDatabasePassphrase(context.applicationContext)
                val factory = SupportFactory(passphrase)

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "woman_companion_database"
                )
                .openHelperFactory(factory)
                .addMigrations(MIGRATION_14_15, MIGRATION_15_16, MIGRATION_16_17, MIGRATION_17_18)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        db.execSQL("PRAGMA foreign_keys = ON;")
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
