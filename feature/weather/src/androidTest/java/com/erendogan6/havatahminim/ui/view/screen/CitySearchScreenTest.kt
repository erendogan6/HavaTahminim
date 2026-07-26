package com.erendogan6.havatahminim.ui.view.screen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.erendogan6.havatahminim.feature.weather.R
import com.erendogan6.havatahminim.model.weather.dailyforecast.City
import com.erendogan6.havatahminim.network.ApiResult
import com.erendogan6.havatahminim.testing.fixture.cityFixture
import com.erendogan6.havatahminim.testing.repository.FakeLocationRepository
import com.erendogan6.havatahminim.ui.viewModel.CitySearchViewModel
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val SEARCH_TIMEOUT_MS = 5_000L
private const val IZMIR_LAT = 38.4237
private const val IZMIR_LON = 27.1428

/** City search: the app's one text-input surface (debounce, min length, selection). */
@RunWith(AndroidJUnit4::class)
class CitySearchScreenTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val locationRepository = FakeLocationRepository()
    private val selectedCities = mutableListOf<City>()

    private fun showScreen() {
        val viewModel = CitySearchViewModel(locationRepository, SavedStateHandle())
        rule.setScreen {
            CitySearchScreen(viewModel = viewModel, onCitySelected = { selectedCities += it })
        }
    }

    private fun typeQuery(query: String) {
        rule.onNodeWithText(rule.activity.getString(R.string.city_search)).performTextInput(query)
    }

    /** The debounce means "search happened" is a coroutine outcome, not a Compose idle state. */
    private fun awaitSearch() {
        rule.waitUntil(SEARCH_TIMEOUT_MS) { locationRepository.searchCitiesQueries.isNotEmpty() }
    }

    @Test
    fun onlyTheCompletedQueryIsSearched() {
        locationRepository.searchCitiesResult = ApiResult.Success(listOf(cityFixture(name = "Istanbul")))
        showScreen()

        typeQuery("Is") // below the minimum length
        typeQuery("tanbul")
        awaitSearch()

        // Debounce coalesces the keystrokes and the short prefix never reaches the repository.
        assertThat(locationRepository.searchCitiesQueries).containsExactly("Istanbul")
    }

    @Test
    fun aValidQueryListsTheMatchingCities() {
        locationRepository.searchCitiesResult =
            ApiResult.Success(
                listOf(
                    cityFixture(name = "Istanbul"),
                    cityFixture(name = "Izmir", latitude = IZMIR_LAT, longitude = IZMIR_LON),
                ),
            )
        showScreen()

        typeQuery("Izm")

        rule.awaitText("Izmir")
        rule.onNodeWithText("Istanbul").assertExists()
    }

    @Test
    fun selectingACityReportsItAndPersistsIt() {
        locationRepository.searchCitiesResult = ApiResult.Success(listOf(cityFixture(name = "Istanbul")))
        showScreen()

        typeQuery("Ista")
        rule.awaitText("Istanbul")
        rule.onNodeWithText("Istanbul").performClick()
        rule.waitForIdle()

        assertThat(selectedCities.map { it.name }).containsExactly("Istanbul")
        assertThat(locationRepository.setActiveLocationCalls.single().third).isTrue()
    }

    @Test
    fun aFailingSearchLeavesTheListEmptyWithoutCrashing() {
        locationRepository.searchCitiesResult = ApiResult.Error.Network
        showScreen()

        typeQuery("Ista")
        awaitSearch()
        rule.waitForIdle()

        rule.onNodeWithText("Istanbul").assertDoesNotExist()
    }
}
