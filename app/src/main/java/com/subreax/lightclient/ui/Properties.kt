package com.subreax.lightclient.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subreax.lightclient.data.Property
import com.subreax.lightclient.ui.colorpicker.ColorDisplay
import com.subreax.lightclient.ui.home.PropertyCallback
import com.subreax.lightclient.ui.theme.LightClientTheme
import kotlin.math.round
import kotlin.math.roundToInt


val PropertyContentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
val PropertyShape = RoundedCornerShape(16.dp)

val PropertyContentPaddingMod = Modifier.padding(PropertyContentPadding)
val PropertyClipMod = Modifier.clip(PropertyShape)
val HorizontalArrangement = Arrangement.spacedBy(8.dp)

@Composable
private fun PropertyInfo(
    name: String,
    modifier: Modifier = Modifier,
    additionalInfo: String = ""
) {
    Text(
        text = buildAnnotatedString {
            append(name)

            withStyle(
                // todo: set text style 'body2'
                SpanStyle(
                    fontSize = 13.3.sp,
                    color = LocalContentColorMediumAlpha
                )
            ) {
                if (additionalInfo.isNotBlank()) {
                    append(System.lineSeparator() + additionalInfo)
                }
            }
        },
        modifier = modifier
    )
}

@Composable
private fun RowPropertyWrapper(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    elevation: Dp = 2.dp,
    content: @Composable RowScope.() -> Unit
) {
    var modifier1: Modifier = modifier.then(PropertyClipMod)
    if (onClick != null) {
        modifier1 = modifier1
            .clickable { onClick() }
    }

    Surface(modifier1, elevation = elevation) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = HorizontalArrangement,
            modifier = PropertyContentPaddingMod
        ) {
            content()
        }
    }

}

@Composable
fun EnumProperty(
    name: String,
    values: List<String>,
    pickedValue: Int,
    onClick: () -> Unit
) {
    val value = if (values.isNotEmpty() && pickedValue < values.size) {
        values[pickedValue]
    } else "E_OUT_OF_BOUNDS"

    RowPropertyWrapper(
        onClick = onClick
    ) {
        PropertyInfo(name = name, Modifier.weight(1.0f))
        Text(text = value)
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "")
    }
}

@Composable
fun EnumProperty(
    baseProperty: Property,
    callback: PropertyCallback
) {
    val property = baseProperty as Property.Enum
    val pickedValue by property.currentValue.collectAsState()

    EnumProperty(
        name = property.name,
        values = property.values,
        pickedValue = pickedValue,
        onClick = { callback.stringEnumClicked(property) }
    )
}


@Composable
fun ToggleProperty(
    name: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    RowPropertyWrapper(
        onClick = { onCheckedChange(!checked) }
    ) {
        PropertyInfo(name = name, Modifier.weight(1.0f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.height(32.dp)
        )
    }
}

@Composable
fun ToggleProperty(
    baseProperty: Property,
    callback: PropertyCallback
) {
    val property = baseProperty as Property.Bool
    val checked by property.toggled.collectAsState()

    ToggleProperty(
        name = property.name,
        checked = checked,
        onCheckedChange = { callback.toggleChanged(property, it) }
    )
}


@Composable
fun FloatSliderProperty(
    name: String,
    min: Float,
    max: Float,
    value: Float,
    onValueChanged: (Float) -> Unit,
    onClick: () -> Unit
) {
    RowPropertyWrapper(
        onClick = onClick
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.weight(1f)
        ) {
            PropertyInfo(
                name = name,
                additionalInfo = "${min.toStringRound2()} .. ${max.toStringRound2()}"
            )

            Text(
                text = value.toStringRound2(),
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.body2
            )
        }

        Slider(
            value = value,
            valueRange = min..max,
            onValueChange = { onValueChanged(it) },
            modifier = Modifier
                .weight(1f)
                .height(37.dp)
        )
    }
}

@Composable
fun FloatSliderProperty(
    baseProperty: Property,
    callback: PropertyCallback
) {
    val property = baseProperty as Property.FloatSlider
    val value by property.current.collectAsState()

    FloatSliderProperty(
        name = property.name,
        min = property.min,
        max = property.max,
        value = value,
        onValueChanged = { callback.floatSliderChanged(property, it) },
        onClick = { callback.floatSliderClicked(property) }
    )
}

@Composable
fun ColorProperty(
    name: String,
    color: Color,
    onClick: () -> Unit
) {
    RowPropertyWrapper(
        onClick = onClick
    ) {
        PropertyInfo(name = name, modifier = Modifier.weight(1.0f))

        ColorDisplay(color = color, size = 24.dp)

        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "")
    }
}

@Composable
fun ColorProperty(
    baseProperty: Property,
    callback: PropertyCallback
) {
    val property = baseProperty as Property.Color
    val intColor by property.color.collectAsState()

    ColorProperty(
        name = property.name,
        color = Color(intColor),
        onClick = { callback.colorPropertyClicked(property) }
    )
}

@Composable
fun IntProperty(
    name: String,
    min: Int,
    max: Int,
    value: Int,
    onValueChanged: (Int) -> Unit,
    onClick: () -> Unit
) {
    var isTotalScrollInitialized by remember { mutableStateOf(false) }
    var totalScroll by remember { mutableStateOf(0f) }

    RowPropertyWrapper(
        modifier = Modifier.scrollable(
            orientation = Orientation.Horizontal,
            state = rememberScrollableState { delta ->
                if (!isTotalScrollInitialized) {
                    isTotalScrollInitialized = true
                    totalScroll = value.toFloat()
                }

                val totalScroll0 = totalScroll
                totalScroll += delta / 24f
                totalScroll = totalScroll.coerceIn(min.toFloat(), max.toFloat())

                if (totalScroll0.toInt() != totalScroll.toInt()) {
                    onValueChanged(totalScroll.toInt())
                }
                delta
            }
        ),
        onClick = onClick
    ) {
        PropertyInfo(name = name, modifier = Modifier.weight(1f))
        Text(text = "$value")
    }
}

@Composable
fun IntProperty(
    baseProperty: Property,
    callback: PropertyCallback
) {
    val property = baseProperty as Property.IntNumber
    val value by property.current.collectAsState()

    IntProperty(
        name = property.name,
        min = property.min,
        max = property.max,
        value = value,
        onValueChanged = { callback.intChanged(property, it) },
        onClick = { callback.intClicked(property) }
    )
}

@Composable
fun IntSliderProperty(
    name: String,
    min: Int,
    max: Int,
    value: Int,
    onValueChanged: (Int) -> Unit,
    onClick: () -> Unit
) {
    FloatSliderProperty(
        name = name,
        min = min.toFloat(),
        max = max.toFloat(),
        value = value.toFloat(),
        onValueChanged = { onValueChanged(it.roundToInt()) },
        onClick = onClick
    )
}

@Composable
fun IntSliderProperty(
    baseProperty: Property,
    callback: PropertyCallback
) {
    val property = baseProperty as Property.IntSlider
    val value by property.current.collectAsState()

    IntSliderProperty(
        name = property.name,
        min = property.min,
        max = property.max,
        value = value,
        onValueChanged = { callback.intChanged(property, it) },
        onClick = { callback.intClicked(property) }
    )
}

@Composable
fun SpecLoadingProperty(
    progress: Float
) {
    RowPropertyWrapper(
        elevation = 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(progress)
            Text("${(progress * 100).roundToInt()}%", style = MaterialTheme.typography.caption)
        }
        Text(
            text = "Загрузка...",
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun SpecLoadingProperty(
    baseProperty: Property,
    callback: PropertyCallback
) {
    val property = baseProperty as Property.SpecLoading
    val progress by property.progress.collectAsState()

    SpecLoadingProperty(
        progress = progress
    )
}

private fun Float.toStringRound2(): String {
    val str = (round(this * 100.0f) / 100.0f).toString()
    if (!str.endsWith(".0")) {
        return str
    }
    return str.substring(0, str.length - 2)
}


@Preview(uiMode = UI_MODE_NIGHT_YES, widthDp = 320)
@Composable
fun EnumPropertyPreview() {
    LightClientTheme {
        EnumProperty(
            name = "Сцена",
            values = listOf("Smoke"),
            pickedValue = 0,
            onClick = {}
        )
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES, widthDp = 320)
@Composable
fun TogglePropertyPreview() {
    LightClientTheme {
        ToggleProperty(
            name = "Датчик движения",
            checked = true,
            onCheckedChange = { }
        )
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES, widthDp = 320)
@Composable
fun IntPropertyPreview() {
    LightClientTheme {
        IntProperty(
            name = "Кол-во светодиодов",
            min = 2,
            max = 300,
            value = 24,
            onValueChanged = {},
            onClick = {}
        )
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES, widthDp = 320)
@Composable
fun IntRangePropertyPreivew() {
    LightClientTheme {
        IntSliderProperty(
            name = "Яркость",
            min = 0,
            max = 100,
            value = 63,
            onValueChanged = {},
            onClick = {}
        )
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES, widthDp = 320)
@Composable
fun FloatRangePropertyPreivew() {
    LightClientTheme {
        FloatSliderProperty(
            name = "Скорость",
            min = 0.0f,
            max = 5.0f,
            value = 1.0f,
            onValueChanged = {},
            onClick = {}
        )
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES, widthDp = 320)
@Composable
fun ColorPropertyPreview() {
    LightClientTheme {
        ColorProperty(
            name = "Основной цвет",
            color = Color(0xFF0099EF),
            onClick = { /*TODO*/ }
        )
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES, widthDp = 320)
@Composable
fun SpecLoadingPropertyPreview() {
    LightClientTheme {
        SpecLoadingProperty(
            progress = 0.4f
        )
    }
}