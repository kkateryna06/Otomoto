package com.example.otomotoapp

import java.math.BigInteger

sealed class Screen(val route: String) {
    object MainScreen : Screen("main_screen")
    object CarDetailsScreen : Screen("car_details_screen") {
        fun withArgs(carId: String, isSpecialEnabled: Boolean): String {
            return "$route/$carId/$isSpecialEnabled"
        }
    }
    object FilterScreen : Screen("filter_screen") {
        fun withArgs(isSpecialEnabled: Boolean): String {
            return "$route/$isSpecialEnabled"
        }
    }
}