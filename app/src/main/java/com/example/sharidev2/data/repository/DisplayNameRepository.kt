package com.example.sharidev2.data.repository

import android.util.Log
import com.example.sharidev2.utility.Constants
import com.example.sharidev2.utility.FirebaseClient
import com.google.firebase.auth.FirebaseAuth
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
    private val currentUser = firebaseAuth.currentUser
    private val isUserLogin = currentUser != null


    suspend fun updateDisplayName(newDisplayName: String): Int {
        return withContext(Dispatchers.IO) {

            if (currentUser != null) {
                try {
                    val documentReference = firestore.collection("user")
                        .document(currentUser.uid)
                        .update("displayName", newDisplayName)
                        .await()

                    return@withContext Constants.FIREBASE_REQUEST_SUCCESS // SUCCESS
                } catch (e: Exception) {
                    // Handle any exceptions here
                    e.printStackTrace()

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
}
