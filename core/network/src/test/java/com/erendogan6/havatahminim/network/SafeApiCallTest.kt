package com.erendogan6.havatahminim.network

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class SafeApiCallTest {
    @Test
    fun `success wraps the block result`() =
        runTest {
            val result = safeApiCall(StandardTestDispatcher(testScheduler)) { 42 }
            assertThat(result).isEqualTo(ApiResult.Success(42))
        }

    @Test
    fun `IOException maps to Network`() =
        runTest {
            val result = safeApiCall(StandardTestDispatcher(testScheduler)) { throw IOException("offline") }
            assertThat(result).isEqualTo(ApiResult.Error.Network)
        }

    @Test
    fun `HttpException maps to Http with its code`() =
        runTest {
            val http = HttpException(Response.error<Any>(404, "".toResponseBody()))
            val result = safeApiCall(StandardTestDispatcher(testScheduler)) { throw http }
            assertThat(result).isEqualTo(ApiResult.Error.Http(404))
        }

    @Test
    fun `other exceptions map to Unknown preserving the cause`() =
        runTest {
            val boom = IllegalStateException("boom")
            val result = safeApiCall(StandardTestDispatcher(testScheduler)) { throw boom }
            assertThat(result).isInstanceOf(ApiResult.Error.Unknown::class.java)
            assertThat((result as ApiResult.Error.Unknown).cause).isSameInstanceAs(boom)
        }

    @Test
    fun `cancellation is rethrown, never converted to an error`() =
        runTest {
            var materialized: ApiResult<Unit>? = null
            val job =
                launch {
                    materialized = safeApiCall(StandardTestDispatcher(testScheduler)) { awaitCancellation() }
                }
            advanceUntilIdle() // let the call suspend inside the block
            job.cancel()
            advanceUntilIdle()
            assertThat(job.isCancelled).isTrue()
            assertThat(materialized).isNull()
        }

    @Test
    fun `delays inside the block run on virtual time`() =
        runTest {
            val result =
                safeApiCall(StandardTestDispatcher(testScheduler)) {
                    delay(10_000)
                    "done"
                }
            // Completing at all (runTest default timeout is 60s real time) proves the injected
            // dispatcher shares the test scheduler — the seam PR-A exists for.
            assertThat(result).isEqualTo(ApiResult.Success("done"))
        }
}
