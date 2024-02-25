package com.example.sharidev2.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharidev2.data.model.Ride
import com.example.sharidev2.data.model.SearchLocation
import com.example.sharidev2.data.model.User
import com.example.sharidev2.data.model.Vehicle
import com.example.sharidev2.data.repository.RideRepository
import com.example.sharidev2.firebase.FirebaseInitializer
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.util.Calendar

class SharedCreateRideViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    // Repository
    private val rideRepository = RideRepository(
        FirebaseInitializer.firestore,
        FirebaseInitializer.firebaseAuth
    )

    // DATA KEY CONSTANT
    private val ORIGIN_KEY = "origin"
    private val DESTINATION_KEY = "destination"
    private val DRIVER_KEY = "driver"
    private val VEHICLE_KEY = "vehicle"
    private val PASSENGER_CAPACITY_KEY = "passenger_capacity"
    private val RIDE_DATE_KEY = "ride_date"
    private val RIDE_TIME_KEY = "ride_time"
    private val CREATE_RIDE_STATUS_KEY = "create_ride_status"


    // INTERNAL DATA MEMBERS
    // Origin Location
    val origin: LiveData<SearchLocation> = savedStateHandle.getLiveData(ORIGIN_KEY)

    // Destination Location
    val destination: LiveData<SearchLocation> = savedStateHandle.getLiveData(DESTINATION_KEY)

    // Driver
    val driver: LiveData<User> = savedStateHandle.getLiveData(DRIVER_KEY)

    // Vehicle
    val vehicle: LiveData<Vehicle> = savedStateHandle.getLiveData(VEHICLE_KEY)

    // Passenger Capacity
    val capacity: LiveData<Int> = savedStateHandle.getLiveData(PASSENGER_CAPACITY_KEY)

    // Ride Date
    val rideDate: LiveData<LocalDate> = savedStateHandle.getLiveData(RIDE_DATE_KEY)

    // Ride Time
    val rideTime: LiveData<LocalTime> = savedStateHandle.getLiveData(RIDE_TIME_KEY)

    // Create Ride Status
    val createRideStatus: LiveData<Boolean> = savedStateHandle.getLiveData(CREATE_RIDE_STATUS_KEY)


    // CONSTRUCTOR
    init {
        if (rideDate.value == null) {
            setRideDate(LocalDate.now())
        }

        // Check if rideTime is not assigned and assign the default value
        if (rideTime.value == null) {
            setRideTime(getDefaultRideTime())
        }

        setCreateRideStatus(false)
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

    // Driver
    fun setDriver(newDriver: User) {
        savedStateHandle[DRIVER_KEY] = newDriver
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

    // Ride Date
    fun setRideDate(newRideDate: LocalDate) {
        savedStateHandle[RIDE_DATE_KEY] = newRideDate
    }

    // Ride Time
    fun setRideTime(newRideTime: LocalTime) {
        savedStateHandle[RIDE_TIME_KEY] = newRideTime
    }

    // Create Ride Status
    fun setCreateRideStatus(success: Boolean) {
        savedStateHandle[CREATE_RIDE_STATUS_KEY] = success
    }


    // Create Ride
    suspend fun createRide() {
        val newCreatedRide = Ride(
            origin = origin.value!!,
            destination = destination.value!!,
            date = rideDate.value!!,
            time = rideTime.value!!,
            driver = FirebaseInitializer.firebaseAuth.currentUser!!,
            vehicle = vehicle.value!!,
            availableSeats = capacity.value!!
        )

        viewModelScope.launch(Dispatchers.Main) {
            rideRepository.createRide(newCreatedRide, object : RideRepository.CreateRideCallback {
                override fun onCreateSuccess() {
                    setCreateRideStatus(true)
                }

                override fun onCreateFailure(error: Throwable) {
                    Log.e("Create Ride", error.message.toString())

                    setCreateRideStatus(false)
                }
            })
        }
    }


    // HELPER METHODS
    private fun getDefaultRideTime(): LocalTime {
        val calendar: Calendar = Calendar.getInstance()

        val currentTime =
            LocalTime.of(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))

        return roundUpToNearestInterval(currentTime, 5)
    }

    private fun roundUpToNearestInterval(currentTime: LocalTime, intervalMinutes: Int): LocalTime {
        val minuteOfHour = currentTime.minute
        val roundedMinute =
            ((minuteOfHour + intervalMinutes - 1) / intervalMinutes) * intervalMinutes
        val hourAdjustment = roundedMinute / 60
        val finalMinute = roundedMinute % 60
        val finalHour = (currentTime.hour + hourAdjustment) % 24
        return currentTime.withHour(finalHour).withMinute(finalMinute).withSecond(0).withNano(0)
    }
}