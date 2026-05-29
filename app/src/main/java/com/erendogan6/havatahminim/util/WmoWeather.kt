package com.erendogan6.havatahminim.util

import com.erendogan6.havatahminim.R

/**
 * Maps Open-Meteo WMO weather codes
 * (https://open-meteo.com/en/docs -> "Weather variable documentation") onto the two pieces of
 * information the UI needs:
 *
 *  - [category]: a coarse bucket string ("Clear", "Clouds", "Rain"...) that the existing icon
 *    selectors (`getWeatherIcon` / `getWeatherIconn`) already understand. Kept identical to the
 *    old OpenWeather `weather.main` values so the drawable mapping did not have to change.
 *  - [descriptionRes]: a localized string resource describing the condition. Open-Meteo returns no
 *    text description, so we provide our own TR/EN strings.
 */
object WmoWeather {
    fun category(code: Int): String =
        when (code) {
            0, 1 -> "Clear"
            2, 3 -> "Clouds"
            45, 48 -> "Fog"
            51, 53, 55, 56, 57 -> "Drizzle"
            61, 63, 65, 66, 67, 80, 81, 82 -> "Rain"
            71, 73, 75, 77, 85, 86 -> "Snow"
            95, 96, 99 -> "Thunderstorm"
            else -> "Clouds"
        }

    fun descriptionRes(code: Int): Int =
        when (code) {
            0 -> R.string.wmo_clear_sky
            1 -> R.string.wmo_mainly_clear
            2 -> R.string.wmo_partly_cloudy
            3 -> R.string.wmo_overcast
            45 -> R.string.wmo_fog
            48 -> R.string.wmo_rime_fog
            51 -> R.string.wmo_drizzle_light
            53 -> R.string.wmo_drizzle_moderate
            55 -> R.string.wmo_drizzle_dense
            56 -> R.string.wmo_freezing_drizzle_light
            57 -> R.string.wmo_freezing_drizzle_dense
            61 -> R.string.wmo_rain_slight
            63 -> R.string.wmo_rain_moderate
            65 -> R.string.wmo_rain_heavy
            66 -> R.string.wmo_freezing_rain_light
            67 -> R.string.wmo_freezing_rain_heavy
            71 -> R.string.wmo_snow_slight
            73 -> R.string.wmo_snow_moderate
            75 -> R.string.wmo_snow_heavy
            77 -> R.string.wmo_snow_grains
            80 -> R.string.wmo_rain_showers_slight
            81 -> R.string.wmo_rain_showers_moderate
            82 -> R.string.wmo_rain_showers_violent
            85 -> R.string.wmo_snow_showers_slight
            86 -> R.string.wmo_snow_showers_heavy
            95 -> R.string.wmo_thunderstorm
            96 -> R.string.wmo_thunderstorm_hail_slight
            99 -> R.string.wmo_thunderstorm_hail_heavy
            else -> R.string.wmo_unknown
        }
}
