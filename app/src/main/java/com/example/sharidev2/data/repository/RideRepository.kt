package com.example.sharidev2.data.repository

import android.util.Log
import com.example.sharidev2.data.model.Chat
import com.example.sharidev2.data.model.Message
import com.example.sharidev2.data.model.MessageType
import com.example.sharidev2.data.model.Passenger
import com.example.sharidev2.data.model.Review
import com.example.sharidev2.data.model.Ride
import com.example.sharidev2.data.model.RideStatus
import com.example.sharidev2.data.model.User
import com.example.sharidev2.data.model.Vehicle
import com.example.sharidev2.utility.Constants
import com.example.sharidev2.utility.Converters
import com.example.sharidev2.utility.FirebaseClient
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


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
                        "chat" to newChatHashMap
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

                // Update the document with the new passenger
                rideRef.update("passengers.${newPassenger.userUid}", newPassenger).await()

                rideRef.collection("passengers")
                    .document(newPassenger.userUid!!)
                    .set(newPassenger)
                    .await()

                rideRef.collection("passengers")
                    .get()
                    .addOnCompleteListener {task ->
                        val data = task.result
                        val passengersCount = data.size()

                        rideRef.get().addOnCompleteListener {
                            val data = it.result.data
                            val vehicle = data?.get("vehicle") as Map<String, Any>

                            val vehicleCapacity = (vehicle["capacity"] as Long).toInt()
                            // Calculate the available seats
                            val availableSeats = vehicleCapacity - passengersCount

                            rideRef.update("availableSeats", availableSeats)
                                .addOnCompleteListener {
                                    // Success
                                }
                        }
                    }

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
                val driverQuerySnapshot = rideCollectionRef
                    .whereEqualTo("driver.userUid", currentUser?.uid ?: "")
                    .orderBy("datetime", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val passengerQuerySnapshot = rideCollectionRef.whereArrayContains("passengers", currentUser?.uid ?: "")
                    .get()
                    .await()


                val driverRideList = createRideListFromQuerySnapshot(driverQuerySnapshot)
                val passengerRideList = createRideListFromQuerySnapshot(passengerQuerySnapshot)

                val rideList = driverRideList + passengerRideList


                Log.e("Get All Rides", "All Rides: " + rideList.size.toString())

                rideList
            } catch (e: Exception) {
                Log.e("Get All Rides", e.message.toString())
                emptyList()
            }
        }
    }


    suspend fun getPassengerRideList(): List<Ride> {
        return withContext(Dispatchers.Main) {
            try {
                if(currentUser != null) {
                    val querySnapshot = firestore.collection("ride")
                        .whereNotEqualTo("driver.userUid", currentUser.uid)
                        .whereGreaterThanOrEqualTo("availableSeats", 1)
                        .get()
                        .await()

                    val filteredRideList = createRideListFromQuerySnapshot(querySnapshot)

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

                    val origin = converters.toSearchLocation(document.get("origin") as Map<String, Any>)
                    val destination = converters.toSearchLocation(document.get("destination") as Map<String, Any>)
                    val datetime = document.getTimestamp("datetime")!!
                    val driver = converters.toDriver(document.get("driver") as Map<String, Any>)

                    // Passenger Sub-Collection
                    val passengersSnapshot = document.reference.collection("passengers")
                        .get()
                        .await()
                    val passengersMap = mutableMapOf<String, Passenger>()

                    if(!passengersSnapshot.isEmpty && passengersSnapshot != null) {
                        for(passengerDoc in passengersSnapshot.documents) {
                            val passengerData: Map<String, Any>? = passengerDoc.data

                            if(passengerData != null) {
                                passengersMap[passengerDoc.id] = converters.toPassenger(passengerData)
                            }
                        }
                    }

                    val rideStatus = RideStatus.valueOf(document.getString("rideStatus") ?: "")
                    val startTime = document.getTimestamp("startTime")
                    val completeTime = document.getTimestamp("completeTime")
                    val availableSeats = (document.get("availableSeats") as Long).toInt()

                    // Review Sub-Collection
                    val reviewSnapshot = document.reference.collection("reviews")
                        .get()
                        .await()
                    val reviewsMap = mutableMapOf<String, Review>()

                    if(!reviewSnapshot.isEmpty && reviewSnapshot != null) {
                        for(reviewDoc in reviewSnapshot.documents) {
                            val reviewData = reviewDoc.data

                            if(reviewData != null) {
                                reviewsMap[reviewDoc.id] = converters.toReview(reviewData)
                            }
                        }
                    }

                    // Chat Sub-Collection
                    //val chat = FirebaseClient.getChatFromChatId(getString("chat") ?: "")
                    val chat = converters.toChat(document.get("chat") as Map<String, Any>)
                    val messagesMap = mutableMapOf<String, Message>()

                    val messageSnapshot = document.reference.collection("messages")
                        .orderBy("timestamp")
                        .get()
                        .await()

                    if(!messageSnapshot.isEmpty && messageSnapshot != null) {
                        for(messageDoc in messageSnapshot.documents) {
                            val messageData = messageDoc.data

                            if(messageData != null) {
                                messagesMap[messageDoc.id] = converters.toMessage(messageData)
                            }
                        }
                    }
                    chat.messages = messagesMap

                    val createdAt = document.getTimestamp("createdAt")


                    val ride = Ride(
                        document.id,
                        origin,
                        destination,
                        datetime,
                        driver,
                        passengersMap,
                        rideStatus,
                        startTime,
                        completeTime,
                        availableSeats,
                        reviewsMap,
                        chat,
                        createdAt
                    )

                    filteredRideList.add(ride)
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