package com.example.sharidev2.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RideOption(
    val driverGender: Gender? = null,
    val vehicleType: VehicleType? = null,
    val petFriendly: Boolean? = false,
) : Parcelable
