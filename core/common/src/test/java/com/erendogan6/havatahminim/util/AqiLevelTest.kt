package com.erendogan6.havatahminim.util

import com.erendogan6.havatahminim.core.common.R
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AqiLevelTest {
    @Test
    fun `null maps to unknown`() {
        assertThat(AqiLevel.labelRes(null)).isEqualTo(R.string.aqi_unknown)
    }

    @Test
    fun `band edges are inclusive on the upper bound`() {
        assertThat(AqiLevel.labelRes(20)).isEqualTo(R.string.aqi_good)
        assertThat(AqiLevel.labelRes(21)).isEqualTo(R.string.aqi_fair)
        assertThat(AqiLevel.labelRes(40)).isEqualTo(R.string.aqi_fair)
        assertThat(AqiLevel.labelRes(41)).isEqualTo(R.string.aqi_moderate)
        assertThat(AqiLevel.labelRes(60)).isEqualTo(R.string.aqi_moderate)
        assertThat(AqiLevel.labelRes(61)).isEqualTo(R.string.aqi_poor)
        assertThat(AqiLevel.labelRes(80)).isEqualTo(R.string.aqi_poor)
        assertThat(AqiLevel.labelRes(81)).isEqualTo(R.string.aqi_very_poor)
        assertThat(AqiLevel.labelRes(100)).isEqualTo(R.string.aqi_very_poor)
        assertThat(AqiLevel.labelRes(101)).isEqualTo(R.string.aqi_extremely_poor)
    }
}
