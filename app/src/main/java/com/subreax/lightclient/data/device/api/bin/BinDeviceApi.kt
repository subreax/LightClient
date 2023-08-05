package com.subreax.lightclient.data.device.api.bin

import com.subreax.lightclient.LResult
import com.subreax.lightclient.R
import com.subreax.lightclient.data.Property
import com.subreax.lightclient.data.PropertyType
import com.subreax.lightclient.data.device.api.DeviceApi
import com.subreax.lightclient.data.device.api.Event
import com.subreax.lightclient.data.device.repo.PropertyGroup
import com.subreax.lightclient.data.device.socket.Socket
import com.subreax.lightclient.data.device.socket.SocketWrapper
import com.subreax.lightclient.utils.getUtf8String
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import java.nio.ByteBuffer


class BinDeviceApi(
    socket: Socket
) : DeviceApi {
    private val socketWrapper = SocketWrapper(socket)

    override val events: Flow<Event>
        get() = socketWrapper.events

    private val propertySerializers = mapOf(
        PropertyType.FloatNumber to FloatNumberSerializer(),
        PropertyType.FloatSlider to FloatSliderSerializer(),
        PropertyType.FloatSmallHSlider to FloatSmallHSliderSerializer(),
        PropertyType.Color to ColorPropertySerializer(),
        PropertyType.Enum to EnumPropertySerializer(),
        PropertyType.IntNumber to IntPropertySerializer(),
        PropertyType.IntSlider to IntSliderPropertySerializer(),
        PropertyType.IntSmallHSlider to IntSmallHSliderPropertySerializer(),
        PropertyType.Bool to BoolPropertySerializer()
    )


    override suspend fun getPropertiesFromGroup(group: PropertyGroup.Id): LResult<List<Property>> {
        Timber.d("getPropertiesFromGroup $group")
        val req = SocketWrapper.Request(FunctionId.GetPropertiesFromGroup.ordinal) {
            put(group.ordinal.toByte())
        }

        return socketWrapper.doRequest(req).then { res ->
            val props = mutableListOf<Property>()
            while (res.position() < res.limit()) {
                val sz = res.getShort().toInt()
                val oldLimit = res.limit()
                res.limit(res.position() + sz)
                val propertyResult = parsePropertyInfo(res)
                if (propertyResult is LResult.Failure) {
                    return@then propertyResult
                }

                val property = (propertyResult as LResult.Success).value
                val deserializer = propertySerializers[property.type]!!
                val parseValueResult = deserializer.deserializeValue(res, property)
                if (parseValueResult is LResult.Failure) {
                    return@then parseValueResult
                }
                res.limit(oldLimit)
                props.add(property)
            }

            LResult.Success(props)
        }
    }

    private fun parsePropertyInfo(buf: ByteBuffer): LResult<Property> {
        val id = buf.getInt()
        val typeInt = buf.get().toInt()
        if (typeInt >= PropertyType.values().size) {
            return LResult.Failure(R.string.unknown_prop_type, typeInt)
        }
        val type = PropertyType.values()[typeInt]

        buf.get() // reading groupId
        val name = buf.getUtf8String()

        return propertySerializers[type]!!.deserializeInfo(id, name, buf)
    }

    override suspend fun uploadPropertyValue(property: Property): LResult<Unit> {
        val serializer = propertySerializers[property.type]
        if (serializer == null) {
            val msg = "No serializer found for property type ${property.type}"
            Timber.e(msg)
            return LResult.Failure(msg)
        }

        val req = SocketWrapper.Request(FunctionId.SetPropertyValueById.ordinal) {
            putInt(property.id)
            serializer.serializeValue(property, this)
        }

        return socketWrapper.doRequest(req).then { LResult.Success(Unit) }
    }
}