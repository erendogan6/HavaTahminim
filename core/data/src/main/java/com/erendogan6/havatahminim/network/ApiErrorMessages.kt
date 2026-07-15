package com.erendogan6.havatahminim.network

import androidx.annotation.StringRes
import com.erendogan6.havatahminim.core.data.R

/**
 * The single place the typed error taxonomy becomes a user-facing message — the last mile of the
 * [ApiResult] envelope. Taxonomy cases with an app-wide meaning map globally (no connection,
 * rate-limited, server down); everything else falls back to the screen's own context message
 * ("couldn't fetch the daily forecast"), which callers pass in.
 *
 * Lives in `:core:data` next to the shared error strings it resolves to. ViewModels call this
 * instead of hand-picking an error string, so a Network failure reads as "check your connection"
 * on every screen — never as a generic fetch error.
 */
@StringRes
fun ApiResult.Error.userMessageRes(
    @StringRes fallbackRes: Int,
): Int =
    when (this) {
        ApiResult.Error.Network -> R.string.error_no_internet
        is ApiResult.Error.Http ->
            when {
                code == HTTP_TOO_MANY_REQUESTS -> R.string.error_rate_limited
                code >= HTTP_SERVER_ERROR_MIN -> R.string.error_server
                else -> fallbackRes
            }
        is ApiResult.Error.Unknown -> fallbackRes
    }

private const val HTTP_TOO_MANY_REQUESTS = 429
private const val HTTP_SERVER_ERROR_MIN = 500
