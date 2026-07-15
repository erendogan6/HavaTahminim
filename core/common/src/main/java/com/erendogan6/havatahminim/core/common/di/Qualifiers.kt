package com.erendogan6.havatahminim.core.common.di

import javax.inject.Qualifier

/**
 * Qualifier for the IO [kotlinx.coroutines.CoroutineDispatcher]. Production binds Dispatchers.IO;
 * tests inject a TestDispatcher so repository delays run under virtual time.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher
