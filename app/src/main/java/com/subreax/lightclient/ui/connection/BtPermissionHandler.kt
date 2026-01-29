package com.subreax.lightclient.ui.connection

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleStartEffect
import com.subreax.lightclient.ui.isPermissionsGranted

class BtPermissionHandler(
    isGrantedState: State<Boolean>,
    private val request: () -> Unit
) {
    val isGranted by isGrantedState

    fun requestPermissions() = request()
}

private class BtPermissionHelper(private val activity: Activity) {
    private var _isGranted = mutableStateOf(checkPermissions())
    val isGranted: State<Boolean>
        get() = _isGranted

    private var shouldShowRationale = false
    private var wasChanged = false

    val shouldNavToSettings: Boolean
        get() = !_isGranted.value && !wasChanged

    fun check(newIsGranted: Boolean = checkPermissions()) {
        val newShouldShowRationale = activity.shouldShowRequestPermissionRationale(PERMISSIONS[0])

        wasChanged =
            _isGranted.value != newIsGranted || shouldShowRationale != newShouldShowRationale
        _isGranted.value = newIsGranted
        shouldShowRationale = newShouldShowRationale
    }

    private fun checkPermissions(): Boolean {
        return activity.isPermissionsGranted(PERMISSIONS.toList())
    }

    companion object {
        val PERMISSIONS =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
            } else {
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            }
    }
}

@Composable
fun rememberBtPermissionHandler(
    onNavToSettings: () -> Unit,
    activity: Activity = LocalContext.current.findActivity()
): BtPermissionHandler {
    val helper = remember { BtPermissionHelper(activity) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { result ->
            val isPermissionsGranted = result.all { (_, isGranted) -> isGranted }
            helper.check(isPermissionsGranted)

            if (helper.shouldNavToSettings) {
                onNavToSettings()
            }
        }
    )

    LifecycleStartEffect(Unit) {
        helper.check()

        onStopOrDispose { }
    }

    return remember(helper) {
        BtPermissionHandler(
            isGrantedState = helper.isGranted,
            request = { permissionLauncher.launch(BtPermissionHelper.PERMISSIONS) }
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
