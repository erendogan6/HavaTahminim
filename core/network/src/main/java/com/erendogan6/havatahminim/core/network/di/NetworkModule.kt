package com.erendogan6.havatahminim.core.network.di

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.erendogan6.havatahminim.core.network.BuildConfig
import com.erendogan6.havatahminim.network.AirQualityApiService
import com.erendogan6.havatahminim.network.CityApiService
import com.erendogan6.havatahminim.network.WeatherApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val TIMEOUT_SECONDS = 30L

    // Open-Meteo is served through Let's Encrypt. We pin the SPKI of the two ISRG (Let's Encrypt)
    // roots rather than a leaf or intermediate: roots don't rotate with every 90-day renewal, so
    // this survives normal cert churn while still blocking a MITM proxy using a different CA.
    // ISRG Root X1 (RSA) covers RSA chains, ISRG Root X2 (ECDSA) the elliptic ones — public,
    // documented values valid until 2035. Applied to release builds only (see below).
    //
    // VERIFY on the Play internal track before a public release: fetch the live pins from a
    // TRUSTED network (not a TLS-inspecting proxy) with
    //   openssl s_client -connect api.open-meteo.com:443 -servername api.open-meteo.com -showcerts \
    //     </dev/null | openssl x509 -pubkey -noout | openssl pkey -pubin -outform der \
    //     | openssl dgst -sha256 -binary | openssl enc -base64
    // and confirm the served chain still anchors to one of these roots.
    private const val OPEN_METEO_HOST_PATTERN = "*.open-meteo.com"
    private val OPEN_METEO_ROOT_PINS =
        listOf(
            "sha256/C5+lpZ7tcVwmwQIMcRtPbsQtWLABXhQzejna0wHFr8M=", // ISRG Root X1 (RSA)
            "sha256/diGVwiVYbubAI3RW4hB9xU8e/CH2GnkuvVFZE8zmgzI=", // ISRG Root X2 (ECDSA)
        )

    @Provides
    @Singleton
    fun provideOkHttpClient(
        @ApplicationContext context: Context,
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()

        if (BuildConfig.DEBUG) {
            val logging =
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            builder.addInterceptor(logging)

            val chuckerInterceptor = ChuckerInterceptor.Builder(context).build()
            builder.addInterceptor(chuckerInterceptor)
        }

        // Certificate pinning is release-only: debug builds keep Chucker / a proxy debugger
        // working, which a pin would break.
        if (!BuildConfig.DEBUG) {
            val pinnerBuilder = CertificatePinner.Builder()
            OPEN_METEO_ROOT_PINS.forEach { pinnerBuilder.add(OPEN_METEO_HOST_PATTERN, it) }
            builder.certificatePinner(pinnerBuilder.build())
        }

        builder.connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        builder.readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        builder.writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)

        return builder.build()
    }

    @Provides
    @Singleton
    fun provideWeatherApiService(okHttpClient: OkHttpClient): WeatherApiService =
        Retrofit
            .Builder()
            .baseUrl("https://api.open-meteo.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)

    @Provides
    @Singleton
    fun provideCityApiService(okHttpClient: OkHttpClient): CityApiService =
        Retrofit
            .Builder()
            .baseUrl("https://geocoding-api.open-meteo.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CityApiService::class.java)

    @Provides
    @Singleton
    fun provideAirQualityApiService(okHttpClient: OkHttpClient): AirQualityApiService =
        Retrofit
            .Builder()
            .baseUrl("https://air-quality-api.open-meteo.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AirQualityApiService::class.java)
}
