package com.example.sharide.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.sharide.data.model.Ride
import com.example.sharide.data.repository.RideRepository
import com.google.android.gms.maps.model.LatLng

class BookingDetailViewModel(
    private val savedStateHandle: SavedStateHandle
): ViewModel() {
    // Repository
    private val rideRepository = RideRepository()


    // DATA KEY CONSTANT
    private val RIDE_ROUTE_KEY = "ride_route"

    // INTERNAL DATA MEMBERS
    // Ride Route Time
    val rideRoute: LiveData<MutableList<LatLng>> = savedStateHandle.getLiveData(RIDE_ROUTE_KEY)


    suspend fun getRoute(originName: String, destName: String): MutableList<LatLng>? {
        return rideRepository.getRideRoute(originName, destName)
    }

    suspend fun startRide(ride: Ride): Int {
        return rideRepository.startRide(ride)
    }


    // SETTER in SavedStateHandle
    // Ride Route
    fun setRideRoute(newRideRoute: MutableList<LatLng>) {
        savedStateHandle[RIDE_ROUTE_KEY] = newRideRoute
    }
}