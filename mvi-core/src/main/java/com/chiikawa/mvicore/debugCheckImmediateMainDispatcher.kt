package com.chiikawa.mvicore

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import timber.log.Timber
import kotlin.coroutines.ContinuationInterceptor

suspend fun debugCheckImmediateMainDispatcher() {
    if (BuildConfig.DEBUG) {
        val interceptor = currentCoroutineContext()[ContinuationInterceptor]
        Timber.d("debugCheckImmediateMainDispatcher: interceptor=$interceptor")

        check(interceptor === Dispatchers.Main.immediate) {
            "Expected ContinuationInterceptor to be Dispatchers.Main.immediate but was $interceptor"
        }
    }
}