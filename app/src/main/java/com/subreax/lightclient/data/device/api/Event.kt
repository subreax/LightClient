package com.subreax.lightclient.data.device.api

import com.subreax.lightclient.data.device.repo.PropertyGroup

sealed class Event {
    class PropertiesChanged(val group: PropertyGroup.Id) : Event()
    object Unknown : Event()
}
