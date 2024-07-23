package com.example.sharide.data.state

import java.time.LocalDate
import java.time.LocalTime

data class SearchRideDetailConfigurationState(
    val driverGender: String? = null,
    val vehicleType: String? = null,
    val date: LocalDate ?= null,
    val time: LocalTime ?= null
)