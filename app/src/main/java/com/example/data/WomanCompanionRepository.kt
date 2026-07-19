package com.example.data

import kotlinx.coroutines.flow.Flow

class WomanCompanionRepository(private val dao: WomanCompanionDao) {

    // --- Pregnancy ---
    val pregnancyFlow: Flow<PregnancyEntity?> = dao.getPregnancyFlow()
    suspend fun getPregnancy(): PregnancyEntity? = dao.getPregnancy()
    suspend fun savePregnancy(pregnancy: PregnancyEntity) = dao.insertOrUpdatePregnancy(pregnancy)
    suspend fun deletePregnancy() = dao.deletePregnancy()

    // --- Period Logs ---
    val allPeriodLogsFlow: Flow<List<PeriodLog>> = dao.getAllPeriodLogsFlow()
    suspend fun getAllPeriodLogs(): List<PeriodLog> = dao.getAllPeriodLogs()
    suspend fun insertPeriodLog(log: PeriodLog) = dao.insertPeriodLog(log)
    suspend fun deletePeriodLog(log: PeriodLog) = dao.deletePeriodLog(log)

    // --- Water Logs ---
    val allWaterLogsFlow: Flow<List<WaterLog>> = dao.getAllWaterLogsFlow()
    fun getWaterLogForDateFlow(date: Long): Flow<WaterLog?> = dao.getWaterLogForDateFlow(date)
    suspend fun getWaterLogForDate(date: Long): WaterLog? = dao.getWaterLogForDate(date)
    suspend fun insertWaterLog(log: WaterLog) = dao.insertWaterLog(log)

    // --- Nutrition Logs ---
    val allNutritionLogsFlow: Flow<List<NutritionLog>> = dao.getAllNutritionLogsFlow()
    fun getNutritionLogsForDayFlow(startOfDay: Long, endOfDay: Long): Flow<List<NutritionLog>> =
        dao.getNutritionLogsForDayFlow(startOfDay, endOfDay)
    suspend fun insertNutritionLog(log: NutritionLog) = dao.insertNutritionLog(log)
    suspend fun deleteNutritionLog(log: NutritionLog) = dao.deleteNutritionLog(log)

    // --- Medications ---
    val allMedicationsFlow: Flow<List<MedicationLog>> = dao.getAllMedicationsFlow()
    val activeMedicationsFlow: Flow<List<MedicationLog>> = dao.getActiveMedicationsFlow()
    suspend fun insertMedication(medication: MedicationLog) = dao.insertMedication(medication)
    suspend fun deleteMedication(medication: MedicationLog) = dao.deleteMedication(medication)

    // --- Symptoms ---
    val allSymptomLogsFlow: Flow<List<SymptomLog>> = dao.getAllSymptomLogsFlow()
    suspend fun insertSymptomLog(log: SymptomLog) = dao.insertSymptomLog(log)
    suspend fun deleteSymptomLog(log: SymptomLog) = dao.deleteSymptomLog(log)

    // --- Blood Pressure Logs ---
    val allBloodPressureLogsFlow: Flow<List<BloodPressureLog>> = dao.getAllBloodPressureLogsFlow()
    suspend fun insertBloodPressureLog(log: BloodPressureLog) = dao.insertBloodPressureLog(log)
    suspend fun deleteBloodPressureLog(log: BloodPressureLog) = dao.deleteBloodPressureLog(log)

    // --- Fetal Kicks ---
    val allFetalKickSessionsFlow: Flow<List<FetalKickSession>> = dao.getAllFetalKickSessionsFlow()
    suspend fun insertFetalKickSession(session: FetalKickSession) = dao.insertFetalKickSession(session)
    suspend fun deleteFetalKickSession(session: FetalKickSession) = dao.deleteFetalKickSession(session)

    // --- Contractions ---
    val allContractionLogsFlow: Flow<List<ContractionLog>> = dao.getAllContractionLogsFlow()
    suspend fun insertContractionLog(log: ContractionLog) = dao.insertContractionLog(log)
    suspend fun clearAllContractions() = dao.clearAllContractions()

    // --- Appointments ---
    val allAppointmentsFlow: Flow<List<Appointment>> = dao.getAllAppointmentsFlow()
    suspend fun insertAppointment(appointment: Appointment) = dao.insertAppointment(appointment)
    suspend fun deleteAppointment(appointment: Appointment) = dao.deleteAppointment(appointment)

    // --- Journal ---
    val allJournalEntriesFlow: Flow<List<JournalEntry>> = dao.getAllJournalEntriesFlow()
    suspend fun insertJournalEntry(entry: JournalEntry) = dao.insertJournalEntry(entry)
    suspend fun deleteJournalEntry(entry: JournalEntry) = dao.deleteJournalEntry(entry)

    // --- Qada Fasts ---
    val allQadaFastsFlow: Flow<List<QadaFast>> = dao.getAllQadaFastsFlow()
    suspend fun insertQadaFast(fast: QadaFast) = dao.insertQadaFast(fast)
    suspend fun deleteQadaFast(fast: QadaFast) = dao.deleteQadaFast(fast)

    // --- App Lock ---
    val appLockSettingsFlow: Flow<AppLockSettings?> = dao.getAppLockSettingsFlow()
    suspend fun getAppLockSettings(): AppLockSettings? = dao.getAppLockSettings()
    suspend fun saveAppLockSettings(settings: AppLockSettings) = dao.insertAppLockSettings(settings)

    // --- Step Logs ---
    val allStepLogsFlow: Flow<List<StepLog>> = dao.getAllStepLogsFlow()
    fun getStepLogForDateFlow(date: Long): Flow<StepLog?> = dao.getStepLogForDateFlow(date)
    suspend fun getStepLogForDate(date: Long): StepLog? = dao.getStepLogForDate(date)
    suspend fun insertStepLog(log: StepLog) = dao.insertStepLog(log)

    // --- Cached Q&A ---
    suspend fun getCachedQA(key: String): CachedQA? = dao.getCachedQA(key)
    suspend fun insertCachedQA(qa: CachedQA) = dao.insertCachedQA(qa)
    suspend fun clearCachedQA() = dao.clearCachedQA()

    // --- السجل الذكي لتحليل النوم (Smart Sleep Analyzer) ---
    val allSleepLogsFlow: Flow<List<SleepLog>> = dao.getAllSleepLogsFlow()
    suspend fun insertSleepLog(log: SleepLog) = dao.insertSleepLog(log)
    suspend fun deleteSleepLog(log: SleepLog) = dao.deleteSleepLog(log)
    suspend fun clearSleepLogs() = dao.clearSleepLogs()

    // --- رسائل ربط الشريك (Companion Sync Messages) ---
    val allPartnerMessagesFlow: Flow<List<PartnerMessage>> = dao.getAllPartnerMessagesFlow()
    suspend fun insertPartnerMessage(msg: PartnerMessage) = dao.insertPartnerMessage(msg)
    suspend fun markPartnerMessageAsRead(id: Int) = dao.markPartnerMessageAsRead(id)
    suspend fun clearPartnerMessages() = dao.clearPartnerMessages()

    // --- سجل الوحم والاشتهاء (Pregnancy Cravings Log) ---
    val allCravingLogsFlow: Flow<List<CravingLog>> = dao.getAllCravingLogsFlow()
    suspend fun insertCravingLog(log: CravingLog) = dao.insertCravingLog(log)
    suspend fun deleteCravingLog(log: CravingLog) = dao.deleteCravingLog(log)
    suspend fun clearCravingLogs() = dao.clearCravingLogs()

    // --- مؤونتي (Maonaty Inventory, Shopping, Tasks) ---
    val allInventoryItemsFlow: Flow<List<MaonatyInventoryItem>> = dao.getAllInventoryItemsFlow()
    suspend fun insertInventoryItem(item: MaonatyInventoryItem) = dao.insertInventoryItem(item)
    suspend fun deleteInventoryItem(item: MaonatyInventoryItem) = dao.deleteInventoryItem(item)
    suspend fun clearInventory() = dao.clearInventory()

    val allShoppingItemsFlow: Flow<List<MaonatyShoppingItem>> = dao.getAllShoppingItemsFlow()
    suspend fun insertShoppingItem(item: MaonatyShoppingItem) = dao.insertShoppingItem(item)
    suspend fun deleteShoppingItem(item: MaonatyShoppingItem) = dao.deleteShoppingItem(item)
    suspend fun updateShoppingItemStatus(id: Int, isBought: Boolean) = dao.updateShoppingItemStatus(id, isBought)
    suspend fun clearShoppingList() = dao.clearShoppingList()

    val allHouseholdTasksFlow: Flow<List<MaonatyHouseholdTask>> = dao.getAllHouseholdTasksFlow()
    suspend fun insertHouseholdTask(task: MaonatyHouseholdTask) = dao.insertHouseholdTask(task)
    suspend fun deleteHouseholdTask(task: MaonatyHouseholdTask) = dao.deleteHouseholdTask(task)
    suspend fun updateTaskStatus(id: Int, isCompleted: Boolean) = dao.updateTaskStatus(id, isCompleted)
    suspend fun clearHouseholdTasks() = dao.clearHouseholdTasks()

    // --- تتبع نمو الجنين (Fetal Growth Tracker) ---
    val allFetalGrowthLogsFlow: Flow<List<FetalGrowthLog>> = dao.getAllFetalGrowthLogsFlow()
    suspend fun insertFetalGrowthLog(log: FetalGrowthLog) = dao.insertFetalGrowthLog(log)
    suspend fun deleteFetalGrowthLog(log: FetalGrowthLog) = dao.deleteFetalGrowthLog(log)
    suspend fun clearFetalGrowthLogs() = dao.clearFetalGrowthLogs()

    // --- Factory Reset ---
    suspend fun factoryReset() {
        dao.deletePregnancy()
        dao.clearPeriodLogs()
        dao.clearWaterLogs()
        dao.clearNutritionLogs()
        dao.clearMedications()
        dao.clearSymptomLogs()
        dao.clearBloodPressureLogs()
        dao.clearFetalKickSessions()
        dao.clearContractionLogs()
        dao.clearAppointments()
        dao.clearJournalEntries()
        dao.clearQadaFasts()
        dao.clearStepLogs()
        dao.clearCachedQA()
        dao.clearSleepLogs()
        dao.clearPartnerMessages()
        dao.clearCravingLogs()
        dao.clearInventory()
        dao.clearShoppingList()
        dao.clearHouseholdTasks()
        dao.clearFetalGrowthLogs()
        // Reset app lock settings to default
        dao.insertAppLockSettings(AppLockSettings(pinHash = null, isLockEnabled = false, isStealthModeEnabled = false, companionName = "جوري", dailyStepTarget = 6000))
    }
}
