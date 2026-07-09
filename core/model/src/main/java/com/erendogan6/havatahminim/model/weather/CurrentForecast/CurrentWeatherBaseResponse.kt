package com.erendogan6.havatahminim.model.weather.CurrentForecast

import com.erendogan6.havatahminim.model.weather.Common.Weather

data class CurrentWeatherBaseResponse(
    val weather: List<Weather>,
    val main: Main,
    val dt: Long,
    val sys: Sys,
    val name: String,
    // Precipitation probability (%) — populated for hourly entries, null for the current snapshot.
    val pop: Int? = null,
)