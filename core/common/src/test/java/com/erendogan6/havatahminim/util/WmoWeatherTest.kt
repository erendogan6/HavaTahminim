package com.erendogan6.havatahminim.util

import com.erendogan6.havatahminim.core.common.R
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WmoWeatherTest {
    @Test
    fun `clear codes map to Clear`() {
        assertThat(WmoWeather.category(0)).isEqualTo("Clear")
        assertThat(WmoWeather.category(1)).isEqualTo("Clear")
    }

    @Test
    fun `cloud codes map to Clouds`() {
        assertThat(WmoWeather.category(2)).isEqualTo("Clouds")
        assertThat(WmoWeather.category(3)).isEqualTo("Clouds")
    }

    @Test
    fun `fog codes map to Fog`() {
        assertThat(WmoWeather.category(45)).isEqualTo("Fog")
        assertThat(WmoWeather.category(48)).isEqualTo("Fog")
    }

    @Test
    fun `drizzle codes map to Drizzle`() {
        listOf(51, 53, 55, 56, 57).forEach { assertThat(WmoWeather.category(it)).isEqualTo("Drizzle") }
    }

    @Test
    fun `rain and shower codes map to Rain`() {
        listOf(61, 63, 65, 66, 67, 80, 81, 82).forEach { assertThat(WmoWeather.category(it)).isEqualTo("Rain") }
    }

    @Test
    fun `snow codes map to Snow`() {
        listOf(71, 73, 75, 77, 85, 86).forEach { assertThat(WmoWeather.category(it)).isEqualTo("Snow") }
    }

    @Test
    fun `thunderstorm codes map to Thunderstorm`() {
        listOf(95, 96, 99).forEach { assertThat(WmoWeather.category(it)).isEqualTo("Thunderstorm") }
    }

    @Test
    fun `unknown code falls back to Clouds`() {
        assertThat(WmoWeather.category(42)).isEqualTo("Clouds")
    }

    @Test
    fun `unknown code falls back to the unknown description`() {
        assertThat(WmoWeather.descriptionRes(42)).isEqualTo(R.string.wmo_unknown)
    }

    @Test
    fun `known codes resolve their own description`() {
        assertThat(WmoWeather.descriptionRes(0)).isEqualTo(R.string.wmo_clear_sky)
        assertThat(WmoWeather.descriptionRes(95)).isEqualTo(R.string.wmo_thunderstorm)
    }
}
