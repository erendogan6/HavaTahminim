package com.erendogan6.havatahminim.util

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/** [ResourcesProvider] backed by the application context. */
@Singleton
class DefaultResourcesProvider
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
    ) : ResourcesProvider {
        override fun getString(resId: Int): String {
            return context.getString(resId)
        }

        override fun getString(
            resId: Int,
            vararg formatArgs: Any,
        ): String {
            return context.getString(resId, *formatArgs)
        }

        override fun getLanguage(): String {
            return Locale.getDefault().language
        }
    }
