package com.example.sharidev2.data.model

import android.net.Uri
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.IgnoreExtraProperties
import java.time.LocalDateTime

data class User(
    val uid: String? = null,
    val displayName: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val photoUrl: Uri? = null,
    val rideOption: RideOption? = RideOption(),
    val rating: Double ?= null,
    val savedAddress: Map<String, SearchLocation>? = mapOf(),
    val gender: Gender ?= null,
    val joinedDate: Timestamp? = null
) {

}
