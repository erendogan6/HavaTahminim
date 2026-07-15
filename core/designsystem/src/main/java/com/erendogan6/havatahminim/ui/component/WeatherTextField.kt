package com.erendogan6.havatahminim.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.erendogan6.havatahminim.ui.theme.HavaTahminimTheme

/** Shared shape, elevation and container colors for all text fields. */
private val FieldShape = RoundedCornerShape(12.dp)
private val FieldElevation = 4.dp

/**
 * Base text field; use this instead of Material3 [TextField] outside the design system.
 * Styling is fixed here, callers only wire value/label/keyboard behavior.
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

@Preview(showBackground = true, backgroundColor = 0xFF6FA8DC)
@Composable
private fun WeatherTextFieldPreview() {
    HavaTahminimTheme(dynamicColor = false) {
        WeatherTextField(
            value = "İstanbul",
            onValueChange = {},
            label = { WeatherText(text = "Şehir ara") },
            modifier = Modifier.padding(16.dp),
        )
    }
}
