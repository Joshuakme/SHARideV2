package com.example.sharidev2.data.model

enum class RideStatus {
    CREATED,        // The initial status when a driver creates a ride.
    AVAILABLE,     // The status when the ride available seats.
    IN_PROGRESS,    // The status indicating that the ride is currently ongoing.
    COMPLETED,      // The status when the ride has been successfully completed.
    CANCELED;       // The status when either the rider or the driver cancels the ride.


    // Optionally, you can add a method to check if the ride is ongoing
    fun isInProgress(): Boolean {
        return this == IN_PROGRESS
    }
}
