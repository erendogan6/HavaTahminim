package com.erendogan6.havatahminim.util

import androidx.compose.ui.graphics.Color
import com.erendogan6.havatahminim.R
import com.erendogan6.havatahminim.model.airquality.PollenRisk
import com.erendogan6.havatahminim.model.airquality.PollenType

/**
 * Open-Meteo returns raw pollen concentrations (grains/m³) with no official risk scale, so we
 * classify them here — analogous to [WmoWeather] for weather codes. Thresholds differ per plant
 * group and are intentionally simple/tunable; values are widely-used European approximations.
 *
 * Also hosts the European AQI band interpretation.
 */
object PollenLevel {
    fun risk(
        type: PollenType,
        grains: Double?,
    ): PollenRisk {
        if (grains == null) return PollenRisk.NONE
        val (low, moderate, high, veryHigh) =
            when (type) {
                // Trees
                PollenType.ALDER, PollenType.BIRCH, PollenType.OLIVE -> listOf(1.0, 10.0, 50.0, 100.0)
                // Grass
                PollenType.GRASS -> listOf(1.0, 20.0, 50.0, 200.0)
                // Weeds
                PollenType.MUGWORT, PollenType.RAGWEED -> listOf(1.0, 10.0, 30.0, 50.0)
            }
        return when {
            grains < low -> PollenRisk.NONE
            grains < moderate -> PollenRisk.LOW
            grains < high -> PollenRisk.MODERATE
            grains < veryHigh -> PollenRisk.HIGH
            else -> PollenRisk.VERY_HIGH
        }
    }

    fun riskLabelRes(risk: PollenRisk): Int =
        when (risk) {
            PollenRisk.NONE -> R.string.pollen_risk_none
            PollenRisk.LOW -> R.string.pollen_risk_low
            PollenRisk.MODERATE -> R.string.pollen_risk_moderate
            PollenRisk.HIGH -> R.string.pollen_risk_high
            PollenRisk.VERY_HIGH -> R.string.pollen_risk_very_high
        }

    fun riskColor(risk: PollenRisk): Color =
        when (risk) {
            PollenRisk.NONE -> Color(0xFF9E9E9E)
            PollenRisk.LOW -> Color(0xFF4CAF50)
            PollenRisk.MODERATE -> Color(0xFFFFC107)
            PollenRisk.HIGH -> Color(0xFFFF9800)
            PollenRisk.VERY_HIGH -> Color(0xFFF44336)
        }

    fun typeNameRes(type: PollenType): Int =
        when (type) {
            PollenType.ALDER -> R.string.pollen_type_alder
            PollenType.BIRCH -> R.string.pollen_type_birch
            PollenType.GRASS -> R.string.pollen_type_grass
            PollenType.MUGWORT -> R.string.pollen_type_mugwort
            PollenType.OLIVE -> R.string.pollen_type_olive
            PollenType.RAGWEED -> R.string.pollen_type_ragweed
        }

    /** Considered worth surfacing in ZekAI / notifications. */
    fun isAlarming(risk: PollenRisk): Boolean = risk == PollenRisk.HIGH || risk == PollenRisk.VERY_HIGH
}

/** European Air Quality Index band interpretation (0–20 good … >100 extremely poor). */
object AqiLevel {
    fun labelRes(aqi: Int?): Int =
        when {
            aqi == null -> R.string.aqi_unknown
            aqi <= 20 -> R.string.aqi_good
            aqi <= 40 -> R.string.aqi_fair
            aqi <= 60 -> R.string.aqi_moderate
            aqi <= 80 -> R.string.aqi_poor
            aqi <= 100 -> R.string.aqi_very_poor
            else -> R.string.aqi_extremely_poor
        }

    fun color(aqi: Int?): Color =
        when {
            aqi == null -> Color(0xFF9E9E9E)
            aqi <= 20 -> Color(0xFF4CAF50)
            aqi <= 40 -> Color(0xFF8BC34A)
            aqi <= 60 -> Color(0xFFFFC107)
            aqi <= 80 -> Color(0xFFFF9800)
            aqi <= 100 -> Color(0xFFF44336)
            else -> Color(0xFF9C27B0)
        }
}
