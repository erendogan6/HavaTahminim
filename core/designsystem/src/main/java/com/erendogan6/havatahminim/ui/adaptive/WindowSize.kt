package com.erendogan6.havatahminim.ui.adaptive

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalConfiguration

/**
 * True when the window height is "compact" per the Material window-size-class threshold
 * (< 480dp) — in practice, a phone in landscape (also tracks multi-window resizes, since it
 * reads the window's Configuration). The app's single adaptive-layout signal: navigation
 * moves from the bottom bar to a rail, and screens switch to side-by-side panes off of it.
 */
@Composable
@ReadOnlyComposable
fun isCompactHeight(): Boolean = LocalConfiguration.current.screenHeightDp < 480
