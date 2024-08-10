package com.subreax.lightclient

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.subreax.lightclient.ui.UiLog
import com.subreax.lightclient.ui.theme.LightClientTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var uiLog: UiLog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.BLUETOOTH_CONNECT, android.Manifest.permission.BLUETOOTH_SCAN),
                0
            )
        }

        setContentView(ComposeView(this).apply {
            consumeWindowInsets = false

            setContent {
                val navController = rememberNavController()

                LightClientTheme(darkTheme = true) {
                    UiLogHandler(uiLog) {
                        MainNavHost(navController)
                    }
                }
            }
        })
    }
}

@Composable
fun UiLogHandler(uiLog: UiLog, content: @Composable BoxScope.() -> Unit) {
    val snackbarState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarState) {
        uiLog.bind(snackbarState)
    }

    Box(Modifier.fillMaxSize()) {
        content()

        SnackbarHost(
            hostState = snackbarState,
            modifier = Modifier.align(Alignment.BottomStart)
        )
    }
}
