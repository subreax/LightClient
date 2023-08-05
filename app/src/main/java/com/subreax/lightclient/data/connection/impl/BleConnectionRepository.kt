package com.subreax.lightclient.data.connection.impl

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import android.util.Log
import com.subreax.lightclient.data.DeviceDesc
import com.subreax.lightclient.data.connection.ConnectionProgress
import com.subreax.lightclient.data.connection.ConnectionRepository
import com.subreax.lightclient.data.device.repo.DeviceRepository
import com.subreax.lightclient.data.device.Device
import com.subreax.lightclient.ui.isPermissionGranted
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow

class BleConnectionRepository(
    private val appContext: Context,
    private val deviceRepository: DeviceRepository
) : ConnectionRepository {
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private val btAdapter: BluetoothAdapter
    private var scanBondedDevicesJob: Job? = null

    private val _devices = MutableStateFlow<List<DeviceDesc>>(emptyList())
    override val devices: Flow<List<DeviceDesc>>
        get() = _devices


    init {
        val btManager = appContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        btAdapter = btManager.adapter!!



        coroutineScope.launch {
            // todo: listen for bluetooth state
            startScanningBondedDevices()

            /*appState.stateId.collect { state ->
                if (state == AppStateId.WaitingForConnectivity) {
                    _devices.value = emptyList()
                }

                if (state == AppStateId.Disconnected) {
                    startScanningBondedDevices()
                }
                else {
                    stopScanningBondedDevices()
                }
            }*/
        }
    }

    override suspend fun connect(deviceDesc: DeviceDesc) = flow {
        if (btAdapter.isEnabled) {
            deviceRepository.connect(deviceDesc).collect {
                Log.d(TAG, it.toString())
                val state = when (it) {
                    Device.State.Connecting -> ConnectionProgress.Connecting
                    Device.State.Fetching -> ConnectionProgress.Fetching
                    Device.State.Ready -> ConnectionProgress.Done
                    Device.State.Disconnected -> ConnectionProgress.FailedToConnect
                }
                emit(state)
            }
        }
        else {
            emit(ConnectionProgress.NoConnectivity)
        }
    }

    override suspend fun disconnect() {
        if (deviceRepository.isConnected()) {
            deviceRepository.getDevice().disconnect()
        }
    }

    @SuppressLint("MissingPermission")
    private fun CoroutineScope.startScanningBondedDevices() {
        scanBondedDevicesJob = launch {
            while (isActive) {
                if (hasBtConnectPermission()) {
                    _devices.value = btAdapter.bondedDevices
                        .filter { it.isBle()  }
                        .map { it.descriptor() }
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

@SuppressLint("MissingPermission")
private fun BluetoothDevice.isBle() =
    type == BluetoothDevice.DEVICE_TYPE_LE ||
    type == BluetoothDevice.DEVICE_TYPE_DUAL

@SuppressLint("MissingPermission")
private fun BluetoothDevice.descriptor() = DeviceDesc(name, address)