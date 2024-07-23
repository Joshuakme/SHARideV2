package com.example.sharide.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharide.data.model.Vehicle
import com.example.sharide.data.repository.DriverVehicleRepository
import kotlinx.coroutines.launch

class DriverVehicleViewModel: ViewModel() {
    private val driverVehicleRepository = DriverVehicleRepository()

    private val _vehicleList = MutableLiveData<List<Vehicle>>()
    val vehicleList: LiveData<List<Vehicle>> get() = _vehicleList

    init {
        viewModelScope.launch {
            try {
                val newVehicleList = driverVehicleRepository.getDriverVehicleList()
                _vehicleList.value = newVehicleList
            } catch (e: Exception) {
                // Handle error
                Log.e("Driver Vehicle ViewModel", e.message ?: "Error")
            }
        }
    }
}