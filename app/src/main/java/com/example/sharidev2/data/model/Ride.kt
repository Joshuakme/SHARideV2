package com.example.sharidev2.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "ride_table")
data class Ride (
    val origin: SearchLocation,         // Maybe a Location class
    val destination: SearchLocation,    // Maybe a Location class
    val date: LocalDate,
    val time: LocalTime,
    val driver: User,           // Will be "User" class / "Driver" class
    val passengers: MutableList<User>,       // Will be "User" class / "Passenger" class
    val rideStatus: RideStatus,
    val driverStatus: DriverStatus,
    val passengersStatus: MutableList<PassengerStatus>,
    val startTime: LocalTime,
    val completeTime: LocalTime,
    val vehicle: Vehicle,
    val availableSeats: Int,
    val price: Double,
    val reviews: List<Review>,    // Will update to "Review" class
    val chat: Chat,       // Wil update to "Chat" class
    ) {

    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}