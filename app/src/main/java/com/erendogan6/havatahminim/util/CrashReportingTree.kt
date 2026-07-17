package com.erendogan6.havatahminim.util

import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

/**
 * Release-build Timber tree: forwards warnings and errors to Crashlytics as breadcrumbs, and
 * reports the throwable of an error as a non-fatal. Lower priorities are dropped so the crash
 * log stays signal. Mirrors the debug DebugTree, but off-device.
 *
 * The priorities are `android.util.Log` levels (Timber's own scheme); referenced fully-qualified
 * so the Log-import ban stays intact — this reads priorities, it doesn't log through Log.
 */
internal class CrashReportingTree : Timber.Tree() {
    override fun log(
        priority: Int,
        tag: String?,
        message: String,
        t: Throwable?,
    ) {
        if (priority < android.util.Log.WARN) return
        val crashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.log(message)
        if (priority == android.util.Log.ERROR && t != null) {
            crashlytics.recordException(t)
        }
    }
}
