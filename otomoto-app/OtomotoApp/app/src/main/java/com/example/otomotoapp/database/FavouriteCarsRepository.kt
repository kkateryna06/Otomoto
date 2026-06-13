package com.example.otomotoapp.database

import kotlinx.coroutines.flow.Flow

class FavouriteCarsRepository(
    private val favouriteCarsDao: FavouriteCarsDao
) {
    fun getAllFavouriteCars(): Flow<List<FavouriteCar>> {
        return favouriteCarsDao.getAll()
    }

    suspend fun addFavouriteCar(id: String) {
        favouriteCarsDao.insert(FavouriteCar(id))
    }

    suspend fun deleteFavouriteCar(id: String) {
        favouriteCarsDao.delete(FavouriteCar(id))
    }
}
