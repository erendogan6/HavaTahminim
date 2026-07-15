package com.erendogan6.havatahminim.ui.viewModel

import com.erendogan6.havatahminim.model.weather.CurrentForecast.CurrentWeatherBaseResponse
import com.erendogan6.havatahminim.model.weather.HourlyForecast.HourlyForecastBaseResponse

/**
 * The Today screen's rendering contract, produced by [TodayViewModel] (the domain→UI shaping
 * lives there; repositories only ever return domain models).
 *
 * Flat-state contract: the fields are morally exclusive — every emission constructs a fresh
 * instance setting exactly one facet (loading / error / content), never a mix. The type system
 * doesn't enforce that (a deliberate trade against a sealed hierarchy), so the render precedence
 * is fixed instead: **isLoading > error > weather**, and the ViewModel tests pin each emission's
 * full shape. [hourly] is the one intentional second facet: it arrives after [weather] in a
 * second emission, so the conditions card renders without waiting for the hourly strip.
 */
data class TodayUiState(
    val isLoading: Boolean = true,
    val weather: CurrentWeatherBaseResponse? = null,
    val hourly: HourlyForecastBaseResponse? = null,
    val error: String? = null,
)
