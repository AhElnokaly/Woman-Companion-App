package com.example.data

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class WeatherInfo(
    val temperature: Double,
    val humidity: Int,
    val description: String,
    val extraWaterMl: Int // Recommended extra water based on temperature & humidity
)

object WeatherService {
    private val client = OkHttpClient()

    // Query weather for latitude & longitude (default to Cairo, Egypt: lat=30.0444, lon=31.2357)
    suspend fun fetchWeather(lat: Double = 30.0444, lon: Double = 31.2357): WeatherInfo = withContext(Dispatchers.IO) {
        val url = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,relative_humidity_2m"
        val request = Request.Builder().url(url).build()
        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string()
                    if (bodyString != null) {
                        val json = JSONObject(bodyString)
                        val current = json.getJSONObject("current")
                        val temp = current.getDouble("temperature_2m")
                        val humidity = current.getInt("relative_humidity_2m")
                        
                        // Calculate weather condition description
                        val desc = when {
                            temp > 35 -> "حار جداً ☀️"
                            temp > 28 -> "حار نسبياً 🌤️"
                            temp > 18 -> "معتدل ولطيف 🍃"
                            else -> "بارد ❄️"
                        }
                        
                        // Calculate extra recommended water
                        val extraWater = when {
                            temp > 35 -> 750
                            temp > 28 -> 500
                            temp > 22 && humidity < 40 -> 250
                            else -> 0
                        }
                        
                        return@withContext WeatherInfo(
                            temperature = temp,
                            humidity = humidity,
                            description = desc,
                            extraWaterMl = extraWater
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("WeatherService", "Failed to fetch weather", e)
        }
        // Fallback info if network is down or failed
        return@withContext WeatherInfo(
            temperature = 25.0,
            humidity = 50,
            description = "معتدل (افتراضي) 🍃",
            extraWaterMl = 0
        )
    }
}
