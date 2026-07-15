package com.erendogan6.havatahminim.ui.viewModel

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.erendogan6.havatahminim.network.ApiResult
import com.erendogan6.havatahminim.testing.fixture.TestCoords
import com.erendogan6.havatahminim.testing.fixture.cityFixture
import com.erendogan6.havatahminim.testing.repository.FakeLocationRepository
import com.erendogan6.havatahminim.testing.rule.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class CitySearchViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val locationRepository = FakeLocationRepository()

    private fun viewModel(handle: SavedStateHandle = SavedStateHandle()) =
        CitySearchViewModel(locationRepository, handle)

    @Test
    fun `query restores from saved state`() =
        runTest {
            val viewModel = viewModel(SavedStateHandle(mapOf("city_query" to "istanbul")))

            assertThat(viewModel.query.value).isEqualTo("istanbul")
        }

    @Test
    fun `rapid typing debounces into a single search for the final query`() =
        runTest {
            locationRepository.searchCitiesResult = ApiResult.Success(listOf(cityFixture(name = "İstanbul")))
            val viewModel = viewModel()

            viewModel.cities.test {
                assertThat(awaitItem()).isEmpty()

                viewModel.onQueryChange("ist")
                advanceTimeBy(100)
                viewModel.onQueryChange("istan")
                advanceTimeBy(100)
                viewModel.onQueryChange("istanbul")
                advanceTimeBy(301) // only now does the debounce fire

                assertThat(awaitItem()).containsExactly(cityFixture(name = "İstanbul"))
            }
            assertThat(locationRepository.searchCitiesQueries).containsExactly("istanbul")
        }

    @Test
    fun `queries shorter than three characters never hit the repository`() =
        runTest {
            val viewModel = viewModel()

            viewModel.cities.test {
                assertThat(awaitItem()).isEmpty()
                viewModel.onQueryChange("is")
                advanceUntilIdle()
                expectNoEvents()
            }
            assertThat(locationRepository.searchCitiesQueries).isEmpty()
        }

    @Test
    fun `a search failure degrades to an empty list`() =
        runTest {
            locationRepository.searchCitiesResult = ApiResult.Error.Network
            val viewModel = viewModel()

            viewModel.cities.test {
                assertThat(awaitItem()).isEmpty()
                viewModel.onQueryChange("istanbul")
                advanceUntilIdle()
                expectNoEvents() // emptyList conflates with the initial emptyList
            }
            assertThat(locationRepository.searchCitiesQueries).containsExactly("istanbul")
        }

    @Test
    fun `selectCity points the whole app at the city and persists it`() =
        runTest {
            val viewModel = viewModel()

            viewModel.selectCity(cityFixture())
            advanceUntilIdle()

            assertThat(locationRepository.setActiveLocationCalls)
                .containsExactly(Triple(TestCoords.ISTANBUL_LAT, TestCoords.ISTANBUL_LON, true))
        }
}
