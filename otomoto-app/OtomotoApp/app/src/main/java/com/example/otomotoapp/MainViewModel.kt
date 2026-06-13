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
    private val repository = CarRepository(application, prefs)

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

    private val _minUrbanConsumption = MutableLiveData<Float?>()
    val minUrbanConsumption: LiveData<Float?> = _minUrbanConsumption

    private val _maxUrbanConsumption = MutableLiveData<Float?>()
    val maxUrbanConsumption: LiveData<Float?> = _maxUrbanConsumption


    private val _draftFilterData = MutableStateFlow<FilterData?>(null)
    val draftFilterData: StateFlow<FilterData?> = _draftFilterData

    private val _appliedFilterData = MutableStateFlow(FilterData())
    val appliedFilterData: StateFlow<FilterData> = _appliedFilterData

    private val _baseFilterData = MutableStateFlow<FilterData?>(null)
    val baseFilterData : StateFlow<FilterData?> = _baseFilterData
    private var isFilterMetadataLoading = false

    fun setBaseFilterData(data: FilterData) {
        if (_baseFilterData.value == null) {
            _baseFilterData.value = data
            _draftFilterData.value = data
        }
    }

    fun loadFilterMetadata() {
        if (_baseFilterData.value != null || isFilterMetadataLoading) return

        isFilterMetadataLoading = true
        viewModelScope.launch {
            try {
                setBaseFilterData(FilterData())
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Error loading filter metadata: ${e.message}"
            } finally {
                isFilterMetadataLoading = false
            }
        }
    }

    fun updateFilterData(update: FilterData.() -> FilterData) {
        val currentData = _draftFilterData.value
        val newData = currentData?.update()
        _draftFilterData.value = newData
    }

    fun resetUserFilters() {
        _draftFilterData.value = _baseFilterData.value
    }

    fun applyDraftFilters() {
        _appliedFilterData.value = _draftFilterData.value ?: FilterData()
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

    private var currentPage = 0
    private val pageSize = 20
    private var isLoading = false
    private var allLoaded = false

    fun fetchNextPage() {
        if (isLoading || allLoaded) return
        isLoading = true

        viewModelScope.launch {
            try {
                val filters = _appliedFilterData.value

                val newCars = repository.getCars(
                    mark = filters.markList,
//                    minPrice = filters.minPrice,
//                    maxPrice = filters.maxPrice,
                    minYear = filters.minYear.toInt(),
                    maxYear = filters.maxYear.toInt(),
                    bodyType = filters.bodyTypeList,
                    minMileage = filters.minMileage,
                    maxMileage = filters.maxMileage,
                    fuelType = filters.fuelTypeList,
                    minEngineCapacity = filters.minEngineCapacity,
                    maxEngineCapacity = filters.maxEngineCapacity,
                    minUrbanConsumption = filters.minUrbanConsumption,
                    maxUrbanConsumption = filters.maxUrbanConsumption,
                    page = currentPage,
                    pageSize = pageSize
                )
                Log.d("DEBUG", "page size: $pageSize")

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
            } finally {
                isLoading = false
            }
        }
    }

    fun resetPaginationAndFetch() {
        currentPage = 0
        allLoaded = false
        _carList.postValue(emptyList())
        fetchNextPage()
    }

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

    fun getPhotoUrl(carId: String): String {
        return "${prefs.getServerUrl().trimEnd('/')}/api/cars/${Uri.encode(carId)}/photo"
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
