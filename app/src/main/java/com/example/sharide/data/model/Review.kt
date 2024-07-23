package com.example.sharide.data.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class Review(
    val reviewID: String? = null,
    val reviewer: String? = null,       // Will be replaced to "User" class
    val reviewedUser: String? = null,   // Will be replaced to "User" class
    val rating: Float? = null,
    val comment: String? = "",
    val dateTime: Timestamp? = Timestamp.now()
) : Parcelable
