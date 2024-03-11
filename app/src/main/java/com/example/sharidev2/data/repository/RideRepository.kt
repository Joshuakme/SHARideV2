package com.example.sharidev2.data.repository

import android.util.Log
import com.example.sharidev2.data.model.Chat
import com.example.sharidev2.data.model.Message
import com.example.sharidev2.data.model.MessageType
import com.example.sharidev2.data.model.Passenger
import com.example.sharidev2.data.model.Ride
import com.example.sharidev2.utility.Constants
import com.example.sharidev2.utility.Converters
import com.example.sharidev2.utility.FirebaseClient
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Calendar


class RideRepository() {
    // Firebase Instances
    private val firestore = FirebaseClient.firestore
    private val rideCollectionRef = firestore.collection("ride")

    private val currentUser = FirebaseClient.firebaseAuth.currentUser
    private val converters = Converters()


    suspend fun createRide(ride: Ride, callback: CreateRideCallback) {
        return withContext(Dispatchers.IO) {
            try {
                if (currentUser != null) {
                    val driverUser = FirebaseClient.getUserFromUid(ride.driver.userUid!!)

                    ride.driver.user = driverUser
                    Log.e("Ride Repository", "Create Ride: ${driverUser.displayName}")

                    val newChat = createEmptyChat()
                    val newChatHashMap = hashMapOf(
                        "chatId" to newChat.chatId,
                        "members" to newChat.members,
                        "lastMessage" to newChat.lastMessage,
                        "timestamp" to newChat.timestamp
                    )

                    val newRide = hashMapOf(
                        "origin" to ride.origin,
                        "destination" to ride.destination,
                        "datetime" to ride.datetime,
                        "driver" to ride.driver,
                        "rideStatus" to ride.rideStatus,
                        "startTime" to ride.startTime,
                        "completeTime" to ride.completeTime,
                        "availableSeats" to ride.availableSeats,
                        "chat" to newChatHashMap,
                        "createdAt" to ride.createdAt
                    )

                    val rideCollectionRef = firestore.collection("ride")
                    val rideId = rideCollectionRef.document().id

                    rideCollectionRef.document(rideId)
                        .set(newRide)
                        .addOnSuccessListener {
                            // Write was successful!
                            callback.onCreateSuccess()
                        }
                        .addOnFailureListener { e ->
                            // Write failed
                            callback.onCreateFailure(e)
                        }

                    // Passengers Sub-Collection
                    for(passenger in ride.passengers) {
                        rideCollectionRef.document(rideId)
                            .collection("passengers")
                            .document(passenger.key)
                            .set(ride.passengers.values)
                            .await()
                    }

                    // Reviews Sub-Collection
                    for(review in ride.reviews) {
                        rideCollectionRef.document(rideId)
                            .collection("reviews")
                            .document()
                            .set(review)
                            .await()
                    }

                    // Message Sub-Collection
                    val messageId = rideCollectionRef.document(rideId)
                                        .collection("messages")
                                        .document().id

                    rideCollectionRef.document(rideId)
                        .collection("messages")
                        .document(messageId)
                        .set(newChat.messages!!)
                        .await()

                } else {
                    // User not logged in yet
                    Log.e("Create Ride : HAIYAA", "User is not logged in yet")
                }
            } catch(e: Exception) {
                Log.e("Create Ride : KENAPA??", e.message.toString())
            }
        }
    }

    private suspend fun createEmptyChat(): Chat {
        return withContext(Dispatchers.IO) {
            try{
                val chatId = firestore.collection("chat").document().id

                val messages = mutableMapOf<String, Message>()

                val messageId = firestore.collection("chat").document(chatId).collection("message").document().id

                val welcomeChatMessage = "Welcome to SHARide! Start you chat here."

                messages[messageId] = Message(
                    messageId,
                    "Admin",
                    welcomeChatMessage,
                    Timestamp.now(),
                    attachmentURL = null,
                    emptyList(),
                    MessageType.Text
                )

                val chatHashMap = hashMapOf(
                    "chatId" to chatId,
                    "members" to listOf(currentUser?.uid!!),
                    "lastMessage" to welcomeChatMessage,
                    "timestamp" to Timestamp.now(),
                )

                val chat = Chat(
                    chatId = chatId,
                    members = listOf(currentUser?.uid!!),
                    lastMessage = welcomeChatMessage,
                    timestamp = Timestamp.now(),
                    messages = messages
                )

                firestore.collection("chat").document(chatId)
                    .set(chatHashMap)
                    .await()


                // Messages Sub-collection
                firestore.collection("chat").document(chatId)
                    .collection("messages")
                    .document(messageId)
                    .set(messages)
                    .await()

                chat
            }
            catch (e: Exception) {
                Log.e("Create Chat", e.message.toString())
                Chat()
            }
        }
    }

    private suspend fun addMessageToChat(chatId: String, newMessage: Message) {
        return withContext(Dispatchers.IO) {
            try{
                firestore.collection("chat")
                    .document(chatId)
                    .collection("message")
                    .add(newMessage)
                    .await()

                firestore.collection("chat")
                    .document(chatId)
                    .update("lastMessage", newMessage.text)
                    .await()

            } catch (e: Exception) {
                Log.e("Add Message To Chat", e.message.toString())
            }
        }
    }

    suspend fun addPassenger(newPassenger: Passenger, rideId: String): Int {
        return withContext(Dispatchers.IO) {
            try {
                val rideRef = firestore.collection("ride").document(rideId)

                newPassenger.user = FirebaseClient.getCurrentUser()

                // Add the new passenger to the "passengers" sub-collection
                rideRef.collection("passengers")
                    .document(newPassenger.userUid!!)
                    .set(newPassenger)
                    .await()

                // Get the count of passengers in the "passengers" sub-collection
                val passengersQuery = rideRef.collection("passengers").get().await()
                val passengersCount = passengersQuery.size()

                // Get the ride data
                val rideData = rideRef.get().await().data
                val driver = converters.toDriver(rideData?.get("driver") as Map<String, Any>)

                if(driver.vehicle?.capacity != null) {
                    val vehicleCapacity = driver.vehicle.capacity.minus(1)

                    // Calculate the available seats
                    val availableSeats = vehicleCapacity.minus(passengersCount)

                    // Update the available seats for the ride
                    rideRef.update("availableSeats", availableSeats)
                        .await()

                    Constants.FIREBASE_REQUEST_SUCCESS
                } else {
                    Constants.FIREBASE_REQUEST_EXCEPTION
                }
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
                if(currentUser?.uid != null) {
                    val driverQuerySnapshot = rideCollectionRef
                        .whereEqualTo("driver.userUid", currentUser.uid ?: "")
                        .orderBy("datetime", Query.Direction.DESCENDING)
                        .get()
                        .await()


                    val passengerQuerySnapshot = firestore.collectionGroup("passengers")
                        .whereEqualTo("userUid", currentUser.uid)
                        .get()
                        .await()

                    val driverRideList = createRideListFromQuerySnapshot(driverQuerySnapshot)
                    val passengerRideList = mutableListOf<Ride>()

                    for (document in passengerQuerySnapshot.documents) {
                        val rideRef = document.reference.parent.parent
                        if (rideRef != null) {
                            val rideSnapshot = rideRef.get().await()

                            val ride = FirebaseClient.createRideFromDocumentSnapshot(rideSnapshot)
                            if (ride != null) {
                                passengerRideList.add(ride)
                            }
                        }
                    }

                    val rideList = driverRideList + passengerRideList


                    Log.e("Get All Rides", "All Rides: " + rideList.size.toString())

                    rideList
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                Log.e("Get All Rides", e.message.toString())
                emptyList()
            }
        }
    }


    suspend fun getAvailableRideList(): List<Ride> {
        return withContext(Dispatchers.Main) {
            try {
                if(currentUser != null) {
                    val querySnapshot = firestore.collection("ride")
                        .whereGreaterThanOrEqualTo("availableSeats", 1)
                        .get()
                        .await()

                    val filteredNotDriverList = createRideListFromQuerySnapshot(querySnapshot)

                    val filteredRideList = filteredNotDriverList.filter {
                        (it.driver.userUid != currentUser.uid) &&
                        (it.availableSeats >= 1) &&
                        (it.datetime > Timestamp.now())
                    }

                    filteredRideList
                } else {
                    // User not logged in yet
                    emptyList()
                }
            } catch (e: Exception) {
                Log.e("Get Passenger Ride", e.message.toString())
                emptyList()
            }
        }
    }


    // UPDATE FUNCTIONS
    suspend fun updatePassenger(oldPassengerUid: String, newPassenger: Passenger) {

    }


    // HELPER METHODS
    private suspend fun createRideListFromQuerySnapshot(querySnapshot: QuerySnapshot): List<Ride> {
        val filteredRideList = mutableListOf<Ride>()

        return withContext(Dispatchers.IO) {
            try {
                for (document in querySnapshot.documents) {

                    val ride = FirebaseClient.createRideFromDocumentSnapshot(document)

                    if(ride != null) {
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