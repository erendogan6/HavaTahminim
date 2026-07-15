package com.erendogan6.havatahminim.network

/** Gemini behind an interface, for testability. */
interface SuggestionGenerator {
    /** Returns the generated text, or null when the model returned an empty candidate. */
    suspend fun generate(userMessage: String): String?
}
