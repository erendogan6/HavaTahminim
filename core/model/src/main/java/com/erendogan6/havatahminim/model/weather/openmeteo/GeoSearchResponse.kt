package com.erendogan6.havatahminim.model.weather.openmeteo

import com.erendogan6.havatahminim.model.weather.DailyForecast.City
import com.google.gson.annotations.SerializedName

/**
 * Wrapper around the Open-Meteo geocoding endpoint
 * (https://geocoding-api.open-meteo.com/v1/search). Results are returned localized via the
 * `language` query parameter, so no per-language name table is needed.
 */
data class GeoSearchResponse(
    @SerializedName("results") val results: List<City>? = null,
)
