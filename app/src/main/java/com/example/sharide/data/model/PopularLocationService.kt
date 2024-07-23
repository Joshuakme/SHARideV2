package com.example.sharide.data.model

import com.google.android.libraries.places.api.model.Place
import com.google.maps.PlacesApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class PopularLocationService(var placeApi: PlacesApi?) {
    val baseUrl = "https://maps.googleapis.com/maps/api"
    constructor() : this(null) {
        // constructor body
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        placeApi = retrofit.create<PlacesApi>(PlacesApi::class.java)
    }




    interface PopularLocationCallback {
        fun onSuccess(places: List<Place?>?)
        fun onError(message: String?)
    }

}