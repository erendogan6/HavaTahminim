package com.erendogan6.havatahminim.ui.view.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.erendogan6.havatahminim.feature.weather.R
import com.erendogan6.havatahminim.model.weather.currentforecast.CurrentWeatherBaseResponse

/** Full-bleed photo backdrop drawn behind the transparent Scaffold in :app. */
@Composable
fun BackgroundImage(
    weatherState: CurrentWeatherBaseResponse?,
    modifier: Modifier = Modifier,
) {
    val backgroundImage = if (weatherState != null) R.drawable.weather_background else R.drawable.splash
    val alpha = if (weatherState != null) 0.5f else 0.7f

    Image(
        painter = painterResource(id = backgroundImage),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier.fillMaxSize(),
        alpha = alpha
    )
}
