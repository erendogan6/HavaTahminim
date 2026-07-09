package com.erendogan6.havatahminim.util

import com.erendogan6.havatahminim.core.common.R
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
    /** [low, moderate, high, veryHigh] grains/m³ cutoffs per plant group. */
    private fun thresholds(type: PollenType): List<Double> =
        when (type) {
            // Trees
            PollenType.ALDER, PollenType.BIRCH, PollenType.OLIVE -> listOf(1.0, 10.0, 50.0, 100.0)
            // Grass
            PollenType.GRASS -> listOf(1.0, 20.0, 50.0, 200.0)
            // Weeds
            PollenType.MUGWORT, PollenType.RAGWEED -> listOf(1.0, 10.0, 30.0, 50.0)
        }

    fun risk(
        type: PollenType,
        grains: Double?,
    ): PollenRisk {
        if (grains == null) return PollenRisk.NONE
        val (low, moderate, high, veryHigh) = thresholds(type)
        return when {
            grains < low -> PollenRisk.NONE
            grains < moderate -> PollenRisk.LOW
            grains < high -> PollenRisk.MODERATE
            grains < veryHigh -> PollenRisk.HIGH
            else -> PollenRisk.VERY_HIGH
        }
    }

    /**
     * Bar fill fraction proportional to the actual concentration, scaled against this pollen's
     * "very high" threshold (so the width reflects the real value, not just the risk bucket).
     * Any positive value keeps a small visible sliver.
     */
    fun fraction(
        type: PollenType,
        grains: Double?,
    ): Float {
        if (grains == null || grains <= 0.0) return 0f
        val veryHigh = thresholds(type).last()
        return (grains / veryHigh).toFloat().coerceIn(0.03f, 1f)
    }

    fun riskLabelRes(risk: PollenRisk): Int =
        when (risk) {
            PollenRisk.NONE -> R.string.pollen_risk_none
            PollenRisk.LOW -> R.string.pollen_risk_low
            PollenRisk.MODERATE -> R.string.pollen_risk_moderate
            PollenRisk.HIGH -> R.string.pollen_risk_high
            PollenRisk.VERY_HIGH -> R.string.pollen_risk_very_high
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
}
