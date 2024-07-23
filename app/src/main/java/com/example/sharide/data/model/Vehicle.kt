package com.example.sharide.data.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Vehicle(
    val vehicleId: String? = "",
    val brand: String? = "",
    val model: String? = "",
    val type: VehicleType = VehicleType.Sedan,
    val plateNumber: String? = "",
    val color: String? = "",  // Enum of vehicle color
    val photos: MutableList<Uri>? = null,    // Link of image
    val capacity: Int = 0,
    val document: VehicleDoc? = null,
    val documentId: String? = "",
    val userUid: String? = ""
) : Parcelable
