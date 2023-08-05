package com.subreax.lightclient.data.device.socket.ble

import com.subreax.lightclient.LResult
import com.subreax.lightclient.R
import com.subreax.lightclient.data.device.BleCentralContainer
import com.subreax.lightclient.data.device.socket.Socket
import com.subreax.lightclient.utils.getWrittenData
import com.subreax.lightclient.utils.toPrettyString
import com.welie.blessed.WriteType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import java.nio.ByteBuffer
import java.nio.ByteOrder

private const val PACKET_SZ = 20

class BleSocket(
    centralContainer: BleCentralContainer,
    address: String
) : Socket {
    private val connector = BleConnector(centralContainer, address)
    private val deviceData: BleDeviceData?
        get() = connector.deviceData

    override val connectionState: StateFlow<Socket.ConnectionState>
        get() = connector.connectionState

    override suspend fun connect(): LResult<Unit> {
        return connector.connect()
    }

    override suspend fun disconnect() {
        connector.disconnect()
    }

    override suspend fun send(data: ByteBuffer): LResult<Unit> = withContext(Dispatchers.IO) {
        val isOk = deviceData?.let {
            it.peripheral.writeCharacteristic(
                it.requestCharacteristic,
                data.getWrittenData(),
                WriteType.WITHOUT_RESPONSE
            )
        } ?: return@withContext LResult.Failure(R.string.failed_to_perform_request_disconnected)

        if (isOk) {
            LResult.Success(Unit)
        } else {
            LResult.Failure("Failed to send data")
        }
    }

    private fun isConnected() = deviceData != null

    override suspend fun receive(): LResult<ByteBuffer> = withContext(Dispatchers.IO) {
        val deviceData = deviceData
            ?: return@withContext LResult.Failure(R.string.failed_to_perform_request_disconnected)

        val packetChannel = deviceData.callback.packetChannel
        val firstPacket = packetChannel.receive()
        val bodySz = firstPacket.getShort().toInt()
        firstPacket.position(0)

        val totalPacketsCount = bytesToPacketsCount(bodySz + 2)
        val body = ByteBuffer.allocate(totalPacketsCount * PACKET_SZ)
            .order(ByteOrder.LITTLE_ENDIAN)
        body.put(firstPacket)
        Timber.d("header packet { bodySz: $bodySz; packetsCount: $totalPacketsCount }: ${firstPacket.toPrettyString(0, 20)}")

        var packetsReceived = 1
        try {
            while (packetsReceived < totalPacketsCount) {
                val packet = withTimeout(2000L) {
                    packetChannel.receive()
                }
                packetsReceived++
                body.put(packet)
                Timber.d("new packet [$packetsReceived/$totalPacketsCount]: ${packet.toPrettyString(0, packet.limit())}")
            }
        } catch (ex: TimeoutCancellationException) {
            return@withContext if (isConnected()) {
                LResult.Failure(
                    R.string.failed_to_receive_response_due_to_packet_loss,
                    packetsReceived,
                    totalPacketsCount
                )
            } else {
                LResult.Failure(R.string.failed_to_perform_request_disconnected)
            }
        }

        body.position(2).limit(bodySz+2)
        LResult.Success(body)
    }

    private fun bytesToPacketsCount(bytes: Int) = (bytes + PACKET_SZ - 1) / PACKET_SZ
}