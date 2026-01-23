package com.subreax.lightclient.ui.cospaletteeditor

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.subreax.lightclient.ui.ToggleButton
import com.subreax.lightclient.ui.TopBar
import com.subreax.lightclient.ui.theme.LightClientTheme


@Composable
fun CosPaletteEditorScreen(
    navBack: () -> Unit,
    viewModel: CosPaletteEditorViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()

    CosPaletteEditorScreen(
        cosPaletteEditorState = viewModel.cosPaletteEditorState,
        cosFieldsEditorState = viewModel.cosFieldsEditorState,
        propertyName = viewModel.propertyName,
        navBack = navBack,
        selectCosine = viewModel::selectCosine,
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(scrollState)
    )
}

@Composable
fun CosPaletteEditorScreen(
    cosPaletteEditorState: CosPaletteEditorState,
    cosFieldsEditorState: CosineFieldsEditorState,
    propertyName: String,
    navBack: () -> Unit,
    selectCosine: (CosPaletteEditorState.CosineId) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        TopBar(
            title = propertyName,
            subtitle = { Text(text = "Настройка цветовой палитры") },
            navBack = navBack
        )

        CosPaletteEditor(
            state = cosPaletteEditorState,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.2f)
        )

        CosPaletteViewer(
            redCosine = cosPaletteEditorState.red,
            greenCosine = cosPaletteEditorState.green,
            blueCosine = cosPaletteEditorState.blue,
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            ToggleButton(
                isActive = cosPaletteEditorState.selectedCosineId == CosPaletteEditorState.CosineId.Red,
                onToggle = { selectCosine(CosPaletteEditorState.CosineId.Red) },
                activeColor = Color.Red
            ) {
                Text("Red")
            }

            ToggleButton(
                isActive = cosPaletteEditorState.selectedCosineId == CosPaletteEditorState.CosineId.Green,
                onToggle = { selectCosine(CosPaletteEditorState.CosineId.Green) },
                activeColor = Color.Green
            ) {
                Text("Green")
            }

            ToggleButton(
                isActive = cosPaletteEditorState.selectedCosineId == CosPaletteEditorState.CosineId.Blue,
                onToggle = { selectCosine(CosPaletteEditorState.CosineId.Blue) },
                activeColor = Color.Blue
            ) {
                Text("Blue")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (cosPaletteEditorState.selectedCosineId != CosPaletteEditorState.CosineId.Null) {
            CosineFieldsEditor(
                state = cosFieldsEditorState,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CosPaletteEditorPreview() {
    LightClientTheme {
        val paletteEditorState = remember {
            CosPaletteEditorState(
                Cosine(0.5f, 0.5f, 1.0f, 0.0f),
                Cosine(0.5f, 0.5f, 1.0f, 1f / 3f),
                Cosine(0.5f, 0.5f, 1.0f, 2f / 3f)
            ).also { it.select(CosPaletteEditorState.CosineId.Red) }
        }

        val cosineEditorState = remember {
            CosineFieldsEditorState(paletteEditorState.red, {})
        }

        CosPaletteEditorScreen(
            cosPaletteEditorState = paletteEditorState,
            cosFieldsEditorState = cosineEditorState,
            propertyName = "PaletteName",
            navBack = {},
            selectCosine = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}