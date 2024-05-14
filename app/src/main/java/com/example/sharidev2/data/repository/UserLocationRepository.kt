package com.example.sharidev2.data.repository

import android.util.Log
import com.example.sharidev2.data.model.UserLocation
import com.example.sharidev2.utility.Converters
import com.example.sharidev2.utility.FirebaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserLocationRepository() {
    // Firebase Instances
    private val firestore = FirebaseClient.firestore
    private val firebaseAuth = FirebaseClient.firebaseAuth

    private val currentUser = firebaseAuth.currentUser
    private val locationCollectionRef = firestore.collection("userLocation")

    // DATA




    // RETRIEVE
    suspend fun getUserLocation(): UserLocation {
        return withContext(Dispatchers.IO) {
            try {
                if(!currentUser?.uid.isNullOrEmpty()){
                    val userLocationRef = locationCollectionRef.document(currentUser!!.uid).get().await()

                   val userLocation = Converters().toUserLocation(userLocationRef)

                    // Return userLocation if exist, else default UserLocation() with null value
                    userLocation ?: UserLocation()
                }
                else{
                    UserLocation()
                }


            } catch(e: Exception) {
                Log.e("Update User Location", e.message.toString())

                UserLocation()
            }
        }
    }


    // UPDATE
    suspend fun updateUserLocation(newUserLocation: UserLocation) {
        return withContext(Dispatchers.IO) {
            if(currentUser != null) {
                val user = FirebaseClient.getUserFromUid(currentUser.uid)

                newUserLocation.user = user

                try {
                    if (user != null) {
                        locationCollectionRef.document(user.uid!!)
                            .set(newUserLocation)
                            .await()
                    }

                } catch (e: Exception) {
                    Log.e("Update User Location", e.message.toString())
                }
            }
        }
    }
}