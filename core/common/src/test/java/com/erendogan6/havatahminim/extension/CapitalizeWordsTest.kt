package com.erendogan6.havatahminim.extension

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CapitalizeWordsTest {
    @Test
    fun `capitalizes each lowercase word`() {
        assertThat("partly cloudy skies".capitalizeWords()).isEqualTo("Partly Cloudy Skies")
    }

    @Test
    fun `already capitalized words are untouched`() {
        assertThat("Clear Sky".capitalizeWords()).isEqualTo("Clear Sky")
    }

    @Test
    fun `empty string stays empty`() {
        assertThat("".capitalizeWords()).isEqualTo("")
    }

    @Test
    fun `consecutive spaces are preserved`() {
        // Pins the split-on-single-space behavior: double spaces yield empty segments, unchanged.
        assertThat("light  rain".capitalizeWords()).isEqualTo("Light  Rain")
    }
}
