package com.erendogan6.havatahminim.ui.view.component

import androidx.compose.ui.graphics.Color
import com.erendogan6.havatahminim.model.airquality.PollenRisk
import com.erendogan6.havatahminim.model.airquality.PollenType
import com.erendogan6.havatahminim.ui.theme.WeatherColors

/**
 * Maps the domain risk/species enums onto the design-system tokens. Lives in the feature (not
 * `:core:common`) so the common module stays free of Compose, and not in `:core:designsystem`
 * so the design system stays free of domain models.
 */
internal fun WeatherColors.riskColor(risk: PollenRisk): Color =
    when (risk) {
        PollenRisk.NONE -> riskNone
        PollenRisk.LOW -> riskLow
        PollenRisk.MODERATE -> riskModerate
        PollenRisk.HIGH -> riskHigh
        PollenRisk.VERY_HIGH -> riskVeryHigh
    }

/** Distinct, high-contrast per-species line color for the intra-day chart (and legend). */
internal fun WeatherColors.typeColor(type: PollenType): Color =
    when (type) {
        PollenType.ALDER -> speciesAlder
        PollenType.BIRCH -> speciesBirch
        PollenType.GRASS -> speciesGrass
        PollenType.MUGWORT -> speciesMugwort
        PollenType.OLIVE -> speciesOlive
        PollenType.RAGWEED -> speciesRagweed
    }

/** European AQI bands reuse the risk ramp, plus the two AQI-only bands (fair / extremely poor). */
internal fun WeatherColors.aqiColor(aqi: Int?): Color =
    when {
        aqi == null -> riskNone
        aqi <= 20 -> riskLow
        aqi <= 40 -> aqiFair
        aqi <= 60 -> riskModerate
        aqi <= 80 -> riskHigh
        aqi <= 100 -> riskVeryHigh
        else -> aqiExtremelyPoor
    }
