package com.example.sharidev2.data.model

import android.net.Uri
import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class VehicleDoc(
    val userUid: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val vehicleType: String? = null,
    val vehicleModel: String? = null,
    val carPlate: String? = null,
    val manufactureDate: Timestamp? = null,
    val vehicleRegisCert: String? = null,
    val roadtax: String? = null,
    val insurance: String? = null,
    val vehicleId: String? = null
) : Parcelable

