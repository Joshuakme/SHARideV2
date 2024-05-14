package com.example.sharidev2.data.repository

import android.net.Uri
import android.util.Log
import com.example.sharidev2.data.model.Vehicle
import com.example.sharidev2.data.model.VehicleType
import com.example.sharidev2.utility.Converters
import com.example.sharidev2.utility.FirebaseClient
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class DriverVehicleRepository() {
    private val firestore = FirebaseClient.firestore

    private val currentUser = FirebaseClient.firebaseAuth.currentUser


    suspend fun getDriverVehicleList(): List<Vehicle> {
        return withContext(Dispatchers.IO) {
            try {
                if (currentUser != null) {
                    val querySnapshot = firestore.collection("vehicle")
                        .whereEqualTo("userUid", currentUser.uid)
                        .get()
                        .await()

                    val vehicleList = convertDriverVehicleToList(querySnapshot)

                    vehicleList
                } else {
                    Log.e("KENAPA??", "Belum Login")
                    emptyList()
                }
            } catch (e: Exception) {
                Log.e("KENAPA??", e.message.toString())
                emptyList()
            }
        }
    }


    // HELPER METHODS
    private fun convertDriverVehicleToList(querySnapshot: QuerySnapshot): List<Vehicle> {
        val vehicleList = mutableListOf<Vehicle>()
        val converters = Converters()


        for (document in querySnapshot.documents) {
            val vehicleID: String = document.id
            val brand: String = document.getString("brand") ?: ""
            val model: String = document.getString("model") ?: ""
            val type: VehicleType = document.getString("type")?.let { VehicleType.valueOf(it) } ?: VehicleType.Sedan
            val plateNumber: String = document.getString("plateNumber") ?: ""
            val color: String = document.getString("color") ?: ""
            val photos: MutableList<Uri> = converters.toUriList(document.get("photos") as MutableList<String>).toMutableList()
            val capacity: Int = (document.get("capacity") as Long).toInt()
            val documentId = document.getString("documentId")
            val userUid = document.getString("userUid")


            val vehicle = Vehicle(vehicleID, brand, model, type, plateNumber, color, photos, capacity, document = null, documentId = documentId, userUid = userUid)
            vehicleList.add(vehicle)
        }

        return vehicleList.toList()
    }
}