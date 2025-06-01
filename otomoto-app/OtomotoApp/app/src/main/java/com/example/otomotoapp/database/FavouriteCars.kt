package com.example.otomotoapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favourite_cars")
data class FavouriteCar(
    @PrimaryKey(autoGenerate = false)
    val id: Long
)