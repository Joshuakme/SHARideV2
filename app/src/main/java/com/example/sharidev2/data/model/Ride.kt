package com.example.sharidev2.data.model

import com.google.firebase.Timestamp

data class Ride (
    val id: String? = null,
    val origin: SearchLocation,
    val destination: SearchLocation,
    val datetime: Timestamp,
    val driver: Driver,
    val passengers: Map<String, Passenger>? = mapOf(),
    val rideStatus: RideStatus = RideStatus.CREATED,
    val startTime: Timestamp? = null,
    val completeTime: Timestamp? = null,
    val availableSeats: Int,
    val reviews: List<Review>? = emptyList(),
    val chat: Chat? = Chat(),
    val createdAt: Timestamp?
)