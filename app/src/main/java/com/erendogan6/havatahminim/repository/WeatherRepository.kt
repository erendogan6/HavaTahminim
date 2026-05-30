package com.erendogan6.havatahminim.repository

import android.content.Context
import android.icu.util.Calendar
import android.location.Geocoder
import android.location.Location
import com.erendogan6.havatahminim.R
import com.erendogan6.havatahminim.model.DailyForecastDao
import com.erendogan6.havatahminim.model.LocationDao
import com.erendogan6.havatahminim.model.airquality.AirQualityInfo
import com.erendogan6.havatahminim.model.airquality.DailyPollenForecast
import com.erendogan6.havatahminim.model.airquality.PollenReading
import com.erendogan6.havatahminim.model.airquality.PollenSeries
import com.erendogan6.havatahminim.model.airquality.PollenType
import com.erendogan6.havatahminim.model.entity.AllergenPreferenceEntity
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
import com.erendogan6.havatahminim.model.weather.openmeteo.AirQualityResponse
import com.erendogan6.havatahminim.model.weather.openmeteo.OpenMeteoResponse
import com.erendogan6.havatahminim.network.AirQualityApiService
import com.erendogan6.havatahminim.network.CityApiService
import com.erendogan6.havatahminim.network.GeminiService
import com.erendogan6.havatahminim.network.WeatherApiService
import com.erendogan6.havatahminim.room.AllergenPreferenceDao
import com.erendogan6.havatahminim.room.WeatherSuggestionDao
import com.erendogan6.havatahminim.util.PollenLevel
import com.erendogan6.havatahminim.util.ResourcesProvider
import com.erendogan6.havatahminim.util.WmoWeather
import kotlinx.coroutines.flow.Flow
import com.google.ai.client.generativeai.type.SerializationException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

class WeatherRepository
    @Inject
    constructor(
        private val weatherApiService: WeatherApiService,
        private val airQualityApiService: AirQualityApiService,
        private val geminiService: GeminiService,
        private val cityApiService: CityApiService,
        private val locationDao: LocationDao,
        private val dailyForecastDao: DailyForecastDao,
        private val resourcesProvider: ResourcesProvider,
        private val weatherSuggestionDao: WeatherSuggestionDao,
        private val allergenPreferenceDao: AllergenPreferenceDao,
        @param:ApplicationContext private val context: Context,
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
            pollenSummary: String = "",
            forceRefresh: Boolean = false,
        ): String {
            val cachedSuggestion = weatherSuggestionDao.getLatestSuggestion()

            val needsNewSuggestion =
                forceRefresh ||
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

                        // The persona/instructions live in the model's systemInstruction
                        // (see GeminiService); here we only send the user-specific data.
                        val userMessage =
                            buildString {
                                append("Konum: $location\nSıcaklık: $temperature")
                                if (pollenSummary.isNotBlank()) {
                                    append("\nSeçili alerjenlerin polen durumu: $pollenSummary")
                                }
                            }
                        val response = geminiService.model.generateContent(userMessage)
                        val suggestion =
                            response.text ?: resourcesProvider.getString(R.string.general_error_message)

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
                return cachedSuggestion.suggestion
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

        suspend fun getAirQuality(
            lat: Double,
            lon: Double,
        ): AirQualityInfo =
            withContext(Dispatchers.IO) {
                try {
                    mapAirQuality(airQualityApiService.getAirQuality(lat, lon))
                } catch (e: Exception) {
                    throw RuntimeException(resourcesProvider.getString(R.string.error_fetching_air_quality), e)
                }
            }

        fun allergenPreferences(): Flow<List<AllergenPreferenceEntity>> = allergenPreferenceDao.getAll()

        suspend fun setAllergenPreference(
            type: PollenType,
            sensitive: Boolean,
        ) {
            withContext(Dispatchers.IO) {
                allergenPreferenceDao.setPreference(AllergenPreferenceEntity(type.name, sensitive))
            }
        }

        /** Allergens the user explicitly marked sensitive; empty means "treat all as relevant". */
        suspend fun sensitiveAllergens(): Set<PollenType> =
            withContext(Dispatchers.IO) {
                allergenPreferenceDao
                    .getSensitive()
                    .mapNotNull { runCatching { PollenType.valueOf(it.type) }.getOrNull() }
                    .toSet()
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
                            pop = hourly.precipitationProbability.getOrNull(i),
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

        private fun mapAirQuality(response: AirQualityResponse): AirQualityInfo {
            val current = response.current
            val rawByType =
                mapOf(
                    PollenType.ALDER to current?.alderPollen,
                    PollenType.BIRCH to current?.birchPollen,
                    PollenType.GRASS to current?.grassPollen,
                    PollenType.MUGWORT to current?.mugwortPollen,
                    PollenType.OLIVE to current?.olivePollen,
                    PollenType.RAGWEED to current?.ragweedPollen,
                )
            val pollen =
                rawByType.map { (type, grains) ->
                    PollenReading(type = type, valueGrains = grains, risk = PollenLevel.risk(type, grains))
                }
            val hourly = response.hourly
            val hourlyByType =
                mapOf(
                    PollenType.ALDER to hourly?.alderPollen.orEmpty(),
                    PollenType.BIRCH to hourly?.birchPollen.orEmpty(),
                    PollenType.GRASS to hourly?.grassPollen.orEmpty(),
                    PollenType.MUGWORT to hourly?.mugwortPollen.orEmpty(),
                    PollenType.OLIVE to hourly?.olivePollen.orEmpty(),
                    PollenType.RAGWEED to hourly?.ragweedPollen.orEmpty(),
                )
            return AirQualityInfo(
                pollen = pollen,
                dailyForecast = buildDailyPollen(response.hourly),
                hourlyTimes = hourly?.time.orEmpty(),
                hourlyByType = hourlyByType,
                pm25 = current?.pm25,
                pm10 = current?.pm10,
                ozone = current?.ozone,
                europeanAqi = current?.europeanAqi,
                pollenAvailable = pollen.any { it.valueGrains != null },
            )
        }

        /**
         * Open-Meteo only forecasts pollen hourly, so we aggregate the hourly series into a per-day
         * outlook by taking each day's **peak** concentration (worst case) per pollen type. Hours
         * are bucketed into local calendar days.
         */
        private fun buildDailyPollen(hourly: com.erendogan6.havatahminim.model.weather.openmeteo.AirQualityHourly?): List<DailyPollenForecast> {
            if (hourly == null || hourly.time.isEmpty()) return emptyList()
            val zone = java.time.ZoneId.systemDefault()
            val seriesByType =
                mapOf(
                    PollenType.ALDER to hourly.alderPollen,
                    PollenType.BIRCH to hourly.birchPollen,
                    PollenType.GRASS to hourly.grassPollen,
                    PollenType.MUGWORT to hourly.mugwortPollen,
                    PollenType.OLIVE to hourly.olivePollen,
                    PollenType.RAGWEED to hourly.ragweedPollen,
                )
            val indicesByDay = LinkedHashMap<java.time.LocalDate, MutableList<Int>>()
            hourly.time.forEachIndexed { i, t ->
                val day = java.time.Instant.ofEpochSecond(t).atZone(zone).toLocalDate()
                indicesByDay.getOrPut(day) { mutableListOf() }.add(i)
            }
            return indicesByDay.values.map { indices ->
                val readings =
                    seriesByType.map { (type, series) ->
                        val peak = indices.mapNotNull { idx -> series?.getOrNull(idx) }.maxOrNull()
                        PollenReading(type, peak, PollenLevel.risk(type, peak))
                    }
                val hourlySeries =
                    seriesByType.map { (type, series) ->
                        PollenSeries(type, indices.map { idx -> series?.getOrNull(idx) })
                    }
                DailyPollenForecast(
                    date = hourly.time[indices.first()],
                    readings = readings,
                    hours = indices.map { hourly.time[it] },
                    hourly = hourlySeries,
                )
            }
        }

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
