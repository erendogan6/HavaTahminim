package com.erendogan6.havatahminim.network

import com.erendogan6.havatahminim.model.CurrentWeatherBaseResponse
import com.erendogan6.havatahminim.model.DailyForecastBaseResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("weather")
    suspend fun getWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "tr"
    ): CurrentWeatherBaseResponse

    @GET("forecast/daily")
    suspend fun getDailyWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("cnt") days: Int = 7,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "tr"
    ): DailyForecastBaseResponse
}
