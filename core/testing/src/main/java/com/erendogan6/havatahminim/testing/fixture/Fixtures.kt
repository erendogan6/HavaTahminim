package com.erendogan6.havatahminim.testing.fixture

import com.erendogan6.havatahminim.model.airquality.AirQualityInfo
import com.erendogan6.havatahminim.model.airquality.PollenReading
import com.erendogan6.havatahminim.model.airquality.PollenRisk
import com.erendogan6.havatahminim.model.airquality.PollenType
import com.erendogan6.havatahminim.model.entity.LocationEntity
import com.erendogan6.havatahminim.model.entity.WeatherSuggestionEntity
import com.erendogan6.havatahminim.model.weather.Common.Weather
import com.erendogan6.havatahminim.model.weather.CurrentForecast.CurrentWeatherBaseResponse
import com.erendogan6.havatahminim.model.weather.CurrentForecast.Main
import com.erendogan6.havatahminim.model.weather.CurrentForecast.Sys
import com.erendogan6.havatahminim.model.weather.DailyForecast.City
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

/**
 * The suite's single notion of "now": a fixed clock. Every time-dependent assertion (cache TTLs,
 * "upcoming hour" cutoffs, day bucketing) is written against this instant, never wall time.
 */
object TestTime {
    val INSTANT: Instant = Instant.parse("2026-07-15T12:00:00Z")
    val ZONE: ZoneId = ZoneId.of("Europe/Istanbul")

    fun clock(): Clock = Clock.fixed(INSTANT, ZONE)

    val EPOCH_SECONDS: Long = INSTANT.epochSecond
    val EPOCH_MILLIS: Long = INSTANT.toEpochMilli()
}

/**
 * Coordinate set for cache-radius tests. The offsets are precomputed against the pure haversine
 * (1° latitude ≈ 111.19 km): each pair deliberately straddles the 5 km / 10 km thresholds.
 */
object TestCoords {
    const val ISTANBUL_LAT = 41.0082
    const val ISTANBUL_LON = 28.9784
    const val ANKARA_LAT = 39.9334
    const val ANKARA_LON = 32.8597

    // Pure north-south offsets from Istanbul: distance = dLat * 111.19 km.
    const val NEAR_4_9_KM_LAT = ISTANBUL_LAT + 0.04407 // ≈ 4.90 km
    const val NEAR_5_1_KM_LAT = ISTANBUL_LAT + 0.04587 // ≈ 5.10 km
    const val NEAR_9_9_KM_LAT = ISTANBUL_LAT + 0.08904 // ≈ 9.90 km
    const val NEAR_10_1_KM_LAT = ISTANBUL_LAT + 0.09083 // ≈ 10.10 km
}

fun locationEntityFixture(
    latitude: Double = TestCoords.ISTANBUL_LAT,
    longitude: Double = TestCoords.ISTANBUL_LON,
): LocationEntity = LocationEntity(latitude = latitude, longitude = longitude)

fun cityFixture(
    name: String = "Istanbul",
    latitude: Double = TestCoords.ISTANBUL_LAT,
    longitude: Double = TestCoords.ISTANBUL_LON,
): City = City(name = name, latitude = latitude, longitude = longitude)

fun currentWeatherFixture(
    name: String = "Istanbul",
    temp: Double = 27.0,
    feelsLike: Double = 29.0,
    humidity: Int = 55,
    dt: Long = TestTime.EPOCH_SECONDS,
    sunrise: Long = TestTime.EPOCH_SECONDS - 6 * 3600,
    sunset: Long = TestTime.EPOCH_SECONDS + 6 * 3600,
    category: String = "Clear",
    description: String = "clear sky",
    pop: Int? = null,
): CurrentWeatherBaseResponse =
    CurrentWeatherBaseResponse(
        weather = listOf(Weather(main = category, description = description)),
        main = Main(temp = temp, feels_like = feelsLike, temp_min = temp, temp_max = temp, humidity = humidity),
        dt = dt,
        sys = Sys(sunrise = sunrise, sunset = sunset),
        name = name,
        pop = pop,
    )

fun suggestionEntityFixture(
    suggestion: String = "cached suggestion",
    latitude: Double = TestCoords.ISTANBUL_LAT,
    longitude: Double = TestCoords.ISTANBUL_LON,
    timestamp: Long = TestTime.EPOCH_MILLIS,
    language: String = "tr",
    location: String = "Istanbul",
    temperature: String = "27°C",
): WeatherSuggestionEntity =
    WeatherSuggestionEntity(
        location = location,
        temperature = temperature,
        suggestion = suggestion,
        latitude = latitude,
        longitude = longitude,
        timestamp = timestamp,
        language = language,
    )

fun pollenReadingFixture(
    type: PollenType = PollenType.GRASS,
    valueGrains: Double? = 30.0,
    risk: PollenRisk = PollenRisk.MODERATE,
): PollenReading = PollenReading(type = type, valueGrains = valueGrains, risk = risk)

fun airQualityInfoFixture(
    pollen: List<PollenReading> = listOf(pollenReadingFixture()),
    hourlyTimes: List<Long> = (0 until 12).map { TestTime.EPOCH_SECONDS + it * 3600 },
    hourlyByType: Map<PollenType, List<Double?>> =
        mapOf(PollenType.GRASS to (0 until 12).map { 10.0 * it }),
    pollenAvailable: Boolean = true,
): AirQualityInfo =
    AirQualityInfo(
        pollen = pollen,
        dailyForecast = emptyList(),
        hourlyTimes = hourlyTimes,
        hourlyByType = hourlyByType,
        pm25 = 8.0,
        pm10 = 15.0,
        ozone = 60.0,
        europeanAqi = 25,
        pollenAvailable = pollenAvailable,
    )
