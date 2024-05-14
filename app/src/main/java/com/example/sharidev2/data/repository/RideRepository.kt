package com.example.sharidev2.data.repository

import android.net.Uri
import android.util.Log
import com.example.sharidev2.data.model.Chat
import com.example.sharidev2.data.model.Message
import com.example.sharidev2.data.model.MessageType
import com.example.sharidev2.data.model.Passenger
import com.example.sharidev2.data.model.Ride
import com.example.sharidev2.data.model.RideStatus
import com.example.sharidev2.data.model.SearchLocation
import com.example.sharidev2.data.model.UserStatus
import com.example.sharidev2.utility.Constants
import com.example.sharidev2.utility.Converters
import com.example.sharidev2.utility.FareUtils
import com.example.sharidev2.utility.FirebaseClient
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID


class RideRepository() {
    // Firebase Instances
    private val firestore = FirebaseClient.firestore
    private val rideCollectionRef = firestore.collection("ride")
    private val routeCollectionRef = firestore.collection("route")

    private val currentUser = FirebaseClient.firebaseAuth.currentUser
    private val converters = Converters()


    // CREATE METHODS
    suspend fun createRide(ride: Ride, callback: CreateRideCallback) {
        return withContext(Dispatchers.IO) {
            try {
                if (currentUser != null) {
                    val driverUser = FirebaseClient.getUserFromUid(ride.driver.userUid!!)
                    val adminUser = FirebaseClient.getUserFromUid("admin")


                    ride.driver.user = driverUser

                    val rideId = rideCollectionRef.document().id

                    val newChat = createEmptyChat(rideId, "Ride to ${ride.destination.name}", adminUser?.photoUrl)

                    val newRide = converters.toRideHashMap(ride, rideId, newChat.chatId)


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
                val welcomeChatMessage = "Welcome to SHARide! Start your chat here."

                val now = Timestamp.now()

                messages.add(
                    Message(
                        UUID.randomUUID().toString(),
                        "admin",
                        "Admin",
                        welcomeChatMessage,
                        now,
                        attachmentURL = null,
                        emptyList(),
                        MessageType.Text,
                        photoUri
                    )
                )

                val chat = Chat(
                    chatId = chatId,
                    chatTitle = rideName,
                    members = listOf(currentUser!!.uid),
                    memberFcmTokens = listOf(FirebaseClient.getUserFcmToken(currentUser.uid)!!),
                    lastMessage = welcomeChatMessage,
                    timestamp = now,
                    messages = messages,
                    rideId = rideId
                )
                val chatHashMap = converters.toChatHashMap(chat)

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


    suspend fun addPassengerToRide(newPassenger: Passenger, rideId: String): Int {
        return withContext(Dispatchers.IO) {
            try {
                val rideRef = rideCollectionRef.document(rideId)

                newPassenger.user = FirebaseClient.getUserFromUid(newPassenger.userUid!!)


                val rideDoc = rideRef.get().await()
                val passengers = converters.toPassengerListFromFirebase(
                    rideDoc.get("passengers") as? List<Map<String, Any>>?: mutableListOf()
                )

                val passengerList = mutableListOf<Passenger>()

                passengers.forEach { passenger ->
                    passenger.ridePrice = newPassenger.ridePrice
                    passengerList.add(passenger)
                }


                passengerList.add(newPassenger)

                val passengerIds = (rideDoc.get("passengerIds") as List<String>).toMutableList()
                passengerIds.add(newPassenger.userUid)

                rideRef.update("passengers", passengerList)
                rideRef.update("passengerIds", passengerIds)


                Constants.FIREBASE_REQUEST_SUCCESS
            } catch (e: Exception) {
                Log.e("Add Passenger", e.message.toString())
                Constants.FIREBASE_REQUEST_EXCEPTION
            }
        }
    }

    suspend fun acceptPassengerToRide(acceptedPassenger: Passenger, rideId: String): Int {
        return withContext(Dispatchers.IO) {
            try {
                val rideRef = rideCollectionRef.document(rideId)

                val rideDoc = rideRef.get().await()
                val passengers = converters.toPassengerListFromFirebase(
                    rideDoc.get("passengers") as? List<Map<String, Any>>?: mutableListOf()
                ).toMutableList()


                // Update Accepted Passenger Status
                passengers.forEach { passenger ->
                    if(passenger.userUid == acceptedPassenger.userUid) {
                        passenger.status = UserStatus.ACCEPTED
                    }
                }
                rideRef.update("passengers", passengers).await()


                // Add user into chatroom
                val chatRef = firestore.collection("chat")
                val chatQuerySnapshot = chatRef
                    .whereEqualTo("rideId", rideId)
                    .limit(1)
                    .get()
                    .await()

                if(!chatQuerySnapshot.isEmpty) {
                    val chatId = chatQuerySnapshot.documents[0].id

                    val chatDocSnapshot = chatRef.document(chatId)
                        .get()
                        .await()

                    val chatData = chatDocSnapshot.data
                    if(chatData != null) {
                        val chatMemberList = (chatData["members"] as List<String>).toMutableList()
                        val chatMemberFcmTokenList = (chatData["memberFcmTokens"] as List<String>).toMutableList()

                        chatMemberList.add(acceptedPassenger.userUid!!)
                        chatMemberFcmTokenList.add(acceptedPassenger.user!!.fcmToken!!)

                        chatRef.document(chatId).update("members", chatMemberList)
                        chatRef.document(chatId).update("memberFcmTokens", chatMemberFcmTokenList)
                    }
                }


                // UPDATE AVAILABLE SEATS
                // Get the ride data
                val rideData = rideDoc.data
                // Get Accepted Passenger Size
                val passengersCount = passengers.filter { it.status == UserStatus.ACCEPTED }.size

                val driver = converters.toDriver(rideData?.get("driver") as Map<String, Any>)
                if(driver.vehicle?.capacity != null) {
                    val vehicleCapacity = driver.vehicle.capacity.minus(1)

                    // Calculate the available seats
                    val availableSeats = vehicleCapacity.minus(passengersCount)

                    // Update the available seats for the ride
                    rideRef.update("availableSeats", availableSeats).await()

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

    suspend fun startRide(ride: Ride): Int {
        return withContext(Dispatchers.IO) {
            try {
                ride.rideStatus = RideStatus.IN_PROGRESS
                ride.startTime = Timestamp.now()

                rideCollectionRef.document(ride.id!!)
                    .update(Converters().toRideHashMap(ride))
                    .await()

                Constants.FIREBASE_REQUEST_SUCCESS
            } catch(e: Exception) {
                Log.e("Start Ride", e.message.toString())
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
                        .whereEqualTo("driver.userUid", currentUser.uid)
                        .orderBy("datetime", Query.Direction.DESCENDING)
                        .get()
                        .await()

                    val passengerQuerySnapshot = rideCollectionRef
                        .whereArrayContains("passengerIds", currentUser.uid)
                        .orderBy("datetime", Query.Direction.DESCENDING)
                        .get()
                        .await()

                    val driverRideList = createRideListFromQuerySnapshot(driverQuerySnapshot)
                    val passengerRideList = createRideListFromQuerySnapshot(passengerQuerySnapshot)

                    val rideList = (driverRideList + passengerRideList).sortedByDescending { it.datetime }


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

    fun listenForAllRidesChanges(listener: (HashMap<String, Ride>) -> Unit) {

        val allRideHashMap = HashMap<String, Ride>()

        if (currentUser?.uid != null) {
            val driverListener = rideCollectionRef
                .whereEqualTo("driver.userUid", currentUser.uid)
                .addSnapshotListener { driverSnapshot, e ->
                    if (e != null) {
                        // Handle error
                        return@addSnapshotListener
                    }


                    driverSnapshot?.documents?.mapNotNull { documentSnapshot ->
                        val ride = converters.toRide(documentSnapshot)
                        allRideHashMap[ride.id!!] = ride
                    }

                    // Call the listener after processing data
                    listener(allRideHashMap)
                }

            val passengersListener = rideCollectionRef
                .whereArrayContains("passengerIds", currentUser.uid)
                .addSnapshotListener { passengersSnapshot, e ->
                    if (e != null) {
                        // Handle error
                        return@addSnapshotListener
                    }

                    passengersSnapshot?.documents?.mapNotNull { documentSnapshot ->
                        val ride = converters.toRide(documentSnapshot)
                        allRideHashMap[ride.id!!] = ride
                    }

                    // Call the listener after processing data
                    listener(allRideHashMap)
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
                        converters.toLatLngList(routeData["route"] as List<Map<String, Any>>)
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

    suspend fun getNearbyRides(origin: LatLng): List<Ride> {
        val querySnapshot = rideCollectionRef
            .whereGreaterThanOrEqualTo("availableSeats", 1)
            .get()
            .await()

        val availableRideList = createRideListFromQuerySnapshot(querySnapshot)

        val nearbyRides = availableRideList.filter { ride ->
            Log.e("Ride Repository", "Distance: ${FareUtils.calculateDistance(ride.origin.geolocation!!, origin)}")
            FareUtils.calculateDistance(ride.origin.geolocation!!, origin) < 100
        }

        return nearbyRides.subList(0, 3)
    }

    fun listenForBookingRequests(rideId: String, listener: (List<Passenger>) -> Unit) {
        rideCollectionRef
            .document(rideId)
            .addSnapshotListener {snapshot, e ->
                if (e != null) {
                    // Handle error
                    return@addSnapshotListener
                }

                val passengerMapList = snapshot?.get("passengers") as List<Map<String, Any>>?

                if(passengerMapList != null) {
                    val passengerList = converters.toPassengerList(passengerMapList)
                    listener(passengerList)
                }
            }
    }


    // UPDATE FUNCTIONS
    suspend fun updatePassenger(oldPassengerUid: String, newPassenger: Passenger) {

    }

    suspend fun rejectPassengerToRide(rejectedPassenger: Passenger, rideId: String): Int {
        return withContext(Dispatchers.IO) {
            try {
                val rideRef = rideCollectionRef.document(rideId)

                val rideDoc = rideRef.get().await()
                val passengers = converters.toPassengerListFromFirebase(
                    rideDoc.get("passengers") as? List<Map<String, Any>>?: mutableListOf()
                ).toMutableList()

                // Update Rejected Passenger Status
                passengers.forEach { passenger ->
                    if(passenger.userUid == rejectedPassenger.userUid) {
                        passenger.status = UserStatus.REJECTED
                    }
                }
                rideRef.update("passengers", passengers).await()


                Constants.FIREBASE_REQUEST_SUCCESS
            } catch (e: Exception) {
                Log.e("Add Passenger", e.message.toString())
                Constants.FIREBASE_REQUEST_EXCEPTION
            }
        }
    }

    // DELETE FUNCTIONS


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