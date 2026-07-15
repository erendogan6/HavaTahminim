package com.erendogan6.havatahminim.ui.viewModel

import com.erendogan6.havatahminim.model.weather.DailyForecast.DailyForecastBaseResponse

/**
 * The Daily screen's rendering contract — the screen shows exactly one of these at any moment.
 * Produced by [DailyForecastViewModel] (the domain→UI shaping lives there; repositories only ever
 * return domain models).
 */
sealed interface DailyUiState {
    data object Loading : DailyUiState

    data class Error(
        val message: String,
    ) : DailyUiState

    data class Success(
        val forecast: DailyForecastBaseResponse,
    ) : DailyUiState
}
