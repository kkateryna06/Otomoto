package com.example.otomotoapp

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.otomotoapp.data.PreferencesHelper
import com.example.otomotoapp.database.FavouriteCarsViewModel
import com.example.otomotoapp.screens.CarDetailsScreen
import com.example.otomotoapp.screens.FavouriteCarsScreen
import com.example.otomotoapp.screens.FilterScreen
import com.example.otomotoapp.screens.OtomotoMainScreen
import com.example.otomotoapp.screens.SettingsScreen

@Composable
fun Navigation(navController: NavHostController,
               appBarsViewModel: AppBarsViewModel,
               viewModel: MainViewModel,
               prefs: PreferencesHelper
) {
    val favCarsViewModel: FavouriteCarsViewModel = viewModel()

    NavHost(navController = navController, startDestination = Screen.MainScreen.route) {

        composable(route = Screen.MainScreen.route) {
            OtomotoMainScreen(navController = navController,
                viewModel = viewModel, favCarsViewModel = favCarsViewModel)
        }

        composable(
            route = Screen.CarDetailsScreen.route + "/{car_id}",
            arguments = listOf(
                navArgument("car_id") {
                    type = NavType.StringType
                    nullable = false
            })
        ) { entry ->
            val carId = entry.arguments?.getString("car_id")
            if (carId != null) {
                CarDetailsScreen(carId = carId.toString(), viewModel = viewModel,
                    favCarsViewModel = favCarsViewModel,
                    navController = navController, appBarsViewModel = appBarsViewModel)
            } else {}
        }

        composable(route = Screen.FilterScreen.route) {
            FilterScreen(viewModel = viewModel, navController = navController)
        }

        composable(
            route = Screen.FavouriteCarsScreen.route
        ) {
            FavouriteCarsScreen(
                viewModel = viewModel,
                favCarsViewModel = favCarsViewModel,
                navController = navController
            )
        }

        composable(
            route = Screen.SettingsScreen.route
        ) {
            SettingsScreen(prefs)
        }
    }
}
