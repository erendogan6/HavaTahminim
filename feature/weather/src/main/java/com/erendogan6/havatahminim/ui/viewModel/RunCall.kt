package com.erendogan6.havatahminim.ui.viewModel

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

private const val MAX_RETRIES = 3
private const val RETRY_BASE_DELAY_MS = 1_000L
private const val GEMINI_RATE_LIMIT_MARKER = "RESOURCE_EXHAUSTED"

/**
 * Runs [call], retrying with exponential backoff (1s, 2s, 4s) only for Gemini's
 * RESOURCE_EXHAUSTED rate-limit errors. CancellationException is rethrown, never treated as a
 * failure — a cancelled pipeline must not surface an error or trigger a retry.
 */
internal suspend fun <T> runCall(call: suspend () -> T): Result<T> {
    var attempt = 0
    while (true) {
        try {
            return Result.success(call())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val retriable =
                attempt < MAX_RETRIES - 1 &&
                    e.message?.contains(GEMINI_RATE_LIMIT_MARKER) == true
            if (!retriable) return Result.failure(e)
            attempt++
            delay(RETRY_BASE_DELAY_MS shl (attempt - 1))
        }
    }
}
