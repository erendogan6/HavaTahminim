package com.erendogan6.havatahminim.network

import com.erendogan6.havatahminim.model.weather.openmeteo.OpenMeteoResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Open-Meteo forecast API (no key required). All three calls hit the same endpoint with different
 * variable sets; `timeformat=unixtime` gives epoch-second timestamps, `timezone=auto` aligns
 * daily aggregation to the location's local day.
 */
interface WeatherApiService {
    @GET("v1/forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,apparent_temperature,weather_code",
        @Query("daily") daily: String = "sunrise,sunset",
        @Query("timezone") timezone: String = "auto",
        @Query("timeformat") timeformat: String = "unixtime",
        @Query("forecast_days") days: Int = 1,
    ): OpenMeteoResponse

    @GET("v1/forecast")
    suspend fun getHourlyWeather(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("hourly") hourly: String = "temperature_2m,weather_code,precipitation_probability",
        @Query("daily") daily: String = "sunrise,sunset",
        @Query("timezone") timezone: String = "auto",
        @Query("timeformat") timeformat: String = "unixtime",
        @Query("forecast_days") days: Int = 2,
    ): OpenMeteoResponse

    @GET("v1/forecast")
    suspend fun getDailyWeather(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("daily") daily: String =
            "weather_code,temperature_2m_max,temperature_2m_min,apparent_temperature_max,sunrise,sunset",
        @Query("timezone") timezone: String = "auto",
        @Query("timeformat") timeformat: String = "unixtime",
        @Query("forecast_days") days: Int = 7,
    ): OpenMeteoResponse
}
