package com.erendogan6.havatahminim.util

/**
 * Resolves localized strings and the current language off the UI layer (repositories, ViewModels,
 * the suggestion prompt). An interface so JVM tests can substitute a fake without a mocking
 * library; the production implementation is [DefaultResourcesProvider].
 */
interface ResourcesProvider {
    fun getString(resId: Int): String

    fun getString(
        resId: Int,
        vararg formatArgs: Any,
    ): String

    fun getLanguage(): String
}
