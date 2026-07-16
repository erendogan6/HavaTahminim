package com.erendogan6.havatahminim.ui.viewModel

import com.erendogan6.havatahminim.model.weather.dailyforecast.DailyForecastBaseResponse

/**
 * What the Daily screen renders. Exactly one of loading / [error] / [forecast] is active per
 * emission, and the screen resolves them in that order of precedence.
 */
data class DailyUiState(
    val isLoading: Boolean = true,
    val forecast: DailyForecastBaseResponse? = null,
    val error: String? = null,
)
