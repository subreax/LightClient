package com.subreax.lightclient.data.deviceapi.ble

import android.graphics.Color
import com.subreax.lightclient.LResult
import com.subreax.lightclient.data.Property
import com.subreax.lightclient.utils.getUtf8String
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
                Property.FloatSlider(id, name, min, max, min)
            )
        } catch (ex: BufferUnderflowException) {
            LResult.Failure("No info provided for min and max values")
        }
    }

    override fun serializeValue(property: Property, out: ByteBuffer): LResult<Unit> {
        val frp = property as Property.FloatSlider
        val value = frp.current.value
        val q15 = (value * 32768).roundToInt()

        return try {
            out.putInt(q15)
            LResult.Success(Unit)
        } catch (ex: BufferUnderflowException) {
            LResult.Failure("Failed to write value of FloatRange")
        }
    }

    override fun deserializeValue(buf: ByteBuffer, target: Property): LResult<Unit> {
        val frp = target as Property.FloatSlider

        return try {
            val value = buf.getInt() / 32768.0f
            frp.current.value = value
            LResult.Success(Unit)
        } catch (ex: BufferUnderflowException) {
            LResult.Failure("No value")
        }
    }
}


class ColorPropertySerializer : PropertySerializer {
    override fun deserializeInfo(id: Int, name: String, buf: ByteBuffer): LResult<Property> {
        return LResult.Success(
            Property.Color(id, name, Color.BLACK)
        )
    }

    override fun serializeValue(property: Property, out: ByteBuffer): LResult<Unit> {
        val colorProperty = property as Property.Color
        val color = colorProperty.color.value

        return try {
            out.putInt(color)
            LResult.Success(Unit)
        } catch (ex: BufferUnderflowException) {
            LResult.Failure("Failed to write value of ColorRange")
        }
    }

    override fun deserializeValue(buf: ByteBuffer, target: Property): LResult<Unit> {
        val colorProp = target as Property.Color

        return try {
            colorProp.color.value = buf.getInt()
            LResult.Success(Unit)
        } catch (ex: BufferUnderflowException) {
            LResult.Failure("No value")
        }
    }
}


class EnumPropertySerializer : PropertySerializer {
    override fun deserializeInfo(id: Int, name: String, buf: ByteBuffer): LResult<Property> {
        val values = mutableListOf<String>()

        try {
            val count = buf.getShort().toInt()
            if (count == 0) {
                return LResult.Failure("Enum should have at least 1 enumerator")
            }

            for (i in 0 until count) {
                values.add(buf.getUtf8String())
            }

            return LResult.Success(Property.Enum(id, name, values, 0))
        } catch (ex: BufferUnderflowException) {
            return LResult.Failure("Failed to read enum property")
        }
    }

    override fun serializeValue(property: Property, out: ByteBuffer): LResult<Unit> {
        val enumProp = property as Property.Enum
        val value = enumProp.currentValue.value

        return try {
            out.putShort(value.toShort())
            LResult.Success(Unit)
        } catch (ex: BufferUnderflowException) {
            LResult.Failure("Failed to write value of StringEnumProperty")
        }
    }

    override fun deserializeValue(buf: ByteBuffer, target: Property): LResult<Unit> {
        val enumProp = target as Property.Enum

        try {
            val value = buf.getShort().toInt()
            if (value >= enumProp.values.size) {
                return LResult.Failure(
                    "Enum value should be in range 0..${enumProp.values.size}, but actual is $value"
                )
            }
            enumProp.currentValue.value = value
            return LResult.Success(Unit)
        } catch (ex: BufferUnderflowException) {
            return LResult.Failure("No value")
        }
    }
}

abstract class BaseIntPropertySerializer : PropertySerializer {
    abstract fun createProperty(
        id: Int,
        name: String,
        value: Int,
        min: Int,
        max: Int
    ): Property.BaseIntProperty

    override fun deserializeInfo(id: Int, name: String, buf: ByteBuffer): LResult<Property> {
        return try {
            val min = buf.getInt()
            val max = buf.getInt()
            LResult.Success(
                createProperty(id, name, min, min, max)
            )
        } catch (ex: BufferUnderflowException) {
            LResult.Failure("No info provided for min and max values")
        }
    }

    override fun serializeValue(property: Property, out: ByteBuffer): LResult<Unit> {
        return try {
            val intProp = property as Property.BaseIntProperty
            out.putInt(intProp.current.value)
            return LResult.Success(Unit)
        } catch (ex: BufferUnderflowException) {
            LResult.Failure("Failed to write IntProperty value")
        }
    }

    override fun deserializeValue(buf: ByteBuffer, target: Property): LResult<Unit> {
        return try {
            val intProp = target as Property.BaseIntProperty
            intProp.current.value = buf.getInt()
            LResult.Success(Unit)
        } catch (ex: BufferUnderflowException) {
            LResult.Failure("No value")
        }
    }
}

class IntPropertySerializer : BaseIntPropertySerializer() {
    override fun createProperty(
        id: Int,
        name: String,
        value: Int,
        min: Int,
        max: Int
    ): Property.BaseIntProperty {
        return Property.IntNumber(id, name, value, min, max)
    }
}

class IntSliderPropertySerializer : BaseIntPropertySerializer() {
    override fun createProperty(
        id: Int,
        name: String,
        value: Int,
        min: Int,
        max: Int
    ): Property.BaseIntProperty {
        return Property.IntSlider(id, name, value, min, max)
    }
}

class BoolPropertySerializer : PropertySerializer {
    override fun deserializeInfo(id: Int, name: String, buf: ByteBuffer): LResult<Property> {
        return LResult.Success(Property.Bool(id, name, false))
    }

    override fun serializeValue(property: Property, out: ByteBuffer): LResult<Unit> {
        return try {
            val prop = property as Property.Bool
            val value = if (prop.toggled.value) 1 else 0
            out.put(value.toByte())
            LResult.Success(Unit)
        } catch (ex: BufferUnderflowException) {
            LResult.Failure("Failed to write bool value")
        }
    }

    override fun deserializeValue(buf: ByteBuffer, target: Property): LResult<Unit> {
        return try {
            val value = buf.get().toInt() > 0
            (target as Property.Bool).toggled.value = value
            LResult.Success(Unit)
        } catch (ex: BufferUnderflowException) {
            LResult.Failure("Failed to read bool value")
        }
    }

}