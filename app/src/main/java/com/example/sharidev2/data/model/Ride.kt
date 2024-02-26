package com.example.sharidev2.data.model

import com.google.firebase.Timestamp

data class Ride (
    val id: String? = null,
    val origin: SearchLocation,
    val destination: SearchLocation,
    val datetime: Timestamp,
    val driver: User,
    val passengers: MutableList<Passenger>? = mutableListOf(),
    val rideStatus: RideStatus = RideStatus.CREATED,
    val driverStatus: UserStatus = UserStatus.REQUESTED,
    val passengersStatus: MutableList<UserStatus> = mutableListOf(),
    val startTime: Timestamp? = null,
    val completeTime: Timestamp? = null,
    val vehicle: Vehicle,
    val availableSeats: Int,
    val price: List<Map<String, Double>>? = listOf(),   // list of Map<userId, price>
    val reviews: List<Review>? = emptyList(),
    val chat: Chat? = null,
)