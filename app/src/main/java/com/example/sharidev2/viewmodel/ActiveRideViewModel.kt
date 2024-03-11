package com.example.sharidev2.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharidev2.data.model.Driver
import com.example.sharidev2.data.model.Passenger
import com.example.sharidev2.data.model.Ride
import com.example.sharidev2.data.model.User
import com.example.sharidev2.data.model.UserLocation
import com.example.sharidev2.data.repository.ActiveRideRepository
import com.example.sharidev2.utility.FirebaseClient
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ActiveRideViewModel(
    private val savedStateHandle: SavedStateHandle
): ViewModel() {
    private val activeRideRepository = ActiveRideRepository()
    private val currentUser = FirebaseClient.firebaseAuth.currentUser

    // DATA KEY CONSTANT
    private val ACTIVE_RIDE_KEY = "active_ride"
    private val ACTIVE_RIDE_DRIVER_KEY = "active_ride_driver"
    private val ACTIVE_RIDE_PASSENGERS_KEY = "active_ride_passengers"
    private val ACTIVE_RIDE_USER_LOCATION_KEY = "active_ride_user_location"
    private val ACTIVE_RIDE_CURRENT_USER_KEY = "active_ride_current_user"
    private val ACTIVE_RIDE_CURRENT_USER_ROLE_KEY = "active_ride_current_user_role"



    // INTERNAL DATA MEMBERS
    val activeRide: LiveData<Ride> = savedStateHandle.getLiveData(ACTIVE_RIDE_KEY)
    val activeRideDriver: LiveData<Driver> = savedStateHandle.getLiveData(ACTIVE_RIDE_DRIVER_KEY)
    val activeRidePassengers: LiveData<List<Passenger>> = savedStateHandle.getLiveData(ACTIVE_RIDE_PASSENGERS_KEY)
    val activeRideUserLocationList: LiveData<List<UserLocation>> = savedStateHandle.getLiveData(ACTIVE_RIDE_PASSENGERS_KEY)
    val activeRideCurrentUserId: LiveData<String> = savedStateHandle.getLiveData(ACTIVE_RIDE_CURRENT_USER_KEY)
    val activeRideCurrentUserRole: LiveData<String> = savedStateHandle.getLiveData(ACTIVE_RIDE_CURRENT_USER_ROLE_KEY)


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

    private fun setActiveRideUserLocationList(newActiveRideUserLocationList: List<UserLocation>) {
        savedStateHandle[ACTIVE_RIDE_USER_LOCATION_KEY] = newActiveRideUserLocationList
    }

    fun setActiveRideCurrentUser(newActiveRideCurrentUser: String) {
        savedStateHandle[ACTIVE_RIDE_CURRENT_USER_KEY] = newActiveRideCurrentUser
    }

    private fun setActiveRideCurrentUserRole(newActiveRideCurrentUserRole: String) {
        savedStateHandle[ACTIVE_RIDE_CURRENT_USER_ROLE_KEY] = newActiveRideCurrentUserRole
    }


    init {
        if(currentUser != null) {
            setActiveRideCurrentUser(currentUser.uid)
        }

        if(activeRide.isInitialized && activeRide.value?.id != null) {
            viewModelScope.launch(Dispatchers.IO) {
                val driver = activeRideRepository.getRideDriver(activeRide.value!!.id!!)
                val passengers = activeRideRepository.getRidePassengers(activeRide.value!!.id!!)

                val userLocationList = mutableListOf<UserLocation>()

                val driverLocation = activeRideRepository.getUserLocation(driver)

                // Determine Current User Role
                if(activeRideCurrentUserId.value != null && driver.userUid == activeRideCurrentUserId.value) {
                    setActiveRideCurrentUserRole("driver")
                } else {
                    setActiveRideCurrentUserRole("passenger")
                }


                userLocationList.add(driverLocation)

                for(passenger in passengers) {
                    if(activeRideCurrentUserId.value != null && passenger.userUid != activeRideCurrentUserId.value) {
                        val passengerLocation = activeRideRepository.getUserLocation(passenger)

                        userLocationList.add(passengerLocation)
                    }
                }

                setActiveRideUserLocationList(userLocationList)
            }
        }
    }


    fun addRoutePathList(routePathList: MutableList<MutableList<LatLng>>) {
        if(activeRide.value?.id != null) {
            viewModelScope.launch(Dispatchers.IO) {
                activeRideRepository.addRoutePathList(activeRide.value!!.id!!, routePathList)
            }
        }
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
