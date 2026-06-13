package com.example.otomotoapp

import android.util.Log
import android.content.Context
import com.example.otomotoapp.data.CarSpecs
import com.google.gson.Gson
import com.example.otomotoapp.data.PreferencesHelper
import java.io.InputStreamReader


class CarRepository(private val context: Context, prefs: PreferencesHelper) {
    val api = RetrofitClient.getInstance(prefs)

    suspend fun getCars(
        mark: List<String>? = null,
        model: String? = null,
//        minPrice: Float? = null,
//        maxPrice: Float? = null,
        minYear: Int? = null,
        maxYear: Int? = null,
        bodyType: List<String>? = null,
        minMileage: Float? = null,
        maxMileage: Float? = null,
        fuelType: List<String>? = null,
        minEngineCapacity: Float? = null,
        maxEngineCapacity: Float? = null,
        minUrbanConsumption: Float? = null,
        maxUrbanConsumption: Float? = null,
        page: Int,
        pageSize: Int
    ): List<CarSpecs> {
        return try {
            api.getAllCars(
                mark, model,
//                    minPrice, maxPrice,
                minYear, maxYear, bodyType,
                minMileage, maxMileage, fuelType, minEngineCapacity,
                maxEngineCapacity, minUrbanConsumption, maxUrbanConsumption
            )
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                Log.w("CarRepository", "Loading mock cars after API failure", e)
                loadMockCars()
            } else {
                throw e
            }
        }
    }

    private fun loadMockCars(): List<CarSpecs> {
        val inputStream = context.assets.open("mock_cars.json")
        val reader = InputStreamReader(inputStream)
        return Gson().fromJson(reader, Array<CarSpecs>::class.java).toList()
    }

    suspend fun getCarById(carId: String): CarSpecs {
        return try {
            api.getCarByIdFromAll(carId)
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                loadMockCars().firstOrNull { it.id == carId }
                    ?: throw IllegalStateException("Mock data does not contain car with id: $carId", e)
            } else {
                throw e
            }
        }
    }
}



