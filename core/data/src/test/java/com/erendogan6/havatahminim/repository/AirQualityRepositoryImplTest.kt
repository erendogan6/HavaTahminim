package com.erendogan6.havatahminim.repository

import com.erendogan6.havatahminim.model.airquality.PollenRisk
import com.erendogan6.havatahminim.model.airquality.PollenType
import com.erendogan6.havatahminim.model.weather.openmeteo.AirQualityCurrent
import com.erendogan6.havatahminim.model.weather.openmeteo.AirQualityHourly
import com.erendogan6.havatahminim.model.weather.openmeteo.AirQualityResponse
import com.erendogan6.havatahminim.network.ApiResult
import com.erendogan6.havatahminim.network.getOrNull
import com.erendogan6.havatahminim.testing.fixture.TestCoords
import com.erendogan6.havatahminim.testing.fixture.TestTime
import com.erendogan6.havatahminim.testing.service.FakeAirQualityApiService
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.IOException
import java.time.LocalDate

class AirQualityRepositoryImplTest {
    private val api = FakeAirQualityApiService()

    private fun TestScope.repository() =
        AirQualityRepositoryImpl(
            airQualityApiService = api,
            clock = TestTime.clock(),
            ioDispatcher = StandardTestDispatcher(testScheduler),
        )

    private suspend fun AirQualityRepository.fetch() =
        getAirQuality(TestCoords.ISTANBUL_LAT, TestCoords.ISTANBUL_LON).getOrNull()!!

    @Test
    fun `maps all six species with risks and the general metrics`() =
        runTest {
            api.response =
                AirQualityResponse(
                    current =
                        AirQualityCurrent(
                            grassPollen = 30.0, // grass MODERATE (20..50)
                            birchPollen = 120.0, // tree VERY_HIGH (>=100)
                            pm25 = 8.5,
                            pm10 = 17.0,
                            ozone = 61.0,
                            europeanAqi = 35,
                        ),
                )

            val info = repository().fetch()

            assertThat(info.pollen).hasSize(6)
            assertThat(info.pollen.first { it.type == PollenType.GRASS }.risk).isEqualTo(PollenRisk.MODERATE)
            assertThat(info.pollen.first { it.type == PollenType.BIRCH }.risk).isEqualTo(PollenRisk.VERY_HIGH)
            assertThat(info.pollen.first { it.type == PollenType.ALDER }.risk).isEqualTo(PollenRisk.NONE)
            assertThat(info.pm25).isEqualTo(8.5)
            assertThat(info.europeanAqi).isEqualTo(35)
        }

    @Test
    fun `pollenAvailable is false when every species is null`() =
        runTest {
            // Non-European response shape: metrics present, pollen absent.
            api.response = AirQualityResponse(current = AirQualityCurrent(pm25 = 12.0, europeanAqi = 40))

            assertThat(repository().fetch().pollenAvailable).isFalse()
        }

    @Test
    fun `pollenAvailable is true with a single non-null species`() =
        runTest {
            api.response = AirQualityResponse(current = AirQualityCurrent(olivePollen = 3.0))

            assertThat(repository().fetch().pollenAvailable).isTrue()
        }

    @Test
    fun `hourly series bucket into local days with per-type peaks`() =
        runTest {
            // 12 hours starting 22:00 Istanbul on Jul 15 — crosses local midnight into Jul 16.
            val start =
                LocalDate
                    .of(2026, 7, 15)
                    .atStartOfDay(TestTime.ZONE)
                    .plusHours(22)
                    .toEpochSecond()
            val hours = (0 until 12).map { start + it * 3600L }
            // Grass peaks at 40 in the first bucket (2 hours), 90 in the second (10 hours).
            val grass = listOf(40.0, 10.0) + (0 until 10).map { if (it == 5) 90.0 else 20.0 }
            api.response =
                AirQualityResponse(
                    current = AirQualityCurrent(grassPollen = 1.0),
                    hourly = AirQualityHourly(time = hours, grassPollen = grass),
                )

            val daily = repository().fetch().dailyForecast

            assertThat(daily).hasSize(2)
            val firstDayGrass = daily[0].readings.first { it.type == PollenType.GRASS }
            val secondDayGrass = daily[1].readings.first { it.type == PollenType.GRASS }
            assertThat(firstDayGrass.valueGrains).isEqualTo(40.0)
            assertThat(secondDayGrass.valueGrains).isEqualTo(90.0)
            assertThat(daily[0].hours).hasSize(2)
            assertThat(daily[1].hours).hasSize(10)
            assertThat(daily[0].date).isEqualTo(hours.first())
        }

    @Test
    fun `species with no hourly data map to NONE readings in the daily outlook`() =
        runTest {
            val hours = listOf(TestTime.EPOCH_SECONDS)
            api.response =
                AirQualityResponse(
                    current = AirQualityCurrent(grassPollen = 1.0),
                    hourly = AirQualityHourly(time = hours, grassPollen = listOf(5.0), birchPollen = null),
                )

            val day = repository().fetch().dailyForecast.single()

            assertThat(day.readings.first { it.type == PollenType.BIRCH }.risk).isEqualTo(PollenRisk.NONE)
        }

    @Test
    fun `missing hourly block yields an empty daily outlook`() =
        runTest {
            api.response = AirQualityResponse(current = AirQualityCurrent(grassPollen = 1.0), hourly = null)

            assertThat(repository().fetch().dailyForecast).isEmpty()
        }

    @Test
    fun `network failure maps to the Network error`() =
        runTest {
            api.nextError = IOException("offline")

            val result = repository().getAirQuality(TestCoords.ISTANBUL_LAT, TestCoords.ISTANBUL_LON)

            assertThat(result).isEqualTo(ApiResult.Error.Network)
        }
}
