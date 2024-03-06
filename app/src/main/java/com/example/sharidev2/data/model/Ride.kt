package com.example.sharidev2.data.model

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class Ride (
    val id: String? = null,
    val origin: SearchLocation = SearchLocation(),
    val destination: SearchLocation = SearchLocation(),
    val waypoints: Map<String, SearchLocation>? = mapOf(),
    val datetime: Timestamp = Timestamp.now(),
    val driver: @RawValue Driver = Driver(),
    val passengers: @RawValue Map<String, Passenger> = mapOf(),
    val rideStatus: RideStatus = RideStatus.CREATED,
    val startTime: Timestamp? = null,
    val completeTime: Timestamp? = null,
    val availableSeats: Int = 0,
    val reviews: @RawValue Map<String, Review> = mapOf(),
    val chat:  @RawValue Chat? = Chat(),
    val createdAt: Timestamp? = null
) : Parcelable
