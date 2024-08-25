package com.erendogan6.havatahminim.repository

import android.icu.util.Calendar
import android.location.Location
import com.erendogan6.havatahminim.R
import com.erendogan6.havatahminim.model.DailyForecastDao
import com.erendogan6.havatahminim.model.LocationDao
import com.erendogan6.havatahminim.model.entity.DailyForecastEntity
import com.erendogan6.havatahminim.model.entity.LocationEntity
import com.erendogan6.havatahminim.model.entity.WeatherSuggestionEntity
import com.erendogan6.havatahminim.model.weather.CurrentForecast.CurrentWeatherBaseResponse
import com.erendogan6.havatahminim.model.weather.DailyForecast.City
import com.erendogan6.havatahminim.model.weather.DailyForecast.DailyForecastBaseResponse
import com.erendogan6.havatahminim.model.weather.HourlyForecast.HourlyForecastBaseResponse
import com.erendogan6.havatahminim.network.CityApiService
import com.erendogan6.havatahminim.network.GeminiService
import com.erendogan6.havatahminim.network.ProWeatherApiService
import com.erendogan6.havatahminim.network.WeatherApiService
import com.erendogan6.havatahminim.room.WeatherSuggestionDao
import com.erendogan6.havatahminim.util.ResourcesProvider
import com.google.ai.client.generativeai.type.SerializationException
import com.google.ai.client.generativeai.type.asTextOrNull
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class WeatherRepository
    @Inject
    constructor(
        private val weatherApiService: WeatherApiService,
        private val proWeatherApiService: ProWeatherApiService,
        private val geminiService: GeminiService,
        private val cityApiService: CityApiService,
        private val locationDao: LocationDao,
        private val dailyForecastDao: DailyForecastDao,
        private val resourcesProvider: ResourcesProvider,
        private val weatherSuggestionDao: WeatherSuggestionDao,
    ) {
        private val DISTANCE_THRESHOLD_METERS = 10000 // 10 km
        private val TIME_THRESHOLD_MILLIS = 24 * 60 * 60 * 1000 // 24 hours

        private val language: String get() = resourcesProvider.getLanguage()

        suspend fun getWeather(
            lat: Double,
            lon: Double,
        ): CurrentWeatherBaseResponse =
            withContext(Dispatchers.IO) {
                try {
                    weatherApiService.getWeather(lat, lon, lang = language)
                } catch (e: Exception) {
                    throw RuntimeException(resourcesProvider.getString(R.string.error_fetching_weather_data), e)
                }
            }

        suspend fun getHourlyWeather(
            lat: Double,
            lon: Double,
        ): HourlyForecastBaseResponse =
            withContext(Dispatchers.IO) {
                try {
                    proWeatherApiService.getHourlyWeather(lat, lon, lang = language)
                } catch (e: Exception) {
                    throw RuntimeException(resourcesProvider.getString(R.string.error_fetching_hourly_forecast), e)
                }
            }

        suspend fun getDailyWeather(
            lat: Double,
            lon: Double,
        ): DailyForecastBaseResponse {
            val today =
                Calendar
                    .getInstance()
                    .apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis

            val savedForecast = dailyForecastDao.getForecastByDate(today)

            if (savedForecast != null) {
                val savedLocation =
                    Location("saved").apply {
                        latitude = savedForecast.latitude
                        longitude = savedForecast.longitude
                    }
                val currentLocation =
                    Location("current").apply {
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
                    val response = weatherApiService.getDailyWeather(lat, lon, lang = language)
                    val forecastEntity =
                        DailyForecastEntity(
                            date = today,
                            latitude = lat,
                            longitude = lon,
                            forecastData = response,
                        )
                    dailyForecastDao.insertForecast(forecastEntity)
                    response
                } catch (e: Exception) {
                    throw RuntimeException(resourcesProvider.getString(R.string.error_fetching_daily_forecast), e)
                }
            }
        }

        suspend fun getWeatherSuggestions(
            lat: Double,
            lon: Double,
            location: String,
            temperature: String,
        ): String {
            val cachedSuggestion = weatherSuggestionDao.getLatestSuggestion()

            val needsNewSuggestion =
                cachedSuggestion?.let {
                    val savedLocation =
                        Location("saved").apply {
                            latitude = it.latitude
                            longitude = it.longitude
                        }
                    val currentLocation =
                        Location("current").apply {
                            latitude = lat
                            longitude = lon
                        }
                    val distance = savedLocation.distanceTo(currentLocation)
                    val timeElapsed = System.currentTimeMillis() - it.timestamp

                    distance > DISTANCE_THRESHOLD_METERS || timeElapsed > TIME_THRESHOLD_MILLIS
                } ?: true

            if (needsNewSuggestion) {
                return withContext(Dispatchers.IO) {
                    try {
                        weatherSuggestionDao.deleteAllSuggestions()

                        val chatHistory =
                            listOf(
                                content("user") {
                                    text("Konum: $location\nSıcaklık: $temperature")
                                },
                            )
                        val chat = geminiService.model.startChat(chatHistory)
                        val response = chat.sendMessage(resourcesProvider.getString(R.string.weather_assistant_instruction))
                        val suggestion =
                            response.candidates
                                .firstOrNull()
                                ?.content
                                ?.parts
                                ?.firstOrNull()
                                ?.asTextOrNull() ?: resourcesProvider.getString(R.string.general_error_message)

                        val suggestionEntity =
                            WeatherSuggestionEntity(
                                location = location,
                                temperature = temperature,
                                suggestion = suggestion,
                                latitude = lat,
                                longitude = lon,
                            )
                        weatherSuggestionDao.insertSuggestion(suggestionEntity)

                        suggestion
                    } catch (e: SerializationException) {
                        resourcesProvider.getString(R.string.serialization_error_message)
                    } catch (e: Exception) {
                        println(e.localizedMessage)
                        resourcesProvider.getString(R.string.error_fetching_weather_suggestions)
                    }
                }
            } else {
                return cachedSuggestion!!.suggestion
            }
        }

        suspend fun getCities(query: String): List<City> =
            withContext(Dispatchers.IO) {
                try {
                    cityApiService.getCities(query)
                } catch (e: Exception) {
                    throw RuntimeException(resourcesProvider.getString(R.string.error_fetching_cities), e)
                }
            }

        suspend fun getSavedLocation(): LocationEntity? =
            withContext(Dispatchers.IO) {
                locationDao.getLocation()
            }

        suspend fun saveLocation(
            latitude: Double,
            longitude: Double,
        ) {
            withContext(Dispatchers.IO) {
                try {
                    locationDao.insertLocation(LocationEntity(latitude = latitude, longitude = longitude))
                } catch (e: Exception) {
                    throw RuntimeException(resourcesProvider.getString(R.string.error_saving_location), e)
                }
            }
        }
    }
