package com.erendogan6.havatahminim.util

import kotlinx.coroutines.yield

/**
 * Fakes for the app's platform seams, following the :core:testing conventions (suspending fakes
 * start with yield()). They live here because the interfaces are app-only.
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
