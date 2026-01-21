package com.subreax.lightclient.ui.colorpickerscreen

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.subreax.lightclient.R
import com.subreax.lightclient.ui.ToggleButton
import com.subreax.lightclient.ui.TopBar
import com.subreax.lightclient.ui.colorpicker.ColorPicker
import com.subreax.lightclient.ui.colorpicker.ColorPickerState
import com.subreax.lightclient.ui.theme.LightClientTheme


@Composable
fun ColorPickerScreen(
    colorPickerViewModel: ColorPickerViewModel = hiltViewModel(),
    navBack: () -> Unit = {}
) {
    val state = colorPickerViewModel.colorPickerState

    ColorPickerScreen(
        colorName = colorPickerViewModel.propertyName,
        state = state,
        navBack = navBack
    )
}

@Composable
fun ColorPickerScreen(
    colorName: String,
    state: ColorPickerState,
    navBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .navigationBarsPadding()
    ) {
        TopBar(
            title = colorName,
            subtitle = {
                Text(stringResource(R.string.choose_a_color))
            },
            navBack = navBack
        )

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            ToggleButton(
                isActive = true,
                onToggle = {}
            ) {
                Text("Выбор цвета")
            }

            ToggleButton(
                isActive = false,
                onToggle = {}
            ) {
                Text("Библиотека")
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            ColorPicker(
                state = state,
                svPickerAspectRatio = 1f
            )
        }

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
            state = ColorPickerState(Color(0xffff9800u), {}),
            navBack = {}
        )
    }
}