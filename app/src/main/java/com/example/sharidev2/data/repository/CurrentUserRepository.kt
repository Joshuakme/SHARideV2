package com.example.sharidev2.data.repository

import android.net.Uri
import android.util.Log
import com.example.sharidev2.data.model.Chat
import com.example.sharidev2.data.model.User
import com.example.sharidev2.utility.Constants
import com.example.sharidev2.utility.Converters
import com.example.sharidev2.utility.FirebaseClient
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class CurrentUserRepository {
    // Firebase Instances
    private val firestore = FirebaseClient.firestore
    private val firebaseAuth = FirebaseClient.firebaseAuth
    private val firebaseStorage = FirebaseClient.firebaseStorage

    // References
    private val userCollectionRef = firestore.collection("user")
    private val chatCollectionRef = firestore.collection("chat")
    private val storageRef = firebaseStorage.reference

    private val currentUser = firebaseAuth.currentUser
    private val storagePath = "images/${currentUser?.uid}"
    private val converters = Converters()


    // CREATE
    suspend fun saveFcmToken(fcmToken: String): Int {
        return withContext(Dispatchers.IO) {
            try {
                userCollectionRef.document(currentUser!!.uid)
                    .update("fcmToken", fcmToken)
                    .await()

                Constants.FIREBASE_REQUEST_SUCCESS
            } catch(e: Exception) {
                Log.e("Current User Repository: saveFcmToken()", e.message.toString())
                Constants.FIREBASE_REQUEST_EXCEPTION
            }
        }
    }



    // RETRIEVE
    suspend fun getCurrentUser(): User? {
        return withContext(Dispatchers.IO) {
            try {
                FirebaseClient.getCurrentUser()

            } catch(e: Exception) {
                Log.e("Current User Repository: getUser()", e.message.toString())
                null
            }
        }
    }

    suspend fun getProfilePic(): Uri? {
        return withContext(Dispatchers.IO) {

            if(currentUser?.uid != null) {
                val profilePicDoc = firestore.collection("user")
                    .document(currentUser.uid)
                    .get()
                    .await()

                val profilePic = profilePicDoc.getString("photoUrl")

                if(profilePic != null) {
                    Uri.parse(profilePic)
                } else {
                    null
                }
            } else {
                null
            }
        }
    }

    suspend fun getDisplayName(userId: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val displayNameSnapshot = userCollectionRef.document(userId).get().await()
                return@withContext displayNameSnapshot.getString("displayName")
            } catch (e: Exception) {
                Log.e("FetchDisplayName", "Error fetching display name: ${e.message}", e)
                return@withContext null
            }
        }
    }

    suspend fun getMobile(userId: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val mobileSnapshot = userCollectionRef.document(userId).get().await()
                mobileSnapshot.getString("phoneNumber")
            } catch (e: Exception) {
                Log.e("FetchMobileNumber", "Error fetching mobile: ${e.message}")
                null
            }
        }
    }

    suspend fun getGender(): String? {
        return withContext(Dispatchers.IO) {
            try {
                currentUser?.let { user ->
                    val documentSnapshot = userCollectionRef.document(user.uid).get().await()
                    documentSnapshot.getString("gender")
                }
            } catch (e: Exception) {
                Log.e("FetchGender", "Error fetching gender: ${e.message}")
                null
            }
        }
    }


    // UPDATE
    suspend fun updateProfilePicture(newImgUri: Uri): Int {
        return withContext(Dispatchers.IO) {
            if(currentUser != null) {
                try {
                    // Save to Firebase Storage
                    val imgRandomName = UUID.randomUUID()
                    // storage = /images/userUid/randomName
                    val userFileStorageRef = storageRef.child("${storagePath}/$imgRandomName")

                    val imgSnapshot = userFileStorageRef.putFile(newImgUri).await()

                    val imgDownloadUrl = imgSnapshot.storage.downloadUrl.await()

                    userCollectionRef
                        .document(currentUser.uid)
                        .update("photoUrl", imgDownloadUrl)
                        .await()


                    // Save to FireAuth User
                    val profileImgUpdate = UserProfileChangeRequest.Builder()
                        .setPhotoUri(imgDownloadUrl)
                        .build()

                    currentUser.updateProfile(profileImgUpdate).await()


                    Constants.FIREBASE_REQUEST_SUCCESS
                } catch (e: Exception) {
                    Log.e("Update Profile Picture", e.message.toString())
                    Constants.FIREBASE_REQUEST_EXCEPTION
                }
            } else {
                Constants.FIREBASE_REQUEST_USER_NOT_AUTHENTICATED
            }
        }
    }

    suspend fun updateDisplayName(newDisplayName: String): Int {
        return withContext(Dispatchers.IO) {
            if (currentUser != null) {
                try {
                    // User collection
                    userCollectionRef
                        .document(currentUser.uid)
                        .update("displayName", newDisplayName)
                        .await()

                    // Auth User
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(newDisplayName)
                        .build()

                    currentUser.updateProfile(profileUpdates).await()

                    // Chat User
                    val chatDocSnapshot = chatCollectionRef.get().await()

                    for(document in chatDocSnapshot.documents) {
                        val chatData = document.data

                        if(chatData != null) {
                            val messageList = converters.toMessageList(chatData["messages"] as List<Map<String, Any>>).toMutableList()

                            for(message in messageList) {
                                if(message.senderId == currentUser.uid) {
                                    message.senderName = newDisplayName
                                }
                            }

                            document.reference.update("messages", messageList)
                        }
                    }

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

    suspend fun updateMobile(newMobile: String): Int {
        return withContext(Dispatchers.IO) {

            if (currentUser != null) {
                try {
                    userCollectionRef
                        .document(currentUser.uid)
                        .update("phoneNumber", newMobile)
                        .await()

                    Constants.FIREBASE_REQUEST_SUCCESS    // SUCCESS
                } catch (e: Exception) {
                    // Handle any exceptions here
                    e.printStackTrace()

                    Constants.FIREBASE_REQUEST_EXCEPTION
                }
            } else {
                Constants.FIREBASE_REQUEST_USER_NOT_AUTHENTICATED
            }
        }
    }

    suspend fun updateGender(newGender: String): Int {
        return withContext(Dispatchers.IO) {
            try {
                currentUser?.let { user ->
                    userCollectionRef.document(user.uid)
                        .update("gender", newGender)
                        .await()
                }
                Constants.FIREBASE_REQUEST_SUCCESS
            } catch (e: Exception) {
                Log.e("UpdateGender", "Error updating gender: ${e.message}")
                Constants.FIREBASE_REQUEST_EXCEPTION
            }
        }
    }



    // UTILITY
    fun listenForUserUpdate(userUid: String, listener: (User) -> Unit): ListenerRegistration {
        return userCollectionRef.document(userUid)
            .addSnapshotListener {snapshots, error ->
            if (error != null) {
                // Handle error
                return@addSnapshotListener
            }

            val userData = snapshots?.data

            if(userData != null) {
               val user = converters.toUser(userData)
                listener(user)
            }
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
    }
}