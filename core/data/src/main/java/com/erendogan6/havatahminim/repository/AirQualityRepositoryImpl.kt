package com.erendogan6.havatahminim.repository

import com.erendogan6.havatahminim.model.airquality.AirQualityInfo
import com.erendogan6.havatahminim.model.airquality.DailyPollenForecast
import com.erendogan6.havatahminim.model.airquality.PollenReading
import com.erendogan6.havatahminim.model.airquality.PollenSeries
import com.erendogan6.havatahminim.model.airquality.PollenType
import com.erendogan6.havatahminim.model.weather.openmeteo.AirQualityHourly
import com.erendogan6.havatahminim.model.weather.openmeteo.AirQualityResponse
import com.erendogan6.havatahminim.network.AirQualityApiService
import com.erendogan6.havatahminim.network.ApiResult
import com.erendogan6.havatahminim.core.common.di.IoDispatcher
import com.erendogan6.havatahminim.network.safeApiCall
import kotlinx.coroutines.CoroutineDispatcher
import com.erendogan6.havatahminim.util.PollenLevel
import java.time.Instant
import java.time.LocalDate
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton

/** All time/zone reads go through the injected [Clock]; the fetch runs on the injected dispatcher. */
@Singleton
class AirQualityRepositoryImpl
    @Inject
    constructor(
        private val airQualityApiService: AirQualityApiService,
        private val clock: Clock,
        @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : AirQualityRepository {
        override suspend fun getAirQuality(
            lat: Double,
            lon: Double,
        ): ApiResult<AirQualityInfo> =
            safeApiCall(ioDispatcher) {
                mapAirQuality(airQualityApiService.getAirQuality(lat, lon))
            }

        private fun mapAirQuality(response: AirQualityResponse): AirQualityInfo {
            val current = response.current
            val rawByType =
                mapOf(
                    PollenType.ALDER to current?.alderPollen,
                    PollenType.BIRCH to current?.birchPollen,
                    PollenType.GRASS to current?.grassPollen,
                    PollenType.MUGWORT to current?.mugwortPollen,
                    PollenType.OLIVE to current?.olivePollen,
                    PollenType.RAGWEED to current?.ragweedPollen,
                )
            val pollen =
                rawByType.map { (type, grains) ->
                    PollenReading(type = type, valueGrains = grains, risk = PollenLevel.risk(type, grains))
                }
            val hourly = response.hourly
            val hourlyByType =
                mapOf(
                    PollenType.ALDER to hourly?.alderPollen.orEmpty(),
                    PollenType.BIRCH to hourly?.birchPollen.orEmpty(),
                    PollenType.GRASS to hourly?.grassPollen.orEmpty(),
                    PollenType.MUGWORT to hourly?.mugwortPollen.orEmpty(),
                    PollenType.OLIVE to hourly?.olivePollen.orEmpty(),
                    PollenType.RAGWEED to hourly?.ragweedPollen.orEmpty(),
                )
            return AirQualityInfo(
                pollen = pollen,
                dailyForecast = buildDailyPollen(response.hourly),
                hourlyTimes = hourly?.time.orEmpty(),
                hourlyByType = hourlyByType,
                pm25 = current?.pm25,
                pm10 = current?.pm10,
                ozone = current?.ozone,
                europeanAqi = current?.europeanAqi,
                pollenAvailable = pollen.any { it.valueGrains != null },
            )
        }

        /**
         * Open-Meteo only forecasts pollen hourly, so we aggregate the hourly series into a per-day
         * outlook by taking each day's **peak** concentration (worst case) per pollen type. Hours
         * are bucketed into local calendar days.
         */
        private fun buildDailyPollen(hourly: AirQualityHourly?): List<DailyPollenForecast> {
            if (hourly == null || hourly.time.isEmpty()) return emptyList()
            val zone = clock.zone
            val seriesByType =
                mapOf(
                    PollenType.ALDER to hourly.alderPollen,
                    PollenType.BIRCH to hourly.birchPollen,
                    PollenType.GRASS to hourly.grassPollen,
                    PollenType.MUGWORT to hourly.mugwortPollen,
                    PollenType.OLIVE to hourly.olivePollen,
                    PollenType.RAGWEED to hourly.ragweedPollen,
                )
            val indicesByDay = LinkedHashMap<LocalDate, MutableList<Int>>()
            hourly.time.forEachIndexed { i, t ->
                val day = Instant.ofEpochSecond(t).atZone(zone).toLocalDate()
                indicesByDay.getOrPut(day) { mutableListOf() }.add(i)
            }
            return indicesByDay.values.map { indices ->
                val readings =
                    seriesByType.map { (type, series) ->
                        val peak = indices.mapNotNull { idx -> series?.getOrNull(idx) }.maxOrNull()
                        PollenReading(type, peak, PollenLevel.risk(type, peak))
                    }
                val hourlySeries =
                    seriesByType.map { (type, series) ->
                        PollenSeries(type, indices.map { idx -> series?.getOrNull(idx) })
                    }
                DailyPollenForecast(
                    date = hourly.time[indices.first()],
                    readings = readings,
                    hours = indices.map { hourly.time[it] },
                    hourly = hourlySeries,
                )
            }
        }
    }
