package com.example.otomotoapp

import android.content.Context
import com.example.otomotoapp.data.PreferencesHelper
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    fun getInstance(prefs: PreferencesHelper): ApiService {
        val baseUrl = prefs.getServerUrl()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(ApiService::class.java)
    }
}