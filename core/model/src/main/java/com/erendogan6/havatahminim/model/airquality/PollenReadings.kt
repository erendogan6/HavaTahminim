package com.erendogan6.havatahminim.model.airquality

/**
 * Readings to consider for summaries/alerts: the user's selected allergens, or every species
 * when nothing is selected. Shared by the allergy screen and the pollen-alert notification.
 */
fun List<PollenReading>.relevantTo(selected: Set<PollenType>): List<PollenReading> =
    filter { selected.isEmpty() || it.type in selected }

/** Most concerning reading: highest risk bucket first, then highest actual concentration. */
fun List<PollenReading>.worst(): PollenReading? =
    maxWithOrNull(compareBy({ it.risk.ordinal }, { it.valueGrains ?: 0.0 }))
