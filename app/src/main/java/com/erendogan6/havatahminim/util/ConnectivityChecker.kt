package com.erendogan6.havatahminim.util

import android.content.Context
import com.erendogan6.havatahminim.extension.NetworkUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Connectivity check behind an interface, for testability. */
interface ConnectivityChecker {
    fun isOnline(): Boolean
}

/** Delegates to [NetworkUtils]. */
@Singleton
class AndroidConnectivityChecker
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
    ) : ConnectivityChecker {
        override fun isOnline(): Boolean = NetworkUtils.isNetworkAvailable(context)
    }
