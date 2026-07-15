package com.erendogan6.havatahminim.util

/** Localized strings and the current language, available below the UI layer. */
interface ResourcesProvider {
    fun getString(resId: Int): String

    fun getString(
        resId: Int,
        vararg formatArgs: Any,
    ): String

    fun getLanguage(): String
}
