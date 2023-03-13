package com.subreax.lightclient

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.subreax.lightclient.ui.ExploreScreen
import com.subreax.lightclient.ui.connection.ConnectionScreen
import com.subreax.lightclient.ui.home.HomeScreen


sealed class Screen(val route: String) {
    object Connection : Screen("connection_route")

    object Home : Screen("home_screen") {
        val deviceAddressArg = "device_address"
        val args = listOf(
            navArgument(deviceAddressArg) { type = NavType.StringType }
        )

        val routeWithArgs = "$route/{$deviceAddressArg}"

        fun buildRoute(deviceAddress: String): String {
            return "$route/$deviceAddress"
        }
    }

    object Explore : Screen("explore_screen")
}

@Composable
fun MainNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Screen.Connection.route) {
        composable(Screen.Connection.route) {
            ConnectionScreen(navHome = {
                val route = Screen.Home.buildRoute(it.address)
                navController.navigate(route) {
                    popUpTo(Screen.Connection.route) { inclusive = true }
                }
            })
        }

        composable(Screen.Home.routeWithArgs, arguments = Screen.Home.args) {
            val deviceAddress = it.arguments?.getString(Screen.Home.deviceAddressArg)!!
            HomeScreen()
        }

        composable(Screen.Explore.route) {
            ExploreScreen()
        }
    }
}

