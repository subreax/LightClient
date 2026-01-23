package com.subreax.lightclient.ui.colorpickerscreen

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.subreax.lightclient.ui.theme.LightClientTheme

@Composable
fun ColorLibrary(
    pickedColor: Color,
    savedColors: List<Color>,
    onColorSelected: (Color) -> Unit,
    onDeleteColor: (Color) -> Unit,
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
                    onColorSelected = { onColorSelected(it) },
                    onDeleteColor = { onDeleteColor(it) },
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
    onDeleteColor: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = tween(durationMillis = 100)
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) {
            color.copy(alpha = 0.6f)
        } else {
            Color.Transparent
        },
        animationSpec = tween(durationMillis = 200)
    )

    Box {
        Spacer(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .border(3.dp, borderColor, CircleShape)
                .padding(6.dp)
                .clip(CircleShape)
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onColorSelected,
                    onLongClick = { isMenuExpanded = true }
                )
                .background(color)
                .then(modifier)
        )

        DropdownMenu(
            expanded = isMenuExpanded,
            onDismissRequest = { isMenuExpanded = false },
            offset = DpOffset(4.dp, 4.dp),
            properties = PopupProperties()
        ) {
            DropdownMenuItem(onClick = {
                isMenuExpanded = false
                onDeleteColor()
            }) {
                Text("Удалить")
            }
        }
    }
}

@Preview(name = "NotSelected (interactive)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun SavedColorPreview() {
    LightClientTheme {
        Surface {
            var isSelected by remember { mutableStateOf(false) }

            SavedColor(
                color = Color.Magenta,
                isSelected = isSelected,
                onColorSelected = { isSelected = !isSelected },
                onDeleteColor = {},
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
                onDeleteColor = {},
                modifier = Modifier.size(48.dp)
            )
        }
    }
}