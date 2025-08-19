package com.example.otomotoapp

sealed class Screen(val route: String, val title: String) {
    object MainScreen : Screen("main_screen", "Otomoto Cars")
    object CarDetailsScreen : Screen("car_details_screen", "Otomoto Cars") {
        fun withArgs(carId: String, isSpecialEnabled: Boolean): String {
            return "$route/$carId/$isSpecialEnabled"
        }
    }
    object FilterScreen : Screen("filter_screen", "Filters") {
        fun withArgs(isSpecialEnabled: Boolean): String {
            return "$route/$isSpecialEnabled"
        }
    }
    object FavouriteCarsScreen : Screen("favourite_cars_screen", "Favourites")
    object SettingsScreen : Screen("settings_screen", "Settings")
}