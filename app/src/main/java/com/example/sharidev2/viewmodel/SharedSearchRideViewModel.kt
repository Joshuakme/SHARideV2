package com.example.sharidev2.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.sharidev2.data.model.Gender
import com.example.sharidev2.data.model.Ride
import com.example.sharidev2.data.model.SearchLocation
import com.example.sharidev2.data.model.SearchRide
import com.example.sharidev2.data.model.VehicleType
import com.example.sharidev2.data.state.SearchRideDetailConfigurationState
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import com.wdullaer.materialdatetimepicker.time.Timepoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

class SharedSearchRideViewModel(
    private val savedStateHandle: SavedStateHandle
): ViewModel() {
    // DATA KEY CONSTANT
    private val ORIGIN_KEY = "origin"
    private val DESTINATION_KEY = "destination"
    private val DRIVER_GENDER_KEY = "driver_gender"
    private val VEHICLE_TYPE_KEY = "vehicle_type"
    private val RIDE_DATE_KEY = "ride_date"
    private val RIDE_TIME_KEY = "ride_time"
    private val SEARCH_RIDE_KEY = "search_ride"



    // INTERNAL DATA MEMBERS
    // Origin Location
    val origin: LiveData<SearchLocation> = savedStateHandle.getLiveData(ORIGIN_KEY)

    // Destination Location
    val destination: LiveData<SearchLocation> = savedStateHandle.getLiveData(DESTINATION_KEY)

    // Driver's Gender
    val driverGender: LiveData<Gender> = savedStateHandle.getLiveData(DRIVER_GENDER_KEY)

    // Vehicle Type
    val vehicleType: LiveData<VehicleType> = savedStateHandle.getLiveData(VEHICLE_TYPE_KEY)

    // Ride Date
    val rideDate: LiveData<LocalDate> = savedStateHandle.getLiveData(RIDE_DATE_KEY)

    // Ride Time
    val rideTime: LiveData<LocalTime> = savedStateHandle.getLiveData(RIDE_TIME_KEY)

    // Seat Needed

    // Search Ride
    val searchRide: LiveData<SearchRide> = savedStateHandle.getLiveData(SEARCH_RIDE_KEY)


    // CONSTRUCTOR
    init {
        if(rideDate.value == null) {
            setRideDate(LocalDate.now())
        }

        // Check if rideTime is not assigned and assign the default value
        if (rideTime.value == null) {
            setRideTime(getDefaultRideTime())
        }
    }


    // SETTER in SavedStateHandle
    // Origin Location
    fun setOrigin(newOrigin: SearchLocation) {
        savedStateHandle[ORIGIN_KEY] = newOrigin
    }

    // Destination Location
    fun setDestination(newDestination: SearchLocation) {
        savedStateHandle[DESTINATION_KEY] = newDestination
        Log.e("TENGOK DESTINASI", newDestination.name)
    }

    // Driver's Gender
    fun setDriverGender(newDriverGender: Gender) {
        savedStateHandle[DRIVER_GENDER_KEY] = newDriverGender
    }

    // Vehicle Type
    fun setVehicleType(newVehicleType: VehicleType) {
        savedStateHandle[VEHICLE_TYPE_KEY] = newVehicleType
    }

    // Ride Date
    fun setRideDate(newRideDate: LocalDate) {
        savedStateHandle[RIDE_DATE_KEY] = newRideDate
    }

    // Ride Time
    fun setRideTime(newRideTime: LocalTime) {
        savedStateHandle[RIDE_TIME_KEY] = newRideTime
    }

    // Search Ride
    fun setSearchRide() {
        val newSearchRide = SearchRide(
                                origin.value!!,
                                destination.value!!,
                                rideDate.value!!,
                                rideTime.value!!,
                                driverGender.value,
                                vehicleType.value
                            )

        savedStateHandle[SEARCH_RIDE_KEY] = newSearchRide
    }


    // HELPER METHODS
    private fun getDefaultRideTime() : LocalTime {
        val calendar: Calendar = Calendar.getInstance()

        val currentTime = LocalTime.of(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))

        return roundUpToNearestInterval(currentTime, 5)
    }

    private fun roundUpToNearestInterval(currentTime: LocalTime, intervalMinutes: Int): LocalTime {
        val minuteOfHour = currentTime.minute
        val roundedMinute = ((minuteOfHour + intervalMinutes - 1) / intervalMinutes) * intervalMinutes
        val hourAdjustment = roundedMinute / 60
        val finalMinute = roundedMinute % 60
        val finalHour = (currentTime.hour + hourAdjustment) % 24
        return currentTime.withHour(finalHour).withMinute(finalMinute).withSecond(0).withNano(0)
    }

    // RESET
    fun resetData() {
        savedStateHandle.remove<SearchLocation>(ORIGIN_KEY)
        savedStateHandle.remove<SearchLocation>(DESTINATION_KEY)
        savedStateHandle.remove<Gender>(DRIVER_GENDER_KEY)
        savedStateHandle.remove<VehicleType>(VEHICLE_TYPE_KEY)
        savedStateHandle.remove<LocalDate>(RIDE_DATE_KEY)
        savedStateHandle.remove<LocalTime>(RIDE_TIME_KEY)
        savedStateHandle.remove<SearchRide>(SEARCH_RIDE_KEY)
    }
}