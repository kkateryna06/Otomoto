package com.example.otomotoapp

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.otomotoapp.data.PreferencesHelper

class MainViewModelFactory(
    private val application: Application,
    private val prefs: PreferencesHelper
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(application, prefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}