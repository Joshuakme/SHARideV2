package com.example.sharidev2.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.sharidev2.data.model.Passenger
import com.example.sharidev2.data.model.UserStatus
import com.example.sharidev2.data.repository.RideRepository

class ViewBookingRequestsViewModel(
    private val savedStateHandle: SavedStateHandle
): ViewModel() {
    private val rideRepository = RideRepository()

    // DATA KEY CONSTANT
    private val PASSENGER_LIST_KEY = "passenger_list"


    // INTERNAL DATA MEMBERS
    // Ride List Location
    val passengerList: LiveData<List<Passenger>> = savedStateHandle.getLiveData(PASSENGER_LIST_KEY, emptyList())


    // SETTER in SavedStateHandle
    fun setPassengerList(newPassenger: List<Passenger>) {
        savedStateHandle[PASSENGER_LIST_KEY] = newPassenger
    }

    suspend fun acceptPassengerToRide(acceptedPassenger: Passenger, rideId: String): Int {
        passengerList.value!!.forEach { passenger ->
            if(passenger.userUid == acceptedPassenger.userUid) {
                passenger.status = UserStatus.ACCEPTED
            }
        }

        return rideRepository.acceptPassengerToRide(acceptedPassenger, rideId)
    }

    suspend fun rejectPassengerToRide(rejectedPassenger: Passenger, rideId: String): Int {
        passengerList.value!!.forEach { passenger ->
            if(passenger.userUid == rejectedPassenger.userUid) {
                passenger.status = UserStatus.REJECTED
            }
        }

        return rideRepository.rejectPassengerToRide(rejectedPassenger, rideId)
    }
}