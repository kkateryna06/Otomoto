package com.example.otomotoapp.data

data class FilterData(
    val brandList: List<String> = emptyList(),
    val modelList: List<String> = emptyList(),
    val fuelTypeList: List<String> = emptyList(),
    val bodyTypeList: List<String> = emptyList(),
    val gearboxList: List<String> = emptyList(),
    val transmissionList: List<String> = emptyList(),
    val sellerTypeList: List<String> = emptyList(),
    val q: String = "",
    val minPrice: Float = 0f,
    val maxPrice: Float = 0f,
    val minYear: Float = 0f,
    val maxYear: Float = 0f,
    val minMileage: Float = 0f,
    val maxMileage: Float = 0f,
    val minEngineCapacity: Float = 0f,
    val maxEngineCapacity: Float = 0f,
    val minEnginePower: Float = 0f,
    val maxEnginePower: Float = 0f,
    val actual: Boolean? = null,
    val postedFrom: String? = null,
    val postedTo: String? = null
)
