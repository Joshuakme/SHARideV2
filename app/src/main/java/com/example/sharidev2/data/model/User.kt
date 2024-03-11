package com.example.sharidev2.data.model

import android.net.Uri
import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val uid: String? = null,
    val displayName: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val photoUri: Uri? = null,
    val rideOption: RideOption? = RideOption(),
    val rating: Double ?= null,
    val savedAddress: Map<String, SearchLocation>? = mapOf(),
    val gender: Gender ?= null,
    val joinedDate: Timestamp? = null
) : Parcelable
