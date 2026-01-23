package com.subreax.lightclient.ui.connection

import android.content.res.Configuration.UI_MODE_NIGHT_YES
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomStart
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.subreax.lightclient.R
import com.subreax.lightclient.data.ConnectionType
import com.subreax.lightclient.data.DeviceDesc
import com.subreax.lightclient.ui.LocalContentColorMediumAlpha
import com.subreax.lightclient.ui.TopBar
import com.subreax.lightclient.ui.edgePadding
import com.subreax.lightclient.ui.theme.LightClientTheme


@Composable
fun ConnectionScreen(
    navHome: (DeviceDesc) -> Unit,
    connectionViewModel: ConnectionViewModel = hiltViewModel()
) {
    val uiState = connectionViewModel.uiState

    LaunchedEffect(true) {
        connectionViewModel.navHome.collect {
            navHome(it)
        }
    }

    ConnectionScreen(
        devices = uiState.devices,
        onDeviceSelected = connectionViewModel::connect,
        isLoading = uiState.loading,
        waitingForConnectivity = uiState.waitingForConnectivity,
        errorTime = uiState.errorMsg.time,
        errorMessage = uiState.errorMsg.msg.stringValue(),
        loadingMessage = uiState.loadingMsg.stringValue()
    )
}

@Composable
private fun ConnectionScreen(
    devices: List<DeviceDesc>,
    onDeviceSelected: (DeviceDesc) -> Unit,
    isLoading: Boolean,
    waitingForConnectivity: Boolean,
    errorTime: Long,
    errorMessage: String,
    loadingMessage: String
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorTime) {
        if (errorMessage.isNotEmpty()) {
            snackbarHostState.showSnackbar(errorMessage)
        }
    }

    Box(Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .navigationBarsPadding()
                .fillMaxSize()
        ) {
            Column(Modifier.fillMaxSize()) {
                TopBar(
                    title = stringResource(R.string.connecting_to_controller),
                    subtitle = {
                        Text(stringResource(R.string.pick_controller))
                    }
                )

                Divider(
                    Modifier
                        .fillMaxWidth(2.0f / 3.0f)
                        .padding(bottom = 16.dp), thickness = 2.dp
                )

                DevicesList(
                    devices = devices,
                    waitingForConnectivity = waitingForConnectivity,
                    onDeviceSelected = onDeviceSelected,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            }

            SnackbarHost(
                hostState = snackbarHostState, modifier = Modifier.align(BottomStart)
            )
        }

        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LoadingOverlay(message = loadingMessage)
        }
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

@Composable
private fun DevicesList(
    devices: List<DeviceDesc>,
    waitingForConnectivity: Boolean,
    onDeviceSelected: (DeviceDesc) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier) {
        if (waitingForConnectivity) {
            Text(
                text = stringResource(R.string.waiting_for_connectivity),
                modifier = Modifier.edgePadding()
            )
        } else {
            if (devices.isEmpty()) {
                Text(
                    text = stringResource(R.string.devices_not_found),
                    modifier = Modifier.edgePadding()
                )
            }

            LazyColumn(Modifier.fillMaxSize()) {
                items(items = devices, key = { it.address }) { device ->
                    DeviceItem(
                        name = device.name,
                        address = device.address,
                        onClick = { onDeviceSelected(device) },
                        modifier = Modifier.animateItem(),
                        contentModifier = Modifier
                            .height(64.dp)
                            .padding(vertical = 8.dp)
                            .edgePadding()
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun DeviceItem(
    name: String,
    address: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .then(contentModifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(4.dp)
                .background(MaterialTheme.colors.primary)
        )
        Spacer(Modifier.width(12.dp))
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(name)
            Text(
                text = address,
                style = MaterialTheme.typography.body2,
                color = LocalContentColorMediumAlpha
            )
        }
    }
}


@Preview(uiMode = UI_MODE_NIGHT_YES, widthDp = 400, heightDp = 800, showBackground = true)
@Composable
private fun ConnectionScreenPreview() {
    LightClientTheme {
        ConnectionScreen(
            devices = listOf(
                DeviceDesc("ESP32-Home", "FC:81:CC:4F:8E:36", ConnectionType.BLE),
                DeviceDesc("ESP32-Kitchen", "D1:09:75:BA:15:2D", ConnectionType.BLE),
                DeviceDesc("ESP32-Bath", "5A:46:70:63:6E:99", ConnectionType.BLE)
            ),
            onDeviceSelected = { },
            isLoading = false,
            waitingForConnectivity = false,
            errorTime = 0,
            errorMessage = "Не удалось подключиться к контроллеру",
            loadingMessage = "Подключение..."
        )
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES, widthDp = 400, heightDp = 800, showBackground = true)
@Composable
private fun ConnectionScreenPreviewNoDevices() {
    LightClientTheme {
        ConnectionScreen(
            devices = emptyList(),
            onDeviceSelected = { },
            isLoading = false,
            waitingForConnectivity = false,
            errorTime = 0,
            errorMessage = "",
            loadingMessage = ""
        )
    }
}