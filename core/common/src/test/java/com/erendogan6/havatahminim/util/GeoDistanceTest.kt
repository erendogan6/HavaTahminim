package com.erendogan6.havatahminim.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class GeoDistanceTest {
    @Test
    fun `identical points are zero meters apart`() {
        assertThat(distanceMeters(41.0082, 28.9784, 41.0082, 28.9784)).isEqualTo(0.0)
    }

    @Test
    fun `distance is symmetric`() {
        val ab = distanceMeters(41.0082, 28.9784, 39.9334, 32.8597)
        val ba = distanceMeters(39.9334, 32.8597, 41.0082, 28.9784)
        assertThat(ab).isWithin(0.001).of(ba)
    }

    @Test
    fun `istanbul to ankara is roughly 349 km`() {
        val d = distanceMeters(41.0082, 28.9784, 39.9334, 32.8597)
        assertThat(d).isWithin(349_000.0 * 0.01).of(349_000.0)
    }

    /**
     * Guard rail for the cache-radius thresholds: these latitude offsets are the exact fixture
     * pairs the repository tests use to straddle 5 km and 10 km. If the haversine ever regresses
     * (or someone reintroduces a platform distance that returns 0 under returnDefaultValues),
     * this test fails loudly.
     */
    @Test
    fun `fixture offsets straddle the 5km and 10km thresholds`() {
        val lat = 41.0082
        val lon = 28.9784
        assertThat(distanceMeters(lat, lon, lat + 0.04407, lon)).isLessThan(5_000.0)
        assertThat(distanceMeters(lat, lon, lat + 0.04587, lon)).isGreaterThan(5_000.0)
        assertThat(distanceMeters(lat, lon, lat + 0.08904, lon)).isLessThan(10_000.0)
        assertThat(distanceMeters(lat, lon, lat + 0.09083, lon)).isGreaterThan(10_000.0)
    }
}
