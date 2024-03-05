package com.example.sharidev2.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.sharidev2.data.model.Ride
import com.example.sharidev2.data.repository.RideRepository

class RideDetailViewModel(
    private val savedStateHandle: SavedStateHandle
): ViewModel() {
    private val rideRepository = RideRepository()

    // DATA KEY CONSTANT
    private val RIDE_KEY = "ride"


    // INTERNAL DATA MEMBERS
    // Ride
    val ride: LiveData<Ride> = savedStateHandle.getLiveData(RIDE_KEY)


    // SETTER in SavedStateHandle
    fun setRide(newRide: Ride) {
        savedStateHandle[RIDE_KEY] = newRide
    }
}