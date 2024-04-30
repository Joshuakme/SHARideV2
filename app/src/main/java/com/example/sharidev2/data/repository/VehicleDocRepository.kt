package com.example.sharidev2.data.repository

import android.net.Uri
import android.util.Log
import com.example.sharidev2.data.model.Vehicle
import com.example.sharidev2.data.model.VehicleDoc
import com.example.sharidev2.databinding.FragmentAddVehicleDocBinding
import com.example.sharidev2.utility.Constants
import com.example.sharidev2.utility.Converters
import com.example.sharidev2.utility.FirebaseClient
import com.example.sharidev2.utility.FirebaseClient.firebaseStorage
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.UUID

class VehicleDocRepository{
    private val firestore = FirebaseClient.firestore
    private val firebaseAuth = FirebaseClient.firebaseAuth
    private val currentUser = firebaseAuth.currentUser

    //Variables
    private val vehicleDocRef = firestore.collection("vehicleDoc")
    private val isUserLogin = currentUser != null
    private val converters = Converters()
    private val storagePath = "images/${currentUser?.uid}"


    private lateinit var binding: FragmentAddVehicleDocBinding
    private lateinit var calendar: Calendar


    suspend fun addVehicleDoc(vehicleDoc: VehicleDoc): Int {
        return withContext(Dispatchers.IO) {
            val currentUser = Firebase.auth.currentUser

            if (currentUser != null) {
                try{
                    val vehicleId = vehicleDocRef.document().id

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
                        .document(vehicleId)
                        .set(newVehicleDoc)
                        .await()

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

    suspend fun addVehicle(vehicleDetails: Vehicle):Int{
        return withContext(Dispatchers.IO) {
            val currentUser = Firebase.auth.currentUser

            if (currentUser != null) {
                try{
                    val vehicleId = vehicleDocRef.document().id

                    val newVehicleDetails = hashMapOf(
                        "vehicleId" to vehicleDetails.vehicleID,
                        "userUid" to currentUser.uid,
                        "vehicleType" to vehicleDetails.type,
                        "vehicleModel" to vehicleDetails.model,
                        "vehicleBrand" to vehicleDetails.brand,
                        "vehicleColor" to vehicleDetails.color,
                        "vehicleCapacity" to vehicleDetails.capacity,
                        "vehiclePhotos" to vehicleDetails.photos,
                        "vehiclePlate" to vehicleDetails.plateNumber
                    )

                    vehicleDocRef
                        .document(vehicleId)
                        .set(newVehicleDetails)
                        .await()

                    Log.e("Add Vehicle ", "Added Successfully")
                    return@withContext Constants.FIREBASE_REQUEST_SUCCESS    // SUCCESS
                } catch (e: Exception) {
                    // Handle any exceptions here
                    Log.e("Add Vehicle Details", e.message.toString())

                    return@withContext Constants.FIREBASE_REQUEST_EXCEPTION
                }
            } else {
                Log.e("Add Vehicle", "User not login")
                return@withContext Constants.FIREBASE_REQUEST_USER_NOT_AUTHENTICATED
            }
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
            val vehicleDocs = snapshot?.documents?.mapNotNull { document ->
                Converters().toVehicleDoc(document)
            }

            // Invoke the callback with the updated data
            callback(vehicleDocs, null)
        }
    }


    suspend fun getAllVehicles(): List<VehicleDoc> {
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
                            "Get Vehicles",
                            "Vehicle data is null for document ID: ${document.id}"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("Get Vehicle", "Error fetching vehicle: ${e.message}")
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

                if(!vehicleQuerySnapshot.isEmpty && vehicleQuerySnapshot != null) {
                    for(doc in vehicleQuerySnapshot.documents) {
                        val vehicleDocData = doc.data

                        if(vehicleDocData != null) {
                            return@withContext converters.toVehicleDoc(vehicleDocData)
                        }
                    }
                    null
                } else {
                    null
                }
            } catch (e:Exception) {
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

                val vehicleDoctRef = firestore.collection("vehicleDoc").document(vehicleId?: "")
                val vehicleDocSnapshot = vehicleDoctRef.get().await()
                val userId = vehicleDocSnapshot.getString("userUid")


                if(isUserLogin){
                    if (userId == contactUserId) {
                        // Update the vehicle doc content
                        vehicleDoctRef.update("firstName", newVehicleDoc.firstName).await()
                        vehicleDoctRef.update("lastName", newVehicleDoc.lastName).await()
                        vehicleDoctRef.update("vehicleRegisCert", newVehicleDoc.vehicleRegisCert).await()
                        vehicleDoctRef.update("roadtax", newVehicleDoc.roadtax).await()
                        vehicleDoctRef.update("insurance", newVehicleDoc.insurance).await()

                        Log.d("UPDATE VEHICLE DOC", "SUCESSFUL")

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
        val userUid = firebaseAuth.currentUser
        return withContext(Dispatchers.IO) {
            try {
                if (userUid != null) {
                    // Check if the contact belongs to the current user
                    val vehicleDocRef = firestore.collection("vehicleDoc").document(vehicleId)
                    val vehicleDocSnapshot = vehicleDocRef.get().await()
                    val userId = vehicleDocSnapshot.getString("userUid")

                    if (userId == userUid.uid) {
                        // Delete the emergency contact
                        vehicleDocRef.delete().await()
                        0 // Deletion successful
                    } else {
                        Constants.FIREBASE_REQUEST_NOT_BELONG_USER // Vehicle doesn't belong to the current user
                    }
                } else {
                    Constants.FIREBASE_REQUEST_USER_NOT_AUTHENTICATED // User not authenticated
                }
            } catch (e: Exception) {
                Constants.FIREBASE_REQUEST_EXCEPTION // Handle exceptions
            }
        }
    }


    suspend fun addVehicleDocImg(registerCertUri: Uri?, insuranceUri: Uri?, roadtaxUri:Uri?):Int{
        if (registerCertUri != null && insuranceUri != null && roadtaxUri != null && currentUser != null){
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
                        "registerCertFileUrl" to registerCertFileUrl,
                        "insuranceFileUrl" to insuranceFileUrl,
                        "roadtaxFileUrl" to roadtaxFileUrl,
                        "userUid" to currentUser.uid
                    )

                    val vehicleDocImgSnapshot = firestore.collection("vehicleDoc")
                        .whereEqualTo("userUid", currentUser.uid)
                        .get()
                        .await()


                    if (!vehicleDocImgSnapshot.isEmpty) {
                        val vehicleDocImgList = vehicleDocImgSnapshot.documents.filter {
                            it.getString("userUid") == currentUser.uid
                        }

                        val vehicleId = vehicleDocImgList[0].id
                        val oldRegisterCertImageURL =
                            vehicleDocImgList[0].getString("vehicleRegisCert")
                        val oldInsuranceImageURL = vehicleDocImgList[0].getString("insurance")
                        val oldRoadtaxImageURL = vehicleDocImgList[0].getString("roadtax")


                        firestore.collection("vehicleDoc")
                            .document(vehicleId)
                            .set(vehicleDocImgData)
                            .await()

                        if (oldRegisterCertImageURL != null) {
                            firebaseStorage.getReferenceFromUrl(oldRegisterCertImageURL)
                                .delete()
                                .await()
                        }

                        if (oldInsuranceImageURL != null) {
                            firebaseStorage.getReferenceFromUrl(oldInsuranceImageURL)
                                .delete()
                                .await()
                        }

                        if (oldRoadtaxImageURL != null) {
                            firebaseStorage.getReferenceFromUrl(oldRoadtaxImageURL)
                                .delete()
                                .await()
                        }
                    } else {
                        // User has no record in database yet

                        // Create new record
                        firestore.collection("vehicleDoc")
                            .add(vehicleDocImgData)
                            .await()
                    }

                    Constants.FIREBASE_REQUEST_SUCCESS
                }catch (e: Exception){
                    Log.e("VehicleDocRepository - Add Vehicle Doc Image", e.message.toString())
                    Constants.FIREBASE_REQUEST_EXCEPTION
                }
            }
        }
        else if(currentUser == null){
            return Constants.FIREBASE_REQUEST_USER_NOT_AUTHENTICATED
        }
        else{
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

            for(document in vehicleDocImgSnapshot.documents) {
                val registerCertImgURL = document.getString("vehicleRegisCert")
                val insuranceImgURL = document.getString("insurance")
                val roadtaxImgURL = document.getString("roadtax")

                val vehicleDocImgMap = mutableMapOf<String, Uri>()
                vehicleDocImgMap["vehicleRegisCertUri"] = Uri.parse(registerCertImgURL?: "")
                vehicleDocImgMap["insuranceUri"] = Uri.parse(insuranceImgURL?: "")
                vehicleDocImgMap["roadtaxUri"] = Uri.parse(roadtaxImgURL?: "")


                vehicleDocImgList.add(vehicleDocImgMap)
            }

            if(!vehicleDocImgList.isNullOrEmpty()) {
                vehicleDocImgList[0]
            } else {
                emptyMap()
            }
        }
    }
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