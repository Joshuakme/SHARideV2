package com.example.sharidev2.data.model

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Vehicle(
    val vehicleID: String,
    val brand: String,
    val model: String,
    val type: VehicleType,
    val plateNumber: String,
    val color: String,  // Enum of vehicle color
    val photos: MutableList<String>? = null,    // Link of image
    val capacity: Int,
) : Parcelable
