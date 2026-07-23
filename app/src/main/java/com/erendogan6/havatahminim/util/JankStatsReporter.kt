package com.erendogan6.havatahminim.util

import androidx.activity.ComponentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.metrics.performance.JankStats
import timber.log.Timber

// Only frames this far over budget are reported; routine jank stays out of the crash log.
private const val JANK_REPORT_THRESHOLD_MS = 200L
private const val NANOS_PER_MS = 1_000_000L

/**
 * Tracks janky frames on the activity window and reports the severe ones to Timber (WARN forwards
 * to Crashlytics in release). Firebase Performance Monitoring covers the aggregate field metrics;
 * this adds per-frame, state-attributed detail. Tracking follows the activity lifecycle.
 */
fun ComponentActivity.installJankStats() {
    val jankStats =
        JankStats.createAndTrack(window) { frame ->
            val durationMs = frame.frameDurationUiNanos / NANOS_PER_MS
            if (frame.isJank && durationMs > JANK_REPORT_THRESHOLD_MS) {
                Timber.w("Jank frame: %d ms, states=%s", durationMs, frame.states)
            }
        }
    lifecycle.addObserver(
        object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                jankStats.isTrackingEnabled = true
            }

            override fun onPause(owner: LifecycleOwner) {
                jankStats.isTrackingEnabled = false
            }
        },
    )
}
