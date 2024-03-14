package com.example.sharidev2.data.repository

import android.net.Uri
import android.util.Log
import com.example.sharidev2.data.model.Chat
import com.example.sharidev2.data.model.ChatStatus
import com.example.sharidev2.data.model.Message
import com.example.sharidev2.data.model.MessageType
import com.example.sharidev2.data.model.Passenger
import com.example.sharidev2.data.model.Ride
import com.example.sharidev2.data.model.SearchLocation
import com.example.sharidev2.utility.Constants
import com.example.sharidev2.utility.Converters
import com.example.sharidev2.utility.FirebaseClient
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.UUID


class RideRepository() {
    // Firebase Instances
    private val firestore = FirebaseClient.firestore
    private val rideCollectionRef = firestore.collection("ride")
    private val routeCollectionRef = firestore.collection("route")

    private val currentUser = FirebaseClient.firebaseAuth.currentUser
    private val converters = Converters()

    // Data Variables
    private val routeList = mutableListOf<MutableList<LatLng>>()


    // CREATE METHODS
    suspend fun createRide(ride: Ride, callback: CreateRideCallback) {
        return withContext(Dispatchers.IO) {
            try {
                if (currentUser != null) {
                    val driverUser = FirebaseClient.getUserFromUid(ride.driver.userUid!!)


                    ride.driver.user = driverUser
                    Log.e("Ride Repository", "Create Ride: ${driverUser.displayName}")

                    val rideId = rideCollectionRef.document().id

                    val newChat = createEmptyChat(rideId, "Ride to ${ride.destination.name}", currentUser.photoUrl)


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
                        "passengers" to ride.passengers,
                        "rideStatus" to ride.rideStatus,
                        "startTime" to ride.startTime,
                        "completeTime" to ride.completeTime,
                        "availableSeats" to ride.availableSeats,
                        "reviews" to ride.reviews,
                        "chat" to newChatHashMap,
                        "createdAt" to ride.createdAt
                    )



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
//                    for(passenger in ride.passengers) {
//                        rideCollectionRef.document(rideId)
//                            .collection("passengers")
//                            .document(passenger.key)
//                            .set(ride.passengers.values)
//                            .await()
//                    }

                    // Reviews Sub-Collection
//                    for(review in ride.reviews) {
//                        rideCollectionRef.document(rideId)
//                            .collection("reviews")
//                            .document()
//                            .set(review)
//                            .await()
//                    }

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

    private suspend fun createEmptyChat(rideId: String, rideName: String, photoUri: Uri?): Chat {
        return withContext(Dispatchers.IO) {
            try{
                val chatId = firestore.collection("chat").document().id

                val messages = mutableListOf<Message>()
                val messagesMap = mutableListOf<HashMap<String, Any?>>()
                val welcomeChatMessage = "Welcome to SHARide! Start your chat here."

                val now = Timestamp.now()

                val wlcMessage = Message(
                    UUID.randomUUID().toString(),
                    "admin",
                    "Admin",
                    welcomeChatMessage,
                    now,
                    attachmentURL = null,
                    emptyList(),
                    MessageType.Text,
                    if(photoUri != null) photoUri else null
                )

                messages.add(
                    wlcMessage
                )

                messagesMap.add(
                    hashMapOf(
                        "messageId" to wlcMessage.messageId,
                        "senderId" to wlcMessage.senderId,
                        "text" to wlcMessage.text,
                        "timestamp" to now,
                        "readBy" to wlcMessage.readBy,
                        "attachmentURL" to wlcMessage.attachmentURL,
                        "messageType" to wlcMessage.messageType,
                    )
                )

                val chatHashMap = hashMapOf(
                    "chatId" to chatId,
                    "chatTitle" to rideName,
                    "members" to listOf(currentUser?.uid!!),
                    "lastMessage" to welcomeChatMessage,
                    "timestamp" to now,
                    "messages" to messagesMap,
                    "rideId" to rideId,
                    "chatStatus" to ChatStatus.ACTIVE
                )

                val chat = Chat(
                    chatId,
                    rideName,
                    listOf(currentUser.uid),
                    welcomeChatMessage,
                    now,
                    messages,
                    rideId
                )

                firestore.collection("chat").document(chatId)
                    .set(chatHashMap)
                    .await()


                chat
            }
            catch (e: Exception) {
                Log.e("Create Chat", e.message.toString())
                Chat()
            }
        }
    }


    suspend fun addPassenger(newPassenger: Passenger, rideId: String): Int {
        return withContext(Dispatchers.IO) {
            try {
                val rideRef = rideCollectionRef.document(rideId)

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

                // Add user into chatroom
                val chatRef = firestore.collection("chat")

                val chatQuerySnapshot = chatRef
                                        .whereEqualTo("rideId", rideId)
                                        .get()
                                        .await()

                if(!chatQuerySnapshot.isEmpty) {
                    val chatDoc = chatQuerySnapshot.documents[1]
                    val chatId = chatDoc.id

                    val chatDocSnapshot = chatRef.
                    document(chatId)
                        .get()
                        .await()

                    val chatData = chatDocSnapshot.data

                    if(chatData != null) {
                        val chatMemberList = (chatData["members"] as List<String>).toMutableList()

                        chatMemberList.add(currentUser!!.uid)
                    }
                }


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

    suspend fun addRoutePath(origin: SearchLocation, destination: SearchLocation, routePath: MutableList<LatLng>): Int {
        return withContext(Dispatchers.IO) {
            try {
                if(getRideRoute(origin.name, destination.name) != null) {
                    Constants.FIREBASE_REQUEST_DATA_NOT_VALID
                } else {
                    val map = hashMapOf(
                        "origin" to origin.name,
                        "destination" to destination.name,
                        "route" to routePath
                    )

                    routeCollectionRef
                        .document()
                        .set(map)
                        .await()

                    Constants.FIREBASE_REQUEST_SUCCESS
                }
            } catch (e: Exception) {
                Log.e("Add Route Path", e.message.toString())
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




    suspend fun getRideRoute(originName: String, destinationName: String): MutableList<LatLng>? {
        return withContext(Dispatchers.IO) {
            try {
                val querySnapshot = routeCollectionRef
                    .whereEqualTo("origin", originName)
                    .whereEqualTo("destination", destinationName)
                    .limit(1)
                    .get()
                    .await()


                if (!querySnapshot.isEmpty) {
                    val routeData = querySnapshot.documents.first().data

                    if(routeData != null) {
                        converters.toLatLng(routeData["route"] as List<Map<String, Any>>)
                    } else {
                        null
                    }

                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e("Get Route Path", e.message.toString())
                null
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