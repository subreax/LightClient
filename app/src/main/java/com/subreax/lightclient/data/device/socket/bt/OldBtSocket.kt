package com.subreax.lightclient.data.device.socket.bt

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import com.subreax.lightclient.LResult
import com.subreax.lightclient.data.connectivity.ConnectivityObserver
import com.subreax.lightclient.data.connectivity.impl.BtConnectivityObserver
import com.subreax.lightclient.data.device.socket.Socket
import com.subreax.lightclient.utils.getWrittenData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException
import java.nio.ByteBuffer
import java.util.UUID


class OldBtSocket(
    context: Context,
    private val btConnectivityObserver: BtConnectivityObserver,
    private val address: String
) : Socket {
    private val adapter =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager)
            .adapter

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val _connState = MutableStateFlow(Socket.ConnectionState.Disconnected)
    override val connectionState: StateFlow<Socket.ConnectionState>
        get() = _connState

    private var nativeSocket: BluetoothSocket? = null

    init {
        coroutineScope.launch {
            btConnectivityObserver.status.collect {
                Timber.d("New event: $it")
                when (it) {
                    ConnectivityObserver.Event.NotAvailable -> {
                        disconnect()
                        _connState.value = Socket.ConnectionState.NoConnectivity
                    }
                    ConnectivityObserver.Event.Available -> {
                        _connState.value = Socket.ConnectionState.Disconnected
                    }
                    ConnectivityObserver.Event.Disconnected -> {
                        // connection lost
                        if (nativeSocket != null) {
                            disconnect()
                        }
                        if (adapter.isEnabled) {
                            _connState.value = Socket.ConnectionState.Disconnected
                        }
                    }
                    else -> {}
                    /*ConnectivityObserver.Event.Connected -> {
                        _connState.value = Socket.ConnectionState.Connected
                    }*/
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override suspend fun connect(): LResult<Unit> = withContext(Dispatchers.IO) {
        _connState.value = Socket.ConnectionState.Connecting
        return@withContext try {
            val remote = adapter.getRemoteDevice(address)
            val nativeSocket = remote.createRfcommSocketToServiceRecord(DEVICE_UUID)
            nativeSocket.connect()
            this@OldBtSocket.nativeSocket = nativeSocket
            _connState.value = Socket.ConnectionState.Connected
            LResult.Success(Unit)
        } catch (ex: IOException) {
            if (adapter.isEnabled) {
                _connState.value = Socket.ConnectionState.Disconnected
            }
            Timber.e(ex, "Failed to connect")
            LResult.Failure(ex.message ?: "Failed to connect")
        }
    }

    override suspend fun disconnect() {
        try {
            nativeSocket?.close()
        } catch (_: Throwable) {}
        nativeSocket = null
    }

    override suspend fun send(data: ByteBuffer): LResult<Unit> = withContext(Dispatchers.IO) {
        val outputStream = nativeSocket?.outputStream
            ?: return@withContext LResult.Failure("Socket is disconnected")

        try {
            outputStream.write(data.getWrittenData())
            LResult.Success(Unit)
        }
        catch (ex: IOException) {
            Timber.e(ex, "Failed to send data")
            LResult.Failure("Failed to send data: ${ex.message}")
        }
    }

    override suspend fun receive(): LResult<ByteBuffer> = withContext(Dispatchers.IO) {
        val inputStream = nativeSocket?.inputStream
            ?: return@withContext LResult.Failure("Socket is disconnected")

        try {
            val buf = ByteBuffer.allocate(64)
            var fetching = true
            while (fetching) {
                val b = inputStream.read()
                if (b == -1) {
                    break
                }
                if (b.toChar() == ']') {
                    fetching = false
                }
                buf.put(b.toByte())
            }
            LResult.Success(ByteBuffer.wrap(buf.array(), 0, buf.position()))
        }
        catch (ex: IOException) {
            Timber.e(ex, "Failed to receive data")
            LResult.Failure("Failed to receive data: ${ex.message}")
        }
    }

    companion object {
        private val DEVICE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }
}