package com.example.otomotoapp

import retrofit2.Call
import retrofit2.http.GET

interface ApiService {
    @GET("/cars")
    fun getCarSpecs(): Call<List<CarSpecs>>

}