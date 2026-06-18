package com.example.otomotoapp.data

import com.google.gson.annotations.SerializedName

data class CarSpecs(
    @SerializedName(value = "id", alternate = ["car_id"])
    val id: String? = null,
    @SerializedName(value = "postedAt", alternate = ["date"])
    val postedAt: String = "",
    @SerializedName(value = "disappearedAt", alternate = ["sell_date"])
    val disappearedAt: String? = null,
    @SerializedName(value = "brand", alternate = ["mark"])
    val brand: String = "",
    val model: String = "",
    val version: String?,
    val year: Int = 0,
    val mileage: Int = 0,
    @SerializedName(value = "fuelType", alternate = ["fuel_type"])
    val fuelType: String = "",
    @SerializedName(value = "engineCapacity", alternate = ["engine_capacity"])
    val engineCapacity: Int = 0,
    @SerializedName(value = "enginePower", alternate = ["engine_power"])
    val enginePower: Int = 0,
    val priceHistory: Map<String, Int>? = null,
    @SerializedName("price")
    val legacyPrice: Double? = null,
    @SerializedName(value = "bodyType", alternate = ["body_type"])
    val bodyType: String = "",
    val gearbox: String = "",
    val transmission: String?,
    @SerializedName(value = "urbanConsumption", alternate = ["urban_consumption"])
    val urbanConsumption: String?,
    @SerializedName(value = "extraUrbanConsumption", alternate = ["extra_urban_consumption"])
    val extraUrbanConsumption: String?,
    val color: String?,
    @SerializedName(value = "doorCount", alternate = ["door_count"])
    val doorCount: Int = 0,
    @SerializedName(value = "seats", alternate = ["nr_seats"])
    val seats: Int?,
    val generation: String = "",
    @SerializedName(value = "hasRegistration", alternate = ["has_registration"])
    val hasRegistration: Boolean = false,
    @SerializedName(value = "sellerType", alternate = ["seller_type"])
    val sellerType: String = "",
    val description: String = "",
    @SerializedName(value = "url", alternate = ["link"])
    val url: String = "",
    val location: Location? = null,
    @SerializedName(value = "photoPath", alternate = ["photo_path"])
    val photoPath: String? = null,
    @SerializedName(value = "photoUrls", alternate = ["photo_urls"])
    val photoUrls: List<String>? = emptyList(),
    @SerializedName(value = "htmlPath", alternate = ["html_path"])
    val htmlPath: String? = null,

) {
    val price: Int
        get() = priceHistory
            ?.maxByOrNull { it.key }
            ?.value
            ?: legacyPrice?.toInt()
            ?: 0
}

data class Location(
    val zoom: Int,
    val radius: Int,
    val latitude: Double,
    val longitude: Double
)
