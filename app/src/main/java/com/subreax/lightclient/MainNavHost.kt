package com.subreax.lightclient

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.subreax.lightclient.ui.ExploreScreen
import com.subreax.lightclient.ui.colorpickerscreen.ColorPickerScreen
import com.subreax.lightclient.ui.connection.ConnectionScreen
import com.subreax.lightclient.ui.cospaletteeditor.CosPaletteEditorScreen
import com.subreax.lightclient.ui.enumscreen.EnumScreen
import com.subreax.lightclient.ui.home.HomeScreen
import com.subreax.lightclient.ui.ping.PingScreen


sealed class Screen(val route: String) {
    object Connection : Screen("connection_route")

    object Home : Screen("home_screen")

    object ColorPicker : Screen("color_picker") {
        const val propertyIdArg = "id"
        val routeWithArgs = "$route/{$propertyIdArg}"
        val args = listOf(
            navArgument(propertyIdArg) { type = NavType.IntType }
        )
    }

    object EnumPicker : Screen("enum_picker_screen") {
        const val propertyIdArg = "id"
        val routeWithArgs = "$route/{$propertyIdArg}"
        val args = listOf(
            navArgument(propertyIdArg) { type = NavType.IntType }
        )
    }

    object CosPaletteEditor : Screen("cos_palette_editor_screen")

    object Ping : Screen("ping_screen")

    object Explore : Screen("explore_screen")
}

@Composable
fun MainNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = Screen.CosPaletteEditor.route,
        enterTransition = {
            fadeIn(tween(200))
        },
        exitTransition = {
            fadeOut(tween(200))
        }
    ) {
        composable(Screen.Connection.route) {
            ConnectionScreen(navHome = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(navController.currentDestination!!.route!!) {
                        inclusive = true
                    }
                }
            })
        }

        composable(Screen.Home.route) {
            HomeScreen(
                navToColorPicker = {
                    navController.navigate("${Screen.ColorPicker.route}/$it")
                },
                navToEnumPicker = {
                    navController.navigate("${Screen.EnumPicker.route}/$it")
                },
                navToPaletteEditor = {
                    navController.navigate(Screen.CosPaletteEditor.route)
                },
                navToPingScreen = {
                    navController.navigate(Screen.Ping.route)
                },
                navBack = {
                    navController.navigate(Screen.Connection.route) {
                        popUpTo(navController.currentDestination!!.route!!) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(
            route = Screen.ColorPicker.routeWithArgs,
            arguments = Screen.ColorPicker.args
        ) {
            ColorPickerScreen(navBack = {
                navController.popBackStack()
            })
        }

        composable(
            route = Screen.EnumPicker.routeWithArgs,
            arguments = Screen.EnumPicker.args
        ) {
            EnumScreen(navBack = {
                navController.popBackStack()
            })
        }

        composable(Screen.CosPaletteEditor.route) {
            CosPaletteEditorScreen()
        }

        composable(Screen.Ping.route) {
            PingScreen(navBack = {
                navController.popBackStack()
            })
        }

        composable(Screen.Explore.route) {
            ExploreScreen()
        }
    }
}

