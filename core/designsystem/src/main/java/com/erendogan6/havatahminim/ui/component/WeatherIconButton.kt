package com.erendogan6.havatahminim.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.erendogan6.havatahminim.ui.theme.HavaTahminimTheme
import com.erendogan6.havatahminim.ui.theme.WeatherTheme

/** Base icon button (circular veil chip, legible over the photo); use instead of raw M3 [IconButton]. */
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

@Preview(showBackground = true, backgroundColor = 0xFF6FA8DC)
@Composable
private fun WeatherIconButtonPreview() {
    HavaTahminimTheme(dynamicColor = false) {
        WeatherIconButton(icon = Icons.Default.Search, contentDescription = "Ara", onClick = {})
    }
}
