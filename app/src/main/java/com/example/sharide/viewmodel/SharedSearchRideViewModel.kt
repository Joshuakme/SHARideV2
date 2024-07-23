package com.example.sharide.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.sharide.data.model.Gender
import com.example.sharide.data.model.Passenger
import com.example.sharide.data.model.Ride
import com.example.sharide.data.model.RideOption
import com.example.sharide.data.model.SearchLocation
import com.example.sharide.data.model.SearchRide
import com.example.sharide.data.model.VehicleType
import com.example.sharide.data.repository.RideRepository
import com.example.sharide.utility.FirebaseClient
import com.example.sharide.utility.RideUtils
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.util.Calendar

class SharedSearchRideViewModel(
    private val savedStateHandle: SavedStateHandle
): ViewModel() {
    // Repository
    val currentLocationViewModel = CurrentLocationViewModel()
    val rideRepository = RideRepository()

    // DATA KEY CONSTANT
    private val ORIGIN_KEY = "origin"
    private val DESTINATION_KEY = "destination"
    private val DRIVER_GENDER_KEY = "driver_gender"
    private val VEHICLE_TYPE_KEY = "vehicle_type"
    private val RIDE_DATE_TIME_KEY = "ride_date_time"
    private val SEARCH_RIDE_KEY = "search_ride"
    private val RIDE_ROUTE_KEY = "ride_route"



    // INTERNAL DATA MEMBERS
    // Origin Location
    val origin: LiveData<SearchLocation?> = savedStateHandle.getLiveData(ORIGIN_KEY)

    // Destination Location
    val destination: LiveData<SearchLocation?> = savedStateHandle.getLiveData(DESTINATION_KEY)

    // Driver's Gender
    val driverGender: LiveData<Gender> = savedStateHandle.getLiveData(DRIVER_GENDER_KEY)

    // Vehicle Type
    val vehicleType: LiveData<VehicleType> = savedStateHandle.getLiveData(VEHICLE_TYPE_KEY)

    // Ride Date Time
    val rideDateTime: LiveData<Timestamp> = savedStateHandle.getLiveData(RIDE_DATE_TIME_KEY)

    // Ride Route Time
    val rideRoute: LiveData<MutableList<LatLng>?> = savedStateHandle.getLiveData(RIDE_ROUTE_KEY)


    // Seat Needed

    // Search Ride
    val searchRide: LiveData<SearchRide> = savedStateHandle.getLiveData(SEARCH_RIDE_KEY)


    // CONSTRUCTOR
    init {
        if(rideDateTime.value == null) {
            setRideDateTime(Timestamp.now())
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
    fun setDriverGender(newDriverGender: Gender) {
        savedStateHandle[DRIVER_GENDER_KEY] = newDriverGender
    }

    // Vehicle Type
    fun setVehicleType(newVehicleType: VehicleType) {
        savedStateHandle[VEHICLE_TYPE_KEY] = newVehicleType
    }

    // Ride Date Time
    fun setRideDateTime(newRideDateTime: Timestamp) {
        savedStateHandle[RIDE_DATE_TIME_KEY] = newRideDateTime
    }

    // Ride Route
    fun setRideRoute(newRideRoute: MutableList<LatLng>) {
        savedStateHandle[RIDE_ROUTE_KEY] = newRideRoute

        saveRoutePath()
    }

    // Save Ride Route
    private fun saveRoutePath() {
        if(origin.value != null && destination.value != null && rideRoute.value != null) {
            viewModelScope.launch(Dispatchers.IO) {
                rideRepository.addRoutePath(origin.value!!, destination.value!!, rideRoute.value!!)
            }
        }
    }

    // Search Ride
    fun setSearchRide() {
        val newSearchRide = SearchRide(
                                origin.value!!,
                                destination.value!!,
                                rideDateTime.value!!,
                                RideOption(
                                    driverGender.value,
                                    vehicleType.value
                                )
                            )

        savedStateHandle[SEARCH_RIDE_KEY] = newSearchRide
    }

    fun searchRide(): LiveData<List<Ride>> {
        return if (FirebaseClient.firebaseAuth.currentUser?.uid != null && searchRide.value != null) {
            liveData(viewModelScope.coroutineContext) {
                val passenger = Passenger(
                    userUid = FirebaseClient.firebaseAuth.currentUser!!.uid,
                    user = FirebaseClient.getCurrentUser(),
                    location = currentLocationViewModel.currentLocation.value
                )

                val availableRideList = rideRepository.getAvailableRideList()

                val matchedRideList = RideUtils().filterRideByPassenger(availableRideList, searchRide.value!!)
//                val matchedRideList = availableRideList.filter { ride ->
//                    RideUtils().matchRidePassenger(ride, searchRide.value!!, passenger) != null
//                }

                emit(matchedRideList)
            }
        } else {
            MutableLiveData<List<Ride>>().apply { value = emptyList() }
        }
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



    // RESET DATA
    fun clearOrigin() {
        savedStateHandle[ORIGIN_KEY] = null

        clearRideRoute()
    }

    fun clearDestination() {
        savedStateHandle[DESTINATION_KEY] = null

        clearRideRoute()
    }
    private fun clearRideRoute() {
        savedStateHandle[RIDE_ROUTE_KEY] = null
    }
}