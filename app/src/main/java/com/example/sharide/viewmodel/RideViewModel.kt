package com.example.sharide.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharide.data.model.Passenger
import com.example.sharide.data.model.Ride
import com.example.sharide.data.repository.RideRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RideViewModel(
    private val savedStateHandle: SavedStateHandle
): ViewModel() {
    private val rideRepository = RideRepository()

    // DATA KEY CONSTANT
    private val RIDE_LIST_KEY = "ride_list"
    private val FILTER_RIDE_LIST_KEY = "filter_ride_list"
    private val ACTIVE_RIDE_LIST_KEY = "active_ride_list"
    private val PAST_RIDE_LIST_KEY = "past_ride_list"


    // INTERNAL DATA MEMBERS
    // Ride List Location
    val rideList: LiveData<List<Ride>> = savedStateHandle.getLiveData(RIDE_LIST_KEY)
    val filterRideList: LiveData<List<Ride>> = savedStateHandle.getLiveData(FILTER_RIDE_LIST_KEY)
    val activeRideList: LiveData<List<Ride>> = savedStateHandle.getLiveData(ACTIVE_RIDE_LIST_KEY)
    val pastRideList: LiveData<List<Ride>> = savedStateHandle.getLiveData(PAST_RIDE_LIST_KEY)

    init {
        viewModelScope.launch(Dispatchers.Main) {
            setFilterRideList(rideRepository.getAvailableRideList())


            startListeningForRideListUpdates { updatedRideHashMap ->
                Log.e("RideViewModel: init", "ride size: " + updatedRideHashMap.size)
                val rideList = mutableListOf<Ride>()

                updatedRideHashMap.forEach { mapEntry ->
                    rideList.add(mapEntry.value)
                }
                setRideList(rideList)
            }
        }
    }

    // SETTER in SavedStateHandle
    private fun setRideList(newRideList: List<Ride>) {
        val sortedNewRideList = newRideList.sortedBy { it.datetime }.reversed()

        savedStateHandle[RIDE_LIST_KEY] = sortedNewRideList

        setActiveRideList(filterRideList(sortedNewRideList, FilterType.ACTIVE))
        setPastRideList(filterRideList(sortedNewRideList, FilterType.PAST))
    }

    fun setFilterRideList(newFilterRideList: List<Ride>) {
        savedStateHandle[FILTER_RIDE_LIST_KEY] = newFilterRideList
    }

    private fun setActiveRideList(newRideList: List<Ride>) {
        savedStateHandle[ACTIVE_RIDE_LIST_KEY] = newRideList
    }

    fun setPastRideList(newRideList: List<Ride>) {
        savedStateHandle[PAST_RIDE_LIST_KEY] = newRideList
    }

    suspend fun addPassengerToRide(passenger: Passenger, rideId: String): Int {
        return rideRepository.addPassengerToRide(passenger, rideId)
    }

//    private fun getRides(filterType: FilterType): List<Ride> {
//        val currentTimestamp = Timestamp.now()
//
//        return rideList.value?.filter { ride ->
//            when (filterType) {
//                FilterType.ACTIVE -> ride.datetime > currentTimestamp
//                FilterType.PAST -> ride.datetime < currentTimestamp
//            }
//        } ?: emptyList()
//    }

    private fun startListeningForRideListUpdates(onRideListUpdate: (HashMap<String,Ride>) -> Unit) {
        rideRepository.listenForAllRidesChanges(onRideListUpdate)
    }

    private fun filterRideList(rideList: List<Ride>, filterType: FilterType): List<Ride> {
        val currentTimestamp = Timestamp.now()

        return rideList.filter { ride ->
            when (filterType) {
                FilterType.ACTIVE -> ride.datetime > currentTimestamp
                FilterType.PAST -> ride.datetime < currentTimestamp
            }
        }
    }

    enum class FilterType {
        ACTIVE,
        PAST
    }
}