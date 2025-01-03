package com.example.otomotoapp

import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CarRepository {

    interface CarSpecsCallback {
        fun onSuccess(cars: List<CarSpecs>)
        fun onError(error: String)
    }

    fun getCarSpecs(callback: CarSpecsCallback) {
        RetrofitClient.instance.getCarSpecs().enqueue(object : Callback<List<CarSpecs>> {
            override fun onResponse(call: Call<List<CarSpecs>>, response: Response<List<CarSpecs>>) {
                if (response.isSuccessful) {
                    val cars = response.body()
                    if (cars != null) {
                        callback.onSuccess(cars)
                    } else {
                        callback.onError("Empty response body")
                    }
                } else {
                    callback.onError("Error: ${response.code()} - ${response.message()}")
                }
            }

            override fun onFailure(call: Call<List<CarSpecs>>, t: Throwable) {
                callback.onError(t.message ?: "Unknown error")
            }
        })
    }
}



