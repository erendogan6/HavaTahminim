package com.erendogan6.havatahminim.ui.viewModel

import com.erendogan6.havatahminim.model.weather.CurrentForecast.CurrentWeatherBaseResponse
import com.erendogan6.havatahminim.model.weather.HourlyForecast.HourlyForecastBaseResponse

/**
 * What the Today screen renders. Exactly one of loading / [error] / [weather] is active per
 * emission, and the screen resolves them in that order of precedence.
 *
 * [hourly] trails [weather] by one emission on purpose: the current-conditions card shows up as
 * soon as it's available instead of waiting for the hourly strip.
 */
data class TodayUiState(
    val isLoading: Boolean = true,
    val weather: CurrentWeatherBaseResponse? = null,
    val hourly: HourlyForecastBaseResponse? = null,
    val error: String? = null,
)
