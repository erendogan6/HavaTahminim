package com.erendogan6.havatahminim.ui.view.component

import androidx.annotation.DrawableRes
import com.erendogan6.havatahminim.feature.weather.R
import com.erendogan6.havatahminim.model.weather.CurrentForecast.CurrentWeatherBaseResponse
import java.util.Calendar

/** Icon-category to drawable mapping; categories come from [com.erendogan6.havatahminim.util.WmoWeather]. */
@DrawableRes
internal fun weatherIconRes(
    main: String,
    isDayTime: Boolean = true,
): Int =
    when (main) {
        "Clouds" -> if (isDayTime) R.drawable.day_partial_cloud else R.drawable.night_half_moon_partial_cloud
        "Clear" -> if (isDayTime) R.drawable.day_clear else R.drawable.night_half_moon_clear
        "Snow" -> if (isDayTime) R.drawable.day_snow else R.drawable.night_half_moon_snow
        "Rain", "Drizzle" -> if (isDayTime) R.drawable.day_rain else R.drawable.night_half_moon_rain
        "Thunderstorm" -> if (isDayTime) R.drawable.day_rain_thunder else R.drawable.night_half_moon_rain_thunder
        "Fog" -> R.drawable.fog
        "Mist" -> R.drawable.mist
        else -> R.drawable.cloudy
    }

/** Whether "now" falls between this forecast's sunrise and sunset. */
internal fun CurrentWeatherBaseResponse.isDayTime(): Boolean {
    val now = Calendar.getInstance().timeInMillis / 1000
    return now in sys.sunrise..sys.sunset
}
