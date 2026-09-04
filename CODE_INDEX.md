# Codebase Index & Architecture Overview

## Architecture & Data Flow

### Data Layer (`/app/src/main/java/com/example/data/`)
- **`Database.kt` / `AppDatabase.kt`**: Encrypted Room Database (`AppDatabase`) via SQLCipher with passphrase protected in `EncryptedSharedPreferences`. Contains schema versioning, baseline migrations (v14 -> v15 -> v16 -> v17), and TypeConverters for complex objects.
- **`Entities.kt`**: Strongly-typed Room Entities:
  - `PregnancyRecord`: Active and historical pregnancy records (LMP, EDD, baby name, active flag).
  - `MedicationLog` & `MedicationAdherenceLog`: Medication schedules, dosage frequency (1..6+ times/day), first-dose timing, food relations, and daily adherence history.
  - `SymptomLog`: Comprehensive physical and psychological symptom logs with severity ratings.
  - `ContraceptiveMethod`: Birth control method tracking with start/end dates and correlated symptoms.
  - `BloodPressureLog`, `WeightRecord`: Maternal vitals and biometric tracking.
  - `FetalGrowthLog`: Ultrasound/sonar measurements (BPD, HC, AC, FL, EFW) linked to active pregnancy.
  - `KickSession`: Fetal movement counting sessions with duration and kick counts.
  - `ContractionRecord`: Labor contraction timer with 5-1-1 active labor detection.
  - `AppointmentRecord`: Prenatal doctor appointments, sonograms, and clinical tests.
  - `SleepRecord`: Nightly sleep duration, quality scores, and sleep stage analysis.
  - `PantryItem`, `SmartRecipe`, `PantryTask`: Home pantry inventory and meal recipes.
  - `PartnerMessage`, `PartnerProfile`: Local encrypted partner sync link and messages.
  - `BabyNote`: Secret journal entries and mood reflections.
  - `QadaFastRecord`: Missed fasts (قضاء الصيام) tracking and compensation logs.
- **`WomanCompanionDao.kt`**: Comprehensive DAO interfaces for all CRUD operations, Flow streams, and custom analytics queries.
- **`WomanCompanionRepository.kt`**: Clean repository abstraction bridging DAOs, SharedPreferences, and UI State.
- **`EgyptianFoodDatabase.kt`**: Built-in Egyptian cuisine database with calorie, macronutrient, micronutrient (Iron, Calcium, Folate) data, and NLP text parsing.
- **`GeminiService.kt` & `OfflineJouriEngine.kt`**: Local-first hybrid intelligence engine with offline rule-based matrix (>500 tips) and optional secure Gemini API integration passing keys via HTTP `x-goog-api-key` headers.
- **`SymptomTriage.kt`**: Clinical safety triage protocol evaluating urgent medical signs (bleeding, reduced movement, severe pain) with emergency hotline actions.
- **`WeatherService.kt`**: Location-aware weather retrieval with health and hydration recommendations.
- **`ApiKeyRepository.kt`**: Secure Gemini API key management utilizing `EncryptedSharedPreferences`.
- **`GitHubSyncRepository.kt`**: Remote advice matrix update sync engine with safe HTTPS stream and connection management.

---

### Presentation & UI Layer (`/app/src/main/java/com/example/ui/`)
- **`WomanCompanionViewModel.kt`**: Central ViewModel managing UI StateFlows, calculations (EDD, cycle, ovulation, sleep score, pregnancy stats, vitals, streaks), and Coroutine exception safety.
- **`SoftTheme.kt` & `theme/`**: Centralized Material Design 3 design system, color palette (`SoftPink`, `CardSlate`, `DeepSlate`, `MintTeal`, `SoftTeal`, `NifasRose`, `PregnancyPurple`), and typography.
- **`GlassmorphicComponents.kt`**: Reusable frosted-glass cards, animated spring buttons, badges, and gradient backgrounds.
- **`AnalyticsCharts.kt`**: Smooth canvas sparklines, progress rings, and trend charts.
- **`PregnancyDashboardScreen.kt`**: Primary Pregnancy Dashboard:
  - 270° Horseshoe Arc radial pregnancy progress gauge with pulsing thumb indicator.
  - Interactive Bento Grid (baby organ development, live activity sparkline, next medical appointment).
  - Daily Vitals Summary Card (water intake, step progress, upcoming appointment shortcuts).
  - Daily Vitamins Card with adherence checkboxes and quick navigation to Medication manager.
  - Quick Craving Log Card with instant category chips and nutrition navigation.
  - Pregnancy Myth Buster Card with expandable clinical facts and direct Jouri AI chat.
  - Egyptian Food Search Widget with nutritional lookup.
  - Milestone Celebrations & Past Pregnancy Memory Cards.
- **`PeriodTrackerScreen.kt`**: Menstrual cycle tracker, ovulation predictor, fertile window calculator, and multi-indicator smart calendar (Period, Pregnancy, Nifas, Ovulation).
- **`NutritionAndWaterScreen.kt`**: Hydration tracker (dynamic daily target), NLP natural language meal logger, Egyptian food nutrition explorer, and nutrient conflict alerts.
- **`SymptomAndMedsScreen.kt`**:
  - Medication & Supplement Manager with flexible daily frequencies (1, 2, 3, 4, 5, 6+ times/day), custom steppers, first-dose time selector (AM/PM), and stock tracking.
  - Symptom logging with severity and clinical red-flag triage alerts.
- **`ToolsScreen.kt`**: Modular tools hub coordinating all secondary health and lifestyle utilities.
- **`ToolsSubScreens.kt`**:
  - `HospitalBagScreen`: Categorized hospital packing checklist (Mom, Baby, Documents).
  - `FoodSafetyScreen`: Pregnancy food safety search database (Safe, Caution, Avoid).
  - `BackupRestoreSubScreen`: Encrypted JSON/database export and import engine.
- **`CompanionFeatureScreens.kt`**: Lightweight facade delegating to modularized `ui/companion/` feature screens.
- **`companion/PartnerSyncScreen.kt`**: Local encrypted partner link & encouraging messages.
- **`companion/HomePharmacyScreen.kt`**: Home medicine cabinet stock, expiry dates, and pregnancy safety status.
- **`companion/CravingScreen.kt`**: Pregnancy craving logger, analytical commentary, and partner message generator.
- **`companion/JouriNotificationsAndUpdates.kt`**: Feature updates discovery banner (`NewFeaturesUpdatesBanner`), smart health alerts (`SmartAlertCard`), and notifications center (`JouriNotificationsDialog`).
- **`PregnancyScreens.kt`**:
  - `SmartConceptionSubScreen`: Conception planner and ovulation date calculator.
  - `FetalKicksSubScreen`: Fetal kick session counter and history.
  - `ContractionsSubScreen`: Labor contraction timer with 5-1-1 alert banner.
- **`PregnancyHistoryScreen.kt`**: Historical pregnancy records, delivery outcomes, and timeline logs.
- **`FetalGrowthSubScreen.kt`**: Live expected week metrics, sonar measurement logs, and weight deviation analysis.
- **`DangerSignalsScreen.kt`**: Emergency danger signs checklist with instant 123 emergency call action.
- **`SleepAnalyzerScreen.kt`**: Sleep duration, quality breakdown, and safe sleeping postures for pregnancy.
- **`MaonatySubScreen.kt`**: Smart pantry manager, ingredients inventory, and healthy recipe generator.
- **`FitnessScreen.kt`**: Lightweight facade delegating to modularized `ui/fitness/` subsystem.
- **`fitness/FitnessModels.kt`**: Specialized data models (`JouriStep`, `JouriExercise`).
- **`fitness/FitnessDrawings.kt`**: Custom anatomical pelvic floor drawings, gear streak trackers, and exercise posture line illustrations.
- **`fitness/JouriWorkoutTimerDialog.kt`**: Interactive countdown timer dialog with dynamic breathing cues and completion celebration.
- **`fitness/StepPedometerDashboard.kt`**: Real-time hardware step tracking dashboard, distance/calories calculators, and 7-day canvas analytics chart.
- **`fitness/FitnessScreen.kt`**: Main container supporting trimester filtering, category tabs, and exercise instruction modals.
- **`AppointmentsScreen.kt`**: Prenatal doctor appointments scheduler, test reminders, and history.
- **`JournalScreen.kt`**: Private emotional journal, mood check-ins, and thoughts recorder.
- **`ContraceptiveScreen.kt`**: Contraceptive method tracker, side-effect correlation, and clinical guidelines.
- **`QadaFastScreen.kt`**: Ramadan missed fasts tracker (قضاء الصيام) with progress counters.
- **`SettingsScreen.kt`**: PIN security lock, encrypted database backup/restore, battery optimization wizard, exact alarm settings, and PDF/Text doctor report exporter.
- **`settings/BackupRestoreCard.kt`**: Dedicated encrypted database SAF export & import card with passphrase protection.
- **`OnboardingScreen.kt`**: Main onboarding orchestrator managing 4-step wizard navigation, form state, and persistence.
- **`onboarding/StepPersonalInfo.kt`**: Step 1 - User full name, nickname, and birth date picker.
- **`onboarding/StepChronicDiseases.kt`**: Step 2 - Chronic diseases and preventative health profile (blood pressure, diabetes, allergies).
- **`onboarding/StepCyclePregnancy.kt`**: Step 3 - Pregnancy status toggle and menstrual cycle tracking date selection.
- **`onboarding/StepSummaryAndSave.kt`**: Step 4 - Health passport review summary, battery exemption guidance, and completion trigger.
- **`JouriChatDialog.kt`**: AI Smart Companion chat dialog with conversational history and emergency symptom detection.
- **`chat/ArabicVirtualKeyboard.kt`**: Custom Arabic on-screen virtual keyboard for quick input.
- **`chat/CloudAiConsentDialog.kt`**: Privacy-first opt-in dialog for Google Gemini cloud AI consultation.
- **`chat/JouriAvatarComponents.kt`**: Animated multi-state avatar (happy, attentive, reassuring, celebratory) and response skeletons.
- **`chat/JouriChatMessageList.kt`**: High-performance lazy column message feed with specialized speech bubbles.
- **`chat/JouriConsultationCatalog.kt`**: Interactive multi-category preset consultation chips and quick questions.
- **`Screens.kt`**: Static reference data (`hierarchicalDatabase`, `adviceLibrary`).

---

### System, Utilities & Background Services
- **`reminder/ReminderScheduler.kt`**: Exact Android `AlarmManager` scheduling with recurring daily alarm slots (`medId * 100 + i`), snooze actions, and mark-taken notifications.
- **`service/StepCounterService.kt`**: Persistent foreground health service (`health` type) reading hardware step sensors with midnight day-boundary rollover handling.
- **`receiver/BootReceiver.kt`**: Automatically reschedules alarms and restarts step tracking on device reboot.
- **`worker/MedicationMonitorWorker.kt`**: WorkManager background job monitoring low stock and expiring medicines.
- **`worker/WeeklyDigestWorker.kt`**: Weekly maternal digest compilation and notification worker.
- **`util/AppLogger.kt`**: Centralized secure logging utility stripping PII in release builds.
- **`util/ArabicFormatter.kt`**: Grammatically correct Arabic plural formatting (`formatArabicDays`).
- **`util/BackupManager.kt`**: Atomic SQLCipher database export and import with user passphrases and integrity verification.
- **`util/PdfReportGenerator.kt`**: Formatted Arabic medical summary PDF generator for doctor appointments.

---

### CI/CD & Automation
- **`.github/workflows/android.yml`**: GitHub Actions workflow for automated CI/CD: Java 17 setup, debug keystore decoding from Base64, gradlew execution permissions, Gradle caching, unit testing (`testDebugUnitTest`), debug APK assembly (`assembleDebug`), and APK artifact upload.
