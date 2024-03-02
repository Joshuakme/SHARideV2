package com.example.sharidev2.data.repository

import android.util.Log
import com.example.sharidev2.data.model.Chat
import com.example.sharidev2.data.model.Passenger
import com.example.sharidev2.data.model.Review
import com.example.sharidev2.data.model.Ride
import com.example.sharidev2.data.model.RideStatus
import com.example.sharidev2.data.model.User
import com.example.sharidev2.utility.Constants
import com.example.sharidev2.utility.Converters
import com.example.sharidev2.utility.FirebaseClient
import com.example.sharidev2.utility.UserClient
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class RideRepository() {
    // Firebase Instances
    private val firestore = FirebaseClient.firestore

    private val currentUser = UserClient.currentUser()
    private val converters = Converters()

    private val rideCollectionRef = firestore.collection("ride")

    suspend fun createRide(ride: Ride, callback: CreateRideCallback) {
        return withContext(Dispatchers.IO) {
            try {
                val newChat = createChat()

                if (currentUser != null) {
                    val newRide = hashMapOf(
                        "origin" to ride.origin,
                        "destination" to ride.destination,
                        "datetime" to ride.datetime,
                        "driver" to ride.driver,
                        "passengers" to ride.passengers,
                        "rideStatus" to ride.rideStatus,
                        "startTime" to ride.startTime,
                        "completeTime" to ride.completeTime,
                        "availableSeats" to ride.availableSeats,
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
                val chatId = firestore.collection("chat").document().id

                val chat = Chat(chatId = chatId, members = listOf(currentUser?.uid!!))

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

    suspend fun addPassenger(newPassenger: Passenger, rideId: String): Int {
        return withContext(Dispatchers.IO) {
            try {
                val rideRef = firestore.collection("ride").document(rideId)

                // Update the document with the new passenger
                rideRef.update("passengers.${newPassenger.userUid}", newPassenger).await()

//                // Get the current passengers count
//                val rideDoc = rideRef.get().await()
//                val passengersCount = (rideDoc.get("passengers") as? Map<*, *>)?.size ?: 0
//
//                // Get the vehicle capacity
//                val vehicleCapacity = (rideDoc.getLong("vehicle.capacity")?.minus(1)) ?: 0
//
//                // Calculate the available seats
//                val availableSeats = vehicleCapacity - passengersCount
//
//                // Update the available seats count
//                rideRef.update("availableSeats", availableSeats).await()

                Constants.FIREBASE_REQUEST_SUCCESS
            } catch (e: Exception) {
                Log.e("Add Passenger", e.message.toString())
                Constants.FIREBASE_REQUEST_EXCEPTION
            }
        }
    }



    // RETRIEVE METHODS
    suspend fun getAllRides(): List<Ride> {
        return withContext(Dispatchers.IO) {
            try{
                val querySnapshot = rideCollectionRef
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


    // UPDATE FUNCTIONS
    suspend fun updatePassenger(newPassenger: Passenger) {

    }

    suspend fun updateRideAvailableSeats(newAvailableSeats: Int, ) {

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

                        val driverUid = (get("driver") as Map<String, Any>)["userUid"] as String
                        val driverUser = FirebaseClient.getUserFromUid(driverUid)
                        val driver = converters.toDriver(get("driver") as Map<String, Any>, driverUser)

                        val passengerUserList = mutableListOf<User>()
                        val passengerMap = get("passengers") as Map<String, Any>
                        for(field in passengerMap) {
                            passengerUserList.add(FirebaseClient.getUserFromUid(field.key))
                        }
                        val passengers = converters.toPassengers(get("passengers") as Map<String, Any>, passengerUserList)


                        val rideStatus = RideStatus.valueOf(getString("rideStatus") ?: "")
                        val startTime = if(get("startTime") != null) (get("startTime") as Timestamp) else null
                        val completeTime = if(get("completeTime") != null) (get("completeTime") as Timestamp) else null
                        val availableSeats = (get("availableSeats") as Long).toInt()

                        val reviewIdList = get("reviews") as List<String>
                        val reviewList = mutableListOf<Review>()
                        for(reviewId in reviewIdList) {
                            reviewList.add(FirebaseClient.getReviewFromChatId(reviewId))
                        }
                        val chat = FirebaseClient.getChatFromChatId(getString("chat") ?: "")
                        val createdAt = getTimestamp("createdAt")


                        val ride = Ride(
                            this.id,
                            origin,
                            destination,
                            datetime,
                            driver,
                            passengers,
                            rideStatus,
                            startTime,
                            completeTime,
                            availableSeats,
                            reviewList,
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