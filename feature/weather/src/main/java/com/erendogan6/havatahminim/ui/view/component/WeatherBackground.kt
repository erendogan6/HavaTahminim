package com.erendogan6.havatahminim.ui.view.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.erendogan6.havatahminim.feature.weather.R
import com.erendogan6.havatahminim.model.weather.CurrentForecast.CurrentWeatherBaseResponse

/**
 * The full-bleed photo backdrop drawn once behind the transparent Scaffold in MainActivity
 * (public: it is part of the feature's cross-module surface).
 */
@Composable
fun BackgroundImage(
    weatherState: CurrentWeatherBaseResponse?,
    modifier: Modifier = Modifier,
) {
    val backgroundImage = if (weatherState != null) R.drawable.aydinlik else R.drawable.splash
    val alpha = if (weatherState != null) 0.5f else 0.7f

    Image(
        painter = painterResource(id = backgroundImage),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier.fillMaxSize(),
        alpha = alpha
    )
}
