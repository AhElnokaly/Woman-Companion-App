package com.example

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.data.AppDatabase
import com.example.data.MIGRATION_14_15
import com.example.data.MIGRATION_15_16
import com.example.data.MIGRATION_16_17
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DatabaseMigrationTest {

    private val TEST_DB_14_15 = "migration-test-14-15"
    private val TEST_DB_15_16 = "migration-test-15-16"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Test
    fun testMigration14To15() {
        // Create database version 14 and insert a sample PeriodLog row via raw SQL
        var db = helper.createDatabase(TEST_DB_14_15, 14).apply {
            execSQL(
                "INSERT INTO period_logs (id, startDate, endDate, flowIntensity, symptoms, painLevel, notes) " +
                "VALUES (1, 1754000000000, 1754400000000, 'medium', 'Cramps', 3, 'Test period log')"
            )
            close()
        }

        // Run migration 14 to 15
        db = helper.runMigrationsAndValidate(TEST_DB_14_15, 15, true, MIGRATION_14_15)

        // Verify period_logs data survived migration
        val cursor = db.query("SELECT * FROM period_logs WHERE id = 1")
        assertTrue(cursor.moveToFirst())
        assertEquals(1754000000000L, cursor.getLong(cursor.getColumnIndexOrThrow("startDate")))
        assertEquals("Cramps", cursor.getString(cursor.getColumnIndexOrThrow("symptoms")))
        cursor.close()

        // Verify new table medication_adherence_logs exists and has correct schema
        val adherenceCursor = db.query("SELECT * FROM medication_adherence_logs")
        assertEquals(0, adherenceCursor.count)
        adherenceCursor.close()

        db.close()
    }

    @Test
    fun testMigration15To16() {
        var db = helper.createDatabase(TEST_DB_15_16, 15).apply {
            execSQL(
                "INSERT INTO pregnancy (id, lastPeriodDate, dueDate, motherName, isPregnant, hasHighBp, hasLowBp, hasDiabetes, isOnboardingCompleted, isDelivered) " +
                "VALUES (1, 1750000000000, 1774000000000, 'Fatima', 1, 0, 0, 0, 1, 0)"
            )
            execSQL(
                "INSERT INTO fetal_growth_logs (id, date, pregnancyWeek, weightGrams, lengthCm) " +
                "VALUES (1, 1751000000000, 20, 320.0, 25.0)"
            )
            close()
        }

        db = helper.runMigrationsAndValidate(TEST_DB_15_16, 16, true, MIGRATION_15_16)

        // Verify pregnancy data survived and gained isActive = 1
        val pregCursor = db.query("SELECT * FROM pregnancy WHERE id = 1")
        assertTrue(pregCursor.moveToFirst())
        assertEquals("Fatima", pregCursor.getString(pregCursor.getColumnIndexOrThrow("motherName")))
        assertEquals(1, pregCursor.getInt(pregCursor.getColumnIndexOrThrow("isActive")))
        pregCursor.close()

        // Verify fetal_growth_logs survived and gained pregnancyId = 1
        val fetalCursor = db.query("SELECT * FROM fetal_growth_logs WHERE id = 1")
        assertTrue(fetalCursor.moveToFirst())
        assertEquals(1, fetalCursor.getInt(fetalCursor.getColumnIndexOrThrow("pregnancyId")))
        assertEquals(20, fetalCursor.getInt(fetalCursor.getColumnIndexOrThrow("pregnancyWeek")))
        assertEquals(320.0, fetalCursor.getDouble(fetalCursor.getColumnIndexOrThrow("weightGrams")), 0.001)
        fetalCursor.close()

        db.close()
    }

    @Test
    fun testMigration15To16_withNonOnePregnancyId() {
        val testDbName = "migration-test-15-16-non-one"
        var db = helper.createDatabase(testDbName, 15).apply {
            execSQL(
                "INSERT INTO pregnancy (id, lastPeriodDate, dueDate, motherName, isPregnant, hasHighBp, hasLowBp, hasDiabetes, isOnboardingCompleted, isDelivered) " +
                "VALUES (42, 1750000000000, 1774000000000, 'Mariam', 1, 0, 0, 0, 1, 0)"
            )
            execSQL(
                "INSERT INTO fetal_growth_logs (id, date, pregnancyWeek, weightGrams, lengthCm) " +
                "VALUES (10, 1751000000000, 24, 600.0, 30.0)"
            )
            close()
        }

        db = helper.runMigrationsAndValidate(testDbName, 16, true, MIGRATION_15_16)

        // Verify pregnancy data survived with id = 42 and isActive = 1
        val pregCursor = db.query("SELECT * FROM pregnancy WHERE id = 42")
        assertTrue(pregCursor.moveToFirst())
        assertEquals("Mariam", pregCursor.getString(pregCursor.getColumnIndexOrThrow("motherName")))
        assertEquals(1, pregCursor.getInt(pregCursor.getColumnIndexOrThrow("isActive")))
        pregCursor.close()

        // Verify fetal_growth_logs survived and correctly linked to pregnancyId = 42
        val fetalCursor = db.query("SELECT * FROM fetal_growth_logs WHERE id = 10")
        assertTrue(fetalCursor.moveToFirst())
        assertEquals(42, fetalCursor.getInt(fetalCursor.getColumnIndexOrThrow("pregnancyId")))
        assertEquals(24, fetalCursor.getInt(fetalCursor.getColumnIndexOrThrow("pregnancyWeek")))
        assertEquals(600.0, fetalCursor.getDouble(fetalCursor.getColumnIndexOrThrow("weightGrams")), 0.001)
        fetalCursor.close()

        db.close()
    }

    @Test
    fun testMigration16To17() {
        val testDbName = "migration-test-16-17"
        var db = helper.createDatabase(testDbName, 16).apply {
            execSQL(
                "INSERT INTO pregnancy (id, lastPeriodDate, dueDate, motherName, isPregnant, hasHighBp, hasLowBp, hasDiabetes, isOnboardingCompleted, isDelivered, isActive) " +
                "VALUES (1, 1750000000000, 1774000000000, 'Nour', 1, 0, 0, 0, 1, 0, 1)"
            )
            close()
        }

        db = helper.runMigrationsAndValidate(testDbName, 17, true, MIGRATION_16_17)

        // Verify contraceptive_methods table exists and accepts records
        db.execSQL(
            "INSERT INTO contraceptive_methods (id, type, startDate, endDate, notes) " +
            "VALUES (1, 'حبوب منع الحمل المركبة', 1750000000000, NULL, 'حبة يوميا')"
        )
        val cursor = db.query("SELECT * FROM contraceptive_methods WHERE id = 1")
        assertTrue(cursor.moveToFirst())
        assertEquals("حبوب منع الحمل المركبة", cursor.getString(cursor.getColumnIndexOrThrow("type")))
        assertEquals(1750000000000L, cursor.getLong(cursor.getColumnIndexOrThrow("startDate")))
        assertTrue(cursor.isNull(cursor.getColumnIndexOrThrow("endDate")))
        assertEquals("حبة يوميا", cursor.getString(cursor.getColumnIndexOrThrow("notes")))
        cursor.close()

        db.close()
    }
}
