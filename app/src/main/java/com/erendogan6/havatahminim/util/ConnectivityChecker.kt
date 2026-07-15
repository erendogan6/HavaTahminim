package com.erendogan6.havatahminim.util

import android.content.Context
import com.erendogan6.havatahminim.extension.NetworkUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * JVM-testable view of "is the network up" for
 * [com.erendogan6.havatahminim.ui.viewModel.MainViewModel]'s offline branching.
 */
interface ConnectivityChecker {
    fun isOnline(): Boolean
}

/** Delegates to [NetworkUtils] — pure platform glue, exercised on device rather than in unit tests. */
@Singleton
class AndroidConnectivityChecker
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
    ) : ConnectivityChecker {
        override fun isOnline(): Boolean = NetworkUtils.isNetworkAvailable(context)
    }
