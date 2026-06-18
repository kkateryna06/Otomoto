package com.example.otomotoapp.data

import android.content.Context

class PreferencesHelper(context: Context) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    private companion object {
        const val DEFAULT_SERVER_URL = "http://10.0.2.2:8080/"
        const val LEGACY_SERVER_URL = "http://10.0.2.2:8000/"
    }

    fun saveServerUrl(url: String) {
        prefs.edit().putString("server_url", normalizeUrl(url)).apply()
    }

    fun getServerUrl(): String {
        val savedUrl = prefs.getString("server_url", DEFAULT_SERVER_URL) ?: DEFAULT_SERVER_URL
        val normalizedUrl = normalizeUrl(savedUrl)

        if (normalizedUrl == LEGACY_SERVER_URL) {
            prefs.edit().putString("server_url", DEFAULT_SERVER_URL).apply()
            return DEFAULT_SERVER_URL
        }

        if (normalizedUrl != savedUrl) {
            prefs.edit().putString("server_url", normalizedUrl).apply()
        }

        return normalizedUrl
    }

    private fun normalizeUrl(url: String): String {
        val trimmedUrl = url.trim()
        return if (trimmedUrl.endsWith("/")) trimmedUrl else "$trimmedUrl/"
    }
}
