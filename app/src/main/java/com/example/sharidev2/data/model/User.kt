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
    val photoUrl: Uri? = null,
    val rideOption: RideOption? = RideOption(),
    val rating: Double? = 0.0,
    val savedAddress: Map<String, SearchLocation>? = mapOf(),
    val gender: Gender? = null,
    val fcmToken: String? = null,
    val joinedDate: Timestamp? = null
) : Parcelable
