package com.example.otomotoapp

import java.math.BigInteger

class CarRepository {

    suspend fun getCars(isSpecialCarsEnabled: Boolean): List<CarSpecs> {
        return try {
            if (isSpecialCarsEnabled) {
            RetrofitClient.instance.getSpecialCars()
        } else {
            RetrofitClient.instance.getAllCars()
        }
        } catch (e: Exception) {
            throw Exception("Failed to fetch car specs: ${e.message}")
        }
    }

    suspend fun getCarById(carId: String, isSpecialCarsEnabled: Boolean): CarSpecs {
        return try {
            if (isSpecialCarsEnabled) {
                RetrofitClient.instance.getCarByIdFromSpecial(carId)
            } else {
                RetrofitClient.instance.getCarByIdFromAll(carId)
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch car specs: ${e.message}")
        }
    }
}



