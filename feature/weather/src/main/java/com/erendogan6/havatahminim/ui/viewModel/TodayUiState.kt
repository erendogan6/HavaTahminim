package com.erendogan6.havatahminim.ui.viewModel

import com.erendogan6.havatahminim.model.weather.CurrentForecast.CurrentWeatherBaseResponse
import com.erendogan6.havatahminim.model.weather.HourlyForecast.HourlyForecastBaseResponse

/**
 * The Today screen's rendering contract — the screen shows exactly one of these at any moment.
 * Produced by [TodayViewModel] (the domain→UI shaping lives there; repositories only ever return
 * domain models). [Success.hourly] is null until the second fetch lands, so the conditions card
 * renders without waiting for the hourly strip.
 */
sealed interface TodayUiState {
    data object Loading : TodayUiState

    data class Error(
        val message: String,
    ) : TodayUiState

    data class Success(
        val weather: CurrentWeatherBaseResponse,
        val hourly: HourlyForecastBaseResponse?,
    ) : TodayUiState
}
