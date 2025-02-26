package com.subreax.lightclient.utils

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow

suspend fun <T> Flow<T>.waitFor(condition: (T) -> Boolean): T {
    var result: T? = null

    withContext(Dispatchers.IO) {
        launch {
            collect {
                if (condition(it)) {
                    result = it
                    cancel()
                }
            }
        }
    }

    return result!!
}

suspend fun <T> Flow<T>.waitForWithTimeout(timeout: Long, condition: (T) -> Boolean): T? {
    var result: T? = null

    try {
        withTimeout(timeout) {
            result = waitFor(condition)
        }
    } catch (_: TimeoutCancellationException) { }

    return result
}
