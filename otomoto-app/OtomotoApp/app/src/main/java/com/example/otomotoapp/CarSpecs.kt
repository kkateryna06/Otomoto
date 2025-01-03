package com.example.otomotoapp

import org.json.JSONObject
import java.math.BigInteger

data class CarSpecs(
    val car_id: BigInteger,
    val date: String,
    val mark: String,
    val model: String,
    val version: String?,
    val year: Int,
    val mileage: Int,
    val fuelType: String,
    val engineCapacity: Int,
    val enginePower: Int,
    val price: Int,
    val body_type: String,
    val gearbox: String,
    val transmission: String?,
    val urbanConsumption: String?,
    val extraUrbanConsumption: String?,
    val color: String?,
    val doorCount: Int,
    val seatsCount: Int?,
    val generation: String,
    val hasRegistration: Boolean,
    val sellerType: String,
//    val equipment: JSONObject,
    val parametersDict: JSONObject,
    val description: String,
    val link: String,
    val location: JSONObject,
    val photoPath: String,
    val htmlPath: String,
    )
