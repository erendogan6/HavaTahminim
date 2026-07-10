package com.erendogan6.havatahminim.repository

import com.erendogan6.havatahminim.model.airquality.AirQualityInfo
import com.erendogan6.havatahminim.network.ApiResult

/** Air-quality domain: the Open-Meteo AQI/pollen fetch mapped into [AirQualityInfo]. */
interface AirQualityRepository {
    suspend fun getAirQuality(
        lat: Double,
        lon: Double,
    ): ApiResult<AirQualityInfo>
}
