package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

class WomanCompanionViewModel(
    application: Application,
    private val repository: WomanCompanionRepository
) : AndroidViewModel(application) {

    // +++ أضيف بناءً على طلبك لدعم المفضلات وذكاء التفضيل المحلي للأطعمة +++
    private val sharedPrefs = application.getSharedPreferences("jouri_behavior_prefs", Context.MODE_PRIVATE)

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
            } else if (diff == oneDayMs) {
                // Consecutive day
                streak += 1
            } else {
                // Streak broken
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

    private val _apiKeyTestStatus = MutableStateFlow<String?>(null) // "testing", "success", "error: <msg>", or null
    val apiKeyTestStatus: StateFlow<String?> = _apiKeyTestStatus.asStateFlow()

    fun testAndSaveApiKey(key: String, customBaseUrl: String? = null, customModel: String? = null) {
        _apiKeyTestStatus.value = "testing"
        viewModelScope.launch {
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
        viewModelScope.launch {
            apiKeyRepository.saveKey(key.trim())
            apiKeyRepository.saveBaseUrl(baseUrl.trim())
            apiKeyRepository.saveModelName(model.trim())
        }
    }

    fun clearApiKey() {
        viewModelScope.launch {
            apiKeyRepository.clearKey()
        }
    }

    private val connectivityManager = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
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

    // --- Active Tools SubScreen State (for cross-screen navigation/direction) ---
    private val _activeSubScreen = MutableStateFlow<String?>(null)
    val activeSubScreen: StateFlow<String?> = _activeSubScreen.asStateFlow()

    fun setActiveSubScreen(screen: String?) {
        _activeSubScreen.value = screen
    }

    val todayStepLogState: StateFlow<StepLog?> = flow {
        val today = getStartOfDay()
        emitAll(repository.getStepLogForDateFlow(today))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- Weather States ---
    private val _weatherState = MutableStateFlow<WeatherInfo?>(null)
    val weatherState: StateFlow<WeatherInfo?> = _weatherState.asStateFlow()

    // Current day's logs
    val todayWaterLogState: StateFlow<WaterLog?> = flow {
        val today = getStartOfDay()
        emitAll(repository.getWaterLogForDateFlow(today))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val todayNutritionLogsState: StateFlow<List<NutritionLog>> = flow {
        val start = getStartOfDay()
        val end = start + 24 * 60 * 60 * 1000
        emitAll(repository.getNutritionLogsForDayFlow(start, end))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Dynamic UI State ---
    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private var sensorManager: SensorManager? = null
    private var stepSensor: Sensor? = null
    private var initialStepsInSensor: Int = -1
    private var lastAccelerometerStepTime: Long = 0L

    private val stepListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event == null) return
            when (event.sensor.type) {
                Sensor.TYPE_STEP_COUNTER -> {
                    val totalSteps = event.values[0].toInt()
                    if (initialStepsInSensor == -1) {
                        initialStepsInSensor = totalSteps
                    } else {
                        val newSteps = totalSteps - initialStepsInSensor
                        if (newSteps > 0) {
                            addSteps(newSteps)
                            initialStepsInSensor = totalSteps
                        }
                    }
                }
                Sensor.TYPE_STEP_DETECTOR -> {
                    if (event.values[0] == 1.0f) {
                        addSteps(1)
                    }
                }
                Sensor.TYPE_ACCELEROMETER -> {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]
                    val magnitude = Math.sqrt((x * x + y * y + z * z).toDouble())
                    val currentTime = System.currentTimeMillis()
                    // Detect movement peak: exceeds 12.5 m/s^2, debounced by 350ms
                    if (magnitude > 12.5 && (currentTime - lastAccelerometerStepTime) > 350) {
                        addSteps(1)
                        lastAccelerometerStepTime = currentTime
                    }
                }
            }
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    fun reinitializeSensors() {
        Log.d("ViewModel", "Sensors are managed by StepCounterService foreground service.")
    }

    private val _isGitHubUpdateAvailable = MutableStateFlow(false)
    val isGitHubUpdateAvailable: StateFlow<Boolean> = _isGitHubUpdateAvailable.asStateFlow()

    fun checkForGitHubUpdates() {
        viewModelScope.launch {
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
                e.printStackTrace()
            }
        }
    }

    init {
        // التحقق من استعادة ملف التعريف عند تشغيل التطبيق في حال اكتمال شاشة التهيئة مسبقًا
        viewModelScope.launch {
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
                e.printStackTrace()
            }
        }

        // Check if lock is enabled on launch
        viewModelScope.launch {
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
            connectivityManager.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    _isNetworkAvailable.value = true
                }
                override fun onLost(network: Network) {
                    _isNetworkAvailable.value = false
                }
            })
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

    override fun onCleared() {
        super.onCleared()
    }

    fun refreshWeather(lat: Double = 30.0444, lon: Double = 31.2357) {
        viewModelScope.launch {
            _weatherState.value = WeatherService.fetchWeather(lat, lon)
        }
    }

    fun addSteps(stepsToAdd: Int) {
        viewModelScope.launch {
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

        viewModelScope.launch {
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

        viewModelScope.launch {
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
        viewModelScope.launch {
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
        viewModelScope.launch {
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
        viewModelScope.launch {
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
        viewModelScope.launch {
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
        viewModelScope.launch {
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

    fun calculateAge(birthDateMs: Long): Int {
        val birthCal = java.util.Calendar.getInstance()
        birthCal.timeInMillis = birthDateMs
        val todayCal = java.util.Calendar.getInstance()
        var calculatedAge = todayCal.get(java.util.Calendar.YEAR) - birthCal.get(java.util.Calendar.YEAR)
        if (todayCal.get(java.util.Calendar.DAY_OF_YEAR) < birthCal.get(java.util.Calendar.DAY_OF_YEAR)) {
            calculatedAge--
        }
        return calculatedAge.coerceAtLeast(0)
    }

    fun clearPregnancy() {
        viewModelScope.launch {
            repository.deletePregnancy()
        }
    }

    fun switchToPeriodTracking() {
        viewModelScope.launch {
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
        viewModelScope.launch {
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
        viewModelScope.launch {
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
        viewModelScope.launch {
            repository.deletePeriodLog(log)
        }
    }

    // Water tracker
    fun addWater(amountMl: Int) {
        viewModelScope.launch {
            val today = getStartOfDay()
            val existing = repository.getWaterLogForDate(today)
            if (existing != null) {
                repository.insertWaterLog(existing.copy(amountMl = existing.amountMl + amountMl))
            } else {
                repository.insertWaterLog(WaterLog(date = today, amountMl = amountMl))
            }
        }
    }

    fun resetTodayWater() {
        viewModelScope.launch {
            val today = getStartOfDay()
            val existing = repository.getWaterLogForDate(today)
            if (existing != null) {
                repository.insertWaterLog(existing.copy(amountMl = 0))
            }
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
        viewModelScope.launch {
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
        viewModelScope.launch {
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
        viewModelScope.launch {
            repository.insertMedication(
                MedicationLog(
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
            )
        }
    }

    fun decrementMedicationStock(medication: MedicationLog, amount: Int = 1) {
        viewModelScope.launch {
            val currentRemaining = medication.remainingQuantity
            val nextRemaining = (currentRemaining - amount).coerceAtLeast(0)
            repository.insertMedication(medication.copy(remainingQuantity = nextRemaining))
        }
    }

    fun toggleMedicationActive(medication: MedicationLog) {
        viewModelScope.launch {
            repository.insertMedication(medication.copy(isActive = !medication.isActive))
        }
    }

    fun deleteMedication(medication: MedicationLog) {
        viewModelScope.launch {
            repository.deleteMedication(medication)
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
        viewModelScope.launch {
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
        viewModelScope.launch {
            repository.deleteSleepLog(log)
        }
    }

    // --- عمليات ربط الشريك والرفيق (Companion Sync Operations) ---
    fun addPartnerMessage(senderName: String, messageText: String, category: String = "Support") {
        viewModelScope.launch {
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
        viewModelScope.launch {
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
        viewModelScope.launch {
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
        viewModelScope.launch {
            repository.deleteCravingLog(log)
        }
    }

    // --- تتبع نمو الجنين (Fetal Growth Tracker operations) ---
    fun addFetalGrowthLog(week: Int, weightGrams: Double, lengthCm: Double, notes: String?) {
        viewModelScope.launch {
            repository.insertFetalGrowthLog(
                FetalGrowthLog(
                    pregnancyWeek = week,
                    weightGrams = weightGrams,
                    lengthCm = lengthCm,
                    notes = notes
                )
            )
        }
    }

    fun deleteFetalGrowthLog(log: FetalGrowthLog) {
        viewModelScope.launch {
            repository.deleteFetalGrowthLog(log)
        }
    }

    // Symptom logger
    fun addSymptom(symptom: String, severity: Int, notes: String?) {
        viewModelScope.launch {
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
        viewModelScope.launch {
            repository.deleteSymptomLog(log)
        }
    }

    // Blood pressure logger
    fun addBloodPressureLog(systolic: Int, diastolic: Int, pulse: Int?, notes: String?) {
        viewModelScope.launch {
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
        viewModelScope.launch {
            repository.deleteBloodPressureLog(log)
        }
    }

    // Appointments
    fun addAppointment(title: String, dateTime: Long, doctor: String?, notes: String?) {
        viewModelScope.launch {
            repository.insertAppointment(
                Appointment(
                    dateTime = dateTime,
                    title = title,
                    doctorName = doctor,
                    notes = notes
                )
            )
        }
    }

    fun toggleAppointmentCompleted(appointment: Appointment) {
        viewModelScope.launch {
            repository.insertAppointment(appointment.copy(completed = !appointment.completed))
        }
    }

    fun deleteAppointment(appointment: Appointment) {
        viewModelScope.launch {
            repository.deleteAppointment(appointment)
        }
    }

    // Journal
    fun addJournalEntry(content: String, mood: String?) {
        viewModelScope.launch {
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
        viewModelScope.launch {
            repository.deleteJournalEntry(entry)
        }
    }

    // Qada Fasts (Ramadan missed days)
    fun addQadaFast(yearHijri: Int, missed: Int, completed: Int) {
        viewModelScope.launch {
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
        viewModelScope.launch {
            val newVal = if (increment) {
                (fast.completedDays + 1).coerceAtMost(fast.missedDays)
            } else {
                (fast.completedDays - 1).coerceAtLeast(0)
            }
            repository.insertQadaFast(fast.copy(completedDays = newVal))
        }
    }

    fun deleteQadaFast(fast: QadaFast) {
        viewModelScope.launch {
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
        viewModelScope.launch {
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
        viewModelScope.launch {
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
        viewModelScope.launch {
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
                    e.printStackTrace()
                }
                _gitHubSyncStatus.value = "تم التحديث بنجاح! مزامنة مصفوفة جوري مكتملة ✅"
            } else {
                _gitHubSyncStatus.value = "فشل التحديث. تم الاحتفاظ بالنسخة المحلية الذكية بنسبة ١٠٠% أوفلاين."
            }
        }
    }

    fun toggleDarkMode() {
        viewModelScope.launch {
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
        viewModelScope.launch {
            repository.factoryReset()
            _isLocked.value = false
        }
    }

    fun clearContractions() {
        viewModelScope.launch {
            repository.clearAllContractions()
        }
    }

    // --- Complex Calculations ---

    // Average cycle length and duration helper
    fun getCycleStats(): CycleStats {
        val logs = periodLogsState.value.sortedBy { it.startDate }
        if (logs.size < 2) {
            return CycleStats(averageCycleLength = 28, averagePeriodDuration = 5, totalCycles = logs.size, isEstimated = true)
        }

        var totalCycleLength = 0L
        var cycleCount = 0
        for (i in 1 until logs.size) {
            val diffMs = logs[i].startDate - logs[i-1].startDate
            val diffDays = diffMs / (24 * 60 * 60 * 1000)
            if (diffDays in 15..50) { // filter outliers to make predictive metrics realistic
                totalCycleLength += diffDays
                cycleCount++
            }
        }

        var totalPeriodDuration = 0L
        var validDurationCount = 0
        for (log in logs) {
            if (log.endDate != null) {
                val durDays = (log.endDate - log.startDate) / (24 * 60 * 60 * 1000)
                if (durDays in 1..15) {
                    totalPeriodDuration += durDays
                    validDurationCount++
                }
            }
        }

        val avgCycle = if (cycleCount > 0) (totalCycleLength / cycleCount).toInt() else 28
        val avgDuration = if (validDurationCount > 0) (totalPeriodDuration / validDurationCount).toInt() else 5

        return CycleStats(
            averageCycleLength = avgCycle,
            averagePeriodDuration = avgDuration,
            totalCycles = cycleCount,
            isEstimated = cycleCount == 0
        )
    }

    // Detect current menstrual cycle phase or late period
    fun getCurrentCyclePhase(): CyclePhaseInfo {
        val stats = getCycleStats()
        val logs = periodLogsState.value.sortedByDescending { it.startDate }
        if (logs.isEmpty()) {
            return CyclePhaseInfo(
                phaseName = "غير محدد",
                phaseArabic = "بيانات غير كافية",
                daysInPhase = 0,
                progressFraction = 0f,
                description = "سجّلي أول دورة شهرية لبدء التتبع التنبؤي الذكي للخصوبة والأطوار."
            )
        }

        val lastLog = logs.first()
        val currentMs = getCurrentTime()
        val daysSinceStart = ((currentMs - lastLog.startDate) / (24 * 60 * 60 * 1000)).toInt().coerceAtLeast(0)

        // Late period detection (Is it possible pregnancy?)
        val delayDays = daysSinceStart - stats.averageCycleLength
        if (delayDays >= 7) {
            return CyclePhaseInfo(
                phaseName = "Late",
                phaseArabic = "متأخرة عن موعدها ⚠️",
                daysInPhase = delayDays,
                progressFraction = 1f,
                description = "الدورة متأخرة بـ $delayDays أيام عن متوسط دورتك المعتاد. هل هناك احتمال للحمل؟ يمكنكِ إجراء اختبار منزلي وتحديث حالتكِ.",
                isLate = true
            )
        }

        val cycleLength = stats.averageCycleLength
        val duration = stats.averagePeriodDuration

        return when {
            daysSinceStart < duration -> {
                CyclePhaseInfo(
                    phaseName = "Menstruation",
                    phaseArabic = "طمث / حيض 🩸",
                    daysInPhase = daysSinceStart + 1,
                    progressFraction = (daysSinceStart + 1).toFloat() / duration.toFloat(),
                    description = "أنتِ الآن في طور الحيض. ركّزي على الراحة، اشربي سوائل دافئة، والعبادات معفاة منها حالياً."
                )
            }
            daysSinceStart < (cycleLength - 16) -> {
                CyclePhaseInfo(
                    phaseName = "Follicular",
                    phaseArabic = "الطور الجريبي 🌱",
                    daysInPhase = daysSinceStart - duration + 1,
                    progressFraction = (daysSinceStart - duration + 1).toFloat() / (cycleLength - 16 - duration).toFloat().coerceAtLeast(1f),
                    description = "يبدأ الجسم بالاستعداد لإنتاج البويضة. طاقة أعلى وتألق مستمر."
                )
            }
            daysSinceStart <= (cycleLength - 12) -> {
                val ovulationDays = daysSinceStart - (cycleLength - 16) + 1
                CyclePhaseInfo(
                    phaseName = "Ovulation",
                    phaseArabic = "طور الإباضة ✨",
                    daysInPhase = ovulationDays,
                    progressFraction = ovulationDays.toFloat() / 5f,
                    description = "فترة الخصوبة العالية والتبويض. مثالية لمتابعة الخصوبة وفرص الحمل."
                )
            }
            else -> {
                val lutealDays = daysSinceStart - (cycleLength - 12) + 1
                CyclePhaseInfo(
                    phaseName = "Luteal",
                    phaseArabic = "الطور اللوتيني 🪵",
                    daysInPhase = lutealDays,
                    progressFraction = lutealDays.toFloat() / 12f,
                    description = "فترة ما قبل الدورة التالية. قد تظهر بعض أعراض متلازمة ما قبل الطمث. كوني لطيفة مع نفسك."
                )
            }
        }
    }

    // Pregnancy progression details
    fun getPregnancyProgression(): PregnancyProgression? {
        val preg = pregnancyState.value ?: return null
        if (!preg.isPregnant) return null
        val lastPeriod = preg.lastPeriodDate ?: return null
        val currentMs = getCurrentTime()

        val totalDurationDays = 280L
        val passedDays = ((currentMs - lastPeriod) / (24L * 60 * 60 * 1000)).coerceAtLeast(0)
        val passedWeeks = (passedDays / 7).toInt()
        val remainingDays = (totalDurationDays - passedDays).coerceAtLeast(0)

        val currentTrimester = when {
            passedWeeks < 13 -> 1
            passedWeeks < 27 -> 2
            else -> 3
        }

        // Fetal comparisons
        val comparison = getFetalComparison(passedWeeks)

        return PregnancyProgression(
            weeks = passedWeeks,
            daysIntoWeek = (passedDays % 7).toInt(),
            remainingDays = remainingDays.toInt(),
            trimester = currentTrimester,
            dueDate = preg.dueDate ?: (lastPeriod + totalDurationDays * 24 * 60 * 60 * 1000),
            comparisonName = comparison.name,
            comparisonIcon = comparison.icon,
            developmentTip = comparison.developmentTip
        )
    }

    private fun getFetalComparison(weeks: Int): FetalComparison {
        return when {
            weeks < 1 -> FetalComparison("تخصيب خلايا", "🧬", "الجسم يستعد للملحمة المذهلة! ركّزي على حمض الفوليك والراحة.")
            weeks < 5 -> FetalComparison("بذرة الخشخاش", "🪹", "بذرة صغيرة جدًا في طور الانغراس. الخلايا الأولى لقلب الطفل تبدأ بالتخلق.")
            weeks < 9 -> FetalComparison("حبة عنب بري", "🫐", "يبدأ طفلك بالحركة البسيطة جداً، وتتشكل براعم الأيدي والأرجل الرائعة.")
            weeks < 13 -> FetalComparison("حبة تين كاملة", "🫓", "تخلق رائع لأعضاء الجنين الأساسية. الذقن والأنف والرموش تبدأ في الظهور.")
            weeks < 17 -> FetalComparison("حبة ليمون نضرة", "🍋", "الطفل يمكنه الآن ابتلاع السائل الأمنيوسي والتعبير بتعابير وجه دقيقة.")
            weeks < 21 -> FetalComparison("حبة بطاطا حلوة", "🍠", "يكسو جسم الطفل زغب رقيق، وجهازه السمعي يعمل بشكل مدهش لسماع صوتكِ.")
            weeks < 25 -> FetalComparison("حبة رمان مكتملة", "🍆", "حركات الركل تزداد قوة، وتظهر بصمات الأصابع الفريدة بوضوح.")
            weeks < 29 -> FetalComparison("رأس خس رائع", "🥬", "يتعلم الجنين فتح وإغلاق عينيه اللطيفتين، ويبدأ بالتنفس التدريبي.")
            weeks < 33 -> FetalComparison("ثمرة جوز هند متينة", "🥥", "نمو دماغي وعظمي هائل. يحتاج إلى الكالسيوم بكثرة لبناء هيكله.")
            weeks < 37 -> FetalComparison("شمامة أو بطيخة صغيرة", "🍈", "الطفل يأخذ وضعية الولادة الطبيعية (الرأس لأسفل غالباً) ويكتسب وزناً دهنياً دافئاً.")
            else -> FetalComparison("بطيخة كاملة مكتملة النضج", "🍉", "الطفل جاهز تماماً للخروج ومقابلتكِ بأمان! دعواتنا لكِ بولادة يسيرة 🌸.")
        }
    }

    // Dynamic water target: 2000ml default. If pregnant, add 500ml. Also adds extra weather-based hydration requirements.
    fun getWaterTarget(): Int {
        val base = if (pregnancyState.value != null) 2500 else 2000
        val extra = weatherState.value?.extraWaterMl ?: 0
        return base + extra
    }

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
            "Underweight" -> 200 // suggest a slight healthy surplus
            "Obese" -> -100 // cautious
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

    data class NutrientTarget(
        val name: String,
        val targetVal: Double,
        val unit: String,
        val isLimit: Boolean = false
    )

    fun getNutrientTargets(): Map<String, NutrientTarget> {
        val isPreg = pregnancyState.value != null
        return mapOf(
            "protein" to NutrientTarget("البروتين", if (isPreg) 75.0 else 60.0, "جم"),
            "carbs" to NutrientTarget("النشويات", if (isPreg) 195.0 else 150.0, "جم"),
            "fat" to NutrientTarget("الدهون", if (isPreg) 75.0 else 65.0, "جم"),
            "sugar" to NutrientTarget("السكريات", 30.0, "جم", isLimit = true),
            "fiber" to NutrientTarget("الألياف", if (isPreg) 28.0 else 25.0, "جم"),
            "iron" to NutrientTarget("الحديد", if (isPreg) 27.0 else 18.0, "ملجم"),
            "calcium" to NutrientTarget("الكالسيوم", 1000.0, "ملجم"),
            "folate" to NutrientTarget("الفوليك", if (isPreg) 600.0 else 400.0, "مكجم"),
            "potassium" to NutrientTarget("البوتاسيوم", 4700.0, "ملجم"),
            "sodium" to NutrientTarget("الصوديوم", 2000.0, "ملجم", isLimit = true),
            "magnesium" to NutrientTarget("الماغنسيوم", if (isPreg) 360.0 else 320.0, "ملجم"),
            "vitaminC" to NutrientTarget("فيتامين سي", if (isPreg) 85.0 else 75.0, "ملجم"),
            "vitaminA" to NutrientTarget("فيتامين أ", if (isPreg) 770.0 else 700.0, "مكجم")
        )
    }

    // Checking for 5-1-1 contraction warning
    fun checkContractionWarning(): Boolean {
        val list = contractionLogsState.value
        if (list.size < 3) return false

        // Take last 3 contractions
        val last3 = list.take(3)
        val allLastHour = last3.all { getCurrentTime() - it.startTime <= 60 * 60 * 1000 }
        if (!allLastHour) return false

        val meanDurationOk = last3.all { it.durationSeconds >= 45 } // around 1 minute (45s to 60s)
        val meanIntervalOk = last3.dropLast(1).all { it.intervalSeconds in 180..360 } // every 3 to 6 minutes

        return meanDurationOk && meanIntervalOk
    }

    // ==========================================
    // --- مؤونتي (Maonaty) Smart Home Functions ---
    // ==========================================

    fun addInventoryItem(name: String, category: String, quantity: Double, minQuantity: Double, unit: String, priceEstimate: Double, id: Int = 0) {
        viewModelScope.launch {
            repository.insertInventoryItem(
                MaonatyInventoryItem(
                    id = id,
                    name = name,
                    category = category,
                    quantity = quantity,
                    minQuantity = minQuantity,
                    unit = unit,
                    priceEstimate = priceEstimate
                )
            )
        }
    }

    fun deleteInventoryItem(item: MaonatyInventoryItem) {
        viewModelScope.launch {
            repository.deleteInventoryItem(item)
        }
    }

    fun clearInventory() {
        viewModelScope.launch {
            repository.clearInventory()
        }
    }

    fun addShoppingItem(name: String, category: String, quantity: Double, unit: String, price: Double, autoGenerated: Boolean = false) {
        viewModelScope.launch {
            repository.insertShoppingItem(
                MaonatyShoppingItem(
                    name = name,
                    category = category,
                    quantity = quantity,
                    unit = unit,
                    price = price,
                    autoGenerated = autoGenerated
                )
            )
        }
    }

    fun deleteShoppingItem(item: MaonatyShoppingItem) {
        viewModelScope.launch {
            repository.deleteShoppingItem(item)
        }
    }

    fun toggleShoppingItemBought(item: MaonatyShoppingItem) {
        viewModelScope.launch {
            repository.updateShoppingItemStatus(item.id, !item.isBought)
        }
    }

    fun clearShoppingList() {
        viewModelScope.launch {
            repository.clearShoppingList()
        }
    }

    fun addHouseholdTask(title: String, category: String, priority: String, dueDate: Long) {
        viewModelScope.launch {
            repository.insertHouseholdTask(
                MaonatyHouseholdTask(
                    title = title,
                    category = category,
                    priority = priority,
                    dueDate = dueDate
                )
            )
        }
    }

    fun deleteHouseholdTask(task: MaonatyHouseholdTask) {
        viewModelScope.launch {
            repository.deleteHouseholdTask(task)
        }
    }

    fun toggleTaskCompleted(task: MaonatyHouseholdTask) {
        viewModelScope.launch {
            repository.updateTaskStatus(task.id, !task.isCompleted)
        }
    }

    fun clearHouseholdTasks() {
        viewModelScope.launch {
            repository.clearHouseholdTasks()
        }
    }

    // Auto-Shopping List Generation based on low stock inventory
    fun generateAutoShoppingList() {
        viewModelScope.launch {
            val inventory = allInventoryItemsState.value
            val currentShopping = allShoppingItemsState.value
            
            inventory.forEach { invItem ->
                if (invItem.quantity <= invItem.minQuantity) {
                    // Check if already in shopping list (and not bought yet)
                    val alreadyAdded = currentShopping.any { shopItem ->
                        shopItem.name.trim().equals(invItem.name.trim(), ignoreCase = true) && !shopItem.isBought
                    }
                    if (!alreadyAdded) {
                        // Calculate standard top-up quantity (e.g. restore to 2x minQuantity or at least 1.0)
                        val buyQty = ((invItem.minQuantity * 2.0) - invItem.quantity).coerceAtLeast(1.0)
                        repository.insertShoppingItem(
                            MaonatyShoppingItem(
                                name = invItem.name,
                                category = invItem.category,
                                quantity = buyQty,
                                unit = invItem.unit,
                                price = invItem.priceEstimate,
                                isBought = false,
                                autoGenerated = true
                            )
                        )
                    }
                }
            }
        }
    }

    // Seed realistic sample data
    fun populateMaonatySampleData() {
        viewModelScope.launch {
            // Check if inventory is empty
            if (allInventoryItemsState.value.isEmpty()) {
                val samples = listOf(
                    MaonatyInventoryItem(name = "أرز مصري فاخر", category = "معلبات وحبوب", quantity = 0.5, minQuantity = 2.0, unit = "كيلوجرام", priceEstimate = 35.0),
                    MaonatyInventoryItem(name = "مكرونة قلم", category = "معلبات وحبوب", quantity = 1.0, minQuantity = 3.0, unit = "كيس", priceEstimate = 15.0),
                    MaonatyInventoryItem(name = "زيت عباد الشمس", category = "معلبات وحبوب", quantity = 0.4, minQuantity = 1.0, unit = "لتر", priceEstimate = 80.0),
                    MaonatyInventoryItem(name = "حليب كامل الدسم", category = "منتجات ألبان", quantity = 3.0, minQuantity = 1.0, unit = "لتر", priceEstimate = 40.0),
                    MaonatyInventoryItem(name = "جبنة بيضاء فيتا", category = "منتجات ألبان", quantity = 0.25, minQuantity = 0.5, unit = "كيلوجرام", priceEstimate = 60.0),
                    MaonatyInventoryItem(name = "طماطم طازجة", category = "خضار وفواكه", quantity = 0.5, minQuantity = 1.5, unit = "كيلوجرام", priceEstimate = 15.0),
                    MaonatyInventoryItem(name = "ليمون أصفر", category = "خضار وفواكه", quantity = 1.0, minQuantity = 0.5, unit = "كيلوجرام", priceEstimate = 25.0),
                    MaonatyInventoryItem(name = "صدور فراخ بانيه", category = "لحوم ودواجن", quantity = 0.0, minQuantity = 1.0, unit = "كيلوجرام", priceEstimate = 220.0),
                    MaonatyInventoryItem(name = "بهارات لحمة مشكلة", category = "بهارات وتوابل", quantity = 50.0, minQuantity = 100.0, unit = "جرام", priceEstimate = 0.5),
                    MaonatyInventoryItem(name = "مسحوق غسيل ملابس", category = "أدوات تنظيف", quantity = 2.5, minQuantity = 1.0, unit = "كيلوجرام", priceEstimate = 180.0),
                    MaonatyInventoryItem(name = "ملح طعام ناعم", category = "بهارات وتوابل", quantity = 3.0, minQuantity = 1.0, unit = "كيس", priceEstimate = 5.0)
                )
                samples.forEach { repository.insertInventoryItem(it) }
            }

            // Check if tasks are empty
            if (allHouseholdTasksState.value.isEmpty()) {
                val baseTime = System.currentTimeMillis()
                val tasks = listOf(
                    MaonatyHouseholdTask(title = "🧼 تنظيف الثلاجة وترتيب أرفف المطبخ", category = "🧼 تنظيف وترتيب", priority = "⚡ متوسط", dueDate = baseTime + 24 * 60 * 60 * 1000),
                    MaonatyHouseholdTask(title = "🛠️ تغيير فلتر مياه المطبخ السبع مراحل", category = "🛠️ صيانة وأعطال", priority = "🔴 عاجل", dueDate = baseTime + 2 * 24 * 60 * 60 * 1000),
                    MaonatyHouseholdTask(title = "📦 جرد الخزانة وجهاز التكييف قبل الصيف", category = "📦 جرد وتخزين", priority = "🟢 عادي", dueDate = baseTime + 5 * 24 * 60 * 60 * 1000),
                    MaonatyHouseholdTask(title = "📅 سداد فاتورة الكهرباء والغاز الطبيعي", category = "📅 شؤون منزلية", priority = "🔴 عاجل", dueDate = baseTime + 12 * 60 * 60 * 1000)
                )
                tasks.forEach { repository.insertHouseholdTask(it) }
            }
        }
    }

    // Export local Ma'onaty data as JSON String
    fun exportMaonatyBackup(): String {
        return try {
            val root = org.json.JSONObject()
            
            val invArray = org.json.JSONArray()
            allInventoryItemsState.value.forEach {
                val obj = org.json.JSONObject()
                obj.put("name", it.name)
                obj.put("category", it.category)
                obj.put("quantity", it.quantity)
                obj.put("minQuantity", it.minQuantity)
                obj.put("unit", it.unit)
                obj.put("priceEstimate", it.priceEstimate)
                invArray.put(obj)
            }
            root.put("inventory", invArray)

            val shopArray = org.json.JSONArray()
            allShoppingItemsState.value.forEach {
                val obj = org.json.JSONObject()
                obj.put("name", it.name)
                obj.put("category", it.category)
                obj.put("quantity", it.quantity)
                obj.put("unit", it.unit)
                obj.put("price", it.price)
                obj.put("isBought", it.isBought)
                obj.put("autoGenerated", it.autoGenerated)
                shopArray.put(obj)
            }
            root.put("shopping", shopArray)

            val taskArray = org.json.JSONArray()
            allHouseholdTasksState.value.forEach {
                val obj = org.json.JSONObject()
                obj.put("title", it.title)
                obj.put("category", it.category)
                obj.put("priority", it.priority)
                obj.put("dueDate", it.dueDate)
                obj.put("isCompleted", it.isCompleted)
                taskArray.put(obj)
            }
            root.put("tasks", taskArray)

            root.toString(2)
        } catch (e: Exception) {
            ""
        }
    }

    // Import Ma'onaty from JSON String
    fun importMaonatyBackup(jsonString: String): Boolean {
        return try {
            val root = org.json.JSONObject(jsonString)
            
            viewModelScope.launch {
                // Parse Inventory
                if (root.has("inventory")) {
                    repository.clearInventory()
                    val arr = root.getJSONArray("inventory")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        repository.insertInventoryItem(
                            MaonatyInventoryItem(
                                name = obj.getString("name"),
                                category = obj.getString("category"),
                                quantity = obj.getDouble("quantity"),
                                minQuantity = obj.getDouble("minQuantity"),
                                unit = obj.getString("unit"),
                                priceEstimate = obj.optDouble("priceEstimate", 0.0)
                            )
                        )
                    }
                }

                // Parse Shopping List
                if (root.has("shopping")) {
                    repository.clearShoppingList()
                    val arr = root.getJSONArray("shopping")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        repository.insertShoppingItem(
                            MaonatyShoppingItem(
                                name = obj.getString("name"),
                                category = obj.getString("category"),
                                quantity = obj.getDouble("quantity"),
                                unit = obj.getString("unit"),
                                price = obj.optDouble("price", 0.0),
                                isBought = obj.optBoolean("isBought", false),
                                autoGenerated = obj.optBoolean("autoGenerated", false)
                            )
                        )
                    }
                }

                // Parse Tasks
                if (root.has("tasks")) {
                    repository.clearHouseholdTasks()
                    val arr = root.getJSONArray("tasks")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        repository.insertHouseholdTask(
                            MaonatyHouseholdTask(
                                title = obj.getString("title"),
                                category = obj.getString("category"),
                                priority = obj.getString("priority"),
                                dueDate = obj.getLong("dueDate"),
                                isCompleted = obj.optBoolean("isCompleted", false)
                            )
                        )
                    }
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}

// Support Data Classes
data class CycleStats(
    val averageCycleLength: Int,
    val averagePeriodDuration: Int,
    val totalCycles: Int,
    val isEstimated: Boolean
)

data class CyclePhaseInfo(
    val phaseName: String,
    val phaseArabic: String,
    val daysInPhase: Int,
    val progressFraction: Float,
    val description: String,
    val isLate: Boolean = false
)

data class PregnancyProgression(
    val weeks: Int,
    val daysIntoWeek: Int,
    val remainingDays: Int,
    val trimester: Int,
    val dueDate: Long,
    val comparisonName: String,
    val comparisonIcon: String,
    val developmentTip: String
)

data class FetalComparison(
    val name: String,
    val icon: String,
    val developmentTip: String
)

data class CalorieGoal(
    val target: Int,
    val details: String
)

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
