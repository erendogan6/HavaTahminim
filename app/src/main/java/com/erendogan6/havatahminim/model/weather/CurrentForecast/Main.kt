package com.erendogan6.havatahminim.model.weather.CurrentForecast

data class Main(
    val temp: Double,
    val feels_like: Double,
    val temp_min: Double,
    val temp_max: Double,
    val humidity: Int
)