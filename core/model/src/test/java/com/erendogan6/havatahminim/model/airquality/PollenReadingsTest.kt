package com.erendogan6.havatahminim.model.airquality

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PollenReadingsTest {
    private fun reading(
        type: PollenType,
        grains: Double?,
        risk: PollenRisk,
    ) = PollenReading(type = type, valueGrains = grains, risk = risk)

    @Test
    fun `empty selection means every reading is relevant`() {
        val all =
            listOf(
                reading(PollenType.GRASS, 10.0, PollenRisk.LOW),
                reading(PollenType.BIRCH, 60.0, PollenRisk.HIGH),
            )
        assertThat(all.relevantTo(emptySet())).isEqualTo(all)
    }

    @Test
    fun `selection filters to the chosen types`() {
        val all =
            listOf(
                reading(PollenType.GRASS, 10.0, PollenRisk.LOW),
                reading(PollenType.BIRCH, 60.0, PollenRisk.HIGH),
                reading(PollenType.OLIVE, 5.0, PollenRisk.LOW),
            )
        val filtered = all.relevantTo(setOf(PollenType.BIRCH, PollenType.OLIVE))
        assertThat(filtered.map { it.type }).containsExactly(PollenType.BIRCH, PollenType.OLIVE)
    }

    @Test
    fun `worst picks the highest risk bucket`() {
        val readings =
            listOf(
                reading(PollenType.GRASS, 500.0, PollenRisk.MODERATE),
                reading(PollenType.BIRCH, 2.0, PollenRisk.HIGH),
            )
        assertThat(readings.worst()?.type).isEqualTo(PollenType.BIRCH)
    }

    @Test
    fun `worst breaks risk ties by concentration`() {
        val readings =
            listOf(
                reading(PollenType.GRASS, 55.0, PollenRisk.HIGH),
                reading(PollenType.BIRCH, 80.0, PollenRisk.HIGH),
            )
        assertThat(readings.worst()?.type).isEqualTo(PollenType.BIRCH)
    }

    @Test
    fun `worst treats null grains as zero in tie-breaks`() {
        val readings =
            listOf(
                reading(PollenType.GRASS, null, PollenRisk.HIGH),
                reading(PollenType.BIRCH, 1.0, PollenRisk.HIGH),
            )
        assertThat(readings.worst()?.type).isEqualTo(PollenType.BIRCH)
    }

    @Test
    fun `worst of empty list is null`() {
        assertThat(emptyList<PollenReading>().worst()).isNull()
    }
}
