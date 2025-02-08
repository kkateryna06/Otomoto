package com.example.otomotoapp

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun Navigation() {
    val navController = rememberNavController()
    val viewModel: MainViewModel = viewModel()

    NavHost(navController = navController, startDestination = Screen.MainScreen.route) {
        composable(route = Screen.MainScreen.route) {
            OtomotoMainScreen(navController = navController, viewModel = viewModel)
        }
        composable(
            route = Screen.CarDetailsScreen.route + "/{car_id}",
            arguments = listOf(
                navArgument("car_id") {
                    type = NavType.StringType
            } )
        ) { entry ->
            val carId = entry.arguments?.getString("car_id")
            if (carId != null) {
                CarDetailsScreen(carId = carId.toString(), viewModel = viewModel)
            } else {}
        }
    }
}
