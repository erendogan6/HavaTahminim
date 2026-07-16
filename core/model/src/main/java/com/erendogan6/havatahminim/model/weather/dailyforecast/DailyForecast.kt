package com.erendogan6.havatahminim.model.weather.dailyforecast

import com.erendogan6.havatahminim.model.weather.common.Weather

data class DailyForecast(
    val dt: Long,
    val sunrise: Long,
    val sunset: Long,
    val temp: Temperature,
    val humidity: Int,
    val weather: List<Weather>,
)
