package com.erendogan6.havatahminim.testing.util

import com.erendogan6.havatahminim.util.ResourcesProvider

/**
 * Deterministic string resolution without Android resources: getString(id) returns "res:<id>",
 * with args "res:<id>:a,b". Tests assert against the real R constants.
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
