package com.erendogan6.havatahminim.model.airquality

/** The six pollen species Open-Meteo forecasts (CAMS Europe). */
enum class PollenType {
    ALDER,
    BIRCH,
    GRASS,
    MUGWORT,
    OLIVE,
    RAGWEED,
}

/** Coarse risk bucket derived from the raw grains/m³ value via [com.erendogan6.havatahminim.util.PollenLevel]. */
enum class PollenRisk {
    NONE,
    LOW,
    MODERATE,
    HIGH,
    VERY_HIGH,
}

data class PollenReading(
    val type: PollenType,
    val valueGrains: Double?,
    val risk: PollenRisk,
)

/** Hourly concentration series for one pollen type within a day; [values] aligns with the day's hour list. */
data class PollenSeries(
    val type: PollenType,
    val values: List<Double?>,
)

/**
 * One day of the pollen outlook. [date] is an epoch-second timestamp inside that local day (used
 * only for day-name formatting). [readings] holds the day's peak value/risk per pollen type, while
 * [hours] + [hourly] expose the intra-day variation for charting.
 */
data class DailyPollenForecast(
    val date: Long,
    val readings: List<PollenReading>,
    val hours: List<Long>,
    val hourly: List<PollenSeries>,
)

/**
 * Domain model consumed by the UI / ViewModel. [pollenAvailable] is false outside Europe (all
 * pollen fields null), in which case only the air-quality metrics are meaningful.
 */
data class AirQualityInfo(
    val pollen: List<PollenReading>,
    val dailyForecast: List<DailyPollenForecast>,
    val pm25: Double?,
    val pm10: Double?,
    val ozone: Double?,
    val europeanAqi: Int?,
    val pollenAvailable: Boolean,
)
