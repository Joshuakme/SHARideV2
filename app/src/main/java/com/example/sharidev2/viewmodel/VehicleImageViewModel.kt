package com.example.sharidev2.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharidev2.data.repository.VehicleDocRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VehicleImageViewModel: ViewModel() {
    private val repository = VehicleDocRepository()

    private val _vehicleFrontImageUri = MutableLiveData<Uri?>()
    val vehicleFrontImageUri: LiveData<Uri?>
        get() = _vehicleFrontImageUri

    private val _vehicleBackImageUri = MutableLiveData<Uri?>()
    val vehicleBackImageUri: LiveData<Uri?>
        get() = _vehicleBackImageUri


    init {
        viewModelScope.launch(Dispatchers.Main) {
            val licenseMap = repository.getVehicleImage()

            setVehicleFrontImageUri(licenseMap["vehicleFrontImageUri"]?: Uri.EMPTY)
            setVehicleBackImageUri(licenseMap["vehicleBackImageUri"]?: Uri.EMPTY)
        }
    }


    // Function to set the front image URI
    fun setVehicleFrontImageUri(uri: Uri?) {
        _vehicleFrontImageUri.value = uri
    }

    // Function to set the back image URI
    fun setVehicleBackImageUri(uri: Uri?) {
        _vehicleBackImageUri.value = uri
    }

    suspend fun addImagesToDB(): Int {
        return repository.addVehicleImage(vehicleFrontImageUri.value, vehicleBackImageUri.value)
    }

}

