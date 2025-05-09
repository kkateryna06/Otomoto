package com.example.otomotoapp

data class CarSpecs(
    val car_id: String,
    val date: String,
    val mark: String,
    val model: String,
    val version: String?,
    val year: Int,
    val mileage: Int,
    val fuel_type: String,
    val engine_capacity: Int,
    val engine_power: Int,
    val price: Int,
    val body_type: String,
    val gearbox: String,
    val transmission: String?,
    val urban_consumption: String?,
    val extra_urban_consumption: String?,
    val color: String?,
    val door_count: Int,
    val seats_count: Int?,
    val generation: String,
    val has_registration: Boolean,
    val seller_type: String,
//    val equipment: JSONObject,
//    val parametersDict: JSONObject,
    val description: String,
    val link: String,
//    val location: JSONObject,
    val photo_path: String,
    val html_path: String,
    )
