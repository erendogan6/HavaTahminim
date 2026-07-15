package com.erendogan6.havatahminim.network

import androidx.annotation.StringRes
import com.erendogan6.havatahminim.core.data.R

/**
 * Maps the error taxonomy to a user-facing message. Errors with an app-wide meaning (offline,
 * rate-limited, server down) map to shared strings; everything else uses the screen's own
 * [fallbackRes].
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
