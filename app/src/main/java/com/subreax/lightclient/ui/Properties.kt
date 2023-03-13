package com.subreax.lightclient.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subreax.lightclient.ui.theme.LightClientTheme
import kotlin.math.round
import kotlin.math.roundToInt

@Composable
private fun PropertyInfo(
    type: String,
    name: String,
    modifier: Modifier = Modifier,
    additionalInfo: String = ""
) {
    Text(
        text = buildAnnotatedString {
            val textColorDark = LocalContentColorMediumAlpha
            withStyle(
                // todo: set text style 'caption'
                SpanStyle(
                    fontSize = 11.1.sp,
                    color = textColorDark
                )
            ) {
                append("$type${System.lineSeparator()}")
            }

            append(name)

            if (additionalInfo.isNotEmpty()) {
                append("  ")
                // todo: set text style 'body2'
                withStyle(SpanStyle(fontSize = 13.3.sp, color = textColorDark)) {
                    append(additionalInfo)
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
    content: @Composable RowScope.() -> Unit
) {
    val modifier1 =
        if (onClick != null)
            Modifier.clickable(onClick = onClick)
        else
            Modifier

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier1.then(modifier)
    ) {
        content()
    }
}

@Composable
private fun ColumnPropertyWrapper(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val modifier1 =
        if (onClick != null)
            Modifier.clickable(onClick = onClick)
        else
            Modifier

    Column(modifier1.then(modifier)) {
        content()
    }
}


@Composable
fun StringEnumProperty(
    name: String,
    values: List<String>,
    pickedValue: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val value = if (values.isNotEmpty() && pickedValue < values.size) {
        values[pickedValue]
    } else "E_OUT_OF_BOUNDS"

    RowPropertyWrapper(onClick = onClick, modifier = modifier) {
        PropertyInfo(type = "enum", name = name, Modifier.weight(1.0f))
        Text(text = value)
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "")
    }
}


@Composable
fun ToggleProperty(
    name: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    RowPropertyWrapper(onClick = { onCheckedChange(!checked) }, modifier = modifier) {
        PropertyInfo(type = "toggle", name = name, Modifier.weight(1.0f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.height(32.dp)
        )
    }
}


@Composable
fun FloatRangeProperty(
    name: String,
    min: Float,
    max: Float,
    value: Float,
    onValueChanged: (Float) -> Unit,
    modifier: Modifier = Modifier,
    typeName: String = "float range"
) {
    ColumnPropertyWrapper(modifier = modifier) {
        PropertyInfo(
            type = typeName,
            name = name,
            additionalInfo = "${min.toStringRound2()} .. ${max.toStringRound2()}"
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Slider(
                value = value,
                valueRange = min..max,
                onValueChange = { onValueChanged(it) },
                modifier = Modifier
                    .weight(1.0f)
                    .height(32.dp)
            )
            Text(
                text = value.toStringRound2(),
                modifier = Modifier.widthIn(48.dp),
                textAlign = TextAlign.End,
                color = LocalContentColorMediumAlpha,
                style = MaterialTheme.typography.body2
            )
        }
    }
}


@Composable
fun IntRangeProperty(
    name: String,
    min: Int,
    max: Int,
    value: Int,
    onValueChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    FloatRangeProperty(
        name = name,
        min = min.toFloat(),
        max = max.toFloat(),
        value = value.toFloat(),
        onValueChanged = { onValueChanged(it.roundToInt()) },
        modifier = modifier,
        typeName = "int range",
    )
}


@Composable
fun ColorProperty(
    name: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    RowPropertyWrapper(onClick = onClick, modifier = modifier) {
        PropertyInfo(type = "color", name = name, modifier = Modifier.weight(1.0f))

        Box(
            Modifier
                .size(width = 24.dp, height = 24.dp)
                .clip(CircleShape)
                .background(color)
        )

        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "")
    }
}


private fun Float.toStringRound2(): String {
    return (round(this * 100.0f) / 100.0f).toString().substringBefore(".0")
}


@Preview(uiMode = UI_MODE_NIGHT_YES, widthDp = 320)
@Composable
fun StringEnumPropertyPreview() {
    LightClientTheme {
        StringEnumProperty(
            name = "Сцена",
            values = listOf("Smoke"),
            pickedValue = 0,
            onClick = {},
            modifier = Modifier.padding(8.dp)
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
            onCheckedChange = { },
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES, widthDp = 320)
@Composable
fun IntRangePropertyPreivew() {
    LightClientTheme {
        IntRangeProperty(
            name = "Яркость",
            min = 0,
            max = 100,
            value = 63,
            onValueChanged = {},
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES, widthDp = 320)
@Composable
fun FloatRangePropertyPreivew() {
    LightClientTheme {
        FloatRangeProperty(
            name = "Скорость",
            min = 0.0f,
            max = 5.0f,
            value = 1.0f,
            onValueChanged = {},
            modifier = Modifier.padding(8.dp)
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
            onClick = { /*TODO*/ },
            modifier = Modifier.padding(8.dp)
        )
    }
}