package com.example.otomotoapp.data

data class CarSpecs(
    val id: String = "",
    val postedAt: String = "",
    val disappearedAt: String? = null,
    val brand: String = "",
    val model: String = "",
    val version: String?,
    val year: Int = 0,
    val mileage: Int = 0,
    val fuelType: String = "",
    val engineCapacity: Int = 0,
    val enginePower: Int = 0,
    val priceHistory: Map<String, Int> = emptyMap(),
    val bodyType: String = "",
    val gearbox: String = "",
    val transmission: String?,
    val urbanConsumption: String?,
    val extraUrbanConsumption: String?,
    val color: String?,
    val doorCount: Int = 0,
    val seats: Int?,
    val generation: String = "",
    val hasRegistration: Boolean = false,
    val sellerType: String = "",
    val description: String = "",
    val url: String = "",
    val location: Location? = null,
    val photoPath: String? = null,
    val htmlPath: String? = null,
) {
    val price: Int
        get() = priceHistory.maxByOrNull { it.key }?.value ?: 0
}

data class Location(
    val zoom: Int,
    val radius: Int,
    val latitude: Double,
    val longitude: Double
)
