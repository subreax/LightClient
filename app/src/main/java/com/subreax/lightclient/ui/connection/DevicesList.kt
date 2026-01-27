package com.subreax.lightclient.ui.connection

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.subreax.lightclient.R
import com.subreax.lightclient.data.DeviceDesc
import com.subreax.lightclient.ui.LocalContentColorMediumAlpha
import com.subreax.lightclient.ui.edgePadding

@Composable
fun DevicesList(
    devices: List<DeviceDesc>,
    waitingForConnectivity: Boolean,
    onDeviceSelected: (DeviceDesc) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier) {
        if (waitingForConnectivity) {
            Text(
                text = stringResource(R.string.waiting_for_connectivity),
                modifier = Modifier.edgePadding()
            )
        } else {
            if (devices.isEmpty()) {
                Text(
                    text = stringResource(R.string.devices_not_found),
                    modifier = Modifier.edgePadding()
                )
            }

            LazyColumn(Modifier.fillMaxSize()) {
                items(items = devices, key = { it.address }) { device ->
                    DeviceItem(
                        name = device.name,
                        address = device.address,
                        onClick = { onDeviceSelected(device) },
                        modifier = Modifier.animateItem(),
                        contentModifier = Modifier
                            .height(64.dp)
                            .padding(vertical = 8.dp)
                            .edgePadding()
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun DeviceItem(
    name: String,
    address: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .then(contentModifier),
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