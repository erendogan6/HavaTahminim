package com.erendogan6.havatahminim.model.weather.currentforecast

import com.erendogan6.havatahminim.model.weather.common.Weather

data class CurrentWeatherBaseResponse(
    val weather: List<Weather>,
    val main: Main,
    val dt: Long,
    val sys: Sys,
    val name: String,
    // Precipitation probability (%); only populated for hourly entries.
    val pop: Int? = null,
)
