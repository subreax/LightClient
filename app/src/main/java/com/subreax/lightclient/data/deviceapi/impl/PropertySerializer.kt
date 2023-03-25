package com.subreax.lightclient.data.deviceapi.impl

import android.graphics.Color
import com.subreax.lightclient.LResult
import com.subreax.lightclient.data.Property
import com.subreax.lightclient.ui.UiText
import java.nio.BufferUnderflowException
import java.nio.ByteBuffer
import kotlin.math.roundToInt

interface PropertySerializer {
    fun deserializeInfo(id: Int, name: String, buf: ByteBuffer): LResult<Property>
    fun serializeValue(property: Property, out: ByteBuffer): LResult<Unit>
    fun deserializeValue(buf: ByteBuffer, target: Property): LResult<Unit>
}

class FloatRangePropertySerializer : PropertySerializer {
    override fun deserializeInfo(id: Int, name: String, buf: ByteBuffer): LResult<Property> {
        return try {
            val min = buf.getInt() / 32768.0f
            val max = buf.getInt() / 32768.0f
            LResult.Success(
                Property.FloatRangeProperty(id, name, min, max, min)
            )
        } catch (ex: BufferUnderflowException) {
            LResult.Failure(UiText.Hardcoded("No info provided for min and max values"))
        }
    }

    override fun serializeValue(property: Property, out: ByteBuffer): LResult<Unit> {
        val frp = property as Property.FloatRangeProperty
        val value = frp.current.value
        val q15 = (value * 32768).roundToInt()

        return try {
            out.putInt(q15)
            LResult.Success(Unit)
        } catch (ex: BufferUnderflowException) {
            LResult.Failure(UiText.Hardcoded("Failed to write value of FloatRange"))
        }
    }

    override fun deserializeValue(buf: ByteBuffer, target: Property): LResult<Unit> {
        val frp = target as Property.FloatRangeProperty

        return try {
            val value = buf.getInt() / 32768.0f
            frp.current.value = value
            LResult.Success(Unit)
        } catch (ex: BufferUnderflowException) {
            LResult.Failure(UiText.Hardcoded("No value"))
        }
    }
}


class ColorPropertySerializer : PropertySerializer {
    override fun deserializeInfo(id: Int, name: String, buf: ByteBuffer): LResult<Property> {
        return LResult.Success(
            Property.ColorProperty(id, name, Color.BLACK)
        )
    }

    override fun serializeValue(property: Property, out: ByteBuffer): LResult<Unit> {
        val colorProperty = property as Property.ColorProperty
        val color = colorProperty.color.value

        return try {
            out.putInt(color)
            return LResult.Success(Unit)
        } catch (ex: BufferUnderflowException) {
            return LResult.Failure(UiText.Hardcoded("Failed to write value of ColorRange"))
        }
    }

    override fun deserializeValue(buf: ByteBuffer, target: Property): LResult<Unit> {
        val colorProp = target as Property.ColorProperty

        return try {
            colorProp.color.value = buf.getInt()
            LResult.Success(Unit)
        } catch (ex: BufferUnderflowException) {
            LResult.Failure(UiText.Hardcoded("No value"))
        }
    }
}


class EnumPropertySerializer : PropertySerializer {
    override fun deserializeInfo(id: Int, name: String, buf: ByteBuffer): LResult<Property> {
        val values = mutableListOf<String>()

        try {
            val count = buf.getShort().toInt()
            if (count == 0) {
                return LResult.Failure(UiText.Hardcoded("Enum should have at least 1 enumerator"))
            }

            for (i in 0 until count) {
                values.add(buf.getUtf8String())
            }

            return LResult.Success(Property.StringEnumProperty(id, name, values, 0))
        } catch (ex: BufferUnderflowException) {
            return LResult.Failure(UiText.Hardcoded("Failed to read enum property"))
        }
    }

    override fun serializeValue(property: Property, out: ByteBuffer): LResult<Unit> {
        val enumProp = property as Property.StringEnumProperty
        val value = enumProp.currentValue.value

        return try {
            out.putShort(value.toShort())
            LResult.Success(Unit)
        } catch (ex: BufferUnderflowException) {
            LResult.Failure(UiText.Hardcoded("Failed to write value of StringEnumProperty"))
        }
    }

    override fun deserializeValue(buf: ByteBuffer, target: Property): LResult<Unit> {
        val enumProp = target as Property.StringEnumProperty

        try {
            val value = buf.getShort().toInt()
            if (value >= enumProp.values.size) {
                return LResult.Failure(UiText.Hardcoded(
                    "Enum value should be in range 0..${enumProp.values.size}, but actual is $value"
                ))
            }
            enumProp.currentValue.value = value
            return LResult.Success(Unit)
        } catch (ex: BufferUnderflowException) {
            return LResult.Failure(UiText.Hardcoded("No value"))
        }
    }
}