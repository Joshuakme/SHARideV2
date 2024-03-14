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
    val vehicleRegisCert: List<Uri>? = null,
    val roadtax: List<Uri>? = null,
    val insurance: List<Uri>? = null,
    val vehicleId: String? = null
) : Parcelable

