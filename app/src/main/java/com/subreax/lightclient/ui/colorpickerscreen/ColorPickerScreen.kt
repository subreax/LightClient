package com.subreax.lightclient.ui.colorpickerscreen

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.subreax.lightclient.ui.TopBar
import com.subreax.lightclient.ui.colorpicker.ColorPicker
import com.subreax.lightclient.ui.colorpicker.HSVColor
import com.subreax.lightclient.ui.colorpicker.toHsv
import com.subreax.lightclient.ui.theme.LightClientTheme


@Composable
fun ColorPickerScreen(
    colorPickerViewModel: ColorPickerViewModel = hiltViewModel(),
    navBack: () -> Unit = {}
) {
    var hsvColor by remember { mutableStateOf(colorPickerViewModel.propertyColor.toHsv()) }

    ColorPickerScreen(
        colorName = colorPickerViewModel.propertyName,
        color = hsvColor,
        onColorChanged = {
            hsvColor = it
            colorPickerViewModel.setColor(it.toColor())
        },
        navBack = navBack
    )
}

@Composable
fun ColorPickerScreen(
    colorName: String,
    color: HSVColor,
    onColorChanged: (HSVColor) -> Unit,
    navBack: () -> Unit
) {
    Column {
        TopBar(
            title = colorName,
            subtitle = {
                Text("Изменение цвета")
            },
            navBack = navBack
        )

        ColorPicker(
            hsv = color,
            onColorChanged = onColorChanged,
            svPickerAspectRatio = 1f
        )
    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    widthDp = 360,
    heightDp = 780,
    showBackground = true
)
@Composable
fun ColorPickerScreenPreview() {
    LightClientTheme {
        ColorPickerScreen(
            colorName = "Main",
            color = Color(0xffff9800).toHsv(),
            onColorChanged = {},
            navBack = {}
        )
    }
}