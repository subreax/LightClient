package com.subreax.lightclient.ui.home

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.subreax.lightclient.R
import com.subreax.lightclient.data.Property
import com.subreax.lightclient.ui.ColumnsCount
import com.subreax.lightclient.ui.LocalContentColorMediumAlpha
import com.subreax.lightclient.ui.UniformGrid
import com.subreax.lightclient.ui.UniformGridScope.Companion.span
import com.subreax.lightclient.ui.colorpicker.ColorDisplay2
import com.subreax.lightclient.ui.colorpicker.lerp
import com.subreax.lightclient.ui.cospaletteeditor.CosPaletteViewer
import com.subreax.lightclient.ui.cospaletteeditor.Cosine
import com.subreax.lightclient.ui.theme.LightClientTheme
import kotlin.math.round
import kotlin.math.roundToInt


private val PropertyCornerRadius = 16.dp
private val PropertyShape = RoundedCornerShape(PropertyCornerRadius)

private const val DefaultTopBorder = 0.4f
private const val DefaultBottomBorder = 0.2f

@Composable
private fun SimpleName(
    name: String,
    modifier: Modifier = Modifier,
    semiTransparent: Boolean = true
) {
    val nameColor =
        if (semiTransparent)
            LocalContentColorMediumAlpha
        else
            LocalContentColor.current

    Text(
        text = name,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.body2,
        color = nameColor,
        modifier = modifier
    )
}

@Composable
private fun SimpleValue(value: String, modifier: Modifier = Modifier) {
    Text(
        text = value,
        style = MaterialTheme.typography.h5,
        textAlign = TextAlign.Center,
        modifier = modifier
    )
}

@Composable
private fun SimpleSmallValue(
    value: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Center
) {
    Text(
        text = value,
        style = MaterialTheme.typography.body1,
        textAlign = textAlign,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}


@Composable
fun BasePropertyContent(
    name: @Composable () -> Unit,
    value: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    topBorder: Float = DefaultTopBorder,
    bottomBorder: Float = DefaultBottomBorder
) {
    ConstraintLayout(modifier) {
        val (nameRef, valueRef) = createRefs()
        val topGuideline = createGuidelineFromTop(topBorder)
        val bottomGuideline = createGuidelineFromBottom(bottomBorder)

        Box(
            modifier = Modifier.constrainAs(valueRef) {
                top.linkTo(topGuideline)
                bottom.linkTo(topGuideline)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
        ) {
            value()
        }

        Box(
            modifier = Modifier.constrainAs(nameRef) {
                top.linkTo(bottomGuideline)
                bottom.linkTo(bottomGuideline)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
        ) {
            name()
        }
    }
}

@Composable
private fun SmallHSliderLayout2(
    name: String,
    value: String,
    onValueClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .padding(start = 12.dp, end = 4.dp)
            .fillMaxSize()
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.body2,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )

        SimpleSmallValue(
            value = value,
            textAlign = TextAlign.End,
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = onValueClicked)
                .widthIn(min = 32.dp)
                .padding(8.dp)
        )
    }
}


@Preview(uiMode = UI_MODE_NIGHT_YES, widthDp = 128, heightDp = 128)
@Composable
fun BasePropertyContentPreview() {
    LightClientTheme {
        BasePropertyContent(
            name = { SimpleName("Hello") },
            value = { SimpleValue(value = "World") }
        )
    }
}


@Composable
fun BaseProperty(
    name: @Composable () -> Unit,
    value: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    var modifier0 = modifier
        .clip(PropertyShape)
        .background(MaterialTheme.colors.surface)
    if (onClick != null)
        modifier0 = modifier0.clickable(onClick = onClick)

    BasePropertyContent(name = name, value = value, modifier = modifier0.padding(horizontal = 4.dp))
}

@Composable
private fun BaseTextValueProperty(
    name: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    BaseProperty(
        name = { SimpleName(name) },
        value = { SimpleValue(value) },
        modifier = modifier,
        onClick = onClick
    )
}


@Composable
fun IntProperty(
    name: String,
    value: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BaseTextValueProperty(
        name = name,
        value = value.toString(),
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
fun IntProperty(baseProperty: Property, callback: PropertyCallback) {
    val property = baseProperty as Property.IntNumber
    val value by property.current.collectAsState()

    IntProperty(
        name = property.name,
        value = value,
        onClick = { callback.intClicked(property) },
        modifier = Modifier.span(2, 2)
    )
}


@Composable
fun FloatProperty(
    name: String,
    value: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BaseTextValueProperty(
        name = name,
        value = "${round(value * 1000f) / 1000f}",
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
fun FloatProperty(baseProperty: Property, callback: PropertyCallback) {
    val property = baseProperty as Property.BaseFloat
    val value by property.current.collectAsState()

    FloatProperty(
        name = property.name,
        value = value,
        onClick = { callback.floatClicked(property) },
        modifier = Modifier.span(2, 2)
    )
}

@Composable
fun BoolProperty(
    name: String,
    value: Boolean,
    onClick: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    BaseProperty(
        name = { SimpleName(name) },
        value = {
            Switch(
                checked = value,
                onCheckedChange = { onClick(it) },
                modifier = Modifier.height(20.dp)
            )
        },
        onClick = { onClick(!value) },
        modifier = modifier
    )
}

@Composable
fun BoolProperty(baseProperty: Property, callback: PropertyCallback) {
    val property = baseProperty as Property.Bool
    val value by property.toggled.collectAsState()

    BoolProperty(
        name = property.name,
        value = value,
        onClick = { callback.toggleChanged(property, it) },
        modifier = Modifier.span(2, 2)
    )
}


@Composable
fun EnumProperty(
    name: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BaseTextValueProperty(name = name, value = value, onClick = onClick, modifier = modifier)
}

@Composable
fun EnumProperty(baseProperty: Property, callback: PropertyCallback) {
    val property = baseProperty as Property.Enum
    val valueIdx by property.currentValue.collectAsState()
    val value =
        if (valueIdx >= 0 && valueIdx < property.values.size)
            property.values[valueIdx]
        else
            "Unknown value: $valueIdx"

    EnumProperty(
        name = property.name,
        value = value,
        onClick = { callback.enumClicked(property) },
        modifier = Modifier.span(vertical = 2, horizontal = 4)
    )
}


@Composable
fun BaseSliderProperty(
    name: String,
    displayedValue: String,
    progress: Float,
    onProgressChanged: (Float) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progressState by rememberUpdatedState(progress)
    val updatedOnProgressChanged by rememberUpdatedState(onProgressChanged)

    Box(
        modifier = modifier
            .clip(PropertyShape)
            .background(MaterialTheme.colors.surface)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val delta = dragAmount.y / size.height
                    val newValue = (progressState - delta).coerceIn(0f, 1f)
                    updatedOnProgressChanged(newValue)
                }
            }
    ) {
        // progress
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(progressState)
                .align(Alignment.BottomStart)
                .background(Color(0xFF4D3B32)) // todo
        )

        BasePropertyContent(
            name = { SimpleName(name = name, semiTransparent = false) },
            value = {
                SimpleValue(
                    value = displayedValue,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onClick)
                        .widthIn(48.dp)
                        .padding(8.dp)
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp),
            topBorder = 0.5f,
            bottomBorder = DefaultBottomBorder * 0.47f
        )
    }
}


private fun round2(value: Float) = round(value * 100f) / 100f

@Composable
fun FloatSliderProperty(
    name: String,
    value: Float,
    min: Float,
    max: Float,
    onValueChanged: (Float) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = ((value - min) / (max - min)).coerceIn(0f, 1f)

    BaseSliderProperty(
        name = name,
        displayedValue = "${round2(value)}",
        progress = progress,
        onProgressChanged = { onValueChanged(lerp(min, max, it)) },
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
fun FloatSliderProperty(baseProperty: Property, callback: PropertyCallback) {
    val property = baseProperty as Property.BaseFloat
    val value by property.current.collectAsState()

    FloatSliderProperty(
        name = property.name,
        value = value,
        min = property.min,
        max = property.max,
        onValueChanged = { callback.floatChanged(property, it) },
        onClick = { callback.floatClicked(property) },
        modifier = Modifier.span(vertical = 4, horizontal = 2)
    )
}


@Composable
fun BaseSliderProperty(
    orientation: Orientation,
    progress: Float,
    onProgressChanged: (Float) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val orientationState by rememberUpdatedState(orientation)
    val progressState by rememberUpdatedState(progress)
    val updatedOnProgressChanged by rememberUpdatedState(onProgressChanged)

    val fillX: Float
    val fillY: Float
    if (orientation == Orientation.Vertical) {
        fillX = 1f
        fillY = progress
    } else {
        fillX = progress
        fillY = 1f
    }

    Box(
        modifier = modifier
            .clip(PropertyShape)
            .background(MaterialTheme.colors.surface)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val delta =
                        if (orientationState == Orientation.Vertical)
                            -dragAmount.y / size.height
                        else
                            dragAmount.x / size.width

                    val newValue = (progressState + delta).coerceIn(0f, 1f)
                    updatedOnProgressChanged(newValue)
                }
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fillX)
                .fillMaxHeight(fillY)
                .align(Alignment.BottomStart)
                .background(Color(0xFF4D3B32)) // todo
        )

        content()
    }
}


@Composable
fun FloatSmallHSliderProperty(
    name: String,
    value: Float,
    min: Float,
    max: Float,
    onValueChanged: (Float) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = ((value - min) / (max - min)).coerceIn(0f, 1f)

    BaseSliderProperty(
        orientation = Orientation.Horizontal,
        progress = progress,
        onProgressChanged = { onValueChanged(lerp(min, max, it)) },
        modifier = modifier
    ) {
        SmallHSliderLayout2(name = name, value = "${round2(value)}", onValueClicked = onClick)
    }
}

@Composable
fun FloatSmallHSliderProperty(baseProperty: Property, callback: PropertyCallback) {
    val prop = baseProperty as Property.BaseFloat
    val value by prop.current.collectAsState()

    FloatSmallHSliderProperty(
        name = prop.name,
        value = value,
        min = prop.min,
        max = prop.max,
        onValueChanged = {
            callback.floatChanged(prop, it)
        },
        onClick = {
            callback.floatClicked(prop)
        },
        modifier = Modifier.span(horizontal = 4)
    )
}

@Composable
fun IntSliderProperty(
    name: String,
    progress: Float,
    value: Int,
    onProgressChanged: (Float) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BaseSliderProperty(
        name = name,
        displayedValue = "$value",
        progress = progress,
        onProgressChanged = onProgressChanged,
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
fun IntSliderProperty(baseProperty: Property, callback: PropertyCallback) {
    val property = baseProperty as Property.IntSlider
    val min = property.min
    val max = property.max
    val value by property.current.collectAsState()

    val progress0 = ((value - min).toFloat() / (max - min)).coerceIn(0f, 1f)
    var progress by remember { mutableFloatStateOf(progress0) }

    IntSliderProperty(
        name = property.name,
        progress = progress,
        value = value,
        onProgressChanged = {
            val newVal = lerp(min.toFloat(), max.toFloat(), it).toInt()
            if (newVal != value) {
                callback.intChanged(property, newVal)
            }
            progress = it
        },
        onClick = { callback.intClicked(property) },
        modifier = Modifier.span(vertical = 4, horizontal = 2)
    )
}

@Composable
fun IntSmallHSliderProperty(
    name: String,
    progress: Float,
    value: Int,
    onProgressChanged: (Float) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BaseSliderProperty(
        orientation = Orientation.Horizontal,
        progress = progress,
        onProgressChanged = onProgressChanged,
        modifier = modifier
    ) {
        SmallHSliderLayout2(name = name, value = "$value", onValueClicked = onClick)
    }
}

@Composable
fun IntSmallHSliderProperty(baseProperty: Property, callback: PropertyCallback) {
    val property = baseProperty as Property.BaseInt
    val min = property.min
    val max = property.max
    val value by property.current.collectAsState()

    val progress0 = ((value - min).toFloat() / (max - min)).coerceIn(0f, 1f)
    var progress by remember { mutableStateOf(progress0) }

    IntSmallHSliderProperty(
        name = property.name,
        progress = progress,
        value = value,
        onProgressChanged = {
            val newVal = lerp(min.toFloat(), max.toFloat(), it).roundToInt()
            if (newVal != value) {
                callback.intChanged(property, newVal)
            }
            progress = it
        },
        onClick = {
            callback.intClicked(property)
        },
        modifier = Modifier.span(horizontal = 4)
    )
}


@Composable
fun ColorProperty(
    name: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ConstraintLayout(
        modifier
            .fillMaxSize()
            .clip(PropertyShape)
            .clickable(onClick = onClick)
    ) {
        val (nameRef, textBgRef) = createRefs()
        val bottomGuideline = createGuidelineFromBottom(DefaultBottomBorder)
        val bgTopGuideline = createGuidelineFromBottom(DefaultBottomBorder * 2f)

        ColorDisplay2(
            color = color,
            rounding = PropertyCornerRadius,
            modifier = Modifier
                .fillMaxSize()
                .zIndex(-1f)
        )

        Box(
            modifier = Modifier
                .background(MaterialTheme.colors.surface.copy(alpha = 0.8f))
                .fillMaxWidth()
                .constrainAs(textBgRef) {
                    top.linkTo(bgTopGuideline)
                    bottom.linkTo(parent.bottom)
                    height = Dimension.fillToConstraints
                }
        )

        SimpleName(
            name = name,
            semiTransparent = false,
            modifier = Modifier
                .constrainAs(nameRef) {
                    top.linkTo(bottomGuideline)
                    bottom.linkTo(bottomGuideline)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )
    }
}

@Composable
fun ColorProperty(baseProperty: Property, callback: PropertyCallback) {
    val property = baseProperty as Property.Color
    val intColor by property.color.collectAsState()
    val value = Color(intColor)

    ColorProperty(
        name = property.name,
        color = value,
        onClick = { callback.colorPropertyClicked(property) },
        modifier = Modifier.span(2, 2)
    )
}

@Composable
fun CosPaletteProperty(
    name: String,
    red: Cosine,
    green: Cosine,
    blue: Cosine,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(PropertyShape)
            .background(MaterialTheme.colors.surface)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        SimpleValue(value = name)
        CosPaletteViewer(
            redCosine = red,
            greenCosine = green,
            blueCosine = blue,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(8.dp)
        )
    }
}

@Composable
fun CosPaletteProperty(baseProperty: Property, callback: PropertyCallback) {
    val prop = baseProperty as Property.CosPalette
    val palette by prop.data.collectAsState()

    CosPaletteProperty(
        name = baseProperty.name,
        red = palette.red,
        green = palette.green,
        blue = palette.blue,
        onClick = {
            callback.cosPaletteClicked(prop)
        },
        modifier = Modifier.span(horizontal = 4, vertical = 2)
    )
}

@Composable
fun LoadingProperty(progress: Float, modifier: Modifier = Modifier) {
    BaseProperty(
        name = {
            SimpleName(name = stringResource(R.string.Loading))
        },
        value = {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(progress, modifier = Modifier.size(48.dp))
                Text("${(progress * 100).roundToInt()}%", style = MaterialTheme.typography.body2)
            }
        }, modifier = modifier
    )
}

@Composable
fun LoadingProperty(baseProperty: Property, callback: PropertyCallback) {
    val property = baseProperty as Property.SpecLoading
    val progress by property.progress.collectAsState()

    LoadingProperty(progress = progress, modifier = Modifier.span(horizontal = 4, vertical = 2))
}


@Preview(widthDp = 500, heightDp = 500, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun PropertiesPreview() {
    LightClientTheme {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(4.dp)
        ) {
            UniformGrid(columns = ColumnsCount.Constant(8), modifier = Modifier.fillMaxSize()) {
                FloatSliderProperty(
                    name = "Q15 Slider",
                    value = 0.5f,
                    min = 0f,
                    max = 1f,
                    onValueChanged = {},
                    onClick = { },
                    modifier = Modifier.span(horizontal = 2, vertical = 4)
                )
                IntSliderProperty(
                    name = "Int Slider",
                    progress = 0.67f,
                    value = 8,
                    onProgressChanged = {},
                    onClick = { },
                    modifier = Modifier.span(2, 4)
                )
                EnumProperty(
                    name = "Enum",
                    value = "Value Name",
                    onClick = {},
                    modifier = Modifier.span(horizontal = 4, vertical = 2)
                )
                ColorProperty(
                    name = "Color",
                    color = Color(0xff0098ff),
                    onClick = {},
                    modifier = Modifier.span(2, 2)
                )
                BoolProperty(
                    name = "Bool",
                    value = true,
                    onClick = {},
                    modifier = Modifier.span(2, 2)
                )
                FloatSmallHSliderProperty(
                    name = "Q15 Small HSlider",
                    value = 0.33f,
                    min = 0f,
                    max = 1f,
                    onValueChanged = { },
                    onClick = { },
                    modifier = Modifier.span(horizontal = 4, vertical = 1)
                )

                FloatProperty(
                    name = "Q15",
                    value = 0.5345f,
                    onClick = { },
                    modifier = Modifier.span(2, 2)
                )

                IntSmallHSliderProperty(
                    name = "Int Small HSlider",
                    progress = 0.67f,
                    value = 4,
                    onProgressChanged = { },
                    onClick = { },
                    modifier = Modifier.span(4, 1)
                )

                IntProperty(
                    name = "Int", value = 24, onClick = {}, modifier = Modifier.span(2, 2)
                )

                /*CosPaletteProperty(
                    name = "Palette",
                    onClick = { },
                    modifier = Modifier.span(horizontal = 4, vertical = 2)
                )*/

                /*LoadingProperty2(
                progress = 0.5f,
                modifier = Modifier.span(horizontal = 4, vertical = 2)
            )*/


            }
        }
    }

}
