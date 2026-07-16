package com.erendogan6.havatahminim.model.weather.dailyforecast

data class DailyForecastBaseResponse(
    val city: City? = null,
    val list: List<DailyForecast>
)
