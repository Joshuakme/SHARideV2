package com.example.sharidev2.data.model

import com.google.firebase.auth.FirebaseUser
import java.time.LocalDate
import java.time.LocalTime

data class Ride (
    val id: Int? = null,
    val origin: SearchLocation,
    val destination: SearchLocation,
    val date: LocalDate,
    val time: LocalTime,
    val driver: FirebaseUser,
    val passengers: MutableList<Passenger>? = mutableListOf(),
    val rideStatus: RideStatus = RideStatus.CREATED,
    val driverStatus: UserStatus = UserStatus.REQUESTED,
    val passengersStatus: MutableList<PassengerStatus> = mutableListOf(),
    val startTime: LocalTime? = null,
    val completeTime: LocalTime? = null,
    val vehicle: Vehicle,
    val availableSeats: Int,
    val price: List<Map<Passenger, Double>>? = listOf(),
    val reviews: List<Review>? = emptyList(),
    val chat: Chat? = null,
)