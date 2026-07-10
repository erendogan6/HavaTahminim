package com.erendogan6.havatahminim.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.erendogan6.havatahminim.ui.theme.WeatherTheme

/**
 * The app's base icon button: a circular white-veil chip with ink-tinted icon, legible over
 * the photo background. Use this for icon actions in `:app` and feature modules instead of
 * raw Material3 [IconButton] + [Icon] pairs.
 */
@Composable
fun WeatherIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(42.dp).clip(CircleShape).background(WeatherTheme.colors.surfaceVeil),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = WeatherTheme.colors.ink,
        )
    }
}
