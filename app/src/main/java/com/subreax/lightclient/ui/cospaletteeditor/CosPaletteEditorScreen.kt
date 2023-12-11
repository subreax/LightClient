package com.subreax.lightclient.ui.cospaletteeditor

import android.content.res.Configuration
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
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.subreax.lightclient.ui.TopBar
import com.subreax.lightclient.ui.theme.LightClientTheme


@Composable
fun CosPaletteEditorScreen(
    navBack: () -> Unit,
    viewModel: CosPaletteEditorViewModel = hiltViewModel()
) {
    val state = viewModel.state

    Column(
        Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        TopBar(
            title = viewModel.propertyName,
            subtitle = {
                Text(text = "Настройка цветовой палитры")
            },
            navBack = navBack
        )

        CosPaletteEditor(
            state = state,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.2f),
            onChange = {
                viewModel.onPaletteChanged()
            }
        )

        CosPaletteViewer(
            palette = CosPaletteData(
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

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 320, heightDp = 200)
@Composable
fun CosPaletteEditorPreview() {
    LightClientTheme {
        Surface {
            CosPaletteEditor(Modifier.fillMaxSize())
        }
    }
}