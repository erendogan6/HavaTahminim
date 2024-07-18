package com.erendogan6.havatahminim.repository

import android.icu.util.Calendar
import android.location.Location
import com.erendogan6.havatahminim.model.City
import com.erendogan6.havatahminim.model.CurrentWeatherBaseResponse
import com.erendogan6.havatahminim.model.DailyForecastBaseResponse
import com.erendogan6.havatahminim.model.DailyForecastDao
import com.erendogan6.havatahminim.model.DailyForecastEntity
import com.erendogan6.havatahminim.model.HourlyForecastBaseResponse
import com.erendogan6.havatahminim.model.LocationDao
import com.erendogan6.havatahminim.model.LocationEntity
import com.erendogan6.havatahminim.network.CityApiService
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
    private val geminiService: GeminiService,
    private val cityApiService: CityApiService,
    private val locationDao: LocationDao,
    private val dailyForecastDao: DailyForecastDao
) {
    private val DISTANCE_THRESHOLD_METERS = 10000  // 10 km

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
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val savedForecast = dailyForecastDao.getForecastByDate(today)

        if (savedForecast != null) {
            val savedLocation = Location("saved").apply {
                latitude = savedForecast.latitude
                longitude = savedForecast.longitude
            }
            val currentLocation = Location("current").apply {
                latitude = lat
                longitude = lon
            }
            val distance = savedLocation.distanceTo(currentLocation)
            if (distance <= DISTANCE_THRESHOLD_METERS) {
                return savedForecast.forecastData
            }
        }

        return withContext(Dispatchers.IO) {
            try {
                val response = weatherApiService.getDailyWeather(lat, lon, apiKey)
                val forecastEntity = DailyForecastEntity(
                    date = today,
                    latitude = lat,
                    longitude = lon,
                    forecastData = response
                )
                dailyForecastDao.insertForecast(forecastEntity)
                response
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
                val response = chat.sendMessage("Sen bir akıllı hava durumu asistanısın. Kullanıcının konum ve mevcut hava durumu bilgilerini alarak, onlara günlük aktiviteler için Türkçe önerilerde bulunuyorsun.")
                response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.asTextOrNull() ?: "No suggestions available"
            } catch (e: Exception) {
                println(e.localizedMessage)
                "Failed to fetch weather suggestions. Error: ${e.message}"
            }
        }
    }

    suspend fun getCities(query: String): List<City> {
        return withContext(Dispatchers.IO) {
            try {
                cityApiService.getCities(query)
            } catch (e: Exception) {
                throw RuntimeException("Failed to fetch cities", e)
            }
        }
    }

    suspend fun getSavedLocation(): LocationEntity? {
        return withContext(Dispatchers.IO) {
            locationDao.getLocation()
        }
    }

    suspend fun saveLocation(latitude: Double, longitude: Double) {
        withContext(Dispatchers.IO) {
            locationDao.insertLocation(LocationEntity(latitude = latitude, longitude = longitude))
        }
    }
}
