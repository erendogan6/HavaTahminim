package com.erendogan6.havatahminim.ui.viewModel

import com.erendogan6.havatahminim.model.weather.currentforecast.CurrentWeatherBaseResponse
import com.erendogan6.havatahminim.model.weather.hourlyforecast.HourlyForecastBaseResponse

/**
 * What the Today screen renders. One of loading / [error] / [weather] is active per emission,
 * resolved in that order. [hourly] arrives one emission after [weather], so the conditions card
 * doesn't wait for the hourly strip.
 */
data class TodayUiState(
    val isLoading: Boolean = true,
    val weather: CurrentWeatherBaseResponse? = null,
    val hourly: HourlyForecastBaseResponse? = null,
    val error: String? = null,
)
