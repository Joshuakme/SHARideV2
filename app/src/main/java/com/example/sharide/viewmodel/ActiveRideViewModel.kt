package com.example.sharide.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharide.data.model.Contact
import com.example.sharide.data.model.Driver
import com.example.sharide.data.model.Passenger
import com.example.sharide.data.model.Ride
import com.example.sharide.data.model.UserLocation
import com.example.sharide.data.model.UserStatus
import com.example.sharide.data.repository.ActiveRideRepository
import com.example.sharide.data.repository.UserLocationRepository
import com.example.sharide.utility.Constants
import com.example.sharide.utility.FirebaseClient
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ActiveRideViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val activeRideRepository = ActiveRideRepository()
    private val userLocationRepository = UserLocationRepository()
    private val currentUser = FirebaseClient.firebaseAuth.currentUser

    // DATA KEY CONSTANT
    private val ACTIVE_RIDE_KEY = "active_ride"
    private val ACTIVE_RIDE_DRIVER_KEY = "active_ride_driver"
    private val ACTIVE_RIDE_PASSENGERS_KEY = "active_ride_passengers"
    private val ACTIVE_RIDE_USER_LOCATION_KEY = "active_ride_user_location"
    private val ACTIVE_RIDE_CURRENT_USER_KEY = "active_ride_current_user"
    private val ACTIVE_RIDE_CURRENT_USER_ROLE_KEY = "active_ride_current_user_role"
    private val ACTIVE_DRIVER_RIDE_SELECTED_PASSENGER = "active_driver_ride_selected_passenger"
    private val CONTACT_KEY = "contact_key"
    private val USER_LOCATION_KEY = "user_location_key"


    // INTERNAL DATA MEMBERS
    val activeRide: LiveData<Ride> = savedStateHandle.getLiveData(ACTIVE_RIDE_KEY)
    val activeRideUserLocationList: LiveData<List<UserLocation>> =
        savedStateHandle.getLiveData(ACTIVE_RIDE_PASSENGERS_KEY)
    val activeRideCurrentUserId: LiveData<String> =
        savedStateHandle.getLiveData(ACTIVE_RIDE_CURRENT_USER_KEY)
    val activeRideCurrentUserRole: LiveData<String> =
        savedStateHandle.getLiveData(ACTIVE_RIDE_CURRENT_USER_ROLE_KEY)
    val activeDriverRideSelectedPassenger: LiveData<Passenger> = savedStateHandle.getLiveData(ACTIVE_DRIVER_RIDE_SELECTED_PASSENGER)
    val contactList: LiveData<List<Contact>> = savedStateHandle.getLiveData(CONTACT_KEY)
    val userLocation: LiveData<UserLocation> = savedStateHandle.getLiveData(USER_LOCATION_KEY)

    // SETTER in SavedStateHandle
    fun startActiveRide(newActiveRide: Ride) {
        activeRideRepository.listenForActiveRideChanges(newActiveRide.id!!) {ride ->
            setActiveRide(ride)
        }
    }

    fun setActiveRide(newActiveRide: Ride) {
        savedStateHandle[ACTIVE_RIDE_KEY] = newActiveRide

        if (currentUser != null) {
            if (newActiveRide.driver.userUid == currentUser.uid) {
                setActiveRideCurrentUserRole("driver")
            } else {
                for (passenger in newActiveRide.passengers) {
                    if (passenger.userUid == currentUser.uid) {
                        setActiveRideCurrentUserRole("passenger")
                    }
                }
            }
        }

        if (activeRide.isInitialized && activeRide.value?.id != null) {
            viewModelScope.launch(Dispatchers.Main) {
                val userLocationList = mutableListOf<UserLocation>()

//                val driver = activeRideRepository.getRideDriver(activeRide.value!!.id!!)
                val driver = activeRide.value!!.driver

                val driverLocation = activeRideRepository.getCurrentUserLocation(driver)
                userLocationList.add(driverLocation)

                // Determine Current User Role
                if (activeRideCurrentUserId.value != null && driver.userUid == activeRideCurrentUserId.value) {
                    setActiveRideCurrentUserRole("driver")
                } else {
                    setActiveRideCurrentUserRole("passenger")
                }

//                val passengers = activeRideRepository.getRidePassengers(activeRide.value!!.id!!)
                val passengers = activeRide.value!!.passengers

                for (passenger in passengers) {
                    if (activeRideCurrentUserId.value != null && passenger.userUid != activeRideCurrentUserId.value) {
                        val passengerLocation = activeRideRepository.getCurrentUserLocation(passenger)

                        userLocationList.add(passengerLocation)
                    }
                }

                if(passengers.isNotEmpty()) {
                    if(activeDriverRideSelectedPassenger.value != null) {
                        passengers.forEach { passenger ->
                            if(passenger.userUid!! == activeDriverRideSelectedPassenger.value!!.userUid!!) setDriverActiveRideSelectedPassenger(passenger)
                        }
                    } else {
                        setDriverActiveRideSelectedPassenger(passengers[0])
                    }
                }

                setActiveRideUserLocationList(userLocationList)
            }
        }
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

    fun setDriverActiveRideSelectedPassenger(newSelectedPassenger: Passenger) {
        savedStateHandle[ACTIVE_DRIVER_RIDE_SELECTED_PASSENGER] = newSelectedPassenger
    }

    private fun setContactList(newContactList: List<Contact>) {
        savedStateHandle[CONTACT_KEY] = newContactList
    }

    private fun setUserLocation(newUserLocation: UserLocation) {
        savedStateHandle[USER_LOCATION_KEY] = newUserLocation
    }


    init {
        if (currentUser != null) {
            setActiveRideCurrentUser(currentUser.uid)


            viewModelScope.launch(Dispatchers.Main) {
                val contactList = activeRideRepository.getEmergencyContactList()
                val userLocation = userLocationRepository.getUserLocation()

                setContactList(contactList)
                setUserLocation(userLocation)
            }
        }
    }


    fun addRoutePathList(routePathList: MutableList<MutableList<LatLng>>) {
        if (activeRide.value?.id != null) {
            viewModelScope.launch(Dispatchers.IO) {
                activeRideRepository.addRoutePathList(activeRide.value!!.id!!, routePathList)
            }
        }
    }

    suspend fun cancelRide(): Int {
        if (activeRideCurrentUserId.isInitialized) {
            if (activeRideCurrentUserRole.value == "passenger") {
                return activeRideRepository.cancelRideByPassenger(
                    activeRide.value!!,
                    activeRideCurrentUserId.value!!
                )
            } else {
                return activeRideRepository.cancelRideByDriver(
                    activeRide.value!!
                )
            }
        }
        return Constants.FIREBASE_REQUEST_FAILED
    }

    suspend fun pickUpPassenger(): Int {
        val selectedPassengerId = activeDriverRideSelectedPassenger.value?.userUid

        return if(activeRide.value != null && selectedPassengerId != null) {
            val passengerList = activeRide.value!!.passengers

            passengerList.map {passenger ->
                if(passenger.userUid == selectedPassengerId) {
                    passenger.status = UserStatus.IN_VEHICLE
                }
            }

            activeRideRepository.pickUpPassenger(activeRide.value!!, selectedPassengerId )
        } else {
            Constants.FIREBASE_REQUEST_FAILED
        }
    }

    suspend fun dropOffPassenger(): Int {
        val selectedPassengerId = activeDriverRideSelectedPassenger.value?.userUid

        return if(activeRide.value != null && selectedPassengerId != null) {
            val passengerList = activeRide.value!!.passengers

            passengerList.map {passenger ->
                if(passenger.userUid == selectedPassengerId) {
                    passenger.status = UserStatus.COMPLETED
                }
            }
            activeRideRepository.dropOffPassenger(activeRide.value!!, selectedPassengerId )
        } else {
            Constants.FIREBASE_REQUEST_FAILED
        }
    }

    suspend fun completeRide(): Int {
        if(activeRide.value != null) {
            return activeRideRepository.completeRide(activeRide.value!!)
        }
        return Constants.FIREBASE_REQUEST_FAILED
    }

    fun getCurrentUserLocation() {
        viewModelScope.launch(Dispatchers.Main) {
            val userLocation = userLocationRepository.getUserLocation()

            setUserLocation(userLocation)
        }
    }

    fun getUsersLocation() {
        viewModelScope.launch {
            val userLocationList = mutableListOf<UserLocation>()
            if(activeRide.value?.driver?.userUid != null) {

                val driverLocation = activeRideRepository.getDriverLocation(activeRide.value!!.driver.userUid!!)

                if(driverLocation != null) {
                    userLocationList.add(driverLocation)
                }
            }

            if(activeRide.value?.passengers != null) {
                val passengerLocationList = activeRideRepository.getPassengersLocation(activeRide.value!!.passengers)

                if(passengerLocationList.isNotEmpty()) {
                    passengerLocationList.forEach {userLocation ->
                        userLocationList.add(userLocation)
                    }
                }
            }

            setActiveRideUserLocationList(userLocationList)
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
