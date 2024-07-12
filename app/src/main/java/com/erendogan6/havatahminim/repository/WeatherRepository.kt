package com.erendogan6.havatahminim.repository

import com.erendogan6.havatahminim.model.CurrentWeatherBaseResponse
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
        return weatherApiService.getWeather(lat, lon, apiKey)
    }

    suspend fun getHourlyWeather(lat: Double, lon: Double, apiKey: String): HourlyForecastBaseResponse {
        return proWeatherApiService.getHourlyWeather(lat, lon, apiKey)
    }

    suspend fun getWeatherSuggestions(location: String, temperature: String): String {
        val chatHistory = listOf(
            content("user") {
                text("Konum: $location\nSıcaklık: $temperature")
            }
        )
        val chat = geminiService.model.startChat(chatHistory)
        return withContext(Dispatchers.IO) {
            val response = chat.sendMessage("INSERT_INPUT_HERE")
            response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.asTextOrNull() ?: "No suggestions available"
        }
    }
}
