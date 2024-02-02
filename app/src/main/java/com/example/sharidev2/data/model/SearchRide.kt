package com.example.sharidev2.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.LocalTime

@Parcelize
data class SearchRide(
    val origin: SearchLocation,
    val destination: SearchLocation,
    val date: LocalDate,
    val time: LocalTime,
    val driverGender: Gender?,
    val vehicleType: VehicleType?
)  : Parcelable
