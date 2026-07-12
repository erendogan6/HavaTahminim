package com.erendogan6.havatahminim.ui.view.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.erendogan6.havatahminim.ui.component.WeatherText

/** Full-screen-friendly error line, shared by the tab screens' Error states. */
@Composable
internal fun ErrorMessage(
    message: String,
    modifier: Modifier = Modifier,
) {
    WeatherText(
        text = message,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.titleSmall,
        // Announce the error when it appears instead of waiting for focus to reach it.
        modifier =
            modifier
                .padding(vertical = 20.dp)
                .semantics { liveRegion = LiveRegionMode.Polite },
    )
}
