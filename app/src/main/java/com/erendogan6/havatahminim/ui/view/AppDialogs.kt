package com.erendogan6.havatahminim.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.erendogan6.havatahminim.R
import com.erendogan6.havatahminim.ui.component.WeatherDialog

@Composable
internal fun NoInternetDialog(onDismiss: () -> Unit) {
    WeatherDialog(
        title = stringResource(id = R.string.no_internet_title),
        message = stringResource(id = R.string.no_internet_message),
        confirmText = stringResource(id = R.string.ok),
        onConfirm = onDismiss,
        onDismissRequest = onDismiss,
    )
}

@Composable
internal fun PermissionRationaleDialog(
    onDismiss: () -> Unit,
    onRequestPermission: () -> Unit,
) {
    WeatherDialog(
        title = stringResource(id = R.string.permission_rationale_title),
        message = stringResource(id = R.string.permission_rationale_message),
        confirmText = stringResource(id = R.string.grant_permission),
        onConfirm = onRequestPermission,
        onDismissRequest = onDismiss,
        dismissText = stringResource(id = R.string.cancel),
        onDismiss = onDismiss,
    )
}

@Composable
internal fun ErrorDialog(
    message: String,
    onDismiss: () -> Unit,
) {
    WeatherDialog(
        title = stringResource(id = R.string.error_title),
        message = message,
        confirmText = stringResource(id = R.string.ok),
        onConfirm = onDismiss,
        onDismissRequest = onDismiss,
    )
}
