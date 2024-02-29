package com.example.sharidev2.data.repository

import android.util.Log
import com.example.sharidev2.data.model.Chat
import com.example.sharidev2.data.model.Ride
import com.example.sharidev2.data.model.RideStatus
import com.example.sharidev2.data.model.UserStatus
import com.example.sharidev2.utility.Converters
import com.example.sharidev2.utility.FirebaseUtils
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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
                val newChat = createChat()

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
                        "chat" to newChat.chatId
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

    private suspend fun createChat(): Chat {
        return withContext(Dispatchers.IO) {
            try{
                val chatId = firestore.collection("chats").document().id

                val chat = Chat(chatId = chatId, members = listOf(currentUser!!.uid))

                firestore.collection("chats").document(chatId)
                    .set(chat)
                    .await()

                chat
            }
            catch (e: Exception) {
                Log.e("Create Chat", e.message.toString())
                Chat(members = listOf(currentUser?.uid ?: ""))
            }
        }
    }


    // RETRIEVE
    suspend fun getAllRides(): List<Ride> {
        return withContext(Dispatchers.IO) {
            try{
                val querySnapshot = firestore.collection("ride")
//                    .where(
//                        Filter.or(
//                            Filter.equalTo("driver", currentUser?.uid),
//                            Filter.inArray("passengers", passengers?.map { passenger -> mapOf("userUid" to passenger.userUid) } ?: emptyList())
//                        ))
                    .orderBy("datetime", Query.Direction.DESCENDING)
                    .get()
                    .await()

                // Destructure object retrieve from firebase and convert to List<Ride>
                val rideList = createRideListFromQuerySnapshot(querySnapshot)

                Log.e("Get All Rides", "All Rides: " + rideList.size.toString())

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
                        val startTime = if(get("startTime") != null) (get("startTime") as Timestamp) else null
                        val completeTime = if(get("completeTime") != null) (get("completeTime") as Timestamp) else null
                        val vehicle = firebaseUtils.getVehicleFromId(getString("vehicle") ?: "")
                        val availableSeats = (get("availableSeats") as Long).toInt()
                        val price = converters.toPrice(get("price") as Map<String, Long>)
                        val reviews = converters.toReviewList(get("reviews") as List<Map<String, Any>>)
                        val chat = firebaseUtils.getChatFromChatId(getString("chat") ?: "")
                        val createdAt = getTimestamp("createdAt")


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
                            chat,
                            createdAt
                        )

                        filteredRideList.add(ride)
                    }
                }
                filteredRideList.toList()
            } catch (e: Exception) {
                Log.e("Create Ride List From Query Snapshot", e.message.toString())

                emptyList()
            }
        }
    }

    interface CreateRideCallback {
        fun onCreateSuccess()
        fun onCreateFailure(error: Throwable)
    }
}