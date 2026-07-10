package com.erendogan6.havatahminim.repository

import com.erendogan6.havatahminim.network.ApiResult
import com.erendogan6.havatahminim.network.getOrNull
import com.erendogan6.havatahminim.testing.dao.FakeWeatherSuggestionDao
import com.erendogan6.havatahminim.testing.fixture.TestCoords
import com.erendogan6.havatahminim.testing.fixture.TestTime
import com.erendogan6.havatahminim.testing.fixture.suggestionEntityFixture
import com.erendogan6.havatahminim.testing.service.FakeSuggestionGenerator
import com.erendogan6.havatahminim.testing.util.FakeResourcesProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SuggestionRepositoryImplTest {
    private val generator = FakeSuggestionGenerator()
    private val dao = FakeWeatherSuggestionDao()
    private val resources = FakeResourcesProvider(currentLanguage = "tr")

    private fun TestScope.repository() =
        SuggestionRepositoryImpl(
            suggestionGenerator = generator,
            weatherSuggestionDao = dao,
            resourcesProvider = resources,
            clock = TestTime.clock(),
            ioDispatcher = StandardTestDispatcher(testScheduler),
        )

    private suspend fun SuggestionRepository.call(
        lat: Double = TestCoords.ISTANBUL_LAT,
        lon: Double = TestCoords.ISTANBUL_LON,
        pollenSummary: String = "",
        forceRefresh: Boolean = false,
    ) = getSuggestions(lat, lon, "Istanbul", "27°C", pollenSummary, forceRefresh)

    // ---- generation & prompt -----------------------------------------------------------------

    @Test
    fun `no cache generates and stores a clock-stamped entity`() =
        runTest {
            generator.script += FakeSuggestionGenerator.success("Drink water!")

            val result = repository().call()

            assertThat(result).isEqualTo(ApiResult.Success("Drink water!"))
            assertThat(dao.stored!!.timestamp).isEqualTo(TestTime.EPOCH_MILLIS)
            assertThat(dao.stored!!.language).isEqualTo("tr")
            assertThat(dao.stored!!.suggestion).isEqualTo("Drink water!")
        }

    @Test
    fun `prompt carries location and temperature, pollen only when present`() =
        runTest {
            generator.script += FakeSuggestionGenerator.success("ok")
            val repository = repository()

            repository.call(pollenSummary = "")
            repository.call(pollenSummary = "Grass: high", forceRefresh = true)

            assertThat(generator.requests[0]).contains("Konum: Istanbul")
            assertThat(generator.requests[0]).contains("Sıcaklık: 27°C")
            assertThat(generator.requests[0]).doesNotContain("polen")
            assertThat(generator.requests[1]).contains("Grass: high")
        }

    @Test
    fun `empty gemini text is an error and nothing is inserted`() =
        runTest {
            generator.script += FakeSuggestionGenerator.success(null)

            val result = repository().call()

            assertThat(result).isInstanceOf(ApiResult.Error.Unknown::class.java)
            assertThat(dao.stored).isNull()
        }

    // ---- cache matrix -------------------------------------------------------------------------

    @Test
    fun `fresh cache is served without touching the generator`() =
        runTest {
            dao.stored = suggestionEntityFixture(suggestion = "cached")

            val result = repository().call()

            assertThat(result.getOrNull()).isEqualTo("cached")
            assertThat(generator.requests).isEmpty()
            assertThat(dao.deleteAllCount).isEqualTo(0)
        }

    @Test
    fun `exactly two hours old still counts as fresh`() =
        runTest {
            dao.stored = suggestionEntityFixture(timestamp = TestTime.EPOCH_MILLIS - 2 * 60 * 60 * 1000L)

            assertThat(repository().call().getOrNull()).isEqualTo("cached suggestion")
            assertThat(generator.requests).isEmpty()
        }

    @Test
    fun `a millisecond past two hours regenerates`() =
        runTest {
            dao.stored = suggestionEntityFixture(timestamp = TestTime.EPOCH_MILLIS - 2 * 60 * 60 * 1000L - 1)
            generator.script += FakeSuggestionGenerator.success("fresh")

            assertThat(repository().call().getOrNull()).isEqualTo("fresh")
            assertThat(generator.requests).hasSize(1)
        }

    @Test
    fun `cache within 5km is fresh, beyond is stale`() =
        runTest {
            dao.stored = suggestionEntityFixture(latitude = TestCoords.NEAR_4_9_KM_LAT)
            assertThat(repository().call().getOrNull()).isEqualTo("cached suggestion")

            dao.stored = suggestionEntityFixture(latitude = TestCoords.NEAR_5_1_KM_LAT)
            generator.script += FakeSuggestionGenerator.success("fresh")
            assertThat(repository().call().getOrNull()).isEqualTo("fresh")
        }

    @Test
    fun `language change invalidates the cache`() =
        runTest {
            dao.stored = suggestionEntityFixture(language = "en")
            generator.script += FakeSuggestionGenerator.success("taze")

            assertThat(repository().call().getOrNull()).isEqualTo("taze")
        }

    @Test
    fun `forceRefresh bypasses a perfectly fresh cache and clears it first`() =
        runTest {
            dao.stored = suggestionEntityFixture()
            generator.script += FakeSuggestionGenerator.success("forced")

            assertThat(repository().call(forceRefresh = true).getOrNull()).isEqualTo("forced")
            assertThat(dao.deleteAllCount).isEqualTo(1)
        }

    // ---- rate-limit retry -----------------------------------------------------------------------

    @Test
    fun `two rate limits then success retries with exponential backoff on virtual time`() =
        runTest {
            generator.script += FakeSuggestionGenerator.rateLimited()
            generator.script += FakeSuggestionGenerator.rateLimited()
            generator.script += FakeSuggestionGenerator.success("finally")
            val start = currentTime

            val result = repository().call()

            assertThat(result).isEqualTo(ApiResult.Success("finally"))
            assertThat(generator.requests).hasSize(3)
            // 1s after the first failure + 2s after the second = exactly 3000 virtual ms.
            assertThat(currentTime - start).isEqualTo(3_000)
        }

    @Test
    fun `three rate limits exhaust the retries without a fourth attempt`() =
        runTest {
            repeat(3) { generator.script += FakeSuggestionGenerator.rateLimited() }
            val start = currentTime

            val result = repository().call()

            assertThat(result).isInstanceOf(ApiResult.Error.Unknown::class.java)
            assertThat(generator.requests).hasSize(3)
            assertThat(currentTime - start).isEqualTo(3_000) // no delay after the final failure
        }

    @Test
    fun `non-retriable failures do not retry or delay`() =
        runTest {
            generator.script += Result.failure(IllegalStateException("schema mismatch"))
            val start = currentTime

            val result = repository().call()

            assertThat(result).isInstanceOf(ApiResult.Error.Unknown::class.java)
            assertThat(generator.requests).hasSize(1)
            assertThat(currentTime).isEqualTo(start)
        }

    @Test
    fun `cancellation during backoff aborts cleanly without inserting`() =
        runTest {
            generator.script += FakeSuggestionGenerator.rateLimited()
            generator.script += FakeSuggestionGenerator.success("late")
            val repository = repository()

            val job = launch { repository.call() }
            advanceTimeBy(500) // inside the first 1s backoff
            job.cancel()
            advanceUntilIdle()

            assertThat(job.isCancelled).isTrue()
            assertThat(dao.stored).isNull()
            assertThat(generator.requests).hasSize(1)
        }
}
