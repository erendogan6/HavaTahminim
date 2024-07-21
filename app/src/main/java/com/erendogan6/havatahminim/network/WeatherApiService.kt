package com.erendogan6.havatahminim.network

import com.erendogan6.havatahminim.BuildConfig.WEATHER_API_KEY
import com.erendogan6.havatahminim.model.weather.CurrentForecast.CurrentWeatherBaseResponse
import com.erendogan6.havatahminim.model.weather.DailyForecast.DailyForecastBaseResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("weather")
    suspend fun getWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String = WEATHER_API_KEY,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String
    ): CurrentWeatherBaseResponse

    @GET("forecast/daily")
    suspend fun getDailyWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String = WEATHER_API_KEY,
        @Query("cnt") days: Int = 7,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String
    ): DailyForecastBaseResponse
}
