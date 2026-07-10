package com.erendogan6.havatahminim.network

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

/**
 * The single place exceptions become [ApiResult]s. Runs [block] on IO, maps the failure taxonomy
 * (IOException → Network, HttpException → Http, rest → Unknown) and — critically — rethrows
 * [CancellationException] so a cancelled pipeline is never mistaken for an error.
 */
suspend fun <T> safeApiCall(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
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
