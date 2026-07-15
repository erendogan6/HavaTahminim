package com.erendogan6.havatahminim.network

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

/**
 * Runs [block] on [dispatcher] and maps exceptions to [ApiResult] (IOException to Network,
 * HttpException to Http, the rest to Unknown). [CancellationException] is rethrown.
 * [dispatcher] has no default so tests can't accidentally fall back to the real Dispatchers.IO.
 */
suspend fun <T> safeApiCall(
    dispatcher: CoroutineDispatcher,
    block: suspend () -> T,
): ApiResult<T> =
    withContext(dispatcher) {
        try {
            ApiResult.Success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            ApiResult.Error.Network
        } catch (e: HttpException) {
            ApiResult.Error.Http(e.code())
        } catch (e: Exception) {
            ApiResult.Error.Unknown(e)
        }
    }
