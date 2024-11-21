package com.subreax.lightclient.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.subreax.lightclient.ui.theme.LightClientTheme

@Composable
fun TinyTextField(
    text: String,
    onTextChanged: (String) -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTextStyle.current
) {
    val mergedTextStyle = textStyle.merge(
        TextStyle(
            color = LocalContentColor.current,
            textAlign = TextAlign.Center
        )
    )

    BasicTextField(
        value = text,
        onValueChange = onTextChanged,
        decorationBox = { innerTextField ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    style = MaterialTheme.typography.body2.copy(color = LocalContentColorMediumAlpha)
                )
                innerTextField()
            }
        },
        modifier = modifier,
        singleLine = true,
        textStyle = mergedTextStyle,
        cursorBrush = SolidColor(MaterialTheme.colors.primary)
    )
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TinyTextFieldPreview() {
    LightClientTheme {
        Surface {
            TinyTextField(
                text = "255",
                onTextChanged = { },
                title = "DC offset",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}
