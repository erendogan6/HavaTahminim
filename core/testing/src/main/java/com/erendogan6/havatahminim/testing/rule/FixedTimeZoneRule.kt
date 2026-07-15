package com.erendogan6.havatahminim.testing.rule

import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.util.TimeZone

/** Pins the JVM default time zone for SimpleDateFormat-based formatting tests. */
class FixedTimeZoneRule(
    private val zoneId: String = "Europe/Istanbul",
) : TestWatcher() {
    private lateinit var original: TimeZone

    override fun starting(description: Description) {
        original = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone(zoneId))
    }

    override fun finished(description: Description) {
        TimeZone.setDefault(original)
    }
}
