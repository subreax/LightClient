package com.subreax.lightclient.data.deviceapi.ble

import com.subreax.lightclient.data.deviceapi.DeviceApi

sealed class BleLightEvent {
    class PropertiesChanged(val group: DeviceApi.PropertyGroupId) : BleLightEvent()
}
