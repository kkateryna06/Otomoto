package com.example.otomotoapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.math.BigInteger

class MainViewModel: ViewModel() {

    private val repository = CarRepository()

    // LiveData for car list
    private val _carList = MutableLiveData<List<CarSpecs>>()
    val carList: LiveData<List<CarSpecs>> = _carList

    // Live Data for special car switch
    private val _isSpecialCarEnabled = MutableLiveData<Boolean>(false)
    val isSpecialCarEnabled: LiveData<Boolean> = _isSpecialCarEnabled

    fun toggleSpecialCarSwitch(isEnabled: Boolean) {
        _isSpecialCarEnabled.value = isEnabled
        fetchCars() // Re-fetch data when the toggle changes
    }

    // LiveData for error messages
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun fetchCars() {
        viewModelScope.launch {
            try {
                val cars = repository.getCars(_isSpecialCarEnabled.value ?: false)
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
}
