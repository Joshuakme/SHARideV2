package com.example.sharidev2.data.model

import com.google.firebase.auth.FirebaseUser

data class Passenger(
    val user: FirebaseUser,
    val location: Location // Location of the passenger
)
