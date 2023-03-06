package com.subreax.lightclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import com.subreax.lightclient.ui.connection.ConnectionScreen
import com.subreax.lightclient.ui.connection.Device
import com.subreax.lightclient.ui.home.HomeScreen
import com.subreax.lightclient.ui.theme.LightClientTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(ComposeView(this).apply {
            consumeWindowInsets = false

            setContent {
                LightClientTheme {
                    ConnectionScreen(listOf(
                        Device("ESP32-Home", "FC:81:CC:4F:8E:36"),
                        Device("ESP32-Kitchen", "D1:09:75:BA:15:2D"),
                        Device("ESP32-Bath", "5A:46:70:63:6E:99")
                    ))
                    //HomeScreen()
                }
            }
        })
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    LightClientTheme {
        Greeting("Android")
    }
}
