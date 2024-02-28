package com.example.sharidev2.data.model

import com.google.firebase.Timestamp

data class Review(
    val reviewID: String? = null,
    val reviewer: String,       // Will be replaced to "User" class
    val reviewedUser: String,   // Will be replaced to "User" class
    val rating: Float,
    val comment: String,
    val dateTime: Timestamp
)
