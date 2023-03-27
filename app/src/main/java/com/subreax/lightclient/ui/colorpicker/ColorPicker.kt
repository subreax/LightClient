package com.subreax.lightclient.ui.colorpicker

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.subreax.lightclient.ui.theme.LightClientTheme


@Composable
fun ColorPicker(
    hsv: HSVColor,
    onColorChanged: (HSVColor) -> Unit,
    modifier: Modifier = Modifier,
    svPickerAspectRatio: Float = 4.0f / 3.0f
) {
    val currentHsv by rememberUpdatedState(hsv)

    Column(
        modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SVPicker(
            hsv = currentHsv,
            onColorChanged = {
                onColorChanged(it)
            },
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
            ColorDisplay(currentHsv.toColor())

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                HuePicker(
                    hue = currentHsv.h,
                    onHueChanged = {
                        onColorChanged(currentHsv.copy(h = it))
                    }
                )

                AlphaPicker(
                    hsv = currentHsv,
                    alpha = currentHsv.a,
                    onAlphaChanged = {
                        onColorChanged(currentHsv.copy(a = it))
                    }
                )
            }
        }
    }
}

@Preview
@Composable
fun ColorPickerPreview() {
    LightClientTheme {
        SVPicker(
            hsv = HSVColor.from(Color(0xffff9800)),
            onColorChanged = { },
            modifier = Modifier.size(300.dp)
        )
    }
}
