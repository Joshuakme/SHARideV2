package com.example.sharidev2.model

import java.time.LocalDate
import java.time.LocalTime

data class Ride (
    val rideID: String,
    val origin: String,         // Maybe a Location class
    val destination: String,    // Maybe a Location class
    val date: LocalDate,
    val time: LocalTime,
    val driver: User,     // Will be "User" class / "Driver" class
    val passengers: User, // Will be "User" class / "Passenger" class
    val rideStatus: RideStatus,
    val driverStatus: DriverStatus,
    val passengersStatus: List<PassengerStatus>,
    val startTime: LocalTime,
    val completeTime: LocalTime,
    val availableSeats: Int,
    val price: Double,
    val reviews: Review,    // Will update to "Review" class
    val chat: Chat,       // Wil update to "Chat" class
    ) {


}