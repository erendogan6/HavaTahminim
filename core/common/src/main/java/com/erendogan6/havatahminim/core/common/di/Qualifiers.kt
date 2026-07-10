package com.erendogan6.havatahminim.core.common.di

import javax.inject.Qualifier

/**
 * Marks the injectable IO [kotlinx.coroutines.CoroutineDispatcher]. Production binds
 * [kotlinx.coroutines.Dispatchers.IO]; tests inject a TestDispatcher sharing the test scheduler,
 * which is what puts repository delays (retry backoff) under virtual time.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher
