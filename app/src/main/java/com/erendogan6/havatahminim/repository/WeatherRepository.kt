package com.erendogan6.havatahminim.repository

import com.erendogan6.havatahminim.model.CurrentWeatherBaseResponse
import com.erendogan6.havatahminim.model.HourlyForecastBaseResponse
import com.erendogan6.havatahminim.network.ProWeatherApiService
import com.erendogan6.havatahminim.network.WeatherApiService
import javax.inject.Inject

class WeatherRepository @Inject constructor(
    private val weatherApiService: WeatherApiService,
    private val proWeatherApiService: ProWeatherApiService
) {
    suspend fun getWeather(lat: Double, lon: Double, apiKey: String): CurrentWeatherBaseResponse {
        return weatherApiService.getWeather(lat, lon, apiKey)
    }

    suspend fun getHourlyWeather(lat: Double, lon: Double, apiKey: String): HourlyForecastBaseResponse {
        return proWeatherApiService.getHourlyWeather(lat, lon, apiKey)
    }
}
