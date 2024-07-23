package com.example.sharide.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.sharide.data.model.Ride
import com.example.sharide.data.repository.RideRepository

class NearbyRideViewModel(
    private val savedStateHandle: SavedStateHandle
): ViewModel() {
    private val rideRepository = RideRepository()


    // DATA KEY CONSTANT
    private val NEARBY_RIDES_KEY = "nearby_rides"


    // INTERNAL DATA MEMBERS
    val nearbyRides: LiveData<List<Ride>> = savedStateHandle.getLiveData(NEARBY_RIDES_KEY, emptyList())


    // SETTER in SavedStateHandle
    fun setNearbyRides(newNearbyRides: List<Ride>) {
        savedStateHandle[NEARBY_RIDES_KEY] = newNearbyRides
    }
}