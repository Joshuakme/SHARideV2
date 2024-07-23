package com.example.sharide.data.repository

import android.util.Log
import com.example.sharide.data.model.PopularLocation
import com.google.android.libraries.places.api.model.Place
import com.google.maps.model.LatLng

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PopularLocationRepository {
    suspend fun getPopularLocation(location: LatLng, fields: List<Place.Field>, placeTypeList: List<Place.Type>): List<PopularLocation> {
        return withContext(Dispatchers.IO) {
            try {
                placeTypeList.forEach {placeType ->



                }


                emptyList<PopularLocation>()
            } catch (e: Exception) {
                Log.e("PopularLocationRepository", e.message.toString())
                emptyList<PopularLocation>()
            }
        }
    }


}