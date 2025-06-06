package com.example.otomotoapp

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.otomotoapp.data.CarSpecs
import com.example.otomotoapp.data.FilterData
import com.example.otomotoapp.data.MinMaxResponse
import com.example.otomotoapp.data.UniqueValueResponse
import com.example.otomotoapp.database.FavouriteCar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

open class MainViewModel: ViewModel() {
    // Screens
    private val _currentScreen = MutableLiveData<Screen>(Screen.MainScreen)
    val currentScreen: LiveData<Screen> = _currentScreen

    fun setCurrentScreen(screen: Screen) {
        _currentScreen.value = screen
    }


    private val repository = CarRepository()

    private val _filterOptions = MutableLiveData(FilterData())
    val filterOptions: LiveData<FilterData> = _filterOptions

    // LiveData for car list
    private val _carList = MutableLiveData<List<CarSpecs>>()
    open val carList: LiveData<List<CarSpecs>> = _carList

    // Live Data for special car switch
    private val _isSpecialCarEnabled = MutableLiveData<Boolean>(false)
    open val isSpecialCarEnabled: LiveData<Boolean> = _isSpecialCarEnabled

    fun toggleSpecialCarSwitch(isEnabled: Boolean) {
        _isSpecialCarEnabled.value = isEnabled
        fetchCars()
    }

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


    private val _userFilterData = MutableStateFlow<FilterData?>(null)
    val userFilterData: StateFlow<FilterData?> = _userFilterData

    private val _baseFilterData = MutableStateFlow<FilterData?>(null)
    val baseFilterData : StateFlow<FilterData?> = _baseFilterData

    fun setBaseFilterData(data: FilterData) {
        if (_baseFilterData.value == null) {
            _baseFilterData.value = data
            _userFilterData.value = data
        }
    }

    fun updateFilterData(update: FilterData.() -> FilterData) {
        val currentData = _userFilterData.value
        val newData = currentData?.update()
        _userFilterData.value = newData
        Log.d("DEBUG", "userFilterData was updated = ${userFilterData.value}")
    }

    fun resetUserFilters() {
        _userFilterData.value = _baseFilterData.value
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



    fun fetchCars() {
        viewModelScope.launch {
            try {
                val filters = _userFilterData.value ?: FilterData()
                Log.d("DEBUG", "Fetching cars with filters: minMileage=${filters.minMileage}, maxMileage=${filters.maxMileage}, minYear=${filters.minYear}, maxYear=${filters.maxYear}")

                val cars = repository.getCars(
                    isSpecialCarsEnabled = _isSpecialCarEnabled.value ?: false,
                    mark = filters.markList,
//                    model = filters.modelList.toString(),
                    minPrice = filters.minPrice,
                    maxPrice = filters.maxPrice,
                    minYear = filters.minYear.toInt(),
                    maxYear = filters.maxYear.toInt(),
                    bodyType = filters.bodyTypeList,
                    minMileage = filters.minMileage,
                    maxMileage = filters.maxMileage,
                    fuelType = filters.fuelTypeList,
                    minEngineCapacity = filters.minEngineCapacity,
                    maxEngineCapacity = filters.maxEngineCapacity,
                    minUrbanConsumption = filters.minUrbanConsumption,
                    maxUrbanConsumption = filters.maxUrbanConsumption
                )
                _carList.postValue(cars)
                _errorMessage.postValue(null)
            } catch (e: Exception) {
                _errorMessage.postValue("Error fetching cars: ${e.message}")
            }
        }
    }

    fun getCarById(carId: String): LiveData<CarSpecs?> {
        val carSpecsLiveData = MutableLiveData<CarSpecs?>()

        viewModelScope.launch {
            try {
                val carSpecs = repository.getCarById(carId, _isSpecialCarEnabled.value ?: false)
                 carSpecsLiveData.value = carSpecs
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Error fetching car specs: ${e.message}"
            }
        }
        return carSpecsLiveData
    }

    private val _carPhotosLiveData = MutableLiveData<Map<String, Bitmap?>>()
    val carPhotosLiveData: LiveData<Map<String, Bitmap?>> get() = _carPhotosLiveData


    fun getPhotoById(carId: String) {
        val currentMap = _carPhotosLiveData.value ?: emptyMap()
        if (currentMap.containsKey(carId)) return

        viewModelScope.launch {
            try {
                val carPhoto = repository.getPhotoBitmap(carId, _isSpecialCarEnabled.value ?: false)
                _carPhotosLiveData.postValue(currentMap + (carId to carPhoto))
                _errorMessage.postValue(null)
            } catch (e: Exception) {
                _errorMessage.postValue("Error fetching car photo: ${e.message}")
            }
        }
    }


    fun getUniqueValues(value: String): MutableLiveData<UniqueValueResponse?> {
        val uniqueValuesLiveData = MutableLiveData<UniqueValueResponse?>()

        viewModelScope.launch {
            try {
                val uniqueValues = repository.getUniqueValues(value, _isSpecialCarEnabled.value ?: false)
                uniqueValuesLiveData.value = uniqueValues
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Error fetching unique values: ${e.message}"
            }
        }
        return uniqueValuesLiveData
    }

    fun getMinMaxValues(value: String): MutableLiveData<MinMaxResponse?> {
        val minMaxValuesLiveData = MutableLiveData<MinMaxResponse?>()

        viewModelScope.launch {
            try {
                val minMaxValues = repository.getMinMaxValues(value, _isSpecialCarEnabled.value ?: false)
                minMaxValuesLiveData.value = minMaxValues
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Error fetching min and max values: ${e.message}"
            }
        }
        return  minMaxValuesLiveData
    }


    private val _favouriteCarsSpecsList = MutableLiveData<List<CarSpecs>>()
    val favouriteCarsSpecsList: LiveData<List<CarSpecs>> = _favouriteCarsSpecsList

    fun fetchFavouriteCarsSpecs(
        favouriteCarsIdList: List<FavouriteCar>,
        isSpecialCarsEnabled: Boolean
    ) {
        viewModelScope.launch {
            val result = favouriteCarsIdList.mapNotNull { favCar ->
                try {
                    repository.getCarById(favCar.id.toString(), isSpecialCarsEnabled)
                } catch (e: Exception) {
                    Log.e("FETCH_ERROR", "Error loading car ${favCar.id}: ${e.message}")
                    null
                }
            }
            _favouriteCarsSpecsList.value = result
        }
    }

}
