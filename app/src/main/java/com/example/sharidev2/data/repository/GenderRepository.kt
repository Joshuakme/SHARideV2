package com.example.sharidev2.data.repository

import android.util.Log
import com.example.sharidev2.firebase.FirebaseInitializer
import com.example.sharidev2.utility.FirebaseUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class GenderRepository(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {

    private val userCollection = firestore.collection("user")
    private val currentUser = firebaseAuth.currentUser

    suspend fun updateGender(newGender: String): Int {
        return withContext(Dispatchers.IO) {
            try {
                currentUser?.let { user ->
                    userCollection.document(user.uid)
                        .update("gender", newGender)
                        .await()
                }
                FirebaseUtils.SUCCESS
            } catch (e: Exception) {
                Log.e("UpdateGender", "Error updating gender: ${e.message}", e)
                FirebaseUtils.EXCEPTION
            }
        }
    }

    suspend fun fetchGender(): String? {
        return withContext(Dispatchers.IO) {
            try {
                currentUser?.let { user ->
                    val documentSnapshot = userCollection.document(user.uid).get().await()
                    documentSnapshot.getString("gender")
                }
            } catch (e: Exception) {
                Log.e("FetchGender", "Error fetching gender: ${e.message}", e)
                null
            }
        }
    }
}
