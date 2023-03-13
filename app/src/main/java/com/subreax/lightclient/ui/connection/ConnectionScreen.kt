package com.subreax.lightclient.ui.connection

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.subreax.lightclient.R
import com.subreax.lightclient.data.Device
import com.subreax.lightclient.ui.LocalContentColorMediumAlpha
import com.subreax.lightclient.ui.TopBar
import com.subreax.lightclient.ui.edgePadding
import com.subreax.lightclient.ui.theme.LightClientTheme


@Composable
fun ConnectionScreen(
    navHome: (Device) -> Unit,
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
        onDevicePicked = connectionViewModel::connect,
        isLoading = uiState.loading,
        waitingForConnectivity = uiState.waitingForConnectivity,
        errorTime = uiState.errorMsg.time,
        errorMessage = uiState.errorMsg.msg.stringValue(),
        loadingMessage = uiState.loadingMsg.stringValue()
    )
}

@Composable
fun ConnectionScreen(
    devices: List<Device>,
    onDevicePicked: (Device) -> Unit,
    isLoading: Boolean,
    waitingForConnectivity: Boolean,
    errorTime: Long,
    errorMessage: String,
    loadingMessage: String
) {
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorTime) {
        if (errorMessage.isNotEmpty()) {
            snackbarHostState.showSnackbar(errorMessage)
        }
    }

    Box(Modifier.fillMaxSize()) {
        if (isLoading) {
            LoadingOverlay(message = loadingMessage)
        }

        Column(
            Modifier
                .verticalScroll(scrollState)
                .fillMaxWidth()
        ) {
            TopBar(title = stringResource(R.string.connecting_to_controller))
            Text(
                text = stringResource(R.string.pick_controller),
                color = LocalContentColorMediumAlpha,
                modifier = Modifier.edgePadding()
            )
            Divider(
                Modifier
                    .fillMaxWidth(2.0f / 3.0f)
                    .padding(vertical = 16.dp), thickness = 2.dp
            )

            if (!waitingForConnectivity && devices.isNotEmpty()) {
                devices.forEach { device ->
                    DeviceItem(
                        name = device.name,
                        address = device.address,
                        onClick = { onDevicePicked(device) }
                    )
                }
            } else if (!waitingForConnectivity) {
                Text(stringResource(R.string.devices_not_found), modifier = Modifier.edgePadding())
            } else {
                Text(
                    stringResource(R.string.waiting_for_connectivity),
                    modifier = Modifier.edgePadding()
                )
            }
        }

        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(BottomStart))
    }
}

@Composable
fun LoadingOverlay(message: String) {
    Box(
        modifier = Modifier
            .background(Color(0xAE000000))
            .fillMaxSize()
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
fun DeviceItem(name: String, address: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .edgePadding()
            .height(48.dp),
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
fun ConnectionScreenPreview() {
    LightClientTheme {
        ConnectionScreen(
            devices = listOf(
                Device("ESP32-Home", "FC:81:CC:4F:8E:36"),
                Device("ESP32-Kitchen", "D1:09:75:BA:15:2D"),
                Device("ESP32-Bath", "5A:46:70:63:6E:99")
            ),
            onDevicePicked = { },
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
fun ConnectionScreenPreviewNoDevices() {
    LightClientTheme {
        ConnectionScreen(
            devices = emptyList(),
            onDevicePicked = { },
            isLoading = false,
            waitingForConnectivity = false,
            errorTime = 0,
            errorMessage = "",
            loadingMessage = ""
        )
    }
}