package com.erendogan6.havatahminim.ui.view.component

import androidx.annotation.RawRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.erendogan6.havatahminim.feature.weather.R
import com.erendogan6.havatahminim.ui.theme.HavaTahminimTheme

/**
 * The Today screen's hero condition icon: a looping Lottie animation when one exists for the
 * category/daytime pair, otherwise the static drawable.
 */
@Composable
internal fun WeatherConditionIcon(
    main: String,
    isDayTime: Boolean,
    modifier: Modifier = Modifier,
) {
    val animationRes = weatherAnimationRes(main, isDayTime)
    if (animationRes != null) {
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(animationRes))
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = modifier,
        )
    } else {
        Image(
            painter = painterResource(id = weatherIconRes(main, isDayTime)),
            contentDescription = null,
            modifier = modifier,
        )
    }
}

/** Animated counterpart of [weatherIconRes]; null means no animation exists for the category. */
@RawRes
internal fun weatherAnimationRes(
    main: String,
    isDayTime: Boolean,
): Int? =
    when (main) {
        "Clear" -> if (isDayTime) R.raw.anim_clear_day else R.raw.anim_clear_night
        "Clouds" -> if (isDayTime) R.raw.anim_clouds_day else R.raw.anim_clouds_night
        "Rain", "Drizzle" -> if (isDayTime) R.raw.anim_rain_day else R.raw.anim_rain_night
        "Snow" -> if (isDayTime) R.raw.anim_snow_day else R.raw.anim_snow_night
        "Thunderstorm" -> if (isDayTime) R.raw.anim_thunder_day else R.raw.anim_thunder_night
        "Fog" -> R.raw.anim_fog
        "Mist" -> R.raw.anim_mist
        else -> null
    }

@Preview(showBackground = true, backgroundColor = 0xFF6FA8DC)
@Composable
private fun WeatherConditionIconAnimatedPreview() {
    HavaTahminimTheme(dynamicColor = false) {
        WeatherConditionIcon(main = "Clear", isDayTime = true, modifier = Modifier.size(96.dp))
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF6FA8DC)
@Composable
private fun WeatherConditionIconStaticFallbackPreview() {
    HavaTahminimTheme(dynamicColor = false) {
        WeatherConditionIcon(main = "Unknown", isDayTime = true, modifier = Modifier.size(96.dp))
    }
}
