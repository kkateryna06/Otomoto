package com.example.otomotoapp

import android.util.Log
import android.content.Context
import com.example.otomotoapp.data.CarSpecs
import com.google.gson.Gson
import com.example.otomotoapp.data.PreferencesHelper
import com.example.otomotoapp.data.FilterData
import com.google.gson.JsonElement
import java.io.InputStreamReader

data class CarsFetchResult(
    val cars: List<CarSpecs>,
    val isMockData: Boolean = false,
    val fallbackReason: String? = null
)

class CarRepository(private val context: Context, prefs: PreferencesHelper) {
    val api = RetrofitClient.getInstance(prefs)
    private val gson = Gson()
    private val filterEndpoints = mapOf(
        "brand" to listOf("brands"),
        "model" to listOf("models"),
        "fuelType" to listOf("fuelTypes", "fuel-types"),
        "bodyType" to listOf("bodyTypes", "body-types"),
        "gearbox" to listOf("gearboxes"),
        "transmission" to listOf("transmissions"),
        "sellerType" to listOf("sellerTypes", "seller-types")
    )

    suspend fun getCars(
        brand: List<String>? = null,
        model: List<String>? = null,
        fuelType: List<String>? = null,
        bodyType: List<String>? = null,
        gearbox: List<String>? = null,
        transmission: List<String>? = null,
        sellerType: List<String>? = null,
        q: String? = null,
        minYear: Int? = null,
        maxYear: Int? = null,
        minMileage: Int? = null,
        maxMileage: Int? = null,
        minPrice: Int? = null,
        maxPrice: Int? = null,
        minEngineCapacity: Int? = null,
        maxEngineCapacity: Int? = null,
        minEnginePower: Int? = null,
        maxEnginePower: Int? = null,
        actual: Boolean? = null,
        postedFrom: String? = null,
        postedTo: String? = null,
        page: Int,
        pageSize: Int
    ): CarsFetchResult {
        return try {
            val response = api.getAllCars(
                brand = emptyToNull(brand),
                model = emptyToNull(model),
                fuelType = emptyToNull(fuelType),
                bodyType = emptyToNull(bodyType),
                gearbox = emptyToNull(gearbox),
                transmission = emptyToNull(transmission),
                sellerType = emptyToNull(sellerType),
                q = q?.takeIf { it.isNotBlank() },
                minYear = minYear,
                maxYear = maxYear,
                minMileage = minMileage,
                maxMileage = maxMileage,
                minPrice = minPrice,
                maxPrice = maxPrice,
                minEngineCapacity = minEngineCapacity,
                maxEngineCapacity = maxEngineCapacity,
                minEnginePower = minEnginePower,
                maxEnginePower = maxEnginePower,
                actual = actual,
                postedFrom = postedFrom?.takeIf { it.isNotBlank() },
                postedTo = postedTo?.takeIf { it.isNotBlank() },
                page = page,
                size = pageSize
            )
            CarsFetchResult(
                cars = parseCarsResponse(response)
            )
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                Log.w("CarRepository", "Loading mock cars after API failure", e)
                CarsFetchResult(
                    cars = loadMockCars(),
                    isMockData = true,
                    fallbackReason = e.message ?: e::class.java.simpleName
                )
            } else {
                throw e
            }
        }
    }

    private fun loadMockCars(): List<CarSpecs> {
        val inputStream = context.assets.open("mock_cars.json")
        val reader = InputStreamReader(inputStream)
        return gson.fromJson(reader, Array<CarSpecs>::class.java).toList()
    }

    private fun parseCarsResponse(response: JsonElement): List<CarSpecs> {
        val carsJson = if (response.isJsonObject && response.asJsonObject.has("content")) {
            response.asJsonObject.get("content")
        } else {
            response
        }

        return gson.fromJson(carsJson, Array<CarSpecs>::class.java).toList()
    }

    private fun emptyToNull(values: List<String>?): List<String>? =
        values?.filter { it.isNotBlank() }?.takeIf { it.isNotEmpty() }

    suspend fun getFilterMetadata(): FilterData {
        return try {
            FilterData(
                brandList = getFilterValues("brand"),
                modelList = emptyList(),
                fuelTypeList = getFilterValues("fuelType"),
                bodyTypeList = getFilterValues("bodyType"),
                gearboxList = getFilterValues("gearbox"),
                transmissionList = getFilterValues("transmission"),
                sellerTypeList = getFilterValues("sellerType")
            )
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                Log.w("CarRepository", "Loading mock filter metadata after API failure", e)
                mockFilterMetadata()
            } else {
                throw e
            }
        }
    }

    suspend fun getModelFilterValues(brand: List<String>): List<String> {
        val normalizedBrand = emptyToNull(brand) ?: return emptyList()

        return try {
            getFilterValues("model", brand = normalizedBrand)
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                Log.w("CarRepository", "Loading mock model metadata after API failure", e)
                uniqueMockValues { car ->
                    if (normalizedBrand.contains(car.brand)) car.model else null
                }
            } else {
                throw e
            }
        }
    }

    private suspend fun getFilterValues(
        filterName: String,
        brand: List<String>? = null,
        model: List<String>? = null,
        fuelType: List<String>? = null,
        bodyType: List<String>? = null,
        gearbox: List<String>? = null,
        transmission: List<String>? = null
    ): List<String> {
        var lastException: Exception? = null

        filterEndpoints.getValue(filterName).forEach { endpoint ->
            try {
                return parseFilterValuesResponse(
                    api.getFilterValues(
                        filterName = endpoint,
                        brand = emptyToNull(brand),
                        model = emptyToNull(model),
                        fuelType = emptyToNull(fuelType),
                        bodyType = emptyToNull(bodyType),
                        gearbox = emptyToNull(gearbox),
                        transmission = emptyToNull(transmission)
                    )
                )
            } catch (e: Exception) {
                lastException = e
            }
        }

        throw lastException ?: IllegalStateException("No endpoint configured for filter: $filterName")
    }

    private fun parseFilterValuesResponse(response: JsonElement): List<String> {
        val valuesJson = if (response.isJsonObject) {
            val responseObject = response.asJsonObject
            when {
                responseObject.has("content") -> responseObject.get("content")
                responseObject.has("values") -> responseObject.get("values")
                responseObject.has("data") -> responseObject.get("data")
                else -> response
            }
        } else {
            response
        }

        return if (valuesJson.isJsonArray) {
            valuesJson.asJsonArray
                .mapNotNull { value ->
                    when {
                        value.isJsonPrimitive -> value.asString
                        value.isJsonObject && value.asJsonObject.has("value") -> {
                            value.asJsonObject.get("value").asString
                        }
                        else -> null
                    }
                }
                .filter { it.isNotBlank() }
                .distinct()
        } else {
            emptyList()
        }
    }

    private fun mockFilterMetadata(): FilterData =
        FilterData(
            brandList = uniqueMockValues { it.brand },
            modelList = emptyList(),
            fuelTypeList = uniqueMockValues { it.fuelType },
            bodyTypeList = uniqueMockValues { it.bodyType },
            gearboxList = uniqueMockValues { it.gearbox },
            transmissionList = uniqueMockValues { it.transmission },
            sellerTypeList = uniqueMockValues { it.sellerType }
        )

    private fun uniqueMockValues(selector: (CarSpecs) -> String?): List<String> =
        loadMockCars()
            .mapNotNull(selector)
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()

    suspend fun getCarById(carId: String): CarSpecs {
        return try {
            api.getCarByIdFromAll(carId)
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                loadMockCars().firstOrNull { it.id == carId }
                    ?: throw IllegalStateException("Mock data does not contain car with id: $carId", e)
            } else {
                throw e
            }
        }
    }
}
