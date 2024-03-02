package com.example.sharidev2.data.repository

import android.util.Log
import com.example.sharidev2.data.model.UserLocation
import com.example.sharidev2.utility.FirebaseClient
import com.example.sharidev2.utility.UserClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserLocationRepository() {
    // Firebase Instances
    private val firestore = FirebaseClient.firestore
    private val firebaseAuth = FirebaseClient.firebaseAuth

    private val currentUser = firebaseAuth.currentUser
    private val locationCollectionRef = firestore.collection("userLocation")


    // RETRIEVE
//    suspend fun getUserLocation(): UserLocation {
//        return withContext(Dispatchers.IO) {
//            try {
//                val userLocation = locationCollectionRef.get().await()
//
//                userLocation.data
//
//                userLocation
//            } catch(e: Exception) {
//                Log.e("Update User Location", e.message.toString())
//
//                UserLocation()
//            }
//        }
//    }


    // UPDATE
    suspend fun updateUserLocation(newUserLocation: UserLocation) {
        return withContext(Dispatchers.IO) {
            val user = newUserLocation.user ?: FirebaseClient.getUserFromUid(currentUser?.uid!!)

            newUserLocation.user = user

            locationCollectionRef.document(user.uid?: "")
                .set(newUserLocation)
                .addOnSuccessListener {
                    Log.e("Update User Location", "Success")
                }
                .addOnFailureListener {
                    Log.e("Update User Location", it.message.toString())
                }
        }
    }
}