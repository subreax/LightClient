package com.subreax.lightclient.ui.cospaletteeditor

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.subreax.lightclient.ui.TopBar
import com.subreax.lightclient.ui.theme.LightClientTheme


@Composable
fun CosPaletteEditorScreen() {
    val state = remember {
        CosPaletteEditorState().also {
            it.setRed(0.5f, 0.5f, 1f, 0f)
            it.setGreen(0.5f, 0.5f, 1f, 0.33333f)
            it.setBlue(0.5f, 0.5f, 1f, 0.66f)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        TopBar(
            title = "Цветовая палитра",
            subtitle = {
                Text(text = "Зажги привычные эффекты по-новому")
            },
            navBack = {
                // todo: not implemented
            }
        )

        CosPaletteEditor(
            state = state,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.2f)
        )

        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(onClick = { state.select(CosPaletteEditorState.CosineId.Red) }) {
                Text("Red")
            }

            Button(onClick = { state.select(CosPaletteEditorState.CosineId.Green) }) {
                Text("Green")
            }

            Button(onClick = { state.select(CosPaletteEditorState.CosineId.Blue) }) {
                Text("Blue")
            }

            Button(onClick = { state.select(CosPaletteEditorState.CosineId.Null) }) {
                Text("Null")
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 320, heightDp = 200)
@Composable
fun CosPaletteEditorPreview() {
    LightClientTheme {
        Surface {
            CosPaletteEditor(Modifier.fillMaxSize())
        }
    }
}