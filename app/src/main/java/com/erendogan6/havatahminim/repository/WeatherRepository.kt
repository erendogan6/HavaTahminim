package com.erendogan6.havatahminim.repository

import android.content.Context
import android.icu.util.Calendar
import android.location.Geocoder
import android.location.Location
import com.erendogan6.havatahminim.R
import com.erendogan6.havatahminim.model.DailyForecastDao
import com.erendogan6.havatahminim.model.LocationDao
import com.erendogan6.havatahminim.model.entity.DailyForecastEntity
import com.erendogan6.havatahminim.model.entity.LocationEntity
import com.erendogan6.havatahminim.model.entity.WeatherSuggestionEntity
import com.erendogan6.havatahminim.model.weather.Common.Weather
import com.erendogan6.havatahminim.model.weather.CurrentForecast.CurrentWeatherBaseResponse
import com.erendogan6.havatahminim.model.weather.CurrentForecast.Main
import com.erendogan6.havatahminim.model.weather.CurrentForecast.Sys
import com.erendogan6.havatahminim.model.weather.DailyForecast.City
import com.erendogan6.havatahminim.model.weather.DailyForecast.DailyForecast
import com.erendogan6.havatahminim.model.weather.DailyForecast.DailyForecastBaseResponse
import com.erendogan6.havatahminim.model.weather.DailyForecast.Temperature
import com.erendogan6.havatahminim.model.weather.HourlyForecast.HourlyForecastBaseResponse
import com.erendogan6.havatahminim.model.weather.openmeteo.OpenMeteoResponse
import com.erendogan6.havatahminim.network.CityApiService
import com.erendogan6.havatahminim.network.GeminiService
import com.erendogan6.havatahminim.network.WeatherApiService
import com.erendogan6.havatahminim.room.WeatherSuggestionDao
import com.erendogan6.havatahminim.util.ResourcesProvider
import com.erendogan6.havatahminim.util.WmoWeather
import com.google.ai.client.generativeai.type.SerializationException
import com.google.ai.client.generativeai.type.asTextOrNull
import com.google.ai.client.generativeai.type.content
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

class WeatherRepository
    @Inject
    constructor(
        private val weatherApiService: WeatherApiService,
        private val geminiService: GeminiService,
        private val cityApiService: CityApiService,
        private val locationDao: LocationDao,
        private val dailyForecastDao: DailyForecastDao,
        private val resourcesProvider: ResourcesProvider,
        private val weatherSuggestionDao: WeatherSuggestionDao,
        @ApplicationContext private val context: Context,
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
                    val response = weatherApiService.getCurrentWeather(lat, lon)
                    mapCurrentWeather(response, lat, lon)
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
                    val response = weatherApiService.getHourlyWeather(lat, lon)
                    mapHourlyWeather(response)
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
                    val response = weatherApiService.getDailyWeather(lat, lon)
                    val forecast = mapDailyWeather(response)
                    val forecastEntity =
                        DailyForecastEntity(
                            date = today,
                            latitude = lat,
                            longitude = lon,
                            forecastData = forecast,
                        )
                    dailyForecastDao.insertForecast(forecastEntity)
                    forecast
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
                    cityApiService.getCities(query, language = language).results ?: emptyList()
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

        // region Open-Meteo -> domain model mapping

        private fun mapCurrentWeather(
            response: OpenMeteoResponse,
            lat: Double,
            lon: Double,
        ): CurrentWeatherBaseResponse {
            val current = response.current ?: throw RuntimeException("Missing current weather data")
            val sys =
                Sys(
                    sunrise = response.daily?.sunrise?.firstOrNull() ?: 0L,
                    sunset = response.daily?.sunset?.firstOrNull() ?: 0L,
                )
            return CurrentWeatherBaseResponse(
                weather = listOf(weatherFor(current.weatherCode)),
                main =
                    Main(
                        temp = current.temperature,
                        feels_like = current.apparentTemperature,
                        temp_min = current.temperature,
                        temp_max = current.temperature,
                        humidity = current.humidity,
                    ),
                dt = current.time,
                sys = sys,
                name = resolveLocationName(lat, lon),
            )
        }

        private fun mapHourlyWeather(response: OpenMeteoResponse): HourlyForecastBaseResponse {
            val hourly = response.hourly ?: return HourlyForecastBaseResponse(emptyList())
            val sys =
                Sys(
                    sunrise = response.daily?.sunrise?.firstOrNull() ?: 0L,
                    sunset = response.daily?.sunset?.firstOrNull() ?: 0L,
                )
            val nowSeconds = System.currentTimeMillis() / 1000
            // Open-Meteo returns hours from 00:00 of the local day; start from the upcoming hour.
            val startIndex = hourly.time.indexOfFirst { it >= nowSeconds }.takeIf { it >= 0 } ?: 0

            val list =
                hourly.time.indices
                    .drop(startIndex)
                    .map { i ->
                        CurrentWeatherBaseResponse(
                            weather = listOf(weatherFor(hourly.weatherCode.getOrElse(i) { 0 })),
                            main =
                                Main(
                                    temp = hourly.temperature.getOrElse(i) { 0.0 },
                                    feels_like = hourly.temperature.getOrElse(i) { 0.0 },
                                    temp_min = hourly.temperature.getOrElse(i) { 0.0 },
                                    temp_max = hourly.temperature.getOrElse(i) { 0.0 },
                                    humidity = 0,
                                ),
                            dt = hourly.time[i],
                            sys = sys,
                            name = "",
                        )
                    }
            return HourlyForecastBaseResponse(list)
        }

        private fun mapDailyWeather(response: OpenMeteoResponse): DailyForecastBaseResponse {
            val daily = response.daily ?: return DailyForecastBaseResponse(list = emptyList())
            val list =
                daily.time.indices.map { i ->
                    DailyForecast(
                        dt = daily.time[i],
                        sunrise = daily.sunrise.getOrElse(i) { 0L },
                        sunset = daily.sunset.getOrElse(i) { 0L },
                        temp =
                            Temperature(
                                day = daily.temperatureMax.getOrElse(i) { 0.0 },
                                night = daily.temperatureMin.getOrElse(i) { 0.0 },
                            ),
                        humidity = 0,
                        weather = listOf(weatherFor(daily.weatherCode.getOrElse(i) { 0 })),
                    )
                }
            return DailyForecastBaseResponse(list = list)
        }

        private fun weatherFor(code: Int): Weather =
            Weather(
                main = WmoWeather.category(code),
                description = resourcesProvider.getString(WmoWeather.descriptionRes(code)),
            )

        /**
         * Open-Meteo's forecast endpoint does not return a place name, so we reverse-geocode the
         * coordinates with the platform [Geocoder]. Falls back gracefully when geocoding is
         * unavailable or returns nothing.
         */
        @Suppress("DEPRECATION")
        private fun resolveLocationName(
            lat: Double,
            lon: Double,
        ): String =
            try {
                val geocoder = Geocoder(context, Locale(language))
                val address = geocoder.getFromLocation(lat, lon, 1)?.firstOrNull()
                address?.locality
                    ?: address?.subAdminArea
                    ?: address?.adminArea
                    ?: address?.countryName
                    ?: ""
            } catch (e: Exception) {
                ""
            }

        // endregion
    }
