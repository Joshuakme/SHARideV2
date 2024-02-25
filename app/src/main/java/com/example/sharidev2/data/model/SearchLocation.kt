package com.example.sharidev2.data.model

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import com.example.sharidev2.R
import com.example.sharidev2.utility.Converters
import com.google.android.gms.maps.model.LatLng
import kotlinx.parcelize.Parcelize

@Parcelize
data class SearchLocation(
    val placeId: String = "",
    val name: String = "",
    val distanceMetersFromOrigin: Int = 0,
    val detailAddress: String = "",
    var geolocation: LatLng? = null
)  : Parcelable {

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
