package com.example.otomotoapp

import java.math.BigInteger

sealed class Screen(val route: String) {
    object MainScreen : Screen("main_screen")
    object CarDetailsScreen : Screen("car_details_screen")

    fun withArgs(carId: String): String {
        return "$route/$carId"
    }
}