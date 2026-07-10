package com.erendogan6.havatahminim.network

/**
 * The app's common response envelope. Open-Meteo/Gemini return no envelope of their own, so the
 * envelope lives on our side: every repository operation that crosses a process boundary returns
 * an [ApiResult] instead of throwing, and the error taxonomy is typed rather than string-matched.
 */
sealed interface ApiResult<out T> {
    data class Success<T>(
        val data: T,
    ) : ApiResult<T>

    sealed interface Error : ApiResult<Nothing> {
        /** No connectivity / DNS / timeout — anything surfacing as an IOException. */
        data object Network : Error

        /** Server replied non-2xx. */
        data class Http(
            val code: Int,
        ) : Error

        /** Everything else (mapping failures, SDK exceptions, …). */
        data class Unknown(
            val cause: Throwable?,
        ) : Error
    }
}

fun <T> ApiResult<T>.getOrNull(): T? = (this as? ApiResult.Success)?.data

inline fun <T> ApiResult<T>.onSuccess(action: (T) -> Unit): ApiResult<T> {
    if (this is ApiResult.Success) action(data)
    return this
}

inline fun <T> ApiResult<T>.onError(action: (ApiResult.Error) -> Unit): ApiResult<T> {
    if (this is ApiResult.Error) action(this)
    return this
}

inline fun <T, R> ApiResult<T>.map(transform: (T) -> R): ApiResult<R> =
    when (this) {
        is ApiResult.Success -> ApiResult.Success(transform(data))
        is ApiResult.Error -> this
    }
