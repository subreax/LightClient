package com.subreax.lightclient.ui.connection

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.subreax.lightclient.ui.LocalContentColorMediumAlpha
import com.subreax.lightclient.ui.TopBar
import com.subreax.lightclient.ui.edgePadding
import com.subreax.lightclient.ui.edgePaddingValue
import com.subreax.lightclient.ui.theme.LightClientTheme

data class Device(
    val name: String,
    val address: String
)

@Composable
fun ConnectionScreen(devices: List<Device>) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .fillMaxWidth()
    ) {
        TopBar(title = "Подключение к контроллеру")
        Text(
            text = "Выберите, к какому контроллеру вы хотите подключиться. Если вашего контроллера нет в этом списке, убедитесь, что вы выполнили сопряжение с ним в настройках Bluetooth",
            color = LocalContentColorMediumAlpha,
            modifier = Modifier.edgePadding()
        )
        Divider(
            Modifier
                .fillMaxWidth(2.0f / 3.0f)
                .padding(vertical = 16.dp), thickness = 2.dp
        )

        if (devices.isNotEmpty()) {
            devices.forEach { device ->
                DeviceItem(name = device.name, address = device.address)
            }
        }
        else {
            Text("Устройств не обнаружено \uD83D\uDE41", modifier = Modifier.edgePadding())
        }
    }
}


@Composable
fun DeviceItem(name: String, address: String) {
    Row(
        modifier = Modifier
            .clickable { }
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .edgePadding()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(4.dp)
                .background(MaterialTheme.colors.primary)
        )
        Spacer(Modifier.width(12.dp))
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(name)
            Text(
                text = address,
                style = MaterialTheme.typography.body2,
                color = LocalContentColorMediumAlpha
            )
        }
    }
}


@Preview(uiMode = UI_MODE_NIGHT_YES, widthDp = 320, heightDp = 640, showBackground = true)
@Composable
fun ConnectionScreenPreview() {
    LightClientTheme {
        ConnectionScreen(
            devices = listOf(
                Device("ESP32-Home", "FC:81:CC:4F:8E:36"),
                Device("ESP32-Kitchen", "D1:09:75:BA:15:2D"),
                Device("ESP32-Bath", "5A:46:70:63:6E:99")
            )
        )
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES, widthDp = 320, heightDp = 640, showBackground = true)
@Composable
fun ConnectionScreenPreviewNoDevices() {
    LightClientTheme {
        ConnectionScreen(
            devices = emptyList()
        )
    }
}