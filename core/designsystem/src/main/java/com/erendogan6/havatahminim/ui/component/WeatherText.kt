package com.erendogan6.havatahminim.ui.component

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit

/**
 * The app's base text component. All text in `:app` and feature modules goes through this —
 * never call Material3 [Text] directly outside the design system — so app-wide text behavior
 * (defaults, future auto-sizing/emphasis/locale tweaks) is managed in one place.
 *
 * The API is deliberately narrower than M3 [Text]: only the parameters the app actually uses.
 * Widen it here when a real need appears instead of bypassing the component.
 */
@Composable
fun WeatherText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    style: TextStyle = LocalTextStyle.current,
    fontWeight: FontWeight? = null,
    fontSize: TextUnit = TextUnit.Unspecified,
    textAlign: TextAlign? = null,
    maxLines: Int = 3,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        style = style,
        fontWeight = fontWeight,
        fontSize = fontSize,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
    )
}
