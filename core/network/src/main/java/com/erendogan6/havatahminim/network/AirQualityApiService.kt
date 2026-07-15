package com.erendogan6.havatahminim.network

import com.erendogan6.havatahminim.model.weather.openmeteo.AirQualityResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Open-Meteo air quality API: AQI metrics plus pollen (Europe only). Same time conventions as
 * [WeatherApiService].
 */
interface AirQualityApiService {
    @GET("v1/air-quality")
    suspend fun getAirQuality(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current") current: String =
            "alder_pollen,birch_pollen,grass_pollen,mugwort_pollen,olive_pollen,ragweed_pollen," +
                "pm2_5,pm10,ozone,european_aqi",
        @Query("hourly") hourly: String =
            "alder_pollen,birch_pollen,grass_pollen,mugwort_pollen,olive_pollen,ragweed_pollen",
        @Query("timezone") timezone: String = "auto",
        @Query("timeformat") timeformat: String = "unixtime",
        @Query("forecast_days") days: Int = 4,
    ): AirQualityResponse
}
