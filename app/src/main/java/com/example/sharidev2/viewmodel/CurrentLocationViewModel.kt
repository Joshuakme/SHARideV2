package com.example.sharidev2.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.google.android.gms.maps.model.LatLng

class CurrentLocationViewModel: ViewModel() {
    // DATA MEMBERS
    private val _currentLocation = MutableLiveData<LatLng>()
    val currentLocation: MutableLiveData<LatLng> get() = _currentLocation


    fun setLocation(location: LatLng) {
        _currentLocation.value = location
    }


    companion object {
        fun getInstance(owner: ViewModelStoreOwner): CurrentLocationViewModel {
            return ViewModelProvider(owner)[CurrentLocationViewModel::class.java]
        }
    }
}