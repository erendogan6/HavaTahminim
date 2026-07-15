package com.erendogan6.havatahminim.repository

import com.erendogan6.havatahminim.model.weather.openmeteo.CurrentBlock
import com.erendogan6.havatahminim.model.weather.openmeteo.DailyBlock
import com.erendogan6.havatahminim.model.weather.openmeteo.HourlyBlock
import com.erendogan6.havatahminim.model.weather.openmeteo.OpenMeteoResponse
import com.erendogan6.havatahminim.network.ApiResult
import com.erendogan6.havatahminim.network.getOrNull
import com.erendogan6.havatahminim.testing.dao.FakeDailyForecastDao
import com.erendogan6.havatahminim.testing.fixture.TestCoords
import com.erendogan6.havatahminim.testing.fixture.TestTime
import com.erendogan6.havatahminim.testing.repository.FakeLocationRepository
import com.erendogan6.havatahminim.testing.service.FakeWeatherApiService
import com.erendogan6.havatahminim.testing.util.FakeResourcesProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.IOException
import java.time.LocalDate
import com.erendogan6.havatahminim.core.common.R as CommonR

class WeatherRepositoryImplTest {
    private val api = FakeWeatherApiService()
    private val dao = FakeDailyForecastDao()
    private val locationRepository = FakeLocationRepository()
    private val resources = FakeResourcesProvider()

    private fun TestScope.repository() =
        WeatherRepositoryImpl(
            weatherApiService = api,
            dailyForecastDao = dao,
            locationRepository = locationRepository,
            resourcesProvider = resources,
            clock = TestTime.clock(),
            ioDispatcher = StandardTestDispatcher(testScheduler),
        )

    // ---- refreshCurrentWeather -------------------------------------------------------------

    @Test
    fun `refresh maps the open-meteo payload into the domain model`() =
        runTest {
            api.currentResponse =
                OpenMeteoResponse(
                    current =
                        CurrentBlock(
                            time = TestTime.EPOCH_SECONDS,
                            temperature = 27.4,
                            apparentTemperature = 29.1,
                            humidity = 63,
                            weatherCode = 0,
                        ),
                    daily = DailyBlock(sunrise = listOf(100L), sunset = listOf(200L)),
                )
            locationRepository.locationName = "Kadıköy"

            val result = repository().refreshCurrentWeather(TestCoords.ISTANBUL_LAT, TestCoords.ISTANBUL_LON)

            val weather = (result as ApiResult.Success).data
            assertThat(weather.main.temp).isEqualTo(27.4)
            assertThat(weather.main.feelsLike).isEqualTo(29.1)
            assertThat(weather.main.humidity).isEqualTo(63)
            assertThat(weather.dt).isEqualTo(TestTime.EPOCH_SECONDS)
            assertThat(weather.sys.sunrise).isEqualTo(100L)
            assertThat(weather.sys.sunset).isEqualTo(200L)
            assertThat(weather.name).isEqualTo("Kadıköy")
            assertThat(weather.weather.first().main).isEqualTo("Clear")
            assertThat(weather.weather.first().description).isEqualTo("res:${CommonR.string.wmo_clear_sky}")
        }

    @Test
    fun `refresh publishes the result into currentWeather`() =
        runTest {
            api.currentResponse = OpenMeteoResponse(current = CurrentBlock(temperature = 20.0))
            val repository = repository()

            repository.refreshCurrentWeather(TestCoords.ISTANBUL_LAT, TestCoords.ISTANBUL_LON)

            assertThat(repository.currentWeather.value?.main?.temp).isEqualTo(20.0)
        }

    @Test
    fun `missing current block is an Unknown error and publishes nothing`() =
        runTest {
            api.currentResponse = OpenMeteoResponse(current = null)
            val repository = repository()

            val result = repository.refreshCurrentWeather(TestCoords.ISTANBUL_LAT, TestCoords.ISTANBUL_LON)

            assertThat(result).isInstanceOf(ApiResult.Error.Unknown::class.java)
            assertThat(repository.currentWeather.value).isNull()
        }

    @Test
    fun `network failure maps to the Network error`() =
        runTest {
            api.nextError = IOException("offline")

            val result = repository().refreshCurrentWeather(TestCoords.ISTANBUL_LAT, TestCoords.ISTANBUL_LON)

            assertThat(result).isEqualTo(ApiResult.Error.Network)
        }

    @Test
    fun `missing daily block defaults sunrise and sunset to zero`() =
        runTest {
            api.currentResponse = OpenMeteoResponse(current = CurrentBlock(), daily = null)

            val weather =
                repository()
                    .refreshCurrentWeather(TestCoords.ISTANBUL_LAT, TestCoords.ISTANBUL_LON)
                    .getOrNull()!!

            assertThat(weather.sys.sunrise).isEqualTo(0L)
            assertThat(weather.sys.sunset).isEqualTo(0L)
        }

    // ---- getHourlyWeather ------------------------------------------------------------------

    @Test
    fun `hourly list starts at the first upcoming hour`() =
        runTest {
            // Hours 10:00..15:00 UTC around the fixed 12:00Z clock.
            val hours = (-2..3).map { TestTime.EPOCH_SECONDS + it * 3600L }
            api.hourlyResponse =
                OpenMeteoResponse(
                    hourly =
                        HourlyBlock(
                            time = hours,
                            temperature = hours.map { 20.0 },
                            weatherCode = hours.map { 0 },
                            precipitationProbability = hours.map { 40 },
                        ),
                )

            val list =
                repository().getHourlyWeather(TestCoords.ISTANBUL_LAT, TestCoords.ISTANBUL_LON).getOrNull()!!.list

            assertThat(list.first().dt).isEqualTo(TestTime.EPOCH_SECONDS) // 12:00, not 10:00
            assertThat(list).hasSize(4)
            assertThat(list.first().pop).isEqualTo(40)
        }

    @Test
    fun `all-past hours fall back to the full list`() =
        runTest {
            val hours = (1..3).map { TestTime.EPOCH_SECONDS - it * 3600L }.sorted()
            api.hourlyResponse =
                OpenMeteoResponse(
                    hourly = HourlyBlock(time = hours, temperature = hours.map { 20.0 }, weatherCode = hours.map { 0 }),
                )

            val list =
                repository().getHourlyWeather(TestCoords.ISTANBUL_LAT, TestCoords.ISTANBUL_LON).getOrNull()!!.list

            assertThat(list).hasSize(3)
        }

    @Test
    fun `missing hourly block yields an empty list`() =
        runTest {
            api.hourlyResponse = OpenMeteoResponse(hourly = null)

            val list =
                repository().getHourlyWeather(TestCoords.ISTANBUL_LAT, TestCoords.ISTANBUL_LON).getOrNull()!!.list

            assertThat(list).isEmpty()
        }

    @Test
    fun `ragged arrays are padded with defaults instead of crashing`() =
        runTest {
            val hours = (0..2).map { TestTime.EPOCH_SECONDS + it * 3600L }
            api.hourlyResponse =
                OpenMeteoResponse(
                    // temperature/weatherCode arrays shorter than time, a real Open-Meteo glitch shape.
                    hourly = HourlyBlock(time = hours, temperature = listOf(21.0), weatherCode = emptyList()),
                )

            val list =
                repository().getHourlyWeather(TestCoords.ISTANBUL_LAT, TestCoords.ISTANBUL_LON).getOrNull()!!.list

            assertThat(list).hasSize(3)
            assertThat(list[1].main.temp).isEqualTo(0.0)
            assertThat(list[1].weather.first().main).isEqualTo("Clear") // code 0 default
        }

    // ---- getDailyWeather (per-day cache) -----------------------------------------------------

    private val todayEpochMillis =
        LocalDate
            .of(2026, 7, 15)
            .atStartOfDay(TestTime.ZONE)
            .toInstant()
            .toEpochMilli()

    private fun seedDailyResponse() {
        api.dailyResponse =
            OpenMeteoResponse(
                daily =
                    DailyBlock(
                        time = listOf(TestTime.EPOCH_SECONDS),
                        weatherCode = listOf(61),
                        temperatureMax = listOf(30.0),
                        temperatureMin = listOf(19.0),
                        sunrise = listOf(100L),
                        sunset = listOf(200L),
                    ),
            )
    }

    @Test
    fun `daily miss fetches, maps and caches under the fixed-clock midnight key`() =
        runTest {
            seedDailyResponse()

            val forecast = repository().getDailyWeather(TestCoords.ISTANBUL_LAT, TestCoords.ISTANBUL_LON).getOrNull()!!

            assertThat(forecast.list).hasSize(1)
            assertThat(forecast.list.first().temp.day).isEqualTo(30.0)
            assertThat(forecast.list.first().temp.night).isEqualTo(19.0)
            assertThat(forecast.list.first().weather.first().main).isEqualTo("Rain")
            assertThat(dao.byDate).containsKey(todayEpochMillis)
            assertThat(api.dailyCallCount).isEqualTo(1)
        }

    @Test
    fun `daily hit within 10km serves the cache without an api call`() =
        runTest {
            seedDailyResponse()
            val repository = repository()
            repository.getDailyWeather(TestCoords.ISTANBUL_LAT, TestCoords.ISTANBUL_LON)

            val second = repository.getDailyWeather(TestCoords.NEAR_9_9_KM_LAT, TestCoords.ISTANBUL_LON)

            assertThat(second.getOrNull()!!.list).hasSize(1)
            assertThat(api.dailyCallCount).isEqualTo(1) // no refetch
        }

    @Test
    fun `daily entry beyond 10km is refetched and overwritten`() =
        runTest {
            seedDailyResponse()
            val repository = repository()
            repository.getDailyWeather(TestCoords.ISTANBUL_LAT, TestCoords.ISTANBUL_LON)

            repository.getDailyWeather(TestCoords.NEAR_10_1_KM_LAT, TestCoords.ISTANBUL_LON)

            assertThat(api.dailyCallCount).isEqualTo(2)
            assertThat(dao.byDate[todayEpochMillis]!!.latitude).isEqualTo(TestCoords.NEAR_10_1_KM_LAT)
        }

    @Test
    fun `daily api failure on a miss surfaces the error and caches nothing`() =
        runTest {
            api.nextError = IOException("offline")

            val result = repository().getDailyWeather(TestCoords.ISTANBUL_LAT, TestCoords.ISTANBUL_LON)

            assertThat(result).isEqualTo(ApiResult.Error.Network)
            assertThat(dao.byDate).isEmpty()
        }
}
