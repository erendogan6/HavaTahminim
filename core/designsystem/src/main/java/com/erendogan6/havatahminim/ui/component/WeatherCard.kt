package com.erendogan6.havatahminim.ui.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.erendogan6.havatahminim.ui.theme.WeatherTheme

/** The app's standard card corner radius. */
private val CardShape = RoundedCornerShape(15.dp)

/**
 * Base card (translucent surface over the photo background); use this instead of Material3
 * [Card] outside the design system. For tappable cards pass [onClick] rather than a
 * `clickable` modifier, so the ripple clips to [shape].
 */
@Composable
fun WeatherCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color = WeatherTheme.colors.cardSurface,
    shape: Shape = CardShape,
    elevation: Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = CardDefaults.cardColors(containerColor = containerColor)
    val cardElevation = CardDefaults.cardElevation(defaultElevation = elevation)
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            colors = colors,
            shape = shape,
            elevation = cardElevation,
            content = content,
        )
    } else {
        Card(
            modifier = modifier,
            colors = colors,
            shape = shape,
            elevation = cardElevation,
            content = content,
        )
    }
}