package com.example.sharidev2.data.model

import com.google.android.gms.maps.model.LatLng

data class Location(
    val latitude: Double,
    val longitude: Double,
    val address: String
) {
    fun toLatLng(): LatLng {
        return LatLng(latitude, longitude)
    }
}

