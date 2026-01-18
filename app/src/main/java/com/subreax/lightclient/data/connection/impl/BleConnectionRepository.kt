package com.subreax.lightclient.data.connection.impl

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import com.subreax.lightclient.data.ConnectionType
import com.subreax.lightclient.data.DeviceDesc
import com.subreax.lightclient.data.connection.ConnectionRepository
import com.subreax.lightclient.data.device.BleCentralContainer
import com.subreax.lightclient.data.device.BleConnectionEvent
import com.subreax.lightclient.data.device.Device
import com.subreax.lightclient.data.device.repo.DeviceRepository
import com.welie.blessed.Transport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber

class BleConnectionRepository(
    private val appContext: Context,
    private val deviceRepository: DeviceRepository,
    private val bleCentralContainer: BleCentralContainer
) : ConnectionRepository {
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val btAdapter: BluetoothAdapter

    private val devicesMap = mutableMapOf<String, DeviceDesc>()
    private val _devices = MutableStateFlow<List<DeviceDesc>>(emptyList())
    override val devices: Flow<List<DeviceDesc>>
        get() = _devices


    private enum class Event {
        ConnectivityChanged, Disconnected, Connected
    }

    private val events = Channel<Event>()

    init {
        val btManager = appContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        btAdapter = btManager.adapter!!

        coroutineScope.launch {
            while (isActive) {
                val event = events.receive()
                val isBtEnabled = bleCentralContainer.manager.isBluetoothEnabled

                when (event) {
                    Event.ConnectivityChanged -> {
                        if (isBtEnabled) {
                            startScan()
                        }
                    }

                    Event.Connected -> {
                        stopScan()
                    }

                    Event.Disconnected -> {
                        if (isBtEnabled) {
                            startScan()
                        }
                    }
                }
            }
        }

        coroutineScope.launch {
            if (bleCentralContainer.manager.isBluetoothEnabled) {
                events.send(Event.ConnectivityChanged)
            }
        }

        coroutineScope.launch {
            bleCentralContainer.connectionListener.status.collect {
                if (it == BleConnectionEvent.NoConnectivity || it == BleConnectionEvent.HasConnectivity) {
                    events.send(Event.ConnectivityChanged)
                }
            }
        }

        coroutineScope.launch {
            bleCentralContainer.connectionListener.discoveredPeripherals.collect {
                val connectionType = if (it.peripheral.transport == Transport.LE) {
                    ConnectionType.BLE
                } else {
                    ConnectionType.BT_CLASSIC
                }

                if (it.peripheral.name.isNotBlank()) {
                    devicesMap[it.peripheral.address] = DeviceDesc(
                        it.peripheral.name,
                        it.peripheral.address,
                        connectionType
                    )
                    _devices.value = devicesMap.values.toList()
                }
            }
        }
    }

    override suspend fun connect(deviceDesc: DeviceDesc) =
        deviceRepository.connect(deviceDesc).onEach {
            if (it == Device.State.Ready) {
                events.send(Event.Connected)
            } else if (it == Device.State.Disconnected || it == Device.State.NoConnectivity) {
                events.send(Event.Disconnected)
            }
        }

    override suspend fun disconnect() {
        if (deviceRepository.isConnected()) {
            deviceRepository.getDevice().disconnect()
        }
    }

    private fun startScan() {
        Timber.d("Start scan")
        bleCentralContainer.manager.scanForPeripherals()
    }

    private fun stopScan() {
        Timber.d("Stop scan")
        bleCentralContainer.manager.stopScan()
    }
}
