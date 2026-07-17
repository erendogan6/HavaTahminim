package com.erendogan6.havatahminim.util

import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

/** Release Timber tree: forwards WARN/ERROR to Crashlytics (message as breadcrumb, error throwables as non-fatals). */
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
