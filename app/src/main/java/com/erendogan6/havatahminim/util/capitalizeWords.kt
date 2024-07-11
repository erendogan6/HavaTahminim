package com.erendogan6.havatahminim.util

import java.util.Locale


fun String.capitalizeWords(): String = split(" ").joinToString(" ") { it.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() } }