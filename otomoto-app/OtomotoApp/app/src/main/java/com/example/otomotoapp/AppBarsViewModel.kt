package com.example.otomotoapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class AppBarsViewModel: ViewModel() {
    var carLink by mutableStateOf<String?>(null)
    var carPrice by mutableStateOf<Int?>(null)

    fun updateBottomInfo(link: String, price: Int) {
        carLink = link
        carPrice = price
    }
}