package com.erendogan6.havatahminim.baselineprofile

import android.Manifest
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import java.util.regex.Pattern

internal const val PACKAGE_NAME = "com.erendogan6.havatahminim"
private const val CONTENT_TIMEOUT_MS = 40_000L
private const val TAB_TIMEOUT_MS = 10_000L
private const val SWIPE_STEPS = 20
private const val SWIPE_TOP_FRACTION = 0.25
private const val SWIPE_BOTTOM_FRACTION = 0.75

// Nav labels in both locales; the emulator's locale decides which one renders.
private val TAB_DAILY = Pattern.compile("Daily|Günlük")
private val TAB_ALLERGY = Pattern.compile("Allergy|Alerji")
private val TAB_ZEKAI = Pattern.compile("ZekAI")
private val TAB_TODAY = Pattern.compile("Today|Bugün")

/**
 * Grants the runtime permissions up front so no system dialog blocks the journey. Without a GPS
 * fix the app falls back to Istanbul, which still exercises the full startup pipeline.
 */
internal fun MacrobenchmarkScope.grantPermissions() {
    val permissions =
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS,
        )
    permissions.forEach { device.executeShellCommand("pm grant $PACKAGE_NAME $it") }
}

/** Cold start until the first weather content (the nav bar only appears once data arrives). */
internal fun MacrobenchmarkScope.startUntilContent() {
    startActivityAndWait()
    device.wait(Until.hasObject(By.text(TAB_DAILY)), CONTENT_TIMEOUT_MS)
}

/**
 * The critical user journey: visit every tab the way a user would. Each visit composes the tab's
 * screen and runs its ViewModel pipeline, which is exactly the code worth ahead-of-time compiling.
 */
internal fun MacrobenchmarkScope.visitAllTabs() {
    device.clickTab(TAB_DAILY)
    device.clickTab(TAB_ALLERGY)
    // Allergy is the longest screen; scroll it like a user checking the daily outlook.
    val centerX = device.displayWidth / 2
    device.swipe(
        centerX,
        (device.displayHeight * SWIPE_BOTTOM_FRACTION).toInt(),
        centerX,
        (device.displayHeight * SWIPE_TOP_FRACTION).toInt(),
        SWIPE_STEPS,
    )
    device.waitForIdle()
    device.clickTab(TAB_ZEKAI)
    device.clickTab(TAB_TODAY)
}

private fun UiDevice.clickTab(label: Pattern) {
    wait(Until.hasObject(By.text(label)), TAB_TIMEOUT_MS)
    findObject(By.text(label))?.click()
    waitForIdle()
    // Give the tab's pipeline a moment to emit so its rendering path is captured too.
    wait(Until.hasObject(By.text(label)), TAB_TIMEOUT_MS)
}
