package com.example.sharide.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.sharide.data.model.Passenger
import com.example.sharide.data.model.UserStatus
import com.example.sharide.data.repository.RideRepository

class ViewBookingRequestsViewModel(
    private val savedStateHandle: SavedStateHandle
): ViewModel() {
    private val rideRepository = RideRepository()

    // DATA KEY CONSTANT
    private val RIDE_ID_KEY = "ride_id"
    private val REQUESTED_PASSENGER_LIST_KEY = "requested_passenger_list"


    // INTERNAL DATA MEMBERS
    // Ride List Location
    val rideId: LiveData<String> = savedStateHandle.getLiveData(RIDE_ID_KEY, "")
    val requestedPassengerList: LiveData<List<Passenger>> = savedStateHandle.getLiveData(REQUESTED_PASSENGER_LIST_KEY, emptyList())


    // SETTER in SavedStateHandle
    fun setRideId(newRideId: String) {
        savedStateHandle[RIDE_ID_KEY] = newRideId

        rideRepository.listenForBookingRequests(newRideId) {passengerList ->
            setRequestedPassengerList(passengerList)
        }
    }

    fun setRequestedPassengerList(newPassengerList: List<Passenger>) {
        val requestedPassengerList = newPassengerList.filter {passenger ->
            passenger.status == UserStatus.REQUESTED
        }

        savedStateHandle[REQUESTED_PASSENGER_LIST_KEY] = requestedPassengerList
    }

    suspend fun acceptPassengerToRide(acceptedPassenger: Passenger, rideId: String): Int {
        requestedPassengerList.value!!.forEach { passenger ->
            if(passenger.userUid == acceptedPassenger.userUid) {
                passenger.status = UserStatus.ACCEPTED
            }
        }

        return rideRepository.acceptPassengerToRide(acceptedPassenger, rideId)
    }

    suspend fun rejectPassengerToRide(rejectedPassenger: Passenger, rideId: String): Int {
        requestedPassengerList.value!!.forEach { passenger ->
            if(passenger.userUid == rejectedPassenger.userUid) {
                passenger.status = UserStatus.REJECTED
            }
        }

        return rideRepository.rejectPassengerToRide(rejectedPassenger, rideId)
    }
}