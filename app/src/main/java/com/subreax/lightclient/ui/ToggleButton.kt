package com.subreax.lightclient.ui

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.subreax.lightclient.ui.theme.LightClientTheme

@Composable
fun ToggleButton(
    isActive: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colors.primary,
    text: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onToggle(!isActive) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val dashColor = if (isActive) {
            activeColor
        } else {
            LocalContentColor.current.copy(alpha = ContentAlpha.disabled)
        }

        text()

        Box(modifier = Modifier
            .size(24.dp, 4.dp)
            .clip(CircleShape)
            .drawBehind {
                drawRect(dashColor)
            }
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CosineToggleButtonPreview() {
    LightClientTheme {
        Surface {
            ToggleButton(isActive = false, onToggle = { }) {
                Text(text = "Green")
            }
        }
    }
}