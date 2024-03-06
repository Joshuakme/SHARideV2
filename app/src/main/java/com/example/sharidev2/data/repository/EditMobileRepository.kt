package com.example.sharidev2.data.repository

import android.util.Log
import com.example.sharidev2.data.model.User
import com.example.sharidev2.firebase.FirebaseInitializer
import com.example.sharidev2.utility.FirebaseUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class EditMobileRepository(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {

    // Variables
    private val mobileRef = firestore.collection("user")
    private val currentUser = FirebaseInitializer.firebaseAuth.currentUser
    private val isUserLogin = currentUser != null


    suspend fun updateMobile(newMobile: String): Int {
        return withContext(Dispatchers.IO) {

            if (currentUser != null) {
                try {
                    val documentReference = firestore.collection("user")
                        .document(currentUser.uid)
                        .update("phoneNumber", newMobile)
                        .await()

                    return@withContext FirebaseUtils.SUCCESS    // SUCCESS
                } catch (e: Exception) {
                    // Handle any exceptions here
                    e.printStackTrace()

                    return@withContext FirebaseUtils.EXCEPTION
                }
            } else {

                return@withContext FirebaseUtils.USER_NOT_AUTHENTICATED
            }
        }
    }

    suspend fun fetchMobile(userId: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val mobileSnapshot = mobileRef.document(userId).get().await()
                return@withContext mobileSnapshot.getString("phoneNumber")
            } catch (e: Exception) {
                Log.e("FetchMobileNumber", "Error fetching mobile: ${e.message}", e)
                return@withContext null
            }
        }
    }
}



//    suspend fun updateMobile(newMobile: User): Int {
//        return withContext(Dispatchers.IO) {
//            try {
//                val nameUserId = newMobile.uid
//
//                val mobileRef = firestore.collection("user").document(nameUserId?: "")
//                val mobileSnapshot = mobileRef.get().await()
//                val userId = mobileSnapshot.getString("uid")
//
//
//                if(isUserLogin){
//                    if (userId == nameUserId) {
//                        // Update the display name content
//                        mobileRef.update("phoneNumber", newMobile.displayName).await()
//
//
//                        Log.d("UPDATE Mobile Number", "SUCESSFUL")
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

