package com.example.sharidev2.model

import android.graphics.Bitmap

data class Country(
    val name: String ?= null,
    val countryCode: String ?= null,
    val flag: Bitmap ?= null
)
