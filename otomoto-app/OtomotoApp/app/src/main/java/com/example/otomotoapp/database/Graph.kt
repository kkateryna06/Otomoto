package com.example.otomotoapp.database

import android.content.Context
import androidx.room.Room

object Graph {
    private lateinit var favouriteCarsDao: FavouriteCarsDao
    private lateinit var favouriteCarsDatabase: FavouriteCarsDatabase
    private lateinit var favouriteCarsRepository: FavouriteCarsRepository

    fun provide(context: Context) {
        favouriteCarsDatabase = Room.databaseBuilder(
            context = context,
            klass = FavouriteCarsDatabase::class.java,
            name = "favourite_cars.db",
        ).build()

        favouriteCarsDao = favouriteCarsDatabase.favouriteCarsDao()
        favouriteCarsRepository = FavouriteCarsRepository(favouriteCarsDao)
    }

    fun getFavouriteCarsRepository(): FavouriteCarsRepository {
        return favouriteCarsRepository
    }

}