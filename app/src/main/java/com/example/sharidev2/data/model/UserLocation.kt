package com.example.sharidev2.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import com.google.android.gms.maps.model.LatLng
import java.util.Date

data class UserLocation(
    val location: LatLng? = null,
    var user: User? = null,
    @ServerTimestamp val timestamp: Date? = null
)
