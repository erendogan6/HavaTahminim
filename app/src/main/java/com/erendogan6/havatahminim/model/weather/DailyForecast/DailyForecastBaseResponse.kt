package com.erendogan6.havatahminim.model.weather.DailyForecast

data class DailyForecastBaseResponse(
    val city: City,
    val list: List<DailyForecast>
)