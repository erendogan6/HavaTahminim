package com.erendogan6.havatahminim.network

import com.erendogan6.havatahminim.BuildConfig.WEATHER_API_KEY
import com.erendogan6.havatahminim.model.weather.DailyForecast.City
import retrofit2.http.GET
import retrofit2.http.Query

interface CityApiService {
    @GET("geo/1.0/direct")
    suspend fun getCities(
        @Query("q") query: String,
        @Query("appid") apiKey: String = WEATHER_API_KEY,
        @Query("limit") limit: Int = 5
    ): List<City>
}