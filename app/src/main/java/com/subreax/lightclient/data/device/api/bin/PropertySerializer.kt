package com.subreax.lightclient.data.device.api.bin

import android.graphics.Color
import com.subreax.lightclient.LResult
import com.subreax.lightclient.R
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

abstract class BaseFloatSerializer : PropertySerializer {
    abstract fun createProperty(
        id: Int,
        name: String,
        value: Float,
        min: Float,
        max: Float
    ): Property.BaseFloat

    override fun deserializeInfo(id: Int, name: String, buf: ByteBuffer): LResult<Property> {
        return try {
            val min = buf.getInt() / 32768.0f
            val max = buf.getInt() / 32768.0f
            LResult.Success(
                createProperty(id, name, min, min, max)
            )
        } catch (ex: BufferUnderflowException) {
            LResult.Failure(R.string.no_info_provided_for_min_and_max_values, name)
        }
    }

    override fun serializeValue(property: Property, out: ByteBuffer): LResult<Unit> {
        val frp = property as Property.BaseFloat
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
        val frp = target as Property.BaseFloat

        return try {
            val value = buf.getInt() / 32768.0f
            frp.current.value = value
            LResult.Success(Unit)
        } catch (ex: BufferUnderflowException) {
            LResult.Failure(R.string.failed_to_deserialize_prop_value, target.name)
        }
    }
}

class FloatNumberSerializer : BaseFloatSerializer() {
    override fun createProperty(
        id: Int,
        name: String,
        value: Float,
        min: Float,
        max: Float
    ): Property.BaseFloat = Property.FloatNumber(id, name, value, min, max)
}

class FloatSliderSerializer : BaseFloatSerializer() {
    override fun createProperty(
        id: Int,
        name: String,
        value: Float,
        min: Float,
        max: Float
    ): Property.BaseFloat = Property.FloatSlider(id, name, value, min, max)
}

class FloatSmallHSliderSerializer : BaseFloatSerializer() {
    override fun createProperty(
        id: Int,
        name: String,
        value: Float,
        min: Float,
        max: Float
    ): Property.BaseFloat = Property.FloatSmallHSlider(id, name, value, min, max)
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
            LResult.Failure(R.string.failed_to_deserialize_prop_value, target.name)
        }
    }
}


class EnumPropertySerializer : PropertySerializer {
    override fun deserializeInfo(id: Int, name: String, buf: ByteBuffer): LResult<Property> {
        val values = mutableListOf<String>()

        try {
            val count = buf.getShort().toInt()
            if (count == 0) {
                return LResult.Failure(R.string.enum_prop_should_have_at_least_1_enum, name)
            }

            for (i in 0 until count) {
                values.add(buf.getUtf8String())
            }

            return LResult.Success(Property.Enum(id, name, values, 0))
        } catch (ex: BufferUnderflowException) {
            return LResult.Failure(R.string.failed_to_deserialize_prop_info, name)
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

        return try {
            var value = buf.getShort().toInt()
            if (value >= enumProp.values.size) {
                value = enumProp.values.size - 1
            }
            enumProp.currentValue.value = value
            LResult.Success(Unit)
        } catch (ex: BufferUnderflowException) {
            LResult.Failure(R.string.failed_to_deserialize_prop_value, target.name)
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
    ): Property.BaseInt

    override fun deserializeInfo(id: Int, name: String, buf: ByteBuffer): LResult<Property> {
        return try {
            val min = buf.getInt()
            val max = buf.getInt()
            LResult.Success(
                createProperty(id, name, min, min, max)
            )
        } catch (ex: BufferUnderflowException) {
            LResult.Failure(R.string.no_info_provided_for_min_and_max_values, name)
        }
    }

    override fun serializeValue(property: Property, out: ByteBuffer): LResult<Unit> {
        return try {
            val intProp = property as Property.BaseInt
            out.putInt(intProp.current.value)
            return LResult.Success(Unit)
        } catch (ex: BufferUnderflowException) {
            LResult.Failure("Failed to write IntProperty value")
        }
    }

    override fun deserializeValue(buf: ByteBuffer, target: Property): LResult<Unit> {
        return try {
            val intProp = target as Property.BaseInt
            intProp.current.value = buf.getInt()
            LResult.Success(Unit)
        } catch (ex: BufferUnderflowException) {
            LResult.Failure(R.string.failed_to_deserialize_prop_value, target.name)
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
    ): Property.BaseInt {
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
    ): Property.BaseInt {
        return Property.IntSlider(id, name, value, min, max)
    }
}

class IntSmallHSliderPropertySerializer : BaseIntPropertySerializer() {
    override fun createProperty(
        id: Int,
        name: String,
        value: Int,
        min: Int,
        max: Int
    ): Property.BaseInt {
        return Property.IntSmallHSlider(id, name, value, min, max)
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
            LResult.Failure(R.string.failed_to_deserialize_prop_value, target.name)
        }
    }

}