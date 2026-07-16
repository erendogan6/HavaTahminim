package com.erendogan6.havatahminim.model.weather.hourlyforecast

import com.erendogan6.havatahminim.model.weather.currentforecast.CurrentWeatherBaseResponse

data class HourlyForecastBaseResponse(
    val list: List<CurrentWeatherBaseResponse>,
)
