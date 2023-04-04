package com.subreax.lightclient.data.deviceapi.ble

import com.subreax.lightclient.data.deviceapi.DeviceApi

sealed class BleLightNotification {
    class PropertiesChanged(val group: DeviceApi.PropertyGroup) : BleLightNotification()
}

