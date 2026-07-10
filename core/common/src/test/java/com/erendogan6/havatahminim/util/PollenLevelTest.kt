package com.erendogan6.havatahminim.util

import com.erendogan6.havatahminim.model.airquality.PollenRisk
import com.erendogan6.havatahminim.model.airquality.PollenType
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PollenLevelTest {
    // Tree thresholds: [1, 10, 50, 100]

    @Test
    fun `null grains maps to NONE`() {
        assertThat(PollenLevel.risk(PollenType.BIRCH, null)).isEqualTo(PollenRisk.NONE)
    }

    @Test
    fun `tree group boundaries use exclusive upper bounds`() {
        assertThat(PollenLevel.risk(PollenType.BIRCH, 0.9)).isEqualTo(PollenRisk.NONE)
        assertThat(PollenLevel.risk(PollenType.BIRCH, 1.0)).isEqualTo(PollenRisk.LOW)
        assertThat(PollenLevel.risk(PollenType.BIRCH, 9.9)).isEqualTo(PollenRisk.LOW)
        assertThat(PollenLevel.risk(PollenType.BIRCH, 10.0)).isEqualTo(PollenRisk.MODERATE)
        assertThat(PollenLevel.risk(PollenType.BIRCH, 49.9)).isEqualTo(PollenRisk.MODERATE)
        assertThat(PollenLevel.risk(PollenType.BIRCH, 50.0)).isEqualTo(PollenRisk.HIGH)
        assertThat(PollenLevel.risk(PollenType.BIRCH, 99.9)).isEqualTo(PollenRisk.HIGH)
        assertThat(PollenLevel.risk(PollenType.BIRCH, 100.0)).isEqualTo(PollenRisk.VERY_HIGH)
    }

    @Test
    fun `grass group has its own cutoffs`() {
        // Grass thresholds: [1, 20, 50, 200]
        assertThat(PollenLevel.risk(PollenType.GRASS, 19.9)).isEqualTo(PollenRisk.LOW)
        assertThat(PollenLevel.risk(PollenType.GRASS, 20.0)).isEqualTo(PollenRisk.MODERATE)
        assertThat(PollenLevel.risk(PollenType.GRASS, 199.9)).isEqualTo(PollenRisk.HIGH)
        assertThat(PollenLevel.risk(PollenType.GRASS, 200.0)).isEqualTo(PollenRisk.VERY_HIGH)
    }

    @Test
    fun `weed group has its own cutoffs`() {
        // Weed thresholds: [1, 10, 30, 50]
        assertThat(PollenLevel.risk(PollenType.RAGWEED, 29.9)).isEqualTo(PollenRisk.MODERATE)
        assertThat(PollenLevel.risk(PollenType.RAGWEED, 30.0)).isEqualTo(PollenRisk.HIGH)
        assertThat(PollenLevel.risk(PollenType.MUGWORT, 50.0)).isEqualTo(PollenRisk.VERY_HIGH)
    }

    @Test
    fun `fraction is zero for null or non-positive grains`() {
        assertThat(PollenLevel.fraction(PollenType.GRASS, null)).isEqualTo(0f)
        assertThat(PollenLevel.fraction(PollenType.GRASS, 0.0)).isEqualTo(0f)
    }

    @Test
    fun `fraction keeps a visible sliver for tiny positive values`() {
        assertThat(PollenLevel.fraction(PollenType.GRASS, 0.5)).isEqualTo(0.03f)
    }

    @Test
    fun `fraction clamps at one beyond the very-high threshold`() {
        assertThat(PollenLevel.fraction(PollenType.GRASS, 400.0)).isEqualTo(1f)
    }

    @Test
    fun `fraction is proportional to the very-high threshold`() {
        // Grass veryHigh = 200 → 100 grains = 0.5
        assertThat(PollenLevel.fraction(PollenType.GRASS, 100.0)).isEqualTo(0.5f)
    }

    @Test
    fun `only HIGH and VERY_HIGH are alarming`() {
        assertThat(PollenLevel.isAlarming(PollenRisk.NONE)).isFalse()
        assertThat(PollenLevel.isAlarming(PollenRisk.LOW)).isFalse()
        assertThat(PollenLevel.isAlarming(PollenRisk.MODERATE)).isFalse()
        assertThat(PollenLevel.isAlarming(PollenRisk.HIGH)).isTrue()
        assertThat(PollenLevel.isAlarming(PollenRisk.VERY_HIGH)).isTrue()
    }

    @Test
    fun `every risk and type has a distinct resource mapping`() {
        val riskIds = PollenRisk.entries.map { PollenLevel.riskLabelRes(it) }
        val typeIds = PollenType.entries.map { PollenLevel.typeNameRes(it) }
        assertThat(riskIds.toSet()).hasSize(PollenRisk.entries.size)
        assertThat(typeIds.toSet()).hasSize(PollenType.entries.size)
    }
}
