package com.example.otomotoapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel: ViewModel() {

    private val repository = CarRepository()

    // LiveData для списка машин
    private val _carList = MutableLiveData<List<CarSpecs>>()
    val carList: LiveData<List<CarSpecs>> = _carList

    // LiveData для сообщений об ошибке
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // Метод для получения данных
    fun getCarSpecs() {
        repository.getCarSpecs(object : CarRepository.CarSpecsCallback {
            override fun onSuccess(cars: List<CarSpecs>) {
                _carList.value = cars // Обновляем список машин
                _errorMessage.value = null // Сбрасываем ошибку, если данные получены
            }

            override fun onError(error: String) {
                _errorMessage.value = error // Устанавливаем сообщение об ошибке
            }
        })
    }
}
