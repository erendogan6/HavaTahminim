package com.erendogan6.havatahminim.ui.view.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.erendogan6.havatahminim.feature.weather.R
import com.erendogan6.havatahminim.ui.adaptive.isCompactHeight
import com.erendogan6.havatahminim.ui.component.CenteredColumn
import com.erendogan6.havatahminim.ui.component.WeatherText
import com.erendogan6.havatahminim.ui.theme.WeatherTheme

/** Animated loading state shown by every tab until its data slice arrives. */
@Composable
internal fun SplashScreen(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "splash")
    val pulse by transition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.18f,
        animationSpec =
            infiniteRepeatable(tween(durationMillis = 1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse",
    )

    // Compact height (landscape): shrink the halo/icon and spacings so the splash fits
    // without clipping.
    val compact = isCompactHeight()
    val glow = WeatherTheme.colors.glow
    CenteredColumn(modifier = modifier) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(if (compact) 132.dp else 200.dp)) {
            // Soft glowing halo that breathes behind the icon.
            Box(
                modifier = Modifier
                    .size(if (compact) 118.dp else 180.dp)
                    .graphicsLayer {
                        scaleX = pulse
                        scaleY = pulse
                    }
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(glow.copy(alpha = 0.53f), glow.copy(alpha = 0.2f), glow.copy(alpha = 0f))
                        )
                    )
            )
            LottieAnimation(
                composition = rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.anim_loading_clouds)).value,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.size(if (compact) 100.dp else 150.dp),
            )
        }
        Spacer(modifier = Modifier.height(if (compact) 12.dp else 28.dp))
        WeatherText(
            text = stringResource(id = R.string.loading_message),
            color = WeatherTheme.colors.ink,
            style =
                (if (compact) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.headlineLarge)
                    .copy(shadow = Shadow(color = glow, blurRadius = 8f)),
        )
        Spacer(modifier = Modifier.height(if (compact) 8.dp else 18.dp))
        LoadingDots()
    }
}

@Composable
private fun LoadingDots() {
    val transition = rememberInfiniteTransition(label = "dots")
    val ink = WeatherTheme.colors.ink
    Row {
        repeat(3) { index ->
            val dotAlpha by transition.animateFloat(
                initialValue = 0.25f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    tween(durationMillis = 600, delayMillis = index * 180, easing = FastOutSlowInEasing),
                    RepeatMode.Reverse,
                ),
                label = "dot$index",
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 5.dp)
                    .size(11.dp)
                    .graphicsLayer { alpha = dotAlpha }
                    .clip(CircleShape)
                    .background(ink)
            )
        }
    }
}
