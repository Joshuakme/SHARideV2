package com.example.sharide.data.repository

import com.example.sharide.utility.FirebaseClient

class EditMobileRepository() {
    private val firestore = FirebaseClient.firestore
    private val firebaseAuth = FirebaseClient.firebaseAuth

    // Variables
    private val mobileRef = firestore.collection("user")
    private val currentUser = FirebaseClient.firebaseAuth.currentUser
    private val isUserLogin = currentUser != null


//    suspend fun updateMobile(newMobile: String): Int {
//        return withContext(Dispatchers.IO) {
//
//            if (currentUser != null) {
//                try {
//                    val documentReference = firestore.collection("user")
//                        .document(currentUser.uid)
//                        .update("phoneNumber", newMobile)
//                        .await()
//
//                    Constants.FIREBASE_REQUEST_SUCCESS    // SUCCESS
//                } catch (e: Exception) {
//                    // Handle any exceptions here
//                    e.printStackTrace()
//
//                    Constants.FIREBASE_REQUEST_EXCEPTION
//                }
//            } else {
//                Constants.FIREBASE_REQUEST_USER_NOT_AUTHENTICATED
//            }
//        }
//    }

//    suspend fun getMobile(userId: String): String? {
//        return withContext(Dispatchers.IO) {
//            try {
//                val mobileSnapshot = mobileRef.document(userId).get().await()
//                mobileSnapshot.getString("phoneNumber")
//            } catch (e: Exception) {
//                Log.e("FetchMobileNumber", "Error fetching mobile: ${e.message}")
//                null
//            }
//        }
//    }
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
