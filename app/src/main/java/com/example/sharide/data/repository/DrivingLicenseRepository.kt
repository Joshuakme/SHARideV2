package com.example.sharide.data.repository

import android.net.Uri
import android.util.Log
import com.example.sharide.utility.Constants
import com.example.sharide.utility.FirebaseClient

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class DrivingLicenseRepository() {
    private val firestore = FirebaseClient.firestore
    private val firebaseAuth = FirebaseClient.firebaseAuth
    private val firebaseStorage = FirebaseClient.firebaseStorage

    private val currentUser = firebaseAuth.currentUser
    private val storageRef = firebaseStorage.reference
    private val storagePath = "images/${currentUser?.uid}"

    suspend fun addDriverLicense(frontImageUri: Uri?, backImageUri: Uri?): Int {
        if (frontImageUri != null && backImageUri != null && currentUser != null) {
            return withContext(Dispatchers.IO) {
                try {

                    // Generate unique names for the images
                    val frontImgRandomName = UUID.randomUUID()
                    val backImgRandomName = UUID.randomUUID()

                    // Upload front image
                    val frontFileRef = firebaseStorage.reference.child("${storagePath}/$frontImgRandomName")
                    val frontFileSnapshot = frontFileRef.putFile(frontImageUri).await()
                    val frontUri = frontFileSnapshot.storage.downloadUrl.await()
                    val frontFileUrl = frontUri.toString()

                    // Upload back image
                    val backFileRef = firebaseStorage.reference.child("${storagePath}/$backImgRandomName")
                    val backFileSnapshot = backFileRef.putFile(backImageUri).await()
                    val backUri = backFileSnapshot.storage.downloadUrl.await()
                    val backFileUrl = backUri.toString()

                    val licenseData = hashMapOf(
                        "frontFileUrl" to frontFileUrl,
                        "backFileUrl" to backFileUrl,
                        "userUid" to currentUser.uid
                    )

                    val licenseSnapshot = firestore.collection("license")
                        .whereEqualTo("userUid", currentUser.uid)
                        .get()
                        .await()

                    if(!licenseSnapshot.isEmpty) {
                        val licenseList = licenseSnapshot.documents.filter {
                            it.getString("userUid") == currentUser.uid
                        }

                        val licenseId = licenseList[0].id
                        val oldFrontImageURL = licenseList[0].getString("frontFileUrl")
                        val oldBackImageURL = licenseList[0].getString("backFileUrl")

                        firestore.collection("license")
                            .document(licenseId)
                            .set(licenseData)
                            .await()

                        if(oldFrontImageURL != null) {
                            firebaseStorage.getReferenceFromUrl(oldFrontImageURL)
                                .delete()
                                .await()
                        }

                        if(oldBackImageURL != null) {
                            firebaseStorage.getReferenceFromUrl(oldBackImageURL)
                                .delete()
                                .await()
                        }
                    } else {
                        // User has no record in database yet

                        // Create new record
                        firestore.collection("license")
                            .add(licenseData)
                            .await()
                    }

                    Constants.FIREBASE_REQUEST_SUCCESS
                } catch (e: Exception) {
                    Log.e("DrivingLicenseRepository - Add Driver License", "Error: ${e.message}", e)
                    Constants.FIREBASE_REQUEST_EXCEPTION
                }
            }
        }
        else if(currentUser == null){
            return Constants.FIREBASE_REQUEST_USER_NOT_AUTHENTICATED
        }
        else {
            return Constants.FIREBASE_REQUEST_DATA_NOT_VALID
            //Toast.makeText(requireContext(), "Please select both front and back driving license images", Toast.LENGTH_SHORT).show()
        }
    }



    suspend fun getDrivingLicense(): Map<String, Uri> {
        return withContext(Dispatchers.IO) {
            val licenseList = mutableListOf<Map<String, Uri>>()

            val licenseSnapshot = firestore.collection("license")
                .whereEqualTo("userUid", currentUser?.uid)
                .get()
                .await()

            for(document in licenseSnapshot.documents) {
                val frontLicenseImgURL = document.getString("frontFileUrl")
                val backLicenseImgURL = document.getString("backFileUrl")


                val licenseMap = mutableMapOf<String, Uri>()
                licenseMap["frontImgUri"] = Uri.parse(frontLicenseImgURL?: "")
                licenseMap["backImgUri"] = Uri.parse(backLicenseImgURL?: "")

                licenseList.add(licenseMap)
            }

            if(!licenseList.isNullOrEmpty()) {
                licenseList[0]
            } else {
                emptyMap()
            }
        }
    }

}