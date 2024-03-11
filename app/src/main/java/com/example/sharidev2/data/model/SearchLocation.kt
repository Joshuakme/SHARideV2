package com.example.sharidev2.data.model

import android.content.Context
import android.location.Location.distanceBetween
import android.os.Parcelable
import com.example.sharidev2.R
import com.example.sharidev2.utility.Converters
import com.google.android.gms.maps.model.LatLng
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class SearchLocation(
    val placeId: String? = null,
    val name: String = "",
    val distanceMetersFromOrigin: Int = 0,
    val detailAddress: String = "",
    var geolocation: LatLng? = null
)  : Parcelable {
    @IgnoredOnParcel
    private val SEARCH_RADIUS_METER: Double = 100.0

    fun overlaps(other: SearchLocation): Boolean {
        // Calculate the distance between the two locations using Haversine formula
        val result = FloatArray(1)
        distanceBetween(
            geolocation?.latitude ?: 0.0,
            geolocation?.longitude ?: 0.0,
            other.geolocation?.latitude ?: 0.0,
            other.geolocation?.longitude ?: 0.0,
            result)

        val distance = result[0]

        return distance <= SEARCH_RADIUS_METER // adjust radius as needed
    }

    fun getDistanceAddressText(context: Context): String {
        val distanceInKiloMeters = Converters.metersToKiloMeters(distanceMetersFromOrigin)

        if(distanceInKiloMeters.toInt() == 0) {
            return context.getString(
                R.string.search_fragment_search_result_place_distance_address_0km,
                distanceInKiloMeters.toInt(),
                detailAddress
            )
        } else {
            return context.getString(
                R.string.search_fragment_search_result_place_distance_address,
                distanceInKiloMeters,
                detailAddress
            )
        }
    }
}
