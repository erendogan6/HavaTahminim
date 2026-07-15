package com.erendogan6.havatahminim.ui.viewModel

import kotlinx.coroutines.flow.SharingStarted

/**
 * Sharing policy for the screen ViewModels' stateIn pipelines: stay live for 5 seconds after the
 * last collector leaves, long enough to bridge a rotation without restarting the upstream.
 */
internal val WhileUiSubscribed: SharingStarted = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000L)
