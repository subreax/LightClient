package com.subreax.lightclient.utils

import android.util.Log
import com.subreax.lightclient.LResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


typealias RequestChainReqAction<T> = suspend () -> LResult<T>
typealias RequestChainOnCompleteAction = suspend () -> Unit
typealias RequestChainOnFailureAction = suspend (LResult.Failure) -> Unit

class RequestChain private constructor(
    private val dispatcher: CoroutineDispatcher,
    private val actions: List<RequestChainReqAction<*>>,
    private val onComplete: RequestChainOnCompleteAction,
    private val onFailure: RequestChainOnFailureAction
) {
    class Builder(private val dispatcher: CoroutineDispatcher) {
        private val actions = mutableListOf<RequestChainReqAction<*>>()
        private var onCompleteAction: RequestChainOnCompleteAction? = null
        private var onFailureAction: RequestChainOnFailureAction? = null

        fun req(action: RequestChainReqAction<*>): Builder {
            actions.add(action)
            return this
        }

        fun onComplete(action: RequestChainOnCompleteAction): Builder {
            onCompleteAction = action
            return this
        }

        fun onFailure(action: RequestChainOnFailureAction): Builder {
            onFailureAction = action
            return this
        }

        fun build(): RequestChain {

            return RequestChain(
                dispatcher,
                actions,
                onCompleteAction ?: defaultOnCompleteAction,
                onFailureAction ?: defaultOnFailureAction
            )
        }
    }

    suspend fun run() = withContext(dispatcher) {
        var success = true

        for (action in actions) {
            val result = action()
            if (result is LResult.Failure) {
                success = false
                onFailure(result)
                break
            }
        }

        if (success) {
            onComplete()
        }
    }

    companion object {
        private val defaultOnCompleteAction: RequestChainOnCompleteAction = {  }
        private val defaultOnFailureAction: RequestChainOnFailureAction = {
            Log.d("RequestChain", "Failure: ${it.message}")
        }
    }
}

fun requestChain(dispatcher: CoroutineDispatcher = Dispatchers.Main): RequestChain.Builder {
    return RequestChain.Builder(dispatcher)
}