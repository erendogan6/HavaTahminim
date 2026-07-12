package com.erendogan6.havatahminim.ui.viewModel

import app.cash.turbine.test
import com.erendogan6.havatahminim.testing.fixture.currentWeatherFixture
import com.erendogan6.havatahminim.testing.fixture.locationEntityFixture
import com.erendogan6.havatahminim.testing.repository.FakeLocationRepository
import com.erendogan6.havatahminim.testing.repository.FakeWeatherRepository
import com.erendogan6.havatahminim.testing.rule.MainDispatcherRule
import com.erendogan6.havatahminim.util.DeviceLocation
import com.erendogan6.havatahminim.util.FakeConnectivityChecker
import com.erendogan6.havatahminim.util.FakeDeviceLocationSource
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class MainViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val locationRepository = FakeLocationRepository()
    private val weatherRepository = FakeWeatherRepository()
    private val deviceLocationSource = FakeDeviceLocationSource()
    private val connectivity = FakeConnectivityChecker(online = true)

    private fun viewModel() =
        MainViewModel(locationRepository, deviceLocationSource, connectivity, weatherRepository)

    private companion object {
        val ISTANBUL = Triple(41.0082, 28.9784, false)
    }

    @Test
    fun `currentWeather mirrors the repository's shared state`() =
        runTest {
            val weather = currentWeatherFixture()
            val viewModel = viewModel()

            viewModel.currentWeather.test {
                assertThat(awaitItem()).isNull()
                weatherRepository.currentWeatherState.value = weather
                assertThat(awaitItem()).isEqualTo(weather)
            }
        }

    // --- Startup: permission granted -----------------------------------------------------------

    @Test
    fun `granted, online with a fix - sets that location and persists it`() =
        runTest {
            deviceLocationSource.fix = DeviceLocation(10.0, 20.0)

            viewModel().onLocationPermissionGranted()
            advanceUntilIdle()

            assertThat(locationRepository.setActiveLocationCalls).containsExactly(Triple(10.0, 20.0, true))
            assertThat(locationRepository.activeLocation.value).isEqualTo(locationEntityFixture(10.0, 20.0))
        }

    @Test
    fun `granted, online without a fix - falls back to the saved location`() =
        runTest {
            deviceLocationSource.fix = null
            locationRepository.savedLocation = locationEntityFixture(1.0, 2.0)

            viewModel().onLocationPermissionGranted()
            advanceUntilIdle()

            assertThat(locationRepository.activeLocation.value).isEqualTo(locationEntityFixture(1.0, 2.0))
            assertThat(locationRepository.setActiveLocationCalls).isEmpty() // no Istanbul fallback
        }

    @Test
    fun `granted, online, no fix and no saved location - falls back to Istanbul unpersisted`() =
        runTest {
            deviceLocationSource.fix = null
            locationRepository.savedLocation = null

            viewModel().onLocationPermissionGranted()
            advanceUntilIdle()

            assertThat(locationRepository.setActiveLocationCalls).containsExactly(ISTANBUL)
        }

    @Test
    fun `granted but offline - skips GPS, shows the offline dialog, seeds a fallback`() =
        runTest {
            connectivity.online = false
            locationRepository.savedLocation = null
            val viewModel = viewModel()

            viewModel.onLocationPermissionGranted()
            advanceUntilIdle()

            assertThat(deviceLocationSource.callCount).isEqualTo(0) // never touched the GPS provider
            assertThat(viewModel.showNoInternetDialog.value).isTrue()
            assertThat(locationRepository.setActiveLocationCalls).containsExactly(ISTANBUL)
        }

    // --- Startup: permission denied ------------------------------------------------------------

    @Test
    fun `denied - starts from the saved location without touching GPS`() =
        runTest {
            locationRepository.savedLocation = locationEntityFixture(1.0, 2.0)

            viewModel().onLocationPermissionDenied()
            advanceUntilIdle()

            assertThat(deviceLocationSource.callCount).isEqualTo(0)
            assertThat(locationRepository.activeLocation.value).isEqualTo(locationEntityFixture(1.0, 2.0))
            assertThat(locationRepository.setActiveLocationCalls).isEmpty()
        }

    @Test
    fun `denied, offline, no saved location - Istanbul fallback and the offline dialog`() =
        runTest {
            connectivity.online = false
            locationRepository.savedLocation = null
            val viewModel = viewModel()

            viewModel.onLocationPermissionDenied()
            advanceUntilIdle()

            assertThat(viewModel.showNoInternetDialog.value).isTrue()
            assertThat(locationRepository.setActiveLocationCalls).containsExactly(ISTANBUL)
        }

    // --- "Use my location" action --------------------------------------------------------------

    @Test
    fun `useCurrentLocation online with a fix - persists it`() =
        runTest {
            deviceLocationSource.fix = DeviceLocation(3.0, 4.0)

            viewModel().useCurrentLocation()
            advanceUntilIdle()

            assertThat(locationRepository.setActiveLocationCalls).containsExactly(Triple(3.0, 4.0, true))
        }

    @Test
    fun `useCurrentLocation offline - shows the dialog and never queries GPS`() =
        runTest {
            connectivity.online = false
            val viewModel = viewModel()

            viewModel.useCurrentLocation()
            advanceUntilIdle()

            assertThat(viewModel.showNoInternetDialog.value).isTrue()
            assertThat(deviceLocationSource.callCount).isEqualTo(0)
            assertThat(locationRepository.setActiveLocationCalls).isEmpty()
        }

    @Test
    fun `useCurrentLocation online but no fix - does nothing`() =
        runTest {
            deviceLocationSource.fix = null
            val viewModel = viewModel()

            viewModel.useCurrentLocation()
            advanceUntilIdle()

            assertThat(locationRepository.setActiveLocationCalls).isEmpty()
            assertThat(viewModel.showNoInternetDialog.value).isFalse()
        }

    @Test
    fun `dismissNoInternetDialog clears the flag`() =
        runTest {
            connectivity.online = false
            val viewModel = viewModel()
            viewModel.useCurrentLocation()
            advanceUntilIdle()
            assertThat(viewModel.showNoInternetDialog.value).isTrue()

            viewModel.dismissNoInternetDialog()

            assertThat(viewModel.showNoInternetDialog.value).isFalse()
        }
}
