package com.subreax.lightclient.data.device.api.old

import android.graphics.Color
import com.subreax.lightclient.LResult
import com.subreax.lightclient.data.Property
import com.subreax.lightclient.data.device.api.DeviceApi
import com.subreax.lightclient.data.device.api.Event
import com.subreax.lightclient.data.device.repo.PropertyGroup
import com.subreax.lightclient.data.device.socket.Socket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.nio.ByteBuffer
import kotlin.math.roundToInt

private data class Response(
    var status: Int,
    var data: List<String>
)

class OldDeviceApi(
    private val socket: Socket
) : DeviceApi {
    private val _events = MutableSharedFlow<Event>()
    override val events: Flow<Event>
        get() = _events


    private val brightnessProp = Property.FloatSlider(1, "Яркость", 1f, 0f, 1f)
    private val sceneProp = Property.Enum(
        id = 2,
        name = "Сцена",
        values = listOf(
            "Steady Primary",
            "Steady Secondary",
            "Transition",
            "Rainbow",
            "Smoke"
        ),
        initialValue = 0
    )
    private val primaryColorProp = Property.Color(3, "Основной", Color.RED)
    private val secondaryColorProp = Property.Color(4, "Вторичный", Color.GREEN)
    //private val motionSensorProp = Property.Bool(5, "Датчик движения", false)

    private val globalProps = listOf(
        brightnessProp,
        sceneProp,
        primaryColorProp,
        secondaryColorProp,
        //motionSensorProp
    )

    override suspend fun getPropertiesFromGroup(group: PropertyGroup.Id): LResult<List<Property>> {
        return if (group == PropertyGroup.Id.General) {
            brightnessProp.current.value = fetchBrightness()
            sceneProp.currentValue.value = fetchSceneId()
            primaryColorProp.color.value = fetchColor(1)
            secondaryColorProp.color.value = fetchColor(2)
            //motionSensorProp.toggled.value = fetchMotionSensorState()
            LResult.Success(globalProps)
        } else {
            LResult.Success(emptyList())
        }
    }

    override suspend fun uploadPropertyValue(property: Property): LResult<Unit> {
        return when (property.id) {
            BRIGHTNESS_PROP_ID -> sendBrightness(property)
            SCENE_PROP_ID -> sendSceneId(property)
            PRIMARY_COLOR_PROP_ID -> sendColor(1, property)
            SECONDARY_COLOR_PROP_ID -> sendColor(2, property)
            //MOTION_SENSOR_PROP_ID -> sendMotionSensorState(property)
            else -> LResult.Failure("Unknown property id: ${property.id}")
        }
    }

    override suspend fun ping(): LResult<Unit> {
        return doRequest("echo[]").then { LResult.Success(Unit) }
    }

    private suspend fun sendBrightness(property: Property): LResult<Unit> {
        val brightness = (property as Property.BaseFloat).current.value
        return sendBrightness(brightness)
    }

    private suspend fun sendBrightness(brightness: Float): LResult<Unit> {
        return doRequest("b", (brightness * 255.0f).roundToInt().toString())
            .then { LResult.Success(Unit) }
    }

    private suspend fun fetchBrightness(): Float {
        val result = doRequest("gb")
        return if (result is LResult.Success) {
            val resp = result.value
            resp.data[0].toInt() / 255.0f
        } else {
            0f
        }
    }

    private suspend fun sendColor(idx: Int, property: Property): LResult<Unit> {
        val prop = property as Property.Color
        return sendColor(idx, prop.color.value)
    }

    private suspend fun sendColor(idx: Int, color: Int): LResult<Unit> {
        val colorHex = color.toColorHexString().lowercase()
        return doRequest("c", idx.toString(), colorHex)
            .then { LResult.Success(Unit) }
    }

    private suspend fun fetchColor(idx: Int): Int {
        val res = doRequest("gc", idx.toString())
        return if (res is LResult.Success) {
            res.value.data[0].toColor()
        } else {
            (0xff000000).toInt()
        }
    }

    private suspend fun fetchSceneId(): Int {
        val res = doRequest("gm")
        return if (res is LResult.Success) {
            res.value.data[0].toInt()
        } else {
            Timber.d("Failed to fetch scene id", (res as LResult.Failure).message.toString())
            0
        }
    }

    private suspend fun fetchMotionSensorState(): Boolean {
        val res = doRequest("getSppState", "1")
        return if (res is LResult.Success) {
            val value = res.value.data[0].toInt()
            value > 0
        } else {
            false
        }
    }

    private suspend fun sendMotionSensorState(property: Property): LResult<Unit> {
        val prop = property as Property.Bool
        val enabled = prop.toggled.value
        val cmd = if (enabled) "es" else "ds"
        return doRequest(cmd, "1")
            .then { LResult.Success(Unit) }
    }

    private suspend fun sendSceneId(property: Property): LResult<Unit> {
        val prop = property as Property.Enum
        return sendSceneId(prop.currentValue.value)
    }

    private suspend fun sendSceneId(sceneId: Int): LResult<Unit> {
        return doRequest("m", sceneId.toString())
            .then { LResult.Success(Unit) }
    }

    private fun buildCommand(name: String, vararg args: String): ByteBuffer {
        val sb = StringBuilder()
        sb.append(name).append('[')
        for (arg in args) {
            sb.append(arg).append(';')
        }
        if (args.isNotEmpty())
            sb.setCharAt(sb.length - 1, ']')
        else
            sb.append(']')

        val buf = ByteBuffer.allocate(sb.length)
        for (ch in sb) {
            buf.put(ch.code.toByte())
        }
        return buf
    }

    private fun parseResponse(raw: ByteBuffer): LResult<Response> {
        val sb = StringBuilder(raw.toAsciiString())
        if (!sb.endsWith("]")) {
            return LResult.Failure("Failed to parse response")
        }

        val e = sb.indexOf('[')
        val status = sb.substring(0, e).toInt()
        val rawData = sb.substring(e + 1, sb.length - 1)
        val data = rawData.split(";")
        return LResult.Success(Response(status, data))
    }

    private fun ByteBuffer.toAsciiString(): String {
        return String(array(), 0, limit(), Charsets.US_ASCII)
    }

    private suspend fun doRequest(name: String, vararg args: String) = withContext(Dispatchers.IO) {
        val cmd = buildCommand(name, *args)
        Timber.d(">> request: ${cmd.toAsciiString()}")
        socket.send(cmd)
            .then { socket.receive() }
            .then {
                Timber.d("<< response: ${it.toAsciiString()}")
                LResult.Success(it)
            }
            .then { parseResponse(it) }
            .onFailure {
                Timber.e("Failed to do request '$name': ${it.message}")
            }
    }

    companion object {
        private const val BRIGHTNESS_PROP_ID = 1
        private const val SCENE_PROP_ID = 2
        private const val PRIMARY_COLOR_PROP_ID = 3
        private const val SECONDARY_COLOR_PROP_ID = 4
        private const val MOTION_SENSOR_PROP_ID = 5
    }
}

private fun Int.toColorHexString(): String {
    val builder = StringBuilder(6)
    for (i in 0 until 6) {
        val digit = (this and (0xf shl 4 * (5 - i))) shr 4 * (5 - i)
        builder.append(digit.digitToChar(16))
    }
    return builder.toString()
}

private fun String.toColor(): Int {
    return Color.parseColor("#ff$this")
}