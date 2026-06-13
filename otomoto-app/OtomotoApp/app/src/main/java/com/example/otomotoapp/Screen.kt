package com.example.otomotoapp

sealed class Screen(val route: String, val title: String) {
    object MainScreen : Screen("main_screen", "Otomoto Cars")
    object CarDetailsScreen : Screen("car_details_screen", "Otomoto Cars") {
        fun withArgs(carId: String): String {
            return "$route/$carId"
        }
    }
    object FilterScreen : Screen("filter_screen", "Filters")
    object FavouriteCarsScreen : Screen("favourite_cars_screen", "Favourites")
    object SettingsScreen : Screen("settings_screen", "Settings")
}
