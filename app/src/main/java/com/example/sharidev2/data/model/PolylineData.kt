package com.example.sharidev2.data.model

import com.google.android.gms.maps.model.Polyline
import com.google.maps.model.DirectionsLeg


class PolylineData(
    var polyline: Polyline,
    var leg: DirectionsLeg
) {

    override fun toString(): String {
        return "PolylineData{" +
                "polyline=" + polyline +
                ", leg=" + leg +
                '}'
    }
}