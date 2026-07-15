package com.erendogan6.havatahminim.repository

import com.erendogan6.havatahminim.core.common.di.IoDispatcher
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
import com.erendogan6.havatahminim.util.distanceMeters
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Open-Meteo to domain mapping plus the per-day Room cache for the daily forecast. Singleton so
 * every consumer shares the same [currentWeather].
 */
@Singleton
class WeatherRepositoryImpl
    @Inject
    constructor(
        private val weatherApiService: WeatherApiService,
        private val dailyForecastDao: DailyForecastDao,
        private val locationRepository: LocationRepository,
        private val resourcesProvider: ResourcesProvider,
        private val clock: Clock,
        @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : WeatherRepository {
        private val _currentWeather = MutableStateFlow<CurrentWeatherBaseResponse?>(null)

        override val currentWeather: StateFlow<CurrentWeatherBaseResponse?> = _currentWeather.asStateFlow()

        override suspend fun refreshCurrentWeather(
            lat: Double,
            lon: Double,
        ): ApiResult<CurrentWeatherBaseResponse> =
            safeApiCall(ioDispatcher) {
                mapCurrentWeather(weatherApiService.getCurrentWeather(lat, lon), lat, lon)
            }.onSuccess { _currentWeather.value = it }

        override suspend fun getHourlyWeather(
            lat: Double,
            lon: Double,
        ): ApiResult<HourlyForecastBaseResponse> =
            safeApiCall(ioDispatcher) {
                mapHourlyWeather(weatherApiService.getHourlyWeather(lat, lon))
            }

        override suspend fun getDailyWeather(
            lat: Double,
            lon: Double,
        ): ApiResult<DailyForecastBaseResponse> =
            safeApiCall(ioDispatcher) {
                val today =
                    LocalDate
                        .now(clock)
                        .atStartOfDay(clock.zone)
                        .toInstant()
                        .toEpochMilli()

                val savedForecast = dailyForecastDao.getForecastByDate(today)
                if (savedForecast != null &&
                    distanceMeters(savedForecast.latitude, savedForecast.longitude, lat, lon) <=
                        DISTANCE_THRESHOLD_METERS
                ) {
                    return@safeApiCall savedForecast.forecastData
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
                        feelsLike = current.apparentTemperature,
                        tempMin = current.temperature,
                        tempMax = current.temperature,
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
            val nowSeconds = clock.millis() / MILLIS_PER_SECOND
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
                                    feelsLike = hourly.temperature.getOrElse(i) { 0.0 },
                                    tempMin = hourly.temperature.getOrElse(i) { 0.0 },
                                    tempMax = hourly.temperature.getOrElse(i) { 0.0 },
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
            const val MILLIS_PER_SECOND = 1000L
            const val DISTANCE_THRESHOLD_METERS = 10000.0 // daily forecast cache reuse radius
        }
    }
