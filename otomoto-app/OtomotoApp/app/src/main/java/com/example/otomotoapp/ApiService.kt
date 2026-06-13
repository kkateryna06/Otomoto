package com.example.otomotoapp

import com.example.otomotoapp.data.CarSpecs
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("/api/cars")
    suspend fun getAllCars(
        @Query("mark") marks: List<String>? = null,
        @Query("model") model: String? = null,
//        @Query("min_price") minPrice: Float? = null,
//        @Query("max_price") maxPrice: Float? = null,
        @Query("min_year") minYear: Int? = null,
        @Query("max_year") maxYear: Int? = null,
        @Query("body_type") bodyType: List<String>? = null,
        @Query("min_mileage") minMileage: Float? = null,
        @Query("max_mileage") maxMileage: Float? = null,
        @Query("fuel_type") fuelType: List<String>? = null,
        @Query("min_engine_capacity") minEngineCapacity: Float? = null,
        @Query("max_engine_capacity") maxEngineCapacity: Float? = null,
        @Query("min_urban_consumption") minUrbanConsumption: Float? = null,
        @Query("max_urban_consumption") maxUrbanConsumption: Float? = null,
    ): List<CarSpecs>

    @GET("/api/cars/{id}")
    suspend fun getCarByIdFromAll(@Path("id") id: String): CarSpecs
}
