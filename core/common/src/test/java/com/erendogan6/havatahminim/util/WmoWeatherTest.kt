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
    fun `every known WMO code resolves its own description`() {
        // Exhaustive on purpose: coverage exposed that sampling a few codes left most of this
        // mapping unexercised — a wrong copy-paste in any branch would have passed silently.
        val expected =
            mapOf(
                0 to R.string.wmo_clear_sky,
                1 to R.string.wmo_mainly_clear,
                2 to R.string.wmo_partly_cloudy,
                3 to R.string.wmo_overcast,
                45 to R.string.wmo_fog,
                48 to R.string.wmo_rime_fog,
                51 to R.string.wmo_drizzle_light,
                53 to R.string.wmo_drizzle_moderate,
                55 to R.string.wmo_drizzle_dense,
                56 to R.string.wmo_freezing_drizzle_light,
                57 to R.string.wmo_freezing_drizzle_dense,
                61 to R.string.wmo_rain_slight,
                63 to R.string.wmo_rain_moderate,
                65 to R.string.wmo_rain_heavy,
                66 to R.string.wmo_freezing_rain_light,
                67 to R.string.wmo_freezing_rain_heavy,
                71 to R.string.wmo_snow_slight,
                73 to R.string.wmo_snow_moderate,
                75 to R.string.wmo_snow_heavy,
                77 to R.string.wmo_snow_grains,
                80 to R.string.wmo_rain_showers_slight,
                81 to R.string.wmo_rain_showers_moderate,
                82 to R.string.wmo_rain_showers_violent,
                85 to R.string.wmo_snow_showers_slight,
                86 to R.string.wmo_snow_showers_heavy,
                95 to R.string.wmo_thunderstorm,
                96 to R.string.wmo_thunderstorm_hail_slight,
                99 to R.string.wmo_thunderstorm_hail_heavy,
            )
        expected.forEach { (code, res) ->
            assertThat(WmoWeather.descriptionRes(code)).isEqualTo(res)
        }
    }
}
