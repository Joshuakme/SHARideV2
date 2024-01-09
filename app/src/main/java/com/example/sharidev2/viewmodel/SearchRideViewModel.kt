package com.example.sharidev2.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng

class SearchRideViewModel: ViewModel() {
    // DATA MEMBERS
    // Origin Location
    private val _origin = MutableLiveData<LatLng>()
    val origin: MutableLiveData<LatLng> get() = _origin

    // Destination Location
    private val _destination = MutableLiveData<LatLng>()
    val destination: MutableLiveData<LatLng> get() = _destination

    // Ride Country
    private val _country = MutableLiveData<String>()
    val country: MutableLiveData<String> get() = _country


    // SETTER
    fun setOrigin(newOrigin: LatLng) {
        _origin.value = newOrigin
    }

    fun setDestination(newDestination: LatLng) {
        _destination.value = newDestination
    }

    fun setRideCountry(newRideCountry: String) {
        _country.value = newRideCountry
    }
}