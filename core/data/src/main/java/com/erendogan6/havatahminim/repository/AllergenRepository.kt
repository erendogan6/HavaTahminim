package com.erendogan6.havatahminim.repository

import android.util.Log
import com.erendogan6.havatahminim.model.airquality.PollenType
import com.erendogan6.havatahminim.model.entity.AllergenPreferenceEntity
import com.erendogan6.havatahminim.room.AllergenPreferenceDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/** Allergen-preference domain: the user's sensitive-pollen selection, backed by Room. */
@Singleton
class AllergenRepository
    @Inject
    constructor(
        private val allergenPreferenceDao: AllergenPreferenceDao,
    ) {
        /** Cold stream of the user's sensitive-allergen selection, mapped to the domain enum. */
        fun sensitiveAllergensFlow(): Flow<Set<PollenType>> =
            allergenPreferenceDao.getAll().map { prefs ->
                prefs
                    .filter { it.sensitive }
                    .mapNotNull { runCatching { PollenType.valueOf(it.type) }.getOrNull() }
                    .toSet()
            }

        /** Allergens the user explicitly marked sensitive; empty means "treat all as relevant". */
        suspend fun sensitiveAllergens(): Set<PollenType> =
            withContext(Dispatchers.IO) {
                allergenPreferenceDao
                    .getSensitive()
                    .mapNotNull { runCatching { PollenType.valueOf(it.type) }.getOrNull() }
                    .toSet()
            }

        /** Best-effort write: a DB failure is logged, never propagated into a UI coroutine. */
        suspend fun setAllergenPreference(
            type: PollenType,
            sensitive: Boolean,
        ) {
            withContext(Dispatchers.IO) {
                runCatching { allergenPreferenceDao.setPreference(AllergenPreferenceEntity(type.name, sensitive)) }
                    .onFailure { Log.e(TAG, "Failed to persist allergen preference", it) }
            }
        }

        private companion object {
            const val TAG = "AllergenRepository"
        }
    }
