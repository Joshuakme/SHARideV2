package com.example.sharidev2.data.repository

import android.net.Uri
import android.util.Log
import com.example.sharidev2.utility.Constants
import com.example.sharidev2.utility.FirebaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class PersonalnformationRepository {
    private val firestore = FirebaseClient.firestore
    private val firebaseAuth = FirebaseClient.firebaseAuth
    private val firebaseStorage = FirebaseClient.firebaseStorage

    private val currentUser = firebaseAuth.currentUser
    private val storagePath = "images/${currentUser?.uid}"

    suspend fun updateProfilePicture(newImgUri: Uri): Int {
        return withContext(Dispatchers.IO) {
            if(currentUser != null) {
                try {
                    val imgRandomName = UUID.randomUUID()
                    // storage = /images/userUid/randomName
                    val userFileStorageRef = firebaseStorage.reference.child("${storagePath}/$imgRandomName")

                    val imgSnapshot = userFileStorageRef.putFile(newImgUri).await()

                    val imgDownloadUrl = imgSnapshot.storage.downloadUrl.await()

                    firestore.collection("user")
                        .document(currentUser.uid)
                        .update("photoUrl", imgDownloadUrl)
                        .await()

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


}