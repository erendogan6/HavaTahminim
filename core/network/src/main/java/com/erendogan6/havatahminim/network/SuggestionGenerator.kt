package com.erendogan6.havatahminim.network

/**
 * The seam over the Gemini SDK: repositories depend on this instead of a concrete model wrapper,
 * so suggestion logic is unit-testable without constructing a real GenerativeModel.
 */
interface SuggestionGenerator {
    /** Returns the generated text, or null when the model returned an empty candidate. */
    suspend fun generate(userMessage: String): String?
}
