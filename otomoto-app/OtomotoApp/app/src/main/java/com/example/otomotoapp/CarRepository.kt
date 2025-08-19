package com.example.otomotoapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.example.otomotoapp.data.CarSpecs
import com.example.otomotoapp.data.MinMaxResponse
import com.example.otomotoapp.data.UniqueValueResponse
import com.google.gson.Gson
import android.content.Context  // Для доступа к активам
import androidx.annotation.RawRes  // Если вдруг будешь использовать res/raw
import com.example.otomotoapp.data.PreferencesHelper
import java.io.InputStreamReader


class CarRepository(private val context: Context, prefs: PreferencesHelper) {
    val api = RetrofitClient.getInstance(prefs)

    suspend fun getCars(
        isSpecialCarsEnabled: Boolean,
        mark: List<String>? = null,
        model: String? = null,
        minPrice: Float? = null,
        maxPrice: Float? = null,
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
            if (isSpecialCarsEnabled) {
            api.getSpecialCars(
                mark, model, minPrice, maxPrice, minYear, maxYear, bodyType,
                minMileage, maxMileage, fuelType, minEngineCapacity,
                maxEngineCapacity, minUrbanConsumption, maxUrbanConsumption, page, pageSize
            )
        } else {
                api.getAllCars(
                mark, model, minPrice, maxPrice, minYear, maxYear, bodyType,
                minMileage, maxMileage, fuelType, minEngineCapacity,
                maxEngineCapacity, minUrbanConsumption, maxUrbanConsumption, page, pageSize
            )
        }
        } catch (e: Exception) {
            Log.d("DEBUG", "ERROR: $e")
            loadMockCars()
        }
    }

    private fun loadMockCars(): List<CarSpecs> {
        val inputStream = context.assets.open("mock_cars.json")
        val reader = InputStreamReader(inputStream)
        Log.d("DEBUG", "$reader")
        return Gson().fromJson(reader, Array<CarSpecs>::class.java).toList()
    }

    suspend fun getCarById(carId: String, isSpecialCarsEnabled: Boolean): CarSpecs {
        return try {
            if (isSpecialCarsEnabled) {
                api.getCarByIdFromSpecial(carId)
            } else {
                api.getCarByIdFromAll(carId)
            }
        } catch (e: Exception) {
            loadMockCars().firstOrNull { it.car_id == carId }
                ?: throw Exception("Mock data does not contain car with id: $carId")

        }
    }

    suspend fun getPhotoBitmap(carId: String, isSpecialCarsEnabled: Boolean): Bitmap? {
        return try {
            val response = if (isSpecialCarsEnabled) {
                api.getPhotoByIdFromSpecial(carId)
            } else {
                api.getPhotoByIdFromAll(carId)
            }

            response.body()?.byteStream()?.use { BitmapFactory.decodeStream(it) }

        } catch (e: Exception) {
            BitmapFactory.decodeResource(context.resources, R.drawable.no_image)
        }
    }

    suspend fun getUniqueValues(value: String, isSpecialCarsEnabled: Boolean): UniqueValueResponse {
        return try {
            if (isSpecialCarsEnabled) {
                api.getUniqueValuesFromSpecial(value)
            } else {
                api.getUniqueValuesFromAll(value)
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch unique values: ${e.message}")
        }
    }

    suspend fun getMinMaxValues(value: String, isSpecialCarsEnabled: Boolean): MinMaxResponse {
        return try {
            if (isSpecialCarsEnabled) {
                api.getMinMaxValuesFromSpecial(value)
            } else {
                api.getMinMaxValuesFromAll(value)
            }
        } catch (e: Exception) {
            throw  Exception("Failed to fetch min and max values: ${e.message}")
        }
    }
}



