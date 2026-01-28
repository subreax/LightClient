package com.subreax.lightclient.ui.connection

data class PermissionData(val isGranted: Boolean, val shouldShowRationale: Boolean)

class PermissionsModule {
    private var data = PermissionData(
        isGranted = false,
        shouldShowRationale = false
    )

    var shouldNavToSettings = false
        private set

    fun setInitialData(isGranted: Boolean, shouldShowRationale: Boolean) {
        data = PermissionData(isGranted, shouldShowRationale)
    }

    fun onPermissionResult(isGranted: Boolean, shouldShowRationale: Boolean) {
        val oldData = data
        val newData = PermissionData(isGranted, shouldShowRationale)
        data = newData

        shouldNavToSettings =
            !isGranted && oldData.shouldShowRationale == false && newData.shouldShowRationale == false
    }
}