package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.reminder.ReminderScheduler
import com.example.widget.WomanCompanionAppWidget
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

class WomanCompanionViewModel(
    application: Application,
    val repository: WomanCompanionRepository
) : AndroidViewModel(application) {

    internal val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("WomanCompanionVM", "Unhandled exception in coroutine", throwable)
    }

    // Consolidated single SharedPreferences bucket for app-level flags
    internal val sharedPrefs = application.getSharedPreferences("woman_companion_prefs", Context.MODE_PRIVATE)

    private val _isUpdatesBannerDismissed = MutableStateFlow(
        sharedPrefs.getBoolean("new_updates_banner_dismissed_v2", false)
    )
    val isUpdatesBannerDismissed: StateFlow<Boolean> = _isUpdatesBannerDismissed.asStateFlow()

    fun dismissUpdatesBanner() {
        _isUpdatesBannerDismissed.value = true
        sharedPrefs.edit().putBoolean("new_updates_banner_dismissed_v2", true).apply()
    }

    private val _favoriteFoods = MutableStateFlow<Set<String>>(
        sharedPrefs.getStringSet("favorites", emptySet()) ?: emptySet()
    )
    val favoriteFoods: StateFlow<Set<String>> = _favoriteFoods.asStateFlow()

    private val _foodLogCounts = MutableStateFlow<Map<String, Int>>(
        sharedPrefs.all.filterKeys { it.startsWith("count_") }
            .map { (key, value) -> key.substringAfter("count_") to (value as? Int ?: 0) }
            .toMap()
    )
    val foodLogCounts: StateFlow<Map<String, Int>> = _foodLogCounts.asStateFlow()

    fun toggleFavoriteFood(foodName: String) {
        val current = _favoriteFoods.value.toMutableSet()
        if (current.contains(foodName)) {
            current.remove(foodName)
        } else {
            current.add(foodName)
        }
        _favoriteFoods.value = current
        sharedPrefs.edit().putStringSet("favorites", current).apply()
    }

    fun incrementFoodLogCount(foodName: String) {
        val current = _foodLogCounts.value.toMutableMap()
        val count = (current[foodName] ?: 0) + 1
        current[foodName] = count
        _foodLogCounts.value = current
        sharedPrefs.edit().putInt("count_$foodName", count).apply()
    }

    fun getLogCountLastTwoWeeks(foodName: String): Int {
        val logs = allNutritionLogsState.value
        val twoWeeksAgo = System.currentTimeMillis() - 14 * 24 * 60 * 60 * 1000L
        return logs.count { (it.description.contains(foodName) || foodName.contains(it.description)) && it.date >= twoWeeksAgo }
    }

    private val _completedWorkoutsCount = MutableStateFlow(sharedPrefs.getInt("completed_workouts", 0))
    val completedWorkoutsCount: StateFlow<Int> = _completedWorkoutsCount.asStateFlow()

    private val _workoutStreak = MutableStateFlow(sharedPrefs.getInt("workout_streak", 0))
    val workoutStreak: StateFlow<Int> = _workoutStreak.asStateFlow()

    fun logCompletedWorkout() {
        val count = _completedWorkoutsCount.value + 1
        _completedWorkoutsCount.value = count
        sharedPrefs.edit().putInt("completed_workouts", count).apply()

        val lastDate = sharedPrefs.getLong("last_workout_date", 0L)
        val today = getStartOfDay()
        val oneDayMs = 24 * 60 * 60 * 1000L

        var streak = _workoutStreak.value
        if (lastDate != 0L) {
            val diff = today - lastDate
            if (diff == 0L) {
                // Already did a workout today, streak remains same
            } else if (diff <= 2 * oneDayMs) {
                // Consecutive day (diff == 1 day) or forgiving grace day (diff == 2 days): streak continues gently
                streak += 1
            } else {
                // 2 or more consecutive missed days: gentle fresh start 🌱
                streak = 1
            }
        } else {
            // First workout
            streak = 1
        }

        _workoutStreak.value = streak
        sharedPrefs.edit()
            .putInt("workout_streak", streak)
            .putLong("last_workout_date", today)
            .apply()
    }

    val apiKeyRepository = ApiKeyRepository(application)
    val apiKeyFlow = apiKeyRepository.apiKeyFlow
    val apiBaseUrlFlow = apiKeyRepository.apiBaseUrlFlow
    val modelNameFlow = apiKeyRepository.modelNameFlow
    val cloudAiConsentFlow = apiKeyRepository.cloudAiConsentFlow

    private val _cloudAiConsentState = MutableStateFlow(false)
    val cloudAiConsentState: StateFlow<Boolean> = _cloudAiConsentState.asStateFlow()

    fun setCloudAiConsent(consented: Boolean) {
        viewModelScope.launch(coroutineExceptionHandler) {
            apiKeyRepository.setCloudAiConsent(consented)
            _cloudAiConsentState.value = consented
        }
    }

    private val _apiKeyTestStatus = MutableStateFlow<String?>(null) // "testing", "success", "error: <msg>", or null
    val apiKeyTestStatus: StateFlow<String?> = _apiKeyTestStatus.asStateFlow()

    fun testAndSaveApiKey(key: String, customBaseUrl: String? = null, customModel: String? = null) {
        _apiKeyTestStatus.value = "testing"
        viewModelScope.launch(coroutineExceptionHandler) {
            val trimmedKey = key.trim()
            val trimmedUrl = customBaseUrl?.trim() ?: "https://generativelanguage.googleapis.com/"
            val trimmedModel = customModel?.trim() ?: "gemini-3.5-flash"
            
            val result = GeminiService.testApiKey(trimmedKey, trimmedUrl, trimmedModel)
            if (result.first) {
                apiKeyRepository.saveKey(trimmedKey)
                apiKeyRepository.saveBaseUrl(trimmedUrl)
                apiKeyRepository.saveModelName(trimmedModel)
                _apiKeyTestStatus.value = "success"
            } else {
                _apiKeyTestStatus.value = "error: ${result.second}"
            }
        }
    }

    fun clearApiTestStatus() {
        _apiKeyTestStatus.value = null
    }

    fun saveApiKey(key: String, baseUrl: String, model: String) {
        viewModelScope.launch(coroutineExceptionHandler) {
            apiKeyRepository.saveKey(key.trim())
            apiKeyRepository.saveBaseUrl(baseUrl.trim())
            apiKeyRepository.saveModelName(model.trim())
        }
    }

    fun clearApiKey() {
        viewModelScope.launch(coroutineExceptionHandler) {
            apiKeyRepository.clearKey()
        }
    }

    private val connectivityManager = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    
    private val _isNetworkAvailable = MutableStateFlow(false)
    val isNetworkAvailable: StateFlow<Boolean> = _isNetworkAvailable.asStateFlow()

    // --- Cached Q&A helper methods ---
    suspend fun getCachedAnswer(question: String): String? {
        val normalized = com.example.data.EgyptianFoodRepository.normalizeText(question.trim().lowercase())
        return repository.getCachedQA(normalized)?.answerText
    }

    suspend fun saveCachedAnswer(question: String, answer: String) {
        val normalized = com.example.data.EgyptianFoodRepository.normalizeText(question.trim().lowercase())
        repository.insertCachedQA(com.example.data.CachedQA(questionKey = normalized, originalQuestion = question, answerText = answer))
    }

    // Current time helper
    private fun getCurrentTime(): Long = System.currentTimeMillis()

    // Today's start-of-day timestamp helper
    private fun getStartOfDay(timestamp: Long = getCurrentTime()): Long {
        val cal = Calendar.getInstance(TimeZone.getDefault())
        cal.timeInMillis = timestamp
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    // --- State Observables ---
    val pregnancyState: StateFlow<PregnancyEntity?> = repository.pregnancyFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allPregnanciesState: StateFlow<List<PregnancyEntity>> = repository.allPregnanciesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val periodLogsState: StateFlow<List<PeriodLog>> = repository.allPeriodLogsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWaterLogsState: StateFlow<List<WaterLog>> = repository.allWaterLogsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNutritionLogsState: StateFlow<List<NutritionLog>> = repository.allNutritionLogsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeMedicationsState: StateFlow<List<MedicationLog>> = repository.activeMedicationsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMedicationsState: StateFlow<List<MedicationLog>> = repository.allMedicationsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMedicationAdherenceLogsState: StateFlow<List<MedicationAdherenceLog>> = repository.allMedicationAdherenceLogsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val symptomLogsState: StateFlow<List<SymptomLog>> = repository.allSymptomLogsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bloodPressureLogsState: StateFlow<List<BloodPressureLog>> = repository.allBloodPressureLogsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val fetalKickSessionsState: StateFlow<List<FetalKickSession>> = repository.allFetalKickSessionsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val contractionLogsState: StateFlow<List<ContractionLog>> = repository.allContractionLogsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appointmentsState: StateFlow<List<Appointment>> = repository.allAppointmentsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val journalEntriesState: StateFlow<List<JournalEntry>> = repository.allJournalEntriesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allContraceptiveMethodsState: StateFlow<List<ContraceptiveMethod>> = repository.allContraceptiveMethods
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeContraceptiveMethodState: StateFlow<ContraceptiveMethod?> = repository.activeContraceptiveMethod
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val qadaFastsState: StateFlow<List<QadaFast>> = repository.allQadaFastsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appLockSettingsState: StateFlow<AppLockSettings?> = repository.appLockSettingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- السجل الذكي لتحليل النوم (Smart Sleep Analyzer States) ---
    val allSleepLogsState: StateFlow<List<SleepLog>> = repository.allSleepLogsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- رسائل ربط الشريك (Companion Sync Message States) ---
    val allPartnerMessagesState: StateFlow<List<PartnerMessage>> = repository.allPartnerMessagesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- سجل الوحم والاشتهاء (Pregnancy Cravings States) ---
    val allCravingLogsState: StateFlow<List<CravingLog>> = repository.allCravingLogsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Step Tracker States ---
    val allStepLogsState: StateFlow<List<StepLog>> = repository.allStepLogsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- مؤونتي (Maonaty States) ---
    val allInventoryItemsState: StateFlow<List<MaonatyInventoryItem>> = repository.allInventoryItemsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allShoppingItemsState: StateFlow<List<MaonatyShoppingItem>> = repository.allShoppingItemsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allHouseholdTasksState: StateFlow<List<MaonatyHouseholdTask>> = repository.allHouseholdTasksFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- تتبع نمو الجنين (Fetal Growth Tracker State) ---
    val allFetalGrowthLogsState: StateFlow<List<FetalGrowthLog>> = repository.allFetalGrowthLogsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- مدة النفاس بالأيام (Configurable Nifas Duration Days State) ---
    private val _nifasDurationDaysState = MutableStateFlow(sharedPrefs.getInt("nifas_duration_days", 40))
    val nifasDurationDaysState: StateFlow<Int> = _nifasDurationDaysState.asStateFlow()

    fun setNifasDurationDays(days: Int) {
        val validDays = days.coerceIn(1, 120)
        sharedPrefs.edit().putInt("nifas_duration_days", validDays).apply()
        _nifasDurationDaysState.value = validDays
    }

    // --- إظهار ذكريات الحمل السابقة (Show Past Pregnancy Memories State) ---
    private val _showPastPregnancyMemoriesState = MutableStateFlow(sharedPrefs.getBoolean("show_past_pregnancy_memories", true))
    val showPastPregnancyMemoriesState: StateFlow<Boolean> = _showPastPregnancyMemoriesState.asStateFlow()

    fun setShowPastPregnancyMemories(enabled: Boolean) {
        _showPastPregnancyMemoriesState.value = enabled
        sharedPrefs.edit().putBoolean("show_past_pregnancy_memories", enabled).apply()
    }

    // --- حالة شارة التصدير والنسخ الاحتياطي (Backup Export Achievement State) ---
    private val _hasExportedBackupState = MutableStateFlow(sharedPrefs.getBoolean("badge_backup_exported", false))
    val hasExportedBackupState: StateFlow<Boolean> = _hasExportedBackupState.asStateFlow()

    fun markBackupExported() {
        _hasExportedBackupState.value = true
        sharedPrefs.edit().putBoolean("badge_backup_exported", true).apply()
    }

    // --- حالة شارة تقرير الطبيبة (Doctor Report Achievement State) ---
    private val _hasGeneratedDoctorReportState = MutableStateFlow(sharedPrefs.getBoolean("badge_doctor_report_generated", false))
    val hasGeneratedDoctorReportState: StateFlow<Boolean> = _hasGeneratedDoctorReportState.asStateFlow()

    fun markDoctorReportGenerated() {
        _hasGeneratedDoctorReportState.value = true
        sharedPrefs.edit().putBoolean("badge_doctor_report_generated", true).apply()
    }

    // --- Active Tools SubScreen State (for cross-screen navigation/direction) ---
    private val _activeSubScreen = MutableStateFlow<String?>(null)
    val activeSubScreen: StateFlow<String?> = _activeSubScreen.asStateFlow()

    fun setActiveSubScreen(screen: String?) {
        _activeSubScreen.value = screen
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val currentDayTick: Flow<Long> = flow {
        while (true) {
            val today = getStartOfDay()
            emit(today)
            val nextMidnight = today + 24 * 60 * 60 * 1000L
            val delayMs = (nextMidnight - System.currentTimeMillis()).coerceAtLeast(1000L)
            kotlinx.coroutines.delay(delayMs)
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val todayStepLogState: StateFlow<StepLog?> = currentDayTick
        .flatMapLatest { today -> repository.getStepLogForDateFlow(today) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- Weather States ---
    private val _weatherState = MutableStateFlow<WeatherInfo?>(null)
    val weatherState: StateFlow<WeatherInfo?> = _weatherState.asStateFlow()

    // Current day's logs
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val todayWaterLogState: StateFlow<WaterLog?> = currentDayTick
        .flatMapLatest { today -> repository.getWaterLogForDateFlow(today) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val todayNutritionLogsState: StateFlow<List<NutritionLog>> = currentDayTick
        .flatMapLatest { start ->
            val end = start + 24 * 60 * 60 * 1000L
            repository.getNutritionLogsForDayFlow(start, end)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Dynamic UI State ---
    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    fun reinitializeSensors() {
        Log.d("ViewModel", "Sensors are managed by StepCounterService foreground service.")
    }

    private val _isGitHubUpdateAvailable = MutableStateFlow(false)
    val isGitHubUpdateAvailable: StateFlow<Boolean> = _isGitHubUpdateAvailable.asStateFlow()

    fun checkForGitHubUpdates() {
        viewModelScope.launch(coroutineExceptionHandler) {
            try {
                val settings = repository.getAppLockSettings() ?: return@launch
                val url = settings.gitHubRepoUrl
                if (url.isNullOrBlank()) return@launch
                val text = GitHubSyncRepository.syncJouriMatrixFromServer(url) ?: return@launch
                val currentHash = text.hashCode().toString()
                val prefs = getApplication<Application>().getSharedPreferences("woman_companion_prefs", Context.MODE_PRIVATE)
                val savedHash = prefs.getString("last_synced_hash", null)
                if (savedHash == null) {
                    // Baseline
                    prefs.edit().putString("last_synced_hash", currentHash).apply()
                    _isGitHubUpdateAvailable.value = false
                } else if (savedHash != currentHash) {
                    _isGitHubUpdateAvailable.value = true
                } else {
                    _isGitHubUpdateAvailable.value = false
                }
            } catch (e: Exception) {
                com.example.util.AppLogger.w("WomanCompanionViewModel", "Failed to check GitHub matrix updates", e)
            }
        }
    }

    init {
        // التحقق من استعادة ملف التعريف عند تشغيل التطبيق في حال اكتمال شاشة التهيئة مسبقًا
        viewModelScope.launch(coroutineExceptionHandler) {
            try {
                val existing = repository.getPregnancy()
                if (existing == null && sharedPrefs.getBoolean("onboarding_completed_v1", false)) {
                    val name = sharedPrefs.getString("backup_mother_name", "جميلة") ?: "جميلة"
                    val nickname = sharedPrefs.getString("backup_nickname", name) ?: name
                    val birthDate = if (sharedPrefs.contains("backup_birth_date")) sharedPrefs.getLong("backup_birth_date", 0L) else null
                    val isPregnant = sharedPrefs.getBoolean("backup_is_pregnant", true)
                    val lastPeriodDate = if (sharedPrefs.contains("backup_last_period_date")) sharedPrefs.getLong("backup_last_period_date", 0L) else null
                    val lastPeriodEndDate = if (sharedPrefs.contains("backup_last_period_end_date")) sharedPrefs.getLong("backup_last_period_end_date", 0L) else null
                    val hasHighBp = sharedPrefs.getBoolean("backup_has_high_bp", false)
                    val hasLowBp = sharedPrefs.getBoolean("backup_has_low_bp", false)
                    val hasDiabetes = sharedPrefs.getBoolean("backup_has_diabetes", false)
                    val chronicOthers = sharedPrefs.getString("backup_chronic_others", "") ?: ""

                    val computedAge = birthDate?.let { calculateAge(it) }
                    val dueDate = if (isPregnant && lastPeriodDate != null) {
                        lastPeriodDate + 280L * 24 * 60 * 60 * 1000
                    } else null

                    repository.savePregnancy(
                        PregnancyEntity(
                            id = 1,
                            motherName = name,
                            nickname = nickname,
                            birthDate = birthDate,
                            age = computedAge,
                            hasHighBp = hasHighBp,
                            hasLowBp = hasLowBp,
                            hasDiabetes = hasDiabetes,
                            chronicOthers = chronicOthers,
                            lastPeriodDate = lastPeriodDate,
                            lastPeriodEndDate = lastPeriodEndDate,
                            isPregnant = isPregnant,
                            userPhase = if (isPregnant) "pregnancy" else "period",
                            isOnboardingCompleted = true,
                            dueDate = dueDate
                        )
                    )
                }
            } catch (e: Exception) {
                com.example.util.AppLogger.w("WomanCompanionViewModel", "Failed to restore profile in init", e)
            }
        }

        // Check if lock is enabled on launch
        viewModelScope.launch(coroutineExceptionHandler) {
            val settings = repository.getAppLockSettings()
            if (settings != null && settings.isLockEnabled && !settings.pinHash.isNullOrEmpty()) {
                _isLocked.value = true
            }
        }
        
        // Monitor connectivity
        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    _isNetworkAvailable.value = true
                }
                override fun onLost(network: Network) {
                    _isNetworkAvailable.value = false
                }
            }
            networkCallback = callback
            connectivityManager.registerNetworkCallback(request, callback)
            // Initial value
            val activeNetwork = connectivityManager.activeNetwork
            val caps = connectivityManager.getNetworkCapabilities(activeNetwork)
            _isNetworkAvailable.value = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } catch (e: Exception) {
            _isNetworkAvailable.value = false
        }

        // Fetch weather on launch
        refreshWeather()
        // Check for updates on GitHub on launch
        checkForGitHubUpdates()
    }

    fun refreshWeather(lat: Double = 30.0444, lon: Double = 31.2357) {
        viewModelScope.launch(coroutineExceptionHandler) {
            _weatherState.value = WeatherService.fetchWeather(lat, lon)
        }
    }

    fun addSteps(stepsToAdd: Int) {
        viewModelScope.launch(coroutineExceptionHandler) {
            val today = getStartOfDay()
            val existing = repository.getStepLogForDate(today)
            val settings = repository.getAppLockSettings()
            val target = settings?.dailyStepTarget ?: 6000
            if (existing != null) {
                repository.insertStepLog(existing.copy(steps = existing.steps + stepsToAdd, targetSteps = target))
            } else {
                repository.insertStepLog(StepLog(date = today, steps = stepsToAdd, targetSteps = target))
            }
        }
    }

    fun unlockApp(pin: String): Boolean {
        val settings = appLockSettingsState.value
        return if (settings != null && settings.isLockEnabled) {
            val isCorrect = settings.pinHash == pin
            if (isCorrect) {
                _isLocked.value = false
            }
            isCorrect
        } else {
            _isLocked.value = false
            true
        }
    }

    fun lockAppManually() {
        val settings = appLockSettingsState.value
        if (settings != null && settings.isLockEnabled) {
            _isLocked.value = true
        }
    }

    // --- Active Fetal Kick Timer Session State ---
    private val _currentKickSessionStart = MutableStateFlow<Long?>(null)
    val currentKickSessionStart = _currentKickSessionStart.asStateFlow()

    private val _currentKickCount = MutableStateFlow(0)
    val currentKickCount = _currentKickCount.asStateFlow()

    fun startFetalKickSession() {
        _currentKickSessionStart.value = getCurrentTime()
        _currentKickCount.value = 0
    }

    fun incrementKickCount() {
        if (_currentKickSessionStart.value != null) {
            _currentKickCount.value += 1
        }
    }

    fun saveFetalKickSession() {
        val start = _currentKickSessionStart.value ?: return
        val end = getCurrentTime()
        val count = _currentKickCount.value
        val duration = (end - start) / 1000

        viewModelScope.launch(coroutineExceptionHandler) {
            repository.insertFetalKickSession(
                FetalKickSession(
                    startTime = start,
                    endTime = end,
                    kickCount = count,
                    durationSeconds = duration
                )
            )
            _currentKickSessionStart.value = null
            _currentKickCount.value = 0
        }
    }

    fun cancelFetalKickSession() {
        _currentKickSessionStart.value = null
        _currentKickCount.value = 0
    }

    // --- Active Contraction Tracker State ---
    private val _activeContractionStart = MutableStateFlow<Long?>(null)
    val activeContractionStart = _activeContractionStart.asStateFlow()

    fun startContraction() {
        _activeContractionStart.value = getCurrentTime()
    }

    fun stopAndSaveContraction() {
        val start = _activeContractionStart.value ?: return
        val end = getCurrentTime()
        val duration = (end - start) / 1000

        viewModelScope.launch(coroutineExceptionHandler) {
            val all = contractionLogsState.value
            val interval = if (all.isNotEmpty()) {
                (start - all.first().startTime) / 1000
            } else {
                0L
            }

            repository.insertContractionLog(
                ContractionLog(
                    startTime = start,
                    endTime = end,
                    durationSeconds = duration,
                    intervalSeconds = interval
                )
            )
            _activeContractionStart.value = null
        }
    }

    // --- Database Operations ---

    // Pregnancy setup
    fun setPregnancy(lastPeriodDate: Long?, preWeight: Double?, height: Double?) {
        viewModelScope.launch(coroutineExceptionHandler) {
            val existing = repository.getPregnancy()
            var finalLmp = lastPeriodDate ?: existing?.lastPeriodDate
            if (finalLmp == null) {
                val logs = repository.getAllPeriodLogs()
                finalLmp = logs.maxByOrNull { it.startDate }?.startDate ?: System.currentTimeMillis()
            }
            val dueDate = finalLmp?.let { it + 280L * 24 * 60 * 60 * 1000 }
            val bmiCategory = if (preWeight != null && height != null && height > 0) {
                val heightM = height / 100.0
                val bmi = preWeight / (heightM * heightM)
                when {
                    bmi < 18.5 -> "Underweight"
                    bmi < 25.0 -> "Normal"
                    bmi < 30.0 -> "Overweight"
                    else -> "Obese"
                }
            } else existing?.bmiCategory

            val updated = existing?.copy(
                lastPeriodDate = finalLmp,
                dueDate = dueDate ?: existing.dueDate,
                prePregnancyWeight = preWeight ?: existing.prePregnancyWeight,
                heightCm = height ?: existing.heightCm,
                bmiCategory = bmiCategory,
                userPhase = "pregnancy",
                isPregnant = true,
                isDelivered = false
            ) ?: PregnancyEntity(
                lastPeriodDate = finalLmp,
                dueDate = dueDate,
                prePregnancyWeight = preWeight,
                heightCm = height,
                bmiCategory = bmiCategory,
                userPhase = "pregnancy",
                isPregnant = true,
                isDelivered = false,
                isOnboardingCompleted = true
            )
            repository.savePregnancy(updated)
        }
    }

    // +++ أضيف بناءً على طلبك لتحديث بيانات جنس الجنين واسمه المقترح +++
    fun updateBabyInfo(gender: String?, name: String?) {
        viewModelScope.launch(coroutineExceptionHandler) {
            val existing = repository.getPregnancy()
            if (existing != null) {
                repository.savePregnancy(
                    existing.copy(
                        babyGender = gender ?: existing.babyGender,
                        babyName = name ?: existing.babyName
                    )
                )
            }
        }
    }

    // +++ أضيف بناءً على طلبك لتحديث حالة وتفاصيل الولادة ونصائحها +++
    fun updateDeliveryInfo(isDelivered: Boolean, birthMethod: String?) {
        viewModelScope.launch(coroutineExceptionHandler) {
            val existing = repository.getPregnancy()
            if (existing != null) {
                repository.savePregnancy(
                    existing.copy(
                        isDelivered = isDelivered,
                        birthMethod = birthMethod ?: existing.birthMethod,
                        isPregnant = !isDelivered,
                        userPhase = if (isDelivered) "period" else "pregnancy"
                    )
                )
            }
        }
    }

    fun setMotherProfile(
        motherName: String?,
        babyName: String?,
        userPhase: String?,
        lastPeriodDate: Long?,
        preWeight: Double?,
        height: Double?
    ) {
        viewModelScope.launch(coroutineExceptionHandler) {
            val existing = repository.getPregnancy()
            val isPreg = if (userPhase != null) {
                userPhase == "pregnancy"
            } else {
                existing?.isPregnant ?: false
            }
            var finalLmp = lastPeriodDate ?: existing?.lastPeriodDate
            if (finalLmp == null && isPreg) {
                val logs = repository.getAllPeriodLogs()
                finalLmp = logs.maxByOrNull { it.startDate }?.startDate ?: System.currentTimeMillis()
            }
            val dueDate = finalLmp?.let { it + 280L * 24 * 60 * 60 * 1000 }
            val bmiCategory = if (preWeight != null && height != null && height > 0) {
                val heightM = height / 100.0
                val bmi = preWeight / (heightM * heightM)
                when {
                    bmi < 18.5 -> "Underweight"
                    bmi < 25.0 -> "Normal"
                    bmi < 30.0 -> "Overweight"
                    else -> "Obese"
                }
            } else existing?.bmiCategory

            val updated = existing?.copy(
                motherName = motherName ?: existing.motherName,
                babyName = babyName ?: existing.babyName,
                userPhase = userPhase ?: existing.userPhase ?: "period",
                lastPeriodDate = finalLmp,
                dueDate = dueDate ?: existing.dueDate,
                prePregnancyWeight = preWeight ?: existing.prePregnancyWeight,
                heightCm = height ?: existing.heightCm,
                bmiCategory = bmiCategory,
                isPregnant = isPreg
            ) ?: PregnancyEntity(
                motherName = motherName,
                babyName = babyName,
                userPhase = userPhase ?: "period",
                lastPeriodDate = finalLmp,
                dueDate = dueDate,
                prePregnancyWeight = preWeight,
                heightCm = height,
                bmiCategory = bmiCategory,
                isPregnant = isPreg,
                isOnboardingCompleted = true
            )
            repository.savePregnancy(updated)
        }
    }

    fun updateUserBirthDate(birthDateMs: Long) {
        viewModelScope.launch(coroutineExceptionHandler) {
            val existing = repository.getPregnancy()
            val computedAge = calculateAge(birthDateMs)
            val updated = existing?.copy(
                birthDate = birthDateMs,
                age = computedAge
            ) ?: PregnancyEntity(
                birthDate = birthDateMs,
                age = computedAge,
                isOnboardingCompleted = true
            )
            repository.savePregnancy(updated)
        }
    }

    fun calculateAge(birthDateMs: Long): Int = WomanCompanionCalculators.calculateAge(birthDateMs)

    fun clearPregnancy() {
        viewModelScope.launch(coroutineExceptionHandler) {
            repository.deletePregnancy()
        }
    }

    fun switchToPeriodTracking() {
        viewModelScope.launch(coroutineExceptionHandler) {
            val existing = repository.getPregnancy()
            if (existing != null) {
                repository.savePregnancy(
                    existing.copy(
                        isPregnant = false,
                        isDelivered = false,
                        userPhase = "period"
                    )
                )
            }
        }
    }

    // +++ أضيف بناءً على طلبك لحفظ شاشة التهيئة (Onboarding) الصحية +++
    fun saveOnboardingProfile(
        name: String,
        nickname: String,
        birthDate: Long?,
        hasHighBp: Boolean,
        hasLowBp: Boolean,
        hasDiabetes: Boolean,
        chronicOthers: String,
        lastPeriodDate: Long?,
        lastPeriodEndDate: Long?,
        isPregnant: Boolean
    ) {
        viewModelScope.launch(coroutineExceptionHandler) {
            // حفظ حالة اكتمال شاشة التهيئة في SharedPreferences لضمان تخطيها دائمًا عند إعادة فتح التطبيق
            sharedPrefs.edit().apply {
                putBoolean("onboarding_completed_v1", true)
                putString("backup_mother_name", name)
                putString("backup_nickname", nickname)
                putBoolean("backup_is_pregnant", isPregnant)
                if (birthDate != null) putLong("backup_birth_date", birthDate) else remove("backup_birth_date")
                if (lastPeriodDate != null) putLong("backup_last_period_date", lastPeriodDate) else remove("backup_last_period_date")
                if (lastPeriodEndDate != null) putLong("backup_last_period_end_date", lastPeriodEndDate) else remove("backup_last_period_end_date")
                putBoolean("backup_has_high_bp", hasHighBp)
                putBoolean("backup_has_low_bp", hasLowBp)
                putBoolean("backup_has_diabetes", hasDiabetes)
                putString("backup_chronic_others", chronicOthers)
                apply()
            }

            val existing = repository.getPregnancy()
            val computedAge = birthDate?.let { calculateAge(it) } ?: existing?.age
            val dueDate = if (isPregnant && lastPeriodDate != null) {
                lastPeriodDate + 280L * 24 * 60 * 60 * 1000
            } else null
            
            repository.savePregnancy(
                PregnancyEntity(
                    id = existing?.id ?: 0,
                    motherName = name,
                    nickname = nickname,
                    birthDate = birthDate,
                    age = computedAge,
                    hasHighBp = hasHighBp,
                    hasLowBp = hasLowBp,
                    hasDiabetes = hasDiabetes,
                    chronicOthers = chronicOthers,
                    lastPeriodDate = lastPeriodDate,
                    lastPeriodEndDate = lastPeriodEndDate,
                    isPregnant = isPregnant,
                    userPhase = if (isPregnant) "pregnancy" else "period",
                    isOnboardingCompleted = true,
                    dueDate = dueDate,
                    babyName = existing?.babyName,
                    prePregnancyWeight = existing?.prePregnancyWeight,
                    heightCm = existing?.heightCm,
                    bmiCategory = existing?.bmiCategory
                )
            )

            // Also automatically add a PeriodLog if user entered lastPeriodDate
            if (lastPeriodDate != null) {
                repository.insertPeriodLog(
                    com.example.data.PeriodLog(
                        startDate = lastPeriodDate,
                        endDate = lastPeriodEndDate,
                        flowIntensity = "medium",
                        symptoms = "تحديد البداية من التهيئة",
                        painLevel = 5,
                        notes = "تم التسجيل تلقائياً عبر شاشة التهيئة والـ Onboarding الترحيبية."
                    )
                )
            }
        }
    }

    // Period log CRUD
    fun addPeriodLog(startDate: Long, endDate: Long?, intensity: String, symptoms: List<String>, painLevel: Int, notes: String?) {
        viewModelScope.launch(coroutineExceptionHandler) {
            repository.insertPeriodLog(
                PeriodLog(
                    startDate = startDate,
                    endDate = endDate,
                    flowIntensity = intensity,
                    symptoms = symptoms.joinToString(","),
                    painLevel = painLevel,
                    notes = notes
                )
            )
        }
    }

    fun deletePeriod(log: PeriodLog) {
        viewModelScope.launch(coroutineExceptionHandler) {
            repository.deletePeriodLog(log)
        }
    }

    // Water tracker
    fun addWater(amountMl: Int) {
        viewModelScope.launch(coroutineExceptionHandler) {
            val today = getStartOfDay()
            val existing = repository.getWaterLogForDate(today)
            if (existing != null) {
                repository.insertWaterLog(existing.copy(amountMl = existing.amountMl + amountMl))
            } else {
                repository.insertWaterLog(WaterLog(date = today, amountMl = amountMl))
            }
            WomanCompanionAppWidget.updateAllWidgets(getApplication())
        }
    }

    fun resetTodayWater() {
        viewModelScope.launch(coroutineExceptionHandler) {
            val today = getStartOfDay()
            val existing = repository.getWaterLogForDate(today)
            if (existing != null) {
                repository.insertWaterLog(existing.copy(amountMl = 0))
            }
            WomanCompanionAppWidget.updateAllWidgets(getApplication())
        }
    }

    // Nutrition
    fun addNutritionMeal(
        mealType: String,
        description: String,
        calories: Int,
        iron: Double,
        folate: Double,
        calcium: Double,
        omega3: Double,
        protein: Double = 0.0,
        carbs: Double = 0.0,
        fat: Double = 0.0,
        sugar: Double = 0.0,
        fiber: Double = 0.0,
        waterBenefit: Int = 0,
        potassium: Double = 0.0,
        sodium: Double = 0.0,
        magnesium: Double = 0.0,
        vitaminC: Double = 0.0,
        vitaminA: Double = 0.0
    ) {
        viewModelScope.launch(coroutineExceptionHandler) {
            val matchedFoods = EgyptianFoodRepository.extractFoodsFromInput(description)
            
            val finalCalories = if (calories > 0) calories else (matchedFoods.sumOf { it.calories })
            val finalIron = if (iron > 0.0) iron else (matchedFoods.sumOf { it.ironMg })
            val finalFolate = if (folate > 0.0) folate else (matchedFoods.sumOf { it.vitaminB_Mg * 100.0 }) // approximation
            val finalCalcium = if (calcium > 0.0) calcium else (matchedFoods.sumOf { it.calciumMg })
            val finalOmega3 = if (omega3 > 0.0) omega3 else (matchedFoods.sumOf { it.vitaminD_Mcg * 0.1 }) // approximation

            val finalProtein = if (protein > 0.0) protein else (matchedFoods.sumOf { it.protein })
            val finalCarbs = if (carbs > 0.0) carbs else (matchedFoods.sumOf { it.carbs })
            val finalFat = if (fat > 0.0) fat else (matchedFoods.sumOf { it.fat })
            val finalSugar = if (sugar > 0.0) sugar else (matchedFoods.sumOf { it.sugarG })
            val finalFiber = if (fiber > 0.0) fiber else (matchedFoods.sumOf { it.fiberG })
            val finalWaterBenefit = if (waterBenefit > 0) waterBenefit else (matchedFoods.sumOf { it.waterBenefitMl })
            val finalPotassium = if (potassium > 0.0) potassium else (matchedFoods.sumOf { it.potassiumMg })
            val finalSodium = if (sodium > 0.0) sodium else (matchedFoods.sumOf { it.sodiumMg })
            val finalMagnesium = if (magnesium > 0.0) magnesium else (matchedFoods.sumOf { it.magnesiumMg })
            val finalVitaminC = if (vitaminC > 0.0) vitaminC else (matchedFoods.sumOf { it.vitaminC_Mg })
            val finalVitaminA = if (vitaminA > 0.0) vitaminA else (matchedFoods.sumOf { it.vitaminA_Mcg })

            repository.insertNutritionLog(
                NutritionLog(
                    date = getCurrentTime(),
                    mealType = mealType,
                    description = description,
                    calories = finalCalories,
                    ironMg = finalIron,
                    folateMcg = finalFolate,
                    calciumMg = finalCalcium,
                    omega3G = finalOmega3,
                    proteinG = finalProtein,
                    carbsG = finalCarbs,
                    fatG = finalFat,
                    sugarG = finalSugar,
                    fiberG = finalFiber,
                    waterBenefitMl = finalWaterBenefit,
                    potassiumMg = finalPotassium,
                    sodiumMg = finalSodium,
                    magnesiumMg = finalMagnesium,
                    vitaminC_Mg = finalVitaminC,
                    vitaminA_Mcg = finalVitaminA
                )
            )

            // +++ أضيف بناءً على طلبك لزيادة عداد تكرار تسجيل الأطعمة لتفعيل المفضلات التلقائية الذكية +++
            matchedFoods.forEach { food ->
                incrementFoodLogCount(food.name)
            }
            if (matchedFoods.isEmpty() && description.isNotEmpty()) {
                incrementFoodLogCount(description)
            }

            if (finalWaterBenefit > 0) {
                addWater(finalWaterBenefit)
            }
        }
    }

    fun deleteNutritionMeal(log: NutritionLog) {
        viewModelScope.launch(coroutineExceptionHandler) {
            repository.deleteNutritionLog(log)
        }
    }

    // Medications (Smart Home Pharmacy)
    fun addMedication(
        name: String,
        dosage: String?,
        timesPerDay: Int,
        prescby: String?,
        notes: String?,
        start: Long?,
        expiryDate: Long? = null,
        totalQuantity: Int = 0,
        remainingQuantity: Int = 0,
        safetyWarning: String? = null
    ) {
        viewModelScope.launch(coroutineExceptionHandler) {
            val med = MedicationLog(
                name = name,
                dosage = dosage,
                timesPerDay = timesPerDay,
                prescribedBy = prescby,
                notes = notes,
                startDate = start,
                isActive = true,
                expiryDate = expiryDate,
                totalQuantity = totalQuantity,
                remainingQuantity = remainingQuantity,
                safetyWarning = safetyWarning
            )
            val insertedId = repository.insertMedication(med).toInt()
            val createdMed = med.copy(id = insertedId)
            ReminderScheduler.scheduleMedicationReminders(getApplication(), createdMed)
            WomanCompanionAppWidget.updateAllWidgets(getApplication())
        }
    }

    fun recordMedicationAdherence(
        medicationId: Int,
        scheduledTime: Long = System.currentTimeMillis(),
        status: String = "TAKEN"
    ) {
        viewModelScope.launch(coroutineExceptionHandler) {
            val log = MedicationAdherenceLog(
                medicationId = medicationId,
                scheduledTime = scheduledTime,
                actualTime = if (status == "TAKEN") System.currentTimeMillis() else null,
                status = status
            )
            repository.insertMedicationAdherenceLog(log)
            WomanCompanionAppWidget.updateAllWidgets(getApplication())
        }
    }

    fun decrementMedicationStock(medication: MedicationLog, amount: Int = 1) {
        viewModelScope.launch(coroutineExceptionHandler) {
            val currentRemaining = medication.remainingQuantity
            val nextRemaining = (currentRemaining - amount).coerceAtLeast(0)
            repository.insertMedication(medication.copy(remainingQuantity = nextRemaining))
            WomanCompanionAppWidget.updateAllWidgets(getApplication())
        }
    }

    fun toggleMedicationActive(medication: MedicationLog) {
        viewModelScope.launch(coroutineExceptionHandler) {
            val newIsActive = !medication.isActive
            val updated = medication.copy(isActive = newIsActive)
            repository.insertMedication(updated)
            if (!newIsActive) {
                ReminderScheduler.cancelMedicationReminders(getApplication(), medication.id, medication.timesPerDay)
            } else {
                ReminderScheduler.scheduleMedicationReminders(getApplication(), updated)
            }
            WomanCompanionAppWidget.updateAllWidgets(getApplication())
        }
    }

    fun updateMedication(medication: MedicationLog) {
        viewModelScope.launch(coroutineExceptionHandler) {
            repository.insertMedication(medication)
            ReminderScheduler.cancelMedicationReminders(getApplication(), medication.id, medication.timesPerDay)
            if (medication.isActive) {
                ReminderScheduler.scheduleMedicationReminders(getApplication(), medication)
            }
            WomanCompanionAppWidget.updateAllWidgets(getApplication())
        }
    }

    fun deleteMedication(medication: MedicationLog) {
        viewModelScope.launch(coroutineExceptionHandler) {
            ReminderScheduler.cancelMedicationReminders(getApplication(), medication.id, medication.timesPerDay)
            repository.deleteMedication(medication)
            WomanCompanionAppWidget.updateAllWidgets(getApplication())
        }
    }

    // --- عمليات السجل الذكي للنوم (Smart Sleep Analyzer CRUD) ---
    fun addSleepLog(
        startTime: Long,
        endTime: Long,
        qualityScore: Int,
        deepSleepMin: Int = 0,
        lightSleepMin: Int = 0,
        remSleepMin: Int = 0,
        awakenings: Int = 0,
        notes: String? = null
    ) {
        viewModelScope.launch(coroutineExceptionHandler) {
            val date = getStartOfDay(startTime)
            repository.insertSleepLog(
                SleepLog(
                    date = date,
                    startTime = startTime,
                    endTime = endTime,
                    qualityScore = qualityScore,
                    deepSleepMinutes = deepSleepMin,
                    lightSleepMinutes = lightSleepMin,
                    remSleepMinutes = remSleepMin,
                    awakeningsCount = awakenings,
                    notes = notes
                )
            )
        }
    }

    fun deleteSleepLog(log: SleepLog) {
        viewModelScope.launch(coroutineExceptionHandler) {
            repository.deleteSleepLog(log)
        }
    }

    // --- عمليات ربط الشريك والرفيق (Companion Sync Operations) ---
    fun addPartnerMessage(senderName: String, messageText: String, category: String = "Support") {
        viewModelScope.launch(coroutineExceptionHandler) {
            repository.insertPartnerMessage(
                PartnerMessage(
                    senderName = senderName,
                    messageText = messageText,
                    category = category
                )
            )
        }
    }

    fun markPartnerMessageAsRead(id: Int) {
        viewModelScope.launch(coroutineExceptionHandler) {
            repository.markPartnerMessageAsRead(id)
        }
    }

    // --- عمليات سجل الوحم والاشتهاء (Pregnancy Cravings CRUD) ---
    fun addCravingLog(
        cravingItem: String,
        cravingType: String = "Sweet",
        intensity: Int = 5,
        notes: String? = null
    ) {
        viewModelScope.launch(coroutineExceptionHandler) {
            repository.insertCravingLog(
                CravingLog(
                    cravingItem = cravingItem,
                    cravingType = cravingType,
                    intensity = intensity,
                    notes = notes
                )
            )
        }
    }

    fun deleteCravingLog(log: CravingLog) {
        viewModelScope.launch(coroutineExceptionHandler) {
            repository.deleteCravingLog(log)
        }
    }

    // --- تتبع نمو الجنين (Fetal Growth Tracker operations) ---
    fun calculateWeightDeviation(loggedWeek: Int, actualWeightGrams: Double): Double {
        return com.example.ui.FetalStandardData.calculateWeightDeviation(loggedWeek, actualWeightGrams)
    }

    fun getWeightDeviationForCurrentPregnancy(): Double? {
        val activePregnancy = pregnancyState.value ?: return null
        if (!activePregnancy.isPregnant) return null
        val logs = allFetalGrowthLogsState.value
        val latestLog = logs
            .filter { it.pregnancyId == activePregnancy.id }
            .maxByOrNull { it.date } ?: return null
        return com.example.ui.FetalStandardData.calculateWeightDeviation(latestLog.pregnancyWeek, latestLog.weightGrams)
    }

    fun addFetalGrowthLog(week: Int, weightGrams: Double, lengthCm: Double, notes: String?) {
        viewModelScope.launch(coroutineExceptionHandler) {
            val activePregnancy = pregnancyState.value
            val targetPregnancyId = if (activePregnancy != null && activePregnancy.isPregnant) {
                activePregnancy.id
            } else {
                // If not actively set in state, check database or create an active pregnancy record
                val all = repository.allPregnanciesFlow.firstOrNull() ?: emptyList()
                val current = all.firstOrNull { it.isPregnant } ?: all.firstOrNull()
                current?.id ?: run {
                    val defaultLmp = System.currentTimeMillis() - (week.toLong() * 7L * 24L * 60L * 60L * 1000L)
                    val newId = repository.savePregnancy(
                        PregnancyEntity(
                            lastPeriodDate = defaultLmp,
                            isPregnant = true
                        )
                    ).toInt()
                    newId
                }
            }
            repository.insertFetalGrowthLog(
                FetalGrowthLog(
                    pregnancyId = targetPregnancyId,
                    pregnancyWeek = week,
                    weightGrams = weightGrams,
                    lengthCm = lengthCm,
                    notes = notes
                )
            )
        }
    }

    fun startNewPregnancy(
        lastPeriodDate: Long,
        babyName: String? = null,
        prePregnancyWeight: Double? = null,
        heightCm: Double? = null,
        babyGender: String? = null
    ) {
        viewModelScope.launch(coroutineExceptionHandler) {
            val existing = repository.getPregnancy()
            repository.deactivateAllPregnancies()
            val dueDate = lastPeriodDate + 280L * 24 * 60 * 60 * 1000
            val computedAge = existing?.birthDate?.let { calculateAge(it) } ?: existing?.age

            val newPregnancy = PregnancyEntity(
                id = 0,
                motherName = existing?.motherName ?: "الأم",
                nickname = existing?.nickname,
                birthDate = existing?.birthDate,
                age = computedAge,
                hasHighBp = existing?.hasHighBp ?: false,
                hasLowBp = existing?.hasLowBp ?: false,
                hasDiabetes = existing?.hasDiabetes ?: false,
                chronicOthers = existing?.chronicOthers,
                lastPeriodDate = lastPeriodDate,
                dueDate = dueDate,
                babyName = babyName,
                prePregnancyWeight = prePregnancyWeight ?: existing?.prePregnancyWeight,
                heightCm = heightCm ?: existing?.heightCm,
                babyGender = babyGender,
                isPregnant = true,
                isActive = true,
                userPhase = "pregnancy",
                isOnboardingCompleted = true
            )
            repository.savePregnancy(newPregnancy)
        }
    }

    fun deleteFetalGrowthLog(log: FetalGrowthLog) {
        viewModelScope.launch(coroutineExceptionHandler) {
            repository.deleteFetalGrowthLog(log)
        }
    }

    // Symptom logger
    fun addSymptom(symptom: String, severity: Int, notes: String?) {
        viewModelScope.launch(coroutineExceptionHandler) {
            repository.insertSymptomLog(
                SymptomLog(
                    date = getCurrentTime(),
                    symptom = symptom,
                    severity = severity,
                    notes = notes
                )
            )
        }
    }

    fun deleteSymptom(log: SymptomLog) {
        viewModelScope.launch(coroutineExceptionHandler) {
            repository.deleteSymptomLog(log)
        }
    }

    // Blood pressure logger
    fun addBloodPressureLog(systolic: Int, diastolic: Int, pulse: Int?, notes: String?) {
        viewModelScope.launch(coroutineExceptionHandler) {
            repository.insertBloodPressureLog(
                BloodPressureLog(
                    date = getCurrentTime(),
                    systolic = systolic,
                    diastolic = diastolic,
                    pulse = pulse,
                    notes = notes
                )
            )
        }
    }

    fun deleteBloodPressureLog(log: BloodPressureLog) {
        viewModelScope.launch(coroutineExceptionHandler) {
            repository.deleteBloodPressureLog(log)
        }
    }

    // Appointments
    fun addAppointment(title: String, dateTime: Long, doctor: String?, notes: String?) {
        viewModelScope.launch(coroutineExceptionHandler) {
            val appt = Appointment(
                dateTime = dateTime,
                title = title,
                doctorName = doctor,
                notes = notes
            )
            val insertedId = repository.insertAppointment(appt).toInt()
            val createdAppt = appt.copy(id = insertedId)
            ReminderScheduler.scheduleAppointmentReminder(getApplication(), createdAppt)
        }
    }

    fun toggleAppointmentCompleted(appointment: Appointment) {
        viewModelScope.launch(coroutineExceptionHandler) {
            val newCompleted = !appointment.completed
            val updated = appointment.copy(completed = newCompleted)
            repository.insertAppointment(updated)
            if (newCompleted) {
                ReminderScheduler.cancelAppointmentReminder(getApplication(), appointment.id)
            } else {
                ReminderScheduler.scheduleAppointmentReminder(getApplication(), updated)
            }
        }
    }

    fun deleteAppointment(appointment: Appointment) {
        viewModelScope.launch(coroutineExceptionHandler) {
            ReminderScheduler.cancelAppointmentReminder(getApplication(), appointment.id)
            repository.deleteAppointment(appointment)
        }
    }

    // Journal
    fun addJournalEntry(content: String, mood: String?) {
        viewModelScope.launch(coroutineExceptionHandler) {
            repository.insertJournalEntry(
                JournalEntry(
                    date = getCurrentTime(),
                    content = content,
                    mood = mood
                )
            )
        }
    }

    fun deleteJournal(entry: JournalEntry) {
        viewModelScope.launch(coroutineExceptionHandler) {
            repository.deleteJournalEntry(entry)
        }
    }

    // Qada Fasts (Ramadan missed days)
    fun addQadaFast(yearHijri: Int, missed: Int, completed: Int) {
        viewModelScope.launch(coroutineExceptionHandler) {
            repository.insertQadaFast(
                QadaFast(
                    yearHijri = yearHijri,
                    missedDays = missed,
                    completedDays = completed
                )
            )
        }
    }

    fun updateQadaFastProgress(fast: QadaFast, increment: Boolean) {
        viewModelScope.launch(coroutineExceptionHandler) {
            val newVal = if (increment) {
                (fast.completedDays + 1).coerceAtMost(fast.missedDays)
            } else {
                (fast.completedDays - 1).coerceAtLeast(0)
            }
            repository.insertQadaFast(fast.copy(completedDays = newVal))
        }
    }

    fun deleteQadaFast(fast: QadaFast) {
        viewModelScope.launch(coroutineExceptionHandler) {
            repository.deleteQadaFast(fast)
        }
    }

    // App lock and companion configurations
    fun configureAppLock(
        pin: String?, 
        isEnabled: Boolean, 
        isStealth: Boolean, 
        companionName: String = "جوري", 
        dailyStepTarget: Int = 6000, 
        isDarkMode: Boolean? = null,
        gitHubRepoUrl: String? = null,
        userApiKey: String? = null
    ) {
        viewModelScope.launch(coroutineExceptionHandler) {
            val current = repository.getAppLockSettings()
            val finalIsDark = isDarkMode ?: current?.isDarkMode ?: true
            val finalGitHubUrl = gitHubRepoUrl ?: current?.gitHubRepoUrl ?: "https://raw.githubusercontent.com/your_username/your_repo/main/matrix.json"
            val finalApiKey = userApiKey ?: current?.userApiKey
            repository.saveAppLockSettings(
                AppLockSettings(
                    pinHash = pin, // For simulation simplicity, we store the pin string directly
                    isLockEnabled = isEnabled,
                    isStealthModeEnabled = isStealth,
                    companionName = companionName,
                    dailyStepTarget = dailyStepTarget,
                    isDarkMode = finalIsDark,
                    gitHubRepoUrl = finalGitHubUrl,
                    userApiKey = finalApiKey
                )
            )
            if (!isEnabled) {
                _isLocked.value = false
            }
        }
    }

    // +++ أضيف بناءً على طلبك لتغيير ألوان التطبيق (داكن/فاتح) برغبة المستخدم أو بطلب من جوري +++
    fun setThemeMode(isDark: Boolean) {
        viewModelScope.launch(coroutineExceptionHandler) {
            val current = repository.getAppLockSettings()
            if (current != null) {
                repository.saveAppLockSettings(current.copy(isDarkMode = isDark))
            } else {
                repository.saveAppLockSettings(
                    AppLockSettings(
                        pinHash = null,
                        isLockEnabled = false,
                        isStealthModeEnabled = false,
                        companionName = "جوري",
                        dailyStepTarget = 6000,
                        isDarkMode = isDark
                    )
                )
            }
        }
    }

    private val _gitHubSyncStatus = MutableStateFlow<String?>(null)
    val gitHubSyncStatus: StateFlow<String?> = _gitHubSyncStatus.asStateFlow()

    fun syncJouriMatrix() {
        viewModelScope.launch(coroutineExceptionHandler) {
            _gitHubSyncStatus.value = "جاري الاتصال بـ GitHub..."
            val settings = repository.getAppLockSettings()
            val url = settings?.gitHubRepoUrl
            if (url.isNullOrBlank()) {
                _gitHubSyncStatus.value = "خطأ: رابط الـ Repository غير مهيأ!"
                return@launch
            }
            val result = GitHubSyncRepository.syncJouriMatrixFromServer(url)
            if (result != null) {
                try {
                    val currentHash = result.hashCode().toString()
                    val prefs = getApplication<Application>().getSharedPreferences("woman_companion_prefs", Context.MODE_PRIVATE)
                    prefs.edit().putString("last_synced_hash", currentHash).apply()
                    _isGitHubUpdateAvailable.value = false
                } catch (e: Exception) {
                    com.example.util.AppLogger.w("WomanCompanionViewModel", "Failed to save synced hash", e)
                }
                _gitHubSyncStatus.value = "تم التحديث بنجاح! مزامنة مصفوفة جوري مكتملة ✅"
            } else {
                _gitHubSyncStatus.value = "فشل التحديث. تم الاحتفاظ بالنسخة المحلية الذكية بنسبة ١٠٠% أوفلاين."
            }
        }
    }

    fun toggleDarkMode() {
        viewModelScope.launch(coroutineExceptionHandler) {
            val current = repository.getAppLockSettings() ?: AppLockSettings()
            repository.saveAppLockSettings(current.copy(isDarkMode = !current.isDarkMode))
        }
    }

    fun getActivityAdvice(steps: Int, target: Int): Pair<String, String> {
        val preg = pregnancyState.value
        val isPreg = preg != null

        if (isPreg) {
            val prog = getPregnancyProgression()
            val trimester = prog?.trimester ?: 1
            return when {
                trimester == 1 -> {
                    if (steps > 4000) {
                        Pair("تمهلّي يا عزيزتي الحامل 🤰", "أنتِ في الثلث الأول من الحمل. ننصحكِ بعدم الإفراط في المجهود البدني والراحة إذا شعرتِ بالتعب. حركتكِ الحالية ($steps خطوة) ممتازة، لا ترهقي نفسكِ.")
                    } else {
                        Pair("خطوات خفيفة وآمنة 🌸", "المشي الخفيف (٣٠٠٠-٤٠٠٠ خطوة) رائع جداً في بداية الحمل لتنشيط الدورة الدموية دون إجهاد. حركتكِ الحالية مناسبة تماماً.")
                    }
                }
                trimester == 2 -> {
                    if (steps < target) {
                        Pair("طور الطاقة المرتفعة! ✨", "أنتِ في الثلث الثاني، وهي أكثر فترات الحمل نشاطاً! حاولي الوصول لهدفكِ ($target خطوة) لتقوية عضلات الحوض والظهر وتحسين التنفس.")
                    } else {
                        Pair("إنجاز رائع وبطلة! 🎉", "وصلتِ لهدفكِ اليومي من الخطوات! احرصي الآن على رفع قدميكِ والاستراحة وشرب كمية جيدة من الماء.")
                    }
                }
                else -> { // Trimester 3
                    if (steps > 5000) {
                        Pair("جهود عظيمة.. خففي السرعة 🤱", "في الثلث الأخير، يزداد وزن الطفل ويصعب المشي الطويل. ننصحكِ بعدم تجاوز ٥٠٠٠ خطوة وتجنب الإرهاق لحماية مفاصل الحوض.")
                    } else {
                        Pair("المشي لتسهيل الولادة 🚶‍♀️", "المشي الخفيف والمريح في الشهر التاسع مفيد جداً لمساعدة الجنين على النزول للحوض بسلام. حركتكِ الحالية ممتازة.")
                    }
                }
            }
        } else {
            val phaseInfo = getCurrentCyclePhase()
            return when (phaseInfo.phaseName) {
                "Menstruation" -> {
                    if (steps > 3000) {
                        Pair("خففي الحركة ودللي نفسكِ 🍫", "في طور الطمث، يفضل تقليل المشي العنيف والتركيز على تمارين الإطالة الخفيفة وتمارين التنفس الهادئة أو الراحة التامة للتخفيف من آلام المغص.")
                    } else {
                        Pair("حركة مريحة ومطهرة 🌸", "المشي الخفيف يقلل من تشنجات البطن ويحسن المزاج بشكل ملحوظ. لا ترهقي نفسكِ اليوم.")
                    }
                }
                "Follicular", "Ovulation" -> {
                    if (steps < target) {
                        Pair("طاقتكِ في أوجها! 🌱", "أنتِ في طور الخصوبة والجريبي، الهرمونات تدعم حيويتكِ ونشاطكِ البدني. حاولي المشي وزيادة نشاطكِ اليومي لتصلي لهدفكِ ($target خطوة)!")
                    } else {
                        Pair("نشاط مذهل وهمة عالية! 💪", "أنتِ اليوم في منتهى الحيوية وقد تجاوزتِ هدفكِ! هذا ممتاز للحفاظ على اللياقة وصحة المبايض وتوازن الهرمونات.")
                    }
                }
                else -> { // Luteal phase
                    if (steps > 6000) {
                        Pair("جهد متوازن وهدوء 🧘‍♀️", "طور ما قبل الدورة (اللوتياني) يترافق أحياناً بقلة طاقة أو تقلبات مزاجية. المشي المعتدل يساعد على إفراز الإندورفين وتحسين النفسية دون مجهود مضاعف.")
                    } else {
                        Pair("حسّني مزاجكِ بمشية خفيفة 🍃", "المشي في الهواء الطلق لمدة ٢٠ دقيقة ممتاز للتخفيف من أعراض متلازمة ما قبل الطمث (PMS).")
                    }
                }
            }
        }
    }

    fun factoryReset() {
        viewModelScope.launch(coroutineExceptionHandler) {
            repository.factoryReset()
            _isLocked.value = false
        }
    }

    fun clearContractions() {
        viewModelScope.launch(coroutineExceptionHandler) {
            repository.clearAllContractions()
        }
    }

    // --- Complex Calculations ---

    // Average cycle length and duration helper
    fun getCycleStats(): CycleStats = WomanCompanionCalculators.getCycleStats(periodLogsState.value)

    // Cycle irregularity pattern detection (local rules, non-diagnostic)
    fun detectCycleIrregularityPatterns(): List<IrregularityNotice> =
        WomanCompanionCalculators.detectCycleIrregularityPatterns(periodLogsState.value, symptomLogsState.value)

    // Check if any cycle irregularity pattern is detected
    fun checkCycleIrregularity(): Boolean = detectCycleIrregularityPatterns().isNotEmpty()

    // Detect current menstrual cycle phase or late period
    fun getCurrentCyclePhase(): CyclePhaseInfo =
        WomanCompanionCalculators.getCurrentCyclePhase(periodLogsState.value, getCurrentTime())

    // Pregnancy progression details
    fun getPregnancyProgression(): PregnancyProgression? =
        WomanCompanionCalculators.getPregnancyProgression(pregnancyState.value, getCurrentTime())

    // Dynamic water target: 2000ml default. If pregnant, add 500ml. Also adds extra weather-based hydration requirements.
    fun getWaterTarget(): Int =
        WomanCompanionCalculators.getWaterTarget(pregnancyState.value != null, weatherState.value?.extraWaterMl ?: 0)

    // Dynamic nutrition target: Base 2000kcal. Adjust based on Pregnancy Trimester, BMI pre-pregnancy, and daily step goal.
    fun getCalorieTarget(): CalorieGoal {
        val stepTarget = appLockSettingsState.value?.dailyStepTarget ?: 6000
        val activityBonus = if (stepTarget > 6000) {
            val diff = stepTarget - 6000
            (diff / 10).coerceAtMost(300) // 10 steps = 1 calorie, up to 300 kcal max bonus
        } else {
            0
        }

        val preg = pregnancyState.value
        if (preg == null) {
            val finalGoal = (2000 + activityBonus).coerceIn(1800, 2500)
            return CalorieGoal(
                target = finalGoal,
                details = "الحد اليومي الطبيعي المتوازن لغير الحوامل مع توازن الحركة (${stepTarget} خطوة)."
            )
        }

        val prog = getPregnancyProgression()
        val trimesterBonus = when (prog?.trimester) {
            1 -> 0
            2 -> 340
            3 -> 450
            else -> 300
        }

        val bmiAdjustment = when (preg.bmiCategory) {
            "Underweight" -> 200
            "Obese" -> -100
            else -> 0
        }

        val finalGoal = (2000 + trimesterBonus + bmiAdjustment + activityBonus).coerceIn(1800, 3100)
        val details = when (prog?.trimester) {
            1 -> "الثلث الأول: لا حاجة لسعرات إضافية كبيرة، ركّزي على جودة العناصر (حمض الفوليك والحديد) مع مواءمة خطواتكِ المستهدفة بـ ${stepTarget} خطوة."
            2 -> "الثلث الثاني: تمت إضافة +340 سعرة لدعم نمو الجنين السريع مع مواءمة خطواتكِ المستهدفة بـ ${stepTarget} خطوة."
            3 -> "الثلث الثالث: تمت إضافة +450 سعرة لتغذية نمو طفلك الأخير وتجهيز الرضاعة مع مواءمة خطواتكِ المستهدفة بـ ${stepTarget} خطوة."
            else -> "دعم مستمر للمغذيات الطبيعية مواءمة بحركتكِ."
        }

        return CalorieGoal(target = finalGoal, details = details)
    }

    fun getNutrientTargets(): Map<String, NutrientTarget> =
        WomanCompanionCalculators.getNutrientTargets(pregnancyState.value != null)

    // Checking for 5-1-1 contraction warning
    fun checkContractionWarning(): Boolean {
        val list = contractionLogsState.value
        if (list.size < 3) return false

        // Take last 3 contractions
        val last3 = list.take(3)
        val allLastHour = last3.all { getCurrentTime() - it.startTime <= 60 * 60 * 1000 }
        if (!allLastHour) return false

        val meanDurationOk = last3.all { it.durationSeconds >= 45 }
        val meanIntervalOk = last3.dropLast(1).all { it.intervalSeconds in 180..360 }

        return meanDurationOk && meanIntervalOk
    }

    // --- Contraceptive Methods Actions ---
    fun addContraceptiveMethod(type: String, startDate: Long, notes: String? = null) {
        viewModelScope.launch(coroutineExceptionHandler) {
            activeContraceptiveMethodState.value?.let { active ->
                repository.updateContraceptiveMethod(active.copy(endDate = startDate))
            }
            repository.insertContraceptiveMethod(
                ContraceptiveMethod(type = type, startDate = startDate, notes = notes)
            )
        }
    }

    fun endContraceptiveMethod(methodId: Int, endDate: Long = System.currentTimeMillis()) {
        viewModelScope.launch(coroutineExceptionHandler) {
            val method = allContraceptiveMethodsState.value.find { it.id == methodId }
            if (method != null) {
                repository.updateContraceptiveMethod(method.copy(endDate = endDate))
            }
        }
    }

    fun deleteContraceptiveMethod(method: ContraceptiveMethod) {
        viewModelScope.launch(coroutineExceptionHandler) {
            repository.deleteContraceptiveMethod(method)
        }
    }

    fun calculateDaysBetween(startDateMs: Long, targetDateMs: Long): Long =
        WomanCompanionCalculators.calculateDaysBetween(startDateMs, targetDateMs)

    fun getContraceptiveTypeName(type: String): String =
        WomanCompanionCalculators.getContraceptiveTypeName(type)

    fun getContraceptiveContextForSymptom(symptomDateMs: Long): ContraceptiveSymptomContext? {
        val methods = allContraceptiveMethodsState.value
        val method = methods.find { m ->
            symptomDateMs >= m.startDate && (m.endDate == null || symptomDateMs <= m.endDate)
        } ?: return null

        val days = calculateDaysBetween(method.startDate, symptomDateMs)
        val typeName = getContraceptiveTypeName(method.type)
        return ContraceptiveSymptomContext(
            methodType = method.type,
            methodTypeName = typeName,
            daysSinceStart = days,
            formattedLabel = "بعد $days يوم من بدء $typeName"
        )
    }

    // --- 💧 Smart Water Daytime Reminders Control ---
    private val _isWaterReminderEnabled = MutableStateFlow(
        sharedPrefs.getBoolean("water_reminders_enabled", false)
    )
    val isWaterReminderEnabled: StateFlow<Boolean> = _isWaterReminderEnabled.asStateFlow()

    fun toggleWaterReminders(enabled: Boolean) {
        _isWaterReminderEnabled.value = enabled
        sharedPrefs.edit().putBoolean("water_reminders_enabled", enabled).apply()
        ReminderScheduler.scheduleDaytimeWaterReminders(getApplication(), enabled)
    }

    override fun onCleared() {
        super.onCleared()
        try {
            networkCallback?.let {
                connectivityManager.unregisterNetworkCallback(it)
            }
        } catch (e: Exception) {
            Log.e("WomanCompanionVM", "Failed to unregister network callback", e)
        }
    }
}

// Factory Provider
class WomanCompanionViewModelFactory(
    private val application: Application,
    private val repository: WomanCompanionRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WomanCompanionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WomanCompanionViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
