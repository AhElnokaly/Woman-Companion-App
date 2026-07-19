package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        PregnancyEntity::class,
        PeriodLog::class,
        WaterLog::class,
        NutritionLog::class,
        MedicationLog::class,
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
        FetalGrowthLog::class
    ],
    version = 14,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun womanCompanionDao(): WomanCompanionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "woman_companion_database"
                )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
