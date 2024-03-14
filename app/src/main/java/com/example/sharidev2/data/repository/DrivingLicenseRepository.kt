package com.example.sharidev2.data.repository

import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.example.sharidev2.utility.Constants
import com.example.sharidev2.utility.FirebaseClient

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.net.URL
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

                    val imgRandomName = UUID.randomUUID()

                    val frontFileRef = firebaseStorage.reference.child("${storagePath}/$imgRandomName")


                    val fileSnapshot = frontFileRef.putFile(frontImageUri).await()

                    val frontUri = fileSnapshot.storage.downloadUrl.await()

                    val frontFileUrl = frontUri.toString()

                    val backFileRef = firebaseStorage.reference.child("${storagePath}/$imgRandomName")

                    val backTaskSnapshot = backFileRef.putFile(backImageUri).await()

                    val backDownloadUri = backTaskSnapshot.storage.downloadUrl.await()

                    val backFileUrl = backDownloadUri.toString()

                    val licenseData = hashMapOf(
                        "frontFileUrl" to frontFileUrl,
                        "backFileUrl" to backFileUrl,
                        "userUid" to currentUser.uid
                    )

                    val licenseSnapshot = firestore.collection("license")
                        .whereEqualTo("userUid", currentUser.uid)
                        .get()
                        .await()

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

                    firebaseStorage.getReferenceFromUrl(oldFrontImageURL?: "")
                        .delete()
                        .await()

                    firebaseStorage.getReferenceFromUrl(oldBackImageURL?: "")
                        .delete()
                        .await()

                    Constants.FIREBASE_REQUEST_SUCCESS
                } catch (e: Exception) {
                    Log.e("Add Driver License", e.message.toString())
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