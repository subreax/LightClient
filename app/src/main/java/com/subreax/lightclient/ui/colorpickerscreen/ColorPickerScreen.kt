package com.subreax.lightclient.ui.colorpickerscreen

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.subreax.lightclient.R
import com.subreax.lightclient.ui.ToggleButton
import com.subreax.lightclient.ui.TopBar
import com.subreax.lightclient.ui.colorpicker.ColorPicker
import com.subreax.lightclient.ui.colorpicker.ColorPickerState
import com.subreax.lightclient.ui.theme.LightClientTheme

private enum class ColorPickerSection {
    Picker, Library
}

@Composable
fun ColorPickerScreen(
    colorPickerViewModel: ColorPickerViewModel = hiltViewModel(),
    navBack: () -> Unit = {}
) {
    val state = colorPickerViewModel.colorPickerState
    var section by rememberSaveable {
        mutableStateOf(ColorPickerSection.Picker)
    }

    val colorLibrary by colorPickerViewModel.colorLibrary.collectAsState()

    ColorPickerScreen(
        colorName = colorPickerViewModel.propertyName,
        state = state,
        colorLibrary = colorLibrary,
        navBack = navBack,
        addColorToLibrary = colorPickerViewModel::addColorToLibrary,
        deleteColorFromLibrary = colorPickerViewModel::deleteColorFromLibrary,
        currentSection = section,
        setCurrentSection = { section = it }
    )
}

@Composable
private fun ColorPickerScreen(
    colorName: String,
    state: ColorPickerState,
    colorLibrary: List<Color>,
    navBack: () -> Unit,
    addColorToLibrary: (Color) -> Unit,
    deleteColorFromLibrary: (Color) -> Unit,
    currentSection: ColorPickerSection,
    setCurrentSection: (ColorPickerSection) -> Unit
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
                isActive = currentSection == ColorPickerSection.Picker,
                onToggle = {
                    setCurrentSection(ColorPickerSection.Picker)
                }
            ) {
                Text("Выбор цвета")
            }

            ToggleButton(
                isActive = currentSection == ColorPickerSection.Library,
                onToggle = {
                    setCurrentSection(ColorPickerSection.Library)
                }
            ) {
                Text("Библиотека")
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            AnimatedContent(
                targetState = currentSection,
                transitionSpec = { slideAndFade() }
            ) { section ->
                when (section) {
                    ColorPickerSection.Picker -> {
                        ColorPicker(
                            state = state,
                            addColorToLibrary = addColorToLibrary,
                            svPickerAspectRatio = 1f
                        )
                    }

                    ColorPickerSection.Library -> {
                        ColorLibrary(
                            pickedColor = state.color,
                            savedColors = colorLibrary,
                            onColorSelected = { state.update(it) },
                            onDeleteColor = deleteColorFromLibrary,
                            modifier = Modifier
                                .padding(top = 8.dp, start = 16.dp, end = 16.dp)
                                .fillMaxWidth()
                        )
                    }
                }
            }
        }

    }
}

fun slideAndFade(): ContentTransform {
    val enter = slideInVertically(animationSpec = tween(200)) + fadeIn(animationSpec = tween(200))
    val exit = slideOutVertically(animationSpec = tween(200)) + fadeOut(animationSpec = tween(200))
    return enter.togetherWith(exit)
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    widthDp = 360,
    heightDp = 780,
    showBackground = true
)
@Composable
private fun ColorPickerScreenPreview() {
    LightClientTheme {
        ColorPickerScreen(
            colorName = "Main",
            state = ColorPickerState(Color(0xffff9800u), {}),
            colorLibrary = emptyList(),
            navBack = {},
            addColorToLibrary = {},
            deleteColorFromLibrary = {},
            currentSection = ColorPickerSection.Picker,
            setCurrentSection = {}
        )
    }
}