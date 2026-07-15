package com.erendogan6.havatahminim.ui.adaptive

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalConfiguration

/**
 * True when the window height is compact (< 480dp, the Material threshold): in practice a phone
 * in landscape. The app's single adaptive-layout signal.
 */
@Composable
@ReadOnlyComposable
fun isCompactHeight(): Boolean = LocalConfiguration.current.screenHeightDp < 480
