package com.example.sharidev2.viewmodel

import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng

class CurrentLocationViewModel {
    // DATA MEMBERS
    private val _currentLocation = MutableLiveData<LatLng>()
    val currentLocation: MutableLiveData<LatLng> get() = _currentLocation
}