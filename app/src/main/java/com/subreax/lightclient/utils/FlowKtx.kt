package com.subreax.lightclient.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

suspend fun <T> Flow<T>.waitFor(condition: (T) -> Boolean): T {
    var result: T? = null

    withContext(Dispatchers.Default) {
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

