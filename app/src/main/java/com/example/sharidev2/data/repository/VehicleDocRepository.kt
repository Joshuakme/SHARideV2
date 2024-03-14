package com.example.sharidev2.data.repository

import android.app.DatePickerDialog
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.DatePicker
import androidx.core.content.ContentProviderCompat
import com.example.sharidev2.data.model.Contact
import com.example.sharidev2.data.model.Vehicle
import com.example.sharidev2.data.model.VehicleDoc
import com.example.sharidev2.databinding.FragmentAddVehicleDocBinding
import com.example.sharidev2.databinding.FragmentVehicleDocBinding
import com.example.sharidev2.utility.Constants
import com.example.sharidev2.utility.Converters
import com.example.sharidev2.utility.FirebaseClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Calendar

class VehicleDocRepository(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseStorage: FirebaseStorage
) {
    private val currentUser = firebaseAuth.currentUser

    private val vehicleDocRef = firestore.collection("vehicleDoc")
    private val isUserLogin = currentUser != null
    private val converters = Converters()

    private lateinit var binding: FragmentAddVehicleDocBinding

    private lateinit var calendar: Calendar


    suspend fun addVehicle(vehicleDoc: VehicleDoc, vehicleRegisCertUri: Uri?, roadtaxUri: Uri?, insuranceUri: Uri?,): Int {
        return withContext(Dispatchers.IO) {
            val currentUser = Firebase.auth.currentUser
            if (currentUser != null) {
                val newVehicleDoc = hashMapOf(
                    "firstName" to vehicleDoc.firstName,
                    "lastName" to vehicleDoc.lastName,
                    "vehicleType" to vehicleDoc.vehicleType,
                    "vehicleModel" to vehicleDoc.vehicleModel,
                    "carPlate" to vehicleDoc.carPlate,
                    "manufactureDate" to vehicleDoc.manufactureDate,
                    "vehicleRegisCert" to vehicleDoc.vehicleRegisCert,
                    "roadtax" to vehicleDoc.roadtax,
                    "insurance" to vehicleDoc.insurance,
                    "vehicleId" to vehicleDoc.vehicleId,
                    "userUid" to currentUser.uid
                )
                try {
                    val documentReference =
                        firestore.collection("vehicleDoc").add(newVehicleDoc).await()
                    val vehicleId = documentReference.id

                    // Update the document with the vehicle ID
                    documentReference.update("vehicleId", vehicleId).await()


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
                document.toObject<VehicleDoc>()
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
                        // Update the evehicle doc content
                        vehicleDoctRef.update("firstName", newVehicleDoc.firstName).await()
                        vehicleDoctRef.update("lastName", newVehicleDoc.lastName).await()
                        vehicleDoctRef.update("vehicleType", newVehicleDoc.vehicleType).await()
                        vehicleDoctRef.update("vehicleModel", newVehicleDoc.vehicleModel).await()
                        vehicleDoctRef.update("carPlate", newVehicleDoc.carPlate).await()
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
    suspend fun deleteVehicle(vehiclreId: String): Int {
        val userUid = firebaseAuth.currentUser
        return withContext(Dispatchers.IO) {
            try {
                if (userUid != null) {
                    // Check if the contact belongs to the current user
                    val vehicleDocRef = firestore.collection("vehicleDoc").document(vehiclreId)
                    val vehicleDocSnapshot = vehicleDocRef.get().await()
                    val userId = vehicleDocSnapshot.getString("userUid")

                    if (userId == userUid.uid) {
                        // Delete the emergency contact
                        vehicleDocRef.delete().await()
                        0 // Deletion successful
                    } else {
                        1 // Vehicle doesn't belong to the current user
                    }
                } else {
                    2 // User not authenticated
                }
            } catch (e: Exception) {
                3 // Handle exceptions
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
//    fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
//        val calendar = Calendar.getInstance()
//        calendar.set(year, month, dayOfMonth)
//        val formattedDate = "${calendar.get(Calendar.DAY_OF_MONTH)}-${calendar.get(Calendar.MONTH) + 1}-${calendar.get(
//            Calendar.YEAR)}"
//        binding.dateManufacture
//            .setText(formattedDate)
//    }


}
