package com.example.sharidev2.data.repository

import android.util.Log
import com.example.sharidev2.data.model.Ride
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RideRepository(
    private val firestore: FirebaseFirestore,
    firebaseAuth: FirebaseAuth
) {
    private val currentUser = firebaseAuth.currentUser

    suspend fun createRide(ride: Ride, callback: CreateRideCallback) {
        return withContext(Dispatchers.IO) {
            try {
                if (currentUser != null) {
                    val newRide = hashMapOf(
                        "origin" to ride.origin,
                        "destination" to ride.destination,
                        "date" to ride.date.toEpochDay(),
                        "time" to ride.time.toNanoOfDay() / 1_000_000,
                        "driver" to currentUser.uid,
                        "passengers" to ride.passengers,
                        "rideStatus" to ride.rideStatus,
                        "driverStatus" to ride.driverStatus,
                        "passengersStatus" to ride.passengersStatus,
                        "startTime" to if(ride.startTime != null) (ride.startTime.toNanoOfDay() / 1_000_000) else null,
                        "completeTime" to if(ride.completeTime != null) (ride.completeTime.toNanoOfDay() / 1_000_000) else null,
                        "vehicle" to ride.vehicle.vehicleID,
                        "availableSeats" to ride.availableSeats,
                        "price" to ride.price,
                        "reviews" to ride.reviews,
                        "chat" to ride.chat
                    )

                    firestore.collection("ride").document().set(newRide)
                        .addOnSuccessListener {
                            // Write was successful!
                            callback.onCreateSuccess()
                        }
                        .addOnFailureListener { e ->
                            // Write failed
                            callback.onCreateFailure(e)
                        }
                } else {
                    // User not logged in yet
                }
            } catch(e: Exception) {
                Log.e("KENAPA??", e.message.toString())
                Log.e("KENAPA??", e.localizedMessage)
            }
        }
    }

    interface CreateRideCallback {
        fun onCreateSuccess()
        fun onCreateFailure(error: Throwable)
    }
}