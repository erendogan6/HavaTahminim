package com.erendogan6.havatahminim.ui.viewModel

import app.cash.turbine.test
import com.erendogan6.havatahminim.model.airquality.PollenType
import com.erendogan6.havatahminim.network.ApiResult
import com.erendogan6.havatahminim.testing.fixture.airQualityInfoFixture
import com.erendogan6.havatahminim.testing.fixture.locationEntityFixture
import com.erendogan6.havatahminim.testing.repository.FakeAirQualityRepository
import com.erendogan6.havatahminim.testing.repository.FakeAllergenRepository
import com.erendogan6.havatahminim.testing.repository.FakeLocationRepository
import com.erendogan6.havatahminim.testing.rule.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class AllergyViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val locationRepository = FakeLocationRepository()
    private val airQualityRepository = FakeAirQualityRepository()
    private val allergenRepository = FakeAllergenRepository()

    private fun viewModel() = AllergyViewModel(locationRepository, airQualityRepository, allergenRepository)

    @Test
    fun `allergen prefs mirror the repository flow and live updates`() =
        runTest {
            allergenRepository.prefs.value = setOf(PollenType.GRASS)

            viewModel().allergenPrefs.test {
                assertThat(awaitItem()).isEmpty() // stateIn initialValue
                assertThat(awaitItem()).containsExactly(PollenType.GRASS)
                allergenRepository.prefs.value = setOf(PollenType.GRASS, PollenType.BIRCH)
                assertThat(awaitItem()).containsExactly(PollenType.GRASS, PollenType.BIRCH)
            }
        }

    @Test
    fun `a failing prefs flow degrades to an empty selection`() =
        runTest {
            allergenRepository.flowError = IllegalStateException("db gone")

            viewModel().allergenPrefs.test {
                assertThat(awaitItem()).isEmpty()
                expectNoEvents() // catch emitted the fallback; no crash
            }
        }

    @Test
    fun `air quality loads for the active location`() =
        runTest {
            val info = airQualityInfoFixture()
            airQualityRepository.result = ApiResult.Success(info)
            locationRepository.activeLocationState.value = locationEntityFixture()

            viewModel().airQuality.test {
                assertThat(awaitItem()).isNull() // initialValue before the fetch resolves
                assertThat(awaitItem()).isEqualTo(info)
            }
        }

    @Test
    fun `an air quality failure pins null even after previous data`() =
        runTest {
            // Deliberate pin: on a location change whose fetch fails, mapLatest emits getOrNull() =
            // null — the screen falls back to the splash rather than showing stale data for the
            // wrong location.
            val info = airQualityInfoFixture()
            airQualityRepository.result = ApiResult.Success(info)
            locationRepository.activeLocationState.value = locationEntityFixture()

            viewModel().airQuality.test {
                skipItems(1)
                assertThat(awaitItem()).isEqualTo(info)

                airQualityRepository.result = ApiResult.Error.Network
                locationRepository.activeLocationState.value = locationEntityFixture(latitude = 39.0)

                assertThat(awaitItem()).isNull()
            }
        }

    @Test
    fun `toggleAllergen delegates to the repository`() =
        runTest {
            val viewModel = viewModel()

            viewModel.allergenPrefs.test {
                viewModel.toggleAllergen(PollenType.OLIVE, sensitive = true)
                skipItems(1) // initial emptySet
                assertThat(awaitItem()).containsExactly(PollenType.OLIVE)
            }
            assertThat(allergenRepository.setPreferenceCalls).containsExactly(PollenType.OLIVE to true)
        }
}
