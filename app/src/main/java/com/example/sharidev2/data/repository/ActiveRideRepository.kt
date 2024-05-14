package com.example.sharidev2.data.repository

import android.util.Log
import com.example.sharidev2.data.model.Contact
import com.example.sharidev2.data.model.Driver
import com.example.sharidev2.data.model.Passenger
import com.example.sharidev2.data.model.Ride
import com.example.sharidev2.data.model.RideParticipant
import com.example.sharidev2.data.model.RideStatus
import com.example.sharidev2.data.model.UserLocation
import com.example.sharidev2.data.model.UserStatus
import com.example.sharidev2.utility.Constants
import com.example.sharidev2.utility.Converters
import com.example.sharidev2.utility.FirebaseClient
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ActiveRideRepository {
    // Firebase Instances
    private val firestore = FirebaseClient.firestore

    private val currentUser = FirebaseClient.firebaseAuth.currentUser
    private val rideCollectionRef = firestore.collection("ride")
    private val userLocationRef = firestore.collection("userLocation")
    private val constants = Constants

    // CREATE
    suspend fun addRoutePathList(rideId: String, routePathList: MutableList<MutableList<LatLng>>) {
        withContext(Dispatchers.IO) {
            try {
                for (routePath in routePathList) {
                    val map = hashMapOf(
                        "route" to routePath,
                        "selected" to false
                    )

                    rideCollectionRef
                        .document(rideId)
                        .collection("routes")
                        .add(map)
                        .await()
                }

            } catch (e: Exception) {
                Log.e("Add Route Path List", e.message.toString())
            }
        }
    }

    // RETRIEVE
    suspend fun getRideDriver(rideId: String): Driver {
        return withContext(Dispatchers.IO) {
            try {
                val driverRef = rideCollectionRef.document(rideId)

                suspendCoroutine { continuation ->
                    driverRef.addSnapshotListener { snapshot, exception ->
                        if (exception != null) {
                            // Handle error
                            continuation.resume(null)
                            return@addSnapshotListener
                        }

                        if (snapshot != null) {
                            val driver = snapshot.toObject(Driver::class.java)
                            continuation.resume(driver)
                        } else {
                            continuation.resume(null)
                        }
                    }

                    // Add cleanup code to remove the listener if needed
                    // For example:
                    /* continuation.invokeOnCancellation {
                        listenerRegistration.remove()
                    } */
                } ?: Driver() // Return a default Driver if snapshot is null
            } catch (e: Exception) {
                Log.e("Get Active Ride Driver", e.message.toString())
                Driver() // Return a default Driver in case of an exception
            }
        }
    }

    suspend fun getRidePassengers(rideId: String): List<Passenger> {
        return withContext(Dispatchers.IO) {
            try {
                val passengerList = mutableListOf<Passenger>()
                val passengerRef = rideCollectionRef.document(rideId)
                    .collection("passengers")

                val listenerRegistration =
                    passengerRef.addSnapshotListener { snapshots, exception ->
                        if (exception != null) {
                            // Handle error
                            return@addSnapshotListener
                        }

                        if (snapshots != null) {
                            passengerList.clear() // Clear the previous list
                            for (document in snapshots.documents) {
                                val passenger = document.toObject(Passenger::class.java)
                                passenger?.let {
                                    passengerList.add(it)
                                }
                            }
                            // Here you can notify your UI or ViewModel about the changes if needed
                        }
                    }
                // Return the list, but keep the listener active so it continues to receive updates
                passengerList.toList()
            } catch (e: Exception) {
                // Handle exceptions here
                Log.e("Get Ride Passengers", e.message.toString())
                emptyList()
            }
        }
    }

    suspend fun getDriverLocation(driverId: String): UserLocation? {
        return withContext(Dispatchers.IO) {
            try {
                val driverQuerySnapshot = userLocationRef.document(driverId).get().await()

                return@withContext Converters().toUserLocation(driverQuerySnapshot)
            } catch (e: Exception) {
                Log.e("ActiveRideRepository: getDriverLocation()", e.message.toString())
            }

            null
        }
    }

    suspend fun getPassengersLocation(passengerList: List<Passenger>): List<UserLocation> {
        val passengerLocationList = mutableListOf<UserLocation>()

        return withContext(Dispatchers.IO) {
            try {
                passengerList.forEach { passenger ->
                    val passengerLocationDocSnapshot = userLocationRef.document(passenger.userUid!!).get().await()


                        val passengerLocation = Converters().toUserLocation(passengerLocationDocSnapshot)

                    if(passengerLocation != null) {
                        passengerLocationList.add(passengerLocation)
                    }
                }
            } catch (e: Exception) {
                Log.e("ActiveRideRepository: getDriverLocation()", e.message.toString())
            }
            passengerLocationList
        }
    }

    suspend fun getEmergencyContactList(): List<Contact> {
        val contactList = mutableListOf<Contact>()

        if (!currentUser?.uid.isNullOrEmpty()) {
            val contactSnapshot =
                firestore.collection("contact").whereEqualTo("userUid", currentUser!!.uid)
                    .get().await()

            if (contactSnapshot != null) {
                for (document in contactSnapshot.documents) {
                    val contactMap = document.data

                    if (contactMap != null) {
                        val contact = Converters().toContact(contactMap)

                        contactList.add(contact)
                    }
                }
            }
        }
        return contactList
    }

    suspend fun getCurrentUserLocation(user: RideParticipant): UserLocation {
        return withContext(Dispatchers.IO) {
            try {
                val userUid = user.userUid ?: ""
                if (userUid.isBlank()) {
                    Log.e("Get User Location", "User UID is blank")
                    return@withContext UserLocation()
                }

                val locationRef = firestore.collection("userLocation").document(userUid)
                val locationSnapshot = locationRef.get().await()

                return@withContext if (locationSnapshot.exists()) {
                    locationSnapshot.toObject(UserLocation::class.java)
                        ?: UserLocation() // Handle null case
                } else {
                    Log.e("Get User Location", "Location document does not exist")
                    UserLocation() // Return a default UserLocation object
                }
            } catch (e: Exception) {
                Log.e("Get User Location", e.message.toString(), e)
                UserLocation() // Return a default UserLocation object in case of an exception
            }
        }
    }

    fun listenForActiveRideChanges(rideId: String, listener: (Ride) -> Unit) {
        rideCollectionRef.document(rideId).addSnapshotListener {snapshot, err ->
            if(err != null) {
                // Handle error
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val ride = Converters().toRide(snapshot)
                listener(ride)
            }
        }
    }


    // UPDATE
    suspend fun cancelRideByPassenger(ride: Ride, passengerId: String): Int {
        return withContext(Dispatchers.IO) {
            try {
                if (ride.id != null) {
                    ride.passengers.forEach { passenger ->
                        if (passenger.userUid == passengerId) {
                            passenger.status = UserStatus.CANCELED
                            passenger.ridePrice = 3.0
                        }

                    }

                    rideCollectionRef.document(ride.id)
                        .update(Converters().toRideHashMap(ride))
                        .await()

                    constants.FIREBASE_REQUEST_SUCCESS
                } else {
                    constants.FIREBASE_REQUEST_DATA_NOT_VALID
                }

                constants.FIREBASE_REQUEST_FAILED
            } catch (e: Exception) {
                Log.e("Get User Location", e.message.toString(), e)
                constants.FIREBASE_REQUEST_FAILED
            }
        }

    }

    suspend fun cancelRideByDriver(ride: Ride): Int {
        return withContext(Dispatchers.IO) {
            try {
                if (ride.id != null) {
                    ride.driver.status = UserStatus.CANCELED
                    ride.rideStatus = RideStatus.CANCELED

                    rideCollectionRef.document(ride.id)
                        .update(Converters().toRideHashMap(ride))
                        .await()

                    constants.FIREBASE_REQUEST_SUCCESS
                } else {
                    constants.FIREBASE_REQUEST_DATA_NOT_VALID
                }

                constants.FIREBASE_REQUEST_FAILED
            } catch (e: Exception) {
                Log.e("Get User Location", e.message.toString(), e)
                constants.FIREBASE_REQUEST_FAILED
            }
        }
    }

    suspend fun pickUpPassenger(ride: Ride, passengerId: String): Int {
        return withContext(Dispatchers.IO) {
            try {
                if (ride.id != null) {
                    val updatedPassengerList = ride.passengers
                    updatedPassengerList.map {passenger ->
                        if(passenger.userUid == passengerId) {
                            passenger.status = UserStatus.IN_VEHICLE
                        }
                    }
                    ride.passengers = updatedPassengerList

                    rideCollectionRef.document(ride.id)
                        .update(Converters().toRideHashMap(ride))
                        .await()

                    constants.FIREBASE_REQUEST_SUCCESS
                } else {
                    constants.FIREBASE_REQUEST_DATA_NOT_VALID
                }

            }catch (e: Exception) {
                Log.e("pickUpPassenger", e.message.toString(), e)
                constants.FIREBASE_REQUEST_FAILED
            }
        }
    }

    suspend fun dropOffPassenger(ride: Ride, passengerId: String): Int {
        return withContext(Dispatchers.IO) {
            try {
                if (ride.id != null) {
                    val updatedPassengerList = ride.passengers
                    updatedPassengerList.map {passenger ->
                        if(passenger.userUid == passengerId) {
                            passenger.status = UserStatus.COMPLETED
                        }
                    }
                    ride.passengers = updatedPassengerList

                    rideCollectionRef.document(ride.id)
                        .update(Converters().toRideHashMap(ride))
                        .await()

                    constants.FIREBASE_REQUEST_SUCCESS
                } else {
                    constants.FIREBASE_REQUEST_DATA_NOT_VALID
                }

            }catch (e: Exception) {
                Log.e("pickUpPassenger", e.message.toString(), e)
                constants.FIREBASE_REQUEST_FAILED
            }
        }
    }

    suspend fun completeRide(ride: Ride): Int {
        return withContext(Dispatchers.IO) {
            try {
                if (ride.id != null) {
                   ride.rideStatus = RideStatus.COMPLETED
                    ride.completeTime = Timestamp.now()

                    rideCollectionRef.document(ride.id)
                        .update(Converters().toRideHashMap(ride))
                        .await()

                    constants.FIREBASE_REQUEST_SUCCESS
                } else {
                    constants.FIREBASE_REQUEST_DATA_NOT_VALID
                }
            } catch (e: Exception) {
                Log.e("completeRide", e.message.toString(), e)
                constants.FIREBASE_REQUEST_FAILED
            }
        }
    }
}