package com.example.sharidev2.data.model

import android.graphics.Bitmap

data class Vehicle(
    val vehicleID: String,
    val model: String,
    val plateNumber: String,
    val color: String,  // Enum of vehicle color
    val photos: MutableList<Bitmap>,
    val capacity: Int,
)
