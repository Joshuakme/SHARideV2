package com.example.sharidev2.data.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.LocalTime

@Parcelize
data class SearchRide(
    val origin: SearchLocation,
    val destination: SearchLocation,
    val datetime: Timestamp,
    val rideOption: RideOption,
)  : Parcelable
