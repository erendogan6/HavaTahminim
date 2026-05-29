package com.erendogan6.havatahminim.network

import com.erendogan6.havatahminim.model.weather.openmeteo.GeoSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface CityApiService {
    @GET("v1/search")
    suspend fun getCities(
        @Query("name") query: String,
        @Query("language") language: String,
        @Query("count") count: Int = 5,
        @Query("format") format: String = "json",
    ): GeoSearchResponse
}
