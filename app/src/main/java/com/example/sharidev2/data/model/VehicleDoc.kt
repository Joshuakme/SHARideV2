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
    val manufactureDate: Timestamp? = null,
    val vehicleId: String? = null,
    val vehicleRegisCert: Uri? = null,
    val roadtax: Uri? = null,
    val insurance: Uri? = null
) : Parcelable

