package com.example.sharidev2.data.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue


data class Ride (
    val id: String? = null,
    val origin: SearchLocation = SearchLocation(),
    val destination: SearchLocation = SearchLocation(),
    val datetime: Timestamp = Timestamp.now(),
    val driver: Driver = Driver(),
    val passengers: Map<String, Passenger> = mapOf(),
    val rideStatus: RideStatus = RideStatus.CREATED,
    val startTime: Timestamp? = null,
    val completeTime: Timestamp? = null,
    val availableSeats: Int = 0,
    val reviews: Map<String, Review> = mapOf(),
    val chat:  Chat? = Chat(),
    val createdAt: Timestamp? = null
)