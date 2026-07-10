package com.erendogan6.havatahminim.repository

import com.erendogan6.havatahminim.model.weather.openmeteo.GeoSearchResponse
import com.erendogan6.havatahminim.network.ApiResult
import com.erendogan6.havatahminim.network.getOrNull
import com.erendogan6.havatahminim.testing.dao.FakeLocationDao
import com.erendogan6.havatahminim.testing.fixture.TestCoords
import com.erendogan6.havatahminim.testing.fixture.cityFixture
import com.erendogan6.havatahminim.testing.fixture.locationEntityFixture
import com.erendogan6.havatahminim.testing.service.FakeCityApiService
import com.erendogan6.havatahminim.testing.util.FakeResourcesProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.IOException

class LocationRepositoryImplTest {
    private val dao = FakeLocationDao()
    private val cityApi = FakeCityApiService()
    private val resources = FakeResourcesProvider(currentLanguage = "tr")

    private val geocoder =
        object : ReverseGeocoder {
            var lastRequest: Triple<Double, Double, String>? = null

            override fun resolve(
                lat: Double,
                lon: Double,
                language: String,
            ): String {
                lastRequest = Triple(lat, lon, language)
                return "Kadıköy"
            }
        }

    private fun TestScope.repository() =
        LocationRepositoryImpl(
            locationDao = dao,
            cityApiService = cityApi,
            resourcesProvider = resources,
            reverseGeocoder = geocoder,
            ioDispatcher = StandardTestDispatcher(testScheduler),
        )

    @Test
    fun `startFromSavedLocation seeds the active location from the dao`() =
        runTest {
            dao.stored = locationEntityFixture()
            val repository = repository()

            repository.startFromSavedLocation()

            assertThat(repository.activeLocation.value?.latitude).isEqualTo(TestCoords.ISTANBUL_LAT)
        }

    @Test
    fun `startFromSavedLocation is a no-op when a location is already active`() =
        runTest {
            val repository = repository()
            repository.setActiveLocation(1.0, 2.0, persist = false)
            dao.stored = locationEntityFixture()

            repository.startFromSavedLocation()

            assertThat(repository.activeLocation.value?.latitude).isEqualTo(1.0)
        }

    @Test
    fun `a dao read failure leaves the active location null instead of crashing`() =
        runTest {
            val failingDao =
                object : com.erendogan6.havatahminim.model.LocationDao {
                    override suspend fun getLocation() = throw IllegalStateException("db corrupt")

                    override suspend fun insertLocation(location: com.erendogan6.havatahminim.model.entity.LocationEntity) = 0L
                }
            val repository =
                LocationRepositoryImpl(
                    locationDao = failingDao,
                    cityApiService = cityApi,
                    resourcesProvider = resources,
                    reverseGeocoder = geocoder,
                    ioDispatcher = StandardTestDispatcher(testScheduler),
                )

            repository.startFromSavedLocation()

            assertThat(repository.activeLocation.value).isNull()
        }

    @Test
    fun `setActiveLocation with persist writes through to the dao`() =
        runTest {
            val repository = repository()

            repository.setActiveLocation(TestCoords.ANKARA_LAT, TestCoords.ANKARA_LON, persist = true)

            assertThat(repository.activeLocation.value?.latitude).isEqualTo(TestCoords.ANKARA_LAT)
            assertThat(dao.stored?.latitude).isEqualTo(TestCoords.ANKARA_LAT)
        }

    @Test
    fun `setActiveLocation without persist leaves the dao untouched`() =
        runTest {
            val repository = repository()

            repository.setActiveLocation(TestCoords.ANKARA_LAT, TestCoords.ANKARA_LON, persist = false)

            assertThat(repository.activeLocation.value?.latitude).isEqualTo(TestCoords.ANKARA_LAT)
            assertThat(dao.stored).isNull()
        }

    @Test
    fun `persistence is best-effort — an insert failure still updates the session location`() =
        runTest {
            dao.insertError = IllegalStateException("disk full")
            val repository = repository()

            repository.setActiveLocation(TestCoords.ANKARA_LAT, TestCoords.ANKARA_LON, persist = true)

            assertThat(repository.activeLocation.value?.latitude).isEqualTo(TestCoords.ANKARA_LAT)
        }

    @Test
    fun `searchCities returns results and forwards the current language`() =
        runTest {
            cityApi.response = GeoSearchResponse(results = listOf(cityFixture(name = "İstanbul")))

            val result = repository().searchCities("ist")

            assertThat(result.getOrNull()!!.first().name).isEqualTo("İstanbul")
            assertThat(cityApi.requests.single()).isEqualTo("ist" to "tr")
        }

    @Test
    fun `null results collapse to an empty list`() =
        runTest {
            cityApi.response = GeoSearchResponse(results = null)

            assertThat(repository().searchCities("x").getOrNull()).isEmpty()
        }

    @Test
    fun `search network failure maps to the Network error`() =
        runTest {
            cityApi.nextError = IOException("offline")

            assertThat(repository().searchCities("ist")).isEqualTo(ApiResult.Error.Network)
        }

    @Test
    fun `resolveLocationName delegates to the reverse geocoder with the current language`() =
        runTest {
            val name = repository().resolveLocationName(1.0, 2.0)

            assertThat(name).isEqualTo("Kadıköy")
            assertThat(geocoder.lastRequest).isEqualTo(Triple(1.0, 2.0, "tr"))
        }
}
