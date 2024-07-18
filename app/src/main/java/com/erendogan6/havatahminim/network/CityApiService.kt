package com.erendogan6.havatahminim.network

import com.erendogan6.havatahminim.model.City
import retrofit2.http.GET
import retrofit2.http.Query

interface CityApiService {
    @GET("geo/1.0/direct")
    suspend fun getCities(
        @Query("q") query: String,
        @Query("appid") apiKey: String = "3d4e2ea2d92e6ec224c1bc97c4057c27",
        @Query("limit") limit: Int = 5
    ): List<City>
}