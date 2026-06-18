package com.example.otomotoapp

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.otomotoapp.data.CarSpecs
import com.example.otomotoapp.data.FilterData
import com.example.otomotoapp.data.PreferencesHelper
import com.example.otomotoapp.database.FavouriteCar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

open class MainViewModel(application: Application, private val prefs: PreferencesHelper) : AndroidViewModel(application) {
    private var repository = CarRepository(application, prefs)

    private val _filterOptions = MutableLiveData(FilterData())
    val filterOptions: LiveData<FilterData> = _filterOptions

    // LiveData for car list
    private val _carList = MutableLiveData<List<CarSpecs>>()
    open val carList: LiveData<List<CarSpecs>> = _carList

    private val _carSpecs = MutableLiveData<CarSpecs?>()
    val carSpecs: LiveData<CarSpecs?> = _carSpecs
    private var loadingCarId: String? = null

    // LiveData for error messages
    private val _errorMessage = MutableLiveData<String?>()
    open val errorMessage: LiveData<String?> = _errorMessage

    private val _mockDataMessage = MutableLiveData<String?>()
    val mockDataMessage: LiveData<String?> = _mockDataMessage

    // LiveData for filters
    private val _selectedMark = MutableLiveData<String?>()
    val selectedMark: LiveData<String?> = _selectedMark

    private val _selectedModel = MutableLiveData<String?>()
    val selectedModel: LiveData<String?> = _selectedModel

    private val _minPrice = MutableLiveData<Float?>()
    val minPrice: LiveData<Float?> = _minPrice

    private val _maxPrice = MutableLiveData<Float?>()
    val maxPrice: LiveData<Float?> = _maxPrice

    private val _minYear = MutableLiveData<Int?>()
    val minYear: LiveData<Int?> = _minYear

    private val _maxYear = MutableLiveData<Int?>()
    val maxYear: LiveData<Int?> = _maxYear

    private val _selectedBodyType = MutableLiveData<String?>()
    val selectedBodyType: LiveData<String?> = _selectedBodyType

    private val _minMileage = MutableLiveData<Float?>()
    val minMileage: LiveData<Float?> = _minMileage

    private val _maxMileage = MutableLiveData<Float?>()
    val maxMileage: LiveData<Float?> = _maxMileage

    private val _selectedFuelType = MutableLiveData<String?>()
    val selectedFuelType: LiveData<String?> = _selectedFuelType

    private val _minEngineCapacity = MutableLiveData<Float?>()
    val minEngineCapacity: LiveData<Float?> = _minEngineCapacity

    private val _maxEngineCapacity = MutableLiveData<Float?>()
    val maxEngineCapacity: LiveData<Float?> = _maxEngineCapacity

    private val _draftFilterData = MutableStateFlow<FilterData?>(null)
    val draftFilterData: StateFlow<FilterData?> = _draftFilterData

    private val _appliedFilterData = MutableStateFlow(FilterData())
    val appliedFilterData: StateFlow<FilterData> = _appliedFilterData

    private val _serverUrl = MutableStateFlow(prefs.getServerUrl())
    val serverUrl: StateFlow<String> = _serverUrl

    private val _baseFilterData = MutableStateFlow<FilterData?>(null)
    val baseFilterData : StateFlow<FilterData?> = _baseFilterData
    private var isFilterMetadataLoading = false

    fun updateServerUrl(url: String) {
        prefs.saveServerUrl(url)
        val normalizedUrl = prefs.getServerUrl()
        if (_serverUrl.value == normalizedUrl) return

        _serverUrl.value = normalizedUrl
        repository = CarRepository(getApplication(), prefs)
        _baseFilterData.value = null
        _draftFilterData.value = _appliedFilterData.value
        loadedFilterData = null
        resetPaginationAndFetch()
    }

    fun setBaseFilterData(data: FilterData) {
        if (_baseFilterData.value == null) {
            _baseFilterData.value = data
            _draftFilterData.value = _appliedFilterData.value
        }
    }

    fun loadFilterMetadata() {
        if (_baseFilterData.value != null || isFilterMetadataLoading) return

        isFilterMetadataLoading = true
        viewModelScope.launch {
            try {
                setBaseFilterData(repository.getFilterMetadata())
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Error loading filter metadata: ${e.message}"
            } finally {
                isFilterMetadataLoading = false
            }
        }
    }

    fun updateFilterData(update: FilterData.() -> FilterData) {
        val currentData = _draftFilterData.value ?: FilterData()
        val newData = currentData.update().withoutModelsIfBrandIsMissing()
        _draftFilterData.value = newData
    }

    fun resetUserFilters() {
        _draftFilterData.value = FilterData()
        clearModelOptions()
    }

    fun applyDraftFilters() {
        _appliedFilterData.value = (_draftFilterData.value ?: FilterData()).withoutModelsIfBrandIsMissing()
    }

    fun applySearchQuery(query: String) {
        val normalizedQuery = query.trim()
        val updatedFilters = _appliedFilterData.value.copy(q = normalizedQuery)
        _appliedFilterData.value = updatedFilters
        _draftFilterData.value = updatedFilters
    }

    fun addToFilterList(selector: FilterData.() -> List<String>, item: String, updater: FilterData.(List<String>) -> FilterData) {
        updateFilterData {
            val updatedList = selector() + item
            updater(updatedList)
        }
    }

    fun removeFromFilterList(selector: FilterData.() -> List<String>, item: String, updater: FilterData.(List<String>) -> FilterData) {
        updateFilterData {
            val updatedList = selector() - item
            updater(updatedList)
        }
    }

    fun addBrandFilter(item: String) {
        val newBrandList = ((_draftFilterData.value ?: FilterData()).brandList + item).distinct()
        updateFilterData {
            copy(brandList = newBrandList)
        }
        loadModelOptionsForBrands(newBrandList)
    }

    fun removeBrandFilter(item: String) {
        val newBrandList = (_draftFilterData.value ?: FilterData()).brandList - item
        updateFilterData {
            copy(
                brandList = newBrandList,
                modelList = if (newBrandList.isEmpty()) emptyList() else modelList
            )
        }
        loadModelOptionsForBrands(newBrandList)
    }

    private fun loadModelOptionsForBrands(brandList: List<String>) {
        val normalizedBrandList = brandList.filter { it.isNotBlank() }

        if (normalizedBrandList.isEmpty()) {
            clearModelOptions()
            return
        }

        viewModelScope.launch {
            try {
                val modelOptions = repository.getModelFilterValues(normalizedBrandList)
                _baseFilterData.value = (_baseFilterData.value ?: FilterData()).copy(
                    modelList = modelOptions
                )
                _draftFilterData.value = (_draftFilterData.value ?: FilterData()).let { draft ->
                    draft.copy(modelList = draft.modelList.filter { modelOptions.contains(it) })
                }
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Error loading model metadata: ${e.message}"
            }
        }
    }

    private fun clearModelOptions() {
        _baseFilterData.value = (_baseFilterData.value ?: FilterData()).copy(modelList = emptyList())
        _draftFilterData.value = (_draftFilterData.value ?: FilterData()).copy(modelList = emptyList())
    }

    private fun FilterData.withoutModelsIfBrandIsMissing(): FilterData =
        if (brandList.isEmpty()) copy(modelList = emptyList()) else this

    private var currentPage = 0
    private val pageSize = 20
    private var isLoading = false
    private var allLoaded = false
    private var loadedFilterData: FilterData? = null

    fun fetchNextPage() {
        if (isLoading || allLoaded) return
        isLoading = true

        viewModelScope.launch {
            try {
                val filters = _appliedFilterData.value

                val result = repository.getCars(
                    brand = filters.brandList,
                    model = filters.modelList,
                    fuelType = filters.fuelTypeList,
                    bodyType = filters.bodyTypeList,
                    gearbox = filters.gearboxList,
                    transmission = filters.transmissionList,
                    sellerType = filters.sellerTypeList,
                    q = filters.q,
                    minYear = positiveIntOrNull(filters.minYear),
                    maxYear = positiveIntOrNull(filters.maxYear),
                    minMileage = positiveIntOrNull(filters.minMileage),
                    maxMileage = positiveIntOrNull(filters.maxMileage),
                    minPrice = positiveIntOrNull(filters.minPrice),
                    maxPrice = positiveIntOrNull(filters.maxPrice),
                    minEngineCapacity = positiveIntOrNull(filters.minEngineCapacity),
                    maxEngineCapacity = positiveIntOrNull(filters.maxEngineCapacity),
                    minEnginePower = positiveIntOrNull(filters.minEnginePower),
                    maxEnginePower = positiveIntOrNull(filters.maxEnginePower),
                    actual = filters.actual,
                    postedFrom = filters.postedFrom,
                    postedTo = filters.postedTo,
                    page = currentPage,
                    pageSize = pageSize
                )
                val newCars = result.cars
                Log.d("DEBUG", "page size: $pageSize, page number: $currentPage")

                _mockDataMessage.postValue(
                    if (result.isMockData) {
                        "API is unavailable or returned an invalid response. Showing mock data. Reason: ${result.fallbackReason}"
                    } else {
                        null
                    }
                )

                if (newCars.isEmpty()) {
                    allLoaded = true
                } else {
                    val updatedList = _carList.value.orEmpty() + newCars
                    _carList.postValue(updatedList)
                    currentPage++
                }

                _errorMessage.postValue(null)
            } catch (e: Exception) {
                _errorMessage.postValue("Error fetching cars: ${e.message}")
                _mockDataMessage.postValue(null)
            } finally {
                isLoading = false
            }
        }
    }

    fun refreshCarsForCurrentFiltersIfNeeded() {
        val filters = _appliedFilterData.value
        if (loadedFilterData == filters) return

        loadedFilterData = filters
        resetPaginationAndFetch()
    }

    private fun resetPaginationAndFetch() {
        currentPage = 0
        allLoaded = false
        _mockDataMessage.postValue(null)
        _carList.postValue(emptyList())
        fetchNextPage()
    }

    private fun positiveIntOrNull(value: Float): Int? =
        value.toInt().takeIf { it > 0 }

    fun getCarById(carId: String) {
        loadingCarId = carId
        _carSpecs.value = null
        viewModelScope.launch {
            try {
                val car = repository.getCarById(carId)
                if (loadingCarId == carId) {
                    _carSpecs.value = car
                    _errorMessage.value = null
                }
            } catch (e: Exception) {
                if (loadingCarId == carId) {
                    _errorMessage.value = "Error fetching car specs: ${e.message}"
                }
            }
        }
    }

    fun getPhotoUrl(photoPath: String?): String? {
        val rawPath = photoPath?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        if (rawPath.startsWith("http://") || rawPath.startsWith("https://")) {
            Log.d("CarPhoto", "photoPath=$rawPath, photoUrl=$rawPath")
            return rawPath
        }

        val relativePath = rawPath
            .replace('\\', '/')
            .trimStart('/')
            .let { path ->
                if (path.startsWith("photos/")) path else "photos/$path"
            }
            .split('/')
            .joinToString("/") { Uri.encode(it, "%") }

        val photoUrl = "${prefs.getServerUrl().trimEnd('/')}/$relativePath"
        Log.d("CarPhoto", "photoPath=$rawPath, relativePath=$relativePath, photoUrl=$photoUrl")
        return photoUrl
    }


    private val _favouriteCarsSpecsList = MutableLiveData<List<CarSpecs>>()
    val favouriteCarsSpecsList: LiveData<List<CarSpecs>> = _favouriteCarsSpecsList

    fun fetchFavouriteCarsSpecs(
        favouriteCarsIdList: List<FavouriteCar>
    ) {
        viewModelScope.launch {
            val result = favouriteCarsIdList.mapNotNull { favCar ->
                try {
                    repository.getCarById(favCar.id.toString())
                } catch (e: Exception) {
                    Log.e("FETCH_ERROR", "Error loading car ${favCar.id}: ${e.message}")
                    null
                }
            }
            _favouriteCarsSpecsList.value = result
        }
    }

}
