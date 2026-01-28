package com.subreax.lightclient.ui.connection

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.BottomStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import com.subreax.lightclient.R
import com.subreax.lightclient.data.ConnectionType
import com.subreax.lightclient.data.DeviceDesc
import com.subreax.lightclient.ui.TopBar
import com.subreax.lightclient.ui.isPermissionsGranted
import com.subreax.lightclient.ui.theme.LightClientTheme
import kotlinx.coroutines.launch

private val permissions =
    arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN)

@Composable
fun ConnectionScreen(
    navHome: (DeviceDesc) -> Unit,
    connectionViewModel: ConnectionViewModel = hiltViewModel()
) {
    val permissionsModule = connectionViewModel.permissions

    val connectionState = connectionViewModel.connectionState
    val devices by connectionViewModel.devices.collectAsState(emptyList())

    val snackbarHostState = remember { SnackbarHostState() }
    val activity = LocalContext.current.findActivity()

    var isPermissionsGranted by remember { mutableStateOf(checkPermissions(activity)) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { result ->
            isPermissionsGranted = result.entries.all { (_, isGranted) -> isGranted }
            permissionsModule.onPermissionResult(
                isPermissionsGranted,
                activity.shouldShowRequestPermissionRationale(permissions[0])
            )

            if (permissionsModule.shouldNavToSettings) {
                openPermissionSettingsActivity(activity)
            }
        }
    )

    ConnectionScreen(
        connectionState = connectionState,
        devices = devices,
        onDeviceSelected = connectionViewModel::connect,
        waitingForConnectivity = false, /* todo */
        isBtPermissionsGranted = isPermissionsGranted,
        onRequestPermissions = { permissionLauncher.launch(permissions) },
        snackbarHostState = snackbarHostState
    )

    LifecycleStartEffect(Unit) {
        isPermissionsGranted = checkPermissions(activity)

        permissionsModule.setInitialData(
            isPermissionsGranted,
            activity.shouldShowRequestPermissionRationale(permissions[0])
        )

        onStopOrDispose {  }
    }

    ScanDevicesEffect(
        isPermissionGranted = isPermissionsGranted,
        startScan = connectionViewModel::startScan,
        stopScan = connectionViewModel::stopScan
    )

    LaunchedEffect(Unit) {
        connectionViewModel.events.collect {
            when (it) {
                is UiConnectionEvents.ConnectError -> {
                    launch {
                        snackbarHostState.showSnackbar(it.message.stringValue(activity))
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

            if (!isBtPermissionsGranted) {
                NoPermissionsAlert(
                    requestPermissions = onRequestPermissions,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            } else {
                DevicesList(
                    devices = devices,
                    waitingForConnectivity = waitingForConnectivity,
                    onDeviceSelected = onDeviceSelected,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            }
        }

        LoadingOverlay(connectionState)
    }
}

@Composable
fun ScanDevicesEffect(
    isPermissionGranted: Boolean,
    startScan: () -> Unit,
    stopScan: () -> Unit
) {
    LifecycleStartEffect(Unit) {
        if (isPermissionGranted) {
            startScan()
        }

        onStopOrDispose {
            if (isPermissionGranted) {
                stopScan()
            }
        }
    }

    LaunchedEffect(isPermissionGranted) {
        if (isPermissionGranted) {
            startScan()
        }
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

private fun Context.findActivity(): Activity {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> throw IllegalStateException()
    }
}

private fun checkPermissions(context: Context) = context.isPermissionsGranted(permissions.toList())

private fun openPermissionSettingsActivity(context: Context) {
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    ).also {
        context.startActivity(it)
    }
}
