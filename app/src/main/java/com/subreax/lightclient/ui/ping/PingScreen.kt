package com.subreax.lightclient.ui.ping

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.subreax.lightclient.R
import com.subreax.lightclient.ui.LocalContentColorMediumAlpha
import com.subreax.lightclient.ui.TopBar
import com.subreax.lightclient.ui.theme.LightClientTheme

@Composable
fun PingScreen(navBack: () -> Unit, pingViewModel: PingViewModel = hiltViewModel()) {
    PingScreen(
        navBack = navBack,
        ping = pingViewModel.ping
    )
}

@Composable
fun PingScreen(
    ping: Int,
    navBack: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        TopBar(
            title = stringResource(R.string.ping_measurement),
            subtitle = {
                Text(stringResource(R.string.figure_out_connection_quality_with_your_controller))
            },
            navBack = navBack
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "$ping", style = MaterialTheme.typography.h3)
            Text("мс", color = LocalContentColorMediumAlpha)
        }
    }
}

@Preview(widthDp = 420, heightDp = 800, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun PingScreenPreview() {
    LightClientTheme {
        PingScreen(ping = 59, navBack = {})
    }
}