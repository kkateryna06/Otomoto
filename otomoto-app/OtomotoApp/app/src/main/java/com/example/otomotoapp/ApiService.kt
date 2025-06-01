package com.example.otomotoapp

import com.example.otomotoapp.data.CarSpecs
import com.example.otomotoapp.data.MinMaxResponse
import com.example.otomotoapp.data.UniqueValueResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("/allcars/")
    suspend fun getAllCars(
        @Query("mark") marks: List<String>? = null,
        @Query("model") model: String? = null,
        @Query("min_price") minPrice: Float? = null,
        @Query("max_price") maxPrice: Float? = null,
        @Query("min_year") minYear: Int? = null,
        @Query("max_year") maxYear: Int? = null,
        @Query("body_type") bodyType: List<String>? = null,
        @Query("min_mileage") minMileage: Float? = null,
        @Query("max_mileage") maxMileage: Float? = null,
        @Query("fuel_type") fuelType: List<String>? = null,
        @Query("min_engine_capacity") minEngineCapacity: Float? = null,
        @Query("max_engine_capacity") maxEngineCapacity: Float? = null,
        @Query("min_urban_consumption") minUrbanConsumption: Float? = null,
        @Query("max_urban_consumption") maxUrbanConsumption: Float? = null
    ): List<CarSpecs>

    @GET("/specialcars/")
    suspend fun getSpecialCars(
        @Query("mark") mark: List<String>? = null,
        @Query("model") model: String? = null,
        @Query("min_price") minPrice: Float? = null,
        @Query("max_price") maxPrice: Float? = null,
        @Query("min_year") minYear: Int? = null,
        @Query("max_year") maxYear: Int? = null,
        @Query("body_type") bodyType: List<String>? = null,
        @Query("min_mileage") minMileage: Float? = null,
        @Query("max_mileage") maxMileage: Float? = null,
        @Query("fuel_type") fuelType: List<String>? = null,
        @Query("min_engine_capacity") minEngineCapacity: Float? = null,
        @Query("max_engine_capacity") maxEngineCapacity: Float? = null,
        @Query("min_urban_consumption") minUrbanConsumption: Float? = null,
        @Query("max_urban_consumption") maxUrbanConsumption: Float? = null
    ): List<CarSpecs>

    @GET("/allcars/{car_id}")
    suspend fun getCarByIdFromAll(@Path("car_id") car_id: String): CarSpecs

    @GET("/specialcars/{car_id}")
    suspend fun getCarByIdFromSpecial(@Path("car_id") car_id: String): CarSpecs

    @GET("/allcars/{car_id}/photo")
    suspend fun getPhotoByIdFromAll(@Path("car_id") car_id: String): Response<ResponseBody>

    @GET("/specialcars/{car_id}/photo")
    suspend fun getPhotoByIdFromSpecial(@Path("car_id") car_id: String): Response<ResponseBody>

    @GET("/allcars/search/{value}")
    suspend fun getUniqueValuesFromAll(@Path("value") value: String): UniqueValueResponse

    @GET("/specialcars/search/{value}")
    suspend fun getUniqueValuesFromSpecial(@Path("value") value: String): UniqueValueResponse

    @GET("/allcars/searchminmax/{value}")
    suspend fun getMinMaxValuesFromAll(@Path("value") value: String): MinMaxResponse

    @GET("/specialcars/searchminmax/{value}")
    suspend fun getMinMaxValuesFromSpecial(@Path("value") value: String): MinMaxResponse
}