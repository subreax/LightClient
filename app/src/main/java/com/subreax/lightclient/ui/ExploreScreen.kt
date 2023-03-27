package com.subreax.lightclient.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.subreax.lightclient.ui.colorpicker.ColorPicker
import com.subreax.lightclient.ui.colorpicker.toHsv
import com.subreax.lightclient.ui.theme.LightClientTheme
import kotlin.math.roundToInt


@Composable
fun ExploreScreen() {
    val initialColor = Color(0xffff9800)
    var hsv by remember { mutableStateOf(initialColor.toHsv()) }

    Column(
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxSize()
    ) {
        ColorPicker(
            hsv = hsv,
            onColorChanged = { hsv = it },
            svPickerAspectRatio = 1f,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = LocalContentColorMediumAlpha)) {
                    append("hex | ")
                }
                append("${hsv.toColor().toHex()}  ${(hsv.a * 100).roundToInt()}%")
            },
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}


@Preview(uiMode = UI_MODE_NIGHT_YES, widthDp = 400, heightDp = 800, showBackground = true)
@Composable
fun ExploreScreenPreview() {
    LightClientTheme {
        ExploreScreen()
    }
}

private fun Int.byteHex(): String {
    val value = this and 0xff
    return if (value > 16) {
        value.toString(16)
    } else {
        "0${value.toString(16)}"
    }.uppercase()
}

private fun Color.toHex(): String {
    val r = (red * 255).roundToInt()
    val g = (green * 255).roundToInt()
    val b = (blue * 255).roundToInt()

    return "${r.byteHex()}${g.byteHex()}${b.byteHex()}"
}