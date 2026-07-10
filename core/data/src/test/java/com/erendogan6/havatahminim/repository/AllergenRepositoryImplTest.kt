package com.erendogan6.havatahminim.repository

import com.erendogan6.havatahminim.model.airquality.PollenType
import com.erendogan6.havatahminim.model.entity.AllergenPreferenceEntity
import com.erendogan6.havatahminim.testing.dao.FakeAllergenPreferenceDao
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AllergenRepositoryImplTest {
    private val dao = FakeAllergenPreferenceDao()

    private fun TestScope.repository() =
        AllergenRepositoryImpl(
            allergenPreferenceDao = dao,
            ioDispatcher = StandardTestDispatcher(testScheduler),
        )

    @Test
    fun `flow maps sensitive rows to the enum set`() =
        runTest {
            dao.seed(
                AllergenPreferenceEntity("GRASS", sensitive = true),
                AllergenPreferenceEntity("BIRCH", sensitive = false),
                AllergenPreferenceEntity("OLIVE", sensitive = true),
            )

            val set = repository().sensitiveAllergensFlow().first()

            assertThat(set).containsExactly(PollenType.GRASS, PollenType.OLIVE)
        }

    @Test
    fun `unknown enum names are dropped instead of crashing`() =
        runTest {
            dao.seed(
                AllergenPreferenceEntity("NOT_A_POLLEN", sensitive = true),
                AllergenPreferenceEntity("GRASS", sensitive = true),
            )

            assertThat(repository().sensitiveAllergensFlow().first()).containsExactly(PollenType.GRASS)
        }

    @Test
    fun `suspend variant matches the flow`() =
        runTest {
            dao.seed(AllergenPreferenceEntity("RAGWEED", sensitive = true))

            assertThat(repository().sensitiveAllergens()).containsExactly(PollenType.RAGWEED)
        }

    @Test
    fun `setAllergenPreference writes through`() =
        runTest {
            val repository = repository()

            repository.setAllergenPreference(PollenType.MUGWORT, sensitive = true)

            assertThat(repository.sensitiveAllergens()).containsExactly(PollenType.MUGWORT)
        }

    @Test
    fun `a write failure is swallowed as best-effort`() =
        runTest {
            dao.setError = IllegalStateException("disk full")
            val repository = repository()

            repository.setAllergenPreference(PollenType.MUGWORT, sensitive = true) // must not throw

            assertThat(repository.sensitiveAllergens()).isEmpty()
        }
}
