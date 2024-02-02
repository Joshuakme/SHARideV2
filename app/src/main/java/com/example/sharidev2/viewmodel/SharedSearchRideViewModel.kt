package com.example.sharidev2.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.sharidev2.data.model.SearchLocation
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



    // INTERNAL DATA MEMBERS
    // Origin Location
    val origin: LiveData<SearchLocation> = savedStateHandle.getLiveData(ORIGIN_KEY)

    // Destination Location
    val destination: LiveData<SearchLocation> = savedStateHandle.getLiveData(DESTINATION_KEY)

    // Driver's Gender
    val driverGender: LiveData<String> = savedStateHandle.getLiveData(DRIVER_GENDER_KEY)

    // Vehicle Type
    val vehicleType: LiveData<VehicleType> = savedStateHandle.getLiveData(VEHICLE_TYPE_KEY)

    // Ride Date
    val rideDate: LiveData<LocalDate> = savedStateHandle.getLiveData(RIDE_DATE_KEY)

    // Ride Time
    val rideTime: LiveData<LocalTime> = savedStateHandle.getLiveData(RIDE_TIME_KEY)


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
    }

    // Driver's Gender
    fun setDriverGender(newDriverGender: String) {
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


    // HELPER METHODS
    private fun getDefaultRideTime() : LocalTime {
        val calendar: Calendar = Calendar.getInstance()

        val currentTime = LocalTime.of(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))

        return roundUpToNearestInterval(currentTime, 5)
    }

    private fun roundUpToNearestInterval(currentTime: LocalTime, intervalMinutes: Int): LocalTime {
        val minuteOfHour = currentTime.minute
        val roundedMinute = ((minuteOfHour + intervalMinutes - 1) / intervalMinutes) * intervalMinutes
        return currentTime.withMinute(roundedMinute).withSecond(0).withNano(0)
    }
}