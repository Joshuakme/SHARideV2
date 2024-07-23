package com.example.sharide.data.model

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class UserLocation(
    val location: LatLng? = null,
    var user: User? = null,
    val timestamp: Date? = null
): Parcelable
