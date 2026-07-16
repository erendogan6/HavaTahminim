package com.erendogan6.havatahminim.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Generates baseline-prof.txt by walking the critical user journey (cold start + all four tabs)
 * and recording every class/method ART touches. Run with:
 *
 *   ./gradlew :app:generateBaselineProfile
 *
 * The result lands in app/src/release/generated/baselineProfiles/ and is committed, so every
 * install gets ahead-of-time compilation of the startup path.
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() {
        rule.collect(
            packageName = PACKAGE_NAME,
            includeInStartupProfile = true,
        ) {
            grantPermissions()
            startUntilContent()
            visitAllTabs()
        }
    }
}
