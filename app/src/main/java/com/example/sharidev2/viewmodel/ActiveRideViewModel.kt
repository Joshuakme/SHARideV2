package com.example.sharidev2.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.sharidev2.data.model.Driver
import com.example.sharidev2.data.model.Passenger
import com.example.sharidev2.data.model.Ride
import com.example.sharidev2.data.repository.ActiveRideRepository
import com.google.firebase.Timestamp

class ActiveRideViewModel(
    private val savedStateHandle: SavedStateHandle
): ViewModel() {
    private val activeRideRepository = ActiveRideRepository()

    // DATA KEY CONSTANT
    private val ACTIVE_RIDE_KEY = "active_ride"
    private val ACTIVE_RIDE_DRIVER_KEY = "active_ride_driver"
    private val ACTIVE_RIDE_PASSENGERS_KEY = "active_ride_passengers"


    // INTERNAL DATA MEMBERS
    val activeRide: LiveData<Ride> = savedStateHandle.getLiveData(ACTIVE_RIDE_KEY)
    val activeRideDriver: LiveData<Driver> = savedStateHandle.getLiveData(ACTIVE_RIDE_DRIVER_KEY)
    val activeRidePassengers: LiveData<List<Passenger>> = savedStateHandle.getLiveData(ACTIVE_RIDE_PASSENGERS_KEY)


    // SETTER in SavedStateHandle
    fun setActiveRide(newActiveRide: Ride) {
        savedStateHandle[ACTIVE_RIDE_KEY] = newActiveRide
    }

    fun setActiveRideDriver(newActiveRideDriver: Driver) {
        savedStateHandle[ACTIVE_RIDE_DRIVER_KEY] = newActiveRideDriver
    }

    fun setActiveRidePassenger(newActiveRidePassenger: List<Passenger>) {
        savedStateHandle[ACTIVE_RIDE_PASSENGERS_KEY] = newActiveRidePassenger
    }

    fun startRide() {
        val timestamp = Timestamp.now()

        // set starttime in firestore
    }

    fun endRide() {
        val timestamp = Timestamp.now()

        // set endtime in firestore
    }
}
