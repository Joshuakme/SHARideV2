package com.example.sharide.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharide.data.model.UserLocation
import com.example.sharide.data.repository.UserLocationRepository
import com.example.sharide.utility.FirebaseClient
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch

class CurrentLocationViewModel(): ViewModel() {
    val locationRepo = UserLocationRepository()

    // DATA MEMBERS
    private val _currentLocation = MutableLiveData<LatLng>()
    val currentLocation: MutableLiveData<LatLng> get() = _currentLocation



    fun setLocation(location: LatLng) {
        _currentLocation.value = location

        viewModelScope.launch {
            if(FirebaseClient.getCurrentUser() != null) {
                val currentUserLocation = UserLocation(
                    location = _currentLocation.value,
                    user = FirebaseClient.getCurrentUser(),
                    timestamp = Timestamp.now().toDate()
                )

                locationRepo.updateUserLocation(currentUserLocation)
            }
        }

    }
}
