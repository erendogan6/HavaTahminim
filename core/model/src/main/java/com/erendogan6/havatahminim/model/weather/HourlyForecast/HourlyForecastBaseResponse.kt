package com.erendogan6.havatahminim.model.weather.HourlyForecast

import com.erendogan6.havatahminim.model.weather.CurrentForecast.CurrentWeatherBaseResponse

data class HourlyForecastBaseResponse(
    val list: List<CurrentWeatherBaseResponse>,
)
