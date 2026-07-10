package com.erendogan6.havatahminim.ui.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable

/**
 * The app's base alert dialog: title + message + confirm button, with an optional dismiss
 * button. All dialogs in `:app` and feature modules go through this — never call Material3
 * [AlertDialog] directly outside the design system. The message deliberately overrides
 * [WeatherText]'s `maxLines = 3` default so long localized messages never clip.
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
