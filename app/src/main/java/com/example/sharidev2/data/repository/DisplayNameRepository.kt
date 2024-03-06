package com.example.sharidev2.data.repository

import android.util.Log
import com.example.sharidev2.utility.Constants
import com.example.sharidev2.utility.FirebaseClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class DisplayNameRepository() {
    private val firestore = FirebaseClient.firestore
    private val firebaseAuth = FirebaseClient.firebaseAuth

    // Variables
    private val displayNameRef = firestore.collection("user")
    private val currentUser = FirebaseClient.firebaseAuth.currentUser


    suspend fun updateDisplayName(newDisplayName: String): Int {
        return withContext(Dispatchers.IO) {

            if (currentUser != null) {
                try {
                    // User collection
                    firestore.collection("user")
                        .document(currentUser.uid)
                        .update("displayName", newDisplayName)
                        .await()

                    // Auth User
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(newDisplayName)
                        .build()

                    firebaseAuth.currentUser!!.updateProfile(profileUpdates).await()

                    return@withContext Constants.FIREBASE_REQUEST_SUCCESS // SUCCESS
                } catch (e: Exception) {
                    // Handle any exceptions here
                    Log.e("Display Name Repository", e.stackTrace.toString())

                    return@withContext Constants.FIREBASE_REQUEST_EXCEPTION
                }
            } else {
                return@withContext Constants.FIREBASE_REQUEST_USER_NOT_AUTHENTICATED
            }
        }
    }

    suspend fun fetchDisplayName(userId: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val displayNameSnapshot = displayNameRef.document(userId).get().await()
                return@withContext displayNameSnapshot.getString("displayName")
            } catch (e: Exception) {
                Log.e("FetchDisplayName", "Error fetching display name: ${e.message}", e)
                return@withContext null
            }
        }
    }


//    suspend fun updateDisplayName(newDisplayName: User): Int {
//        return withContext(Dispatchers.IO) {
//            try {
//                val nameUserId = newDisplayName.uid
//
//                val displayNameRef = firestore.collection("user").document(nameUserId?: "")
//                val displayNameSnapshot = displayNameRef.get().await()
//                val userId = displayNameSnapshot.getString("uid")
//
//
//                if(isUserLogin){
//                    if (userId == nameUserId) {
//                        // Update the display name content
//                        displayNameRef.update("displayName", newDisplayName.displayName).await()
//
//
//                        Log.d("UPDATE DISPLAY NAME", "SUCESSFUL")
//
//                        FirebaseUtils.SUCCESS // Update successful
//                    } else {
//                        FirebaseUtils.NOT_BELONG_USER // Contact doesn't belong to the current user
//                    }
//                } else {
//                    FirebaseUtils.USER_NOT_AUTHENTICATED // User not authenticated
//                }
//            } catch (e: Exception) {
//                FirebaseUtils.EXCEPTION // Handle exceptions
//                Log.d("ERROR", e.message.toString())
//            }
//        }
//
//    }












//    fun updateDisplayName(uid: String, newDisplayName: String, callback: (Boolean) -> Unit) {
//        val userRef = db.collection("users").document(uid)
//        userRef
//            .update("displayName", newDisplayName)
//            .addOnSuccessListener {
//                callback(true)
//            }
//            .addOnFailureListener {
//                callback(false)
//            }
//    }
}
