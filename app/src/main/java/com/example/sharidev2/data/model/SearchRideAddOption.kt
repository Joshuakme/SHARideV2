package com.example.sharidev2.data.model

import java.time.LocalDate
import java.time.LocalTime

data class SearchRideAddOption(
    val driverGender: String? = null,
    val vehicleType: String? = null,
    val date: LocalDate ?= null,
    val time: LocalTime?= null
)
