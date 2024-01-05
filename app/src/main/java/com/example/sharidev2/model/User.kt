package com.example.sharidev2.model

import com.google.firebase.database.IgnoreExtraProperties
import java.time.LocalDateTime

@IgnoreExtraProperties
data class User(
    val uid: String? = null,
    val username: String? = null,
    val email: String? = null,
    val specialAttribute: String? = null,
    // Add other properties as needed
    val rating: Float,
    val savedAddresses: MutableList<Address>,
    val gender: String,         // Enum of gender
    val joinedDate: LocalDateTime
)
