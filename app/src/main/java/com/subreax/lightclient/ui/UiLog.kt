package com.subreax.lightclient.ui

import android.content.Context
import androidx.compose.material.SnackbarHostState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UiLog(
    private val context: Context
) {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var snackbarState: SnackbarHostState? = null

    fun bind(snackbarHostState: SnackbarHostState) {
        this.snackbarState = snackbarHostState
    }

    fun e(msg: UiText) {
        showSnackbar(msg)
    }

    fun i(msg: UiText) {
        showSnackbar(msg)
    }

    private fun showSnackbar(msg: UiText) {
        snackbarState?.let {
            coroutineScope.launch {
                it.showSnackbar(msg.stringValue(context))
            }
        }
    }
}