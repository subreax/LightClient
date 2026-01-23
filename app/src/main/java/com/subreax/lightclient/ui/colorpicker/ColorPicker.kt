package com.subreax.lightclient.ui.colorpicker

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.subreax.lightclient.ui.TinyTextField
import com.subreax.lightclient.ui.TinyTextFieldOrientation
import com.subreax.lightclient.ui.theme.LightClientTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun ColorPicker(
    state: ColorPickerState,
    addColorToLibrary: (Color) -> Unit,
    modifier: Modifier = Modifier,
    svPickerAspectRatio: Float = 4.0f / 3.0f,
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) {
    Column(
        modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SVPicker(
            hsv = state.hsva,
            modifier = Modifier
                .aspectRatio(svPickerAspectRatio)
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ColorDisplay(state.color)

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                HuePicker(
                    hue = state.hsva.h,
                    onHueChanged = {
                        state.hsva.update(hue = it)
                    }
                )

                AlphaPicker(
                    hsv = state.hsva,
                    alpha = state.hsva.a,
                    onAlphaChanged = {
                        state.hsva.update(alpha = it)
                    }
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            TinyTextField(
                text = state.hex.value,
                onTextChanged = { state.hex.update(it) },
                title = "HEX",
                modifier = Modifier.width(128.dp),
                orientation = TinyTextFieldOrientation.Horizontal,
                innerPadding = PaddingValues(vertical = 16.dp)
            )


            SaveButton(
                onClick = { addColorToLibrary(state.color) },
                textSave = {
                    Text(
                        text = "+ Сохранить",
                        softWrap = false
                    )
                },
                textDone = {
                    Text(
                        text = "Готово",
                        softWrap = false
                    )
                },
                coroutineScope = coroutineScope
            )
        }

    }
}

@Composable
private fun SaveButton(
    onClick: () -> Unit,
    textSave: @Composable () -> Unit,
    textDone: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) {
    var done by remember { mutableStateOf(false) }

    TextButton(onClick = {
        coroutineScope.launch {
            delay(2000L)
            done = false
        }
        done = true
        onClick()
    }, modifier = modifier) {
        AnimatedContent(
            targetState = done,
            transitionSpec = {
                val enter = fadeIn(animationSpec = tween(durationMillis = 150, delayMillis = 150))
                val exit = fadeOut(animationSpec = tween(durationMillis = 150))
                enter.togetherWith(exit)
            }
        ) { done ->
            if (!done) {
                textSave()
            } else {
                textDone()
            }
        }
    }
}

@Preview
@Composable
private fun ColorPickerPreview() {
    LightClientTheme {
        SVPicker(
            hsv = HsvaColorPickerState(Color(0xffff9800), {}),
            modifier = Modifier.size(300.dp)
        )
    }
}


@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun SaveButtonPreview() {
    LightClientTheme {
        Surface {
            SaveButton(
                onClick = {},
                textSave = {
                    Text("+ Сохранить", softWrap = false)
                },
                textDone = {
                    Text("Готово", softWrap = false)
                }
            )
        }
    }
}