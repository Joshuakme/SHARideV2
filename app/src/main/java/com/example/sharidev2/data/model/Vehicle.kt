package com.example.sharidev2.data.model

import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Vehicle(
    val vehicleID: String? = "",
    val brand: String? = "",
    val model: String? = "",
    val type: VehicleType = VehicleType.Sedans,
    val plateNumber: String? = "",
    val color: String? = "",  // Enum of vehicle color
    val photos: MutableList<Uri>? = null,    // Link of image
    val capacity: Int = 0,
) : Parcelable
