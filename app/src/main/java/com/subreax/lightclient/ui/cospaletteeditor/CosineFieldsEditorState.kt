package com.subreax.lightclient.ui.cospaletteeditor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.round

class CosineFieldsEditorState(
    initCosine: Cosine,
    val onCosineChanged: (Cosine) -> Unit
) {
    var cosine = initCosine
        set(value) {
            field = value
            updateAllTexts()
        }

    var dcOffsetText by mutableStateOf(initCosine.dcOffset.round4().toString())
        private set

    var ampText by mutableStateOf(initCosine.amp.round4().toString())
        private set

    var freqText by mutableStateOf(initCosine.freq.round4().toString())
        private set

    var phaseText by mutableStateOf(initCosine.phase.round4().toString())
        private set


    fun updateDcOffset(newDcOffset: String) {
        dcOffsetText = newDcOffset
        newDcOffset.tryParseFloat()?.let {
            cosine = cosine.copy(dcOffset = it)
            onCosineChanged()
        }
    }

    fun updateAmp(newAmp: String) {
        ampText = newAmp
        newAmp.tryParseFloat()?.let {
            cosine = cosine.copy(amp = it)
            onCosineChanged()
        }
    }

    fun updateFreq(newFreq: String) {
        freqText = newFreq
        newFreq.tryParseFloat()?.let {
            cosine = cosine.copy(freq = it)
            onCosineChanged()
        }
    }

    fun updatePhase(newPhase: String) {
        phaseText = newPhase
        newPhase.tryParseFloat()?.let {
            cosine = cosine.copy(phase = it)
            onCosineChanged()
        }
    }

    private fun updateAllTexts() {
        dcOffsetText = cosine.dcOffset.toStringR4()
        ampText = cosine.amp.toStringR4()
        freqText = cosine.freq.toStringR4()
        phaseText = cosine.phase.toStringR4()
    }

    private fun onCosineChanged() {
        onCosineChanged(cosine)
    }

    private fun String.tryParseFloat(): Float? {
        return if (this.isNotEmpty() && !this.endsWith('.')) {
            toFloatOrNull()
        } else {
            null
        }
    }

    private fun Float.toStringR4(): String {
        return this.round4().toString()
    }

    private fun Float.round4(): Float {
        return round(this * 10000f) / 10000f
    }
}