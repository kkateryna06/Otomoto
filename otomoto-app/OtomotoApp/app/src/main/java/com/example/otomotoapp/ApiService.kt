package com.example.otomotoapp

import retrofit2.http.GET
import retrofit2.http.Path
import java.math.BigInteger

interface ApiService {
    @GET("/allcars")
    suspend fun getAllCars(): List<CarSpecs>

    @GET("/specialcars")
    suspend fun getSpecialCars(): List<CarSpecs>

    @GET("/allcars/{car_id}")
    suspend fun getCarByIdFromAll(@Path("car_id") car_id: String): CarSpecs

    @GET("/specialcars/{car_id}")
    suspend fun getCarByIdFromSpecial(@Path("car_id") car_id: String): CarSpecs

}