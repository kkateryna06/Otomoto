package com.example.otomotoapp.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [FavouriteCar::class],
    version = 1,
    exportSchema = false
)

abstract class FavouriteCarsDatabase: RoomDatabase() {
    abstract fun favouriteCarsDao(): FavouriteCarsDao
}