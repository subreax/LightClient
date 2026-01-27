package com.subreax.lightclient.ui.connection

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.Divider
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.BottomStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.subreax.lightclient.R
import com.subreax.lightclient.data.ConnectionType
import com.subreax.lightclient.data.DeviceDesc
import com.subreax.lightclient.ui.TopBar
import com.subreax.lightclient.ui.theme.LightClientTheme
import kotlinx.coroutines.launch


@Composable
fun ConnectionScreen(
    navHome: (DeviceDesc) -> Unit,
    connectionViewModel: ConnectionViewModel = hiltViewModel()
) {
    val connectionState = connectionViewModel.connectionState
    val devices by connectionViewModel.devices.collectAsState(emptyList())

    val snackbarHostState = remember { SnackbarHostState() }
    val ctx = LocalContext.current

    ConnectionScreen(
        connectionState = connectionState,
        devices = devices,
        onDeviceSelected = connectionViewModel::connect,
        waitingForConnectivity = false, /* todo */
        isBtPermissionsGranted = true,
        onRequestPermissions = {},
        snackbarHostState = snackbarHostState
    )

    LaunchedEffect(Unit) {
        connectionViewModel.events.collect {
            when (it) {
                is UiConnectionEvents.ConnectError -> {
                    launch {
                        snackbarHostState.showSnackbar(it.message.stringValue(ctx))
                    }
                }

                is UiConnectionEvents.NavHome -> {
                    navHome(it.deviceDesc)
                }
            }
        }
    }
}

@Composable
private fun ScreenBase(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(modifier) {
        Column(Modifier.systemBarsPadding()) {
            content()
        }

        SnackbarHost(
            hostState = snackbarHostState, modifier = Modifier.align(BottomStart)
        )
    }
}

@Composable
private fun ConnectionScreen(
    connectionState: UiConnectionState,
    devices: List<DeviceDesc>,
    onDeviceSelected: (DeviceDesc) -> Unit,
    waitingForConnectivity: Boolean,
    isBtPermissionsGranted: Boolean,
    onRequestPermissions: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Box(Modifier.fillMaxSize()) {
        ScreenBase(
            modifier = Modifier.fillMaxSize(),
            snackbarHostState = snackbarHostState
        ) {
            TopBar(
                title = stringResource(R.string.connecting_to_controller),
                subtitle = {
                    Text(stringResource(R.string.pick_controller))
                },
                insets = WindowInsets(0.dp)
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

        LoadingOverlay(connectionState)
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES, widthDp = 400, heightDp = 800, showBackground = true)
@Composable
private fun ConnectionScreenPreview() {
    LightClientTheme {
        ConnectionScreen(
            waitingForConnectivity = false,
            connectionState = UiConnectionState.Connecting("ESP32-Home"),
            devices = listOf(
                DeviceDesc("ESP32-Home", "FC:81:CC:4F:8E:36", ConnectionType.BLE),
                DeviceDesc("ESP32-Kitchen", "D1:09:75:BA:15:2D", ConnectionType.BLE),
                DeviceDesc("ESP32-Bath", "5A:46:70:63:6E:99", ConnectionType.BLE)
            ),
            onDeviceSelected = { },
            isBtPermissionsGranted = true,
            onRequestPermissions = {}
        )
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES, widthDp = 400, heightDp = 800, showBackground = true)
@Composable
private fun ConnectionScreenPreviewNoDevices() {
    LightClientTheme {
        ConnectionScreen(
            waitingForConnectivity = false,
            connectionState = UiConnectionState.Idle,
            devices = emptyList(),
            onDeviceSelected = { },
            isBtPermissionsGranted = true,
            onRequestPermissions = {}
        )
    }
}