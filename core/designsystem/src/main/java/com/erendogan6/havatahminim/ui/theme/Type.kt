package com.erendogan6.havatahminim.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.erendogan6.havatahminim.core.designsystem.R

// The app's three families. Open Sans and Roboto Medium Italic are downloadable-font XMLs,
// Merriweather is bundled. Only this file may reference R.font; screens use Typography roles.
val OpenSans = FontFamily(Font(R.font.open_sans))
val Merriweather = FontFamily(Font(R.font.merriweather))
val RobotoMediumItalic = FontFamily(Font(R.font.roboto_medium_italic))

/** Text drawn over the photo background carries a soft drop shadow for legibility. */
private val TextShadow = Shadow(color = Palette.ShadowDark, blurRadius = 2f)

/**
 * Display/headline/title roles are tuned for the app's hero text and carry the legibility shadow;
 * body/label roles keep the Material3 defaults so M3 components look right.
 */
val Typography =
    Typography(
        // 50sp temperature readout
        displayLarge =
            TextStyle(
                fontFamily = RobotoMediumItalic,
                fontWeight = FontWeight.Medium,
                fontStyle = FontStyle.Italic,
                fontSize = 50.sp,
                shadow = TextShadow,
            ),
        // 36sp location name
        displayMedium =
            TextStyle(
                fontFamily = Merriweather,
                fontSize = 36.sp,
                shadow = TextShadow,
            ),
        // 36sp hero risk label
        displaySmall =
            TextStyle(
                fontFamily = OpenSans,
                fontWeight = FontWeight.Bold,
                fontSize = 36.sp,
                shadow = TextShadow,
            ),
        // 30sp splash message / screen titles
        headlineLarge =
            TextStyle(
                fontFamily = OpenSans,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
                shadow = TextShadow,
            ),
        // 26sp weather description / card headers
        headlineMedium =
            TextStyle(
                fontFamily = OpenSans,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                shadow = TextShadow,
            ),
        // 24sp list titles / prominent values
        headlineSmall =
            TextStyle(
                fontFamily = OpenSans,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                shadow = TextShadow,
            ),
        // 22sp section titles (Normal-weight variants override fontWeight at the call site)
        titleLarge =
            TextStyle(
                fontFamily = OpenSans,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                shadow = TextShadow,
            ),
        // 20sp row/item titles
        titleMedium =
            TextStyle(
                fontFamily = OpenSans,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                shadow = TextShadow,
            ),
        // 17sp emphasized row labels
        titleSmall =
            TextStyle(
                fontFamily = OpenSans,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
            ),
        bodyLarge =
            TextStyle(
                fontFamily = OpenSans,
                fontSize = 16.sp,
            ),
        bodyMedium =
            TextStyle(
                fontFamily = OpenSans,
                fontSize = 14.sp,
            ),
        bodySmall =
            TextStyle(
                fontFamily = OpenSans,
                fontSize = 13.sp,
            ),
        labelLarge =
            TextStyle(
                fontFamily = OpenSans,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
            ),
        labelMedium =
            TextStyle(
                fontFamily = OpenSans,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
            ),
        labelSmall =
            TextStyle(
                fontFamily = OpenSans,
                fontSize = 10.sp,
            ),
    )
