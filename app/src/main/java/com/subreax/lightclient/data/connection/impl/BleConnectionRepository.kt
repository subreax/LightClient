package com.subreax.lightclient.data.connection.impl

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import com.subreax.lightclient.LResult
import com.subreax.lightclient.data.DeviceDesc
import com.subreax.lightclient.data.connection.ConnectionRepository
import com.subreax.lightclient.data.deviceapi.DeviceApi
import com.subreax.lightclient.data.state.AppEventId
import com.subreax.lightclient.data.state.AppStateId
import com.subreax.lightclient.data.state.ApplicationState
import com.subreax.lightclient.ui.isPermissionGranted
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class BleConnectionRepository(
    private val appContext: Context,
    private val appState: ApplicationState,
    private val deviceApi: DeviceApi
) : ConnectionRepository {
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private val btAdapter: BluetoothAdapter
    private var scanBondedDevicesJob: Job? = null

    private val _devices = MutableStateFlow<List<DeviceDesc>>(emptyList())
    override val devices: Flow<List<DeviceDesc>>
        get() = _devices

    private var selectedDeviceDesc: DeviceDesc? = null


    init {
        val btManager = appContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        btAdapter = btManager.adapter!!

        coroutineScope.launch {
            appState.stateId.collect { state ->
                if (state == AppStateId.WaitingForConnectivity) {
                    _devices.value = emptyList()
                }

                if (state == AppStateId.Disconnected) {
                    startScanningBondedDevices()
                }
                else {
                    stopScanningBondedDevices()
                }
            }
        }

        coroutineScope.launch {
            deviceApi.connectionStatus.collect {
                if (it == DeviceApi.ConnectionStatus.ConnectionLost) {
                    appState.notifyEvent(AppEventId.ConnectionLost)
                }
            }
        }
    }


    override suspend fun selectDevice(deviceDesc: DeviceDesc) {
        selectedDeviceDesc = deviceDesc
        appState.notifyEvent(AppEventId.DeviceSelected)
    }

    override suspend fun connect(): LResult<Unit> {
        return selectedDeviceDesc?.let { device ->
            val result = deviceApi.connect(device)
            if (result is LResult.Success) {
                appState.notifyEvent(AppEventId.Connected)
            }
            result
        } ?: LResult.Failure("Device is not picked")
    }

    override suspend fun disconnect() {
        deviceApi.disconnect()
        appState.notifyEvent(AppEventId.Disconnected)
    }

    @SuppressLint("MissingPermission")
    private fun CoroutineScope.startScanningBondedDevices() {
        scanBondedDevicesJob = launch {
            while (isActive) {
                if (hasBtConnectPermission()) {
                    val devicesList = btAdapter.bondedDevices.map { DeviceDesc(it.name, it.address) }
                    _devices.value = devicesList
                }
                delay(2000)
            }
        }
    }

    private fun CoroutineScope.stopScanningBondedDevices() {
        scanBondedDevicesJob?.cancel()
    }

    private fun hasBtConnectPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return true
        }

        return appContext.isPermissionGranted(Manifest.permission.BLUETOOTH_CONNECT)
    }

    companion object {
        private const val TAG = "BleConnectionRepo"
    }
}