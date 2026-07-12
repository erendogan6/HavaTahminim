package com.erendogan6.havatahminim.util

import kotlinx.coroutines.yield

/**
 * Hand-written fakes for the two app-level platform seams, mirroring the zero-mock convention of
 * `:core:testing` (every suspending fake starts with `yield()` so ordered StateFlow emissions
 * aren't conflated under `StandardTestDispatcher`). They live in `:app`'s test source set because
 * the interfaces themselves are `:app`-only.
 */
class FakeDeviceLocationSource : DeviceLocationSource {
    var fix: DeviceLocation? = null
    var callCount = 0
        private set

    override suspend fun currentLocation(): DeviceLocation? {
        yield()
        callCount++
        return fix
    }
}

class FakeConnectivityChecker(
    var online: Boolean = true,
) : ConnectivityChecker {
    override fun isOnline(): Boolean = online
}
