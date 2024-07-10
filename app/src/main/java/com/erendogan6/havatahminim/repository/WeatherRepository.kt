package com.erendogan6.havatahminim.repository
import com.erendogan6.havatahminim.model.BaseResponse
import com.erendogan6.havatahminim.network.WeatherApiService
import javax.inject.Inject

class WeatherRepository @Inject constructor(
    private val weatherApiService: WeatherApiService
) {
    suspend fun getWeather(lat: Double, lon: Double, apiKey: String): BaseResponse {
        return weatherApiService.getWeather(lat, lon, apiKey)
    }
}
