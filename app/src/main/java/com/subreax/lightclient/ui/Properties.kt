package com.subreax.lightclient.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subreax.lightclient.ui.colorpicker.ColorDisplay
import com.subreax.lightclient.ui.theme.LightClientTheme
import kotlin.math.round
import kotlin.math.roundToInt

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
    shape: Shape = RectangleShape,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onClick: (() -> Unit)? = null,
    elevation: Dp = 2.dp,
    content: @Composable RowScope.() -> Unit
) {
    var modifier1: Modifier = modifier.clip(shape)
    if (onClick != null) {
        modifier1 = modifier1
            .clickable { onClick() }
    }

    Surface(modifier1, elevation = elevation) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(contentPadding)
        ) {
            content()
        }
    }

}

@Composable
fun StringEnumProperty(
    name: String,
    values: List<String>,
    pickedValue: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val value = if (values.isNotEmpty() && pickedValue < values.size) {
        values[pickedValue]
    } else "E_OUT_OF_BOUNDS"

    RowPropertyWrapper(
        onClick = onClick,
        modifier = modifier,
        shape = shape,
        contentPadding = contentPadding
    ) {
        PropertyInfo(name = name, Modifier.weight(1.0f))
        Text(text = value)
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "")
    }
}


@Composable
fun ToggleProperty(
    name: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    RowPropertyWrapper(
        onClick = { onCheckedChange(!checked) },
        modifier = modifier,
        shape = shape,
        contentPadding = contentPadding
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
fun FloatRangeProperty(
    name: String,
    min: Float,
    max: Float,
    value: Float,
    onValueChanged: (Float) -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    RowPropertyWrapper(
        modifier = modifier,
        shape = shape,
        contentPadding = contentPadding
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
fun IntRangeProperty(
    name: String,
    min: Int,
    max: Int,
    value: Int,
    onValueChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    FloatRangeProperty(
        name = name,
        min = min.toFloat(),
        max = max.toFloat(),
        value = value.toFloat(),
        onValueChanged = { onValueChanged(it.roundToInt()) },
        modifier = modifier,
        shape = shape,
        contentPadding = contentPadding
    )
}


@Composable
fun ColorProperty(
    name: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    RowPropertyWrapper(
        onClick = onClick,
        modifier = modifier,
        shape = shape,
        contentPadding = contentPadding
    ) {
        PropertyInfo(name = name, modifier = Modifier.weight(1.0f))

        ColorDisplay(color = color, size = 24.dp)

        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "")
    }
}


@Composable
fun SpecLoadingProperty(
    progress: Float,
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    RowPropertyWrapper(
        modifier = modifier,
        shape = shape,
        contentPadding = contentPadding,
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

private fun Float.toStringRound2(): String {
    val str = (round(this * 100.0f) / 100.0f).toString()
    if (!str.endsWith(".0")) {
        return str
    }
    return str.substring(0, str.length - 2)
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
            contentPadding = PaddingValues(8.dp)
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
            contentPadding = PaddingValues(8.dp)
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
            contentPadding = PaddingValues(8.dp)
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
            contentPadding = PaddingValues(8.dp)
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
            contentPadding = PaddingValues(8.dp)
        )
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES, widthDp = 320)
@Composable
fun SpecLoadingPropertyPreview() {
    LightClientTheme {
        SpecLoadingProperty(
            progress = 0.4f,
            contentPadding = PaddingValues(8.dp)
        )
    }
}