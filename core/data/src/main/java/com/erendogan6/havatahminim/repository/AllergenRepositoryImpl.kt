package com.erendogan6.havatahminim.repository

import com.erendogan6.havatahminim.core.common.di.IoDispatcher
import com.erendogan6.havatahminim.model.airquality.PollenType
import com.erendogan6.havatahminim.model.entity.AllergenPreferenceEntity
import com.erendogan6.havatahminim.room.AllergenPreferenceDao
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

/** Maps the Room rows to [PollenType]s; writes are best-effort (logged, never thrown). */
@Singleton
class AllergenRepositoryImpl
    @Inject
    constructor(
        private val allergenPreferenceDao: AllergenPreferenceDao,
        @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : AllergenRepository {
        override fun sensitiveAllergensFlow(): Flow<Set<PollenType>> =
            allergenPreferenceDao.getAll().map { prefs ->
                prefs
                    .filter { it.sensitive }
                    .mapNotNull { runCatching { PollenType.valueOf(it.type) }.getOrNull() }
                    .toSet()
            }

        override suspend fun sensitiveAllergens(): Set<PollenType> =
            withContext(ioDispatcher) {
                allergenPreferenceDao
                    .getSensitive()
                    .mapNotNull { runCatching { PollenType.valueOf(it.type) }.getOrNull() }
                    .toSet()
            }

        override suspend fun setAllergenPreference(
            type: PollenType,
            sensitive: Boolean,
        ) {
            withContext(ioDispatcher) {
                runCatching { allergenPreferenceDao.setPreference(AllergenPreferenceEntity(type.name, sensitive)) }
                    .onFailure { Timber.e(it, "Failed to persist allergen preference") }
            }
        }
    }
