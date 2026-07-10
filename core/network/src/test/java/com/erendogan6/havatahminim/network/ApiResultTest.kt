package com.erendogan6.havatahminim.network

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ApiResultTest {
    @Test
    fun `getOrNull returns data for success and null for errors`() {
        assertThat(ApiResult.Success("x").getOrNull()).isEqualTo("x")
        assertThat((ApiResult.Error.Network as ApiResult<String>).getOrNull()).isNull()
    }

    @Test
    fun `onSuccess fires only for success and returns the receiver`() {
        var seen: String? = null
        val success: ApiResult<String> = ApiResult.Success("x")
        assertThat(success.onSuccess { seen = it }).isSameInstanceAs(success)
        assertThat(seen).isEqualTo("x")

        seen = null
        val error: ApiResult<String> = ApiResult.Error.Http(500)
        error.onSuccess { seen = it }
        assertThat(seen).isNull()
    }

    @Test
    fun `onError fires only for errors and returns the receiver`() {
        var seen: ApiResult.Error? = null
        val error: ApiResult<String> = ApiResult.Error.Http(500)
        assertThat(error.onError { seen = it }).isSameInstanceAs(error)
        assertThat(seen).isEqualTo(ApiResult.Error.Http(500))

        seen = null
        ApiResult.Success("x").onError { seen = it }
        assertThat(seen).isNull()
    }

    @Test
    fun `map transforms success and passes errors through unchanged`() {
        assertThat(ApiResult.Success(2).map { it * 10 }).isEqualTo(ApiResult.Success(20))
        val error: ApiResult<Int> = ApiResult.Error.Network
        assertThat(error.map { it * 10 }).isSameInstanceAs(error)
    }
}
