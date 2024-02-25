package com.example.sharidev2.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharidev2.data.model.Vehicle
import com.example.sharidev2.data.repository.DriverVehicleRepository
import com.example.sharidev2.firebase.FirebaseInitializer
import kotlinx.coroutines.launch

class DriverVehicleViewModel: ViewModel() {
    private val driverVehicleRepository = DriverVehicleRepository(
        FirebaseInitializer.firestore,
        FirebaseInitializer.firebaseAuth
    )

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