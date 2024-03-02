package com.example.sharidev2.data.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class Ride (
    val id: String? = null,
    val origin: SearchLocation,
    val destination: SearchLocation,
    val datetime: Timestamp,
    val driver:  @RawValue Driver,
    val passengers:  @RawValue Map<String, Passenger>? = mapOf(),
    val rideStatus: RideStatus = RideStatus.CREATED,
    val startTime: Timestamp? = null,
    val completeTime: Timestamp? = null,
    val availableSeats: Int,
    val reviews:  @RawValue List<Review>? = emptyList(),
    val chat:  @RawValue Chat? = Chat(),
    val createdAt: Timestamp?
) : Parcelable