package com.example.otomotoapp.database

import kotlinx.coroutines.flow.Flow

class FavouriteCarsRepository(
    private val favouriteCarsDao: FavouriteCarsDao
) {
    fun getAllFavouriteCars(): Flow<List<FavouriteCar>> {
        return favouriteCarsDao.getAll()
    }

    suspend fun addFavouriteCar(id: Long) {
        favouriteCarsDao.insert(FavouriteCar(id))
    }

    suspend fun deleteFavouriteCar(id: Long) {
        favouriteCarsDao.delete(FavouriteCar(id))
    }
}