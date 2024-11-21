package com.subreax.lightclient.ui.cospaletteeditor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.subreax.lightclient.ui.TinyTextField


@Composable
fun CosineFieldsEditor(
    state: CosineFieldsEditorState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TinyTextField(
            text = state.dcOffsetText,
            onTextChanged = state::updateDcOffset,
            title = "DC offset",
            modifier = Modifier.weight(1f)
        )
        TinyTextField(
            text = state.ampText,
            onTextChanged = state::updateAmp,
            title = "Amp",
            modifier = Modifier.weight(1f)
        )
        TinyTextField(
            text = state.freqText,
            onTextChanged = state::updateFreq,
            title = "Freq",
            modifier = Modifier.weight(1f)
        )
        TinyTextField(
            text = state.phaseText,
            onTextChanged = state::updatePhase,
            title = "Phase",
            modifier = Modifier.weight(1f)
        )
    }
}
