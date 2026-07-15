package com.erendogan6.havatahminim.ui.component

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import com.erendogan6.havatahminim.ui.theme.HavaTahminimTheme

/**
 * Base text component; use this instead of Material3 [Text] outside the design system.
 * Exposes only the parameters the app uses; widen here rather than bypassing.
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

@Preview(showBackground = true, backgroundColor = 0xFF6FA8DC)
@Composable
private fun WeatherTextPreview() {
    HavaTahminimTheme(dynamicColor = false) {
        WeatherText(text = "Parçalı bulutlu", style = MaterialTheme.typography.headlineMedium)
    }
}
