package com.subreax.lightclient.data

enum class ConnectionType {
    BLE, BT_CLASSIC
}

data class DeviceDesc(
    val name: String,
    val address: String,
    val connectionType: ConnectionType
)