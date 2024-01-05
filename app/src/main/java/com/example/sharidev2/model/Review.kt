package com.example.sharidev2.model

import java.time.LocalDateTime

data class Review(
    val reviewID: String,
    val reviewer: String,       // Will be replaced to "User" class
    val reviewedUser: String,   // Will be replaced to "User" class
    val rating: Float,
    val comment: String,
    val dateTime: LocalDateTime
) {
    public fun leaveReview() {
        // TODO: Leave Review function
    }
}
