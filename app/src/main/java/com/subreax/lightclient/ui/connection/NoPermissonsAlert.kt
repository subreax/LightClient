package com.subreax.lightclient.ui.connection

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.subreax.lightclient.ui.LocalContentColorMediumAlpha
import com.subreax.lightclient.ui.theme.LightClientTheme
import com.subreax.lightclient.ui.theme.warning

@Composable
fun NoPermissionsAlert(
    requestPermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)

    Row(
        modifier = modifier
            .clip(shape)
            .clickable(onClick = requestPermissions)
            .background(MaterialTheme.colors.warning.copy(alpha = 0.1f))
            .border(1.dp, MaterialTheme.colors.warning.copy(alpha = 0.5f), shape)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.WarningAmber,
            contentDescription = "Warning",
            modifier = Modifier.size(32.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Отсутствуют необходимые права для работы Bluetooth",
                style = MaterialTheme.typography.body1,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Нажмите здесь, чтобы предоставить их",
                color = LocalContentColorMediumAlpha,
                style = MaterialTheme.typography.body2
            )
        }
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun NoPermissionsAlertPreview() {
    LightClientTheme {
        Surface {
            NoPermissionsAlert(requestPermissions = {}, modifier = Modifier.padding(8.dp))
        }
    }
}