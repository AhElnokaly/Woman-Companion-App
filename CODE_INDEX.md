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
- **`SoftTheme.kt` & `theme/`**: Centralized Material Design 3 design system, color palette (`SoftPink`, `CardSlate`, `DeepSlate`, `MintTeal`, `SoftTeal`, `NifasRose`, `PregnancyPurple`, `EmeraldPrimary`, `WaterBlue`, `FoodOrange`, `SleepPurple`, `WomenPink`, `JouriTurquoise`), and typography.
- **`ui/dashboard/`**: Modern Health & Wellness Super-App Dashboard Suite:
  - `ModernHealthHeader.kt`: Status bar welcoming header with avatar, user greeting, dark/light theme mode toggle button, and notification bell badge.
  - `HealthScoreHeroCard.kt`: 87% circular progress hero card with motivating subtitle and wellness score.
  - `UpcomingMedicationCard.kt`: Active medication dose card (e.g., Amlodipine 5mg / active med) with time badge, countdown tag, and direct "تسجيل التناول" action.
  - `ModernBentoGrid.kt`: Balanced 2x2 grid (Water Intake with progress bar, Nutrition & Macros with meal count, Sleep Duration & Quality with visual bar, Symptoms Tracker with severity indicator) plus full-width Women's Health tracker banner and Jouri AI Smart Insights card.
  - `CurvedFloatingBottomBar.kt`: Elevated curved bottom navigation bar with prominent center circular (+) action button.
  - `QuickActionModal.kt`: Instant modal dialog triggered by the center (+) button (quick water log, meal logging, medication check, symptoms tracker, and Jouri AI chat).
- **`GlassmorphicComponents.kt`**: Reusable frosted-glass cards, animated spring buttons, badges, and gradient backgrounds.
- **`AnalyticsCharts.kt`**: Smooth canvas sparklines, progress rings, and trend charts.
- **`PregnancyDashboardScreen.kt`**: Primary Pregnancy Dashboard:
  - 270° Horseshoe Arc radial pregnancy progress gauge with pulsing thumb indicator.
  - Baby Info & Doctor Care Hub (`PregnancyBabyInfoDisplayCard`): Shows baby identity, proposed name, latest ultrasound weight & length with reference comparison tag, and upcoming doctor appointment with quick-schedule trigger.
  - Unified Baby & Doctor Care Dialog (`BabyAndDoctorCareDialog.kt`): 3-tab dialog for baby name/gender, ultrasound measurements with reference standard autofill, and doctor appointment scheduling with native date/time pickers.
  - Dynamic & Expandable Daily Vitamins Card (`DailyVitaminsCard`): Connected live to Room `activeMedicationsState` and `allMedicationAdherenceLogsState`.
  - Expandable Quick Actions Card (`PregnancyQuickActionsCard`): Water log, BP dialog, Journal, SOS Breathing, and fetal kick counter with collapsible header.
  - **`JouriExploreCardsCarousel.kt` (`pregnancy/PregnancyDashboardExploreCards.kt`)**: Horizontal swipeable carousel condensing secondary explore widgets (Jouri Tips & Weather, Myth Buster, Egyptian Food Search, Quick Craving Log) with animated dots indicator.
  - Milestone Celebrations & Past Pregnancy Memory Cards.
  - Quick Profile Edit entry directly accessible from the header and Settings.
- **`profile/EditProfileDialog.kt`**: Comprehensive profile editor dialog (Name, Nickname, BirthDate, Height, Pre-pregnancy Weight, BP & Diabetes conditions, Baby Name, BMI auto-calculator).
- **`settings/ProfileCard.kt`**: Profile card in Settings screen providing summary of biometrics and quick trigger for editing.
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
- **`util/JouriSpeechHelper.kt`**: Text-to-speech Arabic voice manager (`JouriSpeechManager`, `rememberJouriSpeechManager`) for reading Jouri wellness tips and weather advice aloud with reassuring maternal tone.
- **`ui/report/DoctorVisitReportDialog.kt`**: OB-GYN medical report preview and export dialog with one-click clipboard copying, Android share sheet intent, and vital records breakdown.
- **`ui/meds/DrugConflictChecker.kt`**: Smart Drug-Nutrient conflict banner (`DrugNutrientConflictBanner`) detecting concurrent Iron and Calcium ingestion to prevent malabsorption and maternal anemia.

---

### CI/CD & Automation
- **`.github/workflows/android.yml`**: GitHub Actions workflow for automated CI/CD: JDK 21 setup (matching AGP 9.1+ requirement), automated keystore decoding and keytool fallback generation, environment file preparation, gradlew execution permissions, Gradle caching, debug APK assembly (`assembleDebug`), direct APK artifact upload, and non-blocking unit test execution.
- **`docs/ci-cd-reference.md`**: Canonical reference and troubleshooting guide for modern Android CI/CD pipelines (AGP 9.1+, Gradle 9.3.1, JDK 21, KSP2 headless fix, signing validation fallback, and build performance optimization).
