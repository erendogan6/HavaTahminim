package com.erendogan6.havatahminim.repository

import android.icu.util.Calendar
import android.location.Location
import com.erendogan6.havatahminim.model.DailyForecastDao
import com.erendogan6.havatahminim.model.entity.DailyForecastEntity
import com.erendogan6.havatahminim.model.weather.Common.Weather
import com.erendogan6.havatahminim.model.weather.CurrentForecast.CurrentWeatherBaseResponse
import com.erendogan6.havatahminim.model.weather.CurrentForecast.Main
import com.erendogan6.havatahminim.model.weather.CurrentForecast.Sys
import com.erendogan6.havatahminim.model.weather.DailyForecast.DailyForecast
import com.erendogan6.havatahminim.model.weather.DailyForecast.DailyForecastBaseResponse
import com.erendogan6.havatahminim.model.weather.DailyForecast.Temperature
import com.erendogan6.havatahminim.model.weather.HourlyForecast.HourlyForecastBaseResponse
import com.erendogan6.havatahminim.model.weather.openmeteo.OpenMeteoResponse
import com.erendogan6.havatahminim.network.ApiResult
import com.erendogan6.havatahminim.network.WeatherApiService
import com.erendogan6.havatahminim.network.onSuccess
import com.erendogan6.havatahminim.network.safeApiCall
import com.erendogan6.havatahminim.util.ResourcesProvider
import com.erendogan6.havatahminim.util.WmoWeather
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Weather domain: current/hourly/daily forecasts (Open-Meteo → domain mapping, per-day Room cache
 * for the daily forecast) plus the shared current-weather session state. @Singleton is
 * load-bearing: [currentWeather] is only meaningful if every consumer sees the same instance.
 */
@Singleton
class WeatherRepository
    @Inject
    constructor(
        private val weatherApiService: WeatherApiService,
        private val dailyForecastDao: DailyForecastDao,
        private val locationRepository: LocationRepository,
        private val resourcesProvider: ResourcesProvider,
    ) {
        private val _currentWeather = MutableStateFlow<CurrentWeatherBaseResponse?>(null)

        /** Last fetched current weather; shared by the Today screen, the app background, and the ZekAI prompt. */
        val currentWeather: StateFlow<CurrentWeatherBaseResponse?> = _currentWeather.asStateFlow()

        /** Fetches the current conditions and publishes the result into [currentWeather]. */
        suspend fun refreshCurrentWeather(
            lat: Double,
            lon: Double,
        ): ApiResult<CurrentWeatherBaseResponse> =
            safeApiCall {
                mapCurrentWeather(weatherApiService.getCurrentWeather(lat, lon), lat, lon)
            }.onSuccess { _currentWeather.value = it }

        suspend fun getHourlyWeather(
            lat: Double,
            lon: Double,
        ): ApiResult<HourlyForecastBaseResponse> =
            safeApiCall {
                mapHourlyWeather(weatherApiService.getHourlyWeather(lat, lon))
            }

        /** Daily forecast, served from the per-day Room cache when the location is within 10km. */
        suspend fun getDailyWeather(
            lat: Double,
            lon: Double,
        ): ApiResult<DailyForecastBaseResponse> =
            safeApiCall {
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
                    if (savedLocation.distanceTo(currentLocation) <= DISTANCE_THRESHOLD_METERS) {
                        return@safeApiCall savedForecast.forecastData
                    }
                }

                val forecast = mapDailyWeather(weatherApiService.getDailyWeather(lat, lon))
                dailyForecastDao.insertForecast(
                    DailyForecastEntity(
                        date = today,
                        latitude = lat,
                        longitude = lon,
                        forecastData = forecast,
                    ),
                )
                forecast
            }

        // region Open-Meteo -> domain model mapping

        private fun mapCurrentWeather(
            response: OpenMeteoResponse,
            lat: Double,
            lon: Double,
        ): CurrentWeatherBaseResponse {
            val current = response.current ?: error("Missing current weather data")
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
                name = locationRepository.resolveLocationName(lat, lon),
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

        // endregion

        private companion object {
            const val DISTANCE_THRESHOLD_METERS = 10000 // 10 km — daily forecast cache reuse radius
        }
    }
