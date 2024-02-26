package com.example.sharidev2.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharidev2.data.model.Ride
import com.example.sharidev2.data.model.SearchLocation
import com.example.sharidev2.data.repository.RideRepository
import com.example.sharidev2.firebase.FirebaseInitializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

class RideViewModel(
    private val savedStateHandle: SavedStateHandle
): ViewModel() {
    private val rideRepository = RideRepository(FirebaseInitializer.firestore, FirebaseInitializer.firebaseAuth)

    // DATA KEY CONSTANT
    private val RIDE_LIST_KEY = "ride_list"


    // INTERNAL DATA MEMBERS
    // Ride List Location
    val rideList: LiveData<List<Ride>> = savedStateHandle.getLiveData(RIDE_LIST_KEY)

    init {
        viewModelScope.launch(Dispatchers.Main) {
            setRideList(rideRepository.getAllRides())
        }
    }

    // SETTER in SavedStateHandle
    // Origin Location
    fun setRideList(newRideList: List<Ride>) {
        savedStateHandle[RIDE_LIST_KEY] = newRideList
    }
}