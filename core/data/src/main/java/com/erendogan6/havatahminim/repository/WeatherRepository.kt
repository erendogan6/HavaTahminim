package com.erendogan6.havatahminim.repository

import com.erendogan6.havatahminim.model.weather.CurrentForecast.CurrentWeatherBaseResponse
import com.erendogan6.havatahminim.model.weather.DailyForecast.DailyForecastBaseResponse
import com.erendogan6.havatahminim.model.weather.HourlyForecast.HourlyForecastBaseResponse
import com.erendogan6.havatahminim.network.ApiResult
import kotlinx.coroutines.flow.StateFlow

/**
 * Weather domain: current/hourly/daily forecasts plus the shared current-weather session state.
 */
interface WeatherRepository {
    /** Last fetched current weather; shared by the Today screen, the app background, and the ZekAI prompt. */
    val currentWeather: StateFlow<CurrentWeatherBaseResponse?>

    /** Fetches the current conditions; on success the result is also published into [currentWeather]. */
    suspend fun refreshCurrentWeather(
        lat: Double,
        lon: Double,
    ): ApiResult<CurrentWeatherBaseResponse>

    suspend fun getHourlyWeather(
        lat: Double,
        lon: Double,
    ): ApiResult<HourlyForecastBaseResponse>

    /** Daily forecast, served from the per-day Room cache when the location is within 10km. */
    suspend fun getDailyWeather(
        lat: Double,
        lon: Double,
    ): ApiResult<DailyForecastBaseResponse>
}
