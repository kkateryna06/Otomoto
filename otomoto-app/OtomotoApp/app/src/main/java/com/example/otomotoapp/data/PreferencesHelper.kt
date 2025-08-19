package com.example.otomotoapp.data

import android.content.Context

class PreferencesHelper(context: Context) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    fun saveServerUrl(url: String) {
        prefs.edit().putString("server_url", url).apply()
    }

    fun getServerUrl(): String {
        return prefs.getString("server_url", "http://localhost/")!!
    }
}