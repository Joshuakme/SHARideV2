package com.example.sharide.data.model

import com.google.android.libraries.places.api.model.Place
import com.google.gson.annotations.SerializedName


data class PopularLocationResponse(
    @SerializedName("results")
    val places: List<Place>? = null
)