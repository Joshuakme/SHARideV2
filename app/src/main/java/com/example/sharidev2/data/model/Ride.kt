package com.example.sharidev2.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

data class Ride (
    val id: Int? = null,
    val origin: SearchLocation,
    val destination: SearchLocation,
    val date: LocalDate,
    val time: LocalTime,
    val driver: User,           // Will be "User" class / "Driver" class
    val passengers: MutableList<Passenger>,
    val rideStatus: RideStatus,
    val driverStatus: DriverStatus,
    val passengersStatus: MutableList<PassengerStatus>,
    val startTime: LocalTime,
    val completeTime: LocalTime,
    val vehicle: Vehicle,
    val availableSeats: Int,
    val price: Double,
    val reviews: List<Review>,
    val chat: Chat,
)