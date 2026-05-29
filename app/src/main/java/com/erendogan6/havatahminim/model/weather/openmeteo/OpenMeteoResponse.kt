package com.erendogan6.havatahminim.model.weather.openmeteo

import com.google.gson.annotations.SerializedName

/**
 * Raw response of the Open-Meteo forecast endpoint (https://api.open-meteo.com/v1/forecast).
 *
 * All time fields are requested with `timeformat=unixtime`, so they arrive as epoch seconds (UTC)
 * and map directly onto the [Long] timestamps the domain models expect. The repository converts
 * these blocks into the app's domain models (CurrentWeatherBaseResponse / HourlyForecastBaseResponse /
 * DailyForecastBaseResponse).
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
