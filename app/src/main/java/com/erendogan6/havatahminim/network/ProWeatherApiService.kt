package com.erendogan6.havatahminim.network

import com.erendogan6.havatahminim.BuildConfig.WEATHER_API_KEY
import com.erendogan6.havatahminim.model.weather.HourlyForecast.HourlyForecastBaseResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ProWeatherApiService {
    @GET("forecast/hourly")
    suspend fun getHourlyWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String = WEATHER_API_KEY,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String,
    ): HourlyForecastBaseResponse
}
