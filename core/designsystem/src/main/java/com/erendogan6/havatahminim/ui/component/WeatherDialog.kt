package com.erendogan6.havatahminim.ui.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable

/**
 * Base alert dialog; use this instead of Material3 [AlertDialog] outside the design system.
 * The message overrides [WeatherText]'s maxLines default so long localized text doesn't clip.
 */
@Composable
fun WeatherDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    dismissText: String? = null,
    onDismiss: (() -> Unit)? = null,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { WeatherText(text = title) },
        text = { WeatherText(text = message, maxLines = Int.MAX_VALUE) },
        confirmButton = {
            Button(onClick = onConfirm) {
                WeatherText(confirmText)
            }
        },
        dismissButton =
            if (dismissText != null && onDismiss != null) {
                {
                    Button(onClick = onDismiss) {
                        WeatherText(dismissText)
                    }
                }
            } else {
                null
            },
    )
}