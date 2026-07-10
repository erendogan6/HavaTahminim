package com.erendogan6.havatahminim.testing.util

import com.erendogan6.havatahminim.util.ResourcesProvider

/**
 * Deterministic string resolution without Android resources. Convention (tests assert against it):
 * `getString(id)` → `"res:<id>"`, `getString(id, a, b)` → `"res:<id>:a,b"`. R constants are
 * available on unit-test compile classpaths, so assertions can reference the real ids.
 */
class FakeResourcesProvider(
    var currentLanguage: String = "tr",
) : ResourcesProvider {
    override fun getString(resId: Int): String = "res:$resId"

    override fun getString(
        resId: Int,
        vararg formatArgs: Any,
    ): String = "res:$resId:${formatArgs.joinToString(",")}"

    override fun getLanguage(): String = currentLanguage
}
