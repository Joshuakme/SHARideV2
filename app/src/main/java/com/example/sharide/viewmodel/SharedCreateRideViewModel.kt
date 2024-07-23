package com.example.sharide.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharide.data.model.Driver
import com.example.sharide.data.model.Ride
import com.example.sharide.data.model.SearchLocation
import com.example.sharide.data.model.Vehicle
import com.example.sharide.data.repository.DriverVehicleRepository
import com.example.sharide.data.repository.RideRepository
import com.example.sharide.utility.Constants
import com.example.sharide.utility.FirebaseClient
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SharedCreateRideViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    // Repository
    private val rideRepository = RideRepository()
    private val driverVehicleRepository = DriverVehicleRepository()

    // ViewModel
    private val vehicleViewModel = DriverVehicleViewModel()

    // DATA KEY CONSTANT
    private val ORIGIN_KEY = "origin"
    private val DESTINATION_KEY = "destination"
    private val VEHICLE_KEY = "vehicle"
    private val PASSENGER_CAPACITY_KEY = "passenger_capacity"
    private val RIDE_DATE_TIME_KEY = "ride_date_time"
    private val RIDE_ROUTE_KEY = "ride_route"
    private val CREATE_RIDE_STATUS_KEY = "create_ride_status"



    // INTERNAL DATA MEMBERS
    // Origin Location
    val origin: LiveData<SearchLocation?> = savedStateHandle.getLiveData(ORIGIN_KEY)

    // Destination Location
    val destination: LiveData<SearchLocation?> = savedStateHandle.getLiveData(DESTINATION_KEY)

    // Vehicle
    val vehicle: LiveData<Vehicle> = savedStateHandle.getLiveData(VEHICLE_KEY)

    // Passenger Capacity
    val capacity: LiveData<Int> = savedStateHandle.getLiveData(PASSENGER_CAPACITY_KEY)

    // Ride Date Time
    val rideDateTime: LiveData<Timestamp> = savedStateHandle.getLiveData(RIDE_DATE_TIME_KEY)

    // Ride Route
    val rideRoute: LiveData<MutableList<LatLng>?> = savedStateHandle.getLiveData(RIDE_ROUTE_KEY)

    // Create Ride Status
    val createRideStatus: LiveData<Int> = savedStateHandle.getLiveData(CREATE_RIDE_STATUS_KEY)


    // CONSTRUCTOR
    init {
        Log.e("Shared Create Ride ViewModel", "Init Shared Create Ride ViewModel")
        if (rideDateTime.value == null) {
            setRideDateTime(Timestamp.now())
        }

        if(vehicle.value == null) {
            viewModelScope.launch {
                try {
                    val vehicleList = driverVehicleRepository.getDriverVehicleList()

                    if(vehicleList.isNotEmpty()) {
                        setVehicle(vehicleList[0])
                    }
                } catch (e: Exception) {
                    Log.e("Shared Create Ride ViewModel", e.message.toString())
                }
            }
        }

        setCreateRideStatus(Constants.UI_DATA_LOADING)
    }


    // SETTER in SavedStateHandle
    // Origin Location
    fun setOrigin(newOrigin: SearchLocation) {
        savedStateHandle[ORIGIN_KEY] = newOrigin
    }

    // Destination Location
    fun setDestination(newDestination: SearchLocation) {
        savedStateHandle[DESTINATION_KEY] = newDestination

        if(origin.value?.name != null && destination.value?.name != null) {
            viewModelScope.launch(Dispatchers.Main) {
                val route = rideRepository.getRideRoute(origin.value!!.name, destination.value!!.name)

                if(route != null) {
                    setRideRoute(route)
                }
            }
        }
    }

    // Vehicle
    fun setVehicle(newVehicle: Vehicle) {
        savedStateHandle[VEHICLE_KEY] = newVehicle
        // set default capacity as max possible passenger

        setCapacity(newVehicle.capacity)
    }

    // Passenger Capacity
    fun setCapacity(capacity: Int) {
        if (capacity <= vehicle.value?.capacity!!) {
            savedStateHandle[PASSENGER_CAPACITY_KEY] = capacity
        }
    }

    // Ride Date Time
    fun setRideDateTime(newRideDateTime: Timestamp) {
        savedStateHandle[RIDE_DATE_TIME_KEY] = newRideDateTime
    }

    // Create Ride Route
    fun setRideRoute(newRideRoute: MutableList<LatLng>) {
        savedStateHandle[RIDE_ROUTE_KEY] = newRideRoute
    }

    // Create Ride Status
    fun setCreateRideStatus(response: Int) {
        savedStateHandle[CREATE_RIDE_STATUS_KEY] = response
    }


    // Save Ride Route
    fun saveRoutePath() {
        if(origin.value != null && destination.value != null && rideRoute.value != null) {
            viewModelScope.launch(Dispatchers.IO) {
                rideRepository.addRoutePath(origin.value!!, destination.value!!, rideRoute.value!!)
            }
        }
    }


    // Create Ride
    suspend fun createRide(currentLocation: LatLng) {
        val currentUser = FirebaseClient.firebaseAuth.currentUser

        if(currentUser != null) {
            val newCreatedRide = Ride(
                origin = origin.value!!,
                destination = destination.value!!,
                datetime = rideDateTime.value!!,
                driver = Driver(
                    userUid = currentUser.uid,
                    location = currentLocation,
                    vehicle = vehicle.value!!
                ),
                availableSeats = capacity.value!!,
                createdAt = Timestamp.now()
            )

            viewModelScope.launch(Dispatchers.Main) {
                rideRepository.createRide(newCreatedRide, object : RideRepository.CreateRideCallback {
                    override fun onCreateSuccess() {
                        setCreateRideStatus(Constants.UI_DATA_SUCCESS)
                    }

                    override fun onCreateFailure(error: Throwable) {
                        Log.e("Create Ride", error.message.toString())

                        setCreateRideStatus(Constants.UI_DATA_FAILED)
                    }
                })
            }
        } else {
            setCreateRideStatus(Constants.UI_DATA_FAILED)
        }
    }



    // HELPER METHODS
    fun resetData() {
        savedStateHandle[ORIGIN_KEY] = null
        savedStateHandle[DESTINATION_KEY] = null
        savedStateHandle[VEHICLE_KEY] = Vehicle()
        savedStateHandle[PASSENGER_CAPACITY_KEY] = 0
        savedStateHandle[RIDE_DATE_TIME_KEY] = Timestamp.now()
        savedStateHandle[RIDE_ROUTE_KEY] = null
        savedStateHandle[CREATE_RIDE_STATUS_KEY] = Constants.UI_DATA_LOADING
    }

    fun clearOrigin() {
        savedStateHandle[ORIGIN_KEY] = null

        clearRideRoute()
    }

    fun clearDestination() {
        savedStateHandle[DESTINATION_KEY] = null

        clearRideRoute()
    }

    fun clearRideRoute() {
        savedStateHandle[RIDE_ROUTE_KEY] = null
    }
}