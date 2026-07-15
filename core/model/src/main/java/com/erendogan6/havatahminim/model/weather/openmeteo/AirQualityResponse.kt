package com.erendogan6.havatahminim.model.weather.openmeteo

import com.google.gson.annotations.SerializedName

/**
 * Raw Open-Meteo air-quality response. Pollen is only modelled for Europe, so every pollen field
 * is nullable.
 */
data class AirQualityResponse(
    @SerializedName("current") val current: AirQualityCurrent? = null,
    @SerializedName("hourly") val hourly: AirQualityHourly? = null,
)

data class AirQualityCurrent(
    @SerializedName("time") val time: Long = 0,
    @SerializedName("alder_pollen") val alderPollen: Double? = null,
    @SerializedName("birch_pollen") val birchPollen: Double? = null,
    @SerializedName("grass_pollen") val grassPollen: Double? = null,
    @SerializedName("mugwort_pollen") val mugwortPollen: Double? = null,
    @SerializedName("olive_pollen") val olivePollen: Double? = null,
    @SerializedName("ragweed_pollen") val ragweedPollen: Double? = null,
    @SerializedName("pm2_5") val pm25: Double? = null,
    @SerializedName("pm10") val pm10: Double? = null,
    @SerializedName("ozone") val ozone: Double? = null,
    @SerializedName("european_aqi") val europeanAqi: Int? = null,
)

data class AirQualityHourly(
    @SerializedName("time") val time: List<Long> = emptyList(),
    @SerializedName("alder_pollen") val alderPollen: List<Double?>? = null,
    @SerializedName("birch_pollen") val birchPollen: List<Double?>? = null,
    @SerializedName("grass_pollen") val grassPollen: List<Double?>? = null,
    @SerializedName("mugwort_pollen") val mugwortPollen: List<Double?>? = null,
    @SerializedName("olive_pollen") val olivePollen: List<Double?>? = null,
    @SerializedName("ragweed_pollen") val ragweedPollen: List<Double?>? = null,
)
