package com.example.otomotoapp.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavouriteCarsDao {
    // add new car id
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(favCar: FavouriteCar)

    // get all favourite cars
    @Query("select * from favourite_cars")
    fun getAll(): Flow<List<FavouriteCar>>

    // delete a car id
    @Delete
    suspend fun delete(favCar: FavouriteCar)
}