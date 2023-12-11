package com.subreax.lightclient.ui.cospaletteeditor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.subreax.lightclient.ui.TopBar

@Composable
fun CosPaletteEditorPreviewScreen() {
    val state = remember {
        CosPaletteEditorState().apply {
            setRed(0.5f, 0.5f, 1f, 0f)
            setGreen(0.5f, 0.5f, 1f, 1f/2f)
            setBlue(0.5f, 0.5f, 1f, 2f/3f)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        TopBar(
            title = "Palette",
            subtitle = {
                Text(text = "Настройка цветовой палитры")
            },
            navBack = { }
        )

        CosPaletteEditor(
            state = state,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.2f),
        )

        CosPaletteViewer(
            palette = CosPalette(
                state.getRed(),
                state.getGreen(),
                state.getBlue()
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
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