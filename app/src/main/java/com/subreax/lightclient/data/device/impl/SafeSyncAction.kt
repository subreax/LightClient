package com.subreax.lightclient.data.device.impl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch

class SafeSyncAction(
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    private val action: suspend () -> Unit
) {
    private var job: Job = Job()

    fun run() {
        coroutineScope.launch {
            job.cancelAndJoin()
            job = launch {
                action()
            }
        }
    }
}