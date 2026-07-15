package com.erendogan6.havatahminim.extension

import java.text.SimpleDateFormat
import java.util.Locale

private const val MILLIS_PER_SECOND = 1000L

/** Localized day-of-week name ("Monday" / "Pazartesi") for an epoch-second timestamp. */
fun Long.toDayName(locale: Locale): String = SimpleDateFormat("EEEE", locale).format(this * MILLIS_PER_SECOND)

/** Localized "HH:mm" clock time for an epoch-second timestamp. */
fun Long.toHourMinute(locale: Locale): String = SimpleDateFormat("HH:mm", locale).format(this * MILLIS_PER_SECOND)
