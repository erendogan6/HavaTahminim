package com.erendogan6.havatahminim.ui.component

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** The single source of the app's text-field look: shape, elevation, and container colors. */
private val FieldShape = RoundedCornerShape(12.dp)
private val FieldElevation = 4.dp

/**
 * The app's base text field. All text input in `:app` and feature modules goes through this —
 * never call Material3 [TextField] directly outside the design system. Styling (rounded shape,
 * drop shadow, surface containers, primary/outline indicators) is fixed here so every field
 * looks the same; callers only wire value/label/keyboard behavior.
 */
@Composable
fun WeatherTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    singleLine: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    shadowElevation: Dp = FieldElevation,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.shadow(shadowElevation, FieldShape),
        label = label,
        colors =
            TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
            ),
        shape = FieldShape,
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
    )
}
