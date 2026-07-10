package com.erendogan6.havatahminim.util

import android.content.Context
import java.util.Locale

class ResourcesProvider(private val context: Context) {
    fun getString(resId: Int): String {
        return context.getString(resId)
    }

    fun getString(resId: Int, vararg formatArgs: Any): String {
        return context.getString(resId, *formatArgs)
    }

    fun getLanguage(): String {
        return Locale.getDefault().language
    }
}
