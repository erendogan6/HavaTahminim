package com.erendogan6.havatahminim.network

import com.erendogan6.havatahminim.core.data.R
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ApiErrorMessagesTest {
    private val fallback = R.string.error_fetching_weather_data

    @Test
    fun `network error maps to the no-internet message`() {
        assertThat(ApiResult.Error.Network.userMessageRes(fallback))
            .isEqualTo(R.string.error_no_internet)
    }

    @Test
    fun `429 maps to the rate-limited message`() {
        assertThat(ApiResult.Error.Http(429).userMessageRes(fallback))
            .isEqualTo(R.string.error_rate_limited)
    }

    @Test
    fun `server errors map to the service-unavailable message`() {
        assertThat(ApiResult.Error.Http(500).userMessageRes(fallback))
            .isEqualTo(R.string.error_server)
        assertThat(ApiResult.Error.Http(503).userMessageRes(fallback))
            .isEqualTo(R.string.error_server)
    }

    @Test
    fun `other http codes use the caller's context fallback`() {
        assertThat(ApiResult.Error.Http(404).userMessageRes(fallback)).isEqualTo(fallback)
        assertThat(ApiResult.Error.Http(400).userMessageRes(fallback)).isEqualTo(fallback)
    }

    @Test
    fun `unknown errors use the caller's context fallback`() {
        assertThat(ApiResult.Error.Unknown(IllegalStateException()).userMessageRes(fallback))
            .isEqualTo(fallback)
        assertThat(ApiResult.Error.Unknown(null).userMessageRes(fallback)).isEqualTo(fallback)
    }
}
