package com.example.otomotoapp.database

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class FavouriteCarsViewModel: ViewModel() {
    // Database for favourite cars
    private val favouriteCarsRepository = Graph.getFavouriteCarsRepository()

    private val _favouriteCars: MutableStateFlow<List<FavouriteCar>> = MutableStateFlow(emptyList())
    val favouriteCars: StateFlow<List<FavouriteCar>> = _favouriteCars

    init {
        getAllFavouriteCars()
    }

    private fun getAllFavouriteCars() {
        favouriteCarsRepository.getAllFavouriteCars().onEach { favouriteCar ->
            _favouriteCars.value = favouriteCar
        }.launchIn(viewModelScope)
    }

    fun addFavCar(id: Long) {
        viewModelScope.launch {
            favouriteCarsRepository.addFavouriteCar(id)
        }
    }

    fun deleteFavCar(id: Long) {
        viewModelScope.launch {
            favouriteCarsRepository.deleteFavouriteCar(id)
        }
    }

    fun isFavCar(id: Long): Boolean {
        return favouriteCars.value.contains(FavouriteCar(id))
    }
}