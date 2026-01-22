package com.subreax.lightclient.ui.colorpickerscreen

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.subreax.lightclient.ui.theme.LightClientTheme

@Composable
fun ColorLibrary(
    pickedColor: Color,
    savedColors: List<Color>,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (savedColors.isNotEmpty()) {
            savedColors.forEach {
                SavedColor(
                    color = it,
                    isSelected = it == pickedColor,
                    onColorSelected = {
                        onColorSelected(it)
                    },
                    modifier = Modifier.size(48.dp)
                )
            }
        } else {
            Text(
                text = "Библиотека пуста",
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}


@Composable
private fun SavedColor(
    color: Color,
    isSelected: Boolean,
    onColorSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) {
            color.copy(alpha = 0.6f)
        } else {
            Color.Transparent
        }
    )

    Spacer(
        modifier = Modifier
            .border(3.dp, borderColor, CircleShape)
            .padding(6.dp)
            .clip(CircleShape)
            .clickable(onClick = onColorSelected)
            .background(color)
            .then(modifier)
    )
}

@Preview(name = "NotSelected (can be toggled)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun SavedColorPreview() {
    LightClientTheme {
        Surface {
            var isSelected by remember { mutableStateOf(false) }

            SavedColor(
                color = Color.Magenta,
                isSelected = isSelected,
                onColorSelected = { isSelected = !isSelected },
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@Preview(name = "Selected", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun SavedColorSelectedPreview() {
    LightClientTheme {
        Surface {
            SavedColor(
                color = Color.Magenta,
                isSelected = true,
                onColorSelected = {},
                modifier = Modifier.size(48.dp)
            )
        }
    }
}