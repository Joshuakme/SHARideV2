package com.example.sharide.data.repository

import android.net.Uri
import android.util.Log
import com.example.sharide.data.model.FirebaseResponse
import com.example.sharide.data.model.Vehicle
import com.example.sharide.data.model.VehicleDoc
import com.example.sharide.databinding.FragmentAddVehicleDocBinding
import com.example.sharide.utility.Constants
import com.example.sharide.utility.Converters
import com.example.sharide.utility.FirebaseClient
import com.example.sharide.utility.FirebaseClient.firebaseStorage
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.UUID

class VehicleDocRepository {
    private val firestore = FirebaseClient.firestore
    private val firebaseAuth = FirebaseClient.firebaseAuth
    private val currentUser = firebaseAuth.currentUser

    //Variables
    private val vehicleDocRef = firestore.collection("vehicleDoc")
    private val vehicleRef = firestore.collection("vehicle")
    private val isUserLogin = currentUser != null
    private val converters = Converters()
    private val storagePath = "images/${currentUser?.uid}"


    private lateinit var binding: FragmentAddVehicleDocBinding
    private lateinit var calendar: Calendar


    suspend fun addVehicleDoc(vehicleDoc: VehicleDoc): Int {
        return withContext(Dispatchers.IO) {
            val currentUser = Firebase.auth.currentUser

            if (currentUser != null) {
                try {
                    val vehicleDocId = vehicleDocRef.document().id

                    val newVehicleDoc = hashMapOf(
                        "firstName" to vehicleDoc.firstName,
                        "lastName" to vehicleDoc.lastName,
                        "manufactureDate" to vehicleDoc.manufactureDate,
                        "vehicleRegisCert" to vehicleDoc.vehicleRegisCert,
                        "roadtax" to vehicleDoc.roadtax,
                        "insurance" to vehicleDoc.insurance,
                        "vehicleId" to vehicleDoc.vehicleId,
                        "userUid" to currentUser.uid,
                    )

                    vehicleDocRef
                        .document(vehicleDocId)
                        .set(newVehicleDoc)
                        .await()

                    if (vehicleDoc.vehicleId != null) {
                        vehicleRef.document(vehicleDoc.vehicleId)
                            .update("documentId", vehicleDocId)
                            .await()

                        addVehicleDocImg(
                            registerCertUri = Uri.parse(vehicleDoc.vehicleRegisCert),
                            insuranceUri = Uri.parse(vehicleDoc.insurance),
                            roadtaxUri = Uri.parse(vehicleDoc.roadtax),
                            vehicleId = vehicleDoc.vehicleId
                        )
                    }


                    Log.e("Add Vehicle Doc", "Added Successfully")
                    return@withContext Constants.FIREBASE_REQUEST_SUCCESS    // SUCCESS
                } catch (e: Exception) {
                    // Handle any exceptions here
                    Log.e("Add Vehicle Doc", e.message.toString())

                    return@withContext Constants.FIREBASE_REQUEST_EXCEPTION
                }
            } else {
                Log.e("Add Vehicle Doc", "User not login")
                return@withContext Constants.FIREBASE_REQUEST_USER_NOT_AUTHENTICATED
            }
        }
    }

    suspend fun addVehicle(vehicleDetails: Vehicle): FirebaseResponse<String> {
        return withContext(Dispatchers.IO) {
            val currentUser = Firebase.auth.currentUser

            if (currentUser != null) {
                try {
                    val vehicleId = vehicleRef.document().id

                    val newVehicleDetails = hashMapOf(
                        "vehicleId" to vehicleId,
                        "userUid" to currentUser.uid,
                        "type" to vehicleDetails.type,
                        "model" to vehicleDetails.model,
                        "brand" to vehicleDetails.brand,
                        "color" to vehicleDetails.color,
                        "capacity" to vehicleDetails.capacity,
                        "photos" to vehicleDetails.photos,
                        "plateNumber" to vehicleDetails.plateNumber,
                        "document" to vehicleDetails.document,
                        "documentId" to vehicleDetails.documentId
                    )

                    vehicleRef
                        .document(vehicleId)
                        .set(newVehicleDetails)
                        .await()

                    Log.e("Add Vehicle ", "Added Successfully")
                    return@withContext FirebaseResponse(
                        status = Constants.FIREBASE_REQUEST_SUCCESS,
                        data = vehicleId
                    )    // SUCCESS
                } catch (e: Exception) {
                    // Handle any exceptions here
                    Log.e("Add Vehicle Details", e.message.toString())

                    return@withContext FirebaseResponse(status = Constants.FIREBASE_REQUEST_EXCEPTION)
                }
            } else {
                Log.e("Add Vehicle", "User not login")
                return@withContext FirebaseResponse(status = Constants.FIREBASE_REQUEST_USER_NOT_AUTHENTICATED)
            }
        }
    }

    suspend fun getVehicleList(): List<Vehicle> {
        return withContext(Dispatchers.IO) {
            val currentUser = Firebase.auth.currentUser
            val vehicleList = mutableListOf<Vehicle>()

            if (currentUser != null) {
                try {
                    val vehicleQuerySnapshot = vehicleRef
                        .whereEqualTo("userUid", currentUser.uid)
                        .get()
                        .await()

                    if (vehicleQuerySnapshot.isEmpty) {
                        vehicleList
                    } else {
                        val vehicleDocSnapshot = vehicleQuerySnapshot.documents

                        vehicleDocSnapshot.forEach { document ->
                            val vehicleMap = document.data

                            if(vehicleMap?.toMap() != null) {
                                val vehicle = Converters().toVehicle(vehicleMap.toMap())
                                vehicleList.add(vehicle)
                            }

                        }
                        vehicleList
                    }
                } catch (e: Exception) {
                    // Handle any exceptions here
                    Log.e("Failed to Get Vehicle", e.message.toString())
                }

            }
            vehicleList
        }
    }


    // Retrieve Vehicle Doc
    fun listenForVehicleDocChanges(callback: (List<VehicleDoc>?, Exception?) -> Unit) {
        vehicleDocRef.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                // Handle error
                callback(null, exception)
                return@addSnapshotListener
            }

            // Parse and handle changes in the snapshot
            val vehicleDocs = snapshot?.documents?.filter { document ->
                (document?.data?.get("userUid") as String) == currentUser?.uid
            }?.mapNotNull { document ->
                Converters().toVehicleDoc(document)
            }

            // Invoke the callback with the updated data
            callback(vehicleDocs, null)
        }
    }

    // Retrieve Vehicle
    fun listenForVehicleChanges(callback: (List<Vehicle>?, Exception?) -> Unit) {
        vehicleRef.whereEqualTo("userUid", currentUser?.uid)
            .addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                // Handle error
                callback(null, exception)
                return@addSnapshotListener
            }

            // Parse and handle changes in the snapshot
            val vehicleList = snapshot?.documents?.mapNotNull { document ->
                document.data?.let { Converters().toVehicle(it) }
            }

            // Invoke the callback with the updated data
            callback(vehicleList, null)
        }
    }


    suspend fun getAllVehicleDoc(): List<VehicleDoc> {
        return withContext(Dispatchers.IO) {
            val vehicleDocList = mutableListOf<VehicleDoc>()

            try {
                val querySnapshot = vehicleDocRef
                    .whereEqualTo("userUid", currentUser?.uid ?: "")
                    .get()
                    .await() // Using await() to suspend until the Firestore operation completes

                for (document in querySnapshot.documents) {
                    val vehicleDocData = document.data
                    if (vehicleDocData != null) {
                        vehicleDocList.add(converters.toVehicleDoc(vehicleDocData))

                    } else {
                        Log.e(
                            "Get Vehicle Docs",
                            "Vehicle Doc data is null for document ID: ${document.id}"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("Get Vehicle Docs", "Error fetching vehicleDoc ${e.message}")
            }
            vehicleDocList
        }
    }


    suspend fun getVehicleDoc(vehicleId: String): VehicleDoc? {
        return withContext(Dispatchers.IO) {
            try {
                val vehicleQuerySnapshot = vehicleDocRef
                    .whereEqualTo("vehicleId", vehicleId)
                    .get()
                    .await()

                if (!vehicleQuerySnapshot.isEmpty && vehicleQuerySnapshot != null) {
                    for (doc in vehicleQuerySnapshot.documents) {
                        val vehicleDocData = doc.data

                        if (vehicleDocData != null) {
                            return@withContext converters.toVehicleDoc(vehicleDocData)
                        }
                    }
                    null
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.d("Get Vehicle Doc", e.message.toString())
                null
            }
        }
    }


    //Function to update the vehicle doc to firestore after edited
    suspend fun updateVehicleDoc(newVehicleDoc: VehicleDoc): Int {
        return withContext(Dispatchers.IO) {
            try {
                val vehicleId = newVehicleDoc.vehicleId
                val contactUserId = newVehicleDoc.userUid

                val vehicleDoctRef = firestore.collection("vehicleDoc").document(vehicleId ?: "")
                val vehicleDocSnapshot = vehicleDoctRef.get().await()
                val userId = vehicleDocSnapshot.getString("userUid")


                if (isUserLogin) {
                    if (userId == contactUserId) {
                        // Update the vehicle doc content
                        vehicleDoctRef.update("firstName", newVehicleDoc.firstName).await()
                        vehicleDoctRef.update("lastName", newVehicleDoc.lastName).await()
                        vehicleDoctRef.update("vehicleRegisCert", newVehicleDoc.vehicleRegisCert)
                            .await()
                        vehicleDoctRef.update("roadtax", newVehicleDoc.roadtax).await()
                        vehicleDoctRef.update("insurance", newVehicleDoc.insurance).await()

                        Log.d("UPDATE VEHICLE DOC", "SUCCESSFUL")

                        Constants.FIREBASE_REQUEST_SUCCESS // Update successful
                    } else {
                        Constants.FIREBASE_REQUEST_NOT_BELONG_USER // Contact doesn't belong to the current user
                    }
                } else {
                    Constants.FIREBASE_REQUEST_USER_NOT_AUTHENTICATED // User not authenticated
                }
            } catch (e: Exception) {
                Constants.FIREBASE_REQUEST_EXCEPTION // Handle exceptions
                Log.d("PROBLEMMMM", e.message.toString())
            }
        }

    }


    //Function that allow the user to delete vehicle
    suspend fun deleteVehicle(vehicleId: String): Int {
        Log.e("deleteVehicle", "vehicleId ${vehicleId}")
        val userUid = firebaseAuth.currentUser
        return withContext(Dispatchers.IO) {
            try {
                if (userUid != null) {
                    // Check if the vehicle belongs to the current user
                    val vehicleRef = firestore.collection("vehicle").document(vehicleId)
                    val vehicleSnapshot = vehicleRef.get().await()
                    val userId = vehicleSnapshot.getString("userUid")


                    if (userId == userUid.uid) {
                        if (vehicleSnapshot.getString("documentId") != null) {
                            val vehicleDocRef = firestore.collection("vehicleDoc")
                                .document(vehicleSnapshot.getString("documentId")!!)

                            // Delete the vehicle
                            vehicleRef.delete().await()
                            vehicleDocRef.delete().await()


                            Constants.FIREBASE_REQUEST_SUCCESS // Delete successful
                        } else {
                            Constants.FIREBASE_REQUEST_FAILED // Delete failed
                        }
                    } else {
                        Constants.FIREBASE_REQUEST_NOT_BELONG_USER // Vehicle doesn't belong to the current user
                    }
                } else {
                    Constants.FIREBASE_REQUEST_USER_NOT_AUTHENTICATED // User not authenticated
                }
            } catch (e: Exception) {
                Constants.FIREBASE_REQUEST_EXCEPTION // Handle exceptions
                Log.e("deleteVehicle", "Failed to delete: ${e.message}")
            }
        }
    }


    suspend fun addVehicleDocImg(
        registerCertUri: Uri?,
        insuranceUri: Uri?,
        roadtaxUri: Uri?,
        vehicleId: String
    ): Int {
        if (registerCertUri != null && insuranceUri != null && roadtaxUri != null && currentUser != null) {
            return withContext(Dispatchers.IO) {
                try {
                    val vehicleDocImgRandomName = UUID.randomUUID()

                    val registerCertFileRef =
                        firebaseStorage.reference.child("${storagePath}/$vehicleDocImgRandomName")
                    val registerCertSnapshot = registerCertFileRef.putFile(registerCertUri).await()
                    val regisCertUri = registerCertSnapshot.storage.downloadUrl.await()
                    val registerCertFileUrl = regisCertUri.toString()

                    val insuranceFileRef =
                        firebaseStorage.reference.child("${storagePath}/$vehicleDocImgRandomName")
                    val insuranceSnapshot = insuranceFileRef.putFile(insuranceUri).await()
                    val insuranceUri = insuranceSnapshot.storage.downloadUrl.await()
                    val insuranceFileUrl = insuranceUri.toString()

                    val roadtaxFileRef =
                        firebaseStorage.reference.child("${storagePath}/$vehicleDocImgRandomName")
                    val roadtaxSnapshot = roadtaxFileRef.putFile(roadtaxUri).await()
                    val roadtaxUri = roadtaxSnapshot.storage.downloadUrl.await()
                    val roadtaxFileUrl = roadtaxUri.toString()

                    val vehicleDocImgData = hashMapOf(
                        "vehicleRegisCert" to registerCertFileUrl,
                        "insurance" to insuranceFileUrl,
                        "roadtax" to roadtaxFileUrl,
                        "userUid" to currentUser.uid
                    )

                    val vehicleDocImgSnapshot = firestore.collection("vehicleDoc")
                        .whereEqualTo("userUid", currentUser.uid)
                        .whereEqualTo("vehicleId", vehicleId)
                        .limit(1)
                        .get()
                        .await()


                    if (!vehicleDocImgSnapshot.isEmpty) {
                        val document = vehicleDocImgSnapshot.documents[0]
                        val oldRegisterCertImageURL = document.getString("vehicleRegisCert")
                        val oldInsuranceImageURL = document.getString("insurance")
                        val oldRoadtaxImageURL = document.getString("roadtax")

                        if (!oldRegisterCertImageURL.isNullOrEmpty()) {
                            firebaseStorage.getReferenceFromUrl(oldRegisterCertImageURL)
                                .delete()
                                .await()
                        }

                        if (!oldInsuranceImageURL.isNullOrEmpty()) {
                            firebaseStorage.getReferenceFromUrl(oldInsuranceImageURL)
                                .delete()
                                .await()
                        }

                        if (!oldRoadtaxImageURL.isNullOrEmpty()) {
                            firebaseStorage.getReferenceFromUrl(oldRoadtaxImageURL)
                                .delete()
                                .await()
                        }


                        firestore.collection("vehicleDoc")
                            .document(vehicleId)
                            .update(vehicleDocImgData as Map<String, Any>)
                            .await()


                    } else {
                        // User has no record in database yet

                        // Create new record
                        firestore.collection("vehicleDoc")
                            .add(vehicleDocImgData)
                            .await()
                    }

                    Constants.FIREBASE_REQUEST_SUCCESS
                } catch (e: Exception) {
                    Log.e("VehicleDocRepository - Add Vehicle Doc Image", e.message.toString())
                    Constants.FIREBASE_REQUEST_EXCEPTION
                }
            }
        } else if (currentUser == null) {
            return Constants.FIREBASE_REQUEST_USER_NOT_AUTHENTICATED
        } else {
            return Constants.FIREBASE_REQUEST_DATA_NOT_VALID
        }
    }


    suspend fun getVehicleDocImg(): Map<String, Uri> {
        return withContext(Dispatchers.IO) {
            val vehicleDocImgList = mutableListOf<Map<String, Uri>>()

            val vehicleDocImgSnapshot = firestore.collection("vehicleDoc")
                .whereEqualTo("userUid", currentUser?.uid)
                .get()
                .await()

            for (document in vehicleDocImgSnapshot.documents) {
                val registerCertImgURL = document.getString("vehicleRegisCert")
                val insuranceImgURL = document.getString("insurance")
                val roadtaxImgURL = document.getString("roadtax")

                val vehicleDocImgMap = mutableMapOf<String, Uri>()
                vehicleDocImgMap["vehicleRegisCertUri"] = Uri.parse(registerCertImgURL ?: "")
                vehicleDocImgMap["insuranceUri"] = Uri.parse(insuranceImgURL ?: "")
                vehicleDocImgMap["roadtaxUri"] = Uri.parse(roadtaxImgURL ?: "")


                vehicleDocImgList.add(vehicleDocImgMap)
            }

            if (!vehicleDocImgList.isNullOrEmpty()) {
                vehicleDocImgList[0]
            } else {
                emptyMap()
            }
        }
    }


    suspend fun addVehicleImage(vehicleFrontImageUri: Uri?, vehicleBackImageUri: Uri?): Int {
        if (vehicleFrontImageUri != null && vehicleBackImageUri != null && currentUser != null) {
            return withContext(Dispatchers.IO) {
                try {
                    val imgRandomName = UUID.randomUUID()

                    val vehicleFrontFileRef =
                        firebaseStorage.reference.child("$storagePath/$imgRandomName")
                    val vehicleFrontFileSnapshot =
                        vehicleFrontFileRef.putFile(vehicleFrontImageUri).await()
                    val vehicleFrontUri = vehicleFrontFileSnapshot.storage.downloadUrl.await()
                    val vehicleFrontFileUrl = vehicleFrontUri.toString()

                    val vehicleBackFileRef =
                        firebaseStorage.reference.child("$storagePath/$imgRandomName")
                    val vehicleBackFileSnapshot =
                        vehicleBackFileRef.putFile(vehicleBackImageUri).await()
                    val vehicleBackUri = vehicleBackFileSnapshot.storage.downloadUrl.await()
                    val vehicleBackFileUrl = vehicleBackUri.toString()

                    val vehicleImageData = hashMapOf(
                        "frontPhoto" to vehicleFrontFileUrl,
                        "backPhoto" to vehicleBackFileUrl
                    )

                    val vehicleSnapshot = firestore.collection("vehicles")
                        .whereEqualTo("userUid", currentUser.uid)
                        .get()
                        .await()

                    if (!vehicleSnapshot.isEmpty) {
                        val vehicleDoc = vehicleSnapshot.documents.first()
                        val existingPhotos =
                            vehicleDoc.get("photo") as? ArrayList<String> ?: arrayListOf()
                        existingPhotos.add(vehicleFrontFileUrl)
                        existingPhotos.add(vehicleBackFileUrl)

                        firestore.collection("vehicles")
                            .document(vehicleDoc.id)
                            .update("photo", existingPhotos)
                            .await()
                    } else {
                        // Handle case when no vehicle document exists for the user
                        Log.e("VehicleDocRepository", "No vehicle document found for the user")
                    }

                    Constants.FIREBASE_REQUEST_SUCCESS
                } catch (e: Exception) {
                    Log.e("VehicleDocRepository", "Add Vehicle Image: ${e.message}")
                    Constants.FIREBASE_REQUEST_EXCEPTION
                }
            }
        } else {
            return if (currentUser == null) {
                Constants.FIREBASE_REQUEST_USER_NOT_AUTHENTICATED
            } else {
                Constants.FIREBASE_REQUEST_DATA_NOT_VALID
            }
        }
    }


//    suspend fun addVehicleImage(vehicleFrontImageUri: Uri?, vehicleBackImageUri: Uri?): Int {
//        if (vehicleFrontImageUri != null && vehicleBackImageUri != null && currentUser != null) {
//            return withContext(Dispatchers.IO) {
//                try {
//
//                    val imgRandomName = UUID.randomUUID()
//
//                    val vehicleFrontFileRef = firebaseStorage.reference.child("${storagePath}/$imgRandomName")
//
//                    val vehicleFrontFileSnapshot = vehicleFrontFileRef.putFile(vehicleFrontImageUri).await()
//
//                    val vehicleFrontUri = vehicleFrontFileSnapshot.storage.downloadUrl.await()
//
//                    val vehicleFrontFileUrl = vehicleFrontUri.toString()
//
//                    val vehicleBackFileRef = firebaseStorage.reference.child("${storagePath}/$imgRandomName")
//
//                    val vehicleBackFileSnapshot = vehicleBackFileRef.putFile(vehicleBackImageUri).await()
//
//                    val backDownloadUri = vehicleBackFileSnapshot.storage.downloadUrl.await()
//
//                    val vehicleBackFileUrl = backDownloadUri.toString()
//
//                    val vehicleImageData = hashMapOf(
//                        "frontFileUrl" to vehicleFrontFileUrl,
//                        "backFileUrl" to vehicleBackFileUrl,
//                        "userUid" to currentUser.uid
//                    )
//
//                    val vehicleImageSnapshot = firestore.collection("vehicle")
//                        .whereEqualTo("userUid", currentUser.uid)
//                        .get()
//                        .await()
//
//                    if(!vehicleImageSnapshot.isEmpty) {
//                        val vehicleImageList = vehicleImageSnapshot.documents.filter {
//                            it.getString("userUid") == currentUser.uid
//                        }
//
//                        val vehicleId = vehicleImageList[0].id
//                        val oldVehicleFrontImageURL = vehicleImageList[0].getString("photo")
//                        val oldVehicleBackImageURL = vehicleImageList[0].getString("photo")
//
//                        firestore.collection("vehicle")
//                            .document(vehicleId)
//                            .set(vehicleImageData)
//                            .await()
//
//                        if(oldVehicleFrontImageURL != null) {
//                            firebaseStorage.getReferenceFromUrl(oldVehicleFrontImageURL)
//                                .delete()
//                                .await()
//                        }
//
//                        if(oldVehicleBackImageURL != null) {
//                            firebaseStorage.getReferenceFromUrl(oldVehicleBackImageURL)
//                                .delete()
//                                .await()
//                        }
//                    } else {
//                        // User has no record in database yet
//
//                        // Create new record
//                        firestore.collection("vehicle")
//                            .add(vehicleImageData)
//                            .await()
//                    }
//
//                    Constants.FIREBASE_REQUEST_SUCCESS
//                } catch (e: Exception) {
//                    Log.e("VehicleDocRepository - Add Vehicle Image", e.message.toString())
//                    Constants.FIREBASE_REQUEST_EXCEPTION
//                }
//            }
//        }
//        else if(currentUser == null){
//            return Constants.FIREBASE_REQUEST_USER_NOT_AUTHENTICATED
//        }
//        else {
//            return Constants.FIREBASE_REQUEST_DATA_NOT_VALID
//            //Toast.makeText(requireContext(), "Please select both front and back driving license images", Toast.LENGTH_SHORT).show()
//        }
//    }
}


//    // Function to show DatePickerDialog with minimum date set
//    private fun showDatePickerDialog(context: Context) {
//        val datePickerDialog = DatePickerDialog(
//            ContentProviderCompat.requireContext(),
//            this,
//            calendar.get(Calendar.YEAR),
//            calendar.get(Calendar.MONTH),
//            calendar.get(Calendar.DAY_OF_MONTH)
//        )
//        // Set minimum date to January 1, 2011
//        val minCalendar = Calendar.getInstance()
//        minCalendar.set(2011, Calendar.JANUARY, 1)
//        datePickerDialog.datePicker.minDate = minCalendar.timeInMillis
//        datePickerDialog.show()
//    }
//
//    // Function to update manufacture date EditText with the selected date
//    fun onDateSet(view: DateTimePicker?, year: Int, month: Int, dayOfMonth: Int) {
//        val calendar = Calendar.getInstance()
//        calendar.set(year, month, dayOfMonth)
//        val formattedDate = "${calendar.get(Calendar.DAY_OF_MONTH)}-${calendar.get(Calendar.MONTH) + 1}-${calendar.get(
//            Calendar.YEAR)}"
//        binding.dateManufacture
//            .setText(formattedDate)
//    }