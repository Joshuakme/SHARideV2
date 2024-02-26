package com.example.sharidev2.data.repository

import android.net.Uri
import android.util.Log
import com.example.sharidev2.data.model.Chat
import com.example.sharidev2.data.model.Passenger
import com.example.sharidev2.data.model.Review
import com.example.sharidev2.data.model.Ride
import com.example.sharidev2.data.model.RideStatus
import com.example.sharidev2.data.model.SearchLocation
import com.example.sharidev2.data.model.User
import com.example.sharidev2.data.model.UserStatus
import com.example.sharidev2.data.model.Vehicle
import com.example.sharidev2.data.model.VehicleType
import com.example.sharidev2.utility.Converters
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class RideRepository(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {
    private val currentUser = firebaseAuth.currentUser

    suspend fun createRide(ride: Ride, callback: CreateRideCallback) {
        return withContext(Dispatchers.IO) {
            try {
                if (currentUser != null) {
                    val newRide = hashMapOf(
                        "origin" to ride.origin,
                        "destination" to ride.destination,
                        "datetime" to ride.datetime,
                        "driver" to currentUser.uid,
                        "passengers" to ride.passengers,
                        "rideStatus" to ride.rideStatus,
                        "driverStatus" to ride.driverStatus,
                        "passengersStatus" to ride.passengersStatus,
                        "startTime" to ride.startTime,
                        "completeTime" to ride.completeTime,
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
                    Log.e("HAIYAA", "User is not logged in yet")
                }
            } catch(e: Exception) {
                Log.e("KENAPA??", e.message.toString())
            }
        }
    }


    // RETRIEVE
    suspend fun getAllRides(): List<Ride> {
        return withContext(Dispatchers.Main) {
            try{
                val querySnapshot = firestore.collection("ride")
                    .orderBy("datetime")
                    .get()
                    .await()

                // Destructure object retrieve from firebase and convert to List<Ride>
                val rideList = createRideListFromQuerySnapshot(querySnapshot)
                //emptyList()
                Log.e("Get All Rides", querySnapshot.size().toString())

                rideList
            } catch (e: Exception) {
                Log.e("Get All Rides", e.message.toString())
                emptyList()
            }
        }
    }

    suspend fun getFilterDriverRideList(date: Timestamp): List<Ride> {
        return withContext(Dispatchers.Main) {
            try {
                if(currentUser != null) {
                    val querySnapshot = firestore.collection("ride")
                        .whereEqualTo("date", date)
                        .whereNotEqualTo("driver", currentUser.uid)
                        .whereGreaterThanOrEqualTo("availableSeats", 1)
                        .get()
                        .await()

                    //val filteredRideList = createRideListFromQuerySnapshot(querySnapshot)
                    val filteredRideList = emptyList<Ride>()

                    filteredRideList
                } else {
                    // User not logged in yet
                    emptyList()
                }
            } catch (e: Exception) {
                Log.e("Filter Driver Ride", e.message.toString())
                emptyList()
            }
        }
    }

    suspend fun getVehicleFromId(vehicleId: String): Vehicle {
        val vehicle = firestore.collection("vehicle")
            .document(vehicleId)
            .get()
            .await()

            vehicle.apply {
                val brand = getString("brand")
                val model = getString("model")
                val vehicleType = VehicleType.valueOf(getString("type") ?: "")
                val plateNumber = getString("plateNumber")
                val color = getString("color")

                val photosString = get("photos") as List<String>
                val photos = mutableListOf<Uri>()
                for (photo in photosString) {
                    photos.add(Uri.parse(photo))
                }

                val capacity = get("capacity") as Int

                return Vehicle(
                    vehicleId,
                    brand,
                    model,
                    vehicleType,
                    plateNumber,
                    color,
                    photos,
                    capacity
                )
            }
    }


    // HELPER METHODS
    private fun createRideListFromQuerySnapshot(querySnapshot: QuerySnapshot): List<Ride> {
        val filteredRideList = mutableListOf<Ride>()
        val converters = Converters()

        for(document in querySnapshot.documents) {
            document.apply {
                val origin = converters.toSearchLocation(get("origin") as Map<String, Any>)
                val destination = converters.toSearchLocation(get("destination") as Map<String, Any>)
                val datetime = getTimestamp("datetime") ?: Timestamp.now()
                val driver = converters.toUser(get("driver") as Map<String, Any>)
//                val passengers = converters.toPassengerList(get("passengers") as List<Map<String, Any>>).toMutableList()
//                val rideStatus = RideStatus.valueOf(getString("rideStatus") ?: "")
//                val driverStatus = UserStatus.valueOf(getString("driverStatus") ?: "")
//                val passengersStatus = (get("passengersStatus") as MutableList<UserStatus>)
//                val startTime = getTimestamp("startTime")
//                val completeTime = getTimestamp("completeTime")
//                val vehicle = converters.toVehicle(getString("vehicle") ?: "")
//                val availableSeats = get("availableSeats") as Int
//                val price = get("price") as List<Map<String, Double>>
//                val reviews = get("reviews") as List<Review>
//                val chat = null
//
//
//                val ride = Ride(
//                    this.id,
//                    origin,
//                    destination,
//                    datetime,
//                    driver,
//                    passengers,
//                    rideStatus,
//                    driverStatus,
//                    passengersStatus,
//                    startTime,
//                    completeTime,
//                    vehicle,
//                    availableSeats,
//                    price,
//                    reviews,
//                    chat
//                )
//
//                filteredRideList.add(ride)
            }
        }

        return filteredRideList
    }

    interface CreateRideCallback {
        fun onCreateSuccess()
        fun onCreateFailure(error: Throwable)
    }
}