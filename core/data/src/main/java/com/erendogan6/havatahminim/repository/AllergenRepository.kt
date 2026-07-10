package com.erendogan6.havatahminim.repository

import com.erendogan6.havatahminim.model.airquality.PollenType
import kotlinx.coroutines.flow.Flow

/** Allergen-preference domain: the user's sensitive-pollen selection, backed by Room. */
interface AllergenRepository {
    /** Cold stream of the user's sensitive-allergen selection, mapped to the domain enum. */
    fun sensitiveAllergensFlow(): Flow<Set<PollenType>>

    /** Allergens the user explicitly marked sensitive; empty means "treat all as relevant". */
    suspend fun sensitiveAllergens(): Set<PollenType>

    /** Best-effort write: a DB failure is logged, never propagated into a UI coroutine. */
    suspend fun setAllergenPreference(
        type: PollenType,
        sensitive: Boolean,
    )
}
