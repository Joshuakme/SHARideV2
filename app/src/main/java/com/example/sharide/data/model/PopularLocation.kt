package com.example.sharide.data.model

import android.net.Uri

data class PopularLocation(
    val title: String,
    val distanceFromOrigin: Double = 0.0,
    val thumbnail: Uri? = null
)
