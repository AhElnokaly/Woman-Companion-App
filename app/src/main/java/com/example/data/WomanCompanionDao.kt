package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WomanCompanionDao {

    // --- Pregnancy ---
    @Query("SELECT * FROM pregnancy WHERE id = 1 LIMIT 1")
    fun getPregnancyFlow(): Flow<PregnancyEntity?>

    @Query("SELECT * FROM pregnancy WHERE id = 1 LIMIT 1")
    suspend fun getPregnancy(): PregnancyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePregnancy(pregnancy: PregnancyEntity)

    @Query("DELETE FROM pregnancy WHERE id = 1")
    suspend fun deletePregnancy()

    // --- Period Logs ---
    @Query("SELECT * FROM period_logs ORDER BY startDate DESC")
    fun getAllPeriodLogsFlow(): Flow<List<PeriodLog>>

    @Query("SELECT * FROM period_logs ORDER BY startDate DESC")
    suspend fun getAllPeriodLogs(): List<PeriodLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPeriodLog(log: PeriodLog)

    @Delete
    suspend fun deletePeriodLog(log: PeriodLog)

    // --- Water Logs ---
    @Query("SELECT * FROM water_logs WHERE date = :dateLimit LIMIT 1")
    suspend fun getWaterLogForDate(dateLimit: Long): WaterLog?

    @Query("SELECT * FROM water_logs WHERE date = :dateLimit LIMIT 1")
    fun getWaterLogForDateFlow(dateLimit: Long): Flow<WaterLog?>

    @Query("SELECT * FROM water_logs ORDER BY date DESC")
    fun getAllWaterLogsFlow(): Flow<List<WaterLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterLog(log: WaterLog)

    // --- Nutrition Logs ---
    @Query("SELECT * FROM nutrition_logs ORDER BY date DESC")
    fun getAllNutritionLogsFlow(): Flow<List<NutritionLog>>

    @Query("SELECT * FROM nutrition_logs WHERE date >= :startOfDay AND date < :endOfDay ORDER BY date DESC")
    fun getNutritionLogsForDayFlow(startOfDay: Long, endOfDay: Long): Flow<List<NutritionLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNutritionLog(log: NutritionLog)

    @Delete
    suspend fun deleteNutritionLog(log: NutritionLog)

    // --- Medications ---
    @Query("SELECT * FROM medications ORDER BY isActive DESC, name ASC")
    fun getAllMedicationsFlow(): Flow<List<MedicationLog>>

    @Query("SELECT * FROM medications WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveMedicationsFlow(): Flow<List<MedicationLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: MedicationLog)

    @Delete
    suspend fun deleteMedication(medication: MedicationLog)

    // --- Symptoms ---
    @Query("SELECT * FROM symptom_logs ORDER BY date DESC")
    fun getAllSymptomLogsFlow(): Flow<List<SymptomLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSymptomLog(log: SymptomLog)

    @Delete
    suspend fun deleteSymptomLog(log: SymptomLog)

    // --- Blood Pressure Logs ---
    @Query("SELECT * FROM blood_pressure_logs ORDER BY date DESC")
    fun getAllBloodPressureLogsFlow(): Flow<List<BloodPressureLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBloodPressureLog(log: BloodPressureLog)

    @Delete
    suspend fun deleteBloodPressureLog(log: BloodPressureLog)

    // --- Fetal Kicks ---
    @Query("SELECT * FROM fetal_kick_sessions ORDER BY startTime DESC")
    fun getAllFetalKickSessionsFlow(): Flow<List<FetalKickSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFetalKickSession(session: FetalKickSession)

    @Delete
    suspend fun deleteFetalKickSession(session: FetalKickSession)

    // --- Contractions ---
    @Query("SELECT * FROM contraction_logs ORDER BY startTime DESC")
    fun getAllContractionLogsFlow(): Flow<List<ContractionLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContractionLog(log: ContractionLog)

    @Query("DELETE FROM contraction_logs")
    suspend fun clearAllContractions()

    // --- Appointments ---
    @Query("SELECT * FROM appointments ORDER BY dateTime ASC")
    fun getAllAppointmentsFlow(): Flow<List<Appointment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: Appointment)

    @Delete
    suspend fun deleteAppointment(appointment: Appointment)

    // --- Journal Entries ---
    @Query("SELECT * FROM journal_entries ORDER BY date DESC")
    fun getAllJournalEntriesFlow(): Flow<List<JournalEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournalEntry(entry: JournalEntry)

    @Delete
    suspend fun deleteJournalEntry(entry: JournalEntry)

    // --- Qada Fasts ---
    @Query("SELECT * FROM qada_fasts ORDER BY yearHijri DESC")
    fun getAllQadaFastsFlow(): Flow<List<QadaFast>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQadaFast(fast: QadaFast)

    @Delete
    suspend fun deleteQadaFast(fast: QadaFast)

    // --- App Lock ---
    @Query("SELECT * FROM app_lock_settings WHERE id = 1 LIMIT 1")
    fun getAppLockSettingsFlow(): Flow<AppLockSettings?>

    @Query("SELECT * FROM app_lock_settings WHERE id = 1 LIMIT 1")
    suspend fun getAppLockSettings(): AppLockSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppLockSettings(settings: AppLockSettings)

    // --- Step Logs ---
    @Query("SELECT * FROM step_logs WHERE date = :dateLimit LIMIT 1")
    suspend fun getStepLogForDate(dateLimit: Long): StepLog?

    @Query("SELECT * FROM step_logs WHERE date = :dateLimit LIMIT 1")
    fun getStepLogForDateFlow(dateLimit: Long): Flow<StepLog?>

    @Query("SELECT * FROM step_logs ORDER BY date DESC")
    fun getAllStepLogsFlow(): Flow<List<StepLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStepLog(log: StepLog)

    @Query("DELETE FROM step_logs")
    suspend fun clearStepLogs()

    // --- Nuke/Reset Database ---
    @Query("DELETE FROM period_logs")
    suspend fun clearPeriodLogs()

    @Query("DELETE FROM water_logs")
    suspend fun clearWaterLogs()

    @Query("DELETE FROM nutrition_logs")
    suspend fun clearNutritionLogs()

    @Query("DELETE FROM medications")
    suspend fun clearMedications()

    @Query("DELETE FROM symptom_logs")
    suspend fun clearSymptomLogs()

    @Query("DELETE FROM blood_pressure_logs")
    suspend fun clearBloodPressureLogs()

    @Query("DELETE FROM fetal_kick_sessions")
    suspend fun clearFetalKickSessions()

    @Query("DELETE FROM contraction_logs")
    suspend fun clearContractionLogs()

    @Query("DELETE FROM appointments")
    suspend fun clearAppointments()

    @Query("DELETE FROM journal_entries")
    suspend fun clearJournalEntries()

    @Query("DELETE FROM qada_fasts")
    suspend fun clearQadaFasts()

    // --- Cached Q&A ---
    @Query("SELECT * FROM cached_qa WHERE questionKey = :key LIMIT 1")
    suspend fun getCachedQA(key: String): CachedQA?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedQA(qa: CachedQA)

    @Query("DELETE FROM cached_qa")
    suspend fun clearCachedQA()

    // --- السجل الذكي لتحليل النوم (Smart Sleep Analyzer) ---
    @Query("SELECT * FROM sleep_logs ORDER BY date DESC")
    fun getAllSleepLogsFlow(): Flow<List<SleepLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSleepLog(log: SleepLog)

    @Delete
    suspend fun deleteSleepLog(log: SleepLog)

    @Query("DELETE FROM sleep_logs")
    suspend fun clearSleepLogs()

    // --- رسائل ربط الشريك (Companion Sync Messages) ---
    @Query("SELECT * FROM partner_messages ORDER BY timestamp DESC")
    fun getAllPartnerMessagesFlow(): Flow<List<PartnerMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPartnerMessage(msg: PartnerMessage)

    @Query("UPDATE partner_messages SET isRead = 1 WHERE id = :id")
    suspend fun markPartnerMessageAsRead(id: Int)

    @Query("DELETE FROM partner_messages")
    suspend fun clearPartnerMessages()

    // --- سجل الوحم والاشتهاء (Pregnancy Cravings Log) ---
    @Query("SELECT * FROM craving_logs ORDER BY date DESC")
    fun getAllCravingLogsFlow(): Flow<List<CravingLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCravingLog(log: CravingLog)

    @Delete
    suspend fun deleteCravingLog(log: CravingLog)

    @Query("DELETE FROM craving_logs")
    suspend fun clearCravingLogs()

    // --- مؤونتي (Maonaty Inventory, Shopping, Tasks) ---
    @Query("SELECT * FROM maonaty_inventory ORDER BY category, name")
    fun getAllInventoryItemsFlow(): Flow<List<MaonatyInventoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItem(item: MaonatyInventoryItem)

    @Delete
    suspend fun deleteInventoryItem(item: MaonatyInventoryItem)

    @Query("DELETE FROM maonaty_inventory")
    suspend fun clearInventory()

    @Query("SELECT * FROM maonaty_shopping ORDER BY isBought ASC, category, name")
    fun getAllShoppingItemsFlow(): Flow<List<MaonatyShoppingItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoppingItem(item: MaonatyShoppingItem)

    @Delete
    suspend fun deleteShoppingItem(item: MaonatyShoppingItem)

    @Query("UPDATE maonaty_shopping SET isBought = :isBought WHERE id = :id")
    suspend fun updateShoppingItemStatus(id: Int, isBought: Boolean)

    @Query("DELETE FROM maonaty_shopping")
    suspend fun clearShoppingList()

    @Query("SELECT * FROM maonaty_tasks ORDER BY isCompleted ASC, dueDate ASC, priority DESC")
    fun getAllHouseholdTasksFlow(): Flow<List<MaonatyHouseholdTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHouseholdTask(task: MaonatyHouseholdTask)

    @Delete
    suspend fun deleteHouseholdTask(task: MaonatyHouseholdTask)

    @Query("UPDATE maonaty_tasks SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateTaskStatus(id: Int, isCompleted: Boolean)

    @Query("DELETE FROM maonaty_tasks")
    suspend fun clearHouseholdTasks()

    // --- تتبع نمو الجنين (Fetal Growth Tracker) ---
    @Query("SELECT * FROM fetal_growth_logs ORDER BY pregnancyWeek ASC")
    fun getAllFetalGrowthLogsFlow(): Flow<List<FetalGrowthLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFetalGrowthLog(log: FetalGrowthLog)

    @Delete
    suspend fun deleteFetalGrowthLog(log: FetalGrowthLog)

    @Query("DELETE FROM fetal_growth_logs")
    suspend fun clearFetalGrowthLogs()
}
