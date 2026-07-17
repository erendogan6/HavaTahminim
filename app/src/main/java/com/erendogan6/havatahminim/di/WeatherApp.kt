package com.erendogan6.havatahminim.di

import android.app.Application
import com.erendogan6.havatahminim.BuildConfig
import com.erendogan6.havatahminim.util.CrashReportingTree
import com.erendogan6.havatahminim.util.appCheckProviderFactory
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/** Hilt application; plants the Timber tree (debug logs / release crash-reporting) and installs App Check. */
@HiltAndroidApp
class WeatherApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Debug logs to logcat; release forwards to Crashlytics.
        Timber.plant(if (BuildConfig.DEBUG) Timber.DebugTree() else CrashReportingTree())
        // App Check factory comes from the debug/release source sets.
        Firebase.appCheck.installAppCheckProviderFactory(appCheckProviderFactory())
    }
}
