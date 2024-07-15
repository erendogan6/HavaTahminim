package com.erendogan6.havatahminim.model

data class DailyForecastBaseResponse(
    val city: City,
    val list: List<DailyForecast>
)