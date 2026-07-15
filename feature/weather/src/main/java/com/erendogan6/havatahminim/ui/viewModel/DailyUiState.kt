package com.erendogan6.havatahminim.ui.viewModel

import com.erendogan6.havatahminim.model.weather.DailyForecast.DailyForecastBaseResponse

/**
 * The Daily screen's rendering contract, produced by [DailyForecastViewModel] (the domain→UI
 * shaping lives there; repositories only ever return domain models).
 *
 * Flat-state contract: fields are morally exclusive — every emission sets exactly one facet
 * (loading / error / content). Render precedence is fixed: **isLoading > error > forecast**;
 * the ViewModel tests pin each emission's full shape.
 */
data class DailyUiState(
    val isLoading: Boolean = true,
    val forecast: DailyForecastBaseResponse? = null,
    val error: String? = null,
)
