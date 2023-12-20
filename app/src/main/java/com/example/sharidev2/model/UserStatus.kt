package com.example.sharidev2.model

enum class UserStatus {
    REQUESTED,      // The initial status when a user requests a ride. The system is looking for an available driver / passenger.
    ACCEPTED,       // The status when a driver accepts the ride request.
    REJECTED,       // The status when a driver rejects a ride request.
    WAITING,        // The status when a passenger is waiting for vehicle to arrive.
    IN_VEHICLE,     // The status when the user is in the vehicle.
    COMPLETED,      // The status when the ride is successfully completed.
    CANCELED,
}