package com.example.sharidev2.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class DirectionsResponse(
    @SerializedName("routes")
    val routes: List<Route>
)

data class Route(
    @SerializedName("overview_polyline")
    val polyline: Polyline
)

@Parcelize
data class Polyline(
    @SerializedName("points")
    val points: String
) : Parcelable