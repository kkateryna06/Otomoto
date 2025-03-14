package com.example.otomotoapp

data class FilterData(
    val markList: List<String> = emptyList(),
    val modelList: List<String> = emptyList(),
    val minPrice: Float = 0f,
    val maxPrice: Float = 0f,
    val minYear: Float = 0f,
    val maxYear: Float = 0f,
    val bodyTypeList: List<String> = emptyList(),
    val minMileage: Float = 0f,
    val maxMileage: Float = 0f,
    val fuelTypeList: List<String> = emptyList(),
    val minEngineCapacity: Float = 0f,
    val maxEngineCapacity: Float = 0f,
    val minEnginePower: Float = 0f,
    val maxEnginePower: Float = 0f,
    val minUrbanConsumption: Float = 0f,
    val maxUrbanConsumption: Float = 0f,
    val minExtraUrbanConsumption: Float = 0f,
    val maxExtraUrbanConsumption: Float = 0f
)
