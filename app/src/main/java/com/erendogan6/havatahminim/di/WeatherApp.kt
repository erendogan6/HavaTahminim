package com.erendogan6.havatahminim.di

import android.app.Application
import com.erendogan6.havatahminim.BuildConfig
import com.erendogan6.havatahminim.util.appCheckProviderFactory
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/** Hilt application; plants the Timber tree for debug builds and installs App Check. */
@HiltAndroidApp
class WeatherApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Nothing is planted in release builds or JVM tests, so logging is a no-op there.
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        // App Check attests the caller to Firebase AI Logic; the factory comes from the
        // debug/release source sets (debug provider vs Play Integrity).
        Firebase.appCheck.installAppCheckProviderFactory(appCheckProviderFactory())
    }
}
