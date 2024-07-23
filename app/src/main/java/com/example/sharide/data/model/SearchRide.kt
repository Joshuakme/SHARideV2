package com.example.sharide.data.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class SearchRide(
    val origin: SearchLocation,
    val destination: SearchLocation,
    val datetime: Timestamp,
    val rideOption: RideOption,
)  : Parcelable
