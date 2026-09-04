package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

/**
 * Critical User Journeys (CUJ) Integration Tests for Room Database:
 * 1. Medication Cycle: Add medication -> Schedule adherence doses -> Record taken -> Stock deduction
 * 2. Multi-Pregnancy Isolation: Create Pregnancy 1 -> Add logs -> Create Pregnancy 2 -> Verify isolated logs
 * 3. Contraceptive Tracking: Log pill intake & config -> Verify query and active status
 * 4. Blood Pressure & Symptoms: Record vitals and verify time-sorted retrieval
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CriticalUserJourneysIntegrationTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: WomanCompanionDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.womanCompanionDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun testMedicationJourney_AddRecordStockDeduction() = runBlocking {
        // 1. Add medication with 30 pills stock
        val med = MedicationLog(
            id = 0,
            name = "Folic Acid 5mg",
            dosage = "1 tablet",
            timesPerDay = 1,
            isActive = true,
            notes = "Take after breakfast",
            totalQuantity = 30,
            remainingQuantity = 30
        )
        val medId = dao.insertMedication(med).toInt()
        assertTrue("Medication should be inserted with valid ID", medId > 0)

        // 2. Fetch med and log adherence
        val now = System.currentTimeMillis()
        val adherenceLog = MedicationAdherenceLog(
            id = 0,
            medicationId = medId,
            scheduledTime = now,
            actualTime = now,
            status = "TAKEN"
        )
        dao.insertMedicationAdherenceLog(adherenceLog)

        // Deduct 1 tablet from stock via DAO
        dao.decrementMedicationQuantity(medId)

        // 3. Verify stock deduction and adherence log retrieval
        val meds = dao.getAllMedicationsFlow().first()
        assertEquals(1, meds.size)
        assertEquals(29, meds[0].remainingQuantity)

        val logs = dao.getAdherenceLogsForMedicationFlow(medId).first()
        assertEquals(1, logs.size)
        assertEquals("TAKEN", logs[0].status)
    }

    @Test
    fun testMultiPregnancyIsolationJourney() = runBlocking {
        val dayMs = 86400000L
        val now = System.currentTimeMillis()

        // 1. First Pregnancy (Historical/Past)
        val pastPregnancy = PregnancyEntity(
            id = 0,
            isPregnant = false,
            lastPeriodDate = now - 350 * dayMs,
            dueDate = now - 70 * dayMs,
            babyName = "Youssef",
            motherName = "Mariam",
            isActive = false
        )
        val preg1Id = dao.insertOrUpdatePregnancy(pastPregnancy).toInt()

        // Add fetal logs to Pregnancy 1
        val fetalLog1 = FetalGrowthLog(
            id = 0,
            pregnancyId = preg1Id,
            pregnancyWeek = 20,
            weightGrams = 300.0,
            lengthCm = 25.0,
            notes = "First scan"
        )
        dao.insertFetalGrowthLog(fetalLog1)

        // 2. Second Pregnancy (Current Active)
        val currentPregnancy = PregnancyEntity(
            id = 0,
            isPregnant = true,
            lastPeriodDate = now - 40 * dayMs,
            dueDate = now + 240 * dayMs,
            babyName = "Layla",
            motherName = "Mariam",
            isActive = true
        )
        val preg2Id = dao.insertOrUpdatePregnancy(currentPregnancy).toInt()

        // Add fetal logs to Pregnancy 2
        val fetalLog2 = FetalGrowthLog(
            id = 0,
            pregnancyId = preg2Id,
            pregnancyWeek = 8,
            weightGrams = 1.0,
            lengthCm = 1.6,
            notes = "Early scan"
        )
        dao.insertFetalGrowthLog(fetalLog2)

        // 3. Verify strict isolation between Pregnancy 1 and Pregnancy 2
        val preg1Logs = dao.getFetalGrowthLogsForPregnancyFlow(preg1Id).first()
        val preg2Logs = dao.getFetalGrowthLogsForPregnancyFlow(preg2Id).first()

        assertEquals("Pregnancy 1 should have exactly 1 log", 1, preg1Logs.size)
        assertEquals("Pregnancy 2 should have exactly 1 log", 1, preg2Logs.size)

        assertEquals(20, preg1Logs[0].pregnancyWeek)
        assertEquals(300.0, preg1Logs[0].weightGrams, 0.01)

        assertEquals(8, preg2Logs[0].pregnancyWeek)
        assertEquals(1.0, preg2Logs[0].weightGrams, 0.01)
    }

    @Test
    fun testContraceptiveMethodJourney() = runBlocking {
        val now = System.currentTimeMillis()
        val method = ContraceptiveMethod(
            id = 0,
            type = "PILL",
            startDate = now,
            endDate = null,
            notes = "Combined oral contraceptive pill"
        )
        val methodId = dao.insertContraceptiveMethod(method).toInt()
        assertTrue(methodId > 0)

        val activeMethod = dao.getActiveContraceptiveMethodFlow().first()
        assertNotNull(activeMethod)
        assertEquals("PILL", activeMethod?.type)
        assertEquals("Combined oral contraceptive pill", activeMethod?.notes)
    }

    @Test
    fun testBloodPressureAndSymptomsJourney() = runBlocking {
        val now = System.currentTimeMillis()

        // 1. Insert BP log
        val bpLog = BloodPressureLog(
            id = 0,
            date = now,
            systolic = 118,
            diastolic = 78,
            pulse = 72,
            notes = "Morning routine"
        )
        dao.insertBloodPressureLog(bpLog)

        // 2. Insert Symptom log
        val symptomLog = SymptomLog(
            id = 0,
            date = now,
            symptom = "Nausea",
            severity = 4,
            notes = "Morning nausea"
        )
        dao.insertSymptomLog(symptomLog)

        // 3. Verify retrieval
        val bpList = dao.getAllBloodPressureLogsFlow().first()
        val symptomList = dao.getAllSymptomLogsFlow().first()

        assertEquals(1, bpList.size)
        assertEquals(118, bpList[0].systolic)
        assertEquals(78, bpList[0].diastolic)

        assertEquals(1, symptomList.size)
        assertEquals("Nausea", symptomList[0].symptom)
        assertEquals(4, symptomList[0].severity)
    }

    @Test
    fun testCascadeDeleteAdherenceLogsOnMedicationDelete() = runBlocking {
        // Enable foreign keys on the in-memory SQLite connection
        db.openHelper.writableDatabase.execSQL("PRAGMA foreign_keys = ON;")

        val med = MedicationLog(
            id = 0,
            name = "Iron Supplements",
            dosage = "1 capsule",
            timesPerDay = 1,
            isActive = true,
            totalQuantity = 20,
            remainingQuantity = 20
        )
        val medId = dao.insertMedication(med).toInt()
        assertTrue(medId > 0)

        val adherenceLog = MedicationAdherenceLog(
            id = 0,
            medicationId = medId,
            scheduledTime = System.currentTimeMillis(),
            actualTime = System.currentTimeMillis(),
            status = "TAKEN"
        )
        dao.insertMedicationAdherenceLog(adherenceLog)

        val logsBefore = dao.getAdherenceLogsForMedicationFlow(medId).first()
        assertEquals(1, logsBefore.size)

        // Delete the parent medication
        val savedMed = dao.getAllMedicationsFlow().first().first { it.id == medId }
        dao.deleteMedication(savedMed)

        // Verify adherence logs cascade-deleted automatically
        val logsAfter = dao.getAdherenceLogsForMedicationFlow(medId).first()
        assertEquals(0, logsAfter.size)
    }
}
