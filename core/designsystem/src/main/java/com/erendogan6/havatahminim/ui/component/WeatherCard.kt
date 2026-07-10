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
 * The app's base card: the translucent sky-blue surface drawn over the photo background.
 * All cards in `:app` and feature modules go through this — never call Material3 [Card]
 * directly outside the design system. Defaults cover the common case; pass [containerColor]/
 * [shape]/[elevation] only where a screen genuinely deviates (hero risk card, city results).
 * For tappable cards pass [onClick] instead of a `clickable` modifier — Card clips the ripple
 * and touch target to [shape].
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
