package com.erendogan6.havatahminim.model.weather.openmeteo

import com.google.gson.annotations.SerializedName

/**
 * Raw Open-Meteo forecast response. Time fields arrive as epoch seconds (`timeformat=unixtime`);
 * the repository maps these blocks into the domain models.
 */
data class OpenMeteoResponse(
    @SerializedName("current") val current: CurrentBlock? = null,
    @SerializedName("hourly") val hourly: HourlyBlock? = null,
    @SerializedName("daily") val daily: DailyBlock? = null,
)

data class CurrentBlock(
    @SerializedName("time") val time: Long = 0,
    @SerializedName("temperature_2m") val temperature: Double = 0.0,
    @SerializedName("apparent_temperature") val apparentTemperature: Double = 0.0,
    @SerializedName("relative_humidity_2m") val humidity: Int = 0,
    @SerializedName("weather_code") val weatherCode: Int = 0,
)

data class HourlyBlock(
    @SerializedName("time") val time: List<Long> = emptyList(),
    @SerializedName("temperature_2m") val temperature: List<Double> = emptyList(),
    @SerializedName("weather_code") val weatherCode: List<Int> = emptyList(),
    @SerializedName("precipitation_probability") val precipitationProbability: List<Int?> = emptyList(),
)

data class DailyBlock(
    @SerializedName("time") val time: List<Long> = emptyList(),
    @SerializedName("weather_code") val weatherCode: List<Int> = emptyList(),
    @SerializedName("temperature_2m_max") val temperatureMax: List<Double> = emptyList(),
    @SerializedName("temperature_2m_min") val temperatureMin: List<Double> = emptyList(),
    @SerializedName("apparent_temperature_max") val apparentTemperatureMax: List<Double> = emptyList(),
    @SerializedName("sunrise") val sunrise: List<Long> = emptyList(),
    @SerializedName("sunset") val sunset: List<Long> = emptyList(),
)
