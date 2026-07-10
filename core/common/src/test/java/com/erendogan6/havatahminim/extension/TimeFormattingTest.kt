package com.erendogan6.havatahminim.extension

import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.util.Locale
import java.util.TimeZone

/** SimpleDateFormat reads the JVM-default zone; pin it so assertions are machine-independent. */
class FixedTimeZone(
    private val zoneId: String,
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

class TimeFormattingTest {
    @get:Rule
    val timeZone = FixedTimeZone("Europe/Istanbul")

    // 2026-07-15T12:00:00Z == 15:00 Wednesday in Istanbul (UTC+3)
    private val epochSeconds = 1_784_116_800L

    @Test
    fun `toDayName localizes the weekday`() {
        assertThat(epochSeconds.toDayName(Locale("tr"))).isEqualTo("Çarşamba")
        assertThat(epochSeconds.toDayName(Locale.ENGLISH)).isEqualTo("Wednesday")
    }

    @Test
    fun `toHourMinute renders local wall time`() {
        assertThat(epochSeconds.toHourMinute(Locale.ENGLISH)).isEqualTo("15:00")
    }
}
