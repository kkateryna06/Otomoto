package com.example.otomotoapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.otomotoapp.data.CarSpecs
import com.example.otomotoapp.data.MinMaxResponse
import com.example.otomotoapp.data.UniqueValueResponse

class CarRepository {

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
        maxUrbanConsumption: Float? = null
    ): List<CarSpecs> {
        return try {
            if (isSpecialCarsEnabled) {
            RetrofitClient.instance.getSpecialCars(
                mark, model, minPrice, maxPrice, minYear, maxYear, bodyType,
                minMileage, maxMileage, fuelType, minEngineCapacity,
                maxEngineCapacity, minUrbanConsumption, maxUrbanConsumption
            )
        } else {
            RetrofitClient.instance.getAllCars(
                mark, model, minPrice, maxPrice, minYear, maxYear, bodyType,
                minMileage, maxMileage, fuelType, minEngineCapacity,
                maxEngineCapacity, minUrbanConsumption, maxUrbanConsumption
            )
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

    suspend fun getPhotoBitmap(carId: String, isSpecialCarsEnabled: Boolean): Bitmap? {
        return try {
            val response = if (isSpecialCarsEnabled) {
                RetrofitClient.instance.getPhotoByIdFromSpecial(carId)
            } else {
                RetrofitClient.instance.getPhotoByIdFromAll(carId)
            }

            response.body()?.byteStream()?.use { BitmapFactory.decodeStream(it) }

        } catch (e: Exception) {
            throw Exception("Failed to fetch car photo: ${e.message}")
        }
    }

    suspend fun getUniqueValues(value: String, isSpecialCarsEnabled: Boolean): UniqueValueResponse {
        return try {
            if (isSpecialCarsEnabled) {
                RetrofitClient.instance.getUniqueValuesFromSpecial(value)
            } else {
                RetrofitClient.instance.getUniqueValuesFromAll(value)
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch unique values: ${e.message}")
        }
    }

    suspend fun getMinMaxValues(value: String, isSpecialCarsEnabled: Boolean): MinMaxResponse {
        return try {
            if (isSpecialCarsEnabled) {
                RetrofitClient.instance.getMinMaxValuesFromSpecial(value)
            } else {
                RetrofitClient.instance.getMinMaxValuesFromAll(value)
            }
        } catch (e: Exception) {
            throw  Exception("Failed to fetch min and max values: ${e.message}")
        }
    }
}



