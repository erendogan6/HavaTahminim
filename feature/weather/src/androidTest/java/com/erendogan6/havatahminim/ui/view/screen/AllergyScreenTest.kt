package com.erendogan6.havatahminim.ui.view.screen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.erendogan6.havatahminim.model.airquality.PollenType
import com.erendogan6.havatahminim.network.ApiResult
import com.erendogan6.havatahminim.testing.fixture.airQualityInfoFixture
import com.erendogan6.havatahminim.testing.fixture.locationEntityFixture
import com.erendogan6.havatahminim.testing.repository.FakeAirQualityRepository
import com.erendogan6.havatahminim.testing.repository.FakeAllergenRepository
import com.erendogan6.havatahminim.testing.repository.FakeLocationRepository
import com.erendogan6.havatahminim.ui.viewModel.AllergyViewModel
import com.erendogan6.havatahminim.util.PollenLevel
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Allergy screen: the allergen selector is the app's only persisted user preference. */
@RunWith(AndroidJUnit4::class)
class AllergyScreenTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val locationRepository = FakeLocationRepository()
    private val airQualityRepository = FakeAirQualityRepository()
    private val allergenRepository = FakeAllergenRepository()

    /** Pollen sections are switched off so the only grass label on screen is the filter chip. */
    private fun seedAirQuality() {
        airQualityRepository.result =
            ApiResult.Success(airQualityInfoFixture(pollen = emptyList(), pollenAvailable = false))
        locationRepository.activeLocationState.value = locationEntityFixture()
    }

    private fun showScreen() {
        val viewModel = AllergyViewModel(locationRepository, airQualityRepository, allergenRepository)
        rule.setScreen { AllergyScreen(viewModel = viewModel) }
    }

    private fun grassLabel() = rule.activity.getString(PollenLevel.typeNameRes(PollenType.GRASS))

    @Test
    fun theSelectorIsShownOnceAirQualityArrives() {
        seedAirQuality()

        showScreen()

        rule.awaitText(grassLabel())
    }

    @Test
    fun togglingAnAllergenPersistsThePreference() {
        seedAirQuality()

        showScreen()
        rule.awaitText(grassLabel())
        rule.onNodeWithText(grassLabel()).performClick()
        rule.waitForIdle()

        assertThat(allergenRepository.setPreferenceCalls).containsExactly(PollenType.GRASS to true)
        assertThat(allergenRepository.prefs.value).containsExactly(PollenType.GRASS)
    }

    @Test
    fun aFailingAirQualityCallKeepsTheSplashInsteadOfCrashing() {
        airQualityRepository.result = ApiResult.Error.Network
        locationRepository.activeLocationState.value = locationEntityFixture()

        showScreen()
        rule.waitForIdle()

        rule.onNodeWithText(grassLabel()).assertDoesNotExist()
    }
}
