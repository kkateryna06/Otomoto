package com.example.otomotoapp

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Response

open class MainViewModel: ViewModel() {

    private val repository = CarRepository()

    // LiveData for car list
    private val _carList = MutableLiveData<List<CarSpecs>>()
    open val carList: LiveData<List<CarSpecs>> = _carList

    // Live Data for special car switch
    private val _isSpecialCarEnabled = MutableLiveData<Boolean>(false)
    open val isSpecialCarEnabled: LiveData<Boolean> = _isSpecialCarEnabled

    fun toggleSpecialCarSwitch(isEnabled: Boolean) {
        _isSpecialCarEnabled.value = isEnabled
        fetchCars() // Re-fetch data when the toggle changes
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


    fun fetchCars() {
        viewModelScope.launch {
            try {
                val cars = repository.getCars(
                    isSpecialCarsEnabled = _isSpecialCarEnabled.value ?: false,
                    mark = _selectedMark.value,
                    model = _selectedModel.value,
                    minPrice = _minPrice.value,
                    maxPrice = _maxPrice.value,
                    minYear = _minYear.value,
                    maxYear = _maxYear.value,
                    bodyType = _selectedBodyType.value,
                    minMileage = _minMileage.value,
                    maxMileage = _maxMileage.value,
                    fuelType = _selectedFuelType.value,
                    minEngineCapacity = _minEngineCapacity.value,
                    maxEngineCapacity = _maxEngineCapacity.value,
                    minUrbanConsumption = _minUrbanConsumption.value,
                    maxUrbanConsumption = _maxUrbanConsumption.value
                )
                _carList.value = cars
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Error fetching cars: ${e.message}"
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
}
