package com.erendogan6.havatahminim.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme =
    lightColorScheme(
        primary = Palette.SkyMist,
    )

/** Accessor for the app-specific tokens, mirroring `MaterialTheme.colorScheme` ergonomics. */
object WeatherTheme {
    val colors: WeatherColors
        @Composable
        @ReadOnlyComposable
        get() = LocalWeatherColors.current
}

@Composable
fun HavaTahminimTheme(
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val context = LocalContext.current
            dynamicLightColorScheme(context)
        } else {
            LightColorScheme
        }

    CompositionLocalProvider(LocalWeatherColors provides LightWeatherColors) {
        MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
    }
}
