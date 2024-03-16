package com.example.sharidev2.data.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.sharidev2.data.model.UserLocation
import com.example.sharidev2.utility.FirebaseClient
import com.example.sharidev2.utility.UserClient
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
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