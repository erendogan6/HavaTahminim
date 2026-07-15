package com.erendogan6.havatahminim.ui.viewModel

import kotlinx.coroutines.flow.SharingStarted

/**
 * Sharing policy for every screen ViewModel's `stateIn` pipeline: stay live for 5 seconds after
 * the last collector leaves, so a configuration change (rotation resubscribes well within the
 * window) never restarts the upstream, while real backgrounding eventually stops it.
 *
 * Tests asserting restart behaviour advance virtual time past this window (`advanceTimeBy(5_001)`).
 */
internal val WhileUiSubscribed: SharingStarted = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000L)
