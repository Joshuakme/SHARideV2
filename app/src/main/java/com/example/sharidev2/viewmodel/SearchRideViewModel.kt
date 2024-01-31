package com.example.sharidev2.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sharidev2.data.model.SearchLocation
import com.google.android.gms.maps.model.LatLng

class SearchRideViewModel: ViewModel() {
    // DATA MEMBERS
    // Origin Location
    private val _origin = MutableLiveData<SearchLocation>()
    val origin: MutableLiveData<SearchLocation> get() = _origin

    // Destination Location
    private val _destination = MutableLiveData<SearchLocation>()
    val destination: MutableLiveData<SearchLocation> get() = _destination

    // Ride Country
    private val _country = MutableLiveData<String>()
    val country: MutableLiveData<String> get() = _country

    // Driver's Gender
    private val _driverGender = MutableLiveData<String>()
    val driverGender: MutableLiveData<String> get() = _driverGender

    // Vehicle Type
    private val _vehicleType = MutableLiveData<String>()
    val vehicleType: MutableLiveData<String> get() = _vehicleType


    // SETTER
    fun setOrigin(newOrigin: SearchLocation) {
        _origin.value = newOrigin
    }

    fun setDestination(newDestination: SearchLocation) {
        _destination.value = newDestination
    }

    fun setRideCountry(newRideCountry: String) {
        _country.value = newRideCountry
    }

    fun setDriverGender(newDriverGender: String) {
        _driverGender.value = newDriverGender
    }

    fun setVehicleType(newVehicleType: String) {
        _vehicleType.value = newVehicleType
    }
}