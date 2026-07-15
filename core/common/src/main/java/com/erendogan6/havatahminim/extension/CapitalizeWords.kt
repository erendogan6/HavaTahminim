package com.erendogan6.havatahminim.extension

import java.util.Locale

fun String.capitalizeWords(): String =
    split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
    }
