package com.example.sharidev2.data.repository

import android.util.Log
import com.example.sharidev2.data.model.Ride
import com.example.sharidev2.data.model.RideStatus
import com.example.sharidev2.data.model.UserStatus
import com.example.sharidev2.utility.Converters
import com.example.sharidev2.utility.FirebaseUtils
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class RideRepository(
    private val firestore: FirebaseFirestore,
    firebaseAuth: FirebaseAuth,
    private val firebaseUtils: FirebaseUtils
) {
    private val currentUser = firebaseAuth.currentUser
    private val converters = Converters()

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
        return withContext(Dispatchers.IO) {
            try{
                val querySnapshot = firestore.collection("ride")
                    .orderBy("datetime")
                    .get()
                    .await()

                // Destructure object retrieve from firebase and convert to List<Ride>
                val rideList = createRideListFromQuerySnapshot(querySnapshot)
                //emptyList()
                Log.e("Get All Rides", "All Rides: " + querySnapshot.size().toString())
                Log.e("Get All Rides", "Ride List: " + rideList.size.toString())

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




    // HELPER METHODS
    private suspend fun createRideListFromQuerySnapshot(querySnapshot: QuerySnapshot): List<Ride> {
        val filteredRideList = mutableListOf<Ride>()

        return withContext(Dispatchers.IO) {
            try {
                for (document in querySnapshot.documents) {
                    document.apply {
                        val origin = converters.toSearchLocation(get("origin") as Map<String, Any>)
                        val destination = converters.toSearchLocation(get("destination") as Map<String, Any>)
                        val datetime = getTimestamp("datetime") ?: Timestamp.now()
                        val driver = firebaseUtils.getUserFromUid(getString("driver") ?: "")
                        val passengers = converters.toPassengerList(get("passengers") as List<Map<String, Any>>).toMutableList()
                        val rideStatus = RideStatus.valueOf(getString("rideStatus") ?: "")
                        val driverStatus = UserStatus.valueOf(getString("driverStatus") ?: "")
                        val passengersStatus = converters.toPassengersStatus(get("passengersStatus") as Map<String, String>)
                        val startTime = getTimestamp("startTime")
                        val completeTime = getTimestamp("completeTime")
                        val vehicle = firebaseUtils.getVehicleFromId(getString("vehicle") ?: "")
                        val availableSeats = (get("availableSeats") as Long).toInt()
                        val price = get("price") as Map<String, Double>
                        val reviews = converters.toReviewList(get("reviews") as List<Map<String, Any>>)
                        val chat = firebaseUtils.getChatFromChatId(getString("chat") ?: "")


                        val ride = Ride(
                            this.id,
                            origin,
                            destination,
                            datetime,
                            driver,
                            passengers,
                            rideStatus,
                            driverStatus,
                            passengersStatus,
                            startTime,
                            completeTime,
                            vehicle,
                            availableSeats,
                            price,
                            reviews,
                            chat
                        )

                        filteredRideList.add(ride)
                    }
                }
                filteredRideList.toList()
            } catch (e: Exception) {
                Log.e("Create Ride List From Query Snapshot", e.message.toString())

                emptyList<Ride>()
            }
        }
    }

    interface CreateRideCallback {
        fun onCreateSuccess()
        fun onCreateFailure(error: Throwable)
    }
}