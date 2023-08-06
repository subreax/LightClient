package com.subreax.lightclient.data.device.impl

import com.subreax.lightclient.LResult
import com.subreax.lightclient.data.DeviceDesc
import com.subreax.lightclient.data.Property
import com.subreax.lightclient.data.device.Device
import com.subreax.lightclient.data.device.api.DeviceApi
import com.subreax.lightclient.data.device.api.Event
import com.subreax.lightclient.data.device.repo.PropertyGroup
import com.subreax.lightclient.data.device.socket.Socket
import com.subreax.lightclient.except.ConnectionException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber


open class DeviceImpl(
    private val deviceDesc: DeviceDesc,
    private val socket: Socket,
    private val api: DeviceApi
) : Device {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val _state = MutableStateFlow(Device.State.Disconnected)
    override val state: StateFlow<Device.State>
        get() = _state

    private val _errors = MutableSharedFlow<LResult.Failure>()
    override val errors: Flow<LResult.Failure>
        get() = _errors

    private val globalPropGroup = PropertyGroup(PropertyGroup.Id.General, api)
    override val globalProperties: StateFlow<List<Property>>
        get() = globalPropGroup.props

    private val scenePropGroup = PropertyGroup(PropertyGroup.Id.Scene, api)
    override val sceneProperties: StateFlow<List<Property>>
        get() = scenePropGroup.props

    private var autoFetch = false

    init {
        coroutineScope.launch {
            socket.connectionState.collect {
                when (it) {
                    Socket.ConnectionState.Connecting -> {
                        _state.value = Device.State.Connecting
                    }

                    Socket.ConnectionState.Connected -> {
                        onConnectState()
                    }

                    Socket.ConnectionState.Disconnected -> {
                        _state.value = Device.State.Disconnected
                    }

                    Socket.ConnectionState.NoConnectivity -> {
                        _state.value = Device.State.NoConnectivity
                    }
                }
            }
        }

        coroutineScope.launch {
            api.events.collect {
                if (it is Event.PropertiesChanged) {
                    if (it.group == PropertyGroup.Id.Scene) {
                        scenePropGroup.fetch()
                    } else if (it.group == PropertyGroup.Id.General) {
                        globalPropGroup.fetch()
                    }
                }
            }
        }
    }

    private suspend fun onConnectState() {
        if (autoFetch) {
            val res = fetchData()
            if (res is LResult.Success) {
                _state.value = Device.State.Ready
            } else {
                _errors.emit(res as LResult.Failure)
                disconnect()
            }
        }
    }

    override suspend fun connect() = withContext(Dispatchers.IO) {
        flow {
            _state.value = Device.State.Connecting
            emit(Device.State.Connecting)
            socket.connect()
                .then {
                    emit(Device.State.Fetching)
                    fetchData()
                }
                .onSuccess {
                    _state.value = Device.State.Ready
                    autoFetch = true
                    emit(Device.State.Ready)
                }
                .onFailure {
                    disconnect()
                    throw ConnectionException(it.message)
                }
        }
    }

    private suspend fun fetchData(): LResult<Unit> = withContext(Dispatchers.IO) {
        Timber.d("Fetching data")

        _state.value = Device.State.Fetching
        val job1 = async { globalPropGroup.fetch() }
        val job2 = async { scenePropGroup.fetch() }

        val res1 = job1.await()
        val res2 = job2.await()

        if (res1 is LResult.Failure) {
            res1
        } else {
            res2
        }
    }

    override suspend fun disconnect() {
        autoFetch = false
        socket.disconnect()
    }

    override fun findPropertyById(id: Int): Property? {
        val sceneProp = scenePropGroup.findById(id)
        if (sceneProp != null) {
            return sceneProp
        }
        return globalPropGroup.findById(id)
    }

    override fun getDeviceDesc(): DeviceDesc {
        return deviceDesc
    }
}