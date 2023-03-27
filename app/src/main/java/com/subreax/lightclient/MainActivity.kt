package com.subreax.lightclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.subreax.lightclient.data.state.AppStateId
import com.subreax.lightclient.data.state.ApplicationState
import com.subreax.lightclient.ui.LocalContentColorMediumAlpha
import com.subreax.lightclient.ui.UiLog
import com.subreax.lightclient.ui.theme.LightClientTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var appState: ApplicationState

    @Inject
    lateinit var uiLog: UiLog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(ComposeView(this).apply {
            consumeWindowInsets = false

            setContent {
                val navController = rememberNavController()
                val applicationState = appState.stateId.collectAsState(AppStateId.WaitingForConnectivity)

                AppStateObserver(appState, navController)

                LightClientTheme {
                    UiLogHandler(uiLog) {
                        MainNavHost(navController)

                        AppStateDebugLabel(
                            appStateId = applicationState.value,
                            modifier = Modifier
                                .statusBarsPadding()
                                .align(Alignment.BottomStart)
                                .padding(start = 16.dp)
                        )
                    }
                }
            }
        })
    }
}

@Composable
fun AppStateObserver(appState: ApplicationState, navController: NavHostController) {
    LaunchedEffect(true) {
        appState.stateId.collect {
            when (it) {
                AppStateId.Ready -> {
                    val startDestination = navController.graph.startDestinationRoute!!
                    navController.navigate(Screen.Home.route) {
                        launchSingleTop = true
                        popUpTo(startDestination) { inclusive = true }
                    }
                }

                AppStateId.WaitingForConnectivity,
                AppStateId.Disconnected -> {
                    val currentRoute =
                        navController.currentBackStackEntry?.destination?.route

                    currentRoute.let { route ->
                        if (route != Screen.Connection.route) {
                            navController.navigate(Screen.Connection.route) {
                                launchSingleTop = true
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        }
                    }
                }

                else -> {}
            }
        }
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

@Composable
fun AppStateDebugLabel(appStateId: AppStateId, modifier: Modifier = Modifier) {
    Text(
        text = appStateId.toString(),
        modifier = modifier,
        color = LocalContentColorMediumAlpha
    )
}