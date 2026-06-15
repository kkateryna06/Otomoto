package com.example.otomotoapp

import com.example.otomotoapp.data.CarSpecs
import com.google.gson.JsonElement
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("/api/cars")
    suspend fun getAllCars(
        @Query("brand") brand: List<String>? = null,
        @Query("model") model: List<String>? = null,
        @Query("fuelType") fuelType: List<String>? = null,
        @Query("bodyType") bodyType: List<String>? = null,
        @Query("gearbox") gearbox: List<String>? = null,
        @Query("transmission") transmission: List<String>? = null,
        @Query("sellerType") sellerType: List<String>? = null,
        @Query("q") q: String? = null,
        @Query("minYear") minYear: Int? = null,
        @Query("maxYear") maxYear: Int? = null,
        @Query("minMileage") minMileage: Int? = null,
        @Query("maxMileage") maxMileage: Int? = null,
        @Query("minPrice") minPrice: Int? = null,
        @Query("maxPrice") maxPrice: Int? = null,
        @Query("minEngineCapacity") minEngineCapacity: Int? = null,
        @Query("maxEngineCapacity") maxEngineCapacity: Int? = null,
        @Query("minEnginePower") minEnginePower: Int? = null,
        @Query("maxEnginePower") maxEnginePower: Int? = null,
        @Query("actual") actual: Boolean? = null,
        @Query("postedFrom") postedFrom: String? = null,
        @Query("postedTo") postedTo: String? = null,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): JsonElement

    @GET("/api/cars/{id}")
    suspend fun getCarByIdFromAll(@Path("id") id: String): CarSpecs

    @GET("/api/cars/{filterName}")
    suspend fun getFilterValues(
        @Path("filterName") filterName: String,
        @Query("brand") brand: List<String>? = null,
        @Query("model") model: List<String>? = null,
        @Query("fuelType") fuelType: List<String>? = null,
        @Query("bodyType") bodyType: List<String>? = null,
        @Query("gearbox") gearbox: List<String>? = null,
        @Query("transmission") transmission: List<String>? = null
    ): JsonElement
}
