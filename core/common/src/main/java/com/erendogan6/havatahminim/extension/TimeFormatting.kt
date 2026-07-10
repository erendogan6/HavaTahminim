package com.erendogan6.havatahminim.extension

import java.text.SimpleDateFormat
import java.util.Locale

/** Localized day-of-week name ("Monday" / "Pazartesi") for an epoch-second timestamp. */
fun Long.toDayName(locale: Locale): String = SimpleDateFormat("EEEE", locale).format(this * 1000L)

/** Localized "HH:mm" clock time for an epoch-second timestamp. */
fun Long.toHourMinute(locale: Locale): String = SimpleDateFormat("HH:mm", locale).format(this * 1000L)
