package com.example.sharidev2.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharidev2.data.model.Driver
import com.example.sharidev2.data.model.Ride
import com.example.sharidev2.data.model.SearchLocation
import com.example.sharidev2.data.model.Vehicle
import com.example.sharidev2.data.repository.RideRepository
import com.example.sharidev2.utility.FirebaseClient
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SharedCreateRideViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    // Repository
    private val rideRepository = RideRepository()

    // DATA KEY CONSTANT
    private val ORIGIN_KEY = "origin"
    private val DESTINATION_KEY = "destination"
    private val VEHICLE_KEY = "vehicle"
    private val PASSENGER_CAPACITY_KEY = "passenger_capacity"
    private val RIDE_DATE_TIME_KEY = "ride_date"
    private val CREATE_RIDE_STATUS_KEY = "create_ride_status"

    // RIDE STATUS
    private val CREATE_RIDE_PENDING = 0
    private val CREATE_RIDE_SUCCESS = 1
    private val CREATE_RIDE_FAILED = -1


    // INTERNAL DATA MEMBERS
    // Origin Location
    val origin: LiveData<SearchLocation> = savedStateHandle.getLiveData(ORIGIN_KEY)

    // Destination Location
    val destination: LiveData<SearchLocation> = savedStateHandle.getLiveData(DESTINATION_KEY)

    // Vehicle
    val vehicle: LiveData<Vehicle> = savedStateHandle.getLiveData(VEHICLE_KEY)

    // Passenger Capacity
    val capacity: LiveData<Int> = savedStateHandle.getLiveData(PASSENGER_CAPACITY_KEY)

    // Ride Date Time
    val rideDateTime: LiveData<Timestamp> = savedStateHandle.getLiveData(RIDE_DATE_TIME_KEY)

    // Create Ride Status
    val createRideStatus: LiveData<Int> = savedStateHandle.getLiveData(CREATE_RIDE_STATUS_KEY)


    // CONSTRUCTOR
    init {
        if (rideDateTime.value == null) {
            setRideDateTime(Timestamp.now())
        }

        setCreateRideStatus(CREATE_RIDE_PENDING)
    }


    // SETTER in SavedStateHandle
    // Origin Location
    fun setOrigin(newOrigin: SearchLocation) {
        savedStateHandle[ORIGIN_KEY] = newOrigin
    }

    // Destination Location
    fun setDestination(newDestination: SearchLocation) {
        savedStateHandle[DESTINATION_KEY] = newDestination
    }

    // Vehicle
    fun setVehicle(newVehicle: Vehicle) {
        savedStateHandle[VEHICLE_KEY] = newVehicle
        // set default capacity as max possible passenger
        setCapacity((vehicle.value?.capacity ?: 1) - 1)
    }

    // Passenger Capacity
    fun setCapacity(capacity: Int) {
        if (capacity < vehicle.value?.capacity!!) {
            savedStateHandle[PASSENGER_CAPACITY_KEY] = capacity
        }
    }

    // Ride Date Time
    fun setRideDateTime(newRideDateTime: Timestamp) {
        savedStateHandle[RIDE_DATE_TIME_KEY] = newRideDateTime
    }

    // Create Ride Status
    fun setCreateRideStatus(response: Int) {
        savedStateHandle[CREATE_RIDE_STATUS_KEY] = response
    }


    // Create Ride
    suspend fun createRide() {
        val currentUser = FirebaseClient.firebaseAuth.currentUser

        if(currentUser != null) {
            val newCreatedRide = Ride(
                origin = origin.value!!,
                destination = destination.value!!,
                datetime = rideDateTime.value!!,
                driver = Driver(
                    userUid = currentUser?.uid,
                    vehicle = vehicle.value!!
                ),
                availableSeats = capacity.value!!,
                createdAt = Timestamp.now()
            )

            viewModelScope.launch(Dispatchers.Main) {
                rideRepository.createRide(newCreatedRide, object : RideRepository.CreateRideCallback {
                    override fun onCreateSuccess() {
                        setCreateRideStatus(CREATE_RIDE_SUCCESS)
                    }

                    override fun onCreateFailure(error: Throwable) {
                        Log.e("Create Ride", error.message.toString())

                        setCreateRideStatus(CREATE_RIDE_FAILED)
                    }
                })
            }
        } else {
            setCreateRideStatus(CREATE_RIDE_FAILED)
        }
    }



    // HELPER METHODS
    fun resetData() {
        setOrigin(SearchLocation()) // Pass an empty SearchLocation or null, depending on your implementation
        setDestination(SearchLocation())
        setVehicle(Vehicle()) // Pass an empty Vehicle or null
        setCapacity(0) // Set capacity to 0 or any default value you prefer
        setRideDateTime(Timestamp.now()) // Set the date to the current date or any default date
        setCreateRideStatus(CREATE_RIDE_PENDING) // Reset the create ride status
    }
}