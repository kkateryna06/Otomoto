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
            route = Screen.CarDetailsScreen.route + "/{car_id}/{isSpecialEnabled}",
            arguments = listOf(
                navArgument("car_id") {
                    type = NavType.StringType
                    nullable = false
            },
                navArgument("isSpecialEnabled") {
                    type = NavType.BoolType
                    nullable = false
                })
        ) { entry ->
            val carId = entry.arguments?.getString("car_id")
            val isSpecialCarEnabled = entry.arguments?.getBoolean("isSpecialEnabled") ?: false
            if (carId != null) {
                CarDetailsScreen(carId = carId.toString(), viewModel = viewModel, isSpecialCarEnabled = isSpecialCarEnabled, navController = navController)
            } else {}
        }
        composable(
            route = Screen.FilterScreen.route + "/{isSpecialEnabled}",
            arguments = listOf(
                navArgument("isSpecialEnabled") {
                    type = NavType.BoolType
                    nullable = false
                }
            )
        ) { entry ->
            val isSpecialEnables = entry.arguments?.getBoolean("isSpecialEnabled") ?: false
            FilterScreen(isSpecialEnabled = isSpecialEnables, viewModel = viewModel, navController = navController)
        }
    }
}
