package com.erendogan6.havatahminim.model.weather.CurrentForecast

data class Main(
    val temp: Double,
    val feelsLike: Double,
    val tempMin: Double,
    val tempMax: Double,
    val humidity: Int,
)
