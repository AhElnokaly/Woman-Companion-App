package com.example

import com.example.data.*
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BackupAndCalculationIntegrationTest {

    @Test
    fun testPregnancyCalculations_StandardCycle() {
        val now = System.currentTimeMillis()
        val dayMillis = 86400000L
        
        // Assume LMP was exactly 70 days ago (10 weeks)
        val lmp = now - (70 * dayMillis)
        val cycleLength = 28

        // Standard Naegele's rule calculation: Due date = LMP + 280 days + (cycleLength - 28)
        val expectedDueDate = lmp + (280L * dayMillis) + ((cycleLength - 28) * dayMillis)
        
        val daysElapsed = ((now - lmp) / dayMillis).toInt()
        val currentWeek = (daysElapsed / 7) + 1
        val currentDayOfCurrentWeek = daysElapsed % 7
        val daysRemaining = ((expectedDueDate - now) / dayMillis).toInt()

        assertEquals(11, currentWeek)
        assertEquals(0, currentDayOfCurrentWeek)
        assertEquals(210, daysRemaining)
        assertTrue(expectedDueDate > now)
    }

    @Test
    fun testPregnancyCalculations_LongCycle() {
        val now = System.currentTimeMillis()
        val dayMillis = 86400000L
        val lmp = now - (35 * dayMillis) // 5 weeks ago
        val cycleLength = 32 // 4 days longer than standard

        // Due date adjustment for non-standard cycle
        val expectedDueDate = lmp + (280L * dayMillis) + ((cycleLength - 28) * dayMillis)
        val daysElapsed = ((now - lmp) / dayMillis).toInt()
        val currentWeek = (daysElapsed / 7) + 1

        assertEquals(6, currentWeek)
        assertEquals(280L * dayMillis + 4 * dayMillis, expectedDueDate - lmp)
    }

    @Test
    fun testJsonBackupExportAndImportStructure() {
        val root = JSONObject()
        root.put("version", 2)
        root.put("timestamp", System.currentTimeMillis())

        val pregObj = JSONObject().apply {
            put("lastPeriodDate", 1750000000000L)
            put("dueDate", 1774192000000L)
            put("babyName", "Lina")
            put("motherName", "Sarah")
            put("isPregnant", true)
        }
        root.put("pregnancy", pregObj)

        val bpArray = JSONArray()
        val bp1 = JSONObject().apply {
            put("systolic", 118)
            put("diastolic", 78)
            put("pulse", 72)
            put("date", 1750500000000L)
            put("notes", "Morning measurement")
        }
        bpArray.put(bp1)
        root.put("bloodPressureLogs", bpArray)

        val jsonString = root.toString(2)
        assertNotNull(jsonString)
        assertTrue(jsonString.contains("Lina"))
        assertTrue(jsonString.contains("bloodPressureLogs"))

        // Validate parsing
        val parsedRoot = JSONObject(jsonString)
        assertEquals(2, parsedRoot.getInt("version"))
        val parsedPreg = parsedRoot.getJSONObject("pregnancy")
        assertEquals("Lina", parsedPreg.getString("babyName"))
        assertEquals(1750000000000L, parsedPreg.getLong("lastPeriodDate"))

        val parsedBpArray = parsedRoot.getJSONArray("bloodPressureLogs")
        assertEquals(1, parsedBpArray.length())
        val parsedBp = parsedBpArray.getJSONObject(0)
        assertEquals(118, parsedBp.getInt("systolic"))
        assertEquals(78, parsedBp.getInt("diastolic"))
    }

    @Test
    fun testGitHubSyncUrlValidation() {
        // Allowed HTTPS URLs
        assertTrue(GitHubSyncRepository.isValidSyncUrl("https://raw.githubusercontent.com/user/repo/main/matrix.json"))
        assertTrue(GitHubSyncRepository.isValidSyncUrl("https://github.com/user/repo/raw/main/matrix.json"))
        assertTrue(GitHubSyncRepository.isValidSyncUrl("https://gist.githubusercontent.com/user/123/raw/matrix.json"))

        // Disallowed / Insecure URLs
        assertFalse(GitHubSyncRepository.isValidSyncUrl("http://raw.githubusercontent.com/user/repo/main/matrix.json"))
        assertFalse(GitHubSyncRepository.isValidSyncUrl("https://evil-phishing.com/matrix.json"))
        assertFalse(GitHubSyncRepository.isValidSyncUrl("ftp://raw.githubusercontent.com/file"))
        assertFalse(GitHubSyncRepository.isValidSyncUrl("javascript:alert(1)"))
        assertFalse(GitHubSyncRepository.isValidSyncUrl(""))
    }

    @Test
    fun testApiKeyModelSanitization() {
        assertEquals("gemini-3.5-flash", ApiKeyRepository.sanitizeModelName("gemini-3.5-flash"))
        assertEquals("gemini-1.5-pro", ApiKeyRepository.sanitizeModelName("gemini-1.5-pro"))
        assertEquals("gemini-3.5-flash", ApiKeyRepository.sanitizeModelName(null))
        assertEquals("gemini-3.5-flash", ApiKeyRepository.sanitizeModelName(""))
        assertEquals("gemini-3.5-flash", ApiKeyRepository.sanitizeModelName("invalid model with spaces!@#"))
    }
}
