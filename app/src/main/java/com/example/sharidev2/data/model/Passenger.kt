package com.example.sharidev2.data.model

import com.google.android.gms.maps.model.LatLng

data class Passenger(
    val userId: String? = null,
    val location: LatLng? = null // Location of the passenger
)
