package com.erendogan6.havatahminim.repository

import com.erendogan6.havatahminim.model.CurrentWeatherBaseResponse
import com.erendogan6.havatahminim.model.DailyForecastBaseResponse
import com.erendogan6.havatahminim.model.HourlyForecastBaseResponse
import com.erendogan6.havatahminim.network.GeminiService
import com.erendogan6.havatahminim.network.ProWeatherApiService
import com.erendogan6.havatahminim.network.WeatherApiService
import com.google.ai.client.generativeai.type.asTextOrNull
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class WeatherRepository @Inject constructor(
    private val weatherApiService: WeatherApiService,
    private val proWeatherApiService: ProWeatherApiService,
    private val geminiService: GeminiService
) {
    suspend fun getWeather(lat: Double, lon: Double, apiKey: String): CurrentWeatherBaseResponse {
        return withContext(Dispatchers.IO) {
            try {
                weatherApiService.getWeather(lat, lon, apiKey)
            } catch (e: Exception) {
                throw RuntimeException("Failed to fetch weather data", e)
            }
        }
    }

    suspend fun getHourlyWeather(lat: Double, lon: Double, apiKey: String): HourlyForecastBaseResponse {
        return withContext(Dispatchers.IO) {
            try {
                proWeatherApiService.getHourlyWeather(lat, lon, apiKey)
            } catch (e: Exception) {
                throw RuntimeException("Failed to fetch hourly weather data", e)
            }
        }
    }

    suspend fun getDailyWeather(lat: Double, lon: Double, apiKey: String): DailyForecastBaseResponse {
        return withContext(Dispatchers.IO) {
            try {
                weatherApiService.getDailyWeather(lat, lon, apiKey)
            } catch (e: Exception) {
                throw RuntimeException("Failed to fetch daily weather data", e)
            }
        }
    }

    suspend fun getWeatherSuggestions(location: String, temperature: String): String {
        val chatHistory = listOf(
            content("user") {
                text("Konum: $location\nSıcaklık: $temperature")
            }
        )
        return withContext(Dispatchers.IO) {
            try {
                val chat = geminiService.model.startChat(chatHistory)
                val response = chat.sendMessage("INSERT_INPUT_HERE")
                response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.asTextOrNull() ?: "No suggestions available"
            } catch (e: Exception) {
                throw RuntimeException("Failed to fetch weather suggestions", e)
            }
        }
    }
}
