package com.erendogan6.havatahminim.repository

import com.erendogan6.havatahminim.network.ApiResult

/**
 * ZekAI suggestion domain: the Gemini call plus its Room cache. The cache is invalidated when the
 * location moves beyond 5km, the entry is older than 2h, the device language changes, or the
 * caller forces a refresh (allergen selection changed).
 */
interface SuggestionRepository {
    suspend fun getSuggestions(
        lat: Double,
        lon: Double,
        location: String,
        temperature: String,
        pollenSummary: String = "",
        forceRefresh: Boolean = false,
    ): ApiResult<String>
}
