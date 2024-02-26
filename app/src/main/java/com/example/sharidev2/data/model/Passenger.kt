package com.example.sharidev2.data.model

import com.google.android.gms.maps.model.LatLng

data class Passenger(
    val user: User,
    val location: LatLng // Location of the passenger
)
