package com.example.sharidev2.data.model

import com.google.gson.annotations.SerializedName

data class DirectionsResponse(
    @SerializedName("routes")
    val routes: List<Route>
)

data class Route(
    @SerializedName("overview_polyline")
    val polyline: Polyline
)

data class Polyline(
    @SerializedName("points")
    val points: String
)