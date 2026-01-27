package com.subreax.lightclient.ui.connection

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.subreax.lightclient.R
import com.subreax.lightclient.ui.UiText


@Composable
fun LoadingOverlay(connectionState: UiConnectionState) {
    AnimatedVisibility(
        visible = connectionState !is UiConnectionState.Idle,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        var message by remember { mutableStateOf("") }
        val ctx = LocalContext.current

        LaunchedEffect(connectionState) {
            val msg = when (connectionState) {
                is UiConnectionState.Connecting -> UiText.Res(
                    R.string.connecting_to,
                    connectionState.deviceName
                )

                is UiConnectionState.Fetching -> UiText.Res(
                    R.string.fetching_data
                )

                else -> null
            }

            if (msg != null) {
                message = msg.stringValue(ctx)
            }
        }

        LoadingOverlay(message = message)
    }
}

@Composable
private fun LoadingOverlay(message: String) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .background(Color(0xAE000000))
            .fillMaxSize()
            .focusable(true)
            .clickable(interactionSource, null, true) { }
            .zIndex(2.0f)
    ) {
        Column(
            horizontalAlignment = CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.align(Center)
        ) {
            CircularProgressIndicator()
            Text(message)
        }
    }
}