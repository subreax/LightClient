package com.subreax.lightclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import com.subreax.lightclient.ui.theme.LightClientTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(ComposeView(this).apply {
            consumeWindowInsets = false

            setContent {
                LightClientTheme {
                    MainNavHost()
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
