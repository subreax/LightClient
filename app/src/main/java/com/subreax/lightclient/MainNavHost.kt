package com.subreax.lightclient

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
import com.subreax.lightclient.ui.home.HomeScreen


sealed class Screen(val route: String) {
    object Connection : Screen("connection_route")

    object Home : Screen("home_screen")

    object ColorPicker : Screen("color_picker") {
        val propertyIdArg = "id"
        val routeWithArgs = "$route/{$propertyIdArg}"
        val args = listOf(
            navArgument(propertyIdArg) { type = NavType.IntType }
        )
    }

    object Explore : Screen("explore_screen")
}

@Composable
fun MainNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Screen.Connection.route) {
        composable(Screen.Connection.route) {
            ConnectionScreen(navHome = {})
        }

        composable(Screen.Home.route) {
            HomeScreen(navToColorPicker = {
                navController.navigate("${Screen.ColorPicker.route}/$it")
            })
        }

        composable(
            route = Screen.ColorPicker.routeWithArgs,
            arguments = Screen.ColorPicker.args
        ) {
            ColorPickerScreen(navBack = {
                navController.popBackStack()
            })
        }

        composable(Screen.Explore.route) {
            ExploreScreen()
        }
    }
}

